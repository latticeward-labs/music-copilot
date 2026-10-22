package com.latticeward.musiccopilot.foundation

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
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
import com.latticeward.musiccopilot.feature.search.LocalSearchScreen
import com.latticeward.musiccopilot.feature.search.LocalSearchViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

/**
 * WF-05 synthetic Compose semantics fixtures. AUTHORED NOT_RUN.
 * Required real native execution P12; no screenshots contain personal media.
 */
class Issue6Phase9SearchComposeInstrumentedTest {
    @get:Rule val rule = createComposeRule()

    private val selected = LibraryItem(MediaRef("audio:9"),
        "Aurora Lights", "Aurora", "Northern Sky", 222000L)
    private val artist = LibraryBrowseEntry.Artist(
        LibraryGroupId("artist:synthetic"), "Aurora", 2,
    )
    private val album = LibraryBrowseEntry.Album(
        LibraryGroupId("album:synthetic"), "Northern Sky", "Aurora", 1,
    )

    private class FakeSearch(
        var result: AppResult<LibrarySearchResults>,
    ) : LibraryRepository {
        override val index: StateFlow<LibraryIndexSnapshot> =
            MutableStateFlow(LibraryIndexSnapshot(7, LibraryIndexStatus.Ready, 3))
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
            AppResult.Success(LibraryReconcileResult(0, 0, 0, 0, 7))
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(ref, "synthetic", null, null, 100L))
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())
        override suspend fun searchResults(
            query: LibraryQuery, expectedRevision: Long?,
        ): AppResult<LibrarySearchResults> = result
    }

    private fun mount(
        result: AppResult<LibrarySearchResults>,
        granted: Boolean = true,
        onGroup: (LibraryBrowseEntry) -> Unit = {},
    ): FakeSearch {
        val db = FakeSearch(result)
        val vm = LocalSearchViewModel(db)
        rule.setContent {
            MusicCopilotTheme {
                LocalSearchScreen(
                    model = vm,
                    index = if (granted) LibraryIndexSnapshot(
                        7, LibraryIndexStatus.Ready, 3,
                    ) else LibraryIndexSnapshot(
                        7, LibraryIndexStatus.Unavailable, 0,
                    ),
                    permission = if (granted) PermissionStatus.Granted
                        else PermissionStatus.Revoked,
                    onBack = {}, onOpenGroup = onGroup,
                    onSettings = {}, onReviewAccess = {},
                )
            }
        }
        rule.waitForIdle()
        return db
    }

    private fun result(vararg entries: LibraryBrowseEntry) =
        AppResult.Success(LibrarySearchResults(entries.toList(), 7, 60))

    private fun search(text: String) {
        rule.onNode(hasSetTextAction()).performTextInput(text)
    }

    private fun awaitVisible(text: String) {
        rule.waitUntil(6000) {
            rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun searchIdleShowsBackFieldClearHomeAndSearchNavigation() {
        mount(result())
        // Search appears in both the heading and selected bottom tab.
        // Assert the unambiguous Back/field instead of a multi-match node.
        rule.onNodeWithText("Back").assertExists()
        rule.onNodeWithText("Search local music").assertExists()
        rule.onNodeWithText("Clear").assertExists()
        rule.onNodeWithText("Search your local library.").assertExists()
        rule.onNodeWithText("Home").assertExists()
    }

    @Test fun approvedArtistTrackAlbumAppearAsLocalResultTypes() {
        mount(result(artist, LibraryBrowseEntry.Song(selected), album))
        search("aurora")
        awaitVisible("3 local results")
        rule.onNodeWithText("Aurora").assertExists()
        rule.onNodeWithText("Aurora Lights").assertExists()
        rule.onNodeWithText("Northern Sky").assertExists()
    }

    @Test fun emptyQueryResultPreservesTextWithClearControl() {
        mount(result())
        search("NoSuchSong")
        awaitVisible("No local results")
        rule.onNodeWithText("Edit or clear the preserved query.").assertExists()
        rule.onNodeWithText("Clear").performClick()
        awaitVisible("Search your local library.")
        rule.onNodeWithText("No local results").assertDoesNotExist()
    }

    @Test fun repositoryFailureIsErrorNotNoResultsAndRetryVisible() {
        mount(AppResult.Failure(AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        )))
        search("aurora")
        awaitVisible("Search failed")
        rule.onNodeWithText("Retry").assertExists()
        rule.onNodeWithText("No local results").assertDoesNotExist()
    }

    @Test fun artistOpenNavigatesThroughCallbackWithoutPlaying() {
        val opened = AtomicReference<LibraryBrowseEntry?>()
        mount(result(artist), onGroup = { opened.set(it) })
        search("aurora")
        awaitVisible("1 local results")
        rule.onNodeWithText("Aurora").performClick()
        assertEquals(artist, opened.get())
    }

    @Test fun selectionOnlyShowsNotPlayingAndDoesNotStartMusic() {
        mount(result(LibraryBrowseEntry.Song(selected)))
        search("aurora")
        awaitVisible("1 local results")
        rule.onNodeWithText("Aurora Lights").performClick()
        awaitVisible("Selected: Aurora Lights · Not playing")
    }

    @Test fun revokedPermissionHidesFieldResultsAndShowsRecovery() {
        mount(result(LibraryBrowseEntry.Song(selected)), granted = false)
        rule.onNodeWithText("Review music access").assertExists()
        rule.onNodeWithText("Aurora Lights").assertDoesNotExist()
    }
    @Test fun searchNoResultsIsAnnouncedByTalkBackLiveRegion() {
        mount(result())
        search("nonexistent")
        awaitVisible("No local results")
        rule.onNodeWithText("No local results").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
        )
    }

    @Test fun recoverableSearchErrorIsAnnouncedByTalkBackLiveRegion() {
        mount(AppResult.Failure(AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry,
        )))
        search("aurora")
        awaitVisible("Search failed")
        rule.onNodeWithText("Search failed").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
        )
    }

}
