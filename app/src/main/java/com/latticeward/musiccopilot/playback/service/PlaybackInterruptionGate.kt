package com.latticeward.musiccopilot.playback.service

import android.media.AudioDeviceInfo
import androidx.media3.common.Player

/**
 * P09 high-priority route-safety policy: no Activity, raw device identifiers,
 * network, persisted endpoint labels or secondary player/focus manager.
 *
 * ExoPlayer is the sole audio-focus owner. This gate protects against
 * asynchronous MediaStore work completing after an external route disappears
 * and prevents a focus/system callback from reopening a lost route.
 */
internal class PlaybackInterruptionGate {
    var routeGeneration: Long = 0L
        private set

    var requiresExplicitResume: Boolean = false
        private set

    fun onHeadsetOrBluetoothLost() {
        routeGeneration++
        requiresExplicitResume = true
    }

    fun onExplicitUserPlaybackRequest() {
        // An explicit player/media-key/notification Play or selected song is
        // the ONLY way to clear a route-loss pause. Reconnecting alone is not.
        requiresExplicitResume = false
    }

    fun onManualPauseOrStop() {
        // Manual pause cancels desired playback. ExoPlayer's focus handling
        // observes playWhenReady=false, so a later focus GAIN cannot undo it.
        // Do not clear an existing route-loss latch.
    }

    fun shouldSuppressAutomaticPlay(): Boolean = requiresExplicitResume

    fun requestIsStillCurrent(expectedRouteGeneration: Long): Boolean =
        routeGeneration == expectedRouteGeneration

    companion object {
        /**
         * Only private/external output loss, never built-in speaker/mic.
         * A conservative fail-closed pause is allowed if the removed device
         * was attached but not selected: Android exposes no universally
         * reliable unprivileged currently-active route on API 29+.
         */
        fun isPersonalOutput(type: Int): Boolean = when (type) {
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_ACCESSORY,
            AudioDeviceInfo.TYPE_HEARING_AID,
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
            else -> false
        }

        fun isNoisyChangeReason(reason: Int): Boolean =
            reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
    }
}
