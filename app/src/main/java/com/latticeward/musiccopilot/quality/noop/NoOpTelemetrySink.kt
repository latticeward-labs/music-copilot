package com.latticeward.musiccopilot.quality.noop

import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetrySink

/** Credential-free, offline and non-blocking. Always drops without logging. */
class NoOpTelemetrySink : TelemetrySink {
    override fun tryEmit(event: SanitizedTelemetryEvent): Boolean = false
}

