package com.latticeward.musiccopilot.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 2 adds nullable grouped browse/search fields to the disposable MediaStore projection. Do NOT add
 * destructive fallback when persistent user-owned Likes/history are introduced.
 */
@Database(entities = [LibraryRow::class], version = 2, exportSchema = true)
abstract class MusicCopilotDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
}
