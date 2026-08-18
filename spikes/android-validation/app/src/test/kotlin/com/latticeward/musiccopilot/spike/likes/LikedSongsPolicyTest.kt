package com.latticeward.musiccopilot.spike.likes

import org.junit.Assert.assertEquals
import org.junit.Test

class LikedSongsPolicyTest {
    private val a = MediaId("a")
    private val b = MediaId("b")
    private val missing = MediaId("missing")

    @Test
    fun featureCanBeRemovedWithoutPublishingADeadSurface() {
        assertEquals(
            ReconciliationResult(LikedSongsProjection.Removed, emptyList()),
            LikedSongsPolicy.reconcile(listOf(a), setOf(a), featureEnabled = false),
        )
    }

    @Test
    fun duplicateLikesCollapseAndRepositoryOrderIsPreserved() {
        val records = LikedSongsPolicy.like(listOf(a, a), b)
        assertEquals(listOf(a, b), records)
        assertEquals(listOf(a, b), LikedSongsPolicy.like(records, a))
    }

    @Test
    fun unlikeRemovesOnlyTheSelectedLocalRecord() {
        assertEquals(listOf(b), LikedSongsPolicy.unlike(listOf(a, b, a), a))
    }

    @Test
    fun missingMediaIsExcludedAndReportedForSafeReconciliation() {
        assertEquals(
            ReconciliationResult(
                projection = LikedSongsProjection.Ready(listOf(a, b)),
                staleMediaIds = listOf(missing),
            ),
            LikedSongsPolicy.reconcile(listOf(a, missing, b, a), setOf(a, b)),
        )
    }

    @Test
    fun noAccessibleLikeProducesTheRequiredEmptyState() {
        assertEquals(
            ReconciliationResult(LikedSongsProjection.Empty, listOf(missing)),
            LikedSongsPolicy.reconcile(listOf(missing), emptySet()),
        )
    }

    @Test
    fun playListUsesTheAccessibleProjectionFromItsFirstItem() {
        val ready = LikedSongsProjection.Ready(listOf(a, b))
        assertEquals(PlaybackPlan.Approved(listOf(a, b), 0), LikedSongsPolicy.playList(ready))
    }

    @Test
    fun rowSelectionUsesTheSameQueueAndSelectedIndex() {
        val ready = LikedSongsProjection.Ready(listOf(a, b))
        assertEquals(PlaybackPlan.Approved(listOf(a, b), 1), LikedSongsPolicy.playSelection(ready, b))
    }

    @Test
    fun staleSelectionCannotProduceAPlaybackSuccessPlan() {
        val ready = LikedSongsProjection.Ready(listOf(a, b))
        assertEquals(PlaybackPlan.RejectedStaleSelection, LikedSongsPolicy.playSelection(ready, missing))
        assertEquals(PlaybackPlan.RejectedNoPlayableItem, LikedSongsPolicy.playList(LikedSongsProjection.Empty))
    }
}
