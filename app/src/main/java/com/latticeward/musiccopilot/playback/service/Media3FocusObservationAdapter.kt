package com.latticeward.musiccopilot.playback.service

import android.os.SystemClock
import androidx.media3.common.Player
import com.latticeward.musiccopilot.playback.api.PlaybackVoiceInterruptionReason
import com.latticeward.musiccopilot.playback.interruption.FocusCondition
import com.latticeward.musiccopilot.playback.interruption.InterruptionClock
import com.latticeward.musiccopilot.playback.interruption.InterruptionEffect
import com.latticeward.musiccopilot.playback.interruption.InterruptionEnvelope
import com.latticeward.musiccopilot.playback.interruption.InterruptionEvent
import com.latticeward.musiccopilot.playback.interruption.InterruptionPlayback
import com.latticeward.musiccopilot.playback.interruption.InterruptionReducer
import com.latticeward.musiccopilot.playback.interruption.InterruptionNotice
import com.latticeward.musiccopilot.playback.interruption.InterruptionNoticePolicy
import com.latticeward.musiccopilot.playback.interruption.InterruptionSequenceSource
import com.latticeward.musiccopilot.playback.interruption.InterruptionState
import com.latticeward.musiccopilot.playback.interruption.InterruptionTransition
import com.latticeward.musiccopilot.playback.interruption.PlayRequestOrigin

/**
 * Issue #8 P03/P04: Media3 focus observation plus guarded post-focus recovery.
 *
 * The existing service's single ExoPlayer still owns Audio Focus using
 * setAudioAttributes(..., true). This observer must never request focus,
 * implement telephony detection, duck Player volume or create a Player.
 *
 * Called solely from that Player's application looper (service Main).
 * P04 checks P02's four-guard transient resume policy before honoring
 * Media3 automatic unsuppression. Only a denied recovery is safety-paused
 * through the existing service gate; a permitted recovery is left to Media3.
 * Do NOT pause just for the initial transient suppression, as it would
 * defeat Media3's built-in focus return. No extra Play or app-side ducking.
 */
internal class Media3FocusObservationAdapter(
    private val player: Player,
    private val shouldBlockUnrequestedPlayback: () -> Boolean,
    private val pauseUnsafePlayback: () -> Unit,
    private val onDeniedFocusAutoResume: () -> Unit,
    private val onVoiceCancellationRequested: (PlaybackVoiceInterruptionReason) -> Unit,
    private val onInterruptionNotice: (InterruptionNotice) -> Unit,
    private val clock: InterruptionClock = InterruptionClock {
        SystemClock.elapsedRealtime()
    },
    sequenceSource: InterruptionSequenceSource? = null,
) {
    private var nextSequence = 0L
    private val sequence = sequenceSource ?: InterruptionSequenceSource {
        // Saturate on overflow; the P02 reducer rejects repeated sequence
        // numbers without side effects. Never crash the playback service.
        if (nextSequence != Long.MAX_VALUE) ++nextSequence
        nextSequence
    }
    private var state = InterruptionState(
        playback = if (player.isPlaying) {
            InterruptionPlayback.PLAYING
        } else {
            InterruptionPlayback.PAUSED
        },
    )
    private var lastAudiblePlaying = player.isPlaying
    // Player.Listener callbacks can arrive in either order. This remembers a
    // proven pre-loss audible observation only when actual Media3 suppression
    // already identifies TRANSIENT_AUDIO_FOCUS_LOSS.
    private var capturedPreLossPlaying = false
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
        // P10 only reports accepted typed events. A duplicate/old event,
        // caller-owned URI, device ID, exception or call identity cannot
        // enter the diagnostic/error surface.
        if (transition.applied) {
            InterruptionNoticePolicy.fromAccepted(event, transition)?.let { notice ->
                onInterruptionNotice(notice)
            }
        }
        // P09: Consume the P02 reducer's *accepted effect* at Main-looper
        // receipt time, not after a suspended command/recognition coroutine.
        // A duplicate focus/route signal has no effect, so does not cancel
        // a newer unrelated Voice generation for the same episode.
        for (effect in transition.effects) {
            val reason = when (effect) {
                InterruptionEffect.CANCEL_VOICE_FOR_FOCUS ->
                    PlaybackVoiceInterruptionReason.FOCUS_INVALIDATED
                InterruptionEffect.CANCEL_VOICE_FOR_ROUTE ->
                    PlaybackVoiceInterruptionReason.ROUTE_INVALIDATED
                else -> null
            }
            if (reason != null) {
                // Platform provider teardown is best effort, but no speech
                // provider exception may crash the service-owned playback
                // listener or turn a safety pause into uncontrolled playback.
                try {
                    onVoiceCancellationRequested(reason)
                } catch (_: RuntimeException) {
                    // P10 owns privacy-safe diagnostic/error surfacing.
                }
            }
        }
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
            val actuallyPlayedBeforeLoss =
                player.playWhenReady && (lastAudiblePlaying || capturedPreLossPlaying)
            capturedPreLossPlaying = false
            if (state.focus != FocusCondition.TRANSIENT_LOST &&
                state.focus != FocusCondition.PERMANENT_LOST
            ) {
                accept(
                    InterruptionEvent.FocusLostTransient(
                        wasPlayingBeforeLoss = actuallyPlayedBeforeLoss,
                    )
                )
                observedTransientEpisode =
                    state.focusEpisode.takeIf { state.focus == FocusCondition.TRANSIENT_LOST }
            }
        } else if (before == transient) {
            // Suppression ending is a real Media3 observation, not proof that
            // the reason was a telephone call.
            capturedPreLossPlaying = false
            val episode = observedTransientEpisode
                ?: state.focusEpisode.takeIf {
                    state.focus == FocusCondition.TRANSIENT_LOST
                }
            observedTransientEpisode = null
            if (reason == Player.PLAYBACK_SUPPRESSION_REASON_NONE && episode != null) {
                val restored = accept(InterruptionEvent.FocusRestored(episode))
                // P02 owns focus eligibility; the pre-existing Issue #7
                // route/manual gate is an independent stronger veto until
                // P05-P07 unify route epochs and command receipt provenance.
                val eligibleAutomaticReturn =
                    restored.effects.contains(InterruptionEffect.RESUME_SERVICE_PLAYER) &&
                        !shouldBlockUnrequestedPlayback()
                val freshExplicitUserPlay =
                    verifiedPlayPending && !shouldBlockUnrequestedPlayback()
                if (freshExplicitUserPlay && state.focus == FocusCondition.AVAILABLE) {
                    // A user deliberately asked to play while focus was lost.
                    // Do not confuse this with an automatic focus regain.
                    accept(
                        InterruptionEvent.PlayRequested(
                            origin = PlayRequestOrigin.VERIFIED_USER,
                            expectedRouteGeneration = state.routeGeneration,
                        )
                    )
                }
                if (!eligibleAutomaticReturn && !freshExplicitUserPlay) {
                    // Media3 can start audible output on unsuppression by
                    // itself. Explicitly disallow it if the four guards failed,
                    // including pre-loss already Paused or intervening Pause.
                    latchAndPauseDeniedAutoResume()
                }
                // Eligible recovery is performed only by Media3 itself:
                // NEVER call player.play() or double-apply duck/volume here.
            } else if (reason == Player.PLAYBACK_SUPPRESSION_REASON_NONE &&
                state.focus == FocusCondition.AVAILABLE
            ) {
                // Already terminally invalidated; no new resume permission.
                if (!verifiedPlayPending) latchAndPauseDeniedAutoResume()
            } else {
                // Unknown/non-playable suppression transition is NOT a
                // verified focus gain.
                accept(InterruptionEvent.FocusLostPermanent)
                if (player.playWhenReady) latchAndPauseDeniedAutoResume()
            }
        }
    }

    /**
     * An explicit-play latch is needed even if Media3 has not yet emitted an
     * audible isPlaying=true callback. Reentrant callbacks are safe: state
     * and gate are latched first, and the same Player is paused afterwards.
     */
    private fun latchAndPauseDeniedAutoResume() {
        if (state.focus != FocusCondition.PERMANENT_LOST) {
            accept(InterruptionEvent.FocusAutoResumeRejected)
        }
        onDeniedFocusAutoResume()
        verifiedPlayPending = false
        if (player.playWhenReady) pauseUnsafePlayback()
    }

    /**
     * Explicit Media3 AUDIO_FOCUS_LOSS pause is observable, but does not
     * distinguish a call or classify every transient/permanent focus loss.
     * Conservatively treat it as terminal; actual phone-call identity remains
     * unavailable from Media3's public callback. No telephony receiver.
     */
    fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (closed) return
        if (playWhenReady) {
            // Genuine explicit Play must pass through existing service
            // validation. Neither focus gain nor a stale external controller
            // event by itself authorizes restarting after permanent loss.
            if (shouldBlockUnrequestedPlayback() ||
                (state.focus == FocusCondition.PERMANENT_LOST && !verifiedPlayPending)
            ) {
                latchAndPauseDeniedAutoResume()
            }
            return
        }
        if (reason == Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS) {
            observedTransientEpisode = null
            capturedPreLossPlaying = false
            verifiedPlayPending = false
            accept(InterruptionEvent.FocusLostPermanent)
        }
        // AUDIO_BECOMING_NOISY remains owned by service route safety gate.
        // No duplicate noisy/route generation or phantom focus event here.
    }

    /**
     * P05 urgent route-loss report from a REAL Media3 becoming-noisy signal
     * or service AudioDeviceCallback, collapsed into one gate-owned episode.
     * Clears all focus resume eligibility even if no Player media exists.
     * The physical Player pause and pending-IO invalidation belong to service.
     */
    fun onPersonalOutputDisconnected() {
        if (closed) return
        verifiedPlayPending = false
        observedTransientEpisode = null
        capturedPreLossPlaying = false
        accept(InterruptionEvent.RouteDisconnected)
    }

    /**
     * Called only after an existing validated explicit user Play/selection has
     * been committed by the service. Never invent focus gain at command time;
     * Media3 itself must obtain focus before isPlaying can become true.
     */
    fun onVerifiedUserPlayAccepted() {
        if (closed) return
        // The old personal output is no longer valid. A new *validated
        // deliberate* user Play can target the current Android-selected
        // output, including built-in speaker; reconnect alone never calls
        // this method. P06 further restricts ambiguous AVRCP auto-Play.
        if (!state.routeValid) accept(InterruptionEvent.RouteValidated)
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
        capturedPreLossPlaying = false
        accept(if (stop) InterruptionEvent.UserStop else InterruptionEvent.UserPause)
    }

    /**
     * Media3's real isPlaying is the only observable playback truth.
     * A newly audible user-initiated retry after terminal focus loss is the only
     * evidence used to clear the policy's terminal focus condition. This is
     * not labelled an OS gain callback and does not cause another Play.
     */
    fun onIsPlayingChanged(isPlaying: Boolean) {
        if (closed) return
        // The Player can report isPlaying=false before the separate
        // onPlaybackSuppressionReasonChanged callback. Capture prior actual
        // playing only if Media3's CURRENT suppression reason is transient.
        val observedSuppression = player.playbackSuppressionReason
        val transient = Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS
        if (!isPlaying && lastAudiblePlaying && player.playWhenReady &&
            observedSuppression == transient && lastSuppression != transient
        ) {
            capturedPreLossPlaying = true
        }
        if (observedSuppression != lastSuppression) {
            onSuppressionChanged(observedSuppression)
        }
        // A synchronous safety pause may have changed Player state inside
        // the nested callback; never process the stale audible sample.
        if (isPlaying && !player.isPlaying) return
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
        lastAudiblePlaying = player.isPlaying
        if (isPlaying) verifiedPlayPending = false
        if (isPlaying &&
            (shouldBlockUnrequestedPlayback() ||
                transition.effects.contains(InterruptionEffect.PAUSE_SERVICE_PLAYER))
        ) {
            // P04 preempts an actual unsafe audible continuation, even if
            // Android reports Player callbacks in an unexpected order.
            latchAndPauseDeniedAutoResume()
        }
    }

    fun close() {
        closed = true
        verifiedPlayPending = false
        observedTransientEpisode = null
        capturedPreLossPlaying = false
    }
}
