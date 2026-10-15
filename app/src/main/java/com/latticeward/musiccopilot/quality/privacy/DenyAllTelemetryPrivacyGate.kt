package com.latticeward.musiccopilot.quality.privacy

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate

/** Phase 5 safe default. All events drop until the allowlist sanitizer exists (Phase 9). */
class DenyAllTelemetryPrivacyGate : TelemetryPrivacyGate {
    override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> =
        AppResult.Failure(AppError.Quality(AppErrorCodes.QUALITY_DROPPED))
}
