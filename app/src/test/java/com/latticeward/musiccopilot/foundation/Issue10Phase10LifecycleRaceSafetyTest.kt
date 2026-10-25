package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.voice.VoiceLifecycleSafetyPolicy
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
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P10: authored-only JVM and static source contracts.
 * No JVM, Android, Compose, Lint, Actions, emulator or phone execution.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue10Phase10LifecycleRaceSafetyTest {
    private val policy = VoiceLifecycleSafetyPolicy

    private fun failed(code: String = AppErrorCodes.VOICE_TIMEOUT) =
        VoiceSessionState.Failed(
            VoiceSessionId(1), VoiceOrigin.Player,
            AppError.Voice(
                code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
            ), canRetry = true,
        )

    private class Stt : SpeechToTextEngine {
        var calls = 0
        val cancels = mutableListOf<VoiceSessionId>()
        var answer: suspend () -> AppResult<SpeechRecognitionResult> = {
            AppResult.Success(SpeechRecognitionResult("pause", 0.99, "en-US"))
        }
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest):
            AppResult<SpeechRecognitionResult> {
            calls++
            return answer()
        }
        override fun cancel(sessionId: VoiceSessionId) {
            cancels.add(sessionId)
        }
    }

    private class Tts : TextToSpeechEngine {
        val stops = mutableListOf<VoiceSessionId>()
        val speaks = mutableListOf<SpeechOutput>()
        override fun isAvailable() = true
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            speaks.add(output)
            return AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) { stops.add(sessionId) }
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(
            PlaybackSnapshot(
                1L, 1L, PlaybackState.Paused, listOf(MediaRef("audio:1")),
                0, MediaRef("audio:1"), 1_000L, 50_000L, true, false, false,
            )
        )
        val commands = mutableListOf<PlaybackCommand>()
        var waitFor: CompletableDeferred<AppResult<PlaybackSnapshot>>? = null
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            return waitFor?.await() ?: AppResult.Success(snapshot.value)
        }
    }

    @Test fun onlyIdleAllowsFreshExplicitStart() {
        assertTrue(policy.canAdmitExplicitTap(VoiceSessionState.Idle))
        assertFalse(policy.canAdmitExplicitTap(
            VoiceSessionState.Listening(VoiceSessionId(1), VoiceOrigin.Player, 1)))
        assertFalse(policy.canAdmitExplicitTap(
            VoiceSessionState.Processing(VoiceSessionId(1), VoiceOrigin.Driving, 1)))
        assertFalse(policy.canAdmitExplicitTap(
            VoiceSessionState.Executed(VoiceSessionId(1), VoiceOrigin.Player)))
        assertFalse(policy.canAdmitExplicitTap(failed()))
    }

    @Test fun appBackgroundCancelsAnyLiveOrTerminalVoiceButNotIdle() {
        assertFalse(policy.requiresCancellationAtUiBoundary(VoiceSessionState.Idle))
        assertTrue(policy.requiresCancellationAtUiBoundary(
            VoiceSessionState.Listening(VoiceSessionId(1), VoiceOrigin.Driving, 1)))
        assertTrue(policy.requiresCancellationAtUiBoundary(
            VoiceSessionState.Processing(VoiceSessionId(1), VoiceOrigin.Player, 1)))
        assertTrue(policy.requiresCancellationAtUiBoundary(
            VoiceSessionState.Executed(VoiceSessionId(1), VoiceOrigin.Player)))
        assertTrue(policy.requiresCancellationAtUiBoundary(failed()))
    }

    @Test fun activityRecreationNeverRestoresPreviousVoiceExecution() {
        assertEquals(VoiceSessionState.Idle, policy.restoredStateAfterRecreation())
    }

    @Test fun rapidSecondTapIsRejectedWhileRecognizerIsActive() = runTest {
        val pending = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = Stt().apply { answer = { pending.await() } }
        val tts = Tts()
        val gateway = Gateway()
        val job = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, tts, gateway, recognitionEnabled = true,
        )
        try {
            val first = (c.start(VoiceOrigin.Player) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            assertEquals(1, stt.calls)
            assertTrue(c.start(VoiceOrigin.Driving) is AppResult.Failure)
            assertEquals(VoiceSessionState.Listening(first, VoiceOrigin.Player, 1), c.state.value)
            assertEquals(1, stt.calls)
            assertTrue(gateway.commands.isEmpty())
        } finally { c.close(); job.cancel(); pending.cancel() }
    }

    @Test fun lateMicCallbackAfterUiStoppedCannotStartRecognition() = runTest {
        val stt = Stt()
        var foreground = false
        val parent = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), Gateway(), recognitionEnabled = true,
            canBeginSession = { foreground },
        )
        try {
            assertTrue(c.start(VoiceOrigin.Player) is AppResult.Failure)
            runCurrent()
            assertEquals(0, stt.calls)
            foreground = true
            assertTrue(c.start(VoiceOrigin.Player) is AppResult.Success)
            runCurrent()
            assertTrue(c.state.value is VoiceSessionState.Executed)
            foreground = false
            c.cancel(VoiceCancelReason.OriginInvalidated)
            assertTrue(c.start(VoiceOrigin.Driving) is AppResult.Failure)
            assertEquals(VoiceSessionState.Idle, c.state.value)
            assertEquals(1, stt.calls)
        } finally { c.close(); parent.cancel() }
    }

    @Test fun backgroundAlsoBlocksRetryAfterRecognitionFailure() = runTest {
        val stt = Stt().apply {
            answer = { AppResult.Failure(AppError.Voice(
                AppErrorCodes.VOICE_NO_MATCH,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
            )) }
        }
        var foreground = true
        val parent = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), Gateway(), recognitionEnabled = true,
            canBeginSession = { foreground },
        )
        try {
            c.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue((c.state.value as VoiceSessionState.Failed).canRetry)
            foreground = false
            assertTrue(c.retry() is AppResult.Failure)
            assertEquals(1, stt.calls)
        } finally { c.close(); parent.cancel() }
    }

    @Test fun cancelIgnoresOldRecognitionResultAndCreatesNoPlayback() = runTest {
        val pending = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = Stt().apply { answer = { pending.await() } }
        val gateway = Gateway()
        val parent = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(parent + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true,
        )
        try {
            val id = (c.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            c.cancel(VoiceCancelReason.RouteInvalidated)
            assertEquals(VoiceSessionState.Idle, c.state.value)
            pending.complete(AppResult.Success(SpeechRecognitionResult("next", 0.99, "en-US")))
            runCurrent()
            assertTrue(gateway.commands.isEmpty())
            assertTrue(stt.cancels.contains(id))
        } finally { c.close(); parent.cancel(); pending.cancel() }
    }

    @Test fun cancelDuringMedia3AckDoesNotSpeakOrReportExecuted() = runTest {
        val waiting = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        val gateway = Gateway().apply { waitFor = waiting }
        val tts = Tts()
        val job = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            Stt(), tts, gateway, recognitionEnabled = true,
        )
        try {
            c.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, gateway.commands.size)
            c.cancel(VoiceCancelReason.CallOrFocusInvalidated)
            waiting.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertEquals(VoiceSessionState.Idle, c.state.value)
            assertTrue(tts.speaks.isEmpty())
            assertEquals(1, gateway.commands.size)
        } finally { c.close(); job.cancel(); waiting.cancel() }
    }

    @Test fun duplicateCancelIsIdempotentAndDoesNotReplayMusic() = runTest {
        val pending = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        val stt = Stt().apply { answer = { pending.await() } }
        val gateway = Gateway()
        val job = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true,
        )
        try {
            c.start(VoiceOrigin.Driving)
            runCurrent()
            c.cancel(VoiceCancelReason.DrivingExit)
            c.cancel(VoiceCancelReason.RouteInvalidated)
            c.cancel(VoiceCancelReason.OriginInvalidated)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, c.state.value)
            assertTrue(gateway.commands.isEmpty())
        } finally { c.close(); job.cancel(); pending.cancel() }
    }

    @Test fun newSessionAfterExplicitCancelDoesNotUseOldId() = runTest {
        val pending = CompletableDeferred<AppResult<SpeechRecognitionResult>>()
        var attempts = 0
        val stt = Stt().apply {
            answer = {
                ++attempts
                if (attempts == 1) pending.await()
                else AppResult.Success(SpeechRecognitionResult("pause", 0.99, "en-US"))
            }
        }
        val gateway = Gateway()
        val job = SupervisorJob()
        val c = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true,
        )
        try {
            val first = (c.start(VoiceOrigin.Player) as AppResult.Success<VoiceSessionId>).value
            runCurrent()
            c.cancel(VoiceCancelReason.UserBack)
            val second = (c.start(VoiceOrigin.Driving) as AppResult.Success<VoiceSessionId>).value
            assertNotEquals(first, second)
            runCurrent()
            pending.complete(AppResult.Success(SpeechRecognitionResult("next", 0.99, "en-US")))
            runCurrent()
            assertEquals(1, gateway.commands.size)
            assertTrue(c.state.value is VoiceSessionState.Executed)
            assertEquals(second, (c.state.value as VoiceSessionState.Executed).sessionId)
        } finally { c.close(); job.cancel(); pending.cancel() }
    }

    @Test fun sourceSttCancellationMatchesRecognizerObjectNotSessionIdOnly() {
        val file = File("src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt").readText()
        assertTrue(file.contains("onMain { teardown(cancelFirst = true) }"))
        assertTrue(file.contains("if (live === expected)"))
        assertTrue(file.contains("if (live !== session || !continuation.isActive)"))
        assertTrue(file.contains("onMain { finish(failure(code)) }"))
        assertTrue(file.contains("SpeechRecognizer.createOnDeviceSpeechRecognizer(app)"))
    }

    @Test fun sourceTtsStopIsBoundToExactUtteranceNotReusedSessionId() {
        val tts = File("src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt").readText()
        assertTrue(tts.contains("stopMatched(output.sessionId, utteranceId)"))
        assertTrue(tts.contains("if (active == sessionId && activeUtteranceId == utteranceId)"))
        assertTrue(tts.contains("val expectedUtterance = activeUtteranceId ?: return"))
    }

    @Test fun serviceCancelsOnBluetoothOutputChangeWithoutAutoResume() {
        val service = File("src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt").readText()
        val idx = service.indexOf("onBluetoothSinkAvailable = {")
        val segment = service.substring(idx, idx + 1_700)
        assertTrue(segment.contains("revokeVoiceDuck()"))
        assertTrue(segment.contains("cancelActiveVoiceForPlaybackInterruption("))
        assertFalse(segment.contains("player.play()"))
        assertFalse(segment.contains("player.prepare()"))
    }

    @Test fun applicationBackgroundCancelsVoiceBeforeControllerDetach() {
        val app = File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        val begin = app.indexOf("fun onUiStopped()")
        val block = app.substring(begin, begin + 760)
        assertTrue(block.contains("uiVoiceStarted = false"))
        assertTrue(block.contains("voiceOwner.cancel(VoiceCancelReason.OriginInvalidated)"))
        assertTrue(block.indexOf("voiceOwner.cancel") < block.indexOf("controller.detachUiController()"))
        assertTrue(app.contains("canBeginSession = { uiVoiceStarted }"))
    }

    @Test fun mediaPermissionRevocationCancelsVoiceBeforeLibraryInvalidation() {
        val app = File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        val begin = app.indexOf("fun onMediaAccessRevoked()")
        val block = app.substring(begin, begin + 650)
        assertTrue(block.contains("voiceOwner.cancel(VoiceCancelReason.PermissionLost)"))
        assertTrue(block.indexOf("voiceOwner.cancel") <
            block.indexOf("libraryRepository.invalidateMediaAccess()"))
    }

    @Test fun serviceDuckLeaseReleaseMayFinishBeforeUiControllerDetach() {
        val controller = File("src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt").readText()
        assertTrue(controller.contains("pendingVoiceRelease = controller.sendCustomCommand("))
        assertTrue(controller.contains("withTimeoutOrNull(1_000L) { await(releaseReceipt) }"))
        assertTrue(controller.contains("oldController.release()"))
        assertTrue(controller.contains("relay.disconnected()"))
    }

    @Test fun noVoiceHistoryOrAdditionalMedia3PlayerIntroducedByPhaseTen() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val coordinator = File(root, "voice/session/DefaultVoiceSessionCoordinator.kt").readText()
        val service = File(root, "playback/service/MusicPlaybackService.kt").readText()
        assertFalse(coordinator.contains("SharedPreferences"))
        assertFalse(coordinator.contains("DataStore"))
        assertFalse(coordinator.contains("RoomDatabase"))
        assertFalse(coordinator.contains("GlobalScope"))
        assertFalse(service.contains("SpeechRecognizer.create"))
        assertTrue(service.contains("ExoPlayer.Builder(this)"))
        assertFalse(coordinator.contains("ExoPlayer.Builder"))
    }
}
