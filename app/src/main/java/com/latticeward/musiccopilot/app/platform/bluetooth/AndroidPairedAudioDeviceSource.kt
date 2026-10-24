package com.latticeward.musiccopilot.app.platform.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDevice
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDeviceSource
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Issue #9 P09: never scans, pairs, connects, changes route, or registers a
 * Bluetooth listener. P10 owns lifecycle-aware profile connection events.
 * Every bonded-device read checks current OS grant (not ActivityResult/cache).
 */
class AndroidPairedAudioDeviceSource(context: Context) : PairedAudioDeviceSource {
    private val app = context.applicationContext

    private fun hasConnectAccess(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            ContextCompat.checkSelfPermission(app, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun readPairedAudio(): PairedAudioRead =
        withContext(Dispatchers.IO) {
            if (!hasConnectAccess()) return@withContext PairedAudioRead.PermissionRequired
            if (!app.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH))
                return@withContext PairedAudioRead.Unsupported
            val adapter = try {
                (app.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
            } catch (_: SecurityException) {
                return@withContext PairedAudioRead.PermissionRequired
            } catch (_: RuntimeException) { null }
                ?: return@withContext PairedAudioRead.Unsupported
            try {
                if (!hasConnectAccess()) return@withContext PairedAudioRead.PermissionRequired
                if (!adapter.isEnabled) return@withContext PairedAudioRead.Disabled
                val paired = adapter.bondedDevices ?: return@withContext PairedAudioRead.Available(emptyList())
                val valid = paired.mapNotNull { device ->
                    // No BLE scan/nearby discovery/location permission, only
                    // Android's already paired audio-video device classes.
                    if (device.bondState != android.bluetooth.BluetoothDevice.BOND_BONDED)
                        return@mapNotNull null
                    val btClass = device.bluetoothClass ?: return@mapNotNull null
                    if (btClass.majorDeviceClass != BluetoothClass.Device.Major.AUDIO_VIDEO)
                        return@mapNotNull null
                    val address = device.address ?: return@mapNotNull null
                    if (com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
                            .normalizeAddress(address) == null
                    ) return@mapNotNull null
                    val label = device.name?.trim().takeUnless { it.isNullOrEmpty() }
                        ?: "Paired audio device"
                    PairedAudioDevice(address.uppercase(Locale.ROOT), label)
                }.distinctBy { it.address }.sortedWith(compareBy(
                    { it.displayName.lowercase(Locale.ROOT) }, { it.address }
                ))
                // Grant may be revoked while evaluating individual devices.
                if (!hasConnectAccess()) PairedAudioRead.PermissionRequired
                else PairedAudioRead.Available(valid)
            } catch (_: SecurityException) {
                PairedAudioRead.PermissionRequired
            } catch (_: RuntimeException) {
                PairedAudioRead.Unavailable
            }
        }
}
