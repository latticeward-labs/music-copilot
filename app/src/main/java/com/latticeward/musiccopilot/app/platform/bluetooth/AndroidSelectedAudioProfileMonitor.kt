package com.latticeward.musiccopilot.app.platform.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothObservationCause
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDeviceSource
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioRead
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground + selected preference only. Reads BONDED + actual A2DP/HFP
 * getConnectedDevices; an ACL broadcast, a remembered MAC or HEADSET name is
 * never allowed to count as a usable audio-profile connection.
 *
 * No receiver lives beyond stop(); no scan/location, pairing, sockets,
 * Media3 route changes, startActivity, notifications or microphone actions.
 */
class AndroidSelectedAudioProfileMonitor(
    context: Context,
    private val scope: CoroutineScope,
    private val paired: PairedAudioDeviceSource,
    private val onResult: (SelectedAudioConnectionStatus, BluetoothObservationCause) -> Unit,
) {
    private val app = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var active = false
    private var epoch = 0L
    private var sequence = 0L
    private var selectedToken: String? = null
    private var radio: BluetoothAdapter? = null
    private var receiverRegistered = false
    private var a2dp: BluetoothProfile? = null
    private var headset: BluetoothProfile? = null
    private var query: Job? = null
    private var profileConnectPulse = false
    private var waitingForA2dp = false
    private var waitingForHeadset = false
    private var listener: BluetoothProfile.ServiceListener? = null

    private fun granted(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            ContextCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!active) return
            when (intent?.action) {
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED -> {
                    // Event extras are untrusted and never grant eligibility.
                    // A connected profile event ONLY schedules a fresh profile
                    // + paired-device check from our owned proxies.
                    val connectionState = intent.getIntExtra(
                        BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED
                    )
                    if (connectionState == BluetoothProfile.STATE_CONNECTED)
                        profileConnectPulse = true
                    if (connectionState == BluetoothProfile.STATE_DISCONNECTED)
                        profileConnectPulse = false
                    schedule(BluetoothObservationCause.ProfileConnectionEvent)
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    // Radio may have been off when getProfileProxy was first
                    // attempted; bind proxies on a later actual ON event.
                    refresh()
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED,
                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                    schedule(BluetoothObservationCause.ExternalRecheck)
            }
        }
    }

    // Per-session listener captures an epoch. Always process it on the
    // Activity/main dispatcher; profile callbacks may arrive on binder threads.
    // Old session callbacks after a selection/Activity change are discarded.
    private fun listenerFor(generation: Long): BluetoothProfile.ServiceListener =
        object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
                mainHandler.post {
                    if (!active || epoch != generation) return@post
                    when (profile) {
                        BluetoothProfile.A2DP -> {
                            a2dp = proxy
                            waitingForA2dp = false
                        }
                        BluetoothProfile.HEADSET -> {
                            headset = proxy
                            waitingForHeadset = false
                        }
                    }
                    schedule(BluetoothObservationCause.ProfileService)
                }
            }

            override fun onServiceDisconnected(profile: Int) {
                mainHandler.post {
                    if (!active || epoch != generation) return@post
                    when (profile) {
                        BluetoothProfile.A2DP -> {
                            a2dp = null
                            waitingForA2dp = true
                        }
                        BluetoothProfile.HEADSET -> {
                            headset = null
                            waitingForHeadset = true
                        }
                    }
                    profileConnectPulse = false
                    schedule(BluetoothObservationCause.ProfileService)
                }
            }
        }

    @SuppressLint("MissingPermission")
    fun start(token: String) {
        stop()
        if (!BluetoothSelectionToken.isCurrentFormat(token)) return
        selectedToken = token
        active = true
        epoch++
        if (!granted()) {
            onResult(SelectedAudioConnectionStatus.PermissionRequired,
                BluetoothObservationCause.Initial)
            return
        }
        try {
            if (!app.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                onResult(SelectedAudioConnectionStatus.Unsupported,
                    BluetoothObservationCause.Initial)
                return
            }
            radio = (app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
            val adapter = radio
            if (adapter == null) {
                onResult(SelectedAudioConnectionStatus.Unsupported,
                    BluetoothObservationCause.Initial)
                return
            }
            val filter = IntentFilter().apply {
                addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
            if (Build.VERSION.SDK_INT >= 33)
                app.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            else app.registerReceiver(receiver, filter)
            receiverRegistered = true
            if (!adapter.isEnabled) {
                onResult(SelectedAudioConnectionStatus.BluetoothOff,
                    BluetoothObservationCause.Initial)
                return
            }
            val currentListener = listenerFor(epoch)
            listener = currentListener
            waitingForA2dp = adapter.getProfileProxy(
                app, currentListener, BluetoothProfile.A2DP)
            waitingForHeadset = adapter.getProfileProxy(
                app, currentListener, BluetoothProfile.HEADSET)
            schedule(BluetoothObservationCause.Initial)
        } catch (_: SecurityException) {
            onResult(SelectedAudioConnectionStatus.PermissionRequired,
                BluetoothObservationCause.Initial)
        } catch (_: RuntimeException) {
            onResult(SelectedAudioConnectionStatus.Unavailable,
                BluetoothObservationCause.Initial)
        }
    }

    /** Fresh foreground / permission / Settings return observation. */
    fun refresh() {
        if (!active) return
        // A permission grant/radio-on transition must bind real OS profile
        // proxies, not merely re-query a previously unauthorized null proxy.
        if (granted() && (radio == null ||
                (a2dp == null && headset == null &&
                    !waitingForA2dp && !waitingForHeadset)
            )
        ) {
            val stored = selectedToken
            if (stored != null) start(stored)
        } else schedule(BluetoothObservationCause.ExternalRecheck)
    }

    /** Called on selection removal/change, lifecycle stop or app shutdown. */
    fun stop() {
        epoch++
        sequence++
        query?.cancel()
        query = null
        active = false
        selectedToken = null
        profileConnectPulse = false
        if (receiverRegistered) {
            try { app.unregisterReceiver(receiver) } catch (_: RuntimeException) { }
            receiverRegistered = false
        }
        radio?.let { adapter ->
            for (proxy in listOfNotNull(a2dp, headset)) {
                try {
                    adapter.closeProfileProxy(
                        if (proxy === a2dp) BluetoothProfile.A2DP else BluetoothProfile.HEADSET,
                        proxy,
                    )
                } catch (_: RuntimeException) { }
            }
        }
        a2dp = null
        headset = null
        waitingForA2dp = false
        waitingForHeadset = false
        radio = null
        listener = null
    }

    @SuppressLint("MissingPermission")
    private fun schedule(cause: BluetoothObservationCause) {
        if (!active) return
        val generation = epoch
        val currentSequence = ++sequence
        query?.cancel()
        query = scope.launch {
            try {
                // Small coalescing window for profile-state broadcasts; no
                // fixed repeating background polling or inferred connection.
                if (profileConnectPulse) delay(180L)
                if (!active || epoch != generation || sequence != currentSequence)
                    return@launch
                if (!granted()) {
                    emit(generation, currentSequence,
                        SelectedAudioConnectionStatus.PermissionRequired, cause)
                    return@launch
                }
                val read = paired.readPairedAudio()
                if (!active || epoch != generation || sequence != currentSequence)
                    return@launch
                when (read) {
                    PairedAudioRead.PermissionRequired ->
                        emit(generation, currentSequence,
                            SelectedAudioConnectionStatus.PermissionRequired, cause)
                    PairedAudioRead.Disabled ->
                        emit(generation, currentSequence,
                            SelectedAudioConnectionStatus.BluetoothOff, cause)
                    PairedAudioRead.Unsupported ->
                        emit(generation, currentSequence,
                            SelectedAudioConnectionStatus.Unsupported, cause)
                    PairedAudioRead.Unavailable ->
                        emit(generation, currentSequence,
                            SelectedAudioConnectionStatus.Unavailable, cause)
                    is PairedAudioRead.Available -> {
                        val chosen = read.devices.firstOrNull {
                            BluetoothSelectionToken.matches(selectedToken, it.address)
                        }
                        if (chosen == null) {
                            emit(generation, currentSequence,
                                SelectedAudioConnectionStatus.DeviceUnpaired, cause)
                            return@launch
                        }
                        // Check grant AGAIN before profile API calls, as Android
                        // can revoke Nearby Devices during the suspend read.
                        if (!granted()) {
                            emit(generation, currentSequence,
                                SelectedAudioConnectionStatus.PermissionRequired, cause)
                            return@launch
                        }
                        val a2dpConnected = a2dp?.connectedDevices.orEmpty().any {
                            it.address.equals(chosen.address, ignoreCase = true)
                        }
                        val headsetConnected = headset?.connectedDevices.orEmpty().any {
                            it.address.equals(chosen.address, ignoreCase = true)
                        }
                        val result = when {
                            a2dpConnected -> SelectedAudioConnectionStatus.ConnectedA2dp
                            headsetConnected -> SelectedAudioConnectionStatus.ConnectedHeadset
                            a2dp != null && headset != null ->
                                SelectedAudioConnectionStatus.Disconnected
                            else -> SelectedAudioConnectionStatus.WaitingForProfiles
                        }
                        emit(generation, currentSequence, result, cause)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: SecurityException) {
                emit(generation, currentSequence,
                    SelectedAudioConnectionStatus.PermissionRequired, cause)
            } catch (_: Exception) {
                emit(generation, currentSequence,
                    SelectedAudioConnectionStatus.Unavailable, cause)
            }
        }
    }

    private fun emit(
        generation: Long,
        observedSequence: Long,
        result: SelectedAudioConnectionStatus,
        cause: BluetoothObservationCause,
    ) {
        if (!active || generation != epoch || observedSequence != sequence) return
        val finalCause = if (profileConnectPulse &&
            cause != BluetoothObservationCause.Initial &&
            result in setOf(
                SelectedAudioConnectionStatus.ConnectedA2dp,
                SelectedAudioConnectionStatus.ConnectedHeadset,
            )
        ) BluetoothObservationCause.ProfileConnectionEvent else cause
        // A connected-state broadcast cannot be replayed indefinitely. Other
        // incoming ACL/proxy events after this are verification only.
        profileConnectPulse = false
        onResult(result, finalCause)
    }
}
