package com.latticeward.musiccopilot.feature.driving

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicGrid
import com.latticeward.musiccopilot.app.theme.MusicRadius
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.driving.DrivingAction
import com.latticeward.musiccopilot.core.application.driving.DrivingActionLabel
import com.latticeward.musiccopilot.core.application.driving.DrivingControlState
import com.latticeward.musiccopilot.core.application.driving.DrivingDisabledReason
import com.latticeward.musiccopilot.core.application.driving.DrivingPresentationState
import com.latticeward.musiccopilot.core.application.driving.DrivingRecoveryMessage
import com.latticeward.musiccopilot.core.application.driving.DrivingRecoveryPolicy
import com.latticeward.musiccopilot.core.application.driving.DrivingViewState
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceAvailability
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Issue #9 P04 — actual WF-07/HF-07 Driving Compose surface.
 *
 * UI is a disposable observer; the single playback service owns the selected
 * MediaRef, queue, position, state and audio route. This screen never creates
 * Media3 Player, transports playback on its own, scans MediaStore or records
 * Voice. P06 supplies onTransport; P07 supplies onMic after real eligibility.
 *
 * P04 intentionally leaves controls disabled until real callbacks exist:
 * presenting clickable but no-op transport/Mic buttons would be misleading.
 * P05 extends the Paused/NoTrack/error variants beyond this P04 structure.
 */
@Composable
fun DrivingModeScreen(
    state: DrivingPresentationState,
    currentItem: MediaRef?,
    library: LibraryRepository,
    canReadLocalMetadata: Boolean,
    onExit: () -> Unit,
    lastError: AppError? = null,
    requestFailed: Boolean = false,
    onTransport: ((DrivingAction) -> Unit)? = null,
    onMic: (() -> Unit)? = null,
    voiceState: VoiceSessionState = VoiceSessionState.Idle,
    voiceAvailability: DrivingVoiceAvailability = DrivingVoiceAvailability.ProviderUnavailable,
    voicePermissionStage: VoicePracticeStage = VoicePracticeStage.Idle,
    voiceStartRejected: Boolean = false,
    onVoicePermissionContinue: () -> Unit = {},
    onVoicePermissionCancel: () -> Unit = {},
    onVoiceCancel: () -> Unit = {},
    onVoiceRetry: () -> Unit = {},
) {
    // Key on the verified *service* currentItem, not last clicked row or
    // a previously loaded title. Every change hides old metadata immediately.
    val safeRef = currentItem.takeIf {
        state.showCurrentTrackMetadata && canReadLocalMetadata
    }
    val resolved by produceState<DrivingResolvedMetadata?>(
        initialValue = null, key1 = safeRef, key2 = canReadLocalMetadata,
    ) {
        value = null
        if (safeRef != null) {
            try {
                val result = withContext(Dispatchers.IO) { library.get(safeRef) }
                value = if (result is AppResult.Success && result.value.ref == safeRef)
                    DrivingResolvedMetadata(safeRef, result.value) else null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Metadata is display-only. A revoked/stale local lookup must
                // not crash Driving, leak a path or invent a playing item.
                value = null
            }
        }
    }
    // A cancelled old lookup or a permission revocation cannot reattach stale
    // title/artist to a newer service-selected occurrence.
    val metadata = resolved?.takeIf { it.ref == safeRef }?.item
    val recovery = DrivingRecoveryPolicy.project(state, lastError, requestFailed)
    val voiceActive = voiceState != VoiceSessionState.Idle
    val voicePermissionPrompt = voicePermissionStage == VoicePracticeStage.Rationale ||
        voicePermissionStage == VoicePracticeStage.AwaitingAndroid
    val micFocusRequester = remember { FocusRequester() }
    val overlayFocusRequester = remember { FocusRequester() }
    var overlayWasOpen by remember { mutableStateOf(false) }
    val overlayIsOpen = voiceActive || voicePermissionPrompt
    // Request focus only on the overlay open/close transition, not on each
    // transient STT state / playback position / composition frame.
    LaunchedEffect(overlayIsOpen, state.micEnabled) {
        if (overlayIsOpen) overlayFocusRequester.requestFocus()
        else if (overlayWasOpen && state.micEnabled && onMic != null)
            micFocusRequester.requestFocus()
        overlayWasOpen = overlayIsOpen
    }
    // A Voice Back cancels capture/permission overlay in this same route.
    // Otherwise P03's root BackHandler exits Driving immediately.
    BackHandler(enabled = voiceActive || voicePermissionPrompt) {
        if (voiceActive) onVoiceCancel() else onVoicePermissionCancel()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MusicColors.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .padding(horizontal = MusicGrid.drivingMarginDp, vertical = MusicSpacing.smDp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Fixed, always-accessible Exit. Never scroll it out of reach at
            // 200% font scaling, narrow devices or long error explanations.
            val exitA11y = stringResource(R.string.i9_a11y_exit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
            ) {
                Text(
                    text = stringResource(R.string.i9_driving_heading),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MusicTextStyles.titleMedium,
                    color = MusicColors.textPrimary,
                )
                Button(
                    onClick = onExit,
                    modifier = drivingFocusOutline()
                        .heightIn(min = MusicSize.drivingExitMinDp)
                        .semantics {
                            contentDescription = exitA11y
                        },
                ) {
                    Text(stringResource(R.string.i9_driving_exit_short))
                }
            }
            // Only body scrolls; screen safety Exit remains pinned above it.
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = MusicSpacing.mdDp, bottom = MusicSpacing.mdDp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MusicSpacing.mdDp),
            ) {
            // State comes before verified metadata in TalkBack traversal.
            // Do not announce a guessed service result or every position tick.
            val playbackStatusText = when (state.view) {
                    DrivingViewState.Playing -> stringResource(R.string.i9_driving_state_playing)
                    DrivingViewState.Paused -> stringResource(R.string.i9_driving_state_paused)
                    DrivingViewState.NoTrack -> stringResource(R.string.i9_driving_state_no_track)
                }.uppercase()
            Text(
                text = playbackStatusText,
                modifier = Modifier.fillMaxWidth().semantics {
                    heading()
                    stateDescription = playbackStatusText
                    liveRegion = LiveRegionMode.Polite
                },
                color = MusicColors.primary,
                style = MusicTextStyles.drivingState,
                textAlign = TextAlign.Center,
            )

            if (recovery.showArtworkAndMetadata) {
                // CC-05/CC-06: subordinate CMP-08 neutral artwork only.
                // Never fabricate a bitmap or cache a revoked private file.
                // Artwork is decorative while no verified local bitmap exists.
                // Shrink before touch targets when fonts grow to 200%.
                val artworkSize = when {
                    LocalDensity.current.fontScale >= 2f -> 72.dp
                    LocalDensity.current.fontScale >= 1.5f -> 96.dp
                    else -> 160.dp
                }
                Box(
                    modifier = Modifier.size(artworkSize).background(
                        MusicColors.surfaceVariant,
                        RoundedCornerShape(MusicRadius.mdDp),
                    ).clearAndSetSemantics { /* decorative neutral placeholder */ },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(R.string.i9_driving_artwork_placeholder),
                        color = MusicColors.textMuted,
                        style = MusicTextStyles.labelLarge,
                    )
                }
                // CMP-07: service-verified MediaRef only. The producer cancels
                // prior lookups; mismatched references never render.
                // Group related local title and artist; action buttons remain
                // their own independent accessibility focus targets.
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                ) {
                    Text(
                        text = metadata?.title?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.i7_unknown_track),
                        modifier = Modifier.fillMaxWidth(),
                        color = MusicColors.textPrimary,
                        style = MusicTextStyles.drivingTitle,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = metadata?.artist?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.i9_driving_unknown_artist),
                        modifier = Modifier.fillMaxWidth(),
                        color = MusicColors.textSecondary,
                        style = MusicTextStyles.drivingMeta,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                // CC-07: NO TRACK has no artwork or claimed current artist.
                Text(
                    stringResource(R.string.i9_driving_no_current_track),
                    modifier = Modifier.fillMaxWidth(),
                    color = MusicColors.textPrimary,
                    style = MusicTextStyles.drivingTitle,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.i9_driving_exit_to_browse),
                    modifier = Modifier.fillMaxWidth(),
                    color = MusicColors.textSecondary,
                    style = MusicTextStyles.drivingMeta,
                    textAlign = TextAlign.Center,
                )
                // No trustworthy last-item reference exists in P05. No
                // speculative Play Last action or implicit queue mutation.
            }

            // Single, precedence-ordered reason from the actual service,
            // controller and ACK flag; never print arbitrary exception text.
            val explanation = drivingRecoveryMessage(recovery.message)
            if (explanation != null) {
                Text(
                    text = explanation,
                    modifier = Modifier.fillMaxWidth().semantics {
                        liveRegion = LiveRegionMode.Polite
                    },
                    color = MusicColors.textSecondary,
                    style = MusicTextStyles.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            if (voiceActive || voicePermissionPrompt) {
                // WF-08 Voice state is an origin-owned inline overlay, NOT a
                // NavHost destination. Transport cannot be tapped under it.
                DrivingVoiceSessionOverlay(
                    state = voiceState,
                    permissionStage = voicePermissionStage,
                    onPermissionContinue = onVoicePermissionContinue,
                    onPermissionCancel = onVoicePermissionCancel,
                    onVoiceCancel = onVoiceCancel,
                    onVoiceRetry = onVoiceRetry,
                    focusRequester = overlayFocusRequester,
                )
            } else if (recovery.showTransportControls) {
                if (onTransport == null) {
                    Text(
                        stringResource(R.string.i9_driving_transports_not_ready),
                        modifier = Modifier.fillMaxWidth(),
                        style = MusicTextStyles.bodyMedium,
                        color = MusicColors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }
                // CC-05/CC-06: fixed four approved transports, P06 supplies
                // their real service-ACK callback. They are disabled before P06.
                // On small widths / large font, reflow to 2x2, not tiny targets.
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val split = LocalDensity.current.fontScale >= 1.5f ||
                        maxWidth < 340.dp
                    if (split) {
                        Column(verticalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp)) {
                            state.controls.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                                ) {
                                    pair.forEach { control ->
                                        DrivingTransportButton(
                                            control = control,
                                            modifier = Modifier.weight(1f),
                                            onTransport = onTransport,
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                        ) {
                            state.controls.forEach { control ->
                                DrivingTransportButton(
                                    control = control,
                                    modifier = Modifier.weight(1f),
                                    onTransport = onTransport,
                                )
                            }
                        }
                    }
                }
            }

            // Mic is a one-shot PTT entry, never an always-on listener.
            // A missing permission opens an explicit rationale; the Android
            // result NEVER starts recognition without another user tap.
            if (!voiceActive && !voicePermissionPrompt) {
                val micA11y = stringResource(R.string.i9_a11y_mic)
                OutlinedButton(
                    onClick = { onMic?.invoke() },
                    enabled = state.micEnabled && onMic != null,
                    modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp)
                        .focusRequester(micFocusRequester)
                        .then(drivingFocusOutline())
                        .heightIn(min = MusicSize.drivingMicMinDp)
                        .semantics {
                            contentDescription = micA11y
                        },
                ) {
                    Text(
                        stringResource(R.string.i9_driving_mic_voice),
                        style = MusicTextStyles.drivingMeta,
                        textAlign = TextAlign.Center,
                    )
                }
                val statusText = when {
                    voiceStartRejected -> stringResource(R.string.i9_voice_start_unavailable)
                    voicePermissionStage == VoicePracticeStage.ReadyForVoiceHandoff ->
                        stringResource(R.string.i9_voice_granted_tap_again)
                    voicePermissionStage == VoicePracticeStage.PermissionDenied ->
                        stringResource(R.string.i9_voice_permission_denied)
                    voicePermissionStage == VoicePracticeStage.ProviderUnavailable ||
                        voiceAvailability == DrivingVoiceAvailability.ProviderUnavailable ->
                        stringResource(R.string.i9_voice_local_provider_unavailable)
                    voiceAvailability == DrivingVoiceAvailability.NeedsPermission ->
                        stringResource(R.string.i9_voice_permission_rationale_brief)
                    else -> null
                }
                if (statusText != null) Text(
                    statusText,
                    modifier = Modifier.fillMaxWidth().semantics {
                        liveRegion = LiveRegionMode.Polite
                    },
                    style = MusicTextStyles.bodyMedium,
                    color = MusicColors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                stringResource(R.string.i9_driving_one_layer_note),
                style = MusicTextStyles.bodyMedium,
                color = MusicColors.textMuted,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.first_use_road_safety),
                modifier = Modifier.fillMaxWidth(),
                style = MusicTextStyles.bodyMedium,
                color = MusicColors.textSecondary,
                textAlign = TextAlign.Center,
            )
            } // scrollable body
        } // fixed header + body
    }
}

private data class DrivingResolvedMetadata(val ref: MediaRef, val item: LibraryItem)

@Composable
private fun DrivingTransportButton(
    control: DrivingControlState,
    modifier: Modifier,
    onTransport: ((DrivingAction) -> Unit)?,
) {
    val label = when (control.label) {
        DrivingActionLabel.Previous -> stringResource(R.string.i7_previous)
        DrivingActionLabel.Play -> stringResource(R.string.i7_play)
        DrivingActionLabel.Pause -> stringResource(R.string.i7_pause)
        DrivingActionLabel.Next -> stringResource(R.string.i7_next)
        DrivingActionLabel.SeekBack10Seconds -> stringResource(R.string.i9_driving_seek_back_short)
    }
    val spokenLabel = when (control.label) {
        DrivingActionLabel.Previous -> stringResource(R.string.i9_a11y_previous)
        DrivingActionLabel.Play -> stringResource(R.string.i9_a11y_play)
        DrivingActionLabel.Pause -> stringResource(R.string.i9_a11y_pause)
        DrivingActionLabel.Next -> stringResource(R.string.i9_a11y_next)
        DrivingActionLabel.SeekBack10Seconds -> stringResource(R.string.i9_a11y_seek_back)
    }
    val disabledText = when {
        onTransport == null -> stringResource(R.string.i9_a11y_controller_not_ready)
        else -> drivingDisabledReason(control.disabledReason)
    }
    val enabledNow = control.enabled && onTransport != null
    OutlinedButton(
        onClick = { onTransport?.invoke(control.action) },
        enabled = enabledNow,
        modifier = modifier.then(drivingFocusOutline())
            .heightIn(min = MusicSize.drivingControlMinDp)
            .semantics {
                contentDescription = spokenLabel
                if (!enabledNow) stateDescription = disabledText
            },
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = MusicTextStyles.labelLarge,
        )
    }
}

@Composable
private fun drivingRecoveryMessage(message: DrivingRecoveryMessage?): String? = when (message) {
    null, DrivingRecoveryMessage.NoTrackBrowse -> null
    DrivingRecoveryMessage.NoTrackItemUnavailable ->
        stringResource(R.string.i9_driving_no_track_item_unavailable)
    DrivingRecoveryMessage.PausedManualResume ->
        stringResource(R.string.i9_driving_paused_resume_note)
    DrivingRecoveryMessage.PlaybackConnecting ->
        stringResource(R.string.i7_connecting)
    DrivingRecoveryMessage.PlaybackDisconnected ->
        stringResource(R.string.i7_disconnected)
    DrivingRecoveryMessage.PlaybackControllerUnavailable ->
        stringResource(R.string.i7_controller_error)
    DrivingRecoveryMessage.PlaybackBuffering ->
        stringResource(R.string.i7_buffering)
    DrivingRecoveryMessage.PlaybackFailed ->
        stringResource(R.string.i7_playback_error)
    DrivingRecoveryMessage.LocalItemUnavailable ->
        stringResource(R.string.i7_unavailable_help)
    DrivingRecoveryMessage.RouteOutputChanged ->
        stringResource(R.string.i8_output_changed)
    DrivingRecoveryMessage.AudioFocusUnavailable ->
        stringResource(R.string.i8_focus_not_recovered)
    DrivingRecoveryMessage.InterruptionSafePaused ->
        stringResource(R.string.i8_audio_temporarily_paused)
    DrivingRecoveryMessage.CommandPending ->
        stringResource(R.string.i7_command_pending)
    DrivingRecoveryMessage.CommandRejected ->
        stringResource(R.string.i7_action_failed)
}

/**
 * Same-route Voice overlay. Only the application VoiceSessionCoordinator
 * supplies these states; a disabled provider never creates fake Listening.
 * Confirming is display-only until #10/#11 implement token confirmation.
 */
@Composable
private fun DrivingVoiceSessionOverlay(
    state: VoiceSessionState,
    permissionStage: VoicePracticeStage,
    onPermissionContinue: () -> Unit,
    onPermissionCancel: () -> Unit,
    onVoiceCancel: () -> Unit,
    onVoiceRetry: () -> Unit,
    focusRequester: FocusRequester,
) {
    val rationale = permissionStage == VoicePracticeStage.Rationale
    val awaiting = permissionStage == VoicePracticeStage.AwaitingAndroid
    val headingText = when {
        rationale -> stringResource(R.string.i9_voice_permission_heading)
        awaiting -> stringResource(R.string.i9_voice_permission_wait)
        state is VoiceSessionState.Listening -> stringResource(R.string.i9_voice_listening)
        state is VoiceSessionState.Processing -> stringResource(R.string.i9_voice_processing)
        state is VoiceSessionState.Confirming -> stringResource(R.string.i9_voice_confirmation_unavailable)
        state is VoiceSessionState.Executed -> stringResource(R.string.i9_voice_executed)
        state is VoiceSessionState.Failed -> stringResource(R.string.i9_voice_failed)
        else -> stringResource(R.string.i9_voice_permission_heading)
    }
    Text(
        headingText,
        modifier = Modifier.fillMaxWidth()
            .focusRequester(focusRequester).focusable()
            .semantics {
                heading()
                liveRegion = LiveRegionMode.Polite
                stateDescription = headingText
            },
        style = MusicTextStyles.drivingMeta,
        color = MusicColors.textPrimary,
        textAlign = TextAlign.Center,
    )
    val continueA11y = stringResource(R.string.i9_a11y_continue_permission)
    val retryA11y = stringResource(R.string.i9_a11y_retry_voice)
    val cancelA11y = stringResource(R.string.i9_a11y_cancel_voice)
    if (rationale) {
        Text(
            stringResource(R.string.i9_voice_permission_explanation),
            style = MusicTextStyles.bodyMedium,
            color = MusicColors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onPermissionContinue,
            modifier = Modifier.fillMaxWidth().then(drivingFocusOutline())
                .heightIn(min = MusicSize.drivingExitMinDp)
                .semantics { contentDescription = continueA11y },
        ) { Text(stringResource(R.string.i9_voice_continue)) }
    }
    if (state is VoiceSessionState.Failed && state.canRetry) {
        OutlinedButton(
            onClick = onVoiceRetry,
            modifier = Modifier.fillMaxWidth().then(drivingFocusOutline())
                .heightIn(min = MusicSize.drivingExitMinDp)
                .semantics { contentDescription = retryA11y },
        ) { Text(stringResource(R.string.i9_voice_retry)) }
    }
    // Cancel remains available while Android is awaiting a permission result;
    // it drops the intent but cannot dismiss the OS-owned dialog.
    OutlinedButton(
        onClick = if (rationale || awaiting) onPermissionCancel else onVoiceCancel,
        modifier = Modifier.fillMaxWidth().then(drivingFocusOutline())
            .heightIn(min = MusicSize.drivingExitMinDp)
            .semantics { contentDescription = cancelA11y },
    ) { Text(stringResource(R.string.i9_voice_cancel)) }
}

/** P08 keyboard/switch-access focus outline; Android TalkBack draws its own
 * accessibility rectangle. No extra clickable layer or semantics group.
 * Uses the exact approved Night Signal focus token from MusicColors.
 */
@Composable
private fun drivingFocusOutline(): Modifier {
    var focused by remember { mutableStateOf(false) }
    return Modifier
        .onFocusChanged { focused = it.isFocused }
        .border(
            width = if (focused) MusicSize.focusRingWidthDp else 0.dp,
            color = if (focused) MusicColors.focusRing else MusicColors.background,
            shape = RoundedCornerShape(MusicRadius.mdDp),
        )
}

/** Only sanitized resource-backed reasons, never raw OS/private data. */
@Composable
private fun drivingDisabledReason(reason: DrivingDisabledReason?): String = when (reason) {
    DrivingDisabledReason.NoCurrentTrack -> stringResource(R.string.i9_a11y_missing_track)
    DrivingDisabledReason.ControllerNotReady ->
        stringResource(R.string.i9_a11y_controller_not_ready)
    DrivingDisabledReason.PlaybackUnavailable ->
        stringResource(R.string.i9_a11y_playback_unavailable)
    DrivingDisabledReason.CommandPending -> stringResource(R.string.i9_a11y_command_pending)
    DrivingDisabledReason.Unsupported -> stringResource(R.string.i9_a11y_unsupported_action)
    null -> stringResource(R.string.i9_a11y_action_unavailable)
}
