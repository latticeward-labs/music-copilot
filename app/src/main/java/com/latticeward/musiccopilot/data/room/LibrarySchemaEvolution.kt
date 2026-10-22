package com.latticeward.musiccopilot.data.room

/**
 * The actual P06 Room V2 source contract. `LibraryMigrations.V1_TO_V2`
 * is registered on the production database builder. Native Room annotation
 * processor schema export (2.json) and migrated SQLite verification are
 * still NOT_RUN until P12 build/test; do not assert exported/validated yet.
 */
internal object LibrarySchemaEvolution {
    const val INSTALLED_SCHEMA_VERSION: Int = 2
    const val PREVIOUS_SCHEMA_VERSION: Int = 1
    const val STATUS: String = "V2_SOURCE_IMPLEMENTED_SCHEMA_EXPORT_PENDING_KAPT_P12"

    // Additive derived columns, nullable for V1 migration without data loss.
    val derivedColumns: List<String> = listOf(
        "volumeName", "relativeFolderPath",
        "folderGroupId", "artistGroupId", "albumGroupId",
        "normalizedAlbum", "normalizedFolder", "folderLabel",
        "metadataFingerprint", "indexedGeneration",
    )

    val sqlIndexes: List<String> = listOf(
        "index_library_items_normalizedTitle_mediaRef",
        "index_library_items_artistGroupId_normalizedTitle_mediaRef",
        "index_library_items_albumGroupId_normalizedTitle_mediaRef",
        "index_library_items_folderGroupId_normalizedTitle_mediaRef",
        "index_library_items_indexedGeneration_mediaRef",
    )

    const val MIGRATION_POLICY =
        "V1_TO_V2 preserves all old rows; nullable derived fields are populated " +
            "only by authorized MediaStore reconcile. Android Room KAPT 2.json " +
            "and native migrations must be verified at P12, no destructive fallback."

    const val ROLLBACK_POLICY =
        "Never install a V1 APK over a released V2 database or delete user audio; " +
            "fix-forward or use a reviewed non-destructive schema strategy."
}
