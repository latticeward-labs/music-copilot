package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.ArtistIndexEntry
import com.latticeward.musiccopilot.data.api.ArtistLookupMode
import com.latticeward.musiccopilot.data.api.ArtistLookupPage
import com.latticeward.musiccopilot.data.api.ArtistLookupRequest
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsPage
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.LibraryProjection
import com.latticeward.musiccopilot.data.room.LibraryRow
import com.latticeward.musiccopilot.data.api.LibraryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P03 synthetic port/permission/revision fixtures.
 * SOURCE AUTHORED ONLY, NOT COMPILED/RUN until owner-approved P13 QA.
 * This fake verifies the repository guard contracts; Android Room SQL has its
 * own separately AUTHORED/NOT_RUN instrumentation test.
 */
class Issue11Phase3ArtistLookupPortTest {
    private fun audio(id: Long, artist: String?, title: String = "Synthetic Song") =
        ScannedAudio(MediaStoreRefCodec.encode(id), title, artist, "Synthetic Album", 1000L)

    private class FakeAudio(var allowed: Boolean = true) : LocalAudioSource {
        var songs: List<ScannedAudio> = emptyList()
        override fun hasAccess(): Boolean = allowed
        override suspend fun scan(): List<ScannedAudio> = songs
        override suspend fun requery(ref: MediaRef): ScannedAudio? =
            songs.firstOrNull { it.ref == ref }
    }

    private class FakeProjection : LibraryProjection {
        var rows: List<LibraryRow> = emptyList()
        private val state = MutableStateFlow(rows)
        var artistReads = 0
        var memberReads = 0
        var readHook: (() -> Unit)? = null
        override fun observeAll(): Flow<List<LibraryRow>> = state
        override suspend fun all(): List<LibraryRow> = rows
        override suspend fun replaceAll(rows: List<LibraryRow>) {
            this.rows = rows
            state.value = rows
        }
        private fun valid(): List<LibraryRow> = rows.filter {
            it.artistGroupId != null && it.artist != null &&
                it.normalizedArtist.isNotBlank()
        }
        override suspend fun lookupArtistGroups(
            request: ArtistLookupRequest, acceptedRevision: Long,
        ): ArtistLookupPage {
            artistReads++
            readHook?.invoke()
            val groups = valid().groupBy { it.artistGroupId!! }.map { (id, members) ->
                ArtistIndexEntry(
                    com.latticeward.musiccopilot.data.api.LibraryGroupId(id),
                    members.minOf { it.artist!! },
                    members.minOf { it.normalizedArtist },
                    members.size,
                )
            }.filter {
                request.mode == ArtistLookupMode.CandidatePage ||
                    it.normalizedKey == request.normalizedTarget
            }.sortedWith(compareBy<ArtistIndexEntry> { it.normalizedKey }
                .thenBy { it.id.value })
            val page = groups.drop(request.offset).take(request.limit)
            return ArtistLookupPage(page, groups.size, acceptedRevision,
                if (request.offset + page.size < groups.size && page.isNotEmpty())
                    request.offset + page.size else null, request.limit)
        }
        override suspend fun artistTrackRefs(
            request: ArtistTrackRefsRequest, acceptedRevision: Long,
        ): ArtistTrackRefsPage {
            memberReads++
            readHook?.invoke()
            val rows = valid().filter { it.artistGroupId == request.artistGroupId.value }
                .sortedWith(compareBy<LibraryRow> { it.normalizedTitle }.thenBy { it.mediaRef })
            val page = rows.drop(request.offset).take(request.limit)
            return ArtistTrackRefsPage(page.map { MediaRef(it.mediaRef) },
                rows.size, acceptedRevision,
                if (request.offset + page.size < rows.size && page.isNotEmpty())
                    request.offset + page.size else null, request.limit)
        }
    }

    private fun repo(media: FakeAudio, db: FakeProjection) =
        RoomLibraryRepository(media, db, TestDispatcherProvider(),
            CoroutineScope(SupervisorJob()))

    private fun failureCode(value: AppResult<*>): String {
        assertTrue(value is AppResult.Failure)
        return (value as AppResult.Failure).error.code
    }

    @Test fun requestBoundsRejectBadKeysAndUnpinnedLaterPages() {
        assertTrue(runCatching { ArtistLookupRequest("Adele", ArtistLookupMode.ExactNormalized) }.isFailure)
        assertTrue(runCatching { ArtistLookupRequest("", ArtistLookupMode.ExactNormalized) }.isFailure)
        assertTrue(runCatching { ArtistLookupRequest("a".repeat(121), ArtistLookupMode.CandidatePage) }.isFailure)
        assertTrue(runCatching { ArtistLookupRequest("adele", ArtistLookupMode.CandidatePage, limit = 101) }.isFailure)
        assertTrue(runCatching { ArtistLookupRequest("adele", ArtistLookupMode.CandidatePage, offset = 50) }.isFailure)
        assertTrue(runCatching {
            ArtistTrackRefsRequest(LibraryGroupIdentity.albumId("test", "a", null), 1)
        }.isFailure)
        assertTrue(runCatching {
            ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("a"), 1, limit = 0)
        }.isFailure)
    }

    @Test fun acceptedAuthoritativeIndexRequiredBeforeEitherRead() = runBlocking {
        val media = FakeAudio()
        val db = FakeProjection()
        val repository = repo(media, db)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("synthetic", ArtistLookupMode.CandidatePage))))
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            failureCode(repository.artistTrackRefs(
                ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("synthetic"), 0))))
        assertEquals(0, db.artistReads)
        assertEquals(0, db.memberReads)
    }

    @Test fun revokedPermissionPreventsQueriesAndIsNotMistakenForNotFound() = runBlocking {
        val media = FakeAudio(false)
        val db = FakeProjection()
        val repository = repo(media, db)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("adele", ArtistLookupMode.ExactNormalized))))
        assertEquals(0, db.artistReads)
        assertEquals(LibraryIndexStatus.Unavailable, repository.index.value.status)
    }

    @Test fun exactArtistLookupIsIdentityScopedNotSongTitle() = runBlocking {
        val media = FakeAudio().apply {
            songs = listOf(audio(1, "Synthetic Artist", "Adele"),
                audio(2, "Synthetic Artist", "Adele Song"))
        }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val wrong = repository.lookupArtistGroups(
            ArtistLookupRequest("adele", ArtistLookupMode.ExactNormalized))
        assertTrue(wrong is AppResult.Success<*>)
        assertEquals(0, (wrong as AppResult.Success).value.entries.size)
        val correct = repository.lookupArtistGroups(
            ArtistLookupRequest("synthetic artist", ArtistLookupMode.ExactNormalized))
        assertEquals(1, (correct as AppResult.Success).value.entries.size)
        assertEquals(2, correct.value.entries.single().indexedSongCount)
    }

    @Test fun missingArtistMetadataIsExcludedButLiteralUnknownArtistIsDistinct() = runBlocking {
        val media = FakeAudio().apply {
            songs = listOf(audio(1, null), audio(2, "Unknown Artist"), audio(3, "  "))
        }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val all = repository.lookupArtistGroups(
            ArtistLookupRequest("unknown artist", ArtistLookupMode.CandidatePage))
        val page = (all as AppResult.Success).value
        assertEquals(1, page.totalCount)
        assertEquals("Unknown Artist", page.entries.single().displayLabel)
        assertNotEquals(LibraryGroupIdentity.artistId(null), page.entries.single().id)
    }

    @Test fun candidatePagesAreStableSortedAndBoundedWithRevisionCursor() = runBlocking {
        val media = FakeAudio().apply {
            songs = listOf(audio(1, "Zulu"), audio(2, "Alpha"), audio(3, "Beta"))
        }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val first = (repository.lookupArtistGroups(
            ArtistLookupRequest("alpha", ArtistLookupMode.CandidatePage, limit = 2))
            as AppResult.Success).value
        assertEquals(listOf("Alpha", "Beta"), first.entries.map { it.displayLabel })
        assertEquals(3, first.totalCount)
        assertEquals(2, first.nextOffset)
        val second = (repository.lookupArtistGroups(
            ArtistLookupRequest("alpha", ArtistLookupMode.CandidatePage,
                offset = first.nextOffset!!, limit = 2, expectedRevision = first.revision))
            as AppResult.Success).value
        assertEquals(listOf("Zulu"), second.entries.map { it.displayLabel })
        assertNull(second.nextOffset)
    }

    @Test fun staleExpectedRevisionFailsBeforeRoomIsRead() = runBlocking {
        val media = FakeAudio().apply { songs = listOf(audio(1, "Synthetic Artist")) }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val before = db.artistReads
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("synthetic artist", ArtistLookupMode.ExactNormalized,
                    expectedRevision = 999))))
        assertEquals(before, db.artistReads)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(repository.artistTrackRefs(
                ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("Synthetic Artist"), 999))))
        assertEquals(0, db.memberReads)
    }

    @Test fun membershipOnlyReturnsSelectedArtistInStableTitleRefOrder() = runBlocking {
        val media = FakeAudio().apply {
            songs = listOf(audio(1, "Alpha", "B"),
                audio(2, "Beta", "A"), audio(3, "Alpha", "A"))
        }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val lookup = (repository.lookupArtistGroups(
            ArtistLookupRequest("alpha", ArtistLookupMode.ExactNormalized))
            as AppResult.Success).value
        val members = (repository.artistTrackRefs(ArtistTrackRefsRequest(
            lookup.entries.single().id, lookup.revision, limit = 1,
        )) as AppResult.Success).value
        assertEquals(2, members.totalCount)
        assertEquals(listOf(MediaStoreRefCodec.encode(3)), members.refs)
        assertEquals(1, members.nextOffset)
        val next = (repository.artistTrackRefs(ArtistTrackRefsRequest(
            lookup.entries.single().id, lookup.revision, offset = 1, limit = 2,
        )) as AppResult.Success).value
        assertEquals(listOf(MediaStoreRefCodec.encode(1)), next.refs)
        assertNull(next.nextOffset)
    }

    @Test fun revocationDuringQueryFailsClosedEvenAfterPreviouslyReadyScan() = runBlocking {
        val media = FakeAudio().apply { songs = listOf(audio(1, "Alpha")) }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        db.readHook = { media.allowed = false }
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("alpha", ArtistLookupMode.ExactNormalized))))
        assertEquals(LibraryIndexStatus.Unavailable, repository.index.value.status)
    }

    @Test fun revokeThenRegrantEpochRejectsEvenUnchangedRevision() = runBlocking {
        val media = FakeAudio().apply { songs = listOf(audio(1, "Alpha")) }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        db.readHook = {
            repository.invalidateMediaAccess()
            media.allowed = true
        }
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("alpha", ArtistLookupMode.ExactNormalized))))
    }

    @Test fun memberReadsFailClosedIfPermissionVanishedDuringRead() = runBlocking {
        val media = FakeAudio().apply { songs = listOf(audio(1, "Alpha")) }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        db.readHook = { media.allowed = false }
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            failureCode(repository.artistTrackRefs(
                ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("Alpha"),
                    repository.index.value.revision))))
    }

    @Test fun acceptedRevisionCanGoStaleAfterFreshMediaReconciliation() = runBlocking {
        val media = FakeAudio().apply { songs = listOf(audio(1, "Alpha")) }
        val db = FakeProjection()
        val repository = repo(media, db)
        repository.reconcile()
        val old = repository.index.value.revision
        media.songs = listOf(audio(1, "Alpha"), audio(2, "Beta"))
        repository.reconcile()
        assertTrue(repository.index.value.revision > old)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(repository.lookupArtistGroups(
                ArtistLookupRequest("alpha", ArtistLookupMode.CandidatePage,
                    expectedRevision = old))))
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(repository.artistTrackRefs(
                ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("Alpha"), old))))
    }

    @Test fun opaqueArtistMetadataAndRefsMustNotLeakThroughDiagnostics() {
        val name = "Synthetic Private Artist"
        val req = ArtistLookupRequest("synthetic private artist", ArtistLookupMode.ExactNormalized)
        val group = LibraryGroupIdentity.artistId(name)
        val entry = ArtistIndexEntry(group, name, "synthetic private artist", 1)
        val page = ArtistLookupPage(listOf(entry), 1, 1L, null, 50)
        val memberReq = ArtistTrackRefsRequest(group, 1L)
        val memberPage = ArtistTrackRefsPage(
            listOf(MediaStoreRefCodec.encode(1)), 1, 1L, null, 50)
        for (value in listOf(req, entry, page, memberReq, memberPage)) {
            assertFalse(value.toString().contains(name))
            assertFalse(value.toString().contains(group.value))
            assertFalse(value.toString().contains("synthetic private artist"))
        }
    }

    @Test fun immutableCopyPreventsCallerFromMutatingPublishedCandidateList() {
        val e = ArtistIndexEntry(
            LibraryGroupIdentity.artistId("Alpha"), "Alpha", "alpha", 1)
        val mutable = mutableListOf(e)
        val page = ArtistLookupPage(mutable, 1, 1L, null, 50)
        mutable.clear()
        assertEquals(1, page.entries.size)
    }
}
