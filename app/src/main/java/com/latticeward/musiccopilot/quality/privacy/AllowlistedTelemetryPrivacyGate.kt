package com.latticeward.musiccopilot.quality.privacy

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate

/**
 * Pure, deny-by-default diagnostic boundary. All output values derive from
 * closed Kotlin enums, stable code sets, booleans or explicitly bounded counts.
 * This class never echoes rejected values, throws a provider exception or logs.
 * Output is a private schema wire code, NOT an arbitrary JSON or dynamic map.
 */
class AllowlistedTelemetryPrivacyGate : TelemetryPrivacyGate {
    override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> {
        val safe = when (event) {
            is TelemetryEvent.DrivingModeStarted ->
                if (event.entrySource in ENTRY_SOURCES)
                    wire("driving_mode_started",
                        "entry_source" to event.entrySource,
                        "bt_connected" to event.bluetoothConnected.toString())
                else null

            is TelemetryEvent.VoiceButtonPressed ->
                wire("voice_button_pressed",
                    "playback_state" to event.playbackState.name)

            is TelemetryEvent.IntentDetected ->
                if (event.intentType in INTENTS)
                    wire("intent_detected",
                        "intent_type" to event.intentType,
                        "confidence_bucket" to event.confidenceBucket.name)
                else null

            is TelemetryEvent.IntentFailed ->
                if ((event.intentType == null || event.intentType in INTENTS) &&
                    event.failureCode in FAILURE_CODES)
                    wire("intent_failed",
                        "intent_type" to (event.intentType ?: "none"),
                        "failure_code" to event.failureCode,
                        "confidence_bucket" to (event.confidenceBucket?.name ?: "none"),
                        "latency_bucket" to (event.latencyBucket?.name ?: "none"))
                else null

            is TelemetryEvent.CommandExecuted ->
                if (event.intentType in INTENTS)
                    wire("command_executed",
                        "intent_type" to event.intentType,
                        "latency_bucket" to event.latencyBucket.name)
                else null

            is TelemetryEvent.ManualCorrection ->
                if (event.previousIntent in INTENTS && event.correctionType in CORRECTIONS)
                    wire("manual_correction",
                        "previous_intent" to event.previousIntent,
                        "correction_type" to event.correctionType)
                else null

            is TelemetryEvent.PlayerError ->
                if (event.errorCode in PLAYER_CODES)
                    wire("player_error",
                        "error_code" to event.errorCode,
                        "route_type" to event.routeType.name)
                else null

            is TelemetryEvent.SessionCompleted ->
                if (event.durationBucket in DURATION_BUCKETS &&
                    event.commandsCount in 0..MAX_COMMAND_COUNT)
                    wire("session_completed",
                        "duration_bucket" to event.durationBucket,
                        "commands_count" to event.commandsCount.toString())
                else null
        }
        return if (safe != null) AppResult.Success(SanitizedTelemetryEvent(safe))
        else AppResult.Failure(AppError.Quality(AppErrorCodes.QUALITY_DROPPED))
    }

    private fun wire(event: String, vararg attributes: Pair<String, String>): String =
        buildString {
            append("v1|").append(event)
            attributes.forEach { (key, value) -> append('|').append(key).append('=').append(value) }
        }

    companion object {
        /** Subject to later product/privacy review, not remote-configurable. */
        const val MAX_COMMAND_COUNT = 1000

        private val ENTRY_SOURCES = setOf("home", "player", "notification", "settings")
        private val INTENTS = setOf(
            "PLAY", "PAUSE", "NEXT", "PREVIOUS", "SEEK_BACK", "PLAY_ARTIST"
        )
        private val CORRECTIONS = setOf(
            "manual_play", "manual_pause", "manual_skip", "manual_search", "manual_select"
        )
        private val DURATION_BUCKETS = setOf(
            "under_1m", "1_to_5m", "5_to_15m", "over_15m"
        )
        // Explicit stable non-identifying codes. Unknown provider/exception
        // messages never pass merely because they contain the word "error".
        private val FAILURE_CODES = setOf(
            AppErrorCodes.VOICE_UNAVAILABLE, AppErrorCodes.VOICE_TIMEOUT,
            AppErrorCodes.VOICE_NO_MATCH, AppErrorCodes.VOICE_SECOND_ATTEMPT_FAILED,
            AppErrorCodes.VOICE_STALE_SESSION,
            AppErrorCodes.PLAYBACK_BUSY, AppErrorCodes.PLAYBACK_REJECTED,
            AppErrorCodes.PLAYBACK_FAILED,
            AppErrorCodes.CONTROLLER_DISCONNECTED,
            AppErrorCodes.CONTROLLER_CONNECT_FAILED,
            AppErrorCodes.CONTROLLER_TIMEOUT,
            AppErrorCodes.MEDIA_ITEM_MISSING, AppErrorCodes.MEDIA_ITEM_CORRUPT,
            AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            AppErrorCodes.PERMISSION_DENIED, AppErrorCodes.PERMISSION_REVOKED,
            AppErrorCodes.CONSENT_REQUIRED, AppErrorCodes.FOCUS_NOT_RECOVERED,
            AppErrorCodes.ROUTE_INVALIDATED, AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED
        )
        private val PLAYER_CODES = setOf(
            AppErrorCodes.PLAYBACK_BUSY, AppErrorCodes.PLAYBACK_REJECTED,
            AppErrorCodes.PLAYBACK_FAILED,
            AppErrorCodes.CONTROLLER_DISCONNECTED,
            AppErrorCodes.CONTROLLER_CONNECT_FAILED,
            AppErrorCodes.CONTROLLER_TIMEOUT,
            AppErrorCodes.MEDIA_ITEM_MISSING, AppErrorCodes.MEDIA_ITEM_CORRUPT,
            AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            AppErrorCodes.FOCUS_NOT_RECOVERED,
            AppErrorCodes.ROUTE_INVALIDATED,
            AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED
        )
    }
}
