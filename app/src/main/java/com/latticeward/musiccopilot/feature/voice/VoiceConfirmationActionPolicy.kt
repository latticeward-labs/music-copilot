package com.latticeward.musiccopilot.feature.voice

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState

/**
 * P08: pure UI callback gate; the application-scoped Coordinator remains
 * authoritative for single-use token/generation/revision and playback ACK.
 *
 * Checks use the latest StateFlow value at click time, never a captured
 * composable snapshot, and do not read Room/MediaStore or start a recorder.
 * Also refuse replies from a hidden or replaced origin/screen.
 */
internal object VoiceConfirmationActionPolicy {
    fun mayRespond(
        latest: VoiceSessionState,
        token: VoiceCandidateToken,
        origin: VoiceOrigin,
        originActive: Boolean,
    ): Boolean = originActive &&
        latest is VoiceSessionState.Confirming &&
        latest.origin == origin &&
        latest.token == token
}
