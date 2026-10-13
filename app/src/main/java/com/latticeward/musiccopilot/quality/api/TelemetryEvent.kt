package com.latticeward.musiccopilot.quality.api

/** All String attributes are restricted to stable allowlisted codes in Phase 9 gate. */
enum class ConfidenceBucket { Low, Medium, High }
enum class LatencyBucket { Fast, Expected, Slow }
enum class RouteType { BuiltIn, Wired, Bluetooth, Unknown }
enum class TelemetryPlaybackState { Idle, Buffering, Playing, Paused, Ended, Error }

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
}

