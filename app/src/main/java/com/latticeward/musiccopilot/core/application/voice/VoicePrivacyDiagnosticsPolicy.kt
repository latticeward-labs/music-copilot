package com.latticeward.musiccopilot.core.application.voice

/**
 * P11: privacy-safe diagnostics. All output is a closed, non-personal status.
 * Never publish transcripts, microphone buffers, file paths, Bluetooth IDs,
 * utterance text, device-specific provider names or exception messages.
 */
enum class LocalVoiceDiagnostic {
    ManualOnlyPlatform,
    MicrophonePermissionRequired,
    OnDeviceProviderUnavailable,
    OnDeviceProviderPresentUnverified,
}

object VoicePrivacyDiagnosticsPolicy {
    /**
     * A provider's fast availability check is NOT proof of an installed
     * English model or a successful offline recognition on an OEM device.
     * This pure projection never requests permission or starts the mic.
     */
    fun local(
        apiLevel: Int,
        microphoneGranted: Boolean,
        onDeviceProviderAvailable: Boolean,
    ): LocalVoiceDiagnostic = when {
        apiLevel < 31 -> LocalVoiceDiagnostic.ManualOnlyPlatform
        !microphoneGranted -> LocalVoiceDiagnostic.MicrophonePermissionRequired
        !onDeviceProviderAvailable -> LocalVoiceDiagnostic.OnDeviceProviderUnavailable
        else -> LocalVoiceDiagnostic.OnDeviceProviderPresentUnverified
    }

    /** No production online adapter is approved in this MVP. */
    fun remoteMvp(): CloudSpeechGateDecision =
        CloudSpeechConsentPolicy.evaluate(
            remoteAdapterApproved = false,
            legalApproval = false,
            disclosure = null,
            consentGranted = false,
            drivingMode = false,
        )
}
