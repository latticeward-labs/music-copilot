package com.latticeward.musiccopilot.playback.service

/**
 * Pure P08 service-lifetime decision, not a second playback state machine.
 *
 * Removing the Activity task must NOT tear down a legitimately foreground,
 * user-requested stream. If playback is paused, ended, errored or not actually
 * in a Media3 foreground service, let MediaSessionService perform its default
 * safe cleanup (never try to resurrect a background service or autoplay).
 *
 * Background / screen-off do not change playWhenReady.
 */
internal object PlaybackTaskLifetimePolicy {
    fun retainActivePlayback(
        foregroundOngoing: Boolean,
        hasQueue: Boolean,
        playWhenReady: Boolean,
        isPlaying: Boolean,
        isBuffering: Boolean,
        isEnded: Boolean,
        hasPlayerError: Boolean,
    ): Boolean =
        foregroundOngoing && hasQueue && playWhenReady &&
            !isEnded && !hasPlayerError && (isPlaying || isBuffering)
}
