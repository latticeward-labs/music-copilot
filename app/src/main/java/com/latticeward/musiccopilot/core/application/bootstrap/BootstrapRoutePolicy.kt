package com.latticeward.musiccopilot.core.application.bootstrap

import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus

/** Pure routing proposal only; navigation remains owned by root navigation/UI. */
enum class BootstrapRoute { Preparing, RetryStartup, Onboarding, PermissionRecovery, HomeLibrary }

data class BootstrapEvidence(
    val preferencesLoaded: Boolean,
    val migrationsVerified: Boolean,
    val startupFailed: Boolean,
    val onboardingComplete: Boolean,
    val mediaPermission: PermissionStatus,
    val libraryStatus: LibraryIndexStatus?,
    val userChoseManualFallback: Boolean = false,
)

/**
 * Never derives OS permission from a DataStore flag or LibraryIndex Ready.
 * Initial and cold restart need actual platform/media state before routing.
 */
object BootstrapRoutePolicy {
    fun project(evidence: BootstrapEvidence): BootstrapRoute = when {
        evidence.startupFailed -> BootstrapRoute.RetryStartup
        !evidence.migrationsVerified || !evidence.preferencesLoaded -> BootstrapRoute.Preparing
        !evidence.onboardingComplete -> BootstrapRoute.Onboarding
        evidence.mediaPermission == PermissionStatus.Unknown -> BootstrapRoute.Preparing
        evidence.mediaPermission != PermissionStatus.Granted ->
            if (evidence.userChoseManualFallback) BootstrapRoute.HomeLibrary
            else BootstrapRoute.PermissionRecovery
        evidence.libraryStatus == null -> BootstrapRoute.Preparing
        else -> BootstrapRoute.HomeLibrary
    }
}
