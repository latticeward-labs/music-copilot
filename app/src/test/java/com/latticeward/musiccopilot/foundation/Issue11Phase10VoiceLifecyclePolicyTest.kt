package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.voice.VoiceLifecycleSafetyPolicy
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.VoiceConfirmationOption
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P10 -- SOURCE AUTHORED ONLY, NOT_COMPILED/NOT_RUN.
 * Pure authorization and safety-boundary contract plus source-regression
 * fixtures. Source checks are not Android hardware or Media3 runtime proof.
 */
class Issue11Phase10VoiceLifecyclePolicyTest {
    private val id = VoiceSessionId(70)
    private val token = VoiceCandidateToken("synthetic-token-do-not-persist")

    private fun states() = listOf(
        VoiceSessionState.Listening(id, VoiceOrigin.Player, 1),
        VoiceSessionState.Processing(id, VoiceOrigin.Player, 1),
        VoiceSessionState.Confirming(id, VoiceOrigin.Player, token,
            listOf(VoiceConfirmationOption(0, "Synthetic Artist"))),
        VoiceSessionState.Executed(id, VoiceOrigin.Player),
        VoiceSessionState.Failed(id, VoiceOrigin.Driving,
            AppError.Voice(AppErrorCodes.VOICE_TIMEOUT,
                Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback), false),
    )

    private fun source(path: String) =
        File("src/main/java/com/latticeward/musiccopilot", path).readText()

    @Test fun explicitMicStartsOnlyFromIdleState() {
        assertTrue(VoiceLifecycleSafetyPolicy.canAdmitExplicitTap(VoiceSessionState.Idle))
        for (state in states())
            assertFalse(VoiceLifecycleSafetyPolicy.canAdmitExplicitTap(state))
    }

    @Test fun freshOsGrantAndForegroundAreBothRequired() {
        assertTrue(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(true, true))
        assertFalse(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(true, false))
        assertFalse(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(false, true))
        assertFalse(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(false, false))
    }

    @Test fun grantingMicPermissionDoesNotAuthorizeStoppedActivity() {
        assertFalse(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(false, true))
    }

    @Test fun foregroundReturnDoesNotAuthorizeRevokedMicPermission() {
        assertFalse(VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(true, false))
    }

    @Test fun everyActiveOrTerminalVoiceStateRequiresBoundaryCancellation() {
        for (state in states())
            assertTrue(VoiceLifecycleSafetyPolicy.mustCancelOnSafetyBoundary(state))
    }

    @Test fun idleCannotRequireExtraCancellation() {
        assertFalse(VoiceLifecycleSafetyPolicy.mustCancelOnSafetyBoundary(VoiceSessionState.Idle))
    }

    @Test fun activityRecreationNeverRestoresPendingConfirmingToken() {
        val restored = VoiceLifecycleSafetyPolicy.restoredStateAfterRecreation()
        assertEquals(VoiceSessionState.Idle, restored)
        assertFalse(restored is VoiceSessionState.Confirming)
    }

    @Test fun appBindingReadsOsRecordAudioEveryGateEvaluation() {
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("Manifest.permission.RECORD_AUDIO"))
        assertTrue(app.contains("ContextCompat.checkSelfPermission"))
        assertTrue(app.contains("VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant"))
        assertTrue(app.contains("uiVoiceStarted"))
    }

    @Test fun onStopCancelsVoiceBeforeDetachingMediaController() {
        val app = source("app/di/ApplicationBindings.kt")
        val block = app.substring(app.indexOf("fun onUiStopped()"))
        val cancel = block.indexOf("voiceOwner.cancel(VoiceCancelReason.OriginInvalidated)")
        val detach = block.indexOf("controller.detachUiController()")
        assertTrue(cancel >= 0 && detach > cancel)
        assertTrue(block.contains("uiVoiceStarted = false"))
    }

    @Test fun onStopCannotStopOrPauseTheServiceOwnedMedia3Player() {
        val app = source("app/di/ApplicationBindings.kt")
        val stopBlock = app.substring(app.indexOf("fun onUiStopped()"),
            app.indexOf("override fun close()"))
        assertFalse(stopBlock.contains("stopService("))
        assertFalse(stopBlock.contains("player.pause("))
        assertFalse(stopBlock.contains("stopSelf("))
    }

    @Test fun activityDoesNotPersistVoiceGenerationOrConfirmationInBundle() {
        val activity = source("app/MainActivity.kt")
        val begin = activity.indexOf("override fun onSaveInstanceState")
        val end = activity.indexOf("override fun onPause", begin)
        assertTrue(begin >= 0 && end > begin)
        val saved = activity.substring(begin, end)
        assertFalse(saved.contains("VoiceCandidateToken"))
        assertFalse(saved.contains("ArtistPending"))
        assertFalse(saved.contains("VoiceSessionState.Confirming"))
        assertFalse(saved.contains("SpeechRecognitionResult"))
        assertFalse(saved.contains("putParcelable("))
    }

    @Test fun activityOsMicRevocationInvalidatesSingleAppVoiceOwner() {
        val activity = source("app/MainActivity.kt")
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(activity.contains("onForegroundMicrophonePermissionObserved(freshMic)"))
        assertTrue(app.contains("voiceOwner.cancel(VoiceCancelReason.PermissionLost)"))
    }

    @Test fun onResumeCannotAutomaticallyIssueArtistSelectionOrStartMic() {
        val activity = source("app/MainActivity.kt")
        val start = activity.indexOf("override fun onResume()")
        val end = activity.indexOf("@Composable", start)
        assertTrue(start >= 0 && end > start)
        val resume = activity.substring(start, end)
        assertFalse(resume.contains("voiceSessionCoordinator.start("))
        assertFalse(resume.contains("voiceSessionCoordinator.confirm("))
        assertFalse(resume.contains("stt.recognize("))
    }

    @Test fun newVoiceGenerationRequiresFreshUserGestureAfterPermissionReturn() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(coord.contains("VoiceLifecycleSafetyPolicy.canAdmitExplicitTap"))
        assertTrue(coord.contains("override fun start(origin: VoiceOrigin)"))
        assertFalse(coord.contains("onResume()"))
    }

    @Test fun staleSttAndServiceAckContinuationsRecheckForegroundAndPermission() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(coord.contains("private fun isCurrentAndAuthorizedLocked("))
        assertTrue(coord.contains("canBeginSession()"))
        assertTrue(coord.contains("invalidateLocked()"))
        assertTrue(coord.contains("if (!isCurrent(id, generation, number)) return"))
    }

    @Test fun synchronousConfirmNeedsLatestTokenAndAuthorization() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(coord.contains("override fun confirm(token: VoiceCandidateToken"))
        assertTrue(coord.contains("isCurrentAndAuthorizedLocked(id, generation, number)"))
        assertTrue(coord.contains("snapshot.token != token"))
    }

    @Test fun staleTimeoutCannotFinishOrSpeakIntoAnotherGeneration() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(coord.contains("private fun expireArtistConfirmation("))
        assertTrue(coord.contains("isCurrentAndAuthorizedLocked(id, generation, number)"))
        assertTrue(coord.contains("pending.snapshot.token != token"))
        assertTrue(coord.contains("confirmationTimeoutJob?.cancel()"))
    }

    @Test fun promptAndTtsJobsAreCanceledAtLifecycleBoundary() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        val invalidate = coord.substring(coord.indexOf("private fun invalidateLocked()"))
        assertTrue(invalidate.contains("artistPromptJob?.cancel()"))
        assertTrue(invalidate.contains("confirmationTimeoutJob?.cancel()"))
        assertTrue(invalidate.contains("job?.cancel()"))
        assertTrue(invalidate.contains("tts.stop(id)"))
        assertTrue(invalidate.contains("stt.cancel(id)"))
    }

    @Test fun duckLeaseIsReleasedOnCancellationWithSessionUuid() {
        val coord = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        val invalidate = coord.substring(coord.indexOf("private fun invalidateLocked()"))
        assertTrue(invalidate.contains("ducking.releaseVoiceDuck(token)"))
        assertTrue(coord.contains("UUID.randomUUID()"))
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("maxVoiceDuckDurationMs"))
        assertTrue(service.contains("revokeVoiceDuck()"))
    }

    @Test fun focusAndRouteSignalsAreMappedThroughOneAppVoiceOwner() {
        val app = source("app/MusicCopilotApplication.kt")
        assertTrue(app.contains("PlaybackVoiceInterruptionReason.FOCUS_INVALIDATED"))
        assertTrue(app.contains("VoiceCancelReason.CallOrFocusInvalidated"))
        assertTrue(app.contains("PlaybackVoiceInterruptionReason.ROUTE_INVALIDATED"))
        assertTrue(app.contains("VoiceCancelReason.RouteInvalidated"))
        assertTrue(app.contains("bindings.voiceSessionCoordinator.cancel(voiceReason)"))
    }

    @Test fun disconnectedBluetoothDoesNotTriggerReconnectAutoplay() {
        val gate = source("playback/service/PlaybackInterruptionGate.kt")
        assertTrue(gate.contains("requiresExplicitResume = true"))
        assertTrue(gate.contains("bluetoothConnectionPlayQuarantine = true"))
        assertTrue(gate.contains("shouldSuppressAutomaticPlay()"))
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("PlaybackVoiceInterruptionReason.ROUTE_INVALIDATED"))
        assertTrue(service.contains("interruptionGate.onBluetoothSinkAvailable("))
    }

    @Test fun thereIsNoSecondAudioFocusOwnerOrTelephonyListenerInAppVoiceLayer() {
        val voice = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        val app = source("app/di/ApplicationBindings.kt")
        assertFalse(voice.contains("AudioManager.requestAudioFocus"))
        assertFalse(voice.contains("TelephonyManager"))
        assertFalse(app.contains("PhoneStateListener"))
        assertFalse(app.contains("ExoPlayer.Builder"))
    }

    @Test fun serviceFocusInterruptionRevokesDuckBeforeCancelVoice() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val start = service.indexOf("onVoiceCancellationRequested = { reason ->")
        val end = service.indexOf("onInterruptionNotice = ", start)
        assertTrue(start >= 0 && end > start)
        val callback = service.substring(start, end)
        assertTrue(callback.indexOf("revokeVoiceDuck()") >= 0)
        assertTrue(callback.indexOf("cancelActiveVoiceForPlaybackInterruption") >
            callback.indexOf("revokeVoiceDuck()"))
    }
}
