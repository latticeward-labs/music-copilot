package com.latticeward.musiccopilot.feature.permission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaOutcomePolicy
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
    private var settingsHandoff = false

    val requestsThisSession: Int get() = savedState["p06_media_requests"] ?: 0

    fun requestStarting() {
        savedState["p06_media_requests"] = requestsThisSession + 1
        mutableUi.value = MediaRequestUiState(pending = true)
    }

    fun nativeResult(observation: NativePermissionObservation, action: BlockedAction) {
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
        mutableUi.value = MediaRequestUiState()
    }

    /** Consumption is irreversible, regardless of OS behavior after this tap. */
    fun takeOneGrantedScan(): Boolean {
        val eligible = mutableUi.value.outcome == MediaPermissionOutcome.Granted && scanAuthorized
        scanAuthorized = false
        mutableUi.value = MediaRequestUiState()
        return eligible
    }

    fun manualExit() {
        scanAuthorized = false
        mutableUi.value = MediaRequestUiState()
    }

    fun settingsTapped() {
        scanAuthorized = false
        settingsHandoff = true
    }

    /** P06 minimal result freshness on return; complete P07 recheck/revocation is separate. */
    fun onReturnFromSettings(observation: NativePermissionObservation) {
        if (!settingsHandoff) return
        settingsHandoff = false
        scanAuthorized = false
        val outcome = MediaOutcomePolicy.project(observation, requestsThisSession)
        mutableUi.value = MediaRequestUiState(
            attempted = true,
            denied = !observation.granted,
            outcome = outcome,
        )
    }
}
