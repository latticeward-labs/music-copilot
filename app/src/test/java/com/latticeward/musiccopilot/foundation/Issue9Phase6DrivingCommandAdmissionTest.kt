package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.driving.DrivingAction
import com.latticeward.musiccopilot.core.application.driving.DrivingCommandAdmissionPolicy
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import org.junit.Assert.*
import org.junit.Test

/**
 * #9 P06 authored Kotlin/JUnit command-admission contract; NEVER RUN at P06.
 * Actual Gateway ACK, source compile and Android regression are unverified
 * until the one cumulative P13 build and owner/device verification.
 */
class Issue9Phase6DrivingCommandAdmissionTest {
    private val a = MediaRef("audio:110")
    private val b = MediaRef("audio:120")
    private val cmdId = CommandId("p06-manual-tap")
    private val ready = PlaybackConnectionState.Ready

    private fun snapshot(
        revision: Long = 3L,
        queueRevision: Long = 5L,
        playing: PlaybackState = PlaybackState.Playing,
        queue: List<MediaRef> = listOf(a, b),
        selected: Int? = 0,
        item: MediaRef? = a,
        positionMs: Long = 23_000L,
        durationMs: Long? = 80_000L,
        previous: Boolean = true,
        next: Boolean = true,
        seek: Boolean = true,
    ): PlaybackSnapshot = PlaybackSnapshot(
        revision = revision,
        queueRevision = queueRevision,
        playbackState = playing,
        queue = queue,
        currentIndex = selected,
        currentItem = item,
        positionMs = positionMs,
        durationMs = durationMs,
        canSeek = seek,
        canSkipPrevious = previous,
        canSkipNext = next,
    )

    private fun command(
        shown: PlaybackSnapshot = snapshot(),
        latest: PlaybackSnapshot = shown,
        action: DrivingAction = DrivingAction.PlayPause,
        displayedConnection: PlaybackConnectionState = ready,
        latestConnection: PlaybackConnectionState = ready,
        pending: Boolean = false,
    ): PlaybackCommand? = DrivingCommandAdmissionPolicy.commandFor(
        displayedConnection = displayedConnection,
        displayedSnapshot = shown,
        latestConnection = latestConnection,
        latestSnapshot = latest,
        pending = pending,
        action = action,
        id = cmdId,
    )

    @Test fun playingTapMapsToSingleTypedPauseIntent() {
        assertEquals(PlaybackCommand.Pause(cmdId), command())
    }

    @Test fun pausedTapMapsToExplicitTrustedPrivatePlay() {
        val paused = snapshot(playing = PlaybackState.Paused)
        val actual = command(shown = paused)
        assertEquals(PlaybackCommand.Play(cmdId), actual)
        assertTrue(actual is PlaybackCommand.Play)
        // Service still checks its focus/route guard and ACK.
    }

    @Test fun previousUsesExistingTypedTransportWithoutQueueReplacement() {
        assertEquals(PlaybackCommand.Previous(cmdId),
            command(action = DrivingAction.Previous))
    }

    @Test fun nextUsesExistingTypedTransportWithoutQueueReplacement() {
        assertEquals(PlaybackCommand.Next(cmdId),
            command(action = DrivingAction.Next))
    }

    @Test fun seekBackwardUsesExactlyTenSecondRelativeIntent() {
        assertEquals(PlaybackCommand.SeekBy(cmdId, -10_000L),
            command(action = DrivingAction.SeekBack))
    }

    @Test fun seekBackwardAtStartRemainsServiceClamped() {
        val atZero = snapshot(positionMs = 0L)
        assertEquals(PlaybackCommand.SeekBy(cmdId, -10_000L),
            command(shown = atZero, action = DrivingAction.SeekBack))
    }

    @Test fun requestPendingRejectsReentrantTapBeforeGeneratingSecondCommand() {
        assertNull(command(pending = true))
    }

    @Test fun connectionNoLongerReadyRejectsPreviouslyRenderedButton() {
        assertNull(command(latestConnection = PlaybackConnectionState.Disconnected))
        assertNull(command(latestConnection = PlaybackConnectionState.Connecting))
        assertNull(command(latestConnection = PlaybackConnectionState.RecoverableError))
    }

    @Test fun oldDisconnectedScreenCannotSubmitAfterServiceReconnect() {
        assertNull(command(displayedConnection = PlaybackConnectionState.Disconnected))
    }

    @Test fun newSnapshotRevisionRejectsOldPlayOrPauseTap() {
        assertNull(command(latest = snapshot(revision = 4L)))
    }

    @Test fun sameRevisionButNewQueueRevisionRejectsOldNextTap() {
        assertNull(command(latest = snapshot(queueRevision = 6L),
            action = DrivingAction.Next))
    }

    @Test fun sameRevisionButAnotherOccurrenceRejectsOldPreviousTap() {
        assertNull(command(latest = snapshot(selected = 1, item = b),
            action = DrivingAction.Previous))
    }

    @Test fun playingNowInsteadOfPreviouslyPausedCannotBecomeUnwantedPause() {
        val shown = snapshot(playing = PlaybackState.Paused)
        val actual = snapshot(playing = PlaybackState.Playing)
        assertNull(command(shown = shown, latest = actual))
    }

    @Test fun pausedNowInsteadOfPreviouslyPlayingCannotBecomeUnwantedPlay() {
        val shown = snapshot(playing = PlaybackState.Playing)
        val actual = snapshot(playing = PlaybackState.Paused)
        assertNull(command(shown = shown, latest = actual))
    }

    @Test fun invalidCurrentSelectionNeverProducesTransportCommand() {
        val wrong = snapshot(item = b)
        assertNull(command(shown = wrong, action = DrivingAction.Next))
    }

    @Test fun noTrackCannotCreateAQueueOrRememberPlayLast() {
        val empty = snapshot(queue = emptyList(), selected = null, item = null)
        assertNull(command(shown = empty, action = DrivingAction.PlayPause))
        assertNull(command(shown = empty, action = DrivingAction.Previous))
    }

    @Test fun bufferingOrPlaybackErrorBlocksPrematurePlay() {
        val buffering = snapshot(playing = PlaybackState.Buffering)
        val error = snapshot(playing = PlaybackState.Error)
        assertNull(command(shown = buffering))
        assertNull(command(shown = error))
    }

    @Test fun disabledNextAndPreviousCapabilitiesAreHonored() {
        assertNull(command(shown = snapshot(next = false),
            action = DrivingAction.Next))
        assertNull(command(shown = snapshot(previous = false),
            action = DrivingAction.Previous))
    }

    @Test fun seekBackRequiresSeekabilityAndNonnegativePosition() {
        assertNull(command(shown = snapshot(seek = false),
            action = DrivingAction.SeekBack))
        assertNull(command(shown = snapshot(positionMs = -1L),
            action = DrivingAction.SeekBack))
        assertNull(command(shown = snapshot(durationMs = -1L),
            action = DrivingAction.SeekBack))
    }

    @Test fun capabilityChangeWithoutRevisionAlsoFailsClosed() {
        val shown = snapshot(next = true)
        val latest = snapshot(next = false)
        assertNull(command(shown = shown, latest = latest,
            action = DrivingAction.Next))
    }

    @Test fun confirmedItemAtQueueIndexAllowsNoDirectPlayerInstance() {
        val s = snapshot()
        assertEquals(PlaybackCommand.Next(cmdId),
            command(shown = s, latest = s, action = DrivingAction.Next))
    }
}
