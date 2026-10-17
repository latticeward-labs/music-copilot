package com.latticeward.musiccopilot.feature.permission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaOutcomePolicy
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.SettingsReturnCoordinator
import com.latticeward.musiccopilot.core.application.permission.SettingsReturnOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Presentation owner only. A saved Settings ticket is not a grant or an
 * authorization to scan; Android's checkSelfPermission is always re-read.
 * A Settings return can authorize one explicit Continue, never an auto-scan.
 */
class PermissionViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val mutableUi = MutableStateFlow(MediaRequestUiState())
    val ui: StateFlow<MediaRequestUiState> = mutableUi
    private var scanAuthorized: Boolean = false

    private var settingsHandoff: Boolean
        get() = savedState["p07_settings_handoff"] ?: false
        set(value) { savedState["p07_settings_handoff"] = value }

    private var settingsActivityLeft: Boolean
        get() = savedState["p07_settings_activity_left"] ?: false
        set(value) { savedState["p07_settings_activity_left"] = value }

    val requestsThisSession: Int get() = savedState["p06_media_requests"] ?: 0

    fun requestStarting() {
        savedState["p06_media_requests"] = requestsThisSession + 1
        scanAuthorized = false
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

    /** Consumption is irreversible, including after a duplicate tap or lost grant. */
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
        settingsActivityLeft = false
        scanAuthorized = false
        // Keep Settings-only result and manual exit; do not re-open automatically.
    }

    fun manualExit() {
        scanAuthorized = false
        settingsHandoff = false
        settingsActivityLeft = false
        mutableUi.value = MediaRequestUiState()
    }

    /** Only reached from the explicit Open Settings button. */
    fun settingsTapped() {
        if (mutableUi.value.outcome != MediaPermissionOutcome.SettingsOnly) return
        scanAuthorized = false
        settingsHandoff = true
        settingsActivityLeft = false
    }

    /** Called by Activity.onPause, not by Compose recomposition or onResume. */
    fun onActivityPaused() {
        if (settingsHandoff) settingsActivityLeft = true
    }

    /** Fresh OS observation required. Returns Ignored for startup or duplicate resumes. */
    fun onReturnFromSettings(
        observation: NativePermissionObservation,
    ): SettingsReturnOutcome {
        val result = SettingsReturnCoordinator.classify(
            launchedByUser = settingsHandoff,
            activityLeft = settingsActivityLeft,
            observation = observation,
            explicitRequests = requestsThisSession,
        )
        if (result == SettingsReturnOutcome.Ignored) return result
        settingsHandoff = false
        settingsActivityLeft = false
        // Preserved one-use blocked media intent: explicit Continue still required.
        scanAuthorized = result == SettingsReturnOutcome.Granted
        mutableUi.value = MediaRequestUiState(
            attempted = true,
            denied = !observation.granted,
            outcome = SettingsReturnCoordinator.uiOutcome(result),
        )
        return result
    }

    /**
     * The OS revoked a grant while this app was away or in use. Never keep an
     * optimistic "granted" result or an authorized scan across the revocation.
     */
    fun onMediaRevoked() {
        scanAuthorized = false
        settingsHandoff = false
        settingsActivityLeft = false
        mutableUi.value = MediaRequestUiState()
    }
}
