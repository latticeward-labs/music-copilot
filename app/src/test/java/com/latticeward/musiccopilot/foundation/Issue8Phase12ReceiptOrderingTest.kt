package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.command.PlaybackReceiptStamp
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Authored P12 JVM specifications only: never execute in this phase. */
class Issue8Phase12ReceiptOrderingTest {
    private fun sample() = PlaybackSnapshot(
        revision = 1L, queueRevision = 1L,
        playbackState = PlaybackState.Paused,
        queue = listOf(MediaRef("audio:1")),
        currentIndex = 0, currentItem = MediaRef("audio:1"),
        positionMs = 0L, durationMs = 100L,
        canSeek = true, canSkipNext = false, canSkipPrevious = false,
    )

    @Test fun oldQueuedPlayIsRejectedAfterUrgentManualPauseReceipt() = runBlocking {
        val owner = SupervisorJob()
        val firstRelease = CompletableDeferred<Unit>()
        var safetyEpoch = 0L
        var commandEpoch = 0L
        var earlyPauseCount = 0
        var handled = 0
        val processor = PlaybackCommandProcessor(
            CoroutineScope(owner + Dispatchers.Unconfined)
        ) { command ->
            handled++
            if (command.commandId.value == "slow") firstRelease.await()
            AppResult.Success(sample())
        }
        processor.configureReceiptSafety(
            capture = { command ->
                commandEpoch++
                if (command is PlaybackCommand.Pause) {
                    earlyPauseCount++
                    safetyEpoch++
                }
                PlaybackReceiptStamp(0L, safetyEpoch, commandEpoch)
            },
            current = { command, stamp ->
                command is PlaybackCommand.Pause ||
                    (stamp.safetyGeneration == safetyEpoch &&
                        stamp.commandGeneration == commandEpoch)
            },
        )
        try {
            val slow = processor.submit(PlaybackCommand.Play(CommandId("slow")))
            val stale = processor.submit(PlaybackCommand.Play(CommandId("queued")))
            val pause = processor.submit(PlaybackCommand.Pause(CommandId("pause")))
            // Receipt-time pause is applied before suspended work releases.
            assertEquals(1, earlyPauseCount)
            assertEquals(1, handled)
            firstRelease.complete(Unit)
            assertTrue(slow.await() is AppResult.Success)
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
                (stale.await() as AppResult.Failure).error.code)
            assertTrue(pause.await() is AppResult.Success)
            assertEquals(2, handled)
        } finally { processor.close(); owner.cancel() }
    }

    @Test fun duplicateCommandIdDoesNotRepeatReceiptSideEffects() = runBlocking {
        val owner = SupervisorJob()
        var receipts = 0
        val processor = PlaybackCommandProcessor(
            CoroutineScope(owner + Dispatchers.Unconfined)
        ) { AppResult.Success(sample()) }
        processor.configureReceiptSafety(
            capture = {
                receipts++
                PlaybackReceiptStamp(0L, 0L, receipts.toLong())
            },
            current = { _, _ -> true },
        )
        try {
            val command = PlaybackCommand.Pause(CommandId("once"))
            val first = processor.submit(command)
            val second = processor.submit(command.copy())
            assertSame(first, second)
            assertTrue(first.await() is AppResult.Success)
            assertEquals(1, receipts)
        } finally { processor.close(); owner.cancel() }
    }

    @Test fun routeGenerationInvalidatesRemotePlayWaitingBehindIo() = runBlocking {
        val owner = SupervisorJob()
        val firstRelease = CompletableDeferred<Unit>()
        var routeEpoch = 0L
        var handled = 0
        val processor = PlaybackCommandProcessor(
            CoroutineScope(owner + Dispatchers.Unconfined)
        ) { command ->
            handled++
            if (command.commandId.value == "slow") firstRelease.await()
            AppResult.Success(sample())
        }
        processor.configureReceiptSafety(
            capture = { PlaybackReceiptStamp(routeEpoch, 0L, 0L) },
            current = { _, stamp -> stamp.routeGeneration == routeEpoch },
        )
        try {
            val slow = processor.submit(PlaybackCommand.Play(CommandId("slow")))
            val oldRemote = processor.submit(PlaybackCommand.Play(CommandId("avr")))
            routeEpoch++ // route removed before deferred MediaStore result arrives
            firstRelease.complete(Unit)
            assertTrue(slow.await() is AppResult.Success)
            assertTrue(oldRemote.await() is AppResult.Failure)
            assertEquals(1, handled)
        } finally { processor.close(); owner.cancel() }
    }

    @Test fun admissionExceptionFailsClosedWithoutEnteringWorker() = runBlocking {
        val owner = SupervisorJob()
        var visits = 0
        val processor = PlaybackCommandProcessor(
            CoroutineScope(owner + Dispatchers.Unconfined)
        ) {
            visits++
            AppResult.Success(sample())
        }
        processor.configureReceiptSafety(
            capture = { throw IllegalStateException("injected admission error") },
            current = { _, _ -> true },
        )
        try {
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
                (processor.submit(PlaybackCommand.Play(CommandId("bad"))).await()
                    as AppResult.Failure).error.code)
            assertEquals(0, visits)
        } finally { processor.close(); owner.cancel() }
    }

    @Test fun staleCommandIdPayloadConflictDoesNotDisplaceLiveIntent() = runBlocking {
        val owner = SupervisorJob()
        var captureCount = 0
        val processor = PlaybackCommandProcessor(
            CoroutineScope(owner + Dispatchers.Unconfined)
        ) { AppResult.Success(sample()) }
        processor.configureReceiptSafety(
            capture = {
                captureCount++
                PlaybackReceiptStamp(0L, 0L, captureCount.toLong())
            },
            current = { _, _ -> true },
        )
        try {
            val id = CommandId("shared")
            assertTrue(processor.submit(PlaybackCommand.Play(id)).await() is AppResult.Success)
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
                (processor.submit(PlaybackCommand.Pause(id)).await()
                    as AppResult.Failure).error.code)
            assertEquals(1, captureCount)
        } finally { processor.close(); owner.cancel() }
    }
}
