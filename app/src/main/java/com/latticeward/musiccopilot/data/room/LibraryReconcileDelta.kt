package com.latticeward.musiccopilot.data.room

/**
 * A deterministic row-level delta. Full platform enumeration is still needed
 * to prove deletions; ONLY changed derived rows are written to SQLite.
 *
 * Room is a rebuildable projection, not a substitute for MediaStore existence.
 * Input rows contain local, sensitive metadata: never log this plan or its IDs.
 */
data class LibraryReconcileDelta(
    val upserts: List<LibraryRow>,
    val removedRefs: List<String>,
    val previousCount: Int,
    val resultingCount: Int,
) {
    val changed: Boolean get() = upserts.isNotEmpty() || removedRefs.isNotEmpty()
    val changedCount: Int get() = upserts.size
    val removedCount: Int get() = removedRefs.size
}

object LibraryDeltaPlanner {
    fun calculate(
        previous: List<LibraryRow>,
        current: List<LibraryRow>,
    ): LibraryReconcileDelta {
        val before = previous.associateBy { it.mediaRef }
        val after = current.associateBy { it.mediaRef }
        require(before.size == previous.size) { "duplicate old index keys" }
        require(after.size == current.size) { "duplicate new index keys" }
        require(before.keys.none { it.isBlank() } && after.keys.none { it.isBlank() }) {
            "invalid derived index key"
        }

        val upserts = current
            .filter { before[it.mediaRef] != it }
            .sortedBy { it.mediaRef }
        val removed = (before.keys - after.keys).sorted()

        return LibraryReconcileDelta(
            upserts = upserts,
            removedRefs = removed,
            previousCount = previous.size,
            resultingCount = current.size,
        )
    }
}
