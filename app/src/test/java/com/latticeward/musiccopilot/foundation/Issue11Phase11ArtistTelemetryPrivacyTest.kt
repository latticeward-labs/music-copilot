package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.LatencyBucket
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticResult
import com.latticeward.musiccopilot.quality.api.VoiceManualCorrectionCategory
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.VoiceIntentDiagnosticPolicy
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P11 -- authored privacy fixtures. NOT COMPILED, NOT RUN.
 * No device/network/analytics provider verification is implied.
 */
class Issue11Phase11ArtistTelemetryPrivacyTest {
    private val gate = AllowlistedTelemetryPrivacyGate()
    private val policy = VoiceIntentDiagnosticPolicy

    private fun event(
        code: VoiceDiagnosticResult = VoiceDiagnosticResult.NOT_FOUND,
        confidence: ConfidenceBucket? = null,
        latency: LatencyBucket? = null,
        correction: VoiceManualCorrectionCategory? = null,
    ) = TelemetryEvent.VoiceIntentOutcome(
        VoiceDiagnosticIntent.PLAY_ARTIST, confidence, latency, code, correction,
    )

    private fun safe(event: TelemetryEvent): String {
        val outcome = gate.sanitize(event)
        assertTrue(outcome is AppResult.Success)
        return (outcome as AppResult.Success<SanitizedTelemetryEvent>).value.encoded
    }

    private fun source(relative: String): String =
        File("src/main/java/com/latticeward/musiccopilot", relative).readText()

    @Test fun approvedIntentsAreExactlyTheSixAdrIntents() {
        assertEquals(setOf("PLAY", "PAUSE", "NEXT", "PREVIOUS", "SEEK_BACK", "PLAY_ARTIST"),
            VoiceDiagnosticIntent.values().map { it.name }.toSet())
    }

    @Test fun resultVocabularyIsClosedWithElevenCategories() {
        assertEquals(11, VoiceDiagnosticResult.values().size)
        assertEquals("EXECUTED", VoiceDiagnosticResult.EXECUTED.name)
        assertEquals("MANUAL_CORRECTION", VoiceDiagnosticResult.MANUAL_CORRECTION.name)
    }

    @Test fun manualCorrectionVocabularyIsClosedWithFiveCategories() {
        assertEquals(5, VoiceManualCorrectionCategory.values().size)
    }

    @Test fun nullConfidenceRemainsUnknownRatherThanSyntheticHigh() {
        assertNull(policy.confidence(null))
    }

    @Test fun invalidConfidenceValuesAreRejected() {
        for (x in listOf(Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            -0.001, 1.001)) assertNull(policy.confidence(x))
    }

    @Test fun confidenceBucketsUseExplicitBoundaryValues() {
        assertEquals(ConfidenceBucket.Low, policy.confidence(0.0))
        assertEquals(ConfidenceBucket.Low, policy.confidence(0.5999))
        assertEquals(ConfidenceBucket.Medium, policy.confidence(0.6))
        assertEquals(ConfidenceBucket.Medium, policy.confidence(0.8999))
        assertEquals(ConfidenceBucket.High, policy.confidence(0.9))
        assertEquals(ConfidenceBucket.High, policy.confidence(1.0))
    }

    @Test fun missingAndNegativeElapsedTimeAreUnknown() {
        assertNull(policy.latency(null))
        assertNull(policy.latency(-1L))
        assertNull(policy.latency(Long.MIN_VALUE))
    }

    @Test fun latencyIsOnlyFastExpectedOrSlow() {
        assertEquals(LatencyBucket.Fast, policy.latency(1000L))
        assertEquals(LatencyBucket.Expected, policy.latency(1001L))
        assertEquals(LatencyBucket.Expected, policy.latency(2500L))
        assertEquals(LatencyBucket.Slow, policy.latency(2501L))
        assertEquals(LatencyBucket.Slow, policy.latency(Long.MAX_VALUE))
    }

    @Test fun diagnosticPolicyDeclaresItsVersion() {
        assertEquals(1, policy.SCHEMA_VERSION)
    }

    @Test fun p09SixStableArtistErrorCodesMapToClosedResults() {
        val cases = mapOf(
            AppErrorCodes.VOICE_ARTIST_NOT_FOUND to VoiceDiagnosticResult.NOT_FOUND,
            AppErrorCodes.VOICE_ARTIST_UNSUPPORTED to VoiceDiagnosticResult.UNSUPPORTED,
            AppErrorCodes.VOICE_ARTIST_DECLINED to VoiceDiagnosticResult.DECLINED,
            AppErrorCodes.VOICE_ARTIST_STALE to VoiceDiagnosticResult.STALE,
            AppErrorCodes.VOICE_ARTIST_UNAVAILABLE to VoiceDiagnosticResult.UNAVAILABLE,
            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED to VoiceDiagnosticResult.PLAYBACK_FAILED,
        )
        for ((code, result) in cases) assertEquals(result, policy.fromArtistError(code))
    }

    @Test fun noMatchAndTimeoutMapToSafeResults() {
        assertEquals(VoiceDiagnosticResult.NO_MATCH,
            policy.fromArtistError(AppErrorCodes.VOICE_NO_MATCH))
        assertEquals(VoiceDiagnosticResult.TIMEOUT,
            policy.fromArtistError(AppErrorCodes.VOICE_TIMEOUT))
    }

    @Test fun privateProviderErrorMessagesCannotBecomeResultCodes() {
        for (bad in listOf("secret.wav", "private artist", "audio:1337",
            "content://media/external/audio/1337",
            "/storage/emulated/0/Music/private.mp3",
            "VOICE_ARTIST_NOT_FOUND|token=private",
            "VOICE_ARTIST_NOT_FOUND\ntranscript=secret",
            "STACKTRACE private song")) {
            assertNull(policy.fromArtistError(bad))
        }
    }

    @Test fun encodedOutcomeContainsExactlyTheApprovedFiveFields() {
        val encoded = safe(event(confidence = ConfidenceBucket.High,
            latency = LatencyBucket.Slow))
        assertEquals("v1|voice_intent_outcome|intent_type=PLAY_ARTIST|" +
            "confidence_bucket=High|latency_bucket=Slow|result_code=NOT_FOUND|" +
            "correction_category=none", encoded)
        assertEquals(7, encoded.split("|").size)
    }

    @Test fun unknownProviderConfidenceEncodesAsNone() {
        assertTrue(safe(event()).contains("|confidence_bucket=none|"))
    }

    @Test fun unknownLatencyEncodesAsNone() {
        assertTrue(safe(event()).contains("|latency_bucket=none|"))
    }

    @Test fun eachResultRemainsAConstantSafeCategory() {
        for (result in VoiceDiagnosticResult.values()) {
            val outcome = event(result,
                correction = if (result == VoiceDiagnosticResult.MANUAL_CORRECTION)
                    VoiceManualCorrectionCategory.MANUAL_SELECT else null)
            assertTrue(safe(outcome).contains("|result_code=" + result.name + "|"))
        }
    }

    @Test fun manualCorrectionRequiresObservedTypedAction() {
        val corrected = policy.observedManualCorrection(
            VoiceDiagnosticIntent.PLAY_ARTIST,
            VoiceManualCorrectionCategory.MANUAL_SEARCH,
        )
        assertEquals(VoiceDiagnosticResult.MANUAL_CORRECTION, corrected.resultCode)
        assertTrue(safe(corrected).endsWith("|correction_category=MANUAL_SEARCH"))
    }

    @Test fun nonCorrectionCannotHideACorrectionCategory() {
        try {
            event(VoiceDiagnosticResult.NOT_FOUND,
                correction = VoiceManualCorrectionCategory.MANUAL_SELECT)
            fail("Unobserved correction must not be permitted")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun correctionResultWithoutCategoryIsRejected() {
        try {
            event(VoiceDiagnosticResult.MANUAL_CORRECTION)
            fail("Missing observed action must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun diagnosticsToStringDoesNotLeakEvenSafeFields() {
        assertEquals("VoiceIntentOutcome(redacted)", event().toString())
    }

    @Test fun typedEventHasNoFreeFormTranscriptOrArtistFields() {
        val code = source("quality/api/TelemetryEvent.kt")
        for (unsafe in listOf("transcript:", "artistName:", "mediaRef:",
            "candidateList:", "token:", "sessionId:", "rawAudio:"))
            assertFalse(code.contains(unsafe))
    }

    @Test fun p09ArtistFailuresAreStrictlyAllowlistedInLegacyFailureEvent() {
        for (code in listOf(
            AppErrorCodes.VOICE_ARTIST_NOT_FOUND,
            AppErrorCodes.VOICE_ARTIST_UNSUPPORTED,
            AppErrorCodes.VOICE_ARTIST_DECLINED,
            AppErrorCodes.VOICE_ARTIST_STALE,
            AppErrorCodes.VOICE_ARTIST_UNAVAILABLE,
            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED,
        )) {
            assertTrue(safe(TelemetryEvent.IntentFailed("PLAY_ARTIST",
                code, null, null)).contains("|failure_code=" + code + "|"))
        }
    }

    @Test fun appendedArtistNameOrUriIsRejectedByLegacyGate() {
        assertTrue(gate.sanitize(TelemetryEvent.IntentFailed("PLAY_ARTIST",
            AppErrorCodes.VOICE_ARTIST_NOT_FOUND + "|artist=private",
            null, null)) is AppResult.Failure)
        assertTrue(gate.sanitize(TelemetryEvent.IntentDetected(
            "PLAY_ARTIST|media_uri=private", ConfidenceBucket.High,
        )) is AppResult.Failure)
    }

    @Test fun rawRecognitionBytesAreNotAValidFailureCode() {
        assertTrue(gate.sanitize(TelemetryEvent.IntentFailed(
            "PLAY_ARTIST", "audio:" + "ff00".repeat(50),
            null, null)) is AppResult.Failure)
    }

    @Test fun noOpSinkDropsAllTypedResultsWithoutPersisting() {
        val sanitized = gate.sanitize(event(VoiceDiagnosticResult.EXECUTED))
        val approved = (sanitized as AppResult.Success).value
        repeat(5) { assertFalse(NoOpTelemetrySink().tryEmit(approved)) }
    }

    @Test fun throwingSinkCannotStopBestEffortPublisher() {
        val throwing = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                throw IllegalStateException("provider stack trace/private song")
            }
        }
        assertFalse(BestEffortQualityEventPublisher(gate, throwing).tryRecord(
            event(VoiceDiagnosticResult.EXECUTED)))
    }

    @Test fun throwingGateCannotReachSink() {
        val throwing = object : TelemetryPrivacyGate {
            override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> {
                throw IllegalStateException("private transcript")
            }
        }
        var sent = 0
        val sink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                sent++
                return true
            }
        }
        assertFalse(BestEffortQualityEventPublisher(throwing, sink).tryRecord(event()))
        assertEquals(0, sent)
    }

    @Test fun coordinatorClaimsArtistAttemptBeforeTryingToEmitOutcome() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("markArtistAttempt(id, generation, number,"))
        assertTrue(code.contains("artistIntentObserved = true"))
        assertTrue(code.contains("diagnosticClaimed = true"))
    }

    @Test fun executedOutcomeIsSourceGatedByMedia3Acknowledgement() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("if (sent.value == PlayArtistDecision.Executed)"))
        assertTrue(code.contains("if (ack.value == PlayArtistDecision.Executed)"))
        assertTrue(code.contains("recordArtistOutcome(id, generation, number,"))
        assertTrue(code.contains("VoiceDiagnosticResult.EXECUTED"))
    }

    @Test fun rawArtistMetadataIsNeverSentToTelemetryCallback() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("tryRecordVoiceOutcome: (TelemetryEvent.VoiceIntentOutcome) -> Unit"))
        assertTrue(code.contains("VoiceDiagnosticIntent.PLAY_ARTIST"))
        assertFalse(code.contains("tryRecordVoiceOutcome(recognition"))
        assertFalse(code.contains("tryRecordVoiceOutcome(snapshot"))
    }

    @Test fun diagnosticsAreAsynchronousAndFailuresAreContained() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("ownerScope.launch(Dispatchers.Default)"))
        assertTrue(code.contains("try { tryRecordVoiceOutcome(event) } catch (_: Exception)"))
    }

    @Test fun applicationStillUsesExistingGateAndNoOpSinkOnly() {
        val app = source("app/di/ApplicationBindings.kt")
        val quality = source("app/di/QualityBindings.kt")
        assertTrue(app.contains("qualityEventPublisher.tryRecord(event)"))
        assertTrue(quality.contains("NoOpTelemetrySink()"))
        assertFalse(quality.contains("Firebase"))
        assertFalse(quality.contains("Sentry"))
    }

    @Test fun manualCorrectionIsNotInferredFromDecliningArtist() {
        assertFalse(source("voice/session/DefaultVoiceSessionCoordinator.kt")
            .contains("VoiceDiagnosticResult.MANUAL_CORRECTION"))
    }

    @Test fun noInternetOrAndroidBackupIsEnabled() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
    }
}
