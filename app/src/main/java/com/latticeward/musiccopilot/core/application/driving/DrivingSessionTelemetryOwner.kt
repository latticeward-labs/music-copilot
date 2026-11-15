package com.latticeward.musiccopilot.core.application.driving

import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.voice.api.VoiceOrigin

/**
 * Issue #12 P08: one application-process Driving session diagnostic owner.
 *
 * This is NOT a player/session service, does not own navigation and cannot
 * initiate/stop playback. It exists only after one authorized, accepted
 * inactive -> active Driving transition. It does not persist any identifier,
 * location, media/artist, raw voice, wall-clock timestamp or user data.
 *
 * Configuration recreation/background/focus/BT interruption are NOT exits.
 * Only a verified explicit UI Exit/Back is eligible to produce completion.
 * System invalidation and process teardown abandon without completion.
 * A force kill loses this volatile state: future P09/P10 must report
 * unverifiable sessions UNKNOWN, never cleanly completed/crash-free.
 */
class DrivingSessionTelemetryOwner(
    private val elapsedRealtimeMs: () -> Long,
) {
    enum class ExitEvidence {
        NONE, EXPLICIT_COMPLETED, ABANDONED_UNKNOWN, INVALID_MEASUREMENT
    }

    private class ActiveSession(
        val startedAt: Long,
        var acknowledgedVoiceCommands: Int = 0,
        var countOverflow: Boolean = false,
    )

    private var active: ActiveSession? = null
    private var lastOutcome: ExitEvidence = ExitEvidence.NONE

    /** Called only AFTER DrivingNavigationViewModel accepts the entry. */
    @Synchronized
    fun beginFromAcceptedEntry(): Boolean {
        if (active != null) return false
        val start = safeMonotonicNow() ?: return false
        active = ActiveSession(start)
        lastOutcome = ExitEvidence.NONE
        return true
    }

    /**
     * Called only from VoiceSessionState.Executed after a real Media3 ACK
     * and only for origin Driving, never from UI Mic tap or recognition.
     * Bounds are exact: when 1001st successful command arrives we refuse
     * a falsely truncated commands_count=1000 event at session end.
     */
    @Synchronized
    fun noteAcknowledgedVoiceCommand(origin: VoiceOrigin): Boolean {
        if (origin != VoiceOrigin.Driving) return false
        val session = active ?: return false
        if (session.countOverflow) return false
        if (session.acknowledgedVoiceCommands == MAX_COMMAND_COUNT) {
            session.countOverflow = true
            return false
        }
        session.acknowledgedVoiceCommands++
        return true
    }

    /**
     * One real explicit Exit/Back consumes the active ticket synchronously.
     * All duplicate exits are null. Negative clock delta, clock failure and
     * command-count overflow are UNKNOWN, never completed with made-up data.
     */
    @Synchronized
    fun completeFromExplicitExit(): TelemetryEvent.SessionCompleted? {
        val session = active ?: return null
        active = null // claim completion BEFORE any optional Quality callback
        val end = safeMonotonicNow()
        if (end == null || end < session.startedAt || session.countOverflow) {
            lastOutcome = ExitEvidence.INVALID_MEASUREMENT
            return null
        }
        val durationMs = end - session.startedAt
        lastOutcome = ExitEvidence.EXPLICIT_COMPLETED
        return TelemetryEvent.SessionCompleted(
            durationBucket = when {
                durationMs < 60_000L -> "under_1m"
                durationMs < 300_000L -> "1_to_5m"
                durationMs < 900_000L -> "5_to_15m"
                else -> "over_15m"
            },
            commandsCount = session.acknowledgedVoiceCommands,
        )
    }

    /** Route/permission/system boundary is NOT an explicit user exit. */
    @Synchronized
    fun abandonWithoutCompletion() {
        if (active != null) {
            active = null
            lastOutcome = ExitEvidence.ABANDONED_UNKNOWN
        }
    }

    /** Consent revocation/close discards even the in-memory evidence. */
    @Synchronized
    fun forgetForPrivacy() {
        active = null
        lastOutcome = ExitEvidence.NONE
    }

    @Synchronized
    fun isTracking(): Boolean = active != null

    @Synchronized
    fun mostRecentEvidence(): ExitEvidence = lastOutcome

    private fun safeMonotonicNow(): Long? =
        try { elapsedRealtimeMs().takeIf { it >= 0L } }
        catch (_: Exception) { null }

    companion object {
        const val MAX_COMMAND_COUNT = 1000
    }
}
