package com.latticeward.musiccopilot.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** A single summary of one derived artist/album/folder grouping. */
data class LibraryGroupCountRow(
    val groupId: String,
    val label: String,
    val artistLabel: String?,
    val songCount: Int,
)

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items ORDER BY normalizedTitle, mediaRef")
    fun observeAll(): Flow<List<LibraryRow>>

    @Query("SELECT * FROM library_items ORDER BY normalizedTitle, mediaRef")
    suspend fun all(): List<LibraryRow>

    @Query("SELECT * FROM library_items WHERE mediaRef = :ref LIMIT 1")
    suspend fun find(ref: String): LibraryRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<LibraryRow>)

    /** P05 row-wise stale deletion, 400 keys/batch under one transaction. */
    @Query("DELETE FROM library_items WHERE mediaRef IN (:refs)")
    suspend fun deleteByRefs(refs: List<String>)

    @Query("DELETE FROM library_items")
    suspend fun clearDerived()

    // Song and members: stable title/ref ordering and SQLite-enforced limits.
    @Query("SELECT COUNT(*) FROM library_items")
    suspend fun songCount(): Int

    @Query("SELECT * FROM library_items ORDER BY normalizedTitle, mediaRef LIMIT :limit OFFSET :offset")
    suspend fun songPage(limit: Int, offset: Int): List<LibraryRow>

    @Query("SELECT * FROM library_items WHERE artistGroupId = :groupId ORDER BY normalizedTitle, mediaRef LIMIT :limit OFFSET :offset")
    suspend fun artistSongs(groupId: String, limit: Int, offset: Int): List<LibraryRow>

    @Query("SELECT COUNT(*) FROM library_items WHERE artistGroupId = :groupId")
    suspend fun artistSongCount(groupId: String): Int

    @Query("SELECT * FROM library_items WHERE albumGroupId = :groupId ORDER BY normalizedTitle, mediaRef LIMIT :limit OFFSET :offset")
    suspend fun albumSongs(groupId: String, limit: Int, offset: Int): List<LibraryRow>

    @Query("SELECT COUNT(*) FROM library_items WHERE albumGroupId = :groupId")
    suspend fun albumSongCount(groupId: String): Int

    @Query("SELECT * FROM library_items WHERE folderGroupId = :groupId ORDER BY normalizedTitle, mediaRef LIMIT :limit OFFSET :offset")
    suspend fun folderSongs(groupId: String, limit: Int, offset: Int): List<LibraryRow>

    @Query("SELECT COUNT(*) FROM library_items WHERE folderGroupId = :groupId")
    suspend fun folderSongCount(groupId: String): Int

    // Group listings are aggregated in SQLite, never by hydrating 5,000
    // LibraryRow objects for each tab. Old V1 migrated NULL groups are hidden
    // until MediaStore has revalidated them on explicit permission-gated scan.
    @Query("SELECT COUNT(DISTINCT artistGroupId) FROM library_items WHERE artistGroupId IS NOT NULL")
    suspend fun artistCount(): Int

    @Query("""
        SELECT artistGroupId AS groupId,
               MIN(COALESCE(artist, 'Unknown Artist')) AS label,
               CAST(NULL AS TEXT) AS artistLabel,
               COUNT(*) AS songCount
        FROM library_items
        WHERE artistGroupId IS NOT NULL
        GROUP BY artistGroupId
        ORDER BY MIN(normalizedArtist), groupId
        LIMIT :limit OFFSET :offset
    """)
    suspend fun artists(limit: Int, offset: Int): List<LibraryGroupCountRow>

    @Query("SELECT COUNT(DISTINCT albumGroupId) FROM library_items WHERE albumGroupId IS NOT NULL")
    suspend fun albumCount(): Int

    @Query("""
        SELECT albumGroupId AS groupId,
               MIN(COALESCE(album, 'Unknown Album')) AS label,
               CASE WHEN COUNT(DISTINCT COALESCE(artist, '<unknown>')) > 1
                    THEN 'Various Artists' ELSE MIN(artist) END AS artistLabel,
               COUNT(*) AS songCount
        FROM library_items
        WHERE albumGroupId IS NOT NULL
        GROUP BY albumGroupId
        ORDER BY MIN(COALESCE(normalizedAlbum, '')), groupId
        LIMIT :limit OFFSET :offset
    """)
    suspend fun albums(limit: Int, offset: Int): List<LibraryGroupCountRow>

    @Query("SELECT COUNT(DISTINCT folderGroupId) FROM library_items WHERE folderGroupId IS NOT NULL")
    suspend fun folderCount(): Int

    @Query("""
        SELECT folderGroupId AS groupId,
               MIN(COALESCE(folderLabel, 'Unknown Folder')) AS label,
               CAST(NULL AS TEXT) AS artistLabel,
               COUNT(*) AS songCount
        FROM library_items
        WHERE folderGroupId IS NOT NULL
        GROUP BY folderGroupId
        ORDER BY MIN(COALESCE(normalizedFolder, '')), groupId
        LIMIT :limit OFFSET :offset
    """)
    suspend fun folders(limit: Int, offset: Int): List<LibraryGroupCountRow>

    /**
     * Query substring with literal instr(), not LIKE wildcard injection.
     * A leading substring still scans up to N=5k indexed rows; it avoids
     * pulling every row into Kotlin or building a false O(log n) claim.
     * P12 T04/T07 must benchmark EXPLAIN QUERY PLAN and actual latency.
     */
    @Query("""
        SELECT * FROM library_items
        WHERE instr(normalizedTitle, :needle) > 0
           OR instr(normalizedArtist, :needle) > 0
           OR instr(COALESCE(normalizedAlbum, ''), :needle) > 0
        ORDER BY normalizedTitle, mediaRef
        LIMIT :limit
    """)
    suspend fun searchLocal(needle: String, limit: Int): List<LibraryRow>
}
