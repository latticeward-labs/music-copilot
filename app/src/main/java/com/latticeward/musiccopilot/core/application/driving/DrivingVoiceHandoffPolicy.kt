package com.latticeward.musiccopilot.core.application.driving

import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * Issue #9 P07. Pure origin/capability gate: a button never infers
 * microphone permission, local STT readiness or recording from playback.
 * Permission requests require a subsequent explicit native user gesture.
 */
enum class DrivingVoiceAvailability {
    Ready, NeedsPermission, ProviderUnavailable, OutsideDriving, SessionActive
}

object DrivingVoiceHandoffPolicy {
    fun availability(
        drivingActive: Boolean,
        localProviderSupported: Boolean,
        microphoneGranted: Boolean,
        localProviderAvailable: Boolean,
        session: VoiceSessionState,
    ): DrivingVoiceAvailability = when {
        !drivingActive -> DrivingVoiceAvailability.OutsideDriving
        session != VoiceSessionState.Idle -> DrivingVoiceAvailability.SessionActive
        !localProviderSupported -> DrivingVoiceAvailability.ProviderUnavailable
        !microphoneGranted -> DrivingVoiceAvailability.NeedsPermission
        !localProviderAvailable -> DrivingVoiceAvailability.ProviderUnavailable
        else -> DrivingVoiceAvailability.Ready
    }

    fun canTapMic(availability: DrivingVoiceAvailability): Boolean =
        availability == DrivingVoiceAvailability.Ready ||
            availability == DrivingVoiceAvailability.NeedsPermission

    fun mayStart(
        availability: DrivingVoiceAvailability,
        freshOsGrant: Boolean,
        freshOnDeviceProvider: Boolean,
    ): Boolean = availability == DrivingVoiceAvailability.Ready &&
        freshOsGrant && freshOnDeviceProvider

    /** Hide other origins; never display a Player voice session in Driving. */
    fun VoiceSessionState.isDrivingSession(): Boolean = when (this) {
        VoiceSessionState.Idle -> false
        is VoiceSessionState.Listening -> origin == VoiceOrigin.Driving
        is VoiceSessionState.Processing -> origin == VoiceOrigin.Driving
        is VoiceSessionState.Confirming -> origin == VoiceOrigin.Driving
        is VoiceSessionState.Executed -> origin == VoiceOrigin.Driving
        is VoiceSessionState.Failed -> origin == VoiceOrigin.Driving
    }

    /** Back/Exit/permission-loss cancel only an owned Driving generation. */
    fun shouldCancelOnExit(state: VoiceSessionState): Boolean =
        state.isDrivingSession()
}
