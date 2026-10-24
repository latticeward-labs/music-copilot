package com.latticeward.musiccopilot

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
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
 * P11: Compose UI source cases AUTHORED ONLY; DO NOT RUN before final P13.
 * All connection/device identity/route inputs are isolated in pure policy tests.
 */
@RunWith(AndroidJUnit4::class)
class Issue9Phase11BluetoothInvitationUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun nonModalInvitationHasWrittenOptInAndDismissal() {
        compose.setContent {
            MusicCopilotTheme {
                BluetoothEntrySuggestionBanner(onEnter = {}, onNotNow = {})
            }
        }
        compose.onNodeWithText("Driving Mode?").assertIsDisplayed()
        compose.onNodeWithText("Enter").assertIsDisplayed()
        compose.onNodeWithText("Not now").assertIsDisplayed()
    }

    @Test fun notNowIsAnIndependentActionNotEnter() {
        var entered = 0
        var declined = 0
        compose.setContent {
            MusicCopilotTheme {
                BluetoothEntrySuggestionBanner(
                    onEnter = { entered++ }, onNotNow = { declined++ },
                )
            }
        }
        compose.onNodeWithText("Not now").performClick()
        compose.runOnIdle {
            assertEquals(0, entered)
            assertEquals(1, declined)
        }
    }

    @Test fun enterRequiresDirectTapNoAutomaticCallbackOnComposition() {
        var entered = 0
        compose.setContent {
            MusicCopilotTheme {
                BluetoothEntrySuggestionBanner(
                    onEnter = { entered++ }, onNotNow = {},
                )
            }
        }
        compose.runOnIdle { assertEquals(0, entered) }
        compose.onNodeWithText("Enter").performClick()
        compose.runOnIdle { assertEquals(1, entered) }
    }

    @Test fun twoHundredPercentFontKeepsBothFortyEightDpActions() {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                MusicCopilotTheme {
                    BluetoothEntrySuggestionBanner(onEnter = {}, onNotNow = {})
                }
            }
        }
        // Measure the CLICKABLE Button semantics node, not its Text child.
        compose.onNodeWithTag("issue9_bt_enter")
            .assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("issue9_bt_not_now")
            .assertHeightIsAtLeast(48.dp)
    }
}
