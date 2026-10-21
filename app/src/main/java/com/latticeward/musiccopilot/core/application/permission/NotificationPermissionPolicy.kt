package com.latticeward.musiccopilot.core.application.permission

/** P10 policy: MediaSession notification exemptions never require a runtime prompt. */
enum class NotificationPurpose { MediaSessionPlayback, NonExemptFeature }

/**
 * Only a future independently approved non-exempt notification can request
 * Android 13+ POST_NOTIFICATIONS, after real user value and a contextual tap.
 * Neither the OS's display preference nor denial changes MediaSession playback.
 */
object NotificationPermissionPolicy {
    val activePurposes: Set<NotificationPurpose> = setOf(NotificationPurpose.MediaSessionPlayback)

    fun shouldRequest(
        api: Int,
        purpose: NotificationPurpose,
        nonExemptFeatureApproved: Boolean,
        playbackValueDemonstrated: Boolean,
        explicitUserAction: Boolean,
    ): Boolean = api >= 33 &&
        purpose == NotificationPurpose.NonExemptFeature &&
        nonExemptFeatureApproved &&
        playbackValueDemonstrated &&
        explicitUserAction

    fun currentMvpNeedsRuntimePrompt(api: Int): Boolean =
        activePurposes.any { purpose ->
            shouldRequest(api, purpose, false, false, false)
        }
}
