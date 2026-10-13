package com.latticeward.musiccopilot.playback.service

import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
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
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.command.PlaybackWireProtocol
import com.latticeward.musiccopilot.playback.media3.ServicePlaybackRuntimeOwner
import com.latticeward.musiccopilot.playback.media3.Media3SnapshotProjector
import kotlinx.coroutines.launch

/**
 * Single-process, service-owned Media3 authority. A screen and the application
 * graph never own, recreate or release Player, MediaSession or command worker.
 *
 * Phase 7 foundation intentionally rejects ReplaceQueue: full indexed local
 * queue preparation/validation and system-control parity belong to Issue #7.
 */
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {
    private lateinit var owner: PlaybackServiceScopeOwner
    private lateinit var player: ExoPlayer
    private lateinit var session: MediaSession
    private lateinit var processor: PlaybackCommandProcessor
    private lateinit var runtimeOwner: ServicePlaybackRuntimeOwner<Pair<ExoPlayer, MediaSession>>
    private val pendingResults = linkedSetOf<SettableFuture<SessionResult>>()
    private val projector = Media3SnapshotProjector()
    private var runtimeError: AppError? = null
    private lateinit var confirmed: PlaybackSnapshot

    override fun onCreate() {
        super.onCreate()
        owner = PlaybackServiceScopeOwner(
            object : com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider {
                override val main = kotlinx.coroutines.Dispatchers.Main.immediate
                override val io = kotlinx.coroutines.Dispatchers.IO
                override val default = kotlinx.coroutines.Dispatchers.Default
            }
        )
        // Factory can start once per service instance and closes both resources once.
        runtimeOwner = ServicePlaybackRuntimeOwner(
            create = {
                val candidate = ExoPlayer.Builder(this).build()
                confirmed = projector.capture(candidate)
                try {
                    val builtSession = MediaSession.Builder(this, candidate)
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
                if (events.contains(Player.EVENT_TIMELINE_CHANGED)) projector.queueChanged()
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
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
        ): MediaSession.ConnectionResult {
            // Typed internal commands are app-private, not available to an
            // arbitrary external controller. Android media surface controls
            // still use the same service-owned Player through MediaSession.
            val privateCaller = controller.packageName == packageName &&
                controller.uid == android.os.Process.myUid()
            val commandBuilder = SessionCommands.Builder()
            if (privateCaller) {
                commandBuilder.add(PlaybackWireProtocol.executeCommand())
                commandBuilder.add(PlaybackWireProtocol.snapshotCommand())
            }
            val commands = commandBuilder.build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(commands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> = when (customCommand.customAction) {
            PlaybackWireProtocol.ACTION_SNAPSHOT ->
                Futures.immediateFuture(
                    SessionResult(SessionResult.RESULT_SUCCESS, wrap(confirmed))
                )
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
                is PlaybackCommand.ReplaceQueue ->
                    return rejected() // Local MediaStore queue validation: Issue #7.
                is PlaybackCommand.Play -> {
                    if (player.mediaItemCount == 0) return rejected()
                    player.play()
                }
                is PlaybackCommand.Pause -> player.pause()
                is PlaybackCommand.Next -> {
                    if (!player.hasNextMediaItem()) return rejected()
                    player.seekToNextMediaItem()
                }
                is PlaybackCommand.Previous -> {
                    if (!player.hasPreviousMediaItem()) return rejected()
                    player.seekToPreviousMediaItem()
                }
                is PlaybackCommand.SeekBy -> {
                    if (player.mediaItemCount == 0) return rejected()
                    player.seekTo((player.currentPosition + command.deltaMs).coerceAtLeast(0L))
                }
                is PlaybackCommand.SeekTo -> {
                    if (player.mediaItemCount == 0 || command.positionMs < 0L) return rejected()
                    player.seekTo(command.positionMs)
                }
                is PlaybackCommand.Stop -> player.stop()
            }
            runtimeError = null
            publishSnapshot()
            return AppResult.Success(confirmed)
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
