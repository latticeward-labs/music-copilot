package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.navigation.Destination
import com.latticeward.musiccopilot.app.navigation.FoundationReadiness
import com.latticeward.musiccopilot.app.navigation.RootNavigationStateHolder
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Pure shell contracts; final visual/phone boot proof belongs to instrumentation. */
class Phase10ComposeShellTest {
    private val root = File("src/main/java/com/latticeward/musiccopilot")
    private fun read(path: String) = File(root, path).readText()

    @Test fun onlyBootstrapIsRoutableButIssue16ContractsAreDeclared() {
        assertEquals(Destination.Bootstrap, RootNavigationStateHolder().startDestination)
        assertEquals(setOf(Destination.Bootstrap), Destination.activeInPhase10)
        assertEquals(9, Destination.declared.size)
        assertEquals(9, Destination.declared.map { it.route }.toSet().size)
        assertEquals("bootstrap", Destination.Bootstrap.route)
    }

    @Test fun voiceIsOverlayNotASecondNavigationDestination() {
        assertFalse(Destination.declared.any { it.route.contains("voice") })
        val nav = read("app/navigation/AppNavHost.kt")
        assertEquals(4, Regex("composable\\(").findAll(nav).count())
        assertTrue(nav.contains("composable(Destination.Bootstrap.route)"))
        assertTrue(nav.contains("composable(Destination.Onboarding.route)"))
        assertTrue(nav.contains("composable(Destination.PermissionRecovery.route)"))
        assertTrue(nav.contains("composable(Destination.Library.route)"))
    }

    @Test fun missingLibraryPermissionNeverPretendsReadyOrCrashes() {
        val state = RootNavigationStateHolder()
        for (connection in PlaybackConnectionState.entries) {
            assertEquals(FoundationReadiness.LibraryPermissionOrMediaUnavailable,
                state.readiness(LibraryIndexStatus.Unavailable, connection))
        }
    }

    @Test fun allLocalLibraryStatesProjectToTruthfulNeutralBootstrapState() {
        val state = RootNavigationStateHolder()
        val expected = mapOf(
            LibraryIndexStatus.Unavailable to FoundationReadiness.LibraryPermissionOrMediaUnavailable,
            LibraryIndexStatus.Scanning to FoundationReadiness.IndexingLocalLibrary,
            LibraryIndexStatus.Empty to FoundationReadiness.LocalLibraryEmpty,
            LibraryIndexStatus.Ready to FoundationReadiness.LocalLibraryReady,
            LibraryIndexStatus.RecoverableError to FoundationReadiness.LocalLibraryRecoverableError
        )
        for ((library, readiness) in expected)
            assertEquals(readiness, state.readiness(library, PlaybackConnectionState.Disconnected))
    }

    @Test fun playbackControllerAvailabilityNeverFabricatesMediaAccess() {
        val state = RootNavigationStateHolder()
        assertEquals(FoundationReadiness.LibraryPermissionOrMediaUnavailable,
            state.readiness(LibraryIndexStatus.Unavailable, PlaybackConnectionState.Ready))
        assertEquals(FoundationReadiness.LocalLibraryEmpty,
            state.readiness(LibraryIndexStatus.Empty, PlaybackConnectionState.RecoverableError))
    }

    @Test fun activityOnlyObservesApplicationPortsLifecycleAware() {
        val activity = read("app/MainActivity.kt")
        assertTrue(activity.contains("collectAsStateWithLifecycle()"))
        assertTrue(activity.contains("MusicCopilotTheme {"))
        assertTrue(activity.contains("AppNavHost("))
        assertFalse(activity.contains("MediaSession.Builder("))
        assertFalse(activity.contains("ExoPlayer.Builder("))
        assertFalse(activity.contains("ApplicationBindings("))
    }

    @Test fun themeNeverAddsDynamicColorOrLightModeAndUsesSourceSemanticTokens() {
        val code = read("app/theme/MusicCopilotTheme.kt")
        assertTrue(code.contains("darkColorScheme("))
        assertFalse(code.contains("dynamicDarkColorScheme"))
        assertFalse(code.contains("isSystemInDarkTheme()"))
        assertTrue(code.contains("primary = MusicColors.primary"))
        assertTrue(code.contains("background = MusicColors.background"))
        assertTrue(code.contains("typography = MusicMaterialTypography"))
    }

    @Test fun viewportCanGrowAndRespectSystemInsetsWithoutStaticImageRendering() {
        val ui = read("app/navigation/AppNavHost.kt")
        assertTrue(ui.contains(".safeDrawingPadding()"))
        assertTrue(ui.contains(".verticalScroll(rememberScrollState())"))
        assertFalse(ui.contains("Image("))
        assertFalse(ui.contains("DrivingControls"))
        assertFalse(ui.contains("PlaybackControls"))
    }

    @Test fun buildRetainsSingleAppAndAndroid10Minimum() {
        val build = File("build.gradle.kts").readText()
        assertTrue(build.contains("minSdk = 29"))
        assertTrue(build.contains("implementation(libs.androidx.navigation.compose)"))
        assertTrue(build.contains("implementation(libs.androidx.lifecycle.runtime.compose)"))
        assertEquals(1, File("src/main/AndroidManifest.xml").readText()
            .split("android:name=\".app.MainActivity\"").size - 1)
    }
}
