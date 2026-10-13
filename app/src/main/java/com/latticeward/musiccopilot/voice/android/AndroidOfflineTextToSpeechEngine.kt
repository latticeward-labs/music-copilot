package com.latticeward.musiccopilot.voice.android

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Feedback only: requires a preinstalled, non-network English TTS voice.
 * Never selects a network-required TTS Voice. No feedback owns playback truth.
 */
class AndroidOfflineTextToSpeechEngine(context: Context) : TextToSpeechEngine, AutoCloseable {
    private var ready = false
    private var closed = false
    private var active: VoiceSessionId? = null
    private var serial = 0L

    private val tts = TextToSpeech(context.applicationContext) { status ->
        if (closed || status != TextToSpeech.SUCCESS) return@TextToSpeech
        // Engine callback runs after construction in Android implementations.
        val offline = tts.voices?.firstOrNull {
            !it.isNetworkConnectionRequired && it.locale.language == Locale.ENGLISH.language
        }
        if (offline != null && tts.setVoice(offline) == TextToSpeech.SUCCESS) ready = true
    }

    override fun isAvailable(): Boolean = ready && !closed

    override suspend fun speak(output: SpeechOutput): AppResult<Unit> =
        withContext(Dispatchers.Main.immediate) {
            if (!isAvailable() || output.shortText.length > VoicePolicy.MAX_SHORT_FEEDBACK_CHARS ||
                output.shortText.isBlank() || active != null) return@withContext unavailable()
            try {
                suspendCancellableCoroutine { continuation ->
                    val utteranceId = "musico-feedback-${++serial}"
                    active = output.sessionId
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(id: String?) = Unit
                        override fun onDone(id: String?) {
                            if (id == utteranceId && active == output.sessionId) {
                                active = null
                                if (continuation.isActive) continuation.resume(AppResult.Success(Unit))
                            }
                        }
                        @Deprecated("Media TTS implementation still invokes this callback")
                        override fun onError(id: String?) {
                            if (id == utteranceId && active == output.sessionId) {
                                active = null
                                if (continuation.isActive) continuation.resume(unavailable())
                            }
                        }
                    })
                    continuation.invokeOnCancellation { stop(output.sessionId) }
                    if (tts.speak(output.shortText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                        != TextToSpeech.SUCCESS) {
                        active = null
                        if (continuation.isActive) continuation.resume(unavailable())
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { unavailable() }
        }

    override fun stop(sessionId: VoiceSessionId) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            if (active == sessionId) {
                active = null
                tts.stop()
            }
        }
    }

    override fun close() {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            closed = true
            active = null
            ready = false
            tts.stop()
            tts.shutdown()
        }
    }

    private fun unavailable(): AppResult.Failure = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_UNAVAILABLE,
            Recoverability.RECOVERABLE, RecoveryAction.ManualFallback)
    )
}

