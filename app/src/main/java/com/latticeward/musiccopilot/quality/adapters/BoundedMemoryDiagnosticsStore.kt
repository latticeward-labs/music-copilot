package com.latticeward.musiccopilot.quality.adapters

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.LatencyBucket
import com.latticeward.musiccopilot.quality.api.RouteType
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.LocalDiagnosticsStore
import com.latticeward.musiccopilot.quality.api.LocalDiagnosticsSummary
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.privacy.OutboundTelemetrySchemaV1

/**
 * Issue #12 P09: deliberately VOLATILE local-only diagnostic store.
 *
 * Retention decision: no final time-based retention/TTL is approved; until
 * Product/Privacy/Legal sign off, use NO DISK, NO BACKUP, NO USER IDENTIFIER
 * and NO SDK/network provider. Lifetime: process memory only, or earlier
 * explicit clear/consent revoke. A bounded count is an engineering memory
 * safety ceiling, NOT a claim that an approved retention duration exists.
 *
 * Input is exclusively SanitizedTelemetryEvent after the P03 semantic gate,
 * P04 authorization, and a P04 delivery-time authorization recheck.
 * This adapter itself also fails closed when the approval/consent changes.
 */
class BoundedMemoryDiagnosticsStore(
    private val canCollect: () -> Boolean,
    private val maxEvents: Int = DEFAULT_MAX_EVENTS,
) : LocalDiagnosticsStore {
    init { require(maxEvents in 1..MAX_CONFIGURABLE_EVENTS) }

    private val gate = Any()
    private val semanticGate = AllowlistedTelemetryPrivacyGate()
    private val ring = ArrayDeque<SanitizedTelemetryEvent>()
    private val counts = LinkedHashMap<String, Long>()
    private var accepted = 0L
    private var evicted = 0L

    override fun tryEmit(event: SanitizedTelemetryEvent): Boolean = try {
        synchronized(gate) {
            if (!authorizedOrPurgeLocked()) return@synchronized false
            // Defense in depth: even accidental direct calls cannot place
            // arbitrary event families/keys/values into this RAM buffer.
            val family = validateApprovedEnvelope(event) ?: return@synchronized false
            if (ring.size == maxEvents) {
                ring.removeFirst()
                evicted = safeIncrement(evicted)
            }
            ring.addLast(event)
            accepted = safeIncrement(accepted)
            counts[family] = safeIncrement(counts[family] ?: 0L)
            true
        }
    } catch (_: Exception) { false }

    override fun summaryIfAuthorized(): LocalDiagnosticsSummary? = try {
        synchronized(gate) {
            if (!authorizedOrPurgeLocked()) null
            else LocalDiagnosticsSummary(
                retainedEvents = ring.size,
                acceptedSinceClear = accepted,
                evictedOldestSinceClear = evicted,
                countsByFamilySinceClear = counts.toMap(),
            )
        }
    } catch (_: Exception) { null }

    /**
     * P10-only local aggregation seam. Deliberately internal: UI/export may
     * use coarse summary only; neither a diagnostic file nor a network API.
     * Historical properties older than the cap are unavailable, and P10
     * MUST report coverage loss rather than reconstructing fake records.
     */
    internal fun snapshotSanitizedForLocalProjection(): List<SanitizedTelemetryEvent>? =
        try {
            synchronized(gate) {
                if (!authorizedOrPurgeLocked()) null else ring.toList()
            }
        } catch (_: Exception) { null }

    override fun clear() {
        synchronized(gate) { clearLocked() }
    }

    private fun clearLocked() {
        ring.clear()
        counts.clear()
        accepted = 0L
        evicted = 0L
    }

    /**
     * Consent can also change via the observable PreferencesRepository, not
     * only the explicit Settings revoke callback. On ANY denied access,
     * immediately wipe cached RAM observations, not merely hide the read.
     * This is defense in depth; ApplicationBindings also scrubs on revoke.
     */
    private fun authorizedOrPurgeLocked(): Boolean {
        val approved = try { canCollect() } catch (_: Exception) { false }
        if (!approved) clearLocked()
        return approved
    }

    private fun validateApprovedEnvelope(event: SanitizedTelemetryEvent): String? {
        val source = event.encoded
        if (source.length > OutboundTelemetrySchemaV1.MAX_WIRE_LENGTH) return null
        val pieces = source.split('|')
        if (pieces.size < 3 || pieces[0] != OutboundTelemetrySchemaV1.VERSION) return null
        val family = pieces[1]
        val permittedKeys = OutboundTelemetrySchemaV1.fieldKeys[family] ?: return null
        if (pieces.size != permittedKeys.size + 2) return null
        val attributes = ArrayList<Pair<String, String>>(permittedKeys.size)
        for (piece in pieces.drop(2)) {
            val equal = piece.indexOf('=')
            if (equal <= 0 || piece.indexOf('=', equal + 1) >= 0) return null
            attributes.add(piece.substring(0, equal) to piece.substring(equal + 1))
        }
        if (OutboundTelemetrySchemaV1.encode(family, attributes.toTypedArray()) != source)
            return null

        // A SanitizedTelemetryEvent constructor is module-internal, so
        // defense in depth must also check semantic VALUE vocabulary, not
        // just valid separators and field names. Re-project through the
        // canonical P03 gate; only its exact approved canonical encoding
        // may enter local memory. Never log a malformed attempted value.
        val values = attributes.toMap()
        val rebuilt = when (family) {
            "driving_mode_started" -> {
                val bluetooth = when (values["bt_connected"]) {
                    "true" -> true
                    "false" -> false
                    else -> return null
                }
                TelemetryEvent.DrivingModeStarted(
                    values.getValue("entry_source"), bluetooth
                )
            }
            "voice_button_pressed" -> TelemetryEvent.VoiceButtonPressed(
                TelemetryPlaybackState.valueOf(values.getValue("playback_state"))
            )
            "intent_detected" -> TelemetryEvent.IntentDetected(
                values.getValue("intent_type"),
                ConfidenceBucket.valueOf(values.getValue("confidence_bucket"))
            )
            "intent_failed" -> TelemetryEvent.IntentFailed(
                values.getValue("intent_type").takeUnless { it == "none" },
                values.getValue("failure_code"),
                values.getValue("confidence_bucket").takeUnless { it == "none" }
                    ?.let(ConfidenceBucket::valueOf),
                values.getValue("latency_bucket").takeUnless { it == "none" }
                    ?.let(LatencyBucket::valueOf)
            )
            "command_executed" -> TelemetryEvent.CommandExecuted(
                values.getValue("intent_type"),
                LatencyBucket.valueOf(values.getValue("latency_bucket"))
            )
            "manual_correction" -> TelemetryEvent.ManualCorrection(
                values.getValue("previous_intent"), values.getValue("correction_type")
            )
            "player_error" -> TelemetryEvent.PlayerError(
                values.getValue("error_code"),
                RouteType.valueOf(values.getValue("route_type"))
            )
            "session_completed" -> TelemetryEvent.SessionCompleted(
                values.getValue("duration_bucket"),
                values.getValue("commands_count").toIntOrNull() ?: return null
            )
            else -> return null
        }
        return when (val verified = semanticGate.sanitize(rebuilt)) {
            is AppResult.Success ->
                family.takeIf { verified.value.encoded == source }
            is AppResult.Failure -> null
        }
    }

    private fun safeIncrement(count: Long): Long =
        if (count == Long.MAX_VALUE) count else count + 1

    companion object {
        const val DEFAULT_MAX_EVENTS = 128
        const val MAX_CONFIGURABLE_EVENTS = 1024
    }
}
