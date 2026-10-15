package com.latticeward.musiccopilot.data.api

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class LibraryIndexStatus { Unavailable, Scanning, Ready, Empty, RecoverableError }

data class LibraryItem(
    val ref: MediaRef,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
)

data class LibraryIndexSnapshot(
    val revision: Long,
    val status: LibraryIndexStatus,
    val itemCount: Int,
    val lastError: AppError.Library? = null,
)

data class LibraryReconcileResult(
    val scanned: Int,
    val insertedOrUpdated: Int,
    val removedStale: Int,
    val skippedInvalid: Int,
    val revision: Long,
)

data class LibraryQuery(val text: String, val limit: Int)
data class LibraryCandidate(
    val ref: MediaRef,
    val displayLabel: String,
    val normalizedKey: String,
    val similarity: Double,
)

/** MediaStore is existence truth; derived Room index never owns a live queue. */
interface LibraryRepository {
    val index: StateFlow<LibraryIndexSnapshot>
    fun observeLibrary(): Flow<List<LibraryItem>>
    suspend fun reconcile(): AppResult<LibraryReconcileResult>
    suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>>
    suspend fun get(ref: MediaRef): AppResult<LibraryItem>
    suspend fun resolveVoiceCandidates(
        normalizedTarget: String,
        limit: Int = 2,
    ): AppResult<List<LibraryCandidate>>
}
