package com.latticeward.musiccopilot.app.platform.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

/**
 * Issue #12 P05: sample ONLY the coarse connected output-device TYPE at
 * an explicitly accepted Driving-entry boundary, never on a timer, during
 * pairing or from a Bluetooth broadcast. No device name/address, ID, paired
 * list, route metadata or private token is accessed or exported.
 *
 * Null = OS audio output observation unavailable. The required v1 boolean
 * cannot represent unknown: the caller must DROP that event rather than
 * invent "bt_connected=false" for unobservable hardware.
 */
internal object CoarseBluetoothAudioOutput {
    fun isConnected(context: Context?): Boolean? {
        val app = context?.applicationContext ?: return null
        return try {
            val audio = app.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return null
            audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any { device ->
                isBluetoothOutputType(device.type)
            }
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun isBluetoothOutputType(type: Int): Boolean =
        type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                (type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    type == AudioDeviceInfo.TYPE_BLE_SPEAKER))
}
