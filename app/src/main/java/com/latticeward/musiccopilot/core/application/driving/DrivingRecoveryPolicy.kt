package com.latticeward.musiccopilot.core.application.driving

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes

/**
 * Issue #9 P05 — side-effect-free, single-layer driving recovery projection.
 * The only inputs are service/connection-derived state and a read-only
 * acknowledgment failure flag from the existing shared PlayerViewModel.
 *
 * Never starts playback, moves a queue, requests Android permissions, launches
 * Settings, fakes Play Last, reconnects an audio route, or marks command success.
 */
enum class DrivingRecoveryMessage {
    NoTrackBrowse,
    NoTrackItemUnavailable,
    PausedManualResume,
    PlaybackConnecting,
    PlaybackDisconnected,
    PlaybackControllerUnavailable,
    PlaybackBuffering,
    PlaybackFailed,
    LocalItemUnavailable,
    RouteOutputChanged,
    AudioFocusUnavailable,
    InterruptionSafePaused,
    CommandPending,
    CommandRejected,
}

data class DrivingRecoveryPresentation(
    val message: DrivingRecoveryMessage?,
    val showTransportControls: Boolean,
    val showArtworkAndMetadata: Boolean,
    val showBrowseExplanation: Boolean,
    val verifiedLastItemAvailable: Boolean = false,
)

object DrivingRecoveryPolicy {
    fun project(
        state: DrivingPresentationState,
        lastError: AppError?,
        requestFailed: Boolean,
    ): DrivingRecoveryPresentation {
        val noTrack = state.view == DrivingViewState.NoTrack
        val message = when {
            // The approved CC-07 No Track frame owns the terminal state and
            // never shows a non-validated remembered Play Last button.
            // Controller connectivity is source-authoritative, including
            // when no current item can yet be verified.
            state.notice == DrivingStatusNotice.Connecting ->
                DrivingRecoveryMessage.PlaybackConnecting
            state.notice == DrivingStatusNotice.Disconnected ->
                DrivingRecoveryMessage.PlaybackDisconnected
            state.notice == DrivingStatusNotice.ControllerError ->
                DrivingRecoveryMessage.PlaybackControllerUnavailable
            state.notice == DrivingStatusNotice.CommandPending ->
                DrivingRecoveryMessage.CommandPending

            noTrack && lastError is AppError.MediaItem ->
                DrivingRecoveryMessage.NoTrackItemUnavailable
            noTrack -> DrivingRecoveryMessage.NoTrackBrowse

            // When the service is truly Playing, an earlier item failure
            // or old interruption must not be presented as a current pause.
            state.view == DrivingViewState.Playing && !requestFailed &&
                (state.notice == DrivingStatusNotice.None ||
                    state.notice == DrivingStatusNotice.Interruption) -> null

            // Remaining service notices and typed errors follow priority.
            // Route/focus loss remains safe-paused; only the real playback
            // service may revalidate a later explicit Play (P06).
            lastError is AppError.Interruption &&
                lastError.code == AppErrorCodes.ROUTE_INVALIDATED ->
                DrivingRecoveryMessage.RouteOutputChanged
            lastError is AppError.Interruption &&
                lastError.code == AppErrorCodes.FOCUS_NOT_RECOVERED ->
                DrivingRecoveryMessage.AudioFocusUnavailable
            lastError is AppError.Interruption ->
                DrivingRecoveryMessage.InterruptionSafePaused

            lastError is AppError.MediaItem ->
                DrivingRecoveryMessage.LocalItemUnavailable
            state.notice == DrivingStatusNotice.PlaybackError ||
                lastError is AppError.Playback ->
                DrivingRecoveryMessage.PlaybackFailed
            state.notice == DrivingStatusNotice.Buffering ->
                DrivingRecoveryMessage.PlaybackBuffering

            requestFailed -> DrivingRecoveryMessage.CommandRejected
            state.view == DrivingViewState.Paused ->
                DrivingRecoveryMessage.PausedManualResume
            else -> null
        }
        return DrivingRecoveryPresentation(
            message = message,
            showTransportControls = !noTrack,
            showArtworkAndMetadata = !noTrack,
            showBrowseExplanation = noTrack,
            // There is no P05 service-owned verified last-item contract:
            // it must remain absent, not optimistic or synthesized.
            verifiedLastItemAvailable = false,
        )
    }
}
