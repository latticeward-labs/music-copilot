package com.latticeward.musiccopilot.feature.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot

/**
 * WF-05/HF-05: Back+Search bar, editable local SearchField, result count,
 * artist/song/album rows, Home/Search/Liked/Settings bottom navigation.
 * Only OS-verified local index state can drive results. P10/#7 owns playback.
 */
@Composable
fun LocalSearchScreen(
    model: LocalSearchViewModel,
    index: LibraryIndexSnapshot,
    permission: PermissionStatus,
    onBack: () -> Unit,
    onOpenGroup: (LibraryBrowseEntry) -> Unit,
    onSettings: () -> Unit,
    onReviewAccess: () -> Unit,
) {
    val state by model.ui.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    LaunchedEffect(index.revision, index.status, index.itemCount, permission) {
        model.onVerifiedIndex(index, permission == PermissionStatus.Granted)
    }
    BackHandler { onBack() }

    Surface(modifier = Modifier.fillMaxSize(), color = MusicColors.background) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = MusicSize.appBarHeightDp)
                    .padding(horizontal = MusicSpacing.mdDp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.action_back)) }
                Text(
                    stringResource(R.string.i6_search_title),
                    modifier = Modifier.padding(start = MusicSpacing.mdDp)
                        .semantics { heading() },
                    style = MusicTextStyles.titleLarge,
                    color = MusicColors.textPrimary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = MusicSpacing.mdDp, vertical = MusicSpacing.xsDp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = model::updateQuery,
                    modifier = Modifier.weight(1f)
                        .heightIn(min = MusicSize.touchTargetMinDp),
                    label = { Text(stringResource(R.string.i6_search_field)) },
                    placeholder = { Text(stringResource(R.string.i6_search_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
                    minLines = 1, maxLines = 3,
                    enabled = state.status != LocalSearchStatus.Unavailable,
                )
                OutlinedButton(
                    onClick = model::clearQuery,
                    enabled = state.query.isNotEmpty(),
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.i6_search_clear)) }
            }

            when (state.status) {
                LocalSearchStatus.Idle -> {
                    SearchStatusMessage(
                        Modifier.weight(1f),
                        stringResource(R.string.i6_search_idle),
                    )
                }
                LocalSearchStatus.Searching -> {
                    Box(Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MusicColors.primary)
                            Text(
                                stringResource(R.string.i6_search_searching),
                                modifier = Modifier.padding(MusicSpacing.mdDp),
                                style = MusicTextStyles.bodyLarge,
                                color = MusicColors.textSecondary,
                            )
                        }
                    }
                }
                LocalSearchStatus.Unavailable -> {
                    Box(Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(MusicSpacing.mdDp)) {
                            Text(stringResource(R.string.i6_search_unavailable),
                                color = MusicColors.textPrimary,
                                style = MusicTextStyles.bodyLarge)
                            Button(
                                onClick = onReviewAccess,
                                modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                            ) { Text(stringResource(R.string.action_review_access)) }
                        }
                    }
                }
                LocalSearchStatus.NoResults -> {
                    Box(Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.padding(MusicSpacing.mdDp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(stringResource(R.string.i6_search_no_results),
                                modifier = Modifier.semantics { heading() },
                                color = MusicColors.textPrimary, style = MusicTextStyles.titleMedium)
                            Text(stringResource(R.string.i6_search_no_results_hint),
                                color = MusicColors.textSecondary, style = MusicTextStyles.bodyLarge)
                        }
                    }
                }
                LocalSearchStatus.Error, LocalSearchStatus.Stale -> {
                    Box(Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.padding(MusicSpacing.mdDp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(R.string.i6_search_failed),
                                modifier = Modifier.semantics { heading() },
                                color = MusicColors.textPrimary,
                                style = MusicTextStyles.titleMedium,
                            )
                            Text(
                                stringResource(
                                    if (state.status == LocalSearchStatus.Stale)
                                        R.string.i6_search_stale_hint
                                    else R.string.i6_search_error_hint
                                ),
                                color = MusicColors.textSecondary,
                                style = MusicTextStyles.bodyLarge,
                            )
                            Button(
                                onClick = model::retry,
                                enabled = state.canRetry,
                                modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                            ) { Text(stringResource(R.string.action_retry)) }
                        }
                    }
                }
                LocalSearchStatus.Results -> {
                    Text(
                        stringResource(R.string.i6_search_local_results, state.count),
                        modifier = Modifier.padding(
                            horizontal = MusicSpacing.mdDp,
                            vertical = MusicSpacing.xsDp,
                        ).semantics { heading() },
                        color = MusicColors.textSecondary,
                        style = MusicTextStyles.bodyMedium,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = MusicSpacing.mdDp,
                            vertical = MusicSpacing.xsDp,
                        ),
                    ) {
                        items(state.entries, key = { it.stableSearchKey() }) { entry ->
                            LocalSearchResultRow(
                                entry = entry,
                                onClick = {
                                    when (entry) {
                                        is LibraryBrowseEntry.Song ->
                                            model.previewSong(entry.media)
                                        else -> onOpenGroup(entry)
                                    }
                                },
                            )
                            HorizontalDivider(color = MusicColors.border)
                        }
                    }
                }
            }

            if (state.selectedSong != null &&
                state.status == LocalSearchStatus.Results) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MusicColors.surfaceVariant)
                        .padding(MusicSpacing.mdDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(
                            R.string.p08_selected_not_playing,
                            state.selectedSong?.title.orEmpty(),
                        ),
                        modifier = Modifier.weight(1f),
                        color = MusicColors.textPrimary,
                        style = MusicTextStyles.bodyMedium,
                    )
                    OutlinedButton(
                        onClick = model::closePreview,
                        modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.p08_close_selection)) }
                }
            }

            NavigationBar(containerColor = MusicColors.surface) {
                NavigationBarItem(
                    selected = false, onClick = onBack,
                    icon = { Text("⌂") },
                    label = { Text(stringResource(R.string.p08_nav_home)) },
                )
                NavigationBarItem(
                    selected = true, onClick = {},
                    icon = { Text("⌕") },
                    label = { Text(stringResource(R.string.p08_nav_search)) },
                )
                NavigationBarItem(
                    selected = false, onClick = {}, enabled = false,
                    icon = { Text("♡") },
                    label = { Text(stringResource(R.string.p08_nav_liked)) },
                )
                NavigationBarItem(
                    selected = false, onClick = onSettings,
                    icon = { Text("⚙") },
                    label = { Text(stringResource(R.string.p08_nav_settings)) },
                )
            }
        }
    }
}

@Composable
private fun LocalSearchResultRow(entry: LibraryBrowseEntry, onClick: () -> Unit) {
    val title: String
    val detail: String
    val action: String
    when (entry) {
        is LibraryBrowseEntry.Artist -> {
            title = entry.label
            detail = stringResource(R.string.i6_search_artist_result)
            action = stringResource(R.string.i6_search_open)
        }
        is LibraryBrowseEntry.Album -> {
            title = entry.label
            detail = listOfNotNull(
                entry.artistLabel,
                stringResource(R.string.i6_search_album_result),
            ).joinToString(" · ")
            action = stringResource(R.string.i6_search_open)
        }
        is LibraryBrowseEntry.Song -> {
            title = entry.media.title
            detail = listOfNotNull(
                entry.media.artist,
                stringResource(R.string.i6_search_track_result),
            ).joinToString(" · ")
            action = stringResource(R.string.i6_search_select_song)
        }
        is LibraryBrowseEntry.Folder -> {
            title = entry.label
            detail = stringResource(R.string.i6_search_folder_result)
            action = stringResource(R.string.i6_search_open)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = MusicSize.trackRowMinHeightDp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = MusicSpacing.smDp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MusicTextStyles.bodyLarge, color = MusicColors.textPrimary)
            Text(detail, style = MusicTextStyles.bodyMedium, color = MusicColors.textMuted)
        }
        Text(action, style = MusicTextStyles.labelLarge, color = MusicColors.primary)
    }
}

@Composable
private fun SearchStatusMessage(modifier: Modifier, message: String) {
    Box(
        modifier = modifier.fillMaxWidth()
            .padding(MusicSpacing.mdDp),
        contentAlignment = Alignment.Center,
    ) {
        Text(message, style = MusicTextStyles.bodyLarge,
            color = MusicColors.textSecondary)
    }
}

private fun LibraryBrowseEntry.stableSearchKey(): String = when (this) {
    is LibraryBrowseEntry.Song -> "song:" + media.ref.value
    is LibraryBrowseEntry.Artist -> "artist:" + id.value
    is LibraryBrowseEntry.Album -> "album:" + id.value
    is LibraryBrowseEntry.Folder -> "folder:" + id.value
}
