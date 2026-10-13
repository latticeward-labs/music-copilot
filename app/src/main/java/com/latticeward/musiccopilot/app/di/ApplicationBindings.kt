package com.latticeward.musiccopilot.app.di

import android.content.Context
import kotlinx.coroutines.launch

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.time.Clock
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.controller.PlaybackControllerConnection
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Single composition root, constructed exactly once by MusicCopilotApplication.onCreate().
 * No Activity, ViewModel or Service creates a second application graph. No Player here.
 * Dependencies are constructor-injected/testable; binding library is an implementation choice.
 */
class ApplicationBindings(
    val dispatchers: DispatcherProvider = AndroidDispatcherProvider(),
    val clock: Clock = SystemClock(),
    context: Context? = null,
) : AutoCloseable {
    private val applicationJob = SupervisorJob()
    private val repositoryJob = SupervisorJob(applicationJob)
    private val voiceJob = SupervisorJob(applicationJob)

    internal val applicationScope = CoroutineScope(
        applicationJob + dispatchers.main + CoroutineName("ApplicationControllers")
    )
    internal val repositoryScope = CoroutineScope(
        repositoryJob + dispatchers.io + CoroutineName("Repository")
    )
    internal val voiceScope = CoroutineScope(
        voiceJob + dispatchers.main + CoroutineName("VoiceOwner")
    )

    private val controller: PlaybackControllerConnection =
        PlaybackBindings.controller(applicationScope, context)

    val playbackGateway: PlaybackGateway = controller
    private val dataComponents = DataBindings.create(context, repositoryScope, dispatchers)
    val libraryRepository: LibraryRepository = dataComponents.library
    val preferencesRepository: PreferencesRepository = dataComponents.preferences
    val restorableQueueStore: RestorableQueueStore = dataComponents.restorableQueue

    val speechToTextEngine: SpeechToTextEngine = VoiceBindings.speechToText(context, dispatchers)
    val textToSpeechEngine: TextToSpeechEngine = VoiceBindings.textToSpeech(context)

    private val voiceOwner: DefaultVoiceSessionCoordinator =
        VoiceBindings.coordinator(voiceScope, speechToTextEngine, textToSpeechEngine, playbackGateway, context != null)
    val voiceSessionCoordinator: VoiceSessionCoordinator = voiceOwner

    val telemetryPrivacyGate: TelemetryPrivacyGate = QualityBindings.privacyGate()
    val telemetrySink: TelemetrySink = QualityBindings.sink()
    val qualityEventPublisher = QualityBindings.publisher(telemetryPrivacyGate, telemetrySink)

    /** Recheck permission/platform truth without blocking Main on every foreground return. */
    fun onForeground() {
        controller.ensureConnected()
        repositoryScope.launch { libraryRepository.reconcile() }
    }

    override fun close() {
        voiceOwner.close()
        (speechToTextEngine as? AutoCloseable)?.close()
        (textToSpeechEngine as? AutoCloseable)?.close()
        controller.close()
        dataComponents.close()
        voiceJob.cancel()
        repositoryJob.cancel()
        applicationJob.cancel()
    }
}

