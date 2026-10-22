package com.latticeward.musiccopilot.foundation

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.room.LibraryMigrations
import com.latticeward.musiccopilot.data.room.MusicCopilotDatabase
import com.latticeward.musiccopilot.data.room.RoomLibraryProjection
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Real Room V2 SQL results, using PRIVATE synthetic test data only.
 * AUTHORED and NOT_RUN until P12 Android/SQLite instrumentation.
 */
@RunWith(AndroidJUnit4::class)
class Issue6Phase9SearchSqlInstrumentedTest {
    private val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun source(
        id: Long,
        title: String,
        artist: String = "Aurora",
        album: String = "Aurora Dreams",
    ) = ScannedAudio(
        ref = MediaStoreRefCodec.encode("external_primary", id),
        title = title, artist = artist, album = album, durationMs = 1000L,
        volumeName = "external_primary",
        relativeFolderPath = "Music/Test/",
        platformAlbumId = 55L,
    )

    private suspend fun <T> withData(
        name: String,
        audio: List<ScannedAudio>,
        block: suspend (RoomLibraryProjection) -> T,
    ): T {
        appContext.deleteDatabase(name)
        val db = Room.databaseBuilder(
            appContext, MusicCopilotDatabase::class.java, name,
        ).addMigrations(LibraryMigrations.V1_TO_V2).build()
        return try {
            db.libraryDao().upsert(audio.map(LibraryRowMapper::fromSource))
            block(RoomLibraryProjection(db))
        } finally {
            db.close()
            appContext.deleteDatabase(name)
        }
    }

    @Test fun artistTrackAndAlbumResultsAreDistinctAndAllOffline() = runBlocking {
        withData("i6-p09-three-types.db", listOf(
            source(1, "Aurora Lights"),
            source(2, "Other Track"),
        )) { projection ->
            val r = projection.searchSurface("aurora", 60, 7L)
            assertEquals(7L, r.revision)
            assertTrue(r.entries.any { it is LibraryBrowseEntry.Artist })
            assertTrue(r.entries.any { it is LibraryBrowseEntry.Song })
            assertTrue(r.entries.any { it is LibraryBrowseEntry.Album })
            assertEquals(2, (r.entries.filterIsInstance<LibraryBrowseEntry.Artist>()
                .single()).songCount)
        }
    }

    @Test fun unknownQueryYieldsVerifiedEmptyNotError() = runBlocking {
        withData("i6-p09-empty.db", listOf(source(1, "Aurora Lights"))) { p ->
            val r = p.searchSurface("not-on-this-phone", 60, 7)
            assertTrue(r.entries.isEmpty())
            assertEquals(7L, r.revision)
        }
    }

    @Test fun sqlUsesLiteralPercentInsteadOfLikeWildcard() = runBlocking {
        withData("i6-p09-literal.db", listOf(
            source(1, "100% Synthetic", "Another Artist", "Album One"),
            source(2, "Ordinary Track", "Another Artist", "Album One"),
        )) { p ->
            val r = p.searchSurface("%", 60, 7L)
            assertEquals(1, r.entries.size)
            assertTrue(r.entries.single() is LibraryBrowseEntry.Song)
        }
    }

    @Test fun largeSyntheticIndexNeverReturnsMoreThanBoundedSixty() = runBlocking {
        val records = (1L..140L).map { source(it, "Aurora ${it}") }
        withData("i6-p09-bound.db", records) { p ->
            val r = p.searchSurface("aurora", 60, 7L)
            assertTrue(r.entries.isNotEmpty())
            assertTrue(r.entries.size <= 60)
            assertEquals(60, r.limit)
            assertTrue(r.entries.filterIsInstance<LibraryBrowseEntry.Song>().size <= 30)
        }
    }
}
