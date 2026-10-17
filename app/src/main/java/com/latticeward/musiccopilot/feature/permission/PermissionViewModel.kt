package com.latticeward.musiccopilot.feature.permission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaOutcomePolicy
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Result UI owner; SavedStateHandle only stores ephemeral request count and
 * presentation, NEVER authoritative OS grant/permission settings or user media.
 */
class PermissionViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val mutableUi = MutableStateFlow(MediaRequestUiState())
    val ui: StateFlow<MediaRequestUiState> = mutableUi
    private var scanAuthorized: Boolean = false
    // SavedStateHandle stores only whether Settings was explicitly entered and
    // Android actually backgrounded this Activity. It NEVER stores OS grant truth.
    private var settingsHandoff: Boolean
        get() = savedState["p07_settings_handoff"] ?: false
        set(value) { savedState["p07_settings_handoff"] = value }
    private var settingsLeft: Boolean
        get() = savedState["p07_settings_departed"] ?: false
        set(value) { savedState["p07_settings_departed"] = value }

    val requestsThisSession: Int get() = savedState["p06_media_requests"] ?: 0

    fun requestStarting() {
        savedState["p06_media_requests"] = requestsThisSession + 1
        mutableUi.value = MediaRequestUiState(pending = true)
    }

    fun nativeResult(observation: NativePermissionObservation, action: BlockedAction) {
        settingsHandoff = false
        settingsLeft = false
        scanAuthorized = observation.granted && action == BlockedAction.StartScan
        val outcome = MediaOutcomePolicy.project(observation, requestsThisSession)
        mutableUi.value = MediaRequestUiState(
            pending = false,
            attempted = true,
            denied = !observation.granted,
            outcome = outcome,
        )
    }

    fun nativeLaunchFailed() {
        scanAuthorized = false
        mutableUi.value = MediaRequestUiState(
            attempted = true, denied = true,
            outcome = MediaPermissionOutcome.DeniedRequestable,
        )
    }

    fun retryRationale() {
        scanAuthorized = false
        settingsHandoff = false
        settingsLeft = false
        mutableUi.value = MediaRequestUiState()
    }

    /** Consumption is irreversible, regardless of OS behavior after this tap. */
    fun takeOneGrantedScan(): Boolean {
        val eligible = mutableUi.value.outcome == MediaPermissionOutcome.Granted && scanAuthorized
        scanAuthorized = false
        mutableUi.value = MediaRequestUiState()
        return eligible
    }

    fun restorePending() {
        mutableUi.value = MediaRequestUiState(pending = true)
    }

    fun restoreAttempted() {
        if (!mutableUi.value.pending && mutableUi.value.outcome == MediaPermissionOutcome.None) {
            mutableUi.value = MediaRequestUiState(attempted = true)
        }
    }

    fun settingsLaunchFailed() {
        settingsHandoff = false
        settingsLeft = false
        // Preserve recovery and manual Not now; never relaunch automatically.
    }

    fun manualExit() {
        scanAuthorized = false
        settingsHandoff = false
        settingsLeft = false
        mutableUi.value = MediaRequestUiState()
    }

    fun settingsTapped() {
        scanAuthorized = false
        settingsLeft = false
        settingsHandoff = true
    }

    /** Called by Activity.onPause after a real system Settings handoff. */
    fun settingsDepartureObserved() {
        if (settingsHandoff) settingsLeft = true
    }

    /**
     * Returning from Settings never scans. A fresh Android grant authorizes
     * exactly one *user-tapped* Continue; cancellation leaves manual recovery.
     * Startup and rotation without a completed handoff are not Settings returns.
     */
    fun onReturnFromSettings(observation: NativePermissionObservation) {
        if (!settingsHandoff || !settingsLeft) return
        settingsHandoff = false
        settingsLeft = false
        scanAuthorized = observation.granted
        val outcome = MediaOutcomePolicy.project(observation, requestsThisSession)
        mutableUi.value = MediaRequestUiState(
            attempted = true,
            denied = !observation.granted,
            outcome = outcome,
            revoked = !observation.granted && mutableUi.value.revoked,
        )
    }

    /** Invalidate any pending grant continuation and show safe OS recovery. */
    fun permissionRevoked() {
        scanAuthorized = false
        mutableUi.value = MediaRequestUiState(
            attempted = true, denied = true,
            outcome = MediaPermissionOutcome.SettingsOnly, revoked = true,
        )
    }
}
