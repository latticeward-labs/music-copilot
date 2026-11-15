package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * P08 SOURCE ONLY — written for final P14 verification, NOT executed here.
 * Contract checks complement owner-state JVM tests; service/Android/owner
 * device evidence is still required before AC can ever become PASS.
 */
class Issue12Phase8DrivingLifecycleContractTest {
    private val root = "src/main/java/com/latticeward/musiccopilot/"
    private fun source(path: String) = File(root + path).readText()

    @Test fun onlyRealNavigationEntryCanStartTelemetrySession() {
        val nav = source("app/navigation/AppNavHost.kt")
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(nav.contains("if (drivingNavigation.enterFrom(origin, allowedByAppRoute = allowed))"))
        assertTrue(nav.contains("onDrivingModeEntered(source)"))
        assertTrue(app.contains("fun recordAcceptedDrivingEntry(entrySource: String)"))
        assertTrue(app.contains("drivingSessionOwner.beginFromAcceptedEntry()"))
    }

    @Test fun normalUserExitAndAndroidBackShareExactlySameCallback() {
        val nav = source("app/navigation/AppNavHost.kt")
        val section = nav.substringAfter("val exitDriving: () -> Unit = {")
            .substringBefore("BackHandler(onBack = exitDriving)")
        assertTrue(section.contains("val wasActiveDriving = drivingNavigation.state.active"))
        assertTrue(section.contains("val origin = drivingNavigation.exit()"))
        assertTrue(section.contains("onDrivingModeExplicitlyExited()"))
        assertTrue(nav.contains("onExit = exitDriving"))
    }

    @Test fun permissionRecoveryAndSystemRoutesAreAbandonedNotCompleted() {
        val nav = source("app/navigation/AppNavHost.kt")
        val section = nav.substringAfter("LaunchedEffect(\n        actualRoute,")
            .substringBefore("    val target = when {")
        assertTrue(section.contains("drivingNavigation.clearForSystemBoundary()"))
        assertTrue(section.contains("onDrivingModeSystemInvalidated()"))
        assertFalse(section.contains("onDrivingModeExplicitlyExited()"))
    }

    @Test fun configRecompositionAndActivityStopNeverCompleteSession() {
        val nav = source("app/navigation/AppNavHost.kt")
        val app = source("app/di/ApplicationBindings.kt")
        assertFalse(nav.contains("LaunchedEffect(drivingNavigation.state.active)"))
        val stop = app.substringAfter("fun onUiStopped()")
            .substringBefore("override fun close()")
        assertFalse(stop.contains("completeFromExplicitExit"))
        assertFalse(stop.contains("recordExplicitDrivingExit"))
        assertFalse(stop.contains("drivingSessionOwner.abandonWithoutCompletion"))
    }

    @Test fun acknowledgedVoiceCountMustFollowExecutedNotRecognizedOrRetryTap() {
        val voice = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        val section = voice.substringAfter("private fun publishExecuted(")
            .substringBefore("/** A retryable failure")
        assertTrue(section.contains("mutableState.value = VoiceSessionState.Executed(id, origin)"))
        assertTrue(section.contains("onAcknowledgedDrivingCommand(origin)"))
        assertFalse(voice.substringAfter("private fun recordTerminalFailureLocked(")
            .substringBefore("private fun claimDispatch(")
            .contains("onAcknowledgedDrivingCommand"))
    }

    @Test fun oneAppScopedOwnerIsInjectedNotAnotherMediaServiceOrActivityState() {
        val app = source("app/di/ApplicationBindings.kt")
        val vm = source("app/navigation/DrivingNavigationViewModel.kt")
        assertTrue(app.contains("private val drivingSessionOwner = DrivingSessionTelemetryOwner("))
        assertTrue(app.contains("onAcknowledgedDrivingCommand = ::recordAcknowledgedDrivingVoiceCommand"))
        assertTrue(vm.contains("class DrivingNavigationViewModel : ViewModel()"))
        assertFalse(vm.contains("DrivingSessionTelemetryOwner("))
        assertFalse(app.contains("new MediaSessionService"))
    }

    @Test fun sessionCompletedOnlyComesFromExplicitAppCallback() {
        val app = source("app/di/ApplicationBindings.kt")
        val owner = source("core/application/driving/DrivingSessionTelemetryOwner.kt")
        val callback = app.substringAfter("fun recordExplicitDrivingExit()")
            .substringBefore("fun recordDrivingSystemInvalidation()")
        assertTrue(callback.contains("completeFromExplicitExit()"))
        assertTrue(callback.contains("queueApprovedDiagnostic"))
        assertFalse(callback.contains("SessionCompleted(\"under_1m\", 0)"))
        assertTrue(owner.contains("active = null // claim completion BEFORE"))
        assertTrue(app.contains("if (!diagnosticsCollectionAuthorization.canCollect())"))
    }

    @Test fun noStableIdentifiersOrPersistentDiagnosticMarker() {
        val owner = source("core/application/driving/DrivingSessionTelemetryOwner.kt")
        assertFalse(owner.contains("MediaRef("))
        assertFalse(owner.contains("VoiceSessionId("))
        assertFalse(owner.contains("SharedPreferences"))
        assertFalse(owner.contains("DataStore"))
        assertFalse(owner.contains("RoomDatabase"))
        assertFalse(owner.contains("System.currentTimeMillis()"))
        assertTrue(owner.contains("elapsedRealtimeMs"))
    }

    @Test fun privacyRevocationAndCloseWipePrivateSessionEvidence() {
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.substringAfter("fun revokeDiagnosticsConsent()")
            .substringBefore("fun revokeCloudSpeechConsent()")
            .contains("drivingSessionOwner.forgetForPrivacy()"))
        assertTrue(app.substringAfter("override fun close()")
            .contains("drivingSessionOwner.forgetForPrivacy()"))
    }

    @Test fun entryWithoutBluetoothEvidenceIsNotFabricatedButSessionIsObservable() {
        val app = source("app/di/ApplicationBindings.kt")
        val start = app.substringAfter("fun recordAcceptedDrivingEntry(entrySource: String)")
            .substringBefore("private fun recordAcknowledgedDrivingVoiceCommand(")
        assertTrue(start.indexOf("drivingSessionOwner.beginFromAcceptedEntry()") <
            start.indexOf("CoarseBluetoothAudioOutput.isConnected(mediaAppContext)"))
        assertTrue(start.contains("?: return"))
    }

    @Test fun finishingActivityWithoutAcceptedDrivingExitIsUnknownButRotationIsRetained() {
        val activity = source("app/MainActivity.kt")
        val section = activity.substringAfter("override fun onDestroy()")
            .substringBefore("override fun onStop()")
        assertTrue(section.contains("isFinishing && !isChangingConfigurations"))
        assertTrue(section.contains("recordDrivingSystemInvalidation()"))
        assertFalse(section.contains("recordExplicitDrivingExit()"))
    }

    @Test fun sourceNeverMarksUnknownOrProcessKilledJourneyCrashFree() {
        val app = source("app/di/ApplicationBindings.kt")
        val owner = source("core/application/driving/DrivingSessionTelemetryOwner.kt")
        assertTrue(app.contains("fun recordDrivingSystemInvalidation()"))
        assertTrue(app.contains("drivingSessionOwner.abandonWithoutCompletion()"))
        assertTrue(owner.contains("ABANDONED_UNKNOWN"))
        assertFalse(owner.contains("crashFree = true"))
        assertFalse(owner.contains("CRASH_FREE"))
    }
}
