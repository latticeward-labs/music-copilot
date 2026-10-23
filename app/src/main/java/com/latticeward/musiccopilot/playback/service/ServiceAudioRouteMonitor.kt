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
) : AutoCloseable {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var registered = false
    private val callback = object : AudioDeviceCallback() {
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            if (removedDevices.any {
                    it.isSink && PlaybackInterruptionGate.isPersonalOutput(it.type)
                }
            ) {
                // The listener is registered on the main thread, same as
                // Media3, PlaybackCommandProcessor and route-generation gate.
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
