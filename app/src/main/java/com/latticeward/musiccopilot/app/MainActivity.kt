package com.latticeward.musiccopilot.app

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.graphics.toArgb

/**
 * Android OS permission truth is read on foreground entry. This phase never
 * opens the system permission dialog or caches a "grant" in DataStore.
 */
class MainActivity : ComponentActivity() {
    private val mediaAccess = MutableStateFlow(PermissionStatus.Unknown)
    private val requestUi = MutableStateFlow(MediaRequestUiState())
    private val gate = MediaRequestGate(Build.VERSION.SDK_INT)
    private val requestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Never trust just the dialog result; check the OS capability again.
        val freshGrant = hasMediaPermission()
        val action = gate.complete(
            NativePermissionObservation(
                granted = freshGrant,
                requestedBefore = true,
                shouldShowRationale = shouldShowRequestPermissionRationale(mediaPermissionName()),
            ),
        )
        requestUi.value = MediaRequestUiState(
            pending = false, attempted = true, denied = !freshGrant,
        )
        refreshMediaPermission()
        if (action == BlockedAction.StartScan && freshGrant) {
            (application as MusicCopilotApplication).bindings
                .scanAfterExplicitMediaGrant(applicationContext, mediaPermissionName())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState?.getBoolean("p05_native_media_request_pending") == true) {
            // ActivityResultRegistry will deliver the interrupted native result.
            // Restoring a pending request does NOT open another dialog.
            gate.restoreAwaitingNativeResult()
            requestUi.value = MediaRequestUiState(pending = true)
        } else if (savedInstanceState?.getBoolean("p05_media_attempted") == true) {
            requestUi.value = MediaRequestUiState(attempted = true)
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
                mediaRequestUi = requestUi,
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
            // Denied and permanent Android/OEM recovery are P06/P07; false is never a grant.
            PermissionStatus.NotRequested
        }
    }

    private fun requestMediaPermission() {
        // Called ONLY by an explicit WF-03 Continue tap, not by onResume/bootstrap.
        val effect = gate.begin() ?: return
        requestUi.value = MediaRequestUiState(pending = true)
        val permission = requireNotNull(
            PermissionPlatformPolicy.manifestPermission(effect.capability, Build.VERSION.SDK_INT),
        )
        if (hasMediaPermission()) {
            // If already granted externally, still consume exactly one verified action
            // and never show a redundant native dialog.
            val action = gate.complete(NativePermissionObservation(
                granted = hasMediaPermission(), requestedBefore = true,
                shouldShowRationale = false,
            ))
            requestUi.value = MediaRequestUiState(attempted = true)
            refreshMediaPermission()
            if (action == BlockedAction.StartScan && hasMediaPermission()) {
                (application as MusicCopilotApplication).bindings
                    .scanAfterExplicitMediaGrant(applicationContext, permission)
            }
        } else {
            try {
                requestLauncher.launch(permission)
            } catch (failure: RuntimeException) {
                // A failed launcher cannot claim grant or start a scan.
                gate.abandon()
                requestUi.value = MediaRequestUiState(attempted = true, denied = true)
                refreshMediaPermission()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("p05_native_media_request_pending", gate.awaitingPlatformResult)
        outState.putBoolean("p05_media_attempted", requestUi.value.attempted)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        refreshMediaPermission()
        (application as MusicCopilotApplication).bindings.onForeground()
    }

    @Composable
    private fun RootComposeShell(
        bindings: ApplicationBindings,
        onExit: () -> Unit,
        onRefreshPermission: () -> Unit,
        onRequestMediaPermission: () -> Unit,
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
                mediaRequestUi = mediaRequest,
                onFirstUseComplete = bindings::onForeground,
                onExit = onExit,
            )
        }
    }
}
