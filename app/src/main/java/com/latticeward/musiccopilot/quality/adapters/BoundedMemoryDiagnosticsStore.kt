package com.latticeward.musiccopilot.quality.adapters

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
    private val ring = ArrayDeque<SanitizedTelemetryEvent>()
    private val counts = LinkedHashMap<String, Long>()
    private var accepted = 0L
    private var evicted = 0L

    override fun tryEmit(event: SanitizedTelemetryEvent): Boolean = try {
        synchronized(gate) {
            if (!canCollect()) return@synchronized false
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
            if (!canCollect()) null
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
            synchronized(gate) { if (!canCollect()) null else ring.toList() }
        } catch (_: Exception) { null }

    override fun clear() {
        synchronized(gate) {
            ring.clear()
            counts.clear()
            accepted = 0L
            evicted = 0L
        }
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
        return family.takeIf {
            OutboundTelemetrySchemaV1.encode(family, attributes.toTypedArray()) == source
        }
    }

    private fun safeIncrement(count: Long): Long =
        if (count == Long.MAX_VALUE) count else count + 1

    companion object {
        const val DEFAULT_MAX_EVENTS = 128
        const val MAX_CONFIGURABLE_EVENTS = 1024
    }
}
