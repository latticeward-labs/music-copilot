package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef

/**
 * Transport provenance is set *inside the service* and is NOT encoded into
 * PlaybackWireProtocol. The public media button/AVRCP path cannot distinguish
 * a physical user press from an OEM auto-Play-on-connect event.
 */
enum class PlaybackPlayOrigin { TRUSTED_PRIVATE_COMMAND, AMBIGUOUS_SYSTEM_TRANSPORT }

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

    /** Tap a live Queue occurrence only if the observed service revision still matches. */
    data class SelectQueueIndex(
        override val commandId: CommandId,
        val index: Int,
        val expectedQueueRevision: Long,
        val playWhenReady: Boolean = true,
    ) : PlaybackCommand

    data class Play(
        override val commandId: CommandId,
        val origin: PlaybackPlayOrigin = PlaybackPlayOrigin.TRUSTED_PRIVATE_COMMAND,
        // Only SYSTEM_TRANSPORT commands get a route epoch at their receipt.
        // Null cannot authorize a system Play at the service's worker.
        val routeGenerationAtReceipt: Long? = null,
    ) : PlaybackCommand
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
