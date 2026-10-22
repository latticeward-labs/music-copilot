package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.SystemTransportPolicy
import org.junit.Assert.*
import org.junit.Test

/** P06 pure command allowlist, requires no Android service or real media. */
class Issue7Phase6SystemTransportPolicyTest {
    private val id = CommandId("test-system")

    private fun route(action: SystemTransportPolicy.Action, position: Long = 0L) =
        SystemTransportPolicy.translate(action, id, position)

    @Test fun playMapsIntoServiceCommand() {
        assertEquals(PlaybackCommand.Play(id), route(SystemTransportPolicy.Action.Play))
    }

    @Test fun pauseMapsIntoServiceCommand() {
        assertEquals(PlaybackCommand.Pause(id), route(SystemTransportPolicy.Action.Pause))
    }

    @Test fun nextUsesSameP02PolicyCommandAsInternalController() {
        assertEquals(PlaybackCommand.Next(id), route(SystemTransportPolicy.Action.Next))
    }

    @Test fun previousUsesSameP02PolicyCommandAsInternalController() {
        assertEquals(PlaybackCommand.Previous(id), route(SystemTransportPolicy.Action.Previous))
    }

    @Test fun stopMapsToSameServiceStop() {
        assertEquals(PlaybackCommand.Stop(id), route(SystemTransportPolicy.Action.Stop))
    }

    @Test fun absoluteSeekPreservesPosition() {
        assertEquals(
            PlaybackCommand.SeekTo(id, 45_000L),
            route(SystemTransportPolicy.Action.SeekTo, 45_000L),
        )
    }

    @Test fun zeroPositionSeekIsAllowed() {
        assertEquals(
            PlaybackCommand.SeekTo(id, 0L),
            route(SystemTransportPolicy.Action.SeekTo, 0L),
        )
    }

    @Test fun negativeSeekIsRejectedBeforeEnqueue() {
        assertNull(route(SystemTransportPolicy.Action.SeekTo, -1))
        assertNull(route(SystemTransportPolicy.Action.SeekTo, Long.MIN_VALUE))
    }

    @Test fun externalQueueMutationIsRejectedWithoutTransportFallback() {
        assertNull(route(SystemTransportPolicy.Action.SetQueue))
        assertNull(route(SystemTransportPolicy.Action.AddMediaItem))
    }

    @Test fun arbitraryQueueIndexSelectionIsRejected() {
        assertNull(route(SystemTransportPolicy.Action.SeekOtherQueueItem))
    }

    @Test fun unsupportedRepeatAndShuffleNeverEnterWorker() {
        assertNull(route(SystemTransportPolicy.Action.Repeat))
        assertNull(route(SystemTransportPolicy.Action.Shuffle))
    }

    @Test fun commandIdIsUnchangedByTranslation() {
        val unique = CommandId("synthetic-123")
        val translated = SystemTransportPolicy.translate(
            SystemTransportPolicy.Action.Next, unique
        ) as PlaybackCommand.Next
        assertEquals(unique, translated.commandId)
    }
}
