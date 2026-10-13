package com.latticeward.musiccopilot.playback.controller

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * App-lifetime controller seam, deliberately disconnected until Phase 7 wires Media3.
 * Never constructs a Player/MediaSession or reports successful playback by itself.
 */
class PlaybackControllerConnection(ownerScope: CoroutineScope) : PlaybackGateway, AutoCloseable {
    init { requireNotNull(ownerScope.coroutineContext[Job]) { "Controller requires structured scope" } }

    private val connection = MutableStateFlow(PlaybackConnectionState.Disconnected)
    private val lastConfirmed = MutableStateFlow(
        PlaybackSnapshot(
            revision = 0L, queueRevision = 0L, playbackState = PlaybackState.Idle,
            queue = emptyList(), currentIndex = null, currentItem = null,
            positionMs = 0L, durationMs = null, canSeek = false,
            canSkipPrevious = false, canSkipNext = false,
        )
    )

    override val connectionState: StateFlow<PlaybackConnectionState> = connection
    override val snapshot: StateFlow<PlaybackSnapshot> = lastConfirmed

    override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> =
        AppResult.Failure(
            AppError.Controller(
                code = AppErrorCodes.CONTROLLER_DISCONNECTED,
                recoverability = Recoverability.RECOVERABLE,
                recoveryAction = RecoveryAction.Retry,
            )
        )

    override fun close() { connection.value = PlaybackConnectionState.Disconnected }
}

