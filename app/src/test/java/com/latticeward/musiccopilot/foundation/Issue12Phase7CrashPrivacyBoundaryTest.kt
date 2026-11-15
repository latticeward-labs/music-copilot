package com.latticeward.musiccopilot.foundation

import android.app.ApplicationExitInfo
import com.latticeward.musiccopilot.app.platform.crash.AndroidProcessExitClassification
import com.latticeward.musiccopilot.quality.adapters.ConsentGatedCrashEvidenceReporter
import com.latticeward.musiccopilot.quality.api.SafeCrashCategory
import com.latticeward.musiccopilot.quality.api.SafeCrashEvidenceSink
import com.latticeward.musiccopilot.quality.noop.NoOpCrashEvidenceSink
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * P07 SOURCE ONLY: these scenarios are authored, not compiled, not run.
 * They cannot establish crash-free journeys. No crash SDK, traces or upload.
 */
class Issue12Phase7CrashPrivacyBoundaryTest {
    @Test fun androidJavaNativeAnrAreDifferentClosedReasonCategories() {
        assertEquals(SafeCrashCategory.JAVA_FATAL,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_CRASH))
        assertEquals(SafeCrashCategory.NATIVE_FATAL,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_CRASH_NATIVE))
        assertEquals(SafeCrashCategory.ANR,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_ANR))
    }

    @Test fun lowMemoryIsNotClassifiedAsJavaCrashOrCrashFree() {
        assertEquals(SafeCrashCategory.SYSTEM_LOW_MEMORY,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_LOW_MEMORY))
        assertEquals(SafeCrashCategory.USER_OR_SYSTEM_TERMINATION,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_SIGNALED))
        assertNotEquals(SafeCrashCategory.JAVA_FATAL,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_LOW_MEMORY))
    }

    @Test fun unknownAndroidExitReasonRemainsUnknownNotCrashFree() {
        assertEquals(SafeCrashCategory.UNKNOWN,
            AndroidProcessExitClassification.fromReason(Int.MIN_VALUE))
        assertEquals(SafeCrashCategory.NORMAL_EXIT,
            AndroidProcessExitClassification.fromReason(ApplicationExitInfo.REASON_EXIT_SELF))
    }

    @Test fun crashCollectionRemainsOffWithoutIndependentApprovalAndConsent() {
        var approved = false
        var forwarded = 0
        val reporter = ConsentGatedCrashEvidenceReporter(
            authorized = { approved },
            sink = SafeCrashEvidenceSink { forwarded++; true },
        )
        assertFalse(reporter.record(SafeCrashCategory.JAVA_FATAL))
        assertNull(reporter.peekLocal())
        assertEquals(0, forwarded)
        approved = true
        assertTrue(reporter.record(SafeCrashCategory.JAVA_FATAL))
        assertEquals(SafeCrashCategory.JAVA_FATAL, reporter.peekLocal())
        assertEquals(1, forwarded)
        approved = false
        assertNull(reporter.peekLocal())
        assertFalse(reporter.record(SafeCrashCategory.ANR))
        assertEquals(1, forwarded)
    }

    @Test fun revocationCannotDeliverWhenAuthorizationChangesBeforeSink() {
        var checks = 0
        var attempts = 0
        val reporter = ConsentGatedCrashEvidenceReporter(
            authorized = { ++checks == 1 },
            sink = SafeCrashEvidenceSink { attempts++; true },
        )
        assertFalse(reporter.record(SafeCrashCategory.JAVA_FATAL))
        assertEquals(0, attempts)
        assertNull(reporter.peekLocal())
    }

    @Test fun localEvidenceCanBeClearedWithoutAnyPersistentStore() {
        val reporter = ConsentGatedCrashEvidenceReporter(
            authorized = { true },
            sink = NoOpCrashEvidenceSink()
        )
        assertFalse(reporter.record(SafeCrashCategory.NATIVE_FATAL))
        assertEquals(SafeCrashCategory.NATIVE_FATAL, reporter.peekLocal())
        reporter.clear()
        assertNull(reporter.peekLocal())
    }

    @Test fun throwingCrashSinkIsBestEffortAndCannotCrashTheApp() {
        val reporter = ConsentGatedCrashEvidenceReporter(
            authorized = { true },
            sink = SafeCrashEvidenceSink { throw IllegalStateException("private path") }
        )
        assertFalse(reporter.record(SafeCrashCategory.ANR))
    }

    @Test fun crashReporterHasNoThrowableInputAndNoProviderSdk() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val api = File(root+"quality/api/SafeCrashEvidence.kt").readText()
        val impl = File(root+"quality/adapters/ConsentGatedCrashEvidenceReporter.kt").readText()
        val android = File(root+"app/platform/crash/AndroidProcessExitClassification.kt").readText()
        val app = File(root+"app/di/ApplicationBindings.kt").readText()
        assertTrue(api.contains("enum class SafeCrashCategory"))
        assertTrue(impl.contains("No provider exceptions"))
        assertTrue(android.contains("getHistoricalProcessExitReasons("))
        assertFalse(android.contains("last.traceInputStream"))
        assertFalse(android.contains("last.description"))
        assertFalse(android.contains("last.processName"))
        assertFalse(impl.contains("FirebaseCrashlytics"))
        assertFalse(impl.contains("Sentry"))
        assertTrue(app.contains("NoOpCrashEvidenceSink()"))
        assertTrue(app.contains("if (!diagnosticsCollectionAuthorization.canCollect()) return"))
        assertTrue(app.contains("inspectPriorProcessExitIfApproved"))
    }

    @Test fun androidTenIsExplicitlyUnknownUntilAppropriateOsEvidence() {
        val source = File("src/main/java/com/latticeward/musiccopilot/" +
            "app/platform/crash/AndroidProcessExitClassification.kt").readText()
        assertTrue(source.contains("Build.VERSION.SDK_INT < Build.VERSION_CODES.R"))
        assertTrue(source.contains("return SafeCrashCategory.UNKNOWN"))
    }

    @Test fun fatalClassificationIsNotAUserSessionCompletionMetric() {
        val source = File("src/main/java/com/latticeward/musiccopilot/" +
            "app/di/ApplicationBindings.kt").readText()
        assertFalse(source.contains("TelemetryEvent.SessionCompleted(SafeCrashCategory"))
        assertTrue(source.contains("lastApprovedCrashEvidence(): SafeCrashCategory?"))
    }
}
