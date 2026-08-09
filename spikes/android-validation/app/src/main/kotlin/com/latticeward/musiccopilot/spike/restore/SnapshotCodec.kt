package com.latticeward.musiccopilot.spike.restore

import java.nio.charset.StandardCharsets
import java.util.Base64

/** Strict, versioned wire format used only by the technical-spike harness. */
object SnapshotCodec {
    const val CURRENT_SCHEMA_VERSION = 2
    private const val LEGACY_SCHEMA_VERSION = 1

    fun encode(snapshot: PausedSnapshot): ByteArray {
        require(snapshot.schemaVersion == CURRENT_SCHEMA_VERSION)
        require(snapshot.queueRevision >= 0)
        require(snapshot.queueReferences.isNotEmpty())
        require(snapshot.currentIndex in snapshot.queueReferences.indices)
        require(snapshot.positionMs >= 0)
        require(snapshot.savedAtEpochMs >= 0)

        val encoder = Base64.getUrlEncoder().withoutPadding()
        return buildString {
            appendLine("schema=${snapshot.schemaVersion}")
            appendLine("revision=${snapshot.queueRevision}")
            appendLine("index=${snapshot.currentIndex}")
            appendLine("position=${snapshot.positionMs}")
            appendLine("captured=${snapshot.capturedState}")
            appendLine("savedAt=${snapshot.savedAtEpochMs}")
            snapshot.queueReferences.forEach { reference ->
                appendLine("item=${encoder.encodeToString(reference.toByteArray(StandardCharsets.UTF_8))}")
            }
        }.toByteArray(StandardCharsets.UTF_8)
    }

    fun decode(bytes: ByteArray): SnapshotDecodeResult {
        val values = mutableMapOf<String, String>()
        val items = mutableListOf<String>()
        val decoder = Base64.getUrlDecoder()

        return runCatching {
            bytes.toString(StandardCharsets.UTF_8).lineSequence()
                .filter { it.isNotBlank() }
                .forEach { line ->
                    val separator = line.indexOf('=')
                    require(separator > 0) { "malformed-line" }
                    val key = line.substring(0, separator)
                    val value = line.substring(separator + 1)
                    if (key == "item") {
                        items += decoder.decode(value).toString(StandardCharsets.UTF_8)
                    } else {
                        require(values.put(key, value) == null) { "duplicate-$key" }
                    }
                }

            val version = values.requiredInt("schema")
            require(version == CURRENT_SCHEMA_VERSION || version == LEGACY_SCHEMA_VERSION) {
                "unsupported-schema"
            }
            val revision = values.requiredLong("revision")
            val index = values.requiredInt("index")
            val position = values.requiredLong("position")
            val captured = values.required("captured")
            val savedAt = values.requiredLong("savedAt")
            require(revision >= 0) { "negative-revision" }
            require(items.isNotEmpty()) { "empty-queue" }
            require(index in items.indices) { "invalid-index" }
            require(position >= 0) { "negative-position" }
            require(savedAt >= 0) { "negative-savedAt" }

            SnapshotDecodeResult.Valid(
                PausedSnapshot(
                    schemaVersion = CURRENT_SCHEMA_VERSION,
                    queueRevision = revision,
                    queueReferences = items,
                    currentIndex = index,
                    positionMs = position,
                    capturedState = captured,
                    savedAtEpochMs = savedAt,
                ),
            )
        }.getOrElse { error ->
            val controlledReason = error.message
                ?.takeIf { it.length <= 40 && it.matches(Regex("[a-zA-Z0-9-]+")) }
                ?: "decode-failed"
            SnapshotDecodeResult.Invalid(controlledReason)
        }
    }

    private fun Map<String, String>.required(key: String): String =
        get(key) ?: error("missing-$key")

    private fun Map<String, String>.requiredInt(key: String): Int =
        required(key).toIntOrNull() ?: error("invalid-$key")

    private fun Map<String, String>.requiredLong(key: String): Long =
        required(key).toLongOrNull() ?: error("invalid-$key")
}
