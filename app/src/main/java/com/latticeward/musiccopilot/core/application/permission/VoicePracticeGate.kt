package com.latticeward.musiccopilot.core.application.permission

/**
 * P09 optional education adapter for the existing P02 PermissionMachine.
 * No SpeechRecognizer, VoiceSession, MediaStore, Player, cloud, or persistence.
 */
enum class VoicePracticeStage {
    Idle, Rationale, AwaitingAndroid, ReadyForVoiceHandoff,
    PermissionDenied, ProviderUnavailable, LanguageUnsupported, CloudConsentDeclined,
}
enum class VoiceProviderCapability {
    OnDeviceAvailable, ProviderUnavailable, LanguageUnsupported, RemoteConsentRequired,
}

/**
 * Native ActivityResult and OS checks live in MainActivity, not here.
 * A grant only authorizes the P02 one-shot StartVoicePractice handoff; P09
 * never starts capture (Issue #9/#10 own live voice interactions).
 */
class VoicePracticeGate(private val api: Int = 33) {
    var stage: VoicePracticeStage = VoicePracticeStage.Idle
        private set

    private var permissionFlow: PermissionFlowState? = null

    val awaitingAndroidResult: Boolean
        get() = permissionFlow?.stage == PermissionStage.WaitingForNativeResult

    fun practiceTapped(): Boolean = beginFromGesture(
        PermissionPurpose.ExplicitVoicePractice, BlockedAction.StartVoicePractice
    )

    /** Future Player/Driving microphone buttons share the same permission rules. */
    fun microphoneTapped(): Boolean = beginFromGesture(
        PermissionPurpose.ExplicitMicrophoneTap, BlockedAction.StartVoiceCapture
    )

    private fun beginFromGesture(purpose: PermissionPurpose, action: BlockedAction): Boolean {
        if (awaitingAndroidResult || stage == VoicePracticeStage.Rationale) return false
        permissionFlow = PermissionMachine.begin(
            PermissionCapability.Microphone, purpose, api, action,
        )
        stage = VoicePracticeStage.Rationale
        return true
    }

    fun confirmRationale(): Boolean {
        if (stage != VoicePracticeStage.Rationale) return false
        val flow = permissionFlow ?: return false
        val next = PermissionMachine.reduce(flow, PermissionIntent.Continue)
        if (next.effects.none {
            it is PermissionEffect.RequestAndroidPermission &&
                it.capability == PermissionCapability.Microphone
        }) return false
        permissionFlow = next.state
        stage = VoicePracticeStage.AwaitingAndroid
        return true
    }

    fun androidResult(
        freshAndroidGrant: Boolean,
        provider: VoiceProviderCapability,
    ): VoicePracticeStage {
        val flow = permissionFlow ?: return stage
        if (flow.stage != PermissionStage.WaitingForNativeResult) return stage
        permissionFlow = null // Result and blocked action are single-use.
        val observed = NativePermissionObservation(
            granted = freshAndroidGrant,
            requestedBefore = true,
            shouldShowRationale = !freshAndroidGrant,
        )
        val step = PermissionMachine.reduce(
            flow, PermissionIntent.NativeResult(flow.generation, observed)
        )
        val authorized = freshAndroidGrant &&
            step.authorizedAction in listOf(
                BlockedAction.StartVoicePractice, BlockedAction.StartVoiceCapture,
            )
        stage = when {
            !authorized -> VoicePracticeStage.PermissionDenied
            provider == VoiceProviderCapability.ProviderUnavailable ->
                VoicePracticeStage.ProviderUnavailable
            provider == VoiceProviderCapability.LanguageUnsupported ->
                VoicePracticeStage.LanguageUnsupported
            // Phase 10 has not enabled, disclosed or authorized remote speech.
            // Consent flags alone are never an authorization in Phase 9.
            provider == VoiceProviderCapability.RemoteConsentRequired ->
                VoicePracticeStage.CloudConsentDeclined
            else -> VoicePracticeStage.ReadyForVoiceHandoff
        }
        return stage
    }

    /** Recreate ActivityResult observation without requesting a second time. */
    fun restorePending() {
        val flow = PermissionMachine.begin(
            PermissionCapability.Microphone,
            PermissionPurpose.ExplicitVoicePractice,
            api,
        )
        permissionFlow = PermissionMachine.reduce(flow, PermissionIntent.Continue).state
        stage = VoicePracticeStage.AwaitingAndroid
    }

    fun recheck(freshAndroidGrant: Boolean) {
        if (!freshAndroidGrant && stage == VoicePracticeStage.ReadyForVoiceHandoff) {
            permissionFlow = null
            stage = VoicePracticeStage.PermissionDenied
        }
    }

    /** Skip, Back and Not now preserve all manual music access. */
    fun manualOnly() {
        permissionFlow = null
        stage = VoicePracticeStage.Idle
    }

    fun requestLaunchFailed() {
        permissionFlow = null
        stage = VoicePracticeStage.PermissionDenied
    }
}
