package com.latticeward.musiccopilot.data.mediastore

import com.latticeward.musiccopilot.core.model.MediaRef

/**
 * Backward-compatible local reference codec.
 *
 * Old v1: audio:<id> => PRIMARY volume only (never an ambiguous all-volume id).
 * New:    audio:v:<specific-volume>:<id>
 *
 * Specific MediaStore volume names are verified; only decimal non-negative IDs
 * are accepted. Never construct filesystem paths from media references.
 */
data class ScopedAudioId(val volumeName: String?, val id: Long)

object MediaStoreRefCodec {
    private val validVolume = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")

    fun isSpecificVolume(volumeName: String): Boolean =
        validVolume.matches(volumeName) &&
            volumeName != "external" && volumeName != "internal"

    /** For older tests/Room v1 keys; new scanner MUST use scoped encode. */
    fun encode(id: Long): MediaRef {
        require(id >= 0L)
        return MediaRef("audio:$id")
    }

    fun encode(volumeName: String, id: Long): MediaRef {
        require(isSpecificVolume(volumeName))
        require(id >= 0L)
        return MediaRef("audio:v:$volumeName:$id")
    }

    /** Preserves the pre-P03 numeric decode helper for existing consumers. */
    fun decode(ref: MediaRef): Long? = decodeScoped(ref)?.id

    fun decodeScoped(ref: MediaRef): ScopedAudioId? {
        val raw = ref.value
        if (raw.startsWith("audio:v:")) {
            val segments = raw.removePrefix("audio:v:").split(':')
            if (segments.size != 2 || !isSpecificVolume(segments[0])) return null
            val id = parseId(segments[1]) ?: return null
            return ScopedAudioId(segments[0], id)
        }
        if (!raw.startsWith("audio:")) return null
        val id = parseId(raw.removePrefix("audio:")) ?: return null
        return ScopedAudioId(null, id)
    }

    private fun parseId(text: String): Long? =
        text.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
            ?.toLongOrNull()
            ?.takeIf { it >= 0L }
}
