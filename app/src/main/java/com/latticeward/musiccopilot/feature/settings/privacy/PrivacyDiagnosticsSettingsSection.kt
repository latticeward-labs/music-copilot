package com.latticeward.musiccopilot.feature.settings.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsRevocationReport
import com.latticeward.musiccopilot.core.application.privacy.LocalClearCategoryResult
import com.latticeward.musiccopilot.core.application.privacy.LocalClearOverall
import com.latticeward.musiccopilot.core.application.privacy.LocalPrivacyClearReport
import com.latticeward.musiccopilot.core.application.privacy.LocalPrivacyClearSelection
import com.latticeward.musiccopilot.core.application.privacy.ShortHistoryAvailability
import com.latticeward.musiccopilot.data.api.DiagnosticsConsent
import androidx.compose.ui.res.stringResource

/**
 * Issue #12 P12; approved UF-12 and WF-10 source-owned Settings surface.
 *
 * DOES NOT grant optional diagnostics in current production: the required
 * notice, positive retention schedule, collector/provider and Legal approval
 * are still pending. The current on-device speech and separate cloud-STT
 * consent surface are untouched.
 *
 * Active Driving cannot navigate to Settings in the existing AppNavHost.
 * Selection is OFF by default. Cancel/Back never calls deletion callbacks.
 * The app owner (not Compose) executes clear/revoke so recreation does not
 * cancel a previously accepted DataStore write.
 */
@Composable
fun PrivacyDiagnosticsSettingsSection(
    diagnosticsConsent: DiagnosticsConsent,
    preferencesReady: Boolean,
    microphoneGranted: Boolean,
    shortHistoryAvailability: ShortHistoryAvailability,
    onClearSelected: (
        LocalPrivacyClearSelection, (LocalPrivacyClearReport) -> Unit
    ) -> Unit,
    onRevokeDiagnostics: ((DiagnosticsRevocationReport) -> Unit) -> Unit,
) {
    var clearDiagnosticsSelected by rememberSaveable { mutableStateOf(false) }
    var showClearConfirmation by rememberSaveable { mutableStateOf(false) }
    var showRevokeConfirmation by rememberSaveable { mutableStateOf(false) }
    var clearing by remember { mutableStateOf(false) }
    var revoking by remember { mutableStateOf(false) }
    var clearResult by remember { mutableStateOf<LocalPrivacyClearReport?>(null) }
    var revokeResult by remember { mutableStateOf<DiagnosticsRevocationReport?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.i12_privacy_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.i12_privacy_not_recorded))
        Text(stringResource(R.string.i12_privacy_approved_families))
        Text(stringResource(R.string.i12_privacy_not_sent))
        Text(stringResource(R.string.i12_privacy_retention))
        Text(stringResource(R.string.i12_privacy_user_consent))
        Text(
            stringResource(
                when (diagnosticsConsent) {
                    DiagnosticsConsent.NotAsked -> R.string.i12_privacy_status_notasked
                    DiagnosticsConsent.Declined -> R.string.i12_privacy_status_declined
                    DiagnosticsConsent.Granted -> R.string.i12_privacy_status_granted_blocked
                    DiagnosticsConsent.Revoked -> R.string.i12_privacy_status_revoked
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (!preferencesReady) {
            Text(stringResource(R.string.i12_privacy_loading))
        }
        Text(stringResource(R.string.i12_privacy_grant_unavailable))
        Text(
            stringResource(
                if (microphoneGranted) R.string.i12_privacy_mic_granted
                else R.string.i12_privacy_mic_denied
            )
        )
        Text(stringResource(R.string.i12_privacy_cloud_separate))

        OutlinedButton(
            enabled = !revoking && !clearing,
            onClick = {
                revokeResult = null
                showRevokeConfirmation = true
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.i12_privacy_revoke_button))
        }
        if (revoking) {
            Text(stringResource(R.string.i12_privacy_revoke_busy))
        }
        revokeResult?.let { result ->
            Text(
                stringResource(
                    if (result.complete) R.string.i12_privacy_revoke_success
                    else R.string.i12_privacy_revoke_partial
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!result.complete) {
                OutlinedButton(
                    enabled = !revoking && !clearing,
                    onClick = { showRevokeConfirmation = true },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.i12_privacy_retry)) }
            }
        }

        Text(
            stringResource(R.string.i12_privacy_clear_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp).semantics { heading() },
        )
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = false, onCheckedChange = null, enabled = false,
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(stringResource(R.string.i12_privacy_history_item))
                Text(
                    stringResource(R.string.i12_privacy_history_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        // History is not present in the current Room schema; do not silently
        // delete library items/restorable queue or claim the history was wiped.
        if (shortHistoryAvailability != ShortHistoryAvailability.NOT_STORED_IN_CURRENT_BUILD) {
            Text(stringResource(R.string.i12_privacy_history_unavailable))
        }

        Row(
            modifier = Modifier.fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = clearDiagnosticsSelected,
                    enabled = !clearing && !revoking,
                    onValueChange = {
                        clearDiagnosticsSelected = it
                        clearResult = null
                    },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = clearDiagnosticsSelected, onCheckedChange = null)
            Text(
                stringResource(R.string.i12_privacy_diagnostics_item),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Text(
            stringResource(R.string.i12_privacy_diagnostics_hint),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(stringResource(R.string.i12_privacy_clear_confirm_preserved))
        Text(stringResource(R.string.i12_privacy_clear_confirm_remote))
        OutlinedButton(
            onClick = {
                clearResult = null
                showClearConfirmation = true
            },
            enabled = clearDiagnosticsSelected && !clearing && !revoking,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        ) {
            Text(stringResource(R.string.i12_privacy_clear_button))
        }
        if (!clearDiagnosticsSelected) {
            Text(
                stringResource(R.string.i12_privacy_nothing_selected),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (clearing) Text(stringResource(R.string.i12_privacy_clear_busy))
        clearResult?.let { result ->
            Text(
                stringResource(
                    when (result.overall) {
                        LocalClearOverall.CLEARED -> R.string.i12_privacy_clear_success
                        LocalClearOverall.PARTIALLY_CLEARED ->
                            R.string.i12_privacy_clear_partial
                        else -> R.string.i12_privacy_clear_failed
                    }
                )
            )
            Text(
                stringResource(
                    R.string.i12_privacy_clear_details_local,
                    clearStateLabel(result.diagnostics),
                ),
            )
            Text(
                stringResource(
                    R.string.i12_privacy_clear_details_history,
                    clearStateLabel(result.shortHistory),
                ),
            )
            if (result.overall != LocalClearOverall.CLEARED) {
                OutlinedButton(
                    enabled = clearDiagnosticsSelected && !clearing && !revoking,
                    onClick = { showClearConfirmation = true },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Text(stringResource(R.string.i12_privacy_retry))
                }
            }
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!clearing) showClearConfirmation = false },
            title = { Text(stringResource(R.string.i12_privacy_clear_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.i12_privacy_clear_confirm_diag))
                    Text(stringResource(R.string.i12_privacy_clear_confirm_preserved))
                    Text(stringResource(R.string.i12_privacy_clear_confirm_remote))
                }
            },
            confirmButton = {
                TextButton(
                    enabled = clearDiagnosticsSelected && !clearing,
                    onClick = {
                        showClearConfirmation = false
                        clearing = true
                        clearResult = null
                        onClearSelected(
                            LocalPrivacyClearSelection(diagnostics = true)
                        ) { result ->
                            clearing = false
                            clearResult = result
                            if (result.overall == LocalClearOverall.CLEARED)
                                clearDiagnosticsSelected = false
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.i12_privacy_clear_action)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !clearing,
                    onClick = { showClearConfirmation = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.i12_privacy_cancel)) }
            }
        )
    }

    if (showRevokeConfirmation) {
        AlertDialog(
            onDismissRequest = { if (!revoking) showRevokeConfirmation = false },
            title = { Text(stringResource(R.string.i12_privacy_revoke_confirm_title)) },
            text = { Text(stringResource(R.string.i12_privacy_revoke_confirm_message)) },
            confirmButton = {
                TextButton(
                    enabled = !revoking && !clearing,
                    onClick = {
                        showRevokeConfirmation = false
                        revoking = true
                        revokeResult = null
                        // The application latch denies synchronously here,
                        // not when a later UI coroutine begins execution.
                        onRevokeDiagnostics { result ->
                            revoking = false
                            revokeResult = result
                        }
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.i12_privacy_revoke_action)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !revoking,
                    onClick = { showRevokeConfirmation = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text(stringResource(R.string.i12_privacy_cancel)) }
            }
        )
    }
}

/** Fixed-vocabulary status labels; never contain media/user/exception data. */
@Composable
private fun clearStateLabel(result: LocalClearCategoryResult): String = stringResource(
    when (result) {
        LocalClearCategoryResult.CLEARED -> R.string.i12_privacy_result_cleared
        LocalClearCategoryResult.NOT_STORED -> R.string.i12_privacy_result_notstored
        LocalClearCategoryResult.UNAVAILABLE -> R.string.i12_privacy_result_unavailable
        LocalClearCategoryResult.FAILED -> R.string.i12_privacy_result_failed
        LocalClearCategoryResult.NOT_SELECTED -> R.string.i12_privacy_result_notselected
    }
)
