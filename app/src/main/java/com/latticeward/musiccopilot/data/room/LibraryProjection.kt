package com.latticeward.musiccopilot.data.room

import androidx.room.withTransaction
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow

/** Derived Room projection seam; MediaStore still verifies actual existence. */
interface LibraryProjection {
    fun observeAll(): Flow<List<LibraryRow>>
    suspend fun all(): List<LibraryRow>

    /**
     * Legacy rebuild seam kept for pre-P05 JVM fakes/older callers.
     * Never used in the P05 production reconcile path.
     */
    suspend fun replaceAll(rows: List<LibraryRow>)

    /**
     * Apply only changed rows in one logical transaction.
     * preCommitGuard MUST be checked from inside the transaction, not merely
     * before a coroutine switches to the SQLite dispatcher. It throws when an
     * OS permission/epoch invalidates this scan.
     *
     * Backward compatibility for existing Issue #5 fake projections only:
     * production RoomLibraryProjection always overrides with delta SQL.
     */
    suspend fun applyDelta(
        delta: LibraryReconcileDelta,
        preCommitGuard: () -> Unit,
    ) {
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        if (!delta.changed) return
        val rows = all().associateBy { it.mediaRef }.toMutableMap()
        delta.removedRefs.forEach(rows::remove)
        delta.upserts.forEach { rows[it.mediaRef] = it }
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        replaceAll(rows.values.sortedWith(
            compareBy<LibraryRow> { it.normalizedTitle }.thenBy { it.mediaRef }
        ))
    }
}

class RoomLibraryProjection(private val database: MusicCopilotDatabase) : LibraryProjection {
    private val dao get() = database.libraryDao()

    override fun observeAll(): Flow<List<LibraryRow>> = dao.observeAll()
    override suspend fun all(): List<LibraryRow> = dao.all()

    override suspend fun replaceAll(rows: List<LibraryRow>) {
        // Historical/internal recovery helper, not called by the repository.
        database.withTransaction {
            dao.clearDerived()
            dao.upsert(rows)
        }
    }

    override suspend fun applyDelta(
        delta: LibraryReconcileDelta,
        preCommitGuard: () -> Unit,
    ) {
        // Only a full scan of all currently readable MediaStore volumes can
        // decide stale deletions. A failed scan never invokes this method.
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        if (!delta.changed) return
        database.withTransaction {
            currentCoroutineContext().ensureActive()
            preCommitGuard()
            // SQLite binding limits vary across versions; batches stay below
            // 999 placeholders. ALL chunks use this SINGLE Room transaction.
            for (chunk in delta.removedRefs.chunked(400)) {
                currentCoroutineContext().ensureActive()
                preCommitGuard()
                dao.deleteByRefs(chunk)
            }
            for (chunk in delta.upserts.chunked(400)) {
                currentCoroutineContext().ensureActive()
                preCommitGuard()
                dao.upsert(chunk)
            }
            currentCoroutineContext().ensureActive()
            preCommitGuard()
        }
    }
}
