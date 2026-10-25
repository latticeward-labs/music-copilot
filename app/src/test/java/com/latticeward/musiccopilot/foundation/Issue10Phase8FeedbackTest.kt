package com.latticeward.musiccopilot.foundation

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
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.feedback.EnglishVoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackKey
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackPolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P08 SOURCE-AUTHORED ONLY. These methods have NOT been executed;
 * no JUnit, Robolectric, emulator, hardware, Gradle or GitHub Actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue10Phase8FeedbackTest {
    private fun snapshot() = PlaybackSnapshot(
        1L, 1L, PlaybackState.Paused,
        listOf(MediaRef("audio:1")), 0, MediaRef("audio:1"),
        1_000L, 60_000L, true, false, false,
    )

    private class FakeStt : SpeechToTextEngine {
        var calls = 0
        var response: suspend () -> AppResult<SpeechRecognitionResult> = {
            AppResult.Success(SpeechRecognitionResult("pause", 0.99, "en-US"))
        }
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> {
            calls++
            return response()
        }
        override fun cancel(sessionId: VoiceSessionId) = Unit
    }

    private class FakeTts : TextToSpeechEngine {
        val spoken = mutableListOf<SpeechOutput>()
        val stopped = mutableListOf<VoiceSessionId>()
        var available = true
        var reply: suspend (SpeechOutput) -> AppResult<Unit> = { AppResult.Success(Unit) }
        override fun isAvailable(): Boolean = available
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            spoken += output
            return reply(output)
        }
        override fun stop(sessionId: VoiceSessionId) { stopped += sessionId }
    }

    private class FakeGateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            1L, 1L, PlaybackState.Paused,
            listOf(MediaRef("audio:1")), 0, MediaRef("audio:1"),
            1_000L, 60_000L, true, false, false,
        ))
        val commands = mutableListOf<PlaybackCommand>()
        var reply: suspend () -> AppResult<PlaybackSnapshot> = { AppResult.Success(snapshot.value) }
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands.add(command)
            return reply()
        }
    }

    @Test fun exactlyFiveSuccessfulIntentOutcomesAreAvailable() {
        assertEquals(
            setOf(
                VoiceFeedbackKey.Play, VoiceFeedbackKey.Pause,
                VoiceFeedbackKey.Next, VoiceFeedbackKey.Previous,
                VoiceFeedbackKey.SeekBack,
            ),
            listOf(
                RestrictedIntent.PLAY, RestrictedIntent.PAUSE,
                RestrictedIntent.NEXT, RestrictedIntent.PREVIOUS,
                RestrictedIntent.SEEK_BACK,
            ).mapNotNull(VoiceFeedbackPolicy::acknowledged).toSet(),
        )
        assertNull(VoiceFeedbackPolicy.acknowledged(RestrictedIntent.PLAY_ARTIST))
    }

    @Test fun allCatalogMessagesAreOneEnglishSentenceWithinLimit() {
        for (key in VoiceFeedbackKey.entries) {
            val text = EnglishVoiceFeedbackCatalog.textFor(key)
            assertTrue(key.toString(), VoiceFeedbackPolicy.isShortSentence(text))
            assertTrue(text.length <= 120)
            assertEquals(1, text.count { it == '.' })
        }
    }

    @Test fun unsafeMultipleOrOversizedSentencesAreRejected() {
        for (bad in listOf(
            "", " ", "Done", "Done. Again.", "Done? Again.", " Done.",
            "Done. ", "Done! ", "Ready!","One\nTwo.", "Hi.\r", "a".repeat(125) + "."
        )) assertFalse(bad, VoiceFeedbackPolicy.isShortSentence(bad))
        assertTrue(VoiceFeedbackPolicy.isShortSentence("Playback paused."))
    }

    @Test fun outputUsesOnlyStableNonPersonalResourceKeyAndCatalogValue() {
        val speech = VoiceFeedbackPolicy.output(
            VoiceSessionId(9L), VoiceFeedbackKey.Next, EnglishVoiceFeedbackCatalog
        )!!
        assertEquals("i10_feedback_next", speech.textKey)
        assertEquals("Next track selected.", speech.shortText)
        assertEquals(VoiceSessionId(9L), speech.sessionId)
    }

    @Test fun invalidOrThrowingCatalogCannotBeSpoken() {
        val key = VoiceFeedbackKey.Pause
        assertNull(VoiceFeedbackPolicy.output(VoiceSessionId(7L), key) {
            "Paused. Another sentence."
        })
        assertNull(VoiceFeedbackPolicy.output(VoiceSessionId(7L), key) {
            error("synthetic catalog error")
        })
    }

    @Test fun terminalFailuresAreBriefButIntermediateRetryIsSilent() {
        assertNull(VoiceFeedbackPolicy.terminalFailure(AppErrorCodes.VOICE_TIMEOUT, true))
        assertNull(VoiceFeedbackPolicy.terminalFailure(AppErrorCodes.VOICE_NO_MATCH, true))
        assertEquals(VoiceFeedbackKey.Timeout,
            VoiceFeedbackPolicy.terminalFailure(AppErrorCodes.VOICE_TIMEOUT, false))
        assertEquals(VoiceFeedbackKey.NotRecognized,
            VoiceFeedbackPolicy.terminalFailure(AppErrorCodes.VOICE_NO_MATCH, false))
        assertEquals(VoiceFeedbackKey.Unavailable,
            VoiceFeedbackPolicy.terminalFailure(AppErrorCodes.VOICE_UNAVAILABLE, false))
    }

    @Test fun successTtsStartsOnlyAfterServiceAcknowledgement() = runTest {
        val pending = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        val gateway = FakeGateway().apply { reply = { pending.await() } }
        val tts = FakeTts()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, gateway.commands.size)
            assertTrue(voice.state.value is VoiceSessionState.Processing)
            assertTrue(tts.spoken.isEmpty())
            pending.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            assertEquals(listOf("Playback paused."), tts.spoken.map { it.shortText })
        } finally { voice.close(); job.cancel() }
    }

    @Test fun serviceFailureNeverSpeaksSuccessfulPlaybackMessage() = runTest {
        val gateway = FakeGateway().apply {
            reply = {
                AppResult.Failure(AppError.Playback(
                    AppErrorCodes.PLAYBACK_REJECTED,
                    Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
                ))
            }
        }
        val tts = FakeTts()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Failed)
            assertTrue(tts.spoken.none { it.textKey == "i10_feedback_pause" })
            assertEquals(listOf("Use the manual player controls."),
                tts.spoken.map { it.shortText })
        } finally { voice.close(); job.cancel() }
    }

    @Test fun absentTtsStillPublishesExecutedWithoutAnyProviderSpeech() = runTest {
        val tts = FakeTts().apply { available = false }
        val gateway = FakeGateway()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, gateway.commands.size)
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            assertTrue(tts.spoken.isEmpty())
        } finally { voice.close(); job.cancel() }
    }

    @Test fun failedTtsCannotUndoAnAcknowledgedMusicCommand() = runTest {
        val tts = FakeTts().apply {
            reply = { AppResult.Failure(AppError.Voice(
                AppErrorCodes.VOICE_UNAVAILABLE,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
            )) }
        }
        val gateway = FakeGateway()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            assertEquals(1, gateway.commands.size)
            assertEquals(1, tts.spoken.size)
        } finally { voice.close(); job.cancel() }
    }

    @Test fun hangingTtsTimesOutButDoesNotHoldVoiceStateOrQueue() = runTest {
        val hanging = CompletableDeferred<AppResult<Unit>>()
        val tts = FakeTts().apply { reply = { hanging.await() } }
        val gateway = FakeGateway()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            assertEquals(1, gateway.commands.size)
            advanceTimeBy(VoiceFeedbackPolicy.MAX_SPEAK_MS)
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            assertNull(voice.activeGenerationJob)
            assertEquals(1, tts.spoken.size)
        } finally { voice.close(); job.cancel(); hanging.cancel() }
    }

    @Test fun cancellingDuringFeedbackCannotSpeakAnotherMessageOrRepeatCommand() = runTest {
        val hanging = CompletableDeferred<AppResult<Unit>>()
        val tts = FakeTts().apply { reply = { hanging.await() } }
        val gateway = FakeGateway()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            FakeStt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            val sid = (voice.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            assertTrue(voice.state.value is VoiceSessionState.Executed)
            voice.cancel(VoiceCancelReason.DrivingExit)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, voice.state.value)
            assertTrue(tts.stopped.contains(sid))
            assertEquals(1, gateway.commands.size)
            assertEquals(1, tts.spoken.size)
        } finally { voice.close(); job.cancel(); hanging.cancel() }
    }

    @Test fun retryableNoMatchIsSilentThenSecondTerminalNoMatchSpeaksOnce() = runTest {
        val stt = FakeStt().apply {
            response = {
                AppResult.Failure(AppError.Voice(
                    AppErrorCodes.VOICE_NO_MATCH,
                    Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
                ))
            }
        }
        val tts = FakeTts()
        val gateway = FakeGateway()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, tts, gateway, recognitionEnabled = true,
        )
        try {
            voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue((voice.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(tts.spoken.isEmpty())
            assertTrue(voice.retry() is AppResult.Success)
            runCurrent()
            assertFalse((voice.state.value as VoiceSessionState.Failed).canRetry)
            assertEquals(listOf("Command not recognized."),
                tts.spoken.map { it.shortText })
            assertTrue(gateway.commands.isEmpty())
        } finally { voice.close(); job.cancel() }
    }

    @Test fun sourceTtsOwnsSingleActiveUtteranceAndHandlesAllErrors() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val tts = File(root, "voice/android/AndroidOfflineTextToSpeechEngine.kt").readText()
        val coordinator = File(root, "voice/session/DefaultVoiceSessionCoordinator.kt").readText()
        assertTrue(tts.contains("activeUtteranceId"))
        assertTrue(tts.contains("active != null"))
        assertTrue(tts.contains("TextToSpeech.QUEUE_FLUSH"))
        assertTrue(tts.contains("onError(id: String?, errorCode: Int)"))
        assertTrue(tts.contains("onStop(id: String?, interrupted: Boolean)"))
        assertTrue(coordinator.contains("feedbackClaimed = true"))
        assertTrue(coordinator.contains("withTimeoutOrNull(VoiceFeedbackPolicy.MAX_SPEAK_MS)"))
        assertFalse(coordinator.contains("SpeechOutput(id, \"voice_done\", \"Done\")"))
    }

    @Test fun productionFeedbackIsResourceBackedAndHasNoNetworkOrTranscript() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val binding = File(root, "app/di/VoiceBindings.kt").readText()
        val resolver = File(root, "voice/android/AndroidVoiceFeedbackCatalog.kt").readText()
        val resources = File("src/main/res/values/strings.xml").readText()
        assertTrue(binding.contains("AndroidVoiceFeedbackCatalog(context)"))
        assertTrue(resolver.contains("app.getString"))
        for (key in VoiceFeedbackKey.entries) {
            assertTrue(resources.contains("name=\"i10_feedback_${key.name.lowercase()}\""))
        }
        assertFalse(resolver.contains("transcript"))
        assertFalse(resolver.contains("http://"))
        assertFalse(resolver.contains("https://"))
    }
}
