package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.LibraryDeltaPlanner
import com.latticeward.musiccopilot.data.room.LibraryProjection
import com.latticeward.musiccopilot.data.room.LibraryReconcileDelta
import com.latticeward.musiccopilot.data.room.LibraryRow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * P05 synthetic JVM cases, authored but NOT_RUN until P12 T02/T03.
 * FakeStore models a single atomic Room transaction, not native SQLite.
 * P12 must additionally prove actual Room withTransaction rollback.
 */
class Issue6Phase5IncrementalReconciliationTest {
    private fun audio(id: Long, title: String = "Synthetic A"): ScannedAudio =
        ScannedAudio(MediaStoreRefCodec.encode(id), title, "Artist", "Album", 1000L)

    private class FakeSource : LocalAudioSource {
        var granted = true
        var tracks: List<ScannedAudio> = emptyList()
        var scanCount = 0
        var fail = false
        var suspendScan: (suspend () -> Unit)? = null
        override fun hasAccess(): Boolean = granted
        override suspend fun scan(): List<ScannedAudio> {
            scanCount++
            if (!granted) throw SecurityException("synthetic unavailable")
            suspendScan?.invoke()
            if (fail) throw IllegalStateException("synthetic provider error")
            if (!granted) throw SecurityException("synthetic unavailable")
            return tracks
        }
        override suspend fun requery(ref: MediaRef): ScannedAudio? {
            if (!granted) throw SecurityException("synthetic unavailable")
            return tracks.firstOrNull { it.ref == ref }
        }
    }

    private class FakeStore : LibraryProjection {
        var rows = emptyList<LibraryRow>()
        var commits = 0
        var rebuildCalls = 0
        var failCommit = false
        var beforeCommit: (suspend () -> Unit)? = null
        var previousDeltas = mutableListOf<LibraryReconcileDelta>()
        private val flow = MutableStateFlow(rows)

        override fun observeAll(): Flow<List<LibraryRow>> = flow
        override suspend fun all(): List<LibraryRow> = rows
        override suspend fun replaceAll(rows: List<LibraryRow>) {
            rebuildCalls++
            this.rows = rows
            flow.value = rows
        }
        override suspend fun applyDelta(
            delta: LibraryReconcileDelta,
            preCommitGuard: () -> Unit,
        ) {
            currentCoroutineContext().ensureActive()
            preCommitGuard()
            previousDeltas.add(delta)
            if (!delta.changed) return
            // Simulate a transaction's private write set before publication.
            val draft = rows.associateBy { it.mediaRef }.toMutableMap()
            delta.removedRefs.forEach(draft::remove)
            delta.upserts.forEach { draft[it.mediaRef] = it }
            beforeCommit?.invoke()
            currentCoroutineContext().ensureActive()
            preCommitGuard()
            if (failCommit) throw IllegalStateException("synthetic transaction rollback")
            rows = draft.values.sortedWith(compareBy<LibraryRow> { it.normalizedTitle }
                .thenBy { it.mediaRef })
            commits++
            flow.value = rows
        }
    }

    private fun repository(media: FakeSource, store: FakeStore) =
        RoomLibraryRepository(media, store, TestDispatcherProvider(),
            CoroutineScope(SupervisorJob()))

    @Test fun deltaOnlyContainsAddedChangedAndDeletedRows() {
        val before = listOf(audio(1), audio(2), audio(3)).map(LibraryRowMapper::fromSource)
        val after = listOf(audio(1), audio(2, "Synthetic Changed"), audio(4))
            .map(LibraryRowMapper::fromSource)
        val delta = LibraryDeltaPlanner.calculate(before, after)
        assertEquals(2, delta.changedCount)
        assertEquals(1, delta.removedCount)
        assertEquals(3, delta.previousCount)
        assertEquals(3, delta.resultingCount)
        assertEquals(setOf(audio(2).ref.value, audio(4).ref.value),
            delta.upserts.map { it.mediaRef }.toSet())
        assertEquals(listOf(audio(3).ref.value), delta.removedRefs)
    }

    @Test fun deltaIsOrderIndependentAndRejectsDuplicateIndexKeys() {
        val rows = listOf(audio(1), audio(2)).map(LibraryRowMapper::fromSource)
        assertFalse(LibraryDeltaPlanner.calculate(rows, rows.reversed()).changed)
        assertTrue(runCatching { LibraryDeltaPlanner.calculate(rows + rows[0], rows) }.isFailure)
        assertTrue(runCatching { LibraryDeltaPlanner.calculate(rows, rows + rows[0]) }.isFailure)
    }

    @Test fun firstAuthorizedScanInsertsOnlyCurrentRows() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1), audio(2)) }
        val room = FakeStore()
        val repo = repository(media, room)
        val first = repo.reconcile() as AppResult.Success
        assertEquals(2, first.value.insertedOrUpdated)
        assertEquals(0, first.value.removedStale)
        assertEquals(2, first.value.scanned)
        assertEquals(1L, first.value.revision)
        assertEquals(1, room.commits)
        assertEquals(0, room.rebuildCalls)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
    }

    @Test fun secondIdenticalScanDoesNotWriteOrAdvanceRevision() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1), audio(2)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        val repeat = repo.reconcile() as AppResult.Success
        assertEquals(0, repeat.value.insertedOrUpdated)
        assertEquals(0, repeat.value.removedStale)
        assertEquals(1L, repeat.value.revision)
        assertEquals(1, room.commits)
        assertEquals(2, room.previousDeltas.size)
        assertFalse(room.previousDeltas.last().changed)
        assertEquals(0, room.rebuildCalls)
    }

    @Test fun oneMetadataEditUpdatesOneRowNotEntireLibrary() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1), audio(2), audio(3)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.tracks = listOf(audio(1), audio(2, "Synthetic Edit"), audio(3))
        val result = repo.reconcile() as AppResult.Success
        assertEquals(1, result.value.insertedOrUpdated)
        assertEquals(0, result.value.removedStale)
        assertEquals(2L, result.value.revision)
        assertEquals(audio(2).ref.value, room.previousDeltas.last().upserts.single().mediaRef)
        assertEquals(0, room.rebuildCalls)
    }

    @Test fun deletionAndMoveRemoveOnlyStaleRows() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1), audio(2)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.tracks = listOf(audio(2), audio(3))
        val result = repo.reconcile() as AppResult.Success
        assertEquals(1, result.value.insertedOrUpdated)
        assertEquals(1, result.value.removedStale)
        assertEquals(setOf(audio(2).ref.value, audio(3).ref.value),
            room.rows.map { it.mediaRef }.toSet())
        assertEquals(0, room.rebuildCalls)
    }

    @Test fun emptyFirstScanAndLaterEmptyAreTruthful() = runBlocking {
        val media = FakeSource()
        val room = FakeStore()
        val repo = repository(media, room)
        val first = repo.reconcile() as AppResult.Success
        assertEquals(LibraryIndexStatus.Empty, repo.index.value.status)
        assertEquals(1L, first.value.revision)
        val second = repo.reconcile() as AppResult.Success
        assertEquals(1L, second.value.revision)
        assertEquals(0, room.commits)
        assertEquals(0, room.rebuildCalls)
    }

    @Test fun providerFailureBeforeDeltaPreservesLastAcceptedRows() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.fail = true
        val failure = repo.reconcile()
        assertTrue(failure is AppResult.Failure)
        assertEquals(LibraryIndexStatus.RecoverableError, repo.index.value.status)
        assertEquals(1L, repo.index.value.revision)
        assertEquals(1, repo.observeLibrary().first().size)
        assertEquals(1, room.commits)
        assertEquals(1, room.rows.size)
    }

    @Test fun transactionFailureRollsBackAndNeverPublishesNewRevision() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.tracks = listOf(audio(2))
        room.failCommit = true
        val failure = repo.reconcile()
        assertTrue(failure is AppResult.Failure)
        assertEquals(LibraryIndexStatus.RecoverableError, repo.index.value.status)
        assertEquals(1L, repo.index.value.revision)
        assertEquals(listOf(audio(1).ref.value), room.rows.map { it.mediaRef })
        assertEquals(1, room.commits)
    }

    @Test fun coroutineCancellationBeforeCommitRetainsPreviousVerifiedRevision() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.tracks = listOf(audio(2))
        val entered = CompletableDeferred<Unit>()
        val proceed = CompletableDeferred<Unit>()
        room.beforeCommit = { entered.complete(Unit); proceed.await() }
        val attempt = async { repo.reconcile() }
        entered.await()
        assertEquals(LibraryIndexStatus.Scanning, repo.index.value.status)
        attempt.cancel()
        try {
            attempt.await()
            fail("Expected cancellation")
        } catch (_: CancellationException) { /* expected */ }
        assertEquals(1L, repo.index.value.revision)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(listOf(audio(1).ref.value), room.rows.map { it.mediaRef })
        assertEquals(1, repo.observeLibrary().first().size)
        assertEquals(1, room.commits)
    }

    @Test fun cancellationDuringMediaStoreScanNeverCallsDeltaWriter() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        val entered = CompletableDeferred<Unit>()
        val proceed = CompletableDeferred<Unit>()
        media.suspendScan = { entered.complete(Unit); proceed.await() }
        val attempt = async { repo.reconcile() }
        entered.await()
        attempt.cancel()
        try {
            attempt.await()
            fail("Expected scan cancellation")
        } catch (_: CancellationException) { /* expected */ }
        assertEquals(0, room.commits)
        assertEquals(0, room.previousDeltas.size)
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
    }

    @Test fun revokeWhileTransactionIsStagedCannotPublishOrCommit() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        val entered = CompletableDeferred<Unit>()
        val proceed = CompletableDeferred<Unit>()
        room.beforeCommit = { entered.complete(Unit); proceed.await() }
        val attempt = async { repo.reconcile() }
        entered.await()
        media.granted = false
        repo.invalidateMediaAccess()
        proceed.complete(Unit)
        assertTrue(attempt.await() is AppResult.Failure)
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertEquals(0, room.commits)
        assertTrue(room.rows.isEmpty())
        assertTrue(repo.observeLibrary().first().isEmpty())
    }

    @Test fun regrantNeedsNewRevisionEvenWhenMediaRowsUnchanged() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        repo.reconcile()
        media.granted = false
        repo.invalidateMediaAccess()
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        media.granted = true
        val refreshed = repo.reconcile() as AppResult.Success
        assertEquals(2L, refreshed.value.revision)
        assertEquals(0, refreshed.value.insertedOrUpdated)
        assertEquals(1, room.commits)
    }

    @Test fun concurrentRescansSerializeAndSecondIsNoOp() = runBlocking {
        val media = FakeSource().apply { tracks = listOf(audio(1)) }
        val room = FakeStore()
        val repo = repository(media, room)
        val first = async { repo.reconcile() }
        val second = async { repo.reconcile() }
        assertTrue(first.await() is AppResult.Success)
        assertTrue(second.await() is AppResult.Success)
        assertEquals(2, media.scanCount)
        assertEquals(1, room.commits)
        assertEquals(1L, repo.index.value.revision)
    }

    @Test fun identicalTitlesWithDistinctRefsRemainSeparateAfterDelta() = runBlocking {
        val media = FakeSource().apply {
            tracks = listOf(audio(1, "Shared"), audio(2, "Shared"))
        }
        val room = FakeStore()
        val repo = repository(media, room)
        val success = repo.reconcile() as AppResult.Success
        assertEquals(2, success.value.insertedOrUpdated)
        assertEquals(2, room.rows.size)
        assertNotEquals(room.rows[0].mediaRef, room.rows[1].mediaRef)
    }
}
