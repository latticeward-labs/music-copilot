package com.latticeward.musiccopilot.playback.api

/**
 * Playback-to-application composition boundary for urgent Voice invalidation.
 *
 * The service must not import or instantiate an app-scoped Voice provider.
 * Only the application composition root maps these typed interruption signals
 * to the ONE existing VoiceSessionCoordinator and its provider generation.
 * Called synchronously on the sole playback service Main looper.
 */
enum class PlaybackVoiceInterruptionReason {
    FOCUS_INVALIDATED,
    ROUTE_INVALIDATED,
}

interface PlaybackVoiceInterruptionHost {
    /**
     * Idempotent when there is no active Voice session. Never starts Voice,
     * requests microphone permission, or submits a playback command.
     */
    fun cancelActiveVoiceForPlaybackInterruption(
        reason: PlaybackVoiceInterruptionReason,
    )
}
