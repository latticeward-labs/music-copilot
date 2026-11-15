package com.latticeward.musiccopilot.playback.service

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.playback.api.PlayerRuntimeErrorCode

/**
 * Pure service-owned P07 episode deduplicator. The only input is the same
 * typed PlaybackSnapshot.lastError that the MediaSession publishes.
 * No per-frame/onEvents/notification/recomposition duplicate, clock, user ID,
 * media path, crash SDK or second player/error stream is introduced.
 *
 * One contiguous identical sanitized code is ONE observed episode. A
 * different code is a new episode; a null snapshot rearms the gate.
 * Repeated errors without an intervening clear can be undercounted;
 * P10 must mark this limitation, NOT invent an occurrence identifier.
 */
internal class ServicePlayerErrorEpisodeGate {
    private var previous: PlayerRuntimeErrorCode? = null
    private var nonAllowlistedOpen = false

    fun observe(error: AppError?): PlayerRuntimeErrorCode? {
        if (error == null) {
            previous = null
            nonAllowlistedOpen = false
            return null
        }
        // The source type can contain MediaRef: never forward the object
        // outside this service or format it in an error/log.
        if (error !is AppError.MediaItem &&
            error !is AppError.Playback &&
            error !is AppError.Interruption
        ) return null
        val allowed = PlayerRuntimeErrorCode.fromKnownServiceCode(error.code)
        if (allowed == null) {
            previous = null
            nonAllowlistedOpen = true
            return null
        }
        if (nonAllowlistedOpen) {
            // Unknown -> allowed is a new known source episode.
            nonAllowlistedOpen = false
            previous = allowed
            return allowed
        }
        if (previous == allowed) return null
        previous = allowed
        return allowed
    }
}
