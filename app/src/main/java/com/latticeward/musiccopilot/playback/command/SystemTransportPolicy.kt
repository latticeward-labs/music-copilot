package com.latticeward.musiccopilot.playback.command

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand

/**
 * System controllers may request *transport only*. Arbitrary media URIs,
 * queue replacement, item-index jumps, repeat/shuffle and remote catalog
 * requests have NO translation into a private playback command.
 *
 * Same typed commands as UI/Voice eventually enter the one service worker.
 */
object SystemTransportPolicy {
    enum class Action {
        Play, Pause, Next, Previous, Stop, SeekTo,
        SetQueue, AddMediaItem, SeekOtherQueueItem, Repeat, Shuffle,
    }

    fun translate(
        action: Action,
        commandId: CommandId,
        positionMs: Long = 0L,
    ): PlaybackCommand? = when (action) {
        Action.Play -> PlaybackCommand.Play(commandId)
        Action.Pause -> PlaybackCommand.Pause(commandId)
        Action.Next -> PlaybackCommand.Next(commandId)
        Action.Previous -> PlaybackCommand.Previous(commandId)
        Action.Stop -> PlaybackCommand.Stop(commandId)
        Action.SeekTo ->
            if (positionMs >= 0L) PlaybackCommand.SeekTo(commandId, positionMs) else null
        Action.SetQueue,
        Action.AddMediaItem,
        Action.SeekOtherQueueItem,
        Action.Repeat,
        Action.Shuffle -> null
    }
}
