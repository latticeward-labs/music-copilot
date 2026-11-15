package com.latticeward.musiccopilot.quality.api

/**
 * P07 private, in-process, non-exportable crash evidence (NOT a ninth
 * telemetry family). No Throwable, message, stack trace, breadcrumb,
 * session/user/device ID, process name, timestamp or file reference.
 *
 * Historical OS exit reasons cannot be attributed to a specific Driving
 * session without a reviewed P08/P09 marker. UNKNOWN is not crash-free.
 */
enum class SafeCrashCategory {
    JAVA_FATAL, NATIVE_FATAL, ANR,
    SYSTEM_LOW_MEMORY, USER_OR_SYSTEM_TERMINATION,
    NORMAL_EXIT, UNKNOWN
}

fun interface SafeCrashEvidenceSink {
    /** Best effort; no automatic network/SDK transport or persistence. */
    fun tryRecord(category: SafeCrashCategory): Boolean
}
