package com.latticeward.musiccopilot.app.navigation

import com.latticeward.musiccopilot.core.application.permission.PermissionStatus

/**
 * P07 derived action policy. It does NOT own a scan, OS grant, or persisted
 * readiness. All callbacks still go to the process LibraryScanUseCase owner.
 */
enum class LibraryUiAction { None, ReviewAccess, StartScan, RetryScan, Rescan }

object LibraryActionPolicy {
    fun nextAction(
        permission: PermissionStatus,
        readiness: FoundationReadiness,
    ): LibraryUiAction {
        if (permission != PermissionStatus.Granted) {
            return LibraryUiAction.ReviewAccess
        }
        return when (readiness) {
            FoundationReadiness.Initializing,
            FoundationReadiness.IndexingLocalLibrary -> LibraryUiAction.None
            FoundationReadiness.LibraryPermissionOrMediaUnavailable ->
                LibraryUiAction.StartScan
            FoundationReadiness.LocalLibraryRecoverableError ->
                LibraryUiAction.RetryScan
            FoundationReadiness.LocalLibraryReady,
            FoundationReadiness.LocalLibraryEmpty -> LibraryUiAction.Rescan
        }
    }
}
