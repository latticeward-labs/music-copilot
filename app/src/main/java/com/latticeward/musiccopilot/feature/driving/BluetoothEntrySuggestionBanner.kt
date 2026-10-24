package com.latticeward.musiccopilot.feature.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSize
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles

/**
 * Issue #9 P11: small non-modal suggestion on the existing Library / Full
 * Player route. It never calls Media3, starts an Activity, records audio or
 * changes the route by itself; the owning NavHost validates any Enter tap.
 *
 * Never displays address, device name, saved hash, private media or a claim
 * that music is already playing over the selected Bluetooth profile.
 */
@Composable
fun BluetoothEntrySuggestionBanner(
    onEnter: () -> Unit,
    onNotNow: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
        color = MusicColors.surfaceVariant,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(MusicSpacing.mdDp),
            verticalArrangement = Arrangement.spacedBy(MusicSpacing.smDp),
        ) {
            Text(
                text = stringResource(R.string.i9_bt_offer_title),
                modifier = Modifier.semantics {
                    heading()
                    liveRegion = LiveRegionMode.Polite
                },
                color = MusicColors.textPrimary,
                style = MusicTextStyles.titleMedium,
            )
            Text(
                text = stringResource(R.string.i9_bt_offer_description),
                color = MusicColors.textSecondary,
                style = MusicTextStyles.bodyMedium,
            )
            if (LocalDensity.current.fontScale >= 1.5f) {
                Column(verticalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp)) {
                    Button(
                        onClick = onEnter,
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.i9_bt_offer_enter)) }
                    OutlinedButton(
                        onClick = onNotNow,
                        modifier = Modifier.fillMaxWidth()
                            .heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.i9_bt_offer_not_now)) }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MusicSpacing.xsDp),
                ) {
                    OutlinedButton(
                        onClick = onNotNow,
                        modifier = Modifier.weight(1f)
                            .heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.i9_bt_offer_not_now)) }
                    Button(
                        onClick = onEnter,
                        modifier = Modifier.weight(1f)
                            .heightIn(min = MusicSize.touchTargetMinDp),
                    ) { Text(stringResource(R.string.i9_bt_offer_enter)) }
                }
            }
        }
    }
}
