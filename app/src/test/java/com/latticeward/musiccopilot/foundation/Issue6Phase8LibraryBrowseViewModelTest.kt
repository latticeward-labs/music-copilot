package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowsePage
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.feature.library.LibraryBrowseViewModel
import com.latticeward.musiccopilot.feature.library.LibraryPageFailure
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** P08 synthetic, authored-but-NOT_RUN JVM ViewModel contract suite. */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue6Phase8LibraryBrowseViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @Before fun setup() {
        Dispatchers.setMain(mainDispatcher)
    }
    @After fun cleanup() {
        Dispatchers.resetMain()
    }

    private fun song(id: Int): LibraryItem = LibraryItem(
        ref = MediaRef("audio:$id"), title = "Synthetic $id",
        artist = "Artist", album = "Local Album", durationMs = 1000L,
    )

    private val ready = LibraryIndexSnapshot(7, LibraryIndexStatus.Ready, 120)

    private class FakeLibrary : LibraryRepository {
        private val mutableIndex =
            MutableStateFlow(LibraryIndexSnapshot(0, LibraryIndexStatus.Unavailable, 0))
        override val index: StateFlow<LibraryIndexSnapshot> = mutableIndex
        val requests = mutableListOf<LibraryBrowseRequest>()
        var scanCalls = 0
        var outcome: AppResult<LibraryBrowsePage>? = null
        var deferred: CompletableDeferred<Unit>? = null
        var totalSongs = 120

        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> {
            scanCalls++
            return AppResult.Failure(AppError.Library(
                AppErrorCodes.LIBRARY_RECONCILE_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.Retry,
            ))
        }
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(ref, "Synthetic Selected", null, null, null))
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())

        override suspend fun browse(request: LibraryBrowseRequest): AppResult<LibraryBrowsePage> {
            requests.add(request)
            deferred?.await()
            outcome?.let { return it }
            val count = when (request.category) {
                LibraryBrowseCategory.Songs -> totalSongs
                LibraryBrowseCategory.Artists -> if (request.groupId == null) 2 else 3
                LibraryBrowseCategory.Albums -> if (request.groupId == null) 2 else 3
                LibraryBrowseCategory.Folders -> if (request.groupId == null) 2 else 3
            }
            val entries = (request.page.offset until
                minOf(request.page.offset + request.page.limit, count)).map { n ->
                when {
                    request.groupId != null || request.category == LibraryBrowseCategory.Songs ->
                        LibraryBrowseEntry.Song(LibraryItem(MediaRef("audio:$n"),
                            "Synthetic $n", "Synthetic Artist", "Synthetic Album", 1000L))
                    request.category == LibraryBrowseCategory.Artists ->
                        LibraryBrowseEntry.Artist(LibraryGroupId("artist:$n"), "Artist $n", 3)
                    request.category == LibraryBrowseCategory.Albums ->
                        LibraryBrowseEntry.Album(LibraryGroupId("album:$n"),
                            "Album $n", "Artist", 3)
                    else -> LibraryBrowseEntry.Folder(
                        LibraryGroupId("folder:$n"), "Folder $n", 3)
                }
            }
            val next = if (request.page.offset + entries.size < count)
                request.page.offset + entries.size else null
            return AppResult.Success(LibraryBrowsePage(
                entries = entries, totalCount = count, revision = 7,
                nextOffset = next,
            ))
        }
    }

    @Test fun newViewModelNeverScansOrClaimsReady() {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        assertTrue(vm.ui.value.entries.isEmpty())
        assertNull(vm.ui.value.revision)
        assertEquals(0, data.scanCalls)
        assertTrue(data.requests.isEmpty())
    }

    @Test fun verifiedReadyLoadsOnlyFirstFiftyItemsAndPinnedRevision() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        assertEquals(50, vm.ui.value.entries.size)
        assertEquals(120, vm.ui.value.totalCount)
        assertEquals(50, vm.ui.value.nextOffset)
        assertEquals(7L, data.requests.single().page.expectedRevision)
        assertEquals(0, data.scanCalls)
    }

    @Test fun allPagesAreBoundedAndAppendWithoutDuplicateRows() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(120, vm.ui.value.entries.size)
        assertNull(vm.ui.value.nextOffset)
        assertEquals(listOf(0, 50, 100), data.requests.map { it.page.offset })
        assertFalse(vm.ui.value.canLoadMore)
        assertEquals(0, data.scanCalls)
    }

    @Test fun eachCategoryQueriesItsOwnGroupRowsWithoutScanning() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        vm.selectCategory(LibraryBrowseCategory.Artists)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Artist)
        vm.selectCategory(LibraryBrowseCategory.Albums)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Album)
        vm.selectCategory(LibraryBrowseCategory.Folders)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Folder)
        vm.selectCategory(LibraryBrowseCategory.Songs)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Song)
        assertEquals(0, data.scanCalls)
    }

    @Test fun groupDrilldownIsRevisionPinnedAndBackRestoresGroupList() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        vm.selectCategory(LibraryBrowseCategory.Artists)
        advanceUntilIdle()
        val artist = vm.ui.value.entries.first() as LibraryBrowseEntry.Artist
        vm.openGroup(artist)
        advanceUntilIdle()
        assertEquals(artist.id, vm.ui.value.groupId)
        assertEquals(artist.label, vm.ui.value.groupTitle)
        assertTrue(vm.ui.value.entries.all { it is LibraryBrowseEntry.Song })
        assertEquals(7L, data.requests.last().page.expectedRevision)
        assertTrue(vm.backToGroups())
        advanceUntilIdle()
        assertNull(vm.ui.value.groupId)
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Artist)
    }

    @Test fun revocationImmediatelyDropsEveryPreviouslyVisibleRow() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        assertTrue(vm.ui.value.entries.isNotEmpty())
        vm.onVerifiedIndex(ready.copy(status = LibraryIndexStatus.Unavailable), false)
        assertTrue(vm.ui.value.entries.isEmpty())
        assertNull(vm.ui.value.selectedSong)
        assertNull(vm.ui.value.revision)
        vm.loadMore()
        assertEquals(1, data.requests.size)
    }

    @Test fun oldInFlightCategoryPageCannotOverrideNewCategory() = runTest {
        val data = FakeLibrary()
        data.deferred = CompletableDeferred()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        runCurrent()
        assertEquals(1, data.requests.size)
        vm.selectCategory(LibraryBrowseCategory.Albums)
        data.deferred?.complete(Unit)
        advanceUntilIdle()
        assertEquals(LibraryBrowseCategory.Albums, vm.ui.value.category)
        assertTrue(vm.ui.value.entries.first() is LibraryBrowseEntry.Album)
    }

    @Test fun changedIndexYieldsExplicitStaleStateNotFalseEmpty() = runTest {
        val data = FakeLibrary()
        data.outcome = AppResult.Failure(AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_STALE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        ))
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        assertEquals(LibraryPageFailure.IndexChanged, vm.ui.value.failure)
        assertEquals(0, vm.ui.value.totalCount)
        assertTrue(vm.ui.value.entries.isEmpty())
    }

    @Test fun genericRoomReadErrorHasRetryWithoutStartingScan() = runTest {
        val data = FakeLibrary()
        data.outcome = AppResult.Failure(AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        ))
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        assertEquals(LibraryPageFailure.ReadFailed, vm.ui.value.failure)
        data.outcome = null
        vm.retryPage()
        advanceUntilIdle()
        assertEquals(null, vm.ui.value.failure)
        assertEquals(50, vm.ui.value.entries.size)
        assertEquals(0, data.scanCalls)
    }

    @Test fun selectingSongNeverSendsPlaybackOrMutatesMedia() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        val first = vm.ui.value.entries.first() as LibraryBrowseEntry.Song
        vm.previewSong(first.media)
        assertEquals(first.media.ref, vm.ui.value.selectedSong?.ref)
        assertTrue(vm.backToGroups())
        assertNull(vm.ui.value.selectedSong)
        assertEquals(0, data.scanCalls)
    }

    @Test fun repeatedReadySnapshotDoesNotRefetchSameRevision() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        assertEquals(1, data.requests.size)
    }

    @Test fun recoveredRevisionInvalidatesOldPageBeforeFetchingAgain() = runTest {
        val data = FakeLibrary()
        val vm = LibraryBrowseViewModel(data)
        vm.onVerifiedIndex(ready, true)
        advanceUntilIdle()
        vm.onVerifiedIndex(ready.copy(
            revision = 8, status = LibraryIndexStatus.Scanning), true)
        assertTrue(vm.ui.value.entries.isEmpty())
        vm.onVerifiedIndex(ready.copy(revision = 8), true)
        advanceUntilIdle()
        // Fake provider uses stale revision=7; UI must not accept its rows.
        assertEquals(LibraryPageFailure.IndexChanged, vm.ui.value.failure)
        assertTrue(vm.ui.value.entries.isEmpty())
    }
}
