package com.latticeward.musiccopilot.playback.interruption

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction

/**
 * P10 closed vocabulary for recoverable, privacy-safe interruption notices.
 * No raw MediaRef, file/title/path, audio device identity, Bluetooth address,
 * telephone status, provider exception, transcript or focus source is accepted.
 * This is a view of the existing service's sole PlaybackSnapshot.lastError,
 * NOT another error bus or telemetry sink.
 */
internal enum class InterruptionNotice {
    FOCUS_TEMPORARY,
    FOCUS_UNAVAILABLE,
    ROUTE_UNSAFE,
    CLEARED,
}

internal object InterruptionNoticePolicy {
    fun fromAccepted(
        event: InterruptionEvent,
        transition: InterruptionTransition,
    ): InterruptionNotice? {
        val effects = transition.effects
        return when {
            event is InterruptionEvent.RouteDisconnected &&
                InterruptionEffect.CANCEL_VOICE_FOR_ROUTE in effects ->
                InterruptionNotice.ROUTE_UNSAFE

            (event is InterruptionEvent.FocusLostPermanent ||
                event is InterruptionEvent.FocusAutoResumeRejected) &&
                InterruptionEffect.CANCEL_VOICE_FOR_FOCUS in effects ->
                InterruptionNotice.FOCUS_UNAVAILABLE

            event is InterruptionEvent.FocusLostTransient &&
                InterruptionEffect.CANCEL_VOICE_FOR_FOCUS in effects ->
                InterruptionNotice.FOCUS_TEMPORARY

            // A safe observed return, not just a spurious/reordered gain.
            event is InterruptionEvent.FocusRestored &&
                transition.state.routeValid &&
                transition.state.focus == FocusCondition.AVAILABLE &&
                !transition.state.requiresExplicitPlay ->
                InterruptionNotice.CLEARED

            event is InterruptionEvent.PlayRequested &&
                InterruptionEffect.ALLOW_VERIFIED_USER_PLAY in effects ->
                InterruptionNotice.CLEARED

            event is InterruptionEvent.PlaybackObserved &&
                event.isPlaying &&
                transition.state.focus == FocusCondition.AVAILABLE &&
                transition.state.routeValid &&
                !transition.state.requiresExplicitPlay &&
                InterruptionEffect.PAUSE_SERVICE_PLAYER !in effects ->
                InterruptionNotice.CLEARED

            else -> null
        }
    }

    /** Stable codes already approved by AppErrorCodes and Media3 wire allowlist. */
    fun error(notice: InterruptionNotice): AppError.Interruption? = when (notice) {
        InterruptionNotice.FOCUS_TEMPORARY -> AppError.Interruption(
            AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED,
            Recoverability.RECOVERABLE,
            RecoveryAction.ManualResume,
        )
        InterruptionNotice.FOCUS_UNAVAILABLE -> AppError.Interruption(
            AppErrorCodes.FOCUS_NOT_RECOVERED,
            Recoverability.USER_ACTION_REQUIRED,
            RecoveryAction.ManualResume,
        )
        InterruptionNotice.ROUTE_UNSAFE -> AppError.Interruption(
            AppErrorCodes.ROUTE_INVALIDATED,
            Recoverability.USER_ACTION_REQUIRED,
            RecoveryAction.ManualResume,
        )
        InterruptionNotice.CLEARED -> null
    }
}
