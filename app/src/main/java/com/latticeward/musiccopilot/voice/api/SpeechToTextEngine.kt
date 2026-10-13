package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult

data class SpeechRecognitionRequest(
    val sessionId: VoiceSessionId,
    val languageTag: String,
    val maxListeningMs: Long = 6_000L,
)

data class SpeechRecognitionResult(
    val transcript: String,
    val providerConfidence: Double?,
    val detectedLanguageTag: String?,
)

/** Ephemeral results only; remote STT disabled until independent consent/provider approval. */
interface SpeechToTextEngine {
    fun isAvailable(): Boolean
    suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult>
    fun cancel(sessionId: VoiceSessionId)
}

