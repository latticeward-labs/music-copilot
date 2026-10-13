package com.latticeward.musiccopilot.app.di

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
        PlaybackBindings.controller(applicationScope)

    val playbackGateway: PlaybackGateway = controller
    val libraryRepository: LibraryRepository = DataBindings.library(repositoryScope)
    val preferencesRepository: PreferencesRepository = DataBindings.preferences(repositoryScope)
    val restorableQueueStore: RestorableQueueStore = DataBindings.restorableQueue(repositoryScope)

    val speechToTextEngine: SpeechToTextEngine = VoiceBindings.speechToText()
    val textToSpeechEngine: TextToSpeechEngine = VoiceBindings.textToSpeech()

    private val voiceOwner: DefaultVoiceSessionCoordinator =
        VoiceBindings.coordinator(voiceScope, speechToTextEngine, textToSpeechEngine)
    val voiceSessionCoordinator: VoiceSessionCoordinator = voiceOwner

    val telemetryPrivacyGate: TelemetryPrivacyGate = QualityBindings.privacyGate()
    val telemetrySink: TelemetrySink = QualityBindings.sink()

    override fun close() {
        voiceOwner.close()
        controller.close()
        voiceJob.cancel()
        repositoryJob.cancel()
        applicationJob.cancel()
    }
}

