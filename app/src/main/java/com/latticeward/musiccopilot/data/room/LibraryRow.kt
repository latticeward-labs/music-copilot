package com.latticeward.musiccopilot.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Derived, disposable projection only; never a live playback queue. */
@Entity(tableName = "library_items")
data class LibraryRow(
    @PrimaryKey val mediaRef: String,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
    val normalizedTitle: String,
    val normalizedArtist: String,
)

