package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackMediaResolutionPort
import com.latticeward.musiccopilot.playback.api.VerifiedPlaybackMedia
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.service.ServiceQueueEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** P04 fake-driven queue source tests. No real provider, Media3, device or APK. */
class Issue7Phase4ServiceQueueEngineTest {
    private val dispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }

    private class FakeResolver : PlaybackMediaResolutionPort {
        val seen = mutableListOf<MediaRef>()
        val missing = mutableSetOf<MediaRef>()
        var revoked = false
        var invalidated = 0
        var hook: suspend (MediaRef) -> Unit = {}

        override suspend fun resolve(ref: MediaRef): AppResult<VerifiedPlaybackMedia> {
            seen += ref
            hook(ref)
            if (revoked) return AppResult.Failure(
                AppError.Permission(
                    AppErrorCodes.PERMISSION_REVOKED,
                    Recoverability.USER_ACTION_REQUIRED,
                    RecoveryAction.OpenSettings,
                )
            )
            if (ref in missing) return AppResult.Failure(
                AppError.MediaItem(
                    AppErrorCodes.MEDIA_ITEM_MISSING,
                    Recoverability.RECOVERABLE,
                    RecoveryAction.Skip,
                )
            )
            // Synthetic canonical content reference only; never a personal song.
            return AppResult.Success(VerifiedPlaybackMedia(
                ref, "content://media/external_primary/audio/media/${ref.value.removePrefix("audio:")}"
            ))
        }

        override fun invalidate() { invalidated++ }
    }

    private val a = MediaRef("audio:1")
    private val b = MediaRef("audio:2")
    private val c = MediaRef("audio:3")

    private fun replace(
        items: List<MediaRef>,
        index: Int = 0,
        playing: Boolean = true,
        startMs: Long = 0,
    ) = PlaybackCommand.ReplaceQueue(CommandId("synthetic-queue"), items, index, startMs, playing)

    @Test fun selectedItemIsRevalidatedAfterOthersWithoutResorting() = runBlocking {
        val fake = FakeResolver()
        val result = ServiceQueueEngine(fake, dispatchers).prepare(replace(listOf(c, a, b), 1))
            as AppResult.Success
        assertEquals(listOf(c, a, b), result.value.entries.map { it.ref })
        assertEquals(1, result.value.selectedIndex)
        assertEquals(listOf(a, c, b, a), fake.seen)
    }

    @Test fun nonselectedMissingCandidatesDropWithExactOccurrenceIndex() = runBlocking {
        val fake = FakeResolver().apply { missing += a }
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, c), 2, false, 12_345L)) as AppResult.Success
        assertEquals(listOf(b, c), result.value.entries.map { it.ref })
        assertEquals(1, result.value.selectedIndex)
        assertEquals(1, result.value.skippedUnavailable)
        assertEquals(12_345L, result.value.startPositionMs)
        assertFalse(result.value.playWhenReady)
    }

    @Test fun duplicateOccurrencesRemainOrderedEvenIfSameMediaRef() = runBlocking {
        val fake = FakeResolver()
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, a), 2)) as AppResult.Success
        assertEquals(listOf(a, b, a), result.value.entries.map { it.ref })
        assertEquals(2, result.value.selectedIndex)
        assertEquals(0, result.value.skippedUnavailable)
    }

    @Test fun selectedMissingFailsWithoutInspectingOtherCandidates() = runBlocking {
        val fake = FakeResolver().apply { missing += b }
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, c), 1))
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            (result as AppResult.Failure).error.code)
        assertEquals(listOf(b), fake.seen)
    }

    @Test fun revokedPermissionOnAnyOtherItemAbortsWholeQueue() = runBlocking {
        val fake = FakeResolver()
        fake.hook = { ref -> if (ref == b) fake.revoked = true }
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, c), 0))
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            (result as AppResult.Failure).error.code)
        assertEquals(listOf(a, b), fake.seen)
    }

    @Test fun selectedDisappearsDuringSlowCandidateChecksFailsClosed() = runBlocking {
        val fake = FakeResolver()
        fake.hook = { ref -> if (ref == c) fake.missing += b }
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, c), 1))
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            (result as AppResult.Failure).error.code)
        assertEquals(listOf(b, a, c, b), fake.seen)
    }

    @Test fun malformedEmptyAndOversizedQueueFailWithoutMediaAccess() = runBlocking {
        val fake = FakeResolver()
        val engine = ServiceQueueEngine(fake, dispatchers)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            (engine.prepare(replace(emptyList())) as AppResult.Failure).error.code)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            (engine.prepare(replace(listOf(a), index = 3)) as AppResult.Failure).error.code)
        assertEquals(AppErrorCodes.PLAYBACK_REJECTED,
            (engine.prepare(replace(List(5_001) { a })) as AppResult.Failure).error.code)
        assertTrue(fake.seen.isEmpty())
    }

    @Test fun maximumAllowedQueueIsBoundedAtFiveThousand() = runBlocking {
        val fake = FakeResolver()
        val items = (1..5_000).map { MediaRef("audio:$it") }
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(items, 4_999)) as AppResult.Success
        assertEquals(5_000, result.value.entries.size)
        assertEquals(4_999, result.value.selectedIndex)
        assertEquals(5_001, fake.seen.size) // selected occurrence checked twice
    }

    @Test fun cancellationDuringCandidatePreparationPropagates() = runBlocking {
        val fake = FakeResolver()
        val entered = CompletableDeferred<Unit>()
        val unblock = CompletableDeferred<Unit>()
        fake.hook = { ref ->
            if (ref == c) {
                entered.complete(Unit)
                unblock.await()
            }
        }
        var observed = false
        val job = launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                ServiceQueueEngine(fake, dispatchers).prepare(replace(listOf(a, b, c), 0))
            } catch (cancelled: CancellationException) { observed = true }
        }
        entered.await()
        job.cancelAndJoin()
        assertTrue(observed)
        unblock.complete(Unit)
    }

    @Test fun queuedItemRevalidationAndInvalidationDelegateToSource() = runBlocking {
        val fake = FakeResolver()
        val engine = ServiceQueueEngine(fake, dispatchers)
        assertTrue(engine.revalidate(c) is AppResult.Success)
        engine.invalidate()
        assertEquals(1, fake.invalidated)
        assertEquals(listOf(c), fake.seen)
    }

    @Test fun preparedQueueToStringNeverLeaksContentUri() = runBlocking {
        val result = ServiceQueueEngine(FakeResolver(), dispatchers)
            .prepare(replace(listOf(a))) as AppResult.Success
        assertEquals("PreparedPlaybackQueue(redacted)", result.value.toString())
        assertFalse(result.value.toString().contains("content://"))
    }

    @Test fun invalidSelectedRequestDoesNotYieldAnyCommitProposal() = runBlocking {
        val fake = FakeResolver().apply { missing += b }
        val before = listOf(a, c)
        val result = ServiceQueueEngine(fake, dispatchers)
            .prepare(replace(listOf(a, b, c), 1))
        assertTrue(result is AppResult.Failure)
        assertEquals(listOf(a, c), before) // pure engine cannot mutate live Player
    }
}
