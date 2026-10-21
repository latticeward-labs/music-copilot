package com.latticeward.musiccopilot.core.application.permission

/**
 * Pure one-shot Settings handoff policy. onResume is not a grant and cannot
 * initiate a new OS permission dialog. Only a handoff that actually left the
 * Activity may produce a return result, from a fresh Android observation.
 */
enum class SettingsReturnOutcome { Ignored, Granted, DeniedRequestable, StillSettingsOnly }

object SettingsReturnCoordinator {
    fun classify(
        launchedByUser: Boolean,
        activityLeft: Boolean,
        observation: NativePermissionObservation,
        explicitRequests: Int,
    ): SettingsReturnOutcome {
        if (!launchedByUser || !activityLeft) return SettingsReturnOutcome.Ignored
        return when (MediaOutcomePolicy.project(observation, explicitRequests)) {
            MediaPermissionOutcome.Granted -> SettingsReturnOutcome.Granted
            MediaPermissionOutcome.DeniedRequestable -> SettingsReturnOutcome.DeniedRequestable
            MediaPermissionOutcome.SettingsOnly -> SettingsReturnOutcome.StillSettingsOnly
            MediaPermissionOutcome.None -> SettingsReturnOutcome.Ignored
        }
    }

    fun uiOutcome(result: SettingsReturnOutcome): MediaPermissionOutcome = when (result) {
        SettingsReturnOutcome.Granted -> MediaPermissionOutcome.Granted
        SettingsReturnOutcome.DeniedRequestable -> MediaPermissionOutcome.DeniedRequestable
        SettingsReturnOutcome.StillSettingsOnly -> MediaPermissionOutcome.SettingsOnly
        SettingsReturnOutcome.Ignored -> MediaPermissionOutcome.None
    }
}
