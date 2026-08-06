package com.latticeward.musiccopilot.spike.reconcile

data class ScannedMedia(
    val stableReference: String,
    val durationMs: Long,
)

data class IndexedMedia(
    val stableReference: String,
    val durationMs: Long,
    val generation: Long,
)

data class ReconciliationResult(
    val verified: List<IndexedMedia>,
    val removedReferences: Set<String>,
    val duplicateCount: Int,
)

/** Pure reconciliation core. MediaStore remains authoritative; this result is rebuildable. */
object ReconciliationEngine {
    fun reconcile(
        previous: List<IndexedMedia>,
        scan: List<ScannedMedia>,
        generation: Long,
    ): ReconciliationResult {
        require(generation > 0)

        val unique = LinkedHashMap<String, ScannedMedia>()
        var duplicateCount = 0
        scan.forEach { item ->
            if (item.durationMs > 0 && unique.putIfAbsent(item.stableReference, item) != null) {
                duplicateCount += 1
            }
        }

        val verified = unique.values.map { item ->
            IndexedMedia(
                stableReference = item.stableReference,
                durationMs = item.durationMs,
                generation = generation,
            )
        }
        val currentReferences = unique.keys
        val removed = previous.asSequence()
            .map { it.stableReference }
            .filterNot(currentReferences::contains)
            .toSet()

        return ReconciliationResult(verified, removed, duplicateCount)
    }
}
