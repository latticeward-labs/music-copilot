package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import java.util.Locale

/** Six reserved MVP API labels; PLAY_ARTIST is NOT executable in Issue #10. */
enum class RestrictedIntent { PLAY, PAUSE, NEXT, PREVIOUS, SEEK_BACK, PLAY_ARTIST }

sealed interface VoiceIntentDecision {
    data class Direct(val intent: RestrictedIntent, val artist: String? = null) : VoiceIntentDecision
    data object Rejected : VoiceIntentDecision
    data object RequiresConfirmation : VoiceIntentDecision
}

/** Pure injectable contract; neither STT nor this mapper executes Media3. */
fun interface VoiceIntentMapper {
    fun map(result: SpeechRecognitionResult): VoiceIntentDecision
}

/**
 * P05: exactly five deterministic transport intents from ONE explicit mic tap.
 * The optional spoken "musico" prefix is never an always-on wake listener.
 * The sixth MVP API enum remains reserved for #11 artist matching and must
 * never be returned as a Direct executable intent by this #10 mapper.
 *
 * An exact known phrase below threshold has an uncertain mapping decision,
 * but #10's coordinator maps that to Failed/manual+one recognition retry,
 * NEVER to an unimplemented confirmation dialog or automatic playback.
 */
class RestrictedVoiceIntentMapper(
    configuredThreshold: Double = VoicePolicy.T_DIRECT,
) : VoiceIntentMapper {
    val directThreshold: Double =
        DirectControlPhrasePolicy.validatedThreshold(configuredThreshold)

    override fun map(result: SpeechRecognitionResult): VoiceIntentDecision {
        // An explicitly detected non-English language overrides keyword matches.
        // null means the provider did not report a detected language; do not
        // silently assign the *requested* language as detected evidence.
        val detected = result.detectedLanguageTag?.trim()?.replace('_', '-')
        if (!detected.isNullOrEmpty() &&
            detected.substringBefore('-').lowercase(Locale.ROOT) != "en"
        ) return VoiceIntentDecision.Rejected

        // Unknown/artist/wake/volume/streaming phrases always reject,
        // regardless of confidence (never ask confirmation for unknown text).
        val intent = DirectControlPhrasePolicy.identify(result.transcript)
            ?: return VoiceIntentDecision.Rejected

        // No local estimate or assumed 1.0 confidence may substitute for
        // missing, NaN, infinity or below-threshold provider confidence.
        if (!DirectControlPhrasePolicy.isDirectConfidence(
                result.providerConfidence, directThreshold
            )
        ) return VoiceIntentDecision.RequiresConfirmation

        return VoiceIntentDecision.Direct(intent)
    }
}
