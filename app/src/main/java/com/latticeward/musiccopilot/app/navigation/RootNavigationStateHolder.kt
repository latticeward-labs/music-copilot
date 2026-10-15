package com.latticeward.musiccopilot.app.navigation

import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState

/**
 * Pure startup projection, never an alternate repository, Player or Voice state.
 * No permission/provider readiness is inferred from disconnected/unavailable.
 */
enum class FoundationReadiness {
    Initializing, LibraryPermissionOrMediaUnavailable,
    IndexingLocalLibrary, LocalLibraryEmpty, LocalLibraryReady,
    LocalLibraryRecoverableError
}

data class RootNavigationStateHolder(
    val startDestination: Destination = Destination.Bootstrap
) {
    fun readiness(
        library: LibraryIndexStatus,
        playback: PlaybackConnectionState
    ): FoundationReadiness = when (library) {
        LibraryIndexStatus.Unavailable -> FoundationReadiness.LibraryPermissionOrMediaUnavailable
        LibraryIndexStatus.Scanning -> FoundationReadiness.IndexingLocalLibrary
        LibraryIndexStatus.Empty -> FoundationReadiness.LocalLibraryEmpty
        LibraryIndexStatus.Ready -> FoundationReadiness.LocalLibraryReady
        LibraryIndexStatus.RecoverableError -> FoundationReadiness.LocalLibraryRecoverableError
    }
}
