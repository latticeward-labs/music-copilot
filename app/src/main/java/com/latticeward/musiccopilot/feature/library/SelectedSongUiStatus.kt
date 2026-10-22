package com.latticeward.musiccopilot.feature.library

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes

/** Ephemeral local UI feedback, never a source of PlaybackState. */
enum class SelectedSongUiStatus {
    Idle, Validating, ServicePlaying, ServiceAcceptedPending,
    PermissionRequired, ItemMissing, LibraryChanged, PlaybackUnavailable,
}

object SelectedSongFailurePolicy {
    fun from(error: AppError): SelectedSongUiStatus = when {
        error is AppError.Permission ||
            error.code == AppErrorCodes.PERMISSION_REVOKED ||
            error.code == AppErrorCodes.PERMISSION_DENIED ->
            SelectedSongUiStatus.PermissionRequired
        error is AppError.MediaItem ->
            SelectedSongUiStatus.ItemMissing
        error.code == AppErrorCodes.LIBRARY_BROWSE_STALE ->
            SelectedSongUiStatus.LibraryChanged
        else -> SelectedSongUiStatus.PlaybackUnavailable
    }
}
