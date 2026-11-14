package com.latticeward.musiccopilot.voice.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineMoonshineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.SileroVadModelConfig
import com.k2fsa.sherpa.onnx.Vad
import com.k2fsa.sherpa.onnx.VadModelConfig
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
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Issue #10: fully local replacement for the OEM-dependent Android recognizer.
 *
 * Owns ONLY the microphone, VAD and local transcription; it never maps or
 * executes a playback command. No Internet, network model fetch, background
 * listening, raw-audio storage, transcript logging or persistence.
 *
 * Silero detects the end of an utterance; Moonshine decodes its finite segment.
 * The coordinator retains the authoritative 6s deadline, retry and duck lease.
 */
internal class MoonshineSileroSpeechToTextEngine(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : SpeechToTextEngine, AutoCloseable {
    private val app = context.applicationContext
    private val bootstrap = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadMutex = Mutex()
    private val sessionLock = Any()
    @Volatile private var model: OfflineRecognizer? = null
    @Volatile private var modelBroken = false
    @Volatile private var closed = false
    @Volatile private var active: Session? = null

    private class Session(val id: VoiceSessionId) {
        val cancelled = AtomicBoolean(false)
        @Volatile var recorder: AudioRecord? = null
        fun cancel() {
            cancelled.set(true)
            // AudioRecord.read is bounded to a 32ms frame. Releasing the
            // recorder is done by its capture thread, never from UI/Main.
        }
    }

    init {
        // Cold model initialization must never stall app startup or Compose.
        // A fresh explicit mic tap may also initialize it on the IO dispatcher.
        bootstrap.launch { runCatching { loadModel() } }
    }

    override fun isAvailable(): Boolean {
        if (closed || modelBroken || Build.VERSION.SDK_INT < 29 ||
            !Build.SUPPORTED_ABIS.contains("arm64-v8a")) return false
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED) return false
        return try {
            val modelFiles = app.assets.list(MODEL_DIR).orEmpty()
            val root = app.assets.list("").orEmpty()
            modelFiles.contains("encoder_model.ort") &&
                modelFiles.contains("decoder_model_merged.ort") &&
                modelFiles.contains("tokens.txt") && root.contains("silero_vad.onnx")
        } catch (_: Exception) { false }
    }

    override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> {
        if (!isAvailable() || !request.languageTag.startsWith("en", ignoreCase = true) ||
            request.maxListeningMs !in 1..VoicePolicy.LISTEN_TIMEOUT_MS) return failure(
            AppErrorCodes.VOICE_UNAVAILABLE
        )

        val s = Session(request.sessionId)
        synchronized(sessionLock) {
            if (active != null || closed) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
            active = s
        }
        return try {
            withContext(dispatchers.io) {
                val engine = loadModel()
                if (s.cancelled.get()) return@withContext failure(AppErrorCodes.VOICE_UNAVAILABLE)
                captureAndRecognize(s, request, engine)
            }
        } catch (cancelled: CancellationException) {
            s.cancel()
            throw cancelled
        } catch (_: Exception) {
            failure(AppErrorCodes.VOICE_UNAVAILABLE)
        } finally {
            s.cancel()
            synchronized(sessionLock) { if (active === s) active = null }
        }
    }

    private suspend fun loadModel(): OfflineRecognizer = loadMutex.withLock {
        if (closed) throw IllegalStateException("voice provider closed")
        model?.let { return@withLock it }
        try {
            val config = OfflineRecognizerConfig(
                featConfig = FeatureConfig(sampleRate = RATE, featureDim = 80),
                modelConfig = OfflineModelConfig(
                    moonshine = OfflineMoonshineModelConfig(
                        encoder = "$MODEL_DIR/encoder_model.ort",
                        mergedDecoder = "$MODEL_DIR/decoder_model_merged.ort",
                    ),
                    tokens = "$MODEL_DIR/tokens.txt",
                    numThreads = 2,
                    debug = false,
                ),
            )
            val loaded = OfflineRecognizer(assetManager = app.assets, config = config)
            if (closed) {
                loaded.release()
                throw IllegalStateException("voice provider closed")
            }
            model = loaded
            loaded
        } catch (failure: Exception) {
            modelBroken = true
            throw failure
        }
    }

    private suspend fun captureAndRecognize(
        session: Session,
        request: SpeechRecognitionRequest,
        engine: OfflineRecognizer,
    ): AppResult<SpeechRecognitionResult> {
        // Limit microphone capture to leave some of the coordinator's 6s
        // budget for the on-device decoder. A slow decode still fails closed.
        val maxCaptureMs = minOf(request.maxListeningMs - 750L, 4_500L)
        if (maxCaptureMs <= 0) return failure(AppErrorCodes.VOICE_TIMEOUT)
        val vadConfig = VadModelConfig(
            sileroVadModelConfig = SileroVadModelConfig(
                model = "silero_vad.onnx",
                threshold = 0.5f,
                minSilenceDuration = 0.55f,
                minSpeechDuration = 0.18f,
                windowSize = FRAME,
                maxSpeechDuration = 5.0f,
            ),
            sampleRate = RATE,
            numThreads = 1,
            provider = "cpu",
        )
        var detector: Vad? = null
        var recorder: AudioRecord? = null
        try {
            currentCoroutineContext().ensureActive()
            detector = Vad(assetManager = app.assets, config = vadConfig)
            val minBytes = AudioRecord.getMinBufferSize(
                RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            )
            if (minBytes <= 0) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
            recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION, RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                max(minBytes * 2, FRAME * 8),
            )
            if (recorder.state != AudioRecord.STATE_INITIALIZED)
                return failure(AppErrorCodes.VOICE_UNAVAILABLE)
            session.recorder = recorder
            recorder.startRecording()
            if (recorder.recordingState != AudioRecord.RECORDSTATE_RECORDING)
                return failure(AppErrorCodes.VOICE_UNAVAILABLE)

            val start = android.os.SystemClock.elapsedRealtime()
            val samples = ShortArray(FRAME)
            var speechStarted = false
            var completed = false
            while (!session.cancelled.get()) {
                currentCoroutineContext().ensureActive()
                val elapsed = android.os.SystemClock.elapsedRealtime() - start
                if (elapsed >= maxCaptureMs) break
                // A silent tap exits early and does not pin the microphone.
                if (!speechStarted && elapsed >= 3_500L) break
                var read = 0
                while (read < FRAME && !session.cancelled.get()) {
                    currentCoroutineContext().ensureActive()
                    val count = recorder.read(
                        samples, read, FRAME - read, AudioRecord.READ_BLOCKING,
                    )
                    if (count <= 0) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
                    read += count
                }
                if (read != FRAME || session.cancelled.get()) break
                detector.acceptWaveform(FloatArray(FRAME) { i -> samples[i] / 32768.0f })
                if (detector.isSpeechDetected()) speechStarted = true
                if (!detector.empty()) {
                    completed = true
                    break
                }
            }

            recorder.stop()
            recorder.release()
            recorder = null
            session.recorder = null
            if (session.cancelled.get()) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
            currentCoroutineContext().ensureActive()

            if (!completed && speechStarted) detector.flush()
            if (detector.empty()) return failure(
                if (speechStarted) AppErrorCodes.VOICE_NO_MATCH else AppErrorCodes.VOICE_TIMEOUT
            )
            val utterance = detector.front().samples
            detector.pop()
            if (utterance.size < RATE / 6 || utterance.size > RATE * 6)
                return failure(AppErrorCodes.VOICE_NO_MATCH)

            // Offline ASR is strictly downstream of the VAD endpoint.
            val stream = engine.createStream()
            try {
                stream.acceptWaveform(utterance, RATE)
                currentCoroutineContext().ensureActive()
                engine.decode(stream)
                currentCoroutineContext().ensureActive()
                val text = engine.getResult(stream).text.trim()
                if (text.isBlank() || text.length > 120) return failure(AppErrorCodes.VOICE_NO_MATCH)
                return AppResult.Success(
                    SpeechRecognitionResult(
                        transcript = text,
                        providerConfidence = null, // Moonshine does NOT provide a calibrated score.
                        detectedLanguageTag = null,
                        localVadEndpointVerified = completed,
                    )
                )
            } finally {
                stream.release()
            }
        } finally {
            session.recorder = null
            if (recorder != null) {
                try { if (recorder.recordingState == AudioRecord.RECORDSTATE_RECORDING) recorder.stop() }
                catch (_: Exception) { }
                try { recorder.release() } catch (_: Exception) { }
            }
            try { detector?.release() } catch (_: Exception) { }
        }
    }

    override fun cancel(sessionId: VoiceSessionId) {
        active?.takeIf { it.id == sessionId }?.cancel()
    }

    override fun close() {
        closed = true
        active?.cancel()
        bootstrap.cancel()
        // Native recognizer is process-scoped. Do not release concurrently
        // with a native decode still running on the provider's IO thread.
    }

    private fun failure(code: String): AppResult.Failure = AppResult.Failure(
        AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
    )

    private companion object {
        const val MODEL_DIR = "sherpa-onnx-moonshine-tiny-en-quantized-2026-02-27"
        const val RATE = 16_000
        const val FRAME = 512
    }
}
