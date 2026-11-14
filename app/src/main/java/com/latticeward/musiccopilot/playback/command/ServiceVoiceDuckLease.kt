package com.latticeward.musiccopilot.playback.command

import kotlin.math.abs

/**
 * P07 pure, main-looper owned volume-lease reducer. Only the Media3 service
 * applies the proposed ExoPlayer.volume property changes.
 *
 * ExoPlayer's OS focus multiplier is independent: this policy NEVER requests
 * audio focus, plays, pauses, or changes Android stream/device volume.
 */
class ServiceVoiceDuckLease(
    private val multiplier: Float = 0.25f,
) {
    init { require(multiplier > 0f && multiplier < 1f) }

    data class Lease(val token: String, val baseline: Float, val ducked: Float)
    data class Update(val accepted: Boolean, val volumeToApply: Float? = null)

    private var lease: Lease? = null
    val currentToken: String? get() = lease?.token

    /**
     * A new generation supersedes the prior token without stacking 0.25x.
     * A token repeated by Media3 never applies a second attenuation.
     */
    fun acquire(token: String, currentVolume: Float): Update {
        if (!isValidToken(token) || !currentVolume.isFinite() ||
            currentVolume !in 0f..1f
        ) return Update(false)
        val prior = lease
        if (prior?.token == token) return Update(true)
        val baseline = if (prior != null && matches(currentVolume, prior.ducked))
            prior.baseline else currentVolume
        val ducked = (baseline * multiplier).coerceIn(0f, 1f)
        lease = Lease(token, baseline, ducked)
        return Update(true, volumeToApply = ducked.takeUnless { matches(currentVolume, it) })
    }

    /**
     * Never override a later user/external volume adjustment. A stale release
     * cannot release the next Voice generation's lease.
     */
    fun release(token: String, currentVolume: Float): Update {
        val active = lease ?: return Update(false)
        if (token != active.token) return Update(false)
        lease = null
        val restore = active.baseline.takeIf {
            currentVolume.isFinite() && matches(currentVolume, active.ducked)
        }
        return Update(true, restore)
    }

    /** Emergency teardown (focus/route, service destroy, TTL). */
    fun revoke(currentVolume: Float): Update {
        val token = lease?.token ?: return Update(false)
        return release(token, currentVolume)
    }

    companion object {
        private val tokenPattern = Regex("^voice-duck-[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        fun isValidToken(token: String): Boolean = tokenPattern.matches(token)
        private fun matches(a: Float, b: Float): Boolean = abs(a - b) <= 0.0001f
    }
}
