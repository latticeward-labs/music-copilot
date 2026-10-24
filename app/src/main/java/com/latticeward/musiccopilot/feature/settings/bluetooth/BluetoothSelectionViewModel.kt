package com.latticeward.musiccopilot.feature.settings.bluetooth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDevice
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioDeviceSource
import com.latticeward.musiccopilot.core.application.bluetooth.PairedAudioRead
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class BluetoothSelectionFeedback {
    None, Saved, Removed, StorageUnavailable, DeviceChanged, PermissionLost,
    BluetoothDisabled, Unsupported, ReadFailed, NoPairedAudio,
}

data class BluetoothSelectionUiState(
    val devices: List<PairedAudioDevice> = emptyList(),
    val draftAddress: String? = null,
    val busy: Boolean = false,
    val readAttempted: Boolean = false,
    val feedback: BluetoothSelectionFeedback = BluetoothSelectionFeedback.None,
)

/**
 * User-controlled, Settings-only P1 preference. Holds the raw selected MAC
 * only in transient memory; DataStore receives only an opaque salted digest.
 * No BT connection/suggestion events, no media control, no auto-Driving.
 */
class BluetoothSelectionViewModel(
    private val preferences: PreferencesRepository,
    private val source: PairedAudioDeviceSource,
) : ViewModel() {
    private val mutable = MutableStateFlow(BluetoothSelectionUiState())
    val ui: StateFlow<BluetoothSelectionUiState> = mutable

    /** Called only by explicit Open/Refresh. No background enumeration. */
    fun refresh() {
        if (mutable.value.busy) return
        mutable.value = mutable.value.copy(busy = true, feedback = BluetoothSelectionFeedback.None)
        viewModelScope.launch {
            try {
                val result = source.readPairedAudio()
                mutable.value = when (result) {
                    is PairedAudioRead.Available -> {
                        val selected = mutable.value.draftAddress?.takeIf { address ->
                            result.devices.any { it.address == address }
                        }
                        mutable.value.copy(
                            devices = result.devices, draftAddress = selected,
                            readAttempted = true, busy = false,
                            feedback = if (result.devices.isEmpty())
                                BluetoothSelectionFeedback.NoPairedAudio
                            else BluetoothSelectionFeedback.None,
                        )
                    }
                    else -> mutable.value.copy(
                        devices = emptyList(), draftAddress = null, busy = false,
                        readAttempted = true, feedback = readFeedback(result),
                    )
                }
            } catch (cancelled: CancellationException) {
                mutable.value = mutable.value.copy(busy = false)
                throw cancelled
            } catch (_: Exception) {
                mutable.value = mutable.value.copy(
                    busy = false, devices = emptyList(), draftAddress = null,
                    feedback = BluetoothSelectionFeedback.ReadFailed
                )
            }
        }
    }

    /** Stages a device; Back/Cancel deliberately never persists this draft. */
    fun stage(address: String) {
        if (mutable.value.busy || mutable.value.devices.none { it.address == address })
            return
        mutable.value = mutable.value.copy(
            draftAddress = address, feedback = BluetoothSelectionFeedback.None
        )
    }

    fun cancelDraft() {
        if (mutable.value.busy) return
        mutable.value = mutable.value.copy(
            draftAddress = null, feedback = BluetoothSelectionFeedback.None
        )
    }

    /** Revalidates the *fresh* OS paired list at commit time, not stale UI. */
    fun save() {
        val current = mutable.value
        val candidate = current.draftAddress ?: return
        if (current.busy || current.devices.none { it.address == candidate }) return
        if (preferences.loadState.value != PreferencesLoadState.Ready) {
            mutable.value = current.copy(feedback = BluetoothSelectionFeedback.StorageUnavailable)
            return
        }
        mutable.value = current.copy(busy = true, feedback = BluetoothSelectionFeedback.None)
        viewModelScope.launch {
            try {
                val fresh = source.readPairedAudio()
                when (fresh) {
                    is PairedAudioRead.Available -> {
                        if (fresh.devices.none { it.address == candidate }) {
                            mutable.value = mutable.value.copy(
                                busy = false, draftAddress = null, devices = fresh.devices,
                                feedback = BluetoothSelectionFeedback.DeviceChanged
                            )
                            return@launch
                        }
                    }
                    else -> {
                        mutable.value = mutable.value.copy(
                            busy = false, devices = emptyList(), draftAddress = null,
                            feedback = readFeedback(fresh)
                        )
                        return@launch
                    }
                }
                val token = BluetoothSelectionToken.create(candidate)
                if (token == null) {
                    mutable.value = mutable.value.copy(
                        busy = false, feedback = BluetoothSelectionFeedback.DeviceChanged
                    )
                    return@launch
                }
                // Optimistic local UI is forbidden: only a successful DataStore
                // commit AND matching result can show Saved.
                val result = preferences.update(PreferenceUpdate.SetBluetoothHint(token))
                mutable.value = mutable.value.copy(
                    busy = false, draftAddress = null,
                    feedback = if (result is AppResult.Success &&
                        result.value.selectedBluetoothHint == token)
                        BluetoothSelectionFeedback.Saved
                    else BluetoothSelectionFeedback.StorageUnavailable
                )
            } catch (cancelled: CancellationException) {
                mutable.value = mutable.value.copy(busy = false)
                throw cancelled
            } catch (_: Exception) {
                mutable.value = mutable.value.copy(
                    busy = false, feedback = BluetoothSelectionFeedback.StorageUnavailable
                )
            }
        }
    }

    /** Removal works even if BT is off or permission revoked. */
    fun remove() {
        if (mutable.value.busy) return
        if (preferences.loadState.value != PreferencesLoadState.Ready) {
            mutable.value = mutable.value.copy(
                feedback = BluetoothSelectionFeedback.StorageUnavailable
            )
            return
        }
        mutable.value = mutable.value.copy(busy = true, feedback = BluetoothSelectionFeedback.None)
        viewModelScope.launch {
            try {
                val result = preferences.update(PreferenceUpdate.SetBluetoothHint(null))
                mutable.value = mutable.value.copy(
                    busy = false, draftAddress = null,
                    feedback = if (result is AppResult.Success &&
                        result.value.selectedBluetoothHint == null)
                        BluetoothSelectionFeedback.Removed
                    else BluetoothSelectionFeedback.StorageUnavailable
                )
            } catch (cancelled: CancellationException) {
                mutable.value = mutable.value.copy(busy = false)
                throw cancelled
            } catch (_: Exception) {
                mutable.value = mutable.value.copy(
                    busy = false, feedback = BluetoothSelectionFeedback.StorageUnavailable
                )
            }
        }
    }

    fun dismiss() {
        // Closing Settings does not save a staged selection. OS grant is
        // always rechecked by the next explicit read/save on opening.
        if (!mutable.value.busy) mutable.value = BluetoothSelectionUiState()
    }

    private fun readFeedback(result: PairedAudioRead): BluetoothSelectionFeedback = when (result) {
        PairedAudioRead.PermissionRequired -> BluetoothSelectionFeedback.PermissionLost
        PairedAudioRead.Disabled -> BluetoothSelectionFeedback.BluetoothDisabled
        PairedAudioRead.Unsupported -> BluetoothSelectionFeedback.Unsupported
        PairedAudioRead.Unavailable -> BluetoothSelectionFeedback.ReadFailed
        is PairedAudioRead.Available -> BluetoothSelectionFeedback.None
    }
}
