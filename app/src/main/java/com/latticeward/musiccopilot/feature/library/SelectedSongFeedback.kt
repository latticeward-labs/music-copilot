package com.latticeward.musiccopilot.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.latticeward.musiccopilot.R

/** Never derive Playing from button taps or a pending command. */
@Composable
fun selectedSongFeedback(title: String, state: SelectedSongUiStatus): String =
    when (state) {
        SelectedSongUiStatus.Idle ->
            stringResource(R.string.p08_selected_not_playing, title)
        SelectedSongUiStatus.Validating ->
            stringResource(R.string.i6_selection_verifying)
        SelectedSongUiStatus.ServicePlaying ->
            stringResource(R.string.i6_selection_playing, title)
        SelectedSongUiStatus.ServiceAcceptedPending ->
            stringResource(R.string.i6_selection_accepted_pending, title)
        SelectedSongUiStatus.PermissionRequired ->
            stringResource(R.string.i6_selection_permission)
        SelectedSongUiStatus.ItemMissing ->
            stringResource(R.string.i6_selection_missing)
        SelectedSongUiStatus.LibraryChanged ->
            stringResource(R.string.i6_selection_stale)
        SelectedSongUiStatus.PlaybackUnavailable ->
            stringResource(R.string.i6_selection_unavailable)
    }
