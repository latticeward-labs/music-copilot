package com.latticeward.musiccopilot.voice.api

import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult

data class SpeechOutput(
    val sessionId: VoiceSessionId,
    val textKey: String,
    val shortText: String,
)

/** Feedback only. A failed TTS must never undo acknowledged playback. */
interface TextToSpeechEngine {
    fun isAvailable(): Boolean
    suspend fun speak(output: SpeechOutput): AppResult<Unit>
    fun stop(sessionId: VoiceSessionId)
}
