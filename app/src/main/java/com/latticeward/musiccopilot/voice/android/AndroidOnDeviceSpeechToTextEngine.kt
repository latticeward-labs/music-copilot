package com.latticeward.musiccopilot.voice.android

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
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
import com.latticeward.musiccopilot.voice.policy.EnglishModelReadiness
import com.latticeward.musiccopilot.voice.policy.OnDeviceEnglishSupportPolicy
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * ADR-0004: explicitly on-device recognizer only. API 29/30 are manual-only.
 * An installed English pack is checked via RecognitionSupport on API 33+.
 * On API 31/32 Android provides no support-list API: the on-device recognizer
 * is best effort, and real language readiness is not claimed ahead of time.
 * Never create the generic/cloud recognizer; never trigger a model download.
 */
class AndroidOnDeviceSpeechToTextEngine(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : SpeechToTextEngine, AutoCloseable {
    private val app = context.applicationContext
    private val mainHandler = Handler(app.mainLooper)
    private var live: Session? = null
    @Volatile private var closed = false

    private class Session(
        val id: VoiceSessionId,
        val recognizer: SpeechRecognizer,
    )

    /**
     * This is only a fast structural/permission gate; it does NOT claim a
     * locale pack is installed or that recognition succeeds on the OEM.
     * Actual English support is validated before startListening on API 33+.
     */
    override fun isAvailable(): Boolean {
        if (closed || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) return false
        return try { SpeechRecognizer.isOnDeviceRecognitionAvailable(app) }
        catch (_: Exception) { false }
    }

    override suspend fun recognize(
        request: SpeechRecognitionRequest,
    ): AppResult<SpeechRecognitionResult> = withContext(dispatchers.main) {
        if (!isAvailable() || live != null ||
            !OnDeviceEnglishSupportPolicy.isSupportedRequest(request.languageTag) ||
            request.maxListeningMs !in 1..VoicePolicy.LISTEN_TIMEOUT_MS
        ) return@withContext unavailable()

        try {
            suspendCancellableCoroutine<AppResult<SpeechRecognitionResult>> { continuation ->
                val recognizer = try {
                    // Inline SDK guard is needed for Android 10/11 Lint and OEM safety.
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        continuation.resume(unavailable())
                        return@suspendCancellableCoroutine
                    }
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(app)
                } catch (_: Exception) {
                    continuation.resume(unavailable())
                    return@suspendCancellableCoroutine
                }

                val session = Session(request.sessionId, recognizer)
                live = session
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, request.languageTag)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }

                // All session mutations, recognition callbacks and destruction
                // are serialized on the Android main Looper.
                fun teardown(cancelFirst: Boolean) {
                    if (live !== session) return
                    live = null
                    try {
                        if (cancelFirst) recognizer.cancel()
                    } catch (_: Exception) { /* preserve manual fallback */ }
                    try { recognizer.destroy() } catch (_: Exception) { }
                }

                fun finish(value: AppResult<SpeechRecognitionResult>) {
                    if (live !== session) return
                    teardown(cancelFirst = false)
                    if (continuation.isActive) continuation.resume(value)
                }

                continuation.invokeOnCancellation {
                    mainHandler.post { teardown(cancelFirst = true) }
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
                        val code = when (error) {
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> AppErrorCodes.VOICE_TIMEOUT
                            SpeechRecognizer.ERROR_NO_MATCH -> AppErrorCodes.VOICE_NO_MATCH
                            else -> AppErrorCodes.VOICE_UNAVAILABLE
                        }
                        finish(failure(code))
                    }
                    override fun onResults(results: Bundle?) {
                        // Do not persist transcript or use a requested locale
                        // as fabricated DETECTED language evidence.
                        val transcript = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()?.trim()
                        if (transcript.isNullOrBlank() || transcript.length > 256) {
                            finish(failure(AppErrorCodes.VOICE_NO_MATCH))
                            return
                        }
                        val confidence = results
                            .getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                            ?.firstOrNull()?.toDouble()
                            ?.takeIf { it.isFinite() && it in 0.0..1.0 }
                        finish(AppResult.Success(SpeechRecognitionResult(
                            transcript = transcript,
                            providerConfidence = confidence,
                            detectedLanguageTag = null, // unavailable unless platform proves it
                        )))
                    }
                })

                fun startListening() {
                    if (live !== session || !continuation.isActive) return
                    if (!isAvailable()) {
                        finish(unavailable())
                        return
                    }
                    try { recognizer.startListening(intent) }
                    catch (_: Exception) { finish(unavailable()) }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    try {
                        // Android 13+ provider reports installed vs downloadable
                        // language models. We do NOT request downloads or network.
                        recognizer.checkRecognitionSupport(
                            intent,
                            app.mainExecutor,
                            object : RecognitionSupportCallback {
                                override fun onSupportResult(support: RecognitionSupport) {
                                    if (live !== session || !continuation.isActive) return
                                    val readiness = OnDeviceEnglishSupportPolicy.evaluate(
                                        request.languageTag,
                                        installed = support.installedOnDeviceLanguages,
                                        downloadable = support.supportedOnDeviceLanguages,
                                        pending = support.pendingOnDeviceLanguages,
                                    )
                                    if (readiness == EnglishModelReadiness.Installed)
                                        startListening()
                                    else finish(unavailable())
                                }

                                override fun onError(error: Int) {
                                    // Provider cannot confirm English availability:
                                    // fail closed and retain manual music controls.
                                    finish(unavailable())
                                }
                            },
                        )
                    } catch (_: Exception) { finish(unavailable()) }
                } else {
                    // API 31/32: on-device constructor is mandatory, but there
                    // is no API to prove that English is installed beforehand.
                    startListening()
                }
            }
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { unavailable() }
    }

    override fun cancel(sessionId: VoiceSessionId) {
        onMain {
            val current = live
            if (current?.id == sessionId) {
                live = null
                try { current.recognizer.cancel() } catch (_: Exception) { }
                try { current.recognizer.destroy() } catch (_: Exception) { }
            }
        }
    }

    override fun close() {
        closed = true
        onMain {
            val current = live
            live = null
            if (current != null) {
                try { current.recognizer.cancel() } catch (_: Exception) { }
                try { current.recognizer.destroy() } catch (_: Exception) { }
            }
        }
    }

    private fun onMain(action: () -> Unit) {
        if (Looper.myLooper() == app.mainLooper) action() else mainHandler.post(action)
    }

    private fun unavailable() = failure(AppErrorCodes.VOICE_UNAVAILABLE)

    private fun failure(code: String): AppResult.Failure = AppResult.Failure(
        AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback)
    )
}
