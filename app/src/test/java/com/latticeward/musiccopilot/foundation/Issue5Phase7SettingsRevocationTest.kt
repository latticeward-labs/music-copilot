package com.latticeward.musiccopilot.foundation

import androidx.lifecycle.SavedStateHandle
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.feature.permission.PermissionViewModel
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P07 local logic tests; actual Android Settings navigation needs native AVD QA. */
class Issue5Phase7SettingsRevocationTest {
    private fun obs(granted: Boolean, rationale: Boolean) =
        NativePermissionObservation(granted, true, rationale)

    private fun permanentlyDeniedVm(state: SavedStateHandle = SavedStateHandle()): PermissionViewModel {
        val vm = PermissionViewModel(state)
        repeat(2) {
            vm.requestStarting()
            vm.nativeResult(obs(false, false), BlockedAction.None)
        }
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        return vm
    }

    @Test fun noSettingsRecheckUnlessUserOpenedAndActivityActuallyLeft() {
        val vm = permanentlyDeniedVm()
        vm.onReturnFromSettings(obs(true, false))
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        vm.settingsTapped()
        vm.onReturnFromSettings(obs(true, false)) // accidental early onResume
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        assertFalse(vm.takeOneGrantedScan())
        vm.onActivityPaused()
        vm.onReturnFromSettings(obs(true, false))
        assertEquals(MediaPermissionOutcome.Granted, vm.ui.value.outcome)
        assertTrue(vm.takeOneGrantedScan())
        assertFalse(vm.takeOneGrantedScan())
    }

    @Test fun deniedOrCancelledSettingsReturnNeverTriggersScan() {
        val vm = permanentlyDeniedVm()
        vm.settingsTapped()
        vm.onActivityPaused()
        vm.onReturnFromSettings(obs(false, false))
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        // Denied has no Continue affordance. Start a distinct explicit Settings
        // handoff rather than consuming the UI's grant-only action.
        vm.settingsTapped()
        vm.onActivityPaused()
        vm.onReturnFromSettings(obs(false, true))
        assertEquals(MediaPermissionOutcome.DeniedRequestable, vm.ui.value.outcome)
        assertFalse(vm.takeOneGrantedScan())
        vm.manualExit()
        assertEquals(MediaPermissionOutcome.None, vm.ui.value.outcome)
    }

    @Test fun failedSettingsLaunchAndManualExitClearPendingHandoff() {
        val vm = permanentlyDeniedVm()
        vm.settingsTapped()
        vm.settingsLaunchFailed()
        vm.onActivityPaused()
        vm.onReturnFromSettings(obs(true, false))
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        vm.settingsTapped()
        vm.manualExit()
        vm.onActivityPaused()
        vm.onReturnFromSettings(obs(true, false))
        assertEquals(MediaPermissionOutcome.None, vm.ui.value.outcome)
    }

    @Test fun savedHandoffSurvivesViewModelRecreationWithoutPersistingGrant() {
        val saved = SavedStateHandle()
        val old = permanentlyDeniedVm(saved)
        old.settingsTapped()
        old.onActivityPaused()
        val replacement = PermissionViewModel(saved)
        // P11 safely restores Settings-only display (not an OS grant or scan right).
        assertEquals(MediaPermissionOutcome.SettingsOnly, replacement.ui.value.outcome)
        assertFalse(replacement.takeOneGrantedScan())
        replacement.onReturnFromSettings(obs(true, false))
        assertEquals(MediaPermissionOutcome.Granted, replacement.ui.value.outcome)
        assertTrue(replacement.takeOneGrantedScan())
        assertFalse(replacement.takeOneGrantedScan())
        assertFalse(saved.keys().any { it.contains("grant", ignoreCase = true) })
    }

    @Test fun revokedStateHasNoAuthorizedScanAndOffersNoncoerciveRecovery() {
        val vm = PermissionViewModel(SavedStateHandle())
        vm.requestStarting()
        vm.nativeResult(obs(true, false), BlockedAction.StartScan)
        vm.onMediaRevoked()
        assertEquals(MediaPermissionOutcome.None, vm.ui.value.outcome)
        assertFalse(vm.takeOneGrantedScan())
        // The user may recover manually; onResume never coerces a new OS prompt.
        vm.onReturnFromSettings(obs(false, false))
        assertEquals(MediaPermissionOutcome.None, vm.ui.value.outcome)
        assertFalse(vm.takeOneGrantedScan())
    }

    @Test fun sourceEnforcesImmediateFailClosedAndNoForegroundRescan() {
        val base = "src/main/java/com/latticeward/musiccopilot/"
        val activity = File(base + "app/MainActivity.kt").readText()
        val bindings = File(base + "app/di/ApplicationBindings.kt").readText()
        val repo = File(base + "data/repository/RoomLibraryRepository.kt").readText()
        assertTrue(activity.contains("permissionUi.onActivityPaused()"))
        assertTrue(activity.contains("previouslyGranted || knownIndexed"))
        assertTrue(activity.contains("bindings.onMediaAccessRevoked()"))
        assertTrue(bindings.contains("libraryRepository.invalidateMediaAccess()"))
        assertTrue(repo.contains("accessEpoch.incrementAndGet()"))
        assertTrue(repo.contains("accessEpoch.get() != epoch"))
        assertTrue(repo.contains("status = LibraryIndexStatus.Unavailable"))
        assertFalse(activity.substringAfter("override fun onResume()").substringBefore("@Composable")
            .contains("scanAfterExplicitMediaGrant("))
        assertFalse(activity.substringAfter("override fun onResume()").substringBefore("@Composable")
            .contains("startActivity("))
    }
}
