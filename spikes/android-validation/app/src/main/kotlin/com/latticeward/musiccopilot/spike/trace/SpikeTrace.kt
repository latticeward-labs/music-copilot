package com.latticeward.musiccopilot.spike.trace

import android.util.Log

/**
 * Evidence-safe trace: callers may pass only codes, counts, booleans, revisions,
 * timing, and synthetic/session identifiers. Never pass titles, paths, URIs, or IDs.
 */
object SpikeTrace {
    const val TAG = "MusicCopilotSpike"

    fun event(code: String, vararg fields: Pair<String, Any>) {
        val payload = fields.joinToString(separator = " ") { (key, value) -> "$key=$value" }
        Log.i(TAG, "code=$code${if (payload.isEmpty()) "" else " $payload"}")
    }
}
