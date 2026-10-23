package com.latticeward.musiccopilot.playback.controller

import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Pure, testable publication gate. A new controller must atomically replace
 * the COMPLETE session snapshot before declaring Ready. UI recreation does not
 * create or mutate service authority. An older **or duplicate** service revision cannot overwrite the latest
 * accepted publication. A confirmed fresh connection may reset the epoch.
 */
class ControllerSnapshotRelay {
    private val mutableConnection = MutableStateFlow(PlaybackConnectionState.Disconnected)
    private val mutableSnapshot = MutableStateFlow(
        PlaybackSnapshot(
            revision = 0L, queueRevision = 0L, playbackState = PlaybackState.Idle,
            queue = emptyList(), currentIndex = null, currentItem = null,
            positionMs = 0L, durationMs = null, canSeek = false,
            canSkipPrevious = false, canSkipNext = false
        )
    )
    val connectionState: StateFlow<PlaybackConnectionState> = mutableConnection
    val snapshot: StateFlow<PlaybackSnapshot> = mutableSnapshot

    fun beginConnect() {
        mutableConnection.value = PlaybackConnectionState.Connecting
    }

    fun publish(wholeSnapshot: PlaybackSnapshot, isInitialAfterConnect: Boolean): Boolean {
        if (!isInitialAfterConnect &&
            wholeSnapshot.revision <= mutableSnapshot.value.revision) return false
        mutableSnapshot.value = wholeSnapshot
        mutableConnection.value = PlaybackConnectionState.Ready
        return true
    }

    fun failed() { mutableConnection.value = PlaybackConnectionState.RecoverableError }

    fun disconnected() { mutableConnection.value = PlaybackConnectionState.Disconnected }
}
