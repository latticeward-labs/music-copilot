package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult

/**
 * Issue #12 P10: source category of a transient endpoint marker.
 * A VAD/onEndOfSpeech CALLBACK is an endpoint SIGNAL and may occur AFTER the
 * physical last speech sample (e.g. Silero's configured silence window).
 * None of these provider categories are accepted as independently verified
 * acoustic speech-end without a separate calibrated device/QA annotation.
 */
enum class SpeechEndpointQuality { VAD_DETECTED, ANDROID_RECOGNIZER_DETECTED }

data class SpeechEndpointEvidence(
    val elapsedRealtimeNanos: Long,
    val quality: SpeechEndpointQuality,
)

data class SpeechRecognitionRequest(
    val sessionId: VoiceSessionId,
    val languageTag: String,
    val maxListeningMs: Long = 6_000L,
    // Ephemeral one-attempt callback; no ID/time is serialized or logged.
    val onEndpointSignal: ((SpeechEndpointEvidence) -> Unit)? = null,
)

/**
 * Ephemeral in-memory provider result. Data-class default toString would
 * include the user's spoken words in accidental Logcat/crash breadcrumbs.
 * Deliberately never format the transcript (nor a device/locale identifier).
 * Consumers must map this in-memory and immediately drop the reference.
 */
data class SpeechRecognitionResult(
    val transcript: String,
    val providerConfidence: Double?,
    val detectedLanguageTag: String?,
    /**
     * Explicit assurance from local VAD endpointing ONLY, not model confidence.
     * Never enabled by synthetic results or the Android recognizer by default.
     * The restricted mapper still requires an exact allowlisted English phrase.
     */
    val localVadEndpointVerified: Boolean = false,
) {
    override fun toString(): String = "SpeechRecognitionResult(redacted)"
}

/** Ephemeral results only; remote STT disabled until independent consent/provider approval. */
interface SpeechToTextEngine {
    fun isAvailable(): Boolean
    suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult>
    fun cancel(sessionId: VoiceSessionId)
}
