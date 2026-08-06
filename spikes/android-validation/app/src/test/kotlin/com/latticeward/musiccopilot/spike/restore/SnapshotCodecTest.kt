package com.latticeward.musiccopilot.spike.restore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapshotCodecTest {
    private val valid = PausedSnapshot(
        schemaVersion = SnapshotCodec.CURRENT_SCHEMA_VERSION,
        queueRevision = 42,
        queueReferences = listOf("content://synthetic/1", "content://synthetic/2"),
        currentIndex = 1,
        positionMs = 12_345,
        capturedState = "PLAYING",
        savedAtEpochMs = 100,
    )

    @Test
    fun roundTripPreservesLogicalContract() {
        val result = SnapshotCodec.decode(SnapshotCodec.encode(valid))

        assertTrue(result is SnapshotDecodeResult.Valid)
        assertEquals(valid, (result as SnapshotDecodeResult.Valid).snapshot)
    }

    @Test
    fun unsupportedFutureSchemaFailsClosed() {
        val encoded = SnapshotCodec.encode(valid).decodeToString()
            .replace("schema=2", "schema=99")

        val result = SnapshotCodec.decode(encoded.encodeToByteArray())

        assertTrue(result is SnapshotDecodeResult.Invalid)
    }

    @Test
    fun negativePositionFailsClosed() {
        val encoded = SnapshotCodec.encode(valid).decodeToString()
            .replace("position=12345", "position=-1")

        val result = SnapshotCodec.decode(encoded.encodeToByteArray())

        assertTrue(result is SnapshotDecodeResult.Invalid)
    }

    @Test
    fun legacyVersionMigratesDeterministicallyAndIdempotently() {
        val legacy = SnapshotCodec.encode(valid).decodeToString()
            .replace("schema=2", "schema=1")

        val migrated = SnapshotCodec.decode(legacy.encodeToByteArray())

        assertTrue(migrated is SnapshotDecodeResult.Valid)
        val snapshot = (migrated as SnapshotDecodeResult.Valid).snapshot
        assertEquals(SnapshotCodec.CURRENT_SCHEMA_VERSION, snapshot.schemaVersion)
        val encodedOnce = SnapshotCodec.encode(snapshot)
        val decodedAgain = SnapshotCodec.decode(encodedOnce) as SnapshotDecodeResult.Valid
        assertEquals(encodedOnce.toList(), SnapshotCodec.encode(decodedAgain.snapshot).toList())
    }

    @Test
    fun tornSnapshotFailsClosed() {
        val encoded = SnapshotCodec.encode(valid)
        val result = SnapshotCodec.decode(encoded.copyOf(encoded.size / 2))

        assertTrue(result is SnapshotDecodeResult.Invalid)
    }

    @Test
    fun duplicateScalarFailsClosed() {
        val encoded = SnapshotCodec.encode(valid).decodeToString() + "schema=2\n"

        val result = SnapshotCodec.decode(encoded.encodeToByteArray())

        assertTrue(result is SnapshotDecodeResult.Invalid)
    }
}
