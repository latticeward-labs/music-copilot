package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsPage
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsRequest
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.intent.ArtistMatchEvidence
import com.latticeward.musiccopilot.voice.intent.PlayArtistCandidate
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationOption
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationResponse
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationSnapshot
import com.latticeward.musiccopilot.voice.intent.PlayArtistDecision
import com.latticeward.musiccopilot.voice.session.PlayArtistDispatchClaim
import com.latticeward.musiccopilot.voice.session.PlayArtistPlaybackUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P06 — 25 synthetic fixtures AUTHORED ONLY; NOT_RUN/NOT_COMPILED.
 * Owner restriction prohibits tests, Gradle and APK in P00–P12.
 *
 * Fake gateway success simulates the contractually ACKed service response.
 * These source fixtures do NOT establish real Media3/OS ACK reliability.
 */
class Issue11Phase6ArtistPlaybackUseCaseTest {
    private val sid = VoiceSessionId(17)
    private val artist = LibraryGroupIdentity.artistId("Synthetic Artist")
    private fun ref(i: Int) = MediaRef("audio:$i")
    private fun candidate() = PlayArtistCandidate(
        artist, "Synthetic Artist", ArtistMatchEvidence.ExactNormalized,
    )
    private fun claim(attempt: Int = 1) = PlayArtistDispatchClaim.fromDirect(
        PlayArtistDecision.Direct(candidate(), 7L), sid, attempt,
    )!!

    private fun snap(
        rev: Long = 2L,
        queue: List<MediaRef> = listOf(ref(99)),
        state: PlaybackState = PlaybackState.Paused,
    ) = PlaybackSnapshot(
        revision = rev, queueRevision = rev,
        playbackState = state,
        queue = queue,
        currentIndex = if (queue.isEmpty()) null else 0,
        currentItem = queue.firstOrNull(),
        positionMs = 0L, durationMs = 10000L,
        canSeek = true, canSkipPrevious = false,
        canSkipNext = queue.size > 1,
    )

    private class Library(
        var ids: List<MediaRef> = listOf(MediaRef("audio:1")),
        var expectedArtist: String = "Synthetic Artist",
    ) : LibraryRepository {
        override val index = MutableStateFlow(
            LibraryIndexSnapshot(7L, LibraryIndexStatus.Ready, ids.size),
        )
        var pagesRead = 0
        var getCalls = 0
        var missingIds = emptySet<MediaRef>()
        var movedIds = emptySet<MediaRef>()
        var totalOverride: Int? = null
        var pageError: AppResult.Failure? = null
        var getError: AppResult.Failure? = null
        var readHook: (() -> Unit)? = null
        var getHook: (() -> Unit)? = null
        var breakPagination = false
        var duplicateRef = false

        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
            AppResult.Failure(AppError.Library(
                AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                Recoverability.RECOVERABLE, RecoveryAction.Retry,
            ))
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> =
            AppResult.Success(emptyList())

        override suspend fun artistTrackRefs(
            request: ArtistTrackRefsRequest,
        ): AppResult<ArtistTrackRefsPage> {
            pagesRead++
            readHook?.invoke()
            pageError?.let { return it }
            if (request.expectedRevision != index.value.revision) {
                return AppResult.Failure(AppError.Library(
                    AppErrorCodes.LIBRARY_BROWSE_STALE,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry,
                ))
            }
            val rows = if (duplicateRef && ids.size > 1)
                ids.dropLast(1) + ids.first() else ids
            val count = totalOverride ?: rows.size
            val slice = rows.drop(request.offset).take(request.limit)
            val next = if (request.offset + slice.size < count && slice.isNotEmpty())
                request.offset + slice.size else null
            return AppResult.Success(ArtistTrackRefsPage(
                slice, count, index.value.revision,
                if (breakPagination && request.offset == 0) null else next,
                request.limit,
            ))
        }

        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> {
            getCalls++
            getHook?.invoke()
            getError?.let { return it }
            if (ref in missingIds) return AppResult.Failure(AppError.MediaItem(
                AppErrorCodes.MEDIA_ITEM_MISSING,
                Recoverability.RECOVERABLE, RecoveryAction.Skip, ref,
            ))
            return AppResult.Success(LibraryItem(
                ref, "Synthetic Track", if (ref in movedIds) "Other Artist"
                    else expectedArtist, "Synthetic Album", 15000L,
            ))
        }
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(
            PlaybackSnapshot(1L, 2L, PlaybackState.Paused,
                listOf(MediaRef("audio:99")), 0, MediaRef("audio:99"),
                0L, 10000L, true, false, false),
        )
        val commands = mutableListOf<PlaybackCommand>()
        var forcedFailure: AppResult.Failure? = null
        var forceBadAck = false
        var ackState = PlaybackState.Buffering
        var dropSecond = false
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            forcedFailure?.let { return it }
            val replace = command as PlaybackCommand.ReplaceQueue
            val accepted = if (dropSecond && replace.items.size > 1)
                replace.items.filterIndexed { index, _ -> index != 1 }
            else replace.items
            return AppResult.Success(
                if (forceBadAck) snapshot.value
                else PlaybackSnapshot(
                    revision = 3L, queueRevision = 3L,
                    playbackState = ackState, queue = accepted,
                    currentIndex = 0, currentItem = accepted.first(),
                    positionMs = 0L, durationMs = 15000L,
                    canSeek = true, canSkipPrevious = false,
                    canSkipNext = accepted.size > 1,
                ),
            )
        }
    }

    private fun code(outcome: AppResult<*>): String {
        assertTrue(outcome is AppResult.Failure)
        return (outcome as AppResult.Failure).error.code
    }

    @Test fun claimContainsOneFixedServiceCompatibleVoiceId() {
        val claim = claim()
        assertTrue(claim.commandId.value.matches(
            Regex("^voice-[0-9a-f]{32}-17-1$")))
        assertEquals(claim.commandId, claim.commandId)
        assertFalse(claim.toString().contains(artist.value))
    }

    @Test fun twoAttemptsHaveDifferentOpaqueNonceAndNoPersonalData() {
        val a = claim(1)
        val b = claim(2)
        assertNotEquals(a.commandId, b.commandId)
        assertFalse(a.commandId.value.contains("Synthetic"))
    }

    @Test fun invalidSessionAndAttemptCannotMintClaim() {
        val direct = PlayArtistDecision.Direct(candidate(), 7L)
        assertNull(PlayArtistDispatchClaim.fromDirect(direct, VoiceSessionId(0), 1))
        assertNull(PlayArtistDispatchClaim.fromDirect(direct, sid, 0))
        assertNull(PlayArtistDispatchClaim.fromDirect(direct, sid, 3))
    }

    @Test fun selectedConfirmationRequiresMatchingTokenAndBoundedIndex() {
        val token = VoiceCandidateToken("opaque-token")
        val snap = PlayArtistConfirmationSnapshot(
            sid, VoiceOrigin.Player, token, 7L,
            listOf(PlayArtistConfirmationOption(0, candidate())),
        )
        assertNotNull(PlayArtistDispatchClaim.fromSelectedConfirmation(snap,
            PlayArtistConfirmationResponse.Select(token, 0), 1))
        assertNull(PlayArtistDispatchClaim.fromSelectedConfirmation(snap,
            PlayArtistConfirmationResponse.Select(VoiceCandidateToken("other"), 0), 1))
        assertNull(PlayArtistDispatchClaim.fromSelectedConfirmation(snap,
            PlayArtistConfirmationResponse.Select(token, 1), 1))
    }

    @Test fun verifiedOneSongProducesOneReplaceQueueAndAckedExecuted() = runBlocking {
        val library = Library()
        val gateway = Gateway()
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertSame(PlayArtistDecision.Executed, (result as AppResult.Success<PlayArtistDecision>).value)
        val command = gateway.commands.single() as PlaybackCommand.ReplaceQueue
        assertEquals(listOf(ref(1)), command.items)
        assertEquals(0, command.startIndex)
        assertEquals(0L, command.startPositionMs)
        assertTrue(command.playWhenReady)
        assertEquals(1, library.getCalls)
    }

    @Test fun aClaimCanDispatchOnlyOnceEvenWhenRepeated() = runBlocking {
        val library = Library()
        val gateway = Gateway()
        val useCase = PlayArtistPlaybackUseCase(library, gateway)
        val once = claim()
        assertTrue(useCase.execute(once) { true } is AppResult.Success)
        assertEquals(AppErrorCodes.VOICE_STALE_SESSION,
            code(useCase.execute(once) { true }))
        assertEquals(1, gateway.commands.size)
    }

    @Test fun unownedGenerationCannotReadLibraryOrDispatch() = runBlocking {
        val library = Library()
        val gateway = Gateway()
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { false }
        assertEquals(AppErrorCodes.VOICE_STALE_SESSION, code(result))
        assertEquals(0, library.pagesRead)
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun disconnectedGatewayDoesNotReplaceExistingQueue() = runBlocking {
        val library = Library()
        val gateway = Gateway().apply {
            connectionState.value = PlaybackConnectionState.Disconnected
        }
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertEquals(AppErrorCodes.CONTROLLER_DISCONNECTED, code(result))
        assertEquals(0, library.pagesRead)
        assertEquals(0, gateway.commands.size)
    }

    @Test fun staleRevisionBeforeReadFailsWithoutQueueChange() = runBlocking {
        val library = Library().apply { index.value = index.value.copy(revision = 8L) }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.VOICE_STALE_SESSION,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun emptyArtistMembershipPreservesOldQueue() = runBlocking {
        val library = Library(emptyList())
        val gateway = Gateway()
        assertEquals(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun everyMissingSongFailsWithoutReplacingQueue() = runBlocking {
        val library = Library(listOf(ref(1), ref(2))).apply {
            missingIds = setOf(ref(1), ref(2))
        }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun missingFirstSongIsSkippedAndNextValidSongIsSelected() = runBlocking {
        val library = Library(listOf(ref(1), ref(2))).apply { missingIds = setOf(ref(1)) }
        val gateway = Gateway()
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertSame(PlayArtistDecision.Executed, (result as AppResult.Success<PlayArtistDecision>).value)
        assertEquals(listOf(ref(2)),
            (gateway.commands.single() as PlaybackCommand.ReplaceQueue).items)
    }

    @Test fun changedArtistMembershipMustNeverLeakIntoNewQueue() = runBlocking {
        val library = Library(listOf(ref(1), ref(2))).apply { movedIds = setOf(ref(2)) }
        val gateway = Gateway()
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertSame(PlayArtistDecision.Executed, (result as AppResult.Success<PlayArtistDecision>).value)
        assertEquals(listOf(ref(1)),
            (gateway.commands.single() as PlaybackCommand.ReplaceQueue).items)
    }

    @Test fun exactFiveThousandItemLimitMatchesExistingPlaybackQueuePolicy() {
        assertEquals(5000, PlayArtistPlaybackUseCase.MAX_QUEUE_ITEMS)
        assertEquals(PlaybackQueuePolicy.MAX_ITEMS, PlayArtistPlaybackUseCase.MAX_QUEUE_ITEMS)
        assertEquals(100, PlayArtistPlaybackUseCase.PAGE_SIZE)
    }

    @Test fun moreThanFiveThousandFailsBeforeMediaRequeryOrDispatch() = runBlocking {
        val library = Library(listOf(ref(1))).apply {
            totalOverride = PlayArtistPlaybackUseCase.MAX_QUEUE_ITEMS + 1
        }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertEquals(0, library.getCalls)
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun overOneHundredSongsUseRevisionPinnedPages() = runBlocking {
        val library = Library((1..121).map(::ref))
        val gateway = Gateway()
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertSame(PlayArtistDecision.Executed, (result as AppResult.Success<PlayArtistDecision>).value)
        assertEquals(121, library.getCalls)
        assertEquals(121,
            (gateway.commands.single() as PlaybackCommand.ReplaceQueue).items.size)
        assertTrue(library.pagesRead >= 3)
    }

    @Test fun duplicateIndexedMediaRefFailsBeforeServiceDispatch() = runBlocking {
        val library = Library(listOf(ref(1), ref(2))).apply { duplicateRef = true }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun truncatedRoomPageCannotProducePartialQueue() = runBlocking {
        val library = Library((1..121).map(::ref)).apply { breakPagination = true }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun permissionRevokedWhileReadingFailsBeforeServiceDispatch() = runBlocking {
        val library = Library(listOf(ref(1), ref(2))).apply {
            getHook = {
                index.value = index.value.copy(status = LibraryIndexStatus.Unavailable)
            }
        }
        val gateway = Gateway()
        assertEquals(AppErrorCodes.VOICE_STALE_SESSION,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun laterRoomFailureMustNotMutateQueue() = runBlocking {
        val library = Library()
        library.pageError = AppResult.Failure(AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_STALE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        ))
        val gateway = Gateway()
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertTrue(gateway.commands.isEmpty())
    }

    @Test fun failedServiceAcknowledgementIsNeverExecuted() = runBlocking {
        val library = Library()
        val gateway = Gateway().apply {
            forcedFailure = AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
            ))
        }
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertEquals(1, gateway.commands.size)
    }

    @Test fun incoherentOrOldServiceSnapshotCannotClaimExecuted() = runBlocking {
        val library = Library()
        val gateway = Gateway().apply { forceBadAck = true }
        assertEquals(AppErrorCodes.PLAYBACK_FAILED,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
        assertEquals(1, gateway.commands.size)
    }

    @Test fun gatewayPausedStateDoesNotClaimSuccessfulAudioStart() = runBlocking {
        val library = Library()
        val gateway = Gateway().apply { ackState = PlaybackState.Paused }
        assertEquals(AppErrorCodes.PLAYBACK_FAILED,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }))
    }

    @Test fun serviceMaySkipLaterFilesWithoutBreakingQueueOrder() = runBlocking {
        val library = Library(listOf(ref(1), ref(2), ref(3)))
        val gateway = Gateway().apply { dropSecond = true }
        val result = PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { true }
        assertSame(PlayArtistDecision.Executed, (result as AppResult.Success<PlayArtistDecision>).value)
        assertEquals(listOf(ref(1), ref(2), ref(3)),
            (gateway.commands.single() as PlaybackCommand.ReplaceQueue).items)
    }

    @Test fun canceledSessionBeforeFinalDispatchCannotSendCommand() = runBlocking {
        val library = Library()
        val gateway = Gateway()
        var current = true
        library.getHook = { current = false }
        assertEquals(AppErrorCodes.VOICE_STALE_SESSION,
            code(PlayArtistPlaybackUseCase(library, gateway).execute(claim()) { current }))
        assertTrue(gateway.commands.isEmpty())
    }
}
