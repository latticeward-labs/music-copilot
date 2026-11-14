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

    /** No Voice state — not even Executed — is restored from Android Bundle. */
    fun restoredStateAfterRecreation(): VoiceSessionState = VoiceSessionState.Idle
}
