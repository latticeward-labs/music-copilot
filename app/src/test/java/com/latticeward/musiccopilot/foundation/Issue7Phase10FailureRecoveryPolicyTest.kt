package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.playback.service.PlaybackFailureRecoveryPolicy
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #7 P10: bounded recovery policy specifications.
 * Authored, NOT_RUN; no GitHub Actions and no interim APK.
 */
class Issue7Phase10FailureRecoveryPolicyTest {
    private val policy = PlaybackFailureRecoveryPolicy()

    @Test fun corruptAllowsForwardRecovery() {
        assertEquals(
            PlaybackFailureRecoveryPolicy.Disposition.SkipForward,
            policy.disposition(PlaybackFailureRecoveryPolicy.Failure.Corrupt),
        )
    }

    @Test fun missingAllowsForwardRecovery() {
        assertEquals(
            PlaybackFailureRecoveryPolicy.Disposition.SkipForward,
            policy.disposition(PlaybackFailureRecoveryPolicy.Failure.Missing),
        )
    }

    @Test fun unsupportedDecoderAllowsForwardRecovery() {
        assertEquals(
            PlaybackFailureRecoveryPolicy.Disposition.SkipForward,
            policy.disposition(PlaybackFailureRecoveryPolicy.Failure.Unsupported),
        )
    }

    @Test fun revokedPermissionNeverAutoSkips() {
        assertEquals(
            PlaybackFailureRecoveryPolicy.Disposition.StopAndRequestPermission,
            policy.disposition(PlaybackFailureRecoveryPolicy.Failure.PermissionRevoked),
        )
    }

    @Test fun unknownDecoderFaultSafePauses() {
        assertEquals(
            PlaybackFailureRecoveryPolicy.Disposition.SafePause,
            policy.disposition(PlaybackFailureRecoveryPolicy.Failure.Unknown),
        )
    }

    @Test fun defaultBudgetIsThreeAttemptsNotInfinite() {
        assertEquals(3, policy.maxAttempts)
        assertTrue(policy.permitsAttempt(0))
        assertTrue(policy.permitsAttempt(1))
        assertTrue(policy.permitsAttempt(2))
        assertFalse(policy.permitsAttempt(3))
        assertFalse(policy.permitsAttempt(100_000))
    }

    @Test fun firstForwardCandidateIsNextOccurrence() {
        assertEquals(2, policy.nextIndex(1, 5, 0))
    }

    @Test fun repeatedOpaqueRefsStillHaveDifferentOccurrenceIndex() {
        assertEquals(1, policy.nextIndex(0, 3, 0))
        assertEquals(2, policy.nextIndex(1, 3, 1))
    }

    @Test fun lastOccurrenceCannotWrapAround() {
        assertNull(policy.nextIndex(3, 4, 0))
        assertNull(policy.nextIndex(0, 1, 0))
    }

    @Test fun emptyOrOutOfRangeQueueCannotAdvance() {
        assertNull(policy.nextIndex(0, 0, 0))
        assertNull(policy.nextIndex(-1, 3, 0))
        assertNull(policy.nextIndex(3, 3, 0))
    }

    @Test fun exhaustedBudgetCannotKeepSkippingForever() {
        assertNull(policy.nextIndex(0, 100, 3))
        assertNull(policy.nextIndex(0, 100, 4))
    }

    @Test fun negativeAttemptCounterFailsClosed() {
        assertFalse(policy.permitsAttempt(-1))
        assertNull(policy.nextIndex(0, 10, -1))
    }

    @Test fun invalidBudgetCannotBeConstructed() {
        try {
            PlaybackFailureRecoveryPolicy(0)
            fail("Expected invalid zero budget to be rejected")
        } catch (_: IllegalArgumentException) { }
        try {
            PlaybackFailureRecoveryPolicy(9)
            fail("Expected unreasonably large budget to be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun customBoundedBudgetRespectsStrictUpperLimit() {
        val smaller = PlaybackFailureRecoveryPolicy(1)
        assertTrue(smaller.permitsAttempt(0))
        assertFalse(smaller.permitsAttempt(1))
        assertEquals(2, smaller.nextIndex(1, 5, 0))
        assertNull(smaller.nextIndex(1, 5, 1))
    }
}
