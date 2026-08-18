package com.latticeward.musiccopilot.spike.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeterministicIntentMapperTest {
    private val mapper = DeterministicIntentMapper(ConfidencePolicy(0.90))

    @Test
    fun mapsAllSixApprovedIntents() {
        val corpus = listOf(
            "Play" to VoiceIntent.PLAY,
            "Stop music" to VoiceIntent.PAUSE,
            "Next song" to VoiceIntent.NEXT,
            "Previous song" to VoiceIntent.PREVIOUS,
            "Go back 10 seconds" to VoiceIntent.SEEK_BACK,
            "Play songs by Adele" to VoiceIntent.PLAY_ARTIST,
        )
        val artists = listOf(Artist("a1", "Adele"))

        corpus.forEach { (phrase, expected) ->
            val result = mapper.map(phrase, providerConfidence = 0.96, artists = artists)
            assertTrue("$phrase should be recognized", result is MappingResult.Recognized)
            assertEquals(expected, (result as MappingResult.Recognized).candidate.intent)
        }
    }

    @Test
    fun normalizationIsLocaleIndependentAndStable() {
        assertEquals("guns n' roses", DeterministicIntentMapper.normalize("  GUNS N’ ROSES!!! "))
    }

    @Test
    fun exactArtistExecutesDirectly() {
        val result = mapper.map(
            "play songs by the national",
            0.96,
            listOf(Artist("artist-02", "The National")),
        )
        assertTrue(result is MappingResult.Recognized)
        assertEquals("artist-02", (result as MappingResult.Recognized).candidate.artist?.stableId)
    }

    @Test
    fun oneClearLongNameTypoExecutesDeterministically() {
        val result = mapper.map(
            "play songs by radiohed",
            0.96,
            listOf(
                Artist("artist-01", "Radiohead"),
                Artist("artist-02", "Portishead"),
            ),
        )
        assertTrue(result is MappingResult.Recognized)
        assertEquals("artist-01", (result as MappingResult.Recognized).candidate.artist?.stableId)
    }

    @Test
    fun similarArtistsRequireOneConfirmation() {
        val result = mapper.map(
            "play songs by muse",
            0.96,
            listOf(
                Artist("artist-01", "Muses"),
                Artist("artist-02", "Musee"),
            ),
        )
        assertTrue(result is MappingResult.Ambiguous)
        assertTrue((result as MappingResult.Ambiguous).options.size <= 2)
    }

    @Test
    fun noArtistMatchNeverExecutesNearestCandidate() {
        val result = mapper.map(
            "play songs by completely absent",
            0.96,
            listOf(Artist("artist-01", "Muse")),
        )
        assertEquals(
            MappingResult.Unknown(MappingFailureReason.ARTIST_NOT_FOUND),
            result,
        )
    }

    @Test
    fun lowProviderConfidenceCannotDirectExecute() {
        val result = mapper.map("next", 0.75)
        assertTrue(result is MappingResult.Ambiguous)
    }

    @Test
    fun unavailableProviderConfidenceFailsSafe() {
        val result = mapper.map("next", null)
        assertEquals(MappingResult.Unknown(MappingFailureReason.LOW_CONFIDENCE), result)
    }

    @Test
    fun unsupportedIntentIsDistinctFromUnknown() {
        assertTrue(mapper.map("shuffle everything", 0.99) is MappingResult.Unsupported)
        assertTrue(mapper.map("tell me a story", 0.99) is MappingResult.Unknown)
    }

    @Test
    fun negativeRowsHaveZeroDirectExecutions() {
        val artists = listOf(Artist("artist-01", "Muse"))
        val rows = listOf(
            mapper.map("play songs by absent name", 0.99, artists),
            mapper.map("shuffle everything", 0.99, artists),
            mapper.map("tell me a story", 0.99, artists),
            mapper.map("next", null, artists),
            mapper.map("play songs by", 0.99, artists),
        )
        assertTrue(rows.none { it is MappingResult.Recognized })
    }

    @Test
    fun repeatedResolutionIsDeterministic() {
        val artists = listOf(
            Artist("artist-b", "Musee", historyRank = 3),
            Artist("artist-a", "Muse", historyRank = 8),
        )
        val first = mapper.map("play songs by muses", 0.96, artists)
        repeat(100) {
            assertEquals(first, mapper.map("play songs by muses", 0.96, artists.shuffled()))
        }
    }
}
