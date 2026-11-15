package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.adapters.BoundedMemoryDiagnosticsStore
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.ConsentGatedTelemetrySink
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #12 P09, source-authored only. Do NOT run build/test/CI before P14.
 * P12 owns actual UX proof and user-led device deletion verification.
 */
class Issue12Phase9DiagnosticsDeletionWiringTest {
    private val root = "src/main/java/com/latticeward/musiccopilot/"
    private fun read(file: String) = File(root + file).readText()

    @Test fun productionProviderIsNoOpAndNoRemoteDiagnosticsPathIsInstalled() {
        val quality = read("app/di/QualityBindings.kt")
        assertTrue(quality.contains("LocalDiagnosticsProviderMode.NoOp"))
        assertTrue(quality.contains("NoOpTelemetrySink()"))
        assertTrue(quality.contains("ConsentGatedTelemetrySink"))
        assertFalse(quality.contains("FirebaseAnalytics"))
        assertFalse(quality.contains("Sentry.init"))
        assertFalse(quality.contains("HttpURLConnection"))
    }

    @Test fun bypassingPrivacyGateCannotHappenInAppPublisher() {
        val publisher = read("quality/adapters/BestEffortQualityEventPublisher.kt")
        assertTrue(publisher.contains("privacyGate.sanitize(event)"))
        assertTrue(publisher.contains("if (!canCollect()) false"))
        assertTrue(publisher.contains("sink.tryEmit(outcome.value)"))
        val bindings = read("app/di/ApplicationBindings.kt")
        assertTrue(bindings.contains("qualityEventPublisher.tryRecord(pending.event)"))
        assertFalse(bindings.contains("localDiagnosticsStore.tryEmit(event)"))
    }

    @Test fun aStaleEventQueuedBeforeUserClearsCannotReappear() {
        val bindings = read("app/di/ApplicationBindings.kt")
        assertTrue(bindings.contains("private val diagnosticsEpoch = AtomicLong(0L)"))
        assertTrue(bindings.contains("PendingDiagnostic(diagnosticsEpoch.get(), event)"))
        assertTrue(bindings.contains("pending.epoch == diagnosticsEpoch.get()"))
        assertTrue(bindings.contains("synchronized(diagnosticsDeliveryLock)"))
        assertTrue(bindings.contains("diagnosticsEpoch.incrementAndGet()"))
        assertTrue(bindings.contains("diagnosticEvents.tryReceive().isSuccess"))
    }

    @Test fun userClearOnlyMutatesDiagnosticStateNotMusicData() {
        val app = read("app/di/ApplicationBindings.kt")
        val section = app.substringAfter("fun clearLocalDiagnostics(): Boolean")
            .substringBefore("fun localDiagnosticsSummaryIfApproved()")
        for (required in listOf("localDiagnosticsStore.clear()",
                "crashEvidenceReporter.clear()", "correctionOwner.clear()",
                "drivingSessionOwner.forgetForPrivacy()")) {
            assertTrue(section.contains(required))
        }
        for (forbidden in listOf("libraryRepository", "restorableQueueStore",
                "preferencesRepository.update", "playbackGateway.execute",
                "selectedBluetoothHint", "cloudSpeechConsent")) {
            assertFalse(section.contains(forbidden))
        }
    }

    @Test fun consentRevokeLatchesImmediatelyBeforeAsyncPreferenceMutation() {
        val app = read("app/di/ApplicationBindings.kt")
        val code = app.substringAfter("fun revokeDiagnosticsConsent()")
            .substringBefore("fun revokeCloudSpeechConsent()")
        assertTrue(code.indexOf("requestImmediateRevocation()") <
            code.indexOf("clearLocalDiagnostics()"))
        assertTrue(code.indexOf("clearLocalDiagnostics()") <
            code.indexOf("repositoryScope.launch"))
    }

    @Test fun currentUserStoredGrantedStillNotEnoughForProductionCollection() {
        val authorization = read("core/application/privacy/DiagnosticsConsentPolicy.kt")
        val quality = read("app/di/QualityBindings.kt")
        for (must in listOf("privacyCopyApproved", "retentionApproved",
                "collectionAdapterApproved", "legalApproved")) {
            assertTrue(authorization.contains(must))
        }
        assertTrue(quality.contains("DiagnosticsCollectionAuthorization(repository)"))
    }

    @Test fun realPublisherStaysNonblockingAndOptionalSinkFailureDoesNotEscape() {
        var consent = true
        val sink = BoundedMemoryDiagnosticsStore({ consent }, maxEvents = 3)
        val publisher = BestEffortQualityEventPublisher(
            privacyGate = AllowlistedTelemetryPrivacyGate(),
            sink = ConsentGatedTelemetrySink(sink) { consent },
            canCollect = { consent },
        )
        val event = TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused)
        assertTrue(publisher.tryRecord(event))
        assertEquals(1, sink.summaryIfAuthorized()!!.retainedEvents)
        consent = false
        assertFalse(publisher.tryRecord(event))
        assertNull(sink.summaryIfAuthorized())
    }

    @Test fun cannotStorePrivateVoiceOutcomeEvenIfFutureSinkIsMemoryOnly() {
        val publisher = BestEffortQualityEventPublisher(
            privacyGate = AllowlistedTelemetryPrivacyGate(),
            sink = BoundedMemoryDiagnosticsStore({ true }),
            canCollect = { true },
        )
        val outcome = TelemetryEvent.VoiceIntentOutcome(
            intentType = com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent.PLAY,
            confidenceBucket = null,
            latencyBucket = null,
            resultCode = com.latticeward.musiccopilot.quality.api.VoiceDiagnosticResult.EXECUTED,
        )
        assertFalse(publisher.tryRecord(outcome))
    }

    @Test fun repositoryDoesNotAddDiagnosticFileDatabaseOrCloudBackend() {
        val store = read("quality/adapters/BoundedMemoryDiagnosticsStore.kt")
        assertTrue(store.contains("ArrayDeque<SanitizedTelemetryEvent>()"))
        for (forbidden in listOf("FileOutputStream", "RoomDatabase",
                "SharedPreferences", "SQLiteDatabase", "HttpClient",
                "WebSocket", "Firebase", "Sentry", "getExternalFilesDir")) {
            assertFalse(store.contains(forbidden))
        }
    }

    @Test fun noUnapprovedTtlPretendsToBeProductSignedOff() {
        val store = read("quality/adapters/BoundedMemoryDiagnosticsStore.kt")
        assertTrue(store.contains("no final time-based retention/TTL is approved"))
        assertFalse(store.contains("retentionDays ="))
        assertFalse(store.contains("24 * 60 * 60 * 1000"))
    }

    @Test fun anUnapprovedProviderDoesNotCreateASilentExport() {
        val app = read("app/di/ApplicationBindings.kt")
        val quality = read("app/di/QualityBindings.kt")
        assertTrue(quality.contains("PRODUCTION_LOCAL_DIAGNOSTICS_MODE"))
        assertTrue(quality.contains("LocalDiagnosticsProviderMode.MemoryOnly"))
        assertTrue(app.contains("fun localDiagnosticsSummaryIfApproved()"))
        assertFalse(app.contains("uploadDiagnostics("))
        assertFalse(app.contains("sendAnalyticsToServer("))
    }

    @Test fun deletionIsIndependentOfCloudSpeechConsent() {
        val app = read("app/di/ApplicationBindings.kt")
        val clear = app.substringAfter("fun clearLocalDiagnostics(): Boolean")
            .substringBefore("fun localDiagnosticsSummaryIfApproved()")
        assertFalse(clear.contains("PreferenceUpdate.SetCloudSpeechConsent"))
        assertFalse(clear.contains("SetDiagnosticsConsent"))
    }

    @Test fun sourceOnlyNoDiagnosticsCanOverruleMediaSessionAuthority() {
        val app = read("app/di/ApplicationBindings.kt")
        val gate = read("quality/privacy/ConsentGatedTelemetrySink.kt")
        assertFalse(gate.contains("PlaybackCommand"))
        assertFalse(gate.contains("MediaSession"))
        assertFalse(app.contains("localDiagnosticsStore.applyPlayback"))
    }
}
