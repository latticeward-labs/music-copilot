package com.latticeward.musiccopilot.feature.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.navigation.FoundationReadiness
import com.latticeward.musiccopilot.app.navigation.LibraryActionPolicy
import com.latticeward.musiccopilot.app.navigation.LibraryUiAction
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicRadius
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot

/**
 * WF-04/HF-04 approved hierarchy: Library bar → categories → local rows →
 * service-observing mini-player → four-slot bottom navigation.
 * Actual playback (P10/#7) and optional Likes (P2) have no
 * fabricated behavior here. All personal metadata stays in local UI only.
 */
@Composable
fun LibraryBrowseScreen(
    model: LibraryBrowseViewModel,
    index: LibraryIndexSnapshot,
    readiness: FoundationReadiness,
    permission: PermissionStatus,
    playbackConnection: PlaybackConnectionState,
    playback: PlaybackSnapshot,
    onRefreshPermission: () -> Unit,
    onRetryLibraryScan: () -> Unit,
    onRescanLibrary: () -> Unit,
    onSettings: () -> Unit,
    onSearch: () -> Unit = {},
    initialGroup: LibraryBrowseEntry? = null,
    onGroupConsumed: () -> Unit = {},
    onExit: () -> Unit,
) {
    val state by model.ui.collectAsStateWithLifecycle()
    LaunchedEffect(index.revision, index.status, permission, initialGroup) {
        model.onVerifiedIndex(index, permission == PermissionStatus.Granted)
        // P09 group Open is navigational, never implicit audio playback.
        // The verified index MUST be Ready with nonzero revision before
        // exposing a previous Search result as an active Library member list.
        if (initialGroup != null && permission == PermissionStatus.Granted &&
            readiness == FoundationReadiness.LocalLibraryReady &&
            index.revision > 0L
        ) {
            val kind = when (initialGroup) {
                is LibraryBrowseEntry.Artist -> LibraryBrowseCategory.Artists
                is LibraryBrowseEntry.Album -> LibraryBrowseCategory.Albums
                is LibraryBrowseEntry.Folder -> LibraryBrowseCategory.Folders
                is LibraryBrowseEntry.Song -> LibraryBrowseCategory.Songs
            }
            if (initialGroup !is LibraryBrowseEntry.Song) {
                model.selectCategory(kind)
                model.openGroup(initialGroup)
            }
            onGroupConsumed()
        }
    }
    BackHandler {
        if (!model.backToGroups()) onExit()
    }

    val action = LibraryActionPolicy.nextAction(permission, readiness)
    val canBrowse = permission == PermissionStatus.Granted &&
        readiness == FoundationReadiness.LocalLibraryReady
    val listState = rememberLazyListState()
    LaunchedEffect(state.category, state.groupId) {
        listState.scrollToItem(0)
    }
    val lastVisibleIndex by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
    }
    LaunchedEffect(lastVisibleIndex, state.nextOffset, state.entries.size, canBrowse) {
        if (canBrowse && state.canLoadMore &&
            lastVisibleIndex >= state.entries.size - 6
        ) model.loadMore()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MusicColors.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = MusicSize.appBarHeightDp)
                    .padding(horizontal = MusicSpacing.mdDp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.p08_library_heading),
                    style = MusicTextStyles.titleLarge,
                    color = MusicColors.textPrimary,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                TextButton(
                    onClick = onSettings,
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) {
                    Text(stringResource(R.string.p08_more))
                }
            }

            if (canBrowse) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = MusicSpacing.mdDp),
                    horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LibraryBrowseCategory.values().forEach { category ->
                        FilterChip(
                            selected = state.category == category,
                            onClick = { model.selectCategory(category) },
                            label = { Text(category.label()) },
                            modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                        )
                    }
                }
            }

            if (state.groupTitle != null && canBrowse) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = MusicSpacing.mdDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { model.backToGroups() },
                        modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp)) {
                        Text(stringResource(R.string.action_back))
                    }
                    Text(
                        text = state.groupTitle.orEmpty(),
                        style = MusicTextStyles.titleMedium,
                        color = MusicColors.textPrimary,
                        modifier = Modifier.weight(1f).padding(start = MusicSpacing.mdDp),
                    )
                }
            }

            val headingCount = if (canBrowse && !state.loading && state.failure == null)
                state.totalCount else null
            if (headingCount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = MusicSpacing.mdDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.p08_item_count, headingCount),
                        modifier = Modifier.weight(1f),
                        color = MusicColors.textMuted,
                        style = MusicTextStyles.bodyMedium,
                    )
                    if (action == LibraryUiAction.Rescan) {
                        OutlinedButton(
                            onClick = onRescanLibrary,
                            modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                        ) { Text(stringResource(R.string.p08_action_rescan_short)) }
                    }
                }
            }

            when {
                !canBrowse -> {
                    LibraryStatusPanel(
                        modifier = Modifier.weight(1f),
                        readiness = readiness,
                        action = action,
                        onAction = {
                            when (action) {
                                LibraryUiAction.ReviewAccess -> onRefreshPermission()
                                LibraryUiAction.StartScan, LibraryUiAction.Rescan ->
                                    onRescanLibrary()
                                LibraryUiAction.RetryScan -> onRetryLibraryScan()
                                LibraryUiAction.None -> Unit
                            }
                        },
                    )
                }
                state.loading && state.entries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MusicColors.primary)
                            Text(
                                stringResource(R.string.p08_loading_categories),
                                modifier = Modifier.padding(MusicSpacing.mdDp),
                                style = MusicTextStyles.bodyLarge,
                                color = MusicColors.textSecondary,
                            )
                        }
                    }
                }
                state.failure != null && state.entries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            modifier = Modifier.padding(MusicSpacing.mdDp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stringResource(
                                    if (state.failure == LibraryPageFailure.IndexChanged)
                                        R.string.p08_stale_index
                                    else R.string.p08_browse_error
                                ),
                                modifier = Modifier.semantics {
                                    liveRegion = LiveRegionMode.Polite
                                },
                                style = MusicTextStyles.bodyLarge,
                                color = MusicColors.textPrimary,
                            )
                            Button(
                                onClick = if (state.failure == LibraryPageFailure.IndexChanged)
                                    onRescanLibrary else model::retryPage,
                                modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                            ) {
                                Text(stringResource(R.string.action_retry))
                            }
                        }
                    }
                }
                state.entries.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(
                                if (state.inGroup) R.string.p08_empty_group
                                else R.string.p08_empty_category
                            ),
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                            style = MusicTextStyles.bodyLarge,
                            color = MusicColors.textSecondary,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(
                            horizontal = MusicSpacing.mdDp,
                            vertical = MusicSpacing.xsDp,
                        ),
                    ) {
                        items(state.entries, key = { it.stableItemKey() }) { entry ->
                            LibraryBrowseRow(
                                entry = entry,
                                selected = entry is LibraryBrowseEntry.Song &&
                                    state.selectedSong?.ref == entry.media.ref,
                                onClick = {
                                    when (entry) {
                                        is LibraryBrowseEntry.Song ->
                                            model.previewSong(entry.media)
                                        else -> model.openGroup(entry)
                                    }
                                },
                            )
                            HorizontalDivider(color = MusicColors.border)
                        }
                        if (state.loadingMore) {
                            item(key = "library_load_more_progress") {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .heightIn(min = MusicSize.touchTargetMinDp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator(color = MusicColors.primary) }
                            }
                        }
                        if (state.failure != null) {
                            item(key = "library_load_more_error") {
                                Button(
                                    onClick = if (state.failure == LibraryPageFailure.IndexChanged)
                                        onRescanLibrary else model::retryPage,
                                    modifier = Modifier.heightIn(
                                        min = MusicSize.touchTargetMinDp
                                    ),
                                ) {
                                    Text(stringResource(
                                        if (state.failure == LibraryPageFailure.IndexChanged)
                                            R.string.p08_rescan_local_music
                                        else R.string.p08_retry_page
                                    ))
                                }
                            }
                        }
                    }
                }
            }

            if (state.selectedSong != null && canBrowse) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MusicColors.surfaceVariant)
                        .padding(MusicSpacing.smDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selectedSongFeedback(
                            state.selectedSong?.title.orEmpty(),
                            state.selectionStatus,
                        ),
                        modifier = Modifier.weight(1f).semantics {
                            liveRegion = LiveRegionMode.Polite
                        },
                        color = MusicColors.textPrimary,
                        style = MusicTextStyles.bodyMedium,
                    )
                    OutlinedButton(onClick = model::closePreview,
                        modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp)) {
                        Text(stringResource(R.string.p08_close_selection))
                    }
                }
            }

            // Mini-player renders only a real service-selected item; repository
            // rows and local selection never create a synthetic Playing state.
            if (playbackConnection == PlaybackConnectionState.Ready &&
                playback.currentItem != null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(MusicColors.surfaceRaised)
                        .heightIn(min = MusicSize.trackRowMinHeightDp)
                        .padding(horizontal = MusicSpacing.mdDp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.p08_current_track),
                        modifier = Modifier.weight(1f),
                        style = MusicTextStyles.bodyMedium,
                        color = MusicColors.textPrimary,
                    )
                    Text(
                        playback.playbackState.name,
                        style = MusicTextStyles.labelLarge,
                        color = MusicColors.primary,
                    )
                }
            }

            NavigationBar(containerColor = MusicColors.surface) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Text("⌂") },
                    label = { Text(stringResource(R.string.p08_nav_home)) },
                )
                NavigationBarItem(
                    selected = false, onClick = onSearch,
                    enabled = permission == PermissionStatus.Granted &&
                        (readiness == FoundationReadiness.LocalLibraryReady ||
                         readiness == FoundationReadiness.LocalLibraryEmpty),
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
private fun LibraryBrowseRow(
    entry: LibraryBrowseEntry,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val title: String
    val meta: String
    val glyph: String
    when (entry) {
        is LibraryBrowseEntry.Song -> {
            title = entry.media.title
            meta = listOfNotNull(entry.media.artist, entry.media.album)
                .joinToString(" · ")
            glyph = "♫"
        }
        is LibraryBrowseEntry.Artist -> {
            title = entry.label
            meta = stringResource(R.string.p08_song_count, entry.songCount)
            glyph = "♪"
        }
        is LibraryBrowseEntry.Album -> {
            title = entry.label
            meta = listOfNotNull(entry.artistLabel,
                stringResource(R.string.p08_song_count, entry.songCount)).joinToString(" · ")
            glyph = "▣"
        }
        is LibraryBrowseEntry.Folder -> {
            title = entry.label
            meta = stringResource(R.string.p08_song_count, entry.songCount)
            glyph = "▤"
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = MusicSize.trackRowMinHeightDp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = MusicSpacing.xsDp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
    ) {
        Box(
            modifier = Modifier.size(MusicSize.touchTargetMinDp)
                .background(
                    if (selected) MusicColors.primaryContainer else MusicColors.surfaceVariant,
                    RoundedCornerShape(MusicRadius.smDp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, style = MusicTextStyles.titleMedium, color = MusicColors.primary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, style = MusicTextStyles.bodyLarge,
                color = MusicColors.textPrimary,
            )
            if (meta.isNotBlank()) Text(
                meta, style = MusicTextStyles.bodyMedium,
                color = MusicColors.textMuted,
            )
        }
        if (entry !is LibraryBrowseEntry.Song) {
            Text("›", style = MusicTextStyles.titleMedium, color = MusicColors.textSecondary)
        }
    }
}

@Composable
private fun LibraryStatusPanel(
    modifier: Modifier,
    readiness: FoundationReadiness,
    action: LibraryUiAction,
    onAction: () -> Unit,
) {
    val textId = when (readiness) {
        FoundationReadiness.Initializing -> R.string.foundation_state_initializing
        FoundationReadiness.IndexingLocalLibrary -> R.string.foundation_state_indexing
        FoundationReadiness.LocalLibraryEmpty -> R.string.foundation_state_empty
        FoundationReadiness.LocalLibraryReady -> R.string.foundation_state_ready
        FoundationReadiness.LocalLibraryRecoverableError -> R.string.foundation_state_recoverable
        FoundationReadiness.LibraryPermissionOrMediaUnavailable ->
            R.string.foundation_state_unavailable
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(MusicSpacing.lgDp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (readiness == FoundationReadiness.IndexingLocalLibrary ||
                readiness == FoundationReadiness.Initializing
            ) CircularProgressIndicator(color = MusicColors.primary)
            Text(
                stringResource(textId),
                modifier = Modifier.padding(vertical = MusicSpacing.mdDp)
                    .semantics { heading() },
                color = MusicColors.textPrimary,
                style = MusicTextStyles.bodyLarge,
            )
            if (action != LibraryUiAction.None) {
                val label = when (action) {
                    LibraryUiAction.ReviewAccess -> R.string.action_review_access
                    LibraryUiAction.StartScan -> R.string.p08_scan_local_music
                    LibraryUiAction.RetryScan -> R.string.action_retry
                    LibraryUiAction.Rescan -> R.string.p08_rescan_local_music
                    LibraryUiAction.None -> R.string.action_back
                }
                Button(
                    onClick = onAction,
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(label)) }
            }
        }
    }
}

@Composable
private fun LibraryBrowseCategory.label(): String = when (this) {
    LibraryBrowseCategory.Songs -> stringResource(R.string.p08_songs)
    LibraryBrowseCategory.Artists -> stringResource(R.string.p08_artists)
    LibraryBrowseCategory.Albums -> stringResource(R.string.p08_albums)
    LibraryBrowseCategory.Folders -> stringResource(R.string.p08_folders)
}

private fun LibraryBrowseEntry.stableItemKey(): String = when (this) {
    is LibraryBrowseEntry.Song -> "song:" + media.ref.value
    is LibraryBrowseEntry.Artist -> "artist:" + id.value
    is LibraryBrowseEntry.Album -> "album:" + id.value
    is LibraryBrowseEntry.Folder -> "folder:" + id.value
}
