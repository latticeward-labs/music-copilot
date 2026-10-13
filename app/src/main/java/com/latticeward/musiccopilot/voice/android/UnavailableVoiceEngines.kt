package com.latticeward.musiccopilot.voice.android

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

/** No microphone access, provider SDK or network call during Phase 5. */
class UnavailableSpeechToTextEngine : SpeechToTextEngine {
    override fun isAvailable() = false
    override suspend fun recognize(request: SpeechRecognitionRequest):
        AppResult<SpeechRecognitionResult> = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_UNAVAILABLE,
            Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
    )
    override fun cancel(sessionId: VoiceSessionId) = Unit
}

/** No platform TTS initialization until Phase 8. */
class UnavailableTextToSpeechEngine : TextToSpeechEngine {
    override fun isAvailable() = false
    override suspend fun speak(output: SpeechOutput): AppResult<Unit> =
        AppResult.Failure(AppError.Voice(AppErrorCodes.VOICE_UNAVAILABLE,
            Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback))
    override fun stop(sessionId: VoiceSessionId) = Unit
}

