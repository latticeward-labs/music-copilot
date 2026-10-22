package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.data.datastore.RestoreSnapshotPolicy
import com.latticeward.musiccopilot.data.datastore.SnapshotValidity
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.restore.SafePausedRestorePolicy
import org.junit.Assert.*
import org.junit.Test

/**
 * P11 authored Safe-Paused P1 contract specs. NOT EXECUTED in P11.
 * Real Android/Proto DataStore + Media3 integration is reserved for P13.
 */
class Issue7Phase11SafePausedPolicyTest {
    private fun ref(n: Int) = MediaRef("external:$n")
    private fun snapshot(
        items: List<MediaRef> = listOf(ref(1), ref(2), ref(3)),
        index: Int = 1,
        position: Long = 37_000L,
        schema: Int = 1,
    ) = RestorableQueueSnapshot(
        schemaVersion = schema,
        queueRevision = 12L,
        items = items,
        currentIndex = index,
        positionMs = position,
        capturedAtEpochMs = 1_700_000_000_000L,
    )

    private fun success(result: AppResult<SafePausedRestorePolicy.Plan?>):
        SafePausedRestorePolicy.Plan? {
        assertTrue(result is AppResult.Success)
        return (result as AppResult.Success).value
    }

    @Test fun restoreAlwaysPausedNeverPlays() {
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(), false, setOf(ref(1), ref(2), ref(3)), 90_000L,
        ))!!
        assertEquals(PlaybackState.Paused, plan.playbackState)
        assertFalse(plan.playWhenReady)
    }

    @Test fun liveSessionAlwaysOverridesPersistedQueue() {
        assertNull(success(SafePausedRestorePolicy.prepare(
            snapshot(), true, setOf(ref(1), ref(2), ref(3)), 90_000L,
        )))
    }

    @Test fun nullCheckpointDoesNothing() {
        assertNull(success(SafePausedRestorePolicy.prepare(
            null, false, setOf(ref(1)), 30_000L,
        )))
    }

    @Test fun futureSchemaCannotBeRestored() {
        val result = SafePausedRestorePolicy.prepare(
            snapshot(schema = 2), false, setOf(ref(1), ref(2), ref(3)), 99_000L,
        )
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA,
            (result as AppResult.Failure).error.code)
    }

    @Test fun invalidSelectedOccurrenceIsRejected() {
        val result = SafePausedRestorePolicy.prepare(
            snapshot(index = 8), false, setOf(ref(1), ref(2), ref(3)), 99_000L,
        )
        assertTrue(result is AppResult.Failure)
    }

    @Test fun negativePositionCannotBeRestored() {
        assertTrue(SafePausedRestorePolicy.prepare(
            snapshot(position = -3), false, setOf(ref(1)), 99_000L
        ) is AppResult.Failure)
    }

    @Test fun allMissingItemsYieldNoRestoration() {
        assertNull(success(SafePausedRestorePolicy.prepare(
            snapshot(), false, emptySet(), null,
        )))
    }

    @Test fun dropMissingNonSelectedWithoutReordering() {
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(), false, setOf(ref(1), ref(2)), 50_000L,
        ))!!
        assertEquals(listOf(ref(1), ref(2)), plan.items)
        assertEquals(1, plan.currentIndex)
        assertEquals(37_000L, plan.positionMs)
    }

    @Test fun selectedMissingResetsToFirstSurvivorAndZeroPosition() {
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(), false, setOf(ref(1), ref(3)), 50_000L,
        ))!!
        assertEquals(listOf(ref(1), ref(3)), plan.items)
        assertEquals(0, plan.currentIndex)
        assertEquals(0L, plan.positionMs)
    }

    @Test fun clampPositionToFreshDuration() {
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(position = 73_000), false, setOf(ref(1), ref(2), ref(3)),
            13_000L,
        ))!!
        assertEquals(13_000L, plan.positionMs)
    }

    @Test fun unknownDurationResetsPositionToZero() {
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(), false, setOf(ref(1), ref(2), ref(3)), null,
        ))!!
        assertEquals(0L, plan.positionMs)
    }

    @Test fun duplicateOccurrencesSurviveAsDistinctQueuePositions() {
        val refs = listOf(ref(1), ref(2), ref(1), ref(3))
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(items = refs, index = 2), false, refs.toSet(), 90_000L,
        ))!!
        assertEquals(refs, plan.items)
        assertEquals(2, plan.currentIndex)
        assertEquals(37_000L, plan.positionMs)
    }

    @Test fun duplicateCurrentKeepsOccurrenceAfterMissingPrecedingSong() {
        val refs = listOf(ref(9), ref(2), ref(1), ref(2))
        val plan = success(SafePausedRestorePolicy.prepare(
            snapshot(items = refs, index = 3), false, setOf(ref(2), ref(1)),
            90_000L,
        ))!!
        assertEquals(listOf(ref(2), ref(1), ref(2)), plan.items)
        assertEquals(2, plan.currentIndex)
    }

    @Test fun datastoreAllowsDuplicateStableIds() {
        assertEquals(SnapshotValidity.Valid, RestoreSnapshotPolicy.validate(
            snapshot(items = listOf(ref(1), ref(2), ref(1)), index = 2)
        ))
    }

    @Test fun datastoreRejectsOverlongOpaqueRef() {
        assertEquals(SnapshotValidity.Corrupt, RestoreSnapshotPolicy.validate(
            snapshot(items = listOf(MediaRef("a".repeat(129))), index = 0)
        ))
    }

    @Test fun datastoreRejectsMoreThanFiveThousandItems() {
        assertEquals(SnapshotValidity.Corrupt, RestoreSnapshotPolicy.validate(
            snapshot(items = List(5001) { ref(it) }, index = 0)
        ))
    }

    @Test fun datastoreRejectsFutureSchema() {
        assertEquals(SnapshotValidity.FutureSchema, RestoreSnapshotPolicy.validate(
            snapshot(schema = 99)
        ))
    }

    @Test fun emptyProtoStateIsNonAuthoritative() {
        assertEquals(SnapshotValidity.Valid,
            RestoreSnapshotPolicy.validate(RestoreSnapshotPolicy.empty()))
        assertTrue(RestoreSnapshotPolicy.isEmpty(RestoreSnapshotPolicy.empty()))
    }
}
