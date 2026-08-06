package com.latticeward.musiccopilot.spike.playback

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.latticeward.musiccopilot.spike.restore.AtomicSnapshotStore
import com.latticeward.musiccopilot.spike.restore.PausedSnapshot
import com.latticeward.musiccopilot.spike.restore.SnapshotDecodeResult
import com.latticeward.musiccopilot.spike.restore.SnapshotCodec
import com.latticeward.musiccopilot.spike.trace.SpikeTrace
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SpikePlaybackService : MediaSessionService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val snapshotMutex = Mutex()
    private val queueRevision = AtomicLong(0)
    private val sessionNonce = UUID.randomUUID().toString().take(8)

    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private lateinit var snapshotStore: AtomicSnapshotStore
    private var checkpointJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        snapshotStore = AtomicSnapshotStore(File(filesDir, "spike-paused-snapshot.v1"))
        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(SessionCallback())
            .build()

        player.addListener(
            object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    SpikeTrace.event(
                        code = "PLAYER_STATE",
                        "playing" to isPlaying,
                        "items" to player.mediaItemCount,
                        "session" to sessionNonce,
                    )
                    checkpoint(if (isPlaying) "PLAYING" else "PAUSED")
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (mediaItem != null) queueRevision.incrementAndGet()
                    SpikeTrace.event(
                        code = "MEDIA_TRANSITION",
                        "reason" to reason,
                        "items" to player.mediaItemCount,
                        "session" to sessionNonce,
                    )
                    checkpoint(if (player.isPlaying) "PLAYING" else "PAUSED")
                }
            },
        )

        SpikeTrace.event(
            code = "SERVICE_CREATED",
            "session" to sessionNonce,
            "player" to System.identityHashCode(player),
        )
        serviceScope.launch { restorePausedSnapshot() }
        checkpointJob = serviceScope.launch {
            while (isActive) {
                delay(CHECKPOINT_INTERVAL_MS)
                checkpoint(if (player.isPlaying) "PLAYING" else "PAUSED")
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        SpikeTrace.event(
            code = "TASK_REMOVED",
            "playing" to player.isPlaying,
            "session" to sessionNonce,
        )
        // Deliberately do not stop the service. OEM behavior is captured by TS-02.
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        SpikeTrace.event(
            code = "SERVICE_DESTROYED",
            "session" to sessionNonce,
            "items" to player.mediaItemCount,
        )
        checkpointJob?.cancel()
        mediaSession.release()
        player.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun checkpoint(capturedState: String) {
        if (!::player.isInitialized || player.mediaItemCount == 0) return
        serviceScope.launch {
            snapshotMutex.withLock {
                val references = (0 until player.mediaItemCount).mapNotNull { index ->
                    player.getMediaItemAt(index).localConfiguration?.uri?.toString()
                }
                if (references.size != player.mediaItemCount) return@withLock
                val currentIndex = player.currentMediaItemIndex.coerceIn(references.indices)
                snapshotStore.write(
                    PausedSnapshot(
                        schemaVersion = SnapshotCodec.CURRENT_SCHEMA_VERSION,
                        queueRevision = queueRevision.incrementAndGet(),
                        queueReferences = references,
                        currentIndex = currentIndex,
                        positionMs = player.currentPosition.coerceAtLeast(0),
                        capturedState = capturedState,
                        savedAtEpochMs = System.currentTimeMillis(),
                    ),
                )
                SpikeTrace.event(
                    code = "SNAPSHOT_SAVED",
                    "revision" to queueRevision.get(),
                    "items" to references.size,
                    "captured" to capturedState,
                )
            }
        }
    }

    private suspend fun restorePausedSnapshot() {
        when (val decoded = snapshotStore.read()) {
            null -> SpikeTrace.event(code = "RESTORE_NONE")
            is SnapshotDecodeResult.Invalid -> {
                snapshotStore.clear()
                SpikeTrace.event(code = "RESTORE_REJECTED", "reason" to decoded.reason)
            }
            is SnapshotDecodeResult.Valid -> restoreValidated(decoded.snapshot)
        }
    }

    private suspend fun restoreValidated(snapshot: PausedSnapshot) {
        if (player.mediaItemCount > 0 || player.isPlaying) {
            SpikeTrace.event(code = "RESTORE_LIVE_SESSION_WINS")
            return
        }

        val surviving = withContext(Dispatchers.IO) {
            snapshot.queueReferences.mapIndexedNotNull { originalIndex, reference ->
                val uri = Uri.parse(reference)
                val readable = runCatching {
                    contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
                }.getOrDefault(false)
                if (!readable) return@mapIndexedNotNull null
                val durationMs = runCatching {
                    MediaMetadataRetriever().use { retriever ->
                        retriever.setDataSource(this@SpikePlaybackService, uri)
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
                    }
                }.getOrNull()
                RestorableItem(originalIndex, uri, durationMs)
            }
        }
        if (surviving.isEmpty()) {
            snapshotStore.clear()
            SpikeTrace.event(code = "RESTORE_NO_SURVIVORS")
            return
        }

        val exactIndex = surviving.indexOfFirst { it.originalIndex == snapshot.currentIndex }
        val index = if (exactIndex >= 0) exactIndex else {
            surviving.indexOfFirst { it.originalIndex > snapshot.currentIndex }
                .takeIf { it >= 0 }
                ?: surviving.lastIndex
        }
        val restoredPosition = if (exactIndex >= 0) {
            val verifiedDuration = surviving[index].durationMs
            if (verifiedDuration != null && verifiedDuration >= 0) {
                snapshot.positionMs.coerceAtMost(verifiedDuration)
            } else {
                0L
            }
        } else {
            0L
        }
        queueRevision.set(snapshot.queueRevision)
        player.setMediaItems(
            surviving.map { MediaItem.Builder().setUri(it.uri).build() },
            index,
            restoredPosition,
        )
        player.prepare()
        player.playWhenReady = false
        SpikeTrace.event(
            code = "RESTORE_PAUSED",
            "revision" to snapshot.queueRevision,
            "items" to surviving.size,
            "index" to index,
        )
    }

    private inner class SessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            SpikeTrace.event(code = "CONTROLLER_CONNECTED", "session" to sessionNonce)
            return super.onConnect(session, controller)
        }
    }

    private companion object {
        const val CHECKPOINT_INTERVAL_MS = 5_000L
    }

    private data class RestorableItem(
        val originalIndex: Int,
        val uri: Uri,
        val durationMs: Long?,
    )
}
