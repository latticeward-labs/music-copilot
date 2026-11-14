package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.voice.CloudSpeechConsentPolicy
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
import com.latticeward.musiccopilot.core.application.voice.LocalVoiceDiagnostic
import com.latticeward.musiccopilot.core.application.voice.RemoteSpeechDisclosure
import com.latticeward.musiccopilot.core.application.voice.VoicePrivacyDiagnosticsPolicy
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P11: SOURCE-AUTHORED ONLY — 19 tests, 0 executed.
 * Not a device, network, runtime permission or compiled privacy attestation.
 */
class Issue10Phase11VoicePrivacyBoundaryTest {
    private val diagnostics = VoicePrivacyDiagnosticsPolicy
    private val disclosure = RemoteSpeechDisclosure(
        processor = "Approved processor", destination = "Declared processing region",
        purpose = "Voice command recognition", retention = "Explicit retention policy",
    )

    @Test fun transcriptDoesNotAppearInIncidentalResultStringification() {
        val secret = "private spoken address voice phrase"
        val printed = SpeechRecognitionResult(secret, 0.95, "en-US").toString()
        assertFalse(printed.contains(secret))
        assertFalse(printed.contains("en-US"))
        assertEquals("SpeechRecognitionResult(redacted)", printed)
    }

    @Test fun redactionDoesNotCorruptInMemoryDeterministicIntentMappingInput() {
        val result = SpeechRecognitionResult("pause", 0.99, null)
        assertEquals("pause", result.transcript)
        assertEquals(0.99, result.providerConfidence ?: 0.0, 0.0)
        assertFalse(result.toString().contains("pause"))
    }

    @Test fun storedConsentAloneCannotActivateMvpCloudStt() {
        assertEquals(CloudSpeechGateDecision.DisabledNoApprovedAdapter,
            CloudSpeechConsentPolicy.evaluate(false, false, null, true, false))
        assertEquals(CloudSpeechGateDecision.DisabledNoApprovedAdapter,
            diagnostics.remoteMvp())
    }

    @Test fun legalApprovalIsIndependentlyRequired() {
        assertEquals(CloudSpeechGateDecision.DisabledNoLegalApproval,
            CloudSpeechConsentPolicy.evaluate(true, false, disclosure, true, false))
    }

    @Test fun missingOrPlaceholderDisclosureAlwaysFailsClosed() {
        val variants = listOf(
            null,
            RemoteSpeechDisclosure("UNKNOWN", "EU", "recognition", "30d"),
            RemoteSpeechDisclosure("provider", "Tbd", "recognition", "30d"),
            RemoteSpeechDisclosure("provider", "EU", "  ", "30d"),
            RemoteSpeechDisclosure("provider", "EU", "recognition", "N/A"),
            RemoteSpeechDisclosure("provider", "EU", "recognition", "PENDING"),
            RemoteSpeechDisclosure("provider", "EU", "recognition", "-"),
            RemoteSpeechDisclosure("provider", "EU", "recognition", "not disclosed"),
            RemoteSpeechDisclosure("provider", "EU\nsecret", "recognition", "30d"),
            RemoteSpeechDisclosure("provider", "EU", "recognition", "x".repeat(257)),
        )
        for (bad in variants) {
            assertEquals(CloudSpeechGateDecision.DisabledNoDisclosure,
                CloudSpeechConsentPolicy.evaluate(true, true, bad, true, false))
        }
    }

    @Test fun nonConsentingUserRemainsLocalEvenWithFutureApprovedProcessor() {
        assertEquals(CloudSpeechGateDecision.DisabledNoConsent,
            CloudSpeechConsentPolicy.evaluate(true, true, disclosure, false, false))
    }

    @Test fun drivingDeniesCloudDespiteOtherwiseApprovedParameters() {
        assertEquals(CloudSpeechGateDecision.DisabledWhileDriving,
            CloudSpeechConsentPolicy.evaluate(true, true, disclosure, true, true))
    }

    @Test fun futurePermittedGateStillNeedsAllFiveIndependentConditions() {
        // This policy's theoretical branch is NOT installed in the MVP.
        assertEquals(CloudSpeechGateDecision.Permitted,
            CloudSpeechConsentPolicy.evaluate(true, true, disclosure, true, false))
    }

    @Test fun androidTenAndElevenRemainManualOnlyWithPermissionOrProvider() {
        for (api in listOf(29, 30)) {
            assertEquals(LocalVoiceDiagnostic.ManualOnlyPlatform,
                diagnostics.local(api, true, true))
        }
    }

    @Test fun deniedMicrophonePermissionWinsOverProviderAvailability() {
        assertEquals(LocalVoiceDiagnostic.MicrophonePermissionRequired,
            diagnostics.local(35, false, true))
    }

    @Test fun missingOnDeviceProviderDoesNotPretendEnglishModelExists() {
        assertEquals(LocalVoiceDiagnostic.OnDeviceProviderUnavailable,
            diagnostics.local(35, true, false))
    }

    @Test fun providerPresentIsExplicitlyUnverifiedNotDeclaredReady() {
        assertEquals(LocalVoiceDiagnostic.OnDeviceProviderPresentUnverified,
            diagnostics.local(35, true, true))
    }

    @Test fun telemetrySinkDropsSanitizedEventsRatherThanUploading() {
        val result = AllowlistedTelemetryPrivacyGate().sanitize(
            TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused))
        assertTrue(result is AppResult.Success)
        assertFalse(NoOpTelemetrySink().tryEmit((result as AppResult.Success).value))
    }

    @Test fun arbitrarySpokenOrPathTextIsNotAcceptedAsEventType() {
        val text = "/storage/emulated/0/Music/private-song.mp3"
        val gate = AllowlistedTelemetryPrivacyGate()
        assertTrue(gate.sanitize(TelemetryEvent.IntentDetected(
            text, com.latticeward.musiccopilot.quality.api.ConfidenceBucket.High
        )) is AppResult.Failure)
        assertTrue(gate.sanitize(TelemetryEvent.IntentFailed(
            intentType = null, failureCode = text,
            confidenceBucket = null, latencyBucket = null
        )) is AppResult.Failure)
    }

    @Test fun productionBindingsDoNotRegisterRemoteSttOrAnalyticsTransport() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val bindings = File(root, "app/di/VoiceBindings.kt").readText()
        val app = File(root, "app/di/ApplicationBindings.kt").readText()
        val quality = File(root, "app/di/QualityBindings.kt").readText()
        assertTrue(bindings.contains("AndroidOnDeviceSpeechToTextEngine(context, dispatchers)"))
        assertFalse(bindings.contains("createSpeechRecognizer("))
        assertTrue(app.contains("remoteAdapterApproved = false"))
        assertTrue(app.contains("legalApproval = false"))
        assertTrue(quality.contains("NoOpTelemetrySink()"))
        assertFalse(quality.contains("FirebaseAnalytics"))
    }

    @Test fun manifestHasNoInternetOrBackupVoiceDataExport() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:usesCleartextTraffic=\"false\""))
        assertFalse(manifest.contains("android.permission.RECORD_AUDIO\" android:required"))
    }

    @Test fun onDeviceProviderNeverUsesGenericOrCloudRecognizer() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val stt = File(root, "voice/android/AndroidOnDeviceSpeechToTextEngine.kt").readText()
        assertTrue(stt.contains("SpeechRecognizer.createOnDeviceSpeechRecognizer(app)"))
        assertTrue(stt.contains("putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)"))
        assertFalse(stt.contains("SpeechRecognizer.createSpeechRecognizer("))
        assertFalse(stt.contains("triggerModelDownload("))
        assertFalse(stt.contains("http://"))
        assertFalse(stt.contains("https://"))
    }

    @Test fun recognitionCoordinatorHasNoAudioPersistenceOrFreeformLogging() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val c = File(root, "voice/session/DefaultVoiceSessionCoordinator.kt").readText()
        assertFalse(c.contains("Log.d("))
        assertFalse(c.contains("Log.e("))
        assertFalse(c.contains("printStackTrace"))
        assertFalse(c.contains("transcript.toString()"))
        assertFalse(c.contains("SharedPreferences"))
        assertFalse(c.contains("RoomDatabase"))
        assertFalse(c.contains("FileOutputStream"))
        assertFalse(c.contains("FirebaseCrashlytics"))
    }

    @Test fun settingsShowsOnlyClosedStatusAndDoesNotOfferGrantCloudConsent() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val nav = File(root, "app/navigation/AppNavHost.kt").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(nav.contains("VoicePrivacyDiagnosticsPolicy.local("))
        assertTrue(nav.contains("p11_voice_privacy_heading"))
        assertTrue(nav.contains("p11_voice_provider_unverified"))
        assertFalse(nav.contains("SetCloudSpeechConsent(CloudSpeechConsent.Granted)"))
        assertTrue(strings.contains("Diagnostic events are not sent in this version."))
    }
}
