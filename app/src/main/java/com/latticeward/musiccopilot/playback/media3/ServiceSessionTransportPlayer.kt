package com.latticeward.musiccopilot.playback.media3

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand

/**
 * MediaSession-only facade over the ONE service-owned ExoPlayer.
 *
 * Approved transport methods enqueue exactly the same typed command worker
 * as the private application controller, instead of mutating the Player
 * synchronously and bypassing Previous/Next/Seek policy.
 *
 * The service retains the underlying Player for internal queue commits and
 * Media3 events. No second Player or separate audio engine is created.
 */
class ServiceSessionTransportPlayer(
    delegate: Player,
    private val submit: (PlaybackCommand) -> Unit,
) : ForwardingPlayer(delegate) {
    private var nextId: Long = 0L

    private fun id(): CommandId = CommandId("system-transport-${++nextId}")

    override fun play() = submit(PlaybackCommand.Play(id()))

    override fun pause() = submit(PlaybackCommand.Pause(id()))

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) play() else pause()
    }

    override fun stop() = submit(PlaybackCommand.Stop(id()))

    override fun seekToNext() = submit(PlaybackCommand.Next(id()))

    override fun seekToNextMediaItem() = submit(PlaybackCommand.Next(id()))

    override fun seekToPrevious() = submit(PlaybackCommand.Previous(id()))

    override fun seekToPreviousMediaItem() = submit(PlaybackCommand.Previous(id()))

    override fun seekTo(positionMs: Long) {
        if (positionMs >= 0L) submit(PlaybackCommand.SeekTo(id(), positionMs))
    }

    // Android controllers must not select arbitrary queue occurrences through
    // generic Player.seekTo(index,...). Only the private, revision-guarded
    // SelectQueueIndex command can change selection to an arbitrary index.
    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        if (mediaItemIndex == currentMediaItemIndex && positionMs >= 0L) {
            submit(PlaybackCommand.SeekTo(id(), positionMs))
        }
    }

    /** Command allowlist exposed to both system and in-app controllers. */
    companion object {
        fun permittedCommands(): Player.Commands = Player.Commands.Builder()
            .add(Player.COMMAND_PLAY_PAUSE)
            .add(Player.COMMAND_STOP)
            .add(Player.COMMAND_SEEK_TO_NEXT)
            .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
            .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            .add(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
            .add(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)
            .add(Player.COMMAND_GET_TIMELINE)
            .add(Player.COMMAND_GET_METADATA)
            .add(Player.COMMAND_GET_AUDIO_ATTRIBUTES)
            .build()
    }
}
