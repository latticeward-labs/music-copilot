package com.latticeward.musiccopilot.core.application.bluetooth

/**
 * Issue #9 P11 — a suggestion is a disposable *invitation*, never an action.
 * No device address/name, media item, OS permission or stored token is held.
 */
data class VerifiedAudioReconnectEvent(
    val connectionEventId: Long,
    val selectionEpoch: Long,
)

data class BluetoothEntrySuggestionState(
    val pending: VerifiedAudioReconnectEvent? = null,
    val latestHandledEventId: Long = 0L,
    val declineCooldownUntilMs: Long = 0L,
)

object BluetoothEntrySuggestionPolicy {
    const val DECLINE_COOLDOWN_MS: Long = 5 * 60 * 1000L

    private fun matchingAndConnected(
        event: VerifiedAudioReconnectEvent,
        evidence: SelectedAudioConnectionState,
        currentSelectionEpoch: Long,
    ): Boolean = event.connectionEventId > 0L &&
        event.selectionEpoch == currentSelectionEpoch &&
        evidence.selectedConnected &&
        evidence.connectionEventId == event.connectionEventId &&
        evidence.status in setOf(
            SelectedAudioConnectionStatus.ConnectedA2dp,
            SelectedAudioConnectionStatus.ConnectedHeadset,
        )

    /** The one-shot event is consumed even on an ineligible foreground route. */
    fun onVerifiedReconnect(
        old: BluetoothEntrySuggestionState,
        event: VerifiedAudioReconnectEvent,
        evidence: SelectedAudioConnectionState,
        currentSelectionEpoch: Long,
        surfaceAllowed: Boolean,
        elapsedMs: Long,
    ): BluetoothEntrySuggestionState {
        if (event.connectionEventId <= old.latestHandledEventId) return old
        val consumed = old.copy(
            latestHandledEventId = event.connectionEventId,
        )
        if (!surfaceAllowed || !matchingAndConnected(event, evidence, currentSelectionEpoch) ||
            elapsedMs < old.declineCooldownUntilMs || old.pending != null
        ) return consumed
        return consumed.copy(pending = event)
    }

    /** Any route, permission, selection or audio-profile invalidation hides it. */
    fun reconcile(
        old: BluetoothEntrySuggestionState,
        evidence: SelectedAudioConnectionState,
        currentSelectionEpoch: Long,
        surfaceAllowed: Boolean,
    ): BluetoothEntrySuggestionState {
        val event = old.pending ?: return old
        return if (surfaceAllowed &&
            matchingAndConnected(event, evidence, currentSelectionEpoch)
        ) old else old.copy(pending = null)
    }

    fun notNow(
        old: BluetoothEntrySuggestionState,
        elapsedMs: Long,
    ): BluetoothEntrySuggestionState = old.copy(
        pending = null,
        declineCooldownUntilMs = (elapsedMs + DECLINE_COOLDOWN_MS)
            .coerceAtLeast(old.declineCooldownUntilMs),
    )

    /** Only call after a fresh foreground gate and explicit tap succeed. */
    fun accepted(old: BluetoothEntrySuggestionState): BluetoothEntrySuggestionState =
        old.copy(pending = null)

    fun invalidate(old: BluetoothEntrySuggestionState): BluetoothEntrySuggestionState =
        old.copy(pending = null)
}
