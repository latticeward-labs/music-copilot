package com.latticeward.musiccopilot.playback.service

import android.os.Bundle
import android.content.Intent
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
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.restore.SafePausedRestorePolicy
import com.latticeward.musiccopilot.core.model.CommandId
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.currentCoroutineContext

/**
 * Single-process, service-owned Media3 authority. A screen and the application
 * graph never own, recreate or release Player, MediaSession or command worker.
 *
 * Issue #7 P06: MediaSession uses a ForwardingPlayer façade to make system
 * transport controls share P02–P05 serialized queue/seek/previous rules;
 * no controller may inject arbitrary remote/local playlist URIs.
 * P08: standard Media3 foreground lifecycle, independent of Activity.
 * P09: Media3-owned focus/noisy behavior + conservative service-only
 * headset/Bluetooth route-loss latch. P10 owns decoder recovery QA.
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
    private lateinit var pausedCheckpoint: ServicePausedCheckpoint
    private var restoreGeneration = 0L
    // P10: consecutive decoder failures have one bounded recovery budget.
    // New explicit commands invalidate every outstanding asynchronous probe.
    private val recoveryPolicy = PlaybackFailureRecoveryPolicy()
    private var recoveryGeneration = 0L
    private var recoveryAttempts = 0
    // Both safety paths live in the ONE MediaSessionService. Route callbacks
    // and player callbacks run on Main; no second focus manager is created.
    private val interruptionGate = PlaybackInterruptionGate()
    private lateinit var focusObserver: Media3FocusObservationAdapter
    private var routeMonitor: ServiceAudioRouteMonitor? = null
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
                val candidate = ExoPlayer.Builder(this)
                    // Media3 pauses before the OS sends wired/Bluetooth
                    // playback to a newly selected built-in speaker.
                    .setHandleAudioBecomingNoisy(true)
                    // Media playback with screen off needs LOCAL wake mode;
                    // Media3 acquires/releases its lock only while playing.
                    .setWakeMode(C.WAKE_MODE_LOCAL)
                    .build()
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
                        // A newly accepted system/media-key command always
                        // beats an in-flight cold-restore IO coroutine.
                        ++restoreGeneration
                        ++recoveryGeneration
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
        // #8 P03: one service/Player-looper observer, NOT a second focus owner.
        // Media3 still owns Audio Focus and OS duck/suppression behavior.
        focusObserver = Media3FocusObservationAdapter(
            player = player,
            shouldBlockUnrequestedPlayback = {
                interruptionGate.shouldSuppressAutomaticPlay()
            },
            pauseUnsafePlayback = { player.pause() },
            onDeniedFocusAutoResume = {
                // Media3 must stay the sole focus owner. This latch blocks a
                // denied automatic unsuppression at the existing service gate
                // before a later callback can inadvertently restart playback.
                interruptionGate.onDisallowedFocusAutoResume()
            },
        )
        val appHost = application as PlaybackResolverHost
        pausedCheckpoint = ServicePausedCheckpoint(
            appHost.createPlaybackRestorableQueueStore(),
            appHost.playbackCheckpointWriteScope(),
        )
        routeMonitor = ServiceAudioRouteMonitor(this) {
            if (::player.isInitialized && player.mediaItemCount > 0 &&
                (player.playbackState != Player.STATE_IDLE || player.playWhenReady)
            ) {
                // High-priority route loss must bypass potentially long
                // MediaStore queue preparation; pausing via Player directly
                // is the sole service's emergency safe-pause, not a competing
                // controller, independent queue or ordinary command.
                interruptionGate.onHeadsetOrBluetoothLost()
                player.pause()
                publishSnapshot()
            }
        }.also { it.start() }

        player.addListener(object : Player.Listener {
            override fun onPlayWhenReadyChanged(
                playWhenReady: Boolean, reason: Int
            ) {
                focusObserver.onPlayWhenReadyChanged(playWhenReady, reason)
                if (!playWhenReady && PlaybackInterruptionGate.isNoisyChangeReason(reason)) {
                    // Media3 already paused the real player. Arm the same
                    // latch as the device callback even if OEM emits only
                    // AUDIO_BECOMING_NOISY and no AudioDeviceCallback removal.
                    interruptionGate.onHeadsetOrBluetoothLost()
                }
            }
            override fun onPlaybackSuppressionReasonChanged(
                playbackSuppressionReason: Int
            ) {
                // Public Media3 suppression reason signals a transient focus
                // interruption, not a proven incoming telephone call.
                focusObserver.onSuppressionChanged(playbackSuppressionReason)
            }
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                focusObserver.onIsPlayingChanged(isPlaying)
            }
            override fun onEvents(player: Player, events: Player.Events) {
                // A Bluetooth/wired route-loss latch beats any delayed focus
                // GAIN callback. Automatic resume is disallowed after loss;
                // only a NEW explicit Play may clear this latch.
                if (interruptionGate.shouldSuppressAutomaticPlay() &&
                    player.playWhenReady
                ) player.pause()
                // Projector compares real queue/index; no optimistic UI state.
                publishSnapshot()
            }
            override fun onPlayerError(error: PlaybackException) {
                // The Media3 listener runs on Main before the Player moves to
                // IDLE. Never log exception.message, URI, MediaRef or path.
                handlePlaybackFailure(error)
            }
        })
        // A cold service starts with an empty Player. READ before enabling
        // writes; never let that empty initial snapshot erase saved music.
        val startingGeneration = restoreGeneration
        val startingRouteEpoch = interruptionGate.routeGeneration
        owner.scope.launch {
            try {
                restoreColdPaused(startingGeneration, startingRouteEpoch)
            } finally {
                // A rejected/corrupt snapshot never blocks subsequent user
                // session writes, and never auto-starts playback.
                pausedCheckpoint.enable()
                // After a *validated* restored queue was published while
                // writes were disabled, commit the filtered Safe-Paused form
                // (invalid/missing entries dropped) as the next checkpoint.
                pausedCheckpoint.onSnapshot(confirmed)
            }
        }
        // Only the real service Player position is sampled, every 5 seconds
        // while actually Playing. No UI tick can persist invented progress.
        owner.scope.launch {
            while (currentCoroutineContext().isActive) {
                delay(5_000L)
                // A nonfatal skipped-MediaItem notice may remain visible
                // while the successor is genuinely Playing. Checkpoint real
                // progress instead of treating that notice as fatal.
                if (player.isPlaying &&
                    (runtimeError == null || runtimeError is AppError.MediaItem)
                ) {
                    pausedCheckpoint.onPlayingCheckpoint(
                        confirmed.copy(
                            positionMs = player.currentPosition.coerceAtLeast(0L),
                            playbackState = PlaybackState.Playing,
                        )
                    )
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession = session

    /**
     * P08: keep the SINGLE Media3 runtime across Home, Recents removal,
     * Activity recreation and screen-off only if playback is genuinely
     * foreground and user-requested. Do not keep a paused/failed service
     * alive or infer foreground status from cached UI state.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val retain = ::player.isInitialized &&
            PlaybackTaskLifetimePolicy.retainActivePlayback(
                foregroundOngoing = isPlaybackOngoing(),
                hasQueue = player.mediaItemCount > 0,
                playWhenReady = player.playWhenReady,
                isPlaying = player.isPlaying,
                isBuffering = player.playbackState == Player.STATE_BUFFERING,
                isEnded = player.playbackState == Player.STATE_ENDED,
                // A nonfatal MediaItem notice about a *previous* track
                // must not kill the actively playing recovered successor.
                hasPlayerError = player.playerError != null ||
                    (runtimeError != null &&
                        player.playbackState == Player.STATE_IDLE),
            )
        if (retain) return
        // Media3's default pauses/ends foreground work and asks Android to
        // stop when there is no legitimate active playback. This is NOT a
        // guarantee after process death, force-stop or OEM restrictions.
        super.onTaskRemoved(rootIntent)
    }

    /**
     * Keep Media3's notification provider and foreground-service promotion
     * intact. Never invent an Activity-owned notification, use a parallel
     * NotificationCompat manager or manually start a service on bootstrap.
     */
    override fun onUpdateNotification(
        session: MediaSession,
        startInForegroundRequired: Boolean,
    ) {
        super.onUpdateNotification(session, startInForegroundRequired)
    }

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
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
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
                // Supersede asynchronous cold restore at command-RECEIPT,
                // not after a slow resolver completes in the queued worker.
                ++restoreGeneration
                ++recoveryGeneration
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

    /**
     * Translate only stable Media3 numeric error categories into sanitized
     * AppError. A lost library grant may not be handled as a corrupt item:
     * permission recovery must be explicit, never auto-skip indefinitely.
     */
    private fun classifyPlaybackFailure(
        error: PlaybackException,
    ): PlaybackFailureRecoveryPolicy.Failure {
        // OEM ContentResolver adapters sometimes collapse security and
        // missing-volume faults into IO_UNSPECIFIED. Inspect ONLY exception
        // types, never propagate exception text or private file paths.
        var root: Throwable? = error.cause
        repeat(4) {
            when (root) {
                is SecurityException -> return PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked
                is java.io.FileNotFoundException ->
                    return PlaybackFailureRecoveryPolicy.Failure.Missing
            }
            root = root?.cause
        }
        return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND ->
            PlaybackFailureRecoveryPolicy.Failure.Missing
        PlaybackException.ERROR_CODE_IO_NO_PERMISSION ->
            PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
        PlaybackException.ERROR_CODE_DECODING_FAILED,
        PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE ->
            PlaybackFailureRecoveryPolicy.Failure.Corrupt
        PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DECODING_FORMAT_EXCEEDS_CAPABILITIES ->
            PlaybackFailureRecoveryPolicy.Failure.Unsupported
        else -> PlaybackFailureRecoveryPolicy.Failure.Unknown
        }
    }

    private fun safeMediaFailure(
        kind: PlaybackFailureRecoveryPolicy.Failure,
    ): AppError = when (kind) {
        PlaybackFailureRecoveryPolicy.Failure.Missing -> AppError.MediaItem(
            AppErrorCodes.MEDIA_ITEM_MISSING,
            Recoverability.RECOVERABLE, RecoveryAction.Skip,
        )
        PlaybackFailureRecoveryPolicy.Failure.Corrupt -> AppError.MediaItem(
            AppErrorCodes.MEDIA_ITEM_CORRUPT,
            Recoverability.RECOVERABLE, RecoveryAction.Skip,
        )
        PlaybackFailureRecoveryPolicy.Failure.Unsupported -> AppError.MediaItem(
            AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            Recoverability.RECOVERABLE, RecoveryAction.Skip,
        )
        PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked ->
            AppError.Playback(
                AppErrorCodes.PERMISSION_REVOKED,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings,
            )
        PlaybackFailureRecoveryPolicy.Failure.Unknown -> AppError.Playback(
            AppErrorCodes.PLAYBACK_FAILED,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
        )
    }

    private fun handlePlaybackFailure(error: PlaybackException) {
        val kind = classifyPlaybackFailure(error)
        val failure = safeMediaFailure(kind)
        val failingIndex = player.currentMediaItemIndex
        val requestedPlayback = player.playWhenReady &&
            !interruptionGate.shouldSuppressAutomaticPlay()
        val queueRevision = confirmed.queueRevision
        val routeEpoch = interruptionGate.routeGeneration
        val generation = ++recoveryGeneration

        // A failed decoder must no longer request audio focus or output.
        player.pause()
        runtimeError = failure
        publishSnapshot()
        if (recoveryPolicy.disposition(kind) ==
                PlaybackFailureRecoveryPolicy.Disposition.SkipForward &&
            requestedPlayback && recoveryPolicy.permitsAttempt(recoveryAttempts)
        ) {
            owner.scope.launch {
                // IO-based MediaStore revalidation MUST happen outside Main,
                // while actual Player mutation remains on service Main.
                recoverForward(
                    failedIndex = failingIndex,
                    expectedQueueRevision = queueRevision,
                    expectedRouteGeneration = routeEpoch,
                    generation = generation,
                    notice = failure,
                )
            }
        } else if (kind == PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked) {
            // A revoked storage permission invalidates cached resolver data.
            queueEngine.invalidate()
        }
    }

    /**
     * Forward-only bounded decoder recovery. Never retry the corrupt file.
     * Non-selected missing files are skipped, permission loss is terminal,
     * no valid successor produces a real Paused/Error service state.
     *
     * The active Media3 queue is NOT mutated during IO; only a currently
     * valid same-queue generation may jump to one revalidated occurrence.
     */
    private suspend fun recoverForward(
        failedIndex: Int,
        expectedQueueRevision: Long,
        expectedRouteGeneration: Long,
        generation: Long,
        notice: AppError,
    ): Boolean {
        fun stillCurrent(): Boolean =
            generation == recoveryGeneration &&
                expectedQueueRevision == confirmed.queueRevision &&
                expectedRouteGeneration == interruptionGate.routeGeneration &&
                !interruptionGate.shouldSuppressAutomaticPlay()

        if (!stillCurrent()) return false
        var index = failedIndex
        while (stillCurrent() && recoveryPolicy.permitsAttempt(recoveryAttempts)) {
            val candidate = recoveryPolicy.nextIndex(
                index, player.mediaItemCount, recoveryAttempts,
            ) ?: break
            val media = player.getMediaItemAt(candidate)
            recoveryAttempts++
            val outcome = try {
                queueEngine.revalidate(MediaRef(media.mediaId))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                AppResult.Failure(
                    AppError.Playback(
                        AppErrorCodes.PLAYBACK_FAILED,
                        Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
                    )
                )
            }
            if (!stillCurrent()) return false
            when (outcome) {
                is AppResult.Success -> {
                    // Selection must still identify exactly the URI which
                    // MediaStore JUST verified. Duplicate IDs are occurrences.
                    if (media != player.getMediaItemAt(candidate) ||
                        media.localConfiguration?.uri?.toString() !=
                            outcome.value.contentUri
                    ) return false
                    try {
                        // Error left Media3 IDLE. Restart only an explicitly
                        // requested session and only after revalidation.
                        runtimeError = notice
                        player.seekTo(candidate, 0L)
                        player.prepare()
                        player.play()
                        publishSnapshot()
                        return true
                    } catch (_: Exception) {
                        index = candidate
                    }
                }
                is AppResult.Failure -> when (outcome.error) {
                    is AppError.Permission -> {
                        queueEngine.invalidate()
                        runtimeError = safeMediaFailure(
                            PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked,
                        )
                        player.pause()
                        publishSnapshot()
                        return false
                    }
                    is AppError.MediaItem -> index = candidate
                    else -> {
                        runtimeError = safeMediaFailure(
                            PlaybackFailureRecoveryPolicy.Failure.Unknown,
                        )
                        player.pause()
                        publishSnapshot()
                        return false
                    }
                }
            }
        }
        if (stillCurrent()) {
            // End of queue or bounded attempt cap: remain service-owned,
            // paused and recoverable, with sanitized error for the UI.
            player.pause()
            runtimeError = notice
            publishSnapshot()
        }
        return false
    }

    private suspend fun applyCommand(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
        // A newer private/system transport command may be received while this
        // worker is suspended inside MediaStore I/O. An obsolete ReplaceQueue
        // must never start sound after a newer Pause/Stop/selection receipt.
        val commandEpoch = ++restoreGeneration
        pausedCheckpoint.onExplicitCommand()
        // Any new explicitly dispatched command wins over a pending decoder
        // recovery. In particular, user Pause/Stop cannot be undone by a late
        // MediaStore resolver returning from IO after the click.
        recoveryGeneration++
        recoveryAttempts = 0
        // Serial worker runs with Main.immediate. No optimistic acknowledgement.
        try {
            when (command) {
                is PlaybackCommand.ReplaceQueue -> {
                    val routeAtStart = interruptionGate.routeGeneration
                    // P04: all (up to 5,000) MediaStore FD probes run on IO.
                    // A failed candidate never modifies the existing Player.
                    val proposal = when (val outcome = queueEngine.prepare(command)) {
                        is AppResult.Success -> outcome.value
                        is AppResult.Failure -> return outcome
                    }
                    // A plugged-out Bluetooth/headset device while IO was
                    // verifying 5,000 items INVALIDATES the old play request.
                    // Never autostart after route loss using stale intention.
                    if (!interruptionGate.requestIsStillCurrent(routeAtStart) ||
                        commandEpoch != restoreGeneration
                    ) return rejected()
                    val mediaItems = LocalMedia3QueueItems.from(proposal)
                    // A new explicit user selection may intentionally play
                    // from the current output; a paused replacement does not
                    // clear the route-loss latch.
                    if (proposal.playWhenReady) {
                        interruptionGate.onExplicitUserPlaybackRequest()
                        focusObserver.onVerifiedUserPlayAccepted()
                    }
                    // Clear a previous error BEFORE preparing a new selection.
                    // An error delivered during prepare must not be erased.
                    runtimeError = null
                    // The service/Player remains the ONLY authoritative live queue.
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    player.shuffleModeEnabled = false
                    // A deliberately Paused queue must not inherit old playWhenReady.
                    if (!proposal.playWhenReady) {
                        interruptionGate.onManualPauseOrStop()
                        focusObserver.onManualPauseOrStop(stop = false)
                        player.pause()
                    }
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
                    val routeAtStart = interruptionGate.routeGeneration
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
                    if (!interruptionGate.requestIsStillCurrent(routeAtStart) ||
                        commandEpoch != restoreGeneration ||
                        confirmed.queueRevision != observedQueueRevision ||
                        selectedMedia != player.getMediaItemAt(command.index) ||
                        selectedMedia.localConfiguration?.uri?.toString() !=
                            validated.contentUri
                    ) return rejected()
                    if (!command.playWhenReady) {
                        interruptionGate.onManualPauseOrStop()
                        focusObserver.onManualPauseOrStop(stop = false)
                        player.pause()
                    } else {
                        interruptionGate.onExplicitUserPlaybackRequest()
                        focusObserver.onVerifiedUserPlayAccepted()
                    }
                    player.seekTo(command.index, 0L)
                    if (command.playWhenReady) {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.play()
                    }
                }
                is PlaybackCommand.Play -> {
                    if (player.mediaItemCount == 0) return rejected()
                    if (runtimeError?.code == AppErrorCodes.PERMISSION_REVOKED) {
                        // A revoked Android music grant must be verified
                        // AGAIN after a user leaves Settings. Never trust a
                        // cached valid URI merely because an old queue exists.
                        val index = player.currentMediaItemIndex
                            .takeIf { it in 0 until player.mediaItemCount }
                            ?: return rejected()
                        val selectedMedia = player.getMediaItemAt(index)
                        val queueAtStart = confirmed.queueRevision
                        val routeAtStart = interruptionGate.routeGeneration
                        val checked = queueEngine.revalidate(
                            MediaRef(selectedMedia.mediaId)
                        )
                        if (commandEpoch != restoreGeneration ||
                            queueAtStart != confirmed.queueRevision ||
                            routeAtStart != interruptionGate.routeGeneration ||
                            selectedMedia != player.getMediaItemAt(index)
                        ) return rejected()
                        when (checked) {
                            is AppResult.Failure -> {
                                runtimeError = when (checked.error) {
                                    is AppError.Permission -> {
                                        queueEngine.invalidate()
                                        safeMediaFailure(
                                            PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked
                                        )
                                    }
                                    is AppError.MediaItem -> safeMediaFailure(
                                        PlaybackFailureRecoveryPolicy.Failure.Missing,
                                    )
                                    else -> safeMediaFailure(
                                        PlaybackFailureRecoveryPolicy.Failure.Unknown,
                                    )
                                }
                                publishSnapshot()
                                return AppResult.Failure(runtimeError!!)
                            }
                            is AppResult.Success -> if (
                                selectedMedia.localConfiguration?.uri?.toString() !=
                                    checked.value.contentUri
                            ) return rejected()
                        }
                    }
                    interruptionGate.onExplicitUserPlaybackRequest()
                    focusObserver.onVerifiedUserPlayAccepted()
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
                is PlaybackCommand.Pause -> {
                    interruptionGate.onManualPauseOrStop()
                    focusObserver.onManualPauseOrStop(stop = false)
                    player.pause()
                }
                is PlaybackCommand.Next -> {
                    if (runtimeError?.code == AppErrorCodes.PERMISSION_REVOKED) {
                        // Access was revoked: no next-item read until Android
                        // grants permission again. UI offers Settings/Library.
                        return AppResult.Failure(runtimeError!!)
                    }
                    if (player.playerError != null ||
                        (runtimeError != null &&
                            player.playbackState == Player.STATE_IDLE)
                    ) {
                        // Approved WF-06 "Continue with next" is an explicit
                        // user command: move only to an IO-revalidated playable
                        // successor, with a freshly bounded recovery budget.
                        interruptionGate.onExplicitUserPlaybackRequest()
                        focusObserver.onVerifiedUserPlayAccepted()
                        val failureNotice = runtimeError ?: safeMediaFailure(
                            PlaybackFailureRecoveryPolicy.Failure.Corrupt
                        )
                        val recovered = recoverForward(
                            failedIndex = player.currentMediaItemIndex,
                            expectedQueueRevision = confirmed.queueRevision,
                            expectedRouteGeneration = interruptionGate.routeGeneration,
                            generation = recoveryGeneration,
                            notice = failureNotice,
                        )
                        return if (recovered) AppResult.Success(confirmed)
                            else AppResult.Failure(runtimeError ?: rejected().error)
                    }
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
                is PlaybackCommand.Stop -> {
                    interruptionGate.onManualPauseOrStop()
                    focusObserver.onManualPauseOrStop(stop = true)
                    // Explicit Stop is stronger than safe-paused restore.
                    // It clears the old checkpoint, even though ExoPlayer
                    // retains an in-memory stopped queue for later Play.
                    pausedCheckpoint.onStop()
                    player.stop()
                }
            }
            // Async ExoPlayer transitions are projected as Buffering/Paused,
            // never fabricated "Playing" from command acknowledgement.
            // Do not erase a synchronous onPlayerError callback.
            if (player.playerError != null) {
                // Retain the sanitized P10 typed MediaItem/permission error
                // set by onPlayerError; generic PlaybackFailed must not
                // silently replace it during synchronous Media3 callbacks.
                if (runtimeError == null) runtimeError = AppError.Playback(
                    AppErrorCodes.PLAYBACK_FAILED,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry,
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

    /**
     * P1 cold-only restore. The persisted Proto snapshot is never an
     * authoritative live queue. Every reference is freshly re-queried by
     * the SAME P03 resolver; invalid/missing items are dropped. A newly
     * connected live MediaSession or explicit command always wins.
     *
     * No Play(), autoplay flag, wakeup, notification foreground promotion,
     * or re-insertion of unchecked source URLs can occur in this path.
     */
    private suspend fun restoreColdPaused(
        expectedGeneration: Long,
        expectedRouteEpoch: Long,
    ) {
        fun stillCold(): Boolean =
            expectedGeneration == restoreGeneration &&
                expectedRouteEpoch == interruptionGate.routeGeneration &&
                ::player.isInitialized && player.mediaItemCount == 0 &&
                !player.playWhenReady

        if (!stillCold()) return
        val saved = when (val read = pausedCheckpoint.read()) {
            is AppResult.Success -> read.value
            is AppResult.Failure -> return // Invalid/future schema: fail closed.
        } ?: return
        if (!stillCold()) return
        // Preflight prevents any malformed/future schema from touching Media3
        // or triggering even a MediaStore query for an invalid snapshot.
        if (saved.schemaVersion != SafePausedRestorePolicy.SCHEMA_VERSION ||
            saved.items.isEmpty() || saved.items.size > 5_000 ||
            saved.currentIndex !in saved.items.indices ||
            saved.items.any { it.value.isBlank() || it.value.length > 128 } ||
            saved.positionMs < 0L || saved.queueRevision < 0L ||
            saved.capturedAtEpochMs <= 0L
        ) return

        val verified = LinkedHashSet<MediaRef>()
        val durations = mutableMapOf<MediaRef, Long?>()
        for (ref in saved.items) {
            if (!stillCold()) return
            val result = try {
                queueEngine.revalidate(ref)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { return }
            if (!stillCold()) return
            when (result) {
                is AppResult.Success -> {
                    verified += ref
                    durations[ref] = result.value.durationMs
                }
                is AppResult.Failure -> when (result.error) {
                    is AppError.MediaItem -> Unit // Missing: drop that occurrence.
                    else -> return // Revoked/unknown storage: never restore.
                }
            }
        }
        if (!stillCold() || verified.isEmpty()) return
        val selectedRef = saved.items[saved.currentIndex]
            .takeIf { it in verified } ?: saved.items.firstOrNull { it in verified }
            ?: return
        val plan = when (val outcome = SafePausedRestorePolicy.prepare(
            snapshot = saved,
            liveSessionPresent = false,
            verifiedMediaRefs = verified,
            verifiedDurationMs = durations[selectedRef],
        )) {
            is AppResult.Success -> outcome.value
            is AppResult.Failure -> return
        } ?: return
        if (!stillCold() || plan.items.isEmpty()) return
        // Existing P04 queue engine rechecks the selected item LAST and the
        // rest on IO, producing transient, verified content://media URIs.
        val queued = when (val proposal = queueEngine.prepare(
            PlaybackCommand.ReplaceQueue(
                CommandId("p11-paused-restore"), plan.items,
                plan.currentIndex, plan.positionMs, playWhenReady = false,
            )
        )) {
            is AppResult.Success -> proposal.value
            is AppResult.Failure -> return
        }
        if (!stillCold()) return
        val checkedPosition = queued.entries[queued.selectedIndex]
            .durationMs?.takeIf { it >= 0L }
            ?.let { plan.positionMs.coerceAtMost(it) } ?: 0L
        // Do not allow any interrupted route/focus latch or delayed UI
        // command to be bypassed by snapshot restoration.
        player.playWhenReady = false
        player.setMediaItems(
            LocalMedia3QueueItems.from(queued),
            queued.selectedIndex,
            checkedPosition,
        )
        player.prepare() // Stays PAUSED, no automatic audible output.
        player.pause()
        runtimeError = null
        publishSnapshot()
    }

    private fun publishSnapshot() {
        confirmed = projector.capture(player, runtimeError)
        session.setSessionExtras(wrap(confirmed))
        if (::pausedCheckpoint.isInitialized) pausedCheckpoint.onSnapshot(confirmed)
    }

    private fun wrap(snapshot: PlaybackSnapshot) = Bundle().apply {
        putBundle(PlaybackWireProtocol.KEY_SNAPSHOT, PlaybackWireProtocol.encodeSnapshot(snapshot))
    }

    private fun rejected() = AppResult.Failure(
        AppError.Playback(AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry)
    )

    override fun onDestroy() {
        // Unregister BEFORE disposing the sole service player, so late
        // Android route callbacks cannot touch an already released engine.
        routeMonitor?.close()
        routeMonitor = null
        if (::focusObserver.isInitialized) focusObserver.close()
        ++recoveryGeneration
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
