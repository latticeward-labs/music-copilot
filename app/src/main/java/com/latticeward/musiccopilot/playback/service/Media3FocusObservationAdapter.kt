package com.latticeward.musiccopilot.playback.service

import android.os.SystemClock
import androidx.media3.common.Player
import com.latticeward.musiccopilot.playback.interruption.FocusCondition
import com.latticeward.musiccopilot.playback.interruption.InterruptionClock
import com.latticeward.musiccopilot.playback.interruption.InterruptionEffect
import com.latticeward.musiccopilot.playback.interruption.InterruptionEnvelope
import com.latticeward.musiccopilot.playback.interruption.InterruptionEvent
import com.latticeward.musiccopilot.playback.interruption.InterruptionPlayback
import com.latticeward.musiccopilot.playback.interruption.InterruptionReducer
import com.latticeward.musiccopilot.playback.interruption.InterruptionSequenceSource
import com.latticeward.musiccopilot.playback.interruption.InterruptionState
import com.latticeward.musiccopilot.playback.interruption.InterruptionTransition
import com.latticeward.musiccopilot.playback.interruption.PlayRequestOrigin

/**
 * Issue #8 P03: actual Media3 Player -> pure P02 reducer observation bridge.
 *
 * The existing service's single ExoPlayer still owns Audio Focus using
 * setAudioAttributes(..., true). This observer must never request focus,
 * implement telephony detection, duck Player volume or create a Player.
 *
 * Called solely from that Player's application looper (service Main).
 * The full focus/call recovery effect policy is P04; this adapter applies
 * only a safety PAUSE for unrequested audible playback. A transient focus
 * suppression is already performed by Media3: do NOT call Player.pause()
 * solely for its modelled transient PAUSE, since that could defeat Media3's
 * built-in return-from-suppression. No extra auto-play on focus gain.
 */
internal class Media3FocusObservationAdapter(
    private val player: Player,
    private val shouldBlockUnrequestedPlayback: () -> Boolean,
    private val pauseUnsafePlayback: () -> Unit,
    private val clock: InterruptionClock = InterruptionClock {
        SystemClock.elapsedRealtime()
    },
    sequenceSource: InterruptionSequenceSource? = null,
) {
    private var nextSequence = 0L
    private val sequence = sequenceSource ?: InterruptionSequenceSource {
        check(nextSequence != Long.MAX_VALUE) { "Focus event sequence exhausted" }
        ++nextSequence
    }
    private var state = InterruptionState(
        playback = if (player.isPlaying) {
            InterruptionPlayback.PLAYING
        } else {
            InterruptionPlayback.PAUSED
        },
    )
    private var lastAudiblePlaying = player.isPlaying
    private var lastSuppression = player.playbackSuppressionReason
    private var observedTransientEpisode: Long? = null
    private var verifiedPlayPending = false
    private var closed = false

    /** Policy state is not a second PlaybackSnapshot/Player authority. */
    val policyState: InterruptionState get() = state

    private fun accept(event: InterruptionEvent): InterruptionTransition {
        if (closed) return InterruptionTransition(state)
        val transition = InterruptionReducer.reduce(
            state,
            InterruptionEnvelope(
                sequence = sequence.nextSequence(),
                observedAtElapsedMs = clock.elapsedRealtimeMs().coerceAtLeast(0L),
                event = event,
            ),
        )
        state = transition.state
        return transition
    }

    /**
     * Media3 suppression reason is an actual observable *transient focus*
     * indication. It is NOT necessarily a call; a changing Player state may
     * be delivered before this callback, so uncertain pre-loss Playing fails
     * closed instead of fabricating automatic-resume eligibility.
     */
    fun onSuppressionChanged(reason: Int) {
        if (closed || reason == lastSuppression) return
        val before = lastSuppression
        lastSuppression = reason
        val transient = Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
        if (reason == transient) {
            if (state.focus != FocusCondition.TRANSIENT_LOST &&
                state.focus != FocusCondition.PERMANENT_LOST
            ) {
                accept(
                    InterruptionEvent.FocusLostTransient(
                        wasPlayingBeforeLoss = lastAudiblePlaying && player.playWhenReady,
                    )
                )
                observedTransientEpisode =
                    state.focusEpisode.takeIf { state.focus == FocusCondition.TRANSIENT_LOST }
            }
        } else if (before == transient) {
            val episode = observedTransientEpisode
            observedTransientEpisode = null
            if (reason == Player.PLAYBACK_SUPPRESSION_REASON_NONE && episode != null) {
                // Only a known matching transition can attempt guarded resume.
                // Media3 un-suppresses itself; no additional player.play().
                accept(InterruptionEvent.FocusRestored(episode))
            } else {
                // An unsuitable/unknown output is NOT evidence of focus regain.
                accept(InterruptionEvent.FocusLostPermanent)
            }
        }
    }

    /**
     * Explicit Media3 AUDIO_FOCUS_LOSS pause is observable, but does not
     * distinguish a call or classify every transient/permanent focus loss.
     * Conservative terminal classification; P04 owns final call recovery.
     */
    fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (closed || playWhenReady) return
        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
            observedTransientEpisode = null
            verifiedPlayPending = false
            accept(InterruptionEvent.FocusLostPermanent)
        }
        // AUDIO_BECOMING_NOISY remains owned by service route safety gate.
        // No duplicate noisy/route generation or phantom focus event here.
    }

    /**
     * Called only after an existing validated explicit user Play/selection has
     * been committed by the service. Never invent focus gain at command time;
     * Media3 itself must obtain focus before isPlaying can become true.
     */
    fun onVerifiedUserPlayAccepted() {
        if (closed) return
        verifiedPlayPending = true
        if (state.focus == FocusCondition.AVAILABLE) {
            accept(
                InterruptionEvent.PlayRequested(
                    origin = PlayRequestOrigin.VERIFIED_USER,
                    expectedRouteGeneration = state.routeGeneration,
                )
            )
        }
    }

    fun onManualPauseOrStop(stop: Boolean) {
        if (closed) return
        verifiedPlayPending = false
        observedTransientEpisode = null
        accept(if (stop) InterruptionEvent.UserStop else InterruptionEvent.UserPause)
    }

    /**
     * Media3's real isPlaying is the only observable playback truth.
     * A newly audible user-initiated retry after a focus loss is the only
     * evidence used to clear the policy's terminal focus condition. This is
     * not labelled an OS gain callback and does not cause another Play.
     */
    fun onIsPlayingChanged(isPlaying: Boolean) {
        if (closed) return
        if (isPlaying && verifiedPlayPending &&
            state.focus == FocusCondition.PERMANENT_LOST
        ) {
            accept(InterruptionEvent.FocusRestored(state.focusEpisode))
            accept(
                InterruptionEvent.PlayRequested(
                    origin = PlayRequestOrigin.VERIFIED_USER,
                    expectedRouteGeneration = state.routeGeneration,
                )
            )
        }
        val transition = accept(InterruptionEvent.PlaybackObserved(isPlaying))
        lastAudiblePlaying = isPlaying
        if (isPlaying) verifiedPlayPending = false
        if (isPlaying &&
            (shouldBlockUnrequestedPlayback() ||
                transition.effects.contains(InterruptionEffect.PAUSE_SERVICE_PLAYER))
        ) {
            // P02's suggested PAUSE on focus suppression must not be invoked
            // here; this path only runs for actually audible unsafe playback.
            pauseUnsafePlayback()
        }
    }

    fun close() {
        closed = true
        verifiedPlayPending = false
        observedTransientEpisode = null
    }
}
