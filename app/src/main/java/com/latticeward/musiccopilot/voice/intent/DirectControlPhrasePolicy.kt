package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import java.util.Locale

/**
 * Issue #10 P05: exact English phrase dictionary for explicit push-to-talk.
 * This is neither a wake-word detector nor a free-form/NLU/fuzzy matcher.
 * 'musico' is an optional *spoken prefix after a real microphone tap*.
 */
object DirectControlPhrasePolicy {
    const val GRAMMAR_VERSION = "issue10-five-controls-v1"
    private const val MAX_TRANSCRIPT_CHARS = 120
    private val whitespace = Regex("\\s+")

    /** Approved, finite phrase families only. No nearest-intent fallback. */
    val approvedPhrases: Map<String, RestrictedIntent> = buildMap {
        for (text in listOf("play", "resume", "resume music", "play music")) {
            put(text, RestrictedIntent.PLAY)
        }
        for (text in listOf("pause", "pause music", "stop music")) {
            put(text, RestrictedIntent.PAUSE)
        }
        for (text in listOf("next", "next song", "next track", "skip song")) {
            put(text, RestrictedIntent.NEXT)
        }
        for (text in listOf("previous", "previous song", "previous track")) {
            put(text, RestrictedIntent.PREVIOUS)
        }
        for (text in listOf(
            "go back 10 seconds", "back 10 seconds", "seek back 10 seconds",
            "go back ten seconds", "back ten seconds"
        )) {
            put(text, RestrictedIntent.SEEK_BACK)
        }
    }

    /** Accepts whitespace variation and one final STT sentence mark, not questions. */
    fun normalize(raw: String): String? {
        if (raw.isBlank() || raw.length > MAX_TRANSCRIPT_CHARS) return null
        if (raw.any { Character.isISOControl(it) && !it.isWhitespace() }) return null
        var normalized = raw.trim().lowercase(Locale.ROOT)
            .replace(whitespace, " ")
            .trimEnd('.', '!')
            .trim()
        if (normalized.startsWith("musico ")) {
            normalized = normalized.removePrefix("musico ").trim()
        }
        // A bare "musico", repeated invocation, or any extra words reject.
        return normalized.takeIf { it.isNotEmpty() }
    }

    fun identify(raw: String): RestrictedIntent? =
        normalize(raw)?.let(approvedPhrases::get)

    /** BuildConfig may tune the threshold, but cannot loosen bounds. */
    fun validatedThreshold(configured: Double): Double =
        configured.takeIf {
            it.isFinite() &&
                it >= VoicePolicy.T_DIRECT_MIN_ALLOWED &&
                it <= VoicePolicy.T_DIRECT_MAX_ALLOWED
        } ?: VoicePolicy.T_DIRECT

    fun isDirectConfidence(value: Double?, configuredThreshold: Double): Boolean =
        value != null && value.isFinite() &&
            value in validatedThreshold(configuredThreshold)..1.0
}
