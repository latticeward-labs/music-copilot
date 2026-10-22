package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.playback.service.PlaybackTaskLifetimePolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * P08 task-removal contract. Authored for the P13 technical readiness gate;
 * DO NOT run in GitHub Actions or any interim issue phase.
 */
class Issue7Phase8TaskRemovalPolicyTest {
    private fun retain(
        foreground: Boolean = true,
        queue: Boolean = true,
        ready: Boolean = true,
        playing: Boolean = true,
        buffering: Boolean = false,
        ended: Boolean = false,
        error: Boolean = false,
    ) = PlaybackTaskLifetimePolicy.retainActivePlayback(
        foregroundOngoing = foreground,
        hasQueue = queue,
        playWhenReady = ready,
        isPlaying = playing,
        isBuffering = buffering,
        isEnded = ended,
        hasPlayerError = error,
    )

    @Test fun audibleForegroundPlaybackSurvivesTaskRemoval() {
        assertTrue(retain())
    }

    @Test fun requestedForegroundBufferingSurvivesTaskRemoval() {
        assertTrue(retain(playing = false, buffering = true))
    }

    @Test fun activityBoundButNotForegroundCannotKeepService() {
        assertFalse(retain(foreground = false))
    }

    @Test fun emptyQueueCannotKeepService() {
        assertFalse(retain(queue = false))
    }

    @Test fun pausedUserSessionCannotKeepForegroundIndefinitely() {
        assertFalse(retain(ready = false, playing = false))
    }

    @Test fun stopCommandCannotKeepServiceAlive() {
        assertFalse(retain(ready = false))
    }

    @Test fun idlePlayerCannotKeepServiceAlive() {
        assertFalse(retain(playing = false, buffering = false))
    }

    @Test fun naturalEndCannotKeepServiceAlive() {
        assertFalse(retain(ended = true, playing = false))
    }

    @Test fun decoderErrorCannotKeepServiceAlive() {
        assertFalse(retain(error = true))
    }

    @Test fun oldForegroundTimeoutWithNoUserPlayCannotKeepServiceAlive() {
        assertFalse(retain(playing = false, buffering = false))
    }

    @Test fun bufferingWithoutPlayRequestCannotKeepServiceAlive() {
        assertFalse(retain(ready = false, playing = false, buffering = true))
    }

    @Test fun taskRemovalDoesNotInferPlaybackFromQueuePresence() {
        assertFalse(retain(ready = false, playing = false, queue = true))
    }
}
