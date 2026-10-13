package com.latticeward.musiccopilot.quality.api

import com.latticeward.musiccopilot.core.result.AppResult

/** Only the privacy gate can produce this. No public free-form payload/map. */
@JvmInline
value class SanitizedTelemetryEvent internal constructor(internal val encoded: String)

interface TelemetryPrivacyGate {
    fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent>
}

interface TelemetrySink {
    /** Nonblocking best effort; false means dropped. Never throw or affect product flow. */
    fun tryEmit(event: SanitizedTelemetryEvent): Boolean
}
