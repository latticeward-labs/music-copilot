package com.latticeward.musiccopilot.spike.voice

import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

class DeterministicIntentMapper(
    private val confidencePolicy: ConfidencePolicy,
    private val artistPolicy: ArtistMatchPolicy = ArtistMatchPolicy(),
) {
    private val controlPhrases = linkedMapOf(
        VoiceIntent.PLAY to setOf("play", "resume", "resume music"),
        VoiceIntent.PAUSE to setOf("pause", "pause music", "stop music"),
        VoiceIntent.NEXT to setOf("next", "next song", "next track"),
        VoiceIntent.PREVIOUS to setOf("previous", "previous song", "previous track"),
        VoiceIntent.SEEK_BACK to setOf(
            "go back 10 seconds",
            "seek back 10 seconds",
            "back 10 seconds",
        ),
    )

    private val unsupportedTokens = setOf(
        "shuffle",
        "repeat",
        "volume",
        "playlist",
        "radio",
        "like",
        "download",
        "stream",
    )

    fun map(
        transcript: String,
        providerConfidence: Double?,
        artists: List<Artist> = emptyList(),
    ): MappingResult {
        val normalized = normalize(transcript)
        if (normalized.isEmpty()) return MappingResult.Unknown(MappingFailureReason.EMPTY)

        if (unsupportedTokens.any { token -> normalized.split(' ').contains(token) }) {
            return MappingResult.Unsupported(MappingFailureReason.UNSUPPORTED_INTENT)
        }

        controlPhrases.entries.firstOrNull { normalized in it.value }?.let { (intent, _) ->
            val candidate = IntentCandidate(intent = intent, score = normalizedConfidence(providerConfidence))
            return confidenceDecision(candidate, providerConfidence)
        }

        val artistSlot = extractArtistSlot(normalized)
        if (artistSlot != null) {
            if (artistSlot.isBlank()) {
                return MappingResult.Unknown(MappingFailureReason.MISSING_ARTIST)
            }
            return resolveArtist(artistSlot, providerConfidence, artists)
        }

        return MappingResult.Unknown(MappingFailureReason.UNKNOWN_PHRASE)
    }

    private fun confidenceDecision(
        candidate: IntentCandidate,
        providerConfidence: Double?,
    ): MappingResult {
        val confidence = normalizedConfidence(providerConfidence)
        return when {
            confidence >= confidencePolicy.directThreshold -> MappingResult.Recognized(candidate)
            confidence >= confidencePolicy.ambiguityThreshold -> MappingResult.Ambiguous(
                options = listOf(candidate),
                reason = MappingFailureReason.LOW_CONFIDENCE,
            )
            else -> MappingResult.Unknown(MappingFailureReason.LOW_CONFIDENCE)
        }
    }

    private fun resolveArtist(
        requestedName: String,
        providerConfidence: Double?,
        artists: List<Artist>,
    ): MappingResult {
        val exact = artists.filter { normalize(it.displayName) == requestedName }
            .sortedWith(compareByDescending<Artist> { it.historyRank }.thenBy { it.stableId })
        if (exact.size == 1) {
            return confidenceDecision(
                IntentCandidate(VoiceIntent.PLAY_ARTIST, exact.single(), 1.0),
                providerConfidence,
            )
        }
        if (exact.size > 1) {
            return MappingResult.Ambiguous(
                exact.take(2).map { IntentCandidate(VoiceIntent.PLAY_ARTIST, it, 1.0) },
                MappingFailureReason.LOW_CONFIDENCE,
            )
        }

        val scored = artists.map { artist ->
            IntentCandidate(
                intent = VoiceIntent.PLAY_ARTIST,
                artist = artist,
                score = similarity(requestedName, normalize(artist.displayName)),
            )
        }.filter { it.score >= artistPolicy.plausibleThreshold }
            .sortedWith(
                compareByDescending<IntentCandidate> { it.score }
                    .thenByDescending { it.artist?.historyRank ?: 0 }
                    .thenBy { it.artist?.stableId.orEmpty() },
            )

        if (scored.isEmpty()) {
            return MappingResult.Unknown(MappingFailureReason.ARTIST_NOT_FOUND)
        }

        val top = scored.first()
        val runnerUp = scored.getOrNull(1)
        val clearSeparation = runnerUp == null || top.score - runnerUp.score >= artistPolicy.minimumSeparation
        val providerIsCertain = normalizedConfidence(providerConfidence) >= confidencePolicy.directThreshold
        if (top.score >= artistPolicy.directThreshold && clearSeparation && providerIsCertain) {
            return MappingResult.Recognized(top)
        }

        return MappingResult.Ambiguous(
            options = scored.take(2),
            reason = MappingFailureReason.LOW_CONFIDENCE,
        )
    }

    private fun extractArtistSlot(normalized: String): String? {
        val prefixes = listOf("play songs by ", "play music by ", "play artist ")
        val prefix = prefixes.firstOrNull(normalized::startsWith)
        if (prefix != null) return normalized.removePrefix(prefix).trim()
        return if (normalized in setOf("play songs by", "play music by", "play artist")) "" else null
    }

    private fun normalizedConfidence(value: Double?): Double =
        value?.takeIf { it.isFinite() && it in 0.0..1.0 } ?: 0.0

    companion object {
        fun normalize(value: String): String {
            val decomposed = Normalizer.normalize(value, Normalizer.Form.NFKD)
            return decomposed
                .replace(Regex("\\p{M}+"), "")
                .lowercase(Locale.ROOT)
                .replace('’', '\'')
                .replace(Regex("[^a-z0-9' ]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        fun similarity(left: String, right: String): Double {
            if (left == right) return 1.0
            if (left.isEmpty() || right.isEmpty()) return 0.0
            val distance = levenshtein(left, right)
            return 1.0 - distance.toDouble() / max(left.length, right.length)
        }

        private fun levenshtein(left: String, right: String): Int {
            var previous = IntArray(right.length + 1) { it }
            for (leftIndex in left.indices) {
                val current = IntArray(right.length + 1)
                current[0] = leftIndex + 1
                for (rightIndex in right.indices) {
                    val substitution = if (left[leftIndex] == right[rightIndex]) 0 else 1
                    current[rightIndex + 1] = minOf(
                        current[rightIndex] + 1,
                        previous[rightIndex + 1] + 1,
                        previous[rightIndex] + substitution,
                    )
                }
                previous = current
            }
            return previous[right.length]
        }
    }
}
