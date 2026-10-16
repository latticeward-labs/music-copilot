package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaRequestGate
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.PermissionCapability
import com.latticeward.musiccopilot.core.application.permission.PermissionPlatformPolicy
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P05 native permission handoff contract. Never uses real media metadata. */
class Issue5Phase5MediaPermissionTest {
    private fun observation(granted: Boolean) =
        NativePermissionObservation(granted, true, !granted)

    @Test fun api29To32UseOnlyReadExternalStorage() {
        for (api in 29..32) {
            assertEquals("android.permission.READ_EXTERNAL_STORAGE",
                PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, api))
            val gate = MediaRequestGate(api)
            assertNotNull(gate.begin())
            assertEquals(BlockedAction.StartScan, gate.complete(observation(true)))
        }
    }

    @Test fun api33To36UseOnlyReadMediaAudio() {
        for (api in 33..36) {
            assertEquals("android.permission.READ_MEDIA_AUDIO",
                PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, api))
            assertEquals(BlockedAction.StartScan,
                MediaRequestGate(api).run { begin(); complete(observation(true)) })
        }
    }

    @Test fun noPromptBeforeExplicitContinue() {
        val gate = MediaRequestGate(33)
        assertFalse(gate.awaitingPlatformResult)
        assertEquals(BlockedAction.None, gate.complete(observation(true)))
        assertTrue(gate.begin() != null)
        assertTrue(gate.awaitingPlatformResult)
    }

    @Test fun repeatedContinueWhileRequestPendingCannotLaunchTwice() {
        val gate = MediaRequestGate(35)
        assertNotNull(gate.begin())
        assertNull(gate.begin())
        assertNull(gate.begin())
        assertTrue(gate.awaitingPlatformResult)
    }

    @Test fun deniedOrDismissedDialogCannotStartScan() {
        val gate = MediaRequestGate(29)
        gate.begin()
        assertEquals(BlockedAction.None, gate.complete(observation(false)))
        assertFalse(gate.awaitingPlatformResult)
        assertEquals(BlockedAction.None, gate.complete(observation(true)))
    }

    @Test fun grantCanBeConsumedExactlyOnce() {
        val gate = MediaRequestGate(33)
        gate.begin()
        assertEquals(BlockedAction.StartScan, gate.complete(observation(true)))
        assertEquals(BlockedAction.None, gate.complete(observation(true)))
        assertFalse(gate.awaitingPlatformResult)
    }

    @Test fun restoredNativeRequestDoesNotRelaunchSecondDialog() {
        val gate = MediaRequestGate(32)
        gate.restoreAwaitingNativeResult()
        assertTrue(gate.awaitingPlatformResult)
        assertNull(gate.begin())
        assertEquals(BlockedAction.StartScan, gate.complete(observation(true)))
        assertEquals(BlockedAction.None, gate.complete(observation(true)))
    }

    @Test fun abandonedOrCancelledRequestDiscardsBlockedAction() {
        val gate = MediaRequestGate(35)
        gate.begin()
        gate.abandon()
        assertEquals(BlockedAction.None, gate.complete(observation(true)))
        assertFalse(gate.awaitingPlatformResult)
    }

    @Test fun onlyApprovedPermissionsAppearInManifest() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android.permission.READ_EXTERNAL_STORAGE"))
        assertTrue(manifest.contains("android:maxSdkVersion=\"32\""))
        assertTrue(manifest.contains("android.permission.READ_MEDIA_AUDIO"))
        for (forbidden in listOf("MANAGE_EXTERNAL_STORAGE", "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO", "WRITE_EXTERNAL_STORAGE", "READ_MEDIA_VISUAL_USER_SELECTED")) {
            assertFalse("Unexpected media capability " + forbidden, manifest.contains(forbidden))
        }
    }

    @Test fun activityOwnsLifecycleSafeRequestAndFreshPermissionCheck() {
        val activity = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        assertTrue(activity.contains("registerForActivityResult(ActivityResultContracts.RequestPermission())"))
        assertTrue(activity.contains("gate.begin()"))
        assertTrue(activity.contains("gate.complete("))
        assertTrue(activity.contains("if (action == BlockedAction.StartScan && freshGrant)"))
        assertTrue(activity.contains("override fun onSaveInstanceState(outState: Bundle)"))
        assertTrue(activity.contains("gate.restoreAwaitingNativeResult()"))
        assertFalse(activity.contains("requestPermissions("))
        assertFalse(activity.contains("READ_MEDIA_IMAGES"))
    }

    @Test fun scanNeverStartsFromResumeOrDeniedCallback() {
        val bindings = File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        val activity = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        val source = File("src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt").readText()
        assertTrue(bindings.contains("fun scanAfterExplicitMediaGrant("))
        assertTrue(bindings.contains("ContextCompat.checkSelfPermission(application, permissionName)"))
        assertTrue(source.contains("if (!hasAccess()) throw SecurityException"))
        val onResume = activity.substringAfter("override fun onResume()").substringBefore("@Composable")
        assertFalse(onResume.contains("scanAfterExplicitMediaGrant("))
        assertFalse(onResume.contains("requestLauncher.launch("))
        assertFalse(activity.contains("android.permission.RECORD_AUDIO"))
    }

    @Test fun rationaleContainsApprovedCopyAndManualFallback() {
        val ui = File("src/main/java/com/latticeward/musiccopilot/feature/permission/MediaPermissionRationaleScreen.kt").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        for (s in listOf("Permissions", "PERMISSION", "Media access",
            "Index and play local music.", "[ANDROID / SYSTEM] handoff",
            "Fresh result is authoritative")) assertTrue("Missing " + s, strings.contains(">" + s + "</string>"))
        assertTrue(ui.contains("onClick = onContinue"))
        assertTrue(ui.contains("onClick = onNotNow"))
        assertTrue(ui.contains("enabled = !request.pending"))
        assertTrue(ui.contains("heightIn(min = 48.dp)"))
        assertTrue(ui.contains(".verticalScroll(rememberScrollState())"))
        assertTrue(ui.contains("BackHandler"))
        assertFalse(ui.contains("RECORD_AUDIO"))
    }
}
