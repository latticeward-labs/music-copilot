package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.policy.VoicePolicy

/**
 * Issue #11 P05 PURE Execute / Confirm / Reject admission for PLAY_ARTIST.
 *
 * Only maps P02 grammar + P04 artist identity outcome + *actual* STT provider
 * confidence. It does not own session/token, access MediaStore, synthesize
 * ArtistResolution, execute PlaybackGateway, or show a confirmation UI.
 * These remain P06/P07/P08/P12 responsibilities.
 *
 * The five #10 exact transport intents MUST continue through their existing
 * RestrictedVoiceIntentMapper; do not route them through this policy.
 */
class ArtistDecisionPolicy(
    configuredDirectThreshold: Double = VoicePolicy.T_DIRECT,
) {
    companion object {
        const val POLICY_VERSION = "issue11-play-artist-decision-v1"
    }

    val directThreshold: Double =
        ArtistConfidencePolicy.validatedDirectThreshold(configuredDirectThreshold)

    /** True iff a malformed/out-of-range BuildConfig setting was rejected. */
    val usingSafeDefault: Boolean =
        !configuredDirectThreshold.isFinite() ||
            configuredDirectThreshold < VoicePolicy.T_DIRECT_MIN_ALLOWED ||
            configuredDirectThreshold > VoicePolicy.T_DIRECT_MAX_ALLOWED

    /**
     * Null resolution means no approved local artist index outcome was supplied.
     * It is NOT equivalent to a valid empty lookup or NotFound.
     *
     * AppResult.Failure from P03/P04 propagates unchanged: permission, stale,
     * incomplete index and provider errors must not be disguised as NotFound.
     * ArtistResolution.Clear is still merely a match, not Play/Executed.
     */
    fun decide(
        phrase: PlayArtistPhraseResult,
        providerConfidence: Double?,
        resolution: AppResult<ArtistResolution>? = null,
    ): AppResult<PlayArtistDecision> {
        when (phrase) {
            is PlayArtistPhraseResult.NotArtistPhrase,
            is PlayArtistPhraseResult.Malformed,
            is PlayArtistPhraseResult.NonEnglish ->
                return AppResult.Success(PlayArtistDecision.Unknown)

            is PlayArtistPhraseResult.Unsupported ->
                return AppResult.Success(PlayArtistDecision.Unsupported)

            is PlayArtistPhraseResult.Recognized -> Unit
        }

        val available = resolution ?: return unavailable()
        val matched = when (available) {
            is AppResult.Failure -> return available
            is AppResult.Success -> available.value
        }
        if (matched.revision < 0L) return unknown()

        val evidence = ArtistConfidencePolicy.classify(providerConfidence)
        // Corrupt provider score is not evidence of a low-confidence utterance.
        // Do NOT offer a guessed artist or substitute its similarity score.
        if (evidence is ArtistProviderConfidence.Invalid) return unknown()

        return when (matched) {
            is ArtistResolution.NotFound ->
                AppResult.Success(PlayArtistDecision.NotFound)
            is ArtistResolution.TooManyPlausible ->
                // At most TWO choices are supported; silently dropping a third
                // changes product meaning and can confirm the wrong person.
                unknown()
            is ArtistResolution.Clear -> {
                val candidate = matched.candidate
                if (!plausibleEvidence(candidate)) return unknown()

                if (ArtistConfidencePolicy.permitsDirect(evidence, directThreshold) &&
                    strongEnoughForDirect(candidate)
                ) {
                    // P06 must still requery MediaStore and await a real ACK;
                    // this decision alone NEVER means music has played.
                    AppResult.Success(
                        PlayArtistDecision.Direct(candidate, matched.revision),
                    )
                } else {
                    AppResult.Success(
                        PlayArtistDecision.Ambiguous(
                            candidates = listOf(candidate),
                            expectedLibraryRevision = matched.revision,
                            reason = confidenceReason(evidence),
                        ),
                    )
                }
            }
            is ArtistResolution.NeedsConfirmation -> {
                val candidates = matched.candidates
                if (candidates.size !in 1..2 ||
                    candidates.map { it.artistGroupId }.distinct().size != candidates.size ||
                    candidates.any { !plausibleEvidence(it) }
                ) return unknown()

                // A good STT score cannot defeat an artist tie. One bounded
                // confirmation is still necessary even for provider=1.0.
                AppResult.Success(
                    PlayArtistDecision.Ambiguous(
                        candidates = candidates,
                        expectedLibraryRevision = matched.revision,
                        reason = if (evidence is ArtistProviderConfidence.Missing)
                            ArtistAmbiguityReason.MISSING_PROVIDER_CONFIDENCE
                        else if (evidence is ArtistProviderConfidence.Calibrated &&
                            evidence.probability < directThreshold
                        ) ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE
                        else ArtistAmbiguityReason.CLOSE_ARTIST_MATCHES,
                    ),
                )
            }
        }
    }

    private fun confidenceReason(evidence: ArtistProviderConfidence): ArtistAmbiguityReason =
        when (evidence) {
            ArtistProviderConfidence.Missing ->
                ArtistAmbiguityReason.MISSING_PROVIDER_CONFIDENCE
            is ArtistProviderConfidence.Calibrated ->
                ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE
            ArtistProviderConfidence.Invalid ->
                // Invalid is rejected BEFORE this method is reached.
                ArtistAmbiguityReason.LOW_PROVIDER_CONFIDENCE
        }

    /**
     * A P04 fuzzy result is meaningful only if it came from the CURRENT
     * versioned scorer and clears the published plausible floor. A fabricated,
     * NaN, stale-version or sub-plausible match never enters confirmation.
     */
    private fun plausibleEvidence(candidate: PlayArtistCandidate): Boolean =
        when (val match = candidate.matchEvidence) {
            ArtistMatchEvidence.ExactNormalized -> true
            is ArtistMatchEvidence.Fuzzy ->
                match.algorithmVersion == ArtistMatchPolicy.SCORER_VERSION &&
                    ArtistMatchPolicy.isPlausible(match.similarity)
        }

    /**
     * P04 certified a unique/separated Clear result. P05 independently
     * prevents a weak fuzzy similarity from being promoted to Direct even
     * when provider confidence is high. This is NOT STT-score synthesis.
     */
    private fun strongEnoughForDirect(candidate: PlayArtistCandidate): Boolean =
        when (val match = candidate.matchEvidence) {
            ArtistMatchEvidence.ExactNormalized -> true
            is ArtistMatchEvidence.Fuzzy ->
                match.algorithmVersion == ArtistMatchPolicy.SCORER_VERSION &&
                    ArtistMatchPolicy.isClear(match.similarity)
        }

    private fun unknown(): AppResult<PlayArtistDecision> =
        AppResult.Success(PlayArtistDecision.Unknown)

    private fun unavailable(): AppResult<PlayArtistDecision> =
        AppResult.Failure(
            AppError.Library(
                code = AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                recoverability = Recoverability.RECOVERABLE,
                recoveryAction = RecoveryAction.ManualFallback,
            ),
        )
}
