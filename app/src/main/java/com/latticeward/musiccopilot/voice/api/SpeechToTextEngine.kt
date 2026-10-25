package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult

data class SpeechRecognitionRequest(
    val sessionId: VoiceSessionId,
    val languageTag: String,
    val maxListeningMs: Long = 6_000L,
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
) {
    override fun toString(): String = "SpeechRecognitionResult(redacted)"
}

/** Ephemeral results only; remote STT disabled until independent consent/provider approval. */
interface SpeechToTextEngine {
    fun isAvailable(): Boolean
    suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult>
    fun cancel(sessionId: VoiceSessionId)
}
