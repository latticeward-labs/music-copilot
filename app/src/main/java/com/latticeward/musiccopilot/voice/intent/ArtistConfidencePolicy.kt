package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.voice.policy.VoicePolicy

/**
 * Issue #11 P05. PURE, provider-independent confidence classification.
 *
 * Confidence is STT provider evidence, NOT an artist fuzzy-match similarity,
 * local-VAD completion flag, transcript length, or historical playback score.
 * Moonshine Tiny currently reports null: that remains Missing.
 *
 * The distinct artist BuildConfig value is read ONLY by the DI adapter;
 * this pure class does not import Android or BuildConfig.
 */
object ArtistConfidencePolicy {
    const val POLICY_VERSION = "issue11-artist-provider-confidence-v1"

    /** BuildConfig is allowed to tune the direct threshold, not its safe bounds. */
    fun validatedDirectThreshold(configured: Double): Double =
        configured.takeIf {
            it.isFinite() &&
                it >= VoicePolicy.T_DIRECT_MIN_ALLOWED &&
                it <= VoicePolicy.T_DIRECT_MAX_ALLOWED
        } ?: VoicePolicy.T_DIRECT

    /**
     * Missing != Invalid. In particular, invalid NaN, infinities, negatives
     * or values >1.0 must not be silently clamped to an executable score.
     */
    fun classify(rawProviderConfidence: Double?): ArtistProviderConfidence =
        when {
            rawProviderConfidence == null -> ArtistProviderConfidence.Missing
            !rawProviderConfidence.isFinite() ||
                rawProviderConfidence !in 0.0..1.0 -> ArtistProviderConfidence.Invalid
            else -> ArtistProviderConfidence.Calibrated(rawProviderConfidence)
        }

    /**
     * Only a real calibrated provider score may authorize direct execution.
     * The #10 local-VAD exact-five-phrase exception is intentionally absent.
     */
    fun permitsDirect(
        evidence: ArtistProviderConfidence,
        configuredDirectThreshold: Double,
    ): Boolean = evidence is ArtistProviderConfidence.Calibrated &&
        evidence.probability >= validatedDirectThreshold(configuredDirectThreshold)
}
