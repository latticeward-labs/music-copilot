package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.mediastore.MediaStoreAudioPolicy
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.mediastore.ScopedAudioId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Synthetic, metadata-free P03 contract fixtures. Tests are AUTHORED here;
 * actual T01/T05 execution, Android 29/31/33/35 provider + OEM evidence
 * remains explicitly scheduled for the consolidated P12 gate.
 */
class Issue6Phase3MediaStoreContractTest {

    @Test fun oldPrimaryMediaRefsRemainDecodable() {
        val old = MediaStoreRefCodec.encode(123L)
        assertEquals("audio:123", old.value)
        assertEquals(123L, MediaStoreRefCodec.decode(old))
        assertEquals(ScopedAudioId(null, 123L), MediaStoreRefCodec.decodeScoped(old))
    }

    @Test fun twoVolumesWithTheSameNumericIdNeverCollide() {
        val primary = MediaStoreRefCodec.encode("external_primary", 123L)
        val removable = MediaStoreRefCodec.encode("ABCD-1234", 123L)
        assertNotEquals(primary, removable)
        assertEquals(123L, MediaStoreRefCodec.decode(primary))
        assertEquals(ScopedAudioId("external_primary", 123L),
            MediaStoreRefCodec.decodeScoped(primary))
        assertEquals(ScopedAudioId("ABCD-1234", 123L),
            MediaStoreRefCodec.decodeScoped(removable))
    }

    @Test fun decodingRejectsMalformedVolumesAndOpaqueUriOrPath() {
        val bad = listOf(
            "audio:v:external:1",
            "audio:v:internal:1",
            "audio:v:../other:1",
            "audio:v:secondary/other:1",
            "audio:v:invalid name:1",
            "audio:v::1",
            "audio:v:external_primary:-1",
            "audio:v:external_primary:ab",
            "audio:v:external_primary:1:2",
            "audio:v:external_primary:",
            "audio:bad",
            "audio:-1",
            "audio:",
            "audio:1/2",
            "content://media/external/audio/media/1",
            "/storage/emulated/0/Music/a.mp3",
        )
        assertTrue(bad.all { MediaStoreRefCodec.decodeScoped(MediaRef(it)) == null })
    }

    @Test fun oversizedLongAndUnsupportedVolumeAreRejected() {
        assertNull(MediaStoreRefCodec.decodeScoped(MediaRef(
            "audio:v:external_primary:99999999999999999999999")))
        assertTrue(runCatching { MediaStoreRefCodec.encode("external", 1L) }.isFailure)
        assertTrue(runCatching { MediaStoreRefCodec.encode("../x", 1L) }.isFailure)
        assertTrue(runCatching { MediaStoreRefCodec.encode("external_primary", -1L) }.isFailure)
        assertTrue(MediaStoreRefCodec.isSpecificVolume("0123-ABCD"))
    }

    @Test fun scanningDtoPreservesExistingFiveParameterConstructors() {
        val legacy = ScannedAudio(MediaStoreRefCodec.encode(4), "Synthetic track",
            null, null, 1000L)
        assertNull(legacy.volumeName)
        assertNull(legacy.relativeFolderPath)
        assertNull(legacy.platformAlbumId)
        assertNull(legacy.generationModified)
    }

    @Test fun fullMetadataIsCarriedWithoutMakingRoomAnAuthority() {
        val media = ScannedAudio(
            ref = MediaStoreRefCodec.encode("external_primary", 5L),
            title = "Synthetic Track", artist = "Synthetic Artist", album = "Synthetic Album",
            durationMs = 1234L, volumeName = "external_primary",
            relativeFolderPath = "Music/Synthetic/", platformAlbumId = 8L,
            mimeType = "audio/flac", sizeBytes = 8192L, dateModifiedSeconds = 5L,
            generationModified = 3L,
        )
        assertEquals("Music/Synthetic/", media.relativeFolderPath)
        assertEquals(8L, media.platformAlbumId)
        assertEquals("external_primary", media.volumeName)
        assertEquals(3L, media.generationModified)
    }

    @Test fun invalidFolderPathsAreNotPassedThrough() {
        val values = listOf(
            "/storage/emulated/0/Music/",
            "\\storage\\Music\\",
            "../Music/",
            "Music/../Other/",
            "Music//Other/",
            "Music/./Other/",
            "Music/\u0000Private/",
            "",
        )
        assertTrue(values.all { MediaStoreAudioPolicy.safeRelativeFolder(it) == null })
        assertEquals("Music/Synthetic/",
            MediaStoreAudioPolicy.safeRelativeFolder(" Music\\Synthetic\\ "))
        assertEquals("Music/Albums/", MediaStoreAudioPolicy.safeRelativeFolder("Music/Albums"))
    }

    @Test fun optionalAndRequiredMetadataHaveDistinctRules() {
        assertNull(MediaStoreAudioPolicy.usableTitle(null))
        assertNull(MediaStoreAudioPolicy.usableTitle(" "))
        assertNull(MediaStoreAudioPolicy.usableTitle("A\u0000B"))
        assertEquals("Synthetic Song", MediaStoreAudioPolicy.usableTitle(" Synthetic Song "))
        assertNull(MediaStoreAudioPolicy.optionalMetadata(" "))
        assertNull(MediaStoreAudioPolicy.optionalMetadata(null))
        assertEquals("Synthetic Artist",
            MediaStoreAudioPolicy.optionalMetadata(" Synthetic Artist "))
    }

    @Test fun mimeAndDurationPolicyNeverAssumesArbitraryFilesAreMusic() {
        assertTrue(MediaStoreAudioPolicy.acceptableMime("audio/flac"))
        assertTrue(MediaStoreAudioPolicy.acceptableMime("AUDIO/OGG"))
        assertTrue(MediaStoreAudioPolicy.acceptableMime(null))
        assertFalse(MediaStoreAudioPolicy.acceptableMime("video/mp4"))
        assertFalse(MediaStoreAudioPolicy.acceptableMime("application/pdf"))
        assertNull(MediaStoreAudioPolicy.optionalPositive(null))
        assertNull(MediaStoreAudioPolicy.optionalPositive(0L))
        assertNull(MediaStoreAudioPolicy.optionalPositive(-1L))
        assertEquals(5L, MediaStoreAudioPolicy.optionalPositive(5L))
    }
}
