package com.latticeward.musiccopilot.playback.command

/** Pure decisions only: all actual Player mutations remain service-serialized. */
object PlaybackNavigationPolicy {
    const val PREVIOUS_RESTART_THRESHOLD_MS = 3_000L
    const val VOICE_SEEK_BACK_MS = 10_000L

    enum class PreviousDecision { RestartCurrent, MoveToPrevious }

    /** At exactly 3 seconds restart current, per approved UF-05 wording. */
    fun previous(positionMs: Long, hasPreviousItem: Boolean): PreviousDecision =
        if (positionMs >= PREVIOUS_RESTART_THRESHOLD_MS || !hasPreviousItem) {
            PreviousDecision.RestartCurrent
        } else {
            PreviousDecision.MoveToPrevious
        }

    /** Unknown duration disallows absolute seeks; never guess an upper bound. */
    fun seekTo(positionMs: Long, durationMs: Long?, canSeek: Boolean): Long? =
        if (!canSeek || durationMs == null || durationMs < 0L) null
        else positionMs.coerceIn(0L, durationMs)

    /** Saturating relative seek avoids Long overflow before clamping. */
    fun seekBy(positionMs: Long, deltaMs: Long, durationMs: Long?, canSeek: Boolean): Long? {
        if (!canSeek || positionMs < 0L || (durationMs == null && deltaMs > 0L)) return null
        val sum = when {
            deltaMs > 0 && positionMs > Long.MAX_VALUE - deltaMs -> Long.MAX_VALUE
            deltaMs < 0 && positionMs < Long.MIN_VALUE - deltaMs -> Long.MIN_VALUE
            else -> positionMs + deltaMs
        }
        val nonnegative = sum.coerceAtLeast(0L)
        return if (durationMs != null && durationMs >= 0L) nonnegative.coerceAtMost(durationMs)
        else if (durationMs == null) nonnegative else null
    }

    /** Voice's 10-second backward action is permitted even before duration is known. */
    fun seekBack(positionMs: Long, durationMs: Long?, canSeek: Boolean): Long? =
        seekBy(positionMs, -VOICE_SEEK_BACK_MS, durationMs, canSeek)
}
