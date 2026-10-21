package com.latticeward.musiccopilot.data.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Existing installs: Room V1 (7 columns) -> V2 additive derived metadata.
 * No file deletion, no table drop/rebuild, no destructive fallback.
 * Old rows deliberately retain null grouping fields until the next authorized
 * MediaStore reconcile. Database and source-verified UI must not infer Ready
 * from the migrated cache alone.
 *
 * Room wraps migrations in its own transaction. Individual SQL is idempotent
 * only for the one intended version edge; don't call manually twice.
 */
object LibraryMigrations {
    val V1_TO_V2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE library_items ADD COLUMN volumeName TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN relativeFolderPath TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN folderGroupId TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN artistGroupId TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN albumGroupId TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN normalizedAlbum TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN normalizedFolder TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN metadataFingerprint TEXT")
            db.execSQL("ALTER TABLE library_items ADD COLUMN indexedGeneration INTEGER")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_library_items_normalizedTitle_mediaRef " +
                "ON library_items (normalizedTitle, mediaRef)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_library_items_artistGroupId_normalizedTitle_mediaRef " +
                "ON library_items (artistGroupId, normalizedTitle, mediaRef)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_library_items_albumGroupId_normalizedTitle_mediaRef " +
                "ON library_items (albumGroupId, normalizedTitle, mediaRef)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_library_items_folderGroupId_normalizedTitle_mediaRef " +
                "ON library_items (folderGroupId, normalizedTitle, mediaRef)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_library_items_indexedGeneration_mediaRef " +
                "ON library_items (indexedGeneration, mediaRef)")
        }
    }
}
