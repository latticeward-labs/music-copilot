package com.latticeward.musiccopilot.app.navigation

import androidx.activity.compose.BackHandler
import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.collect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import android.os.SystemClock
import kotlinx.coroutines.flow.collect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoute
import com.latticeward.musiccopilot.core.application.driving.DrivingPresentationPolicy
import com.latticeward.musiccopilot.core.application.driving.DrivingViewState
import com.latticeward.musiccopilot.core.application.driving.DrivingMicCapability
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceAvailability
import com.latticeward.musiccopilot.core.application.driving.DrivingVoiceHandoffPolicy
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.feature.driving.DrivingModeScreen
import com.latticeward.musiccopilot.core.application.playback.LibraryMediaSelectionUseCase
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.PreferencesLoadState
import com.latticeward.musiccopilot.app.platform.bluetooth.AndroidPairedAudioDeviceSource
import com.latticeward.musiccopilot.app.platform.bluetooth.SelectedBluetoothConnectionCoordinator
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionState
import com.latticeward.musiccopilot.feature.driving.BluetoothEntrySuggestionBanner
import com.latticeward.musiccopilot.app.platform.bluetooth.SelectedBluetoothConnectionCoordinator
import com.latticeward.musiccopilot.core.application.bluetooth.VerifiedAudioReconnectEvent
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothEntrySuggestionPolicy
import com.latticeward.musiccopilot.core.application.bluetooth.BluetoothEntrySuggestionState
import com.latticeward.musiccopilot.core.application.bluetooth.SelectedAudioConnectionState
import com.latticeward.musiccopilot.feature.driving.BluetoothEntrySuggestionBanner
import com.latticeward.musiccopilot.feature.settings.bluetooth.BluetoothSelectionViewModel
import com.latticeward.musiccopilot.feature.settings.bluetooth.BluetoothPreferenceSettingsSection
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.feature.search.LocalSearchScreen
import com.latticeward.musiccopilot.feature.search.LocalSearchViewModel
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.feature.player.PlayerViewModel
import com.latticeward.musiccopilot.feature.player.FullPlayerScreen
import com.latticeward.musiccopilot.feature.library.LibraryBrowseScreen
import com.latticeward.musiccopilot.feature.library.LibraryBrowseViewModel
import com.latticeward.musiccopilot.feature.onboarding.FirstUseScreen
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.feature.permission.MediaPermissionOutcomeScreen
import com.latticeward.musiccopilot.feature.permission.MediaPermissionRationaleScreen
import com.latticeward.musiccopilot.feature.permission.MediaRequestUiState

/** P03: single NavHost, Bootstrap always entry, no second playback/Voice authority. */
@Composable
fun AppNavHost(
    route: BootstrapRoute,
    readiness: FoundationReadiness,
    permission: PermissionStatus,
    libraryRepository: LibraryRepository,
    mediaSelection: LibraryMediaSelectionUseCase,
    libraryIndex: LibraryIndexSnapshot,
    playbackConnection: PlaybackConnectionState,
    playbackSnapshot: PlaybackSnapshot,
    playbackGateway: PlaybackGateway,
    preferencesRepository: PreferencesRepository,
    remoteSpeechDecision: CloudSpeechGateDecision,
    onRevokeCloudSpeechConsent: () -> Unit,
    bluetoothConnectGranted: Boolean = false,
    selectedBluetoothConnection: SelectedBluetoothConnectionCoordinator? = null,
    onRequestBluetoothConnectPermission: () -> Unit = {},
    onOpenBluetoothPermissionSettings: () -> Unit = {},
    selectedBluetoothConnection: SelectedBluetoothConnectionCoordinator? = null,
    freshBluetoothConnectPermission: () -> Boolean = { false },
    onRetry: () -> Unit,
    onRefreshPermission: () -> Unit,
    onRetryLibraryScan: () -> Unit,
    onRescanLibrary: () -> Unit,
    onRequestMediaPermission: () -> Unit,
    onContinueGranted: () -> Unit,
    onRetryMediaPermission: () -> Unit,
    onExitPermissionResult: () -> Unit,
    onOpenAndroidSettings: () -> Unit,
    mediaRequestUi: MediaRequestUiState,
    voicePracticeStage: VoicePracticeStage,
    onPracticeVoice: () -> Unit,
    onPracticeRationaleContinue: () -> Unit,
    onPracticeManualOnly: () -> Unit,
    onFirstUseComplete: () -> Unit,
    onExit: () -> Unit,
    voiceSessionCoordinator: VoiceSessionCoordinator? = null,
    voiceSessionState: VoiceSessionState = VoiceSessionState.Idle,
    microphoneGranted: Boolean = false,
    localVoiceProviderSupported: Boolean = false,
    localVoiceProviderAvailable: Boolean = false,
    freshCanStartDrivingVoice: () -> Boolean = { false },
    drivingMicPermissionStage: VoicePracticeStage = VoicePracticeStage.Idle,
    onDrivingMicPermissionTap: () -> Unit = {},
    onDrivingMicPermissionContinue: () -> Unit = {},
    onDrivingMicPermissionDismiss: () -> Unit = {},
) {
    val navController = rememberNavController()
    // A single navigation-scoped ViewModel observes the existing Gateway.
    // It never creates an ExoPlayer/MediaSession or replaces a queue.
    val playerModel: PlayerViewModel = viewModel(
        key = "issue7-service-player",
        factory = remember(playbackGateway, libraryRepository) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    check(modelClass == PlayerViewModel::class.java)
                    return PlayerViewModel(playbackGateway, libraryRepository) as T
                }
            }
        },
    )
    val playerUi by playerModel.ui.collectAsStateWithLifecycle()
    // P09: the existing one Settings route owns only preference UI.
    // Platform enumerator never scans, connects or observes BT events (P10).
    val appContext = LocalContext.current.applicationContext
    val pairedAudioSource = remember(appContext) {
        AndroidPairedAudioDeviceSource(appContext)
    }
    val bluetoothModel: BluetoothSelectionViewModel = viewModel(
        key = "issue9-settings-bluetooth-selection",
        factory = remember(preferencesRepository, pairedAudioSource) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    check(modelClass == BluetoothSelectionViewModel::class.java)
                    return BluetoothSelectionViewModel(
                        preferencesRepository, pairedAudioSource
                    ) as T
                }
            }
        },
    )
    val storedBluetoothPreferences by
        preferencesRepository.preferences.collectAsStateWithLifecycle()
    val bluetoothStorageState by
        preferencesRepository.loadState.collectAsStateWithLifecycle()
    // P11: an invitation, not a destination or a player command. No saved
    // state/persistent prompt survives process death or background replay.
    val bluetoothSuggestionModel: BluetoothEntrySuggestionViewModel = viewModel(
        key = "issue9-bluetooth-foreground-invitation",
    )
    val selectedAudioEvidence = selectedBluetoothConnection?.state
        ?.collectAsStateWithLifecycle()?.value ?: SelectedAudioConnectionState()
    val bluetoothForeground = selectedBluetoothConnection?.uiForeground
        ?.collectAsStateWithLifecycle()?.value == true
    // Activity-scoped, configuration-retained, but never SavedState-restored.
    // No automatic Driving entry on cold launch/process restart/BT reconnect.
    val drivingNavigation: DrivingNavigationViewModel = viewModel(
        key = "issue9-root-driving-navigation",
    )
    var manualFallback by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var playerOpen by rememberSaveable { mutableStateOf(false) }
    var pendingSearchGroup by remember { mutableStateOf<LibraryBrowseEntry?>(null) }
    var replayingEducation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permission) {
        if (permission == PermissionStatus.Granted) {
            manualFallback = false
        } else {
            // OS denial/revocation owns recovery; never expose old Search
            // rows when no longer authorized to browse local audio.
            searchOpen = false
            playerOpen = false
            pendingSearchGroup = null
        }
    }
    val actualRoute = if (route == BootstrapRoute.PermissionRecovery && manualFallback) {
        BootstrapRoute.HomeLibrary
    } else route
    // System/permission and first-use routes always override a Driving session.
    // The Voice overlay is deliberately not a NavHost destination.
    LaunchedEffect(
        actualRoute, mediaRequestUi.outcome, replayingEducation, settingsOpen, permission,
        voiceSessionState,
    ) {
        if (actualRoute != BootstrapRoute.HomeLibrary ||
            mediaRequestUi.outcome != MediaPermissionOutcome.None ||
            replayingEducation || settingsOpen ||
            permission != PermissionStatus.Granted
        ) {
            if (voiceSessionCoordinator != null &&
                DrivingVoiceHandoffPolicy.shouldCancelOnExit(voiceSessionState)
            ) voiceSessionCoordinator.cancel(VoiceCancelReason.OriginInvalidated)
            if (drivingMicPermissionStage != VoicePracticeStage.Idle)
                onDrivingMicPermissionDismiss()
            drivingNavigation.clearForSystemBoundary()
        }
    }
    val target = when {
        actualRoute == BootstrapRoute.Preparing || actualRoute == BootstrapRoute.RetryStartup ->
            Destination.Bootstrap
        actualRoute == BootstrapRoute.Onboarding -> Destination.Onboarding
        replayingEducation -> Destination.Onboarding
        mediaRequestUi.outcome != MediaPermissionOutcome.None ->
            Destination.PermissionRecovery
        actualRoute == BootstrapRoute.PermissionRecovery -> Destination.PermissionRecovery
        settingsOpen -> Destination.Settings
        drivingNavigation.state.active && actualRoute == BootstrapRoute.HomeLibrary ->
            Destination.Driving
        playerOpen && actualRoute == BootstrapRoute.HomeLibrary -> Destination.FullPlayer
        searchOpen && actualRoute == BootstrapRoute.HomeLibrary ->
            Destination.Search
        else -> Destination.Library
    }
    // P11 suggestions are one-use, transient and never SavedState-restored.
    // Activity foreground/profile listener is P10. P11 only observes its
    // verified event stream while STARTED; no background app surface is made.
    val lifecycleOwner = LocalLifecycleOwner.current
    var btOffer by remember { mutableStateOf(BluetoothEntrySuggestionState()) }
    val liveConnection = selectedBluetoothConnection?.state
        ?.collectAsStateWithLifecycle()?.value ?: SelectedAudioConnectionState()
    val uiForeground = selectedBluetoothConnection?.uiForeground
        ?.collectAsStateWithLifecycle()?.value ?: false
    val selectionEpoch = selectedBluetoothConnection?.currentSelectionEpoch ?: -1L
    val canShowBluetoothOffer = selectedBluetoothConnection != null &&
        uiForeground && bluetoothConnectGranted &&
        bluetoothStorageState == PreferencesLoadState.Ready &&
        storedBluetoothPreferences.selectedBluetoothHint != null &&
        actualRoute == BootstrapRoute.HomeLibrary &&
        permission == PermissionStatus.Granted &&
        mediaRequestUi.outcome == MediaPermissionOutcome.None &&
        !settingsOpen && !searchOpen && !replayingEducation &&
        !drivingNavigation.state.active &&
        voiceSessionState == VoiceSessionState.Idle &&
        drivingMicPermissionStage == VoicePracticeStage.Idle &&
        (target == Destination.Library || target == Destination.FullPlayer)

    val onVerifiedBluetoothReconnect by rememberUpdatedState<
        (VerifiedAudioReconnectEvent) -> Unit
    >(newValue = { event ->
        // Ignore all events received on the wrong surface, after a changed
        // preference or from a lifecycle/permission-stale Android callback.
        val validAtReceipt = canShowBluetoothOffer &&
            selectedBluetoothConnection?.isUnclaimedOfferCurrent(event) == true
        btOffer = BluetoothEntrySuggestionPolicy.onVerifiedReconnect(
            old = btOffer,
            event = event,
            evidence = selectedBluetoothConnection?.state?.value
                ?: SelectedAudioConnectionState(),
            currentSelectionEpoch = selectedBluetoothConnection?.currentSelectionEpoch ?: -1L,
            surfaceAllowed = validAtReceipt,
            elapsedMs = SystemClock.elapsedRealtime(),
        )
    })
    LaunchedEffect(selectedBluetoothConnection, lifecycleOwner) {
        // No event replay or collector when app is in background.
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            selectedBluetoothConnection?.verifiedReconnects?.collect { event ->
                onVerifiedBluetoothReconnect(event)
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                btOffer = BluetoothEntrySuggestionPolicy.invalidate(btOffer)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(canShowBluetoothOffer, liveConnection, selectionEpoch) {
        // Clear immediately on route/selection/connection/permission changes.
        btOffer = BluetoothEntrySuggestionPolicy.reconcile(
            old = btOffer,
            evidence = liveConnection,
            currentSelectionEpoch = selectionEpoch,
            surfaceAllowed = canShowBluetoothOffer,
        )
    }
    val visibleBluetoothOffer = btOffer.pending?.takeIf { event ->
        canShowBluetoothOffer &&
            selectedBluetoothConnection?.isUnclaimedOfferCurrent(event) == true
    }
    val notNowBluetooth: () -> Unit = {
        btOffer = BluetoothEntrySuggestionPolicy.notNow(
            btOffer, SystemClock.elapsedRealtime()
        )
    }
    val enterBluetoothDriving: () -> Unit = {
        val event = btOffer.pending
        val origin = if (playerOpen) Destination.FullPlayer else Destination.Library
        // A click can occur after OS permission is revoked or a disconnect.
        // Recheck source, route, full priority stack and one-use event now.
        if (event != null && canShowBluetoothOffer &&
            (origin == Destination.Library || origin == Destination.FullPlayer) &&
            selectedBluetoothConnection?.claimOffer(event) == true
        ) {
            drivingNavigation.enterFrom(origin, allowedByAppRoute = true)
            btOffer = BluetoothEntrySuggestionPolicy.accepted(btOffer)
        } else {
            btOffer = BluetoothEntrySuggestionPolicy.invalidate(btOffer)
        }
        // Critically: no mediaPlay, voice start, BT connect, player/queue
        // command, notification or background Activity launch.
    }

    // Only the existing Library and Full Player can host a small, nonmodal
    // invitation. No Search/Settings/permission/onboarding/Driving overlay.
    val invitationSurfaceAllowed =
        (target == Destination.Library || target == Destination.FullPlayer) &&
        actualRoute == BootstrapRoute.HomeLibrary &&
        permission == PermissionStatus.Granted &&
        mediaRequestUi.outcome == MediaPermissionOutcome.None &&
        !settingsOpen && !searchOpen && !replayingEducation &&
        !drivingNavigation.state.active &&
        bluetoothConnectGranted && bluetoothForeground &&
        bluetoothStorageState == PreferencesLoadState.Ready &&
        storedBluetoothPreferences.selectedBluetoothHint != null &&
        voiceSessionState == VoiceSessionState.Idle &&
        drivingMicPermissionStage == VoicePracticeStage.Idle
    // Keep a single foreground collector across nav/permission recompositions.
    // When a reconnect occurs, evaluate the LATEST surface and OS evidence,
    // not stale values captured at LaunchedEffect creation time.
    val latestInvitationSurface by rememberUpdatedState(invitationSurfaceAllowed)
    val latestBluetoothEvidence by rememberUpdatedState(selectedAudioEvidence)
    val latestBluetoothEpoch by rememberUpdatedState(
        selectedBluetoothConnection?.currentSelectionEpoch ?: -1L
    )
    LaunchedEffect(selectedBluetoothConnection, bluetoothSuggestionModel) {
        selectedBluetoothConnection?.verifiedReconnects?.collect { event ->
            bluetoothSuggestionModel.onReconnect(
                event = event,
                latest = latestBluetoothEvidence,
                selectionEpoch = latestBluetoothEpoch,
                isAllowedSurface = latestInvitationSurface &&
                    selectedBluetoothConnection.isUnclaimedOfferCurrent(event),
                nowElapsedMs = SystemClock.elapsedRealtime(),
            )
        }
    }
    LaunchedEffect(invitationSurfaceAllowed, selectedAudioEvidence,
        selectedBluetoothConnection?.currentSelectionEpoch
    ) {
        bluetoothSuggestionModel.reconcile(
            latest = selectedAudioEvidence,
            selectionEpoch = selectedBluetoothConnection?.currentSelectionEpoch ?: -1L,
            isAllowedSurface = invitationSurfaceAllowed,
        )
        if (!bluetoothForeground) bluetoothSuggestionModel.invalidate()
    }
    val offeredReconnect = bluetoothSuggestionModel.state.pending?.takeIf { event ->
        invitationSurfaceAllowed &&
            selectedBluetoothConnection?.isUnclaimedOfferCurrent(event) == true
    }
    val declineBluetoothOffer: () -> Unit = {
        bluetoothSuggestionModel.notNow(SystemClock.elapsedRealtime())
    }
    val acceptBluetoothOffer: (Destination) -> Unit = { origin ->
        val event = bluetoothSuggestionModel.state.pending
        val userIsOnApprovedOrigin = origin == target &&
            (origin == Destination.Library || origin == Destination.FullPlayer)
        val freshSafe = userIsOnApprovedOrigin &&
            invitationSurfaceAllowed && freshBluetoothConnectPermission() &&
            mediaRequestUi.outcome == MediaPermissionOutcome.None &&
            permission == PermissionStatus.Granted &&
            voiceSessionState == VoiceSessionState.Idle &&
            !drivingNavigation.state.active &&
            event != null && event == offeredReconnect &&
            selectedBluetoothConnection?.isUnclaimedOfferCurrent(event) == true
        if (freshSafe && event != null &&
            selectedBluetoothConnection?.claimOffer(event) == true
        ) {
            // Identical P03 in-app user-origin entry; never play, toggle
            // pause, change route, request mic, start another Activity or
            // jump into Driving from a background Bluetooth broadcast.
            drivingNavigation.enterFrom(origin, allowedByAppRoute = true)
            bluetoothSuggestionModel.accepted()
        } else bluetoothSuggestionModel.invalidate()
    }
    LaunchedEffect(target) {
        if (navController.currentBackStackEntry?.destination?.route != target.route) {
            // Return to an EXISTING destination when present. In particular,
            // Search -> Library must pop Search, not push a duplicate Library
            // above it and retain a private search query in its ViewModel.
            // Settings -> Search similarly restores only the active surface.
            // Bootstrap/PermissionRecovery remain system-state authoritative.
            val returnedToExisting = navController.popBackStack(
                target.route, inclusive = false,
            )
            if (!returnedToExisting) {
                navController.navigate(target.route) {
                    popUpTo(Destination.Bootstrap.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
    NavHost(navController = navController, startDestination = Destination.Bootstrap.route) {
        composable(Destination.Bootstrap.route) {
            if (route == BootstrapRoute.RetryStartup) {
                FirstLaunchBoundary(
                    title = stringResource(R.string.startup_failure_title),
                    message = stringResource(R.string.startup_failure_body),
                    primary = stringResource(R.string.action_retry),
                    onPrimary = onRetry,
                )
            } else {
                FoundationBootstrapScreen(readiness)
            }
        }
        composable(Destination.Onboarding.route) {
            FirstUseScreen(
                repository = preferencesRepository,
                replay = replayingEducation,
                voicePracticeStage = voicePracticeStage,
                onPracticeVoice = onPracticeVoice,
                onPracticeRationaleContinue = onPracticeRationaleContinue,
                onPracticeManualOnly = onPracticeManualOnly,
                onComplete = onFirstUseComplete,
                onExit = {
                    if (replayingEducation) {
                        replayingEducation = false
                        settingsOpen = true
                    } else {
                        onExit()
                    }
                },
            )
        }
        composable(Destination.PermissionRecovery.route) {
            if (mediaRequestUi.outcome == MediaPermissionOutcome.None) {
                MediaPermissionRationaleScreen(
                    request = mediaRequestUi,
                    onContinue = onRequestMediaPermission,
                    onNotNow = {
                        onExitPermissionResult()
                        manualFallback = true
                    },
                )
            } else {
                MediaPermissionOutcomeScreen(
                    outcome = mediaRequestUi.outcome,
                    revoked = mediaRequestUi.revoked,
                    onContinueGranted = onContinueGranted,
                    onTryAgain = {
                        onRetryMediaPermission()
                        manualFallback = false
                    },
                    onOpenSettings = onOpenAndroidSettings,
                    onNotNow = {
                        onExitPermissionResult()
                        manualFallback = true
                    },
                )
            }
        }
        composable(Destination.Library.route) {
            val browseModel: LibraryBrowseViewModel = viewModel(
                factory = remember(libraryRepository, mediaSelection) {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            check(modelClass == LibraryBrowseViewModel::class.java)
                            return LibraryBrowseViewModel(libraryRepository, mediaSelection) as T
                        }
                    }
                },
            )
            Column(modifier = Modifier.fillMaxSize()) {
                if (offeredReconnect != null) {
                    BluetoothEntrySuggestionBanner(
                        onEnter = { acceptBluetoothOffer(Destination.Library) },
                        onNotNow = declineBluetoothOffer,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
            LibraryBrowseScreen(
                model = browseModel,
                index = libraryIndex,
                readiness = readiness,
                permission = permission,
                playbackConnection = playbackConnection,
                playback = playbackSnapshot,
                onRefreshPermission = {
                    manualFallback = false
                    onRefreshPermission()
                },
                onRetryLibraryScan = onRetryLibraryScan,
                onRescanLibrary = onRescanLibrary,
                onSettings = { settingsOpen = true },
                onSearch = { searchOpen = true },
                playerUi = playerUi,
                onOpenPlayer = { playerOpen = true },
                onTogglePlayback = playerModel::togglePlayPause,
                onEnterDriving = {
                    drivingNavigation.enterFrom(
                        Destination.Library,
                        allowedByAppRoute = actualRoute == BootstrapRoute.HomeLibrary &&
                            permission == PermissionStatus.Granted &&
                            mediaRequestUi.outcome == MediaPermissionOutcome.None &&
                            !settingsOpen && !searchOpen && !replayingEducation &&
                            !playerOpen,
                    )
                },
                initialGroup = pendingSearchGroup,
                onGroupConsumed = { pendingSearchGroup = null },
                onExit = onExit,
            )
                } // body remains playable/searchable beneath small offer
            }
        }
        composable(Destination.Search.route) {
            val searchModel: LocalSearchViewModel = viewModel(
                factory = remember(libraryRepository) {
                    object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            check(modelClass == LocalSearchViewModel::class.java)
                            return LocalSearchViewModel(libraryRepository, mediaSelection) as T
                        }
                    }
                },
            )
            LocalSearchScreen(
                model = searchModel,
                index = libraryIndex,
                permission = permission,
                onBack = { searchOpen = false },
                onOpenGroup = { group ->
                    pendingSearchGroup = group
                    searchOpen = false
                },
                onSettings = { settingsOpen = true },
                playerUi = playerUi,
                onOpenPlayer = { playerOpen = true },
                onTogglePlayback = playerModel::togglePlayPause,
                onReviewAccess = {
                    searchOpen = false
                    manualFallback = false
                    onRefreshPermission()
                },
            )
        }
        composable(Destination.FullPlayer.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (offeredReconnect != null) {
                    BluetoothEntrySuggestionBanner(
                        onEnter = { acceptBluetoothOffer(Destination.FullPlayer) },
                        onNotNow = declineBluetoothOffer,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
            FullPlayerScreen(
                model = playerModel,
                state = playerUi,
                library = libraryRepository,
                canReadLocalMetadata = permission == PermissionStatus.Granted,
                onBack = { playerOpen = false },
                onEnterDriving = {
                    drivingNavigation.enterFrom(
                        Destination.FullPlayer,
                        allowedByAppRoute = actualRoute == BootstrapRoute.HomeLibrary &&
                            permission == PermissionStatus.Granted &&
                            mediaRequestUi.outcome == MediaPermissionOutcome.None &&
                            playerOpen && !settingsOpen && !replayingEducation,
                    )
                },
            )
                } // existing full player/Media3 controls remain unchanged
            }
        }
        composable(Destination.Driving.route) {
            // P07: exact application-scoped Voice owner; never a Voice NavHost
            // destination. Availability is strictly current OS grant +
            // explicitly local STT capability + active Driving origin.
            val availability = DrivingVoiceHandoffPolicy.availability(
                drivingActive = drivingNavigation.state.active &&
                    actualRoute == BootstrapRoute.HomeLibrary &&
                    permission == PermissionStatus.Granted,
                localProviderSupported = localVoiceProviderSupported,
                microphoneGranted = microphoneGranted,
                localProviderAvailable = localVoiceProviderAvailable,
                session = voiceSessionState,
            )
            var voiceStartRejected by remember { mutableStateOf(false) }
            val projected = DrivingPresentationPolicy.project(
                connection = playerUi.connection,
                snapshot = playerUi.snapshot,
                pending = playerUi.pending,
                micCapability = if (DrivingVoiceHandoffPolicy.canTapMic(availability) &&
                    voiceSessionCoordinator != null)
                    DrivingMicCapability.Available else DrivingMicCapability.Unavailable,
            )
            // Both Exit and Android Back have the same navigation-only result.
            // Exit never stops/pauses playback or executes a transport command.
            val exitDriving: () -> Unit = {
                if (voiceSessionCoordinator != null &&
                    DrivingVoiceHandoffPolicy.shouldCancelOnExit(voiceSessionCoordinator.state.value)
                ) voiceSessionCoordinator.cancel(VoiceCancelReason.DrivingExit)
                onDrivingMicPermissionDismiss()
                voiceStartRejected = false
                val origin = drivingNavigation.exit()
                // Explicitly restore the recorded, non-Driving origin, rather
                // than trusting independently changing Player UI flags.
                if (actualRoute == BootstrapRoute.HomeLibrary &&
                    mediaRequestUi.outcome == MediaPermissionOutcome.None &&
                    permission == PermissionStatus.Granted
                ) {
                    // CC-07 specifically requires Exit-to-Library when
                    // no current playable track remains, even if entry
                    // originally came from Full Player. Other states still
                    // restore their precise P03 recorded origin.
                    playerOpen = projected.view != DrivingViewState.NoTrack &&
                        origin == Destination.FullPlayer
                }
            }
            BackHandler(onBack = exitDriving)
            // P07 extends the existing WF-07 screen with a *same-route*
            // cancellable Voice overlay, never a second Voice destination.
            // Voice transport only runs through the application coordinator.
            DrivingModeScreen(
                state = projected,
                currentItem = playerUi.snapshot.currentItem,
                library = libraryRepository,
                canReadLocalMetadata = permission == PermissionStatus.Granted,
                onExit = exitDriving,
                lastError = playerUi.snapshot.lastError,
                requestFailed = playerUi.requestFailed,
                voiceState = if (DrivingVoiceHandoffPolicy.shouldCancelOnExit(voiceSessionState))
                    voiceSessionState else VoiceSessionState.Idle,
                voiceAvailability = availability,
                voicePermissionStage = drivingMicPermissionStage,
                voiceStartRejected = voiceStartRejected,
                onVoicePermissionContinue = onDrivingMicPermissionContinue,
                onVoicePermissionCancel = onDrivingMicPermissionDismiss,
                onVoiceCancel = {
                    if (voiceSessionCoordinator != null &&
                        DrivingVoiceHandoffPolicy.shouldCancelOnExit(
                            voiceSessionCoordinator.state.value
                        )
                    ) voiceSessionCoordinator.cancel(VoiceCancelReason.UserBack)
                    voiceStartRejected = false
                },
                onVoiceRetry = {
                    if (drivingNavigation.state.active &&
                        actualRoute == BootstrapRoute.HomeLibrary &&
                        permission == PermissionStatus.Granted &&
                        voiceSessionState is VoiceSessionState.Failed &&
                        voiceSessionState.origin == VoiceOrigin.Driving &&
                        voiceSessionState.canRetry &&
                        freshCanStartDrivingVoice()
                    ) {
                        voiceStartRejected = voiceSessionCoordinator?.retry() !is AppResult.Success
                    } else voiceStartRejected = true
                },
                onMic = {
                    if (drivingNavigation.state.active &&
                        actualRoute == BootstrapRoute.HomeLibrary &&
                        permission == PermissionStatus.Granted &&
                        mediaRequestUi.outcome == MediaPermissionOutcome.None &&
                        !settingsOpen && !replayingEducation
                    ) {
                        when (availability) {
                            DrivingVoiceAvailability.NeedsPermission -> onDrivingMicPermissionTap()
                            DrivingVoiceAvailability.Ready -> {
                                // Fresh OS+provider recheck at the actual tap;
                                // ActivityResult/Resume never auto-start voice.
                                if (freshCanStartDrivingVoice() &&
                                    voiceSessionCoordinator != null &&
                                    voiceSessionCoordinator.state.value == VoiceSessionState.Idle
                                ) {
                                    voiceStartRejected =
                                        voiceSessionCoordinator.start(VoiceOrigin.Driving) !is AppResult.Success
                                } else voiceStartRejected = true
                            }
                            else -> Unit
                        }
                    }
                },
                onTransport = { action ->
                    // Event-time source gate. A queued/stale tap after Exit,
                    // permission revocation or system recovery is discarded.
                    if (drivingNavigation.state.active &&
                        actualRoute == BootstrapRoute.HomeLibrary &&
                        permission == PermissionStatus.Granted &&
                        mediaRequestUi.outcome == MediaPermissionOutcome.None &&
                        !settingsOpen && !replayingEducation
                    ) playerModel.drivingTransport(action)
                },
            )
        }
        composable(Destination.Settings.route) {
            BackHandler {
                bluetoothModel.dismiss()
                settingsOpen = false
            }
            FirstLaunchBoundary(
                title = stringResource(R.string.first_use_settings),
                message = stringResource(R.string.first_use_replay_help),
                primary = stringResource(R.string.first_use_replay_action),
                onPrimary = { replayingEducation = true },
                secondary = stringResource(R.string.action_back),
                onSecondary = {
                    bluetoothModel.dismiss()
                    settingsOpen = false
                },
                extraContent = {
                    // WF-10 Settings Bluetooth suggestion P1. All actual
                    // selection stays outside Driving; no automatic entry.
                    BluetoothPreferenceSettingsSection(
                        model = bluetoothModel,
                        persistedToken = storedBluetoothPreferences.selectedBluetoothHint,
                        persistenceReady = bluetoothStorageState == PreferencesLoadState.Ready,
                        connectPermissionGranted = bluetoothConnectGranted,
                        onRequestConnectPermission = onRequestBluetoothConnectPermission,
                        onOpenAndroidPermissionSettings = onOpenBluetoothPermissionSettings,
                    )
                    Text(
                        stringResource(R.string.p10_notification_exempt_status),
                        modifier = Modifier.padding(top = MusicSpacing.lgDp),
                        style = MusicTextStyles.bodyLarge,
                    )
                    Text(
                        stringResource(R.string.p10_cloud_stt_disabled),
                        modifier = Modifier.padding(top = MusicSpacing.smDp),
                        style = MusicTextStyles.bodyLarge,
                    )
                    Text(
                        stringResource(
                            if (preferencesRepository.preferences.value.cloudSpeechConsent ==
                                CloudSpeechConsent.Revoked
                            ) R.string.p10_cloud_consent_revoked
                            else R.string.p10_cloud_consent_not_active
                        ),
                        modifier = Modifier.padding(top = MusicSpacing.smDp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    // A saved Granted flag alone cannot approve an unregistered remote provider.
                    if (remoteSpeechDecision != CloudSpeechGateDecision.DisabledNoApprovedAdapter) {
                        Text(stringResource(R.string.p10_remote_gate_unexpected))
                    }
                    OutlinedButton(
                        onClick = onRevokeCloudSpeechConsent,
                        modifier = Modifier.padding(top = MusicSpacing.smDp)
                            .heightIn(min = MusicSize.touchTargetMinDp),
                    ) {
                        Text(stringResource(R.string.p10_revoke_cloud_consent))
                    }
                },
            )
        }
    }
    if (visibleBluetoothOffer != null) {
        // In-route, nonmodal, no extra NavHost route, no system notification.
        // Underlying screen remains available outside this compact banner.
        BluetoothEntrySuggestionBanner(
            onEnter = enterBluetoothDriving,
            onNotNow = notNowBluetooth,
            modifier = Modifier.align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(MusicSpacing.mdDp),
        )
    }
    // Back dismisses this optional invitation instead of leaving the
    // current manual music screen. No player action accompanies Back.
    BackHandler(enabled = visibleBluetoothOffer != null) {
        notNowBluetooth()
    }
    } // Root Box enclosing existing NavHost and optional P1 banner
}

@Composable
internal fun FoundationBootstrapScreen(readiness: FoundationReadiness) {
    FirstLaunchBoundary(
        title = stringResource(R.string.app_name),
        message = stringResource(R.string.foundation_state_initializing),
    )
}

@Composable
private fun FirstLaunchBoundary(
    title: String,
    message: String,
    primary: String? = null,
    onPrimary: () -> Unit = {},
    secondary: String? = null,
    onSecondary: () -> Unit = {},
    extraContent: @Composable (() -> Unit)? = null,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(MusicSpacing.lgDp),
            verticalArrangement = Arrangement.Center,
        ) {
            // P12: Library/Settings/Bootstrap have a navigable TalkBack heading.
            Text(title, modifier = Modifier.semantics { heading() },
                color = MusicColors.textPrimary, style = MusicTextStyles.titleLarge)
            Text(
                message, modifier = Modifier.padding(top = MusicSpacing.smDp),
                color = MusicColors.textSecondary, style = MusicTextStyles.bodyLarge,
            )
            if (primary != null) {
                Button(onClick = onPrimary, modifier = Modifier
                    .padding(top = MusicSpacing.lgDp).heightIn(min = MusicSize.touchTargetMinDp)) {
                    Text(primary)
                }
            }
            if (secondary != null) {
                OutlinedButton(onClick = onSecondary, modifier = Modifier
                    .padding(top = MusicSpacing.smDp).heightIn(min = MusicSize.touchTargetMinDp)) {
                    Text(secondary)
                }
            }
            extraContent?.invoke()
        }
    }
}

private fun FoundationReadiness.messageResource(): Int = when (this) {
    FoundationReadiness.Initializing -> R.string.foundation_state_initializing
    FoundationReadiness.LibraryPermissionOrMediaUnavailable -> R.string.foundation_state_unavailable
    FoundationReadiness.IndexingLocalLibrary -> R.string.foundation_state_indexing
    FoundationReadiness.LocalLibraryEmpty -> R.string.foundation_state_empty
    FoundationReadiness.LocalLibraryReady -> R.string.foundation_state_ready
    FoundationReadiness.LocalLibraryRecoverableError -> R.string.foundation_state_recoverable
}
