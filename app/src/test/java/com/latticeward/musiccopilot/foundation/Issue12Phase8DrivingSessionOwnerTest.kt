package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.driving.DrivingSessionTelemetryOwner
import com.latticeward.musiccopilot.core.application.driving.DrivingSessionTelemetryOwner.ExitEvidence
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import org.junit.Assert.*
import org.junit.Test

/** P08 SOURCE ONLY; deliberately NOT compiled, linted or executed until P14. */
class Issue12Phase8DrivingSessionOwnerTest {
    @Test fun startsOnceOnlyOnAcceptedEntryWithoutDuplicateStart() {
        var now = 10L
        val owner = DrivingSessionTelemetryOwner { now }
        assertTrue(owner.beginFromAcceptedEntry())
        assertFalse(owner.beginFromAcceptedEntry())
        assertTrue(owner.isTracking())
        assertEquals(ExitEvidence.NONE, owner.mostRecentEvidence())
    }

    @Test fun completedFromRealExplicitExitExactlyOnce() {
        var now = 10L
        val owner = DrivingSessionTelemetryOwner { now }
        assertTrue(owner.beginFromAcceptedEntry())
        now += 30_000L
        val event = owner.completeFromExplicitExit()
        assertEquals(TelemetryEvent.SessionCompleted("under_1m", 0), event)
        assertNull(owner.completeFromExplicitExit())
        assertFalse(owner.isTracking())
        assertEquals(ExitEvidence.EXPLICIT_COMPLETED, owner.mostRecentEvidence())
    }

    @Test fun oneAckedDrivingCommandAddsOneVoiceCommandAndNothingElse() {
        var now = 20L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        assertFalse(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Player))
        assertTrue(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
        assertTrue(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
        now += 700L
        assertEquals(2, owner.completeFromExplicitExit()!!.commandsCount)
        assertFalse(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
    }

    @Test fun failedNoMatchAndMicTapHaveNoCountSource() {
        val owner = DrivingSessionTelemetryOwner { 1L }
        owner.beginFromAcceptedEntry()
        assertEquals(0, owner.completeFromExplicitExit()!!.commandsCount)
    }

    @Test fun durationBucketBoundaryAtOneMinuteIsExact() {
        var now = 0L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        now = 59_999L
        assertEquals("under_1m", owner.completeFromExplicitExit()!!.durationBucket)
        owner.beginFromAcceptedEntry()
        now = 119_999L
        assertEquals("1_to_5m", owner.completeFromExplicitExit()!!.durationBucket)
    }

    @Test fun durationBucketBoundaryAtFiveMinutesIsExact() {
        var now = 100L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        now += 300_000L
        assertEquals("5_to_15m", owner.completeFromExplicitExit()!!.durationBucket)
    }

    @Test fun durationBucketBoundaryAtFifteenMinutesIsExact() {
        var now = 100L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        now += 900_000L
        assertEquals("over_15m", owner.completeFromExplicitExit()!!.durationBucket)
    }

    @Test fun duplicateCallbacksAfterExitDoNotEmitAgainOrIncreaseCounts() {
        var now = 1_000L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving)
        now += 1_000L
        assertNotNull(owner.completeFromExplicitExit())
        assertNull(owner.completeFromExplicitExit())
        assertFalse(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
    }

    @Test fun subsequentNewRealEntryIsNewSessionWithFreshCounter() {
        var now = 0L
        val owner = DrivingSessionTelemetryOwner { now }
        owner.beginFromAcceptedEntry()
        owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving)
        now = 500L
        assertEquals(1, owner.completeFromExplicitExit()!!.commandsCount)
        owner.beginFromAcceptedEntry()
        now = 10_000L
        assertEquals(0, owner.completeFromExplicitExit()!!.commandsCount)
    }

    @Test fun permissionOrSystemInvalidationNeverFinishesCleanly() {
        val owner = DrivingSessionTelemetryOwner { 100L }
        owner.beginFromAcceptedEntry()
        owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving)
        owner.abandonWithoutCompletion()
        assertEquals(ExitEvidence.ABANDONED_UNKNOWN, owner.mostRecentEvidence())
        assertNull(owner.completeFromExplicitExit())
        owner.abandonWithoutCompletion()
        assertFalse(owner.isTracking())
    }

    @Test fun invalidAndReversedMonotonicClockAreUnknown() {
        var now = -1L
        val owner = DrivingSessionTelemetryOwner { now }
        assertFalse(owner.beginFromAcceptedEntry())
        now = 10_000L
        assertTrue(owner.beginFromAcceptedEntry())
        now = 9_999L
        assertNull(owner.completeFromExplicitExit())
        assertEquals(ExitEvidence.INVALID_MEASUREMENT, owner.mostRecentEvidence())
    }

    @Test fun clockReadExceptionDoesNotInventCompletion() {
        var bad = false
        val owner = DrivingSessionTelemetryOwner { if (bad) error("clock") else 1L }
        owner.beginFromAcceptedEntry()
        bad = true
        assertNull(owner.completeFromExplicitExit())
        assertEquals(ExitEvidence.INVALID_MEASUREMENT, owner.mostRecentEvidence())
    }

    @Test fun countAt1000IsAllowedBut1001IsUnknownNotClamped() {
        val owner = DrivingSessionTelemetryOwner { 0L }
        owner.beginFromAcceptedEntry()
        repeat(1000) { assertTrue(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving)) }
        assertFalse(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
        assertNull(owner.completeFromExplicitExit())
        assertEquals(ExitEvidence.INVALID_MEASUREMENT, owner.mostRecentEvidence())
    }

    @Test fun exactly1000ApprovedCommandsPassV1Gate() {
        val owner = DrivingSessionTelemetryOwner { 0L }
        owner.beginFromAcceptedEntry()
        repeat(1000) { owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving) }
        val event = owner.completeFromExplicitExit()!!
        assertEquals(1000, event.commandsCount)
        assertTrue(AllowlistedTelemetryPrivacyGate().sanitize(event) is AppResult.Success<*>)
    }

    @Test fun privacyClearErasesEphemeralEvidenceWithoutReportingSuccess() {
        val owner = DrivingSessionTelemetryOwner { 1L }
        owner.beginFromAcceptedEntry()
        owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving)
        owner.forgetForPrivacy()
        assertFalse(owner.isTracking())
        assertEquals(ExitEvidence.NONE, owner.mostRecentEvidence())
        assertNull(owner.completeFromExplicitExit())
    }

    @Test fun zeroDurationIsValidAndDoesNotProveVoiceSuccessOrCrashFree() {
        val owner = DrivingSessionTelemetryOwner { 42L }
        owner.beginFromAcceptedEntry()
        assertEquals(TelemetryEvent.SessionCompleted("under_1m", 0),
            owner.completeFromExplicitExit())
    }

    @Test fun noEntryCannotBeCompletedFromImplicitCallback() {
        val owner = DrivingSessionTelemetryOwner { 10L }
        assertNull(owner.completeFromExplicitExit())
        assertFalse(owner.noteAcknowledgedVoiceCommand(VoiceOrigin.Driving))
        assertEquals(ExitEvidence.NONE, owner.mostRecentEvidence())
    }
}
