package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.AppPreferences
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.DiagnosticsConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.ConsentGatedTelemetrySink
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsCollectionApprovals
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsCollectionAuthorization
import com.latticeward.musiccopilot.quality.privacy.DiagnosticsConsentPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * P04 SOURCE-AUTHORED ONLY: do not compile, test, run JVM, CI or phone until
 * owner authorizes the final integrated QA phase.
 */
class Issue12Phase4DiagnosticsConsentTest {
    private val reviewed = DiagnosticsCollectionApprovals(
        privacyCopyApproved = true,
        retentionApproved = true,
        collectionAdapterApproved = true,
        legalApproved = true,
    )
    private fun repo(
        consent: DiagnosticsConsent = DiagnosticsConsent.NotAsked,
        state: PreferencesLoadState = PreferencesLoadState.Ready,
        cloud: CloudSpeechConsent = CloudSpeechConsent.NotAsked,
    ) = FakeConsentPreferences(consent, state, cloud)

    private class FakeConsentPreferences(
        consent: DiagnosticsConsent,
        state: PreferencesLoadState,
        cloud: CloudSpeechConsent,
    ) : PreferencesRepository {
        val data = MutableStateFlow(AppPreferences(
            onboardingComplete = false, selectedBluetoothHint = null,
            cloudSpeechConsent = cloud, diagnosticsConsent = consent
        ))
        val ready = MutableStateFlow(state)
        override val preferences: StateFlow<AppPreferences> = data
        override val loadState: StateFlow<PreferencesLoadState> = ready
        override suspend fun reload(): AppResult<AppPreferences> = AppResult.Success(data.value)
        override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> {
            if (update is PreferenceUpdate.SetDiagnosticsConsent) {
                data.value = data.value.copy(diagnosticsConsent = update.value)
            }
            return AppResult.Success(data.value)
        }
    }

    @Test fun missingOldPreferenceDefaultsToIndependentNotAsked() {
        val old = AppPreferences(false, null, CloudSpeechConsent.Granted)
        assertEquals(DiagnosticsConsent.NotAsked, old.diagnosticsConsent)
    }

    @Test fun absentDeniedAndRevokedPreferencesAllFailClosed() {
        for (v in listOf(DiagnosticsConsent.NotAsked, DiagnosticsConsent.Declined,
                DiagnosticsConsent.Revoked)) {
            assertFalse(DiagnosticsCollectionAuthorization(repo(v), reviewed).canCollect())
        }
    }

    @Test fun storedGrantedAloneDoesNotApproveProductionCollection() {
        val auth = DiagnosticsCollectionAuthorization(repo(DiagnosticsConsent.Granted))
        assertFalse(auth.canCollect())
    }

    @Test fun everyApprovalIsIndependentlyRequired() {
        val p = repo(DiagnosticsConsent.Granted)
        assertFalse(DiagnosticsCollectionAuthorization(p,
            reviewed.copy(privacyCopyApproved = false)).canCollect())
        assertFalse(DiagnosticsCollectionAuthorization(p,
            reviewed.copy(retentionApproved = false)).canCollect())
        assertFalse(DiagnosticsCollectionAuthorization(p,
            reviewed.copy(collectionAdapterApproved = false)).canCollect())
        assertFalse(DiagnosticsCollectionAuthorization(p,
            reviewed.copy(legalApproved = false)).canCollect())
        assertTrue(DiagnosticsCollectionAuthorization(p, reviewed).canCollect())
    }

    @Test fun preferenceStorageUnavailableLoadingOrFailedCannotCollect() {
        for (state in listOf(PreferencesLoadState.Loading, PreferencesLoadState.Failed)) {
            assertFalse(DiagnosticsCollectionAuthorization(
                repo(DiagnosticsConsent.Granted, state), reviewed).canCollect())
        }
    }

    @Test fun cloudSpeechGrantDoesNotGrantNonAudioDiagnostics() {
        val p = repo(DiagnosticsConsent.NotAsked, cloud = CloudSpeechConsent.Granted)
        assertFalse(DiagnosticsCollectionAuthorization(p, reviewed).canCollect())
        p.data.value = p.data.value.copy(diagnosticsConsent = DiagnosticsConsent.Granted,
            cloudSpeechConsent = CloudSpeechConsent.Revoked)
        assertTrue(DiagnosticsCollectionAuthorization(p, reviewed).canCollect())
    }

    @Test fun currentStateIsReadOnEveryAuthorizationAttempt() {
        val p = repo(DiagnosticsConsent.Granted)
        val auth = DiagnosticsCollectionAuthorization(p, reviewed)
        assertTrue(auth.canCollect())
        p.data.value = p.data.value.copy(diagnosticsConsent = DiagnosticsConsent.Declined)
        assertFalse(auth.canCollect())
        p.data.value = p.data.value.copy(diagnosticsConsent = DiagnosticsConsent.Granted)
        p.ready.value = PreferencesLoadState.Loading
        assertFalse(auth.canCollect())
    }

    @Test fun immediateRevokeLatchBlocksEvenBeforePersistedValueChanges() {
        val p = repo(DiagnosticsConsent.Granted)
        val auth = DiagnosticsCollectionAuthorization(p, reviewed)
        assertTrue(auth.canCollect())
        auth.requestImmediateRevocation()
        assertFalse(auth.canCollect())
        // Simulated edit failure means stored state can remain Granted. Fail closed.
        assertEquals(DiagnosticsConsent.Granted, p.data.value.diagnosticsConsent)
        assertFalse(auth.canCollect())
    }

    @Test fun publisherChecksBeforeSanitizationAndBeforeSinkDelivery() {
        val p = repo(DiagnosticsConsent.NotAsked)
        val auth = DiagnosticsCollectionAuthorization(p, reviewed)
        var sanitizations = 0
        var emits = 0
        val gate = object : TelemetryPrivacyGate {
            override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> {
                sanitizations++
                return AllowlistedTelemetryPrivacyGate().sanitize(event)
            }
        }
        val sink = ConsentGatedTelemetrySink(object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                emits++
                return true
            }
        }, auth::canCollect)
        val publish = BestEffortQualityEventPublisher(gate, sink, auth::canCollect)
        val event = TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)
        assertFalse(publish.tryRecord(event))
        assertEquals(0, sanitizations)
        assertEquals(0, emits)
        p.data.value = p.data.value.copy(diagnosticsConsent = DiagnosticsConsent.Granted)
        assertTrue(publish.tryRecord(event))
        assertEquals(1, sanitizations)
        assertEquals(1, emits)
        auth.requestImmediateRevocation()
        assertFalse(publish.tryRecord(event))
        assertEquals(1, sanitizations)
        assertEquals(1, emits)
    }

    @Test fun preSanitizedPendingEventIsStillDeniedAtFlush() {
        val p = repo(DiagnosticsConsent.Granted)
        val auth = DiagnosticsCollectionAuthorization(p, reviewed)
        var emits = 0
        val sink = ConsentGatedTelemetrySink(object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                emits++
                return true
            }
        }, auth::canCollect)
        val event = (AllowlistedTelemetryPrivacyGate().sanitize(
            TelemetryEvent.IntentDetected("NEXT", ConfidenceBucket.Medium))
            as AppResult.Success).value
        auth.requestImmediateRevocation()
        assertFalse(sink.tryEmit(event))
        assertEquals(0, emits)
    }

    @Test fun unexpectedGateAndSinkExceptionsCannotChangeProduct() {
        val auth = DiagnosticsCollectionAuthorization(repo(DiagnosticsConsent.Granted), reviewed)
        val brokenSink = ConsentGatedTelemetrySink(
            object : TelemetrySink {
                override fun tryEmit(event: SanitizedTelemetryEvent): Boolean =
                    throw IllegalStateException("provider private filename")
            }, auth::canCollect
        )
        val event = (AllowlistedTelemetryPrivacyGate().sanitize(
            TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High))
            as AppResult.Success).value
        assertFalse(brokenSink.tryEmit(event))
        assertFalse(NoOpTelemetrySink().tryEmit(event))
    }

    @Test fun sourceWiringKeepsNoOpOfflineAndDistinctDataStoreKey() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val data = File(root + "data/datastore/PreferencesDataStore.kt").readText()
        val binding = File(root + "app/di/QualityBindings.kt").readText()
        val app = File(root + "app/di/ApplicationBindings.kt").readText()
        assertTrue(data.contains("non_audio_diagnostics_consent"))
        assertTrue(data.contains("cloud_speech_consent"))
        assertTrue(binding.contains("NoOpTelemetrySink()"))
        assertTrue(binding.contains("ConsentGatedTelemetrySink"))
        assertTrue(app.contains("diagnosticsCollectionAuthorization.requestImmediateRevocation()"))
        assertTrue(app.contains("QualityBindings.sink(diagnosticsCollectionAuthorization)"))
    }
}
