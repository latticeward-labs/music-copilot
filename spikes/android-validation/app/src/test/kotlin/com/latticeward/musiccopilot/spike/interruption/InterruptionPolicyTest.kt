package com.latticeward.musiccopilot.spike.interruption

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InterruptionPolicyTest {
    @Test
    fun transientLossResumesOnlyAfterAllGuardsPass() {
        val loss = InterruptionPolicy.reduce(InterruptionState(isPlaying = true), InterruptionEvent.TRANSIENT_LOSS)
        assertEquals(listOf(InterruptionEffect.PAUSE, InterruptionEffect.PUBLISH_STATE), loss.effects)
        assertTrue(loss.state.resumeEligible)

        val gain = InterruptionPolicy.reduce(loss.state, InterruptionEvent.FOCUS_GAIN)
        assertEquals(listOf(InterruptionEffect.RESUME, InterruptionEffect.PUBLISH_STATE), gain.effects)
        assertTrue(gain.state.isPlaying)
    }

    @Test
    fun alreadyPausedNeverAutoResumes() {
        val loss = InterruptionPolicy.reduce(InterruptionState(isPlaying = false), InterruptionEvent.TRANSIENT_LOSS)
        val gain = InterruptionPolicy.reduce(loss.state, InterruptionEvent.FOCUS_GAIN)
        assertFalse(gain.state.isPlaying)
        assertFalse(gain.effects.contains(InterruptionEffect.RESUME))
    }

    @Test
    fun manualPauseDuringInterruptionClearsResumeEligibility() {
        val loss = InterruptionPolicy.reduce(InterruptionState(isPlaying = true), InterruptionEvent.TRANSIENT_LOSS)
        val manual = InterruptionPolicy.reduce(loss.state, InterruptionEvent.MANUAL_PAUSE)
        val gain = InterruptionPolicy.reduce(manual.state, InterruptionEvent.FOCUS_GAIN)
        assertFalse(gain.state.isPlaying)
        assertTrue(gain.effects.isEmpty())
    }

    @Test
    fun permanentLossNeverResumes() {
        val loss = InterruptionPolicy.reduce(InterruptionState(isPlaying = true), InterruptionEvent.PERMANENT_LOSS)
        val gain = InterruptionPolicy.reduce(loss.state, InterruptionEvent.FOCUS_GAIN)
        assertFalse(gain.state.isPlaying)
        assertFalse(gain.effects.contains(InterruptionEffect.RESUME))
    }

    @Test
    fun callCancelsVoiceBeforeAnyRecovery() {
        val call = InterruptionPolicy.reduce(
            InterruptionState(isPlaying = true, isDucked = true, voiceActive = true),
            InterruptionEvent.CALL_STARTED,
        )
        assertEquals(InterruptionEffect.CANCEL_VOICE, call.effects.first())
        assertTrue(call.effects.contains(InterruptionEffect.PAUSE))
        assertFalse(call.state.voiceActive)
        assertFalse(call.state.isDucked)
    }

    @Test
    fun routeDisconnectPausesImmediatelyAndReconnectNeverAutoResumes() {
        val disconnected = InterruptionPolicy.reduce(
            InterruptionState(isPlaying = true),
            InterruptionEvent.ROUTE_DISCONNECTED,
        )
        assertTrue(disconnected.effects.contains(InterruptionEffect.PAUSE))
        assertFalse(disconnected.state.routeAvailable)
        assertFalse(disconnected.state.resumeEligible)

        val reconnected = InterruptionPolicy.reduce(disconnected.state, InterruptionEvent.ROUTE_RECONNECTED)
        assertTrue(reconnected.state.routeAvailable)
        assertFalse(reconnected.state.isPlaying)
        assertFalse(reconnected.effects.contains(InterruptionEffect.RESUME))
    }

    @Test
    fun routeChangeInvalidatesPendingFocusResume() {
        val loss = InterruptionPolicy.reduce(InterruptionState(isPlaying = true), InterruptionEvent.TRANSIENT_LOSS)
        val disconnected = InterruptionPolicy.reduce(loss.state, InterruptionEvent.ROUTE_DISCONNECTED)
        val reconnected = InterruptionPolicy.reduce(disconnected.state, InterruptionEvent.ROUTE_RECONNECTED)
        val gain = InterruptionPolicy.reduce(reconnected.state, InterruptionEvent.FOCUS_GAIN)
        assertFalse(gain.state.isPlaying)
        assertFalse(gain.effects.contains(InterruptionEffect.RESUME))
    }

    @Test
    fun duplicateDisconnectIsIdempotent() {
        val first = InterruptionPolicy.reduce(InterruptionState(isPlaying = true), InterruptionEvent.ROUTE_DISCONNECTED)
        val second = InterruptionPolicy.reduce(first.state, InterruptionEvent.ROUTE_DISCONNECTED)
        assertTrue(second.effects.isEmpty())
        assertEquals(first.state, second.state)
    }

    @Test
    fun corruptItemSkipsOnlyWhenValidatedNextExists() {
        val withNext = InterruptionPolicy.reduce(
            InterruptionState(isPlaying = true),
            InterruptionEvent.CORRUPT_ITEM_WITH_NEXT,
        )
        assertEquals(
            listOf(InterruptionEffect.EMIT_COARSE_ERROR, InterruptionEffect.SKIP_TO_NEXT),
            withNext.effects,
        )

        val withoutNext = InterruptionPolicy.reduce(
            InterruptionState(isPlaying = true),
            InterruptionEvent.CORRUPT_ITEM_WITHOUT_NEXT,
        )
        assertTrue(withoutNext.effects.contains(InterruptionEffect.END_QUEUE))
        assertFalse(withoutNext.state.isPlaying)
    }
}
