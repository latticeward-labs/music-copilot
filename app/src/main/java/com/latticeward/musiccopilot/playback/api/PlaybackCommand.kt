package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef

/** Intent only: service-owned player/queue remains the sole playback authority. */
sealed interface PlaybackCommand {
    val commandId: CommandId

    data class ReplaceQueue(
        override val commandId: CommandId,
        val items: List<MediaRef>,
        val startIndex: Int,
        val startPositionMs: Long = 0L,
        val playWhenReady: Boolean,
    ) : PlaybackCommand

    data class Play(override val commandId: CommandId) : PlaybackCommand
    data class Pause(override val commandId: CommandId) : PlaybackCommand
    data class Next(override val commandId: CommandId) : PlaybackCommand
    data class Previous(override val commandId: CommandId) : PlaybackCommand
    data class SeekBy(
        override val commandId: CommandId,
        val deltaMs: Long,
    ) : PlaybackCommand
    data class SeekTo(
        override val commandId: CommandId,
        val positionMs: Long,
    ) : PlaybackCommand
    data class Stop(override val commandId: CommandId) : PlaybackCommand
}

