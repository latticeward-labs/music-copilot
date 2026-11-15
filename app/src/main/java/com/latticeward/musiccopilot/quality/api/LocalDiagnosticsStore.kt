package com.latticeward.musiccopilot.quality.api

/**
 * Issue #12 P09: only bounded, already sanitized non-audio observations.
 *
 * A summary may be used for opt-in local review by P10/P12. It contains no
 * session/user/installation identifiers, timestamps, tracks or individual
 * event payloads. Counts are since the last user clear/process creation,
 * NOT a complete Beta cohort, seven-day window or validated KPI.
 */
data class LocalDiagnosticsSummary(
    val retainedEvents: Int,
    val acceptedSinceClear: Long,
    val evictedOldestSinceClear: Long,
    val countsByFamilySinceClear: Map<String, Long>,
    // Queued events rejected at the fixed-cap source queue are unobserved.
    // P10 must not interpret these as successful or failed user actions.
    val sourceQueueDropsSinceClear: Long = 0L,
)

/**
 * Clearing diagnostic data is independent of liking songs, user-owned media,
 * DataStore settings/consent and the service-owned player.
 */
interface LocalDiagnosticsStore : TelemetrySink {
    /** Idempotent; erases all retained individual events AND coarse counters. */
    fun clear()

    /**
     * Returns null when current diagnostics authorization is not approved.
     * No raw event envelope or provider-internal data is exposed.
     */
    fun summaryIfAuthorized(): LocalDiagnosticsSummary?
}
