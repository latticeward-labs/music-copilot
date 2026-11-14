package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.intent.ArtistAmbiguityReason
import com.latticeward.musiccopilot.voice.intent.ArtistDecisionPolicy
import com.latticeward.musiccopilot.voice.intent.ArtistMatchEvidence
import com.latticeward.musiccopilot.voice.intent.ArtistMatchPolicy
import com.latticeward.musiccopilot.voice.intent.ArtistResolution
import com.latticeward.musiccopilot.voice.intent.PlayArtistCandidate
import com.latticeward.musiccopilot.voice.intent.PlayArtistDecision
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhrasePolicy
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhraseResult
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P05. All scenarios are only AUTHORED, NOT_COMPILED / NOT_RUN.
 * Synthetic artist labels, provider scores and typed P04 results only.
 * No live STT, Media3, app BuildConfig task, emulator, or device execution.
 */
class Issue11Phase5ArtistDecisionPolicyTest {
    private val acceptedPhrase = PlayArtistPhrasePolicy.classify("Play songs by Adele")
    private val defaultPolicy = ArtistDecisionPolicy()

    private fun artist(
        name: String = "Adele",
        evidence: ArtistMatchEvidence = ArtistMatchEvidence.ExactNormalized,
    ): PlayArtistCandidate = PlayArtistCandidate(
        LibraryGroupIdentity.artistId(name), name, evidence,
    )

    private fun fuzzy(name: String, similarity: Double,
        version: String = ArtistMatchPolicy.SCORER_VERSION,
    ) = artist(name, ArtistMatchEvidence.Fuzzy(similarity, version))

    private fun clear(candidate: PlayArtistCandidate = artist(),
        revision: Long = 3L,
    ): AppResult<ArtistResolution> =
        AppResult.Success(ArtistResolution.Clear(candidate, revision))

    private fun needs(vararg candidates: PlayArtistCandidate,
        revision: Long = 3L,
    ): AppResult<ArtistResolution> =
        AppResult.Success(ArtistResolution.NeedsConfirmation(candidates.toList(), revision))

    private fun value(result: AppResult<PlayArtistDecision>): PlayArtistDecision {
        assertTrue(result is AppResult.Success<*>)
        return (result as AppResult.Success<PlayArtistDecision>).value
    }

    private fun decide(
        confidence: Double?,
        matched: AppResult<ArtistResolution> = clear(),
        phrase: PlayArtistPhraseResult = acceptedPhrase,
        policy: ArtistDecisionPolicy = defaultPolicy,
    ): PlayArtistDecision = value(policy.decide(phrase, confidence, matched))

    @Test fun namedPolicyAndDefaultUseExistingSafeValues() {
        assertEquals("issue11-play-artist-decision-v1", ArtistDecisionPolicy.POLICY_VERSION)
        assertEquals(0.90, defaultPolicy.directThreshold, 0.0)
        assertFalse(defaultPolicy.usingSafeDefault)
    }

    @Test fun highRealProviderConfidenceAndUniqueExactResultAdmitDirectCandidate() {
        val result = decide(0.95)
        assertTrue(result is PlayArtistDecision.Direct)
        assertEquals(3L, (result as PlayArtistDecision.Direct).expectedLibraryRevision)
        assertEquals(LibraryGroupIdentity.artistId("Adele"), result.candidate.artistGroupId)
    }

    @Test fun exactlyAtDirectThresholdIsAccepted() {
        assertTrue(decide(0.90) is PlayArtistDecision.Direct)
    }

    @Test fun calibratedHighFuzzyClearCanBeAdmittedWithoutExecutingPlayback() {
        val result = decide(0.96, clear(fuzzy("The Weekend", 0.91)))
        assertTrue(result is PlayArtistDecision.Direct)
        assertEquals("The Weekend", (result as PlayArtistDecision.Direct).candidate.displayLabel)
    }

    @Test fun validScoreJustBelowThresholdOffersOneConfirmationNotDirect() {
        val result = decide(0.899999)
        assertTrue(result is PlayArtistDecision.Ambiguous)
        val choice = result as PlayArtistDecision.Ambiguous
        assertEquals(ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE, choice.reason)
        assertEquals(1, choice.candidates.size)
    }

    @Test fun providerZeroEvenWithExactArtistDoesNotAutoPlay() {
        val result = decide(0.0)
        assertTrue(result is PlayArtistDecision.Ambiguous)
        assertEquals(ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE,
            (result as PlayArtistDecision.Ambiguous).reason)
    }

    @Test fun missingMoonshineConfidenceWithUniqueExactCandidateRequiresConfirmation() {
        val result = decide(null)
        assertTrue(result is PlayArtistDecision.Ambiguous)
        val ambiguity = result as PlayArtistDecision.Ambiguous
        assertEquals(ArtistAmbiguityReason.MISSING_PROVIDER_CONFIDENCE, ambiguity.reason)
        assertEquals(1, ambiguity.candidates.size)
        assertEquals(3L, ambiguity.expectedLibraryRevision)
    }

    @Test fun nullProviderConfidenceAndClearFuzzyCandidateStillNeverDirect() {
        val result = decide(null, clear(fuzzy("The Weekend", 0.92)))
        assertTrue(result is PlayArtistDecision.Ambiguous)
        assertEquals(ArtistAmbiguityReason.MISSING_PROVIDER_CONFIDENCE,
            (result as PlayArtistDecision.Ambiguous).reason)
    }

    @Test fun localVadCompleteNeverSubstitutesForArtistProviderConfidence() {
        val result = SpeechRecognitionResult(
            "Play songs by Adele", null, "en-US", localVadEndpointVerified = true,
        )
        val mapped = RestrictedVoiceIntentMapper()
        assertSame(VoiceIntentDecision.Rejected, mapped.map(result))
        val admission = defaultPolicy.decide(
            mapped.classifyPlayArtist(result), result.providerConfidence, clear(),
        )
        assertTrue(value(admission) is PlayArtistDecision.Ambiguous)
    }

    @Test fun invalidNanInfinityAndOutOfRangeConfidenceRejectEvenExactCandidate() {
        for (invalid in listOf(Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -0.001, 1.001)) {
            assertSame(PlayArtistDecision.Unknown, decide(invalid))
        }
    }

    @Test fun highProviderConfidenceCannotSkipTwoArtistConfirmation() {
        val result = decide(1.0, needs(artist("Adelle"), artist("Adelee")))
        assertTrue(result is PlayArtistDecision.Ambiguous)
        val ambiguity = result as PlayArtistDecision.Ambiguous
        assertEquals(2, ambiguity.candidates.size)
        assertEquals(ArtistAmbiguityReason.CLOSE_ARTIST_MATCHES, ambiguity.reason)
    }

    @Test fun lowConfidenceKeepsBoundedTwoCandidateConfirmation() {
        val result = decide(0.50, needs(artist("Adelle"), artist("Adelee")))
        assertTrue(result is PlayArtistDecision.Ambiguous)
        assertEquals(ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE,
            (result as PlayArtistDecision.Ambiguous).reason)
        assertEquals(2, result.candidates.size)
    }

    @Test fun missingConfidenceWithArtistTieKeepsOneStepBoundedResponse() {
        val result = decide(null, needs(artist("Adelle"), artist("Adelee")))
        assertTrue(result is PlayArtistDecision.Ambiguous)
        assertEquals(ArtistAmbiguityReason.MISSING_PROVIDER_CONFIDENCE,
            (result as PlayArtistDecision.Ambiguous).reason)
        assertEquals(2, result.candidates.size)
    }

    @Test fun inconsistentP04ClearBelowClearThresholdMustFailClosed() {
        val result = decide(0.99, clear(fuzzy("Adelle", 0.80)))
        assertSame(PlayArtistDecision.Unknown, result)
    }

    @Test fun fuzzyBelowPlausibleFloorIsNeverPresentedAsAnArtistCandidate() {
        val result = decide(1.0, clear(fuzzy("Other", 0.70)))
        assertSame(PlayArtistDecision.Unknown, result)
    }

    @Test fun scorerVersionMismatchRejectsFuzzyResult() {
        assertSame(PlayArtistDecision.Unknown,
            decide(0.99, clear(fuzzy("Adelle", 0.92, "unapproved-version"))))
    }

    @Test fun exactCandidateDoesNotRequireFabricatedSimilarity() {
        val result = decide(0.90, clear(artist("Adele")))
        assertTrue(result is PlayArtistDecision.Direct)
        assertSame(ArtistMatchEvidence.ExactNormalized,
            (result as PlayArtistDecision.Direct).candidate.matchEvidence)
    }

    @Test fun sameArtistIdTwiceCannotEnterConfirmation() {
        val repeated = artist("Adelle")
        assertSame(PlayArtistDecision.Unknown,
            decide(0.99, needs(repeated, repeated)))
    }

    @Test fun noMatchAfterFullIndexResolutionProducesNotFoundWithoutExecution() {
        assertSame(PlayArtistDecision.NotFound,
            decide(0.95, AppResult.Success(ArtistResolution.NotFound(3L))))
        assertSame(PlayArtistDecision.NotFound,
            decide(null, AppResult.Success(ArtistResolution.NotFound(3L))))
    }

    @Test fun invalidProviderEvidenceDoesNotClaimDefinitiveNotFound() {
        assertSame(PlayArtistDecision.Unknown,
            decide(Double.NaN, AppResult.Success(ArtistResolution.NotFound(3L))))
    }

    @Test fun moreThanTwoPlausibleArtistsFailsSafeWithoutTruncatedConfirmation() {
        assertSame(PlayArtistDecision.Unknown,
            decide(0.99, AppResult.Success(ArtistResolution.TooManyPlausible(3L))))
        assertSame(PlayArtistDecision.Unknown,
            decide(null, AppResult.Success(ArtistResolution.TooManyPlausible(3L))))
    }

    @Test fun staleIndexFailureIsPreservedAndNeverConvertedToNotFound() {
        val failed: AppResult<ArtistResolution> = AppResult.Failure(
            AppError.Library(AppErrorCodes.LIBRARY_BROWSE_STALE,
                Recoverability.RECOVERABLE, RecoveryAction.Retry),
        )
        val result = defaultPolicy.decide(acceptedPhrase, 0.99, failed)
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            (result as AppResult.Failure).error.code)
    }

    @Test fun revokedPermissionFailureIsPreservedEvenWithMissingConfidence() {
        val failed: AppResult<ArtistResolution> = AppResult.Failure(
            AppError.Permission(AppErrorCodes.PERMISSION_REVOKED,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings),
        )
        val result = defaultPolicy.decide(acceptedPhrase, null, failed)
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            (result as AppResult.Failure).error.code)
    }

    @Test fun absentRepositoryOutcomeIsUnavailableRatherThanFabricatedNotFound() {
        val result = defaultPolicy.decide(acceptedPhrase, 0.95, resolution = null)
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            (result as AppResult.Failure).error.code)
    }

    @Test fun unknownAndMalformedPhrasesNeverUseArtistResolution() {
        val cases = listOf(
            PlayArtistPhrasePolicy.classify("Play music by Adele"),
            PlayArtistPhrasePolicy.classify("Play songs by"),
            PlayArtistPhrasePolicy.classify("Play songs by Adele", "fa-IR"),
        )
        for (phrase in cases) {
            assertSame(PlayArtistDecision.Unknown,
                value(defaultPolicy.decide(phrase, 1.0, resolution = null)))
        }
    }

    @Test fun unsupportedStreamingRequestIsNotConvertedToLocalPlay() {
        val phrase = PlayArtistPhrasePolicy.classify("Play songs by Adele on Spotify")
        assertSame(PlayArtistDecision.Unsupported,
            value(defaultPolicy.decide(phrase, 1.0, resolution = null)))
    }

    @Test fun negativeRevisionIsRejectedEvenIfScoreIsPerfect() {
        assertSame(PlayArtistDecision.Unknown,
            decide(1.0, clear(artist("Adele"), revision = -1L)))
    }

    @Test fun invalidDirectConfigUsesSafeDefaultAndNoUnboundedBypass() {
        val policy = ArtistDecisionPolicy(Double.NaN)
        assertTrue(policy.usingSafeDefault)
        assertEquals(0.90, policy.directThreshold, 0.0)
        assertTrue(decide(0.90, policy = policy) is PlayArtistDecision.Direct)
        assertTrue(decide(0.85, policy = policy) is PlayArtistDecision.Ambiguous)
    }

    @Test fun separateArtistConfigDoesNotAlterExistingTransportMapper() {
        val mapping = RestrictedVoiceIntentMapper(0.90)
        assertEquals(0.90, mapping.directThreshold, 0.0)
        assertSame(VoiceIntentDecision.Rejected,
            mapping.map(SpeechRecognitionResult("Play songs by Adele", 1.0, "en-US")))
        val pause = mapping.map(SpeechRecognitionResult("Pause", 0.91, "en-US"))
        assertTrue(pause is VoiceIntentDecision.Direct)
    }

    @Test fun privateArtistIdentifiersDoNotAppearInDecisionDiagnosticMessages() {
        val result = decide(0.99, clear(artist("Synthetic Private Person Name")))
        assertTrue(result is PlayArtistDecision.Direct)
        assertFalse(result.toString().contains("Synthetic"))
        assertFalse(result.toString().contains("artist:"))
        val uncertain = decide(null, clear(artist("Synthetic Private Person Name")))
        assertTrue(uncertain is PlayArtistDecision.Ambiguous)
        assertFalse(uncertain.toString().contains("Synthetic"))
        assertFalse(uncertain.toString().contains("artist:"))
    }
}
