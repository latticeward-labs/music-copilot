package com.latticeward.musiccopilot.foundation

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
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.session.VoiceTransitionPolicy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

/**
 * P02 AUTHORED_ONLY. Never run JVM/Gradle tests before the owner's later
 * explicit technical-test authorization. These do not claim real STT/Media3.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue10Phase2VoiceStateMachineTest {
    private fun utterance(text: String = "musico next") =
        AppResult.Success(SpeechRecognitionResult(text, 0.99, "en-US"))

    private class Stt(
        var response: suspend (SpeechRecognitionRequest) -> AppResult<SpeechRecognitionResult> = {
            AppResult.Success(SpeechRecognitionResult("musico next", 0.99, "en-US"))
        },
    ) : SpeechToTextEngine {
        val attempted = mutableListOf<VoiceSessionId>()
        val cancelled = mutableListOf<VoiceSessionId>()
        override fun isAvailable(): Boolean = true
        override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> {
            attempted += request.sessionId
            return response(request)
        }
        override fun cancel(sessionId: VoiceSessionId) { cancelled += sessionId }
    }

    private class Tts : TextToSpeechEngine {
        val spoken = mutableListOf<SpeechOutput>()
        val stopped = mutableListOf<VoiceSessionId>()
        override fun isAvailable() = true
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            spoken += output
            return AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) { stopped += sessionId }
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(
            PlaybackSnapshot(
                0, 0, PlaybackState.Paused, emptyList(), null, null,
                0, null, false, false, false
            )
        )
        val commands = mutableListOf<PlaybackCommand>()
        var ack: suspend (PlaybackCommand) -> AppResult<PlaybackSnapshot> = {
            AppResult.Success(snapshot.value)
        }
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            return ack(command)
        }
    }

    @Test fun transitionAdmissionRejectsStaleIdsAttemptsAndRepeatDispatch() {
        val id = VoiceSessionId(11)
        val listening = VoiceSessionState.Listening(id, VoiceOrigin.Player, 1)
        val processing = VoiceSessionState.Processing(id, VoiceOrigin.Player, 1)
        assertTrue(VoiceTransitionPolicy.mayProcess(listening, id, VoiceOrigin.Player, 1))
        assertFalse(VoiceTransitionPolicy.mayProcess(listening, VoiceSessionId(12), VoiceOrigin.Player, 1))
        assertFalse(VoiceTransitionPolicy.mayProcess(listening, id, VoiceOrigin.Driving, 1))
        assertFalse(VoiceTransitionPolicy.mayProcess(listening, id, VoiceOrigin.Player, 2))
        assertFalse(VoiceTransitionPolicy.mayDispatch(listening, id, VoiceOrigin.Player, 1, false))
        assertTrue(VoiceTransitionPolicy.mayDispatch(processing, id, VoiceOrigin.Player, 1, false))
        assertFalse(VoiceTransitionPolicy.mayDispatch(processing, id, VoiceOrigin.Player, 1, true))
        assertTrue(VoiceTransitionPolicy.mayComplete(processing, id, VoiceOrigin.Player, 1))
    }

    @Test fun retryPolicyExcludesThirdAttemptAndAlreadySubmittedCommandError() {
        assertTrue(VoiceTransitionPolicy.canOfferRetry(1, AppErrorCodes.VOICE_TIMEOUT))
        assertTrue(VoiceTransitionPolicy.canOfferRetry(1, AppErrorCodes.VOICE_NO_MATCH))
        assertFalse(VoiceTransitionPolicy.canOfferRetry(2, AppErrorCodes.VOICE_TIMEOUT))
        assertFalse(VoiceTransitionPolicy.canOfferRetry(1, AppErrorCodes.VOICE_UNAVAILABLE))
        val id = VoiceSessionId(7)
        val failure = VoiceSessionState.Failed(
            id, VoiceOrigin.Driving,
            AppError.Voice(
                AppErrorCodes.VOICE_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback
            ), canRetry = true
        )
        assertTrue(VoiceTransitionPolicy.mayRetry(failure, id, VoiceOrigin.Driving, 1))
        assertFalse(VoiceTransitionPolicy.mayRetry(failure, id, VoiceOrigin.Driving, 2))
        assertFalse(VoiceTransitionPolicy.mayRetry(failure, VoiceSessionId(8), VoiceOrigin.Driving, 1))
    }

    @Test fun successfulAcknowledgementIsTerminalAndItsJobIsReleased() = runTest {
        val stt = Stt()
        val tts = Tts()
        val gateway = Gateway()
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, tts, gateway, recognitionEnabled = true
        )
        try {
            val session = (coordinator.start(VoiceOrigin.Player) as AppResult.Success<VoiceSessionId>).value
            val generation = coordinator.activeGenerationJob
            assertEquals(VoiceSessionState.Listening(session, VoiceOrigin.Player, 1), coordinator.state.value)
            runCurrent()
            assertEquals(VoiceSessionState.Executed(session, VoiceOrigin.Player), coordinator.state.value)
            assertEquals(1, gateway.commands.size)
            assertEquals(1, tts.spoken.size)
            assertNull(coordinator.activeGenerationJob)
            assertFalse(generation?.isActive ?: true)
            coordinator.cancel(VoiceCancelReason.UserBack)
            assertEquals(VoiceSessionState.Idle, coordinator.state.value)
        } finally { coordinator.close(); parent.cancel() }
    }

    @Test fun sixSecondTimeoutYieldsOneRetryThenManualOnly() = runTest {
        val waiting = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = Stt(response = { waiting.await() })
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), Gateway(), recognitionEnabled = true
        )
        try {
            val id = (coordinator.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            advanceTimeBy(6_000L)
            runCurrent()
            val failure = coordinator.state.value as VoiceSessionState.Failed
            assertEquals(id, failure.sessionId)
            assertTrue(failure.canRetry)
            assertEquals(AppErrorCodes.VOICE_TIMEOUT, failure.error.code)
            assertNull(coordinator.activeGenerationJob)
            assertTrue(coordinator.retry() is AppResult.Success)
            assertEquals(VoiceSessionState.Listening(id, VoiceOrigin.Driving, 2), coordinator.state.value)
            runCurrent()
            advanceTimeBy(6_000L)
            runCurrent()
            assertFalse((coordinator.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(coordinator.retry() is AppResult.Failure)
            assertEquals(2, stt.attempted.size)
        } finally { coordinator.close(); parent.cancel(); waiting.cancel() }
    }

    @Test fun cancellingDuringProcessingIgnoresLateAcknowledgement() = runTest {
        val pendingAck = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        val stt = Stt()
        val tts = Tts()
        val gateway = Gateway().apply {
            ack = { withContext(NonCancellable) { pendingAck.await() } }
        }
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, tts, gateway, recognitionEnabled = true
        )
        try {
            coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(coordinator.state.value is VoiceSessionState.Processing)
            assertEquals(1, gateway.commands.size)
            coordinator.cancel(VoiceCancelReason.UserBack)
            pendingAck.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertEquals(VoiceSessionState.Idle, coordinator.state.value)
            assertTrue(tts.spoken.isEmpty())
            assertEquals(1, gateway.commands.size)
        } finally { coordinator.close(); parent.cancel(); pendingAck.cancel() }
    }

    @Test fun replacedSlowProviderCannotPublishOrExecuteAfterNewGeneration() = runTest {
        val delayed = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = Stt(response = { req ->
            if (req.sessionId.value == 1L) withContext(NonCancellable) { delayed.await() }
            else utterance()
        })
        val gateway = Gateway()
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true
        )
        try {
            coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val replaced = coordinator.activeGenerationJob
            val newId = (coordinator.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            assertEquals(VoiceSessionState.Executed(newId, VoiceOrigin.Driving), coordinator.state.value)
            delayed.complete(utterance("musico pause"))
            runCurrent()
            assertFalse(replaced?.isActive ?: true)
            assertEquals(1, gateway.commands.size)
            assertTrue(gateway.commands.single() is PlaybackCommand.Next)
            assertEquals(VoiceSessionState.Executed(newId, VoiceOrigin.Driving), coordinator.state.value)
        } finally { coordinator.close(); parent.cancel(); delayed.cancel() }
    }

    @Test fun mapperRejectionRemainsRecoverableButNeverExecutesPlayer() = runTest {
        val stt = Stt(response = { utterance("musico unknown") })
        val gateway = Gateway()
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true
        )
        try {
            coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(coordinator.state.value is VoiceSessionState.Failed)
            assertTrue((coordinator.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(gateway.commands.isEmpty())
        } finally { coordinator.close(); parent.cancel() }
    }

    @Test fun closedCoordinatorCannotRestartOrPublishVoice() = runTest {
        val parent = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt(), Tts(), Gateway(), recognitionEnabled = true
        )
        coordinator.close()
        assertTrue(coordinator.start(VoiceOrigin.Player) is AppResult.Failure)
        assertEquals(VoiceSessionState.Idle, coordinator.state.value)
        assertNull(coordinator.activeGenerationJob)
        parent.cancel()
    }
}
