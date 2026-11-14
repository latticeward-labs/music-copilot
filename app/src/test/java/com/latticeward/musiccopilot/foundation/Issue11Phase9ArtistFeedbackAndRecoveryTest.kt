package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayExit
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayPresentationPolicy
import com.latticeward.musiccopilot.core.application.voice.VoiceOverlayStatus
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.feedback.EnglishVoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackKey
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackPolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P09 — source-authored, NOT_COMPILED/NOT_RUN.
 * Short English artist feedback, typed terminal errors, privacy and UI
 * fallback. This is NOT a real device/TTS/Media3 success claim.
 */
class Issue11Phase9ArtistFeedbackAndRecoveryTest {
    private val sessionId = VoiceSessionId(88L)
    private val catalog = EnglishVoiceFeedbackCatalog
    private val policy = VoiceFeedbackPolicy
    private val overlay = VoiceOverlayPresentationPolicy

    private fun failed(code: String, origin: VoiceOrigin = VoiceOrigin.Player) =
        VoiceSessionState.Failed(
            sessionId, origin,
            AppError.Voice(code, Recoverability.TERMINAL_FOR_ATTEMPT,
                RecoveryAction.ManualFallback),
            canRetry = false,
        )

    private fun source(path: String): String =
        File("src/main/java/com/latticeward/musiccopilot", path).readText()

    private val artistKeys = listOf(
        VoiceFeedbackKey.ArtistPlaying, VoiceFeedbackKey.ArtistConfirmOne,
        VoiceFeedbackKey.ArtistConfirmTwo, VoiceFeedbackKey.ArtistNotFound,
        VoiceFeedbackKey.ArtistUnsupported, VoiceFeedbackKey.ArtistDeclined,
        VoiceFeedbackKey.ArtistCanceled, VoiceFeedbackKey.ArtistStale,
        VoiceFeedbackKey.ArtistUnavailable, VoiceFeedbackKey.ArtistPlaybackFailure,
    )

    @Test fun allArtistMessagesAreSingleShortEnglishSentences() {
        for (key in artistKeys) {
            val sentence = catalog.textFor(key)
            assertTrue(key.toString(), policy.isShortSentence(sentence))
            assertTrue(key.toString(), sentence.length <= 120)
        }
    }

    @Test fun shortOneCandidateQuestionEndsWithQuestionMark() {
        assertEquals("Did you mean this local artist?",
            catalog.textFor(VoiceFeedbackKey.ArtistConfirmOne))
    }

    @Test fun shortTwoCandidateQuestionIsExactlyOneSentence() {
        val sentence = catalog.textFor(VoiceFeedbackKey.ArtistConfirmTwo)
        assertTrue(sentence.endsWith("?"))
        assertEquals(1, sentence.count { it == '?' })
        assertTrue(policy.isShortSentence(sentence))
    }

    @Test fun validationAllowsOneQuestionButNotTwoOrTrailingWhitespace() {
        assertTrue(policy.isShortSentence("Which artist did you mean?"))
        assertFalse(policy.isShortSentence("Which artist? Another one?"))
        assertFalse(policy.isShortSentence("Which artist? "))
        assertFalse(policy.isShortSentence("Which artist?\n"))
    }

    @Test fun legacyControlSentenceValidationRemainsValid() {
        assertTrue(policy.isShortSentence("Playback paused."))
        assertFalse(policy.isShortSentence("Done. Again."))
        assertFalse(policy.isShortSentence("Ready!"))
    }

    @Test fun allNewOutputsHaveStableNonPersonalI11Keys() {
        for (key in artistKeys) {
            val output = policy.output(sessionId, key, catalog)!!
            assertTrue(output.textKey.startsWith("i11_feedback_artist_"))
            assertEquals(sessionId, output.sessionId)
            assertFalse(output.shortText.contains("audio:"))
        }
    }

    @Test fun playingSpeechNeverRequiresArtistNameOrTranscript() {
        val text = catalog.textFor(VoiceFeedbackKey.ArtistPlaying)
        assertEquals("Playing songs by the selected artist.", text)
        assertFalse(text.contains("Synthetic Artist"))
    }

    @Test fun notFoundFeedbackReferencesOnlyLocalLibrary() {
        val text = catalog.textFor(VoiceFeedbackKey.ArtistNotFound)
        assertTrue(text.contains("your library"))
        assertFalse(text.contains("Spotify"))
    }

    @Test fun playbackFailureNeverClaimsPlayStarted() {
        val text = catalog.textFor(VoiceFeedbackKey.ArtistPlaybackFailure)
        assertTrue(text.contains("could not start"))
        assertFalse(text.startsWith("Playing"))
    }

    @Test fun staleIndexFeedbackPromptsManualWithoutGuessing() {
        val text = catalog.textFor(VoiceFeedbackKey.ArtistStale)
        assertTrue(text.contains("library changed"))
        assertTrue(text.contains("manually"))
    }

    @Test fun unavailableFeedbackPromisesNoAutomaticReplay() {
        val text = catalog.textFor(VoiceFeedbackKey.ArtistUnavailable)
        assertTrue(text.contains("manual"))
        assertFalse(text.contains("retrying automatically"))
    }

    @Test fun canceledAndDeclinedHaveDistinctConstantLabels() {
        assertNotEquals(catalog.textFor(VoiceFeedbackKey.ArtistDeclined),
            catalog.textFor(VoiceFeedbackKey.ArtistCanceled))
    }

    @Test fun artistNotFoundIsTerminalVoiceFeedback() {
        assertEquals(VoiceFeedbackKey.ArtistNotFound,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_NOT_FOUND, false))
    }

    @Test fun artistUnsupportedIsTerminalVoiceFeedback() {
        assertEquals(VoiceFeedbackKey.ArtistUnsupported,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_UNSUPPORTED, false))
    }

    @Test fun artistDeclinedIsTerminalVoiceFeedback() {
        assertEquals(VoiceFeedbackKey.ArtistDeclined,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_DECLINED, false))
    }

    @Test fun staleAndUnavailableUseDifferentRecoveryCodes() {
        assertEquals(VoiceFeedbackKey.ArtistStale,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_STALE, false))
        assertEquals(VoiceFeedbackKey.ArtistUnavailable,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_UNAVAILABLE, false))
    }

    @Test fun playbackFailedHasDistinctVoiceFailureKey() {
        assertEquals(VoiceFeedbackKey.ArtistPlaybackFailure,
            policy.terminalFailure(AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED, false))
    }

    @Test fun retryableFailuresStaySilentBeforeTheSecondRecognition() {
        for (code in listOf(AppErrorCodes.VOICE_TIMEOUT, AppErrorCodes.VOICE_NO_MATCH)) {
            assertNull(policy.terminalFailure(code, true))
        }
    }

    @Test fun typedNotFoundIsShownInPlayerWithManualReturn() {
        val view = overlay.project(failed(AppErrorCodes.VOICE_ARTIST_NOT_FOUND),
            VoicePracticeStage.Idle)
        assertEquals(VoiceOverlayStatus.ArtistNotFound, view.status)
        assertTrue(view.visible)
        assertFalse(view.showRetry)
        assertEquals(VoiceOverlayExit.ReturnToManual, view.exit)
    }

    @Test fun typedNotFoundIsShownIdenticallyInDriving() {
        val player = overlay.project(failed(AppErrorCodes.VOICE_ARTIST_NOT_FOUND),
            VoicePracticeStage.Idle)
        val driving = overlay.project(
            failed(AppErrorCodes.VOICE_ARTIST_NOT_FOUND, VoiceOrigin.Driving),
            VoicePracticeStage.Idle,
        )
        assertEquals(player, driving)
    }

    @Test fun declinedSelectionIsTerminalAndDoesNotAskAnotherQuestion() {
        val view = overlay.project(failed(AppErrorCodes.VOICE_ARTIST_DECLINED),
            VoicePracticeStage.Idle)
        assertEquals(VoiceOverlayStatus.ArtistDeclined, view.status)
        assertFalse(view.showRetry)
        assertFalse(view.showPermissionContinue)
    }

    @Test fun unavailableAndServiceFailureRemainDifferentUiStatuses() {
        assertEquals(VoiceOverlayStatus.ArtistUnavailable,
            overlay.project(failed(AppErrorCodes.VOICE_ARTIST_UNAVAILABLE),
                VoicePracticeStage.Idle).status)
        assertEquals(VoiceOverlayStatus.ArtistPlaybackFailure,
            overlay.project(failed(AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED),
                VoicePracticeStage.Idle).status)
    }

    @Test fun staleAndUnsupportedUseSpecificUiStatuses() {
        assertEquals(VoiceOverlayStatus.ArtistStale,
            overlay.project(failed(AppErrorCodes.VOICE_ARTIST_STALE),
                VoicePracticeStage.Idle).status)
        assertEquals(VoiceOverlayStatus.ArtistUnsupported,
            overlay.project(failed(AppErrorCodes.VOICE_ARTIST_UNSUPPORTED),
                VoicePracticeStage.Idle).status)
    }

    @Test fun unknownPrivateErrorCodeDoesNotLeakToUiText() {
        val view = overlay.project(failed("private/uri/transcript"),
            VoicePracticeStage.Idle)
        assertEquals(VoiceOverlayStatus.Unavailable, view.status)
    }

    @Test fun fiveExistingTransportIntentAcknowledgementsAreUnchanged() {
        assertEquals(VoiceFeedbackKey.Play,
            policy.acknowledged(RestrictedIntent.PLAY))
        assertEquals(VoiceFeedbackKey.Pause,
            policy.acknowledged(RestrictedIntent.PAUSE))
        assertNull(policy.acknowledged(RestrictedIntent.PLAY_ARTIST))
    }

    @Test fun coordinatorSpeaksArtistSuccessOnlyAfterPublishingAckedExecution() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("if (sent.value == PlayArtistDecision.Executed)"))
        assertTrue(code.contains("if (publishExecuted(id, origin, number, generation))"))
        assertTrue(code.contains("VoiceFeedbackKey.ArtistPlaying"))
        assertTrue(code.contains("if (ack.value == PlayArtistDecision.Executed)"))
    }

    @Test fun clarificationIsOptionalAndNeverBlocksConfirmationUi() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("queueArtistClarification(id, origin, number, generation)"))
        assertTrue(code.contains("ownerScope.launch(generation)"))
        assertTrue(code.contains("pendingArtist"))
        assertTrue(code.contains("withTimeoutOrNull(VoiceFeedbackPolicy.MAX_SPEAK_MS)"))
        assertTrue(code.contains("artistPromptClaimed = true"))
        assertTrue(code.contains("tts.stop(id)"))
    }

    @Test fun terminalArtistErrorDisablesRecognitionRetry() {
        val code = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(code.contains("ARTIST_TERMINAL_CODES"))
        assertTrue(code.contains("Recoverability.TERMINAL_FOR_ATTEMPT"))
        assertTrue(code.contains("code !in ARTIST_TERMINAL_CODES"))
    }

    @Test fun allEnglishAndroidSpeechStringsExistExactlyOnce() {
        val xml = File("src/main/res/values/strings.xml").readText()
        val names = listOf(
            "playing", "confirm_one", "confirm_two", "not_found",
            "unsupported", "declined", "canceled", "stale", "unavailable",
            "playback_failure",
        )
        for (name in names) {
            val token = "name=\"i11_feedback_artist_$name\""
            assertEquals(token, 1, Regex(Regex.escape(token)).findAll(xml).count())
        }
    }

    @Test fun failureUiStringsAreResourceBackedAndNonDuplicated() {
        val xml = File("src/main/res/values/strings.xml").readText()
        for (name in listOf("not_found", "unsupported", "declined", "stale",
            "unavailable", "playback_failed", "manual_fallback")
        ) {
            val token = "name=\"i11_voice_artist_$name\""
            assertEquals(token, 1, Regex(Regex.escape(token)).findAll(xml).count())
        }
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("R.string.i11_voice_artist_not_found"))
        assertTrue(panel.contains("R.string.i11_voice_artist_manual_fallback"))
    }

    @Test fun spokenTextIsFixedNotBuiltFromArtistNamesOrMediaRefs() {
        val feedback = source("voice/feedback/VoiceFeedbackCatalog.kt")
        assertFalse(feedback.contains("mediaRef"))
        assertFalse(feedback.contains("candidate.displayLabel"))
        assertFalse(feedback.contains(".transcript"))
        assertFalse(feedback.contains("artistGroupId"))
    }

    @Test fun voiceAndAndroidProviderKeepPlaybackAuthorityInMedia3() {
        val catalog = source("voice/android/AndroidVoiceFeedbackCatalog.kt")
        val feedback = source("voice/feedback/VoiceFeedbackCatalog.kt")
        assertFalse(catalog.contains("PlaybackGateway"))
        assertFalse(catalog.contains("MediaStore"))
        assertFalse(feedback.contains("PlaybackCommand.ReplaceQueue"))
    }
}
