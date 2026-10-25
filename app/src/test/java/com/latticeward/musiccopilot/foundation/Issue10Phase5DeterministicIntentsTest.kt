package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.intent.DirectControlPhrasePolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * P05: deterministic matcher + bounded confidence, AUTHORED_ONLY / NOT_RUN.
 * No Android recognizer, emulator, playback service or Gradle task is run.
 */
class Issue10Phase5DeterministicIntentsTest {
    private val mapper = RestrictedVoiceIntentMapper()
    private fun speech(
        phrase: String,
        confidence: Double? = 0.99,
        language: String? = null,
    ) = SpeechRecognitionResult(phrase, confidence, language)
    private fun decision(
        phrase: String,
        confidence: Double? = 0.99,
        language: String? = null,
    ): VoiceIntentDecision = mapper.map(speech(phrase, confidence, language))

    @Test fun grammarIsVersionedFiniteAndContainsExactlyFiveIntentFamilies() {
        assertEquals("issue10-five-controls-v1", DirectControlPhrasePolicy.GRAMMAR_VERSION)
        assertEquals(
            setOf(
                RestrictedIntent.PLAY, RestrictedIntent.PAUSE,
                RestrictedIntent.NEXT, RestrictedIntent.PREVIOUS,
                RestrictedIntent.SEEK_BACK,
            ), DirectControlPhrasePolicy.approvedPhrases.values.toSet()
        )
        assertEquals(19, DirectControlPhrasePolicy.approvedPhrases.size)
        assertFalse(DirectControlPhrasePolicy.approvedPhrases.values
            .contains(RestrictedIntent.PLAY_ARTIST))
    }

    @Test fun playAcceptsApprovedBareAndPrefixedPhrases() {
        for (phrase in listOf("play", "resume", "resume music", "play music")) {
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PLAY), decision(phrase))
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PLAY), decision("musico $phrase"))
        }
    }

    @Test fun pauseAcceptsApprovedBareAndPrefixedPhrases() {
        for (phrase in listOf("pause", "pause music", "stop music")) {
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision(phrase))
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision("musico $phrase"))
        }
    }

    @Test fun nextAcceptsApprovedBareAndPrefixedPhrases() {
        for (phrase in listOf("next", "next song", "next track", "skip song")) {
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.NEXT), decision(phrase))
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.NEXT), decision("musico $phrase"))
        }
    }

    @Test fun previousAcceptsApprovedBareAndPrefixedPhrases() {
        for (phrase in listOf("previous", "previous song", "previous track")) {
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PREVIOUS), decision(phrase))
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PREVIOUS), decision("musico $phrase"))
        }
    }

    @Test fun seekBackAcceptsOnlyExplicitTenSecondPhrases() {
        for (phrase in listOf(
            "go back 10 seconds", "back 10 seconds", "seek back 10 seconds",
            "go back ten seconds", "back ten seconds",
        )) {
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.SEEK_BACK), decision(phrase))
            assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.SEEK_BACK), decision("musico $phrase"))
        }
    }

    @Test fun microphoneWakePhraseIsNotRequiredForExplicitPtt() {
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision("pause"))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision("musico pause"))
    }

    @Test fun formattingNormalizesCaseWhitespaceAndTerminalSTTPunctuation() {
        val input = " \tMUSICO \n NEXT   SONG. "
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.NEXT), decision(input))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PLAY), decision(" PLAY! "))
        assertEquals("next song", DirectControlPhrasePolicy.normalize("  NEXT  song "))
    }

    @Test fun repeatedInvocationAndBareWakeWordNeverBecomeExecutable() {
        for (phrase in listOf("musico", "musico musico play", "musico, pause",
            "musico please play", "hey musico play")) {
            assertEquals(phrase, VoiceIntentDecision.Rejected, decision(phrase))
        }
    }

    @Test fun artistIntentIsReservedForIssueElevenEvenAtHighConfidence() {
        for (phrase in listOf("play songs by Adele", "musico play songs by Adele",
            "play artist Adele", "musico play songs by synthetic artist")) {
            assertEquals(VoiceIntentDecision.Rejected, decision(phrase, 1.0))
        }
    }

    @Test fun unrelatedCapabilitiesNeverMapToPlayerControls() {
        for (phrase in listOf(
            "volume up", "volume down", "play YouTube", "play on Spotify",
            "play playlist", "enable microphone", "open settings", "stop the car",
            "tell me the weather", "delete all songs", "stream next", "play podcast",
        )) assertEquals(phrase, VoiceIntentDecision.Rejected, decision(phrase))
    }

    @Test fun commandsContainingExtraWordsOrMultipleIntentsAreRejected() {
        for (phrase in listOf(
            "pause and next", "next after pause", "play play", "next song twice",
            "musico next please", "stop", "next next", "skip two songs",
        )) assertEquals(phrase, VoiceIntentDecision.Rejected, decision(phrase))
    }

    @Test fun unsafeSeekTargetsNeverApproximateTenSeconds() {
        for (phrase in listOf(
            "seek to minute 3", "go forward 10 seconds", "back 30 seconds",
            "go back 5 seconds", "rewind", "seek back thirty seconds",
        )) assertEquals(VoiceIntentDecision.Rejected, decision(phrase))
    }

    @Test fun unknownWordsRejectBeforeConfidencePolicyToAvoidFakeConfirmation() {
        for (confidence in listOf<Double?>(null, 0.1, 0.99)) {
            assertEquals(VoiceIntentDecision.Rejected, decision("next five tracks", confidence))
        }
    }

    @Test fun noProviderConfidenceFailsClosedForAnOtherwiseValidPhrase() {
        assertEquals(VoiceIntentDecision.RequiresConfirmation, decision("next song", null))
    }

    @Test fun lowerThanDefaultThresholdNeedsNonExecutingFallback() {
        assertEquals(VoiceIntentDecision.RequiresConfirmation, decision("pause", 0.89))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision("pause", 0.90))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PAUSE), decision("pause", 1.0))
    }

    @Test fun invalidConfidenceNeverAutoExecutes() {
        for (value in listOf(
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            -0.001, 1.001
        )) assertEquals(VoiceIntentDecision.RequiresConfirmation, decision("play", value))
    }

    @Test fun validBoundedThresholdCanBeAdjustedWithoutAddingPhrases() {
        assertEquals(0.90, mapper.directThreshold, 0.0)
        val strict = RestrictedVoiceIntentMapper(0.95)
        assertEquals(
            VoiceIntentDecision.RequiresConfirmation,
            strict.map(speech("next", 0.94))
        )
        assertEquals(
            VoiceIntentDecision.Direct(RestrictedIntent.NEXT),
            strict.map(speech("next", 0.95))
        )
        assertEquals(VoiceIntentDecision.Rejected, strict.map(speech("play artist Adele", 1.0)))
    }

    @Test fun invalidOrOutOfRangeConfigRevertsToSafeThreshold() {
        for (value in listOf(
            Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            0.0, 0.799, 0.981, 1.01
        )) {
            val fallback = RestrictedVoiceIntentMapper(value)
            assertEquals(VoicePolicy.T_DIRECT, fallback.directThreshold, 0.0)
            assertEquals(VoiceIntentDecision.RequiresConfirmation,
                fallback.map(speech("pause", 0.89)))
        }
    }

    @Test fun exactThresholdBoundsAreAccepted() {
        assertEquals(0.80, RestrictedVoiceIntentMapper(0.80).directThreshold, 0.0)
        assertEquals(0.98, RestrictedVoiceIntentMapper(0.98).directThreshold, 0.0)
    }

    @Test fun providerDetectedNonEnglishOverridesEnglishWords() {
        assertEquals(VoiceIntentDecision.Rejected, decision("play", 0.99, "de-DE"))
        assertEquals(VoiceIntentDecision.Rejected, decision("play", 0.99, "fa-IR"))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PLAY),
            decision("play", 0.99, "en-US"))
        assertEquals(VoiceIntentDecision.Direct(RestrictedIntent.PLAY),
            decision("play", 0.99, null))
    }

    @Test fun blankOversizedAndControlCharInputsReject() {
        assertEquals(VoiceIntentDecision.Rejected, decision(""))
        assertEquals(VoiceIntentDecision.Rejected, decision(" ".repeat(130)))
        assertEquals(VoiceIntentDecision.Rejected, decision("play\u0000"))
        assertEquals(VoiceIntentDecision.Rejected, decision("play".repeat(50)))
    }

    @Test fun dictionaryIsDeterministicWithoutMutableRecognitionHistory() {
        val before = decision("musico next song")
        repeat(100) {
            assertEquals(before, decision("musico next song"))
            assertEquals(VoiceIntentDecision.Rejected, decision("next and play"))
        }
    }

    @Test fun sourceInjectsBuildConfigThresholdWithoutRemoteRuntimeControl() {
        val app = File("src/main/java/com/latticeward/musiccopilot")
        val binding = File(app, "app/di/VoiceBindings.kt").readText()
        val policy = File(app, "voice/intent/RestrictedVoiceIntentMapper.kt").readText()
        val build = File("build.gradle.kts").readText()
        assertTrue(binding.contains("BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD"))
        assertTrue(build.contains("VOICE_DIRECT_CONFIDENCE_THRESHOLD"))
        assertTrue(policy.contains("DirectControlPhrasePolicy.identify"))
        assertFalse(binding.contains("RemoteConfig"))
    }

    @Test fun coordinatorNeverExecutesReservedArtistIntent() {
        val src = File("src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt")
            .readText()
        assertTrue(src.contains("RestrictedIntent.PLAY_ARTIST -> null"))
        assertTrue(src.contains("RestrictedIntent.SEEK_BACK -> PlaybackCommand.SeekBy(commandId, -10_000L)"))
        assertFalse(src.contains("fuzzyArtistResolver"))
    }
}
