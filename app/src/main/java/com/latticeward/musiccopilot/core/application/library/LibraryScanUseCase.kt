package com.latticeward.musiccopilot.core.application.library

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Issue #5/P08: permission-bound readiness handoff, NOT a music catalog or Player.
 * A single owner serializes an explicit post-grant action and user-requested
 * recovery, so repeated callbacks never start a second implicit scan.
 */
enum class LibraryScanReason { AfterMediaGrant, RetryRecoverableError, ExplicitRescan }

sealed interface LibraryScanOutcome {
    data class Completed(val result: LibraryReconcileResult) : LibraryScanOutcome
    data object PermissionUnavailable : LibraryScanOutcome
    data object AlreadyCurrentOrIneligible : LibraryScanOutcome
    data object RecoverableFailure : LibraryScanOutcome
}

class LibraryScanUseCase(
    private val library: LibraryRepository,
    private val hasMediaPermission: () -> Boolean,
) {
    private val mutex = Mutex()

    suspend fun execute(reason: LibraryScanReason): LibraryScanOutcome = mutex.withLock {
        // Android (not Room, preferences or an ActivityResult Boolean) is truth.
        if (!hasMediaPermission()) {
            library.invalidateMediaAccess()
            return@withLock LibraryScanOutcome.PermissionUnavailable
        }

        val status = library.index.value.status
        val eligible = when (reason) {
            LibraryScanReason.AfterMediaGrant ->
                status == LibraryIndexStatus.Unavailable ||
                    status == LibraryIndexStatus.RecoverableError
            LibraryScanReason.RetryRecoverableError ->
                status == LibraryIndexStatus.RecoverableError
            LibraryScanReason.ExplicitRescan ->
                status != LibraryIndexStatus.Scanning
        }
        if (!eligible) return@withLock LibraryScanOutcome.AlreadyCurrentOrIneligible

        val outcome = library.reconcile()
        // Revocation while scanning must never be published as a successful handoff.
        if (!hasMediaPermission()) {
            library.invalidateMediaAccess()
            return@withLock LibraryScanOutcome.PermissionUnavailable
        }
        when (outcome) {
            is AppResult.Success -> LibraryScanOutcome.Completed(outcome.value)
            is AppResult.Failure -> {
                if (library.index.value.status == LibraryIndexStatus.Unavailable)
                    LibraryScanOutcome.PermissionUnavailable
                else LibraryScanOutcome.RecoverableFailure
            }
        }
    }
}

/** Only an authorized, completed reconciliation may assert verified Empty/Ready. */
object LibraryReadinessResolver {
    fun verifiedStatus(permissionGranted: Boolean, snapshot: LibraryIndexSnapshot): LibraryIndexStatus {
        if (!permissionGranted) return LibraryIndexStatus.Unavailable
        return when (snapshot.status) {
            LibraryIndexStatus.Ready ->
                if (snapshot.itemCount > 0 && snapshot.revision > 0L)
                    LibraryIndexStatus.Ready
                else LibraryIndexStatus.Unavailable
            LibraryIndexStatus.Empty ->
                if (snapshot.itemCount == 0 && snapshot.revision > 0L)
                    LibraryIndexStatus.Empty else LibraryIndexStatus.Unavailable
            LibraryIndexStatus.Unavailable -> LibraryIndexStatus.Unavailable
            LibraryIndexStatus.Scanning -> LibraryIndexStatus.Scanning
            LibraryIndexStatus.RecoverableError -> LibraryIndexStatus.RecoverableError
        }
    }
}
