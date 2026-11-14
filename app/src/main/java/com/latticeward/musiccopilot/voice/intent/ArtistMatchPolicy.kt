package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.data.api.ArtistIndexEntry
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import java.util.Locale
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Issue #11 P04 pure, deterministic, versioned artist scorer.
 * Only PLAY_ARTIST uses fuzzy match; this file MUST NOT be reused by the
 * five #10 transport-command phrase families.
 *
 * No STT confidence, playback/queue authority, database, UI, telemetry, or
 * mutable recognized-speech history belongs to this policy.
 */
object ArtistMatchPolicy {
    const val NORMALIZATION_VERSION = "issue11-artist-key-nfkc-punctuation-v1"
    const val SCORER_VERSION = "issue11-artist-levenshtein-codepoint-v1"
    const val MAX_ARTIST_CODE_POINTS = 80
    // Comparisons at floating-point policy boundaries should not depend on
    // a one-ULP representation difference (e.g., 0.96 - 0.88).
    private const val EPSILON = 1e-12

    /**
     * Symmetric normalization: apply identically to the P02 query and P03
     * locally indexed artist labels. Source Room's normalizedArtist may retain
     * curly apostrophes, so P04 MUST normalize both sides, not query only.
     *
     * Order: NFKC/Unicode spacing via existing Data normalization -> common
     * apostrophe/dash/quote variants -> fixed ROOT casing/spacing.
     * Accents, periods, slash, ampersands and meaningful punctuation stay.
     */
    fun normalizedKey(text: String?): String? {
        val display = LibraryTextNormalization.display(text) ?: return null
        val folded = buildString(display.length) {
            for (character in display) {
                append(
                    when (character) {
                        '\u2018', '\u2019', '\u201b', '\u02bc', '\uff07' -> '\''
                        '\u2010', '\u2011', '\u2012', '\u2013', '\u2014',
                        '\u2015', '\u2212' -> '-'
                        '\u201c', '\u201d', '\u201e', '\u201f' -> '"'
                        else -> character
                    }
                )
            }
        }
        return LibraryTextNormalization.key(folded).takeIf { it.isNotBlank() }
    }

    /** Finite score for a plausible candidate, or null if too dissimilar/long. */
    fun fuzzyScore(requestKey: String, artistKey: String): Double? {
        val a = normalizedKey(requestKey)?.codePoints()?.toArray() ?: return null
        val b = normalizedKey(artistKey)?.codePoints()?.toArray() ?: return null
        if (a.isEmpty() || b.isEmpty() ||
            a.size > MAX_ARTIST_CODE_POINTS || b.size > MAX_ARTIST_CODE_POINTS
        ) return null
        if (a.contentEquals(b)) return 1.0
        val maxLength = max(a.size, b.size)
        val maxEdits = floor(
            (1.0 - VoicePolicy.ARTIST_PLAUSIBLE_SIMILARITY) * maxLength + EPSILON
        ).toInt()
        if (kotlin.math.abs(a.size - b.size) > maxEdits) return null

        // Bounded Levenshtein: O(80) memory and <= 80*80 DP cells per artist.
        // Abort early when every entry in a row already exceeds the plausible
        // edit budget. No unchecked allocation based on library metadata size.
        var previous = IntArray(b.size + 1) { it }
        var current = IntArray(b.size + 1)
        for (i in a.indices) {
            current[0] = i + 1
            var rowMinimum = current[0]
            for (j in b.indices) {
                val substitution = if (a[i] == b[j]) 0 else 1
                current[j + 1] = min(
                    min(previous[j + 1] + 1, current[j] + 1),
                    previous[j] + substitution,
                )
                if (current[j + 1] < rowMinimum) rowMinimum = current[j + 1]
            }
            if (rowMinimum > maxEdits) return null
            val swap = previous
            previous = current
            current = swap
        }
        val distance = previous[b.size]
        if (distance > maxEdits) return null
        return (1.0 - distance.toDouble() / maxLength.toDouble())
            .coerceIn(0.0, 1.0)
    }

    fun isPlausible(score: Double): Boolean =
        score.isFinite() && score + EPSILON >= VoicePolicy.ARTIST_PLAUSIBLE_SIMILARITY

    fun isClear(score: Double): Boolean =
        score.isFinite() && score + EPSILON >= VoicePolicy.ARTIST_CLEAR_SIMILARITY

    fun isSeparated(first: Double, second: Double): Boolean =
        first.isFinite() && second.isFinite() &&
            first >= second && first - second + EPSILON >= VoicePolicy.MIN_TOP_TWO_SEPARATION

    /** Trusted index entry must have a derived group ID and matching key. */
    fun isConsistentIndexEntry(entry: ArtistIndexEntry): Boolean {
        val rawIndexKey = LibraryTextNormalization.key(entry.displayLabel)
        return entry.indexedSongCount > 0 &&
            entry.id.value.startsWith("artist:") &&
            rawIndexKey.isNotBlank() &&
            rawIndexKey == entry.normalizedKey
    }
}

/**
 * The current app has no approved artist-history ranking source. Runtime
 * MUST use None until a privacy-reviewed local playback-history source exists.
 * History is only a secondary tie-break at identical match scores; it MUST
 * NOT authorize direct playback or resolve near-equal ambiguity.
 */
fun interface ArtistHistoryPriority {
    fun priority(groupId: LibraryGroupId): Int
}

object NoArtistHistoryPriority : ArtistHistoryPriority {
    override fun priority(groupId: LibraryGroupId): Int = 0
}

/** Private ranked candidate. Never log private artist identity/label. */
internal class RankedArtist(
    val entry: ArtistIndexEntry,
    val exact: Boolean,
    val similarity: Double,
    val historyPriority: Int,
) {
    fun toPlayArtistCandidate(): PlayArtistCandidate = PlayArtistCandidate(
        artistGroupId = entry.id,
        displayLabel = entry.displayLabel,
        matchEvidence = if (exact) ArtistMatchEvidence.ExactNormalized
            else ArtistMatchEvidence.Fuzzy(similarity, ArtistMatchPolicy.SCORER_VERSION),
    )

    override fun toString(): String = "RankedArtist(redacted)"
}

/** Stable ADR-0005 tuple: exact > fuzzy score > history > opaque group ID. */
internal val rankedArtistComparator: Comparator<RankedArtist> =
    compareByDescending<RankedArtist> { it.exact }
        .thenByDescending { it.similarity }
        .thenByDescending { it.historyPriority }
        .thenBy { it.entry.id.value }

/**
 * Streamed, bounded memory scorer: only best three candidates are retained.
 * The third is used as a safety gate rather than silently hiding a third
 * plausible option behind a one-step, two-option confirmation.
 */
internal class ArtistRankAccumulator(
    private val normalizedTarget: String,
    private val history: ArtistHistoryPriority = NoArtistHistoryPriority,
) {
    private val top = ArrayList<RankedArtist>(3)
    private var exactCount = 0
    private var plausibleCount = 0

    fun accept(entry: ArtistIndexEntry) {
        val normalizedArtist = ArtistMatchPolicy.normalizedKey(entry.displayLabel) ?: return
        val exact = normalizedArtist == normalizedTarget
        val score = if (exact) 1.0 else
            ArtistMatchPolicy.fuzzyScore(normalizedTarget, normalizedArtist) ?: return
        if (!exact && !ArtistMatchPolicy.isPlausible(score)) return
        if (exact) exactCount++
        plausibleCount++
        val candidate = RankedArtist(
            entry = entry, exact = exact, similarity = score,
            historyPriority = history.priority(entry.id),
        )
        top.add(candidate)
        top.sortWith(rankedArtistComparator)
        while (top.size > 3) top.removeAt(top.lastIndex)
    }

    fun finish(revision: Long): ArtistResolution {
        if (top.isEmpty()) return ArtistResolution.NotFound(revision)
        val exact = top.filter { it.exact }
        if (exactCount > 0) {
            if (exactCount == 1) return ArtistResolution.Clear(exact.first()
                .toPlayArtistCandidate(), revision)
            if (exactCount > 2) return ArtistResolution.TooManyPlausible(revision)
            return ArtistResolution.NeedsConfirmation(
                exact.take(2).map { it.toPlayArtistCandidate() }, revision,
            )
        }

        val first = top[0]
        val second = top.getOrNull(1)
        if (ArtistMatchPolicy.isClear(first.similarity) &&
            (second == null ||
                ArtistMatchPolicy.isSeparated(first.similarity, second.similarity))
        ) {
            return ArtistResolution.Clear(first.toPlayArtistCandidate(), revision)
        }
        if (plausibleCount > 2) return ArtistResolution.TooManyPlausible(revision)
        return ArtistResolution.NeedsConfirmation(
            top.take(2).map { it.toPlayArtistCandidate() }, revision,
        )
    }
}

/**
 * Artist matching result is NOT a Voice/Playback decision. P05 applies real
 * provider confidence; P06 verifies MediaStore files and service ACK; P07
 * owns single-use confirmation authority. These results never execute music.
 */
sealed interface ArtistResolution {
    val revision: Long

    class Clear(
        val candidate: PlayArtistCandidate,
        override val revision: Long,
    ) : ArtistResolution {
        override fun toString(): String = "ArtistResolution.Clear(redacted)"
    }

    class NeedsConfirmation(
        candidates: List<PlayArtistCandidate>,
        override val revision: Long,
    ) : ArtistResolution {
        val candidates: List<PlayArtistCandidate> =
            java.util.Collections.unmodifiableList(candidates.toList())

        init { require(this.candidates.size in 1..2) }
        override fun toString(): String = "ArtistResolution.NeedsConfirmation(redacted)"
    }

    data class NotFound(override val revision: Long) : ArtistResolution
    data class TooManyPlausible(override val revision: Long) : ArtistResolution
}
