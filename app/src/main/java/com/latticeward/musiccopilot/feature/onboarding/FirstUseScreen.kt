package com.latticeward.musiccopilot.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.data.api.FirstUsePage
import com.latticeward.musiccopilot.data.api.PreferencesRepository

/**
 * Issue #5 P04: WF-02 / HF-02 approved TWO slides, not a third feature screen.
 * Stateless Android UI surrounding the pure P02 state machine and DataStore ACKs.
 * No permission launch, service playback, cloud STT or automatic voice capture.
 */
@Composable
fun FirstUseScreen(
    repository: PreferencesRepository,
    replay: Boolean = false,
    voicePracticeStage: VoicePracticeStage = VoicePracticeStage.Idle,
    onPracticeVoice: () -> Unit = {},
    onPracticeRationaleContinue: () -> Unit = {},
    onPracticeManualOnly: () -> Unit = {},
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    val controller: FirstUseViewModel = viewModel(
        key = if (replay) "first_use_replay" else "first_use_initial",
        factory = remember(repository, replay) { FirstUseViewModel.factory(repository, replay) },
    )
    val machine = controller.machine
    fun dispatch(event: FirstUseEvent) {
        if (event == FirstUseEvent.Back || event == FirstUseEvent.Skip ||
            event == FirstUseEvent.Continue && machine is FirstUseState.Education &&
                (machine as FirstUseState.Education).page == FirstUsePage.SafetyAndVoice
        ) onPracticeManualOnly()
        controller.dispatch(event, onComplete, onExit)
    }

    BackHandler { dispatch(FirstUseEvent.Back) }
    val saving = machine is FirstUseState.Saving
    val failed = machine is FirstUseState.SaveError
    val finished = machine is FirstUseState.Finished
    val page = when (val state = machine) {
        is FirstUseState.Education -> state.page
        is FirstUseState.Saving -> state.pageBefore
        is FirstUseState.SaveError -> state.pageBefore
        is FirstUseState.Finished -> FirstUsePage.SafetyAndVoice
    }
    val voiceSlide = page == FirstUsePage.SafetyAndVoice
    val enabled = !saving && !finished
    val scrollState = rememberScrollState()
    LaunchedEffect(page) { scrollState.scrollTo(0) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = { dispatch(FirstUseEvent.Back) },
                    enabled = enabled,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.action_back)) }
                Text(
                    stringResource(R.string.first_use_welcome),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (voiceSlide) {
                    TextButton(
                        onClick = { dispatch(FirstUseEvent.Skip) },
                        enabled = enabled,
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.action_skip)) }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (voiceSlide) OptionalVoiceIllustration() else LocalMusicIllustration()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(
                    if (voiceSlide) R.string.first_use_optional_voice else R.string.first_use_local_music,
                ),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(
                    if (voiceSlide) R.string.first_use_voice_heading else R.string.first_use_local_heading,
                ),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                stringResource(
                    if (voiceSlide) R.string.first_use_voice_body else R.string.first_use_local_body,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (voiceSlide) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.first_use_voice_manual),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.p09_six_commands_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                listOf(
                    R.string.p09_command_play,
                    R.string.p09_command_pause,
                    R.string.p09_command_next,
                    R.string.p09_command_previous,
                    R.string.p09_command_seek_back,
                    R.string.p09_command_play_artist,
                ).forEach { phrase ->
                    Text(
                        stringResource(phrase),
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.first_use_road_safety),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            FirstUsePageIndicator(page)
            if (saving) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(R.string.first_use_saving), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (failed) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.first_use_save_error),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (voiceSlide && !failed) {
                // P09: an optional second-step JIT rationale, not auto-listening.
                // Only the Activity owns the OS request, only VoiceSessionCoordinator
                // can own eventual capture; no new onboarding value slide.
                if (voicePracticeStage == VoicePracticeStage.Idle) {
                    OutlinedButton(
                        onClick = onPracticeVoice,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.first_use_practice_voice)) }
                } else {
                    Text(
                        stringResource(R.string.p09_microphone_purpose_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        stringResource(when (voicePracticeStage) {
                            VoicePracticeStage.Idle -> R.string.first_use_voice_manual
                            VoicePracticeStage.Rationale -> R.string.p09_microphone_purpose_body
                            VoicePracticeStage.AwaitingAndroid -> R.string.p09_microphone_waiting
                            VoicePracticeStage.ReadyForVoiceHandoff -> R.string.p09_voice_handoff_ready
                            VoicePracticeStage.PermissionDenied -> R.string.p09_microphone_denied
                            VoicePracticeStage.ProviderUnavailable -> R.string.p09_provider_unavailable
                            VoicePracticeStage.LanguageUnsupported -> R.string.p09_language_unsupported
                            VoicePracticeStage.CloudConsentDeclined -> R.string.p09_cloud_consent_declined
                        }),
                        modifier = Modifier.padding(top = 8.dp)
                            .semantics { liveRegion = LiveRegionMode.Polite },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (voicePracticeStage == VoicePracticeStage.Rationale) {
                        Button(
                            onClick = onPracticeRationaleContinue,
                            enabled = enabled,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        ) { Text(stringResource(R.string.p09_allow_microphone)) }
                    }
                    OutlinedButton(
                        onClick = onPracticeManualOnly,
                        enabled = enabled && voicePracticeStage != VoicePracticeStage.AwaitingAndroid,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text(stringResource(R.string.p09_manual_only)) }
                }
            }
            Button(
                onClick = {
                    dispatch(if (failed) FirstUseEvent.Retry else FirstUseEvent.Continue)
                },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) {
                Text(
                    stringResource(
                        if (failed) R.string.action_retry
                        else if (voiceSlide) R.string.first_use_finish_without_voice
                        else R.string.action_continue,
                    ),
                )
            }
            if (failed) {
                OutlinedButton(
                    onClick = { dispatch(FirstUseEvent.Back) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.action_back)) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocalMusicIllustration() {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 188.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "♫",
            modifier = Modifier.clearAndSetSemantics { },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.first_use_device_music_placeholder),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OptionalVoiceIllustration() {
    Box(
        modifier = Modifier.fillMaxWidth().heightIn(min = 148.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.first_use_voice_illustration),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.clearAndSetSemantics { },
        )
    }
}

@Composable
private fun FirstUsePageIndicator(page: FirstUsePage) {
    val current = if (page == FirstUsePage.Value) 1 else 2
    val description = stringResource(
        if (current == 1) R.string.first_use_page_one_a11y else R.string.first_use_page_two_a11y,
    )
    Row(
        modifier = Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
            contentDescription = description
        },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(2) { idx ->
            val selected = idx == current - 1
            Box(
                modifier = Modifier.size(if (selected) 12.dp else 10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                    )
                    .clearAndSetSemantics { },
            )
            if (idx == 0) Spacer(modifier = Modifier.width(12.dp))
        }
    }
}
