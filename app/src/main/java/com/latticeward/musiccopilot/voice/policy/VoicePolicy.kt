package com.latticeward.musiccopilot.voice.policy

/** Approved Issue #16 restricted deterministic policy. No cloud configuration authority. */
object VoicePolicy {
    const val LISTEN_TIMEOUT_MS = 6_000L
    const val MAX_ATTEMPTS = 2
    const val T_DIRECT = 0.90
    const val T_DIRECT_MIN_ALLOWED = 0.80
    const val T_DIRECT_MAX_ALLOWED = 0.98
    const val ARTIST_CLEAR_SIMILARITY = 0.88
    const val ARTIST_PLAUSIBLE_SIMILARITY = 0.72
    const val MIN_TOP_TWO_SEPARATION = 0.08
    const val MAX_SHORT_FEEDBACK_CHARS = 120
}
