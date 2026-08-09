package com.latticeward.musiccopilot.spike.reconcile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReconciliationEngineTest {
    @Test
    fun scanTruthRemovesStaleRowsAndDeduplicates() {
        val previous = listOf(
            IndexedMedia("volume-a:1", 1_000, 1),
            IndexedMedia("volume-a:stale", 2_000, 1),
        )
        val scan = listOf(
            ScannedMedia("volume-a:1", 1_000),
            ScannedMedia("volume-a:1", 1_000),
            ScannedMedia("volume-a:2", 3_000),
        )

        val result = ReconciliationEngine.reconcile(previous, scan, generation = 2)

        assertEquals(listOf("volume-a:1", "volume-a:2"), result.verified.map { it.stableReference })
        assertEquals(setOf("volume-a:stale"), result.removedReferences)
        assertEquals(1, result.duplicateCount)
        assertTrue(result.verified.all { it.generation == 2L })
    }

    @Test
    fun zeroDurationRowsAreNotDeclaredPlayable() {
        val result = ReconciliationEngine.reconcile(
            previous = emptyList(),
            scan = listOf(ScannedMedia("volume-a:1", 0)),
            generation = 1,
        )

        assertTrue(result.verified.isEmpty())
    }

    @Test
    fun fiveThousandRowsRemainUniqueAndStable() {
        val scan = (1..5_000).map { ScannedMedia("volume-a:$it", 60_000) }

        val result = ReconciliationEngine.reconcile(emptyList(), scan, generation = 1)

        assertEquals(5_000, result.verified.size)
        assertEquals(0, result.duplicateCount)
        assertEquals("volume-a:1", result.verified.first().stableReference)
        assertEquals("volume-a:5000", result.verified.last().stableReference)
    }
}
