package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.flow.StateFlow

enum class PlaybackConnectionState { Disconnected, Connecting, Ready, RecoverableError }

interface PlaybackGateway {
    val connectionState: StateFlow<PlaybackConnectionState>
    val snapshot: StateFlow<PlaybackSnapshot>

    /** Success requires service acknowledgement; capacity 32 overflow yields PLAYBACK_BUSY. */
    suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot>

    /**
     * P07 optional current-position refresh for a visible Full Player.
     * Concrete Media3 controller overrides with a fresh service snapshot;
     * legacy test adapters remain source-compatible and make no audio claims.
     */
    suspend fun refreshSnapshot(): AppResult<PlaybackSnapshot> = AppResult.Success(snapshot.value)
}
