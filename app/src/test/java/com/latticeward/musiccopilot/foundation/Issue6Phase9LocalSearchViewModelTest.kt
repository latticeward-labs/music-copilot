package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibrarySearchResults
import com.latticeward.musiccopilot.feature.search.LocalSearchStatus
import com.latticeward.musiccopilot.feature.search.LocalSearchViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** P09 synthetic Search state machine tests. AUTHORED, NOT EXECUTED until P12. */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue6Phase9LocalSearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    @Before fun before() { Dispatchers.setMain(testDispatcher) }
    @After fun after() { Dispatchers.resetMain() }

    private val ready = LibraryIndexSnapshot(7, LibraryIndexStatus.Ready, 3)
    private val song = LibraryItem(MediaRef("audio:9"), "Aurora Lights",
        "Aurora", "Northern Sky", 222000L)

    private class FakeLibrary : LibraryRepository {
        override val index: StateFlow<LibraryIndexSnapshot> =
            MutableStateFlow(LibraryIndexSnapshot(0, LibraryIndexStatus.Unavailable, 0))
        val calls = mutableListOf<Pair<LibraryQuery, Long?>>()
        var delayGate: CompletableDeferred<Unit>? = null
        var reply: AppResult<LibrarySearchResults>? = null
        var scans = 0

        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> {
            scans++
            return AppResult.Success(LibraryReconcileResult(0, 0, 0, 0, 7))
        }
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(ref, "Fake", null, null, null))
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())

        override suspend fun searchResults(
            query: LibraryQuery,
            expectedRevision: Long?,
        ): AppResult<LibrarySearchResults> {
            calls.add(query to expectedRevision)
            delayGate?.await()
            return reply ?: AppResult.Success(
                LibrarySearchResults(
                    listOf(LibraryBrowseEntry.Song(
                        LibraryItem(MediaRef("audio:9"), "Aurora Lights", "Aurora", null, 100L)
                    )), expectedRevision ?: 7, query.limit,
                )
            )
        }
    }

    private fun success(vararg entries: LibraryBrowseEntry, revision: Long = 7L) =
        AppResult.Success(LibrarySearchResults(entries.toList(), revision, 60))

    private fun error(code: String) = AppResult.Failure(AppError.Library(
        code, Recoverability.RECOVERABLE, RecoveryAction.Retry,
    ))

    @Test fun initialIdleContainsNoResultsOrUserQuery() {
        val fake = FakeLibrary()
        val vm = LocalSearchViewModel(fake)
        assertEquals(LocalSearchStatus.Idle, vm.ui.value.status)
        assertEquals("", vm.ui.value.query)
        assertTrue(vm.ui.value.entries.isEmpty())
        assertTrue(fake.calls.isEmpty())
        assertEquals(0, fake.scans)
    }

    @Test fun authorizedReadyTypingDebouncesBeforeLocalQuery() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        assertEquals(LocalSearchStatus.Searching, vm.ui.value.status)
        advanceTimeBy(279)
        runCurrent()
        assertTrue(f.calls.isEmpty())
        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals(1, f.calls.size)
        assertEquals("aurora", f.calls.single().first.text)
        assertEquals(7L, f.calls.single().second)
        assertEquals(LocalSearchStatus.Results, vm.ui.value.status)
    }

    @Test fun rapidTypingOnlyTheLatestQueryExecutes() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("a")
        vm.updateQuery("au")
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(listOf("aurora"), f.calls.map { it.first.text })
        assertEquals(0, f.scans)
    }

    @Test fun clearReturnsIdleAndNeverRunsEmptyQuery() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        vm.clearQuery()
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.Idle, vm.ui.value.status)
        assertTrue(vm.ui.value.entries.isEmpty())
        assertTrue(f.calls.isEmpty())
    }

    @Test fun noResultsIsOnlyPublishedAfterSuccessfulEmptyResponse() = runTest {
        val f = FakeLibrary()
        f.reply = success()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("missing")
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.NoResults, vm.ui.value.status)
        assertEquals("missing", vm.ui.value.query)
        assertEquals(0, vm.ui.value.count)
    }

    @Test fun typedQueryErrorPreservesActiveQueryAndRetryRecovers() = runTest {
        val f = FakeLibrary()
        f.reply = error(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE)
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.Error, vm.ui.value.status)
        assertEquals("aurora", vm.ui.value.query)
        f.reply = null
        vm.retry()
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.Results, vm.ui.value.status)
        assertEquals(2, f.calls.size)
    }

    @Test fun staleRevisionNeverBecomesFalseNoResults() = runTest {
        val f = FakeLibrary()
        f.reply = error(AppErrorCodes.LIBRARY_BROWSE_STALE)
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.Stale, vm.ui.value.status)
        assertEquals("aurora", vm.ui.value.query)
        assertTrue(vm.ui.value.entries.isEmpty())
    }

    @Test fun mismatchedSuccessfulRevisionIsDiscarded() = runTest {
        val f = FakeLibrary()
        f.reply = success(revision = 13)
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.Stale, vm.ui.value.status)
        assertTrue(vm.ui.value.entries.isEmpty())
    }

    @Test fun revocationImmediatelyHidesOldResultsButKeepsEditableQueryContext() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.isNotEmpty())
        vm.onVerifiedIndex(ready.copy(status = LibraryIndexStatus.Unavailable), false)
        assertEquals(LocalSearchStatus.Unavailable, vm.ui.value.status)
        assertTrue(vm.ui.value.entries.isEmpty())
        assertEquals("aurora", vm.ui.value.query)
        assertNull(vm.ui.value.acceptedRevision)
    }

    @Test fun interruptedOldQueryCannotOverwriteNewRevision() = runTest {
        val f = FakeLibrary()
        val hold = CompletableDeferred<Unit>()
        f.delayGate = hold
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceTimeBy(280)
        runCurrent()
        assertEquals(1, f.calls.size)
        vm.onVerifiedIndex(ready.copy(revision = 8), true)
        f.delayGate = null
        // The newer repository response is deliberately stale (revision 7),
        // proving the ViewModel will not present it under accepted revision 8.
        f.reply = success(revision = 7)
        hold.complete(Unit)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.isEmpty())
        assertEquals(LocalSearchStatus.Stale, vm.ui.value.status)
        assertEquals(8L, f.calls.last().second)
    }

    @Test fun artistAlbumAndTrackRowsAreNotConvertedIntoPlaybackCommands() = runTest {
        val f = FakeLibrary()
        val artist = LibraryBrowseEntry.Artist(LibraryGroupId("artist:x"), "Aurora", 4)
        val album = LibraryBrowseEntry.Album(LibraryGroupId("album:y"),
            "Aurora Dreams", "Aurora", 2)
        f.reply = success(artist, LibraryBrowseEntry.Song(song), album)
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(3, vm.ui.value.count)
        assertEquals(artist, vm.ui.value.entries[0])
        assertEquals(album, vm.ui.value.entries[2])
        vm.previewSong(song)
        assertEquals(song.ref, vm.ui.value.selectedSong?.ref)
        assertEquals(0, f.scans)
    }

    @Test fun queryIsClampedAndNeverSentToRepositoryBeyondCap() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("x".repeat(5000))
        advanceUntilIdle()
        assertEquals(LocalSearchViewModel.QUERY_MAX_CHARS, vm.ui.value.query.length)
        assertEquals(60, f.calls.single().first.limit)
    }

    @Test fun originalQuerySurvivesSameRevisionRotationObservation() = runTest {
        val f = FakeLibrary()
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(ready, true)
        vm.updateQuery("   Aurora  ")
        advanceUntilIdle()
        vm.onVerifiedIndex(ready, true) // Activity re-creation, same Nav ViewModel
        assertEquals("   Aurora  ", vm.ui.value.query)
        assertEquals(1, f.calls.size)
    }

    @Test fun emptyAuthorizedIndexMayShowNoResultsAfterLocalQuery() = runTest {
        val f = FakeLibrary()
        f.reply = success(revision = 8)
        val vm = LocalSearchViewModel(f)
        vm.onVerifiedIndex(LibraryIndexSnapshot(8, LibraryIndexStatus.Empty, 0), true)
        vm.updateQuery("aurora")
        advanceUntilIdle()
        assertEquals(LocalSearchStatus.NoResults, vm.ui.value.status)
        assertEquals(8L, f.calls.single().second)
    }
}
