package com.latticeward.musiccopilot.data.mapping

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.room.LibraryRow
import java.util.Locale

/** Deterministic app-internal search keys; never emit title/artist to telemetry. */
object LibraryRowMapper {
    fun normalize(value: String): String =
        value.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

    fun fromSource(media: ScannedAudio): LibraryRow = LibraryRow(
        mediaRef = media.ref.value,
        title = media.title,
        artist = media.artist,
        album = media.album,
        durationMs = media.durationMs,
        normalizedTitle = normalize(media.title),
        normalizedArtist = normalize(media.artist.orEmpty()),
    )

    fun toItem(row: LibraryRow): LibraryItem =
        LibraryItem(MediaRef(row.mediaRef), row.title, row.artist, row.album, row.durationMs)
}

