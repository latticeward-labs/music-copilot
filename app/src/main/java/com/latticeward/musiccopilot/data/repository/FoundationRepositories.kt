package com.latticeward.musiccopilot.data.repository

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.AppPreferences
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Fail-closed Phase 5 repositories. They do NOT impersonate MediaStore, Room or DataStore.
 * Phase 6 replaces the adapters without changing any public API or ownership.
 */
class FoundationLibraryRepository(repositoryScope: CoroutineScope) : LibraryRepository {
    init { requireNotNull(repositoryScope.coroutineContext[Job]) }
    private val status = MutableStateFlow(
        LibraryIndexSnapshot(0, LibraryIndexStatus.Unavailable, 0)
    )
    override val index: StateFlow<LibraryIndexSnapshot> = status
    override fun invalidateMediaAccess() {
        status.value = status.value.copy(status = LibraryIndexStatus.Unavailable, itemCount = 0)
    }
    override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())

    private fun unavailable() = AppResult.Failure(
        AppError.Library(
            code = AppErrorCodes.LIBRARY_SCAN_FAILED,
            recoverability = Recoverability.RECOVERABLE,
            recoveryAction = RecoveryAction.Retry,
        )
    )

    override suspend fun reconcile(): AppResult<LibraryReconcileResult> = unavailable()
    override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> = unavailable()
    override suspend fun get(ref: MediaRef): AppResult<LibraryItem> = unavailable()
    override suspend fun resolveVoiceCandidates(
        normalizedTarget: String, limit: Int
    ): AppResult<List<LibraryCandidate>> = unavailable()
}

class FoundationPreferencesRepository(repositoryScope: CoroutineScope) : PreferencesRepository {
    init { requireNotNull(repositoryScope.coroutineContext[Job]) }
    private val localDefaults = MutableStateFlow(
        AppPreferences(
            onboardingComplete = false,
            selectedBluetoothHint = null,
            cloudSpeechConsent = CloudSpeechConsent.NotAsked,
        )
    )
    override val preferences: StateFlow<AppPreferences> = localDefaults
    override val loadState: StateFlow<PreferencesLoadState> = MutableStateFlow(PreferencesLoadState.Failed)
    override suspend fun reload(): AppResult<AppPreferences> =
        AppResult.Failure(
            AppError.Storage(AppErrorCodes.STORAGE_READ_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Retry),
        )

    // Do not falsely acknowledge persistence until Phase 6's actual DataStore is bound.
    override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> =
        AppResult.Failure(
            AppError.Storage(AppErrorCodes.STORAGE_WRITE_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Retry)
        )
}

class FoundationRestorableQueueStore(repositoryScope: CoroutineScope) : RestorableQueueStore {
    init { requireNotNull(repositoryScope.coroutineContext[Job]) }
    private fun unavailable() = AppResult.Failure(
        AppError.Storage(AppErrorCodes.STORAGE_READ_FAILED,
            Recoverability.RECOVERABLE, RecoveryAction.None)
    )
    override suspend fun read(): AppResult<RestorableQueueSnapshot?> = unavailable()
    override suspend fun write(snapshot: RestorableQueueSnapshot): AppResult<Unit> =
        AppResult.Failure(
            AppError.Storage(AppErrorCodes.STORAGE_WRITE_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.None)
        )
    override suspend fun clear(): AppResult<Unit> = unavailable()
}
