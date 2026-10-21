package com.latticeward.musiccopilot.data.mapping

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.room.LibraryRow
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * P04 pure derived view, not a second source of truth. Group tokens are internal
 * and must never be published as telemetry identifiers.
 * P06 persists the additional fields with a registered Room v1 -> v2 migration.
 */
data class NormalizedLibraryMetadata(
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String,
    val volumeName: String?,
    val relativeFolderPath: String?,
    val folderLabel: String,
    val artistGroupId: LibraryGroupId,
    val albumGroupId: LibraryGroupId,
    val folderGroupId: LibraryGroupId,
)

/** Deterministic app-internal projection. Never log input or output strings. */
object LibraryRowMapper {
    /** Existing public normalization helper remains source-compatible. */
    fun normalize(value: String): String = LibraryTextNormalization.key(value)

    /** Preserve source identity and missing-vs-literal unknown semantics. */
    fun normalizeMetadata(media: ScannedAudio): NormalizedLibraryMetadata {
        val title = LibraryTextNormalization.title(media.title)
        val artist = LibraryTextNormalization.display(media.artist)
        val album = LibraryTextNormalization.display(media.album)
        val volume = media.volumeName?.takeIf(MediaStoreRefCodec::isSpecificVolume)
        val folder = LibraryTextNormalization.relativeFolder(media.relativeFolderPath)
        return NormalizedLibraryMetadata(
            title = title,
            artist = artist,
            album = album,
            durationMs = LibraryTextNormalization.durationMs(media.durationMs),
            normalizedTitle = LibraryTextNormalization.key(title),
            normalizedArtist = LibraryTextNormalization.key(artist),
            normalizedAlbum = LibraryTextNormalization.key(album),
            volumeName = volume,
            relativeFolderPath = folder,
            folderLabel = LibraryTextNormalization.folderLabel(folder),
            artistGroupId = LibraryGroupIdentity.artistId(artist),
            albumGroupId = LibraryGroupIdentity.albumId(
                album, artist, volume, media.platformAlbumId?.takeIf { it >= 0L },
            ),
            folderGroupId = LibraryGroupIdentity.folderId(volume, folder),
        )
    }

    /** Full Room V2 derived row; legacy seven-argument callers remain valid. */
    fun fromSource(media: ScannedAudio): LibraryRow {
        val metadata = normalizeMetadata(media)
        return LibraryRow(
            mediaRef = media.ref.value,
            title = metadata.title,
            artist = metadata.artist,
            album = metadata.album,
            durationMs = metadata.durationMs,
            normalizedTitle = metadata.normalizedTitle,
            normalizedArtist = metadata.normalizedArtist,
            volumeName = metadata.volumeName,
            relativeFolderPath = metadata.relativeFolderPath,
            folderGroupId = metadata.folderGroupId.value,
            artistGroupId = metadata.artistGroupId.value,
            albumGroupId = metadata.albumGroupId.value,
            normalizedAlbum = metadata.normalizedAlbum,
            normalizedFolder = LibraryTextNormalization.key(metadata.folderLabel),
            folderLabel = metadata.folderLabel,
            metadataFingerprint = metadataFingerprint(media, metadata),
            indexedGeneration = media.generationModified?.takeIf { it >= 0L },
        )
    }

    /**
     * Sensitive local-only content fingerprint to detect provider metadata
     * changes. SHA tokens, raw metadata and refs MUST NOT be logged, exported
     * or sent to telemetry. Full source scan still determines deletions.
     */
    private fun metadataFingerprint(
        media: ScannedAudio,
        metadata: NormalizedLibraryMetadata,
    ): String {
        val parts = listOf(
            metadata.title,
            metadata.artist.orEmpty(),
            metadata.album.orEmpty(),
            metadata.durationMs?.toString().orEmpty(),
            metadata.volumeName.orEmpty(),
            metadata.relativeFolderPath.orEmpty(),
            media.platformAlbumId?.toString().orEmpty(),
            media.mimeType.orEmpty(),
            media.sizeBytes?.toString().orEmpty(),
            media.dateModifiedSeconds?.toString().orEmpty(),
            media.generationModified?.toString().orEmpty(),
        )
        val payload = parts.joinToString("") { "${it.length}:$it" }
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
        val hex = "0123456789abcdef"
        return buildString(bytes.size * 2) {
            for (byte in bytes) {
                val n = byte.toInt() and 0xff
                append(hex[n ushr 4])
                append(hex[n and 0x0f])
            }
        }
    }

    fun toItem(row: LibraryRow): LibraryItem =
        LibraryItem(MediaRef(row.mediaRef), row.title, row.artist, row.album, row.durationMs)

    /**
     * Fold only identical platform references, NEVER display names.
     * The same data in any provider scan order selects the same winner. More
     * complete metadata wins; ties sort on deterministic normalized/raw fields.
     * Noncanonical/malformed source refs are not indexed.
     *
     * This is not incremental reconciliation: P05 still owns changed-row
     * detection, transactional delta writes, race/cancel and scan generation.
     */
    fun deriveIndexRows(scanned: List<ScannedAudio>): List<LibraryRow> =
        scanned.asSequence()
            .filter { MediaStoreRefCodec.decodeScoped(it.ref) != null }
            .map(::fromSource)
            .sortedWith(preferredSourceRow)
            .distinctBy { it.mediaRef }
            .sortedWith(compareBy<LibraryRow> { it.normalizedTitle }.thenBy { it.mediaRef })
            .toList()

    private val preferredSourceRow: Comparator<LibraryRow> =
        compareByDescending<LibraryRow> {
            it.title != LibraryTextNormalization.UNKNOWN_TITLE
        }
            .thenByDescending { it.artist != null }
            .thenByDescending { it.album != null }
            .thenByDescending { it.durationMs != null }
            .thenBy { it.normalizedTitle }
            .thenBy { it.normalizedArtist }
            .thenBy { LibraryTextNormalization.key(it.album) }
            .thenBy { it.title }
            .thenBy { it.artist.orEmpty() }
            .thenBy { it.album.orEmpty() }
            .thenBy { it.durationMs ?: Long.MAX_VALUE }
            .thenBy { it.metadataFingerprint.orEmpty() }
}
