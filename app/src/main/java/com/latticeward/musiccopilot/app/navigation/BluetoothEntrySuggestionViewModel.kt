package com.latticeward.musiccopilot.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothEntrySuggestionPolicy
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothEntrySuggestionState
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionState
import com.latticeward.musiccopilot.core.application.bluetooth.VerifiedAudioReconnectEvent

/** P11 Activity-scoped UI-only gate. Nothing is SavedState/persisted/replayed. */
class BluetoothEntrySuggestionViewModel : ViewModel() {
    var state by mutableStateOf(BluetoothEntrySuggestionState())
        private set

    fun onReconnect(
        event: VerifiedAudioReconnectEvent,
        latest: SelectedAudioConnectionState,
        selectionEpoch: Long,
        isAllowedSurface: Boolean,
        nowElapsedMs: Long,
    ) {
        state = BluetoothEntrySuggestionPolicy.onVerifiedReconnect(
            state, event, latest, selectionEpoch, isAllowedSurface, nowElapsedMs
        )
    }

    fun reconcile(
        latest: SelectedAudioConnectionState,
        selectionEpoch: Long,
        isAllowedSurface: Boolean,
    ) {
        state = BluetoothEntrySuggestionPolicy.reconcile(
            state, latest, selectionEpoch, isAllowedSurface
        )
    }

    fun notNow(nowElapsedMs: Long) {
        state = BluetoothEntrySuggestionPolicy.notNow(state, nowElapsedMs)
    }

    fun accepted() {
        state = BluetoothEntrySuggestionPolicy.accepted(state)
    }

    fun invalidate() {
        state = BluetoothEntrySuggestionPolicy.invalidate(state)
    }
}
