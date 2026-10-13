package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
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
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class Phase8VoiceFoundationTest {
    private fun result(text: String = "musico pause", confidence: Double? = 0.99) =
        AppResult.Success(SpeechRecognitionResult(text, confidence, "en-US"))

    private class FakeStt(
        var available: Boolean = true,
        var handler: suspend (SpeechRecognitionRequest) -> AppResult<SpeechRecognitionResult> = {
            AppResult.Success(SpeechRecognitionResult("musico pause", 0.99, "en-US"))
        },
    ) : SpeechToTextEngine {
        val requests = mutableListOf<SpeechRecognitionRequest>()
        val cancelled = mutableListOf<VoiceSessionId>()
        override fun isAvailable() = available
        override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> {
            requests += request
            return handler(request)
        }
        override fun cancel(sessionId: VoiceSessionId) { cancelled += sessionId }
    }

    private class FakeTts : TextToSpeechEngine {
        var available = true
        val spoken = mutableListOf<SpeechOutput>()
        val stopped = mutableListOf<VoiceSessionId>()
        override fun isAvailable() = available
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            spoken += output
            return AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) { stopped += sessionId }
    }

    private class FakePlayback(var accept: Boolean = true) : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            0, 0, PlaybackState.Paused, emptyList(), null, null, 0, null, false, false, false
        ))
        val commands = mutableListOf<PlaybackCommand>()
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            return if (accept) AppResult.Success(snapshot.value)
            else AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED, Recoverability.RECOVERABLE,
                RecoveryAction.ManualFallback
            ))
        }
    }

    @Test fun grammarIsBoundedToSixApprovedIntentsWithMusicoPrefixAndConfidenceGate() {
        val mapper = RestrictedVoiceIntentMapper()
        val pairs = listOf(
            "musico play" to RestrictedIntent.PLAY,
            "musico pause" to RestrictedIntent.PAUSE,
            "musico next track" to RestrictedIntent.NEXT,
            "musico previous" to RestrictedIntent.PREVIOUS,
            "musico go back 10 seconds" to RestrictedIntent.SEEK_BACK,
            "musico play songs by Synthetic Artist" to RestrictedIntent.PLAY_ARTIST,
        )
        pairs.forEach { (spoken, expected) ->
            assertEquals(expected, (mapper.map(result(spoken).value)
                as VoiceIntentDecision.Direct).intent)
        }
        assertEquals(VoiceIntentDecision.Rejected,
            mapper.map(result("pause").value))
        assertEquals(VoiceIntentDecision.Rejected,
            mapper.map(result("musico seek to minute 3").value))
        assertEquals(VoiceIntentDecision.RequiresConfirmation,
            mapper.map(result("musico pause", 0.79).value))
        assertEquals(VoiceIntentDecision.RequiresConfirmation,
            mapper.map(result("musico pause", null).value))
        assertEquals(6_000L, VoicePolicy.LISTEN_TIMEOUT_MS)
        assertEquals(2, VoicePolicy.MAX_ATTEMPTS)
    }

    @Test fun successIsPublishedOnlyAfterAcknowledgementAndTtsIsFeedback() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val stt = FakeStt()
        val tts = FakeTts()
        val player = FakePlayback()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + dispatcher), stt, tts, player,
            recognitionEnabled = true
        )
        try {
            val started = voice.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>
            assertEquals(VoiceSessionState.Listening(started.value, VoiceOrigin.Driving, 1),
                voice.state.value)
            advanceUntilIdle()
            assertEquals(VoiceSessionState.Executed(started.value, VoiceOrigin.Driving), voice.state.value)
            assertTrue(player.commands.single() is PlaybackCommand.Pause)
            assertEquals("Done", tts.spoken.single().shortText)
            assertEquals(6_000L, stt.requests.single().maxListeningMs)
        } finally { voice.close(); job.cancel() }
    }

    @Test fun rejectedPlaybackCannotReportVoiceExecutedOrSpeakSuccess() = runTest {
        val stt = FakeStt()
        val tts = FakeTts()
        val player = FakePlayback(accept = false)
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, tts, player, recognitionEnabled = true
        )
        try {
            voice.start(VoiceOrigin.Player)
            advanceUntilIdle()
            assertTrue(voice.state.value is VoiceSessionState.Failed)
            assertTrue(tts.spoken.isEmpty())
        } finally { voice.close(); job.cancel() }
    }

    @Test fun sixthIntentArtistIsNeverGuessedOrSentStraightToPlayback() = runTest {
        val stt = FakeStt(handler = {
            AppResult.Success(SpeechRecognitionResult("musico play songs by Synthetic Artist", .98, "en-US"))
        })
        val player = FakePlayback()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, FakeTts(), player, recognitionEnabled = true
        )
        try {
            voice.start(VoiceOrigin.Player)
            advanceUntilIdle()
            assertTrue(voice.state.value is VoiceSessionState.Failed)
            assertTrue(player.commands.isEmpty())
        } finally { voice.close(); job.cancel() }
    }

    @Test fun timeoutSixSecondsAndExactlyOneRetryEnforcedByVirtualTime() = runTest {
        val never = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = FakeStt(handler = { never.await() })
        val tts = FakeTts()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, tts, FakePlayback(), recognitionEnabled = true
        )
        try {
            val id = (voice.start(VoiceOrigin.Player) as AppResult.Success<VoiceSessionId>).value
            advanceTimeBy(6_001)
            advanceUntilIdle()
            val failed = voice.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_TIMEOUT, failed.error.code)
            assertTrue(failed.canRetry)
            assertEquals(id, failed.sessionId)
            assertTrue(voice.retry() is AppResult.Success)
            advanceTimeBy(6_001)
            advanceUntilIdle()
            assertFalse((voice.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(voice.retry() is AppResult.Failure)
            assertEquals(2, stt.requests.size)
            assertTrue(stt.cancelled.isNotEmpty())
        } finally { voice.close(); job.cancel(); never.cancel() }
    }

    @Test fun replacingGenerationCancelsLateTranscriptionBeforePlayback() = runTest {
        val older = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = FakeStt(handler = { request ->
            if (request.sessionId.value == 1L) older.await()
            else AppResult.Success(SpeechRecognitionResult("musico next", 0.97, "en-US"))
        })
        val player = FakePlayback()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, FakeTts(), player, recognitionEnabled = true
        )
        try {
            voice.start(VoiceOrigin.Player)
            testScheduler.runCurrent()
            val oldJob = voice.activeGenerationJob
            voice.start(VoiceOrigin.Driving)
            advanceUntilIdle()
            assertFalse(oldJob?.isActive ?: true)
            assertEquals(VoiceSessionState.Executed(VoiceSessionId(2), VoiceOrigin.Driving),
                voice.state.value)
            assertTrue(player.commands.single() is PlaybackCommand.Next)
            older.complete(result("musico pause"))
            advanceUntilIdle()
            assertEquals(1, player.commands.size)
            assertTrue(stt.cancelled.contains(VoiceSessionId(1)))
        } finally { voice.close(); job.cancel() }
    }

    @Test fun backDrivingExitAndPermissionLossInvalidateVoiceAndCancelProviders() = runTest {
        val wait = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = FakeStt(handler = { wait.await() })
        val tts = FakeTts()
        val player = FakePlayback()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, tts, player, recognitionEnabled = true
        )
        try {
            for (reason in listOf(VoiceCancelReason.UserBack,
                VoiceCancelReason.DrivingExit, VoiceCancelReason.PermissionLost)) {
                val id = (voice.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
                testScheduler.runCurrent()
                voice.cancel(reason)
                assertEquals(VoiceSessionState.Idle, voice.state.value)
                assertTrue(stt.cancelled.contains(id))
                assertTrue(tts.stopped.contains(id))
            }
            advanceUntilIdle()
            assertTrue(player.commands.isEmpty())
        } finally { voice.close(); job.cancel(); wait.cancel() }
    }

    @Test fun noProviderAndNoContextCannotProduceVoiceOrNetworkPlayback() = runTest {
        val stt = FakeStt(available = false)
        val player = FakePlayback()
        val job = SupervisorJob()
        val voice = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, FakeTts(), player, recognitionEnabled = true
        )
        try {
            assertTrue(voice.start(VoiceOrigin.Player) is AppResult.Failure)
            assertNull(voice.activeGenerationJob)
            assertEquals(VoiceSessionState.Idle, voice.state.value)
            assertTrue(player.commands.isEmpty())
        } finally { voice.close(); job.cancel() }
    }

    @Test fun androidAdapterHasNoGenericOrCloudRecognizerFallbackInProductionSource() {
        val files = java.io.File("src/main/java/com/latticeward/musiccopilot")
        val android = java.io.File(files, "voice/android/AndroidOnDeviceSpeechToTextEngine.kt").readText()
        val binding = java.io.File(files, "app/di/VoiceBindings.kt").readText()
        val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
        assertTrue(android.contains("createOnDeviceSpeechRecognizer"))
        assertTrue(android.contains("isOnDeviceRecognitionAvailable"))
        assertFalse(android.contains("SpeechRecognizer.createSpeechRecognizer("))
        assertFalse(android.contains("HttpURLConnection"))
        assertFalse(binding.contains("CloudSpeech"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.RECORD_AUDIO"))
    }
}
