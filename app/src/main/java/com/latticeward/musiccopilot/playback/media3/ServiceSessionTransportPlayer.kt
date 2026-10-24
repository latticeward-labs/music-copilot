package com.latticeward.musiccopilot.playback.media3

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackPlayOrigin
import com.latticeward.musiccopilot.playback.command.SystemTransportPolicy

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
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
class ServiceSessionTransportPlayer(
    delegate: Player,
    private val submit: (PlaybackCommand) -> Unit,
) : ForwardingPlayer(delegate) {
    private var nextId: Long = 0L

    // Exactly one Main-thread service owner installs these before the session
    // is created. The gate excludes disconnected/reconnected BT and unsafe
    // focus, but a user's previous Pause cannot permanently block new Play.
    private var canAcceptExternalPlay: (() -> Boolean)? = null
    private var currentRouteGeneration: (() -> Long)? = null

    fun configureSystemPlaySafety(
        mayPlay: () -> Boolean,
        routeGeneration: () -> Long,
    ) {
        canAcceptExternalPlay = mayPlay
        currentRouteGeneration = routeGeneration
    }

    private fun id(): CommandId = CommandId("system-transport-${++nextId}")

    private fun forward(action: SystemTransportPolicy.Action, positionMs: Long = 0L) {
        val translated = SystemTransportPolicy.translate(action, id(), positionMs) ?: return
        if (translated is PlaybackCommand.Play) {
            // Android/AVRCP supplies no trustworthy user-intent bit.
            // Keep unsafe route/reconnect quarantine, but allow a fresh Play
            // after an ordinary manual Pause while the route stayed valid.
            val epoch = currentRouteGeneration?.invoke() ?: return
            if (canAcceptExternalPlay?.invoke() != true) return
            submit(
                translated.copy(
                    origin = PlaybackPlayOrigin.AMBIGUOUS_SYSTEM_TRANSPORT,
                    routeGenerationAtReceipt = epoch,
                )
            )
        } else {
            // Pause/Stop/seek are never withheld by the auto-Play fence.
            submit(translated)
        }
    }

    override fun play() = forward(SystemTransportPolicy.Action.Play)

    override fun pause() = forward(SystemTransportPolicy.Action.Pause)

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) play() else pause()
    }

    override fun stop() = forward(SystemTransportPolicy.Action.Stop)

    override fun seekToNext() = forward(SystemTransportPolicy.Action.Next)

    override fun seekToNextMediaItem() = forward(SystemTransportPolicy.Action.Next)

    override fun seekToPrevious() = forward(SystemTransportPolicy.Action.Previous)

    override fun seekToPreviousMediaItem() = forward(SystemTransportPolicy.Action.Previous)

    override fun seekTo(positionMs: Long) =
        forward(SystemTransportPolicy.Action.SeekTo, positionMs)

    // Android controllers must not select arbitrary queue occurrences through
    // generic Player.seekTo(index,...). Only the private, revision-guarded
    // SelectQueueIndex command can change selection to an arbitrary index.
    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        if (mediaItemIndex == currentMediaItemIndex && positionMs >= 0L) {
            forward(SystemTransportPolicy.Action.SeekTo, positionMs)
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
