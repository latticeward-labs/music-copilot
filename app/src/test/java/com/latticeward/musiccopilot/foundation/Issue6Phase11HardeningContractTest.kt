package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowsePage
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryPageRequest
import com.latticeward.musiccopilot.data.api.LibrarySearchResults
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.data.mediastore.MediaStoreAudioPolicy
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.feature.library.SelectedSongFailurePolicy
import com.latticeward.musiccopilot.feature.library.SelectedSongUiStatus
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import org.junit.Assert.*
import org.junit.Test

/** P11 pure Kotlin fixtures. AUTHORED ONLY; execute in final P12 JVM batch. */
class Issue6Phase11HardeningContractTest {
    private fun audio(volume: String, id: Long, title: String) = ScannedAudio(
        ref = MediaStoreRefCodec.encode(volume, id),
        title = title,
        artist = "Synthetic Artist",
        album = "Synthetic Album",
        durationMs = 120000L,
        volumeName = volume,
        relativeFolderPath = "Music/Fixture/",
    )

    @Test fun equalNumericIdOnTwoVolumesMustNotCollide() {
        val one = audio("external_primary", 7, "Synthetic One")
        val two = audio("external_123", 7, "Synthetic Two")
        assertNotEquals(one.ref, two.ref)
        assertEquals("external_primary", MediaStoreRefCodec.decodeScoped(one.ref)?.volumeName)
        assertEquals("external_123", MediaStoreRefCodec.decodeScoped(two.ref)?.volumeName)
        assertEquals(2, LibraryRowMapper.deriveIndexRows(listOf(one,two)).size)
    }

    @Test fun staleOrMalformedScopedReferencesFailClosed() {
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef("audio:v:external:7")))
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef("audio:v:../../raw:7")))
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef("audio:v:external_primary:-2")))
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef("audio:v:external_primary:9223372036854775808")))
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef("content://unsafe/path")))
    }

    @Test fun unknownArtistAndLiteralUnknownArtistMustKeepDistinctGroups() {
        val missing = LibraryGroupIdentity.artistId(null)
        val literal = LibraryGroupIdentity.artistId("Unknown Artist")
        assertNotEquals(missing, literal)
    }

    @Test fun nestedRelativeFolderTraversalIsNotAccepted() {
        assertNull(MediaStoreAudioPolicy.safeRelativeFolder("../Music/"))
        assertNull(MediaStoreAudioPolicy.safeRelativeFolder("/storage/emulated/0/"))
        assertEquals("Music/Fixture/", MediaStoreAudioPolicy.safeRelativeFolder("Music/Fixture/"))
    }

    @Test fun duplicateProviderReferenceKeepsExactlyOneIndexedSong() {
        val a = audio("external_primary", 9, "Synthetic")
        val b = a.copy(artist = null)
        val rows = LibraryRowMapper.deriveIndexRows(listOf(a,b))
        assertEquals(1,rows.size)
        assertEquals("Synthetic Artist",rows.single().artist)
    }

    @Test fun equalTitlesWithDifferentRefsRemainDistinct() {
        val a = audio("external_primary", 7, "One Title")
        val b = audio("external_primary", 8, "One Title")
        assertEquals(2, LibraryRowMapper.deriveIndexRows(listOf(a,b)).size)
    }

    @Test fun thousandItemFixtureNeverBypassesPageRequestLimit() {
        assertEquals(50, LibraryPageRequest.DEFAULT_PAGE_SIZE)
        assertEquals(100, LibraryPageRequest.MAX_PAGE_SIZE)
        try {
            LibraryPageRequest(offset = 0, limit = 101)
            fail("Must reject unbounded page")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun aPageCannotPretendToContainMoreThanMaximum() {
        val entries = (1..101).map { i ->
            LibraryBrowseEntry.Song(
                com.latticeward.musiccopilot.data.api.LibraryItem(
                    MediaRef("audio:$i"), "Synthetic $i", null, null, 1000L
                )
            )
        }
        try {
            LibraryBrowsePage(entries, 101, 9L, null)
            fail("Too-large page must throw")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun searchResultContractCannotReturnMoreThanLimit() {
        val songs = (1..3).map { i ->
            LibraryBrowseEntry.Song(
                com.latticeward.musiccopilot.data.api.LibraryItem(
                    MediaRef("audio:$i"), "Synthetic $i", null, null, 1000L
                )
            )
        }
        try {
            LibrarySearchResults(songs, revision = 3L, limit = 2)
            fail("Search cap must be enforced")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun normalizedSearchIsLocaleIndependentForUnicodeInput() {
        assertEquals("aurora", LibraryTextNormalization.key(" AURORA "))
        assertEquals(LibraryTextNormalization.key("Ｓｏｎｇ"),
            LibraryTextNormalization.key("Song"))
    }

    @Test fun libraryStaleStatusIsNotMisreportedAsPlaying() {
        val error = AppError.Library(
            AppErrorCodes.LIBRARY_BROWSE_STALE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry
        )
        assertEquals(SelectedSongUiStatus.LibraryChanged,
            SelectedSongFailurePolicy.from(error))
    }

    @Test fun permissionRevocationHasExplicitRecoveryStatus() {
        val error = AppError.Permission(
            AppErrorCodes.PERMISSION_REVOKED,
            Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings
        )
        assertEquals(SelectedSongUiStatus.PermissionRequired,
            SelectedSongFailurePolicy.from(error))
    }
}
