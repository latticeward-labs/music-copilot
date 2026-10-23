package com.latticeward.musiccopilot.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Explicit Protocol Buffers wire schema for a versioned, non-authoritative P1 checkpoint.
 * Fields: 1 schemaVersion (int32), 2 queueRevision (int64), 3 items (repeated string),
 * 4 currentIndex (int32), 5 positionMs (int64), 6 capturedAtEpochMs (int64).
 * No title/artist/path/URI/audio/transcript fields. This is a Proto DataStore serializer
 * even without a generated .proto Kotlin class; wire format is canonical protobuf.
 */
object RestoreSnapshotPolicy {
    const val CURRENT_SCHEMA = 1
    const val MAX_ITEMS = 5_000
    fun empty() = RestorableQueueSnapshot(CURRENT_SCHEMA, 0, emptyList(), 0, 0, 0)

    fun isEmpty(snapshot: RestorableQueueSnapshot): Boolean =
        snapshot.items.isEmpty() && snapshot.positionMs == 0L &&
            snapshot.queueRevision == 0L

    fun validate(snapshot: RestorableQueueSnapshot): SnapshotValidity {
        if (snapshot.schemaVersion > CURRENT_SCHEMA) return SnapshotValidity.FutureSchema
        if (snapshot.schemaVersion != CURRENT_SCHEMA) return SnapshotValidity.Corrupt
        if (snapshot.items.size > MAX_ITEMS || snapshot.queueRevision < 0L ||
            snapshot.positionMs < 0L || snapshot.capturedAtEpochMs < 0L)
            return SnapshotValidity.Corrupt
        if (snapshot.items.isEmpty())
            return if (snapshot.currentIndex == 0 && snapshot.positionMs == 0L)
                SnapshotValidity.Valid else SnapshotValidity.Corrupt
        // Queue occurrences are positional: duplicate stable MediaStore IDs
        // are valid, but blank/oversized refs must fail closed.
        if (snapshot.currentIndex !in snapshot.items.indices ||
            snapshot.items.any {
                it.value.isBlank() || it.value.length > 128
            }) return SnapshotValidity.Corrupt
        return SnapshotValidity.Valid
    }
}

enum class SnapshotValidity { Valid, Corrupt, FutureSchema }

object RestoreProtoCodec : Serializer<RestorableQueueSnapshot> {
    override val defaultValue: RestorableQueueSnapshot = RestoreSnapshotPolicy.empty()

    override suspend fun readFrom(input: InputStream): RestorableQueueSnapshot {
        try {
            val coded = CodedInputStream.newInstance(input)
            var version = 0
            var revision = 0L
            val items = mutableListOf<MediaRef>()
            var index = 0
            var position = 0L
            var timestamp = 0L
            while (true) {
                val tag = coded.readTag()
                if (tag == 0) break
                when (WireFormat.getTagFieldNumber(tag)) {
                    1 -> version = coded.readInt32()
                    2 -> revision = coded.readInt64()
                    3 -> {
                        if (items.size >= RestoreSnapshotPolicy.MAX_ITEMS)
                            throw IOException("snapshot item count limit")
                        items += MediaRef(coded.readStringRequireUtf8())
                    }
                    4 -> index = coded.readInt32()
                    5 -> position = coded.readInt64()
                    6 -> timestamp = coded.readInt64()
                    else -> if (!coded.skipField(tag)) break
                }
            }
            val decoded = RestorableQueueSnapshot(
                version, revision, items, index, position, timestamp
            )
            // A future schema is surfaced as a typed error at Store.read rather than
            // silently substituting a restored queue from an incompatible format.
            if (RestoreSnapshotPolicy.validate(decoded) == SnapshotValidity.Corrupt)
                throw CorruptionException("invalid checkpoint structure")
            return decoded
        } catch (failure: IOException) {
            if (failure is CorruptionException) throw failure
            throw CorruptionException("invalid checkpoint protobuf", failure)
        } catch (failure: RuntimeException) {
            throw CorruptionException("invalid checkpoint content", failure)
        }
    }

    override suspend fun writeTo(t: RestorableQueueSnapshot, output: OutputStream) {
        require(RestoreSnapshotPolicy.validate(t) == SnapshotValidity.Valid)
        val coded = CodedOutputStream.newInstance(output)
        coded.writeInt32(1, t.schemaVersion)
        coded.writeInt64(2, t.queueRevision)
        t.items.forEach { coded.writeString(3, it.value) }
        coded.writeInt32(4, t.currentIndex)
        coded.writeInt64(5, t.positionMs)
        coded.writeInt64(6, t.capturedAtEpochMs)
        coded.flush()
    }
}
