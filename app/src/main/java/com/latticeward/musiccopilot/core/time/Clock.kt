package com.latticeward.musiccopilot.core.time

/** Shared testable clock. Time is not media/voice/telemetry identity. */
fun interface Clock {
    fun nowEpochMillis(): Long
}
