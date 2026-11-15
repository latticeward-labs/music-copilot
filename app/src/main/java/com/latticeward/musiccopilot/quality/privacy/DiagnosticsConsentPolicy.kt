package com.latticeward.musiccopilot.quality.privacy

import com.latticeward.musiccopilot.data.api.DiagnosticsConsent
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository

/**
 * P04 policy decisions are separate from Android RECORD_AUDIO permission
 * and from CloudSpeechConsent. A stored "Granted" is NEVER sufficient alone.
 *
 * No approved diagnostic privacy copy, retention duration, non-NoOp collector,
 * legal approval or remote processor exists as of 2026-11-15. All production
 * approvals remain false until Product/Privacy/Legal review.
 */
data class DiagnosticsCollectionApprovals(
    val privacyCopyApproved: Boolean = false,
    val retentionApproved: Boolean = false,
    val collectionAdapterApproved: Boolean = false,
    val legalApproved: Boolean = false,
)

/** Never persists app data or sends diagnostics; a pure policy result. */
object DiagnosticsConsentPolicy {
    fun canCollect(
        consent: DiagnosticsConsent,
        preferencesReady: Boolean,
        approvals: DiagnosticsCollectionApprovals,
        revocationPending: Boolean,
    ): Boolean =
        !revocationPending &&
            preferencesReady &&
            consent == DiagnosticsConsent.Granted &&
            approvals.privacyCopyApproved &&
            approvals.retentionApproved &&
            approvals.collectionAdapterApproved &&
            approvals.legalApproved
}

/**
 * One app-scoped denial-first authorization; always reads current StateFlow
 * (not a captured historical permission). Synchronous latch makes the
 * future revoke action deny immediately, before asynchronous DataStore edit.
 *
 * If a persistence write fails, remain denied in this process. P12 must
 * surface a failure and require remediation, not report successful deletion.
 * Production approval flags intentionally default to false.
 */
class DiagnosticsCollectionAuthorization(
    private val repository: PreferencesRepository,
    private val approvals: DiagnosticsCollectionApprovals = DiagnosticsCollectionApprovals(),
) {
    @Volatile private var revocationPending: Boolean = false

    fun requestImmediateRevocation() {
        revocationPending = true
    }

    fun canCollect(): Boolean = DiagnosticsConsentPolicy.canCollect(
        consent = repository.preferences.value.diagnosticsConsent,
        preferencesReady = repository.loadState.value == PreferencesLoadState.Ready,
        approvals = approvals,
        revocationPending = revocationPending,
    )
}
