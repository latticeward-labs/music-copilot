package com.latticeward.musiccopilot.feature.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WF-06 / HF-06: one composable projection of service-owned playback.
 * No MediaStore scan, queue mutation, Player or MediaSession in presentation.
 * Back returns to its origin without stopping audio.
 */
@Composable
fun FullPlayerScreen(
    model: PlayerViewModel,
    state: PlayerUiState,
    library: LibraryRepository,
    canReadLocalMetadata: Boolean,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    val snap = state.snapshot
    val title = state.title?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.i7_unknown_track)
    val duration = snap.durationMs?.takeIf { it > 0L }
    var scrubFraction by remember { mutableFloatStateOf(0f) }
    var scrubbing by remember { mutableStateOf(false) }
    LaunchedEffect(snap.currentItem, snap.queueRevision) { scrubbing = false }
    val progress = if (duration == null) 0f
        else (snap.positionMs.toDouble() / duration.toDouble()).toFloat().coerceIn(0f, 1f)

    Surface(modifier = Modifier.fillMaxSize(), color = MusicColors.background) {
        LazyColumn(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(MusicSpacing.mdDp)) {
            item(key = "player_header") {
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = MusicSpacing.mdDp, vertical = MusicSpacing.smDp),
                    horizontalArrangement = Arrangement.spacedBy(MusicSpacing.mdDp)) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.action_back)) }
                    Text(stringResource(R.string.i7_player_title),
                        modifier = Modifier.semantics { heading() },
                        style = MusicTextStyles.titleLarge,
                        color = MusicColors.textPrimary)
                }
            }
            item(key = "player_current") {
                Column(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = MusicSpacing.mdDp),
                    verticalArrangement = Arrangement.spacedBy(MusicSpacing.smDp)) {
                    Text(title,
                        modifier = Modifier.semantics { heading() },
                        style = MusicTextStyles.titleLarge,
                        color = MusicColors.textPrimary)
                    Text(
                        listOfNotNull(state.artist, state.album).filter { it.isNotBlank() }
                            .joinToString(" • ").ifBlank {
                                stringResource(R.string.i7_local_music)
                            },
                        style = MusicTextStyles.bodyLarge,
                        color = MusicColors.textSecondary,
                    )
                    Text(playbackStatusLabel(state),
                        style = MusicTextStyles.bodyMedium,
                        color = MusicColors.primary)
                    if (state.requestFailed || snap.lastError != null) {
                        Text(stringResource(R.string.i7_action_failed),
                            color = MusicColors.textPrimary,
                            style = MusicTextStyles.bodyLarge)
                    }
                    if (!state.hasTrack) {
                        Text(stringResource(R.string.i7_no_track),
                            style = MusicTextStyles.bodyLarge,
                            color = MusicColors.textSecondary)
                    }
                }
            }
            item(key = "player_seek") {
                Column(modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = MusicSpacing.mdDp)) {
                    val position = if (scrubbing && duration != null)
                        (duration.toDouble() * scrubFraction).toLong()
                    else snap.positionMs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(formatTime(position), color = MusicColors.textSecondary)
                        Text(duration?.let(::formatTime)
                            ?: stringResource(R.string.i7_unknown_duration),
                            color = MusicColors.textSecondary)
                    }
                    Slider(
                        value = if (scrubbing) scrubFraction else progress,
                        onValueChange = {
                            scrubFraction = it.coerceIn(0f, 1f)
                            scrubbing = true
                        },
                        onValueChangeFinished = {
                            if (duration != null) {
                                model.seekTo((duration.toDouble() * scrubFraction).toLong())
                            }
                            scrubbing = false
                        },
                        enabled = state.canControl && snap.canSeek && duration != null,
                        modifier = Modifier.fillMaxWidth().heightIn(
                            min = MusicSize.touchTargetMinDp
                        ).semantics {
                            contentDescription = "Seek within current local track"
                        },
                    )
                }
            }
            item(key = "player_controls") {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = MusicSpacing.mdDp),
                    verticalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = model::previous,
                            enabled = state.canControl && snap.canSkipPrevious,
                            modifier = Modifier.weight(1f).heightIn(
                                min = MusicSize.touchTargetMinDp
                            ),
                        ) { Text(stringResource(R.string.i7_previous)) }
                        Button(
                            onClick = model::togglePlayPause,
                            enabled = state.canControl,
                            modifier = Modifier.weight(1f).heightIn(
                                min = MusicSize.touchTargetMinDp
                            ),
                        ) {
                            Text(stringResource(
                                if (state.isPlaying) R.string.i7_pause else R.string.i7_play
                            ))
                        }
                        OutlinedButton(
                            onClick = model::next,
                            enabled = state.canControl && snap.canSkipNext,
                            modifier = Modifier.weight(1f).heightIn(
                                min = MusicSize.touchTargetMinDp
                            ),
                        ) { Text(stringResource(R.string.i7_next)) }
                    }
                    OutlinedButton(
                        onClick = model::seekBackTenSeconds,
                        enabled = state.canControl && snap.canSeek,
                        modifier = Modifier.fillMaxWidth().heightIn(
                            min = MusicSize.touchTargetMinDp
                        ),
                    ) { Text(stringResource(R.string.i7_seek_back)) }
                    if (state.pending) {
                        Text(stringResource(R.string.i7_command_pending),
                            style = MusicTextStyles.bodyMedium,
                            color = MusicColors.textSecondary)
                    }
                }
            }
            item(key = "queue_header") {
                Text(stringResource(R.string.i7_queue_title, snap.queue.size),
                    modifier = Modifier.padding(horizontal = MusicSpacing.mdDp)
                        .semantics { heading() },
                    style = MusicTextStyles.titleMedium,
                    color = MusicColors.textPrimary)
            }
            itemsIndexed(snap.queue, key = { index, _ ->
                "queue-${snap.queueRevision}-$index"
            }) { index, ref ->
                val isSelected = index == snap.currentIndex
                val rowTitle = if (isSelected) state.title else null
                val fallback = stringResource(R.string.i7_queue_track_number, index + 1)
                val display by produceState(initialValue = rowTitle,
                    key1 = ref, key2 = canReadLocalMetadata) {
                    value = rowTitle
                    if (canReadLocalMetadata && rowTitle == null) {
                        val resolved = withContext(Dispatchers.IO) { library.get(ref) }
                        value = if (resolved is AppResult.Success &&
                            resolved.value.ref == ref) resolved.value.title else null
                    }
                }
                Column {
                    Text(
                        (display?.takeIf { it.isNotBlank() } ?: fallback) +
                            if (isSelected) " • " +
                                stringResource(R.string.i7_current_track_marker) else "",
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = MusicSize.trackRowMinHeightDp)
                            .clickable(
                                enabled = state.canControl && index != snap.currentIndex,
                                role = Role.Button,
                            ) {
                                model.selectQueueOccurrence(index, snap.queueRevision)
                            }
                            .semantics { selected = isSelected }
                            .padding(horizontal = MusicSpacing.mdDp,
                                vertical = MusicSpacing.smDp),
                        style = MusicTextStyles.bodyLarge,
                        color = if (isSelected) MusicColors.primary
                            else MusicColors.textPrimary,
                    )
                    HorizontalDivider(color = MusicColors.border)
                }
            }
            item(key = "queue_footer") {
                Text(stringResource(R.string.i7_service_queue_note),
                    modifier = Modifier.padding(MusicSpacing.mdDp),
                    style = MusicTextStyles.bodyMedium,
                    color = MusicColors.textSecondary)
            }
        }
    }
}

/** Pure display formatting; never moves the service playback position. */
private fun formatTime(positionMs: Long): String {
    val seconds = positionMs.coerceAtLeast(0L) / 1000
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
