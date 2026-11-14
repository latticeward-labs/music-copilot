package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.data.api.ArtistIndexEntry
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.voice.intent.ArtistHistoryPriority
import com.latticeward.musiccopilot.voice.intent.ArtistMatchEvidence
import com.latticeward.musiccopilot.voice.intent.ArtistMatchPolicy
import com.latticeward.musiccopilot.voice.intent.ArtistRankAccumulator
import com.latticeward.musiccopilot.voice.intent.ArtistResolution
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P04 PURE policy fixtures, AUTHORED ONLY — NOT COMPILED, NOT_RUN.
 * Owner explicitly permits tests/build ONLY at integrated P13.
 */
class Issue11Phase4ArtistMatchPolicyTest {
    private fun entry(name: String): ArtistIndexEntry =
        ArtistIndexEntry(LibraryGroupIdentity.artistId(name), name,
            LibraryTextNormalization.key(name), 1)

    private fun classify(
        target: String,
        vararg names: String,
        history: ArtistHistoryPriority = ArtistHistoryPriority { 0 },
    ): ArtistResolution {
        val rank = ArtistRankAccumulator(ArtistMatchPolicy.normalizedKey(target)!!, history)
        names.forEach { rank.accept(entry(it)) }
        return rank.finish(9L)
    }

    @Test fun versionsAreStableAndMatchApprovedPolicyBounds() {
        assertEquals("issue11-artist-key-nfkc-punctuation-v1",
            ArtistMatchPolicy.NORMALIZATION_VERSION)
        assertEquals("issue11-artist-levenshtein-codepoint-v1",
            ArtistMatchPolicy.SCORER_VERSION)
        assertEquals(80, ArtistMatchPolicy.MAX_ARTIST_CODE_POINTS)
        assertEquals(0.88, VoicePolicy.ARTIST_CLEAR_SIMILARITY, 0.0)
        assertEquals(0.72, VoicePolicy.ARTIST_PLAUSIBLE_SIMILARITY, 0.0)
        assertEquals(0.08, VoicePolicy.MIN_TOP_TWO_SEPARATION, 0.0)
    }

    @Test fun normalizationIsSymmetricForCurlyApostrophesAndExtraSpaces() {
        assertEquals("l'arc en ciel",
            ArtistMatchPolicy.normalizedKey("  L’Arc   en  Ciel "))
        assertEquals(ArtistMatchPolicy.normalizedKey("L’Arc"),
            ArtistMatchPolicy.normalizedKey("l'arc"))
        assertEquals(ArtistMatchPolicy.normalizedKey("Alpha–Beta"),
            ArtistMatchPolicy.normalizedKey("alpha-beta"))
    }

    @Test fun accentsAndSemanticPunctuationArePreserved() {
        assertEquals("adèle", ArtistMatchPolicy.normalizedKey("Adèle"))
        assertNotEquals(ArtistMatchPolicy.normalizedKey("Adèle"),
            ArtistMatchPolicy.normalizedKey("Adele"))
        assertEquals("ac/dc", ArtistMatchPolicy.normalizedKey("AC/DC"))
        assertEquals("r.e.m.", ArtistMatchPolicy.normalizedKey("R.E.M."))
        assertNull(ArtistMatchPolicy.normalizedKey("   "))
    }

    @Test fun exactCanonicalNameHasScoreOne() {
        assertEquals(1.0, ArtistMatchPolicy.fuzzyScore("l'arc", "L’Arc")!!, 0.0)
    }

    @Test fun normalizedEditDistanceFavorsOneCharacterMistake() {
        val score = ArtistMatchPolicy.fuzzyScore("the weeknd", "the weekend")!!
        assertTrue(score >= VoicePolicy.ARTIST_CLEAR_SIMILARITY)
        assertTrue(score <= 1.0)
    }

    @Test fun plausibleButBelowClearThresholdNeedsConfirmation() {
        val score = ArtistMatchPolicy.fuzzyScore("adele", "adelle")!!
        assertTrue(ArtistMatchPolicy.isPlausible(score))
        assertFalse(ArtistMatchPolicy.isClear(score))
        assertTrue(classify("adele", "Adelle") is ArtistResolution.NeedsConfirmation)
    }

    @Test fun unrelatedNamesAndExcessLengthCannotMakePlausibleCandidate() {
        assertNull(ArtistMatchPolicy.fuzzyScore("adele", "xyz"))
        assertNull(ArtistMatchPolicy.fuzzyScore("a".repeat(81), "a".repeat(81)))
        assertNull(ArtistMatchPolicy.fuzzyScore("a", "a".repeat(80)))
    }

    @Test fun directScoreBoundaryAndSeparationAreDeterministic() {
        assertTrue(ArtistMatchPolicy.isClear(0.88))
        assertFalse(ArtistMatchPolicy.isClear(0.8799))
        assertTrue(ArtistMatchPolicy.isPlausible(0.72))
        assertFalse(ArtistMatchPolicy.isPlausible(0.7199))
        assertTrue(ArtistMatchPolicy.isSeparated(0.96, 0.88))
        assertFalse(ArtistMatchPolicy.isSeparated(0.95, 0.88))
    }

    @Test fun validIndexKeyMustCorrespondToTheArtistLabel() {
        assertTrue(ArtistMatchPolicy.isConsistentIndexEntry(entry("Alpha")))
        assertFalse(ArtistMatchPolicy.isConsistentIndexEntry(
            ArtistIndexEntry(LibraryGroupIdentity.artistId("Alpha"), "Alpha", "beta", 1)))
    }

    @Test fun uniqueExactDominatesFuzzyRegardlessOfHistory() {
        val preferredOther = LibraryGroupIdentity.artistId("Adelle")
        val decision = classify("Adele", "Adelle", "Adele",
            history = ArtistHistoryPriority { id -> if (id == preferredOther) 500 else 0 })
        assertTrue(decision is ArtistResolution.Clear)
        val candidate = (decision as ArtistResolution.Clear).candidate
        assertEquals(LibraryGroupIdentity.artistId("Adele"), candidate.artistGroupId)
        assertSame(ArtistMatchEvidence.ExactNormalized, candidate.matchEvidence)
    }

    @Test fun canonicalEquivalentArtistGroupsCannotSilentlyPickFirstRow() {
        val decision = classify("L'Arc", "L’Arc", "L'Arc")
        assertTrue(decision is ArtistResolution.NeedsConfirmation)
        assertEquals(2, (decision as ArtistResolution.NeedsConfirmation).candidates.size)
    }

    @Test fun clearlyHighFuzzyWithNoSecondCandidateMayBeClear() {
        val decision = classify("the weeknd", "The Weekend")
        assertTrue(decision is ArtistResolution.Clear)
        val fuzzy = (decision as ArtistResolution.Clear).candidate.matchEvidence
        assertTrue(fuzzy is ArtistMatchEvidence.Fuzzy)
        assertEquals(ArtistMatchPolicy.SCORER_VERSION,
            (fuzzy as ArtistMatchEvidence.Fuzzy).algorithmVersion)
    }

    @Test fun nearEqualFuzzyCandidatesStayAmbiguousNotAutomaticallyPicked() {
        val decision = classify("adele", "Adelle", "Adelee")
        assertTrue(decision is ArtistResolution.NeedsConfirmation)
        assertEquals(2, (decision as ArtistResolution.NeedsConfirmation).candidates.size)
    }

    @Test fun localHistoryIsSecondaryTieBreakOnlyAndCannotSuppressConfirmation() {
        val favored = LibraryGroupIdentity.artistId("Adelee")
        val decision = classify("adele", "Adelle", "Adelee",
            history = ArtistHistoryPriority { id -> if (id == favored) 8 else 0 })
        assertTrue(decision is ArtistResolution.NeedsConfirmation)
        val candidates = (decision as ArtistResolution.NeedsConfirmation).candidates
        assertEquals(favored, candidates[0].artistGroupId)
    }

    @Test fun absentHistoryHasStableIdSortForTiedScores() {
        val one = classify("adele", "Adelle", "Adelee") as ArtistResolution.NeedsConfirmation
        val two = classify("adele", "Adelee", "Adelle") as ArtistResolution.NeedsConfirmation
        assertEquals(one.candidates.map { it.artistGroupId },
            two.candidates.map { it.artistGroupId })
        assertEquals(one.candidates.map { it.displayLabel },
            two.candidates.map { it.displayLabel })
    }

    @Test fun threePlausibleSimilarArtistsFailClosedRatherThanHideAChoice() {
        val decision = classify("adele", "Adelle", "Adelee", "Adela")
        assertTrue(decision is ArtistResolution.TooManyPlausible)
    }

    @Test fun unrelatedCandidatesReturnNotFoundWithoutChangingPlayback() {
        val decision = classify("adele", "Bach", "Mozart")
        assertEquals(ArtistResolution.NotFound(9L), decision)
    }

    @Test fun emptyArtistIndexIsNotFoundOnlyForCompleteSnapshot() {
        val decision = classify("adele")
        assertEquals(ArtistResolution.NotFound(9L), decision)
    }

    @Test fun privateArtistNamesNeverAppearInOutcomeDiagnostics() {
        val decision = classify("Adele", "Adelle")
        assertFalse(decision.toString().contains("Adelle"))
        val c = (decision as ArtistResolution.NeedsConfirmation).candidates.single()
        assertFalse(c.toString().contains("Adelle"))
    }

    @Test fun identicalScoringIsDeterministicAcrossRepeatedInvocations() {
        val labels = arrayOf("Adelle", "Adelee", "Bach")
        val baseline = classify("adele", *labels).toString()
        repeat(10) {
            assertEquals(baseline, classify("adele", *labels).toString())
        }
    }
}
