package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.adapters.BoundedMemoryDiagnosticsStore
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.ConsentGatedTelemetrySink
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsCollectionAuthorization
import com.latticeward.musiccopilot.data.api.PreferencesRepository

/**
 * Issue #12 P09: provider choice is explicit and compile-time reviewed.
 * No remote SDK/provider/transport is installed. Production defaults NoOp.
 * MemoryOnly is a future opt-in review path (NOT a user-preference toggle)
 * and still requires the full P04 four-approval + user-consent gate.
 */
internal enum class LocalDiagnosticsProviderMode { NoOp, MemoryOnly }

internal object QualityBindings {
    fun privacyGate(): TelemetryPrivacyGate = AllowlistedTelemetryPrivacyGate()

    // Approval defaults FALSE. No persisted "Granted" preference can
    // activate local storage without legal, privacy, copy & retention review.
    fun authorization(repository: PreferencesRepository) =
        DiagnosticsCollectionAuthorization(repository)

    /** NoOp in BOTH debug and release until a reviewed config changes it. */
    const val PRODUCTION_LOCAL_DIAGNOSTICS_MODE =
        LocalDiagnosticsProviderMode.NoOp

    fun localStore(authorization: DiagnosticsCollectionAuthorization) =
        BoundedMemoryDiagnosticsStore(authorization::canCollect)

    fun sink(
        authorization: DiagnosticsCollectionAuthorization,
        localStore: BoundedMemoryDiagnosticsStore? = null,
        mode: LocalDiagnosticsProviderMode = PRODUCTION_LOCAL_DIAGNOSTICS_MODE,
    ): TelemetrySink {
        val selected: TelemetrySink = when (mode) {
            LocalDiagnosticsProviderMode.NoOp -> NoOpTelemetrySink()
            LocalDiagnosticsProviderMode.MemoryOnly ->
                localStore ?: NoOpTelemetrySink() // fail closed if misconfigured
        }
        // Recheck at delivery even for already sanitized queued events.
        return ConsentGatedTelemetrySink(selected, authorization::canCollect)
    }

    fun publisher(
        gate: TelemetryPrivacyGate,
        sink: TelemetrySink,
        authorization: DiagnosticsCollectionAuthorization,
    ) = BestEffortQualityEventPublisher(gate, sink, authorization::canCollect)
}
