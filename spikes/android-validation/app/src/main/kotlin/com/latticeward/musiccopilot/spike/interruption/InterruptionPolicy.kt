package com.latticeward.musiccopilot.spike.interruption

enum class InterruptionEvent {
    TRANSIENT_LOSS,
    DUCKABLE_LOSS,
    PERMANENT_LOSS,
    FOCUS_GAIN,
    CALL_STARTED,
    MANUAL_PAUSE,
    ROUTE_DISCONNECTED,
    ROUTE_RECONNECTED,
    CORRUPT_ITEM_WITH_NEXT,
    CORRUPT_ITEM_WITHOUT_NEXT,
}

enum class InterruptionEffect {
    PAUSE,
    RESUME,
    DUCK,
    UNDUCK,
    CANCEL_VOICE,
    PUBLISH_STATE,
    SKIP_TO_NEXT,
    END_QUEUE,
    EMIT_COARSE_ERROR,
}

data class InterruptionState(
    val isPlaying: Boolean,
    val isDucked: Boolean = false,
    val voiceActive: Boolean = false,
    val routeAvailable: Boolean = true,
    val routeGeneration: Int = 0,
    val interruptionRouteGeneration: Int? = null,
    val resumeEligible: Boolean = false,
    val manuallyPaused: Boolean = !isPlaying,
)

data class InterruptionDecision(
    val state: InterruptionState,
    val effects: List<InterruptionEffect>,
)

object InterruptionPolicy {
    fun reduce(state: InterruptionState, event: InterruptionEvent): InterruptionDecision = when (event) {
        InterruptionEvent.TRANSIENT_LOSS,
        InterruptionEvent.CALL_STARTED
        -> pauseForTransientInterruption(state)

        InterruptionEvent.DUCKABLE_LOSS -> {
            if (!state.isPlaying || state.isDucked) unchanged(state) else decision(
                state.copy(isDucked = true),
                InterruptionEffect.DUCK,
                InterruptionEffect.PUBLISH_STATE,
            )
        }

        InterruptionEvent.PERMANENT_LOSS -> {
            val effects = terminalPauseEffects(state)
            decision(
                state.copy(
                    isPlaying = false,
                    isDucked = false,
                    voiceActive = false,
                    resumeEligible = false,
                    interruptionRouteGeneration = null,
                ),
                *effects.toTypedArray(),
            )
        }

        InterruptionEvent.FOCUS_GAIN -> {
            val sameRoute = state.interruptionRouteGeneration == state.routeGeneration
            val canResume = state.resumeEligible && !state.manuallyPaused &&
                state.routeAvailable && sameRoute
            if (canResume) {
                decision(
                    state.copy(
                        isPlaying = true,
                        isDucked = false,
                        resumeEligible = false,
                        interruptionRouteGeneration = null,
                    ),
                    InterruptionEffect.RESUME,
                    InterruptionEffect.PUBLISH_STATE,
                )
            } else if (state.isDucked) {
                decision(state.copy(isDucked = false), InterruptionEffect.UNDUCK, InterruptionEffect.PUBLISH_STATE)
            } else {
                unchanged(state.copy(resumeEligible = false, interruptionRouteGeneration = null))
            }
        }

        InterruptionEvent.MANUAL_PAUSE -> {
            val effects = buildList {
                if (state.isPlaying) add(InterruptionEffect.PAUSE)
                if (state.isDucked) add(InterruptionEffect.UNDUCK)
                add(InterruptionEffect.PUBLISH_STATE)
            }.distinct()
            decision(
                state.copy(
                    isPlaying = false,
                    isDucked = false,
                    manuallyPaused = true,
                    resumeEligible = false,
                    interruptionRouteGeneration = null,
                ),
                *effects.toTypedArray(),
            )
        }

        InterruptionEvent.ROUTE_DISCONNECTED -> {
            if (!state.routeAvailable) unchanged(state) else {
                val effects = terminalPauseEffects(state)
                decision(
                    state.copy(
                        isPlaying = false,
                        isDucked = false,
                        voiceActive = false,
                        routeAvailable = false,
                        routeGeneration = state.routeGeneration + 1,
                        resumeEligible = false,
                        interruptionRouteGeneration = null,
                    ),
                    *effects.toTypedArray(),
                )
            }
        }

        InterruptionEvent.ROUTE_RECONNECTED -> {
            if (state.routeAvailable) unchanged(state) else decision(
                state.copy(routeAvailable = true, resumeEligible = false),
                InterruptionEffect.PUBLISH_STATE,
            )
        }

        InterruptionEvent.CORRUPT_ITEM_WITH_NEXT -> decision(
            state,
            InterruptionEffect.EMIT_COARSE_ERROR,
            InterruptionEffect.SKIP_TO_NEXT,
        )

        InterruptionEvent.CORRUPT_ITEM_WITHOUT_NEXT -> decision(
            state.copy(isPlaying = false, isDucked = false, resumeEligible = false),
            InterruptionEffect.EMIT_COARSE_ERROR,
            InterruptionEffect.PAUSE,
            InterruptionEffect.END_QUEUE,
            InterruptionEffect.PUBLISH_STATE,
        )
    }

    private fun pauseForTransientInterruption(state: InterruptionState): InterruptionDecision {
        if (!state.isPlaying && !state.isDucked && !state.voiceActive && state.resumeEligible) return unchanged(state)
        val wasPlaying = state.isPlaying
        val effects = buildList {
            if (state.voiceActive) add(InterruptionEffect.CANCEL_VOICE)
            if (state.isDucked) add(InterruptionEffect.UNDUCK)
            if (wasPlaying) add(InterruptionEffect.PAUSE)
            add(InterruptionEffect.PUBLISH_STATE)
        }.distinct()
        return decision(
            state.copy(
                isPlaying = false,
                isDucked = false,
                voiceActive = false,
                resumeEligible = wasPlaying,
                interruptionRouteGeneration = if (wasPlaying) state.routeGeneration else null,
            ),
            *effects.toTypedArray(),
        )
    }

    private fun terminalPauseEffects(state: InterruptionState): List<InterruptionEffect> = buildList {
        if (state.voiceActive) add(InterruptionEffect.CANCEL_VOICE)
        if (state.isDucked) add(InterruptionEffect.UNDUCK)
        if (state.isPlaying) add(InterruptionEffect.PAUSE)
        add(InterruptionEffect.PUBLISH_STATE)
    }.distinct()

    private fun unchanged(state: InterruptionState) = InterruptionDecision(state, emptyList())

    private fun decision(
        state: InterruptionState,
        vararg effects: InterruptionEffect,
    ) = InterruptionDecision(state, effects.toList())
}
