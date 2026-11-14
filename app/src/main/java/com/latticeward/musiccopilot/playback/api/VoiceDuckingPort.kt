package com.latticeward.musiccopilot.playback.api

/**
 * Voice requests a temporary attenuation lease from the SAME Media3 service.
 * Audio focus, queue, Play/Pause and effective volume remain service-owned.
 * IDs are random, short-lived process-local tokens; never transcript/media data.
 */
interface VoiceDuckingPort {
    /** Returns true only after the service acknowledges a lease. */
    suspend fun acquireVoiceDuck(token: String): Boolean

    /** Safe, idempotent, best-effort release; service also imposes a TTL. */
    fun releaseVoiceDuck(token: String)
}

/** Test-only/disabled execution seam; production always injects the controller. */
object NoOpVoiceDuckingPort : VoiceDuckingPort {
    override suspend fun acquireVoiceDuck(token: String) = true
    override fun releaseVoiceDuck(token: String) = Unit
}

/** Explicit manual-only fallback if the production service port is absent. */
object UnavailableVoiceDuckingPort : VoiceDuckingPort {
    override suspend fun acquireVoiceDuck(token: String) = false
    override fun releaseVoiceDuck(token: String) = Unit
}
