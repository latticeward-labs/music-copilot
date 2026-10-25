package com.latticeward.musiccopilot.core.application.voice

import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * Issue #10 P03: pure, presentation-neutral permission/provider/session gate.
 * This never requests Android permission or constructs a recognizer.
 * UI eligibility is only a hint: callers MUST recheck the OS and provider
 * at tap time, then recheck the singleton coordinator's current state.
 */
enum class VoicePttAvailability {
    Ready, NeedsPermission, ProviderUnavailable, OutsideOrigin, SessionActive
}

object VoicePttAdmissionPolicy {
    fun availability(
        originActive: Boolean,
        providerSupported: Boolean,
        microphoneGranted: Boolean,
        providerAvailable: Boolean,
        session: VoiceSessionState,
    ): VoicePttAvailability = when {
        !originActive -> VoicePttAvailability.OutsideOrigin
        session != VoiceSessionState.Idle -> VoicePttAvailability.SessionActive
        !providerSupported -> VoicePttAvailability.ProviderUnavailable
        !microphoneGranted -> VoicePttAvailability.NeedsPermission
        !providerAvailable -> VoicePttAvailability.ProviderUnavailable
        else -> VoicePttAvailability.Ready
    }

    fun canTapMic(value: VoicePttAvailability): Boolean =
        value == VoicePttAvailability.Ready ||
            value == VoicePttAvailability.NeedsPermission

    fun mayStart(
        value: VoicePttAvailability,
        freshAndroidGrant: Boolean,
        freshOnDeviceProvider: Boolean,
        latest: VoiceSessionState,
    ): Boolean = value == VoicePttAvailability.Ready &&
        freshAndroidGrant && freshOnDeviceProvider &&
        latest == VoiceSessionState.Idle

    fun owns(state: VoiceSessionState, origin: VoiceOrigin): Boolean = when (state) {
        VoiceSessionState.Idle -> false
        is VoiceSessionState.Listening -> state.origin == origin
        is VoiceSessionState.Processing -> state.origin == origin
        is VoiceSessionState.Confirming -> state.origin == origin
        is VoiceSessionState.Executed -> state.origin == origin
        is VoiceSessionState.Failed -> state.origin == origin
    }
}
