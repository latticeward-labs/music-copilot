package com.latticeward.musiccopilot.app.theme

import androidx.compose.ui.unit.dp

/** One-to-one Issue #17 geometry token bridge; all units are dp. */
object MusicSpacing {
    val xxsDp = 4.dp
    val xsDp = 8.dp
    val smDp = 12.dp
    val mdDp = 16.dp
    val lgDp = 24.dp
    val xlDp = 32.dp
    val xxlDp = 40.dp
    val xxxlDp = 48.dp
    val hugeDp = 64.dp
}

object MusicGrid {
    const val compactColumns = 4
    val baselineDp = 4.dp
    val gutterDp = 12.dp
    val screenMarginDp = 16.dp
    val drivingMarginDp = 20.dp
    val referenceViewportDp_width = 360.dp
    val referenceViewportDp_height = 800.dp
}

object MusicRadius {
    val noneDp = 0.dp
    val xsDp = 4.dp
    val smDp = 8.dp
    val mdDp = 12.dp
    val lgDp = 16.dp
    val xlDp = 24.dp
    val pillDp = 999.dp
}

object MusicElevation {
    val level0Dp = 0.dp
    val level1Dp = 1.dp
    val level2Dp = 3.dp
    val level3Dp = 6.dp
    val level4Dp = 12.dp
}

object MusicSize {
    val touchTargetMinDp = 48.dp
    val appBarHeightDp = 56.dp
    val bottomNavigationHeightDp = 64.dp
    val trackRowMinHeightDp = 64.dp
    val iconDefaultDp = 24.dp
    val iconPlayerDp = 32.dp
    val iconDrivingDp = 36.dp
    val iconVisualStrokeDp = 2.dp
    val drivingControlMinDp = 64.dp
    val drivingMicMinDp = 72.dp
    val drivingExitMinDp = 56.dp
    val focusRingWidthDp = 2.dp
    val focusRingOffsetDp = 2.dp
}

object MusicMotion {
    const val instantMs = 0
    const val fastMs = 100
    const val standardMs = 160
    const val deliberateMs = 220
    const val maxNonCriticalMs = 300
    const val drivingOrdinaryMaxMs = 160
}
