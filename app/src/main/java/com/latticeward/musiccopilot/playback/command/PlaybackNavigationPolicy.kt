package com.latticeward.musiccopilot.playback.command

/**
 * Pure playback navigation policy. The MediaSessionService retains execution
 * authority; none of these decisions can mutate Player or the live queue.
 */
object PlaybackNavigationPolicy {
    const val PREVIOUS_RESTART_THRESHOLD_MS = 3_000L
    const val VOICE_SEEK_BACK_MS = 10_000L

    enum class PreviousDecision { RestartCurrent, MoveToPrevious, StayCurrent }

    fun previous(positionMs: Long, hasPreviousItem: Boolean): PreviousDecision =
        when {
            positionMs > PREVIOUS_RESTART_THRESHOLD_MS -> PreviousDecision.RestartCurrent
            hasPreviousItem -> PreviousDecision.MoveToPrevious
            else -> PreviousDecision.StayCurrent
        }

    /** Returns null when seeking is unsupported; unknown duration has no upper clamp. */
    fun seekBack(positionMs: Long, durationMs: Long?, canSeek: Boolean): Long? {
        if (!canSeek) return null
        val target = (positionMs - VOICE_SEEK_BACK_MS).coerceAtLeast(0L)
        return if (durationMs != null && durationMs >= 0L) target.coerceAtMost(durationMs)
            else target
    }
}
