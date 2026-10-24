package com.latticeward.musiccopilot.app.platform.bluetooth

import android.content.Context
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionState
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionStatus
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedBluetoothConnectionGate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Issue #9 P10. Application-owned but UI-foreground-only OS event adapter.
 * No Activity refs, ViewModel-generated monitor, second media player or
 * implicit Driving navigation. P11 will *consume* a verified one-shot edge.
 */
class SelectedBluetoothConnectionCoordinator(
    context: Context?,
    private val scope: CoroutineScope,
    private val preferences: PreferencesRepository,
) : AutoCloseable {
    private val gate = SelectedBluetoothConnectionGate()
    private val mutable = MutableStateFlow(gate.state)
    val state: StateFlow<SelectedAudioConnectionState> = mutable
    private val source = context?.let { appContext ->
        AndroidSelectedAudioProfileMonitor(
            context = appContext,
            scope = scope,
            paired = AndroidPairedAudioDeviceSource(appContext),
        ) { status, cause ->
            val token = selectedToken
            if (foreground && token != null) {
                mutable.value = gate.observe(token, status, cause)
            }
        }
    }
    private var foreground = false
    private var selectedToken: String? = null
    private var loadReady = false
    private var savedToken: String? = null

    init {
        scope.launch {
            combine(preferences.preferences, preferences.loadState) { p, load ->
                p.selectedBluetoothHint to (load == PreferencesLoadState.Ready)
            }.collect { (token, ready) ->
                savedToken = token
                loadReady = ready
                reconcile()
            }
        }
    }

    /** Lifecycle-bound: call on Activity START, not from a BT receiver. */
    fun onUiStarted() {
        if (foreground) return
        foreground = true
        reconcile()
    }

    /** No background receiver, profile proxy or stale eligibility. */
    fun onUiStopped() {
        foreground = false
        source?.stop()
        selectedToken = null
        gate.stop()
        mutable.value = gate.state
    }

    /** Foreground OS permission/radio recheck; NEVER navigates or autoplays. */
    fun onForegroundPermissionObserved() {
        if (foreground && selectedToken != null) source?.refresh()
    }

    private fun reconcile() {
        val next = savedToken.takeIf {
            foreground && loadReady && source != null &&
                BluetoothSelectionToken.isCurrentFormat(it)
        }
        if (next == selectedToken) return
        source?.stop()
        selectedToken = next
        if (!foreground) gate.stop() else gate.start(next)
        mutable.value = gate.state
        if (next != null) source?.start(next)
    }

    override fun close() {
        onUiStopped()
    }
}
