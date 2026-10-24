package com.latticeward.musiccopilot.feature.settings.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDevice

/**
 * HF-10 Settings / CE-20 P1 section. No nested NavHost or forced Driving.
 * Open/Refresh enumerate already-paired audio only. Save is the *only* user
 * action that opts into a stored device preference. Connection ≠ entering.
 */
@Composable
fun BluetoothPreferenceSettingsSection(
    model: BluetoothSelectionViewModel,
    persistedToken: String?,
    persistenceReady: Boolean,
    connectPermissionGranted: Boolean,
    onRequestConnectPermission: () -> Unit,
    onOpenAndroidPermissionSettings: () -> Unit,
) {
    val ui by model.ui.collectAsStateWithLifecycle()
    var expanded by rememberSaveable { mutableStateOf(false) }
    // Disposing Settings loses drafts but never changes committed DataStore.
    DisposableEffect(model) { onDispose { model.dismiss() } }
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = MusicSpacing.lgDp),
        verticalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
    ) {
        Text(
            stringResource(R.string.i9_bt_section_title),
            modifier = Modifier.semantics { heading() },
            color = MusicColors.textPrimary,
            style = MusicTextStyles.titleMedium,
        )
        Text(
            stringResource(R.string.i9_bt_section_subtitle),
            color = MusicColors.textSecondary,
            style = MusicTextStyles.bodyMedium,
        )
        if (!expanded) {
            OutlinedButton(
                onClick = {
                    expanded = true
                    // Explicit user Open only; not auto on Settings, startup,
                    // permissions result, resumed Activity or BT reconnect.
                    if (connectPermissionGranted) model.refresh()
                },
                modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
            ) { Text(stringResource(R.string.i9_bt_open)) }
            if (persistedToken != null) {
                Text(
                    stringResource(R.string.i9_bt_saved_preference),
                    color = MusicColors.textSecondary,
                    style = MusicTextStyles.bodyMedium,
                )
            }
        } else {
            Text(
                stringResource(R.string.i9_bt_selection_title),
                modifier = Modifier.semantics { heading() },
                color = MusicColors.textPrimary,
                style = MusicTextStyles.titleMedium,
            )
            Text(
                stringResource(R.string.i9_bt_selection_subtitle),
                color = MusicColors.textSecondary,
                style = MusicTextStyles.bodyMedium,
            )
            Text(
                stringResource(R.string.i9_bt_connection_note),
                color = MusicColors.textPrimary,
                style = MusicTextStyles.bodyMedium,
            )
            Text(
                stringResource(R.string.i9_bt_privacy_note),
                color = MusicColors.textSecondary,
                style = MusicTextStyles.bodyMedium,
            )
            Text(
                stringResource(R.string.i9_bt_never_automatic),
                color = MusicColors.textPrimary,
                style = MusicTextStyles.bodyMedium,
            )
            if (!connectPermissionGranted) {
                Text(
                    stringResource(R.string.i9_bt_permission_needed),
                    color = MusicColors.textSecondary,
                    style = MusicTextStyles.bodyMedium,
                )
                OutlinedButton(
                    onClick = onRequestConnectPermission,
                    enabled = !ui.busy,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.i9_bt_request_permission)) }
                OutlinedButton(
                    onClick = onOpenAndroidPermissionSettings,
                    enabled = !ui.busy,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.i9_bt_app_settings)) }
            } else {
                OutlinedButton(
                    onClick = model::refresh,
                    enabled = !ui.busy,
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.i9_bt_refresh)) }
                if (ui.readAttempted) {
                    ui.devices.forEachIndexed { index, device ->
                        BluetoothDeviceChoice(
                            ordinal = index + 1,
                            device = device,
                            checked = ui.draftAddress == device.address,
                            committed = BluetoothSelectionToken.matches(
                                persistedToken, device.address
                            ),
                            enabled = !ui.busy,
                            onSelect = { model.stage(device.address) },
                        )
                    }
                }
                if (persistedToken != null &&
                    ui.devices.none { BluetoothSelectionToken.matches(persistedToken, it.address) }
                ) {
                    Text(
                        stringResource(R.string.i9_bt_saved_cannot_verify),
                        color = MusicColors.textSecondary,
                        style = MusicTextStyles.bodyMedium,
                    )
                }
                OutlinedButton(
                    onClick = model::save,
                    enabled = !ui.busy && persistenceReady &&
                        ui.draftAddress != null && ui.readAttempted &&
                        ui.devices.any { it.address == ui.draftAddress },
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = MusicSize.touchTargetMinDp)
                        .semantics {
                            contentDescription = "Save selected Bluetooth audio device"
                        },
                ) { Text(stringResource(R.string.i9_bt_save)) }
            }

            // The user can REMOVE a stale selection without OS Bluetooth
            // access. This does not clear/modify paired Android device data.
            if (persistedToken != null) {
                OutlinedButton(
                    onClick = model::remove,
                    enabled = !ui.busy && persistenceReady,
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = MusicSize.touchTargetMinDp)
                        .semantics {
                            contentDescription = "Remove saved Bluetooth device preference"
                        },
                ) { Text(stringResource(R.string.i9_bt_remove)) }
            }
            val feedback = when (ui.feedback) {
                BluetoothSelectionFeedback.None -> null
                BluetoothSelectionFeedback.Saved -> R.string.i9_bt_saved
                BluetoothSelectionFeedback.Removed -> R.string.i9_bt_removed
                BluetoothSelectionFeedback.StorageUnavailable -> R.string.i9_bt_storage_failed
                BluetoothSelectionFeedback.DeviceChanged -> R.string.i9_bt_device_changed
                BluetoothSelectionFeedback.PermissionLost -> R.string.i9_bt_permission_needed
                BluetoothSelectionFeedback.BluetoothDisabled -> R.string.i9_bt_disabled
                BluetoothSelectionFeedback.Unsupported -> R.string.i9_bt_unsupported
                BluetoothSelectionFeedback.ReadFailed -> R.string.i9_bt_read_failed
                BluetoothSelectionFeedback.NoPairedAudio -> R.string.i9_bt_no_paired_audio
            }
            if (feedback != null) Text(
                stringResource(feedback),
                color = MusicColors.textPrimary,
                style = MusicTextStyles.bodyMedium,
            )
            if (ui.busy) Text(
                stringResource(R.string.i9_bt_working),
                color = MusicColors.textSecondary,
                style = MusicTextStyles.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp)) {
                OutlinedButton(
                    onClick = {
                        if (!ui.busy) {
                            model.cancelDraft()
                            expanded = false
                        }
                    },
                    enabled = !ui.busy,
                    modifier = Modifier.heightIn(min = MusicSize.touchTargetMinDp),
                ) { Text(stringResource(R.string.i9_bt_cancel)) }
            }
        }
    }
}

@Composable
private fun BluetoothDeviceChoice(
    ordinal: Int,
    device: PairedAudioDevice,
    checked: Boolean,
    committed: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
) {
    val label = buildString {
        append(device.displayName)
        append(" · ")
        append(ordinal)
        if (checked) append(" · selected, not saved")
        else if (committed) append(" · saved preference")
    }
    OutlinedButton(
        onClick = onSelect,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = MusicSize.touchTargetMinDp),
    ) {
        Text(label, style = MusicTextStyles.bodyLarge)
    }
}
