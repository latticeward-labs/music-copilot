package com.latticeward.musiccopilot.foundation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #12 P12 source-only review checklist (zero cases executed).
 * P14 must actually exercise these behaviours on the owner's Android 15
 * handset with TalkBack/font scale and permission/rotation/restart QA.
 */
class Issue12Phase12PrivacySettingsWiringTest {
    private val root = "src/main/java/com/latticeward/musiccopilot/"
    private fun read(path: String) = File(root + path).readText()
    private fun strings() = File("src/main/res/values/strings.xml").readText()

    @Test fun existingSettingsRouteOwnsPrivacySectionNotDrivingMode() {
        val nav = read("app/navigation/AppNavHost.kt")
        val settings = nav.substringAfter("composable(Destination.Settings.route)")
            .substringBefore("internal fun FoundationBootstrapScreen")
        assertTrue(settings.contains("PrivacyDiagnosticsSettingsSection("))
        assertTrue(nav.contains("settingsOpen -> Destination.Settings"))
        assertTrue(nav.contains("drivingNavigation.state.active"))
        assertFalse(nav.contains("Destination.Driving.route).PrivacyDiagnosticsSettingsSection"))
    }

    @Test fun noConsentOrClearRunsAtCompositionTime() {
        val settings = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(settings.contains("onClick = {"))
        assertTrue(settings.contains("showClearConfirmation = true"))
        assertTrue(settings.contains("showRevokeConfirmation = true"))
        assertFalse(settings.contains("LaunchedEffect("))
        assertFalse(settings.contains("SideEffect("))
    }

    @Test fun noDeletionSelectedWithoutUserAction() {
        val settings = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(settings.contains("var clearDiagnosticsSelected by rememberSaveable { mutableStateOf(false) }"))
        assertTrue(settings.contains("enabled = clearDiagnosticsSelected && !clearing && !revoking"))
        assertTrue(settings.contains("LocalPrivacyClearSelection(diagnostics = true)"))
    }

    @Test fun noClearOnCancelOrAndroidBack() {
        val settings = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(settings.contains("onDismissRequest = { if (!clearing) showClearConfirmation = false }"))
        assertTrue(settings.contains("onClick = { showClearConfirmation = false }"))
        assertTrue(settings.contains("onClick = { showRevokeConfirmation = false }"))
    }

    @Test fun historyCheckboxRemainsDisabledWhenNoOwnerExists() {
        val settings = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(settings.contains("checked = false, onCheckedChange = null, enabled = false"))
        val app = read("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("ShortHistoryAvailability.NOT_STORED_IN_CURRENT_BUILD"))
        assertTrue(app.contains("else LocalClearCategoryResult.UNAVAILABLE"))
        assertFalse(app.contains("libraryDao().clearDerived()"))
        assertFalse(app.contains("restorableQueueStore.clear()"))
    }

    @Test fun selectedClearPreservesPlayerLibraryLikesAndSettings() {
        val app = read("app/di/ApplicationBindings.kt")
        val clear = app.substringAfter("fun clearSelectedAppData(")
            .substringBefore("fun revokeDiagnosticsConsentWithResult(")
        assertTrue(clear.contains("clearLocalDiagnostics()"))
        for (forbidden in listOf("mediaSelection", "playbackGateway.execute",
                "libraryRepository.reconcile", "room", "MediaStore",
                "SetCloudSpeechConsent", "SetBluetoothHint",
                "restorableQueueStore")) {
            assertFalse(clear.contains(forbidden))
        }
    }

    @Test fun privacyClearIsConfirmedInAccessibleAlertDialog() {
        val settings = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(settings.contains("AlertDialog("))
        assertTrue(settings.contains("i12_privacy_clear_confirm_title"))
        assertTrue(settings.contains("i12_privacy_clear_confirm_preserved"))
        assertTrue(settings.contains("i12_privacy_clear_confirm_remote"))
        assertTrue(settings.contains("heightIn(min = 48.dp)"))
        assertTrue(settings.contains("semantics { heading() }"))
    }

    @Test fun resultShowsBothSelectedCategoryAndIndependentLocalOutcome() {
        val ui = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(ui.contains("result.overall"))
        assertTrue(ui.contains("clearStateLabel(result.diagnostics)"))
        assertTrue(ui.contains("clearStateLabel(result.shortHistory)"))
        assertTrue(ui.contains("LocalClearOverall.PARTIALLY_CLEARED"))
        assertTrue(ui.contains("i12_privacy_retry"))
    }

    @Test fun defaultP04ApprovalsStayFalseAndNoGrantIsAvailable() {
        val approvals = read("core/application/privacy/DiagnosticsConsentPolicy.kt")
        for (field in listOf("privacyCopyApproved: Boolean = false",
                "retentionApproved: Boolean = false",
                "collectionAdapterApproved: Boolean = false",
                "legalApproved: Boolean = false")) {
            assertTrue(approvals.contains(field))
        }
        val ui = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(ui.contains("i12_privacy_grant_unavailable"))
        assertFalse(ui.contains("SetDiagnosticsConsent(DiagnosticsConsent.Granted)"))
    }

    @Test fun diagnosticsRevokeDeniesBeforeAsyncPreferenceWrite() {
        val bindings = read("app/di/ApplicationBindings.kt")
        val revoke = bindings.substringAfter("fun revokeDiagnosticsConsentWithResult(")
            .substringBefore("fun revokeCloudSpeechConsent()")
        assertTrue(revoke.indexOf("requestImmediateRevocation()") <
            revoke.indexOf("clearLocalDiagnostics()"))
        assertTrue(revoke.indexOf("clearLocalDiagnostics()") <
            revoke.indexOf("repositoryScope.launch"))
        assertTrue(revoke.contains("saved = try"))
        assertTrue(revoke.contains("onFinished(DiagnosticsRevocationReport("))
    }

    @Test fun appScopedTasksSurviveSettingsComposableDisposal() {
        val bindings = read("app/di/ApplicationBindings.kt")
        assertTrue(bindings.contains("private val repositoryJob = SupervisorJob(applicationJob)"))
        assertTrue(bindings.contains("repositoryScope.launch"))
        val ui = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertFalse(ui.contains("rememberCoroutineScope()"))
        assertFalse(ui.contains("scope.launch"))
    }

    @Test fun cloudAndDiagnosticsAreIndependentConsentBoundaries() {
        val bindings = read("app/di/ApplicationBindings.kt")
        val cloud = bindings.substringAfter("fun revokeCloudSpeechConsentWithResult(")
            .substringBefore("val telemetryPrivacyGate:")
        assertTrue(cloud.contains("SetCloudSpeechConsent(CloudSpeechConsent.Revoked)"))
        assertFalse(cloud.contains("SetDiagnosticsConsent"))
        val diagnostic = bindings.substringAfter("fun revokeDiagnosticsConsentWithResult(")
            .substringBefore("fun revokeCloudSpeechConsent()")
        assertFalse(diagnostic.contains("SetCloudSpeechConsent"))
    }

    @Test fun cloudConsentUiShowsConfirmedDataStoreOutcomeAndRetry() {
        val nav = read("app/navigation/AppNavHost.kt")
        assertTrue(nav.contains("onRevokeCloudSpeechConsentWithResult { saved ->"))
        assertTrue(nav.contains("if (saved) R.string.i12_privacy_cloud_revoke_success"))
        assertTrue(nav.contains("R.string.i12_privacy_cloud_revoke_failed"))
        assertTrue(nav.contains("cloudSaving"))
    }

    @Test fun UIPrivacyCopyDoesNotInventProviderRetentionOrRemoteDeletion() {
        val text = strings()
        assertTrue(text.contains("No permanent diagnostic retention period"))
        assertTrue(text.contains("No external diagnostics provider is installed"))
        assertTrue(text.contains("Cloud speech processing is separate"))
        assertFalse(text.contains("Diagnostics will be deleted from our servers"))
    }

    @Test fun noNewPermissionSdkOrRawAudioUploadWasAdded() {
        val ui = read("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        val contract = read("core/application/privacy/LocalPrivacyClearContract.kt")
        assertFalse(ui.contains("HttpClient"))
        assertFalse(ui.contains("MediaStore.delete"))
        assertFalse(contract.contains("Firebase"))
        assertFalse(contract.contains("Sentry"))
        assertFalse(ui.contains("upload"))
    }

    @Test fun ActivityWiresOnlyExistingSingleAppScopedOwner() {
        val main = read("app/MainActivity.kt")
        for (wire in listOf("bindings::clearSelectedAppData",
                "bindings::revokeDiagnosticsConsentWithResult",
                "bindings::revokeCloudSpeechConsentWithResult")) {
            assertTrue(main.contains(wire))
        }
    }
}
