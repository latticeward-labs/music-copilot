package com.latticeward.musiccopilot.spike.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class ConfidencePolicyTest {
    @Test
    fun validRemoteWins() {
        val result = ConfidencePolicy.resolve(remoteValue = 0.92, lastValidValue = 0.91)
        assertEquals(0.92, result.policy.directThreshold, 0.0)
        assertEquals(ConfidencePolicy.Source.REMOTE_VALID, result.source)
    }

    @Test
    fun malformedRemoteUsesLastValid() {
        val result = ConfidencePolicy.resolve(remoteValue = Double.NaN, lastValidValue = 0.91)
        assertEquals(0.91, result.policy.directThreshold, 0.0)
        assertEquals(ConfidencePolicy.Source.LAST_VALID, result.source)
    }

    @Test
    fun outOfRangeRemoteAndLastValueUseBuildDefault() {
        val result = ConfidencePolicy.resolve(remoteValue = 0.40, lastValidValue = 1.2)
        assertEquals(ConfidencePolicy.BUILD_DEFAULT_DIRECT_THRESHOLD, result.policy.directThreshold, 0.0)
        assertEquals(ConfidencePolicy.Source.BUILD_DEFAULT, result.source)
    }

    @Test
    fun remoteFailureIsNonBlocking() {
        val result = ConfidencePolicy.resolve(remoteValue = null, lastValidValue = null)
        assertEquals(ConfidencePolicy.Source.BUILD_DEFAULT, result.source)
    }
}
