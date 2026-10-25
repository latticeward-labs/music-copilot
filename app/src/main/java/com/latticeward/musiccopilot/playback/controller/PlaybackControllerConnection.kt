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
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Application-owned Media3 controller. Never creates Player/MediaSession.
 * Ready is published only *after* one complete authoritative service snapshot.
 * A short snapshot refresh closes an onConnect race without overwriting later
 * monotonic service revisions. No second service/Player is created.
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    // Do NOT bind/start MediaSessionService from ApplicationBindings'
    // constructor. MusicCopilotApplication.bindings is not yet assigned,
    // which could race service restore's store/scope provider access.
    // Activity.onResume explicitly invokes ensureConnected(); Android
    // lock-screen/system clients can still bind the service themselves.


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
                    } else {
                        // The first accepted complete snapshot establishes
                        // Ready. Race-safe follow-up rereads actual service
                        // state in case playback changed during connecting.
                        ownerScope.launch {
                            val response = try {
                                withTimeoutOrNull(3_000L) {
                                    await(controller.sendCustomCommand(
                                        PlaybackWireProtocol.snapshotCommand(),
                                        Bundle.EMPTY,
                                    ))
                                }
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) { null }
                            if (!released && generation == token &&
                                active === controller &&
                                response?.resultCode == SessionResult.RESULT_SUCCESS
                            ) accept(response.extras, false)
                        }
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

    /** Explicit, bounded service read. UI never advances position locally. */
    override suspend fun refreshSnapshot(): AppResult<PlaybackSnapshot> {
        val controller = active
        val capturedGeneration = generation
        if (controller == null || connectionState.value != PlaybackConnectionState.Ready) {
            ensureConnected()
            return disconnected()
        }
        return try {
            val result = withTimeoutOrNull(2_500L) {
                await(controller.sendCustomCommand(
                    PlaybackWireProtocol.snapshotCommand(), Bundle.EMPTY
                ))
            } ?: return AppResult.Failure(AppError.Controller(
                AppErrorCodes.CONTROLLER_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.Retry
            ))
            if (released || generation != capturedGeneration ||
                active !== controller ||
                connectionState.value != PlaybackConnectionState.Ready
            ) return disconnected()
            if (result.resultCode != SessionResult.RESULT_SUCCESS) {
                PlaybackWireProtocol.decodeFailure(result.extras)
            } else {
                val current = PlaybackWireProtocol.decodeSnapshot(
                    result.extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
                ) ?: return disconnected()
                // A late refresh must not return a snapshot older than an
                // interruption event already published by onExtrasChanged.
                relay.publish(current, false)
                AppResult.Success(relay.snapshot.value)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            disconnected()
        }
    }

    override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
        val controller = active
        val capturedGeneration = generation
        if (controller == null || connectionState.value != PlaybackConnectionState.Ready) {
            ensureConnected()
            return disconnected()
        }
        return try {
            // An ordered MediaStore re-query and FD check for the currently
            // loaded queue (up to 5,000) may legitimately exceed 10 seconds
            // on slow SD media. Keep one bounded wait for a complete service
            // acknowledgement rather than returning a false "failed" result
            // while the already-accepted queue later starts playing.
            // Small transport controls retain their shorter 10s budget.
            val acknowledgementTimeoutMs = when (command) {
                is PlaybackCommand.ReplaceQueue -> 90_000L
                is PlaybackCommand.SelectQueueIndex -> 20_000L
                else -> 10_000L
            }
            val result = withTimeoutOrNull(acknowledgementTimeoutMs) {
                await(controller.sendCustomCommand(
                    PlaybackWireProtocol.executeCommand(),
                    PlaybackWireProtocol.encodeCommand(command)
                ))
            } ?: return AppResult.Failure(AppError.Controller(
                AppErrorCodes.CONTROLLER_TIMEOUT,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
            // A detached Activity's old suspended await must not publish
            // stale data or report success after a new UI generation exists.
            if (released || generation != capturedGeneration ||
                active !== controller ||
                connectionState.value != PlaybackConnectionState.Ready
            ) return disconnected()
            if (result.resultCode != SessionResult.RESULT_SUCCESS) {
                PlaybackWireProtocol.decodeFailure(result.extras)
            } else {
                // P06: Voice cannot report Executed from an uncorrelated
                // success/stale service response. Manual commands preserve
                // their original compatibility and service snapshot path.
                if (!PlaybackWireProtocol.acknowledges(command, result.extras)) {
                    return AppResult.Failure(AppError.Controller(
                        AppErrorCodes.CONTROLLER_CONNECT_FAILED,
                        Recoverability.RECOVERABLE,
                        RecoveryAction.ManualFallback,
                    ))
                }
                val confirmed = PlaybackWireProtocol.decodeSnapshot(
                    result.extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
                ) ?: return AppResult.Failure(AppError.Controller(
                    AppErrorCodes.CONTROLLER_CONNECT_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry))
                // Command acknowledgements can arrive after a newer
                // Media3 focus/route event. Do not return the obsolete
                // success payload to Full/Mini/Driving presentations.
                relay.publish(confirmed, false)
                AppResult.Success(relay.snapshot.value)
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

    /**
     * P08: called when the last Activity UI stops. Release only the bound
     * controller, NOT the Media3 service/player. This avoids pinning an
     * otherwise idle service alive after Recents closure.
     *
     * Normal foreground reentry calls ensureConnected(), which accepts a
     * fresh authoritative snapshot before exposing Ready to the UI.
     */
    fun detachUiController() {
        if (released) return
        ++generation // Ignore callbacks from old binding/reconnect.
        connecting?.let(MediaController::releaseFuture)
        active?.release()
        active = null
        connecting = null
        relay.disconnected()
    }

    override fun close() {
        if (released) return
        detachUiController()
        released = true
    }
}
