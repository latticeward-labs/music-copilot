package com.latticeward.musiccopilot.data.repository

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.room.LibraryRow
import com.latticeward.musiccopilot.data.room.LibraryProjection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Platform source wins. Projection changes only after a successful atomic reconcile. */
class RoomLibraryRepository(
    private val source: LocalAudioSource,
    private val projection: LibraryProjection,
    private val dispatchers: DispatcherProvider,
    repositoryScope: CoroutineScope,
) : LibraryRepository {
    private val scanMutex = Mutex()
    private val state = MutableStateFlow(LibraryIndexSnapshot(0, LibraryIndexStatus.Unavailable, 0))
    override val index: StateFlow<LibraryIndexSnapshot> = state

    override fun observeLibrary(): Flow<List<LibraryItem>> =
        combine(projection.observeAll(), index) { rows, status ->
            if (status.status != LibraryIndexStatus.Ready) emptyList()
            else rows.map(LibraryRowMapper::toItem)
        }

    override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
        withContext(dispatchers.io) {
            scanMutex.withLock {
                if (!source.hasAccess()) return@withLock permissionUnavailable()
                val last = state.value
                state.value = last.copy(status = LibraryIndexStatus.Scanning, lastError = null)
                try {
                    val scanned = source.scan()
                    // Reject duplicate IDs deterministically; no personal media in logs.
                    val rows = scanned.map(LibraryRowMapper::fromSource).distinctBy { it.mediaRef }
                    val before = projection.all().map { it.mediaRef }.toSet()
                    val after = rows.map { it.mediaRef }.toSet()
                    projection.replaceAll(rows) // Atomic Room transaction in adapter.
                    val revision = last.revision + 1
                    state.value = LibraryIndexSnapshot(
                        revision, if (rows.isEmpty()) LibraryIndexStatus.Empty
                        else LibraryIndexStatus.Ready, rows.size
                    )
                    AppResult.Success(
                        LibraryReconcileResult(
                            scanned = scanned.size, insertedOrUpdated = rows.size,
                            removedStale = (before - after).size,
                            skippedInvalid = scanned.size - rows.size, revision = revision
                        )
                    )
                } catch (cancelled: CancellationException) {
                    state.value = last.copy(status = LibraryIndexStatus.Unavailable)
                    throw cancelled
                } catch (security: SecurityException) {
                    permissionUnavailable()
                } catch (failure: Exception) {
                    val error = AppError.Library(AppErrorCodes.LIBRARY_RECONCILE_FAILED,
                        Recoverability.RECOVERABLE, RecoveryAction.Rebuild)
                    state.value = last.copy(status = LibraryIndexStatus.RecoverableError, lastError = error)
                    AppResult.Failure(error)
                }
            }
        }

    private fun permissionUnavailable(): AppResult.Failure {
        state.value = state.value.copy(
            status = LibraryIndexStatus.Unavailable,
            itemCount = 0, lastError = null
        )
        return AppResult.Failure(
            AppError.Permission(AppErrorCodes.PERMISSION_REVOKED,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings)
        )
    }

    override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
        withContext(dispatchers.io) {
            if (!source.hasAccess()) return@withContext permissionUnavailable()
            if (index.value.status != LibraryIndexStatus.Ready)
                return@withContext AppResult.Success(emptyList())
            val requested = LibraryRowMapper.normalize(query.text)
            if (requested.isBlank() || query.limit <= 0)
                return@withContext AppResult.Success(emptyList())
            try {
                val matched = projection.all().filter {
                    it.normalizedTitle.contains(requested) ||
                        it.normalizedArtist.contains(requested)
                }.take(query.limit.coerceAtMost(100)).map(LibraryRowMapper::toItem)
                AppResult.Success(matched)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { AppResult.Failure(
                AppError.Library(AppErrorCodes.LIBRARY_SCAN_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Rebuild)) }
        }

    override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
        withContext(dispatchers.io) {
            if (!source.hasAccess()) return@withContext permissionUnavailable()
            try {
                val verified = source.requery(ref)
                if (verified == null) AppResult.Failure(
                    AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_MISSING,
                        Recoverability.RECOVERABLE, RecoveryAction.Skip, ref))
                else AppResult.Success(
                    LibraryItem(verified.ref, verified.title, verified.artist,
                        verified.album, verified.durationMs))
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (security: SecurityException) { permissionUnavailable() }
            catch (failure: Exception) { AppResult.Failure(
                AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
                    Recoverability.RECOVERABLE, RecoveryAction.Skip, ref)) }
        }

    override suspend fun resolveVoiceCandidates(
        normalizedTarget: String, limit: Int,
    ): AppResult<List<LibraryCandidate>> = withContext(dispatchers.default) {
        if (!source.hasAccess()) return@withContext permissionUnavailable()
        val q = LibraryRowMapper.normalize(normalizedTarget)
        if (q.isBlank() || index.value.status != LibraryIndexStatus.Ready)
            return@withContext AppResult.Success(emptyList())
        try {
            val candidates = withContext(dispatchers.io) { projection.all() }
            AppResult.Success(candidates.mapNotNull { row ->
                val key = row.normalizedTitle
                val similarity = when {
                    key == q -> 1.0
                    key.contains(q) -> 0.88
                    row.normalizedArtist.contains(q) -> 0.72
                    else -> return@mapNotNull null
                }
                LibraryCandidate(
                    MediaRef(row.mediaRef), row.title, key, similarity
                )
            }.sortedWith(compareByDescending<LibraryCandidate> { it.similarity }
                .thenBy { it.ref.value }).take(limit.coerceIn(0, 2)))
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { AppResult.Failure(
            AppError.Library(AppErrorCodes.LIBRARY_SCAN_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Rebuild)) }
    }
}

