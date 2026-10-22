package com.latticeward.musiccopilot.playback.service

/**
 * P10 deterministic, bounded recovery from genuine local-media decoder/IO
 * failures. No URI, title, exception text, MediaRef or paths are retained.
 *
 * The service is the only queue owner. Failed content is never rewritten,
 * deleted, retried infinitely or replaced by a network catalog. Automatic
 * forward recovery is bounded to at most three distinct later occurrences
 * in one uninterrupted episode. Permission loss and unclassified failures
 * are terminal for that attempt, not automatic skips.
 */
internal class PlaybackFailureRecoveryPolicy(
    private val maxForwardCandidates: Int = 3,
) {
    init { require(maxForwardCandidates in 1..8) }

    enum class Failure {
        Corrupt, Missing, PermissionRevoked, Unsupported, Unknown,
    }

    enum class Disposition { SkipForward, StopAndRequestPermission, SafePause }

    fun disposition(kind: Failure): Disposition = when (kind) {
        Failure.Corrupt, Failure.Missing, Failure.Unsupported -> Disposition.SkipForward
        Failure.PermissionRevoked -> Disposition.StopAndRequestPermission
        Failure.Unknown -> Disposition.SafePause
    }

    /**
     * A candidate is always strictly AFTER the current failed occurrence.
     * The index is an occurrence index, not an ID: duplicates are distinct.
     */
    fun nextIndex(
        failedIndex: Int,
        queueSize: Int,
        alreadyAttempted: Int,
    ): Int? {
        if (queueSize <= 0 || failedIndex !in 0 until queueSize ||
            alreadyAttempted !in 0 until maxForwardCandidates
        ) return null
        return (failedIndex + 1).takeIf { it in 0 until queueSize }
    }

    fun permitsAttempt(attempts: Int): Boolean =
        attempts in 0 until maxForwardCandidates

    val maxAttempts: Int get() = maxForwardCandidates
}
