package com.latticeward.musiccopilot.data.api

import com.latticeward.musiccopilot.core.model.MediaRef
import java.util.Collections

/**
 * Issue #11 P03: private, bounded artist-IDENTITY lookup, not title search.
 *
 * Room is a derived MediaStore index. Returned groups and references do NOT
 * establish media playability. P04 scores groups; P06 must re-query each chosen
 * MediaRef through LibraryRepository.get() before PlaybackGateway dispatch.
 *
 * Artist labels, opaque group IDs and refs are PRIVATE device data: never log,
 * persist in Voice history, emit to Quality, or put into error strings.
 */
enum class ArtistLookupMode {
    /** SQL equality over canonical normalizedArtist, before fuzzy scoring. */
    ExactNormalized,
    /** Stable, paginated list of real artist groups for P04 bounded scoring. */
    CandidatePage,
}

data class ArtistLookupRequest(
    val normalizedTarget: String,
    val mode: ArtistLookupMode,
    val offset: Int = 0,
    val limit: Int = DEFAULT_PAGE_SIZE,
    val expectedRevision: Long? = null,
) {
    init {
        require(normalizedTarget.isNotBlank() &&
            normalizedTarget.length <= MAX_TARGET_CHARS &&
            LibraryTextNormalization.key(normalizedTarget) == normalizedTarget
        ) { "target must be a bounded, canonical lookup key" }
        require(offset >= 0 && offset <= Int.MAX_VALUE - MAX_PAGE_SIZE)
        require(limit in 1..MAX_PAGE_SIZE)
        require(expectedRevision == null || expectedRevision >= 0L)
        require(offset == 0 || expectedRevision != null) {
            "next-page requests must pin the first page's library revision"
        }
    }

    override fun toString(): String = "ArtistLookupRequest(redacted)"

    companion object {
        const val MAX_TARGET_CHARS = 120
        const val DEFAULT_PAGE_SIZE = 50
        const val MAX_PAGE_SIZE = 100
    }
}

/** Display-only metadata from a REAL nonblank MediaStore artist, not title. */
class ArtistIndexEntry(
    val id: LibraryGroupId,
    val displayLabel: String,
    val normalizedKey: String,
    val indexedSongCount: Int,
) {
    init {
        require(id.value.startsWith("artist:"))
        require(displayLabel.isNotBlank())
        require(normalizedKey.isNotBlank())
        require(indexedSongCount > 0)
    }

    override fun toString(): String = "ArtistIndexEntry(redacted)"
}

/** Pagination MUST use the same revision, or fail stale instead of mixing. */
class ArtistLookupPage(
    entries: List<ArtistIndexEntry>,
    val totalCount: Int,
    val revision: Long,
    val nextOffset: Int?,
    val requestedLimit: Int,
) {
    val entries: List<ArtistIndexEntry> =
        Collections.unmodifiableList(entries.toList())

    init {
        require(totalCount >= 0 && revision >= 0L)
        require(requestedLimit in 1..ArtistLookupRequest.MAX_PAGE_SIZE)
        require(this.entries.size <= requestedLimit)
        require(this.entries.map { it.id }.distinct().size == this.entries.size)
        require(nextOffset == null || nextOffset in 1..totalCount)
    }

    override fun toString(): String = "ArtistLookupPage(redacted)"
}

/**
 * Fixed artist ID + accepted library revision before fetching queue candidates.
 * No method here claims the references still exist on disk.
 */
data class ArtistTrackRefsRequest(
    val artistGroupId: LibraryGroupId,
    val expectedRevision: Long,
    val offset: Int = 0,
    val limit: Int = DEFAULT_PAGE_SIZE,
) {
    init {
        require(artistGroupId.value.startsWith("artist:"))
        require(expectedRevision >= 0L)
        require(offset >= 0 && offset <= Int.MAX_VALUE - MAX_PAGE_SIZE)
        require(limit in 1..MAX_PAGE_SIZE)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
        const val MAX_PAGE_SIZE = 100
    }

    // Avoid logging an opaque personal-media ID via data class toString().
    override fun toString(): String = "ArtistTrackRefsRequest(redacted)"
}

/**
 * Bounded opaque refs only; no full LibraryRows, content URI/artist labels or
 * queue authority crosses this Data API. P06 must call get(ref) to re-verify
 * each item against live MediaStore, and check expected revision again.
 */
class ArtistTrackRefsPage(
    refs: List<MediaRef>,
    val totalCount: Int,
    val revision: Long,
    val nextOffset: Int?,
    val requestedLimit: Int,
) {
    val refs: List<MediaRef> = Collections.unmodifiableList(refs.toList())

    init {
        require(totalCount >= 0 && revision >= 0L)
        require(requestedLimit in 1..ArtistTrackRefsRequest.MAX_PAGE_SIZE)
        require(this.refs.size <= requestedLimit)
        require(this.refs.map { it.value }.distinct().size == this.refs.size)
        require(nextOffset == null || nextOffset in 1..totalCount)
    }

    override fun toString(): String = "ArtistTrackRefsPage(redacted)"
}
