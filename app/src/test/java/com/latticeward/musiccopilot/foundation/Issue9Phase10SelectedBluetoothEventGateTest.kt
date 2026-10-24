package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothObservationCause
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothSelectionToken
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionStatus
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedBluetoothConnectionGate
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #9 P10 — 24 source-authored pure JUnit tests, NOT EXECUTED.
 * Test tokens and MAC input are synthetic, no actual Bluetooth hardware or
 * receiver/proxy is started. Physical OEM/vehicle proof is P13 owner QA.
 */
class Issue9Phase10SelectedBluetoothEventGateTest {
    private val syntheticAddress = "02:11:22:33:44:55"
    private val token = requireNotNull(
        BluetoothSelectionToken.create(syntheticAddress, ByteArray(16) { 9 })
    )
    private val alternate = requireNotNull(
        BluetoothSelectionToken.create("06:AA:BB:CC:DD:EE", ByteArray(16) { 2 })
    )
    private fun gate() = SelectedBluetoothConnectionGate().also { it.start(token) }
    private fun SelectedBluetoothConnectionGate.update(
        status: SelectedAudioConnectionStatus,
        cause: BluetoothObservationCause = BluetoothObservationCause.ProfileConnectionEvent,
        current: String? = token,
    ) = observe(current, status, cause)

    @Test fun coldGateIsInactiveAndDoesNotSuggest() {
        val state = SelectedBluetoothConnectionGate().state
        assertEquals(SelectedAudioConnectionStatus.Inactive, state.status)
        assertFalse(state.selectedConnected)
        assertFalse(state.canOfferEntry)
    }

    @Test fun noSavedSelectionHasNoEligibility() {
        val g = SelectedBluetoothConnectionGate()
        g.start(null)
        assertEquals(SelectedAudioConnectionStatus.NoSelection, g.state.status)
        assertFalse(g.state.canOfferEntry)
    }

    @Test fun obsoleteRawMacPreferenceCannotActivateProfileMonitor() {
        val g = SelectedBluetoothConnectionGate()
        g.start(syntheticAddress)
        assertEquals(SelectedAudioConnectionStatus.NoSelection, g.state.status)
        assertFalse(g.state.selectedConnected)
    }

    @Test fun savedTokenIsNotConnectedOnStart() {
        val g = gate()
        assertEquals(SelectedAudioConnectionStatus.WaitingForProfiles, g.state.status)
        assertFalse(g.state.selectedConnected)
    }

    @Test fun alreadyConnectedA2dpOnInitialProbeDoesNotOfferEntry() {
        val g = gate()
        val result = g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.Initial)
        assertTrue(result.selectedConnected)
        assertFalse(result.canOfferEntry)
        assertEquals(0L, result.connectionEventId)
    }

    @Test fun alreadyConnectedHeadsetOnProxyArrivalDoesNotOfferEntry() {
        val g = gate()
        val result = g.update(SelectedAudioConnectionStatus.ConnectedHeadset,
            BluetoothObservationCause.ProfileService)
        assertTrue(result.selectedConnected)
        assertFalse(result.canOfferEntry)
    }

    @Test fun baselineDisconnectionArmsARealFutureConnection() {
        val g = gate()
        val offline = g.update(SelectedAudioConnectionStatus.Disconnected,
            BluetoothObservationCause.Initial)
        assertFalse(offline.canOfferEntry)
        val connected = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        assertTrue(connected.selectedConnected)
        assertTrue(connected.canOfferEntry)
        assertEquals(1L, connected.connectionEventId)
    }

    @Test fun headsetProfileCanCreateOneValidEdgeAfterOffline() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val result = g.update(SelectedAudioConnectionStatus.ConnectedHeadset)
        assertTrue(result.canOfferEntry)
        assertEquals(1L, result.connectionEventId)
    }

    @Test fun repeatIdenticalProfileConnectionDoesNotRepeatSuggestionEdge() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val first = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        val second = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        assertTrue(first.canOfferEntry)
        assertFalse(second.canOfferEntry)
        assertEquals(first.connectionEventId, second.connectionEventId)
    }

    @Test fun aclOnlyOrExternalProbeCannotCreateFirstConnectionEdge() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val connected = g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.ExternalRecheck)
        assertTrue(connected.selectedConnected)
        assertFalse(connected.canOfferEntry)
    }

    @Test fun profileProxyArrivalNeverBehavesLikeNewConnectionBroadcast() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        assertFalse(g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.ProfileService).canOfferEntry)
    }

    @Test fun connectedWithoutPreviouslyVerifiedOfflineHasNoOffer() {
        val g = gate()
        assertFalse(g.update(SelectedAudioConnectionStatus.ConnectedA2dp).canOfferEntry)
    }

    @Test fun reconnectAfterVerifiedDisconnectCreatesSecondDistinctEdge() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val first = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val second = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        assertEquals(first.connectionEventId + 1L, second.connectionEventId)
        assertTrue(second.canOfferEntry)
    }

    @Test fun connectedA2dpSwitchingToHeadsetIsNotNewReconnect() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        val a = g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        val h = g.update(SelectedAudioConnectionStatus.ConnectedHeadset)
        assertEquals(a.connectionEventId, h.connectionEventId)
        assertFalse(h.canOfferEntry)
    }

    @Test fun permissionRevocationImmediatelyInvalidatesActiveSuggestion() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        val denied = g.update(SelectedAudioConnectionStatus.PermissionRequired)
        assertFalse(denied.selectedConnected)
        assertFalse(denied.canOfferEntry)
    }

    @Test fun permissionRegrantWithoutNewDisconnectedProfileEventIsNotOffer() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.PermissionRequired)
        assertFalse(g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.ExternalRecheck).canOfferEntry)
    }

    @Test fun radioOffClearsConnectedAndArming() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        val status = g.update(SelectedAudioConnectionStatus.BluetoothOff)
        assertFalse(status.selectedConnected)
        assertFalse(status.canOfferEntry)
    }

    @Test fun unpairedDeviceClearsEligibilityImmediately() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedHeadset)
        val status = g.update(SelectedAudioConnectionStatus.DeviceUnpaired)
        assertFalse(status.selectedConnected)
        assertFalse(status.canOfferEntry)
    }

    @Test fun unavailableProfilesAreNotPermissionToOffer() {
        val g = gate()
        for (s in listOf(SelectedAudioConnectionStatus.WaitingForProfiles,
            SelectedAudioConnectionStatus.Unavailable,
            SelectedAudioConnectionStatus.Unsupported
        )) {
            val state = g.update(s)
            assertFalse(state.selectedConnected)
            assertFalse(state.canOfferEntry)
        }
    }

    @Test fun savedDeviceChangeInvalidatesOldSessionBeforeAnyNewObservation() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        g.start(alternate)
        assertFalse(g.state.selectedConnected)
        assertFalse(g.state.canOfferEntry)
        assertEquals(SelectedAudioConnectionStatus.WaitingForProfiles, g.state.status)
    }

    @Test fun obsoleteTokenCallbackCannotAdvanceNewSelection() {
        val g = gate()
        g.start(alternate)
        val stale = g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            current = token)
        assertFalse(stale.selectedConnected)
        assertFalse(stale.canOfferEntry)
    }

    @Test fun activityStopInvalidatesPreviouslyConnectedState() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        g.stop()
        assertEquals(SelectedAudioConnectionStatus.Inactive, g.state.status)
        assertFalse(g.state.selectedConnected)
    }

    @Test fun reopeningWhileAlreadyConnectedDoesNotReplayOldEdge() {
        val g = gate()
        g.update(SelectedAudioConnectionStatus.Disconnected)
        g.update(SelectedAudioConnectionStatus.ConnectedA2dp)
        g.stop()
        g.start(token)
        assertFalse(g.update(SelectedAudioConnectionStatus.ConnectedA2dp,
            BluetoothObservationCause.Initial).canOfferEntry)
    }

    @Test fun selectedConnectedStatusNeverPersistsIdentifierOrName() {
        val g = gate()
        val current = g.update(SelectedAudioConnectionStatus.ConnectedHeadset,
            BluetoothObservationCause.Initial)
        assertFalse(current.toString().contains(syntheticAddress))
        assertFalse(current.toString().contains(token))
    }
}
