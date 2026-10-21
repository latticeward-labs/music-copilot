package com.latticeward.musiccopilot.data.api

import java.text.Normalizer
import java.util.Locale

/**
 * Shared, Android-free P04 text normalization for Search, Browse and Room.
 * Never use a localized casing default: an artist must not move groups when
 * the phone locale changes. Display fields stay local and must not be logged.
 */
object LibraryTextNormalization {
    const val UNKNOWN_TITLE = "Unknown Title"
    const val MAX_METADATA_LENGTH = 2048

    private fun unsafeFormat(ch: Char): Boolean =
        ch == '\u200e' || ch == '\u200f' ||
            ch in '\u202a'..'\u202e' ||
            ch in '\u2066'..'\u2069' ||
            ch == '\ufeff'

    /**
     * Remove unsafe invisible formatting, fold all Unicode spacing to one
     * ASCII space, and normalize composed/compatibility characters (NFKC).
     * An entirely missing/unsupported field stays null, NOT a literal
     * "Unknown Artist" value: missing and user-authored labels differ.
     */
    fun display(value: String?): String? {
        if (value == null || value.length > MAX_METADATA_LENGTH) return null
        val canonical = Normalizer.normalize(value, Normalizer.Form.NFKC)
        val out = StringBuilder(canonical.length)
        var previousSpace = true
        for (ch in canonical) {
            if (unsafeFormat(ch)) continue
            if (ch.isWhitespace() || Character.isSpaceChar(ch) ||
                Character.isISOControl(ch)) {
                if (!previousSpace) {
                    out.append(' ')
                    previousSpace = true
                }
                continue
            }
            out.append(ch)
            previousSpace = false
        }
        val clean = out.toString().trim()
        if (clean.isEmpty() || clean.equals("<unknown>", ignoreCase = true) ||
            clean == "\ufffd") return null
        return clean
    }

    /** A deterministic search/group key; punctuation and accents are retained. */
    fun key(value: String?): String = display(value)?.lowercase(Locale.ROOT).orEmpty()

    /** Display fallback is NOT used to create a real artist/album identity. */
    fun title(value: String?): String = display(value) ?: UNKNOWN_TITLE

    fun durationMs(value: Long?): Long? = value?.takeIf { it > 0L }

    /**
     * Pure validation: Android MediaStore RELATIVE_PATH, never an absolute path.
     * Preserve directory case for display, but use key() for matching/grouping.
     */
    fun relativeFolder(value: String?): String? {
        if (value == null || value.length > MAX_METADATA_LENGTH) return null
        val raw = value.trim()
        if (raw.startsWith('/') || raw.startsWith('\\')) return null
        val canonical = raw.replace('\\', '/').trimEnd('/')
        if (canonical.isEmpty()) return null
        val segments = canonical.split('/')
        if (segments.any { part ->
                part.isBlank() || part == "." || part == ".." ||
                    part.any { Character.isISOControl(it) || unsafeFormat(it) }
            }) return null
        return segments.joinToString("/") + "/"
    }

    fun folderLabel(value: String?): String =
        relativeFolder(value)?.trimEnd('/')?.substringAfterLast('/')
            ?: LibraryGroupIdentity.UNKNOWN_FOLDER
}
