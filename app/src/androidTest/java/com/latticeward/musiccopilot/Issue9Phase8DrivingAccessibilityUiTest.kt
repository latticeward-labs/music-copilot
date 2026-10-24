package com.latticeward.musiccopilot

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.core.application.driving.DrivingAction
import com.latticeward.musiccopilot.core.application.driving.DrivingActionLabel
import com.latticeward.musiccopilot.core.application.driving.DrivingControlState
import com.latticeward.musiccopilot.core.application.driving.DrivingDisabledReason
import com.latticeward.musiccopilot.core.application.driving.DrivingPresentationState
import com.latticeward.musiccopilot.core.application.driving.DrivingStatusNotice
import com.latticeward.musiccopilot.core.application.driving.DrivingViewState
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceAvailability
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.feature.driving.DrivingModeScreen
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Issue #9 P08 — Compose accessibility source tests ONLY.
 * DO NOT RUN until the agreed final P13 APK/owner QA gate.
 * Uses synthetic/empty in-memory library; no microphone, player, audio,
 * media titles, screenshots, system permission prompts or vehicle.
 */
@RunWith(AndroidJUnit4::class)
class Issue9Phase8DrivingAccessibilityUiTest {
    @get:Rule val compose = createComposeRule()
    private val library: LibraryRepository = FakeEmptyLibrary()

    private fun controls(
        playing: Boolean = true,
        nextEnabled: Boolean = true,
    ): List<DrivingControlState> = listOf(
        DrivingControlState(DrivingAction.Previous, DrivingActionLabel.Previous, true),
        DrivingControlState(
            DrivingAction.PlayPause,
            if (playing) DrivingActionLabel.Pause else DrivingActionLabel.Play,
            true,
        ),
        DrivingControlState(DrivingAction.Next, DrivingActionLabel.Next,
            nextEnabled,
            if (nextEnabled) null else DrivingDisabledReason.Unsupported),
        DrivingControlState(DrivingAction.SeekBack, DrivingActionLabel.SeekBack10Seconds, true),
    )

    private fun snapshot(
        view: DrivingViewState = DrivingViewState.Playing,
        notice: DrivingStatusNotice = DrivingStatusNotice.None,
        microphone: Boolean = true,
        nextEnabled: Boolean = true,
    ) = DrivingPresentationState(
        view = view,
        notice = notice,
        controls = controls(playing = view == DrivingViewState.Playing, nextEnabled = nextEnabled),
        showCurrentTrackMetadata = false,
        micEnabled = microphone,
    )

    private fun mount(
        view: DrivingViewState = DrivingViewState.Playing,
        notice: DrivingStatusNotice = DrivingStatusNotice.None,
        microphone: Boolean = true,
        nextEnabled: Boolean = true,
        voice: VoiceSessionState = VoiceSessionState.Idle,
        micPermission: VoicePracticeStage = VoicePracticeStage.Idle,
        scale: Float = 1f,
        onExit: () -> Unit = {},
        onMic: () -> Unit = {},
        onVoiceCancel: () -> Unit = {},
        onVoicePermissionCancel: () -> Unit = {},
    ) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = scale)) {
                MusicCopilotTheme {
                    DrivingModeScreen(
                        state = snapshot(view, notice, microphone, nextEnabled),
                        currentItem = null,
                        library = library,
                        canReadLocalMetadata = false,
                        onExit = onExit,
                        onTransport = {},
                        onMic = onMic,
                        voiceState = voice,
                        voiceAvailability = if (microphone)
                            DrivingVoiceAvailability.Ready else DrivingVoiceAvailability.ProviderUnavailable,
                        voicePermissionStage = micPermission,
                        onVoiceCancel = onVoiceCancel,
                        onVoicePermissionCancel = onVoicePermissionCancel,
                    )
                }
            }
        }
    }

    @Test fun playingScreenExposesExitAndFourIndependentlyLabeledLargeButtons() {
        mount()
        compose.onNodeWithText("DRIVING MODE").assertIsDisplayed()
        compose.onNodeWithContentDescription("Exit Driving Mode immediately")
            .assertHeightIsAtLeast(56.dp)
        compose.onNodeWithText("PLAYING").assertIsDisplayed()
        for (name in listOf("Previous track", "Pause playback", "Next track",
            "Seek backward ten seconds")) {
            compose.onNodeWithContentDescription(name)
                .assertHeightIsAtLeast(64.dp).assertWidthIsAtLeast(64.dp)
                .assertIsEnabled()
        }
        compose.onNodeWithContentDescription("Voice command; press to talk")
            .assertHeightIsAtLeast(72.dp).assertIsEnabled()
    }

    @Test fun pausedScreenAnnouncesPausedAndLabelsPlayNotPause() {
        mount(view = DrivingViewState.Paused)
        compose.onNodeWithText("PAUSED").assertIsDisplayed()
        compose.onNodeWithContentDescription("Play playback").assertIsEnabled()
        compose.onNodeWithContentDescription("Pause playback").assertDoesNotExist()
        compose.onNodeWithText("Playback is paused. Use Play when your audio output is safe.")
            .assertExists()
    }

    @Test fun noTrackDoesNotExposeUnverifiedMusicOrInvalidTransportActions() {
        mount(view = DrivingViewState.NoTrack)
        compose.onNodeWithText("NO TRACK").assertIsDisplayed()
        compose.onNodeWithText("Exit to browse local music").assertExists()
        compose.onNodeWithContentDescription("Play playback").assertDoesNotExist()
        compose.onNodeWithContentDescription("Next track").assertDoesNotExist()
        compose.onNodeWithText("Play last").assertDoesNotExist()
        compose.onNodeWithContentDescription("Exit Driving Mode immediately")
            .assertIsDisplayed()
    }

    @Test fun disabledTransportSpeaksItsActualUnsupportedReason() {
        mount(nextEnabled = false)
        compose.onNodeWithContentDescription("Next track")
            .assertIsNotEnabled()
            .assert(SemanticsMatcher.expectValue(
                SemanticsProperties.StateDescription,
                "Action unavailable for this track",
            ))
    }

    @Test fun twoHundredPercentReflowRetainsFixedExitAndSixtyFourDpControls() {
        mount(scale = 2f)
        compose.onNodeWithContentDescription("Exit Driving Mode immediately")
            .assertHeightIsAtLeast(56.dp).assertIsDisplayed()
        for (name in listOf("Previous track", "Pause playback",
            "Next track", "Seek backward ten seconds")) {
            compose.onNodeWithContentDescription(name).assertHeightIsAtLeast(64.dp)
        }
        compose.onNodeWithContentDescription("Voice command; press to talk")
            .assertHeightIsAtLeast(72.dp)
    }

    @Test fun oneHundredFiftyPercentReflowStillProvidesAnImmediateExit() {
        mount(scale = 1.5f)
        compose.onNodeWithContentDescription("Exit Driving Mode immediately")
            .assertIsDisplayed().assertHeightIsAtLeast(56.dp)
        compose.onNodeWithContentDescription("Next track").assertHeightIsAtLeast(64.dp)
    }

    @Test fun voiceRationaleReplacesTransportControlsWithAccessibleCancel() {
        var dismissed = 0
        mount(micPermission = VoicePracticeStage.Rationale,
            onVoicePermissionCancel = { dismissed++ })
        compose.onNodeWithText("Microphone permission").assertExists()
        compose.onNodeWithContentDescription("Next track").assertDoesNotExist()
        compose.onNodeWithContentDescription("Continue to Android microphone permission")
            .assertHeightIsAtLeast(56.dp)
        compose.onNodeWithContentDescription("Cancel voice command and return to Driving")
            .assertHeightIsAtLeast(56.dp).performClick()
        compose.runOnIdle { assertEquals(1, dismissed) }
    }

    @Test fun realVoiceListeningShowsCancelNotOptimisticCommandSuccess() {
        var canceled = 0
        mount(voice = VoiceSessionState.Listening(
            VoiceSessionId(100), VoiceOrigin.Driving, 1,
        ), onVoiceCancel = { canceled++ })
        compose.onNodeWithText("Listening · speak one short English command").assertExists()
        compose.onNodeWithText("Command completed by the playback service")
            .assertDoesNotExist()
        compose.onNodeWithContentDescription("Next track").assertDoesNotExist()
        compose.onNodeWithContentDescription("Cancel voice command and return to Driving")
            .performClick()
        compose.runOnIdle { assertEquals(1, canceled) }
    }

    @Test fun pendingAndErrorReasonsUseTextNotJustColor() {
        mount(notice = DrivingStatusNotice.CommandPending)
        compose.onNodeWithText("Checking with playback service…").assertExists()
    }

    @Test fun exitIsAlwaysAvailableInVoiceOverlayEvenAtTwoHundredPercent() {
        var exits = 0
        mount(
            voice = VoiceSessionState.Listening(VoiceSessionId(11), VoiceOrigin.Driving, 1),
            scale = 2f,
            onExit = { exits++ },
        )
        compose.onNodeWithContentDescription("Exit Driving Mode immediately")
            .assertHeightIsAtLeast(56.dp).assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, exits) }
    }

    /** Source fixture, deliberately no disk/MediaStore and no private metadata. */
    private class FakeEmptyLibrary : LibraryRepository {
        override val index: StateFlow<LibraryIndexSnapshot> = MutableStateFlow(
            LibraryIndexSnapshot(0, LibraryIndexStatus.Empty, 0)
        )
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> = noItems()
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> = noItems()
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> = noItems()
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = noItems()

        private fun <T> noItems(): AppResult<T> = AppResult.Failure(
            AppError.Library(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
        )
    }
}
