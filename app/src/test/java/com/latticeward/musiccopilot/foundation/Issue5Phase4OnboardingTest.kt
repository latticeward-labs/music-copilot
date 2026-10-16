package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEffect
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMachine
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.data.api.FirstUsePage
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P04: approved WF-02 / HF-02 two-slide copy, semantics and safe local transitions. */
class Issue5Phase4OnboardingTest {
    private val ui = File("src/main/java/com/latticeward/musiccopilot/feature/onboarding/FirstUseScreen.kt").readText()
    private val strings = File("src/main/res/values/strings.xml").readText()
    private val viewModel = File("src/main/java/com/latticeward/musiccopilot/feature/onboarding/FirstUseViewModel.kt").readText()

    @Test fun exactlyTwoValueSlidesWithApprovedHighFidelityCopy() {
        for (copy in listOf(
            "Welcome", "LOCAL MUSIC", "Device music placeholder",
            "Play local music. No account needed.", "Your library stays on this device.",
            "Continue", "OPTIONAL VOICE", "6 supported commands",
            "Voice starts only after your tap.", "Skip keeps all manual controls.",
            "Practice Voice", "Finish without Voice",
        )) assertTrue("Missing approved string: $copy", strings.contains(">$copy</string>"))
        assertFalse(ui.contains("ThirdSlide"))
        assertTrue(ui.contains("FirstUsePage.SafetyAndVoice"))
        assertTrue(ui.contains("FirstUsePage.Value"))
    }

    @Test fun realPagerIndicatorIsNotColorOnlyAndIsAccessible() {
        assertTrue(ui.contains("FirstUsePageIndicator(page)"))
        assertTrue(strings.contains("Page 1 of 2"))
        assertTrue(strings.contains("Page 2 of 2"))
        assertTrue(ui.contains("contentDescription = description"))
        assertTrue(ui.contains(".semantics { heading() }"))
    }

    @Test fun buttonsAndSafeInsetsAllowReflowWithoutTrappingBack() {
        assertTrue(ui.contains(".safeDrawingPadding()"))
        assertTrue(ui.contains(".verticalScroll(scrollState)"))
        assertTrue(ui.contains("LaunchedEffect(page) { scrollState.scrollTo(0) }"))
        assertTrue(ui.contains("heightIn(min = 48.dp)"))
        assertTrue(ui.contains("BackHandler { dispatch(FirstUseEvent.Back) }"))
        assertTrue(ui.contains("dispatch(FirstUseEvent.Skip)"))
        assertTrue(ui.contains("dispatch(if (failed) FirstUseEvent.Retry"))
    }

    @Test fun practiceVoiceIsVisiblyUnavailableUntilJitMicrophonePhase() {
        assertTrue(ui.contains("stringResource(R.string.first_use_practice_voice)"))
        assertTrue(ui.contains("enabled = false"))
        assertTrue(strings.contains("No microphone access is requested here."))
        assertFalse(ui.contains("RequestPermission"))
        assertFalse(ui.contains("RECORD_AUDIO"))
        assertFalse(ui.contains("SpeechRecognizer"))
        assertFalse(ui.contains("MediaStore"))
    }

    @Test fun slideOneContinuePersistsSecondPageBeforeVisibleTransition() {
        val pending = FirstUseMachine.reduce(
            FirstUseMachine.enter(false, FirstUsePage.Value), FirstUseEvent.Continue
        )
        assertTrue(pending.state is FirstUseState.Saving)
        assertEquals(listOf(FirstUseEffect.PersistCheckpoint(FirstUsePage.SafetyAndVoice, 1)), pending.effects)
        assertEquals(pending.state, FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(99)).state)
        assertEquals(FirstUsePage.SafetyAndVoice,
            (FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(1)).state as FirstUseState.Education).page)
    }

    @Test fun slideTwoBackRestoresSlideOneWithAckAndNoCompletion() {
        val saving = FirstUseMachine.reduce(
            FirstUseMachine.enter(false, FirstUsePage.SafetyAndVoice), FirstUseEvent.Back
        )
        assertEquals(listOf(FirstUseEffect.PersistCheckpoint(FirstUsePage.Value, 1)), saving.effects)
        val ready = FirstUseMachine.reduce(saving.state, FirstUseEvent.Saved(1))
        assertEquals(FirstUsePage.Value, (ready.state as FirstUseState.Education).page)
        assertTrue(ready.effects.isEmpty())
    }

    @Test fun slideTwoSkipAndFinishBothCommitWithoutMicRequest() {
        for (event in listOf(FirstUseEvent.Skip, FirstUseEvent.Continue)) {
            val saving = FirstUseMachine.reduce(
                FirstUseMachine.enter(false, FirstUsePage.SafetyAndVoice), event
            )
            assertEquals(listOf(FirstUseEffect.PersistCompletion(1)), saving.effects)
            assertTrue(FirstUseMachine.reduce(saving.state, FirstUseEvent.SaveFailed(1)).effects.isEmpty())
            assertEquals(listOf(FirstUseEffect.EnterReadiness),
                FirstUseMachine.reduce(saving.state, FirstUseEvent.Saved(1)).effects)
        }
    }

    @Test fun destinationViewModelOwnsLifecycleAndOnlyStoresAcknowledgedUiState() {
        assertTrue(ui.contains("viewModel("))
        assertTrue(ui.contains("FirstUseViewModel.factory(repository, replay)"))
        assertTrue(viewModel.contains("SavedStateHandle"))
        assertTrue(viewModel.contains("viewModelScope.launch"))
        assertTrue(viewModel.contains("ackedFirstUsePage"))
        assertTrue(viewModel.contains("FirstUseMachine.enter("))
        assertFalse(viewModel.contains("READ_MEDIA_AUDIO"))
        assertFalse(viewModel.contains("MediaStoreScanner"))
        assertFalse(viewModel.contains("ExoPlayer"))
    }

    @Test fun settingsReplayIsRealReadOnlyAndPreservesCompletion() {
        val host = File("src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt").readText()
        assertTrue(host.contains("composable(Destination.Settings.route)"))
        assertTrue(host.contains("onPrimary = { replayingEducation = true }"))
        assertTrue(host.contains("replay = replayingEducation"))
        assertTrue(host.contains("replayingEducation = false"))
        val start = FirstUseMachine.enter(true, FirstUsePage.SafetyAndVoice, replay = true)
        val next = FirstUseMachine.reduce(start, FirstUseEvent.Continue)
        assertTrue(next.effects.isEmpty())
        val done = FirstUseMachine.reduce(next.state, FirstUseEvent.Continue)
        assertEquals(listOf(FirstUseEffect.ReturnToOrigin), done.effects)
        assertFalse(done.effects.any { it is FirstUseEffect.PersistCompletion })
    }

    @Test fun screenDoesNotOptimisticallyNavigateOnFailedWriteOrDoubleTap() {
        assertTrue(viewModel.contains("FirstUseEvent.SaveFailed(operationId)"))
        assertTrue(viewModel.contains("FirstUseEvent.Saved(operationId)"))
        assertTrue(ui.contains("val enabled = !saving && !finished"))
        assertTrue(viewModel.contains("FirstUseEffect.EnterReadiness"))
        val save = FirstUseMachine.reduce(FirstUseMachine.enter(false, FirstUsePage.Value), FirstUseEvent.Skip)
        assertTrue(FirstUseMachine.reduce(save.state, FirstUseEvent.Continue).effects.isEmpty())
        assertTrue(FirstUseMachine.reduce(save.state, FirstUseEvent.Saved(0)).effects.isEmpty())
    }
}
