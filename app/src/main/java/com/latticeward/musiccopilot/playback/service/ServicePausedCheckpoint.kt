package com.latticeward.musiccopilot.playback.service

import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.restore.SafePausedRestorePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * P11: non-authoritative persistence adapter owned by the single service.
 *
 * Reads are separate from writes: the service first tries a validated paused
 * restore, and only THEN enables writes. No startup empty snapshot can erase
 * the previous checkpoint. Source URI, title, album, artist and audio never
 * enter this store; only stable opaque MediaStore re-query references.
 *
 * Atomicity is supplied by the one Proto DataStore instance in app.di.
 * The mutex serializes commits, a generation fences late/stale writes.
 */
internal class ServicePausedCheckpoint(
    private val store: RestorableQueueStore,
    private val scope: CoroutineScope,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    private val mutex = Mutex()
    private var generation = 0L
    private var enabled = false
    private var suppressedByStop = false
    private var previousQueue: List<com.latticeward.musiccopilot.core.model.MediaRef>? = null
    private var previousIndex: Int? = null
    private var previousPositionMs: Long? = null
    private var previousPaused: Boolean? = null

    suspend fun read() = store.read()

    /** Enable only after restore decision or an explicit accepted command. */
    fun enable() {
        enabled = true
    }

    fun onExplicitCommand() {
        enabled = true
        suppressedByStop = false
    }

    fun onStop() {
        enabled = true
        suppressedByStop = true
        ++generation
        scheduleClear()
    }

    fun onSnapshot(snapshot: PlaybackSnapshot) {
        if (!enabled || suppressedByStop) return
        if (snapshot.queue.isEmpty()) {
            if (previousQueue?.isNotEmpty() == true) scheduleClear()
            previousQueue = emptyList()
            previousIndex = null
            previousPositionMs = null
            previousPaused = null
            return
        }
        val changed = previousQueue != snapshot.queue ||
            previousIndex != snapshot.currentIndex
        val paused = snapshot.playbackState == PlaybackState.Paused
        val newlyPaused = paused && previousPaused != true
        val pausedPositionChanged = paused && previousPositionMs != snapshot.positionMs
        previousQueue = snapshot.queue.toList()
        previousIndex = snapshot.currentIndex
        previousPositionMs = snapshot.positionMs
        previousPaused = paused
        if (snapshot.playbackState == PlaybackState.Error ||
            snapshot.lastError != null
        ) return
        // A user seeking while already Paused changes the saved position.
        // Playing progress is intentionally debounced to the 5s ticker.
        if (changed || newlyPaused || pausedPositionChanged) save(snapshot)
    }

    /** Called by service-owned 5s ticker while ExoPlayer is really playing. */
    fun onPlayingCheckpoint(snapshot: PlaybackSnapshot) {
        if (enabled && !suppressedByStop &&
            snapshot.playbackState == PlaybackState.Playing
        ) save(snapshot)
    }

    private fun scheduleClear() {
        val myGeneration = ++generation
        scope.launch {
            mutex.withLock {
                if (myGeneration != generation) return@withLock
                try { store.clear() }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* Never leak storage errors into audio. */ }
            }
        }
    }

    private fun save(snapshot: PlaybackSnapshot) {
        val index = snapshot.currentIndex ?: return
        if (index !in snapshot.queue.indices ||
            snapshot.queue.size > 5_000
        ) return
        // Runtime is allowed to be Playing. Persist *only* a Safe-Paused
        // restoration hint; playWhenReady is not in the Proto schema.
        val saved = RestorableQueueSnapshot(
            schemaVersion = SafePausedRestorePolicy.SCHEMA_VERSION,
            queueRevision = snapshot.queueRevision,
            items = snapshot.queue.toList(),
            currentIndex = index,
            positionMs = snapshot.positionMs.coerceAtLeast(0L),
            capturedAtEpochMs = nowMs().coerceAtLeast(0L),
        )
        val myGeneration = ++generation
        scope.launch {
            mutex.withLock {
                if (myGeneration != generation) return@withLock
                try { store.write(saved) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { /* Non-authoritative P1 aid only. */ }
            }
        }
    }
}
