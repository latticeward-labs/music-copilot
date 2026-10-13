package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.LibraryProjection
import com.latticeward.musiccopilot.data.room.LibraryRow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LocalFirstRepositoryTest {
    private val itemA = ScannedAudio(MediaStoreRefCodec.encode(10), "Synthetic Alpha", "A", null, 1200)
    private val itemB = ScannedAudio(MediaStoreRefCodec.encode(20), "Synthetic Beta", "B", null, 2400)

    private class FakeMediaStore : LocalAudioSource {
        var granted = false
        var items = emptyList<ScannedAudio>()
        override fun hasAccess() = granted
        override suspend fun scan(): List<ScannedAudio> {
            if (!granted) throw SecurityException("revoked")
            return items
        }
        override suspend fun requery(ref: MediaRef): ScannedAudio? =
            if (granted) items.firstOrNull { it.ref == ref } else throw SecurityException("revoked")
    }

    private class FakeProjection : LibraryProjection {
        var rows = emptyList<LibraryRow>()
        var replaceCalls = 0
        var throwOnReplace = false
        private val stream = MutableStateFlow(emptyList<LibraryRow>())
        override fun observeAll(): Flow<List<LibraryRow>> = stream
        override suspend fun all(): List<LibraryRow> = rows
        override suspend fun replaceAll(rows: List<LibraryRow>) {
            if (throwOnReplace) throw IllegalStateException("synthetic failure")
            this.rows = rows
            replaceCalls++
            stream.value = rows
        }
    }

    @Test fun cannotTreatCachedProjectionAsPermissionOrExistenceTruth() = runBlocking {
        val media = FakeMediaStore()
        val projection = FakeProjection()
        val repo = RoomLibraryRepository(media, projection, TestDispatcherProvider(),
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()))
        projection.replaceAll(listOf(LibraryRowMapper.fromSource(itemA)))
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertTrue(repo.reconcile() is AppResult.Failure)
        assertEquals(0, repo.observeLibrary().first().size)
        assertTrue(repo.get(itemA.ref) is AppResult.Failure)
        assertEquals(1, projection.rows.size) // stale projection never becomes accessible truth
    }

    @Test fun reconciliationRebuildsAndRemovesStaleDerivedRows() = runBlocking {
        val media = FakeMediaStore().apply { granted = true; items = listOf(itemA, itemB) }
        val projection = FakeProjection()
        val repo = RoomLibraryRepository(media, projection, TestDispatcherProvider(),
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()))
        val first = repo.reconcile()
        assertTrue(first is AppResult.Success<*>)
        assertEquals(2, projection.rows.size)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(2, repo.observeLibrary().first().size)

        media.items = listOf(itemB)
        val later = repo.reconcile() as AppResult.Success<*>
        val report = later.value as com.latticeward.musiccopilot.data.api.LibraryReconcileResult
        assertEquals(1, report.removedStale)
        assertEquals(1, projection.rows.size)
        assertEquals(itemB.ref.value, projection.rows.single().mediaRef)
        assertTrue(repo.get(itemA.ref) is AppResult.Failure)
        assertEquals(2, projection.replaceCalls)
    }

    @Test fun revocationAndPermissionRecheckInvalidateReadyAndSearch() = runBlocking {
        val media = FakeMediaStore().apply { granted = true; items = listOf(itemA) }
        val projection = FakeProjection()
        val repo = RoomLibraryRepository(media, projection, TestDispatcherProvider(),
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()))
        assertTrue(repo.reconcile() is AppResult.Success<*>)
        assertEquals(1, (repo.search(LibraryQuery("alpha", 5)) as AppResult.Success<*>)
            .let { (it.value as List<*>).size })
        media.granted = false
        assertTrue(repo.search(LibraryQuery("alpha", 5)) is AppResult.Failure)
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertTrue(repo.reconcile() is AppResult.Failure)
        assertEquals(0, repo.observeLibrary().first().size)
        assertEquals(1, projection.rows.size) // no hard-delete of app-owned data on revoke
    }

    @Test fun projectionFailureNeverPublishesFakeReadyOrDeletesKnownRows() = runBlocking {
        val media = FakeMediaStore().apply { granted = true; items = listOf(itemA) }
        val projection = FakeProjection().apply { throwOnReplace = true }
        val repo = RoomLibraryRepository(media, projection, TestDispatcherProvider(),
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()))
        assertTrue(repo.reconcile() is AppResult.Failure)
        assertEquals(LibraryIndexStatus.RecoverableError, repo.index.value.status)
        assertTrue(projection.rows.isEmpty())
    }

    @Test fun mediaRefsAreOpaqueNumericQueriesAndMalformedOnesAreRejected() {
        assertEquals(123L, MediaStoreRefCodec.decode(MediaStoreRefCodec.encode(123)))
        assertNull(MediaStoreRefCodec.decode(MediaRef("content://personal/path")))
        assertNull(MediaStoreRefCodec.decode(MediaRef("audio:-1")))
        assertNull(MediaStoreRefCodec.decode(MediaRef("audio:notnumber")))
        assertEquals("synthetic alpha", LibraryRowMapper.normalize("  SYNTHETIC   ALPHA "))
    }
}
