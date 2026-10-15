package com.latticeward.musiccopilot.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Fixed dark Night Signal baseline, NOT dynamic colors or a new theme setting.
 * Android 10+ supported, no device wallpaper palette dependence.
 */
private val nightSignalScheme = darkColorScheme(
    primary = MusicColors.primary,
    onPrimary = MusicColors.onPrimary,
    primaryContainer = MusicColors.primaryContainer,
    onPrimaryContainer = MusicColors.onPrimaryContainer,
    secondary = MusicColors.secondary,
    onSecondary = MusicColors.onSecondary,
    background = MusicColors.background,
    onBackground = MusicColors.textPrimary,
    surface = MusicColors.surface,
    onSurface = MusicColors.textPrimary,
    surfaceVariant = MusicColors.surfaceVariant,
    onSurfaceVariant = MusicColors.textSecondary,
    error = MusicColors.error,
    onError = MusicColors.onError,
    errorContainer = MusicColors.errorContainer,
    onErrorContainer = MusicColors.onErrorContainer,
    outline = MusicColors.border,
    outlineVariant = MusicColors.borderStrong,
    scrim = MusicColors.scrim,
    surfaceTint = MusicColors.primary
)

private val nightSignalShapes = Shapes(
    extraSmall = RoundedCornerShape(MusicRadius.xsDp),
    small = RoundedCornerShape(MusicRadius.smDp),
    medium = RoundedCornerShape(MusicRadius.mdDp),
    large = RoundedCornerShape(MusicRadius.lgDp),
    extraLarge = RoundedCornerShape(MusicRadius.xlDp)
)

@Composable
fun MusicCopilotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = nightSignalScheme,
        typography = MusicMaterialTypography,
        shapes = nightSignalShapes,
        content = content
    )
}
