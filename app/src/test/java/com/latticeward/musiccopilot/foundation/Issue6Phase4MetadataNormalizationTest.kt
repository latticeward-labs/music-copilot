package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Synthetic-only P04 fixtures; authored now, executed in consolidated P12. */
class Issue6Phase4MetadataNormalizationTest {
    private fun song(
        id: Long,
        title: String = "Synthetic Song",
        artist: String? = "Synthetic Artist",
        album: String? = "Synthetic Album",
        duration: Long? = 5000L,
        volume: String = "external_primary",
        folder: String? = "Music/Synthetic/",
        albumId: Long? = 9L,
    ) = ScannedAudio(
        ref = MediaStoreRefCodec.encode(volume, id),
        title = title, artist = artist, album = album, durationMs = duration,
        volumeName = volume, relativeFolderPath = folder, platformAlbumId = albumId,
    )

    @Test fun unicodeCompatibilityAndAllSpaceKindsAreDeterministic() {
        assertEquals("café", LibraryRowMapper.normalize("  CAFE\u0301  "))
        assertEquals("synthetic artist",
            LibraryRowMapper.normalize("\u00a0SYNTHETIC\t  ARTIST\u2003"))
        assertEquals("a b", LibraryRowMapper.normalize(" A\n\tB "))
        assertEquals("a b", LibraryRowMapper.normalize("A\u202e B"))
    }

    @Test fun deviceLocaleDoesNotChangeIndexKeysOrBrowseGrouping() {
        val before = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("i", LibraryRowMapper.normalize("I"))
            assertEquals(LibraryGroupIdentity.artistId("ARTIST"),
                LibraryGroupIdentity.artistId("artist"))
        } finally {
            Locale.setDefault(before)
        }
    }

    @Test fun unknownProviderStringsStayMissingRatherThanUserKnownLabels() {
        val metadata = LibraryRowMapper.normalizeMetadata(song(
            1, artist = "<unknown>", album = " <UNKNOWN> "))
        assertNull(metadata.artist)
        assertNull(metadata.album)
        assertEquals("", metadata.normalizedArtist)
        assertEquals("", metadata.normalizedAlbum)
        assertNotEquals(metadata.artistGroupId,
            LibraryGroupIdentity.artistId("Unknown Artist"))
        assertNotEquals(metadata.albumGroupId,
            LibraryGroupIdentity.albumId("Unknown Album", null, "external_primary", 9L))
    }

    @Test fun literalUnknownArtistIsNotEquivalentToMissingArtist() {
        val nullArtist = LibraryRowMapper.normalizeMetadata(song(1, artist = null))
        val literal = LibraryRowMapper.normalizeMetadata(song(1, artist = "Unknown Artist"))
        assertNull(nullArtist.artist)
        assertEquals("Unknown Artist", literal.artist)
        assertNotEquals(nullArtist.artistGroupId, literal.artistGroupId)
    }

    @Test fun emptyAndUnknownTitlesYieldSafeNonSensitiveFallback() {
        val empty = LibraryRowMapper.fromSource(song(1, title = " "))
        val unsupported = LibraryRowMapper.fromSource(song(2, title = "<unknown>"))
        assertEquals(LibraryTextNormalization.UNKNOWN_TITLE, empty.title)
        assertEquals("unknown title", empty.normalizedTitle)
        assertEquals(LibraryTextNormalization.UNKNOWN_TITLE, unsupported.title)
        assertEquals("unknown title", unsupported.normalizedTitle)
    }

    @Test fun malformedAndOversizedMetadataAreSanitizedSafely() {
        val oversized = "X".repeat(LibraryTextNormalization.MAX_METADATA_LENGTH + 1)
        val metadata = LibraryRowMapper.normalizeMetadata(song(
            3, title = "  Synthetic\u0000 Track ", artist = oversized,
            album = "\u200eSynthetic\u202e Album"))
        assertEquals("Synthetic Track", metadata.title)
        assertNull(metadata.artist)
        assertEquals("Synthetic Album", metadata.album)
        assertEquals(5000L, metadata.durationMs)
        assertFalse(metadata.normalizedTitle.contains('\u0000'))
        assertFalse(metadata.normalizedAlbum.contains('\u202e'))
    }

    @Test fun zeroNegativeAndUnavailableDurationsStayNull() {
        for (duration in listOf<Long?>(null, 0L, -200L)) {
            val row = LibraryRowMapper.fromSource(song(4, duration = duration))
            assertNull(row.durationMs)
        }
        assertEquals(3000L, LibraryRowMapper.fromSource(song(4, duration = 3000L)).durationMs)
    }

    @Test fun foldersPreserveFullRelativePathButDoNotExposeAbsoluteTraversal() {
        val good = LibraryRowMapper.normalizeMetadata(song(
            5, folder = "Music/Synthetic/Rock/"))
        val other = LibraryRowMapper.normalizeMetadata(song(
            6, folder = "Podcasts/Synthetic/Rock/"))
        assertEquals("Rock", good.folderLabel)
        assertEquals("Music/Synthetic/Rock/", good.relativeFolderPath)
        assertNotEquals(good.folderGroupId, other.folderGroupId)
        val invalid = LibraryRowMapper.normalizeMetadata(song(
            7, folder = "Music/../Hidden/"))
        assertNull(invalid.relativeFolderPath)
        assertEquals(LibraryGroupIdentity.UNKNOWN_FOLDER, invalid.folderLabel)
        assertNotEquals(good.folderGroupId, invalid.folderGroupId)
    }

    @Test fun albumGroupsAreVolumeScopedAndHonorPlatformAlbumIdentity() {
        val a = LibraryRowMapper.normalizeMetadata(song(5, volume = "external_primary",
            artist = "One", albumId = 55L))
        val b = LibraryRowMapper.normalizeMetadata(song(6, volume = "external_primary",
            artist = "Two", albumId = 55L))
        val c = LibraryRowMapper.normalizeMetadata(song(7, volume = "ABCD-1234",
            artist = "One", albumId = 55L))
        assertEquals(a.albumGroupId, b.albumGroupId)
        assertNotEquals(a.albumGroupId, c.albumGroupId)
    }

    @Test fun identicalSongTitlesNeverMergeDistinctSourceRefs() {
        val rows = LibraryRowMapper.deriveIndexRows(listOf(
            song(1, title = "Same Title"), song(2, title = "Same Title")))
        assertEquals(2, rows.size)
        assertNotEquals(rows[0].mediaRef, rows[1].mediaRef)
    }

    @Test fun oneRefWithMultipleMetadataVersionsHasDeterministicWinner() {
        val minimal = song(8, title = "<unknown>", artist = null,
            album = null, duration = null)
        val good = song(8, title = "Synthetic Alpha", artist = "Band",
            album = "Album", duration = 2500)
        val rowsForward = LibraryRowMapper.deriveIndexRows(listOf(minimal, good))
        val rowsBackwards = LibraryRowMapper.deriveIndexRows(listOf(good, minimal))
        assertEquals(rowsForward, rowsBackwards)
        assertEquals(1, rowsForward.size)
        assertEquals("Synthetic Alpha", rowsForward.single().title)
        assertEquals("Band", rowsForward.single().artist)
    }

    @Test fun allScanOrderPermutationsKeepStableSortedRows() {
        val alpha = song(1, title = "Alpha")
        val beta = song(2, title = "beta")
        val duplicate = song(1, title = "ALPHA", artist = null, album = null)
        val baseline = LibraryRowMapper.deriveIndexRows(listOf(alpha, beta, duplicate))
        val shuffled = LibraryRowMapper.deriveIndexRows(listOf(duplicate, beta, alpha))
        assertEquals(baseline, shuffled)
        assertEquals(listOf(alpha.ref.value, beta.ref.value), baseline.map { it.mediaRef })
    }

    @Test fun malformedRefIsSkippedWithoutInventingMedia() {
        val valid = song(9)
        val bad = valid.copy(ref = com.latticeward.musiccopilot.core.model.MediaRef(
            "content://not-an-opaque-reference"))
        val rows = LibraryRowMapper.deriveIndexRows(listOf(valid, bad))
        assertEquals(1, rows.size)
        assertEquals(valid.ref.value, rows.single().mediaRef)
    }

    @Test fun afterMappingToRoomV1PublicLibraryItemsRemainCompatible() {
        val media = song(10, title = "  Synthetic   Track ",
            artist = "   Artist  ", album = "  An Album ")
        val row = LibraryRowMapper.fromSource(media)
        val item = LibraryRowMapper.toItem(row)
        assertEquals("Synthetic Track", item.title)
        assertEquals("Artist", item.artist)
        assertEquals("An Album", item.album)
        assertEquals(media.ref, item.ref)
        assertTrue(row.normalizedTitle.isNotBlank())
    }

    @Test fun folderNameWithMultipleSpacesIsNotSilentlyChangedToAnotherFolder() {
        val original = "Music/Live  Session/"
        val metadata = LibraryRowMapper.normalizeMetadata(song(12, folder = original))
        assertEquals(original, metadata.relativeFolderPath)
        assertEquals("Live  Session", metadata.folderLabel)
    }
}
