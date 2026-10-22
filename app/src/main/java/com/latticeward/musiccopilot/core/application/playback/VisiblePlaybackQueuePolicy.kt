package com.latticeward.musiccopilot.core.application.playback

import com.latticeward.musiccopilot.core.model.MediaRef

/**
 * P05 confirmation of the *specific clicked occurrence*, after the Service
 * drops individually unavailable non-selected items. Never assumes a
 * successful RPC proves audio output or accepts an optimistic UI queue.
 */
object VisiblePlaybackQueuePolicy {
    fun matchesAcknowledgement(
        requested: List<MediaRef>,
        requestedIndex: Int,
        actual: List<MediaRef>,
        actualIndex: Int?,
    ): Boolean {
        if (requested.isEmpty() || requestedIndex !in requested.indices ||
            actual.isEmpty() || actualIndex == null || actualIndex !in actual.indices ||
            actual[actualIndex] != requested[requestedIndex]
        ) return false
        // An accepted service proposal can remove only unavailable other
        // items. The chosen occurrence is an immovable anchor, even if the
        // same opaque MediaRef appears elsewhere in the collection.
        return isSubsequence(
            actual.subList(0, actualIndex),
            requested.subList(0, requestedIndex),
        ) && isSubsequence(
            actual.subList(actualIndex + 1, actual.size),
            requested.subList(requestedIndex + 1, requested.size),
        )
    }

    private fun isSubsequence(kept: List<MediaRef>, original: List<MediaRef>): Boolean {
        var index = 0
        for (ref in original) {
            if (index < kept.size && kept[index] == ref) index++
        }
        return index == kept.size
    }
}
