package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.*
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.OutboundTelemetrySchemaV1
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #12 P03 — SOURCE-AUTHORED TEST CONTRACT ONLY.
 * DO NOT RUN until authorized final QA; no JVM, Gradle, CI, or device evidence.
 */
class Issue12Phase3TelemetrySchemaTest {
    private val gate = AllowlistedTelemetryPrivacyGate()

    private fun sanitized(event: TelemetryEvent): String {
        val result = gate.sanitize(event)
        assertTrue("Expected approved outbound event", result is AppResult.Success<*>)
        return (result as AppResult.Success<SanitizedTelemetryEvent>).value.encoded
    }

    @Test fun exactlyEightExternalFamiliesAndFieldKeysAreFrozen() {
        assertEquals("v1", OutboundTelemetrySchemaV1.VERSION)
        assertEquals(
            linkedSetOf("driving_mode_started", "voice_button_pressed", "intent_detected",
                "intent_failed", "command_executed", "manual_correction",
                "player_error", "session_completed"),
            OutboundTelemetrySchemaV1.fieldKeys.keys
        )
        assertEquals(listOf("entry_source", "bt_connected"),
            OutboundTelemetrySchemaV1.fieldKeys["driving_mode_started"])
        assertEquals(listOf("playback_state"),
            OutboundTelemetrySchemaV1.fieldKeys["voice_button_pressed"])
        assertEquals(listOf("intent_type", "failure_code", "confidence_bucket", "latency_bucket"),
            OutboundTelemetrySchemaV1.fieldKeys["intent_failed"])
    }

    @Test fun allEightApprovedEventTypesEncodeInStableWireFormat() {
        val encoded = listOf(
            sanitized(TelemetryEvent.DrivingModeStarted("home", false)),
            sanitized(TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused)),
            sanitized(TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)),
            sanitized(TelemetryEvent.IntentFailed(null, "VOICE_NO_MATCH", null, null)),
            sanitized(TelemetryEvent.CommandExecuted("PAUSE", LatencyBucket.Fast)),
            sanitized(TelemetryEvent.ManualCorrection("PLAY", "manual_pause")),
            sanitized(TelemetryEvent.PlayerError("PLAYBACK_FAILED", RouteType.Unknown)),
            sanitized(TelemetryEvent.SessionCompleted("under_1m", 0))
        )
        assertEquals(8, encoded.size)
        assertEquals(8, encoded.map { it.split("|")[1] }.toSet().size)
        assertEquals("v1|intent_failed|intent_type=none|failure_code=VOICE_NO_MATCH|" +
            "confidence_bucket=none|latency_bucket=none", encoded[3])
        assertTrue(encoded.all { it.startsWith("v1|") })
    }

    @Test fun ninthIssue11VoiceOutcomeIsNotExternallyApproved() {
        for (result in VoiceDiagnosticResult.values()) {
            val correction = if (result == VoiceDiagnosticResult.MANUAL_CORRECTION)
                VoiceManualCorrectionCategory.MANUAL_SEARCH else null
            val internal = TelemetryEvent.VoiceIntentOutcome(
                VoiceDiagnosticIntent.PLAY_ARTIST, null, null, result, correction)
            assertTrue(gate.sanitize(internal) is AppResult.Failure)
        }
        assertNull(OutboundTelemetrySchemaV1.fieldKeys["voice_intent_outcome"])
    }

    @Test fun futureSinkCannotReceiveAnInternalNinthEvent() {
        var delivered = 0
        val sink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                delivered++
                return true
            }
        }
        val publisher = BestEffortQualityEventPublisher(gate, sink)
        assertFalse(publisher.tryRecord(TelemetryEvent.VoiceIntentOutcome(
            VoiceDiagnosticIntent.PLAY_ARTIST, null, null, VoiceDiagnosticResult.NOT_FOUND)))
        assertEquals(0, delivered)
        assertTrue(publisher.tryRecord(TelemetryEvent.CommandExecuted("PLAY_ARTIST",
            LatencyBucket.Expected)))
        assertEquals(1, delivered)
    }

    @Test fun noOneCanInjectUnexpectedFieldsOrAReorderedFieldSet() {
        val schema = OutboundTelemetrySchemaV1
        assertNull(schema.encode("driving_mode_started",
            arrayOf("entry_source" to "home", "bt_connected" to "true",
                "transcript" to "private")))
        assertNull(schema.encode("driving_mode_started",
            arrayOf("bt_connected" to "true", "entry_source" to "home")))
        assertNull(schema.encode("voice_button_pressed",
            arrayOf("playback_state" to "Playing", "session_id" to "uuid")))
        assertNull(schema.encode("player_error",
            arrayOf("error_code" to "PLAYBACK_FAILED", "route_type" to "Unknown",
                "device_id" to "private")))
        assertNull(schema.encode("unapproved_family", emptyArray()))
    }

    @Test fun untrustedDelimitersWhitespaceAndNewlinesAreRejected() {
        val s = OutboundTelemetrySchemaV1
        for (unsafe in listOf("PLAY|transcript=x", "PLAY=identity", "private song",
            "a\nb", "content://media/1", "a\tb", "")) {
            assertNull(s.encode("intent_detected",
                arrayOf("intent_type" to unsafe, "confidence_bucket" to "High")))
            assertTrue(gate.sanitize(TelemetryEvent.IntentDetected(
                unsafe, ConfidenceBucket.High)) is AppResult.Failure)
        }
    }

    @Test fun closedCodesNeverAcceptProviderErrorStrings() {
        for (bad in listOf("PLAYBACK_FAILED|uri=private", "VOICE_NO_MATCH?text=x",
            "/storage/emulated/0/Music/song.mp3", "user-secret", "token=a")) {
            assertTrue(gate.sanitize(TelemetryEvent.PlayerError(bad, RouteType.Unknown))
                is AppResult.Failure)
            assertTrue(gate.sanitize(TelemetryEvent.IntentFailed("PLAY", bad, null, null))
                is AppResult.Failure)
        }
    }

    @Test fun countAndDurationAreCoarseAndBounded() {
        assertTrue(gate.sanitize(TelemetryEvent.SessionCompleted("2m", 1))
            is AppResult.Failure)
        assertTrue(gate.sanitize(TelemetryEvent.SessionCompleted("under_1m", -1))
            is AppResult.Failure)
        assertTrue(gate.sanitize(TelemetryEvent.SessionCompleted("under_1m", 1001))
            is AppResult.Failure)
        assertTrue(sanitized(TelemetryEvent.SessionCompleted("over_15m", 1000))
            .endsWith("commands_count=1000"))
    }

    @Test fun optionalV1SessionIdIsIntentionallyAbsentPendingPrivacyReview() {
        val voice = sanitized(TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Playing))
        assertEquals("v1|voice_button_pressed|playback_state=Playing", voice)
        assertFalse(voice.contains("session_id"))
    }

    @Test fun valueLengthAndWireLengthAreBounded() {
        val s = OutboundTelemetrySchemaV1
        assertNull(s.encode("intent_detected", arrayOf(
            "intent_type" to "a".repeat(65), "confidence_bucket" to "High")))
        assertNotNull(s.encode("intent_detected", arrayOf(
            "intent_type" to "PLAY", "confidence_bucket" to "High")))
        assertEquals(512, s.MAX_WIRE_LENGTH)
    }
}
