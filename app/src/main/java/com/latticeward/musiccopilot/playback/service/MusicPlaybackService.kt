package com.latticeward.musiccopilot.playback.service

import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionCommands
import androidx.media3.session.SessionError
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.playback.api.PlaybackResolverHost
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy
import com.latticeward.musiccopilot.playback.media3.LocalMedia3QueueItems
import com.latticeward.musiccopilot.playback.media3.ServiceSessionTransportPlayer
import kotlinx.coroutines.CancellationException
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.command.PlaybackWireProtocol
import com.latticeward.musiccopilot.playback.media3.ServicePlaybackRuntimeOwner
import com.latticeward.musiccopilot.playback.media3.Media3SnapshotProjector
import kotlinx.coroutines.launch

/**
 * Single-process, service-owned Media3 authority. A screen and the application
 * graph never own, recreate or release Player, MediaSession or command worker.
 *
 * Issue #7 P06: MediaSession uses a ForwardingPlayer façade to make system
 * transport controls share P02–P05 serialized queue/seek/previous rules;
 * no controller may inject arbitrary remote/local playlist URIs.
 * P07–P10 still owe full visual/background/route/decoder device QA.
 */
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {
    private lateinit var owner: PlaybackServiceScopeOwner
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaSession
    private lateinit var systemTransport: ServiceSessionTransportPlayer
    private lateinit var processor: PlaybackCommandProcessor
    private lateinit var queueEngine: ServiceQueueEngine
    private lateinit var runtimeOwner: ServicePlaybackRuntimeOwner<Pair<ExoPlayer, MediaSession>>
    private val pendingResults = linkedSetOf<SettableFuture<SessionResult>>()
    private val projector = Media3SnapshotProjector()
    private var runtimeError: AppError? = null
    private lateinit var confirmed: PlaybackSnapshot

    override fun onCreate() {
        super.onCreate()
        val dispatchers = object : DispatcherProvider {
            override val main = kotlinx.coroutines.Dispatchers.Main.immediate
            override val io = kotlinx.coroutines.Dispatchers.IO
            override val default = kotlinx.coroutines.Dispatchers.Default
        }
        owner = PlaybackServiceScopeOwner(dispatchers)
        // Process Application is created before a media service even when no
        // Activity exists. app.di owns concrete MediaStore adapter composition.
        queueEngine = ServiceQueueEngine(
            (application as PlaybackResolverHost).createPlaybackMediaResolver(dispatchers),
            dispatchers,
        )
        // Factory can start once per service instance and closes both resources once.
        runtimeOwner = ServicePlaybackRuntimeOwner(
            create = {
                val candidate = ExoPlayer.Builder(this).build()
                // P05: explicitly mark audio as music and delegate normal
                // focus policy to ExoPlayer. P09 owns interruption/route QA.
                candidate.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true,
                )
                confirmed = projector.capture(candidate)
                try {
                    // Only the MediaSession sees this facade; application
                    // service commands still mutate the real ExoPlayer through
                    // the one serialized PlaybackCommandProcessor.
                    systemTransport = ServiceSessionTransportPlayer(candidate) { intent ->
                        if (::processor.isInitialized) processor.submit(intent)
                    }
                    val builtSession = MediaSession.Builder(this, systemTransport)
                        .setCallback(PlaybackCallbacks())
                        .setSessionExtras(wrap(confirmed))
                        .build()
                    candidate to builtSession
                } catch (failure: Throwable) {
                    candidate.release()
                    throw failure
                }
            },
            dispose = { (p, s) ->
                s.release()
                p.release()
            }
        )
        val runtime = runtimeOwner.start()
        player = runtime.first
        session = runtime.second
        processor = PlaybackCommandProcessor(owner.scope, ::applyCommand)

        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                // Projector itself compares queue/index; metadata or duration
                // timeline updates never invalidate the queue token.
                publishSnapshot()
            }
            override fun onPlayerError(error: PlaybackException) {
                runtimeError = AppError.Playback(
                    AppErrorCodes.PLAYBACK_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry
                )
                publishSnapshot()
            }
        })
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    private inner class PlaybackCallbacks : MediaSession.Callback {
        private fun isPrivateCaller(controller: MediaSession.ControllerInfo): Boolean =
            controller.packageName == packageName &&
                controller.uid == android.os.Process.myUid()

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            // Typed internal commands are app-private, while approved system
            // transport operations are forwarded to the *same* command worker.
            // In particular, external URI/catalog/setMediaItems/queue mutation,
            // shuffle/repeat and arbitrary queue-index seeks are NOT exposed.
            val privateCaller = isPrivateCaller(controller)
            val commandBuilder = SessionCommands.Builder()
            if (privateCaller) {
                commandBuilder.add(PlaybackWireProtocol.executeCommand())
                commandBuilder.add(PlaybackWireProtocol.snapshotCommand())
            }
            val commands = commandBuilder.build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session, controller)
                .setAvailableSessionCommands(commands)
                .setAvailablePlayerCommands(ServiceSessionTransportPlayer.permittedCommands())
                .build()
        }

        // Explicit fail-closed defense in addition to the Player command
        // allowlist. Legacy MediaControllerCompat playFromUri/playFromSearch
        // must not substitute external, unverified or network media.
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
        ): ListenableFuture<List<MediaItem>> =
            Futures.immediateFailedFuture(UnsupportedOperationException("External queue disabled"))

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: List<MediaItem>,
            startIndex: Int,
            startPositionMs: Long,
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> =
            Futures.immediateFailedFuture(UnsupportedOperationException("External queue disabled"))

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> =
            if (!isPrivateCaller(controller)) {
                Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
            } else when (customCommand.customAction) {
            PlaybackWireProtocol.ACTION_SNAPSHOT -> {
                // Position advances between Media3 callbacks. Refresh only
                // when an authorized controller explicitly asks; the one
                // service-owned Player is the time/queue/state authority.
                publishSnapshot()
                Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS, wrap(confirmed))
                )
            }
            PlaybackWireProtocol.ACTION_EXECUTE -> {
                val command = PlaybackWireProtocol.decodeCommand(args)
                if (command == null) Futures.immediateFuture(
                    SessionResult(SessionError.ERROR_BAD_VALUE,
                        PlaybackWireProtocol.errorExtras(rejected().error))
                ) else {
                    val result = SettableFuture.create<SessionResult>()
                    pendingResults.add(result)
                    result.addListener(
                        { pendingResults.remove(result) },
                        com.google.common.util.concurrent.MoreExecutors.directExecutor()
                    )
                    val accepted = processor.submit(command)
                    owner.scope.launch {
                        val response = try { accepted.await() } catch (_: Exception) { rejected() }
                        if (!result.isDone) {
                            result.set(when (response) {
                                is AppResult.Success -> SessionResult(
                                    SessionResult.RESULT_SUCCESS, wrap(response.value)
                                )
                                is AppResult.Failure -> SessionResult(
                                    SessionError.ERROR_UNKNOWN,
                                    PlaybackWireProtocol.errorExtras(response.error)
                                )
                            })
                        }
                    }
                    result
                }
            }
            else -> Futures.immediateFuture(SessionResult(SessionError.ERROR_NOT_SUPPORTED))
        }
    }

    private suspend fun applyCommand(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
        // Serial worker runs with Main.immediate. No optimistic acknowledgement.
        try {
            when (command) {
                is PlaybackCommand.ReplaceQueue -> {
                    // P04: all (up to 5,000) MediaStore FD probes run on IO.
                    // A failed candidate never modifies the existing Player.
                    val proposal = when (val outcome = queueEngine.prepare(command)) {
                        is AppResult.Success -> outcome.value
                        is AppResult.Failure -> return outcome
                    }
                    val mediaItems = LocalMedia3QueueItems.from(proposal)
                    // Clear a previous error BEFORE preparing a new selection.
                    // An error delivered during prepare must not be erased.
                    runtimeError = null
                    // The service/Player remains the ONLY authoritative live queue.
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    player.shuffleModeEnabled = false
                    // A deliberately Paused queue must not inherit old playWhenReady.
                    if (!proposal.playWhenReady) player.pause()
                    player.setMediaItems(
                        mediaItems,
                        proposal.selectedIndex,
                        proposal.startPositionMs,
                    )
                    if (proposal.playWhenReady) {
                        player.prepare()
                        player.play()
                    }
                }
                is PlaybackCommand.SelectQueueIndex -> {
                    val observedQueueRevision = confirmed.queueRevision
                    if (command.expectedQueueRevision != observedQueueRevision ||
                        command.index !in 0 until player.mediaItemCount
                    ) return rejected()
                    val selectedMedia = player.getMediaItemAt(command.index)
                    val validated = when (val check = queueEngine.revalidate(
                        MediaRef(selectedMedia.mediaId)
                    )) {
                        is AppResult.Success -> check.value
                        is AppResult.Failure -> return check
                    }
                    // Revalidate the exact URI and authoritative timeline AFTER
                    // the IO suspension; never act on a stale UI queue index.
                    if (confirmed.queueRevision != observedQueueRevision ||
                        selectedMedia != player.getMediaItemAt(command.index) ||
                        selectedMedia.localConfiguration?.uri?.toString() !=
                            validated.contentUri
                    ) return rejected()
                    if (!command.playWhenReady) player.pause()
                    player.seekTo(command.index, 0L)
                    if (command.playWhenReady) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.play()
                    }
                }
                is PlaybackCommand.Play -> {
                    if (player.mediaItemCount == 0) return rejected()
                    // setMediaItems(... playWhenReady=false) and Stop leave
                    // STATE_IDLE; play() alone cannot start an unprepared item.
                    if (player.playbackState == Player.STATE_ENDED) {
                        val index = player.currentMediaItemIndex
                        if (index !in 0 until player.mediaItemCount) return rejected()
                        player.seekTo(index, 0L)
                    }
                    if (player.playbackState == Player.STATE_IDLE ||
                        player.playbackState == Player.STATE_ENDED ||
                        player.playerError != null
                    ) {
                        runtimeError = null
                        player.prepare()
                    }
                    player.play()
                }
                is PlaybackCommand.Pause -> player.pause()
                is PlaybackCommand.Next -> {
                    val index = player.currentMediaItemIndex
                        .takeIf { it in 0 until player.mediaItemCount }
                    when (val decision = PlaybackQueuePolicy.next(index, player.mediaItemCount)) {
                        is PlaybackQueuePolicy.NextDecision.Advance ->
                            player.seekTo(decision.index, 0L)
                        PlaybackQueuePolicy.NextDecision.EndOfQueue -> {
                            // Remain on the final occurrence without wrap/repeat.
                            player.pause()
                            val duration = player.duration
                            if (duration >= 0L) player.seekTo(duration)
                        }
                        PlaybackQueuePolicy.NextDecision.NoTrack -> return rejected()
                    }
                }
                is PlaybackCommand.Previous -> {
                    val index = player.currentMediaItemIndex
                        .takeIf { it in 0 until player.mediaItemCount } ?: return rejected()
                    when (PlaybackNavigationPolicy.previous(
                        player.currentPosition, hasPreviousItem = index > 0
                    )) {
                        PlaybackNavigationPolicy.PreviousDecision.RestartCurrent ->
                            player.seekTo(index, 0L)
                        PlaybackNavigationPolicy.PreviousDecision.MoveToPrevious ->
                            player.seekTo(index - 1, 0L)
                    }
                }
                is PlaybackCommand.SeekBy -> {
                    if (player.mediaItemCount == 0) return rejected()
                    val target = PlaybackNavigationPolicy.seekBy(
                        player.currentPosition,
                        command.deltaMs,
                        player.duration.takeIf { it >= 0L },
                        player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
                    ) ?: return rejected()
                    player.seekTo(target)
                }
                is PlaybackCommand.SeekTo -> {
                    if (player.mediaItemCount == 0) return rejected()
                    val target = PlaybackNavigationPolicy.seekTo(
                        command.positionMs,
                        player.duration.takeIf { it >= 0L },
                        player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
                    ) ?: return rejected()
                    player.seekTo(target)
                }
                is PlaybackCommand.Stop -> player.stop()
            }
            // Async ExoPlayer transitions are projected as Buffering/Paused,
            // never fabricated "Playing" from command acknowledgement.
            // Do not erase a synchronous onPlayerError callback.
            if (player.playerError != null) {
                runtimeError = AppError.Playback(
                    AppErrorCodes.PLAYBACK_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry
                )
                publishSnapshot()
                return AppResult.Failure(runtimeError!!)
            }
            if (command !is PlaybackCommand.ReplaceQueue) runtimeError = null
            publishSnapshot()
            return AppResult.Success(confirmed)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            runtimeError = AppError.Playback(
                AppErrorCodes.PLAYBACK_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Retry
            )
            publishSnapshot()
            return AppResult.Failure(runtimeError!!)
        }
    }

    private fun publishSnapshot() {
        confirmed = projector.capture(player, runtimeError)
        session.setSessionExtras(wrap(confirmed))
    }

    private fun wrap(snapshot: PlaybackSnapshot) = Bundle().apply {
        putBundle(PlaybackWireProtocol.KEY_SNAPSHOT, PlaybackWireProtocol.encodeSnapshot(snapshot))
    }

    private fun rejected() = AppResult.Failure(
        AppError.Playback(AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry)
    )

    override fun onDestroy() {
        if (::queueEngine.isInitialized) queueEngine.invalidate()
        if (::processor.isInitialized) processor.close()
        // Resolve in-flight session response futures even if coroutine owner is
        // about to be cancelled; never leave accepted calls silently suspended.
        val teardown = SessionResult(
            SessionError.ERROR_UNKNOWN,
            PlaybackWireProtocol.errorExtras(rejected().error)
        )
        pendingResults.toList().forEach { it.set(teardown) }
        pendingResults.clear()
        if (::runtimeOwner.isInitialized) runtimeOwner.close()
        if (::owner.isInitialized) owner.close()
        super.onDestroy()
    }
}
