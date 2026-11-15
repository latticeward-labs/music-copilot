package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.ConsentGatedTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.DiagnosticsCollectionAuthorization
import com.latticeward.musiccopilot.data.api.PreferencesRepository

/** No analytics/crash/remote provider is installed in either build variant. */
internal object QualityBindings {
    fun privacyGate(): TelemetryPrivacyGate = AllowlistedTelemetryPrivacyGate()

    // Default approvals are false until reviewed disclosure, retention,
    // collection implementation, and Legal/Product/Privacy sign-off exist.
    fun authorization(repository: PreferencesRepository) =
        DiagnosticsCollectionAuthorization(repository)

    fun sink(authorization: DiagnosticsCollectionAuthorization): TelemetrySink =
        ConsentGatedTelemetrySink(NoOpTelemetrySink(), authorization::canCollect)

    fun publisher(
        gate: TelemetryPrivacyGate,
        sink: TelemetrySink,
        authorization: DiagnosticsCollectionAuthorization,
    ) = BestEffortQualityEventPublisher(gate, sink, authorization::canCollect)
}
