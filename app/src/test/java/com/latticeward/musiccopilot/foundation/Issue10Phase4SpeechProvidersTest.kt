package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.policy.EnglishModelReadiness
import com.latticeward.musiccopilot.voice.policy.OnDeviceEnglishSupportPolicy as EnglishPolicy
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P04 — SOURCE AUTHORED ONLY, NOT EXECUTED. These are pure JVM
 * policies and fake-port contracts, not evidence of English packs on a phone.
 */
class Issue10Phase4SpeechProvidersTest {
    private val request = SpeechRecognitionRequest(
        VoiceSessionId(10), EnglishPolicy.REQUIRED_LANGUAGE_TAG,
        VoicePolicy.LISTEN_TIMEOUT_MS,
    )

    @Test fun englishRequestUsesExactlyOneExplicitLanguageTag() {
        assertEquals("en-US", EnglishPolicy.REQUIRED_LANGUAGE_TAG)
        assertTrue(EnglishPolicy.isSupportedRequest("en-US"))
        assertTrue(EnglishPolicy.isSupportedRequest("EN-us"))
        assertFalse(EnglishPolicy.isSupportedRequest("de-DE"))
        assertFalse(EnglishPolicy.isSupportedRequest("en-GB"))
    }

    @Test fun installedUSModelIsReadyWithoutRemoteFallback() {
        assertEquals(EnglishModelReadiness.Installed, EnglishPolicy.evaluate(
            "en-US", installed = listOf("en-US"),
            downloadable = emptyList(), pending = emptyList(),
        ))
    }

    @Test fun installedGenericEnglishModelSupportsRequestedUSLocale() {
        assertEquals(EnglishModelReadiness.Installed, EnglishPolicy.evaluate(
            "en-US", installed = listOf("en"),
            downloadable = emptyList(), pending = emptyList(),
        ))
        assertTrue(EnglishPolicy.matchesRequestedEnglish("EN_us", "en-US"))
    }

    @Test fun britishEnglishDoesNotClaimToBeAnInstalledUSLanguagePack() {
        assertEquals(EnglishModelReadiness.Unsupported, EnglishPolicy.evaluate(
            "en-US", installed = listOf("en-GB"),
            downloadable = emptyList(), pending = emptyList(),
        ))
    }

    @Test fun downloadableOrPendingModelNeverCountsAsInstalled() {
        assertEquals(EnglishModelReadiness.DownloadRequired, EnglishPolicy.evaluate(
            "en-US", installed = emptyList(),
            downloadable = listOf("en-US"), pending = emptyList(),
        ))
        assertEquals(EnglishModelReadiness.DownloadRequired, EnglishPolicy.evaluate(
            "en-US", installed = emptyList(),
            downloadable = emptyList(), pending = listOf("en-US"),
        ))
    }

    @Test fun emptyOrUnrelatedSupportFailsClosed() {
        assertEquals(EnglishModelReadiness.Unsupported, EnglishPolicy.evaluate(
            "en-US", installed = listOf("de-DE"),
            downloadable = emptyList(), pending = emptyList(),
        ))
        assertEquals(EnglishModelReadiness.Unsupported, EnglishPolicy.evaluate(
            "de-DE", installed = listOf("en-US"),
            downloadable = emptyList(), pending = emptyList(),
        ))
    }

    @Test fun fakeSttCanReturnAnAbsentConfidenceWithoutFabricatingOne() = runBlocking {
        val stt: SpeechToTextEngine = object : SpeechToTextEngine {
            override fun isAvailable() = true
            override suspend fun recognize(
                request: SpeechRecognitionRequest,
            ): AppResult<SpeechRecognitionResult> = AppResult.Success(
                SpeechRecognitionResult("pause", providerConfidence = null,
                    detectedLanguageTag = null)
            )
            override fun cancel(sessionId: VoiceSessionId) = Unit
        }
        val spoken = stt.recognize(request) as AppResult.Success
        assertNull(spoken.value.providerConfidence)
        assertNull(spoken.value.detectedLanguageTag)
    }

    @Test fun fakeUnavailableSpeechProviderMustNotBecomePlaybackAuthority() = runBlocking {
        val stt: SpeechToTextEngine = object : SpeechToTextEngine {
            override fun isAvailable() = false
            override suspend fun recognize(
                request: SpeechRecognitionRequest,
            ): AppResult<SpeechRecognitionResult> = AppResult.Failure(
                AppError.Voice(AppErrorCodes.VOICE_UNAVAILABLE,
                    Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
            )
            override fun cancel(sessionId: VoiceSessionId) = Unit
        }
        assertFalse(stt.isAvailable())
        assertTrue(stt.recognize(request) is AppResult.Failure)
    }

    @Test fun fakeOfflineFeedbackPortDoesNotControlPlayback() = runBlocking {
        val commands = mutableListOf<SpeechOutput>()
        val tts: TextToSpeechEngine = object : TextToSpeechEngine {
            override fun isAvailable() = true
            override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
                commands += output
                return AppResult.Success(Unit)
            }
            override fun stop(sessionId: VoiceSessionId) = Unit
        }
        val output = SpeechOutput(request.sessionId, "voice_done", "Done")
        assertTrue(tts.speak(output) is AppResult.Success)
        assertEquals(listOf(output), commands)
    }

    @Test fun androidSourceUsesOnlyExplicitOnDeviceRecognitionAndNoAutomaticModelDownload() {
        val src = File("src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOnDeviceSpeechToTextEngine.kt")
            .readText()
        assertTrue(src.contains("createOnDeviceSpeechRecognizer"))
        assertTrue(src.contains("checkRecognitionSupport"))
        assertTrue(src.contains("installedOnDeviceLanguages"))
        assertFalse(src.contains("SpeechRecognizer.createSpeechRecognizer("))
        assertFalse(src.contains("recognizer.triggerModelDownload("))
        assertTrue(src.contains("continuation.invokeOnCancellation"))
    }

    @Test fun ttsSourceSelectsOnlyInstalledNonNetworkEnglishVoiceAndGuardsLateCallbacks() {
        val src = File("src/main/java/com/latticeward/musiccopilot/voice/android/AndroidOfflineTextToSpeechEngine.kt")
            .readText()
        assertTrue(src.contains("!it.isNetworkConnectionRequired"))
        assertTrue(src.contains("Locale.ENGLISH.language"))
        assertTrue(src.contains("activeUtteranceId"))
        assertTrue(src.contains("continuation.invokeOnCancellation"))
        assertTrue(src.contains("engine?.shutdown()"))
    }

    @Test fun manifestAndProductionBindingsDoNotRegisterCloudSTT() {
        val base = File("src/main/java/com/latticeward/musiccopilot")
        val bindings = File(base, "app/di/VoiceBindings.kt").readText()
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertTrue(bindings.contains("AndroidOnDeviceSpeechToTextEngine"))
        assertTrue(bindings.contains("AndroidOfflineTextToSpeechEngine"))
        assertFalse(bindings.contains("CloudSpeech"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.RECORD_AUDIO"))
    }
}
