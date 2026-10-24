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
    private var lastPublished: PlaybackSnapshot? = null

    /**
     * Capture one immutable observation of the ONE Media3 Player. An OS focus
     * suppression or urgent service route/manual latch is Safe Paused, NOT
     * Buffering/Playing, even when Media3 has a pending playWhenReady flag.
     * MediaSession still owns the actual playback/notification transport.
     *
     * Repeated identical observations keep their revision: controller/UI
     * receive a new revision only for a real state/queue/progress/error change.
     */
    fun capture(
        player: Player,
        error: AppError? = null,
        safePauseRequired: Boolean = false,
    ): PlaybackSnapshot {
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
            // Do not overwrite a terminal error with a plausible Playing UI.
            player.playerError != null ||
                (error != null && error !is AppError.Interruption &&
                    player.playbackState == Player.STATE_IDLE) ->
                PlaybackState.Error
            player.playbackState == Player.STATE_ENDED -> PlaybackState.Ended
            player.playbackState == Player.STATE_IDLE && queue.isEmpty() ->
                PlaybackState.Idle
            // Even an unfinished decoder buffer is *not* an active audible
            // session if the user paused, OS suppressed focus or route failed.
            safePauseRequired ||
                !player.playWhenReady ||
                player.playbackSuppressionReason !=
                    Player.PLAYBACK_SUPPRESSION_REASON_NONE ->
                PlaybackState.Paused
            player.isPlaying -> PlaybackState.Playing
            player.playbackState == Player.STATE_BUFFERING -> PlaybackState.Buffering
            else -> PlaybackState.Paused
        }
        val sample = PlaybackSnapshot(
            revision = revision,
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
        val prior = lastPublished
        if (prior != null && prior == sample) return prior
        if (revision != Long.MAX_VALUE) revision++
        val published = sample.copy(revision = revision)
        lastPublished = published
        return published
    }

    /** Legacy explicit refresh hook. No queue mutation is inferred here. */
    fun queueChanged() = Unit
}
