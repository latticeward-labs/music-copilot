package com.latticeward.musiccopilot.core.application.permission

/**
 * P09: optional Voice education/rationale. This owner does NOT open a microphone,
 * create a SpeechRecognizer, or start a VoiceSession. Only Activity can request
 * the Android permission; the real VoiceSessionCoordinator owns future capture.
 */
enum class VoicePracticeStage {
    Idle, Rationale, AwaitingAndroid, ReadyForVoiceHandoff,
    PermissionDenied, ProviderUnavailable, LanguageUnsupported, CloudConsentDeclined,
}
enum class VoiceProviderCapability {
    OnDeviceAvailable, ProviderUnavailable, LanguageUnsupported, RemoteConsentRequired,
}

/** No personal voice, transcript, permission-grant or service identifiers. */
class VoicePracticeGate {
    var stage: VoicePracticeStage = VoicePracticeStage.Idle
        private set
    private var pending = false

    val awaitingAndroidResult get() = pending

    /** Only the explicit Practice Voice tap can open a purpose explanation. */
    fun practiceTapped(): Boolean {
        if (pending || stage == VoicePracticeStage.Rationale) return false
        stage = VoicePracticeStage.Rationale
        return true
    }

    /** Future Player/Driving microphone buttons may enter this same JIT gate. */
    fun microphoneTapped(): Boolean = practiceTapped()

    /** One system request maximum per explicit rationale confirmation. */
    fun confirmRationale(): Boolean {
        if (stage != VoicePracticeStage.Rationale || pending) return false
        pending = true
        stage = VoicePracticeStage.AwaitingAndroid
        return true
    }

    fun androidResult(
        freshAndroidGrant: Boolean,
        provider: VoiceProviderCapability,
    ): VoicePracticeStage {
        if (!pending) return stage // Ignore stale and duplicate callbacks.
        pending = false
        stage = when {
            !freshAndroidGrant -> VoicePracticeStage.PermissionDenied
            provider == VoiceProviderCapability.ProviderUnavailable ->
                VoicePracticeStage.ProviderUnavailable
            provider == VoiceProviderCapability.LanguageUnsupported ->
                VoicePracticeStage.LanguageUnsupported
            // P09 has no remote provider or approved remote disclosure. Even a
            // saved consent flag never authorizes cloud capture in this phase.
            provider == VoiceProviderCapability.RemoteConsentRequired ->
                VoicePracticeStage.CloudConsentDeclined
            else -> VoicePracticeStage.ReadyForVoiceHandoff
        }
        return stage
    }

    /** Restored ActivityResult registration only; NEVER launch a second prompt. */
    fun restorePending() {
        pending = true
        stage = VoicePracticeStage.AwaitingAndroid
    }

    /** If microphone access was revoked, Ready cannot remain authoritative. */
    fun recheck(freshAndroidGrant: Boolean) {
        if (!freshAndroidGrant && stage == VoicePracticeStage.ReadyForVoiceHandoff)
            stage = VoicePracticeStage.PermissionDenied
    }

    /** Skip, Back or Not now only drops education intent, not playback. */
    fun manualOnly() {
        pending = false
        stage = VoicePracticeStage.Idle
    }

    fun requestLaunchFailed() {
        pending = false
        stage = VoicePracticeStage.PermissionDenied
    }
}
