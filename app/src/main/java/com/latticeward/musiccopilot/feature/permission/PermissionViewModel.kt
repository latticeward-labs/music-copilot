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
    // P11: SavedStateHandle retains ONLY non-authoritative, safe presentation.
    // Never save a granted result or a one-use scan authorization across process death.
    private val mutableUi = MutableStateFlow(restorePresentation())

    private fun restorePresentation(): MediaRequestUiState {
        val outcome = when (savedState.get<String>("p11_media_ui_outcome")) {
            MediaPermissionOutcome.DeniedRequestable.name -> MediaPermissionOutcome.DeniedRequestable
            MediaPermissionOutcome.SettingsOnly.name -> MediaPermissionOutcome.SettingsOnly
            else -> MediaPermissionOutcome.None
        }
        return MediaRequestUiState(
            attempted = savedState["p11_media_ui_attempted"] ?: false,
            denied = outcome != MediaPermissionOutcome.None,
            outcome = outcome,
            revoked = outcome == MediaPermissionOutcome.SettingsOnly &&
                (savedState["p11_media_ui_revoked"] ?: false),
        )
    }

    private fun publish(state: MediaRequestUiState) {
        mutableUi.value = state
        savedState["p11_media_ui_attempted"] = state.attempted
        savedState["p11_media_ui_outcome"] = when (state.outcome) {
            MediaPermissionOutcome.DeniedRequestable, MediaPermissionOutcome.SettingsOnly ->
                state.outcome.name
            else -> null // A Granted projection cannot survive a killed process.
        }
        savedState["p11_media_ui_revoked"] =
            state.outcome == MediaPermissionOutcome.SettingsOnly && state.revoked
    }
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
        publish(MediaRequestUiState(pending = true))
    }

    fun nativeResult(observation: NativePermissionObservation, action: BlockedAction) {
        scanAuthorized = observation.granted && action == BlockedAction.StartScan
        val outcome = MediaOutcomePolicy.project(observation, requestsThisSession)
        publish(MediaRequestUiState(
            pending = false,
            attempted = true,
            denied = !observation.granted,
            outcome = outcome,
        ))
    }

    fun nativeLaunchFailed() {
        scanAuthorized = false
        publish(MediaRequestUiState(
            attempted = true, denied = true,
            outcome = MediaPermissionOutcome.DeniedRequestable,
        ))
    }

    fun retryRationale() {
        scanAuthorized = false
        publish(MediaRequestUiState())
    }

    /** Consumption is irreversible, including after a duplicate tap or lost grant. */
    fun takeOneGrantedScan(): Boolean {
        val eligible = mutableUi.value.outcome == MediaPermissionOutcome.Granted && scanAuthorized
        scanAuthorized = false
        publish(MediaRequestUiState())
        return eligible
    }

    fun restorePending() {
        publish(MediaRequestUiState(pending = true))
    }

    fun restoreAttempted() {
        if (!mutableUi.value.pending && mutableUi.value.outcome == MediaPermissionOutcome.None) {
            publish(MediaRequestUiState(attempted = true))
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
        publish(MediaRequestUiState())
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
        publish(MediaRequestUiState(
            attempted = true,
            denied = !observation.granted,
            outcome = SettingsReturnCoordinator.uiOutcome(result),
        ))
        return result
    }

    /**
     * P11: a recovered Denied/Settings-only UI is display state, not OS truth.
     * If a fresh foreground OS read is now Granted and no Settings-return ticket
     * is pending, discard the stale denial. Do NOT create a scan authorization;
     * the user still chooses a new explicit Scan/Rescan action.
     */
    fun reconcileFreshAndroidGrant(granted: Boolean) {
        if (granted && !settingsHandoff && mutableUi.value.outcome in listOf(
                MediaPermissionOutcome.DeniedRequestable, MediaPermissionOutcome.SettingsOnly,
            )) {
            scanAuthorized = false
            publish(MediaRequestUiState())
        }
    }

    /**
     * The OS revoked a grant while this app was away or in use. Never keep an
     * optimistic "granted" result or an authorized scan across the revocation.
     */
    fun onMediaRevoked() {
        scanAuthorized = false
        settingsHandoff = false
        settingsActivityLeft = false
        publish(MediaRequestUiState())
    }
}
