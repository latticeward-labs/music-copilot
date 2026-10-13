package com.latticeward.musiccopilot.playback.controller

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackWireProtocol
import com.latticeward.musiccopilot.playback.service.MusicPlaybackService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Application-owned Media3 controller. Never creates Player/MediaSession.
 * Ready is published only *after* one complete authoritative service snapshot.
 */
class PlaybackControllerConnection(
    private val ownerScope: CoroutineScope,
    context: Context? = null,
) : PlaybackGateway, AutoCloseable {
    init { requireNotNull(ownerScope.coroutineContext[Job]) { "Controller requires structured scope" } }

    private val appContext = context?.applicationContext
    private var generation = 0L
    private var released = false
    private var connecting: ListenableFuture<MediaController>? = null
    private var active: MediaController? = null

    private val relay = ControllerSnapshotRelay()
    override val connectionState: StateFlow<PlaybackConnectionState> = relay.connectionState
    override val snapshot: StateFlow<PlaybackSnapshot> = relay.snapshot

    init { if (appContext != null) ensureConnected() }

    /** Called from app foreground when a prior controller/service went away. */
    fun ensureConnected() {
        val context = appContext ?: return
        if (released || connecting != null || active != null) return
        val token = ++generation
        relay.beginConnect()
        try {
            val sessionToken = SessionToken(
                context, ComponentName(context, MusicPlaybackService::class.java)
            )
            val callback = object : MediaController.Listener {
                override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
                    if (generation == token && !released && active === controller) accept(extras, false)
                }

                override fun onDisconnected(controller: MediaController) {
                    if (generation != token || released) return
                    active = null
                    relay.failed()
                }
            }
            val future = MediaController.Builder(context, sessionToken)
                .setListener(callback)
                .buildAsync()
            connecting = future
            future.addListener({
                if (generation != token || released) {
                    MediaController.releaseFuture(future)
                    return@addListener
                }
                try {
                    val controller = future.get()
                    active = controller
                    connecting = null
                    if (!accept(controller.sessionExtras, true)) {
                        relay.failed()
                        active = null
                        MediaController.releaseFuture(future)
                    }
                } catch (_: Exception) {
                    connecting = null
                    active = null
                    relay.failed()
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (_: Exception) {
            connecting = null
            relay.failed()
        }
    }

    /** Full replacement before Ready. No field-by-field optimistic UI mutation. */
    private fun accept(extras: Bundle, allowRevisionReset: Boolean): Boolean {
        val received = PlaybackWireProtocol.decodeSnapshot(
            extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
        ) ?: return false
        return relay.publish(received, allowRevisionReset)
    }

    override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
        val controller = active
        if (controller == null || connectionState.value != PlaybackConnectionState.Ready) {
            ensureConnected()
            return disconnected()
        }
        return try {
            // Transport timeout is an adapter limit, not a product Voice timeout target.
            val result = withTimeoutOrNull(10_000L) {
                await(controller.sendCustomCommand(
                    PlaybackWireProtocol.executeCommand(),
                    PlaybackWireProtocol.encodeCommand(command)
                ))
            } ?: return AppResult.Failure(AppError.Controller(
                AppErrorCodes.CONTROLLER_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
            if (result.resultCode != SessionResult.RESULT_SUCCESS) {
                PlaybackWireProtocol.decodeFailure(result.extras)
            } else {
                val confirmed = PlaybackWireProtocol.decodeSnapshot(
                    result.extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
                ) ?: return AppResult.Failure(AppError.Controller(
                    AppErrorCodes.CONTROLLER_CONNECT_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry))
                relay.publish(confirmed, false)
                AppResult.Success(confirmed)
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { disconnected() }
    }

    private suspend fun await(future: ListenableFuture<SessionResult>): SessionResult =
        suspendCancellableCoroutine { continuation ->
            future.addListener({
                if (!continuation.isActive) return@addListener
                try {
                    continuation.resume(future.get())
                } catch (failure: Exception) {
                    continuation.resumeWith(Result.failure(failure))
                }
            }, com.google.common.util.concurrent.MoreExecutors.directExecutor())
            continuation.invokeOnCancellation { future.cancel(true) }
        }

    private fun disconnected(): AppResult.Failure =
        AppResult.Failure(AppError.Controller(
            AppErrorCodes.CONTROLLER_DISCONNECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))

    override fun close() {
        released = true
        ++generation
        connecting?.let(MediaController::releaseFuture)
        active?.release()
        active = null
        connecting = null
        relay.disconnected()
    }
}

