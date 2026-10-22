package com.latticeward.musiccopilot.data.api

import com.latticeward.musiccopilot.core.model.MediaRef
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Issue #6 P02: presentation-independent contract for the FOUR local browse views.
 * Actual queries are implemented behind LibraryRepository in P06. No playback
 * queue, MediaStore cursor, Room DAO or Android UI type crosses this boundary.
 */
enum class LibraryBrowseCategory { Songs, Artists, Albums, Folders }

/** Local opaque grouping token. Sensitive even if hashed: NEVER log or send to telemetry. */
@JvmInline
value class LibraryGroupId(val value: String)

/**
 * Bounded offset-based page. The optional revision pins the request to a
 * previously observed LibraryIndexSnapshot. P06 MUST fail closed (typed error)
 * if an index revision changed, rather than mixing pages from two snapshots.
 */
data class LibraryPageRequest(
    val offset: Int = 0,
    val limit: Int = DEFAULT_PAGE_SIZE,
    val expectedRevision: Long? = null,
) {
    init {
        require(offset >= 0) { "offset must be non-negative" }
        require(limit in 1..MAX_PAGE_SIZE) { "limit out of range" }
        require(expectedRevision == null || expectedRevision >= 0) {
            "revision must be non-negative"
        }
    }

    companion object {
        const val DEFAULT_PAGE_SIZE: Int = 50
        const val MAX_PAGE_SIZE: Int = 100
    }
}

/**
 * No groupId: category listing. An Artists/Albums/Folders groupId asks for that
 * group's member Songs (same cursor and revision semantics as the category).
 * Songs with a groupId is invalid; presentation never manufactures a queue.
 */
data class LibraryBrowseRequest(
    val category: LibraryBrowseCategory,
    val page: LibraryPageRequest = LibraryPageRequest(),
    val groupId: LibraryGroupId? = null,
) {
    init {
        require(groupId == null || category != LibraryBrowseCategory.Songs) {
            "Songs does not accept a groupId"
        }
        require(groupId == null || groupId.value.isNotBlank()) {
            "groupId must not be blank"
        }
    }
}

/** Four closed projections; a group request returns Song entries for its members. */
sealed interface LibraryBrowseEntry {
    data class Song(val media: LibraryItem) : LibraryBrowseEntry

    data class Artist(
        val id: LibraryGroupId,
        val label: String,
        val songCount: Int,
    ) : LibraryBrowseEntry {
        init { require(songCount >= 0) }
    }

    data class Album(
        val id: LibraryGroupId,
        val label: String,
        val artistLabel: String?,
        val songCount: Int,
    ) : LibraryBrowseEntry {
        init { require(songCount >= 0) }
    }

    data class Folder(
        val id: LibraryGroupId,
        val label: String,
        val songCount: Int,
    ) : LibraryBrowseEntry {
        init { require(songCount >= 0) }
    }
}

/**
 * Query result is only a derived projection; no row/page proves existence or
 * playability. Selection still calls LibraryRepository.get(ref) for re-query.
 */
data class LibraryBrowsePage(
    val entries: List<LibraryBrowseEntry>,
    val totalCount: Int,
    val revision: Long,
    val nextOffset: Int?,
) {
    init {
        require(totalCount >= 0)
        require(revision >= 0)
        require(entries.size <= LibraryPageRequest.MAX_PAGE_SIZE)
        require(nextOffset == null || nextOffset in 0..totalCount)
    }
}

/**
 * Stable grouping contract, independent of SQLite and Android. P03 supplies
 * MediaStore ALBUM_ID and VOLUME_NAME / RELATIVE_PATH where available.
 * Artist key: normalized artist. Album key: volume+platform album ID, or
 * normalized album+artist fallback. Folder key: volume+full relative path.
 *
 * IDs are SHA-256 derived for locality and do not expose raw names/paths to UI
 * navigation; they remain private and are prohibited in analytics/crash data.
 * Unknown sentinel is distinct from a literal user-owned "Unknown Artist".
 */
object LibraryGroupIdentity {
    const val UNKNOWN_ARTIST: String = "Unknown Artist"
    const val UNKNOWN_ALBUM: String = "Unknown Album"
    const val UNKNOWN_FOLDER: String = "Unknown Folder"

    // Shared P04 keys for Browse, Room and Search; no device locale drift.
    fun normalize(input: String?): String? =
        LibraryTextNormalization.display(input)?.let { LibraryTextNormalization.key(it) }

    fun artistId(artist: String?): LibraryGroupId =
        digest("artist", identityPart(normalize(artist)))

    fun albumId(
        album: String?,
        artist: String?,
        volumeName: String?,
        platformAlbumId: Long? = null,
    ): LibraryGroupId {
        val volume = normalize(volumeName) ?: "unknown-volume"
        // Missing metadata must not accidentally merge with a literal
        // user-authored "Unknown Album" simply because a provider reused an ID.
        val albumIdentity = if (normalize(album) != null &&
            platformAlbumId != null && platformAlbumId >= 0) {
            listOf("platform", volume, platformAlbumId.toString())
        } else {
            listOf("fallback", volume, identityPart(normalize(album)),
                identityPart(normalize(artist)))
        }
        return digest("album", *albumIdentity.toTypedArray())
    }

    /**
     * Android 10+ RELATIVE_PATH is a media-relative directory path, not an
     * absolute filesystem path. Keep the whole normalized relative path as
     * grouping input; display just the last component. Do not log either.
     */
    fun folderId(volumeName: String?, relativePath: String?): LibraryGroupId =
        digest("folder", normalize(volumeName) ?: "unknown-volume",
            identityPart(normalizeRelativePath(relativePath)))

    fun folderLabel(relativePath: String?): String =
        LibraryTextNormalization.folderLabel(relativePath)

    private fun identityPart(value: String?): String =
        if (value == null) "missing" else "present:$value"

    private fun normalizeRelativePath(value: String?): String? =
        // Actual Android folder paths are case/spacing sensitive. Never
        // merge "Live  Session" with "Live Session" or Rock with rock.
        LibraryTextNormalization.relativeFolder(value)

    private fun digest(kind: String, vararg components: String): LibraryGroupId {
        // A length-prefixed encoding prevents ambiguous tuples or separator
        // collisions. No personal strings appear in the resulting token.
        val payload = (listOf(kind) + components).joinToString("") {
            "${it.length}:$it"
        }
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(payload.toByteArray(StandardCharsets.UTF_8))
        val hex = "0123456789abcdef"
        val out = StringBuilder(kind.length + 1 + bytes.size * 2)
        out.append(kind).append(':')
        for (byte in bytes) {
            val unsigned = byte.toInt() and 0xff
            out.append(hex[unsigned ushr 4]).append(hex[unsigned and 0x0f])
        }
        return LibraryGroupId(out.toString())
    }
}

/** Stable sort order required for P06 and the 5,000-row test in P12. */
object LibraryBrowseOrdering {
    /** Tie-break on the opaque local source ref, never on a personal filename. */
    fun songs(items: List<LibraryItem>): List<LibraryItem> =
        items.sortedWith(
            compareBy<LibraryItem> { LibraryGroupIdentity.normalize(it.title).orEmpty() }
                .thenBy { it.ref.value },
        )
}
