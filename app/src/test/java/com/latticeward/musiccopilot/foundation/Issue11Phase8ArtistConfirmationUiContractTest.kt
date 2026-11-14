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
 * Issue #11 P08: SOURCE-AUTHORED fixtures only, NOT_RUN and NOT_COMPILED.
 * Semantic/density/TalkBack/accessibility assertions must be physically
 * observed on the owner's Android handset at P13; these are not PASS evidence.
 */
class Issue11Phase8ArtistConfirmationUiContractTest {
    private val token = VoiceCandidateToken("synthetic-token")
    private val old = VoiceCandidateToken("old-token")
    private val id = VoiceSessionId(91)

    private fun confirming(
        origin: VoiceOrigin = VoiceOrigin.Player,
        count: Int = 1,
    ) = VoiceSessionState.Confirming(
        id, origin, token,
        (0 until count).map {
            VoiceConfirmationOption(it, if (it == 0) "Synthetic Artist"
                else "Synthetic Alternate Artist")
        },
    )

    private fun voiceFile(suffix: String): String =
        File("src/main/java/com/latticeward/musiccopilot/$suffix").readText()

    private fun strings(): String = File("src/main/res/values/strings.xml").readText()

    @Test fun oneArtistProjectsRealConfirmationInsteadOfUnavailable() {
        val p = VoiceOverlayPresentationPolicy.project(
            confirming(), VoicePracticeStage.Idle,
        )
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, p.status)
        assertTrue(p.visible)
        assertFalse(p.showRetry)
        assertFalse(p.showPermissionContinue)
        assertEquals(VoiceOverlayExit.CancelCapture, p.exit)
    }

    @Test fun twoArtistsProjectSameSingleStepStatus() {
        val p = VoiceOverlayPresentationPolicy.project(
            confirming(count = 2), VoicePracticeStage.Idle,
        )
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, p.status)
        assertFalse(p.showRetry)
    }

    @Test fun currentCoordinatorConfirmationOverridesStalePermissionRationale() {
        val p = VoiceOverlayPresentationPolicy.project(
            confirming(), VoicePracticeStage.Rationale,
        )
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, p.status)
        assertFalse(p.showPermissionContinue)
    }

    @Test fun currentConfirmationOverridesStalePermissionAwaitingState() {
        val p = VoiceOverlayPresentationPolicy.project(
            confirming(), VoicePracticeStage.AwaitingAndroid,
        )
        assertEquals(VoiceOverlayStatus.ConfirmationRequired, p.status)
        assertFalse(p.showPermissionContinue)
    }

    @Test fun oldFiveControlIdlePermissionPresentationIsUnchanged() {
        val p = VoiceOverlayPresentationPolicy.project(
            VoiceSessionState.Idle, VoicePracticeStage.Rationale,
        )
        assertEquals(VoiceOverlayStatus.PermissionRationale, p.status)
        assertTrue(p.showPermissionContinue)
    }

    @Test fun activeMatchingPlayerTokenAllowsOneUiCallback() {
        assertTrue(VoiceConfirmationActionPolicy.mayRespond(
            confirming(), token, VoiceOrigin.Player, true))
    }

    @Test fun activeMatchingDrivingTokenAllowsOneUiCallback() {
        assertTrue(VoiceConfirmationActionPolicy.mayRespond(
            confirming(VoiceOrigin.Driving), token, VoiceOrigin.Driving, true))
    }

    @Test fun wrongTokenIsRejectedWithoutNavigationOrPlaybackAction() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            confirming(), old, VoiceOrigin.Player, true))
    }

    @Test fun hiddenOriginIsRejectedEvenForMatchingToken() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            confirming(), token, VoiceOrigin.Player, false))
    }

    @Test fun differentOriginIsRejectedEvenForMatchingToken() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            confirming(VoiceOrigin.Driving), token, VoiceOrigin.Player, true))
    }

    @Test fun staleProcessingGenerationDoesNotAdmitAnyAnswer() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            VoiceSessionState.Processing(id, VoiceOrigin.Player, 1),
            token, VoiceOrigin.Player, true))
    }

    @Test fun canceledIdleSessionDoesNotAdmitOldToken() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            VoiceSessionState.Idle, token, VoiceOrigin.Player, true))
    }

    @Test fun executedStateDoesNotAdmitReplayOfConfirmedChoice() {
        assertFalse(VoiceConfirmationActionPolicy.mayRespond(
            VoiceSessionState.Executed(id, VoiceOrigin.Player),
            token, VoiceOrigin.Player, true))
    }

    @Test fun sharedPanelContainsExactlyOneSourceOfConfirmationOptions() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("VoiceSessionState.Confirming"))
        assertTrue(src.contains("confirming.options.forEach"))
        assertTrue(src.contains("option.optionIndex"))
        assertFalse(src.contains("lookupArtistGroups("))
        assertFalse(src.contains("artistTrackRefs("))
        assertFalse(src.contains("MediaStore"))
    }

    @Test fun sharedPanelUsesResourceBackedQuestionsAndNoRawTranscript() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("i11_artist_confirm_one_question"))
        assertTrue(src.contains("i11_artist_confirm_two_question"))
        assertTrue(src.contains("i11_artist_confirm_select_a11y"))
        assertFalse(src.contains(".transcript"))
        assertFalse(src.contains("SpeechRecognizer("))
    }

    @Test fun sharedPanelHasSelectNeitherAndTokenScopedCancel() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("onConfirm(confirming.token, option.optionIndex)"))
        assertTrue(src.contains("onNeither(confirming.token)"))
        assertTrue(src.contains("onCancelConfirmation(confirming.token)"))
        assertFalse(src.contains("stt.recognize("))
    }

    @Test fun allChoiceButtonsAreFullWidthAndOnePerVerticalRow() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("confirming.options.forEach"))
        assertTrue(src.contains("Modifier.fillMaxWidth()"))
        assertTrue(src.contains("heightIn(min ="))
        assertFalse(src.contains("maxLines = 1"))
    }

    @Test fun selectionAndCancelHaveTalkBackDescriptionsAndFocusIndicators() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("contentDescription = accessibleLabel"))
        assertTrue(src.contains("contentDescription = neitherLabel"))
        assertTrue(src.contains("contentDescription = exitA11y"))
        assertTrue(src.contains("voiceActionFocusOutline()"))
        assertTrue(src.contains("liveRegion = LiveRegionMode.Polite"))
    }

    @Test fun sharedPanelUsesP07TokenAndNeverCreatesLocalVoiceOwner() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        assertTrue(src.contains("confirming.token"))
        assertFalse(src.contains("DefaultVoiceSessionCoordinator("))
        assertFalse(src.contains("ExoPlayer.Builder"))
        assertFalse(src.contains("NavHost("))
    }

    @Test fun playerOnlyPassesConfirmingThroughExistingInlinePanel() {
        val src = voiceFile("feature/player/FullPlayerScreen.kt")
        assertTrue(src.contains("VoiceSessionPanel("))
        assertTrue(src.contains("onConfirm = onVoiceConfirm"))
        assertTrue(src.contains("onNeither = onVoiceNeither"))
        assertTrue(src.contains("onCancelConfirmation = onVoiceCancelConfirmation"))
    }

    @Test fun playerBackCancelsOnlyVoiceInsteadOfClosingScreen() {
        val src = voiceFile("feature/player/FullPlayerScreen.kt")
        assertTrue(src.contains("BackHandler(enabled = voiceActive || voicePermissionPrompt)"))
        assertTrue(src.contains("onVoiceCancelConfirmation(confirming.token)"))
        assertTrue(src.contains("onVoiceCancel()"))
    }

    @Test fun drivingChoicesAppearBeforeArtworkAndKeepExitPinned() {
        val src = voiceFile("feature/driving/DrivingModeScreen.kt")
        assertTrue(src.contains("if (voiceState is VoiceSessionState.Confirming)"))
        assertTrue(src.indexOf("if (voiceState is VoiceSessionState.Confirming)") <
            src.indexOf("if (recovery.showArtworkAndMetadata)"))
        assertTrue(src.contains("Fixed, always-accessible Exit"))
    }

    @Test fun drivingMaintainsSingleSharedPanelAndTokenScopedBack() {
        val src = voiceFile("feature/driving/DrivingModeScreen.kt")
        assertTrue(src.contains("VoiceSessionPanel("))
        assertTrue(src.contains("onVoiceCancelConfirmation(confirming.token)"))
        assertTrue(src.contains("onConfirm = onVoiceConfirm"))
        assertFalse(src.contains("SpeechRecognizer("))
    }

    @Test fun applicationRoutesBothOriginsThroughTheOneCoordinator() {
        val src = voiceFile("app/navigation/AppNavHost.kt")
        assertTrue(src.contains("playerConfirmationAllowed"))
        assertTrue(src.contains("drivingConfirmationAllowed"))
        assertEquals(2, Regex("onVoiceConfirm = \\{ token, index ->")
            .findAll(src).count())
        assertTrue(src.contains("voiceSessionCoordinator?.confirm(token, index)"))
        assertTrue(src.contains("voiceSessionCoordinator?.neither(token)"))
        assertTrue(src.contains("voiceSessionCoordinator?.cancelConfirmation(token)"))
    }

    @Test fun navHostUsesCurrentCoordinatorStateAtTimeOfClick() {
        val src = voiceFile("app/navigation/AppNavHost.kt")
        assertTrue(src.contains("VoiceConfirmationActionPolicy.mayRespond("))
        assertTrue(src.contains("latest = coordinator.state.value"))
        assertTrue(src.contains("visibleBackStackEntry?.destination?.route"))
    }

    @Test fun allEightArtistUiStringsAreDefinedInAndroidResources() {
        val xml = strings()
        val keys = listOf(
            "i11_artist_confirm_one_question", "i11_artist_confirm_two_question",
            "i11_artist_confirm_explanation", "i11_artist_confirm_select_a11y",
            "i11_artist_confirm_neither", "i11_artist_confirm_neither_a11y",
            "i11_artist_confirm_cancel", "i11_artist_confirm_cancel_a11y",
        )
        keys.forEach { assertTrue(it, xml.contains("name=\"$it\"")) }
    }

    @Test fun artistLabelsAndTokensNeverEnterAnalyticsOrUiLogs() {
        val src = voiceFile("feature/voice/VoiceSessionPanel.kt")
        val gate = voiceFile("feature/voice/VoiceConfirmationActionPolicy.kt")
        assertFalse(src.contains("Log."))
        assertFalse(src.contains("Timber."))
        assertFalse(src.contains("Telemetry"))
        assertFalse(gate.contains("Log."))
        assertFalse(gate.contains("library.get("))
    }
}
