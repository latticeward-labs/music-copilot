package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.controller.ControllerSnapshotRelay
import com.latticeward.musiccopilot.playback.media3.ServicePlaybackRuntimeOwner
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Phase7PlaybackFoundationTest {
    private fun cmd(n: Int) = PlaybackCommand.Pause(CommandId("synthetic-$n"))

    private fun confirmed(
        revision: Long,
        error: AppError? = null
    ) = PlaybackSnapshot(
        revision = revision, queueRevision = revision,
        playbackState = if (error != null) PlaybackState.Error else PlaybackState.Paused,
        queue = listOf(MediaRef("audio:1")), currentIndex = 0,
        currentItem = MediaRef("audio:1"), positionMs = 1200,
        durationMs = 9000, canSeek = true,
        canSkipPrevious = false, canSkipNext = false, lastError = error
    )

    @Test fun serviceRuntimeFactoryIsSingleStartAndCloseIsIdempotent() {
        var created = 0
        var disposed = 0
        val factory = ServicePlaybackRuntimeOwner(
            create = { ++created; "synthetic" },
            dispose = { disposed++ }
        )
        assertEquals("synthetic", factory.start())
        var duplicateRejected = false
        try { factory.start() } catch (_: IllegalStateException) { duplicateRejected = true }
        assertTrue(duplicateRejected)
        assertEquals(1, created)
        factory.close()
        factory.close()
        assertEquals(1, disposed)
        var postCloseRejected = false
        try { factory.start() } catch (_: IllegalStateException) { postCloseRejected = true }
        assertTrue(postCloseRejected)
    }

    @Test fun boundedCapacityIsExactly32WaitingAndOverloadIsTypedBusy() = runBlocking {
        val job = SupervisorJob()
        val owner = CoroutineScope(job + Dispatchers.Unconfined)
        val releaseFirst = CompletableDeferred<Unit>()
        val visited = mutableListOf<String>()
        val worker = PlaybackCommandProcessor(owner) { c ->
            visited += c.commandId.value
            if (visited.size == 1) releaseFirst.await()
            AppResult.Success(confirmed(visited.size.toLong()))
        }
        try {
            val inFlight = worker.submit(cmd(0))
            val pending = (1..32).map { worker.submit(cmd(it)) }
            val overflow = worker.submit(cmd(33)).await()
            assertTrue(overflow is AppResult.Failure)
            assertEquals(AppErrorCodes.PLAYBACK_BUSY,
                (overflow as AppResult.Failure).error.code)
            assertTrue(inFlight.isActive)
            assertEquals(1, visited.size)
            releaseFirst.complete(Unit)
            assertTrue(inFlight.await() is AppResult.Success<*>)
            pending.forEach { assertTrue(it.await() is AppResult.Success<*>) }
            assertEquals((0..32).map { "synthetic-$it" }, visited)
        } finally { worker.close(); job.cancel() }
    }

    @Test fun serviceDestructionCompletesQueuedAndActiveCommandsWithoutSuccess() = runBlocking {
        val job = SupervisorJob()
        val gate = CompletableDeferred<Unit>()
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            gate.await()
            AppResult.Success(confirmed(7))
        }
        try {
            val active = worker.submit(cmd(1))
            val queued = worker.submit(cmd(2))
            worker.close()
            assertTrue(active.await() is AppResult.Failure)
            assertTrue(queued.await() is AppResult.Failure)
            val afterClose = worker.submit(cmd(3)).await()
            assertTrue(afterClose is AppResult.Failure)
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
                (afterClose as AppResult.Failure).error.code)
        } finally { gate.cancel(); job.cancel() }
    }

    @Test fun handlerExceptionsAreSanitizedAndNeverAcknowledged() = runBlocking {
        val job = SupervisorJob()
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            throw IllegalArgumentException("synthetic sensitive path / do not leak")
        }
        try {
            val result = worker.submit(cmd(1)).await()
            assertTrue(result is AppResult.Failure)
            assertEquals(AppErrorCodes.PLAYBACK_FAILED,
                (result as AppResult.Failure).error.code)
            assertFalse(result.error.code.contains("/"))
        } finally { worker.close(); job.cancel() }
    }

    @Test fun controllerRequiresCompleteSnapshotBeforeReadyAndRecreationKeepsAuthority() {
        val owner = ControllerSnapshotRelay()
        val oldScreen = owner.snapshot
        owner.beginConnect()
        assertEquals(PlaybackConnectionState.Connecting, owner.connectionState.value)
        assertEquals(0L, owner.snapshot.value.revision)
        assertTrue(owner.publish(confirmed(5), isInitialAfterConnect = true))
        assertEquals(PlaybackConnectionState.Ready, owner.connectionState.value)
        val newScreen = owner.snapshot
        assertSame(oldScreen, newScreen)
        assertEquals(5L, newScreen.value.revision)
        assertFalse(owner.publish(confirmed(4), isInitialAfterConnect = false))
        assertEquals(5L, owner.snapshot.value.revision)
        owner.failed()
        assertEquals(PlaybackConnectionState.RecoverableError, owner.connectionState.value)
        assertEquals(5L, owner.snapshot.value.revision)
        owner.beginConnect()
        // A reconnected service may legitimately restart its monotonic sequence.
        assertTrue(owner.publish(confirmed(1), isInitialAfterConnect = true))
        assertEquals(1L, newScreen.value.revision)
        assertEquals(PlaybackConnectionState.Ready, owner.connectionState.value)
    }

    @Test fun errorProjectionIsRestrictedToServiceOwnedFamilies() {
        val playbackError = AppError.Playback(AppErrorCodes.PLAYBACK_FAILED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry)
        val itemError = AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_MISSING,
            Recoverability.RECOVERABLE, RecoveryAction.Skip, MediaRef("audio:1"))
        assertSame(playbackError, confirmed(1, playbackError).lastError)
        assertSame(itemError, confirmed(2, itemError).lastError)
        var rejected = false
        try {
            confirmed(3, AppError.Controller(AppErrorCodes.CONTROLLER_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
        } catch (_: IllegalArgumentException) { rejected = true }
        assertTrue(rejected)
    }

    @Test fun serviceAndActivityOwnershipCannotBeInvertedInProductionFiles() {
        val root = java.io.File("src/main/java/com/latticeward/musiccopilot")
        fun source(path: String) = java.io.File(root, path).readText()
        val service = source("playback/service/MusicPlaybackService.kt")
        val controller = source("playback/controller/PlaybackControllerConnection.kt")
        val activity = source("app/MainActivity.kt")
        val graph = source("app/di/ApplicationBindings.kt")
        assertTrue(service.contains("class MusicPlaybackService : MediaSessionService()"))
        assertEquals(1, Regex("ExoPlayer\\.Builder\\(").findAll(service).count())
        assertEquals(1, Regex("MediaSession\\.Builder\\(").findAll(service).count())
        for (text in listOf(controller, activity, graph)) {
            assertFalse(text.contains("ExoPlayer.Builder("))
            assertFalse(text.contains("MediaSession.Builder("))
            assertFalse(text.contains("PlaybackCommandProcessor("))
        }
        assertTrue(service.contains("processor.close()"))
        assertTrue(service.contains("runtimeOwner.close()"))
        assertTrue(service.contains("s.release()"))
        assertTrue(service.contains("p.release()"))
    }
}

