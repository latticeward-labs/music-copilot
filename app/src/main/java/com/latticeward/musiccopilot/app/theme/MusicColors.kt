package com.latticeward.musiccopilot.app.theme

import androidx.compose.ui.graphics.Color

/**
 * Generated faithfully from Issue #17 foundations/TOKENS.json v1.0.0, Night Signal.
 * Exact #RRGGBB and #RRGGBBAA -> Compose 0xAARRGGBB.
 * See scripts/check_phase10_design_tokens.py for exhaustive source truth audit.
 */
object MusicColors {
    val background = Color(0xFF090D12uL)
    val surface = Color(0xFF111720uL)
    val surfaceVariant = Color(0xFF18212CuL)
    val surfaceRaised = Color(0xFF202A36uL)
    val systemBar = Color(0xFF090D12uL)
    val primary = Color(0xFF67D8FFuL)
    val onPrimary = Color(0xFF031117uL)
    val primaryContainer = Color(0xFF143646uL)
    val onPrimaryContainer = Color(0xFFC7F2FFuL)
    val secondary = Color(0xFFA9A1FFuL)
    val onSecondary = Color(0xFF0E0B1BuL)
    val success = Color(0xFF76E0A3uL)
    val onSuccess = Color(0xFF07110BuL)
    val warning = Color(0xFFFFD166uL)
    val onWarning = Color(0xFF151008uL)
    val error = Color(0xFFFF7088uL)
    val onError = Color(0xFF0E0B0DuL)
    val errorContainer = Color(0xFF3A1720uL)
    val onErrorContainer = Color(0xFFFFD9DFuL)
    val textPrimary = Color(0xFFF5F7FAuL)
    val textSecondary = Color(0xFFC8D0DAuL)
    val textMuted = Color(0xFF9AA5B3uL)
    val border = Color(0xFF2B3746uL)
    val borderStrong = Color(0xFF5A6878uL)
    val focusRing = Color(0xFFA2EBFFuL)
    val disabledContainer = Color(0xFF202A36uL)
    val disabledContent = Color(0xFF7C8998uL)
    val scrim = Color(0xB8000000uL)
}

object MusicPrimitiveColors {
    val neutral950 = Color(0xFF090D12uL)
    val neutral900 = Color(0xFF111720uL)
    val neutral850 = Color(0xFF18212CuL)
    val neutral800 = Color(0xFF202A36uL)
    val neutral700 = Color(0xFF2B3746uL)
    val neutral500 = Color(0xFF5A6878uL)
    val neutral400 = Color(0xFF7C8998uL)
    val neutral300 = Color(0xFF9AA5B3uL)
    val neutral200 = Color(0xFFC8D0DAuL)
    val neutral100 = Color(0xFFE4E9EFuL)
    val neutral050 = Color(0xFFF5F7FAuL)
    val white = Color(0xFFFFFFFFuL)
    val black = Color(0xFF000000uL)
    val cyan500 = Color(0xFF67D8FFuL)
    val cyan300 = Color(0xFFA2EBFFuL)
    val cyan100 = Color(0xFFC7F2FFuL)
    val violet500 = Color(0xFFA9A1FFuL)
    val violet100 = Color(0xFFE5E1FFuL)
    val green500 = Color(0xFF76E0A3uL)
    val amber500 = Color(0xFFFFD166uL)
    val red500 = Color(0xFFFF7088uL)
    val red100 = Color(0xFFFFD9DFuL)
}

object MusicStateColors {
    val pressedOverlay = Color(0x1AFFFFFFuL)
    val focus = MusicColors.focusRing
    val selectedContainer = MusicColors.primaryContainer
    val selectedContent = MusicColors.onPrimaryContainer
    val disabledContainer = MusicColors.disabledContainer
    val disabledContent = MusicColors.disabledContent
    val error = MusicColors.error
    val errorContainer = MusicColors.errorContainer
}
