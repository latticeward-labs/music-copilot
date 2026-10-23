package com.latticeward.musiccopilot.playback.interruption

/**
 * Issue #8 P02: pure interruption/focus/route policy; no Android, Media3,
 * telephony, audio-device, dispatcher, database or network dependency.
 *
 * The service remains the only Player/MediaSession and Audio Focus authority.
 * P03+ will normalize real platform observations into these typed events and
 * apply effects on the one service Player thread. Merely producing effects
 * here is NOT evidence of a real Android Audio Focus callback.
 */
internal enum class InterruptionPlayback { PLAYING, PAUSED, STOPPED }
internal enum class FocusCondition { AVAILABLE, TRANSIENT_LOST, PERMANENT_LOST, DUCKED }
internal enum class PlayRequestOrigin { VERIFIED_USER, AMBIGUOUS_EXTERNAL }

/** Identifies exactly the interruption that paused an already-playing session. */
internal data class ResumeEligibility(
    val focusEpisode: Long,
    val routeGeneration: Long,
    val manualRevision: Long,
)

internal data class InterruptionState(
    val playback: InterruptionPlayback = InterruptionPlayback.PAUSED,
    val focus: FocusCondition = FocusCondition.AVAILABLE,
    val routeGeneration: Long = 0L,
    val routeValid: Boolean = true,
    val requiresExplicitPlay: Boolean = false,
    val manualRevision: Long = 0L,
    val focusEpisode: Long = 0L,
    val pendingResume: ResumeEligibility? = null,
    val lastEventSequence: Long = 0L,
    val lastObservedAtElapsedMs: Long = 0L,
) {
    init {
        require(routeGeneration >= 0L)
        require(manualRevision >= 0L)
        require(focusEpisode >= 0L)
        require(lastEventSequence >= 0L)
        require(lastObservedAtElapsedMs >= 0L)
    }
}

/**
 * Typed platform-neutral observations. A source must record the actual
 * pre-interruption Playing truth BEFORE Media3 changes playWhenReady/isPlaying.
 * "FocusRestored" needs a token from the corresponding observed focus loss;
 * guessing a focus/call event is never permitted.
 */
internal sealed interface InterruptionEvent {
    /** Playback observed from the authoritative service Player, not UI. */
    data class PlaybackObserved(val isPlaying: Boolean) : InterruptionEvent

    /**
     * This value is a captured, authoritative pre-loss sample, NOT a guess
     * derived from a later Paused snapshot. P03's adapter must prove it.
     */
    data class FocusLostTransient(val wasPlayingBeforeLoss: Boolean) : InterruptionEvent

    /** Includes lasting focus loss or focus refused by the OS. */
    data object FocusLostPermanent : InterruptionEvent

    /** OS/Media3-managed duck, not permission for app-side volume change. */
    data object FocusDuck : InterruptionEvent

    /** The source must identify the same focus episode; stale gains are ignored. */
    data class FocusRestored(val focusEpisode: Long) : InterruptionEvent

    /**
     * Focus suppression has ended, but the observed focus episode did not
     * satisfy the four resume guards. Latch explicit-play-only WITHOUT
     * pretending the user manually paused or generating an OS focus event.
     */
    data object FocusAutoResumeRejected : InterruptionEvent

    /** Includes becoming-noisy and relevant output removal, after normalization. */
    data object RouteDisconnected : InterruptionEvent

    /** Only if the active/replacement output is actually valid for playback. */
    data object RouteValidated : InterruptionEvent

    /** A real user Pause, stronger than any pending transient resume. */
    data object UserPause : InterruptionEvent

    /** Stop also clears a pending transient resume. */
    data object UserStop : InterruptionEvent

    /**
     * Clear safety latches only for a VERIFIED user Play and an unchanged
     * valid route, with focus available. An ambiguous remote/AVRCP Play cannot
     * be treated as user intent, including immediately after reconnect.
     */
    data class PlayRequested(
        val origin: PlayRequestOrigin,
        val expectedRouteGeneration: Long,
    ) : InterruptionEvent
}

/**
 * Event order comes from the service's serialized receipt-time dispatcher.
 * capture time can be injected by a fake clock; it is diagnostic metadata,
 * NEVER proof of ordering and NEVER authorizes auto-resume.
 */
internal data class InterruptionEnvelope(
    val sequence: Long,
    val observedAtElapsedMs: Long,
    val event: InterruptionEvent,
)

/** Commands suggested to P03+ adapters. No side effect occurs in P02. */
internal enum class InterruptionEffect {
    PAUSE_SERVICE_PLAYER,
    RESUME_SERVICE_PLAYER,
    ALLOW_VERIFIED_USER_PLAY,
    REJECT_UNSAFE_PLAY,
    CANCEL_VOICE_FOR_FOCUS,
    CANCEL_VOICE_FOR_ROUTE,
}

internal data class InterruptionTransition(
    val state: InterruptionState,
    val effects: List<InterruptionEffect> = emptyList(),
    val applied: Boolean = false,
)

/**
 * Injected only at the platform boundary. Synthetic clocks/sequences will be
 * used by future authored tests, not by this reducer and not in P02 QA.
 */
internal fun interface InterruptionClock {
    fun elapsedRealtimeMs(): Long
}

/** Deterministic serial event numbering, injectable for future tests. */
internal fun interface InterruptionSequenceSource {
    fun nextSequence(): Long
}

internal fun interface InterruptionEventReceiver {
    fun dispatch(envelope: InterruptionEnvelope): InterruptionTransition
}

/**
 * Deterministic reducer. The *only* mutable owner is its caller, which must
 * serialize delivery on the existing service looper and persist neither route
 * identifiers nor Voice content. In particular, this object requests NO focus.
 */
internal object InterruptionReducer {
    fun reduce(
        state: InterruptionState,
        envelope: InterruptionEnvelope,
    ): InterruptionTransition {
        // Reject duplicate/out-of-order receipt sequences without effects.
        if (envelope.sequence <= state.lastEventSequence ||
            envelope.observedAtElapsedMs < 0L
        ) return InterruptionTransition(state)

        // Saturating sequence is fail-closed: the adapter must restart its
        // serial sequence owner before exhaustion; never wrap to negative.
        val accepted = state.copy(
            lastEventSequence = envelope.sequence,
            lastObservedAtElapsedMs = envelope.observedAtElapsedMs,
        )

        fun out(
            next: InterruptionState,
            vararg effects: InterruptionEffect,
        ) = InterruptionTransition(next, effects.toList(), applied = true)

        return when (val event = envelope.event) {
            is InterruptionEvent.PlaybackObserved -> {
                val playback = if (event.isPlaying) {
                    InterruptionPlayback.PLAYING
                } else {
                    InterruptionPlayback.PAUSED
                }
                val unsafe = event.isPlaying && (
                    !accepted.routeValid ||
                        accepted.requiresExplicitPlay ||
                        accepted.focus == FocusCondition.PERMANENT_LOST ||
                        accepted.focus == FocusCondition.TRANSIENT_LOST
                    )
                if (unsafe) {
                    // If the real Player starts again without authorization,
                    // reissue safety pause even after prior pause requests.
                    out(
                        accepted.copy(playback = InterruptionPlayback.PAUSED),
                        InterruptionEffect.PAUSE_SERVICE_PLAYER,
                    )
                } else {
                    out(accepted.copy(playback = playback))
                }
            }

            is InterruptionEvent.FocusLostTransient -> {
                if (accepted.focus == FocusCondition.TRANSIENT_LOST ||
                    accepted.focus == FocusCondition.PERMANENT_LOST
                ) {
                    // Duplicate transient loss must not rearm eligibility.
                    out(accepted)
                } else {
                    val episode = nextEpoch(accepted.focusEpisode)
                    val eligible = event.wasPlayingBeforeLoss &&
                        accepted.routeValid &&
                        !accepted.requiresExplicitPlay &&
                        accepted.playback != InterruptionPlayback.STOPPED
                    val candidate = if (eligible && episode != accepted.focusEpisode) {
                        ResumeEligibility(
                            focusEpisode = episode,
                            routeGeneration = accepted.routeGeneration,
                            manualRevision = accepted.manualRevision,
                        )
                    } else null
                    out(
                        accepted.copy(
                            playback = InterruptionPlayback.PAUSED,
                            focus = FocusCondition.TRANSIENT_LOST,
                            focusEpisode = episode,
                            pendingResume = candidate,
                        ),
                        InterruptionEffect.PAUSE_SERVICE_PLAYER,
                        InterruptionEffect.CANCEL_VOICE_FOR_FOCUS,
                    )
                }
            }

            InterruptionEvent.FocusLostPermanent -> {
                if (accepted.focus == FocusCondition.PERMANENT_LOST) {
                    out(accepted)
                } else {
                    out(
                        accepted.copy(
                            playback = InterruptionPlayback.PAUSED,
                            focus = FocusCondition.PERMANENT_LOST,
                            focusEpisode = nextEpoch(accepted.focusEpisode),
                            pendingResume = null,
                            requiresExplicitPlay = true,
                        ),
                        InterruptionEffect.PAUSE_SERVICE_PLAYER,
                        InterruptionEffect.CANCEL_VOICE_FOR_FOCUS,
                    )
                }
            }

            InterruptionEvent.FocusDuck -> {
                if (accepted.focus != FocusCondition.AVAILABLE) {
                    out(accepted)
                } else {
                    // Duck is handled by Media3/Android. Never separately
                    // alter Player volume or start a previously paused song.
                    out(
                        accepted.copy(
                            focus = FocusCondition.DUCKED,
                            focusEpisode = nextEpoch(accepted.focusEpisode),
                            pendingResume = null,
                        ),
                        InterruptionEffect.CANCEL_VOICE_FOR_FOCUS,
                    )
                }
            }

            is InterruptionEvent.FocusRestored -> {
                if (event.focusEpisode != accepted.focusEpisode ||
                    accepted.focus == FocusCondition.AVAILABLE
                ) {
                    // Old gain cannot undo a newer focus loss/manual pause.
                    out(accepted)
                } else {
                    val candidate = accepted.pendingResume
                    val mayResume = accepted.focus == FocusCondition.TRANSIENT_LOST &&
                        candidate != null &&
                        candidate.focusEpisode == event.focusEpisode &&
                        accepted.routeValid &&
                        candidate.routeGeneration == accepted.routeGeneration &&
                        candidate.manualRevision == accepted.manualRevision &&
                        !accepted.requiresExplicitPlay &&
                        accepted.playback == InterruptionPlayback.PAUSED
                    val restored = accepted.copy(
                        focus = FocusCondition.AVAILABLE,
                        pendingResume = null,
                    )
                    if (mayResume) {
                        out(restored, InterruptionEffect.RESUME_SERVICE_PLAYER)
                    } else {
                        out(restored)
                    }
                }
            }

            InterruptionEvent.FocusAutoResumeRejected -> {
                // Media3 may resume automatically on suppression removal.
                // If no eligible resume effect was issued, the service vetoes
                // that playback before it becomes audible where callbacks permit.
                // This policy latch outlives the suppression event until a
                // verified fresh user Play, but does NOT count as manual Pause.
                out(
                    accepted.copy(
                        playback = InterruptionPlayback.PAUSED,
                        pendingResume = null,
                        requiresExplicitPlay = true,
                    ),
                    InterruptionEffect.PAUSE_SERVICE_PLAYER,
                )
            }

            InterruptionEvent.RouteDisconnected -> {
                if (!accepted.routeValid) {
                    // Noisy + AudioDeviceCallback often represent the SAME
                    // removal; it must not emit a second Pause/Voice cancel.
                    out(accepted)
                } else {
                    out(
                        accepted.copy(
                            playback = InterruptionPlayback.PAUSED,
                            routeGeneration = nextEpoch(accepted.routeGeneration),
                            routeValid = false,
                            requiresExplicitPlay = true,
                            pendingResume = null,
                        ),
                        InterruptionEffect.PAUSE_SERVICE_PLAYER,
                        InterruptionEffect.CANCEL_VOICE_FOR_ROUTE,
                    )
                }
            }

            InterruptionEvent.RouteValidated -> {
                // Reconnection may only revalidate; never clear safety latch,
                // emit Resume or recreate the old focus resume candidate.
                out(accepted.copy(routeValid = true))
            }

            InterruptionEvent.UserPause -> {
                out(
                    accepted.copy(
                        playback = InterruptionPlayback.PAUSED,
                        manualRevision = nextEpoch(accepted.manualRevision),
                        pendingResume = null,
                        requiresExplicitPlay = true,
                    ),
                    InterruptionEffect.PAUSE_SERVICE_PLAYER,
                )
            }

            InterruptionEvent.UserStop -> {
                out(
                    accepted.copy(
                        playback = InterruptionPlayback.STOPPED,
                        manualRevision = nextEpoch(accepted.manualRevision),
                        pendingResume = null,
                        requiresExplicitPlay = true,
                    ),
                    InterruptionEffect.PAUSE_SERVICE_PLAYER,
                )
            }

            is InterruptionEvent.PlayRequested -> {
                val verified = event.origin == PlayRequestOrigin.VERIFIED_USER
                if (!verified ||
                    !accepted.routeValid ||
                    event.expectedRouteGeneration != accepted.routeGeneration ||
                    accepted.focus != FocusCondition.AVAILABLE
                ) {
                    out(accepted, InterruptionEffect.REJECT_UNSAFE_PLAY)
                } else {
                    // The adapter validates permissions/queue/source and
                    // executes this accepted Play via the existing processor.
                    // No real Playing is claimed until a Player observation.
                    out(
                        accepted.copy(
                            requiresExplicitPlay = false,
                            pendingResume = null,
                            // Preserve observed playback; only the actual
                            // service Player can confirm a transition.
                            playback = accepted.playback,
                        ),
                        InterruptionEffect.ALLOW_VERIFIED_USER_PLAY,
                    )
                }
            }
        }
    }

    private fun nextEpoch(current: Long): Long =
        if (current == Long.MAX_VALUE) current else current + 1L
}
