package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.controller.ControllerSnapshotRelay
import org.junit.Assert.*
import org.junit.Test

/** Pure deterministic MediaController projection tests, no Android runtime. */
class Issue7Phase6ControllerSnapshotParityTest {
    private fun snapshot(revision: Long, index: Int = 0): PlaybackSnapshot {
        val items = listOf(MediaRef("audio:1"), MediaRef("audio:2"))
        return PlaybackSnapshot(
            revision = revision, queueRevision = 2,
            playbackState = PlaybackState.Paused, queue = items,
            currentIndex = index, currentItem = items[index],
            positionMs = 3_500, durationMs = 10_000, canSeek = true,
            canSkipPrevious = true, canSkipNext = index == 0,
        )
    }

    @Test fun readyRequiresCompleteFirstServiceSnapshot() {
        val relay = ControllerSnapshotRelay()
        assertEquals(PlaybackConnectionState.Disconnected, relay.connectionState.value)
        relay.beginConnect()
        assertEquals(PlaybackConnectionState.Connecting, relay.connectionState.value)
        assertTrue(relay.publish(snapshot(10L), true))
        assertEquals(PlaybackConnectionState.Ready, relay.connectionState.value)
        assertEquals(10L, relay.snapshot.value.revision)
    }

    @Test fun lateOlderNotificationCannotUndoNewerAck() {
        val relay = ControllerSnapshotRelay()
        relay.beginConnect()
        assertTrue(relay.publish(snapshot(2L), true))
        assertTrue(relay.publish(snapshot(4L, 1), false))
        assertFalse(relay.publish(snapshot(3L, 0), false))
        assertEquals(1, relay.snapshot.value.currentIndex)
    }

    @Test fun sameRevisionContradictoryEventsAreDropped() {
        val relay = ControllerSnapshotRelay()
        relay.beginConnect()
        assertTrue(relay.publish(snapshot(4L), true))
        assertFalse(relay.publish(snapshot(4L, 1), false))
        assertEquals(0, relay.snapshot.value.currentIndex)
    }

    @Test fun reconnectedProcessMayResetRevisionAfterCompleteSnapshot() {
        val relay = ControllerSnapshotRelay()
        relay.beginConnect()
        assertTrue(relay.publish(snapshot(50L), true))
        relay.failed()
        relay.beginConnect()
        assertTrue(relay.publish(snapshot(1L, 1), true))
        assertEquals(PlaybackConnectionState.Ready, relay.connectionState.value)
        assertEquals(1L, relay.snapshot.value.revision)
        assertEquals(1, relay.snapshot.value.currentIndex)
    }

    @Test fun disconnectKeepsLastTruthButNotReady() {
        val relay = ControllerSnapshotRelay()
        relay.beginConnect()
        assertTrue(relay.publish(snapshot(8L), true))
        relay.disconnected()
        assertEquals(PlaybackConnectionState.Disconnected, relay.connectionState.value)
        assertEquals(8L, relay.snapshot.value.revision)
    }
}
