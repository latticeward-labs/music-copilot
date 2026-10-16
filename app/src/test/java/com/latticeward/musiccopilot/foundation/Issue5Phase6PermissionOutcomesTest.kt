package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaOutcomePolicy
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.MediaRequestGate
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** No live permission truth is mocked as a completed native integration test. */
class Issue5Phase6PermissionOutcomesTest {
    private fun os(granted: Boolean, rationale: Boolean, requested: Boolean = true) =
        NativePermissionObservation(granted, requested, rationale)

    @Test fun freshOrFirstDenialNeverIncorrectlyBecomesPermanent() {
        assertEquals(MediaPermissionOutcome.DeniedRequestable,
            MediaOutcomePolicy.project(os(false, false, requested = false), 0))
        assertEquals(MediaPermissionOutcome.DeniedRequestable,
            MediaOutcomePolicy.project(os(false, false), 1))
        assertEquals(MediaPermissionOutcome.DeniedRequestable,
            MediaOutcomePolicy.project(os(false, true), 3))
    }

    @Test fun onlyRepeatedDenialWithoutRationaleSuggestsSettingsOnly() {
        assertEquals(MediaPermissionOutcome.SettingsOnly,
            MediaOutcomePolicy.project(os(false, false), 2))
        assertEquals(MediaPermissionOutcome.SettingsOnly,
            MediaOutcomePolicy.project(os(false, false), 3))
        assertEquals(MediaPermissionOutcome.DeniedRequestable,
            MediaOutcomePolicy.project(os(false, true), 2))
    }

    @Test fun realGrantOutranksRequestHistoryAndAllowsExactlyOneBlockedAction() {
        val gate = MediaRequestGate(33)
        assertEquals(BlockedAction.None, gate.complete(os(true, false)))
        assertNotNull(gate.begin())
        assertEquals(MediaPermissionOutcome.Granted,
            MediaOutcomePolicy.project(os(true, false), 3))
        assertEquals(BlockedAction.StartScan, gate.complete(os(true, false)))
        assertEquals(BlockedAction.None, gate.complete(os(true, false)))
    }

    @Test fun denialsNeverAuthorizeScanAndManualRetryRequiresNewGesture() {
        val gate = MediaRequestGate(29)
        assertNotNull(gate.begin())
        assertEquals(BlockedAction.None, gate.complete(os(false, true)))
        assertFalse(gate.awaitingPlatformResult)
        assertEquals(BlockedAction.None, gate.complete(os(true, false)))
        assertNotNull(gate.begin())
        assertEquals(BlockedAction.None, gate.complete(os(false, false)))
    }

    @Test fun exactlyApprovedThreeOutcomeCopyAndNoNewRoute() {
        val strings = File("src/main/res/values/strings.xml").readText()
        for (text in listOf("Access granted", "Continue to the blocked action.",
            "Access denied", "Manual controls remain available.", "Try again",
            "Open Android Settings", "Permission must be changed explicitly.",
            "Open Settings", "Not now", "Fresh result is authoritative")) {
            assertTrue("Missing approved copy: " + text, strings.contains(">" + text + "</string>"))
        }
        val nav = File("src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt").readText()
        assertTrue(nav.contains("composable(Destination.PermissionRecovery.route)"))
        assertTrue(nav.contains("MediaPermissionOutcomeScreen("))
        assertFalse(nav.contains("composable(Destination.Driving.route)"))
    }

    @Test fun outcomeUiPreservesBackNotNowAndAccessibilityAtLargeFonts() {
        val ui = File("src/main/java/com/latticeward/musiccopilot/feature/permission/MediaPermissionOutcomeScreen.kt")
            .readText()
        assertTrue(ui.contains("BackHandler(onBack = onNotNow)"))
        assertTrue(ui.contains("onClick = onNotNow"))
        assertTrue(ui.contains("onClick = onPrimary"))
        assertTrue(ui.contains("heightIn(min = 48.dp)"))
        assertTrue(ui.contains(".verticalScroll(rememberScrollState())"))
        assertTrue(ui.contains("heading()"))
        assertTrue(ui.contains("liveRegion = LiveRegionMode.Polite"))
        assertTrue(ui.contains("onOpenSettings"))
        assertFalse(ui.contains("requestPermissions("))
    }

    @Test fun grantedContinueConsumesScanAndRechecksNativeTruth() {
        val act = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        val vm = File("src/main/java/com/latticeward/musiccopilot/feature/permission/PermissionViewModel.kt").readText()
        assertTrue(act.contains("val authorized = permissionUi.takeOneGrantedScan()"))
        assertTrue(act.contains("val freshGrant = hasMediaPermission()"))
        assertTrue(act.contains("if (authorized && freshGrant)"))
        assertTrue(vm.contains("scanAuthorized = false"))
        assertTrue(vm.contains("val eligible = mutableUi.value.outcome"))
        assertFalse(act.substringAfter("private val requestLauncher =")
            .substringBefore("override fun onCreate").contains("scanAfterExplicitMediaGrant("))
    }

    @Test fun retryAndSettingsRequireExplicitActionsWithNoAutoLoop() {
        val nav = File("src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt").readText()
        val act = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        assertTrue(nav.contains("onTryAgain = {"))
        assertTrue(nav.contains("onRetryMediaPermission()"))
        assertTrue(nav.contains("onOpenSettings = onOpenAndroidSettings"))
        assertTrue(act.contains("Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS"))
        assertTrue(act.contains("toUri()"))
        assertTrue(act.contains("permissionUi.settingsTapped()"))
        assertTrue(act.contains("startActivity(intent)"))
        assertFalse(act.substringAfter("override fun onResume()").substringBefore("@Composable")
            .contains("startActivity("))
    }
}
