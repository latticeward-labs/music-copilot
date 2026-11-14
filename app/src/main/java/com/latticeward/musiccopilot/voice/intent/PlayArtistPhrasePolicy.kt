package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import java.text.Normalizer
import java.util.Locale

/**
 * Issue #11 P02. Deterministic, bounded grammar and artist-slot extraction.
 *
 * This is an INERT source-only parse contract: no library lookup, confidence
 * acceptance, playback dispatch, or confirmation occurs here. P03-P07 own
 * those steps. The five Issue #10 transport phrases remain a separate bank.
 */
sealed interface PlayArtistPhraseResult {
    /**
     * Slot is ephemeral private user/media data. Never log, persist, report
     * through telemetry, or use as an exception/error-code string.
     */
    class Recognized(val slot: PlayArtistSlot) : PlayArtistPhraseResult {
        override fun toString(): String = "PlayArtistPhraseResult.Recognized(redacted)"
    }

    /** Not the one approved artist-command phrase. Do not guess a synonym. */
    data object NotArtistPhrase : PlayArtistPhraseResult

    /** Understood request for an out-of-scope feature/compound command. */
    data object Unsupported : PlayArtistPhraseResult

    /** Known command prefix but an unusable slot or unsafe input. */
    class Malformed(val reason: PlayArtistPhraseError) : PlayArtistPhraseResult {
        override fun toString(): String = "PlayArtistPhraseResult.Malformed($reason)"
    }

    /** Provider explicitly reported a language other than English. */
    data object NonEnglish : PlayArtistPhraseResult
}

enum class PlayArtistPhraseError {
    EMPTY_INPUT,
    INPUT_TOO_LONG,
    UNSAFE_CHARACTER,
    EMPTY_ARTIST_SLOT,
    ARTIST_SLOT_TOO_LONG,
    INVALID_ARTIST_SLOT,
    QUESTION_OR_COMPOUND,
}

/**
 * Immutable pair used only in memory. The display label preserves normal
 * artist punctuation; the NFKC/root-locale key is NOT yet a fuzzy score.
 * The scorer/index must normalize BOTH query and indexed artist in P04.
 */
class PlayArtistSlot(
    val displayLabel: String,
    val normalizedKey: String,
) {
    init {
        require(displayLabel.isNotBlank())
        require(normalizedKey.isNotBlank())
    }

    override fun toString(): String = "PlayArtistSlot(redacted)"
}

/**
 * Exact v1.0 prefix only: "play songs by [artist]".
 * No unsupported paraphrases, generative synonyms or partial prefix guesses.
 *
 * "musico" is an optional spoken prefix AFTER an explicit Mic tap, per the
 * existing Voice Contract. This object itself cannot start recognition.
 */
object PlayArtistPhrasePolicy {
    const val GRAMMAR_VERSION = "issue11-play-artist-en-v1"
    const val APPROVED_PREFIX = "play songs by"
    const val MAX_TRANSCRIPT_CHARS = 120
    const val MAX_ARTIST_SLOT_CHARS = 80

    private val commandPrefix = "$APPROVED_PREFIX "
    private val whitespace = Regex("\\s+")

    // Disallow demonstrable follow-on actions/remote requests. These are
    // rejection markers, NOT additional accepted command phrase families.
    private val unsupportedSuffix = Regex(
        "(?:\\s+(?:on|from)\\s+(?:spotify|youtube|soundcloud|apple\\s+music)" +
            "|\\s+and\\s+then\\s+(?:play|pause|skip|next|previous|stop)" +
            "|\\s+then\\s+(?:play|pause|skip|next|previous|stop)" +
            "|\\s+and\\s+(?:pause|skip|next|previous|stop))" +
            "(?:\\s+(?:music|song|track))?$",
        RegexOption.IGNORE_CASE,
    )

    /** Pure, deterministic English language guard; null is unknown, not proof. */
    fun classify(raw: String, detectedLanguageTag: String? = null): PlayArtistPhraseResult {
        val detected = detectedLanguageTag?.trim()?.replace('_', '-')
        if (!detected.isNullOrEmpty() &&
            detected.substringBefore('-').lowercase(Locale.ROOT) != "en"
        ) return PlayArtistPhraseResult.NonEnglish

        if (raw.isBlank()) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.EMPTY_INPUT)
        }
        if (raw.length > MAX_TRANSCRIPT_CHARS) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.INPUT_TOO_LONG)
        }
        if (raw.any { unsafe(it) }) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.UNSAFE_CHARACTER)
        }

        // Fixed normalization order: NFKC -> apostrophe fold -> ASCII-space
        // collapse -> outer whitespace -> one optional PTT prefix ->
        // exact English command prefix -> bounded artist slot extraction.
        var input = Normalizer.normalize(raw, Normalizer.Form.NFKC)
            .replace('\u2018', '\'')
            .replace('\u2019', '\'')
            .replace('\u02bc', '\'')
            .replace('\u201b', '\'')
            .replace('\u201c', '"')
            .replace('\u201d', '"')
            .replace(whitespace, " ")
            .trim()

        // Allow a single balanced double quote around the entire utterance;
        // this is surrounding punctuation, not a new voice command.
        if (input.length >= 2 && input.first() == '"' && input.last() == '"') {
            input = input.substring(1, input.length - 1).trim()
        }

        val lower = input.lowercase(Locale.ROOT)
        if (lower.startsWith("musico ")) {
            input = input.substring("musico ".length).trimStart()
        }

        val normalizedCommand = input.lowercase(Locale.ROOT)
        if (normalizedCommand == APPROVED_PREFIX ||
            normalizedCommand.trimEnd('.', '!', ',') == APPROVED_PREFIX
        ) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.EMPTY_ARTIST_SLOT)
        }
        if (!normalizedCommand.startsWith(commandPrefix)) {
            // Includes "play music by", repeated "musico", wake phrases,
            // bare control commands and unrelated text. No guesswork.
            return PlayArtistPhraseResult.NotArtistPhrase
        }

        var artist = input.substring(commandPrefix.length).trim()
        if (artist.isEmpty()) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.EMPTY_ARTIST_SLOT)
        }
        if ('?' in artist || ';' in artist) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.QUESTION_OR_COMPOUND)
        }
        if (unsupportedSuffix.containsMatchIn(artist)) {
            return PlayArtistPhraseResult.Unsupported
        }
        // A comma followed by an explicit control phrase is compound; commas
        // within genuine names (e.g. "Earth, Wind & Fire") are retained.
        if (Regex(",\\s*(?:play|pause|next|previous|skip|stop)(?:\\s+\\w+)*$",
                RegexOption.IGNORE_CASE).containsMatchIn(artist)
        ) return PlayArtistPhraseResult.Unsupported

        // Trim non-semantic sentence-ending punctuation only at the edge.
        // A trailing period is preserved for initialism artist names such as
        // R.E.M.; later artist-normalization must respect that distinction.
        artist = artist.trimEnd('!', ',')
        if (!artist.dropLast(1).contains('.') && artist.endsWith('.')) {
            artist = artist.dropLast(1)
        }
        artist = artist.trim()
        if (artist.isEmpty()) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.EMPTY_ARTIST_SLOT)
        }
        if (artist.length > MAX_ARTIST_SLOT_CHARS) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.ARTIST_SLOT_TOO_LONG)
        }
        if (artist.any { unsafe(it) } || artist.none { it.isLetterOrDigit() }) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.INVALID_ARTIST_SLOT)
        }

        val display = LibraryTextNormalization.display(artist)
            ?: return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.INVALID_ARTIST_SLOT)
        val key = LibraryTextNormalization.key(display)
        if (key.isBlank()) {
            return PlayArtistPhraseResult.Malformed(PlayArtistPhraseError.INVALID_ARTIST_SLOT)
        }

        return PlayArtistPhraseResult.Recognized(PlayArtistSlot(display, key))
    }

    private fun unsafe(character: Char): Boolean =
        (Character.isISOControl(character) && !character.isWhitespace()) ||
            Character.getType(character) == Character.FORMAT ||
            character == '\ufffd'
}
