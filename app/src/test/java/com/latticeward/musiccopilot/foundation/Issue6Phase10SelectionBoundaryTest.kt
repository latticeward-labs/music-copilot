package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.playback.LibraryMediaSelectionUseCase
import com.latticeward.musiccopilot.core.application.playback.SelectedPlaybackState
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** P10 deterministic fake service contract cases; AUTHORED, NOT_RUN until P12. */
class Issue6Phase10SelectionBoundaryTest {
    private val ref = MediaRef("audio:v:external_primary:9")
    private val other = MediaRef("audio:v:external_primary:10")
    private fun item(ref: MediaRef) =
        LibraryItem(ref, "Synthetic Nine", "Synthetic Artist", "Fixture Album", 20_000L)
    private fun snapshot(
        state: PlaybackState = PlaybackState.Playing,
        current: MediaRef? = ref,
        revision: Long = 11L,
    ) = PlaybackSnapshot(
        revision = revision,
        queueRevision = 4L,
        playbackState = state,
        queue = if (current == null) emptyList() else listOf(current),
        currentIndex = if (current == null) null else 0,
        currentItem = current,
        positionMs = 0L,
        durationMs = 20_000L,
        canSeek = current != null,
        canSkipPrevious = false,
        canSkipNext = false,
    )
    private class FakeLibrary(val target: LibraryItem) : LibraryRepository {
        val mutableIndex = MutableStateFlow(LibraryIndexSnapshot(
            7L, LibraryIndexStatus.Ready, 1))
        override val index: StateFlow<LibraryIndexSnapshot> = mutableIndex
        var reads = 0
        var failure: AppResult<LibraryItem>? = null
        var whenRead: () -> Unit = {}
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(listOf(target))
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
            error("No rescan allowed during selection")
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            error("No full search allowed during selection")
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> {
            reads++
            whenRead()
            return failure ?: AppResult.Success(target)
        }
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> =
            error("No voice execution allowed during selection")
    }
    private inner class FakeGateway : PlaybackGateway {
        val connection = MutableStateFlow(PlaybackConnectionState.Ready)
        override val connectionState: StateFlow<PlaybackConnectionState> = connection
        override val snapshot: StateFlow<PlaybackSnapshot> = MutableStateFlow(snapshot())
        val commands = mutableListOf<PlaybackCommand>()
        var answer: AppResult<PlaybackSnapshot> = AppResult.Success(snapshot())
        var onExecute: () -> Unit = {}
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            commands += command
            onExecute()
            return answer
        }
    }
    private fun boundary(
        lib: FakeLibrary, player: FakeGateway,
        hasAccess: () -> Boolean = { true },
    ) = LibraryMediaSelectionUseCase(
        library = lib, gateway = player, hasOsMediaAccess = hasAccess,
        nextCommandId = { CommandId("synthetic-id-1") },
    )

    @Test fun noOsGrantPreventsMediaStoreRequeryAndPlayback() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        val result = boundary(lib, player) { false }.select(ref, 7)
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, (result as AppResult.Failure).error.code)
        assertEquals(0, lib.reads)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun invalidOrStaleIndexRevisionBlocksSelection() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        for (rev in listOf(0L, 6L, 9L)) {
            val response = boundary(lib, player).select(ref, rev)
            assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
                (response as AppResult.Failure).error.code)
        }
        assertEquals(0, lib.reads)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun scanningAndEmptyStateCannotStartPlayback() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        for (status in listOf(
            LibraryIndexStatus.Unavailable,
            LibraryIndexStatus.Scanning,
            LibraryIndexStatus.Empty,
            LibraryIndexStatus.RecoverableError,
        )) {
            lib.mutableIndex.value = lib.mutableIndex.value.copy(status = status)
            val result = boundary(lib, player).select(ref, 7)
            assertTrue(result is AppResult.Failure)
        }
        assertEquals(0, lib.reads)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun mediaStoreMissingItemReturnsTypedMissingNotSuccess() = runBlocking {
        val lib = FakeLibrary(item(ref))
        lib.failure = AppResult.Failure(AppError.MediaItem(
            AppErrorCodes.MEDIA_ITEM_MISSING, Recoverability.RECOVERABLE,
            RecoveryAction.Skip, ref))
        val player = FakeGateway()
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            (result as AppResult.Failure).error.code)
        assertEquals(1, lib.reads)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun platformItemWithDifferentRefIsNotEligible() = runBlocking {
        val lib = FakeLibrary(item(other))
        val player = FakeGateway()
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            (result as AppResult.Failure).error.code)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun osGrantRevokedDuringRequerySuppressesCommand() = runBlocking {
        var permission = true
        val lib = FakeLibrary(item(ref))
        lib.whenRead = { permission = false }
        val player = FakeGateway()
        val result = boundary(lib, player) { permission }.select(ref, 7)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            (result as AppResult.Failure).error.code)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun scanRevisionChangingDuringRequerySuppressesCommand() = runBlocking {
        val lib = FakeLibrary(item(ref))
        lib.whenRead = {
            lib.mutableIndex.value = lib.mutableIndex.value.copy(revision = 8L)
        }
        val player = FakeGateway()
        val response = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            (response as AppResult.Failure).error.code)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun disconnectedGatewayNeverReceivesCommand() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.connection.value = PlaybackConnectionState.Disconnected
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.CONTROLLER_DISCONNECTED,
            (result as AppResult.Failure).error.code)
        assertEquals(1, lib.reads)
        assertTrue(player.commands.isEmpty())
    }

    @Test fun realIssue7ServiceRejectionIsPropagatedNotPlaying() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.answer = AppResult.Failure(AppError.Playback(
            AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        ))
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            (result as AppResult.Failure).error.code)
        assertEquals(1, player.commands.size)
    }

    @Test fun acceptedReplaceQueueUsesOneSourceRefAndServiceOwnedStart() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(SelectedPlaybackState.Playing,
            (result as AppResult.Success).value.state)
        val cmd = player.commands.single() as PlaybackCommand.ReplaceQueue
        assertEquals(CommandId("synthetic-id-1"), cmd.commandId)
        assertEquals(listOf(ref), cmd.items)
        assertEquals(0, cmd.startIndex)
        assertEquals(0L, cmd.startPositionMs)
        assertTrue(cmd.playWhenReady)
    }

    @Test fun acceptedBufferingIsNotOptimisticallyCalledPlaying() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.answer = AppResult.Success(snapshot(PlaybackState.Buffering))
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(SelectedPlaybackState.AcceptedNotPlaying,
            (result as AppResult.Success).value.state)
    }

    @Test fun fakeSuccessfulAckWithWrongSelectedItemIsRejected() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.answer = AppResult.Success(snapshot(current = other))
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            (result as AppResult.Failure).error.code)
    }

    @Test fun fakeAckWithNoCurrentItemOrIdleCannotClaimPlaying() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.answer = AppResult.Success(snapshot(PlaybackState.Idle))
        val result = boundary(lib, player).select(ref, 7)
        assertTrue(result is AppResult.Failure)
    }

    @Test fun revokedAfterServiceAckStillHidesSuccessfulResult() = runBlocking {
        var granted = true
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.onExecute = { granted = false }
        val result = boundary(lib, player) { granted }.select(ref, 7)
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            (result as AppResult.Failure).error.code)
    }

    @Test fun revisionChangedDuringServiceAckDoesNotClaimPlaying() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        player.onExecute = {
            lib.mutableIndex.value = lib.mutableIndex.value.copy(
                revision = 8L, status = LibraryIndexStatus.Scanning)
        }
        val result = boundary(lib, player).select(ref, 7)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            (result as AppResult.Failure).error.code)
    }

    @Test fun coroutineCancellationDuringMediaRequeryPropagates() = runBlocking {
        val lib = FakeLibrary(item(ref))
        val player = FakeGateway()
        lib.whenRead = { throw CancellationException("synthetic cancellation") }
        try {
            boundary(lib, player).select(ref, 7)
            fail("Cancellation must propagate")
        } catch (_: CancellationException) {
            assertTrue(player.commands.isEmpty())
        }
    }
}
