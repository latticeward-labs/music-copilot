package com.latticeward.musiccopilot.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.application.playback.LibraryMediaSelectionUseCase
import com.latticeward.musiccopilot.core.application.playback.SelectedPlaybackState
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibraryItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** P08 UI-only projection; the repository/MediaStore owns all index truth. */
enum class LibraryPageFailure { ReadFailed, IndexChanged }

data class LibraryBrowseUiState(
    val category: LibraryBrowseCategory = LibraryBrowseCategory.Songs,
    val groupId: LibraryGroupId? = null,
    val groupTitle: String? = null,
    val entries: List<LibraryBrowseEntry> = emptyList(),
    val totalCount: Int = 0,
    val revision: Long? = null,
    val nextOffset: Int? = null,
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val failure: LibraryPageFailure? = null,
    val selectedSong: LibraryItem? = null,
    val selectionStatus: SelectedSongUiStatus = SelectedSongUiStatus.Idle,
) {
    val inGroup: Boolean get() = groupId != null
    val canLoadMore: Boolean
        get() = nextOffset != null && !loading && !loadingMore && failure == null
}

/**
 * A single presentation owner survives Activity rotation, but not process
 * death. No song title/path/ref, group ID, cursor or permission grant is saved.
 * The only action which starts an OS scan remains ApplicationBindings.
 *
 * Browse requests are revision-pinned and generation-guarded: results from a
 * cancelled page or different category/group/revision never contaminate UI.
 */
class LibraryBrowseViewModel(
    private val library: LibraryRepository,
    private val selection: LibraryMediaSelectionUseCase? = null,
) : ViewModel() {
    private val mutable = MutableStateFlow(LibraryBrowseUiState())
    val ui: StateFlow<LibraryBrowseUiState> = mutable

    private var acceptedRevision: Long? = null
    private var epoch = 0L
    private var pageJob: Job? = null
    private var selectJob: Job? = null
    private var selectTicket = 0L

    private fun cancelSelection() {
        ++selectTicket
        selectJob?.cancel()
        selectJob = null
    }

    /** Called by lifecycle-aware Compose when the *verified* index changes. */
    fun onVerifiedIndex(snapshot: LibraryIndexSnapshot, permissionGranted: Boolean) {
        val ready = permissionGranted &&
            snapshot.status == LibraryIndexStatus.Ready &&
            snapshot.itemCount > 0 && snapshot.revision > 0
        val empty = permissionGranted &&
            snapshot.status == LibraryIndexStatus.Empty &&
            snapshot.itemCount == 0 && snapshot.revision > 0
        if (!ready && !empty) {
            acceptedRevision = null
            epoch++
            pageJob?.cancel()
            pageJob = null
            cancelSelection()
            // Revocation, Scanning and errors must not show a previously
            // accepted page as current or playable.
            mutable.value = mutable.value.copy(
                groupId = null, groupTitle = null, entries = emptyList(),
                totalCount = 0, revision = null, nextOffset = null,
                loading = false, loadingMore = false, failure = null,
                selectedSong = null,
            )
            return
        }
        if (acceptedRevision == snapshot.revision) return
        acceptedRevision = snapshot.revision
        cancelSelection()
        epoch++
        pageJob?.cancel()
        cancelSelection()
        mutable.value = mutable.value.copy(
            groupId = null, groupTitle = null, entries = emptyList(),
            totalCount = 0, revision = snapshot.revision, nextOffset = null,
            loading = false, loadingMore = false, failure = null,
            selectedSong = null,
        )
        // Even an approved Empty index may be queried for its zero song
        // count; no scan is started and no invented results are generated.
        requestPage(0)
    }

    fun selectCategory(category: LibraryBrowseCategory) {
        if (mutable.value.category == category && !mutable.value.inGroup) return
        epoch++
        pageJob?.cancel()
        cancelSelection()
        mutable.value = mutable.value.copy(
            category = category, groupId = null, groupTitle = null,
            entries = emptyList(), totalCount = 0, nextOffset = null,
            loading = false, loadingMore = false, failure = null,
            selectedSong = null,
        )
        if (acceptedRevision != null) requestPage(0)
    }

    fun openGroup(entry: LibraryBrowseEntry) {
        val group = when (entry) {
            is LibraryBrowseEntry.Artist -> entry.id to entry.label
            is LibraryBrowseEntry.Album -> entry.id to entry.label
            is LibraryBrowseEntry.Folder -> entry.id to entry.label
            is LibraryBrowseEntry.Song -> return
        }
        if (acceptedRevision == null) return
        epoch++
        pageJob?.cancel()
        cancelSelection()
        mutable.value = mutable.value.copy(
            groupId = group.first, groupTitle = group.second,
            entries = emptyList(), totalCount = 0, nextOffset = null,
            loading = false, loadingMore = false, failure = null,
            selectedSong = null,
        )
        requestPage(0)
    }

    fun backToGroups(): Boolean {
        if (mutable.value.selectedSong != null) {
            cancelSelection()
            mutable.value = mutable.value.copy(selectedSong = null,
                selectionStatus = SelectedSongUiStatus.Idle)
            return true
        }
        if (!mutable.value.inGroup) return false
        epoch++
        pageJob?.cancel()
        mutable.value = mutable.value.copy(
            groupId = null, groupTitle = null, entries = emptyList(),
            totalCount = 0, nextOffset = null,
            loading = false, loadingMore = false, failure = null,
            selectedSong = null,
        )
        if (acceptedRevision != null) requestPage(0)
        return true
    }

    /**
     * P10: a tap is validated by MediaStore first and acknowledged only by
     * service-owned PlaybackGateway. Older isolated P08 JVM fakes may omit
     * the selection boundary and therefore show only a local preview.
     */
    fun previewSong(song: LibraryItem) {
        val revision = acceptedRevision ?: return
        if (mutable.value.entries.none {
                it is LibraryBrowseEntry.Song && it.media.ref == song.ref
            }) return
        if (mutable.value.selectedSong?.ref == song.ref &&
            mutable.value.selectionStatus == SelectedSongUiStatus.Validating) return
        cancelSelection()
        val ticket = selectTicket
        val delegate = selection
        mutable.value = mutable.value.copy(
            selectedSong = song,
            selectionStatus = if (delegate == null) SelectedSongUiStatus.Idle
                else SelectedSongUiStatus.Validating,
        )
        if (delegate == null) return
        selectJob = viewModelScope.launch {
            try {
                val answer = delegate.select(song.ref, revision)
                if (selectTicket != ticket || acceptedRevision != revision ||
                    mutable.value.selectedSong?.ref != song.ref) return@launch
                mutable.value = mutable.value.copy(
                    selectionStatus = when (answer) {
                        is AppResult.Success ->
                            if (answer.value.state == SelectedPlaybackState.Playing)
                                SelectedSongUiStatus.ServicePlaying
                            else SelectedSongUiStatus.ServiceAcceptedPending
                        is AppResult.Failure ->
                            SelectedSongFailurePolicy.from(answer.error)
                    },
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (selectTicket == ticket) mutable.value = mutable.value.copy(
                    selectionStatus = SelectedSongUiStatus.PlaybackUnavailable)
            }
        }
    }

    fun closePreview() {
        cancelSelection()
        mutable.value = mutable.value.copy(
            selectedSong = null, selectionStatus = SelectedSongUiStatus.Idle)
    }

    fun loadMore() {
        if (!mutable.value.canLoadMore || acceptedRevision == null) return
        requestPage(mutable.value.nextOffset ?: return)
    }

    fun retryPage() {
        if (acceptedRevision == null || mutable.value.failure == null) return
        requestPage(mutable.value.nextOffset ?: mutable.value.entries.size)
    }

    private fun requestPage(offset: Int) {
        val revision = acceptedRevision ?: return
        val before = mutable.value
        if (before.loading || before.loadingMore) return
        val ticket = ++epoch
        val more = offset > 0
        mutable.value = before.copy(
            loading = !more, loadingMore = more, failure = null,
        )
        pageJob = viewModelScope.launch {
            try {
                val query = LibraryBrowseRequest(
                    category = before.category,
                    page = LibraryPageRequest(
                        offset = offset, limit = LibraryPageRequest.DEFAULT_PAGE_SIZE,
                        expectedRevision = revision,
                    ),
                    groupId = before.groupId,
                )
                val answer = library.browse(query)
                if (ticket != epoch || acceptedRevision != revision) return@launch
                when (answer) {
                    is AppResult.Success -> {
                        val page = answer.value
                        if (page.revision != revision) {
                            mutable.value = mutable.value.copy(
                                entries = emptyList(), totalCount = 0,
                                nextOffset = null, loading = false,
                                loadingMore = false, failure = LibraryPageFailure.IndexChanged,
                            )
                        } else {
                            mutable.value = mutable.value.copy(
                                entries = if (more) before.entries + page.entries
                                          else page.entries,
                                totalCount = page.totalCount,
                                nextOffset = page.nextOffset,
                                loading = false, loadingMore = false, failure = null,
                            )
                        }
                    }
                    is AppResult.Failure -> {
                        mutable.value = mutable.value.copy(
                            loading = false, loadingMore = false,
                            failure = if (answer.error.code == AppErrorCodes.LIBRARY_BROWSE_STALE)
                                LibraryPageFailure.IndexChanged
                            else LibraryPageFailure.ReadFailed,
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (ticket == epoch) {
                    mutable.value = mutable.value.copy(
                        loading = false, loadingMore = false,
                        failure = LibraryPageFailure.ReadFailed,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        epoch++
        pageJob?.cancel()
        cancelSelection()
        super.onCleared()
    }
}
