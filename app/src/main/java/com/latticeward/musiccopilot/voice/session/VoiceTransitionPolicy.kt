package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.policy.VoicePolicy

/**
 * P02: pure, deterministic admission policy for one bounded PTT session.
 * No Android dependencies, transcript handling, playback or provider ownership.
 *
 * Failed and Executed are observable terminal presentations, not an automatic
 * permission to start another recording. UI dismissal/cancel returns to Idle.
 * Confirming is reserved for #11; five direct commands do not enter it.
 */
internal object VoiceTransitionPolicy {
    fun mayProcess(
        state: VoiceSessionState,
        id: VoiceSessionId,
        origin: VoiceOrigin,
        attempt: Int,
    ): Boolean = state is VoiceSessionState.Listening &&
        state.sessionId == id && state.origin == origin && state.attempt == attempt

    fun mayDispatch(
        state: VoiceSessionState,
        id: VoiceSessionId,
        origin: VoiceOrigin,
        attempt: Int,
        alreadyClaimed: Boolean,
    ): Boolean = !alreadyClaimed && state is VoiceSessionState.Processing &&
        state.sessionId == id && state.origin == origin && state.attempt == attempt

    fun mayComplete(
        state: VoiceSessionState,
        id: VoiceSessionId,
        origin: VoiceOrigin,
        attempt: Int,
    ): Boolean = state is VoiceSessionState.Processing &&
        state.sessionId == id && state.origin == origin && state.attempt == attempt

    /** Exactly one recognition retry; no retry after command delivery/error. */
    fun canOfferRetry(attempt: Int, errorCode: String): Boolean =
        attempt in 1 until VoicePolicy.MAX_ATTEMPTS &&
            (errorCode == AppErrorCodes.VOICE_TIMEOUT ||
                errorCode == AppErrorCodes.VOICE_NO_MATCH)

    fun mayRetry(
        state: VoiceSessionState,
        currentId: VoiceSessionId?,
        currentOrigin: VoiceOrigin?,
        attempt: Int,
    ): Boolean = state is VoiceSessionState.Failed &&
        state.sessionId == currentId && state.origin == currentOrigin &&
        state.canRetry && attempt in 1 until VoicePolicy.MAX_ATTEMPTS
}
