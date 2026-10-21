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
import com.latticeward.musiccopilot.data.room.LibraryDeltaPlanner
import com.latticeward.musiccopilot.data.room.LibraryProjection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
    private val accessLock = Any()
    private val accessEpoch = java.util.concurrent.atomic.AtomicLong(0L)
    private val state = MutableStateFlow(LibraryIndexSnapshot(0, LibraryIndexStatus.Unavailable, 0))
    override val index: StateFlow<LibraryIndexSnapshot> = state

    /**
     * Publish the verified item set together with its revision, independently
     * of Room's asynchronous invalidation Flow. Until a newly committed
     * revision is approved, readers see only the previous verified items.
     * None of these rows proves media existence: get(ref) re-queries OS.
     */
    private data class VerifiedRows(val revision: Long, val items: List<LibraryItem>)
    private val verifiedRows = MutableStateFlow(VerifiedRows(0L, emptyList()))

    override fun invalidateMediaAccess() {
        // Derived Room rows are retained, but remain completely inaccessible
        // while the OS permission is absent. Do not purge app-owned cache or
        // race a fresh authorized reconciliation after a later re-grant.
        synchronized(accessLock) {
            accessEpoch.incrementAndGet()
            state.value = state.value.copy(
                status = LibraryIndexStatus.Unavailable, itemCount = 0, lastError = null,
            )
        }
    }

    override fun observeLibrary(): Flow<List<LibraryItem>> =
        combine(verifiedRows, index) { committed, snapshot ->
            // A previous successful revision stays readable during a later
            // authorized Scanning/RecoverableError, but NEVER after revoke.
            val visible = snapshot.status == LibraryIndexStatus.Ready ||
                snapshot.status == LibraryIndexStatus.Scanning ||
                snapshot.status == LibraryIndexStatus.RecoverableError
            if (!visible || !source.hasAccess() ||
                committed.revision != snapshot.revision ||
                committed.items.size != snapshot.itemCount
            ) emptyList()
            else committed.items
        }

    override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
        withContext(dispatchers.io) {
            // Scan attempts serialize on the repository, not the Activity.
            scanMutex.withLock {
                if (!source.hasAccess()) return@withLock permissionUnavailable()
                val epoch = accessEpoch.get()
                val previousSnapshot = state.value
                state.value = previousSnapshot.copy(
                    status = LibraryIndexStatus.Scanning, lastError = null,
                )
                try {
                    // A cancelled or failing MediaStore scan MUST NOT touch Room.
                    val scanned = source.scan()
                    currentCoroutineContext().ensureActive()
                    val normalized = LibraryRowMapper.deriveIndexRows(scanned)
                    currentCoroutineContext().ensureActive()

                    val guard = {
                        if (accessEpoch.get() != epoch || !source.hasAccess()) {
                            throw SecurityException("media access invalidated")
                        }
                    }
                    guard()
                    val stored = projection.all()
                    currentCoroutineContext().ensureActive()
                    guard()

                    // Whole source snapshot is needed to confirm removals; the
                    // SQLite update itself is genuinely incremental.
                    val delta = LibraryDeltaPlanner.calculate(stored, normalized)
                    currentCoroutineContext().ensureActive()
                    guard()
                    // Room's production adapter writes/deletes only delta rows,
                    // in a single cancellable transaction and with a guard
                    // inside it. No delete-all on successful reconciliation.
                    projection.applyDelta(delta, guard)
                    currentCoroutineContext().ensureActive()
                    guard()

                    // Empty first scan must produce revision > 0; no-op scans
                    // retain a stable version for paginated Browse/Search.
                    // Regrant after permission loss requires a fresh revision.
                    val revision = if (previousSnapshot.revision == 0L ||
                        previousSnapshot.status == LibraryIndexStatus.Unavailable ||
                        delta.changed
                    ) previousSnapshot.revision + 1L else previousSnapshot.revision
                    val approved = synchronized(accessLock) {
                        if (accessEpoch.get() != epoch || !source.hasAccess()) false
                        else {
                            verifiedRows.value = VerifiedRows(
                                revision, normalized.map(LibraryRowMapper::toItem),
                            )
                            state.value = LibraryIndexSnapshot(
                                revision = revision,
                                status = if (normalized.isEmpty()) LibraryIndexStatus.Empty
                                    else LibraryIndexStatus.Ready,
                                itemCount = normalized.size,
                                lastError = null,
                            )
                            true
                        }
                    }
                    if (!approved) return@withLock permissionUnavailable()

                    AppResult.Success(
                        LibraryReconcileResult(
                            scanned = scanned.size,
                            insertedOrUpdated = delta.changedCount,
                            removedStale = delta.removedCount,
                            // Duplicate provider rows are merged, not malformed.
                            skippedInvalid = scanned.count {
                                com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
                                    .decodeScoped(it.ref) == null
                            },
                            revision = revision,
                        ),
                    )
                } catch (cancelled: CancellationException) {
                    // Room withTransaction rolls back its write on coroutine
                    // cancellation. A previously verified revision is still
                    // valid when access was not revoked.
                    synchronized(accessLock) {
                        if (accessEpoch.get() == epoch && source.hasAccess()) {
                            state.value = previousSnapshot
                        } else {
                            invalidateMediaAccess()
                        }
                    }
                    throw cancelled
                } catch (security: SecurityException) {
                    permissionUnavailable()
                } catch (failure: Exception) {
                    if (accessEpoch.get() != epoch || !source.hasAccess()) {
                        return@withLock permissionUnavailable()
                    }
                    val error = AppError.Library(
                        AppErrorCodes.LIBRARY_RECONCILE_FAILED,
                        Recoverability.RECOVERABLE, RecoveryAction.Rebuild,
                    )
                    // Preserve the previously accepted count/revision/cache,
                    // but report a truthful recoverable refresh failure.
                    state.value = previousSnapshot.copy(
                        status = LibraryIndexStatus.RecoverableError,
                        lastError = error,
                    )
                    AppResult.Failure(error)
                }
            }
        }

    private fun permissionUnavailable(): AppResult.Failure {
        invalidateMediaAccess()
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
