package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.playback.VisiblePlaybackQueuePolicy
import com.latticeward.musiccopilot.core.model.MediaRef
import org.junit.Assert.*
import org.junit.Test

/** Deterministic checked-ack logic for P05. Not a Media3 audible-output test. */
class Issue7Phase5VisibleQueuePolicyTest {
    private val a = MediaRef("audio:1")
    private val b = MediaRef("audio:2")
    private val c = MediaRef("audio:3")
    private val d = MediaRef("audio:4")

    @Test fun acceptsFullVisibleCollectionAndClickedMiddle() {
        assertTrue(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, c), 1, listOf(a, b, c), 1
        ))
    }

    @Test fun acceptsMissingNonselectedBeforeAndAfterWithRemappedIndex() {
        assertTrue(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, c, d), 2, listOf(b, c), 1
        ))
    }

    @Test fun rejectsQueueReorderingAndInjectedExtraEntries() {
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, c), 1, listOf(c, b, a), 1
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, c), 1, listOf(a, b, d, c), 1
        ))
    }

    @Test fun refusesWrongClickedOccurrenceEvenWhenDuplicateRefsRemain() {
        assertTrue(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, a, c), 2, listOf(b, a, c), 1
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b, a, c), 2, listOf(a, b, c), 0
        ))
    }

    @Test fun rejectsAbsentOrIncorrectCurrentIndex() {
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b), 1, listOf(a, b), null
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b), 1, listOf(a, b), 9
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, b), 1, listOf(a, b), 0
        ))
    }

    @Test fun rejectsEmptyOrOutOfRangeRequestsWithoutExceptions() {
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            emptyList(), 0, listOf(a), 0
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a), -1, listOf(a), 0
        ))
        assertFalse(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a), 1, listOf(a), 0
        ))
    }

    @Test fun sameOpaqueTrackSelectedDifferentOccurrenceIsNotConfused() {
        assertTrue(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, a, b), 1, listOf(a, a, b), 1
        ))
        assertTrue(VisiblePlaybackQueuePolicy.matchesAcknowledgement(
            listOf(a, a, b), 1, listOf(a, b), 0
        ))
    }
}
