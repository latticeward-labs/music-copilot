package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy as Nav
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy as Queue
import org.junit.Assert.*
import org.junit.Test

/** Pure Kotlin policy contract; service, resolver and Android tests belong to P03-P12. */
class Issue7Phase2PlaybackPolicyTest {
    private val a = MediaRef("audio:1")
    private val b = MediaRef("audio:2")
    private val c = MediaRef("audio:3")
    private fun request(items: List<MediaRef>, index: Int = 0, ms: Long = 0L) =
        PlaybackCommand.ReplaceQueue(CommandId("synthetic-1"), items, index, ms, true)

    @Test fun preservesVisibleOrderDuplicatesAndOccurrenceIndex() {
        val decision = Queue.validate(request(listOf(a, b, a, c), 2))
            as Queue.Decision.Accepted
        assertEquals(listOf(a, b, a, c), decision.items)
        assertEquals(2, decision.selectedIndex)
        assertTrue(decision.playWhenReady)
    }

    @Test fun emptyQueueIsRejectedInsteadOfClearingLiveQueue() {
        assertEquals(Queue.Decision.Rejected(Queue.Rejection.EmptyQueue),
            Queue.validate(request(emptyList())))
    }

    @Test fun rejectsOutOfRangeIndicesAndNegativePosition() {
        for (index in listOf(-1, 2)) assertEquals(Queue.Rejection.InvalidStartIndex,
            (Queue.validate(request(listOf(a, b), index)) as Queue.Decision.Rejected).reason)
        assertEquals(Queue.Rejection.NegativePosition,
            (Queue.validate(request(listOf(a), ms = -1L)) as Queue.Decision.Rejected).reason)
    }

    @Test fun queueHasFiveThousandEntryCapacity() {
        val within = List(5_000) { MediaRef("audio:$it") }
        assertTrue(Queue.validate(request(within, 4_999)) is Queue.Decision.Accepted)
        assertEquals(Queue.Rejection.QueueTooLarge,
            (Queue.validate(request(within + c)) as Queue.Decision.Rejected).reason)
    }

    @Test fun rejectsMalformedOpaqueCommandAndMediaIdentifiers() {
        assertEquals(Queue.Rejection.InvalidCommandId,
            (Queue.validate(request(listOf(a)).copy(commandId = CommandId(" ")))
                as Queue.Decision.Rejected).reason)
        assertEquals(Queue.Rejection.InvalidMediaRef,
            (Queue.validate(request(listOf(MediaRef("")))) as Queue.Decision.Rejected).reason)
        assertEquals(Queue.Rejection.InvalidMediaRef,
            (Queue.validate(request(listOf(MediaRef("x".repeat(129)))))
                as Queue.Decision.Rejected).reason)
    }

    @Test fun missingNonSelectedItemsAreOmittedAndSelectedIndexRemapped() {
        val candidate = Queue.validate(request(listOf(a, b, c, b), 2))
            as Queue.Decision.Accepted
        val filtered = Queue.retainPlayable(candidate, listOf(false, true, true, false))
            as Queue.Decision.Accepted
        assertEquals(listOf(b, c), filtered.items)
        assertEquals(1, filtered.selectedIndex)
    }

    @Test fun missingSelectedItemFailsClosed() {
        val candidate = Queue.validate(request(listOf(a, b, c), 1))
            as Queue.Decision.Accepted
        assertEquals(Queue.Rejection.SelectedItemUnavailable,
            (Queue.retainPlayable(candidate, listOf(true, false, true))
                as Queue.Decision.Rejected).reason)
    }

    @Test fun providerValidationLengthMismatchFailsClosed() {
        val candidate = Queue.validate(request(listOf(a, b), 0))
            as Queue.Decision.Accepted
        assertEquals(Queue.Rejection.InvalidValidationShape,
            (Queue.retainPlayable(candidate, listOf(true)) as Queue.Decision.Rejected).reason)
    }

    @Test fun nextIsNonWrappingAndReportsEndOrNoTrack() {
        assertEquals(Queue.NextDecision.Advance(1), Queue.next(0, 3))
        assertEquals(Queue.NextDecision.EndOfQueue, Queue.next(2, 3))
        assertEquals(Queue.NextDecision.NoTrack, Queue.next(null, 3))
        assertEquals(Queue.NextDecision.NoTrack, Queue.next(0, 0))
        assertEquals(Queue.NextDecision.NoTrack, Queue.next(-1, 3))
    }

    @Test fun previousBoundaryIsExactlyThreeSecondsWithSafeFirstTrack() {
        assertEquals(Nav.PreviousDecision.MoveToPrevious, Nav.previous(2_999L, true))
        assertEquals(Nav.PreviousDecision.RestartCurrent, Nav.previous(3_000L, true))
        assertEquals(Nav.PreviousDecision.RestartCurrent, Nav.previous(0L, false))
    }

    @Test fun absoluteSeekClampsBothEndsAndRejectsUnknownDuration() {
        assertEquals(0L, Nav.seekTo(-4L, 100L, true))
        assertEquals(100L, Nav.seekTo(900L, 100L, true))
        assertNull(Nav.seekTo(50L, null, true))
        assertNull(Nav.seekTo(50L, 100L, false))
    }

    @Test fun relativeSeekSaturatesOverflowAndClamps() {
        assertEquals(100L, Nav.seekBy(Long.MAX_VALUE, 3L, 100L, true))
        assertEquals(0L, Nav.seekBy(1L, Long.MIN_VALUE, 100L, true))
        assertNull(Nav.seekBy(5L, 10L, null, true))
        assertNull(Nav.seekBy(5L, -10L, 100L, false))
    }

    @Test fun seekBackTenSecondsWorksWithUnknownDuration() {
        assertEquals(0L, Nav.seekBack(3_000L, null, true))
        assertEquals(2_000L, Nav.seekBack(12_000L, 7_000L, true))
        assertNull(Nav.seekBack(-1L, null, true))
    }
}
