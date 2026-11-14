package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy
import com.latticeward.musiccopilot.voice.intent.PlayArtistCandidate
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationResponse
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationSnapshot
import com.latticeward.musiccopilot.voice.intent.PlayArtistDecision
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * P06: one-use, one-attempt dispatch claim. Fixed process-unique command ID.
 * P07 alone owns current session/generation/token/confirmation single-use.
 * This source capability cannot be persisted, reconstructed or logged.
 */
internal class PlayArtistDispatchClaim private constructor(
    val sessionId: VoiceSessionId,
    val attempt: Int,
    val artistGroupId: LibraryGroupId,
    val expectedLibraryRevision: Long,
    val commandId: CommandId,
) {
    private val consumed = AtomicBoolean(false)
    fun claimOnce(): Boolean = consumed.compareAndSet(false, true)
    override fun toString(): String = "PlayArtistDispatchClaim(redacted)"

    companion object {
        fun fromDirect(
            decision: PlayArtistDecision.Direct,
            sessionId: VoiceSessionId,
            attempt: Int,
        ): PlayArtistDispatchClaim? =
            issue(decision.candidate, decision.expectedLibraryRevision, sessionId, attempt)

        /**
         * Token and index equality only; current coordinator ownership,
         * generation and expiration are MUST-check preconditions for P07.
         */
        fun fromSelectedConfirmation(
            snapshot: PlayArtistConfirmationSnapshot,
            response: PlayArtistConfirmationResponse.Select,
            attempt: Int,
        ): PlayArtistDispatchClaim? {
            if (response.token != snapshot.token) return null
            val option = snapshot.options.getOrNull(response.optionIndex)
                ?.takeIf { it.optionIndex == response.optionIndex }
                ?: return null
            return issue(option.candidate, snapshot.expectedLibraryRevision,
                snapshot.sessionId, attempt)
        }

        private fun issue(
            candidate: PlayArtistCandidate,
            revision: Long,
            session: VoiceSessionId,
            attempt: Int,
        ): PlayArtistDispatchClaim? {
            if (session.value <= 0L || attempt !in 1..2 || revision < 0L ||
                !candidate.artistGroupId.value.startsWith("artist:")
            ) return null
            val nonce = UUID.randomUUID().toString().replace("-", "")
            val id = CommandId("voice-" + nonce + "-" + session.value + "-" + attempt)
            return PlayArtistDispatchClaim(
                session, attempt, candidate.artistGroupId, revision, id,
            )
        }
    }
}

/**
 * P06: gather ONLY selected group's refs, requery local MediaStore per item,
 * then dispatch a single ReplaceQueue through the existing PlaybackGateway.
 * No ExoPlayer, second queue owner, STT provider or UI in this class.
 * P12 source integration: instantiated only by the app-scoped P07 Voice
 * Coordinator when both local LibraryRepository and service-owned
 * PlaybackGateway are present. No independent player or fallback queue.
 */
internal class PlayArtistPlaybackUseCase(
    private val library: LibraryRepository,
    private val playback: PlaybackGateway,
) {
    companion object {
        const val POLICY_VERSION = "issue11-play-artist-playback-v1"
        const val PAGE_SIZE = ArtistTrackRefsRequest.MAX_PAGE_SIZE
        const val MAX_QUEUE_ITEMS = PlaybackQueuePolicy.MAX_ITEMS
    }

    /**
     * P07 MUST supply the live current-session/generation/token predicate;
     * an unconditional true is not acceptable in production. This claim is
     * atomically consumed BEFORE the first suspension. Later callbacks with
     * the same claim cannot dispatch a duplicate command.
     */
    suspend fun execute(
        claim: PlayArtistDispatchClaim,
        isCurrentSession: () -> Boolean,
    ): AppResult<PlayArtistDecision> {
        if (!claim.claimOnce()) return stale()
        currentCoroutineContext().ensureActive()
        if (!current(isCurrentSession)) return stale()

        val baseline = library.index.value
        if (baseline.revision != claim.expectedLibraryRevision) return stale()
        if (baseline.status != LibraryIndexStatus.Ready) return libraryUnavailable()
        if (playback.connectionState.value != PlaybackConnectionState.Ready)
            return controllerUnavailable()
        val before = playback.snapshot.value

        val accepted = ArrayList<MediaRef>()
        val seen = HashSet<MediaRef>()
        var total: Int? = null
        var offset = 0
        var firstIndexed: MediaRef? = null
        while (true) {
            currentCoroutineContext().ensureActive()
            if (!current(isCurrentSession) ||
                !sameRevision(claim.expectedLibraryRevision)
            ) return stale()
            val request = ArtistTrackRefsRequest(
                claim.artistGroupId, claim.expectedLibraryRevision,
                offset = offset, limit = PAGE_SIZE,
            )
            val outcome = try {
                library.artistTrackRefs(request)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { return libraryUnavailable() }
            val page = when (outcome) {
                is AppResult.Failure -> return outcome
                is AppResult.Success -> outcome.value
            }
            if (!current(isCurrentSession) ||
                !sameRevision(claim.expectedLibraryRevision) ||
                page.revision != claim.expectedLibraryRevision
            ) return stale()
            if (total != null && page.totalCount != total) return stale()
            if (page.totalCount > MAX_QUEUE_ITEMS) return queueRejected()
            if (page.totalCount < offset || page.refs.size > PAGE_SIZE ||
                page.refs.isEmpty() && offset < page.totalCount
            ) return libraryUnavailable()
            if (total == null) total = page.totalCount

            for (ref in page.refs) {
                if (!seen.add(ref) || ref.value.isBlank() ||
                    ref.value.length > PlaybackQueuePolicy.MAX_OPAQUE_ID_LENGTH
                ) return libraryUnavailable()
                if (firstIndexed == null) firstIndexed = ref
                currentCoroutineContext().ensureActive()
                if (!current(isCurrentSession)) return stale()
                // get(ref) re-queries MediaStore. An indexed group alone
                // NEVER establishes the file still exists or belongs here.
                val live = try {
                    library.get(ref)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) { return libraryUnavailable() }
                when (live) {
                    is AppResult.Success -> {
                        val item = live.value
                        if (item.ref == ref && item.artist != null &&
                            LibraryGroupIdentity.normalize(item.artist) != null &&
                            LibraryGroupIdentity.artistId(item.artist) == claim.artistGroupId
                        ) accepted.add(ref)
                    }
                    is AppResult.Failure -> {
                        when (live.error) {
                            is AppError.MediaItem -> if (
                                live.error.code != AppErrorCodes.MEDIA_ITEM_MISSING &&
                                live.error.code != AppErrorCodes.MEDIA_ITEM_UNPLAYABLE
                            ) return noPlayableArtist()
                            else -> return live
                        }
                    }
                }
            }
            val next = offset + page.refs.size
            if (page.nextOffset == null) {
                if (next != page.totalCount) return libraryUnavailable()
                break
            }
            if (next >= page.totalCount || page.nextOffset != next)
                return libraryUnavailable()
            offset = next
        }

        // Final revision and permission/epoch-checked SQL read after all
        // possibly slow MediaStore requery work. No partial queue on failure.
        currentCoroutineContext().ensureActive()
        if (!current(isCurrentSession) ||
            !sameRevision(claim.expectedLibraryRevision)
        ) return stale()
        val finalResult = try {
            library.artistTrackRefs(ArtistTrackRefsRequest(
                claim.artistGroupId, claim.expectedLibraryRevision, limit = 1,
            ))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) { return libraryUnavailable() }
        when (finalResult) {
            is AppResult.Failure -> return finalResult
            is AppResult.Success -> {
                val page = finalResult.value
                if (page.revision != claim.expectedLibraryRevision ||
                    page.totalCount != total ||
                    page.refs.firstOrNull() != firstIndexed
                ) return stale()
            }
        }
        if (!current(isCurrentSession) ||
            !sameRevision(claim.expectedLibraryRevision)
        ) return stale()
        if (accepted.isEmpty()) return noPlayableArtist()
        if (playback.connectionState.value != PlaybackConnectionState.Ready)
            return controllerUnavailable()

        val command = PlaybackCommand.ReplaceQueue(
            commandId = claim.commandId,
            items = accepted.toList(),
            startIndex = 0,
            startPositionMs = 0L,
            playWhenReady = true,
        )
        if (PlaybackQueuePolicy.validate(command) !is PlaybackQueuePolicy.Decision.Accepted)
            return queueRejected()
        if (!current(isCurrentSession) ||
            !sameRevision(claim.expectedLibraryRevision)
        ) return stale()

        // Only ONE playback mutation; Media3 reopens file descriptors,
        // checks route/focus and returns an ID-correlated service ACK.
        val outcome = try {
            playback.execute(command)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) { return playbackFailed() }

        if (!current(isCurrentSession)) return stale()
        return when (outcome) {
            is AppResult.Failure -> outcome
            is AppResult.Success -> if (
                validAck(outcome.value, before, accepted)
            ) AppResult.Success(PlayArtistDecision.Executed)
            else playbackFailed()
        }
    }

    private fun sameRevision(expected: Long): Boolean {
        val state = library.index.value
        return state.revision == expected && state.status == LibraryIndexStatus.Ready
    }
    private fun current(predicate: () -> Boolean): Boolean =
        try { predicate() } catch (_: Exception) { false }

    /**
     * Success from PlaybackGateway promises a service ACK, not an optimistic
     * UI state. Check new queue revision, selected ref and stable subsequence
     * order. The service may drop subsequent now-unplayable items. A paused
     * or error snapshot must never claim executed playback.
     */
    private fun validAck(
        snapshot: PlaybackSnapshot,
        before: PlaybackSnapshot,
        proposed: List<MediaRef>,
    ): Boolean {
        if (snapshot.queueRevision <= before.queueRevision ||
            snapshot.queue.isEmpty() || proposed.isEmpty() ||
            snapshot.queue.first() != proposed.first() ||
            snapshot.currentIndex != 0 ||
            snapshot.currentItem != snapshot.queue.first() ||
            snapshot.playbackState !in setOf(PlaybackState.Playing, PlaybackState.Buffering) ||
            snapshot.lastError != null
        ) return false
        var cursor = 0
        for (ref in snapshot.queue) {
            while (cursor < proposed.size && proposed[cursor] != ref) cursor++
            if (cursor >= proposed.size) return false
            cursor++
        }
        return true
    }

    private fun stale(): AppResult.Failure = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_STALE_SESSION,
            Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback),
    )
    private fun libraryUnavailable(): AppResult.Failure = AppResult.Failure(
        AppError.Library(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )
    private fun queueRejected(): AppResult.Failure = AppResult.Failure(
        AppError.Playback(AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )
    private fun noPlayableArtist(): AppResult.Failure = AppResult.Failure(
        AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )
    private fun controllerUnavailable(): AppResult.Failure = AppResult.Failure(
        AppError.Controller(AppErrorCodes.CONTROLLER_DISCONNECTED,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )
    private fun playbackFailed(): AppResult.Failure = AppResult.Failure(
        AppError.Playback(AppErrorCodes.PLAYBACK_FAILED,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )
}
