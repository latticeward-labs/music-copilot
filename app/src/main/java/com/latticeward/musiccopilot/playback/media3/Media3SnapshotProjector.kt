package com.latticeward.musiccopilot.playback.media3

import androidx.media3.common.Player
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState

/**
 * Only the service calls this on the Player application looper. A full immutable
 * projection is published atomically; UI never owns a player or queue.
 */
class Media3SnapshotProjector {
    private var revision = 0L
    private var queueRevision = 0L

    fun capture(player: Player, error: AppError? = null): PlaybackSnapshot {
        val queue = (0 until player.mediaItemCount).map { pos ->
            MediaRef(player.getMediaItemAt(pos).mediaId)
        }
        val index = player.currentMediaItemIndex.takeIf { it in queue.indices }
        val state = when {
            player.playerError != null -> PlaybackState.Error
            player.playbackState == Player.STATE_ENDED -> PlaybackState.Ended
            player.playbackState == Player.STATE_BUFFERING -> PlaybackState.Buffering
            player.isPlaying -> PlaybackState.Playing
            player.playbackState == Player.STATE_IDLE && queue.isEmpty() -> PlaybackState.Idle
            else -> PlaybackState.Paused
        }
        return PlaybackSnapshot(
            revision = ++revision,
            queueRevision = queueRevision,
            playbackState = state,
            queue = queue,
            currentIndex = index,
            currentItem = index?.let(queue::get),
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it >= 0 },
            canSeek = index != null && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
            canSkipPrevious = player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM),
            canSkipNext = player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM),
            lastError = error,
        )
    }

    fun queueChanged() { ++queueRevision }
}

