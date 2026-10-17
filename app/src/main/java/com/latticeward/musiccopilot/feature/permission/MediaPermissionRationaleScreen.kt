package com.latticeward.musiccopilot.feature.permission

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CircularProgressIndicator
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
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome

/** Transient ActivityResult presentation only; NEVER authoritative OS-grant storage. */
data class MediaRequestUiState(
    val pending: Boolean = false,
    val attempted: Boolean = false,
    val denied: Boolean = false,
    val outcome: MediaPermissionOutcome = MediaPermissionOutcome.None,
    val revoked: Boolean = false,
)

/**
 * WF-03 / HF-03 media-only rationale. Android system dialog is NOT a Compose screen.
 * P05 owns explanation/one-shot native request; P06 owns full three-outcome recovery.
 */
@Composable
fun MediaPermissionRationaleScreen(
    request: MediaRequestUiState,
    onContinue: () -> Unit,
    onNotNow: () -> Unit,
) {
    BackHandler { if (!request.pending) onNotNow() }
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(
                    onClick = onNotNow,
                    enabled = !request.pending,
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.action_back)) }
                Text(
                    stringResource(R.string.media_permission_app_bar),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                stringResource(R.string.media_permission_label),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.media_rationale_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.media_rationale_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
            if (request.denied) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.media_permission_denied_hint),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onContinue,
                enabled = !request.pending,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.action_continue)) }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onNotNow,
                enabled = !request.pending,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.action_not_now)) }
            if (request.pending) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                    Text(
                        stringResource(R.string.media_permission_pending),
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.media_permission_system_note),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                stringResource(R.string.media_permission_fresh_result),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
