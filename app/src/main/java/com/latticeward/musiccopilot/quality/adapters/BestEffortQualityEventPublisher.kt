package com.latticeward.musiccopilot.quality.adapters

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink

/**
 * Best-effort adapter around the required privacy gate. No product caller
 * receives Quality exceptions; events are never queued, persisted or retried.
 * Keep this off all product critical paths: not awaited before playback/Voice.
 */
class BestEffortQualityEventPublisher(
    private val privacyGate: TelemetryPrivacyGate,
    private val sink: TelemetrySink,
) {
    fun tryRecord(event: TelemetryEvent): Boolean = try {
        when (val outcome = privacyGate.sanitize(event)) {
            is AppResult.Failure -> false
            is AppResult.Success -> try { sink.tryEmit(outcome.value) } catch (_: Exception) { false }
        }
    } catch (_: Exception) { false }
}

