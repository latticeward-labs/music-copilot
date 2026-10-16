package com.latticeward.musiccopilot.app.navigation

/**
 * Issue #16 typed destination registry. Bootstrap remains the start destination; P03 adds the approved First Launch
 * routes. Other feature destinations remain declarative only.
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
        val activeInPhase3 = setOf(Bootstrap, Onboarding, PermissionRecovery, Library)
    }
}
