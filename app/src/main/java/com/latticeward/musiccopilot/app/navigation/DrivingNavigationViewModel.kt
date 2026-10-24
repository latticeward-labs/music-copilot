package com.latticeward.musiccopilot.app.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/**
 * Issue #9 P03: root navigation owns the non-Driving origin, not a screen.
 * UI-only state lives in the Activity-scoped ViewModel: it survives normal
 * configuration recreation, but is NOT saved to disk or SavedStateHandle.
 * On a fresh process it starts inactive: never auto-enter Driving.
 */
data class DrivingNavigationState(
    val active: Boolean = false,
    val origin: Destination = Destination.Library,
)

object DrivingNavigationPolicy {
    private val approvedOrigins = setOf(Destination.Library, Destination.FullPlayer)

    /** Only an explicit in-app tap at an approved origin may enter Driving. */
    fun enter(
        state: DrivingNavigationState,
        origin: Destination,
        allowedByAppRoute: Boolean,
    ): DrivingNavigationState =
        if (!allowedByAppRoute || state.active || origin !in approvedOrigins) state
        else DrivingNavigationState(active = true, origin = origin)

    /** Exit/Back never starts, pauses, stops, or changes the service Player. */
    fun exit(@Suppress("UNUSED_PARAMETER") state: DrivingNavigationState): DrivingNavigationState =
        DrivingNavigationState()

    fun destinationAfterExit(state: DrivingNavigationState): Destination =
        state.origin.takeIf { it in approvedOrigins } ?: Destination.Library
}

class DrivingNavigationViewModel : ViewModel() {
    var state by mutableStateOf(DrivingNavigationState())
        private set

    fun enterFrom(origin: Destination, allowedByAppRoute: Boolean) {
        state = DrivingNavigationPolicy.enter(state, origin, allowedByAppRoute)
    }

    fun exit(): Destination {
        val destination = DrivingNavigationPolicy.destinationAfterExit(state)
        state = DrivingNavigationPolicy.exit(state)
        return destination
    }

    /** Bootstrap/onboarding/permission recovery outrank a stale Driving route. */
    fun clearForSystemBoundary() {
        if (state.active) state = DrivingNavigationState()
    }
}
