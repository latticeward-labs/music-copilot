package com.latticeward.musiccopilot.playback.media3

import androidx.media3.common.Player
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState

/**
 * P05: one full service-owned projection. Revisions are generated from observed
 * Player truth; the UI never publishes or constructs playback state.
 */
class Media3SnapshotProjector {
    private var revision = 0L
    private var queueRevision = 0L
    private var previousQueue: List<MediaRef> = emptyList()
    private var previousIndex: Int? = null

    fun capture(player: Player, error: AppError? = null): PlaybackSnapshot {
        val queue = (0 until player.mediaItemCount).map { pos ->
            MediaRef(player.getMediaItemAt(pos).mediaId)
        }
        val index = player.currentMediaItemIndex.takeIf { it in queue.indices }
        // Timeline-changed callbacks also fire for timeline metadata/duration
        // adjustments. Only a logical queue or selected-occurrence change
        // should invalidate a Queue-screen selection token.
        if (queue != previousQueue || index != previousIndex) {
            queueRevision++
            previousQueue = queue
            previousIndex = index
        }
        val state = when {
            // A sanitized skipped-item notice must not misrepresent a
            // successfully recovering/playing successor as PlaybackState.Error.
            // Terminal errors remain Error when Media3 is actually IDLE.
            player.playerError != null ||
                (error != null && player.playbackState == Player.STATE_IDLE) ->
                PlaybackState.Error
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
            durationMs = player.duration.takeIf { it >= 0L },
            canSeek = index != null &&
                player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
            canSkipPrevious = index != null && (
                index > 0 || player.currentPosition > 0L
            ),
            canSkipNext = index != null && index < queue.lastIndex,
            lastError = error,
        )
    }

    /** Legacy explicit refresh hook. No queue mutation is inferred here. */
    fun queueChanged() = Unit
}
