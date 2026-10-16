package com.latticeward.musiccopilot.core.application.permission

/**
 * Android-owned truth. These values are PRESENTATION/ORCHESTRATION projections,
 * never persisted grant authority and never copied to analytics.
 */
enum class PermissionCapability { MediaAudio, Microphone, Notifications }
enum class PermissionStatus {
    Unknown, NotRequested, Granted, DeniedRequestable, PermanentlyDenied, Revoked, NotApplicable
}

enum class PermissionPurpose {
    FirstConsentedMediaScan, ExplicitMicrophoneTap, ExplicitVoicePractice, AcknowledgedPlaybackControls
}

data class NativePermissionObservation(
    val granted: Boolean,
    val requestedBefore: Boolean,
    val shouldShowRationale: Boolean,
    val previouslyGranted: Boolean = false,
)

/** Android SDK/API mapping, not an OS request or a replacement for runtime checks. */
object PermissionPlatformPolicy {
    fun manifestPermission(capability: PermissionCapability, api: Int): String? = when (capability) {
        PermissionCapability.MediaAudio ->
            if (api >= 33) "android.permission.READ_MEDIA_AUDIO" else "android.permission.READ_EXTERNAL_STORAGE"
        PermissionCapability.Microphone -> "android.permission.RECORD_AUDIO"
        PermissionCapability.Notifications ->
            if (api >= 33) "android.permission.POST_NOTIFICATIONS" else null
    }

    fun project(
        capability: PermissionCapability,
        api: Int,
        observation: NativePermissionObservation,
    ): PermissionStatus {
        if (manifestPermission(capability, api) == null) return PermissionStatus.NotApplicable
        return when {
            observation.granted -> PermissionStatus.Granted
            observation.previouslyGranted -> PermissionStatus.Revoked
            !observation.requestedBefore -> PermissionStatus.NotRequested
            observation.shouldShowRationale -> PermissionStatus.DeniedRequestable
            else -> PermissionStatus.PermanentlyDenied
        }
    }

    fun compatible(capability: PermissionCapability, purpose: PermissionPurpose): Boolean =
        when (capability) {
            PermissionCapability.MediaAudio -> purpose == PermissionPurpose.FirstConsentedMediaScan
            PermissionCapability.Microphone ->
                purpose == PermissionPurpose.ExplicitMicrophoneTap ||
                    purpose == PermissionPurpose.ExplicitVoicePractice
            PermissionCapability.Notifications ->
                purpose == PermissionPurpose.AcknowledgedPlaybackControls
        }
}

/** Explicit, one-shot user gestures are required to create an active permission flow. */
enum class PermissionStage {
    Explanation, WaitingForNativeResult, Recovery, WaitingForSettingsReturn,
    WaitingForPlatformRecheck, Ready, ManualFallback
}

data class PermissionFlowState(
    val capability: PermissionCapability,
    val purpose: PermissionPurpose,
    val api: Int,
    val stage: PermissionStage = PermissionStage.Explanation,
    val status: PermissionStatus = PermissionStatus.Unknown,
    val generation: Long = 0,
)

sealed interface PermissionIntent {
    data object Continue : PermissionIntent
    data object Retry : PermissionIntent
    data object NotNow : PermissionIntent
    data object OpenSettings : PermissionIntent
    data object ReturnedFromSettings : PermissionIntent
    data class NativeResult(
        val generation: Long,
        val observation: NativePermissionObservation,
    ) : PermissionIntent
    data class Rechecked(
        val generation: Long,
        val observation: NativePermissionObservation,
    ) : PermissionIntent
    data object Revoked : PermissionIntent
}

sealed interface PermissionEffect {
    data class RequestAndroidPermission(
        val capability: PermissionCapability,
        val generation: Long,
    ) : PermissionEffect
    data object OpenAndroidSettings : PermissionEffect
    data class RecheckAndroidPermission(val generation: Long) : PermissionEffect
    data object ContinueBlockedAction : PermissionEffect
    data object PreserveManualControls : PermissionEffect
    data object ReconcileRevokedMedia : PermissionEffect
    data object CancelVoiceGeneration : PermissionEffect
}

data class PermissionStep(
    val state: PermissionFlowState,
    val effects: List<PermissionEffect> = emptyList(),
)

/**
 * The only trusted permission update comes from a fresh OS observation in P04/P06.
 * A result generation mismatch or onResume without re-query has no side effect.
 */
object PermissionMachine {
    fun begin(
        capability: PermissionCapability,
        purpose: PermissionPurpose,
        api: Int,
    ): PermissionFlowState {
        require(api >= 29) { "Android 10+ required" }
        require(PermissionPlatformPolicy.compatible(capability, purpose)) {
            "Permission cannot be requested for an unrelated purpose"
        }
        return PermissionFlowState(capability, purpose, api)
    }

    fun reduce(state: PermissionFlowState, intent: PermissionIntent): PermissionStep {
        if (intent == PermissionIntent.Revoked) {
            return PermissionStep(
                state.copy(stage = PermissionStage.Recovery, status = PermissionStatus.Revoked),
                when (state.capability) {
                    PermissionCapability.MediaAudio -> listOf(
                        PermissionEffect.ReconcileRevokedMedia,
                        PermissionEffect.PreserveManualControls,
                    )
                    PermissionCapability.Microphone -> listOf(
                        PermissionEffect.CancelVoiceGeneration,
                        PermissionEffect.PreserveManualControls,
                    )
                    PermissionCapability.Notifications -> listOf(PermissionEffect.PreserveManualControls)
                },
            )
        }
        return when (intent) {
            PermissionIntent.Continue ->
                if (state.stage == PermissionStage.Explanation) request(state) else PermissionStep(state)
            PermissionIntent.Retry -> if (
                state.stage == PermissionStage.Recovery &&
                (state.status == PermissionStatus.DeniedRequestable ||
                    state.status == PermissionStatus.NotRequested ||
                    state.status == PermissionStatus.Revoked)
            ) PermissionStep(state.copy(stage = PermissionStage.Explanation))
            else PermissionStep(state)
            PermissionIntent.NotNow -> if (
                state.stage == PermissionStage.Explanation ||
                state.stage == PermissionStage.Recovery
            ) PermissionStep(
                state.copy(stage = PermissionStage.ManualFallback),
                listOf(PermissionEffect.PreserveManualControls),
            ) else PermissionStep(state)
            PermissionIntent.OpenSettings -> if (
                state.stage == PermissionStage.Recovery &&
                (state.status == PermissionStatus.PermanentlyDenied ||
                    state.status == PermissionStatus.Revoked)
            ) PermissionStep(
                state.copy(stage = PermissionStage.WaitingForSettingsReturn),
                listOf(PermissionEffect.OpenAndroidSettings),
            ) else PermissionStep(state)
            PermissionIntent.ReturnedFromSettings ->
                if (state.stage == PermissionStage.WaitingForSettingsReturn) {
                    val next = state.generation + 1
                    PermissionStep(
                        state.copy(stage = PermissionStage.WaitingForPlatformRecheck, generation = next),
                        listOf(PermissionEffect.RecheckAndroidPermission(next)),
                    )
                } else PermissionStep(state)
            is PermissionIntent.NativeResult -> if (
                state.stage == PermissionStage.WaitingForNativeResult &&
                state.generation == intent.generation
            ) observe(state, intent.observation) else PermissionStep(state)
            is PermissionIntent.Rechecked -> if (
                state.stage == PermissionStage.WaitingForPlatformRecheck &&
                state.generation == intent.generation
            ) observe(state, intent.observation) else PermissionStep(state)
            PermissionIntent.Revoked -> PermissionStep(state)
        }
    }

    private fun request(state: PermissionFlowState): PermissionStep {
        if (PermissionPlatformPolicy.manifestPermission(state.capability, state.api) == null) {
            return PermissionStep(
                state.copy(stage = PermissionStage.Ready, status = PermissionStatus.NotApplicable),
                listOf(PermissionEffect.ContinueBlockedAction),
            )
        }
        val next = state.generation + 1
        return PermissionStep(
            state.copy(stage = PermissionStage.WaitingForNativeResult, generation = next),
            listOf(PermissionEffect.RequestAndroidPermission(state.capability, next)),
        )
    }

    private fun observe(
        state: PermissionFlowState,
        observation: NativePermissionObservation,
    ): PermissionStep {
        val result = PermissionPlatformPolicy.project(state.capability, state.api, observation)
        return if (result == PermissionStatus.Granted || result == PermissionStatus.NotApplicable) {
            PermissionStep(
                state.copy(status = result, stage = PermissionStage.Ready),
                listOf(PermissionEffect.ContinueBlockedAction),
            )
        } else {
            val extras = when (result) {
                PermissionStatus.Revoked -> if (state.capability == PermissionCapability.MediaAudio) {
                    listOf(PermissionEffect.ReconcileRevokedMedia)
                } else emptyList()
                else -> emptyList()
            }
            PermissionStep(
                state.copy(status = result, stage = PermissionStage.Recovery),
                extras + PermissionEffect.PreserveManualControls,
            )
        }
    }
}
