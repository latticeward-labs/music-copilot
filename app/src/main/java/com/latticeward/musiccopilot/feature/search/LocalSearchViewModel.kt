package com.latticeward.musiccopilot.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class LocalSearchStatus {
    Idle, Searching, Results, NoResults, Error, Stale, Unavailable
}

/**
 * Search text is ACTIVE-SURFACE memory only: retained by NavBackStackEntry
 * ViewModel across Activity rotation, not stored to SavedStateHandle,
 * DataStore, Room, analytics, logs or disk. Leaving Search releases it.
 */
data class LocalSearchUiState(
    val query: String = "",
    val status: LocalSearchStatus = LocalSearchStatus.Idle,
    val entries: List<LibraryBrowseEntry> = emptyList(),
    val acceptedRevision: Long? = null,
    val selectedSong: LibraryItem? = null,
) {
    val count: Int get() = entries.size
    val canRetry: Boolean
        get() = acceptedRevision != null &&
            (status == LocalSearchStatus.Error || status == LocalSearchStatus.Stale)
}

/**
 * WF-05 local Search owner. Debounced input (280 ms), cancellation/generation
 * guards, typed errors and OS/MediaStore permission-and-revision gating.
 * Search NEVER scans, plays, queues, invokes voice, or uses network.
 */
class LocalSearchViewModel(private val repository: LibraryRepository) : ViewModel() {
    companion object {
        const val QUERY_MAX_CHARS = 120
        const val MAX_RESULTS = 60
        const val DEBOUNCE_MS = 280L
    }

    private val mutable = MutableStateFlow(LocalSearchUiState())
    val ui: StateFlow<LocalSearchUiState> = mutable
    private var generation = 0L
    private var queryJob: Job? = null
    private var trustedRevision: Long? = null

    /** Called from real OS grant + repository index observed by Compose. */
    fun onVerifiedIndex(snapshot: LibraryIndexSnapshot, permissionGranted: Boolean) {
        val verified = permissionGranted && snapshot.revision > 0L &&
            when (snapshot.status) {
                LibraryIndexStatus.Ready -> snapshot.itemCount > 0
                LibraryIndexStatus.Empty -> snapshot.itemCount == 0
                else -> false
            }
        if (!verified) {
            if (trustedRevision != null ||
                mutable.value.status != LocalSearchStatus.Unavailable) {
                generation++
                queryJob?.cancel()
                trustedRevision = null
                mutable.value = mutable.value.copy(
                    status = LocalSearchStatus.Unavailable,
                    acceptedRevision = null, entries = emptyList(),
                    selectedSong = null,
                )
            }
            return
        }
        if (trustedRevision == snapshot.revision) return
        generation++
        queryJob?.cancel()
        trustedRevision = snapshot.revision
        mutable.value = mutable.value.copy(
            status = LocalSearchStatus.Idle, entries = emptyList(),
            acceptedRevision = snapshot.revision, selectedSong = null,
        )
        if (LibraryTextNormalization.key(mutable.value.query).isNotEmpty()) {
            scheduleSearch(debounce = false)
        }
    }

    /** Only this active-surface field survives rotation through the ViewModel. */
    fun updateQuery(text: String) {
        val bounded = text.take(QUERY_MAX_CHARS)
        if (bounded == mutable.value.query) return
        generation++
        queryJob?.cancel()
        val normalized = LibraryTextNormalization.key(bounded)
        mutable.value = mutable.value.copy(
            query = bounded,
            entries = emptyList(),
            selectedSong = null,
            status = when {
                trustedRevision == null -> LocalSearchStatus.Unavailable
                normalized.isEmpty() -> LocalSearchStatus.Idle
                else -> LocalSearchStatus.Searching
            },
        )
        if (trustedRevision != null && normalized.isNotEmpty()) scheduleSearch(debounce = true)
    }

    fun clearQuery() = updateQuery("")

    fun retry() {
        if (!mutable.value.canRetry) return
        generation++
        queryJob?.cancel()
        scheduleSearch(debounce = false)
    }

    /**
     * P10 will re-query the MediaStore source and hand off to service-owned
     * PlaybackGateway. P09 selection is explicitly NOT a Play command.
     */
    fun previewSong(item: LibraryItem) {
        if (trustedRevision != null &&
            mutable.value.status == LocalSearchStatus.Results &&
            mutable.value.entries.any {
                it is LibraryBrowseEntry.Song && it.media.ref == item.ref
            }) {
            mutable.value = mutable.value.copy(selectedSong = item)
        }
    }

    fun closePreview() {
        mutable.value = mutable.value.copy(selectedSong = null)
    }

    private fun scheduleSearch(debounce: Boolean) {
        val revision = trustedRevision ?: return
        val queryText = mutable.value.query
        if (LibraryTextNormalization.key(queryText).isEmpty()) {
            mutable.value = mutable.value.copy(
                status = LocalSearchStatus.Idle, entries = emptyList(),
            )
            return
        }
        val ticket = ++generation
        mutable.value = mutable.value.copy(
            status = LocalSearchStatus.Searching, entries = emptyList(),
            selectedSong = null,
        )
        queryJob = viewModelScope.launch {
            try {
                if (debounce) delay(DEBOUNCE_MS)
                val answer = repository.searchResults(
                    LibraryQuery(queryText, MAX_RESULTS),
                    expectedRevision = revision,
                )
                if (ticket != generation || trustedRevision != revision) return@launch
                when (answer) {
                    is AppResult.Success -> {
                        val result = answer.value
                        if (result.revision != revision) {
                            mutable.value = mutable.value.copy(
                                status = LocalSearchStatus.Stale,
                                entries = emptyList(),
                            )
                        } else {
                            mutable.value = mutable.value.copy(
                                status = if (result.entries.isEmpty())
                                    LocalSearchStatus.NoResults
                                else LocalSearchStatus.Results,
                                entries = result.entries,
                            )
                        }
                    }
                    is AppResult.Failure -> {
                        mutable.value = mutable.value.copy(
                            status = if (answer.error.code == AppErrorCodes.LIBRARY_BROWSE_STALE)
                                LocalSearchStatus.Stale
                            else LocalSearchStatus.Error,
                            entries = emptyList(),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (ticket == generation && trustedRevision == revision) {
                    mutable.value = mutable.value.copy(
                        status = LocalSearchStatus.Error, entries = emptyList(),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        generation++
        queryJob?.cancel()
        super.onCleared()
    }
}
