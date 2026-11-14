package com.latticeward.musiccopilot.core.application.voice

import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * Issue #10 P10 — pure foreground/re-entry policy for an explicit PTT owner.
 *
 * A rapid second Mic tap, Bluetooth event, permission result, Activity
 * recreation or a delayed callback is not authority to replace a Voice
 * generation. A new session requires BOTH Idle and a fresh user gesture.
 * Foreground return never implicitly calls start.
 */
object VoiceLifecycleSafetyPolicy {
    fun canAdmitExplicitTap(state: VoiceSessionState): Boolean =
        state == VoiceSessionState.Idle

    fun requiresCancellationAtUiBoundary(state: VoiceSessionState): Boolean =
        state != VoiceSessionState.Idle

    /**
     * Issue #11 P10: permission is a *fresh Android OS observation*, not
     * DataStore, a prior ActivityResult or the microphone button's status.
     * An active generation may never become authorized merely because the
     * app/Activity returned to foreground; it needs a new explicit PTT tap.
     */
    fun canContinueWithFreshOsGrant(
        uiStarted: Boolean,
        recordAudioGranted: Boolean,
    ): Boolean = uiStarted && recordAudioGranted

    /** Interruption/permission loss must invalidate even an in-flight ACK. */
    fun mustCancelOnSafetyBoundary(state: VoiceSessionState): Boolean =
        requiresCancellationAtUiBoundary(state)

    /** No Voice state — not even Executed — is restored from Android Bundle. */
    fun restoredStateAfterRecreation(): VoiceSessionState = VoiceSessionState.Idle
}
