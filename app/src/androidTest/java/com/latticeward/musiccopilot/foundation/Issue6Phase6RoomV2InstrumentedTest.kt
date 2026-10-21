package com.latticeward.musiccopilot.foundation

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
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
 * P06 instrumented fixtures. AUTHORED, NOT EXECUTED until P12 T03/T04/T07.
 * No user's real MediaStore files; a private synthetic SQLite test database.
 */
@RunWith(AndroidJUnit4::class)
class Issue6Phase6RoomV2InstrumentedTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun source(
        id: Long, title: String, artist: String? = "Synthetic Artist",
        album: String? = "Synthetic Album",
        folder: String? = "Music/Synthetic/", volume: String = "external_primary",
        albumId: Long? = 77L,
    ): ScannedAudio = ScannedAudio(
        ref = MediaStoreRefCodec.encode(volume, id),
        title = title, artist = artist, album = album, durationMs = 1000L,
        volumeName = volume, relativeFolderPath = folder, platformAlbumId = albumId,
    )

    private fun database(name: String): MusicCopilotDatabase =
        Room.databaseBuilder(context, MusicCopilotDatabase::class.java, name)
            .addMigrations(LibraryMigrations.V1_TO_V2)
            .build()

    @Test fun nativeV1ToV2MigrationPreservesExistingRowsAndAddsNullDerivedFields() = runBlocking {
        val name = "issue6-p06-v1-migration.db"
        context.deleteDatabase(name)
        // Start with a *real SQLite V1* layout identical to the committed 1.json.
        val cfg = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(name)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS library_items (
                          mediaRef TEXT NOT NULL PRIMARY KEY,
                          title TEXT NOT NULL,
                          artist TEXT,
                          album TEXT,
                          durationMs INTEGER,
                          normalizedTitle TEXT NOT NULL,
                          normalizedArtist TEXT NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL(
                        "INSERT INTO library_items VALUES ('audio:7', 'Synthetic Legacy', " +
                            "'Fixture', 'Legacy Album', 2000, 'synthetic legacy', 'fixture')"
                    )
                    db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table " +
                        "(id INTEGER PRIMARY KEY, identity_hash TEXT)")
                    db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) " +
                        "VALUES(42, '7cfd18f79e2f1c7e07fd57f4427d4f33')")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    throw IllegalStateException("Only Room may upgrade the V1 fixture")
                }
            }).build()
        val fixture = FrameworkSQLiteOpenHelperFactory().create(cfg)
        fixture.writableDatabase
        fixture.close()
        val upgraded = database(name)
        try {
            val rows = upgraded.libraryDao().all()
            assertEquals(1, rows.size)
            val old = rows.single()
            assertEquals("audio:7", old.mediaRef)
            assertEquals("Synthetic Legacy", old.title)
            assertNull(old.volumeName)
            assertNull(old.artistGroupId)
            assertNull(old.albumGroupId)
            assertNull(old.folderGroupId)
            assertNull(old.metadataFingerprint)
            assertNull(old.indexedGeneration)
            assertNull(old.folderLabel)
        } finally {
            upgraded.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun fourCategoriesAndAlbumFolderMemberCountsComeFromBoundedSql() = runBlocking {
        val name = "issue6-p06-browse.db"
        context.deleteDatabase(name)
        val db = database(name)
        try {
            val rows = listOf(
                source(1, "Alpha", "Artist A", "Album A", "Music/A/", albumId = 11),
                source(2, "Beta", "Artist A", "Album A", "Music/A/", albumId = 11),
                source(3, "Gamma", "Artist B", "Album B", "Music/B/", albumId = 12),
            ).map(LibraryRowMapper::fromSource)
            db.libraryDao().upsert(rows)
            val p = RoomLibraryProjection(db)
            suspend fun page(category: LibraryBrowseCategory) =
                p.browsePage(LibraryBrowseRequest(category), 5L)
            assertEquals(3, page(LibraryBrowseCategory.Songs).totalCount)
            assertEquals(2, page(LibraryBrowseCategory.Artists).totalCount)
            assertEquals(2, page(LibraryBrowseCategory.Albums).totalCount)
            assertEquals(2, page(LibraryBrowseCategory.Folders).totalCount)
            val artists = page(LibraryBrowseCategory.Artists).entries
                .filterIsInstance<LibraryBrowseEntry.Artist>()
            val group = artists.first { it.label == "Artist A" }
            assertEquals(2, group.songCount)
            val members = p.browsePage(LibraryBrowseRequest(
                category = LibraryBrowseCategory.Artists,
                groupId = group.id,
            ), 5L)
            assertEquals(2, members.totalCount)
            assertEquals(2, members.entries.size)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun stableSqlPaginationNeverReturnsMoreThanRequestAndPreservesTieBreak() = runBlocking {
        val name = "issue6-p06-pagination.db"
        context.deleteDatabase(name)
        val db = database(name)
        try {
            db.libraryDao().upsert((1L..7L).map {
                LibraryRowMapper.fromSource(source(it, "Same Title"))
            })
            val p = RoomLibraryProjection(db)
            val first = p.browsePage(LibraryBrowseRequest(
                LibraryBrowseCategory.Songs, LibraryPageRequest(offset = 0, limit = 3)), 3L)
            val second = p.browsePage(LibraryBrowseRequest(
                LibraryBrowseCategory.Songs, LibraryPageRequest(offset = 3, limit = 3)), 3L)
            val third = p.browsePage(LibraryBrowseRequest(
                LibraryBrowseCategory.Songs, LibraryPageRequest(offset = 6, limit = 3)), 3L)
            assertEquals(7, first.totalCount)
            assertEquals(3, first.nextOffset)
            assertEquals(6, second.nextOffset)
            assertNull(third.nextOffset)
            val refs = (first.entries + second.entries + third.entries)
                .filterIsInstance<LibraryBrowseEntry.Song>().map { it.media.ref.value }
            assertEquals(7, refs.toSet().size)
            assertEquals(refs.sorted(), refs)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun titleArtistAlbumSearchIsLiteralAndBounded() = runBlocking {
        val name = "issue6-p06-search.db"
        context.deleteDatabase(name)
        val db = database(name)
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(source(1, "First Song", "One Artist", "Album X")),
                LibraryRowMapper.fromSource(source(2, "Second Song", "Other Artist", "Special Album")),
                LibraryRowMapper.fromSource(source(3, "Percent% Song", "Three", "Third")),
            ))
            val p = RoomLibraryProjection(db)
            assertEquals(1, p.searchLocal("first", 10).size)
            assertEquals(1, p.searchLocal("other", 10).size)
            assertEquals(1, p.searchLocal("special", 10).size)
            // Literal '%' must not become a SQLite LIKE wildcard.
            assertEquals(1, p.searchLocal("%", 10).size)
            assertEquals(1, p.searchLocal("song", 1).size)
            assertTrue(p.searchLocal("not-a-real-song", 10).isEmpty())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun nativeRoomV2CreatesExpectedIndexesAndSqlPlannerIsInspectable() = runBlocking {
        val name = "issue6-p06-indexes.db"
        context.deleteDatabase(name)
        val db = database(name)
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(source(1, "Synthetic Alpha"))
            ))
            val names = mutableSetOf<String>()
            db.openHelper.readableDatabase.query("PRAGMA index_list('library_items')")
                .use { cursor ->
                    val column = cursor.getColumnIndexOrThrow("name")
                    while (cursor.moveToNext()) names.add(cursor.getString(column))
                }
            assertTrue(names.contains("index_library_items_normalizedTitle_mediaRef"))
            assertTrue(names.contains("index_library_items_artistGroupId_normalizedTitle_mediaRef"))
            assertTrue(names.contains("index_library_items_albumGroupId_normalizedTitle_mediaRef"))
            assertTrue(names.contains("index_library_items_folderGroupId_normalizedTitle_mediaRef"))
            assertTrue(names.contains("index_library_items_indexedGeneration_mediaRef"))
            // The planner output is evidence to inspect at P12, not a
            // fabricated speed result or guarantee for small fixture sizes.
            db.openHelper.readableDatabase.query(
                "EXPLAIN QUERY PLAN SELECT * FROM library_items " +
                    "WHERE artistGroupId = 'synthetic-test' " +
                    "ORDER BY normalizedTitle, mediaRef LIMIT 50"
            ).use { cursor -> assertTrue(cursor.moveToFirst()) }
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun distinctMissingAndLiteralUnknownArtistRemainSeparateGroups() = runBlocking {
        val name = "issue6-p06-unknown.db"
        context.deleteDatabase(name)
        val db = database(name)
        try {
            db.libraryDao().upsert(listOf(
                LibraryRowMapper.fromSource(source(1, "One", artist = null)),
                LibraryRowMapper.fromSource(source(2, "Two", artist = "Unknown Artist")),
            ))
            assertEquals(2, RoomLibraryProjection(db).browsePage(
                LibraryBrowseRequest(LibraryBrowseCategory.Artists), 1L).totalCount)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
