package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayExit
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayPresentationPolicy
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayStatus
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.feature.voice.VoiceConfirmationActionPolicy
import com.latticeward.musiccopilot.voice.api.VoiceConfirmationOption
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P08 — SOURCE-AUTHORED JVM fixtures; NOT_COMPILED, NOT_RUN.
 *
 * Pure UI event-time guards + source inspection of Compose wiring.
 * These are NOT evidence of a real TalkBack, font-scale, keyboard,
 * Bluetooth, hardware-back or Media3/device run. P13 owns physical QA.
 */
class Issue11Phase8ArtistConfirmationUiTest {
    private val id = VoiceSessionId(42)
    private val token = VoiceCandidateToken("p08-private-opaque-token")
    private val wrong = VoiceCandidateToken("not-the-pending-token")
    private val state = VoiceSessionState.Confirming(
        id, VoiceOrigin.Player, token,
        listOf(VoiceConfirmationOption(0, "Local Artist")),
    )
    private val gate = VoiceConfirmationActionPolicy
    private val projection = VoiceOverlayPresentationPolicy

    private fun source(relative: String): String =
        File("src/main/java/com/latticeward/musiccopilot", relative).readText()

    @Test fun correctTokenAndVisiblePlayerOriginIsEligibleForCoordinatorDelegation() {
        assertTrue(gate.mayRespond(state, token, VoiceOrigin.Player, true))
    }

    @Test fun wrongTokenNeverReachesCoordinatorSelection() {
        assertFalse(gate.mayRespond(state, wrong, VoiceOrigin.Player, true))
    }

    @Test fun wrongOriginNeverConsumesAnotherScreensPendingToken() {
        assertFalse(gate.mayRespond(state, token, VoiceOrigin.Driving, true))
    }

    @Test fun hiddenPlayerOrReplacedDestinationCannotReply() {
        assertFalse(gate.mayRespond(state, token, VoiceOrigin.Player, false))
    }

    @Test fun validDrivingStateRequiresDrivingOrigin() {
        val driving = VoiceSessionState.Confirming(
            id, VoiceOrigin.Driving, token,
            listOf(VoiceConfirmationOption(0, "Local Artist")),
        )
        assertTrue(gate.mayRespond(driving, token, VoiceOrigin.Driving, true))
        assertFalse(gate.mayRespond(driving, token, VoiceOrigin.Player, true))
    }

    @Test fun idleStateCannotAcceptFormerConfirmation() {
        assertFalse(gate.mayRespond(VoiceSessionState.Idle, token, VoiceOrigin.Player, true))
    }

    @Test fun listeningStateCannotAcceptConfirmation() {
        val listening = VoiceSessionState.Listening(id, VoiceOrigin.Player, 1)
        assertFalse(gate.mayRespond(listening, token, VoiceOrigin.Player, true))
    }

    @Test fun processingStateCannotAcceptSameConfirmationAgain() {
        val processing = VoiceSessionState.Processing(id, VoiceOrigin.Player, 1)
        assertFalse(gate.mayRespond(processing, token, VoiceOrigin.Player, true))
    }

    @Test fun executedStateCannotAcceptOldTokenAgain() {
        val executed = VoiceSessionState.Executed(id, VoiceOrigin.Player)
        assertFalse(gate.mayRespond(executed, token, VoiceOrigin.Player, true))
    }

    @Test fun confirmationProjectsToRealPanelWithNoRetry() {
        val ui = projection.project(state, VoicePracticeStage.Idle)
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, ui.status)
        assertTrue(ui.visible)
        assertFalse(ui.showRetry)
        assertFalse(ui.showPermissionContinue)
        assertEquals(VoiceOverlayExit.CancelCapture, ui.exit)
    }

    @Test fun stalePermissionRationaleDoesNotHideLiveArtistChoice() {
        val ui = projection.project(state, VoicePracticeStage.Rationale)
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, ui.status)
        assertFalse(ui.showPermissionContinue)
    }

    @Test fun staleAndroidPermissionPromptDoesNotHideLiveArtistChoice() {
        val ui = projection.project(state, VoicePracticeStage.AwaitingAndroid)
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, ui.status)
        assertFalse(ui.showPermissionContinue)
    }

    @Test fun oneChoiceAndTwoChoicesRemainOnlyBoundedCoordinatorOptions() {
        val two = VoiceSessionState.Confirming(
            id, VoiceOrigin.Driving, token,
            listOf(
                VoiceConfirmationOption(0, "Artist One"),
                VoiceConfirmationOption(1, "Artist Two"),
            ),
        )
        assertEquals(1, state.options.size)
        assertEquals(listOf(0, 1), two.options.map { it.optionIndex })
        assertEquals(VoiceOverlayStatus.ConfirmationRequired,
            projection.project(two, VoicePracticeStage.Idle).status)
    }

    @Test fun publicConfirmationDiagnosticsRedactPrivateLabelsAndToken() {
        assertFalse(state.toString().contains("Local Artist"))
        assertFalse(state.toString().contains(token.value))
        assertFalse(state.options.single().toString().contains("Local Artist"))
    }

    @Test fun playerAndDrivingUseTheSameVoiceSessionPanelNotTwoOverlayImplementations() {
        val player = source("feature/player/FullPlayerScreen.kt")
        val driving = source("feature/driving/DrivingModeScreen.kt")
        assertTrue(player.contains("VoiceSessionPanel("))
        assertTrue(driving.contains("VoiceSessionPanel("))
        assertFalse(player.contains("ExoPlayer.Builder"))
        assertFalse(driving.contains("ExoPlayer.Builder"))
    }

    @Test fun panelOnlyUsesExistingTypedConfirmingSnapshotForArtistNames() {
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("state as? VoiceSessionState.Confirming"))
        assertTrue(panel.contains("confirming.options.forEach"))
        assertTrue(panel.contains("option.displayLabel"))
        assertFalse(panel.contains("library.get("))
        assertFalse(panel.contains("artistTrackRefs("))
        assertFalse(panel.contains("resolveVoiceCandidates("))
        assertFalse(panel.contains(".transcript"))
    }

    @Test fun panelArtistActionsNeverAppearToBeClickableNoOpHandlers() {
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("onConfirm: ((VoiceCandidateToken, Int) -> Unit)? = null"))
        assertTrue(panel.contains("enabled = !responseSubmitted && onConfirm != null"))
        assertTrue(panel.contains("enabled = !responseSubmitted && onNeither != null"))
        assertTrue(panel.contains("onCancelConfirmation != null"))
        assertTrue(panel.contains("remember(confirming?.token)"))
    }

    @Test fun artistButtonsHaveBoundedAccessibleLargeTouchTargets() {
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("MusicSize.drivingExitMinDp"))
        assertTrue(panel.contains("MusicSize.touchTargetMinDp"))
        assertTrue(panel.contains(".heightIn(min = actionSize)"))
        assertTrue(panel.contains("contentDescription = accessibleLabel"))
        assertTrue(panel.contains("contentDescription = neitherLabel"))
        assertTrue(panel.contains("contentDescription = exitA11y"))
    }

    @Test fun talkBackAnnouncementsAndFocusHandoffRemainPoliteAndOneLayer() {
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("liveRegion = LiveRegionMode.Polite"))
        assertTrue(panel.contains("heading()"))
        assertFalse(panel.contains("requestFocus()"))
        assertFalse(panel.contains("NavHost("))
        assertFalse(panel.contains("SpeechRecognizer("))
    }

    @Test fun drivingChoicesAppearBeforeArtworkInScrollableBodyAndExitRemainsPinned() {
        val driving = source("feature/driving/DrivingModeScreen.kt")
        val anchor = driving.indexOf("if (voiceState is VoiceSessionState.Confirming)")
        val art = driving.indexOf("if (recovery.showArtworkAndMetadata)")
        val header = driving.indexOf("val exitA11y")
        val scroll = driving.indexOf(".verticalScroll(rememberScrollState())")
        assertTrue(header >= 0 && scroll > header)
        assertTrue(anchor > scroll && art > anchor)
        assertTrue(driving.contains("onCancelConfirmation = onVoiceCancelConfirmation"))
    }

    @Test fun playerBackAndHeaderUseTokenScopedCancel() {
        val player = source("feature/player/FullPlayerScreen.kt")
        assertTrue(player.contains("onVoiceCancelConfirmation(confirming.token)"))
        assertTrue(player.contains("onVoiceCancelConfirmation(voiceState.token)"))
        assertTrue(player.contains("onConfirm = onConfirm"))
    }

    @Test fun rootCallbacksReadCurrentCoordinatorStateAtClickNotStoredSnapshot() {
        val nav = source("app/navigation/AppNavHost.kt")
        val policy = source("feature/voice/VoiceConfirmationActionPolicy.kt")
        assertTrue(nav.contains("VoiceConfirmationActionPolicy.mayRespond"))
        assertTrue(nav.contains("coordinator.state.value"))
        assertTrue(nav.contains("voiceSessionCoordinator?.confirm(token, index)"))
        assertTrue(nav.contains("voiceSessionCoordinator?.neither(token)"))
        assertTrue(nav.contains("voiceSessionCoordinator?.cancelConfirmation(token)"))
        assertTrue(policy.contains("latest.token == token"))
        assertTrue(policy.contains("latest.origin == origin"))
        assertTrue(policy.contains("originActive"))
    }

    @Test fun allEightUserFacingStringsHaveOneNonDuplicatedDefinition() {
        val xml = File("src/main/res/values/strings.xml").readText()
        val keys = listOf(
            "one_question", "two_question", "explanation", "select_a11y",
            "neither", "neither_a11y", "cancel", "cancel_a11y",
        )
        for (key in keys) {
            val marker = "name=\"i11_artist_confirm_$key\""
            assertEquals(marker, 1, Regex(Regex.escape(marker)).findAll(xml).count())
        }
    }

    @Test fun originalTransportAndPlayerOwnershipRemainOutsideConfirmationUI() {
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        val policy = source("feature/voice/VoiceConfirmationActionPolicy.kt")
        assertFalse(panel.contains("PlaybackGateway"))
        assertFalse(panel.contains("MediaStore"))
        assertFalse(panel.contains("VoiceSessionCoordinator.start"))
        assertFalse(policy.contains("PlaybackGateway"))
        assertFalse(policy.contains("MediaStore"))
    }
}
