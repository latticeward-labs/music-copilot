package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.playback.interruption.FocusCondition
import com.latticeward.musiccopilot.playback.interruption.InterruptionEffect
import com.latticeward.musiccopilot.playback.interruption.InterruptionEnvelope
import com.latticeward.musiccopilot.playback.interruption.InterruptionEvent
import com.latticeward.musiccopilot.playback.interruption.InterruptionPlayback
import com.latticeward.musiccopilot.playback.interruption.InterruptionReducer
import com.latticeward.musiccopilot.playback.interruption.InterruptionState
import com.latticeward.musiccopilot.playback.interruption.InterruptionTransition
import com.latticeward.musiccopilot.playback.interruption.PlayRequestOrigin
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #8 P12 authored JVM regression specifications. NEVER run in P12,
 * GitHub Actions, or the P13 build-only job. These cannot prove phone audio,
 * actual call identity, Media3 focus ordering, or Bluetooth output routing.
 */
class Issue8Phase12InterruptionReducerTest {
    private fun step(
        state: InterruptionState,
        event: InterruptionEvent,
    ): InterruptionTransition = InterruptionReducer.reduce(
        state,
        InterruptionEnvelope(
            sequence = state.lastEventSequence + 1L,
            observedAtElapsedMs = state.lastObservedAtElapsedMs + 1L,
            event = event,
        )
    )

    @Test fun transientLossFromAudiblePlaybackCapturesFourGuardCandidate() {
        val lost = step(
            InterruptionState(playback = InterruptionPlayback.PLAYING),
            InterruptionEvent.FocusLostTransient(wasPlayingBeforeLoss = true),
        )
        assertEquals(FocusCondition.TRANSIENT_LOST, lost.state.focus)
        assertEquals(InterruptionPlayback.PAUSED, lost.state.playback)
        assertNotNull(lost.state.pendingResume)
        assertTrue(lost.effects.contains(InterruptionEffect.CANCEL_VOICE_FOR_FOCUS))
        val gained = step(lost.state,
            InterruptionEvent.FocusRestored(lost.state.focusEpisode))
        assertEquals(FocusCondition.AVAILABLE, gained.state.focus)
        assertTrue(gained.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
        assertNull(gained.state.pendingResume)
    }

    @Test fun lossBeforePlaybackWasAudibleNeverEnablesAutoResume() {
        val lost = step(
            InterruptionState(playback = InterruptionPlayback.PAUSED),
            InterruptionEvent.FocusLostTransient(wasPlayingBeforeLoss = false),
        )
        assertNull(lost.state.pendingResume)
        val gain = step(lost.state,
            InterruptionEvent.FocusRestored(lost.state.focusEpisode))
        assertFalse(gain.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
    }

    @Test fun verifiedManualPauseDefeatsDelayedFocusGain() {
        val lost = step(
            InterruptionState(playback = InterruptionPlayback.PLAYING),
            InterruptionEvent.FocusLostTransient(true),
        )
        val manual = step(lost.state, InterruptionEvent.UserPause)
        assertTrue(manual.state.requiresExplicitPlay)
        assertEquals(1L, manual.state.manualRevision)
        assertNull(manual.state.pendingResume)
        val gain = step(manual.state,
            InterruptionEvent.FocusRestored(lost.state.focusEpisode))
        assertFalse(gain.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
    }

    @Test fun routeRemovalDefeatsFocusGainEvenAfterRouteValidated() {
        val lost = step(
            InterruptionState(playback = InterruptionPlayback.PLAYING),
            InterruptionEvent.FocusLostTransient(true),
        )
        val removed = step(lost.state, InterruptionEvent.RouteDisconnected)
        assertEquals(1L, removed.state.routeGeneration)
        assertTrue(removed.effects.contains(InterruptionEffect.CANCEL_VOICE_FOR_ROUTE))
        val reconnected = step(removed.state, InterruptionEvent.RouteValidated)
        assertTrue(reconnected.state.routeValid)
        assertTrue(reconnected.state.requiresExplicitPlay)
        val gained = step(reconnected.state,
            InterruptionEvent.FocusRestored(lost.state.focusEpisode))
        assertFalse(gained.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
    }

    @Test fun noisyAndAudioDeviceRemovalDeduplicateOneEpisode() {
        val once = step(InterruptionState(), InterruptionEvent.RouteDisconnected)
        val twice = step(once.state, InterruptionEvent.RouteDisconnected)
        assertEquals(once.state.routeGeneration, twice.state.routeGeneration)
        assertTrue(twice.effects.isEmpty())
        assertEquals(1L, twice.state.routeGeneration)
    }

    @Test fun permanentlyLostFocusNeverRestoresAutomaticPlayback() {
        val lost = step(
            InterruptionState(playback = InterruptionPlayback.PLAYING),
            InterruptionEvent.FocusLostPermanent,
        )
        assertEquals(FocusCondition.PERMANENT_LOST, lost.state.focus)
        assertTrue(lost.state.requiresExplicitPlay)
        assertNull(lost.state.pendingResume)
        val oldGain = step(lost.state,
            InterruptionEvent.FocusRestored(lost.state.focusEpisode))
        assertFalse(oldGain.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
    }

    @Test fun staleFocusEpisodeCannotGrantNewerFocusLoss() {
        val first = step(InterruptionState(),
            InterruptionEvent.FocusLostTransient(true))
        val later = step(first.state, InterruptionEvent.FocusLostPermanent)
        val oldGain = step(later.state,
            InterruptionEvent.FocusRestored(first.state.focusEpisode))
        assertEquals(FocusCondition.PERMANENT_LOST, oldGain.state.focus)
        assertFalse(oldGain.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
    }

    @Test fun untrustedSystemPlayNeverClearsReconnectLatch() {
        val removed = step(InterruptionState(), InterruptionEvent.RouteDisconnected)
        val available = step(removed.state, InterruptionEvent.RouteValidated)
        val rejected = step(available.state, InterruptionEvent.PlayRequested(
            origin = PlayRequestOrigin.AMBIGUOUS_EXTERNAL,
            expectedRouteGeneration = available.state.routeGeneration,
        ))
        assertTrue(rejected.effects.contains(InterruptionEffect.REJECT_UNSAFE_PLAY))
        assertTrue(rejected.state.requiresExplicitPlay)
    }

    @Test fun trustedPlayRequiresExactRouteEpochAndAvailableFocus() {
        val current = InterruptionState(routeGeneration = 3L, requiresExplicitPlay = true)
        val stale = step(current, InterruptionEvent.PlayRequested(
            PlayRequestOrigin.VERIFIED_USER, expectedRouteGeneration = 2L))
        assertTrue(stale.effects.contains(InterruptionEffect.REJECT_UNSAFE_PLAY))
        val valid = step(current, InterruptionEvent.PlayRequested(
            PlayRequestOrigin.VERIFIED_USER, expectedRouteGeneration = 3L))
        assertTrue(valid.effects.contains(InterruptionEffect.ALLOW_VERIFIED_USER_PLAY))
        assertFalse(valid.state.requiresExplicitPlay)
        assertEquals(InterruptionPlayback.PAUSED, valid.state.playback)
    }

    @Test fun observedUnsafeAudiblePlayerAlwaysRequestsServicePause() {
        val state = InterruptionState(routeValid = false, requiresExplicitPlay = true)
        val observed = step(state, InterruptionEvent.PlaybackObserved(isPlaying = true))
        assertTrue(observed.effects.contains(InterruptionEffect.PAUSE_SERVICE_PLAYER))
        assertEquals(InterruptionPlayback.PAUSED, observed.state.playback)
    }

    @Test fun deniedAutoResumeCancelsVoiceAndRequiresExplicitPlay() {
        val denied = step(InterruptionState(), InterruptionEvent.FocusAutoResumeRejected)
        assertTrue(denied.effects.contains(InterruptionEffect.CANCEL_VOICE_FOR_FOCUS))
        assertTrue(denied.effects.contains(InterruptionEffect.PAUSE_SERVICE_PLAYER))
        assertTrue(denied.state.requiresExplicitPlay)
    }

    @Test fun duckRequestsNoSecondPlayOrVolumeChange() {
        val duck = step(InterruptionState(), InterruptionEvent.FocusDuck)
        assertEquals(FocusCondition.DUCKED, duck.state.focus)
        assertNull(duck.state.pendingResume)
        assertFalse(duck.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER))
        assertTrue(duck.effects.contains(InterruptionEffect.CANCEL_VOICE_FOR_FOCUS))
    }

    @Test fun rejectedDuplicateAndOutOfOrderEnvelopesHaveNoEffects() {
        val one = step(InterruptionState(), InterruptionEvent.RouteDisconnected)
        val repeated = InterruptionReducer.reduce(one.state, InterruptionEnvelope(
            sequence = one.state.lastEventSequence,
            observedAtElapsedMs = 500L,
            event = InterruptionEvent.FocusLostPermanent,
        ))
        assertFalse(repeated.applied)
        assertEquals(one.state, repeated.state)
        assertTrue(repeated.effects.isEmpty())
    }

    @Test fun negativeClockIsRejectedWithoutChangingState() {
        val initial = InterruptionState()
        val bad = InterruptionReducer.reduce(initial, InterruptionEnvelope(
            sequence = 1L, observedAtElapsedMs = -1L,
            event = InterruptionEvent.FocusLostPermanent,
        ))
        assertFalse(bad.applied)
        assertEquals(initial, bad.state)
    }
}
