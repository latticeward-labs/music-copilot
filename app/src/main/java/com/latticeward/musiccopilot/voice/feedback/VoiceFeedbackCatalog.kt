package com.latticeward.musiccopilot.voice.feedback

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.policy.VoicePolicy

/** Stable, non-personal message IDs. Never include STT text or media metadata. */
enum class VoiceFeedbackKey {
    Play, Pause, Next, Previous, SeekBack, Timeout, NotRecognized, Unavailable
}

/** Replaceable catalog; production resolves Android English string resources. */
fun interface VoiceFeedbackCatalog {
    fun textFor(key: VoiceFeedbackKey): String
}

/** JVM/no-Context fallback; exactly the same English copy as Android resources. */
object EnglishVoiceFeedbackCatalog : VoiceFeedbackCatalog {
    override fun textFor(key: VoiceFeedbackKey): String = when (key) {
        VoiceFeedbackKey.Play -> "Play command accepted."
        VoiceFeedbackKey.Pause -> "Playback paused."
        VoiceFeedbackKey.Next -> "Next track selected."
        VoiceFeedbackKey.Previous -> "Previous command accepted."
        VoiceFeedbackKey.SeekBack -> "Moved back ten seconds."
        VoiceFeedbackKey.Timeout -> "I didn't hear a command."
        VoiceFeedbackKey.NotRecognized -> "Command not recognized."
        VoiceFeedbackKey.Unavailable -> "Use the manual player controls."
    }
}

/**
 * Only a known terminal outcome is eligible for a single short sentence.
 * Retryable recognition failures use the existing visual Retry affordance;
 * they are intentionally silent so the second attempt is not talked over.
 */
object VoiceFeedbackPolicy {
    const val MAX_SPEAK_MS = 2_000L

    fun acknowledged(intent: RestrictedIntent): VoiceFeedbackKey? = when (intent) {
        RestrictedIntent.PLAY -> VoiceFeedbackKey.Play
        RestrictedIntent.PAUSE -> VoiceFeedbackKey.Pause
        RestrictedIntent.NEXT -> VoiceFeedbackKey.Next
        RestrictedIntent.PREVIOUS -> VoiceFeedbackKey.Previous
        RestrictedIntent.SEEK_BACK -> VoiceFeedbackKey.SeekBack
        RestrictedIntent.PLAY_ARTIST -> null
    }

    fun terminalFailure(code: String, canRetry: Boolean): VoiceFeedbackKey? {
        if (canRetry) return null
        return when (code) {
            AppErrorCodes.VOICE_TIMEOUT -> VoiceFeedbackKey.Timeout
            AppErrorCodes.VOICE_NO_MATCH -> VoiceFeedbackKey.NotRecognized
            else -> VoiceFeedbackKey.Unavailable
        }
    }

    fun isShortSentence(text: String): Boolean {
        if (text.isBlank() || text.length > VoicePolicy.MAX_SHORT_FEEDBACK_CHARS ||
            text != text.trim() || !text.endsWith(".") ||
            text.dropLast(1).any { it == '.' || it == '!' || it == '?' || it == '\n' || it == '\r' } ||
            text.any { Character.isISOControl(it) }
        ) return false
        return true
    }

    fun output(
        sessionId: VoiceSessionId,
        key: VoiceFeedbackKey,
        catalog: VoiceFeedbackCatalog,
    ): SpeechOutput? {
        val text = try { catalog.textFor(key) } catch (_: Exception) { return null }
        if (!isShortSentence(text)) return null
        return SpeechOutput(sessionId, "i10_feedback_${key.name.lowercase()}", text)
    }
}
