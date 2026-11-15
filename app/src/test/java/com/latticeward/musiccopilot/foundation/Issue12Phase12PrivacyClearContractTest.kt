package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsRevocationReport
import com.latticeward.musiccopilot.core.application.privacy.ExternalDiagnosticsDeletion
import com.latticeward.musiccopilot.core.application.privacy.LocalClearCategoryResult
import com.latticeward.musiccopilot.core.application.privacy.LocalClearOverall
import com.latticeward.musiccopilot.core.application.privacy.LocalPrivacyClearReport
import com.latticeward.musiccopilot.core.application.privacy.LocalPrivacyClearSelection
import com.latticeward.musiccopilot.core.application.privacy.ShortHistoryAvailability
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #12 P12 source contract tests, AUTHORED ONLY.
 * NO build, Gradle, tests, CI, emulator or APK before final P14.
 */
class Issue12Phase12PrivacyClearContractTest {
    @Test fun noDeleteCategoryIsSelectedByDefault() {
        val selected = LocalPrivacyClearSelection()
        assertFalse(selected.shortHistory)
        assertFalse(selected.diagnostics)
        assertFalse(selected.anySelected)
    }

    @Test fun userCanSelectOnlyDiagnosticsWithoutImplicitlyDeletingHistory() {
        val selected = LocalPrivacyClearSelection(diagnostics = true)
        assertTrue(selected.anySelected)
        assertTrue(selected.diagnostics)
        assertFalse(selected.shortHistory)
    }

    @Test fun reportWithoutSelectionCannotClaimDeletionSuccess() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.NOT_SELECTED,
            LocalClearCategoryResult.NOT_SELECTED,
        )
        assertEquals(LocalClearOverall.NOTHING_SELECTED, report.overall)
    }

    @Test fun successfulLocalDiagnosticsDoesNotClaimExternalDeletion() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.NOT_SELECTED,
            LocalClearCategoryResult.CLEARED,
        )
        assertEquals(LocalClearOverall.CLEARED, report.overall)
        assertEquals(ExternalDiagnosticsDeletion.NOT_CONFIGURED, report.external)
    }

    @Test fun missingConcreteHistoryOwnerIsNotAFakeClearedHistory() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.UNAVAILABLE,
            LocalClearCategoryResult.NOT_SELECTED,
        )
        assertEquals(LocalClearOverall.FAILED, report.overall)
        assertEquals(ShortHistoryAvailability.NOT_STORED_IN_CURRENT_BUILD,
            ShortHistoryAvailability.NOT_STORED_IN_CURRENT_BUILD)
    }

    @Test fun mixedCategorySuccessAndFailureShowsPartialClear() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.UNAVAILABLE,
            LocalClearCategoryResult.CLEARED,
        )
        assertEquals(LocalClearOverall.PARTIALLY_CLEARED, report.overall)
    }

    @Test fun noLocalEraseSuccessIfBothAttemptedCategoriesFail() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.FAILED,
            LocalClearCategoryResult.UNAVAILABLE,
        )
        assertEquals(LocalClearOverall.FAILED, report.overall)
    }

    @Test fun successfulSingleHistoryClearWouldNotImplyDiagnosticClear() {
        val report = LocalPrivacyClearReport(
            LocalClearCategoryResult.CLEARED,
            LocalClearCategoryResult.NOT_SELECTED,
        )
        assertEquals(LocalClearOverall.CLEARED, report.overall)
        assertEquals(LocalClearCategoryResult.NOT_SELECTED, report.diagnostics)
    }

    @Test fun repeatClearIsIdempotentInResultContract() {
        val first = LocalPrivacyClearReport(
            LocalClearCategoryResult.NOT_SELECTED, LocalClearCategoryResult.CLEARED,
        )
        val second = first.copy()
        assertEquals(first, second)
    }

    @Test fun diagnosticsRevokeRequiresBothImmediateClearAndPersistedAck() {
        for (local in listOf(true, false)) {
            for (saved in listOf(true, false)) {
                val result = DiagnosticsRevocationReport(local, saved)
                assertEquals(local && saved, result.complete)
            }
        }
    }

    @Test fun partialRevokeIsReportedEvenWhenDataStoreWriteFailed() {
        val result = DiagnosticsRevocationReport(
            localDiagnosticsCleared = true, persisted = false,
        )
        assertFalse(result.complete)
        assertTrue(result.partiallyCompleted)
    }

    @Test fun failureToClearRamIsNotReportedAsFullyRevoked() {
        val result = DiagnosticsRevocationReport(
            localDiagnosticsCleared = false, persisted = true,
        )
        assertFalse(result.complete)
        assertTrue(result.partiallyCompleted)
    }

    @Test fun simultaneousRevokeFailureIsNotMisreportedAsPartialSuccess() {
        val result = DiagnosticsRevocationReport(
            localDiagnosticsCleared = false, persisted = false,
        )
        assertFalse(result.complete)
        assertFalse(result.partiallyCompleted)
    }

    @Test fun noCategoryEnumIncludesProtectedMediaOrPlaybackOwners() {
        val values = LocalClearCategoryResult.entries.map { it.name }.toSet()
        assertFalse(values.any {
            it.contains("MEDIASTORE") || it.contains("LIVE_QUEUE") ||
                it.contains("LIKES") || it.contains("CLOUD_SPEECH")
        })
    }
}
