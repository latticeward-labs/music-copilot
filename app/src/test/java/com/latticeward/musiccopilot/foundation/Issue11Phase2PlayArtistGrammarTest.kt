package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.intent.DirectControlPhrasePolicy
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhraseError
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhrasePolicy
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhraseResult
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P02 — AUTHORED ONLY, NOT RUN / NOT COMPILED.
 *
 * P00-P12 owner restriction prohibits every test/Gradle/CI/build action.
 * P13 is the first eligible phase for test execution and APK delivery.
 */
class Issue11Phase2PlayArtistGrammarTest {
    private val mapper = RestrictedVoiceIntentMapper()

    private fun recognized(input: String, lang: String? = null): PlayArtistPhraseResult.Recognized {
        val result = PlayArtistPhrasePolicy.classify(input, lang)
        assertTrue("expected typed artist slot", result is PlayArtistPhraseResult.Recognized)
        return result as PlayArtistPhraseResult.Recognized
    }

    private fun speech(
        text: String,
        confidence: Double? = null,
        language: String? = null,
        localVadVerified: Boolean = false,
    ) = SpeechRecognitionResult(text, confidence, language, localVadVerified)

    @Test fun grammarHasStableVersionAndOnlyApprovedPrefix() {
        assertEquals("issue11-play-artist-en-v1", PlayArtistPhrasePolicy.GRAMMAR_VERSION)
        assertEquals("play songs by", PlayArtistPhrasePolicy.APPROVED_PREFIX)
        assertEquals(120, PlayArtistPhrasePolicy.MAX_TRANSCRIPT_CHARS)
        assertEquals(80, PlayArtistPhrasePolicy.MAX_ARTIST_SLOT_CHARS)
    }

    @Test fun canonicalCommandExtractsPrivateArtistSlotWithoutPlayback() {
        val slot = recognized("Play songs by Adele").slot
        assertEquals("Adele", slot.displayLabel)
        assertEquals("adele", slot.normalizedKey)
    }

    @Test fun artistSlotPreservesCaseWhileLookupKeyUsesLocaleIndependentCase() {
        val slot = recognized("  PLAY   SONGS    BY   The Weeknd  ").slot
        assertEquals("The Weeknd", slot.displayLabel)
        assertEquals("the weeknd", slot.normalizedKey)
    }

    @Test fun approvedOptionalPostTapMusicoPrefixIsAcceptedOnlyOnce() {
        val slot = recognized(" Musico   PLAY songs   by  2Pac! ").slot
        assertEquals("2Pac", slot.displayLabel)
        assertEquals("2pac", slot.normalizedKey)
        assertSame(
            PlayArtistPhraseResult.NotArtistPhrase,
            PlayArtistPhrasePolicy.classify("musico musico play songs by Adele"),
        )
    }

    @Test fun commonUnicodeQuotesAndApostrophesAreCanonicalized() {
        val slot = recognized("“PLAY SONGS BY L’Arc”").slot
        assertEquals("L'Arc", slot.displayLabel)
        assertEquals("l'arc", slot.normalizedKey)
        assertEquals("Adele", recognized("Play songs by Adele.").slot.displayLabel)
    }

    @Test fun internalPunctuationAndArtistInitialismsAreNotBlindlyRemoved() {
        assertEquals(
            "Earth, Wind & Fire",
            recognized("Play songs by Earth, Wind & Fire").slot.displayLabel,
        )
        assertEquals("R.E.M.", recognized("Play songs by R.E.M.").slot.displayLabel)
        assertEquals("AC/DC", recognized("Play songs by AC/DC").slot.displayLabel)
    }

    @Test fun blankAndEmptyArtistAreTypedMalformedRatherThanDirect() {
        assertEquals(PlayArtistPhraseError.EMPTY_INPUT,
            (PlayArtistPhrasePolicy.classify("   ") as PlayArtistPhraseResult.Malformed).reason)
        for (phrase in listOf("Play songs by", "PLAY SONGS BY.", "Play songs by   !", "musico play songs by ")) {
            assertEquals(phrase, PlayArtistPhraseError.EMPTY_ARTIST_SLOT,
                (PlayArtistPhrasePolicy.classify(phrase) as PlayArtistPhraseResult.Malformed).reason)
        }
    }

    @Test fun unrelatedAndUnapprovedAliasesAreNotGuessed() {
        for (phrase in listOf(
            "Play music by Adele", "Play artist Adele", "Songs by Adele",
            "Play Adele", "Hey musico play songs by Adele",
            "Play", "Pause", "Next", "Go back 10 seconds",
        )) assertSame(phrase, PlayArtistPhraseResult.NotArtistPhrase,
            PlayArtistPhrasePolicy.classify(phrase))
    }

    @Test fun detectedNonEnglishOverridesEnglishPrefix() {
        for (tag in listOf("de-DE", "fa-IR", "tr-TR", "fr")) {
            assertSame(PlayArtistPhraseResult.NonEnglish,
                PlayArtistPhrasePolicy.classify("Play songs by Adele", tag))
        }
        assertEquals("Adele", recognized("Play songs by Adele", "en-US").slot.displayLabel)
        assertEquals("Adele", recognized("Play songs by Adele", null).slot.displayLabel)
    }

    @Test fun outputIsDeterministicForSameInputAndLanguage() {
        val input = "Musico Play songs by The 1975"
        val baseline = recognized(input).slot
        repeat(25) {
            val current = recognized(input).slot
            assertEquals(baseline.displayLabel, current.displayLabel)
            assertEquals(baseline.normalizedKey, current.normalizedKey)
        }
    }

    @Test fun inputLimitIsBoundedAndRejectsControlAndFormatCharacters() {
        val long = "play songs by " + "A".repeat(121)
        assertEquals(PlayArtistPhraseError.INPUT_TOO_LONG,
            (PlayArtistPhrasePolicy.classify(long) as PlayArtistPhraseResult.Malformed).reason)
        for (text in listOf("Play songs by Ad\u0000ele", "Play songs by A\u202Edele")) {
            assertEquals(PlayArtistPhraseError.UNSAFE_CHARACTER,
                (PlayArtistPhrasePolicy.classify(text) as PlayArtistPhraseResult.Malformed).reason)
        }
    }

    @Test fun artistSlotLengthIsBoundedIndependentlyOfTranscriptLimit() {
        val artist = "A".repeat(81)
        assertEquals(PlayArtistPhraseError.ARTIST_SLOT_TOO_LONG,
            (PlayArtistPhrasePolicy.classify("Play songs by $artist") as
                PlayArtistPhraseResult.Malformed).reason)
        assertEquals("A".repeat(80),
            recognized("Play songs by " + "A".repeat(80)).slot.displayLabel)
    }

    @Test fun questionAndSemicolonCompoundDoNotGuessArtist() {
        for (text in listOf("Play songs by Adele?", "Play songs by Adele; pause")) {
            assertEquals(PlayArtistPhraseError.QUESTION_OR_COMPOUND,
                (PlayArtistPhrasePolicy.classify(text) as PlayArtistPhraseResult.Malformed).reason)
        }
    }

    @Test fun explicitStreamingAndMultipleTransportActionsAreUnsupported() {
        for (text in listOf(
            "Play songs by Adele on Spotify",
            "Play songs by Adele from YouTube",
            "Play songs by Adele then pause",
            "Play songs by Adele and next",
            "Play songs by Adele and then skip song",
            "Play songs by Adele, next",
        )) assertSame(text, PlayArtistPhraseResult.Unsupported,
            PlayArtistPhrasePolicy.classify(text))
    }

    @Test fun punctuationOnlyArtistIsMalformed() {
        assertEquals(PlayArtistPhraseError.INVALID_ARTIST_SLOT,
            (PlayArtistPhrasePolicy.classify("Play songs by ---") as
                PlayArtistPhraseResult.Malformed).reason)
    }

    @Test fun artistParsingExposesNoPersonalContentInDiagnosticToString() {
        val result = recognized("Play songs by PrivateArtistName")
        assertFalse(result.toString().contains("PrivateArtistName"))
        assertFalse(result.slot.toString().contains("PrivateArtistName"))
        assertEquals("privateartistname", result.slot.normalizedKey)
    }

    @Test fun pureSourceMapperCanClassifyButLegacyFiveControlRuntimeIsUnchanged() {
        val result = mapper.classifyPlayArtist(speech(
            "Play songs by Adele", confidence = 0.99, language = "en-US",
        ))
        assertEquals("Adele", (result as PlayArtistPhraseResult.Recognized).slot.displayLabel)

        // P02 MUST NOT make the sixth intent executable via old map() path.
        assertSame(VoiceIntentDecision.Rejected,
            mapper.map(speech("Play songs by Adele", confidence = 0.99)))
        assertSame(VoiceIntentDecision.Rejected,
            mapper.map(speech("Play songs by Adele", localVadVerified = true)))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE),
            mapper.map(speech("pause", confidence = 0.99)))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.NEXT),
            mapper.map(speech("next song", confidence = null, localVadVerified = true)))
        assertEquals("issue10-five-controls-v1", DirectControlPhrasePolicy.GRAMMAR_VERSION)
    }

    @Test fun localVadIsNotAnArtistConfidenceScore() {
        val parseResult = mapper.classifyPlayArtist(speech(
            "Play songs by Adele", confidence = null, localVadVerified = true,
        ))
        assertTrue(parseResult is PlayArtistPhraseResult.Recognized)
        // No provider-confidence fabrication or implicit execution admission.
        assertSame(VoiceIntentDecision.Rejected,
            mapper.map(speech("Play songs by Adele", localVadVerified = true)))
    }
}
