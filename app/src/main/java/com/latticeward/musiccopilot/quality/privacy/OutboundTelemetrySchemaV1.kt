package com.latticeward.musiccopilot.quality.privacy

/**
 * Issue #12 P03: outbound v1.0 telemetry envelope. Field order is intentional.
 *
 * The English PRD v1.0 p.10 and approved architecture p.17 specify exactly
 * eight public event families. The MVP table p.10 lists seven rows (omits
 * intent_failed); it does not authorize a ninth event.
 *
 * This validator is only a schema boundary, NOT diagnostics consent.
 * P04 must gate capture/flush separately and P09 must approve retention/sinks.
 * A ninth Issue #11 VoiceIntentOutcome remains a private typed observation
 * which is NEVER converted into an outbound v1 event by this object.
 */
internal object OutboundTelemetrySchemaV1 {
    const val VERSION = "v1"
    const val MAX_WIRE_LENGTH = 512
    const val MAX_TOKEN_LENGTH = 64

    /** Closed event/attribute vocabulary. Keys are compared in exact order. */
    val fieldKeys: Map<String, List<String>> = linkedMapOf(
        "driving_mode_started" to listOf("entry_source", "bt_connected"),
        // MVP includes session_id as an optional contextual field, but P03
        // intentionally omits it until privacy approves a non-identifying
        // representation. No device/session/user UUID is exported.
        "voice_button_pressed" to listOf("playback_state"),
        "intent_detected" to listOf("intent_type", "confidence_bucket"),
        "intent_failed" to listOf(
            "intent_type", "failure_code", "confidence_bucket", "latency_bucket"
        ),
        "command_executed" to listOf("intent_type", "latency_bucket"),
        "manual_correction" to listOf("previous_intent", "correction_type"),
        "player_error" to listOf("error_code", "route_type"),
        "session_completed" to listOf("duration_bucket", "commands_count"),
    )

    private val safeToken = Regex("[A-Za-z0-9_]+")

    /** No free-form maps, arbitrary keys, delimiters or unapproved families. */
    fun encode(event: String, attributes: Array<out Pair<String, String>>): String? {
        val approved = fieldKeys[event] ?: return null
        if (attributes.map { it.first } != approved) return null
        if (attributes.any { (key, value) ->
                key.isEmpty() || value.isEmpty() ||
                    key.length > MAX_TOKEN_LENGTH || value.length > MAX_TOKEN_LENGTH ||
                    !safeToken.matches(key) || !safeToken.matches(value)
            }) return null
        val serialized = buildString {
            append(VERSION).append('|').append(event)
            attributes.forEach { (key, value) ->
                append('|').append(key).append('=').append(value)
            }
        }
        return serialized.takeIf { it.length <= MAX_WIRE_LENGTH }
    }
}
