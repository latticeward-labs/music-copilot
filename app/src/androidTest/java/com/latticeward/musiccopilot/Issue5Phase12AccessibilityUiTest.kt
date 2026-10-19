package com.latticeward.musiccopilot

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.core.application.permission.MediaPermissionOutcome
import com.latticeward.musiccopilot.feature.permission.MediaPermissionOutcomeScreen
import com.latticeward.musiccopilot.feature.permission.MediaPermissionRationaleScreen
import com.latticeward.musiccopilot.feature.permission.MediaRequestUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Issue #5 P12 — Compose UI verification without fake Android permission dialogs.
 * Actual OS prompts/permission grants belong to the separate native API matrix.
 * No screenshot, logcat capture, media identity, or network access in this suite.
 */
@RunWith(AndroidJUnit4::class)
class Issue5Phase12AccessibilityUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun rationaleExplainsMediaAccessWithReachable48dpActions() {
        var requests = 0
        var notNow = 0
        compose.setContent {
            MusicCopilotTheme {
                MediaPermissionRationaleScreen(
                    request = MediaRequestUiState(),
                    onContinue = { requests++ },
                    onNotNow = { notNow++ },
                )
            }
        }
        compose.onNodeWithText("Media access").assertIsDisplayed()
        compose.onNodeWithText("Index and play local music.").assertExists()
        compose.onNodeWithText("Continue").assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("Not now").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle {
            assertEquals(1, requests)
            assertEquals(1, notNow)
        }
    }

    @Test fun pendingPermissionCannotStartDuplicateRequest() {
        var requests = 0
        compose.setContent {
            MusicCopilotTheme {
                MediaPermissionRationaleScreen(
                    request = MediaRequestUiState(pending = true, attempted = true),
                    onContinue = { requests++ },
                    onNotNow = {},
                )
            }
        }
        compose.onNodeWithText("Waiting for Android permission").assertExists()
        compose.onNodeWithText("Continue").assertIsNotEnabled()
        compose.onNodeWithText("Not now").assertIsNotEnabled()
        compose.runOnIdle { assertEquals(0, requests) }
    }

    @Test fun deniedOutcomeOffersRetryAndNonCoerciveExit() {
        var retries = 0
        var exits = 0
        compose.setContent {
            MusicCopilotTheme {
                MediaPermissionOutcomeScreen(
                    outcome = MediaPermissionOutcome.DeniedRequestable,
                    onContinueGranted = {},
                    onTryAgain = { retries++ },
                    onOpenSettings = {},
                    onNotNow = { exits++ },
                )
            }
        }
        compose.onNodeWithText("Access denied").assertIsDisplayed()
        compose.onNodeWithText("Try again").assertHeightIsAtLeast(48.dp).performClick()
        compose.onNodeWithText("Not now").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            assertEquals(1, exits)
        }
    }

    @Test fun settingsOnlyOffersExplicitSettingsAndNoGrantClaim() {
        var launches = 0
        compose.setContent {
            MusicCopilotTheme {
                MediaPermissionOutcomeScreen(
                    outcome = MediaPermissionOutcome.SettingsOnly,
                    onContinueGranted = {},
                    onTryAgain = {},
                    onOpenSettings = { launches++ },
                    onNotNow = {},
                )
            }
        }
        compose.onNodeWithText("Open Android Settings").assertIsDisplayed()
        compose.onNodeWithText("Open Settings").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, launches) }
        // No granted state is synthesized by the Settings-only UI.
        compose.onNodeWithText("Access granted").assertDoesNotExist()
    }

    @Test fun grantedOutcomeStillRequiresExplicitContinue() {
        var continues = 0
        compose.setContent {
            MusicCopilotTheme {
                MediaPermissionOutcomeScreen(
                    outcome = MediaPermissionOutcome.Granted,
                    onContinueGranted = { continues++ },
                    onTryAgain = {},
                    onOpenSettings = {},
                    onNotNow = {},
                )
            }
        }
        compose.runOnIdle { assertEquals(0, continues) }
        compose.onNodeWithText("Access granted").assertIsDisplayed()
        compose.onNodeWithText("Continue").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, continues) }
    }

    @Test fun twoHundredPercentTextStillAllowsSettingsRecoveryAction() {
        var settings = 0
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale = 2f)) {
                MusicCopilotTheme {
                    MediaPermissionOutcomeScreen(
                        outcome = MediaPermissionOutcome.SettingsOnly,
                        onContinueGranted = {},
                        onTryAgain = {},
                        onOpenSettings = { settings++ },
                        onNotNow = {},
                    )
                }
            }
        }
        compose.onNodeWithText("Open Settings").assertHeightIsAtLeast(48.dp)
            .performClick()
        compose.runOnIdle { assertEquals(1, settings) }
    }
}
