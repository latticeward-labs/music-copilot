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
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #12 P06 — source-authored JVM contract only. Tests NOT RUN.
 * No Gradle, emulator, playback service, CI or APK build is allowed before P14.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue12Phase6VoiceOutcomeEventsTest {
    private class Stt(
        var reply: suspend () -> AppResult<SpeechRecognitionResult> = {
            AppResult.Success(SpeechRecognitionResult("pause", 0.99, "en-US"))
        }
    ) : SpeechToTextEngine {
        var calls = 0
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest):
            AppResult<SpeechRecognitionResult> {
            calls++
            return reply()
        }
        override fun cancel(sessionId: VoiceSessionId) = Unit
    }

    private class SilentTts : TextToSpeechEngine {
        override fun isAvailable() = false
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> = AppResult.Success(Unit)
        override fun stop(sessionId: VoiceSessionId) = Unit
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            0L, 0L, PlaybackState.Paused, emptyList(), null, null, 0L,
            null, false, false, false
        ))
        var commands = 0
        var reply: suspend () -> AppResult<PlaybackSnapshot> = {
            AppResult.Success(snapshot.value)
        }
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands++
            return reply()
        }
    }

    private fun failed(code: String) = AppResult.Failure(
        AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback)
    )

    @Test fun successfulPauseEmitsDetectedThenAckExecutedOnce() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val gateway = Gateway()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt(), SilentTts(), gateway, recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            assertTrue(coordinator.start(VoiceOrigin.Player) is AppResult.Success<*>)
            advanceUntilIdle()
            assertEquals(1, gateway.commands)
            assertTrue(coordinator.state.value is VoiceSessionState.Executed)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.IntentDetected>().size)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
            assertEquals(0, events.filterIsInstance<TelemetryEvent.IntentFailed>().size)
            assertEquals("PAUSE",
                events.filterIsInstance<TelemetryEvent.CommandExecuted>().single().intentType)
        } finally { coordinator.close(); parent.cancel() }
    }

    @Test fun pendingServiceAckCannotBeCountedAsExecuted() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val ack = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        val gateway = Gateway().apply { reply = { ack.await() } }
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt(), SilentTts(), gateway, recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Driving)
            runCurrent()
            assertEquals(1, gateway.commands)
            assertEquals(0, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
            assertTrue(voice.state.value is VoiceSessionState.Processing)
            ack.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertEquals(1, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun rejectedServiceCommandEmitsOneFailureAndNeverSuccess() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val gateway = Gateway().apply {
            reply = { AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback
            )) }
        }
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt(), SilentTts(), gateway, recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Driving)
            advanceUntilIdle()
            assertTrue(voice.state.value is VoiceSessionState.Failed)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.IntentFailed>().size)
            assertEquals(0, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun rejectedDeterministicPhraseRecordsUnknownFailureOnly() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt { AppResult.Success(SpeechRecognitionResult("turn the volume up", 0.99, "en-US")) },
            SilentTts(), Gateway(), recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Player)
            advanceUntilIdle()
            val failure = events.filterIsInstance<TelemetryEvent.IntentFailed>().single()
            assertNull(failure.intentType)
            assertEquals(AppErrorCodes.VOICE_NO_MATCH, failure.failureCode)
            assertTrue(events.none { it is TelemetryEvent.IntentDetected ||
                it is TelemetryEvent.CommandExecuted })
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun secondExplicitRetryIsSeparateAttemptWithoutDuplicateTerminal() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val stt = Stt()
        stt.reply = {
            if (stt.calls == 1) failed(AppErrorCodes.VOICE_NO_MATCH)
            else AppResult.Success(SpeechRecognitionResult("pause", 0.99, "en-US"))
        }
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, SilentTts(), Gateway(), recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue((voice.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(voice.retry() is AppResult.Success<*>)
            advanceUntilIdle()
            assertEquals(2, stt.calls)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.IntentFailed>().size)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.IntentDetected>().size)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
            assertTrue(voice.retry() is AppResult.Failure)
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun permissionOrOriginCancellationRejectsLateAckSuccess() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val pending = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        val gateway = Gateway().apply { reply = { pending.await() } }
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt(), SilentTts(), gateway, recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Driving)
            runCurrent()
            voice.cancel(VoiceCancelReason.OriginInvalidated)
            pending.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertEquals(0, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
            assertTrue(voice.state.value == VoiceSessionState.Idle)
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun unknownProviderConfidenceDoesNotFabricateHighBucket() = runTest {
        val parent = SupervisorJob()
        val events = mutableListOf<TelemetryEvent>()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            Stt { AppResult.Success(SpeechRecognitionResult(
                "pause", null, "en-US", localVadEndpointVerified = true
            )) },
            SilentTts(), Gateway(), recognitionEnabled = true,
            tryRecordApprovedVoiceEvent = { events.add(it) }
        )
        try {
            voice.start(VoiceOrigin.Player)
            advanceUntilIdle()
            assertEquals(0, events.filterIsInstance<TelemetryEvent.IntentDetected>().size)
            assertEquals(1, events.filterIsInstance<TelemetryEvent.CommandExecuted>().size)
        } finally { voice.close(); parent.cancel() }
    }

    @Test fun endpointResultCanNeverExportNinthIssue11Diagnostic() {
        val source = File("src/main/java/com/latticeward/musiccopilot/" +
            "quality/privacy/AllowlistedTelemetryPrivacyGate.kt").readText()
        val coordinator = File("src/main/java/com/latticeward/musiccopilot/" +
            "voice/session/DefaultVoiceSessionCoordinator.kt").readText()
        assertTrue(source.contains("is TelemetryEvent.VoiceIntentOutcome ->"))
        assertTrue(coordinator.contains("recordTerminalFailureLocked(code)"))
        assertTrue(coordinator.contains("dispatchApprovedVoiceEvent(TelemetryEvent.CommandExecuted"))
        assertTrue(coordinator.contains("noteAcceptedIntent("))
        assertTrue(coordinator.contains("acceptedTerminalIntentForCorrection"))
        assertFalse(coordinator.contains("tryRecordApprovedVoiceEvent(event.toString())"))
    }

    @Test fun exactlySixIntentEnumsAndNoUnapprovedEventFamily() {
        assertEquals(6, com.latticeward.musiccopilot.voice.intent.RestrictedIntent.entries.size)
        assertEquals(8,
            com.latticeward.musiccopilot.quality.privacy.OutboundTelemetrySchemaV1.fieldKeys.size)
        assertNull(com.latticeward.musiccopilot.quality.privacy.OutboundTelemetrySchemaV1
            .fieldKeys["voice_intent_outcome"])
    }
}
