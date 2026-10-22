package com.latticeward.musiccopilot.foundation

import android.os.Bundle
import android.os.SystemClock
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
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
 * P11 source-only authored native perf tests. At P12 run with the complete
 * instrumentation APK on Android 10/12/14+/owner phone as available.
 *
 * 5,000 records are synthetic. No actual personal filename, identifier or
 * path is ever logged. Metrics are delivered as numbers in instrumentation
 * status, not invented in docs. No arbitrary latency cutoff is assumed;
 * actual release budgets require PRD/QA signoff from observed P95.
 */
@RunWith(AndroidJUnit4::class)
class Issue6Phase11FiveThousandRoomBenchmarkTest {
    private fun fixture(id: Long): ScannedAudio = ScannedAudio(
        ref = MediaStoreRefCodec.encode("external_primary", id),
        title = "Synthetic Track " + id.toString().padStart(5, '0'),
        artist = "Fixture Artist " + id.rem(80L),
        album = "Fixture Album " + id.rem(125L),
        durationMs = 90000L,
        volumeName = "external_primary",
        relativeFolderPath = "Music/Fixture/" + id.rem(50L) + "/",
        platformAlbumId = id.rem(125L),
        mimeType = "audio/mpeg",
    )

    private suspend fun <T> withFiveThousand(block: suspend (RoomLibraryProjection) -> T): T {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(
            context, MusicCopilotDatabase::class.java
        ).build()
        return try {
            val dao = db.libraryDao()
            // Insert only synthetic content into a private test database.
            for (part in (1L..5000L).chunked(250)) {
                dao.upsert(part.map(::fixture).map(LibraryRowMapper::fromSource))
            }
            block(RoomLibraryProjection(db))
        } finally {
            db.close()
        }
    }

    private fun record(name: String, elapsedMillis: Long) {
        // Numeric-only instrumentation measurement. No user media values.
        val metrics = Bundle()
        metrics.putLong("issue6_p11_" + name + "_elapsed_ms", elapsedMillis)
        InstrumentationRegistry.getInstrumentation().sendStatus(0, metrics)
    }

    @Test fun fiveThousandSongsPageInFiftyRowsWithStableNoDuplicateRefs() = runBlocking {
        withFiveThousand { projection ->
            val begin = SystemClock.elapsedRealtimeNanos()
            val visited = HashSet<String>(5000)
            for (page in 0..99) {
                val result = projection.browsePage(LibraryBrowseRequest(
                    LibraryBrowseCategory.Songs,
                    LibraryPageRequest(offset = page * 50, limit = 50),
                ), 2L)
                assertEquals(5000, result.totalCount)
                assertEquals(50, result.entries.size)
                for (row in result.entries.filterIsInstance<LibraryBrowseEntry.Song>()) {
                    assertTrue(visited.add(row.media.ref.value))
                }
                if (page == 99) assertNull(result.nextOffset)
                else assertEquals((page + 1) * 50, result.nextOffset)
            }
            val elapsed = (SystemClock.elapsedRealtimeNanos() - begin) / 1_000_000L
            assertEquals(5000, visited.size)
            record("browse_100_pages", elapsed)
        }
    }

    @Test fun fiveThousandSongSearchRemainsBoundedBySixty() = runBlocking {
        withFiveThousand { projection ->
            val began = SystemClock.elapsedRealtimeNanos()
            val r = projection.searchSurface("synthetic", 60, 2L)
            val ms = (SystemClock.elapsedRealtimeNanos() - began) / 1_000_000L
            assertEquals(60,r.limit)
            assertTrue(r.entries.size <= 60)
            assertTrue(r.entries.filterIsInstance<LibraryBrowseEntry.Song>().size <= 30)
            record("search_mixed_bounded",ms)
        }
    }

    @Test fun fiveThousandAlbumAndFolderGroupsStaySqlBounded() = runBlocking {
        withFiveThousand { projection ->
            for (category in listOf(LibraryBrowseCategory.Artists,
                LibraryBrowseCategory.Albums, LibraryBrowseCategory.Folders)) {
                val began = SystemClock.elapsedRealtimeNanos()
                val page = projection.browsePage(
                    LibraryBrowseRequest(category, LibraryPageRequest(limit = 50)),
                    acceptedRevision = 2L,
                )
                assertTrue(page.entries.size <= 50)
                assertTrue(page.totalCount > 0)
                record("group_" + category.name.lowercase(), 
                    (SystemClock.elapsedRealtimeNanos()-began)/1_000_000L)
            }
        }
    }
}
