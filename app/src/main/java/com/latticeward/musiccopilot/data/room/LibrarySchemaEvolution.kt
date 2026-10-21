package com.latticeward.musiccopilot.data.room

/**
 * Issue #6 P02 migration specification, NOT an executed Room migration.
 *
 * Keep Room @Database(version=1), LibraryRow and exported 1.json unchanged in
 * P02: changing any one without the others would make existing installations
 * fail to open. P06 atomically delivers the V1_TO_V2 migration, annotated V2
 * row/indices, databaseBuilder registration and generated 2.json.
 */
internal object LibrarySchemaEvolution {
    const val INSTALLED_SCHEMA_VERSION: Int = 1
    const val PLANNED_BROWSE_SCHEMA_VERSION: Int = 2
    const val STATUS: String = "PLAN_ONLY_P06_IMPLEMENTATION_REQUIRED"

    // New V2 columns on DERIVED library_items; nullable initially so existing
    // v1 data survives. Reconcile P05/P06 fills values only after OS grant.
    val plannedDerivedColumns: List<String> = listOf(
        "volumeName",
        "relativeFolderPath",
        "folderGroupId",
        "artistGroupId",
        "albumGroupId",
        "normalizedAlbum",
        "normalizedFolder",
        "metadataFingerprint",
        "indexedGeneration",
    )

    // Logical query access patterns, NOT an assertion that indexes already
    // exist. '%substring%' needs a measured search strategy/FTS or bounded
    // scan; SQLite B-tree alone cannot optimize a leading wildcard.
    val requiredQueryPatterns: List<String> = listOf(
        "Songs: normalizedTitle, mediaRef",
        "Artists: artistGroupId, normalizedTitle, mediaRef",
        "Albums: albumGroupId, normalizedTitle, mediaRef",
        "Folders: folderGroupId, normalizedTitle, mediaRef",
        "Search: normalizedTitle / normalizedArtist / normalizedAlbum, bounded",
        "Reconciliation: indexedGeneration, mediaRef",
    )

    const val MIGRATION_POLICY =
        "V1_TO_V2 must be transactionally registered with @Database(version=2), " +
            "Room entity and generated schema; no destructive fallback, no deletion " +
            "of app-owned tables or user audio, and prior index must be " +
            "permission-revalidated before publishing Ready."

    const val ROLLBACK_POLICY =
        "Never downgrade a released V2 database by force-clearing it. " +
            "Use a forward repair migration or gate an older binary before " +
            "rollback; MediaStore remains the audio authority."
}
