package com.latticeward.musiccopilot

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.data.api.ArtistLookupMode
import com.latticeward.musiccopilot.data.api.ArtistLookupRequest
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.room.MusicCopilotDatabase
import com.latticeward.musiccopilot.data.room.RoomLibraryProjection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Issue #11 P03 Room/SQLite SQL integration fixtures.
 * AUTHORED ONLY: do NOT run, build or compile before Issue #11 P13.
 * Synthetic local metadata; no real device user's media is examined.
 */
@RunWith(AndroidJUnit4::class)
class Issue11Phase3RoomArtistQueryTest {
    private fun synthetic(id: Long, title: String, artist: String?): ScannedAudio =
        ScannedAudio(MediaStoreRefCodec.encode(id), title, artist, "Synthetic Album", 1000L)

    private fun memoryDb(): MusicCopilotDatabase =
        Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MusicCopilotDatabase::class.java,
        ).build()

    @Test fun roomExactLookupUsesArtistIdentityNotSongTitles() = runBlocking {
        val db = memoryDb()
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(synthetic(1L, "Adele", "Local Artist")),
                LibraryRowMapper.fromSource(synthetic(2L, "Synthetic Track", "Local Artist")),
                LibraryRowMapper.fromSource(synthetic(3L, "Different", "Other Artist")),
            ))
            val projection = RoomLibraryProjection(db)
            val title = projection.lookupArtistGroups(
                ArtistLookupRequest("adele", ArtistLookupMode.ExactNormalized), 7L)
            assertTrue(title.entries.isEmpty())
            val exact = projection.lookupArtistGroups(
                ArtistLookupRequest("local artist", ArtistLookupMode.ExactNormalized), 7L)
            assertEquals(1, exact.totalCount)
            assertEquals("Local Artist", exact.entries.single().displayLabel)
            assertEquals(2, exact.entries.single().indexedSongCount)
            assertEquals(7L, exact.revision)
        } finally { db.close() }
    }

    @Test fun roomCandidatePagesStableAndNoMissingArtistMetadata() = runBlocking {
        val db = memoryDb()
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(synthetic(1L, "One", "Zulu")),
                LibraryRowMapper.fromSource(synthetic(2L, "Two", "Alpha")),
                LibraryRowMapper.fromSource(synthetic(3L, "Three", "Beta")),
                LibraryRowMapper.fromSource(synthetic(4L, "Four", null)),
                LibraryRowMapper.fromSource(synthetic(5L, "Five", " ")),
                LibraryRowMapper.fromSource(synthetic(6L, "Six", "Alpha")),
            ))
            val projection = RoomLibraryProjection(db)
            val first = projection.lookupArtistGroups(
                ArtistLookupRequest("alpha", ArtistLookupMode.CandidatePage, limit = 2), 7L)
            assertEquals(3, first.totalCount)
            assertEquals(listOf("Alpha", "Beta"), first.entries.map { it.displayLabel })
            assertEquals(2, first.nextOffset)
            val second = projection.lookupArtistGroups(
                ArtistLookupRequest("alpha", ArtistLookupMode.CandidatePage,
                    offset = first.nextOffset!!, limit = 2, expectedRevision = 7L), 7L)
            assertEquals(listOf("Zulu"), second.entries.map { it.displayLabel })
            assertNull(second.nextOffset)
            assertEquals(2, first.entries.first().indexedSongCount)
        } finally { db.close() }
    }

    @Test fun roomSongRefsAreBoundedAndStrictlyArtistScoped() = runBlocking {
        val db = memoryDb()
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(synthetic(1L, "Z Track", "Alpha")),
                LibraryRowMapper.fromSource(synthetic(2L, "A Track", "Alpha")),
                LibraryRowMapper.fromSource(synthetic(3L, "B Track", "Beta")),
                LibraryRowMapper.fromSource(synthetic(4L, "A Track", "Alpha")),
            ))
            val projection = RoomLibraryProjection(db)
            val groupId = LibraryGroupIdentity.artistId("alpha")
            val first = projection.artistTrackRefs(
                ArtistTrackRefsRequest(groupId, 9L, limit = 2), 9L)
            assertEquals(3, first.totalCount)
            assertEquals(listOf(MediaStoreRefCodec.encode(2L), MediaStoreRefCodec.encode(4L)),
                first.refs)
            assertEquals(2, first.nextOffset)
            val second = projection.artistTrackRefs(
                ArtistTrackRefsRequest(groupId, 9L, offset = 2, limit = 2), 9L)
            assertEquals(listOf(MediaStoreRefCodec.encode(1L)), second.refs)
            assertNull(second.nextOffset)
            val missing = projection.artistTrackRefs(
                ArtistTrackRefsRequest(LibraryGroupIdentity.artistId("no such artist"),
                    9L), 9L)
            assertTrue(missing.refs.isEmpty())
        } finally { db.close() }
    }

    @Test fun roomLiteralUnknownArtistRemainsDifferentFromMissingMetadata() = runBlocking {
        val db = memoryDb()
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(synthetic(1L, "A", null)),
                LibraryRowMapper.fromSource(synthetic(2L, "B", "Unknown Artist")),
            ))
            val projection = RoomLibraryProjection(db)
            val page = projection.lookupArtistGroups(
                ArtistLookupRequest("unknown artist", ArtistLookupMode.ExactNormalized), 11L)
            assertEquals(1, page.entries.size)
            assertEquals(LibraryGroupIdentity.artistId("Unknown Artist"),
                page.entries.single().id)
            assertNotEquals(LibraryGroupIdentity.artistId(null), page.entries.single().id)
        } finally { db.close() }
    }
}
