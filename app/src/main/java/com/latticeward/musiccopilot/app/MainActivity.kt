package com.latticeward.musiccopilot.app

import android.content.Intent
import androidx.core.net.toUri
import android.provider.Settings
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.app.di.ApplicationBindings.StorageStartState
import com.latticeward.musiccopilot.app.navigation.AppNavHost
import com.latticeward.musiccopilot.app.navigation.RootNavigationStateHolder
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapEvidence
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoutePolicy
import com.latticeward.musiccopilot.core.application.permission.BlockedAction
import com.latticeward.musiccopilot.core.application.permission.MediaRequestGate
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.PermissionCapability
import com.latticeward.musiccopilot.core.application.permission.PermissionPlatformPolicy
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.feature.permission.MediaRequestUiState
import com.latticeward.musiccopilot.feature.permission.PermissionViewModel
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.graphics.toArgb

/**
 * Android OS permission truth is read on foreground entry. This phase never
 * opens the system permission dialog or caches a "grant" in DataStore.
 */
class MainActivity : ComponentActivity() {
    private val mediaAccess = MutableStateFlow(PermissionStatus.Unknown)
    private val permissionUi: PermissionViewModel by viewModels()
    private val gate = MediaRequestGate(Build.VERSION.SDK_INT)
    private val requestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // ActivityResult Boolean is not permission truth. Re-read Android OS.
            val wasPending = gate.awaitingPlatformResult
            val freshGrant = hasMediaPermission()
            val observation = NativePermissionObservation(
                granted = freshGrant,
                requestedBefore = true,
                shouldShowRationale =
                    shouldShowRequestPermissionRationale(mediaPermissionName()),
            )
            val action = gate.complete(observation)
            if (wasPending) permissionUi.nativeResult(observation, action)
            refreshMediaPermission()
            // Never scan in the OS callback. Granted screen Continue owns that
            // one remaining explicit action, with a second fresh OS recheck.
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean("p05_native_media_request_pending") == true) {
            // ActivityResultRegistry will deliver the interrupted native result.
            // Restoring a pending request does NOT open another dialog.
            gate.restoreAwaitingNativeResult()
            permissionUi.restorePending()
        } else if (savedInstanceState?.getBoolean("p05_media_attempted") == true) {
            permissionUi.restoreAttempted()
        }
        window.statusBarColor = MusicColors.systemBar.toArgb()
        window.navigationBarColor = MusicColors.systemBar.toArgb()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        val bindings = (application as MusicCopilotApplication).bindings
        setContent {
            RootComposeShell(
                bindings = bindings,
                onExit = ::finish,
                onRefreshPermission = ::refreshMediaPermission,
                onRequestMediaPermission = ::requestMediaPermission,
                onContinueGranted = ::continueGrantedScan,
                onRetryMediaPermission = permissionUi::retryRationale,
                onExitPermissionResult = permissionUi::manualExit,
                onOpenAndroidSettings = ::openMediaAppSettings,
                mediaRequestUi = permissionUi.ui,
            )
        }
    }

    private fun mediaPermissionName(): String =
        requireNotNull(PermissionPlatformPolicy.manifestPermission(
            PermissionCapability.MediaAudio, Build.VERSION.SDK_INT,
        ))

    private fun hasMediaPermission(): Boolean {
        val permission = mediaPermissionName()
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun refreshMediaPermission() {
        val permission = mediaPermissionName()
        mediaAccess.value = if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.Granted
        } else {
            // Preserve observed revocation, not a cached permission grant.
            if (mediaAccess.value == PermissionStatus.Granted ||
                mediaAccess.value == PermissionStatus.Revoked) {
                PermissionStatus.Revoked
            } else PermissionStatus.NotRequested
        }
    }

    private fun requestMediaPermission() {
        // Called ONLY by an explicit WF-03 Continue tap, not by onResume/bootstrap.
        val effect = gate.begin() ?: return
        permissionUi.requestStarting()
        val permission = requireNotNull(
            PermissionPlatformPolicy.manifestPermission(effect.capability, Build.VERSION.SDK_INT),
        )
        if (hasMediaPermission()) {
            // No redundant platform prompt: project only a real OS grant.
            val freshGrant = hasMediaPermission()
            val action = gate.complete(NativePermissionObservation(
                granted = freshGrant, requestedBefore = true,
                shouldShowRationale = false,
            ))
            permissionUi.nativeResult(NativePermissionObservation(
                granted = freshGrant, requestedBefore = true,
                shouldShowRationale = false,
            ), action)
            refreshMediaPermission()
        } else {
            try {
                requestLauncher.launch(permission)
            } catch (failure: RuntimeException) {
                gate.abandon()
                permissionUi.nativeLaunchFailed()
                refreshMediaPermission()
            }
        }
    }

    private fun continueGrantedScan() {
        // Consume the block-action first; even duplicate taps cannot scan twice.
        val authorized = permissionUi.takeOneGrantedScan()
        val freshGrant = hasMediaPermission()
        refreshMediaPermission()
        if (authorized && freshGrant) {
            (application as MusicCopilotApplication).bindings
                .scanAfterExplicitMediaGrant(applicationContext, mediaPermissionName())
        }
    }

    private fun openMediaAppSettings() {
        // Only called from explicit SettingsOnly button. Never opens on startup.
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            ("package:$packageName").toUri())
        try {
            permissionUi.settingsTapped()
            startActivity(intent)
        } catch (failure: RuntimeException) {
            // No coercive Settings loop or crash on OEMs without the Activity.
            permissionUi.settingsLaunchFailed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("p05_native_media_request_pending", gate.awaitingPlatformResult)
        outState.putBoolean("p05_media_attempted", permissionUi.ui.value.attempted)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        // Only a real transition away from Activity can complete Settings return.
        permissionUi.settingsDepartureObserved()
    }

    override fun onResume() {
        super.onResume()
        val previouslyGranted = mediaAccess.value == PermissionStatus.Granted
        val freshGrant = hasMediaPermission()
        refreshMediaPermission()
        if (previouslyGranted && !freshGrant) {
            // Immediately make any previous derived Ready library untrustworthy.
            (application as MusicCopilotApplication).bindings.onMediaAccessRevoked()
            permissionUi.permissionRevoked()
        }
        permissionUi.onReturnFromSettings(NativePermissionObservation(
            granted = freshGrant, requestedBefore = permissionUi.requestsThisSession > 0,
            shouldShowRationale = shouldShowRequestPermissionRationale(mediaPermissionName()),
            previouslyGranted = previouslyGranted && !freshGrant,
        ))
        (application as MusicCopilotApplication).bindings.onForeground()
    }

    @Composable
    private fun RootComposeShell(
        bindings: ApplicationBindings,
        onExit: () -> Unit,
        onRefreshPermission: () -> Unit,
        onRequestMediaPermission: () -> Unit,
        onContinueGranted: () -> Unit,
        onRetryMediaPermission: () -> Unit,
        onExitPermissionResult: () -> Unit,
        onOpenAndroidSettings: () -> Unit,
        mediaRequestUi: kotlinx.coroutines.flow.StateFlow<MediaRequestUiState>,
    ) {
        val library by bindings.libraryRepository.index.collectAsStateWithLifecycle()
        val playbackConnection by bindings.playbackGateway.connectionState.collectAsStateWithLifecycle()
        val preferences by bindings.preferencesRepository.preferences.collectAsStateWithLifecycle()
        val preferencesLoad by bindings.preferencesRepository.loadState.collectAsStateWithLifecycle()
        val storage by bindings.storageStartState.collectAsStateWithLifecycle()
        val permission by mediaAccess.collectAsStateWithLifecycle()
        val mediaRequest by mediaRequestUi.collectAsStateWithLifecycle()

        val evidence = BootstrapEvidence(
            preferencesLoaded = preferencesLoad == PreferencesLoadState.Ready,
            migrationsVerified = storage == StorageStartState.Ready,
            startupFailed = preferencesLoad == PreferencesLoadState.Failed ||
                storage == StorageStartState.Failed,
            onboardingComplete = preferences.onboardingComplete,
            mediaPermission = permission,
            libraryStatus = library.status,
        )
        val route = BootstrapRoutePolicy.project(evidence)
        MusicCopilotTheme {
            AppNavHost(
                route = route,
                readiness = RootNavigationStateHolder().readiness(library.status, playbackConnection),
                permission = permission,
                preferencesRepository = bindings.preferencesRepository,
                onRetry = bindings::retryFirstLaunch,
                onRefreshPermission = onRefreshPermission,
                onRequestMediaPermission = onRequestMediaPermission,
                onContinueGranted = onContinueGranted,
                onRetryMediaPermission = onRetryMediaPermission,
                onExitPermissionResult = onExitPermissionResult,
                onOpenAndroidSettings = onOpenAndroidSettings,
                mediaRequestUi = mediaRequest,
                onFirstUseComplete = bindings::onForeground,
                onExit = onExit,
            )
        }
    }
}
