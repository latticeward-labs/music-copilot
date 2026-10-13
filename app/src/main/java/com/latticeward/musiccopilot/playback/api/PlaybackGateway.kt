package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackConnectionState { Disconnected, Connecting, Ready, RecoverableError }

interface PlaybackGateway {
    val connectionState: StateFlow<PlaybackConnectionState>
    val snapshot: StateFlow<PlaybackSnapshot>

    /** Success requires service acknowledgement; capacity 32 overflow yields PLAYBACK_BUSY. */
    suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot>
}

