package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceAvailability
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceHandoffPolicy as Policy
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #9 P07: PURE, authored-not-run JVM policy tests.
 * No Android mic, STT, TTS, real MediaSession, vehicle or permission request
 * is executed in this phase. Do not mark Issue #10 or GS-04 PASS.
 */
class Issue9Phase7DrivingVoiceHandoffPolicyTest {
    private val id = VoiceSessionId(101)
    private fun availability(
        driving: Boolean = true,
        supported: Boolean = true,
        granted: Boolean = true,
        available: Boolean = true,
        session: VoiceSessionState = VoiceSessionState.Idle,
    ) = Policy.availability(driving, supported, granted, available, session)

    @Test fun realSupportedGrantedAvailableOnDeviceIsReady() {
        assertEquals(DrivingVoiceAvailability.Ready, availability())
    }

    @Test fun cannotStartFromAHiddenOrClosedDrivingSurface() {
        assertEquals(DrivingVoiceAvailability.OutsideDriving,
            availability(driving = false))
    }

    @Test fun unsupportedAndroidProviderCannotOfferFakeRecording() {
        assertEquals(DrivingVoiceAvailability.ProviderUnavailable,
            availability(supported = false))
    }

    @Test fun absentMicrophoneGrantOffersOnlyAUserInitiatedPermissionRationale() {
        assertEquals(DrivingVoiceAvailability.NeedsPermission,
            availability(granted = false))
        assertTrue(Policy.canTapMic(DrivingVoiceAvailability.NeedsPermission))
    }

    @Test fun unavailableLocalRecognitionNeverFallsBackToCloud() {
        val a = availability(available = false)
        assertEquals(DrivingVoiceAvailability.ProviderUnavailable, a)
        assertFalse(Policy.canTapMic(a))
    }

    @Test fun noProviderWhenSupportedFlagIsFalseEvenWithSavedGrant() {
        assertEquals(DrivingVoiceAvailability.ProviderUnavailable,
            availability(supported = false, granted = true, available = false))
    }

    @Test fun permissionRequestIsNotEquivalentToAuthorizationToStart() {
        val a = availability(granted = false)
        assertFalse(Policy.mayStart(a, true, true))
    }

    @Test fun nativePermissionResultCannotLaunchVoiceWithoutSecondTap() {
        assertFalse(Policy.mayStart(DrivingVoiceAvailability.NeedsPermission,
            freshOsGrant = true, freshOnDeviceProvider = true))
    }

    @Test fun freshPermissionRevocationAtTapPreventsCapture() {
        assertFalse(Policy.mayStart(DrivingVoiceAvailability.Ready,
            freshOsGrant = false, freshOnDeviceProvider = true))
    }

    @Test fun disappearingLocalSpeechServiceAtTapPreventsCapture() {
        assertFalse(Policy.mayStart(DrivingVoiceAvailability.Ready,
            freshOsGrant = true, freshOnDeviceProvider = false))
    }

    @Test fun freshBothRequirementsAreNecessaryForAnExplicitPttStart() {
        assertTrue(Policy.mayStart(DrivingVoiceAvailability.Ready, true, true))
    }

    @Test fun ownedListeningDisablesSecondCaptureStart() {
        val current = VoiceSessionState.Listening(id, VoiceOrigin.Driving, attempt = 1)
        assertEquals(DrivingVoiceAvailability.SessionActive,
            availability(session = current))
        assertFalse(Policy.canTapMic(availability(session = current)))
        assertTrue(Policy.shouldCancelOnExit(current))
    }

    @Test fun ownedProcessingCanBeCancelledOnBackOrExit() {
        assertTrue(Policy.shouldCancelOnExit(
            VoiceSessionState.Processing(id, VoiceOrigin.Driving, attempt = 1)))
    }

    @Test fun ownedExecutedStateCanBeDismissedWithoutAnotherPlayerCommand() {
        assertTrue(Policy.shouldCancelOnExit(
            VoiceSessionState.Executed(id, VoiceOrigin.Driving)))
    }

    @Test fun ownedFailureIsRetryableOnlyThroughCoordinatorNotMicDoubleTap() {
        val failed = VoiceSessionState.Failed(
            id, VoiceOrigin.Driving,
            AppError.Voice(AppErrorCodes.VOICE_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
            canRetry = true,
        )
        assertTrue(Policy.shouldCancelOnExit(failed))
        assertFalse(Policy.canTapMic(availability(session = failed)))
    }

    @Test fun neverShowAnUnrelatedPlayerVoiceSessionInDriving() {
        val player = VoiceSessionState.Listening(id, VoiceOrigin.Player, 1)
        assertFalse(Policy.shouldCancelOnExit(player))
        assertEquals(DrivingVoiceAvailability.SessionActive,
            availability(session = player))
    }

    @Test fun idleNeverRequiresOrTriggersCancel() {
        assertFalse(Policy.shouldCancelOnExit(VoiceSessionState.Idle))
    }

    @Test fun aCancelledDrivingSessionDoesNotRemainAuthorized() {
        assertEquals(DrivingVoiceAvailability.OutsideDriving,
            availability(driving = false, session = VoiceSessionState.Idle))
    }

    @Test fun everyUnavailableStateExceptNeedsPermissionIsNonClickable() {
        for (s in listOf(
            DrivingVoiceAvailability.ProviderUnavailable,
            DrivingVoiceAvailability.OutsideDriving,
            DrivingVoiceAvailability.SessionActive,
        )) assertFalse(Policy.canTapMic(s))
    }
}
