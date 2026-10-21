package com.latticeward.musiccopilot.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room V2 is a disposable, permission-gated MediaStore projection.
 * Nullable V2 columns preserve every V1 row without inventing folder/album
 * identities; a fresh authorized reconciliation populates derived fields.
 */
@Entity(
    tableName = "library_items",
    indices = [
        Index(value = ["normalizedTitle", "mediaRef"]),
        Index(value = ["artistGroupId", "normalizedTitle", "mediaRef"]),
        Index(value = ["albumGroupId", "normalizedTitle", "mediaRef"]),
        Index(value = ["folderGroupId", "normalizedTitle", "mediaRef"]),
        Index(value = ["indexedGeneration", "mediaRef"]),
    ],
)
data class LibraryRow(
    @PrimaryKey val mediaRef: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
    val normalizedTitle: String,
    val normalizedArtist: String,
    // Trailing defaults maintain existing JVM fixtures and V1 source calls.
    val volumeName: String? = null,
    val relativeFolderPath: String? = null,
    val folderGroupId: String? = null,
    val artistGroupId: String? = null,
    val albumGroupId: String? = null,
    val normalizedAlbum: String? = null,
    val normalizedFolder: String? = null,
    val metadataFingerprint: String? = null,
    val indexedGeneration: Long? = null,
)
