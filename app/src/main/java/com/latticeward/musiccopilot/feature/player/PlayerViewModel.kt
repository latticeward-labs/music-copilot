package com.latticeward.musiccopilot.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * P07 presentation only. Never constructs/owns Player, Session or a mutable
 * queue. A command is acknowledged only by PlaybackGateway.execute and the
 * actual playback mode is ALWAYS the latest service snapshot.
 */
data class PlayerUiState(
    val connection: PlaybackConnectionState,
    val snapshot: PlaybackSnapshot,
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val pending: Boolean = false,
    val requestFailed: Boolean = false,
) {
    val hasTrack: Boolean get() = snapshot.currentItem != null
    val ready: Boolean get() = connection == PlaybackConnectionState.Ready
    val canControl: Boolean get() = ready && hasTrack && !pending
    val isPlaying: Boolean get() = snapshot.playbackState == PlaybackState.Playing
}

class PlayerViewModel(
    private val gateway: PlaybackGateway,
    private val library: LibraryRepository,
) : ViewModel() {
    private val mutable = MutableStateFlow(PlayerUiState(
        connection = gateway.connectionState.value,
        snapshot = gateway.snapshot.value,
    ))
    val ui: StateFlow<PlayerUiState> = mutable

    init {
        viewModelScope.launch {
            gateway.connectionState.collect { status ->
                mutable.value = mutable.value.copy(
                    connection = status,
                    // Do not retain a misleading transient command failure
                    // during a new controller connection.
                    requestFailed = if (status == PlaybackConnectionState.Ready)
                        mutable.value.requestFailed else false,
                )
            }
        }
        viewModelScope.launch {
            gateway.snapshot.collect { confirmed ->
                mutable.value = mutable.value.copy(
                    snapshot = confirmed,
                    requestFailed = if (confirmed.lastError != null)
                        true else mutable.value.requestFailed,
                )
            }
        }
        // Metadata is DISPLAY-ONLY. Identity comes exclusively from the
        // service-confirmed MediaRef. Rapid skips cancel stale lookups.
        viewModelScope.launch {
            gateway.snapshot.map { it.currentItem }.distinctUntilChanged()
                .collectLatest { selected ->
                    mutable.value = mutable.value.copy(title = null, artist = null, album = null)
                    if (selected == null) return@collectLatest
                    val info = withContext(Dispatchers.IO) { library.get(selected) }
                    if (gateway.snapshot.value.currentItem != selected) return@collectLatest
                    if (info is AppResult.Success && info.value.ref == selected) {
                        mutable.value = mutable.value.copy(
                            title = info.value.title,
                            artist = info.value.artist,
                            album = info.value.album,
                        )
                    }
                    // Missing/revoked/stale metadata silently becomes an
                    // anonymous placeholder, never a previous clicked row.
                }
        }
    }

    private fun submit(make: (CommandId) -> PlaybackCommand) {
        if (!mutable.value.canControl) return
        val command = make(CommandId("player-ui-${UUID.randomUUID()}"))
        mutable.value = mutable.value.copy(pending = true, requestFailed = false)
        viewModelScope.launch {
            try {
                val result = gateway.execute(command)
                // No optimistic service snapshot; collector is sole authority.
                if (result is AppResult.Failure) {
                    mutable.value = mutable.value.copy(requestFailed = true)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutable.value = mutable.value.copy(requestFailed = true)
            } finally {
                mutable.value = mutable.value.copy(pending = false)
            }
        }
    }

    fun togglePlayPause() {
        if (mutable.value.isPlaying) submit { PlaybackCommand.Pause(it) }
        else submit { PlaybackCommand.Play(it) }
    }

    fun next() {
        if (mutable.value.snapshot.canSkipNext) submit { PlaybackCommand.Next(it) }
    }

    fun previous() {
        if (mutable.value.snapshot.canSkipPrevious) submit { PlaybackCommand.Previous(it) }
    }

    fun seekBackTenSeconds() {
        if (mutable.value.snapshot.canSeek)
            submit { PlaybackCommand.SeekBy(it, -10_000L) }
    }

    fun seekTo(positionMs: Long) {
        if (mutable.value.snapshot.canSeek && positionMs >= 0L)
            submit { PlaybackCommand.SeekTo(it, positionMs) }
    }

    fun selectQueueOccurrence(index: Int, observedQueueRevision: Long) {
        val snap = mutable.value.snapshot
        if (index !in snap.queue.indices ||
            observedQueueRevision != snap.queueRevision) return
        submit {
            PlaybackCommand.SelectQueueIndex(it, index, observedQueueRevision, true)
        }
    }

    fun clearRequestError() {
        mutable.value = mutable.value.copy(requestFailed = false)
    }
}
