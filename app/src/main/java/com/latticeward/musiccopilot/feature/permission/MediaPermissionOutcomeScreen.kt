package com.latticeward.musiccopilot.feature.permission

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R

/**
 * P06: three native-result projections, NOT OS permission authority.
 * The Android system dialog and Settings are NEVER Compose screens.
 */
enum class MediaPermissionOutcome { None, Granted, DeniedRequestable, SettingsOnly }

@Composable
fun MediaPermissionOutcomeScreen(
    outcome: MediaPermissionOutcome,
    onContinueGranted: () -> Unit,
    onTryAgain: () -> Unit,
    onOpenSettings: () -> Unit,
    onNotNow: () -> Unit,
) {
    if (outcome == MediaPermissionOutcome.None) return
    BackHandler(onBack = onNotNow)
    val heading = when (outcome) {
        MediaPermissionOutcome.Granted -> R.string.p06_granted_title
        MediaPermissionOutcome.DeniedRequestable -> R.string.p06_denied_title
        MediaPermissionOutcome.SettingsOnly -> R.string.p06_settings_only_title
        MediaPermissionOutcome.None -> error("Not a result")
    }
    val message = when (outcome) {
        MediaPermissionOutcome.Granted -> R.string.p06_granted_description
        MediaPermissionOutcome.DeniedRequestable -> R.string.p06_denied_description
        MediaPermissionOutcome.SettingsOnly -> R.string.p06_settings_only_description
        MediaPermissionOutcome.None -> error("Not a result")
    }
    val primary = when (outcome) {
        MediaPermissionOutcome.Granted -> R.string.action_continue
        MediaPermissionOutcome.DeniedRequestable -> R.string.p06_try_again
        MediaPermissionOutcome.SettingsOnly -> R.string.p06_open_settings
        MediaPermissionOutcome.None -> error("Not a result")
    }
    val onPrimary = when (outcome) {
        MediaPermissionOutcome.Granted -> onContinueGranted
        MediaPermissionOutcome.DeniedRequestable -> onTryAgain
        MediaPermissionOutcome.SettingsOnly -> onOpenSettings
        MediaPermissionOutcome.None -> onNotNow
    }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onNotNow, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.action_back))
                }
                Text(stringResource(R.string.media_permission_app_bar),
                    style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(48.dp))
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(stringResource(R.string.media_permission_label),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(heading),
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onPrimary,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(primary)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.action_not_now)) }
            if (outcome == MediaPermissionOutcome.SettingsOnly) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(stringResource(R.string.media_permission_system_note),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.media_permission_fresh_result),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
