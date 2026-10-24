package com.latticeward.musiccopilot.app.platform.bluetooth

import android.content.Context
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionState
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionStatus
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedBluetoothConnectionGate
import com.latticeward.musiccopilot.core.application.bluetooth.VerifiedAudioReconnectEvent
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
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

    // P11: replay=0, so Activity stop/relaunch never surfaces an old offer.
    // Values carry only a verified event counter + private selection epoch,
    // no Bluetooth MAC/name/hashed token. No UI/Activity is started here.
    private val reconnectMutable = MutableSharedFlow<VerifiedAudioReconnectEvent>(
        replay = 0, extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val verifiedReconnects: SharedFlow<VerifiedAudioReconnectEvent> = reconnectMutable
    private val uiForegroundMutable = MutableStateFlow(false)
    val uiForeground: StateFlow<Boolean> = uiForegroundMutable
    private var selectionEpoch = 0L
    private var claimedEventId = 0L

    /** Read immediately at the explicit Enter tap, not from a stale screen. */
    fun isUnclaimedOfferCurrent(event: VerifiedAudioReconnectEvent): Boolean {
        val current = mutable.value
        return foreground && uiForegroundMutable.value &&
            loadReady && preferences.loadState.value == PreferencesLoadState.Ready &&
            selectedToken != null &&
            selectedToken == preferences.preferences.value.selectedBluetoothHint &&
            event.selectionEpoch == selectionEpoch &&
            event.connectionEventId > claimedEventId &&
            current.selectedConnected &&
            current.connectionEventId == event.connectionEventId
    }

    /** Consumes exactly one invitation after the user explicitly chooses Enter. */
    fun claimOffer(event: VerifiedAudioReconnectEvent): Boolean {
        if (!isUnclaimedOfferCurrent(event)) return false
        claimedEventId = event.connectionEventId
        return true
    }

    val currentSelectionEpoch: Long get() = selectionEpoch
    private val source = context?.let { appContext ->
        AndroidSelectedAudioProfileMonitor(
            context = appContext,
            scope = scope,
            paired = AndroidPairedAudioDeviceSource(appContext),
        ) { status, cause ->
            val token = selectedToken
            if (foreground && token != null) {
                val verified = gate.observe(token, status, cause)
                mutable.value = verified
                if (verified.canOfferEntry) {
                    reconnectMutable.tryEmit(
                        VerifiedAudioReconnectEvent(
                            connectionEventId = verified.connectionEventId,
                            selectionEpoch = selectionEpoch,
                        )
                    )
                }
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
        uiForegroundMutable.value = true
        reconcile()
    }

    /** No background receiver, profile proxy or stale eligibility. */
    fun onUiStopped() {
        foreground = false
        uiForegroundMutable.value = false
        selectionEpoch++
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
        selectionEpoch++
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
