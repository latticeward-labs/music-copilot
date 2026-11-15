package com.latticeward.musiccopilot.core.application.voice

import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceManualCorrectionCategory
import com.latticeward.musiccopilot.voice.api.VoiceOrigin

/**
 * Issue #12 P06: an ephemeral, explicit correction-intent ticket.
 *
 * Never infer a correction from a generic playback touch after Voice.
 * The user must CHOOSE "Correct using manual controls" on a terminal
 * Voice panel, then an in-origin, eligible local manual playback command
 * must be positively acknowledged by the existing service. The first ACK
 * consumes the ticket; failures, stale origins and unrelated touches
 * must not emit manual_correction.
 *
 * This is application memory only. It stores an enum/monotonic expiry,
 * never VoiceSessionId, transcript, media, candidate, command ID, user ID
 * or file path. No persistent identity or new Voice/Player authority.
 */
class ExplicitManualCorrectionOwner(
    private val nowElapsedMs: () -> Long,
    private val windowMs: Long = 60_000L,
) {
    init { require(windowMs in 1L..60_000L) }
    private data class Ticket(
        val origin: VoiceOrigin,
        val previousIntent: VoiceDiagnosticIntent,
        val expiresAt: Long,
    )
    private var pending: Ticket? = null

    @Synchronized
    fun arm(origin: VoiceOrigin, intent: VoiceDiagnosticIntent): Boolean {
        val now = nowElapsedMs()
        if (now < 0L || now > Long.MAX_VALUE - windowMs) return false
        pending = Ticket(origin, intent, now + windowMs)
        return true
    }

    @Synchronized
    fun observeAcknowledgedManualAction(
        origin: VoiceOrigin,
        category: VoiceManualCorrectionCategory,
    ): TelemetryEvent.ManualCorrection? {
        val ticket = pending ?: return null
        val now = nowElapsedMs()
        if (now < 0L || now > ticket.expiresAt) {
            pending = null
            return null
        }
        // Wrong origin cannot steal a ticket, but a later legitimate action
        // in the original origin can still complete it within the deadline.
        if (ticket.origin != origin) return null
        pending = null
        return TelemetryEvent.ManualCorrection(
            previousIntent = ticket.previousIntent.name,
            correctionType = when (category) {
                VoiceManualCorrectionCategory.MANUAL_PLAY -> "manual_play"
                VoiceManualCorrectionCategory.MANUAL_PAUSE -> "manual_pause"
                VoiceManualCorrectionCategory.MANUAL_SKIP -> "manual_skip"
                VoiceManualCorrectionCategory.MANUAL_SEARCH -> "manual_search"
                VoiceManualCorrectionCategory.MANUAL_SELECT -> "manual_select"
            }
        )
    }

    @Synchronized
    fun clear() { pending = null }
}
