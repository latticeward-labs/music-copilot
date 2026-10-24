package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.bluetooth.*
import org.junit.Assert.*
import org.junit.Test

/** Issue #9 P11: pure authored source tests; never run in P11. */
class Issue9Phase11BluetoothEntrySuggestionPolicyTest {
    private val evt = VerifiedAudioReconnectEvent(7L, 4L)
    private val online = SelectedAudioConnectionState(
        status = SelectedAudioConnectionStatus.ConnectedA2dp,
        selectedConnected = true, connectionEventId = 7L, canOfferEntry = true
    )
    private fun offer(
        old: BluetoothEntrySuggestionState = BluetoothEntrySuggestionState(),
        event: VerifiedAudioReconnectEvent = evt,
        state: SelectedAudioConnectionState = online,
        epoch: Long = 4L,
        allowed: Boolean = true,
        now: Long = 1000L,
    ) = BluetoothEntrySuggestionPolicy.onVerifiedReconnect(
        old, event, state, epoch, allowed, now
    )

    @Test fun startsWithNoPrompt() { assertNull(BluetoothEntrySuggestionState().pending) }
    @Test fun freshProfileEdgeOffersOnce() { assertEquals(evt, offer().pending) }
    @Test fun realHeadsetProfileMayOffer() {
        assertEquals(evt, offer(state = online.copy(
            status = SelectedAudioConnectionStatus.ConnectedHeadset)).pending)
    }
    @Test fun wrongSelectionEpochFailsClosed() { assertNull(offer(epoch = 5L).pending) }
    @Test fun wrongEventCounterFailsClosed() {
        assertNull(offer(state = online.copy(connectionEventId = 6L)).pending)
    }
    @Test fun savedHintWithoutConnectionCannotOffer() {
        assertNull(offer(state = online.copy(selectedConnected = false)).pending)
    }
    @Test fun disconnectedAudioCannotOffer() {
        assertNull(offer(state = online.copy(
            status = SelectedAudioConnectionStatus.Disconnected,
            selectedConnected = false)).pending)
    }
    @Test fun pendingProfileProxyCannotOffer() {
        assertNull(offer(state = online.copy(
            status = SelectedAudioConnectionStatus.WaitingForProfiles)).pending)
    }
    @Test fun systemOrSettingsRouteConsumesButNeverQueuesPrompt() {
        val s = offer(allowed = false)
        assertNull(s.pending)
        assertEquals(7L, s.latestHandledEventId)
    }
    @Test fun duplicateProfileEventIsIgnored() {
        val first = offer()
        assertEquals(first, offer(old = first))
    }
    @Test fun handledAndDismissedEdgeNeverReappears() {
        assertNull(offer(old = offer().copy(pending = null)).pending)
    }
    @Test fun olderCounterCannotOverwriteNewer() {
        val old = offer().copy(pending = null)
        val event = evt.copy(connectionEventId = 6L)
        assertNull(offer(old, event,
            online.copy(connectionEventId = 6L)).pending)
    }
    @Test fun pendingInviteNotReplacedByNewConnection() {
        val newer = evt.copy(connectionEventId = 8L)
        val result = offer(offer(), newer, online.copy(connectionEventId = 8L))
        assertEquals(evt, result.pending)
        assertEquals(8L, result.latestHandledEventId)
    }
    @Test fun declineDropsInviteWithoutAccepting() {
        val s = BluetoothEntrySuggestionPolicy.notNow(offer(), 1000L)
        assertNull(s.pending)
        assertEquals(301000L, s.declineCooldownUntilMs)
    }
    @Test fun cooldownBlocksNewConnectionAndConsumesEvent() {
        val declined = BluetoothEntrySuggestionPolicy.notNow(offer(), 1000L)
        val next = evt.copy(connectionEventId = 8L)
        val s = offer(declined, next, online.copy(connectionEventId = 8L),
            now = 5000L)
        assertNull(s.pending)
        assertEquals(8L, s.latestHandledEventId)
    }
    @Test fun reconnectionAfterCooldownCanBeOffered() {
        val declined = BluetoothEntrySuggestionPolicy.notNow(offer(), 1000L)
        val next = evt.copy(connectionEventId = 8L)
        assertEquals(next, offer(declined, next,
            online.copy(connectionEventId = 8L), now = 301001L).pending)
    }
    @Test fun repeatedDeclineNeverShortensCooldown() {
        val s = BluetoothEntrySuggestionPolicy.notNow(offer(), 10000L)
        val next = BluetoothEntrySuggestionPolicy.notNow(s, 500L)
        assertEquals(s.declineCooldownUntilMs, next.declineCooldownUntilMs)
    }
    @Test fun permissionRevocationHidesPrompt() {
        val s = BluetoothEntrySuggestionPolicy.reconcile(offer(),
            online.copy(status = SelectedAudioConnectionStatus.PermissionRequired,
                selectedConnected = false), 4L, true)
        assertNull(s.pending)
    }
    @Test fun backgroundHidesPromptImmediately() {
        assertNull(BluetoothEntrySuggestionPolicy.reconcile(
            offer(), online, 4L, false).pending)
    }
    @Test fun selectionChangeHidesOldPrompt() {
        assertNull(BluetoothEntrySuggestionPolicy.reconcile(
            offer(), online, 5L, true).pending)
    }
    @Test fun currentA2dpToHeadsetProfileCanRetainSameOffer() {
        assertEquals(evt, BluetoothEntrySuggestionPolicy.reconcile(
            offer(), online.copy(
                status = SelectedAudioConnectionStatus.ConnectedHeadset), 4L, true).pending)
    }
    @Test fun acceptDoesNothingExceptClearPendingInvitation() {
        val old = offer()
        val next = BluetoothEntrySuggestionPolicy.accepted(old)
        assertNull(next.pending)
        assertEquals(old.latestHandledEventId, next.latestHandledEventId)
    }
    @Test fun invalidationNeverResetsHandledCounter() {
        val old = offer()
        val next = BluetoothEntrySuggestionPolicy.invalidate(old)
        assertNull(next.pending)
        assertEquals(old.latestHandledEventId, next.latestHandledEventId)
    }
    @Test fun zeroCounterCannotOffer() {
        val invalid = VerifiedAudioReconnectEvent(0L, 4L)
        assertNull(offer(event = invalid,
            state = online.copy(connectionEventId = 0L)).pending)
    }
}
