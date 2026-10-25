package com.latticeward.musiccopilot.feature.voice

import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicRadius
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayExit
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayPresentationPolicy
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayStatus
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * WF-08 single-origin, one-layer Voice panel. The same code projects the
 * same Coordinator in Full Player and Driving without new navigation, players,
 * recognizers, transcript history or modal TalkBack focus traps.
 *
 * Back/focus restoration remain under the two origin screens. This panel
 * provides explicit exit, safe retry, ordered semantic actions and a polite
 * single state announcement; no unconditional focus grab on state changes.
 */
@Composable
fun VoiceSessionPanel(
    state: VoiceSessionState,
    permissionStage: VoicePracticeStage,
    onPermissionContinue: () -> Unit,
    onPermissionCancel: () -> Unit,
    onVoiceCancel: () -> Unit,
    onVoiceRetry: () -> Unit,
    focusRequester: FocusRequester,
    driving: Boolean,
    modifier: Modifier = Modifier,
) {
    val projected = VoiceOverlayPresentationPolicy.project(state, permissionStage)
    if (!projected.visible) return

    val heading = when (projected.status) {
        VoiceOverlayStatus.Hidden -> return
        VoiceOverlayStatus.PermissionRationale -> stringResource(R.string.i10_voice_permission_title)
        VoiceOverlayStatus.PermissionPending -> stringResource(R.string.i10_voice_permission_waiting)
        VoiceOverlayStatus.Listening -> stringResource(R.string.i10_voice_listening)
        VoiceOverlayStatus.Processing -> stringResource(R.string.i10_voice_processing)
        VoiceOverlayStatus.ConfirmationUnavailable ->
            stringResource(R.string.i10_voice_confirmation_unavailable)
        VoiceOverlayStatus.Executed -> stringResource(R.string.i10_voice_executed)
        VoiceOverlayStatus.Timeout -> stringResource(R.string.i10_voice_timeout)
        VoiceOverlayStatus.NotRecognized -> stringResource(R.string.i10_voice_not_recognized)
        VoiceOverlayStatus.Unavailable -> stringResource(R.string.i10_voice_unavailable)
    }
    val explanation = when (projected.status) {
        VoiceOverlayStatus.PermissionRationale ->
            stringResource(R.string.i9_voice_permission_explanation)
        VoiceOverlayStatus.PermissionPending ->
            stringResource(R.string.i10_voice_permission_result_note)
        VoiceOverlayStatus.Listening ->
            stringResource(R.string.i10_voice_listen_help)
        VoiceOverlayStatus.Processing ->
            stringResource(R.string.i10_voice_processing_help)
        VoiceOverlayStatus.Executed ->
            stringResource(R.string.i10_voice_executed_help)
        VoiceOverlayStatus.Timeout, VoiceOverlayStatus.NotRecognized,
        VoiceOverlayStatus.Unavailable, VoiceOverlayStatus.ConfirmationUnavailable ->
            stringResource(R.string.i10_voice_manual_fallback)
        VoiceOverlayStatus.Hidden -> ""
    }
    val actionSize = if (driving) MusicSize.drivingExitMinDp else MusicSize.touchTargetMinDp
    val textStyle = if (driving) MusicTextStyles.drivingMeta else MusicTextStyles.bodyLarge
    val alignment = if (driving) TextAlign.Center else TextAlign.Start

    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = MusicSpacing.xsDp),
        verticalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
    ) {
        Text(
            text = heading,
            modifier = Modifier.fillMaxWidth()
                .focusRequester(focusRequester)
                .focusable()
                .semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
            color = MusicColors.textPrimary,
            style = textStyle,
            textAlign = alignment,
        )
        Text(
            text = explanation,
            modifier = Modifier.fillMaxWidth(),
            color = MusicColors.textSecondary,
            style = MusicTextStyles.bodyMedium,
            textAlign = alignment,
        )
        if (projected.showPermissionContinue) {
            val label = stringResource(R.string.i9_a11y_continue_permission)
            Button(
                onClick = onPermissionContinue,
                modifier = Modifier.fillMaxWidth().heightIn(min = actionSize)
                    .then(voiceActionFocusOutline())
                    .semantics { contentDescription = label },
            ) { Text(stringResource(R.string.i9_voice_continue)) }
        }
        if (projected.showRetry) {
            val label = stringResource(R.string.i10_voice_retry_a11y)
            OutlinedButton(
                onClick = onVoiceRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = actionSize)
                    .then(voiceActionFocusOutline())
                    .semantics { contentDescription = label },
            ) { Text(stringResource(R.string.i9_voice_retry)) }
        }
        val exitLabel = when (projected.exit) {
            VoiceOverlayExit.CancelCapture -> stringResource(R.string.i10_voice_cancel_active)
            VoiceOverlayExit.DismissPermission -> stringResource(R.string.i10_voice_not_now)
            VoiceOverlayExit.ReturnToManual -> stringResource(R.string.i10_voice_back_manual)
        }
        val exitA11y = when (projected.exit) {
            VoiceOverlayExit.CancelCapture -> stringResource(R.string.i10_voice_cancel_a11y)
            VoiceOverlayExit.DismissPermission -> stringResource(R.string.i10_voice_not_now_a11y)
            VoiceOverlayExit.ReturnToManual -> stringResource(R.string.i10_voice_back_manual_a11y)
        }
        OutlinedButton(
            onClick = if (projected.exit == VoiceOverlayExit.DismissPermission)
                onPermissionCancel else onVoiceCancel,
            modifier = Modifier.fillMaxWidth().heightIn(min = actionSize)
                .then(voiceActionFocusOutline())
                .semantics { contentDescription = exitA11y },
        ) { Text(exitLabel) }
    }
}

/** Keyboard/switch navigation focus indicator; TalkBack keeps native focus. */
@Composable
private fun voiceActionFocusOutline(): Modifier {
    var focused by remember { mutableStateOf(false) }
    return Modifier
        .onFocusChanged { focused = it.isFocused }
        .border(
            width = if (focused) MusicSize.focusRingWidthDp else 0.dp,
            color = if (focused) MusicColors.focusRing else MusicColors.background,
            shape = RoundedCornerShape(MusicRadius.mdDp),
        )
}
