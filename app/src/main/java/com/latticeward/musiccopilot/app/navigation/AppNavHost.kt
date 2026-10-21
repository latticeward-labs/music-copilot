package com.latticeward.musiccopilot.app.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoute
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferencesRepository
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
    preferencesRepository: PreferencesRepository,
    remoteSpeechDecision: CloudSpeechGateDecision,
    onRevokeCloudSpeechConsent: () -> Unit,
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
) {
    val navController = rememberNavController()
    var manualFallback by rememberSaveable { mutableStateOf(false) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var replayingEducation by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permission) {
        if (permission == PermissionStatus.Granted) manualFallback = false
    }
    val actualRoute = if (route == BootstrapRoute.PermissionRecovery && manualFallback) {
        BootstrapRoute.HomeLibrary
    } else route
    val target = when {
        actualRoute == BootstrapRoute.Preparing || actualRoute == BootstrapRoute.RetryStartup ->
            Destination.Bootstrap
        actualRoute == BootstrapRoute.Onboarding -> Destination.Onboarding
        replayingEducation -> Destination.Onboarding
        settingsOpen -> Destination.Settings
        mediaRequestUi.outcome != MediaPermissionOutcome.None ->
            Destination.PermissionRecovery
        actualRoute == BootstrapRoute.PermissionRecovery -> Destination.PermissionRecovery
        else -> Destination.Library
    }
    LaunchedEffect(target) {
        if (navController.currentBackStackEntry?.destination?.route != target.route) {
            navController.navigate(target.route) {
                popUpTo(Destination.Bootstrap.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }
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
            BackHandler { onExit() }
            // Pure presentation policy; the repository/Android OS still own
            // actual scan validity, versioning and permission truth.
            val action = LibraryActionPolicy.nextAction(permission, readiness)
            FirstLaunchBoundary(
                title = stringResource(R.string.library_title),
                message = when (action) {
                    LibraryUiAction.ReviewAccess ->
                        stringResource(R.string.library_without_access)
                    else -> stringResource(readiness.messageResource())
                },
                primary = when (action) {
                    LibraryUiAction.None -> null
                    LibraryUiAction.ReviewAccess ->
                        stringResource(R.string.action_review_access)
                    LibraryUiAction.StartScan ->
                        stringResource(R.string.p08_scan_local_music)
                    LibraryUiAction.RetryScan ->
                        stringResource(R.string.action_retry)
                    LibraryUiAction.Rescan ->
                        stringResource(R.string.p08_rescan_local_music)
                },
                onPrimary = {
                    when (action) {
                        LibraryUiAction.None -> Unit
                        LibraryUiAction.ReviewAccess -> {
                            manualFallback = false
                            onRefreshPermission()
                        }
                        LibraryUiAction.StartScan,
                        LibraryUiAction.Rescan -> onRescanLibrary()
                        LibraryUiAction.RetryScan -> onRetryLibraryScan()
                    }
                },
                secondary = stringResource(R.string.first_use_settings),
                onSecondary = { settingsOpen = true },
            )
        }
        composable(Destination.Settings.route) {
            BackHandler { settingsOpen = false }
            FirstLaunchBoundary(
                title = stringResource(R.string.first_use_settings),
                message = stringResource(R.string.first_use_replay_help),
                primary = stringResource(R.string.first_use_replay_action),
                onPrimary = { replayingEducation = true },
                secondary = stringResource(R.string.action_back),
                onSecondary = { settingsOpen = false },
                extraContent = {
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
