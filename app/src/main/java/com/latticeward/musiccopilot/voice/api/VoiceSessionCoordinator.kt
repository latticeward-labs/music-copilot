package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
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
)

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
            require(options.map { it.optionIndex }.distinct().size == options.size) {
                "Voice confirmation indexes must be distinct"
            }
        }
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
    fun retry(): AppResult<Unit>
    fun cancel(reason: VoiceCancelReason)
}

