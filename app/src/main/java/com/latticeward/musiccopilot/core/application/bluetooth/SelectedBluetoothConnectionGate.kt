package com.latticeward.musiccopilot.core.application.bluetooth

/**
 * Issue #9 P10 — pure selected-audio connection evidence.
 *
 * An ACL / bond event is NEVER connection proof. The Android source must
 * verify both current bonding and an A2DP/HFP profile connection before
 * reporting Connected. The persisted salted device hint is not an OS grant.
 */
enum class SelectedAudioConnectionStatus {
    Inactive, NoSelection, PermissionRequired, BluetoothOff, Unsupported,
    DeviceUnpaired, WaitingForProfiles, Disconnected, ConnectedA2dp,
    ConnectedHeadset, Unavailable,
}

enum class BluetoothObservationCause {
    Initial, ProfileService, ProfileConnectionEvent, ExternalRecheck,
}

/** No name, address, BluetoothDevice or saved token ever enters UI state. */
data class SelectedAudioConnectionState(
    val status: SelectedAudioConnectionStatus = SelectedAudioConnectionStatus.Inactive,
    val selectedConnected: Boolean = false,
    /** Increases only after a verified disconnected -> connected profile event. */
    val connectionEventId: Long = 0L,
    val canOfferEntry: Boolean = false,
)

/**
 * Main-thread owned, deterministic reducer. Each selection/foreground epoch
 * starts unarmed. This avoids a surprise suggestion from an already connected
 * car, initial profile-proxy callback or Activity recreation.
 */
class SelectedBluetoothConnectionGate {
    private var active = false
    private var token: String? = null
    private var knownDisconnected = false
    private var current = SelectedAudioConnectionState()
    private var eventCounter = 0L

    val state: SelectedAudioConnectionState get() = current

    fun start(selectedToken: String?) {
        active = BluetoothSelectionToken.isCurrentFormat(selectedToken)
        token = selectedToken.takeIf { active }
        knownDisconnected = false
        current = SelectedAudioConnectionState(
            status = if (active) SelectedAudioConnectionStatus.WaitingForProfiles
                     else SelectedAudioConnectionStatus.NoSelection,
            connectionEventId = eventCounter,
        )
    }

    fun stop() {
        active = false
        token = null
        knownDisconnected = false
        current = SelectedAudioConnectionState(
            status = SelectedAudioConnectionStatus.Inactive,
            connectionEventId = eventCounter,
        )
    }

    fun observe(
        selectedToken: String?,
        status: SelectedAudioConnectionStatus,
        cause: BluetoothObservationCause,
    ): SelectedAudioConnectionState {
        if (!active) return current
        if (selectedToken == null || token != selectedToken) {
            // Old callbacks must neither grant eligibility nor clobber a
            // newer, still-valid selection that is being verified.
            return current
        }
        val connected = status == SelectedAudioConnectionStatus.ConnectedA2dp ||
            status == SelectedAudioConnectionStatus.ConnectedHeadset
        // Only profile connection broadcasts plus a prior *verified* negative
        // observation may increment the one-shot edge counter.
        if (connected && knownDisconnected &&
            cause == BluetoothObservationCause.ProfileConnectionEvent
        ) {
            eventCounter++
        }
        knownDisconnected = if (connected) false
            else status == SelectedAudioConnectionStatus.Disconnected

        current = SelectedAudioConnectionState(
            status = status,
            selectedConnected = connected,
            connectionEventId = eventCounter,
            canOfferEntry = connected && cause == BluetoothObservationCause.ProfileConnectionEvent &&
                eventCounter > 0L && current.connectionEventId != eventCounter,
        )
        return current
    }
}
