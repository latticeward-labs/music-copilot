package com.latticeward.musiccopilot.core.application.privacy

/**
 * Issue #12 P12; UF-12 / WF-10. Only APP-OWNED local data categories may
 * be selected for deletion. Never present MediaStore music, the Room library
 * projection, Likes, selected Bluetooth, preferences, cloud speech consent or
 * the service-owned playback queue as a clearable diagnostic category.
 */
data class LocalPrivacyClearSelection(
    val shortHistory: Boolean = false,
    val diagnostics: Boolean = false,
) {
    val anySelected: Boolean get() = shortHistory || diagnostics
}

enum class LocalClearCategoryResult {
    NOT_SELECTED, CLEARED, NOT_STORED, UNAVAILABLE, FAILED
}

/**
 * No remote analytics provider exists in MVP. A local RAM purge cannot
 * authenticate remote-provider deletion (and never attempts it).
 */
enum class ExternalDiagnosticsDeletion {
    NOT_CONFIGURED,
    REQUIRES_PROVIDER_CONFIRMATION,
}

enum class LocalClearOverall {
    CLEARED, PARTIALLY_CLEARED, FAILED, NOTHING_SELECTED
}

data class LocalPrivacyClearReport(
    val shortHistory: LocalClearCategoryResult,
    val diagnostics: LocalClearCategoryResult,
    val external: ExternalDiagnosticsDeletion = ExternalDiagnosticsDeletion.NOT_CONFIGURED,
) {
    val overall: LocalClearOverall get() {
        val states = listOf(shortHistory, diagnostics).filter {
            it != LocalClearCategoryResult.NOT_SELECTED
        }
        if (states.isEmpty()) return LocalClearOverall.NOTHING_SELECTED
        val successful = states.any {
            it == LocalClearCategoryResult.CLEARED || it == LocalClearCategoryResult.NOT_STORED
        }
        val failed = states.any {
            it == LocalClearCategoryResult.FAILED || it == LocalClearCategoryResult.UNAVAILABLE
        }
        return when {
            failed && successful -> LocalClearOverall.PARTIALLY_CLEARED
            failed -> LocalClearOverall.FAILED
            else -> LocalClearOverall.CLEARED
        }
    }
}

data class DiagnosticsRevocationReport(
    /** FALSE means local RAM/queued events were not proven erased. */
    val localDiagnosticsCleared: Boolean,
    /** TRUE only after AppResult.Success of the existing DataStore update. */
    val persisted: Boolean,
) {
    val complete: Boolean get() = localDiagnosticsCleared && persisted
    val partiallyCompleted: Boolean get() = localDiagnosticsCleared != persisted
}

/**
 * NO history table/DAO/repository currently exists. Avoid a fake
 * "Cleared history" success and, above all, never reuse RestorableQueueStore,
 * LibraryDao.clearDerived or delete MediaStore user audio as short history.
 * A future history owner must implement a genuinely independent port.
 */
enum class ShortHistoryAvailability {
    NOT_STORED_IN_CURRENT_BUILD,
    AVAILABLE_WITH_OWNED_CLEAR_PORT,
}
