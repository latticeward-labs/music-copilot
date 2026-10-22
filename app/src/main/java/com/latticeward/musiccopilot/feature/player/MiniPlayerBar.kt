package com.latticeward.musiccopilot.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState

/**
 * CMP-09. Uses the very same PlayerViewModel projection as Full Player.
 * No selected search/library row can override service currentItem.
 */
@Composable
fun MiniPlayerBar(
    state: PlayerUiState,
    onOpenPlayer: () -> Unit,
    onTogglePlayback: () -> Unit,
) {
    if (!state.hasTrack) return
    val displayName = state.title?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.i7_unknown_track)
    val status = playbackStatusLabel(state)
    Surface(color = MusicColors.surfaceRaised) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = MusicSpacing.smDp, vertical = MusicSpacing.xsDp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
        ) {
            Column(
                modifier = Modifier.weight(1f)
                    .clickable(role = Role.Button, onClick = onOpenPlayer)
                    .heightIn(min = MusicSize.touchTargetMinDp)
                    .semantics { contentDescription = "$displayName. Open full player" },
            ) {
                Text(displayName, style = MusicTextStyles.bodyLarge,
                    color = MusicColors.textPrimary, maxLines = 2)
                Text(status, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MusicTextStyles.bodyMedium,
                    color = MusicColors.textSecondary, maxLines = 2)
            }
            Button(
                onClick = onTogglePlayback,
                enabled = state.canControl,
                modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
            ) {
                Text(stringResource(
                    if (state.isPlaying) R.string.i7_pause
                    else R.string.i7_play
                ))
            }
        }
    }
}

@Composable
fun playbackStatusLabel(state: PlayerUiState): String =
    when (state.connection) {
        PlaybackConnectionState.Connecting -> stringResource(R.string.i7_connecting)
        PlaybackConnectionState.Disconnected -> stringResource(R.string.i7_disconnected)
        PlaybackConnectionState.RecoverableError -> stringResource(R.string.i7_controller_error)
        PlaybackConnectionState.Ready -> when (state.snapshot.playbackState) {
            com.latticeward.musiccopilot.playback.api.PlaybackState.Playing ->
                stringResource(R.string.i7_playing)
            com.latticeward.musiccopilot.playback.api.PlaybackState.Buffering ->
                stringResource(R.string.i7_buffering)
            com.latticeward.musiccopilot.playback.api.PlaybackState.Paused ->
                stringResource(R.string.i7_paused)
            com.latticeward.musiccopilot.playback.api.PlaybackState.Ended ->
                stringResource(R.string.i7_ended)
            com.latticeward.musiccopilot.playback.api.PlaybackState.Error ->
                stringResource(R.string.i7_playback_error)
            com.latticeward.musiccopilot.playback.api.PlaybackState.Idle ->
                stringResource(R.string.i7_no_track)
        }
    }
