package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.adapters.BoundedMemoryDiagnosticsStore
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticResult
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #12 P09 — only authored, NOT compiled/tested/run until final P14.
 * These are privacy, boundedness and explicit-deletion contracts, not
 * evidence of live consumer telemetry or an approved retention SLA.
 */
class Issue12Phase9BoundedDiagnosticsStoreTest {
    private val schema = AllowlistedTelemetryPrivacyGate()

    private fun approved(event: TelemetryEvent): SanitizedTelemetryEvent {
        val result = schema.sanitize(event)
        assertTrue(result is AppResult.Success<*>)
        return (result as AppResult.Success<SanitizedTelemetryEvent>).value
    }

    private fun ptt() = approved(
        TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused)
    )

    @Test fun defaultFactoryBoundIs128AndConfigCannotBeUnbounded() {
        assertEquals(128, BoundedMemoryDiagnosticsStore.DEFAULT_MAX_EVENTS)
        assertEquals(1024, BoundedMemoryDiagnosticsStore.MAX_CONFIGURABLE_EVENTS)
        assertThrows(IllegalArgumentException::class.java) {
            BoundedMemoryDiagnosticsStore({ true }, maxEvents = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            BoundedMemoryDiagnosticsStore({ true }, maxEvents = 1025)
        }
    }

    @Test fun noConsentMeansNoCaptureOrAggregateDisclosure() {
        val store = BoundedMemoryDiagnosticsStore({ false })
        assertFalse(store.tryEmit(ptt()))
        assertNull(store.summaryIfAuthorized())
        assertNull(store.snapshotSanitizedForLocalProjection())
    }

    @Test fun sanitizedCaptureOnlyAfterRealAuthorization() {
        var approvedConsent = false
        val store = BoundedMemoryDiagnosticsStore({ approvedConsent })
        assertFalse(store.tryEmit(ptt()))
        approvedConsent = true
        assertTrue(store.tryEmit(ptt()))
        assertEquals(1, store.summaryIfAuthorized()!!.retainedEvents)
        assertEquals(1L, store.summaryIfAuthorized()!!.acceptedSinceClear)
    }

    @Test fun ringIsBoundedAndOldestRecordIsEvicted() {
        val store = BoundedMemoryDiagnosticsStore({ true }, maxEvents = 2)
        store.tryEmit(ptt())
        store.tryEmit(approved(TelemetryEvent.ManualCorrection("PLAY", "manual_pause")))
        store.tryEmit(approved(TelemetryEvent.SessionCompleted("under_1m", 1)))
        val summary = store.summaryIfAuthorized()!!
        assertEquals(2, summary.retainedEvents)
        assertEquals(3L, summary.acceptedSinceClear)
        assertEquals(1L, summary.evictedOldestSinceClear)
        assertEquals(1L, summary.countsByFamilySinceClear["voice_button_pressed"])
        assertEquals(1L, summary.countsByFamilySinceClear["manual_correction"])
        assertEquals(1L, summary.countsByFamilySinceClear["session_completed"])
        val live = store.snapshotSanitizedForLocalProjection()!!
        assertEquals(2, live.size)
        assertFalse(live.any { it.encoded.contains("voice_button_pressed") })
    }

    @Test fun exactEightFamilyCountsOnlyAndNoPerEventValuesInSummary() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        store.tryEmit(ptt())
        store.tryEmit(approved(TelemetryEvent.SessionCompleted("1_to_5m", 0)))
        val summary = store.summaryIfAuthorized()!!
        assertEquals(setOf("voice_button_pressed", "session_completed"),
            summary.countsByFamilySinceClear.keys)
        assertFalse(summary.toString().contains("commands_count="))
        assertFalse(summary.toString().contains("playback_state="))
        assertFalse(summary.toString().contains("1_to_5m"))
    }

    @Test fun clearDeletesBothRetainedEventsAndAggregateCounters() {
        val store = BoundedMemoryDiagnosticsStore({ true }, maxEvents = 1)
        repeat(5) { store.tryEmit(ptt()) }
        assertEquals(4L, store.summaryIfAuthorized()!!.evictedOldestSinceClear)
        store.clear()
        val summary = store.summaryIfAuthorized()!!
        assertEquals(0, summary.retainedEvents)
        assertEquals(0L, summary.acceptedSinceClear)
        assertEquals(0L, summary.evictedOldestSinceClear)
        assertTrue(summary.countsByFamilySinceClear.isEmpty())
        assertTrue(store.snapshotSanitizedForLocalProjection()!!.isEmpty())
    }

    @Test fun clearIsIdempotentEvenWithoutConsent() {
        val store = BoundedMemoryDiagnosticsStore({ false })
        store.clear()
        store.clear()
        assertNull(store.summaryIfAuthorized())
    }

    @Test fun turningOffConsentPreventsReadingOldSnapshots() {
        var consent = true
        val store = BoundedMemoryDiagnosticsStore({ consent })
        assertTrue(store.tryEmit(ptt()))
        consent = false
        assertNull(store.summaryIfAuthorized())
        assertNull(store.snapshotSanitizedForLocalProjection())
        assertFalse(store.tryEmit(ptt()))
    }

    @Test fun neverPersistOrConstructSessionIdentifiers() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        store.tryEmit(ptt())
        val summary = store.summaryIfAuthorized()!!
        assertEquals(1L, summary.countsByFamilySinceClear["voice_button_pressed"])
        assertTrue(store.snapshotSanitizedForLocalProjection()!!
            .none { it.encoded.contains("session_id") || it.encoded.contains("device_id") })
    }

    @Test fun rejectedNinthInternalDiagnosticNeverGetsIntoLocalStore() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        val rejected = schema.sanitize(TelemetryEvent.VoiceIntentOutcome(
            intentType = VoiceDiagnosticIntent.PLAY,
            confidenceBucket = null,
            latencyBucket = null,
            resultCode = VoiceDiagnosticResult.EXECUTED
        ))
        assertTrue(rejected is AppResult.Failure)
        assertEquals(0, store.summaryIfAuthorized()!!.retainedEvents)
    }

    @Test fun maliciousExtraFieldAndDelimiterCannotPassDefenseInDepth() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        val forged = SanitizedTelemetryEvent(
            "v1|voice_button_pressed|playback_state=Paused|file_path=personal"
        )
        assertFalse(store.tryEmit(forged))
        assertEquals(0L, store.summaryIfAuthorized()!!.acceptedSinceClear)
    }

    @Test fun arbitraryNinthFamilyNeverPassesLocalStore() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|voice_intent_outcome|intent_type=PLAY"
        )))
        assertTrue(store.summaryIfAuthorized()!!.countsByFamilySinceClear.isEmpty())
    }

    @Test fun forgedCanonicalShapeWithForbiddenSemanticValueIsDropped() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|voice_button_pressed|playback_state=SecretSongName"
        )))
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|player_error|error_code=private_file_path|route_type=Unknown"
        )))
        assertEquals(0, store.summaryIfAuthorized()!!.retainedEvents)
    }

    @Test fun allowedFieldNameCannotSmuggleArbitraryIntentValue() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|manual_correction|previous_intent=PersonalArtist|correction_type=manual_pause"
        )))
        assertEquals(0L, store.summaryIfAuthorized()!!.acceptedSinceClear)
    }

    @Test fun malformedPayloadExceedingMaxLengthIsDroppedNotLogged() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|intent_failed|intent_type=" + "A".repeat(900)
        )))
        assertEquals(0, store.summaryIfAuthorized()!!.retainedEvents)
    }

    @Test fun emptyValueOrReorderedFieldsIsNotAccepted() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|driving_mode_started|bt_connected=true|entry_source=home"
        )))
        assertFalse(store.tryEmit(SanitizedTelemetryEvent(
            "v1|driving_mode_started|entry_source=|bt_connected=true"
        )))
    }

    @Test fun processLocalMemoryDoesNotCreateSerializableExportOrUserIdentity() {
        val store = BoundedMemoryDiagnosticsStore({ true })
        assertTrue(store.tryEmit(ptt()))
        val summary = store.summaryIfAuthorized()!!
        assertEquals(1, summary.retainedEvents)
        assertFalse(summary.toString().contains("sessionId"))
        assertFalse(summary.toString().contains("mediaRef"))
        assertFalse(summary.toString().contains("timestamp"))
    }
}
