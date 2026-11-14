package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.voice.intent.ArtistConfidencePolicy
import com.latticeward.musiccopilot.voice.intent.ArtistProviderConfidence
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P05: synthetic confidence evidence. TEST SOURCE ONLY.
 * Authoring is allowed; compilation/execution/build is forbidden until P13.
 * These tests do not call a live STT provider or Android device.
 */
class Issue11Phase5ArtistConfidencePolicyTest {

    @Test fun policyHasVersionAndMatchesApprovedSafeDefaults() {
        assertEquals("issue11-artist-provider-confidence-v1",
            ArtistConfidencePolicy.POLICY_VERSION)
        assertEquals(0.90, VoicePolicy.T_DIRECT, 0.0)
        assertEquals(0.80, VoicePolicy.T_DIRECT_MIN_ALLOWED, 0.0)
        assertEquals(0.98, VoicePolicy.T_DIRECT_MAX_ALLOWED, 0.0)
    }

    @Test fun missingMoonshineConfidenceIsMissingNotCalibrated() {
        val actual = ArtistConfidencePolicy.classify(null)
        assertSame(ArtistProviderConfidence.Missing, actual)
        assertFalse(ArtistConfidencePolicy.permitsDirect(actual, 0.90))
    }

    @Test fun nanoInvalidAndInfiniteScoresAreNeverCalibrated() {
        for (value in listOf(Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -0.01, 1.01)) {
            assertSame(ArtistProviderConfidence.Invalid,
                ArtistConfidencePolicy.classify(value))
        }
    }

    @Test fun finiteZeroAndOneAreLegitimateProviderEvidenceButNotAllDirect() {
        assertEquals(0.0,
            (ArtistConfidencePolicy.classify(0.0)
                as ArtistProviderConfidence.Calibrated).probability, 0.0)
        assertEquals(1.0,
            (ArtistConfidencePolicy.classify(1.0)
                as ArtistProviderConfidence.Calibrated).probability, 0.0)
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.0), 0.90))
        assertTrue(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(1.0), 0.90))
    }

    @Test fun directProviderThresholdIsInclusive() {
        assertTrue(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.90), 0.90))
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.899999), 0.90))
    }

    @Test fun validThresholdBoundsAreAcceptedExactly() {
        assertEquals(0.80, ArtistConfidencePolicy.validatedDirectThreshold(0.80), 0.0)
        assertEquals(0.98, ArtistConfidencePolicy.validatedDirectThreshold(0.98), 0.0)
        assertEquals(0.94, ArtistConfidencePolicy.validatedDirectThreshold(0.94), 0.0)
    }

    @Test fun malformedThresholdsFallBackToApprovedDefault() {
        for (value in listOf(Double.NaN, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, -1.0, 0.799999, 0.980001, 2.0)) {
            assertEquals(0.90,
                ArtistConfidencePolicy.validatedDirectThreshold(value), 0.0)
        }
    }

    @Test fun lowerAllowedThresholdDoesNotBypassProviderValidity() {
        assertTrue(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.80), 0.80))
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.79), 0.80))
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistProviderConfidence.Missing, 0.80))
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistProviderConfidence.Invalid, 0.80))
    }

    @Test fun malformedThresholdDoesNotSilentlyBecomeLowOrZero() {
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.80), Double.NaN))
        assertTrue(ArtistConfidencePolicy.permitsDirect(
            ArtistConfidencePolicy.classify(0.90), Double.NaN))
    }

    @Test fun similarityAndVadAreNotConfidenceInputs() {
        // This API takes ONLY nullable provider probability. There is no
        // ArtistMatchEvidence or localVadEndpointVerified argument or override.
        assertSame(ArtistProviderConfidence.Missing,
            ArtistConfidencePolicy.classify(null))
        assertFalse(ArtistConfidencePolicy.permitsDirect(
            ArtistProviderConfidence.Missing, 0.80))
    }
}
