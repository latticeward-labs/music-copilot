package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.driving.DrivingPresentationState
import com.latticeward.musiccopilot.core.application.driving.DrivingRecoveryMessage
import com.latticeward.musiccopilot.core.application.driving.DrivingRecoveryPolicy
import com.latticeward.musiccopilot.core.application.driving.DrivingStatusNotice
import com.latticeward.musiccopilot.core.application.driving.DrivingViewState
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #9 P05: authored pure recovery-policy source cases. NOT EXECUTED.
 * Real Android/UI/route/notification and source compilation remain P13 QA.
 */
class Issue9Phase5DrivingRecoveryPolicyTest {
    private fun state(
        view: DrivingViewState,
        notice: DrivingStatusNotice = DrivingStatusNotice.None,
    ) = DrivingPresentationState(
        view = view,
        notice = notice,
        controls = emptyList(),
        showCurrentTrackMetadata = view != DrivingViewState.NoTrack,
        micEnabled = false,
    )

    private fun project(
        view: DrivingViewState,
        notice: DrivingStatusNotice = DrivingStatusNotice.None,
        error: AppError? = null,
        rejected: Boolean = false,
    ) = DrivingRecoveryPolicy.project(
        state = state(view, notice),
        lastError = error,
        requestFailed = rejected,
    )

    private fun interruption(code: String) = AppError.Interruption(
        code = code,
        recoverability = Recoverability.USER_ACTION_REQUIRED,
        recoveryAction = RecoveryAction.ManualResume,
    )

    private fun missingItem() = AppError.MediaItem(
        code = AppErrorCodes.MEDIA_ITEM_MISSING,
        recoverability = Recoverability.USER_ACTION_REQUIRED,
        recoveryAction = RecoveryAction.ManualFallback,
    )

    @Test fun playingIsNotAssignedAFalseRecoveryMessage() {
        val result = project(DrivingViewState.Playing)
        assertNull(result.message)
        assertTrue(result.showTransportControls)
        assertTrue(result.showArtworkAndMetadata)
        assertFalse(result.verifiedLastItemAvailable)
    }

    @Test fun pausedRequiresAnExplicitManualResume() {
        val result = project(DrivingViewState.Paused)
        assertEquals(DrivingRecoveryMessage.PausedManualResume, result.message)
        assertTrue(result.showTransportControls)
        assertTrue(result.showArtworkAndMetadata)
    }

    @Test fun noTrackRemovesInvalidTransportsAndMetadata() {
        val result = project(DrivingViewState.NoTrack)
        assertEquals(DrivingRecoveryMessage.NoTrackBrowse, result.message)
        assertFalse(result.showTransportControls)
        assertFalse(result.showArtworkAndMetadata)
        assertTrue(result.showBrowseExplanation)
        assertFalse(result.verifiedLastItemAvailable)
    }

    @Test fun noTrackNeverClaimsRememberedPlayableItemEvenWhenRequestRejected() {
        val result = project(DrivingViewState.NoTrack, rejected = true)
        assertEquals(DrivingRecoveryMessage.NoTrackBrowse, result.message)
        assertFalse(result.verifiedLastItemAvailable)
    }

    @Test fun noTrackWithFailedMediaItemUsesExplicitUnavailableCopy() {
        val result = project(DrivingViewState.NoTrack, error = missingItem())
        assertEquals(DrivingRecoveryMessage.NoTrackItemUnavailable, result.message)
        assertFalse(result.showTransportControls)
    }

    @Test fun connectingControllerDoesNotSuggestResume() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.Connecting,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackConnecting, result.message)
    }

    @Test fun disconnectedControllerIsNotFalsePlayingSuccess() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.Disconnected,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackDisconnected, result.message)
    }

    @Test fun recoverableControllerErrorIsDistinctFromTrackFailure() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.ControllerError,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackControllerUnavailable, result.message)
    }

    @Test fun pendingTakesPrecedenceOverEarlierServiceItemFailure() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.CommandPending, missingItem(),
        )
        assertEquals(DrivingRecoveryMessage.CommandPending, result.message)
    }

    @Test fun bufferingIsNeverAdvertisedAsCompletedPlaying() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.Buffering,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackBuffering, result.message)
    }

    @Test fun playbackErrorUsesServiceErrorWithoutExposingRawThrowable() {
        val result = project(
            DrivingViewState.Paused, DrivingStatusNotice.PlaybackError,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackFailed, result.message)
    }

    @Test fun outputRouteInvalidationUsesSafeManualGuidance() {
        val result = project(
            DrivingViewState.Paused, error = interruption(AppErrorCodes.ROUTE_INVALIDATED),
        )
        assertEquals(DrivingRecoveryMessage.RouteOutputChanged, result.message)
    }

    @Test fun unrecoveredFocusUsesSafePausedGuidance() {
        val result = project(
            DrivingViewState.Paused, error = interruption(AppErrorCodes.FOCUS_NOT_RECOVERED),
        )
        assertEquals(DrivingRecoveryMessage.AudioFocusUnavailable, result.message)
    }

    @Test fun callOrRouteSafePausedDoesNotClaimAutomaticResumption() {
        val result = project(
            DrivingViewState.Paused,
            error = interruption(AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED),
        )
        assertEquals(DrivingRecoveryMessage.InterruptionSafePaused, result.message)
    }

    @Test fun missingLocalItemProducesMediaFailureWithoutAnotherDestination() {
        val result = project(
            DrivingViewState.Paused, error = missingItem(),
        )
        assertEquals(DrivingRecoveryMessage.LocalItemUnavailable, result.message)
        assertTrue(result.showTransportControls)
    }

    @Test fun actualRejectedCommandIsVisibleButDoesNotSetPlaybackState() {
        val result = project(DrivingViewState.Paused, rejected = true)
        assertEquals(DrivingRecoveryMessage.CommandRejected, result.message)
    }

    @Test fun realServiceRouteReasonHasPriorityOverGenericRequestFailure() {
        val result = project(
            DrivingViewState.Paused,
            error = interruption(AppErrorCodes.ROUTE_INVALIDATED),
            rejected = true,
        )
        assertEquals(DrivingRecoveryMessage.RouteOutputChanged, result.message)
    }

    @Test fun controllerDisconnectionOverridesStaleRouteReason() {
        val result = project(
            DrivingViewState.Paused,
            notice = DrivingStatusNotice.Disconnected,
            error = interruption(AppErrorCodes.ROUTE_INVALIDATED),
        )
        assertEquals(DrivingRecoveryMessage.PlaybackDisconnected, result.message)
    }

    @Test fun noTrackWhileDisconnectedShowsConnectionProblemNotFalseEmptySuccess() {
        val result = project(
            DrivingViewState.NoTrack, DrivingStatusNotice.Disconnected,
        )
        assertEquals(DrivingRecoveryMessage.PlaybackDisconnected, result.message)
        assertFalse(result.showTransportControls)
        assertFalse(result.verifiedLastItemAvailable)
    }

    @Test fun acknowledgedPlayingSuppressesStaleMediaItemFailure() {
        val result = project(DrivingViewState.Playing, error = missingItem())
        assertNull(result.message)
        assertTrue(result.showTransportControls)
    }

    @Test fun playingAfterRouteRecoverySuppressesOldSafePausedMessage() {
        val result = project(
            DrivingViewState.Playing, DrivingStatusNotice.Interruption,
            interruption(AppErrorCodes.ROUTE_INVALIDATED),
        )
        assertNull(result.message)
    }
}
