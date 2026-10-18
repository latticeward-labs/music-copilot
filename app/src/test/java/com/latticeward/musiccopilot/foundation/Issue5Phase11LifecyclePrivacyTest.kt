package com.latticeward.musiccopilot.foundation

import androidx.lifecycle.SavedStateHandle
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEffect
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMachine
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeGate
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.permission.VoiceProviderCapability
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.AppPreferences
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.feature.onboarding.FirstUseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import com.latticeward.musiccopilot.data.api.FirstUsePage
import com.latticeward.musiccopilot.feature.permission.PermissionViewModel
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** P11 lifecycle/privacy contracts. Native process/rotation/device evidence is separate. */
class Issue5Phase11LifecyclePrivacyTest {
    private fun observe(granted: Boolean, rationale: Boolean = false) =
        NativePermissionObservation(granted, true, rationale)

    @Test fun denialPresentationSurvivesProcessRecreationWithoutGrantOrAutoScan() {
        val saved = SavedStateHandle()
        val original = PermissionViewModel(saved)
        original.requestStarting()
        original.nativeResult(observe(false, true), BlockedAction.None)
        assertEquals(MediaPermissionOutcome.DeniedRequestable, original.ui.value.outcome)

        val recreated = PermissionViewModel(saved)
        assertEquals(MediaPermissionOutcome.DeniedRequestable, recreated.ui.value.outcome)
        assertFalse(recreated.takeOneGrantedScan())
        assertFalse(recreated.ui.value.pending)
        assertFalse(saved.keys().any { it.contains("grant", ignoreCase = true) })
    }

    @Test fun restoredGrantResultCannotCarryOneUseAuthorizationAcrossProcessDeath() {
        val saved = SavedStateHandle()
        val original = PermissionViewModel(saved)
        original.requestStarting()
        original.nativeResult(observe(true), BlockedAction.StartScan)
        assertEquals(MediaPermissionOutcome.Granted, original.ui.value.outcome)

        val recreated = PermissionViewModel(saved)
        assertEquals(MediaPermissionOutcome.None, recreated.ui.value.outcome)
        assertFalse(recreated.takeOneGrantedScan())
        // Same ViewModel retains the legitimate single-use gesture on rotation.
        assertTrue(original.takeOneGrantedScan())
        assertFalse(original.takeOneGrantedScan())
    }

    @Test fun restoredSettingsTicketNeedsFreshOsObservationAndSingleContinue() {
        val saved = SavedStateHandle()
        val old = PermissionViewModel(saved)
        repeat(2) {
            old.requestStarting()
            old.nativeResult(observe(false), BlockedAction.None)
        }
        assertEquals(MediaPermissionOutcome.SettingsOnly, old.ui.value.outcome)
        old.settingsTapped()
        old.onActivityPaused()

        val recreated = PermissionViewModel(saved)
        assertEquals(MediaPermissionOutcome.SettingsOnly, recreated.ui.value.outcome)
        assertFalse(recreated.takeOneGrantedScan())
        recreated.onReturnFromSettings(observe(true))
        assertEquals(MediaPermissionOutcome.Granted, recreated.ui.value.outcome)
        assertTrue(recreated.takeOneGrantedScan())
        assertFalse(recreated.takeOneGrantedScan())
        assertFalse(saved.keys().any { it.contains("grant", ignoreCase = true) })
    }

    @Test fun cancellationAndManualExitDiscardPermissionPresentation() {
        val saved = SavedStateHandle()
        val vm = PermissionViewModel(saved)
        vm.requestStarting()
        vm.nativeLaunchFailed()
        assertEquals(MediaPermissionOutcome.DeniedRequestable, vm.ui.value.outcome)
        vm.manualExit()
        val recreated = PermissionViewModel(saved)
        assertEquals(MediaPermissionOutcome.None, recreated.ui.value.outcome)
        assertFalse(recreated.takeOneGrantedScan())
    }

    @Test fun voiceRationaleRestoresWithoutLaunchingNativeOrVoiceSession() {
        val old = VoicePracticeGate()
        assertTrue(old.practiceTapped())
        val restored = VoicePracticeGate()
        restored.restorePresentation(old.stage.name)
        assertEquals(VoicePracticeStage.Rationale, restored.stage)
        assertFalse(restored.awaitingAndroidResult)
        assertFalse(restored.practiceTapped())
        // A NEW explicit Continue is required to reach the Android permission boundary.
        assertTrue(restored.confirmRationale())
        assertTrue(restored.awaitingAndroidResult)
        assertFalse(restored.confirmRationale())
    }

    @Test fun voiceNativePendingDoesNotIssueDuplicateRequestOnRecreation() {
        val restored = VoicePracticeGate()
        restored.restorePending()
        restored.restorePresentation(VoicePracticeStage.Rationale.name)
        assertEquals(VoicePracticeStage.AwaitingAndroid, restored.stage)
        assertFalse(restored.practiceTapped())
        assertFalse(restored.confirmRationale())
        assertEquals(VoicePracticeStage.PermissionDenied,
            restored.androidResult(false, VoiceProviderCapability.ProviderUnavailable))
        assertFalse(restored.awaitingAndroidResult)
    }

    @Test fun noSavedGrantOrReadyProviderCanAuthorizeRestoredVoice() {
        for (unsafe in listOf(VoicePracticeStage.ReadyForVoiceHandoff.name,
                VoicePracticeStage.AwaitingAndroid.name, "bogus")) {
            val restored = VoicePracticeGate()
            restored.restorePresentation(unsafe)
            assertEquals(VoicePracticeStage.Idle, restored.stage)
            assertFalse(restored.awaitingAndroidResult)
        }
        val denied = VoicePracticeGate()
        denied.restorePresentation(VoicePracticeStage.PermissionDenied.name)
        assertEquals(VoicePracticeStage.PermissionDenied, denied.stage)
        denied.manualOnly()
        assertEquals(VoicePracticeStage.Idle, denied.stage)
    }

    @Test fun firstUseInterruptedSaveReentersFromAcknowledgedCheckpointOnly() {
        val first = FirstUseMachine.enter(false, FirstUsePage.Value)
        val saving = FirstUseMachine.reduce(first, FirstUseEvent.Continue)
        assertTrue(saving.state is FirstUseState.Saving)
        assertTrue(saving.effects.any { it is FirstUseEffect.PersistCheckpoint })
        // Process disappears before the ACK: no second write or hidden Finish.
        val recovered = FirstUseMachine.enter(false, FirstUsePage.Value)
        assertTrue(recovered is FirstUseState.Education)
        assertEquals(FirstUsePage.Value, (recovered as FirstUseState.Education).page)
        val afterAck = FirstUseMachine.enter(false, FirstUsePage.SafetyAndVoice)
        assertEquals(FirstUsePage.SafetyAndVoice, (afterAck as FirstUseState.Education).page)
    }

    @Test fun replayPageSurvivesProcessRecreationWithoutDataStoreWrite() {
        val saved = SavedStateHandle()
        var updates = 0
        val snapshot = MutableStateFlow(AppPreferences(false, null, CloudSpeechConsent.NotAsked))
        val load = MutableStateFlow(PreferencesLoadState.Ready)
        val repository = object : PreferencesRepository {
            override val preferences = snapshot
            override val loadState = load
            override suspend fun reload() = AppResult.Success(snapshot.value)
            override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> {
                updates++
                return AppResult.Success(snapshot.value)
            }
        }
        val original = FirstUseViewModel(repository, true, saved)
        original.dispatch(FirstUseEvent.Continue, {}, {})
        assertEquals(FirstUsePage.SafetyAndVoice,
            (original.machine as FirstUseState.Education).page)
        val restored = FirstUseViewModel(repository, true, saved)
        assertEquals(FirstUsePage.SafetyAndVoice,
            (restored.machine as FirstUseState.Education).page)
        assertEquals(0, updates)
    }

    @Test fun telemetryRejectsFakeTitlesTranscriptsPathsBluetoothAndLocation() {
        val gate = AllowlistedTelemetryPrivacyGate()
        val forbidden = listOf(
            "private-artist-and-song-title",
            "full spoken voice transcript",
            "/storage/emulated/0/Music/private.mp3",
            "content://media/external/audio/media/123",
            "device-AA:BB:CC:DD:EE:FF",
            "GPS 35.7,51.4",
            "android.permission.RECORD_AUDIO=granted",
        )
        forbidden.forEach { value ->
            assertTrue(gate.sanitize(
                TelemetryEvent.IntentDetected(value, ConfidenceBucket.Medium)) is AppResult.Failure)
            assertTrue(gate.sanitize(
                TelemetryEvent.DrivingModeStarted(value, true)) is AppResult.Failure)
            assertTrue(gate.sanitize(
                TelemetryEvent.ManualCorrection("PLAY", value)) is AppResult.Failure)
            assertTrue(gate.sanitize(
                TelemetryEvent.IntentFailed("PLAY", value, null, null)) is AppResult.Failure)
        }
        assertTrue(gate.sanitize(
            TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.Medium)) is AppResult.Success)
    }

    @Test fun lifecycleAndCancellationStaticGuardRejectsNewAuthorityOrPrivateLogs() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val activity = File(root + "app/MainActivity.kt").readText()
        val voice = File(root + "core/application/permission/VoicePracticeGate.kt").readText()
        val db = File(root + "app/di/DataBindings.kt").readText()
        val quality = File(root + "quality/adapters/BestEffortQualityEventPublisher.kt").readText()
        val prefs = File(root + "data/datastore/PreferencesDataStore.kt").readText()
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(activity.contains("p11_voice_practice_stage"))
        assertTrue(activity.contains("voicePracticeGate.restorePending()"))
        assertTrue(voice.contains("fun restorePresentation(savedStageName: String?)"))
        assertTrue(db.contains("catch (cancelled: CancellationException)"))
        assertTrue(quality.contains("catch (cancelled: CancellationException)"))
        assertTrue(prefs.contains("catch (cancelled: CancellationException)"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("android.permission.POST_NOTIFICATIONS"))
        assertFalse(activity.contains("voiceSessionCoordinator.start("))
        val resumeOnly = activity.substringAfter("override fun onResume()")
            .substringBefore("@Composable")
        assertFalse(resumeOnly.contains("scanAfterExplicitMediaGrant("))
        assertFalse(resumeOnly.contains("microphoneLauncher.launch("))
        assertFalse(resumeOnly.contains("requestLauncher.launch("))
        assertFalse(activity.contains("Log.d("))
        assertFalse(quality.contains("printStackTrace("))
    }
}
