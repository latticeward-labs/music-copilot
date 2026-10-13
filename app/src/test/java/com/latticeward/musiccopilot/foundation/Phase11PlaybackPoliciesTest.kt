package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy
import com.latticeward.musiccopilot.playback.restore.SafePausedRestorePolicy
import org.junit.Assert.*
import org.junit.Test

class Phase11PlaybackPoliciesTest {
    private val a = MediaRef("audio:1")
    private val b = MediaRef("audio:2")
    private val c = MediaRef("audio:3")

    private fun snapshot() = RestorableQueueSnapshot(
        1, 9L, listOf(a, b, c), 1, 15_000L, 23L
    )

    @Test fun previousUsesThreeSecondThresholdAndNeverProducesInvalidIndex() {
        val policy = PlaybackNavigationPolicy
        assertEquals(PlaybackNavigationPolicy.PreviousDecision.RestartCurrent,
            policy.previous(3_001L, hasPreviousItem = false))
        assertEquals(PlaybackNavigationPolicy.PreviousDecision.MoveToPrevious,
            policy.previous(3_000L, hasPreviousItem = true))
        assertEquals(PlaybackNavigationPolicy.PreviousDecision.StayCurrent,
            policy.previous(0L, hasPreviousItem = false))
    }

    @Test fun tenSecondSeekIsClampedAndUnsupportedSeekNeverMutates() {
        val policy = PlaybackNavigationPolicy
        assertEquals(0L, policy.seekBack(3_000L, null, true))
        assertEquals(2_000L, policy.seekBack(12_000L, 7_000L, true))
        assertEquals(3_000L, policy.seekBack(13_000L, 3_000L, true))
        assertNull(policy.seekBack(15_000L, 99_000L, false))
        assertEquals(10_000L, policy.VOICE_SEEK_BACK_MS)
    }

    @Test fun validColdRestoreOnlyProducesPausedNeverAutoplay() {
        val outcome = SafePausedRestorePolicy.prepare(snapshot(), false,
            setOf(a, b, c), 10_000L) as AppResult.Success<SafePausedRestorePolicy.Plan?>
        val plan = outcome.value!!
        assertEquals(listOf(a, b, c), plan.items)
        assertEquals(1, plan.currentIndex)
        assertEquals(10_000L, plan.positionMs)
        assertEquals(PlaybackState.Paused, plan.playbackState)
        assertFalse(plan.playWhenReady)
    }

    @Test fun liveSessionAlwaysWinsOverPersistedCheckpoint() {
        val outcome = SafePausedRestorePolicy.prepare(
            snapshot().copy(schemaVersion = 999), true, setOf(a, b), 10_000L
        ) as AppResult.Success<SafePausedRestorePolicy.Plan?>
        assertNull(outcome.value)
    }

    @Test fun removedMediaIsDroppedAndOldPositionNeverTransfersToWrongTrack() {
        val outcome = SafePausedRestorePolicy.prepare(
            snapshot(), false, setOf(a, c), 30_000L
        ) as AppResult.Success<SafePausedRestorePolicy.Plan?>
        val plan = outcome.value!!
        assertEquals(listOf(a, c), plan.items)
        assertEquals(0, plan.currentIndex)
        assertEquals(0L, plan.positionMs)
        assertFalse(plan.playWhenReady)
    }

    @Test fun staleOrRevokedLibraryNeverCreatesRestoredQueue() {
        val outcome = SafePausedRestorePolicy.prepare(
            snapshot(), false, emptySet(), 20_000L
        ) as AppResult.Success<SafePausedRestorePolicy.Plan?>
        assertNull(outcome.value)
    }

    @Test fun corruptSnapshotFailsTypedAndDoesNotLeakPaths() {
        val outcome = SafePausedRestorePolicy.prepare(
            snapshot().copy(positionMs = -1L), false, setOf(a, b), 30_000L
        ) as AppResult.Failure
        assertEquals(AppErrorCodes.SNAPSHOT_CORRUPT, outcome.error.code)
        assertTrue(outcome.error is AppError.Storage)
        assertFalse(outcome.error.code.contains("/"))
    }

    @Test fun futureSchemaIsRejectedBeforeRestore() {
        val outcome = SafePausedRestorePolicy.prepare(
            snapshot().copy(schemaVersion = 2), false, setOf(a, b), 30_000L
        ) as AppResult.Failure
        assertEquals(AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA, outcome.error.code)
    }

    @Test fun emptyAndUnverifiedDurationFallsBackSafely() {
        val snap = snapshot()
        val empty = SafePausedRestorePolicy.prepare(
            snap.copy(items = emptyList(), currentIndex = 0, positionMs = 0L),
            false, setOf(a), 20_000L
        ) as AppResult.Success<SafePausedRestorePolicy.Plan?>
        assertNull(empty.value)
        val unknown = SafePausedRestorePolicy.prepare(snap, false, setOf(a,b,c), null)
            as AppResult.Success<SafePausedRestorePolicy.Plan?>
        assertEquals(0L, unknown.value!!.positionMs)
        assertFalse(unknown.value!!.playWhenReady)
    }
    @Test fun media3ServiceExportIsDocumentedAndTypedCommandsStayPrivate() {
        val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
        val service = java.io.File(
            "src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt"
        ).readText()
        assertTrue(manifest.contains("android:exported=\"true\""))
        assertTrue(manifest.contains("tools:ignore=\"ExportedService\""))
        assertTrue(service.contains("controller.packageName == packageName &&"))
        assertTrue(service.contains("controller.uid == android.os.Process.myUid()"))
        assertTrue(service.contains("commandBuilder.add(PlaybackWireProtocol.executeCommand())"))
    }

}
