package com.latticeward.musiccopilot

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.feature.driving.BluetoothEntrySuggestionBanner
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * P12 source-authored UI integration regression fixtures — NOT EXECUTED.
 * The banner is a child of the existing route, not a new permission/BT UI
 * owner. Physical TalkBack and OEM font/route behavior are P13 phone checks.
 */
@RunWith(AndroidJUnit4::class)
class Issue9Phase12BluetoothEntryIntegrationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun p1InvitationDoesNotBlockOriginalMusicSurfaceAction() {
        var originalTapped = 0
        compose.setContent {
            MusicCopilotTheme {
                Column {
                    BluetoothEntrySuggestionBanner(onEnter = {}, onNotNow = {})
                    Button(
                        onClick = { originalTapped++ },
                        modifier = androidx.compose.ui.Modifier.heightIn(min = 48.dp)
                    ) { Text("Original music action") }
                }
            }
        }
        compose.onNodeWithText("Original music action").performClick()
        compose.runOnIdle { assertEquals(1, originalTapped) }
    }

    @Test fun p1OfferAtTwoHundredPercentMeasuresActualButtons() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                MusicCopilotTheme {
                    BluetoothEntrySuggestionBanner(onEnter = {}, onNotNow = {})
                }
            }
        }
        compose.onNodeWithTag("issue9_bt_enter")
            .assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("issue9_bt_not_now")
            .assertHeightIsAtLeast(48.dp)
    }

    @Test fun bluetoothInvitationNeverClaimsAutomaticallyStartedPlayback() {
        compose.setContent {
            MusicCopilotTheme {
                BluetoothEntrySuggestionBanner(onEnter = {}, onNotNow = {})
            }
        }
        compose.onNodeWithText("Driving Mode?").assertIsDisplayed()
        compose.onNodeWithText("Enter").assertIsDisplayed()
        compose.onNodeWithText("Not now").assertIsDisplayed()
        compose.onNodeWithText("Playing").assertDoesNotExist()
    }
}
