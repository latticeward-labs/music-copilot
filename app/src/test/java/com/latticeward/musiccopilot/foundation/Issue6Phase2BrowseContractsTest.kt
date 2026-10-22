package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowseOrdering
import com.latticeward.musiccopilot.data.api.LibraryBrowsePage
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
import com.latticeward.musiccopilot.data.repository.FoundationLibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Authored in Issue #6 P02; real JVM suite execution/evidence is P12 T01/T03.
 * Use synthetic metadata only. No human/real media fixture is recorded here.
 */
class Issue6Phase2BrowseContractsTest {
    @Test fun distinctNormalizedArtistsGroupTogetherWithoutStoringTheirNamesInId() {
        val one = LibraryGroupIdentity.artistId("  Synthetic  ARTIST ")
        val two = LibraryGroupIdentity.artistId("synthetic artist")
        assertEquals(one, two)
        assertTrue(one.value.startsWith("artist:"))
        assertFalse(one.value.contains("synthetic"))
        assertNotEquals(one, LibraryGroupIdentity.artistId(null))
        assertNotEquals(LibraryGroupIdentity.artistId("Unknown Artist"),
            LibraryGroupIdentity.artistId(null))
    }

    @Test fun albumPlatformIdentityWinsAndFallbackDisambiguatesArtists() {
        val platform = LibraryGroupIdentity.albumId(
            "Synthetic Album", "Artist A", "external_primary", 55L)
        val samePlatform = LibraryGroupIdentity.albumId(
            "Synthetic Album", "Artist B", "external_primary", 55L)
        assertEquals(platform, samePlatform)
        assertNotEquals(platform, LibraryGroupIdentity.albumId(
            "Synthetic Album", "Artist A", "secondary", 55L))
        assertNotEquals(LibraryGroupIdentity.albumId(
            "Shared Album", "Artist A", null), LibraryGroupIdentity.albumId(
            "Shared Album", "Artist B", null))
        assertNotEquals(LibraryGroupIdentity.albumId(
            null, "Artist A", null), LibraryGroupIdentity.albumId(
            "Unknown Album", "Artist A", null))
    }

    @Test fun folderIdentityUsesVolumeAndFullRelativePathWhileUiShowsOnlyBasename() {
        val first = LibraryGroupIdentity.folderId("external_primary", "Music/Rock/")
        val second = LibraryGroupIdentity.folderId("external_primary", "Podcasts/Rock/")
        val third = LibraryGroupIdentity.folderId("secondary", "Music/Rock/")
        assertNotEquals(first, second)
        assertNotEquals(first, third)
        assertEquals("Rock", LibraryGroupIdentity.folderLabel("Music/Rock/"))
        assertEquals("Rock", LibraryGroupIdentity.folderLabel("Music\\Rock\\"))
        assertFalse(first.value.contains("Music"))
        assertFalse(first.value.contains("Rock"))
        assertEquals(LibraryGroupIdentity.UNKNOWN_FOLDER,
            LibraryGroupIdentity.folderLabel("../Music/"))
        assertEquals(LibraryGroupIdentity.UNKNOWN_FOLDER,
            LibraryGroupIdentity.folderLabel(null))
    }

    @Test fun labelsAndNullNormalizationAreDistinct() {
        assertNull(LibraryGroupIdentity.normalize(null))
        assertNull(LibraryGroupIdentity.normalize("   "))
        assertEquals("synthetic artist",
            LibraryGroupIdentity.normalize(" Synthetic   Artist "))
        assertEquals(LibraryGroupIdentity.UNKNOWN_ARTIST, "Unknown Artist")
        assertEquals(LibraryGroupIdentity.UNKNOWN_ALBUM, "Unknown Album")
    }

    @Test fun pagesRejectInvalidBoundsAndGroupMisuse() {
        assertTrue(runCatching { LibraryPageRequest(offset = -1) }.isFailure)
        assertTrue(runCatching { LibraryPageRequest(limit = 0) }.isFailure)
        assertTrue(runCatching { LibraryPageRequest(limit = 101) }.isFailure)
        assertTrue(runCatching { LibraryPageRequest(expectedRevision = -1) }.isFailure)
        assertTrue(runCatching { LibraryBrowseRequest(
            LibraryBrowseCategory.Songs, groupId = LibraryGroupId("artist:abc")) }.isFailure)
        assertEquals(50, LibraryPageRequest().limit)
        assertEquals(100, LibraryPageRequest.MAX_PAGE_SIZE)
    }

    @Test fun fourBrowseProjectionsAreExplicitAndReturnTypedEntries() {
        assertEquals(4, LibraryBrowseCategory.entries.size)
        assertTrue(LibraryBrowseEntry.Song(synthetic("audio:1", "Alpha")) is LibraryBrowseEntry)
        assertTrue(LibraryBrowseEntry.Artist(LibraryGroupId("artist:a"), "A", 1)
            is LibraryBrowseEntry)
        assertTrue(LibraryBrowseEntry.Album(LibraryGroupId("album:a"), "A", null, 1)
            is LibraryBrowseEntry)
        assertTrue(LibraryBrowseEntry.Folder(LibraryGroupId("folder:a"), "A", 1)
            is LibraryBrowseEntry)
        assertTrue(runCatching { LibraryBrowsePage(emptyList(), 0, -1, null) }.isFailure)
        assertTrue(runCatching { LibraryBrowsePage(emptyList(), 0, 0, 1) }.isFailure)
    }

    @Test fun stableSongOrderingDoesNotCollapseDuplicateTitles() {
        val rows = listOf(synthetic("audio:9", "Beta"),
            synthetic("audio:2", "Alpha"), synthetic("audio:1", "ALPHA"))
        assertEquals(listOf("audio:1", "audio:2", "audio:9"),
            LibraryBrowseOrdering.songs(rows).map { it.ref.value })
    }

    @Test fun preP06BrowsePortFailsClosedInsteadOfClaimingEmptyReady() = runBlocking {
        val repo = FoundationLibraryRepository(CoroutineScope(SupervisorJob()))
        val result = repo.browse(LibraryBrowseRequest(LibraryBrowseCategory.Folders))
        assertTrue(result is AppResult.Failure)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            (result as AppResult.Failure).error.code)
    }

    private fun synthetic(ref: String, title: String): LibraryItem =
        LibraryItem(MediaRef(ref), title, "Synthetic", "Fixture", 1000L)
}
