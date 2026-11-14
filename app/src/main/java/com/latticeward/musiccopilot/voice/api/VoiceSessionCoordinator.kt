package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.flow.StateFlow

enum class VoiceOrigin { Player, Driving }

enum class VoiceCancelReason {
    UserBack, DrivingExit, ReplacedByNewSession, PermissionLost,
    CallOrFocusInvalidated, RouteInvalidated, OriginInvalidated
}

/**
 * Display-only, never persisted/logged. The coordinator publishes a stable
 * copied option list per token; UI passes optionIndex back without repository lookup.
 */
data class VoiceConfirmationOption(
    val optionIndex: Int,
    val displayLabel: String,
) {
    // Labels are private local artist metadata; never expose through diagnostics.
    override fun toString(): String = "VoiceConfirmationOption(redacted)"
}

sealed interface VoiceSessionState {
    data object Idle : VoiceSessionState

    data class Listening(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val attempt: Int,
    ) : VoiceSessionState

    data class Processing(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val attempt: Int,
    ) : VoiceSessionState

    /** Phase 10 corrected shape: options, not optionCount/secondary lookup. */
    data class Confirming(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val token: VoiceCandidateToken,
        val options: List<VoiceConfirmationOption>,
    ) : VoiceSessionState {
        init {
            require(options.size in 1..2) { "Voice confirmation requires 1 or 2 options" }
            require(options.map { it.optionIndex } == options.indices.toList()) {
                "Voice confirmation indexes must be contiguous, zero-based and ordered"
            }
        }
        override fun toString(): String = "VoiceSessionState.Confirming(redacted)"
    }

    data class Executed(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
    ) : VoiceSessionState

    data class Failed(
        val sessionId: VoiceSessionId,
        val origin: VoiceOrigin,
        val error: AppError.Voice,
        val canRetry: Boolean,
    ) : VoiceSessionState
}

/** Single app-scoped generation owner. No playback/queue authority in Voice. */
interface VoiceSessionCoordinator {
    val state: StateFlow<VoiceSessionState>
    fun start(origin: VoiceOrigin): AppResult<VoiceSessionId>
    fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit>
    /** P07 one-step negative response. Invalid/stale token MUST NOT change state. */
    fun neither(token: VoiceCandidateToken): AppResult<Unit> = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_STALE_SESSION,
            Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback)
    )
    /** P07 explicit token-scoped Cancel; distinct from global lifecycle cancel. */
    fun cancelConfirmation(token: VoiceCandidateToken): AppResult<Unit> = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_STALE_SESSION,
            Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback)
    )
    fun retry(): AppResult<Unit>
    fun cancel(reason: VoiceCancelReason)
}
