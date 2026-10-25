package com.latticeward.musiccopilot.voice.android

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * ADR-0004: replaceable, optional offline TTS adapter. It does not own playback
 * or touch recognition. Async callbacks (including OEM onInit callbacks) are
 * serialized on Main to avoid late listener callbacks reviving a closed engine.
 */
class AndroidOfflineTextToSpeechEngine(context: Context) : TextToSpeechEngine, AutoCloseable {
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var ready = false
    @Volatile private var closed = false
    private var active: VoiceSessionId? = null
    private var activeUtteranceId: String? = null
    private var serial = 0L
    private var tts: TextToSpeech? = null

    init {
        // Posting the callback also handles OEMs that invoke onInit before
        // constructor assignment to 'tts' has completed.
        tts = TextToSpeech(context.applicationContext) { status ->
            mainHandler.post {
                if (!closed && status == TextToSpeech.SUCCESS) prepareOfflineVoice()
            }
        }
    }

    /** Called only on the main Looper after the engine reference is assigned. */
    private fun prepareOfflineVoice() {
        if (closed) return
        val engine = tts ?: return
        ready = false
        try {
            val installed = engine.voices.orEmpty().filter {
                !it.isNetworkConnectionRequired &&
                    it.locale.language == Locale.ENGLISH.language
            }
            // Prefer matching command locale, then generic installed English.
            val candidate = installed.firstOrNull {
                it.locale.toLanguageTag().equals("en-US", ignoreCase = true)
            } ?: installed.firstOrNull()
            if (candidate != null && engine.setVoice(candidate) == TextToSpeech.SUCCESS)
                ready = true
        } catch (_: Exception) {
            ready = false // no voice: visual feedback/manual controls remain
        }
    }

    override fun isAvailable(): Boolean = ready && !closed

    override suspend fun speak(output: SpeechOutput): AppResult<Unit> =
        withContext(Dispatchers.Main.immediate) {
            if (!isAvailable() ||
                !VoiceFeedbackPolicy.isShortSentence(output.shortText) ||
                active != null
            ) return@withContext unavailable()
            val engine = tts ?: return@withContext unavailable()
            try {
                suspendCancellableCoroutine<AppResult<Unit>> { continuation ->
                    val utteranceId = "musico-feedback-${++serial}"
                    active = output.sessionId
                    activeUtteranceId = utteranceId

                    fun complete(response: AppResult<Unit>) {
                        onMain {
                            if (active != output.sessionId ||
                                activeUtteranceId != utteranceId) return@onMain
                            active = null
                            activeUtteranceId = null
                            if (continuation.isActive) continuation.resume(response)
                        }
                    }

                    try {
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(id: String?) = Unit
                            override fun onDone(id: String?) {
                                if (id == utteranceId) complete(AppResult.Success(Unit))
                            }
                            @Deprecated("Some TTS implementations invoke this overload")
                            override fun onError(id: String?) {
                                if (id == utteranceId) complete(unavailable())
                            }
                            override fun onError(id: String?, errorCode: Int) {
                                if (id == utteranceId) complete(unavailable())
                            }
                            override fun onStop(id: String?, interrupted: Boolean) {
                                if (id == utteranceId) complete(unavailable())
                            }
                        })
                        continuation.invokeOnCancellation { stop(output.sessionId) }
                        if (engine.speak(
                                output.shortText, TextToSpeech.QUEUE_FLUSH, null, utteranceId
                            ) != TextToSpeech.SUCCESS
                        ) complete(unavailable())
                    } catch (_: Exception) { complete(unavailable()) }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { unavailable() }
        }

    override fun stop(sessionId: VoiceSessionId) {
        onMain {
            if (active == sessionId) {
                active = null
                activeUtteranceId = null
                try { tts?.stop() } catch (_: Exception) { }
            }
        }
    }

    override fun close() {
        closed = true
        ready = false
        onMain {
            active = null
            activeUtteranceId = null
            val engine = tts
            tts = null
            try { engine?.stop() } catch (_: Exception) { }
            try { engine?.shutdown() } catch (_: Exception) { }
        }
    }

    private fun onMain(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) action()
        else mainHandler.post(action)
    }

    private fun unavailable(): AppResult.Failure = AppResult.Failure(
        AppError.Voice(
            AppErrorCodes.VOICE_UNAVAILABLE,
            Recoverability.RECOVERABLE,
            RecoveryAction.ManualFallback
        )
    )
}
