package com.latticeward.musiccopilot.app.theme

import androidx.compose.ui.graphics.Color

/**
 * Generated faithfully from Issue #17 foundations/TOKENS.json v1.0.0, Night Signal.
 * Exact #RRGGBB and #RRGGBBAA -> Compose 0xAARRGGBB.
 * See scripts/check_phase10_design_tokens.py for exhaustive source truth audit.
 */
object MusicColors {
    val background = Color(0xFF090D12)
    val surface = Color(0xFF111720)
    val surfaceVariant = Color(0xFF18212C)
    val surfaceRaised = Color(0xFF202A36)
    val systemBar = Color(0xFF090D12)
    val primary = Color(0xFF67D8FF)
    val onPrimary = Color(0xFF031117)
    val primaryContainer = Color(0xFF143646)
    val onPrimaryContainer = Color(0xFFC7F2FF)
    val secondary = Color(0xFFA9A1FF)
    val onSecondary = Color(0xFF0E0B1B)
    val success = Color(0xFF76E0A3)
    val onSuccess = Color(0xFF07110B)
    val warning = Color(0xFFFFD166)
    val onWarning = Color(0xFF151008)
    val error = Color(0xFFFF7088)
    val onError = Color(0xFF0E0B0D)
    val errorContainer = Color(0xFF3A1720)
    val onErrorContainer = Color(0xFFFFD9DF)
    val textPrimary = Color(0xFFF5F7FA)
    val textSecondary = Color(0xFFC8D0DA)
    val textMuted = Color(0xFF9AA5B3)
    val border = Color(0xFF2B3746)
    val borderStrong = Color(0xFF5A6878)
    val focusRing = Color(0xFFA2EBFF)
    val disabledContainer = Color(0xFF202A36)
    val disabledContent = Color(0xFF7C8998)
    val scrim = Color(0xB8000000)
}

object MusicPrimitiveColors {
    val neutral950 = Color(0xFF090D12)
    val neutral900 = Color(0xFF111720)
    val neutral850 = Color(0xFF18212C)
    val neutral800 = Color(0xFF202A36)
    val neutral700 = Color(0xFF2B3746)
    val neutral500 = Color(0xFF5A6878)
    val neutral400 = Color(0xFF7C8998)
    val neutral300 = Color(0xFF9AA5B3)
    val neutral200 = Color(0xFFC8D0DA)
    val neutral100 = Color(0xFFE4E9EF)
    val neutral050 = Color(0xFFF5F7FA)
    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)
    val cyan500 = Color(0xFF67D8FF)
    val cyan300 = Color(0xFFA2EBFF)
    val cyan100 = Color(0xFFC7F2FF)
    val violet500 = Color(0xFFA9A1FF)
    val violet100 = Color(0xFFE5E1FF)
    val green500 = Color(0xFF76E0A3)
    val amber500 = Color(0xFFFFD166)
    val red500 = Color(0xFFFF7088)
    val red100 = Color(0xFFFFD9DF)
}

object MusicStateColors {
    val pressedOverlay = Color(0x1AFFFFFF)
    val focus = MusicColors.focusRing
    val selectedContainer = MusicColors.primaryContainer
    val selectedContent = MusicColors.onPrimaryContainer
    val disabledContainer = MusicColors.disabledContainer
    val disabledContent = MusicColors.disabledContent
    val error = MusicColors.error
    val errorContainer = MusicColors.errorContainer
}
