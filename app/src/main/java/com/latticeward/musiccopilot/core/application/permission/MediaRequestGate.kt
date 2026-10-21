package com.latticeward.musiccopilot.core.application.permission

/**
 * Per-user-gesture, single-flight gate. Android's ActivityResult callback supplies
 * a FRESH OS observation; the Boolean returned by the dialog is not grant truth.
 * No permission result or history is written to DataStore or analytics.
 *
 * The native launcher, Settings and lifecycle are owned by the Activity, not core.
 */
class MediaRequestGate(private val api: Int) {
    private var pending: PermissionFlowState? = null

    val awaitingPlatformResult: Boolean get() = pending != null

    /** Null means a duplicate tap/request; only the first tap may launch Android. */
    fun begin(): PermissionEffect.RequestAndroidPermission? {
        if (pending != null) return null
        val initial = PermissionMachine.begin(
            capability = PermissionCapability.MediaAudio,
            purpose = PermissionPurpose.FirstConsentedMediaScan,
            api = api,
        )
        val step = PermissionMachine.reduce(initial, PermissionIntent.Continue)
        val effect = step.effects.singleOrNull() as? PermissionEffect.RequestAndroidPermission
            ?: return null
        pending = step.state
        return effect
    }

    /**
     * Called only after ActivityResult, with checkSelfPermission queried again.
     * A denied/cancelled/stale callback can never authorize a scan.
     */
    fun complete(observation: NativePermissionObservation): BlockedAction {
        val state = pending ?: return BlockedAction.None
        pending = null
        val step = PermissionMachine.reduce(
            state, PermissionIntent.NativeResult(state.generation, observation),
        )
        return if (observation.granted && step.authorizedAction == BlockedAction.StartScan) {
            BlockedAction.StartScan
        } else BlockedAction.None
    }

    /** Restore an interrupted ActivityResult registration without re-launching it. */
    fun restoreAwaitingNativeResult() {
        if (pending == null) {
            val state = PermissionMachine.begin(
                PermissionCapability.MediaAudio, PermissionPurpose.FirstConsentedMediaScan, api,
            )
            pending = PermissionMachine.reduce(state, PermissionIntent.Continue).state
        }
    }

    fun abandon() { pending = null }
}
