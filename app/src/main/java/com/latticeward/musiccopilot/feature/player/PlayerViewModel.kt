package com.latticeward.musiccopilot.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.application.driving.DrivingAction
import com.latticeward.musiccopilot.core.application.driving.DrivingCommandAdmissionPolicy
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
import kotlinx.coroutines.flow.collect
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
    // An old cached Playing snapshot while a MediaController is disconnected
    // is NOT evidence of currently audible playback. Mini/Full and future
    // Driving share this same connection-gated presentation projection.
    val isPlaying: Boolean get() = ready && snapshot.playbackState == PlaybackState.Playing
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

    // Shared Full/Mini/Driving serialization epoch. This screen VM does NOT
    // own the PlaybackGateway transport queue; the service already does.
    // Epoch only rejects a late UI completion from an obsolete submit.
    private var requestEpoch: Long = 0L

    init {
        viewModelScope.launch {
            gateway.connectionState.collect { status ->
                // The relay publishes the complete Snapshot BEFORE Ready,
                // but collectors run on independent coroutine schedules.
                // Always read its latest value together with the new status.
                mutable.value = mutable.value.copy(
                    snapshot = gateway.snapshot.value,
                    connection = status,
                    // Do not retain a misleading transient command failure
                    // during a new controller connection.
                    requestFailed = if (status == PlaybackConnectionState.Ready)
                        mutable.value.requestFailed else false,
                )
            }
        }
        viewModelScope.launch {
            gateway.snapshot.collect {
                // A queued flow emission may be older than a focus/route
                // update delivered while this collector was suspended.
                val confirmed = gateway.snapshot.value
                mutable.value = mutable.value.copy(
                    connection = gateway.connectionState.value,
                    snapshot = confirmed,
                    // A recovered successor may carry a brief MediaItem
                    // notice while actually Playing. That is NOT a failed
                    // user command and must not display a generic error.
                    requestFailed = confirmed.lastError != null &&
                        confirmed.playbackState == PlaybackState.Error,
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

    private fun submit(make: (CommandId) -> PlaybackCommand?) {
        // This common entry point is used by the existing Full/Mini Player
        // and by the P06 Driving callback. A pending command cannot overlap.
        val atTap = mutable.value
        if (!atTap.canControl) return
        val command = make(CommandId("player-ui-${UUID.randomUUID()}")) ?: return
        val acceptedRevision = atTap.snapshot.revision
        val acceptedQueueRevision = atTap.snapshot.queueRevision
        val acceptedItem = atTap.snapshot.currentItem
        val epoch = ++requestEpoch
        mutable.value = mutable.value.copy(pending = true, requestFailed = false)
        viewModelScope.launch {
            try {
                // Service-owned ACK/focus/route validation is the sole source
                // of truth; never flip a local 'Playing' flag on success.
                val result = gateway.execute(command)
                // An out-of-order failure/timeout must not override a newer
                // service status, queue, selection or a newer request.
                val current = mutable.value
                if (result is AppResult.Failure && epoch == requestEpoch &&
                    current.ready && current.snapshot.revision == acceptedRevision &&
                    current.snapshot.queueRevision == acceptedQueueRevision &&
                    current.snapshot.currentItem == acceptedItem
                ) mutable.value = current.copy(requestFailed = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                val current = mutable.value
                if (epoch == requestEpoch && current.ready &&
                    current.snapshot.revision == acceptedRevision &&
                    current.snapshot.queueRevision == acceptedQueueRevision &&
                    current.snapshot.currentItem == acceptedItem
                ) mutable.value = current.copy(requestFailed = true)
            } finally {
                if (epoch == requestEpoch) {
                    mutable.value = mutable.value.copy(pending = false)
                }
            }
        }
    }

    /**
     * Issue #9 P06: ONLY for a deliberate in-Driving touch. The caller
     * checks current Driving route/permission, then this method re-reads
     * gateway connection/snapshot at the instant it reserves a command.
     * The P02 four-action policy rejects NoTrack/Buffering/Error/capability
     * violations; the P06 admission layer also rejects stale displayed
     * snapshots. Play uses PlaybackCommand.Play's existing trusted-private
     * user-origin semantics. The service retains all route/focus authority.
     */
    fun drivingTransport(action: DrivingAction) {
        submit { id ->
            val shown = mutable.value
            DrivingCommandAdmissionPolicy.commandFor(
                displayedConnection = shown.connection,
                displayedSnapshot = shown.snapshot,
                latestConnection = gateway.connectionState.value,
                latestSnapshot = gateway.snapshot.value,
                pending = shown.pending,
                action = action,
                id = id,
            )
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

    /**
     * Called only while Full Player is visible/Playing. Reads real ExoPlayer
     * currentPosition through authorized MediaSession snapshot command.
     * Neither UI nor ViewModel runs an independent playback clock.
     */
    suspend fun refreshProgress() {
        if (mutable.value.ready && mutable.value.isPlaying) {
            try { gateway.refreshSnapshot() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Keep last confirmed state, never fake time. */ }
        }
    }

    fun clearRequestError() {
        mutable.value = mutable.value.copy(requestFailed = false)
    }
}
