package com.latticeward.musiccopilot.quality.adapters

import com.latticeward.musiccopilot.quality.api.SafeCrashCategory
import com.latticeward.musiccopilot.quality.api.SafeCrashEvidenceSink

/**
 * P07 on-device crash adapter seam. Safe enum in RAM only, no throwable
 * input is accepted. A crash SDK must never be installed or substituted
 * without explicit Product/Privacy/Legal and provider approval.
 */
class ConsentGatedCrashEvidenceReporter(
    private val authorized: () -> Boolean,
    private val sink: SafeCrashEvidenceSink,
) {
    @Volatile private var lastCategory: SafeCrashCategory? = null

    fun record(category: SafeCrashCategory): Boolean = try {
        if (!authorized()) false
        else {
            // Diagnostic evidence is not a claim about a particular session.
            lastCategory = category
            // Re-authorize immediately before provider delivery: revoke wins.
            if (!authorized()) {
                lastCategory = null
                false
            } else sink.tryRecord(category)
        }
    } catch (_: Exception) {
        // No provider exceptions, stack text or raw payload surfaced.
        false
    }

    fun peekLocal(): SafeCrashCategory? = try {
        if (authorized()) lastCategory else null
    } catch (_: Exception) { null }

    fun clear() { lastCategory = null }
}
