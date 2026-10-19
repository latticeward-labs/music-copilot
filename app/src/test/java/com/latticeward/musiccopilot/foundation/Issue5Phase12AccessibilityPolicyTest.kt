package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** P12 source + pure WCAG contrast contracts; not a substitute for native TalkBack. */
class Issue5Phase12AccessibilityPolicyTest {
    private val main = "src/main/java/com/latticeward/musiccopilot/"
    private fun source(path: String) = File(main + path).readText()
    private fun contrast(foreground: String, background: String): Double {
        fun luminance(hex: String): Double {
            require(hex.matches(Regex("[0-9A-Fa-f]{6}")))
            val rgb = (0..2).map { idx ->
                val raw = hex.substring(idx * 2, idx * 2 + 2).toInt(16) / 255.0
                if (raw <= 0.04045) raw / 12.92 else
                    Math.pow((raw + 0.055) / 1.055, 2.4)
            }
            return rgb[0] * 0.2126 + rgb[1] * 0.7152 + rgb[2] * 0.0722
        }
        val a = luminance(foreground)
        val b = luminance(background)
        return (maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05)
    }

    private fun palette(): Map<String, String> {
        val text = source("app/theme/MusicColors.kt")
        return Regex("val\\s+(\\w+)\\s*=\\s*Color\\(0xFF([0-9A-Fa-f]{6})\\)")
            .findAll(text)
            .associate { it.groupValues[1] to it.groupValues[2] }
    }

    @Test fun enabledTextContrastAtLeastWcagAa() {
        val colors = palette()
        for ((foreground, background) in listOf(
            "textPrimary" to "background",
            "textSecondary" to "surface",
            "textSecondary" to "surfaceVariant",
            "primary" to "background",
            "onPrimary" to "primary",
            "error" to "background",
        )) {
            val ratio = contrast(
                requireNotNull(colors[foreground]), requireNotNull(colors[background])
            )
            assertTrue("Contrast $foreground/$background $ratio < 4.5", ratio >= 4.5)
        }
    }

    @Test fun contrastCheckerRejectsAnActualBadFixture() {
        assertEquals(1.0, contrast("090D12", "090D12"), 0.0001)
        assertTrue(contrast("7C8998", "202A36") < 4.5)
        // The low-contrast disabled token is not used as ordinary enabled body copy.
        val theme = source("app/theme/MusicCopilotTheme.kt")
        assertFalse(theme.contains("onSurface = MusicColors.disabledContent"))
    }

    @Test fun availableActionsAreMinimum48DpIncludingBackSkipRetryAndSettings() {
        val geometry = source("app/theme/MusicGeometry.kt")
        assertTrue(geometry.contains("touchTargetMinDp = 48.dp"))
        val firstUse = source("feature/onboarding/FirstUseScreen.kt")
        val rationale = source("feature/permission/MediaPermissionRationaleScreen.kt")
        val outcome = source("feature/permission/MediaPermissionOutcomeScreen.kt")
        val host = source("app/navigation/AppNavHost.kt")
        for (screen in listOf(firstUse, rationale, outcome)) {
            assertTrue(screen.contains("heightIn(min = 48.dp)"))
            assertTrue(screen.contains("safeDrawingPadding()"))
            assertTrue(screen.contains("verticalScroll("))
        }
        assertTrue(host.contains("heightIn(min = MusicSize.touchTargetMinDp)"))
        assertTrue(firstUse.contains("onClick = { dispatch(FirstUseEvent.Skip) }"))
        assertTrue(outcome.contains("R.string.p06_try_again"))
        assertTrue(outcome.contains("R.string.p06_open_settings"))
    }

    @Test fun screenReaderHasHeadingAndTextualErrorOrRecoveryStatus() {
        val firstUse = source("feature/onboarding/FirstUseScreen.kt")
        val rationale = source("feature/permission/MediaPermissionRationaleScreen.kt")
        val outcome = source("feature/permission/MediaPermissionOutcomeScreen.kt")
        val host = source("app/navigation/AppNavHost.kt")
        val strings = File("src/main/res/values/strings.xml").readText()
        assertTrue(firstUse.contains("contentDescription = description"))
        assertTrue(firstUse.contains("liveRegion = LiveRegionMode.Assertive"))
        assertTrue(rationale.contains("focusRequester(rationaleFocus)"))
        assertTrue(rationale.contains("heading()"))
        assertTrue(outcome.contains("focusRequester(outcomeFocus)"))
        assertTrue(outcome.contains("liveRegion = LiveRegionMode.Polite"))
        assertTrue(host.contains("Text(title, modifier = Modifier.semantics { heading() }"))
        for (phrase in listOf("Access granted", "Access denied",
                "Open Android Settings", "Page 1 of 2", "Page 2 of 2")) {
            assertTrue("Missing text status: $phrase", strings.contains(phrase))
        }
    }

    @Test fun noPermissionLauncherOrScanOccursDuringAccessibilityRecomposition() {
        val firstUse = source("feature/onboarding/FirstUseScreen.kt")
        val rationale = source("feature/permission/MediaPermissionRationaleScreen.kt")
        val outcome = source("feature/permission/MediaPermissionOutcomeScreen.kt")
        val app = source("app/MainActivity.kt")
        for (ui in listOf(firstUse, rationale, outcome)) {
            assertFalse(ui.contains("requestPermissionLauncher.launch"))
            assertFalse(ui.contains("MediaStoreScanner"))
            assertFalse(ui.contains("voiceSessionCoordinator.start("))
        }
        val onResume = app.substringAfter("override fun onResume()").substringBefore("@Composable")
        assertFalse(onResume.contains("requestLauncher.launch("))
        assertFalse(onResume.contains("microphoneLauncher.launch("))
        assertFalse(onResume.contains("scanAfterExplicitMediaGrant("))
    }

    @Test fun mediaAndCloudNegativeGuardsArePresentInTheTargetApk() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val voice = source("app/di/ApplicationBindings.kt")
        val settings = source("app/navigation/AppNavHost.kt")
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertFalse(manifest.contains("android.permission.POST_NOTIFICATIONS"))
        assertTrue(voice.contains("remoteAdapterApproved = false"))
        assertTrue(voice.contains("legalApproval = false"))
        assertTrue(settings.contains("R.string.p10_cloud_stt_disabled"))
    }
}
