package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import java.util.Collections

/**
 * Issue #11 P01: SOURCE-ONLY contract for the sixth, local PLAY_ARTIST intent.
 *
 * These declarations are intentionally NOT wired to the mapper, coordinator,
 * repository, Media3 dispatch, or UI in P01. P02-P12 implement the behavior;
 * P13 is the sole app-build and owner-QA handoff phase.
 *
 * Private artist labels, opaque local group IDs and confirmation tokens must
 * never be logged, persisted to disk, or emitted to telemetry.
 */

/** Provider evidence, never an artist-match score; missing is NOT 0.90. */
sealed interface ArtistProviderConfidence {
    data object Missing : ArtistProviderConfidence
    data object Invalid : ArtistProviderConfidence

    data class Calibrated(val probability: Double) : ArtistProviderConfidence {
        init {
            require(probability.isFinite() && probability in 0.0..1.0) {
                "calibrated confidence must be a finite probability"
            }
        }
    }
}

/** The resolver may score artists, but cannot manufacture provider confidence. */
sealed interface ArtistMatchEvidence {
    data object ExactNormalized : ArtistMatchEvidence

    data class Fuzzy(
        val similarity: Double,
        val algorithmVersion: String,
    ) : ArtistMatchEvidence {
        init {
            require(similarity.isFinite() && similarity in 0.0..1.0)
            require(algorithmVersion.isNotBlank())
        }
    }
}

/**
 * Ephemeral local artist identity; display label/ID MUST NOT reach Quality.
 * A missing metadata artist must not become a made-up candidate (P03).
 */
class PlayArtistCandidate(
    val artistGroupId: LibraryGroupId,
    val displayLabel: String,
    val matchEvidence: ArtistMatchEvidence,
) {
    init {
        require(artistGroupId.value.isNotBlank())
        require(displayLabel.isNotBlank())
    }

    override fun toString(): String = "PlayArtistCandidate(redacted)"
}

enum class ArtistAmbiguityReason {
    CLOSE_ARTIST_MATCHES,
    LOW_PROVIDER_CONFIDENCE,
    MISSING_PROVIDER_CONFIDENCE,
}

/**
 * Explicit product decision/result vocabulary; no execution occurs by creating
 * one of these values. A Direct decision still requires P05 policy and P06
 * live-file validation + a service-owned PlaybackGateway ACK.
 *
 * Unknown = cannot confidently map a supported command.
 * Unsupported = understood request outside the six-intent MVP scope.
 * NotFound = supported PLAY_ARTIST, but no plausible lawful local artist.
 * Cancel = Neither/Cancel/Back rejects the proposed choice without playback.
 * Stale = token/session/revision/permission no longer authorizes a choice.
 * PlaybackFailure = dispatch/ACK was not successful; never claim Executed.
 * Executed = publish ONLY after a validated service acknowledgement (P06/P07).
 */
sealed interface PlayArtistDecision {
    class Direct(
        val candidate: PlayArtistCandidate,
        val expectedLibraryRevision: Long,
    ) : PlayArtistDecision {
        init { require(expectedLibraryRevision >= 0L) }
        override fun toString(): String = "PlayArtistDecision.Direct(redacted)"
    }

    class Ambiguous(
        candidates: List<PlayArtistCandidate>,
        val expectedLibraryRevision: Long,
        val reason: ArtistAmbiguityReason,
    ) : PlayArtistDecision {
        /** Defensive snapshot; UI must not mutate or re-query candidates. */
        val candidates: List<PlayArtistCandidate> =
            Collections.unmodifiableList(candidates.toList())

        init {
            require(expectedLibraryRevision >= 0L)
            require(this.candidates.size in 1..2) { "one or two artist candidates" }
            require(this.candidates.map { it.artistGroupId }.distinct().size ==
                this.candidates.size) { "artist identities must be distinct" }
        }

        override fun toString(): String = "PlayArtistDecision.Ambiguous(redacted)"
    }

    data object NotFound : PlayArtistDecision
    data object Unknown : PlayArtistDecision
    data object Unsupported : PlayArtistDecision
    data object Cancel : PlayArtistDecision
    data object Stale : PlayArtistDecision
    data object PlaybackFailure : PlayArtistDecision
    data object Executed : PlayArtistDecision
}

/** Only the coordinator assigns these zero-based display/selection positions. */
class PlayArtistConfirmationOption(
    val optionIndex: Int,
    val candidate: PlayArtistCandidate,
) {
    init { require(optionIndex in 0..1) }
    override fun toString(): String = "PlayArtistConfirmationOption(redacted)"
}

/**
 * Pending authority lives ONLY in the app-scoped VoiceSessionCoordinator.
 * This copied, bounded packet is ephemeral and never a restored data record.
 * Its public UI projection uses VoiceSessionState.Confirming's existing
 * VoiceConfirmationOption(displayLabel, optionIndex), not a second database read.
 */
class PlayArtistConfirmationSnapshot(
    val sessionId: VoiceSessionId,
    val origin: VoiceOrigin,
    val token: VoiceCandidateToken,
    val expectedLibraryRevision: Long,
    options: List<PlayArtistConfirmationOption>,
) {
    val options: List<PlayArtistConfirmationOption> =
        Collections.unmodifiableList(options.toList())

    init {
        require(sessionId.value > 0L)
        require(token.value.isNotBlank())
        require(expectedLibraryRevision >= 0L)
        require(this.options.size in 1..2)
        require(this.options.map { it.optionIndex } == this.options.indices.toList()) {
            "confirmation indexes must be contiguous, zero-based and ordered"
        }
        require(this.options.map { it.candidate.artistGroupId }.distinct().size ==
            this.options.size) { "confirmation artist identities must be distinct" }
    }

    override fun toString(): String = "PlayArtistConfirmationSnapshot(redacted)"
}

/**
 * The ONE permitted answer to an existing confirmation is Select(index),
 * Neither or Cancel. No arbitrary artist text, new candidate, second question
 * or STT retry can be submitted as a confirmation response.
 *
 * P07 must verify the CURRENT session + token + one-shot claim + valid option
 * + library revision BEFORE dispatch; these inert types do not enforce time or
 * exactly-once execution on their own.
 */
sealed interface PlayArtistConfirmationResponse {
    val token: VoiceCandidateToken

    class Select(
        override val token: VoiceCandidateToken,
        val optionIndex: Int,
    ) : PlayArtistConfirmationResponse {
        init {
            require(token.value.isNotBlank())
            require(optionIndex in 0..1)
        }
        override fun toString(): String = "Select(optionIndex=$optionIndex, redacted)"
    }

    class Neither(
        override val token: VoiceCandidateToken,
    ) : PlayArtistConfirmationResponse {
        init { require(token.value.isNotBlank()) }
        override fun toString(): String = "Neither(redacted)"
    }

    class Cancel(
        override val token: VoiceCandidateToken,
    ) : PlayArtistConfirmationResponse {
        init { require(token.value.isNotBlank()) }
        override fun toString(): String = "Cancel(redacted)"
    }
}
