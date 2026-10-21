package com.latticeward.musiccopilot.core.application.permission

/** Presentation-only projection; not permission grant truth. */
enum class MediaPermissionOutcome { None, Granted, DeniedRequestable, SettingsOnly }

/**
 * Conservative denial presentation: shouldShowRationale=false alone is not
 * evidence of permanent denial (also false before the first prompt).
 * On the FIRST observed denial we always offer a user-initiated Retry.
 * Only repeat denial + no-rationale can suggest Settings-only.
 * A Settings-only label is recovery UX, not a privileged OEM certainty.
 */
object MediaOutcomePolicy {
    fun project(observation: NativePermissionObservation, attempts: Int): MediaPermissionOutcome =
        when {
            observation.granted -> MediaPermissionOutcome.Granted
            attempts >= 2 && observation.requestedBefore &&
                !observation.shouldShowRationale -> MediaPermissionOutcome.SettingsOnly
            else -> MediaPermissionOutcome.DeniedRequestable
        }
}
