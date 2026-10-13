package com.latticeward.musiccopilot.voice.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Explicit on-device recognizer ONLY (Android 12/API31+). Never construct a
 * generic SpeechRecognizer that may silently use a remote recognition service.
 * Android 10/11 and unsupported OEMs fail closed to manual playback.
 */
class AndroidOnDeviceSpeechToTextEngine(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : SpeechToTextEngine, AutoCloseable {
    private val app = context.applicationContext
    private var live: Session? = null

    private data class Session(val id: VoiceSessionId, val recognizer: SpeechRecognizer)

    override fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < 31) return false
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) return false
        return try { SpeechRecognizer.isOnDeviceRecognitionAvailable(app) }
        catch (_: Exception) { false }
    }

    override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> =
        withContext(dispatchers.main) {
            if (!isAvailable()) return@withContext unavailable()
            if (live != null) return@withContext unavailable()
            if (request.maxListeningMs !in 1..VoicePolicy.LISTEN_TIMEOUT_MS)
                return@withContext unavailable()
            try {
                suspendCancellableCoroutine<AppResult<SpeechRecognitionResult>> { continuation ->
                    val recognizer = try {
                        SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
                    } catch (_: Exception) {
                        continuation.resume(unavailable())
                        return@suspendCancellableCoroutine
                    }
                    val session = Session(request.sessionId, recognizer)
                    live = session

                    fun finish(value: AppResult<SpeechRecognitionResult>) {
                        if (live !== session) return
                        live = null
                        recognizer.destroy()
                        if (continuation.isActive) continuation.resume(value)
                    }

                    recognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) = Unit
                        override fun onBeginningOfSpeech() = Unit
                        override fun onRmsChanged(rmsdB: Float) = Unit
                        override fun onBufferReceived(buffer: ByteArray?) = Unit
                        override fun onEndOfSpeech() = Unit
                        override fun onEvent(eventType: Int, params: Bundle?) = Unit
                        override fun onPartialResults(partialResults: Bundle?) = Unit
                        override fun onError(error: Int) {
                            val code = if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                                error == SpeechRecognizer.ERROR_NO_MATCH)
                                AppErrorCodes.VOICE_TIMEOUT else AppErrorCodes.VOICE_UNAVAILABLE
                            finish(failure(code))
                        }
                        override fun onResults(results: Bundle?) {
                            val words = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val transcript = words?.firstOrNull()?.trim()
                            if (transcript.isNullOrBlank()) {
                                finish(failure(AppErrorCodes.VOICE_NO_MATCH))
                                return
                            }
                            val rawConfidence = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                                ?.firstOrNull()?.toDouble()
                            val confidence = rawConfidence?.takeIf { it.isFinite() && it in 0.0..1.0 }
                            finish(AppResult.Success(SpeechRecognitionResult(
                                transcript, confidence, request.languageTag
                            )))
                        }
                    })
                    continuation.invokeOnCancellation {
                        // Cancellation callback may run from another thread; schedule
                        // recognizer teardown on the Android main dispatcher.
                        android.os.Handler(app.mainLooper).post {
                            if (live === session) {
                                live = null
                                recognizer.cancel()
                                recognizer.destroy()
                            }
                        }
                    }
                    try {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, request.languageTag)
                            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        }
                        recognizer.startListening(intent)
                    } catch (_: Exception) {
                        finish(unavailable())
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { unavailable() }
        }

    override fun cancel(sessionId: VoiceSessionId) {
        onMain {
            val active = live
            if (active != null && active.id == sessionId) {
                live = null
                active.recognizer.cancel()
                active.recognizer.destroy()
            }
        }
    }

    override fun close() {
        onMain {
            val active = live
            live = null
            active?.recognizer?.cancel()
            active?.recognizer?.destroy()
        }
    }

    private fun onMain(action: () -> Unit) {
        if (android.os.Looper.myLooper() == app.mainLooper) action()
        else android.os.Handler(app.mainLooper).post(action)
    }

    private fun unavailable(): AppResult.Failure = failure(AppErrorCodes.VOICE_UNAVAILABLE)

    private fun failure(code: String) = AppResult.Failure(
        AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback)
    )
}

