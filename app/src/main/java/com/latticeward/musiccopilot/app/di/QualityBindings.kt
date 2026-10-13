package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.DenyAllTelemetryPrivacyGate

/** Quality is optional best effort; Phase 9 owns the validated allowlist implementation. */
internal object QualityBindings {
    fun privacyGate(): TelemetryPrivacyGate = DenyAllTelemetryPrivacyGate()
    fun sink(): TelemetrySink = NoOpTelemetrySink()
}

