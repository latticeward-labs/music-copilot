package com.latticeward.musiccopilot.app.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Exact Issue #17 SP/line-height/weight geometry; SP accessibility scaling stays OS-owned. */
object MusicTextStyles {
    val display = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp
    )

    val titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp
    )

    val titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp
    )

    val bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp
    )

    val bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(400),
        letterSpacing = 0.sp
    )

    val labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp
    )

    val labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp
    )

    val drivingTitle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight(700),
        letterSpacing = 0.sp
    )

    val drivingMeta = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight(500),
        letterSpacing = 0.sp
    )

    val drivingState = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight(600),
        letterSpacing = 0.sp
    )
}

internal val MusicMaterialTypography = Typography(
    headlineLarge = MusicTextStyles.display,
    titleLarge = MusicTextStyles.titleLarge,
    titleMedium = MusicTextStyles.titleMedium,
    bodyLarge = MusicTextStyles.bodyLarge,
    bodyMedium = MusicTextStyles.bodyMedium,
    labelLarge = MusicTextStyles.labelLarge,
    labelMedium = MusicTextStyles.labelMedium,
)
