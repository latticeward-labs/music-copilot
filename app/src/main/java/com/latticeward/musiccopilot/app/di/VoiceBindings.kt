package com.latticeward.musiccopilot.app.di

import android.content.Context
import com.latticeward.musiccopilot.BuildConfig
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.VoiceDuckingPort
import com.latticeward.musiccopilot.playback.api.UnavailableVoiceDuckingPort
import com.latticeward.musiccopilot.voice.android.AndroidOfflineTextToSpeechEngine
import com.latticeward.musiccopilot.voice.android.AndroidVoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.EnglishVoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.android.MoonshineSileroSpeechToTextEngine
import com.latticeward.musiccopilot.voice.android.UnavailableSpeechToTextEngine
import com.latticeward.musiccopilot.voice.android.UnavailableTextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import kotlinx.coroutines.CoroutineScope

/**
 * Production selects ONLY bundled local Moonshine + Silero, otherwise fail
 * closed to manual. Tests with no Context retain synthetic disabled providers.
 * No remote provider registration, cloud key, streaming, or network fallback.
 */
internal object VoiceBindings {
    fun speechToText(context: Context?, dispatchers: DispatcherProvider): SpeechToTextEngine =
        if (context == null) UnavailableSpeechToTextEngine()
        else MoonshineSileroSpeechToTextEngine(context, dispatchers)

    fun textToSpeech(context: Context?): TextToSpeechEngine =
        if (context == null) UnavailableTextToSpeechEngine()
        else AndroidOfflineTextToSpeechEngine(context)

    fun coordinator(
        scope: CoroutineScope, stt: SpeechToTextEngine, tts: TextToSpeechEngine,
        playback: PlaybackGateway, enabled: Boolean,
        context: Context? = null,
        canBeginSession: () -> Boolean = { true },
    ): DefaultVoiceSessionCoordinator = DefaultVoiceSessionCoordinator(
        scope, stt, tts, playback = playback,
        intentMapper = RestrictedVoiceIntentMapper(
            BuildConfig.VOICE_DIRECT_CONFIDENCE_THRESHOLD
        ),
        recognitionEnabled = enabled,
        // No fake success in production: if this app lost its Media3
        // controller, STT fails closed and manual Player remains usable.
        ducking = (playback as? VoiceDuckingPort) ?: UnavailableVoiceDuckingPort,
        feedbackCatalog = if (context == null) EnglishVoiceFeedbackCatalog
            else AndroidVoiceFeedbackCatalog(context),
        canBeginSession = canBeginSession,
    )
}
