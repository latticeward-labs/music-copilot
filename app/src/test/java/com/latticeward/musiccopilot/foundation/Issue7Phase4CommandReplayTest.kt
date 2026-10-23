package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** P04 processor replay invariants; no Android Player instantiated. */
class Issue7Phase4CommandReplayTest {
    private fun snapshot(revision: Long) = PlaybackSnapshot(
        revision = revision,
        queueRevision = revision,
        playbackState = PlaybackState.Paused,
        queue = listOf(MediaRef("audio:1")),
        currentIndex = 0,
        currentItem = MediaRef("audio:1"),
        positionMs = 0L,
        durationMs = 100L,
        canSeek = true,
        canSkipPrevious = false,
        canSkipNext = false,
    )

    @Test fun duplicatePendingCommandReusesOneDeferredAndExecutesOnce() = runBlocking {
        val job = SupervisorJob()
        val release = CompletableDeferred<Unit>()
        var executions = 0
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            executions++
            release.await()
            AppResult.Success(snapshot(executions.toLong()))
        }
        try {
            val intent = PlaybackCommand.Pause(CommandId("one"))
            val first = worker.submit(intent)
            val retry = worker.submit(intent.copy())
            assertSame(first, retry)
            assertEquals(1, executions)
            release.complete(Unit)
            assertTrue(first.await() is AppResult.Success)
            assertTrue(retry.await() is AppResult.Success)
            assertEquals(1, executions)
        } finally { worker.close(); job.cancel() }
    }

    @Test fun reusedIdWithDifferentPayloadFailsAndDoesNotExecute() = runBlocking {
        val job = SupervisorJob()
        var executions = 0
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            executions++
            AppResult.Success(snapshot(executions.toLong()))
        }
        try {
            val id = CommandId("shared")
            assertTrue(worker.submit(PlaybackCommand.Play(id)).await() is AppResult.Success)
            val conflict = worker.submit(PlaybackCommand.Pause(id)).await()
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
                (conflict as AppResult.Failure).error.code)
            assertEquals(1, executions)
        } finally { worker.close(); job.cancel() }
    }

    @Test fun completedCommandRetryReturnsExactAcknowledgedSnapshot() = runBlocking {
        val job = SupervisorJob()
        var visited = 0
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            visited++
            AppResult.Success(snapshot(17L))
        }
        try {
            val command = PlaybackCommand.Next(CommandId("next"))
            val first = worker.submit(command)
            assertEquals(first, worker.submit(command))
            assertEquals(17L, (first.await() as AppResult.Success).value.revision)
            assertEquals(1, visited)
        } finally { worker.close(); job.cancel() }
    }

    @Test fun replayCacheIsBoundedAndDoesNotBreakDistinctRequests() = runBlocking {
        val job = SupervisorJob()
        var visited = 0
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            visited++
            AppResult.Success(snapshot(visited.toLong()))
        }
        try {
            for (i in 1..300) {
                val result = worker.submit(
                    PlaybackCommand.Pause(CommandId("request-$i"))
                ).await()
                assertTrue(result is AppResult.Success)
            }
            assertEquals(300, visited)
            assertTrue(worker.submit(
                PlaybackCommand.Pause(CommandId("request-300"))
            ).await() is AppResult.Success)
            assertEquals(300, visited)
        } finally { worker.close(); job.cancel() }
    }

    @Test fun queuedDuplicateDoesNotConsumeAnAdditionalWaitingSlot() = runBlocking {
        val job = SupervisorJob()
        val release = CompletableDeferred<Unit>()
        var visited = 0
        val worker = PlaybackCommandProcessor(CoroutineScope(job + Dispatchers.Unconfined)) {
            visited++
            if (visited == 1) release.await()
            AppResult.Success(snapshot(visited.toLong()))
        }
        try {
            val active = PlaybackCommand.Play(CommandId("active"))
            val first = worker.submit(active)
            val same = worker.submit(active)
            assertSame(first, same)
            val queued = (1..32).map {
                worker.submit(PlaybackCommand.Pause(CommandId("pending-$it")))
            }
            val busy = worker.submit(PlaybackCommand.Next(CommandId("over"))).await()
            assertEquals(AppErrorCodes.PLAYBACK_BUSY,
                (busy as AppResult.Failure).error.code)
            release.complete(Unit)
            assertTrue(first.await() is AppResult.Success)
            queued.forEach { assertTrue(it.await() is AppResult.Success) }
            assertEquals(33, visited)
        } finally { worker.close(); job.cancel() }
    }
}
