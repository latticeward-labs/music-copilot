package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.navigation.Destination
import com.latticeward.musiccopilot.app.navigation.DrivingNavigationPolicy as Policy
import com.latticeward.musiccopilot.app.navigation.DrivingNavigationState
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #9 P03 pure navigation contract cases. AUTHORED ONLY, NOT RUN.
 * NavHost/UI/back-stack/device validations remain for owner P13 handoff.
 */
class Issue9Phase3DrivingNavigationPolicyTest {
    private val inactive = DrivingNavigationState()

    @Test fun defaultsToInactiveLibraryAndNeverAutoEntersOnFreshProcess() {
        assertFalse(inactive.active)
        assertEquals(Destination.Library, inactive.origin)
        assertEquals(Destination.Library, Policy.destinationAfterExit(inactive))
    }

    @Test fun explicitLibraryTapEntersDrivingAndRecordsLibrary() {
        val result = Policy.enter(inactive, Destination.Library, allowedByAppRoute = true)
        assertTrue(result.active)
        assertEquals(Destination.Library, result.origin)
    }

    @Test fun explicitFullPlayerTapRecordsFullPlayerForExit() {
        val result = Policy.enter(inactive, Destination.FullPlayer, allowedByAppRoute = true)
        assertTrue(result.active)
        assertEquals(Destination.FullPlayer, Policy.destinationAfterExit(result))
    }

    @Test fun systemOrPermissionRecoveryCanBlockManualEntry() {
        for (origin in listOf(Destination.Library, Destination.FullPlayer)) {
            assertEquals(inactive, Policy.enter(
                inactive, origin, allowedByAppRoute = false,
            ))
        }
    }

    @Test fun repeatedTapCannotOverwriteOriginalFullPlayerReturnTarget() {
        val initial = Policy.enter(inactive, Destination.FullPlayer, true)
        assertEquals(initial, Policy.enter(initial, Destination.Library, true))
        assertEquals(Destination.FullPlayer, Policy.destinationAfterExit(initial))
    }

    @Test fun directEntryFromSearchSettingsOrBootstrapIsRejected() {
        for (origin in listOf(
            Destination.Search, Destination.Settings, Destination.Bootstrap,
            Destination.PermissionRecovery, Destination.Onboarding, Destination.Driving,
        )) {
            assertEquals(inactive, Policy.enter(inactive, origin, true))
        }
    }

    @Test fun exitFromFullPlayerReturnsRecordedOriginAndResetsSession() {
        val active = Policy.enter(inactive, Destination.FullPlayer, true)
        assertEquals(Destination.FullPlayer, Policy.destinationAfterExit(active))
        assertEquals(inactive, Policy.exit(active))
    }

    @Test fun exitFromLibraryReturnsLibraryAndResetsSession() {
        val active = Policy.enter(inactive, Destination.Library, true)
        assertEquals(Destination.Library, Policy.destinationAfterExit(active))
        assertEquals(inactive, Policy.exit(active))
    }

    @Test fun exitIsIdempotentWithNoPlaybackSideEffects() {
        assertEquals(inactive, Policy.exit(inactive))
        assertEquals(inactive, Policy.exit(Policy.exit(inactive)))
    }

    @Test fun invalidOrRestoredOriginFallsBackToLibrary() {
        val stale = DrivingNavigationState(true, Destination.Settings)
        assertEquals(Destination.Library, Policy.destinationAfterExit(stale))
        assertEquals(inactive, Policy.exit(stale))
    }

    @Test fun reentryAfterExitMayRecordNewExplicitOrigin() {
        val initial = Policy.enter(inactive, Destination.FullPlayer, true)
        val later = Policy.enter(Policy.exit(initial), Destination.Library, true)
        assertEquals(Destination.Library, Policy.destinationAfterExit(later))
        assertTrue(later.active)
    }

    @Test fun noSaveableOrSerializedPlaybackStateExistsInThePolicy() {
        // Pure state contains only the UI navigation route and an explicit
        // active bit. No MediaRef, Queue, Position, permission grant, MAC,
        // Bluetooth identity, recording intent or Player is cached here.
        assertEquals(2, DrivingNavigationState::class.java.declaredFields.count {
            it.name == "active" || it.name == "origin"
        })
    }
}
