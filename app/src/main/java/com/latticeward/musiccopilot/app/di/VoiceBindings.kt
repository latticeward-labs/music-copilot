package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.voice.android.UnavailableSpeechToTextEngine
import com.latticeward.musiccopilot.voice.android.UnavailableTextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CoroutineScope

/** Phase 5 installs one Voice owner, without activating microphone or remote STT. */
internal object VoiceBindings {
    fun speechToText(): SpeechToTextEngine = UnavailableSpeechToTextEngine()
    fun textToSpeech(): TextToSpeechEngine = UnavailableTextToSpeechEngine()
    fun coordinator(
        scope: CoroutineScope, stt: SpeechToTextEngine, tts: TextToSpeechEngine,
    ): DefaultVoiceSessionCoordinator = DefaultVoiceSessionCoordinator(scope, stt, tts)
}

