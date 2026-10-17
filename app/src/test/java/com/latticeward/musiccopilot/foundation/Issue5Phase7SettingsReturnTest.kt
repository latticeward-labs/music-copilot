package com.latticeward.musiccopilot.foundation

import androidx.lifecycle.SavedStateHandle
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.SettingsReturnCoordinator
import com.latticeward.musiccopilot.core.application.permission.SettingsReturnOutcome
import com.latticeward.musiccopilot.feature.permission.PermissionViewModel
import org.junit.Assert.*
import org.junit.Test

/** OS observations are fixtures here; native OS truth is tested separately on AVD. */
class Issue5Phase7SettingsReturnTest {
    private fun os(granted: Boolean, rationale: Boolean) =
        NativePermissionObservation(granted, requestedBefore = true, shouldShowRationale = rationale)

    private fun settingsOnlyVm(handle: SavedStateHandle = SavedStateHandle()): PermissionViewModel {
        val vm = PermissionViewModel(handle)
        vm.requestStarting()
        vm.nativeResult(os(false, true), BlockedAction.None)
        vm.retryRationale()
        vm.requestStarting()
        vm.nativeResult(os(false, false), BlockedAction.None)
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        return vm
    }

    @Test fun startupAndResumeWithoutExplicitSettingsNeverInferGrant() {
        assertEquals(SettingsReturnOutcome.Ignored,
            SettingsReturnCoordinator.classify(false, true, os(true, false), 2))
        assertEquals(SettingsReturnOutcome.Ignored,
            SettingsReturnCoordinator.classify(true, false, os(true, false), 2))
    }

    @Test fun allThreeFreshSettingsReturnsStayDistinct() {
        assertEquals(SettingsReturnOutcome.Granted,
            SettingsReturnCoordinator.classify(true, true, os(true, false), 2))
        assertEquals(SettingsReturnOutcome.DeniedRequestable,
            SettingsReturnCoordinator.classify(true, true, os(false, true), 2))
        assertEquals(SettingsReturnOutcome.StillSettingsOnly,
            SettingsReturnCoordinator.classify(true, true, os(false, false), 2))
    }

    @Test fun grantedReturnRequiresExplicitContinueAndIsOneShot() {
        val vm = settingsOnlyVm()
        vm.settingsTapped()
        assertEquals(SettingsReturnOutcome.Ignored, vm.onReturnFromSettings(os(true, false)))
        vm.onActivityPaused()
        assertEquals(SettingsReturnOutcome.Granted, vm.onReturnFromSettings(os(true, false)))
        assertEquals(MediaPermissionOutcome.Granted, vm.ui.value.outcome)
        assertTrue(vm.takeOneGrantedScan())
        assertFalse(vm.takeOneGrantedScan())
        assertEquals(SettingsReturnOutcome.Ignored, vm.onReturnFromSettings(os(true, false)))
    }

    @Test fun requestableReturnPreservesRetryAndNoScan() {
        val vm = settingsOnlyVm()
        vm.settingsTapped()
        vm.onActivityPaused()
        assertEquals(SettingsReturnOutcome.DeniedRequestable,
            vm.onReturnFromSettings(os(false, true)))
        assertEquals(MediaPermissionOutcome.DeniedRequestable, vm.ui.value.outcome)
        assertFalse(vm.takeOneGrantedScan())
    }

    @Test fun stillSettingsOnlyNeverLoopsOrScans() {
        val vm = settingsOnlyVm()
        vm.settingsTapped()
        vm.onActivityPaused()
        assertEquals(SettingsReturnOutcome.StillSettingsOnly,
            vm.onReturnFromSettings(os(false, false)))
        assertEquals(MediaPermissionOutcome.SettingsOnly, vm.ui.value.outcome)
        assertEquals(SettingsReturnOutcome.Ignored, vm.onReturnFromSettings(os(false, false)))
        assertFalse(vm.takeOneGrantedScan())
        vm.manualExit()
        assertEquals(MediaPermissionOutcome.None, vm.ui.value.outcome)
    }

    @Test fun recreationRetainsOnlyHandoffTicketNotOldGrant() {
        val handle = SavedStateHandle()
        val before = settingsOnlyVm(handle)
        before.settingsTapped()
        before.onActivityPaused()
        val recreated = PermissionViewModel(handle)
        assertEquals(SettingsReturnOutcome.Granted,
            recreated.onReturnFromSettings(os(true, false)))
        assertTrue(recreated.takeOneGrantedScan())
        assertFalse(recreated.takeOneGrantedScan())
    }

    @Test fun recreationWhileSettingsNotYetLeftCannotCompleteHandoff() {
        val handle = SavedStateHandle()
        val before = settingsOnlyVm(handle)
        before.settingsTapped()
        val recreated = PermissionViewModel(handle)
        assertEquals(SettingsReturnOutcome.Ignored,
            recreated.onReturnFromSettings(os(true, false)))
        recreated.onActivityPaused()
        assertEquals(SettingsReturnOutcome.Granted,
            recreated.onReturnFromSettings(os(true, false)))
    }

    @Test fun failedLaunchManualExitAndRevokeDiscardScanAuthorization() {
        val vm = settingsOnlyVm()
        vm.settingsTapped()
        vm.settingsLaunchFailed()
        vm.onActivityPaused()
        assertEquals(SettingsReturnOutcome.Ignored, vm.onReturnFromSettings(os(true, false)))
        assertFalse(vm.takeOneGrantedScan())
        val vm2 = settingsOnlyVm()
        vm2.settingsTapped()
        vm2.onActivityPaused()
        vm2.onReturnFromSettings(os(true, false))
        vm2.onMediaRevoked()
        assertFalse(vm2.takeOneGrantedScan())
        assertEquals(MediaPermissionOutcome.None, vm2.ui.value.outcome)
    }

    @Test fun criticalRevocationAndSettingsArchitectureHooksExist() {
        val activity = java.io.File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        val bindings = java.io.File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        val library = java.io.File("src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt").readText()
        assertTrue(activity.contains("permissionUi.onActivityPaused()"))
        assertTrue(activity.contains("permissionUi.onReturnFromSettings("))
        assertTrue(activity.contains("bindings.onMediaAccessRevoked()"))
        assertTrue(bindings.contains("activeMediaScan?.cancel()"))
        assertTrue(library.contains("accessEpoch.get() != epoch"))
        assertTrue(library.contains("projection.replaceAll(emptyList())"))
        assertFalse(activity.substringAfter("override fun onResume()").substringBefore("@Composable")
            .contains("startActivity("))
    }
}
