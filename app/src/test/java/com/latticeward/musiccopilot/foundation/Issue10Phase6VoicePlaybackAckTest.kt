package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy
import com.latticeward.musiccopilot.playback.command.PlaybackReceiptStamp
import com.latticeward.musiccopilot.playback.command.VoiceCommandReceiptPolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.session.VoicePlaybackDispatcher
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * P06 AUTHOR_ONLY — no Gradle, JVM, Robolectric, Android or phone execution.
 * Fake gateways are not proof of a Media3 device ACK; P13 needs phone checks.
 */
class Issue10Phase6VoicePlaybackAckTest {
    private val sid = VoiceSessionId(42)
    private fun snapshot(index: Int = 0, queueCount: Int = 3) = PlaybackSnapshot(
        revision = 8L,
        queueRevision = 2L,
        playbackState = PlaybackState.Paused,
        queue = (0 until queueCount).map { MediaRef("audio:$it") },
        currentIndex = index.takeIf { it in 0 until queueCount },
        currentItem = index.takeIf { it in 0 until queueCount }?.let { MediaRef("audio:$it") },
        positionMs = 500L,
        durationMs = 50_000L,
        canSeek = true,
        canSkipPrevious = index > 0,
        canSkipNext = index < queueCount - 1,
    )

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(
            PlaybackSnapshot(
                8L, 2L, PlaybackState.Paused,
                listOf(MediaRef("audio:0")), 0, MediaRef("audio:0"),
                1_000L, 50_000L, true, false, false,
            )
        )
        val commands = mutableListOf<PlaybackCommand>()
        var reply: AppResult<PlaybackSnapshot> = AppResult.Success(snapshot.value)
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            return reply
        }
    }

    @Test fun preciselyFiveTransportCommandsUseOneDeterministicVoiceIdPerAttempt() {
        // P12: stable *within* one process/attempt, with a process-local
        // nonce so a surviving service cannot replay an older receipt.
        val id = VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, sid, 1)!!.commandId
        assertTrue(VoiceCommandReceiptPolicy.isVoiceCommand(PlaybackCommand.Play(id)))
        assertTrue(id.value.matches(Regex("^voice-[0-9a-f]{32}-42-1$")))
        val expected = listOf(
            RestrictedIntent.PLAY to PlaybackCommand.Play(id),
            RestrictedIntent.PAUSE to PlaybackCommand.Pause(id),
            RestrictedIntent.NEXT to PlaybackCommand.Next(id),
            RestrictedIntent.PREVIOUS to PlaybackCommand.Previous(id),
            RestrictedIntent.SEEK_BACK to PlaybackCommand.SeekBy(id, -10_000L),
        )
        for ((intent, command) in expected) {
            assertEquals(command, VoicePlaybackDispatcher.commandFor(intent, sid, 1))
        }
    }

    @Test fun artistAndMalformedSessionCannotProduceExecutableCommand() {
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY_ARTIST, sid, 1))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, VoiceSessionId(0), 1))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 0))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 3))
    }

    @Test fun secondRecognitionAttemptUsesDifferentIdWithoutSecondActionInFirst() {
        val first = VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1)!!
        val second = VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 2)!!
        assertNotEquals(first.commandId, second.commandId)
        assertEquals(first, VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1))
    }

    @Test fun voiceReceiptMustMatchSubmittedIdExactly() {
        val command = PlaybackCommand.Next(CommandId("voice-42-1"))
        assertTrue(VoiceCommandReceiptPolicy.isVoiceCommand(command))
        assertTrue(VoiceCommandReceiptPolicy.confirms(command, "voice-42-1"))
        assertFalse(VoiceCommandReceiptPolicy.confirms(command, null))
        assertFalse(VoiceCommandReceiptPolicy.confirms(command, "voice-42-2"))
        assertFalse(VoiceCommandReceiptPolicy.confirms(command, "voice-41-1"))
    }

    @Test fun nonVoiceManualCommandsRetainOriginalAckCompatibility() {
        val manual = PlaybackCommand.Pause(CommandId("manual-1"))
        assertFalse(VoiceCommandReceiptPolicy.isVoiceCommand(manual))
        assertTrue(VoiceCommandReceiptPolicy.confirms(manual, null))
        assertFalse(VoiceCommandReceiptPolicy.isVoiceCommand(
            PlaybackCommand.Next(CommandId("voice-artist-Adele"))
        ))
    }

    @Test fun finalQueuePositionAndEmptyQueueHaveNoVoiceNextSuccess() {
        assertTrue(VoiceCommandReceiptPolicy.canAdvance(0, 3))
        assertTrue(VoiceCommandReceiptPolicy.canAdvance(1, 3))
        assertFalse(VoiceCommandReceiptPolicy.canAdvance(2, 3))
        assertFalse(VoiceCommandReceiptPolicy.canAdvance(0, 1))
        assertFalse(VoiceCommandReceiptPolicy.canAdvance(-1, 1))
        assertFalse(VoiceCommandReceiptPolicy.canAdvance(0, 0))
    }

    @Test fun coherentServiceSnapshotIsRequiredForQueueIdentity() {
        assertTrue(VoiceCommandReceiptPolicy.hasCoherentQueue(snapshot()))
        assertFalse(VoiceCommandReceiptPolicy.hasCoherentQueue(snapshot(index = 0, queueCount = 0)))
        assertFalse(VoiceCommandReceiptPolicy.hasCoherentQueue(snapshot(index = -1, queueCount = 2)))
    }

    @Test fun tenSecondRelativeSeekIsClampedByExistingServicePolicy() {
        assertEquals(0L, PlaybackNavigationPolicy.seekBack(5_000L, 60_000L, true))
        assertEquals(2_000L, PlaybackNavigationPolicy.seekBack(12_000L, 60_000L, true))
        assertEquals(0L, PlaybackNavigationPolicy.seekBack(0L, null, true))
        assertNull(PlaybackNavigationPolicy.seekBack(9_000L, 60_000L, false))
    }

    @Test fun disconnectedPortDoesNotSendAnUnverifiedVoiceCommand() = runBlocking {
        val gateway = Gateway().apply {
            connectionState.value = PlaybackConnectionState.Disconnected
        }
        val result = VoicePlaybackDispatcher(gateway).execute(RestrictedIntent.PLAY, sid, 1)
        assertTrue(result is AppResult.Failure)
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun recognizedAndAcknowledgedControlReturnsGatewaySnapshotNotGuessedState() = runBlocking {
        val gateway = Gateway()
        val result = VoicePlaybackDispatcher(gateway).execute(RestrictedIntent.NEXT, sid, 1)
        assertTrue(result is AppResult.Success)
        assertEquals(gateway.snapshot.value, (result as AppResult.Success<PlaybackSnapshot>).value)
        assertEquals(listOf(VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1)!!), gateway.commands)
    }

    @Test fun rejectedServiceCommandNeverBecomesVoiceSuccess() = runBlocking {
        val gateway = Gateway().apply {
            reply = AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED,
                Recoverability.RECOVERABLE,
                RecoveryAction.ManualFallback,
            ))
        }
        val result = VoicePlaybackDispatcher(gateway).execute(RestrictedIntent.NEXT, sid, 1)
        assertTrue(result is AppResult.Failure)
        assertEquals(1, gateway.commands.size)
    }

    @Test fun processorCoalescesIdenticalVoiceCommandWithoutSecondReceipt() = runBlocking {
        val owner = SupervisorJob()
        var received = 0
        var effects = 0
        val blocker = CompletableDeferred<Unit>()
        val proc = PlaybackCommandProcessor(CoroutineScope(owner + Dispatchers.Unconfined)) {
            effects++
            blocker.await()
            AppResult.Success(snapshot())
        }
        proc.configureReceiptSafety(
            capture = {
                received++
                PlaybackReceiptStamp(0L, 0L, received.toLong())
            },
            current = { _, _ -> true },
        )
        try {
            val cmd = PlaybackCommand.Next(CommandId("voice-42-1"))
            val original = proc.submit(cmd)
            val duplicate = proc.submit(cmd.copy())
            assertSame(original, duplicate)
            assertEquals(1, received)
            assertEquals(1, effects)
            blocker.complete(Unit)
            assertTrue(original.await() is AppResult.Success)
            assertTrue(duplicate.await() is AppResult.Success)
            assertEquals(1, effects)
        } finally { proc.close(); owner.cancel(); blocker.cancel() }
    }

    @Test fun processorRejectsIdCollisionWithDifferentPayload() = runBlocking {
        val owner = SupervisorJob()
        var receipts = 0
        val proc = PlaybackCommandProcessor(CoroutineScope(owner + Dispatchers.Unconfined)) {
            AppResult.Success(snapshot())
        }
        proc.configureReceiptSafety(
            capture = {
                receipts++
                PlaybackReceiptStamp(0L, 0L, receipts.toLong())
            },
            current = { _, _ -> true }
        )
        try {
            val key = CommandId("voice-42-1")
            assertTrue(proc.submit(PlaybackCommand.Play(key)).await() is AppResult.Success)
            val failed = proc.submit(PlaybackCommand.Next(key)).await()
            assertTrue(failed is AppResult.Failure)
            assertEquals(AppErrorCodes.PLAYBACK_REJECTED, (failed as AppResult.Failure).error.code)
            assertEquals(1, receipts)
        } finally { proc.close(); owner.cancel() }
    }

    @Test fun sourceCarriesAuthoritativeServiceAcknowledgementNotOptimisticUiState() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val service = File(root, "playback/service/MusicPlaybackService.kt").readText()
        val controller = File(root, "playback/controller/PlaybackControllerConnection.kt").readText()
        val wire = File(root, "playback/command/PlaybackWireProtocol.kt").readText()
        assertTrue(service.contains("PlaybackWireProtocol.acknowledgedSnapshot("))
        assertTrue(service.contains("VoiceCommandReceiptPolicy.canAdvance("))
        assertTrue(service.contains("VoiceCommandReceiptPolicy.isVoiceCommand(command)"))
        assertTrue(controller.contains("PlaybackWireProtocol.acknowledges(command, result.extras)"))
        assertTrue(wire.contains("KEY_COMMAND_ACK"))
        assertTrue(wire.contains("putString(KEY_COMMAND_ACK, id.value)"))
        assertFalse(service.contains("VoiceSessionCoordinator("))
    }

    @Test fun serviceOnlyKeepsSingleMedia3PlayerAndExistingWorker() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val service = File(root, "playback/service/MusicPlaybackService.kt").readText()
        val adapter = File(root, "voice/session/VoicePlaybackDispatcher.kt").readText()
        assertTrue(service.contains("processor = PlaybackCommandProcessor("))
        assertTrue(service.contains("ExoPlayer.Builder(this)"))
        assertFalse(adapter.contains("ExoPlayer.Builder"))
        assertFalse(adapter.contains("MediaController.Builder"))
        assertFalse(adapter.contains("createPlayer("))
    }
}
