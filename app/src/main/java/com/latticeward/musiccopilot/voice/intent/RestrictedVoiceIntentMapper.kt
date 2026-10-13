package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import java.util.Locale

enum class RestrictedIntent { PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST }

sealed interface VoiceIntentDecision {
    data class Direct(val intent: RestrictedIntent, val artist: String? = null) : VoiceIntentDecision
    data object Rejected : VoiceIntentDecision
    data object RequiresConfirmation : VoiceIntentDecision
}

/** Injectable pure seam; no SDK, Player, persistence, network or provider-specific classification. */
fun interface VoiceIntentMapper {
    fun map(result: SpeechRecognitionResult): VoiceIntentDecision
}

/**
 * Deliberately bounded six-intent MVP grammar. Voice input is accepted only
 * after explicit push-to-talk and (for this app policy) the leading 'musico'
 * invocation phrase. Absence, unsupported wording or unreliable confidence
 * never leads to a guessed playback operation.
 */
class RestrictedVoiceIntentMapper : VoiceIntentMapper {
    override fun map(result: SpeechRecognitionResult): VoiceIntentDecision {
        val confidence = result.providerConfidence
        if (confidence == null || !confidence.isFinite() ||
            confidence < VoicePolicy.T_DIRECT || confidence > 1.0)
            return VoiceIntentDecision.RequiresConfirmation
        val phrase = result.transcript.lowercase(Locale.ROOT)
            .trim().replace(Regex("\\s+"), " ")
        if (!phrase.startsWith("musico ")) return VoiceIntentDecision.Rejected
        val action = phrase.removePrefix("musico ").trim()
        val intent = when (action) {
            "play", "resume", "resume music" -> RestrictedIntent.PLAY
            "pause", "pause music", "stop music" -> RestrictedIntent.PAUSE
            "next", "next song", "next track" -> RestrictedIntent.NEXT
            "previous", "previous song", "previous track" -> RestrictedIntent.PREVIOUS
            "go back 10 seconds", "back 10 seconds" -> RestrictedIntent.SEEK_BACK
            else -> null
        }
        if (intent != null) return VoiceIntentDecision.Direct(intent)
        val artist = action.removePrefix("play songs by ").takeIf {
            action.startsWith("play songs by ") && it.isNotBlank() &&
                it.length <= 80
        } ?: return VoiceIntentDecision.Rejected
        return VoiceIntentDecision.Direct(RestrictedIntent.PLAY_ARTIST, artist)
    }
}

