package com.latticeward.musiccopilot.quality.privacy

import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetrySink

/**
 * P04 second authorization check at every sink delivery/flush.
 * A queued sanitized event cannot bypass revocation or a failed preference
 * read, even if it was created before the current consent decision.
 */
class ConsentGatedTelemetrySink(
    private val delegate: TelemetrySink,
    private val canCollect: () -> Boolean,
) : TelemetrySink {
    override fun tryEmit(event: SanitizedTelemetryEvent): Boolean =
        try {
            if (!canCollect()) false else delegate.tryEmit(event)
        } catch (_: Exception) {
            // Diagnostics are never allowed to affect music commands.
            false
        }
}
