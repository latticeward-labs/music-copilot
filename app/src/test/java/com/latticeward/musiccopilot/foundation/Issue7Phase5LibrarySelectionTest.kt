package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.playback.LibraryMediaSelectionUseCase
import com.latticeward.musiccopilot.core.application.playback.SelectedPlaybackState
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryCandidate
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** All song IDs are synthetic. The real MediaStore/Media3 is not mocked as proof. */
class Issue7Phase5LibrarySelectionTest {
    private val a = MediaRef("audio:1")
    private val b = MediaRef("audio:2")
    private val c = MediaRef("audio:3")

    private class FakeLibrary : LibraryRepository {
        override val index = MutableStateFlow(LibraryIndexSnapshot(5L, LibraryIndexStatus.Ready, 3))
        var got: MediaRef? = null
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile() =
            AppResult.Success(LibraryReconcileResult(0, 0, 0, 0, 5))
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> {
            got = ref
            return AppResult.Success(LibraryItem(ref, "Synthetic", null, null, 5000L))
        }
        override suspend fun resolveVoiceCandidates(normalizedTarget: String, limit: Int) =
            AppResult.Success(emptyList<LibraryCandidate>())
    }

    private class FakeGateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot: StateFlow<PlaybackSnapshot> =
            MutableStateFlow(state(listOf(MediaRef("audio:2")), 0, PlaybackState.Buffering))
        var submitted: PlaybackCommand? = null
        var acknowledgement: PlaybackSnapshot = state(
            listOf(MediaRef("audio:1"), MediaRef("audio:2"), MediaRef("audio:3")), 1,
            PlaybackState.Buffering
        )
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            submitted = command
            return AppResult.Success(acknowledgement)
        }
        companion object {
            fun state(refs: List<MediaRef>, index: Int, mode: PlaybackState) =
                PlaybackSnapshot(
                    revision = 17, queueRevision = 12, playbackState = mode,
                    queue = refs, currentIndex = index, currentItem = refs[index],
                    positionMs = 0, durationMs = null, canSeek = false,
                    canSkipPrevious = index > 0, canSkipNext = index < refs.lastIndex
                )
        }
    }

    private fun useCase(library: FakeLibrary, gateway: FakeGateway, hasAccess: () -> Boolean = { true }) =
        LibraryMediaSelectionUseCase(library, gateway, hasAccess, { CommandId("fake-selection") })

    @Test fun sendsVisibleOrderedQueueAndCorrectIndexInsteadOfSingleTrack() = runBlocking {
        val library = FakeLibrary()
        val gateway = FakeGateway()
        val result = useCase(library, gateway).select(b, 5L, listOf(a, b, c), 1)
        assertTrue(result is AppResult.Success)
        val intent = gateway.submitted as PlaybackCommand.ReplaceQueue
        assertEquals(listOf(a, b, c), intent.items)
        assertEquals(1, intent.startIndex)
        assertEquals(b, library.got)
        assertTrue(intent.playWhenReady)
    }

    @Test fun bufferingAcknowledgementIsNotClaimedAsAudiblePlaying() = runBlocking {
        val result = useCase(FakeLibrary(), FakeGateway())
            .select(b, 5L, listOf(a, b, c), 1) as AppResult.Success
        assertEquals(SelectedPlaybackState.AcceptedNotPlaying, result.value.state)
    }

    @Test fun actualPlayerStatePlayingIsRecognizedOnlyFromSnapshot() = runBlocking {
        val gateway = FakeGateway()
        gateway.acknowledgement = FakeGateway.state(listOf(a, b, c), 1, PlaybackState.Playing)
        val result = useCase(FakeLibrary(), gateway)
            .select(b, 5L, listOf(a, b, c), 1) as AppResult.Success
        assertEquals(SelectedPlaybackState.Playing, result.value.state)
    }

    @Test fun missingOtherItemIsAllowedWhenServiceKeptSelectedOccurrence() = runBlocking {
        val gateway = FakeGateway()
        gateway.acknowledgement = FakeGateway.state(listOf(b, c), 0, PlaybackState.Buffering)
        val result = useCase(FakeLibrary(), gateway).select(b, 5L, listOf(a, b, c), 1)
        assertTrue(result is AppResult.Success)
    }

    @Test fun fabricatedOrReorderedQueueIsRejected() = runBlocking {
        val gateway = FakeGateway()
        gateway.acknowledgement = FakeGateway.state(listOf(c, b, a), 1, PlaybackState.Playing)
        val result = useCase(FakeLibrary(), gateway).select(b, 5L, listOf(a, b, c), 1)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED, (result as AppResult.Failure).error.code)
    }

    @Test fun invalidOccurrenceAndOversizedCollectionNeverCallGateway() = runBlocking {
        val gateway = FakeGateway()
        val useCase = useCase(FakeLibrary(), gateway)
        assertTrue(useCase.select(b, 5L, listOf(a, b, c), 0) is AppResult.Failure)
        assertTrue(useCase.select(b, 5L, List(5001) { b }, 0) is AppResult.Failure)
        assertNull(gateway.submitted)
    }

    @Test fun staleRevisionOrPermissionNeverInvokePlayer() = runBlocking {
        val library = FakeLibrary()
        val gateway = FakeGateway()
        assertTrue(useCase(library, gateway).select(b, 4L, listOf(b), 0) is AppResult.Failure)
        assertNull(gateway.submitted)
        val fail = useCase(library, gateway, { false }).select(b, 5L)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, (fail as AppResult.Failure).error.code)
        assertNull(gateway.submitted)
    }

    @Test fun disconnectedGatewayCannotAckSelection() = runBlocking {
        val gateway = FakeGateway()
        gateway.connectionState.value = PlaybackConnectionState.Disconnected
        val result = useCase(FakeLibrary(), gateway).select(b, 5L, listOf(b), 0)
        assertEquals(AppErrorCodes.CONTROLLER_DISCONNECTED,
            (result as AppResult.Failure).error.code)
    }
}
