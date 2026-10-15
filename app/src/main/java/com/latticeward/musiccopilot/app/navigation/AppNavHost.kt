package com.latticeward.musiccopilot.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.app.theme.MusicColors
import com.latticeward.musiccopilot.app.theme.MusicSpacing
import com.latticeward.musiccopilot.app.theme.MusicTextStyles

/**
 * One real Compose NavHost, one boot destination. No user-facing navigation to
 * unimplemented UX, no duplicate Voice route, no fabricated playback success.
 */
@Composable
fun AppNavHost(readiness: FoundationReadiness) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Destination.Bootstrap.route
    ) {
        composable(Destination.Bootstrap.route) {
            FoundationBootstrapScreen(readiness)
        }
    }
}

@Composable
internal fun FoundationBootstrapScreen(readiness: FoundationReadiness) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(MusicSpacing.lgDp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                color = MusicColors.textPrimary,
                style = MusicTextStyles.titleLarge
            )
            Text(
                modifier = Modifier.padding(top = MusicSpacing.smDp),
                text = stringResource(R.string.foundation_shell_message),
                color = MusicColors.textSecondary,
                style = MusicTextStyles.bodyLarge
            )
            Text(
                modifier = Modifier.padding(top = MusicSpacing.mdDp),
                text = stringResource(readiness.messageResource()),
                color = MusicColors.textMuted,
                style = MusicTextStyles.bodyMedium
            )
        }
    }
}

private fun FoundationReadiness.messageResource(): Int = when (this) {
    FoundationReadiness.Initializing -> R.string.foundation_state_initializing
    FoundationReadiness.LibraryPermissionOrMediaUnavailable ->
        R.string.foundation_state_unavailable
    FoundationReadiness.IndexingLocalLibrary -> R.string.foundation_state_indexing
    FoundationReadiness.LocalLibraryEmpty -> R.string.foundation_state_empty
    FoundationReadiness.LocalLibraryReady -> R.string.foundation_state_ready
    FoundationReadiness.LocalLibraryRecoverableError -> R.string.foundation_state_recoverable
}
