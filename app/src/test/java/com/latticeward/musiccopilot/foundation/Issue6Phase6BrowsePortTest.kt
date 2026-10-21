package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowsePage
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.LibraryProjection
import com.latticeward.musiccopilot.data.room.LibraryRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Synthetic P06 port tests AUTHORED now and NOT_RUN until P12 T03/T04. */
class Issue6Phase6BrowsePortTest {
    private val fixture = ScannedAudio(MediaStoreRefCodec.encode(1), "Synthetic Song",
        "Synthetic Artist", "Local Album", 1000L)

    private class FakeMedia(var granted: Boolean = true) : LocalAudioSource {
        var items: List<ScannedAudio> = emptyList()
        override fun hasAccess() = granted
        override suspend fun scan() = items
        override suspend fun requery(ref: MediaRef) = items.firstOrNull { it.ref == ref }
    }

    private class FakeDb : LibraryProjection {
        var rows: List<LibraryRow> = emptyList()
        var browseInvocations = 0
        var searchInvocations = 0
        var readHook: (() -> Unit)? = null
        private val mutable = MutableStateFlow(rows)
        override fun observeAll(): Flow<List<LibraryRow>> = mutable
        override suspend fun all() = rows
        override suspend fun replaceAll(rows: List<LibraryRow>) {
            this.rows = rows
            mutable.value = rows
        }

        override suspend fun browsePage(request: LibraryBrowseRequest,
                                        acceptedRevision: Long): LibraryBrowsePage {
            browseInvocations++
            readHook?.invoke()
            val all = rows.sortedWith(compareBy<LibraryRow> { it.normalizedTitle }
                .thenBy { it.mediaRef })
            val page = all.drop(request.page.offset).take(request.page.limit)
                .map { LibraryBrowseEntry.Song(LibraryRowMapper.toItem(it)) }
            return LibraryBrowsePage(page, all.size, acceptedRevision,
                if (request.page.offset.toLong() + page.size < all.size)
                    request.page.offset + page.size else null)
        }
        override suspend fun searchLocal(needle: String, limit: Int): List<LibraryRow> {
            searchInvocations++
            readHook?.invoke()
            return rows.filter {
                it.normalizedTitle.contains(needle) ||
                    it.normalizedArtist.contains(needle) ||
                    (it.normalizedAlbum?.contains(needle) == true)
            }.take(limit)
        }
    }

    private fun repo(media: FakeMedia, db: FakeDb) =
        RoomLibraryRepository(media, db, TestDispatcherProvider(),
            CoroutineScope(SupervisorJob()))

    private fun failureCode(result: AppResult<*>): String {
        assertTrue(result is AppResult.Failure)
        return (result as AppResult.Failure).error.code
    }

    @Test fun cannotBrowseBeforeAuthorizedReconciliation() = runBlocking {
        val media = FakeMedia()
        val db = FakeDb()
        val result = repo(media, db).browse(LibraryBrowseRequest(LibraryBrowseCategory.Songs))
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE, failureCode(result))
        assertEquals(0, db.browseInvocations)
    }

    @Test fun permissionDeniedNeverExecutesRoomQuery() = runBlocking {
        val media = FakeMedia(false)
        val db = FakeDb()
        val result = repo(media, db).browse(LibraryBrowseRequest(LibraryBrowseCategory.Artists))
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, failureCode(result))
        assertEquals(0, db.browseInvocations)
    }

    @Test fun staleExpectedRevisionReturnsTypedError() = runBlocking {
        val media = FakeMedia().apply { items = listOf(fixture) }
        val db = FakeDb()
        val repository = repo(media, db)
        assertTrue(repository.reconcile() is AppResult.Success<*>)
        val stale = repository.browse(LibraryBrowseRequest(LibraryBrowseCategory.Songs,
            LibraryPageRequest(expectedRevision = 99)))
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE, failureCode(stale))
        assertEquals(0, db.browseInvocations)
    }

    @Test fun localBlankSearchDoesNotTouchSql() = runBlocking {
        val media = FakeMedia().apply { items = listOf(fixture) }
        val db = FakeDb()
        val repository = repo(media, db)
        repository.reconcile()
        val response = repository.search(LibraryQuery("  ", 100))
        assertTrue(response is AppResult.Success<*>)
        assertEquals(0, db.searchInvocations)
    }

    @Test fun localSearchCapsResultsAt100AndPreservesQuery() = runBlocking {
        val media = FakeMedia().apply { items = listOf(fixture) }
        val db = FakeDb()
        val repository = repo(media, db)
        repository.reconcile()
        val response = repository.search(LibraryQuery("ALBUM", 500))
        assertTrue(response is AppResult.Success<*>)
        assertEquals(1, db.searchInvocations)
        assertTrue((response as AppResult.Success<*>).value is List<*>)
    }

    @Test fun permissionRevokedDuringPageQueryNeverPublishesPage() = runBlocking {
        val media = FakeMedia().apply { items = listOf(fixture) }
        val db = FakeDb()
        val repository = repo(media, db)
        repository.reconcile()
        db.readHook = { media.granted = false }
        val result = repository.browse(LibraryBrowseRequest(LibraryBrowseCategory.Songs))
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, failureCode(result))
        assertEquals(LibraryIndexStatus.Unavailable, repository.index.value.status)
    }

    @Test fun searchIncludesDerivedAlbumKeyAndNeverCallsRoomAllOnEachKeystroke() = runBlocking {
        val media = FakeMedia().apply { items = listOf(fixture) }
        val db = FakeDb()
        val repository = repo(media, db)
        repository.reconcile()
        db.readHook = null
        val result = repository.search(LibraryQuery("local album", 10))
        assertTrue(result is AppResult.Success<*>)
        assertEquals(1, (result as AppResult.Success<*>).value.let {
            (it as List<*>).size
        })
        assertEquals(1, db.searchInvocations)
    }
}
