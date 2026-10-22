package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.navigation.FoundationReadiness
import com.latticeward.musiccopilot.app.navigation.LibraryActionPolicy
import com.latticeward.musiccopilot.app.navigation.LibraryUiAction
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapEvidence
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoute
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoutePolicy
import com.latticeward.musiccopilot.core.application.library.LibraryReadinessResolver
import com.latticeward.musiccopilot.core.application.library.LibraryScanLaunchGate
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * P07 synthetic unit fixtures (AUTHORED, NOT_RUN until consolidated P12).
 * Does not mock an actual Settings Activity or OEM MediaStore permission.
 */
class Issue6Phase7ReadinessLifecycleTest {
    private fun state(status: LibraryIndexStatus, count: Int = 0, rev: Long = 0) =
        LibraryIndexSnapshot(rev, status, count)

    @Test fun singleFlightCoalescesRapidDuplicateExplicitScanRequests() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val gate = LibraryScanLaunchGate(scope)
        val entered = CompletableDeferred<Unit>()
        val continueScan = CompletableDeferred<Unit>()
        var invocations = 0
        assertTrue(gate.launchExplicit {
            invocations++
            entered.complete(Unit)
            continueScan.await()
        })
        entered.await()
        assertTrue(gate.isScanInFlight())
        assertFalse(gate.launchExplicit { invocations++ })
        assertFalse(gate.launchExplicit { invocations++ })
        assertEquals(1, invocations)
        continueScan.complete(Unit)
        assertFalse(gate.isScanInFlight())
        assertTrue(gate.launchExplicit { invocations++ })
        assertEquals(2, invocations)
    }

    @Test fun revocationCancelsOldLeaseAndNewExplicitGrantCanLaunch() = runBlocking {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val gate = LibraryScanLaunchGate(scope)
        val entered = CompletableDeferred<Unit>()
        val neverCompletes = CompletableDeferred<Unit>()
        val cancelled = CompletableDeferred<Unit>()
        assertTrue(gate.launchExplicit {
            try {
                entered.complete(Unit)
                neverCompletes.await()
            } finally {
                cancelled.complete(Unit)
            }
        })
        entered.await()
        gate.cancelInFlight()
        cancelled.await()
        assertFalse(gate.isScanInFlight())
        var runs = 0
        assertTrue(gate.launchExplicit { runs++ })
        assertEquals(1, runs)
    }

    @Test fun foregroundWithoutScanRequestStartsNoJob() {
        val gate = LibraryScanLaunchGate(
            CoroutineScope(SupervisorJob() + Dispatchers.Unconfined),
        )
        // Observing OS permission and navigating must not call launchExplicit.
        assertFalse(gate.isScanInFlight())
    }

    @Test fun unavailableOrDeniedNeverAppearsAsEmptyOrReady() {
        val apparentlyReady = state(LibraryIndexStatus.Ready, 4, 3)
        val apparentlyEmpty = state(LibraryIndexStatus.Empty, 0, 3)
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(false, apparentlyReady))
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(false, apparentlyEmpty))
    }

    @Test fun fabricatedReadyWithoutAcceptedRevisionIsUnavailable() {
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(true,
                state(LibraryIndexStatus.Ready, 2, rev = 0)))
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(true,
                state(LibraryIndexStatus.Ready, 0, rev = 12)))
        assertEquals(LibraryIndexStatus.Ready,
            LibraryReadinessResolver.verifiedStatus(true,
                state(LibraryIndexStatus.Ready, 2, rev = 12)))
    }

    @Test fun emptyOnlyAfterVerifiedAuthorizedZeroResult() {
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(true,
                state(LibraryIndexStatus.Empty, 0, 0)))
        assertEquals(LibraryIndexStatus.Empty,
            LibraryReadinessResolver.verifiedStatus(true,
                state(LibraryIndexStatus.Empty, 0, 1)))
    }

    @Test fun allFiveReadinessStatesHaveDistinctRecoveryActions() {
        val granted = PermissionStatus.Granted
        assertEquals(LibraryUiAction.StartScan, LibraryActionPolicy.nextAction(
            granted, FoundationReadiness.LibraryPermissionOrMediaUnavailable))
        assertEquals(LibraryUiAction.RetryScan, LibraryActionPolicy.nextAction(
            granted, FoundationReadiness.LocalLibraryRecoverableError))
        assertEquals(LibraryUiAction.Rescan, LibraryActionPolicy.nextAction(
            granted, FoundationReadiness.LocalLibraryEmpty))
        assertEquals(LibraryUiAction.Rescan, LibraryActionPolicy.nextAction(
            granted, FoundationReadiness.LocalLibraryReady))
        assertEquals(LibraryUiAction.None, LibraryActionPolicy.nextAction(
            granted, FoundationReadiness.IndexingLocalLibrary))
    }

    @Test fun noScanButtonDuringIndexingOrBootstrapInitialization() {
        assertEquals(LibraryUiAction.None, LibraryActionPolicy.nextAction(
            PermissionStatus.Granted, FoundationReadiness.Initializing))
        assertEquals(LibraryUiAction.None, LibraryActionPolicy.nextAction(
            PermissionStatus.Granted, FoundationReadiness.IndexingLocalLibrary))
    }

    @Test fun anyMissingOrRevokedPermissionShowsOnlyReviewAccess() {
        for (permission in listOf(
            PermissionStatus.Unknown, PermissionStatus.NotRequested,
            PermissionStatus.Revoked,
        )) {
            for (view in FoundationReadiness.values()) {
                assertEquals(LibraryUiAction.ReviewAccess,
                    LibraryActionPolicy.nextAction(permission, view))
            }
        }
    }

    @Test fun routeDoesNotTrustRoomWithoutOnboardingOrGrant() {
        val base = BootstrapEvidence(
            preferencesLoaded = true,
            migrationsVerified = true,
            startupFailed = false,
            onboardingComplete = false,
            mediaPermission = PermissionStatus.Granted,
            libraryStatus = LibraryIndexStatus.Ready,
        )
        assertEquals(BootstrapRoute.Onboarding, BootstrapRoutePolicy.project(base))
        assertEquals(BootstrapRoute.PermissionRecovery,
            BootstrapRoutePolicy.project(base.copy(
                onboardingComplete = true, mediaPermission = PermissionStatus.Revoked)))
    }

    @Test fun anAuthorizedFreshScanCanReachHomeWithoutInventingReady() {
        val evidence = BootstrapEvidence(
            preferencesLoaded = true,
            migrationsVerified = true,
            startupFailed = false,
            onboardingComplete = true,
            mediaPermission = PermissionStatus.Granted,
            libraryStatus = LibraryReadinessResolver.verifiedStatus(
                true, state(LibraryIndexStatus.Unavailable)),
        )
        assertEquals(BootstrapRoute.HomeLibrary,
            BootstrapRoutePolicy.project(evidence))
        assertEquals(LibraryIndexStatus.Unavailable, evidence.libraryStatus)
    }

    @Test fun badRoomMigrationNeverRoutesUserPastStartupRetry() {
        val evidence = BootstrapEvidence(
            preferencesLoaded = true,
            migrationsVerified = false,
            startupFailed = true,
            onboardingComplete = true,
            mediaPermission = PermissionStatus.Granted,
            libraryStatus = LibraryIndexStatus.Ready,
        )
        assertEquals(BootstrapRoute.RetryStartup, BootstrapRoutePolicy.project(evidence))
    }
}
