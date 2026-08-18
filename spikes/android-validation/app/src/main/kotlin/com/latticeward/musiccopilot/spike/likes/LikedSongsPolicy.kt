package com.latticeward.musiccopilot.spike.likes

@JvmInline
value class MediaId(val value: String) {
    init {
        require(value.isNotBlank()) { "media ID must not be blank" }
    }
}

sealed interface LikedSongsProjection {
    data object Removed : LikedSongsProjection
    data object Empty : LikedSongsProjection
    data class Ready(val mediaIds: List<MediaId>) : LikedSongsProjection
}

data class ReconciliationResult(
    val projection: LikedSongsProjection,
    val staleMediaIds: List<MediaId>,
)

sealed interface PlaybackPlan {
    data class Approved(
        val queue: List<MediaId>,
        val startIndex: Int,
    ) : PlaybackPlan

    data object RejectedNoPlayableItem : PlaybackPlan
    data object RejectedStaleSelection : PlaybackPlan
}

object LikedSongsPolicy {
    fun like(records: List<MediaId>, mediaId: MediaId): List<MediaId> =
        if (mediaId in records) distinct(records) else distinct(records + mediaId)

    fun unlike(records: List<MediaId>, mediaId: MediaId): List<MediaId> =
        distinct(records).filterNot { it == mediaId }

    fun reconcile(
        records: List<MediaId>,
        accessibleMediaIds: Set<MediaId>,
        featureEnabled: Boolean = true,
    ): ReconciliationResult {
        if (!featureEnabled) {
            return ReconciliationResult(LikedSongsProjection.Removed, emptyList())
        }

        val acknowledged = distinct(records)
        val playable = acknowledged.filter { it in accessibleMediaIds }
        val stale = acknowledged.filterNot { it in accessibleMediaIds }
        val projection = if (playable.isEmpty()) {
            LikedSongsProjection.Empty
        } else {
            LikedSongsProjection.Ready(playable)
        }
        return ReconciliationResult(projection, stale)
    }

    fun playList(projection: LikedSongsProjection): PlaybackPlan =
        when (projection) {
            is LikedSongsProjection.Ready -> PlaybackPlan.Approved(projection.mediaIds, startIndex = 0)
            LikedSongsProjection.Empty,
            LikedSongsProjection.Removed,
            -> PlaybackPlan.RejectedNoPlayableItem
        }

    fun playSelection(
        projection: LikedSongsProjection,
        selected: MediaId,
    ): PlaybackPlan = when (projection) {
        is LikedSongsProjection.Ready -> {
            val index = projection.mediaIds.indexOf(selected)
            if (index >= 0) PlaybackPlan.Approved(projection.mediaIds, index)
            else PlaybackPlan.RejectedStaleSelection
        }
        LikedSongsProjection.Empty,
        LikedSongsProjection.Removed,
        -> PlaybackPlan.RejectedNoPlayableItem
    }

    private fun distinct(records: List<MediaId>): List<MediaId> = records.distinct()
}
