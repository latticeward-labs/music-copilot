package com.latticeward.musiccopilot.playback.service

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper

/**
 * Service-owned Android API 29+ output-disconnect observer. This is a
 * second independent *signal*, not another audio focus owner/player.
 *
 * Media3's handleAudioBecomingNoisy(true) runs before a typical speaker
 * reroute; this callback provides a fail-closed fallback for OEM/Bluetooth
 * paths that omit AUDIO_BECOMING_NOISY.
 *
 * Only device categories are examined; NEVER collect device names, MAC
 * addresses, IDs, manufacturer information or user audio.
 */
internal class ServiceAudioRouteMonitor(
    context: Context,
    private val onPersonalOutputDisconnected: () -> Unit,
    private val onBluetoothSinkAvailable: () -> Unit = {},
) : AutoCloseable {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var registered = false
    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            if (!registered) return
            if (addedDevices.any {
                    it.isSink && PlaybackInterruptionGate.isBluetoothOutput(it.type)
                }
            ) {
                // May include the initial API-provided list of available
                // outputs. Presence is NOT proof of selection/active routing,
                // intentional Play, focus regain or successful pairing.
                // Never trigger Player.play(), restore playback or clear a
                // route-loss latch just because an A2DP/BLE sink appeared.
                onBluetoothSinkAvailable()
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            // OEMs can deliver already-queued callbacks after unregister.
            // No released Player may receive a late route signal.
            if (!registered) return
            if (removedDevices.any {
                    it.isSink && PlaybackInterruptionGate.isPersonalOutput(it.type)
                }
            ) {
                // One callback may remove several endpoints, but constitutes
                // just one safety signal. Do not require a nonempty Player:
                // an in-flight queue request may still be verifying MediaStore
                // content while the old headset/USB output disappears.
                // Both callbacks and Player policy run on the service Main.
                onPersonalOutputDisconnected()
            }
        }
    }

    fun start() {
        if (registered) return
        try {
            audioManager.registerAudioDeviceCallback(
                callback, Handler(Looper.getMainLooper())
            )
            registered = true
        } catch (_: RuntimeException) {
            // Some OEMs block device callbacks; Media3's own Noisy listener
            // remains enabled. Never log identifiable endpoint information.
        }
    }

    override fun close() {
        if (!registered) return
        registered = false
        try {
            audioManager.unregisterAudioDeviceCallback(callback)
        } catch (_: RuntimeException) {
            // Teardown must never interrupt the single ExoPlayer release.
        }
    }
}
