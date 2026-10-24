package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.navigation.Destination
import com.latticeward.musiccopilot.app.navigation.DrivingNavigationPolicy
import com.latticeward.musiccopilot.app.navigation.DrivingNavigationState
import com.latticeward.musiccopilot.core.application.bluetooth.*
import com.latticeward.musiccopilot.core.application.driving.*
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import org.junit.Assert.*
import org.junit.Test

/**
 * P12 cross-feature source regression cases. Authored but never compiled or
 * executed during P00–P12; real app/device verification belongs to P13 owner.
 *
 * These cases combine existing immutable policies only; no additional
 * player, media route, Bluetooth transport, microphone or UI destination.
 */
class Issue9Phase12CrossFeatureSafetyContractsTest {
    private val ref = MediaRef("audio:901")
    private val saved = requireNotNull(BluetoothSelectionToken.create(
        "02:AB:CD:EF:00:12", ByteArray(16) { 5 }
    ))
    private fun snapshot(
        playback: PlaybackState = PlaybackState.Paused,
        item: MediaRef? = ref,
        error: AppError? = null,
    ) = PlaybackSnapshot(
        revision = 17L, queueRevision = 4L, playbackState = playback,
        queue = if (item != null) listOf(ref) else emptyList(),
        currentIndex = if (item != null) 0 else null,
        currentItem = item, positionMs = 1800L, durationMs = 40000L,
        canSeek = item != null, canSkipPrevious = false, canSkipNext = false,
        lastError = error,
    )
    private fun interruption(code: String) = AppError.Interruption(
        code, Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualResume,
    )
    private fun gate() = SelectedBluetoothConnectionGate().also { it.start(saved) }
    private fun connected(g: SelectedBluetoothConnectionGate): SelectedAudioConnectionState {
        g.observe(saved, SelectedAudioConnectionStatus.Disconnected,
            BluetoothObservationCause.Initial)
        return g.observe(saved, SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.ProfileConnectionEvent)
    }
    private fun offer(
        event: VerifiedAudioReconnectEvent,
        proof: SelectedAudioConnectionState,
        old: BluetoothEntrySuggestionState = BluetoothEntrySuggestionState(),
        epoch: Long = 7L,
        eligible: Boolean = true,
    ) = BluetoothEntrySuggestionPolicy.onVerifiedReconnect(
        old, event, proof, epoch, eligible, 1000L
    )

    @Test fun p0ManualLibraryEntryNeverRequiresBluetoothOrTrack() {
        val active = DrivingNavigationPolicy.enter(
            DrivingNavigationState(), Destination.Library, true
        )
        assertTrue(active.active)
        assertEquals(Destination.Library, active.origin)
    }

    @Test fun p0ManualFullPlayerEntryPreservesSourceAndExitOrigin() {
        val active = DrivingNavigationPolicy.enter(
            DrivingNavigationState(), Destination.FullPlayer, true
        )
        assertEquals(Destination.FullPlayer,
            DrivingNavigationPolicy.destinationAfterExit(active))
        assertFalse(DrivingNavigationPolicy.exit(active).active)
    }

    @Test fun deniedAppRouteCannotForceDrivingEvenWithBluetoothProfile() {
        val proof = connected(gate())
        assertTrue(proof.selectedConnected)
        assertFalse(DrivingNavigationPolicy.enter(
            DrivingNavigationState(), Destination.Library, false
        ).active)
    }

    @Test fun noTrackNeverShowsMetadataAfterPermissionOrQueueLoss() {
        val state = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot(item = null)
        )
        assertEquals(DrivingViewState.NoTrack, state.view)
        assertFalse(state.showCurrentTrackMetadata)
        assertTrue(state.controls.none { it.enabled })
        val recovery = DrivingRecoveryPolicy.project(state, null, false)
        assertFalse(recovery.verifiedLastItemAvailable)
        assertFalse(recovery.showArtworkAndMetadata)
    }

    @Test fun cachedPlayingAfterControllerDisconnectNeverLooksAudible() {
        val state = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Disconnected,
            snapshot(playback = PlaybackState.Playing)
        )
        assertEquals(DrivingViewState.Paused, state.view)
        assertTrue(state.controls.none { it.enabled })
        assertFalse(state.showCurrentTrackMetadata)
    }

    @Test fun routeLostSnapshotRequiresManualUserResumption() {
        val error = interruption(AppErrorCodes.ROUTE_INVALIDATED)
        val state = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot(error = error)
        )
        val help = DrivingRecoveryPolicy.project(state, error, false)
        assertEquals(DrivingViewState.Paused, state.view)
        assertEquals(DrivingRecoveryMessage.RouteOutputChanged, help.message)
    }

    @Test fun callInterruptionShowsSafePauseWithoutAutoplay() {
        val error = interruption(AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED)
        val state = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot(error = error)
        )
        assertEquals(DrivingRecoveryMessage.InterruptionSafePaused,
            DrivingRecoveryPolicy.project(state, error, false).message)
        assertEquals(DrivingViewState.Paused, state.view)
    }

    @Test fun focusNotRecoveredShowsManualGuidance() {
        val error = interruption(AppErrorCodes.FOCUS_NOT_RECOVERED)
        val state = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot(error = error)
        )
        assertEquals(DrivingRecoveryMessage.AudioFocusUnavailable,
            DrivingRecoveryPolicy.project(state, error, false).message)
    }

    @Test fun incomingBluetoothConnectionNeverChangesServicePausedProjection() {
        val before = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot()
        )
        val proof = connected(gate())
        val after = DrivingPresentationPolicy.project(
            PlaybackConnectionState.Ready, snapshot()
        )
        assertTrue(proof.canOfferEntry)
        assertEquals(DrivingViewState.Paused, before.view)
        assertEquals(before, after)
    }

    @Test fun initialAlreadyConnectedProfileNeverOffersDriving() {
        val g = gate()
        val status = g.observe(saved, SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.Initial)
        assertFalse(status.canOfferEntry)
        val attempt = offer(VerifiedAudioReconnectEvent(1L, 7L), status)
        assertNull(attempt.pending)
    }

    @Test fun reconnectMayOnlyCreateAnInvitationNotNavigation() {
        val evidence = connected(gate())
        val event = VerifiedAudioReconnectEvent(evidence.connectionEventId, 7L)
        val invitation = offer(event, evidence)
        assertEquals(event, invitation.pending)
        assertFalse(DrivingNavigationState().active)
    }

    @Test fun userDeclineLeavesManualDrivingStateUnchanged() {
        val evidence = connected(gate())
        val invitation = offer(VerifiedAudioReconnectEvent(
            evidence.connectionEventId, 7L
        ), evidence)
        val dropped = BluetoothEntrySuggestionPolicy.notNow(invitation, 1000L)
        assertNull(dropped.pending)
        assertEquals(301000L, dropped.declineCooldownUntilMs)
        assertFalse(DrivingNavigationState().active)
    }

    @Test fun profileLossImmediatelyInvalidatesWaitingInvitation() {
        val g = gate()
        val evidence = connected(g)
        val invitation = offer(VerifiedAudioReconnectEvent(
            evidence.connectionEventId, 7L
        ), evidence)
        val lost = g.observe(saved, SelectedAudioConnectionStatus.Disconnected,
            BluetoothObservationCause.ProfileConnectionEvent)
        assertNull(BluetoothEntrySuggestionPolicy.reconcile(
            invitation, lost, 7L, true
        ).pending)
    }

    @Test fun bluetoothRevocationDoesNotBlockIndependentManualDriving() {
        val g = gate()
        val connected = connected(g)
        val invite = offer(VerifiedAudioReconnectEvent(
            connected.connectionEventId, 7L
        ), connected)
        val revoked = g.observe(saved, SelectedAudioConnectionStatus.PermissionRequired,
            BluetoothObservationCause.ExternalRecheck)
        assertNull(BluetoothEntrySuggestionPolicy.reconcile(
            invite, revoked, 7L, true
        ).pending)
        assertTrue(DrivingNavigationPolicy.enter(
            DrivingNavigationState(), Destination.Library, true
        ).active)
    }

    @Test fun selectionEpochChangeDiscardsUnclaimedPriorInvitation() {
        val proof = connected(gate())
        val old = offer(VerifiedAudioReconnectEvent(proof.connectionEventId, 7L), proof)
        assertNull(BluetoothEntrySuggestionPolicy.reconcile(
            old, proof, 8L, true
        ).pending)
    }

    @Test fun originalPlaybackIsNotAutoPausedOrPlayedByDismissal() {
        val before = snapshot(PlaybackState.Playing)
        val proof = connected(gate())
        val offer = offer(VerifiedAudioReconnectEvent(proof.connectionEventId, 7L), proof)
        BluetoothEntrySuggestionPolicy.notNow(offer, 1000L)
        val after = snapshot(PlaybackState.Playing)
        assertEquals(before, after)
    }
}
