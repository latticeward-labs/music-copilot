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
import com.latticeward.musiccopilot.core.application.library.LibraryReadinessResolver
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
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeGate
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.permission.VoiceProviderCapability
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceHandoffPolicy
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
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
    private var observedMicrophoneGrant = false
    private val voicePracticeGate = VoicePracticeGate(Build.VERSION.SDK_INT)
    private val voicePracticeUi = MutableStateFlow(VoicePracticeStage.Idle)
    // P07: separate transient Driving presentation, reusing the EXACT same
    // VoicePracticeGate and Android RequestPermission launcher. No second
    // microphone permission subsystem or saved OS grant.
    private val drivingMicUi = MutableStateFlow(VoicePracticeStage.Idle)
    private val microphoneAccess = MutableStateFlow(false)
    // P09 Nearby Devices permission is never saved as DataStore consent;
    // freshly recheck OS on every resume and explicit paired-device action.
    private val bluetoothConnectAccess = MutableStateFlow(Build.VERSION.SDK_INT < 31)
    private var drivingMicPermissionOrigin = false
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

    private val microphoneLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Never trust ActivityResult Boolean as OS truth.
            val freshGrant = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            val provider = if (freshGrant)
                (application as MusicCopilotApplication).bindings.onDeviceVoicePracticeCapability()
            else VoiceProviderCapability.ProviderUnavailable
            microphoneAccess.value = freshGrant
            val stage = voicePracticeGate.androidResult(freshGrant, provider)
            if (drivingMicPermissionOrigin) {
                // Permission result NEVER auto-starts capture. The user must
                // explicitly tap Mic again after this stage is displayed.
                drivingMicUi.value = stage
                voicePracticeUi.value = VoicePracticeStage.Idle
                drivingMicPermissionOrigin = false
            } else {
                voicePracticeUi.value = stage
            }
            // No VoiceSessionCoordinator.start() from an OS permission result.
        }

    private val bluetoothConnectLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Native result updates permission presentation only. No automatic
            // enumerate/scan/Connect/Driving/playback/Voice side effects.
            bluetoothConnectAccess.value = hasBluetoothConnectPermission()
        }

    private fun hasBluetoothConnectPermission(): Boolean =
        Build.VERSION.SDK_INT < 31 ||
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED

    /** Only called after tapping the Bluetooth request button in Settings. */
    private fun onRequestBluetoothConnectPermission() {
        if (hasBluetoothConnectPermission()) {
            bluetoothConnectAccess.value = true
            return
        }
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                bluetoothConnectLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
            } catch (_: RuntimeException) {
                bluetoothConnectAccess.value = hasBluetoothConnectPermission()
            }
        }
    }

    /** Separate from media permission's retry gate; explicit Settings button. */
    private fun onOpenBluetoothPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            ("package:$packageName").toUri())
        try { startActivity(intent) } catch (_: RuntimeException) { /* app stays usable */ }
    }

    private fun onDrivingMicPermissionTap() {
        // Called only by an explicit Mic tap while Driving is the NavHost
        // target. A rationale is shown BEFORE any Android OS request.
        if (drivingMicPermissionOrigin || voicePracticeGate.awaitingAndroidResult) return
        if (voicePracticeGate.microphoneTapped()) {
            drivingMicPermissionOrigin = true
            drivingMicUi.value = voicePracticeGate.stage
            voicePracticeUi.value = VoicePracticeStage.Idle
        }
    }

    private fun onDrivingMicPermissionContinue() {
        if (!drivingMicPermissionOrigin || !voicePracticeGate.confirmRationale()) return
        drivingMicUi.value = voicePracticeGate.stage
        val alreadyGranted = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            microphoneAccess.value = true
            drivingMicUi.value = voicePracticeGate.androidResult(
                true, (application as MusicCopilotApplication).bindings.onDeviceVoicePracticeCapability()
            )
            drivingMicPermissionOrigin = false
        } else {
            try { microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
            catch (_: RuntimeException) {
                voicePracticeGate.requestLaunchFailed()
                drivingMicUi.value = voicePracticeGate.stage
                drivingMicPermissionOrigin = false
            }
        }
    }

    private fun onDrivingMicPermissionDismiss() {
        // Do not touch an unrelated onboarding permission/education gate.
        if (!drivingMicPermissionOrigin &&
            drivingMicUi.value == VoicePracticeStage.Idle) return
        drivingMicPermissionOrigin = false
        drivingMicUi.value = VoicePracticeStage.Idle
        voicePracticeGate.manualOnly()
    }

    private fun onExplicitPracticeTap() {
        if (voicePracticeGate.practiceTapped())
            voicePracticeUi.value = voicePracticeGate.stage
    }

    private fun onPracticeRationaleContinue() {
        if (!voicePracticeGate.confirmRationale()) return
        voicePracticeUi.value = voicePracticeGate.stage
        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED) {
            voicePracticeUi.value = voicePracticeGate.androidResult(
                true, (application as MusicCopilotApplication).bindings.onDeviceVoicePracticeCapability()
            )
        } else {
            try { microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }
            catch (_: RuntimeException) {
                voicePracticeGate.requestLaunchFailed()
                voicePracticeUi.value = voicePracticeGate.stage
            }
        }
    }

    private fun onPracticeManualOnly() {
        voicePracticeGate.manualOnly()
        voicePracticeUi.value = voicePracticeGate.stage
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
        val nativeMicPending =
            savedInstanceState?.getBoolean("p09_native_microphone_request_pending") == true
        val pendingDrivingOrigin = nativeMicPending &&
            savedInstanceState?.getBoolean("i9_native_driving_mic_origin") == true
        if (nativeMicPending) {
            voicePracticeGate.restorePending()
        } else {
            // Restore only harmless onboarding presentation; never a grant
            // or an executable microphone command.
            voicePracticeGate.restorePresentation(
                savedInstanceState?.getString("p11_voice_practice_stage"),
            )
        }
        drivingMicPermissionOrigin = pendingDrivingOrigin
        drivingMicUi.value = if (pendingDrivingOrigin)
            VoicePracticeStage.AwaitingAndroid else VoicePracticeStage.Idle
        voicePracticeUi.value = if (pendingDrivingOrigin)
            VoicePracticeStage.Idle else voicePracticeGate.stage
        // ActivityResult may deliver after Activity recreation, but NEVER
        // launches recognition by itself. A fresh, explicit Mic tap is needed.
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
                voicePracticeUi = voicePracticeUi,
                drivingMicUi = drivingMicUi,
                microphoneAccess = microphoneAccess,
                bluetoothConnectAccess = bluetoothConnectAccess,
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
        outState.putBoolean("p09_native_microphone_request_pending", voicePracticeGate.awaitingAndroidResult)
        // One pending UI-origin bit is harmless; never persist grant, speech
        // session, recording intent, voice token or selected media.
        outState.putBoolean("i9_native_driving_mic_origin",
            drivingMicPermissionOrigin && voicePracticeGate.awaitingAndroidResult)
        // Enum presentation only. Never write actual OS grant, provider status,
        // media identity, voice transcript, or executable blocked action to Bundle.
        outState.putString("p11_voice_practice_stage", voicePracticeGate.stage.name)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        // Only a real transition away from Activity can complete Settings return.
        permissionUi.onActivityPaused()
    }

    override fun onStart() {
        super.onStart()
        // P10: register selected-audio profile observation only during the
        // foreground Activity lifecycle; no receiver when the app is stopped.
        (application as MusicCopilotApplication).bindings.onUiStarted()
    }

    override fun onStop() {
        super.onStop()
        // Do not keep the microphone recording after the app leaves the
        // foreground. Route/focus interruption may cancel earlier as well.
        val voice = (application as MusicCopilotApplication).bindings.voiceSessionCoordinator
        if (DrivingVoiceHandoffPolicy.shouldCancelOnExit(voice.state.value))
            voice.cancel(VoiceCancelReason.OriginInvalidated)
        // P08: detach the Activity-facing MediaController only. An ongoing
        // playback service is independent of Activity/Compose/Home/rotation/
        // screen-off. Do not call stopService(), player.pause() or stopSelf().
        (application as MusicCopilotApplication).bindings.onUiStopped()
    }

    override fun onResume() {
        super.onResume()
        val previouslyGranted = mediaAccess.value == PermissionStatus.Granted
        val freshGrant = hasMediaPermission()
        refreshMediaPermission()
        val knownIndexed = (application as MusicCopilotApplication).bindings
            .libraryRepository.index.value.status.let { status ->
                status == com.latticeward.musiccopilot.data.api.LibraryIndexStatus.Ready ||
                    status == com.latticeward.musiccopilot.data.api.LibraryIndexStatus.Empty ||
                    status == com.latticeward.musiccopilot.data.api.LibraryIndexStatus.Scanning
            }
        // One process-scoped owner handles permission revocation + cancellation.
        // A Settings return, onResume or rotation never requests a new scan.
        (application as MusicCopilotApplication).bindings
            .onForegroundMediaPermissionObserved(freshGrant)
        if (!freshGrant && (previouslyGranted || knownIndexed)) {
            permissionUi.onMediaRevoked()
        }
        permissionUi.onReturnFromSettings(NativePermissionObservation(
            granted = freshGrant, requestedBefore = permissionUi.requestsThisSession > 0,
            shouldShowRationale = shouldShowRequestPermissionRationale(mediaPermissionName()),
            previouslyGranted = previouslyGranted && !freshGrant,
        ))
        // P11: an old denial result is not authoritative after a fresh OS grant.
        // Clearing stale UI never authorizes or triggers an automatic scan.
        permissionUi.reconcileFreshAndroidGrant(freshGrant)
        val freshMic = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        val voiceState = (application as MusicCopilotApplication).bindings.voiceSessionCoordinator.state.value
        if (!freshMic && (observedMicrophoneGrant ||
                voiceState != com.latticeward.musiccopilot.voice.api.VoiceSessionState.Idle)) {
            (application as MusicCopilotApplication).bindings.voiceSessionCoordinator.cancel(
                com.latticeward.musiccopilot.voice.api.VoiceCancelReason.PermissionLost,
            )
        }
        observedMicrophoneGrant = freshMic
        microphoneAccess.value = freshMic
        // Never enumerate paired devices on resume/permission result. Settings
        // explicitly asks the user to Refresh after OS permission changes.
        bluetoothConnectAccess.value = hasBluetoothConnectPermission()
        voicePracticeGate.recheck(freshMic)
        if (!freshMic && drivingMicUi.value == VoicePracticeStage.ReadyForVoiceHandoff) {
            drivingMicUi.value = VoicePracticeStage.PermissionDenied
        }
        voicePracticeUi.value = voicePracticeGate.stage
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
        voicePracticeUi: kotlinx.coroutines.flow.StateFlow<VoicePracticeStage>,
        drivingMicUi: kotlinx.coroutines.flow.StateFlow<VoicePracticeStage>,
        microphoneAccess: kotlinx.coroutines.flow.StateFlow<Boolean>,
        bluetoothConnectAccess: kotlinx.coroutines.flow.StateFlow<Boolean>,
    ) {
        val library by bindings.libraryRepository.index.collectAsStateWithLifecycle()
        val playbackConnection by bindings.playbackGateway.connectionState.collectAsStateWithLifecycle()
        val playbackSnapshot by bindings.playbackGateway.snapshot.collectAsStateWithLifecycle()
        val preferences by bindings.preferencesRepository.preferences.collectAsStateWithLifecycle()
        val preferencesLoad by bindings.preferencesRepository.loadState.collectAsStateWithLifecycle()
        val storage by bindings.storageStartState.collectAsStateWithLifecycle()
        val permission by mediaAccess.collectAsStateWithLifecycle()
        val mediaRequest by mediaRequestUi.collectAsStateWithLifecycle()
        val practice by voicePracticeUi.collectAsStateWithLifecycle()
        val drivingMicStage by drivingMicUi.collectAsStateWithLifecycle()
        val micGranted by microphoneAccess.collectAsStateWithLifecycle()
        val btGranted by bluetoothConnectAccess.collectAsStateWithLifecycle()
        val voiceState by bindings.voiceSessionCoordinator.state.collectAsStateWithLifecycle()

        // Permission-aware readiness is computed from the same OS observation
        // and repository revision for navigation AND the visible Library status.
        // A migrated Room row or stale cached Ready is never proof of access.
        val verifiedLibraryStatus = LibraryReadinessResolver.verifiedStatus(
            permission == PermissionStatus.Granted, library,
        )
        val evidence = BootstrapEvidence(
            preferencesLoaded = preferencesLoad == PreferencesLoadState.Ready,
            migrationsVerified = storage == StorageStartState.Ready,
            startupFailed = preferencesLoad == PreferencesLoadState.Failed ||
                storage == StorageStartState.Failed,
            onboardingComplete = preferences.onboardingComplete,
            mediaPermission = permission,
            libraryStatus = verifiedLibraryStatus,
        )
        val route = BootstrapRoutePolicy.project(evidence)
        MusicCopilotTheme {
            AppNavHost(
                route = route,
                readiness = RootNavigationStateHolder().readiness(verifiedLibraryStatus, playbackConnection),
                permission = permission,
                libraryRepository = bindings.libraryRepository,
                mediaSelection = bindings.mediaSelection,
                libraryIndex = library,
                playbackConnection = playbackConnection,
                playbackSnapshot = playbackSnapshot,
                playbackGateway = bindings.playbackGateway,
                preferencesRepository = bindings.preferencesRepository,
                remoteSpeechDecision = bindings.remoteSpeechDecision(),
                onRevokeCloudSpeechConsent = bindings::revokeCloudSpeechConsent,
                bluetoothConnectGranted = btGranted,
                onRequestBluetoothConnectPermission = ::onRequestBluetoothConnectPermission,
                onOpenBluetoothPermissionSettings = ::onOpenBluetoothPermissionSettings,
                onRetry = bindings::retryFirstLaunch,
                onRefreshPermission = onRefreshPermission,
                onRetryLibraryScan = bindings::retryLibraryAfterError,
                onRescanLibrary = bindings::rescanLibraryOnUserRequest,
                onRequestMediaPermission = onRequestMediaPermission,
                onContinueGranted = onContinueGranted,
                onRetryMediaPermission = onRetryMediaPermission,
                onExitPermissionResult = onExitPermissionResult,
                onOpenAndroidSettings = onOpenAndroidSettings,
                mediaRequestUi = mediaRequest,
                voicePracticeStage = practice,
                onPracticeVoice = ::onExplicitPracticeTap,
                onPracticeRationaleContinue = ::onPracticeRationaleContinue,
                onPracticeManualOnly = ::onPracticeManualOnly,
                voiceSessionCoordinator = bindings.voiceSessionCoordinator,
                voiceSessionState = voiceState,
                microphoneGranted = micGranted,
                localVoiceProviderSupported = Build.VERSION.SDK_INT >= 31,
                localVoiceProviderAvailable = micGranted &&
                    bindings.onDeviceVoicePracticeCapability() ==
                        VoiceProviderCapability.OnDeviceAvailable,
                freshCanStartDrivingVoice = {
                    checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED &&
                        bindings.onDeviceVoicePracticeCapability() ==
                            VoiceProviderCapability.OnDeviceAvailable
                },
                drivingMicPermissionStage = drivingMicStage,
                onDrivingMicPermissionTap = ::onDrivingMicPermissionTap,
                onDrivingMicPermissionContinue = ::onDrivingMicPermissionContinue,
                onDrivingMicPermissionDismiss = ::onDrivingMicPermissionDismiss,
                onFirstUseComplete = {
                    onPracticeManualOnly()
                    bindings.onForeground()
                },
                onExit = onExit,
            )
        }
    }
}
