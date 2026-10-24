package com.latticeward.musiccopilot.playback.service

import android.media.AudioDeviceInfo
import android.os.Build
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

    var explicitlyPaused: Boolean = false
        private set

    // One physical route-loss episode may arrive as both the Media3 noisy
    // event and Android AudioDeviceCallback. Do not bump the epoch twice.
    // A fresh verified Play closes this episode; the next removal is new.
    private var routeEpochExhausted = false

    /**
     * Coarse event only, NEVER an active-route claim: the system reports
     * available Bluetooth sinks, not which one currently carries music.
     * Reconnect can NEVER clear requiresExplicitResume by itself.
     */
    var bluetoothReconnectedWhilePaused: Boolean = false
        private set
    private var bluetoothConnectionPlayQuarantine: Boolean = false

    /**
     * Android reports newly available devices, including initial enumeration.
     * If playback is currently paused, a controller Play immediately after
     * availability cannot be proven to be a user keypress, so quarantine it.
     * Do NOT pause uninterrupted playback on other routes.
     */
    fun onBluetoothSinkAvailable(alreadyPlayingOrRequested: Boolean) {
        if (requiresExplicitResume) bluetoothReconnectedWhilePaused = true
        if (!alreadyPlayingOrRequested) bluetoothConnectionPlayQuarantine = true
    }

    /**
     * Arm even when the Player is empty/idle: a suspended MediaStore queue
     * request may otherwise start playing on the built-in speaker later.
     * Returns true only for the first signal of this unsafe episode.
     */
    fun onHeadsetOrBluetoothLost(): Boolean {
        if (requiresExplicitResume) return false
        requiresExplicitResume = true
        bluetoothReconnectedWhilePaused = false
        bluetoothConnectionPlayQuarantine = true
        if (routeGeneration == Long.MAX_VALUE) {
            // Never wrap/reuse a stale epoch. At exhaustion, stay paused.
            routeEpochExhausted = true
        } else {
            routeGeneration++
        }
        return true
    }

    fun onExplicitUserPlaybackRequest() {
        // An explicit player/media-key/notification Play or selected song is
        // the ONLY way to clear a route-loss pause. Reconnecting alone is not.
        if (!routeEpochExhausted) requiresExplicitResume = false
        bluetoothReconnectedWhilePaused = false
        bluetoothConnectionPlayQuarantine = false
        explicitlyPaused = false
    }

    fun onManualPauseOrStop() {
        // Even if a delayed focus callback incorrectly claims playWhenReady,
        // explicit user Pause/Stop wins until the next explicit Play.
        explicitlyPaused = true
        // Do not clear an existing route-loss latch.
    }

    /**
     * P04: Media3 can remove transient suppression without a user command.
     * If the four-guard reducer refuses that recovery, preserve Safe Paused.
     * Distinct from manual Pause: never falsify manual intent/history.
     */
    fun onDisallowedFocusAutoResume() {
        explicitlyPaused = true // An explicit user Play is needed to clear.
    }

    fun shouldSuppressAutomaticPlay(): Boolean =
        requiresExplicitResume || explicitlyPaused || routeEpochExhausted ||
            bluetoothConnectionPlayQuarantine

    fun requestIsStillCurrent(expectedRouteGeneration: Long): Boolean =
        !routeEpochExhausted && routeGeneration == expectedRouteGeneration

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
            AudioDeviceInfo.TYPE_HEARING_AID -> true
            else -> isBleOutputOnApi31Plus(type)
        }

        /** Public Bluetooth output category only; no device/address/route ID. */
        fun isBluetoothOutput(type: Int): Boolean = when (type) {
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_HEARING_AID -> true
            else -> isBleOutputOnApi31Plus(type)
        }

        /**
         * BLE headset/speaker device categories were added in Android 12
         * (API 31), while minSdk is 29. Keep every API31 field access behind
         * an explicit runtime version gate for Android 10/11 and strict lint.
         * An unknown future device category is NOT evidence of a safe route.
         */
        private fun isBleOutputOnApi31Plus(type: Int): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
            return when (type) {
                AudioDeviceInfo.TYPE_BLE_HEADSET,
                AudioDeviceInfo.TYPE_BLE_SPEAKER -> true
                else -> false
            }
        }

        fun isNoisyChangeReason(reason: Int): Boolean =
            reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
    }
}
