package com.latticeward.musiccopilot.quality.privacy

import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.LatencyBucket
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticResult
import com.latticeward.musiccopilot.quality.api.VoiceManualCorrectionCategory

/**
 * Issue #11 P11: pure, local-only transformation into non-identifying buckets.
 *
 * Thresholds are versioned ENGINEERING CHOICES pending P13 review, not numeric
 * constants claimed to be prescribed by ADR-0005. No remote configuration.
 * The actual provider score and elapsed milliseconds never enter telemetry.
 */
object VoiceIntentDiagnosticPolicy {
    const val SCHEMA_VERSION = 1
    const val FAST_LATENCY_MAX_MS = 1_000L
    const val EXPECTED_LATENCY_MAX_MS = 2_500L
    const val LOW_CONFIDENCE_BELOW = 0.60
    const val HIGH_CONFIDENCE_AT_LEAST = 0.90

    fun confidence(providerConfidence: Double?): ConfidenceBucket? = when {
        providerConfidence == null || !providerConfidence.isFinite() ||
            providerConfidence < 0.0 || providerConfidence > 1.0 -> null
        providerConfidence < LOW_CONFIDENCE_BELOW -> ConfidenceBucket.Low
        providerConfidence < HIGH_CONFIDENCE_AT_LEAST -> ConfidenceBucket.Medium
        else -> ConfidenceBucket.High
    }

    fun latency(elapsedMillis: Long?): LatencyBucket? = when {
        elapsedMillis == null || elapsedMillis < 0L -> null
        elapsedMillis <= FAST_LATENCY_MAX_MS -> LatencyBucket.Fast
        elapsedMillis <= EXPECTED_LATENCY_MAX_MS -> LatencyBucket.Expected
        else -> LatencyBucket.Slow
    }

    /**
     * Translate ONLY established, sanitized AppErrorCodes. A provider-supplied
     * exception message or arbitrary code maps to no event, never to a
     * fabricated stable failure code or an identifier-bearing string.
     */
    fun fromArtistError(errorCode: String): VoiceDiagnosticResult? = when (errorCode) {
        AppErrorCodes.VOICE_ARTIST_NOT_FOUND -> VoiceDiagnosticResult.NOT_FOUND
        AppErrorCodes.VOICE_ARTIST_UNSUPPORTED -> VoiceDiagnosticResult.UNSUPPORTED
        AppErrorCodes.VOICE_ARTIST_DECLINED -> VoiceDiagnosticResult.DECLINED
        AppErrorCodes.VOICE_ARTIST_STALE -> VoiceDiagnosticResult.STALE
        AppErrorCodes.VOICE_ARTIST_UNAVAILABLE -> VoiceDiagnosticResult.UNAVAILABLE
        AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED -> VoiceDiagnosticResult.PLAYBACK_FAILED
        AppErrorCodes.VOICE_NO_MATCH -> VoiceDiagnosticResult.NO_MATCH
        AppErrorCodes.VOICE_TIMEOUT -> VoiceDiagnosticResult.TIMEOUT
        else -> null
    }

    /** No inference from a negative artist response: only a real UI action. */
    fun observedManualCorrection(
        intent: VoiceDiagnosticIntent,
        category: VoiceManualCorrectionCategory,
    ): TelemetryEvent.VoiceIntentOutcome = TelemetryEvent.VoiceIntentOutcome(
        intent, null, null, VoiceDiagnosticResult.MANUAL_CORRECTION, category,
    )
}
