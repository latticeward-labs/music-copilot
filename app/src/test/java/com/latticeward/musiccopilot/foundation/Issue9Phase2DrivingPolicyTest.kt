package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.driving.DrivingAction
import com.latticeward.musiccopilot.core.application.driving.DrivingActionLabel
import com.latticeward.musiccopilot.core.application.driving.DrivingDisabledReason
import com.latticeward.musiccopilot.core.application.driving.DrivingMicCapability
import com.latticeward.musiccopilot.core.application.driving.DrivingPresentationPolicy as Policy
import com.latticeward.musiccopilot.core.application.driving.DrivingStatusNotice
import com.latticeward.musiccopilot.core.application.driving.DrivingViewState
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import org.junit.Assert.*
import org.junit.Test

/**
 * Authored in #9 P02: PURE source tests, NOT EXECUTED at this phase.
 * They are neither Android/instrumentation nor real Media3/device QA.
 */
class Issue9Phase2DrivingPolicyTest {
    private val first = MediaRef("audio:101")
    private val second = MediaRef("audio:102")
    private val id = CommandId("driving-action-1")

    private fun snapshot(
        state: PlaybackState = PlaybackState.Playing,
        items: List<MediaRef> = listOf(first, second),
        selected: Int? = 0,
        item: MediaRef? = selected?.let(items::get),
        position: Long = 15_000L,
        duration: Long? = 100_000L,
        seek: Boolean = true,
        previous: Boolean = true,
        next: Boolean = true,
    ) = PlaybackSnapshot(
        revision = 7L,
        queueRevision = 2L,
        playbackState = state,
        queue = items,
        currentIndex = selected,
        currentItem = item,
        positionMs = position,
        durationMs = duration,
        canSeek = seek,
        canSkipPrevious = previous,
        canSkipNext = next,
    )

    private fun state(
        snapshot: PlaybackSnapshot = snapshot(),
        connection: PlaybackConnectionState = PlaybackConnectionState.Ready,
        pending: Boolean = false,
        mic: DrivingMicCapability = DrivingMicCapability.Unavailable,
    ) = Policy.project(connection, snapshot, pending, mic)

    @Test fun playingUsesOnlyConfirmedServicePlayingSnapshot() {
        val p = state()
        assertEquals(DrivingViewState.Playing, p.view)
        assertEquals(DrivingActionLabel.Pause, p.control(DrivingAction.PlayPause).label)
        assertTrue(p.showCurrentTrackMetadata)
        assertTrue(p.exitEnabled)
    }

    @Test fun pausedNeverStartsPlayingOnProjection() {
        val p = state(snapshot(state = PlaybackState.Paused))
        assertEquals(DrivingViewState.Paused, p.view)
        assertEquals(DrivingActionLabel.Play, p.control(DrivingAction.PlayPause).label)
        assertTrue(p.control(DrivingAction.PlayPause).enabled)
    }

    @Test fun endedWithTrackRemainsPausedAndOffersExplicitPlay() {
        val p = state(snapshot(state = PlaybackState.Ended))
        assertEquals(DrivingViewState.Paused, p.view)
        assertEquals(DrivingActionLabel.Play, p.control(DrivingAction.PlayPause).label)
    }

    @Test fun idleWithTrackIsNotFakePlaying() {
        assertEquals(DrivingViewState.Paused, state(snapshot(state = PlaybackState.Idle)).view)
    }

    @Test fun genuineEmptyQueueIsNoTrackAndHasNoEnabledTransports() {
        val p = state(snapshot(items = emptyList(), selected = null, item = null))
        assertEquals(DrivingViewState.NoTrack, p.view)
        assertFalse(p.showCurrentTrackMetadata)
        assertTrue(p.controls.none { it.enabled })
        assertTrue(p.controls.all { it.disabledReason == DrivingDisabledReason.NoCurrentTrack })
        assertTrue(p.exitEnabled)
    }

    @Test fun mismatchedCurrentItemFailsClosedRatherThanShowsWrongSong() {
        val p = state(snapshot(item = second))
        assertEquals(DrivingViewState.NoTrack, p.view)
        assertTrue(p.controls.none { it.enabled })
        assertFalse(Policy.hasConfirmedCurrentTrack(snapshot(item = second)))
    }

    @Test fun missingOrOutOfBoundsIndexIsNoTrackEvenWhenPlaybackSaysPlaying() {
        val cases = listOf(
            snapshot(selected = null, item = first),
            snapshot(selected = 7, item = first),
            snapshot(selected = -1, item = first),
        )
        cases.forEach {
            assertFalse(Policy.hasConfirmedCurrentTrack(it))
            assertEquals(DrivingViewState.NoTrack, state(it).view)
        }
    }

    @Test fun disconnectedCachedPlayingIsPausedAndCannotDispatch() {
        val p = state(connection = PlaybackConnectionState.Disconnected)
        assertEquals(DrivingViewState.Paused, p.view)
        assertEquals(DrivingStatusNotice.Disconnected, p.notice)
        assertFalse(p.showCurrentTrackMetadata)
        assertTrue(p.controls.none { it.enabled })
        assertEquals(DrivingDisabledReason.ControllerNotReady,
            p.control(DrivingAction.PlayPause).disabledReason)
        assertNull(Policy.commandFor(
            PlaybackConnectionState.Disconnected, snapshot(), false, DrivingAction.PlayPause, id,
        ))
    }

    @Test fun connectingAndRecoverableConnectionExposeTruthfulNotices() {
        val connecting = state(connection = PlaybackConnectionState.Connecting)
        val failed = state(connection = PlaybackConnectionState.RecoverableError)
        assertEquals(DrivingStatusNotice.Connecting, connecting.notice)
        assertEquals(DrivingStatusNotice.ControllerError, failed.notice)
        assertEquals(DrivingViewState.Paused, connecting.view)
        assertTrue(connecting.controls.none { it.enabled })
        assertTrue(failed.controls.none { it.enabled })
    }

    @Test fun pendingPreventsDoubleDispatchWithImmediateExitStillEnabled() {
        val p = state(pending = true)
        assertEquals(DrivingStatusNotice.CommandPending, p.notice)
        assertTrue(p.controls.none { it.enabled })
        assertEquals(DrivingDisabledReason.CommandPending,
            p.control(DrivingAction.Next).disabledReason)
        assertTrue(p.exitEnabled)
        assertNull(Policy.commandFor(
            PlaybackConnectionState.Ready, snapshot(), true, DrivingAction.Next, id,
        ))
    }

    @Test fun bufferingIsNeverPresentedAsAudiblePlaying() {
        val p = state(snapshot(state = PlaybackState.Buffering))
        assertEquals(DrivingViewState.Paused, p.view)
        assertEquals(DrivingStatusNotice.Buffering, p.notice)
        assertTrue(p.controls.none { it.enabled })
        assertEquals(DrivingDisabledReason.PlaybackUnavailable,
            p.control(DrivingAction.PlayPause).disabledReason)
    }

    @Test fun errorIsPausedWithExplicitErrorAndNoUnsafeTransport() {
        val p = state(snapshot(state = PlaybackState.Error))
        assertEquals(DrivingViewState.Paused, p.view)
        assertEquals(DrivingStatusNotice.PlaybackError, p.notice)
        assertTrue(p.controls.none { it.enabled })
        assertTrue(p.exitEnabled)
    }

    @Test fun transportOrderAndLabelsAreDeterministicFourButtons() {
        val p = state()
        assertEquals(listOf(
            DrivingAction.Previous, DrivingAction.PlayPause,
            DrivingAction.Next, DrivingAction.SeekBack,
        ), p.controls.map { it.action })
        assertEquals(listOf(
            DrivingActionLabel.Previous, DrivingActionLabel.Pause,
            DrivingActionLabel.Next, DrivingActionLabel.SeekBack10Seconds,
        ), p.controls.map { it.label })
    }

    @Test fun queueEdgeCapabilitiesAreStrictlyHonored() {
        val snap = snapshot(previous = false, next = false)
        val p = state(snap)
        assertFalse(p.control(DrivingAction.Previous).enabled)
        assertFalse(p.control(DrivingAction.Next).enabled)
        assertEquals(DrivingDisabledReason.Unsupported,
            p.control(DrivingAction.Next).disabledReason)
        assertTrue(p.control(DrivingAction.PlayPause).enabled)
        assertNull(Policy.commandFor(PlaybackConnectionState.Ready, snap,
            false, DrivingAction.Next, id))
    }

    @Test fun seekCapabilityAndInvalidPositionDisableSeekBack() {
        val denied = snapshot(seek = false)
        val invalid = snapshot(position = -1L)
        assertFalse(state(denied).control(DrivingAction.SeekBack).enabled)
        assertFalse(state(invalid).control(DrivingAction.SeekBack).enabled)
        assertNull(Policy.commandFor(PlaybackConnectionState.Ready,
            denied, false, DrivingAction.SeekBack, id))
    }

    @Test fun previousNextAndPauseMapToActualTypedServiceCommands() {
        val snap = snapshot()
        assertEquals(PlaybackCommand.Previous(id), Policy.commandFor(
            PlaybackConnectionState.Ready, snap, false, DrivingAction.Previous, id))
        assertEquals(PlaybackCommand.Next(id), Policy.commandFor(
            PlaybackConnectionState.Ready, snap, false, DrivingAction.Next, id))
        assertEquals(PlaybackCommand.Pause(id), Policy.commandFor(
            PlaybackConnectionState.Ready, snap, false, DrivingAction.PlayPause, id))
    }

    @Test fun pausedPlayIsExplicitPrivateCommandNotSystemAutoPlay() {
        val command = Policy.commandFor(PlaybackConnectionState.Ready,
            snapshot(state = PlaybackState.Paused), false, DrivingAction.PlayPause, id)
        assertEquals(PlaybackCommand.Play(id), command)
        assertTrue(command is PlaybackCommand.Play)
        // Only the service decides whether route/focus allows actual Play.
    }

    @Test fun seekBackIsExactlyTenSecondsViaTypedServiceCommand() {
        val s = snapshot()
        assertEquals(PlaybackCommand.SeekBy(id, -10_000L), Policy.commandFor(
            PlaybackConnectionState.Ready, s, false, DrivingAction.SeekBack, id))
        // The existing service clamps below zero. No UI clock or direct player.
    }

    @Test fun zeroPositionStillUsesServiceClampingWithoutNegativeSeekTarget() {
        val s = snapshot(position = 0L)
        assertEquals(PlaybackCommand.SeekBy(id, -10_000L), Policy.commandFor(
            PlaybackConnectionState.Ready, s, false, DrivingAction.SeekBack, id))
    }

    @Test fun voiceAvailabilityIsIndependentOfPlaybackAndDefaultIsUnavailable() {
        val noTrack = snapshot(items = emptyList(), selected = null, item = null)
        assertFalse(state(noTrack).micEnabled)
        assertTrue(state(noTrack, mic = DrivingMicCapability.Available).micEnabled)
        assertTrue(state(noTrack, mic = DrivingMicCapability.Available).exitEnabled)
        // No VoiceSessionCoordinator.start is invoked by a projection.
    }

    @Test fun transitionAfterServiceAckUsesNewSnapshotOnly() {
        val old = state(snapshot(state = PlaybackState.Paused))
        val confirmed = state(snapshot(state = PlaybackState.Playing))
        assertEquals(DrivingViewState.Paused, old.view)
        assertEquals(DrivingViewState.Playing, confirmed.view)
        // No local flag turns Paused into Playing absent a fresh snapshot.
    }
}
