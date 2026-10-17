package com.latticeward.musiccopilot.app.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    enum class StorageStartState { Preparing, Ready, Failed }
    private val storageMutable = MutableStateFlow(StorageStartState.Preparing)
    val storageStartState: StateFlow<StorageStartState> = storageMutable

    init { retryStoragePreparation() }

    fun retryStoragePreparation() {
        storageMutable.value = StorageStartState.Preparing
        repositoryScope.launch {
            storageMutable.value = if (dataComponents.verifyStorage(dispatchers)) {
                StorageStartState.Ready
            } else {
                StorageStartState.Failed
            }
        }
    }

    fun retryFirstLaunch() {
        retryStoragePreparation()
        repositoryScope.launch { preferencesRepository.reload() }
    }

    val speechToTextEngine: SpeechToTextEngine = VoiceBindings.speechToText(context, dispatchers)
    val textToSpeechEngine: TextToSpeechEngine = VoiceBindings.textToSpeech(context)

    private val voiceOwner: DefaultVoiceSessionCoordinator =
        VoiceBindings.coordinator(voiceScope, speechToTextEngine, textToSpeechEngine, playbackGateway, context != null)
    val voiceSessionCoordinator: VoiceSessionCoordinator = voiceOwner

    val telemetryPrivacyGate: TelemetryPrivacyGate = QualityBindings.privacyGate()
    val telemetrySink: TelemetrySink = QualityBindings.sink()
    val qualityEventPublisher = QualityBindings.publisher(telemetryPrivacyGate, telemetrySink)

    /** Recheck permission/platform truth without blocking Main on every foreground return. */
    private val consentedScanMutex = Mutex()

    /** Never invoked from bootstrap/resume; only a one-shot P05 ActivityResult grant. */
    fun scanAfterExplicitMediaGrant(context: Context, permissionName: String) {
        val application = context.applicationContext
        repositoryScope.launch {
            consentedScanMutex.withLock {
                // Fresh Android grant check immediately before the real scanner.
                // RoomLibraryRepository and MediaStoreScanner each check it again.
                if (ContextCompat.checkSelfPermission(application, permissionName) ==
                    PackageManager.PERMISSION_GRANTED) {
                    libraryRepository.reconcile()
                }
            }
        }
    }

    /** Invalidate the public library projection immediately; no auto scan or playback takeover. */
    fun onMediaAccessRevoked() {
        libraryRepository.invalidateMediaAccess()
    }

    fun onForeground() {
        controller.ensureConnected()
        // P05/P08 must authorize explicit OS media permission before scan.
        // Bootstrap/return-to-foreground MUST NOT initiate a MediaStore scan.
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
