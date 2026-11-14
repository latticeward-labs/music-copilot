package com.latticeward.musiccopilot.voice.feedback

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.policy.VoicePolicy

/** Stable, non-personal message IDs. Never include STT text or media metadata. */
enum class VoiceFeedbackKey {
    Play, Pause, Next, Previous, SeekBack, Timeout, NotRecognized, Unavailable,
    // P09: all constant, privacy-safe one-sentence English feedback.
    ArtistPlaying, ArtistConfirmOne, ArtistConfirmTwo, ArtistNotFound,
    ArtistUnsupported, ArtistDeclined, ArtistCanceled, ArtistStale,
    ArtistUnavailable, ArtistPlaybackFailure
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
        VoiceFeedbackKey.ArtistPlaying -> "Playing songs by the selected artist."
        VoiceFeedbackKey.ArtistConfirmOne -> "Did you mean this local artist?"
        VoiceFeedbackKey.ArtistConfirmTwo -> "Which of these two local artists did you mean?"
        VoiceFeedbackKey.ArtistNotFound -> "I could not find that artist in your library."
        VoiceFeedbackKey.ArtistUnsupported -> "That artist request is not supported."
        VoiceFeedbackKey.ArtistDeclined -> "No artist selected."
        VoiceFeedbackKey.ArtistCanceled -> "Artist selection canceled."
        VoiceFeedbackKey.ArtistStale -> "Your music library changed, so please choose manually."
        VoiceFeedbackKey.ArtistUnavailable -> "Artist playback is unavailable; use manual controls."
        VoiceFeedbackKey.ArtistPlaybackFailure -> "The player could not start that artist."
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
            AppErrorCodes.VOICE_ARTIST_NOT_FOUND -> VoiceFeedbackKey.ArtistNotFound
            AppErrorCodes.VOICE_ARTIST_UNSUPPORTED -> VoiceFeedbackKey.ArtistUnsupported
            AppErrorCodes.VOICE_ARTIST_DECLINED -> VoiceFeedbackKey.ArtistDeclined
            AppErrorCodes.VOICE_ARTIST_STALE -> VoiceFeedbackKey.ArtistStale
            AppErrorCodes.VOICE_ARTIST_UNAVAILABLE -> VoiceFeedbackKey.ArtistUnavailable
            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED -> VoiceFeedbackKey.ArtistPlaybackFailure
            else -> VoiceFeedbackKey.Unavailable
        }
    }

    fun isShortSentence(text: String): Boolean {
        if (text.isBlank() || text.length > VoicePolicy.MAX_SHORT_FEEDBACK_CHARS ||
            text != text.trim() || (text.last() != '.' && text.last() != '?') ||
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
        val stableKey = when (key) {
            VoiceFeedbackKey.ArtistPlaying -> "i11_feedback_artist_playing"
            VoiceFeedbackKey.ArtistConfirmOne -> "i11_feedback_artist_confirm_one"
            VoiceFeedbackKey.ArtistConfirmTwo -> "i11_feedback_artist_confirm_two"
            VoiceFeedbackKey.ArtistNotFound -> "i11_feedback_artist_not_found"
            VoiceFeedbackKey.ArtistUnsupported -> "i11_feedback_artist_unsupported"
            VoiceFeedbackKey.ArtistDeclined -> "i11_feedback_artist_declined"
            VoiceFeedbackKey.ArtistCanceled -> "i11_feedback_artist_canceled"
            VoiceFeedbackKey.ArtistStale -> "i11_feedback_artist_stale"
            VoiceFeedbackKey.ArtistUnavailable -> "i11_feedback_artist_unavailable"
            VoiceFeedbackKey.ArtistPlaybackFailure -> "i11_feedback_artist_playback_failure"
            else -> "i10_feedback_${key.name.lowercase()}"
        }
        return SpeechOutput(sessionId, stableKey, text)
    }
}
