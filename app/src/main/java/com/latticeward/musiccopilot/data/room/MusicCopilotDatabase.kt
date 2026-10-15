package com.latticeward.musiccopilot.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 1 only has disposable MediaStore projection rows. Do NOT add
 * destructive fallback when persistent user-owned Likes/history are introduced.
 */
@Database(entities = [LibraryRow::class], version = 1, exportSchema = true)
abstract class MusicCopilotDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
