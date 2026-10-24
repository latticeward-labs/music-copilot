package com.latticeward.musiccopilot.core.application.driving

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.core.result.AppError

/**
 * Issue #9 P02: PURE, side-effect-free Driving presentation/action policy.
 *
 * A complete PlaybackSnapshot and connection state come from the existing
 * application-scoped PlaybackGateway. This policy NEVER owns or constructs a
 * Player, MediaSession, playback queue, media resolver, Voice session or route
 * monitor. Real execution and out-of-order ACK safety remain in the one
 * service-owned command path (P06 integration).
 */
enum class DrivingViewState { Playing, Paused, NoTrack }

enum class DrivingStatusNotice {
    None, Connecting, Disconnected, ControllerError, Buffering, PlaybackError, CommandPending,
    Interruption
}

enum class DrivingAction { Previous, PlayPause, Next, SeekBack }

enum class DrivingActionLabel { Previous, Play, Pause, Next, SeekBack10Seconds }

enum class DrivingDisabledReason {
    NoCurrentTrack, ControllerNotReady, PlaybackUnavailable, CommandPending, Unsupported
}

/**
 * Explicit capability supplied by the P07 Voice/permission seam: never infer
 * permission, recording or speech-provider readiness from a playing track.
 */
enum class DrivingMicCapability { Unavailable, Available }

data class DrivingControlState(
    val action: DrivingAction,
    val label: DrivingActionLabel,
    val enabled: Boolean,
    val disabledReason: DrivingDisabledReason? = null,
)

data class DrivingPresentationState(
    val view: DrivingViewState,
    val notice: DrivingStatusNotice,
    val controls: List<DrivingControlState>,
    val showCurrentTrackMetadata: Boolean,
    val micEnabled: Boolean,
    val exitEnabled: Boolean = true,
) {
    fun control(action: DrivingAction): DrivingControlState =
        controls.first { it.action == action }
}

object DrivingPresentationPolicy {
    /**
     * Approved WF-07 order is fixed: Previous -> Play/Pause -> Next -> Seek back,
     * followed by the separate Mic and immediate Exit on the screen.
     */
    // WF-07 uses the existing service's approved -10s SeekBy transport contract.\n    // P02 intentionally does NOT import playback.command implementation: ADR-0008\n    // application code may depend only on the playback/api public port.\n    const val SEEK_BACK_MS = 10_000L\n\n    val transportOrder: List<DrivingAction> = listOf(
        DrivingAction.Previous,
        DrivingAction.PlayPause,
        DrivingAction.Next,
        DrivingAction.SeekBack,
    )

    /**
     * Treat an inconsistent queue/current occurrence as No Track rather than
     * displaying an unverified item. Normal Media3SnapshotProjector always
     * publishes currentItem and currentIndex from the same queue observation.
     */
    fun hasConfirmedCurrentTrack(snapshot: PlaybackSnapshot): Boolean {
        val index = snapshot.currentIndex ?: return false
        return index in snapshot.queue.indices &&
            snapshot.currentItem != null &&
            snapshot.queue[index] == snapshot.currentItem
    }

    fun project(
        connection: PlaybackConnectionState,
        snapshot: PlaybackSnapshot,
        pending: Boolean = false,
        micCapability: DrivingMicCapability = DrivingMicCapability.Unavailable,
    ): DrivingPresentationState {
        val hasTrack = hasConfirmedCurrentTrack(snapshot)
        val ready = connection == PlaybackConnectionState.Ready
        // An old cached service 'Playing' while the controller is disconnected
        // is context only, never proof that audio is currently playing.
        val view = when {
            !hasTrack -> DrivingViewState.NoTrack
            ready && snapshot.playbackState == PlaybackState.Playing ->
                DrivingViewState.Playing
            else -> DrivingViewState.Paused
        }
        val notice = when {
            connection == PlaybackConnectionState.Connecting -> DrivingStatusNotice.Connecting
            connection == PlaybackConnectionState.Disconnected -> DrivingStatusNotice.Disconnected
            connection == PlaybackConnectionState.RecoverableError ->
                DrivingStatusNotice.ControllerError
            pending -> DrivingStatusNotice.CommandPending
            snapshot.playbackState == PlaybackState.Error ->
                DrivingStatusNotice.PlaybackError
            snapshot.playbackState == PlaybackState.Buffering ->
                DrivingStatusNotice.Buffering
            snapshot.lastError is AppError.Interruption ->
                DrivingStatusNotice.Interruption
            else -> DrivingStatusNotice.None
        }
        val blocked = when {
            !ready -> DrivingDisabledReason.ControllerNotReady
            pending -> DrivingDisabledReason.CommandPending
            !hasTrack -> DrivingDisabledReason.NoCurrentTrack
            snapshot.playbackState == PlaybackState.Error ||
                snapshot.playbackState == PlaybackState.Buffering ->
                DrivingDisabledReason.PlaybackUnavailable
            else -> null
        }
        val controls = transportOrder.map { action ->
            val isSupported = when (action) {
                DrivingAction.Previous -> snapshot.canSkipPrevious
                DrivingAction.PlayPause -> true
                DrivingAction.Next -> snapshot.canSkipNext
                DrivingAction.SeekBack -> snapshot.canSeek &&
                    snapshot.positionMs >= 0L &&
                    (snapshot.durationMs == null || snapshot.durationMs >= 0L)
            }
            val reason = blocked ?: if (!isSupported) DrivingDisabledReason.Unsupported else null
            val label = when (action) {
                DrivingAction.Previous -> DrivingActionLabel.Previous
                DrivingAction.PlayPause ->
                    if (view == DrivingViewState.Playing) DrivingActionLabel.Pause
                    else DrivingActionLabel.Play
                DrivingAction.Next -> DrivingActionLabel.Next
                DrivingAction.SeekBack -> DrivingActionLabel.SeekBack10Seconds
            }
            DrivingControlState(action, label, enabled = reason == null, disabledReason = reason)
        }
        return DrivingPresentationState(
            view = view,
            notice = notice,
            controls = controls,
            // Metadata is separately resolved from the validated service MediaRef
            // only with OS-granted media access in P04/P05; never persisted here.
            showCurrentTrackMetadata = hasTrack && ready,
            micEnabled = micCapability == DrivingMicCapability.Available,
        )
    }

    /**
     * P02 command *decision*, not command execution. P06 must call this with
     * the freshest connection/snapshot and a newly allocated CommandId,
     * submit via PlaybackGateway.execute and await a real service ACK. The
     * service still checks audio-route/focus/manual-Play safety.
     */
    fun commandFor(
        connection: PlaybackConnectionState,
        snapshot: PlaybackSnapshot,
        pending: Boolean,
        action: DrivingAction,
        commandId: CommandId,
    ): PlaybackCommand? {
        if (!project(connection, snapshot, pending).control(action).enabled) return null
        return when (action) {
            DrivingAction.Previous -> PlaybackCommand.Previous(commandId)
            DrivingAction.PlayPause ->
                if (snapshot.playbackState == PlaybackState.Playing)
                    PlaybackCommand.Pause(commandId)
                else PlaybackCommand.Play(commandId)
            DrivingAction.Next -> PlaybackCommand.Next(commandId)
            DrivingAction.SeekBack -> PlaybackCommand.SeekBy(
                commandId, -SEEK_BACK_MS,
            )
        }
    }
}
