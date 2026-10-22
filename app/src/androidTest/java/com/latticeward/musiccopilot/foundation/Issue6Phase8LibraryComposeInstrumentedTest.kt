package com.latticeward.musiccopilot.foundation

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.junit4.createComposeRule
import com.latticeward.musiccopilot.app.navigation.FoundationReadiness
import com.latticeward.musiccopilot.app.theme.MusicCopilotTheme
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
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
import com.latticeward.musiccopilot.feature.library.LibraryBrowseScreen
import com.latticeward.musiccopilot.feature.library.LibraryBrowseViewModel
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * P08 real Compose accessibility/interaction tests, AUTHORED NOT_RUN.
 * P12 executes these using a test APK/device and approved synthetic media.
 */
class Issue6Phase8LibraryComposeInstrumentedTest {
    @get:Rule val rule = createComposeRule()

    private class FakeBrowse : LibraryRepository {
        override val index: StateFlow<LibraryIndexSnapshot> =
            MutableStateFlow(LibraryIndexSnapshot(3, LibraryIndexStatus.Ready, 1))
        var scanCount = 0
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> {
            scanCount++
            return AppResult.Success(LibraryReconcileResult(0, 0, 0, 0, 3))
        }
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(ref, "Synthetic One", null, null, null))
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())

        override suspend fun browse(request: LibraryBrowseRequest): AppResult<LibraryBrowsePage> {
            val entry = when {
                request.groupId != null || request.category == LibraryBrowseCategory.Songs ->
                    LibraryBrowseEntry.Song(LibraryItem(MediaRef("audio:1"),
                        "Synthetic One", "Synthetic Artist", "Synthetic Album", 1500L))
                request.category == LibraryBrowseCategory.Artists ->
                    LibraryBrowseEntry.Artist(LibraryGroupId("artist:fixture"), "Synthetic Artist", 1)
                request.category == LibraryBrowseCategory.Albums ->
                    LibraryBrowseEntry.Album(LibraryGroupId("album:fixture"), "Synthetic Album",
                        "Synthetic Artist", 1)
                else -> LibraryBrowseEntry.Folder(
                    LibraryGroupId("folder:fixture"), "Synthetic Folder", 1)
            }
            return AppResult.Success(LibraryBrowsePage(
                listOf(entry), 1, 3, null,
            ))
        }
    }

    private val noServiceTrack = PlaybackSnapshot(
        revision = 0, queueRevision = 0, playbackState = PlaybackState.Idle,
        queue = emptyList(), currentIndex = null, currentItem = null,
        positionMs = 0L, durationMs = null, canSeek = false,
        canSkipPrevious = false, canSkipNext = false,
    )

    private fun mount(
        db: FakeBrowse,
        status: LibraryIndexStatus = LibraryIndexStatus.Ready,
        permission: PermissionStatus = PermissionStatus.Granted,
        readiness: FoundationReadiness = FoundationReadiness.LocalLibraryReady,
        onRescan: () -> Unit = {},
    ) {
        val vm = LibraryBrowseViewModel(db)
        rule.setContent {
            MusicCopilotTheme {
                LibraryBrowseScreen(
                    model = vm,
                    index = LibraryIndexSnapshot(3, status, if (status == LibraryIndexStatus.Ready) 1 else 0),
                    readiness = readiness, permission = permission,
                    playbackConnection = PlaybackConnectionState.Disconnected,
                    playback = noServiceTrack,
                    onRefreshPermission = {},
                    onRetryLibraryScan = {},
                    onRescanLibrary = onRescan,
                    onSettings = {}, onExit = {},
                )
            }
        }
        rule.waitForIdle()
    }

    @Test fun approvedLibraryHeadingFourCategoriesAndNavigationAreVisible() {
        mount(FakeBrowse())
        rule.onNodeWithText("Library").assertExists()
        rule.onNodeWithText("Songs").assertExists()
        rule.onNodeWithText("Artists").assertExists()
        rule.onNodeWithText("Albums").assertExists()
        rule.onNodeWithText("Folders").assertExists()
        rule.onNodeWithText("Home").assertExists()
        rule.onNodeWithText("Settings").assertExists()
        // P09 now activates this previously disabled local Search tab.
        rule.onNodeWithText("Search").assertIsEnabled()
        rule.onNodeWithText("Liked").assertIsNotEnabled()
    }

    @Test fun groupsCanBeEnteredAndBackReturnsToCategory() {
        mount(FakeBrowse())
        rule.onNodeWithText("Artists").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Synthetic Artist").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Synthetic One").assertExists()
        rule.onNodeWithText("Back").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Synthetic Artist").assertExists()
    }

    @Test fun songSelectionIsExplicitlyNotAPlayingClaim() {
        val repo = FakeBrowse()
        mount(repo)
        rule.onNodeWithText("Synthetic One").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Selected: Synthetic One · Not playing").assertExists()
        assertEquals(0, repo.scanCount)
    }

    @Test fun deniedMusicPermissionNeverRendersTrackOrSearchResults() {
        mount(FakeBrowse(), status = LibraryIndexStatus.Unavailable,
            permission = PermissionStatus.Revoked,
            readiness = FoundationReadiness.LibraryPermissionOrMediaUnavailable)
        rule.onNodeWithText("Review music access").assertExists()
        rule.onNodeWithText("Synthetic One").assertDoesNotExist()
    }
}
