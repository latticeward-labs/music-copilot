package com.latticeward.musiccopilot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.latticeward.musiccopilot.app.navigation.AppNavHost
import com.latticeward.musiccopilot.app.navigation.RootNavigationStateHolder
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.app.theme.MusicColors
import androidx.compose.ui.graphics.toArgb

/**
 * Single Android Activity, application-owned graph; no Activity owns Player,
 * queue or Voice. Phase 10 renders only non-optimistic Bootstrap diagnostics.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = MusicColors.systemBar.toArgb()
        window.navigationBarColor = MusicColors.systemBar.toArgb()
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightNavigationBars = false

        val appBindings = (application as MusicCopilotApplication).bindings
        setContent { RootComposeShell(appBindings) }
    }

    override fun onResume() {
        super.onResume()
        (application as MusicCopilotApplication).bindings.onForeground()
    }
}

@Composable
private fun RootComposeShell(bindings: com.latticeward.musiccopilot.app.di.ApplicationBindings) {
    val library by bindings.libraryRepository.index.collectAsStateWithLifecycle()
    val playbackConnection by bindings.playbackGateway.connectionState.collectAsStateWithLifecycle()
    val navigation = RootNavigationStateHolder()
    MusicCopilotTheme {
        AppNavHost(navigation.readiness(library.status, playbackConnection))
    }
}
