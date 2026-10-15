package com.latticeward.musiccopilot.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    @Query("SELECT * FROM library_items ORDER BY title COLLATE NOCASE, mediaRef")
    fun observeAll(): Flow<List<LibraryRow>>

    @Query("SELECT * FROM library_items ORDER BY title COLLATE NOCASE, mediaRef")
    suspend fun all(): List<LibraryRow>

    @Query("SELECT * FROM library_items WHERE mediaRef = :ref LIMIT 1")
    suspend fun find(ref: String): LibraryRow?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(items: List<LibraryRow>)

    @Query("DELETE FROM library_items")
    suspend fun clearDerived()
}
