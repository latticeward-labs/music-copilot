package com.latticeward.musiccopilot.app.navigation

/**
 * Issue #16 typed destination registry. Only Bootstrap is registered in the
 * Phase 10 NavHost. The rest are contracts, NOT approved feature screens.
 * Voice is intentionally absent: it is a cancellable origin-owned overlay.
 */
sealed class Destination(val route: String) {
    data object Bootstrap : Destination("bootstrap")
    data object Onboarding : Destination("onboarding")
    data object PermissionRecovery : Destination("permission_recovery")
    data object Library : Destination("library")
    data object Search : Destination("search")
    data object FullPlayer : Destination("full_player")
    data object Driving : Destination("driving")
    data object Settings : Destination("settings")
    data object LikedSongsP2 : Destination("liked_songs_p2")
    companion object {
        val declared = listOf(
            Bootstrap, Onboarding, PermissionRecovery, Library,
            Search, FullPlayer, Driving, Settings, LikedSongsP2
        )
        val activeInPhase10 = setOf(Bootstrap)
    }
}
