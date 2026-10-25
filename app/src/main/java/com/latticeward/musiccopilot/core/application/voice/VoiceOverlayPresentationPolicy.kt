package com.latticeward.musiccopilot.core.application.voice

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/** P09: pure WF-08 projection shared by Driving and Full Player. */
enum class VoiceOverlayStatus {
    Hidden, PermissionRationale, PermissionPending, Listening,
    Processing, ConfirmationUnavailable, Executed,
    Timeout, NotRecognized, Unavailable,
}

enum class VoiceOverlayExit { CancelCapture, DismissPermission, ReturnToManual }

data class VoiceOverlayPresentation(
    val status: VoiceOverlayStatus,
    val showRetry: Boolean,
    val showPermissionContinue: Boolean,
    val exit: VoiceOverlayExit,
) {
    val visible: Boolean get() = status != VoiceOverlayStatus.Hidden
}

/**
 * The native Android permission dialog and application VoiceCoordinator remain
 * the ONLY sources of truth. This presenter never starts STT, assumes a grant,
 * dispatches Media3 commands or saves transcript/voice state.
 */
object VoiceOverlayPresentationPolicy {
    fun project(state: VoiceSessionState, permissionStage: VoicePracticeStage):
        VoiceOverlayPresentation {
        if (permissionStage == VoicePracticeStage.Rationale) {
            return VoiceOverlayPresentation(
                VoiceOverlayStatus.PermissionRationale, false, true,
                VoiceOverlayExit.DismissPermission,
            )
        }
        if (permissionStage == VoicePracticeStage.AwaitingAndroid) {
            return VoiceOverlayPresentation(
                VoiceOverlayStatus.PermissionPending, false, false,
                VoiceOverlayExit.DismissPermission,
            )
        }
        return when (state) {
            VoiceSessionState.Idle -> VoiceOverlayPresentation(
                VoiceOverlayStatus.Hidden, false, false, VoiceOverlayExit.ReturnToManual,
            )
            is VoiceSessionState.Listening -> VoiceOverlayPresentation(
                VoiceOverlayStatus.Listening, false, false, VoiceOverlayExit.CancelCapture,
            )
            is VoiceSessionState.Processing -> VoiceOverlayPresentation(
                VoiceOverlayStatus.Processing, false, false, VoiceOverlayExit.CancelCapture,
            )
            is VoiceSessionState.Confirming -> VoiceOverlayPresentation(
                VoiceOverlayStatus.ConfirmationUnavailable, false, false,
                VoiceOverlayExit.ReturnToManual,
            )
            is VoiceSessionState.Executed -> VoiceOverlayPresentation(
                VoiceOverlayStatus.Executed, false, false, VoiceOverlayExit.ReturnToManual,
            )
            is VoiceSessionState.Failed -> VoiceOverlayPresentation(
                when (state.error.code) {
                    AppErrorCodes.VOICE_TIMEOUT -> VoiceOverlayStatus.Timeout
                    AppErrorCodes.VOICE_NO_MATCH -> VoiceOverlayStatus.NotRecognized
                    else -> VoiceOverlayStatus.Unavailable
                }, state.canRetry, false, VoiceOverlayExit.ReturnToManual,
            )
        }
    }
}
