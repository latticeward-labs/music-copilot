package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError

enum class PlaybackState { Idle, Buffering, Playing, Paused, Ended, Error }

/**
 * Authoritative service snapshot. Phase 10 correction: lastError is AppError?,
 * limited to MediaItem/Playback/Interruption families, never a second error bus.
 * The service is responsible for monotonic revision/queueRevision and item validation.
 */
data class PlaybackSnapshot(
    val revision: Long,
    val queueRevision: Long,
    val playbackState: PlaybackState,
    val queue: List<MediaRef>,
    val currentIndex: Int?,
    val currentItem: MediaRef?,
    val positionMs: Long,
    val durationMs: Long?,
    val canSeek: Boolean,
    val canSkipPrevious: Boolean,
    val canSkipNext: Boolean,
    val lastError: AppError? = null,
) {
    init {
        require(lastError == null ||
            lastError is AppError.MediaItem ||
            lastError is AppError.Playback ||
            lastError is AppError.Interruption) {
            "PlaybackSnapshot only permits service-owned runtime error families"
        }
    }
}
