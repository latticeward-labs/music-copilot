package com.latticeward.musiccopilot.core.application.voice

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * Issue #12 P05: an admission-only, non-authoritative view of the ONE
 * application-scoped coordinator. No second microphone owner or session.
 *
 * A real explicit PTT start and a separately accepted explicit Retry are
 * counted once AFTER the coordinator has admitted them. Rejected taps,
 * rationale/OS permission callbacks, duplicate taps and one-step artist
 * confirmations cannot enter this callback. The one Voice coordinator
 * still owns its generation, state and all STT/playback dispatch.
 */
class ObservedVoicePttCoordinator(
    private val delegate: VoiceSessionCoordinator,
    private val onAcceptedAttempt: () -> Unit,
) : VoiceSessionCoordinator by delegate {
    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> {
        val result = delegate.start(origin)
        if (result is AppResult.Success) notifyBestEffort()
        return result
    }

    override fun retry(): AppResult<Unit> {
        // The same logical Voice session gains a distinct accepted PTT
        // attempt; the callback is NOT called for any failed/stale retry.
        val result = delegate.retry()
        if (result is AppResult.Success) notifyBestEffort()
        return result
    }

    private fun notifyBestEffort() {
        try {
            // The production callback only enqueues non-blocking quality
            // work and never changes an AppResult or Voice state.
            onAcceptedAttempt()
        } catch (_: Exception) {
            // No diagnostic failure may alter a successfully admitted PTT.
        }
    }
}
