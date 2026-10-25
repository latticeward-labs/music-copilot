package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeGate
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.VoicePttAdmissionPolicy
import com.latticeward.musiccopilot.core.application.voice.VoicePttAvailability
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P03: authored-only JVM contracts. These test sources MUST NOT be
 * executed in P03; the owner runs all phone cases on the cumulative P13 APK.
 */
class Issue10Phase3PushToTalkTest {
    private fun availability(
        active: Boolean = true,
        supported: Boolean = true,
        granted: Boolean = true,
        ready: Boolean = true,
        session: VoiceSessionState = VoiceSessionState.Idle,
    ) = VoicePttAdmissionPolicy.availability(
        originActive = active, providerSupported = supported,
        microphoneGranted = granted, providerAvailable = ready, session = session,
    )

    @Test fun idleSupportedAndFreshDeviceCanEnterPlayerPtt() {
        assertEquals(VoicePttAvailability.Ready, availability())
        assertTrue(VoicePttAdmissionPolicy.canTapMic(availability()))
        assertTrue(VoicePttAdmissionPolicy.mayStart(
            availability(), true, true, VoiceSessionState.Idle,
        ))
    }

    @Test fun revokedMicNeverStartsRecognitionBeforeNativePermission() {
        assertEquals(VoicePttAvailability.NeedsPermission, availability(granted = false))
        assertTrue(VoicePttAdmissionPolicy.canTapMic(availability(granted = false)))
        assertFalse(VoicePttAdmissionPolicy.mayStart(
            availability(granted = false), true, true, VoiceSessionState.Idle,
        ))
    }

    @Test fun onDeviceProviderUnavailableIsManualOnlyNotRemoteFallback() {
        assertEquals(VoicePttAvailability.ProviderUnavailable, availability(ready = false))
        assertEquals(VoicePttAvailability.ProviderUnavailable, availability(supported = false))
        assertFalse(VoicePttAdmissionPolicy.canTapMic(availability(ready = false)))
        assertFalse(VoicePttAdmissionPolicy.canTapMic(availability(supported = false)))
    }

    @Test fun hiddenPlayerCannotStartOnStaleClick() {
        val hidden = availability(active = false)
        assertEquals(VoicePttAvailability.OutsideOrigin, hidden)
        assertFalse(VoicePttAdmissionPolicy.canTapMic(hidden))
        assertFalse(VoicePttAdmissionPolicy.mayStart(hidden, true, true, VoiceSessionState.Idle))
    }

    @Test fun rapidSecondTapCannotReplaceExistingListeningOrProcessing() {
        val id = VoiceSessionId(10)
        val listening = VoiceSessionState.Listening(id, VoiceOrigin.Player, 1)
        val processing = VoiceSessionState.Processing(id, VoiceOrigin.Player, 1)
        assertEquals(VoicePttAvailability.SessionActive, availability(session = listening))
        assertFalse(VoicePttAdmissionPolicy.canTapMic(availability(session = listening)))
        assertFalse(VoicePttAdmissionPolicy.mayStart(
            VoicePttAvailability.Ready, true, true, processing,
        ))
    }

    @Test fun existingDrivingGenerationBlocksPlayerFromStealingMicrophone() {
        val driving = VoiceSessionState.Listening(VoiceSessionId(11), VoiceOrigin.Driving, 1)
        assertEquals(VoicePttAvailability.SessionActive, availability(session = driving))
        assertFalse(VoicePttAdmissionPolicy.owns(driving, VoiceOrigin.Player))
        assertTrue(VoicePttAdmissionPolicy.owns(driving, VoiceOrigin.Driving))
    }

    @Test fun everyNonIdleStateIsOwnedOnlyByItsOriginalSurface() {
        val id = VoiceSessionId(12)
        val states = listOf(
            VoiceSessionState.Listening(id, VoiceOrigin.Player, 1),
            VoiceSessionState.Processing(id, VoiceOrigin.Player, 1),
            VoiceSessionState.Executed(id, VoiceOrigin.Player),
            VoiceSessionState.Failed(
                id, VoiceOrigin.Player,
                AppError.Voice(
                    AppErrorCodes.VOICE_TIMEOUT, Recoverability.RECOVERABLE,
                    RecoveryAction.ManualFallback,
                ), canRetry = true,
            ),
        )
        assertTrue(states.all { VoicePttAdmissionPolicy.owns(it, VoiceOrigin.Player) })
        assertTrue(states.none { VoicePttAdmissionPolicy.owns(it, VoiceOrigin.Driving) })
        assertFalse(VoicePttAdmissionPolicy.owns(VoiceSessionState.Idle, VoiceOrigin.Player))
    }

    @Test fun stateCouldBeIdleButFreshPermissionOrProviderStillBlocksStart() {
        assertFalse(VoicePttAdmissionPolicy.mayStart(
            VoicePttAvailability.Ready, false, true, VoiceSessionState.Idle,
        ))
        assertFalse(VoicePttAdmissionPolicy.mayStart(
            VoicePttAvailability.Ready, true, false, VoiceSessionState.Idle,
        ))
    }

    @Test fun permissionGateOnlyPreparesNativeHandoffNotAnImplicitRecord() {
        val gate = VoicePracticeGate(35)
        assertTrue(gate.microphoneTapped())
        assertEquals(VoicePracticeStage.Rationale, gate.stage)
        assertTrue(gate.confirmRationale())
        assertEquals(VoicePracticeStage.AwaitingAndroid, gate.stage)
        assertTrue(gate.awaitingAndroidResult)
        // OS launch and ActivityResult cannot be exercised as a JVM fake.
        gate.manualOnly()
        assertEquals(VoicePracticeStage.Idle, gate.stage)
    }

    @Test fun sourceWiresOneActivityLauncherAndDifferentSurfacePresentations() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val activity = File(root, "app/MainActivity.kt").readText()
        val navigation = File(root, "app/navigation/AppNavHost.kt").readText()
        val player = File(root, "feature/player/FullPlayerScreen.kt").readText()
        assertTrue(activity.contains("private val microphoneLauncher"))
        assertTrue(activity.contains("private val playerMicUi"))
        assertTrue(activity.contains("private val drivingMicUi"))
        assertTrue(activity.contains("private val voicePracticeGate = VoicePracticeGate"))
        assertTrue(activity.contains("private fun onMicPermissionContinue(origin: VoiceOrigin)"))
        assertTrue(activity.contains("i10_native_player_mic_origin"))
        assertTrue(navigation.contains("voiceSessionCoordinator.start(VoiceOrigin.Player)"))
        assertTrue(navigation.contains("voiceSessionCoordinator.start(VoiceOrigin.Driving)"))
        assertTrue(player.contains("PlayerVoiceControls("))
        assertFalse(activity.contains("SpeechRecognizer.createSpeechRecognizer"))
    }

    @Test fun permissionResultCanOnlyPublishUiAndNeverStartVoice() {
        val source = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt")
            .readText()
        val nativeBlock = source.substringAfter("private val microphoneLauncher =")
            .substringBefore("private val bluetoothConnectLauncher")
        assertFalse(nativeBlock.contains(".start(VoiceOrigin."))
        assertTrue(nativeBlock.contains("micPermissionOrigin = null"))
        assertTrue(nativeBlock.contains("playerMicUi.value = stage"))
    }

    @Test fun sourceMaintainsNonAutoplayServiceAndBackgroundCancellation() {
        val activity = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt")
            .readText()
        assertTrue(activity.contains("override fun onStop()"))
        assertTrue(activity.contains("voice.state.value != VoiceSessionState.Idle"))
        assertTrue(activity.contains("voice.cancel(VoiceCancelReason.OriginInvalidated)"))
        assertFalse(activity.contains("stopService("))
        assertFalse(activity.contains("player.pause()"))
    }
}
