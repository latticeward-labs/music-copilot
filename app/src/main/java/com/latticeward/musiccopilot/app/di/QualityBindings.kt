package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate

/** No analytics, crash or remote-config provider is installed in either build. */
internal object QualityBindings {
    fun privacyGate(): TelemetryPrivacyGate = AllowlistedTelemetryPrivacyGate()
    fun sink(): TelemetrySink = NoOpTelemetrySink()
    fun publisher(gate: TelemetryPrivacyGate, sink: TelemetrySink) =
        BestEffortQualityEventPublisher(gate, sink)
}
