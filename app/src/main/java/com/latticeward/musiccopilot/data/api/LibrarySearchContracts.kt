package com.latticeward.musiccopilot.data.api

/**
 * P09 bounded local search surface; all rows come from the derived Room
 * projection after MediaStore authorization. A result is not proof of
 * playability and is NEVER a MediaSession command.
 *
 * Query text/labels/IDs are private and must never be logged or persisted.
 */
data class LibrarySearchResults(
    val entries: List<LibraryBrowseEntry>,
    val revision: Long,
    val limit: Int,
) {
    init {
        require(revision >= 0)
        require(limit in 1..100)
        require(entries.size <= limit)
    }
}
