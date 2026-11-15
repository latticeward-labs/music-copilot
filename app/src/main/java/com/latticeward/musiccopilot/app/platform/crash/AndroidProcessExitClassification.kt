package com.latticeward.musiccopilot.app.platform.crash

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import com.latticeward.musiccopilot.quality.api.SafeCrashCategory

/**
 * P07: on-demand AFTER approved diagnostic consent. Android 11+ OS
 * historical process exit classification, NOT a custom uncaught handler.
 * Android 10 has no safe portable exit-reason API: UNKNOWN.
 *
 * Prohibitions: never read ApplicationExitInfo.description, processName,
 * timestamp, pid, traceInputStream, stack, message, or any app files.
 * Never print/debug/serialize the ApplicationExitInfo object.
 */
internal object AndroidProcessExitClassification {
    fun previousExit(context: Context?): SafeCrashCategory {
        val app = context?.applicationContext ?: return SafeCrashCategory.UNKNOWN
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R)
            return SafeCrashCategory.UNKNOWN
        return try {
            val manager = app.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                ?: return SafeCrashCategory.UNKNOWN
            val last = manager.getHistoricalProcessExitReasons(
                null, 0, 1,
            ).firstOrNull() ?: return SafeCrashCategory.UNKNOWN
            fromReason(last.reason)
        } catch (_: RuntimeException) {
            SafeCrashCategory.UNKNOWN
        } catch (_: SecurityException) {
            SafeCrashCategory.UNKNOWN
        }
    }

    /** Only the Android numeric REASON enum crosses the platform boundary. */
    fun fromReason(reason: Int): SafeCrashCategory = when (reason) {
        ApplicationExitInfo.REASON_CRASH -> SafeCrashCategory.JAVA_FATAL
        ApplicationExitInfo.REASON_CRASH_NATIVE -> SafeCrashCategory.NATIVE_FATAL
        ApplicationExitInfo.REASON_ANR -> SafeCrashCategory.ANR
        ApplicationExitInfo.REASON_LOW_MEMORY -> SafeCrashCategory.SYSTEM_LOW_MEMORY
        ApplicationExitInfo.REASON_USER_REQUESTED,
        ApplicationExitInfo.REASON_SIGNALED ->
            SafeCrashCategory.USER_OR_SYSTEM_TERMINATION
        ApplicationExitInfo.REASON_EXIT_SELF ->
            SafeCrashCategory.NORMAL_EXIT
        else -> SafeCrashCategory.UNKNOWN
    }
}
