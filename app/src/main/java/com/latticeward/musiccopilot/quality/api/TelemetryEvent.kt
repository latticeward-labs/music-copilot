package com.latticeward.musiccopilot.quality.api

/** All String attributes are restricted to stable allowlisted codes in Phase 9 gate. */
enum class ConfidenceBucket { Low, Medium, High }
enum class LatencyBucket { Fast, Expected, Slow }
enum class RouteType { BuiltIn, Wired, Bluetooth, Unknown }
enum class TelemetryPlaybackState { Idle, Buffering, Playing, Paused, Ended, Error }

enum class VoiceDiagnosticIntent { PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST }

/**
 * Issue #11 P11: closed result vocabulary. Neither raw STT text nor
 * artist/local-media identifiers may appear in a diagnostic event.
 */
enum class VoiceDiagnosticResult {
    EXECUTED, NOT_FOUND, UNSUPPORTED, DECLINED, STALE, UNAVAILABLE,
    PLAYBACK_FAILED, NO_MATCH, TIMEOUT, CANCELED, MANUAL_CORRECTION
}
enum class VoiceManualCorrectionCategory {
    MANUAL_PLAY, MANUAL_PAUSE, MANUAL_SKIP, MANUAL_SEARCH, MANUAL_SELECT
}


sealed interface TelemetryEvent {
    data class DrivingModeStarted(
        val entrySource: String,
        val bluetoothConnected: Boolean,
    ) : TelemetryEvent
    data class VoiceButtonPressed(
        val playbackState: TelemetryPlaybackState,
    ) : TelemetryEvent
    data class IntentDetected(
        val intentType: String,
        val confidenceBucket: ConfidenceBucket,
    ) : TelemetryEvent
    data class IntentFailed(
        val intentType: String?,
        val failureCode: String,
        val confidenceBucket: ConfidenceBucket?,
        val latencyBucket: LatencyBucket?,
    ) : TelemetryEvent
    data class CommandExecuted(
        val intentType: String,
        val latencyBucket: LatencyBucket,
    ) : TelemetryEvent
    data class ManualCorrection(
        val previousIntent: String,
        val correctionType: String,
    ) : TelemetryEvent
    data class PlayerError(
        val errorCode: String,
        val routeType: RouteType,
    ) : TelemetryEvent
    data class SessionCompleted(
        val durationBucket: String,
        val commandsCount: Int,
    ) : TelemetryEvent

    /**
     * Issue #11 P11 legacy INTERNAL-only outcome envelope.
     * Issue #12 P03: this ninth family is NOT approved for outbound
     * telemetry; AllowlistedTelemetryPrivacyGate deliberately rejects it.
     * Later P06 may safely project verified outcomes into the eight
     * approved families without copying personal media or transcripts.
     * All values are closed enums; manual correction is explicitly observed.
     */
    data class VoiceIntentOutcome(
        val intentType: VoiceDiagnosticIntent,
        val confidenceBucket: ConfidenceBucket?,
        val latencyBucket: LatencyBucket?,
        val resultCode: VoiceDiagnosticResult,
        val correctionCategory: VoiceManualCorrectionCategory? = null,
    ) : TelemetryEvent {
        init {
            require((resultCode == VoiceDiagnosticResult.MANUAL_CORRECTION) ==
                (correctionCategory != null)) { "Invalid diagnostic category" }
        }
        override fun toString(): String = "VoiceIntentOutcome(redacted)"
    }
}
