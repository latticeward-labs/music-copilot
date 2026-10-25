package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayExit
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayPresentationPolicy
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayStatus
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
 * Issue #10 P09, written only. No JVM/Compose/instrumented test was executed.
 * TalkBack announcements and physical 150%/200% layouts need owner phone QA.
 */
class Issue10Phase9VoiceOverlayAccessibilityTest {
    private val id = VoiceSessionId(91)
    private val policy = VoiceOverlayPresentationPolicy
    private fun fail(
        code: String,
        canRetry: Boolean,
        origin: VoiceOrigin = VoiceOrigin.Player,
    ) = VoiceSessionState.Failed(
        id, origin,
        AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
        canRetry,
    )

    @Test fun idleStateHasNoOverlayOrUnintendedRecordingEntry() {
        val x = policy.project(VoiceSessionState.Idle, VoicePracticeStage.Idle)
        assertFalse(x.visible)
        assertEquals(VoiceOverlayStatus.Hidden, x.status)
        assertFalse(x.showRetry)
        assertFalse(x.showPermissionContinue)
    }

    @Test fun permissionRationaleIsVisibleOnlyWithExplicitContinue() {
        val x = policy.project(VoiceSessionState.Idle, VoicePracticeStage.Rationale)
        assertTrue(x.visible)
        assertEquals(VoiceOverlayStatus.PermissionRationale, x.status)
        assertTrue(x.showPermissionContinue)
        assertFalse(x.showRetry)
        assertEquals(VoiceOverlayExit.DismissPermission, x.exit)
    }

    @Test fun androidPermissionPendingCannotAutoGrantOrShowContinueAgain() {
        val x = policy.project(VoiceSessionState.Idle, VoicePracticeStage.AwaitingAndroid)
        assertEquals(VoiceOverlayStatus.PermissionPending, x.status)
        assertFalse(x.showPermissionContinue)
        assertFalse(x.showRetry)
        assertEquals(VoiceOverlayExit.DismissPermission, x.exit)
    }

    @Test fun microphonePermissionResultWithoutVoiceStartKeepsOverlayHidden() {
        val x = policy.project(VoiceSessionState.Idle, VoicePracticeStage.ReadyForVoiceHandoff)
        assertFalse(x.visible)
        assertEquals(VoiceOverlayStatus.Hidden, x.status)
    }

    @Test fun permissionDeniedDoesNotCreateFakeListeningOrModalOverlay() {
        val x = policy.project(VoiceSessionState.Idle, VoicePracticeStage.PermissionDenied)
        assertFalse(x.visible)
        assertEquals(VoiceOverlayStatus.Hidden, x.status)
    }

    @Test fun listeningStateHasExplicitCancelAndNeverRetry() {
        val x = policy.project(
            VoiceSessionState.Listening(id, VoiceOrigin.Player, 1), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Listening, x.status)
        assertEquals(VoiceOverlayExit.CancelCapture, x.exit)
        assertFalse(x.showRetry)
        assertTrue(x.visible)
    }

    @Test fun processingStateHasExplicitCancelAndNeverASecondMicStart() {
        val x = policy.project(
            VoiceSessionState.Processing(id, VoiceOrigin.Driving, 1),
            VoicePracticeStage.Idle,
        )
        assertEquals(VoiceOverlayStatus.Processing, x.status)
        assertEquals(VoiceOverlayExit.CancelCapture, x.exit)
        assertFalse(x.showRetry)
    }

    @Test fun executedStateUsesReturnNotMisleadingCancel() {
        val x = policy.project(
            VoiceSessionState.Executed(id, VoiceOrigin.Player), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Executed, x.status)
        assertEquals(VoiceOverlayExit.ReturnToManual, x.exit)
        assertFalse(x.showRetry)
    }

    @Test fun timeoutWithRetryDisplaysExactlyOneRecoveryAction() {
        val x = policy.project(
            fail(AppErrorCodes.VOICE_TIMEOUT, true), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Timeout, x.status)
        assertTrue(x.showRetry)
        assertFalse(x.showPermissionContinue)
        assertEquals(VoiceOverlayExit.ReturnToManual, x.exit)
    }

    @Test fun secondTimeoutHasNoThirdRetryButton() {
        val x = policy.project(
            fail(AppErrorCodes.VOICE_TIMEOUT, false), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Timeout, x.status)
        assertFalse(x.showRetry)
    }

    @Test fun noMatchHasSpecificSanitizedStatusAndManualFallback() {
        val x = policy.project(
            fail(AppErrorCodes.VOICE_NO_MATCH, false), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.NotRecognized, x.status)
        assertEquals(VoiceOverlayExit.ReturnToManual, x.exit)
        assertFalse(x.showRetry)
    }

    @Test fun providerUnavailableIsNotPresentedAsPermissionDenial() {
        val x = policy.project(
            fail(AppErrorCodes.VOICE_UNAVAILABLE, false), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Unavailable, x.status)
        assertEquals(VoiceOverlayExit.ReturnToManual, x.exit)
    }

    @Test fun unknownErrorCodesCannotLeakAsScreenCopy() {
        val x = policy.project(
            fail("private/transcript/device/path", false), VoicePracticeStage.Idle
        )
        assertEquals(VoiceOverlayStatus.Unavailable, x.status)
        assertFalse(x.showRetry)
    }

    @Test fun fullPlayerAndDrivingShareSameExactStateProjection() {
        val player = policy.project(
            fail(AppErrorCodes.VOICE_TIMEOUT, true, VoiceOrigin.Player),
            VoicePracticeStage.Idle,
        )
        val driving = policy.project(
            fail(AppErrorCodes.VOICE_TIMEOUT, true, VoiceOrigin.Driving),
            VoicePracticeStage.Idle,
        )
        assertEquals(player, driving)
    }

    @Test fun everyResultCanBeDismissedBackToManualControls() {
        for (state in listOf(
            VoiceSessionState.Executed(id, VoiceOrigin.Player),
            fail(AppErrorCodes.VOICE_NO_MATCH, false),
            fail(AppErrorCodes.VOICE_TIMEOUT, true),
        )) {
            val x = policy.project(state, VoicePracticeStage.Idle)
            assertEquals(VoiceOverlayExit.ReturnToManual, x.exit)
        }
    }

    @Test fun sourceReusesOnePanelOnBothOriginsWithoutAddingANavigationDestination() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val player = File(root, "feature/player/FullPlayerScreen.kt").readText()
        val driving = File(root, "feature/driving/DrivingModeScreen.kt").readText()
        val panel = File(root, "feature/voice/VoiceSessionPanel.kt").readText()
        assertTrue(player.contains("VoiceSessionPanel("))
        assertTrue(driving.contains("VoiceSessionPanel("))
        assertFalse(driving.contains("private fun DrivingVoiceSessionOverlay("))
        assertFalse(panel.contains("NavHost("))
        assertFalse(panel.contains("SpeechRecognizer("))
        assertFalse(panel.contains("ExoPlayer.Builder"))
    }

    @Test fun sourceUsesPoliteLiveRegionAndLargeAccessibleTouchTargets() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val panel = File(root, "feature/voice/VoiceSessionPanel.kt").readText()
        assertTrue(panel.contains("liveRegion = LiveRegionMode.Polite"))
        assertTrue(panel.contains("heading()"))
        assertTrue(panel.contains("heightIn(min = actionSize)"))
        assertTrue(panel.contains("MusicSize.drivingExitMinDp"))
        assertTrue(panel.contains("MusicSize.touchTargetMinDp"))
        assertTrue(panel.contains("contentDescription = exitA11y"))
        assertTrue(panel.contains("contentDescription = label"))
        assertFalse(panel.contains("assertive"))
        assertFalse(panel.contains("requestFocus()"))
    }

    @Test fun focusReturnIsOnPanelOpenCloseNotEveryVoiceTransition() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val player = File(root, "feature/player/FullPlayerScreen.kt").readText()
        val driving = File(root, "feature/driving/DrivingModeScreen.kt").readText()
        assertTrue(player.contains("LaunchedEffect(panelVisible)"))
        assertTrue(player.contains("panelWasVisible"))
        assertTrue(player.contains("micFocus.requestFocus()"))
        assertTrue(driving.contains("LaunchedEffect(overlayIsOpen, state.micEnabled)"))
        assertTrue(driving.contains("micFocusRequester.requestFocus()"))
    }

    @Test fun noRawTranscriptOrPersonalMediaTextIsShownInVoicePanel() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val panel = File(root, "feature/voice/VoiceSessionPanel.kt").readText()
        assertFalse(panel.contains(".transcript"))
        assertFalse(panel.contains("mediaRef"))
        assertFalse(panel.contains("getSpeechResult"))
        assertTrue(panel.contains("VoiceOverlayPresentationPolicy.project"))
    }

    @Test fun stringsIncludeEveryRequiredVoiceStateAndAccessibleExitAction() {
        val xml = File("src/main/res/values/strings.xml").readText()
        val keys = listOf(
            "i10_voice_permission_title", "i10_voice_permission_waiting",
            "i10_voice_listening", "i10_voice_processing", "i10_voice_executed",
            "i10_voice_timeout", "i10_voice_not_recognized", "i10_voice_unavailable",
            "i10_voice_retry_a11y", "i10_voice_cancel_a11y",
            "i10_voice_back_manual_a11y", "i10_voice_not_now_a11y",
            "i10_voice_manual_fallback",
        )
        for (key in keys) assertTrue(key, xml.contains("name=\"$key\""))
    }
}
