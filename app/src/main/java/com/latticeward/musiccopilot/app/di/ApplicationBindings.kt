package com.latticeward.musiccopilot.app.di

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.os.Build
import android.Manifest
import com.latticeward.musiccopilot.core.application.permission.VoiceProviderCapability
import com.latticeward.musiccopilot.core.application.voice.VoiceLifecycleSafetyPolicy
import com.latticeward.musiccopilot.core.application.voice.ObservedVoicePttCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.app.platform.bluetooth.SelectedBluetoothConnectionCoordinator
import com.latticeward.musiccopilot.app.platform.audio.CoarseBluetoothAudioOutput
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechConsentPolicy
import com.latticeward.musiccopilot.core.application.voice.CloudSpeechGateDecision
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.DiagnosticsConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.core.application.library.LibraryScanUseCase
import com.latticeward.musiccopilot.core.application.library.LibraryScanLaunchGate
import com.latticeward.musiccopilot.core.application.library.LibraryScanReason
import com.latticeward.musiccopilot.core.application.playback.LibraryMediaSelectionUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.time.Clock
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.controller.PlaybackControllerConnection
import com.latticeward.musiccopilot.quality.api.TelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
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
    // P10: foreground is not inferred from a historical permission grant.
    // Activity onStart/onStop remain the only writers; never DataStore-backed.
    @Volatile private var uiVoiceStarted = false

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

    // Issue #12 P04: separate diagnostic consent, NO audio/cloud association.
    // Always fail closed until Product/Privacy/Legal signoff. The in-memory
    // revocation latch is synchronous even before the DataStore edit completes.
    val diagnosticsCollectionAuthorization =
        QualityBindings.authorization(preferencesRepository)

    // P10: app-scoped, but Android Bluetooth callbacks exist ONLY while the
    // Activity is foreground and a user-selected saved audio token is valid.
    // This component NEVER opens a screen or issues a playback command.
    val selectedBluetoothConnection = SelectedBluetoothConnectionCoordinator(
        context, applicationScope, preferencesRepository
    )

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
        VoiceBindings.coordinator(
            voiceScope, speechToTextEngine, textToSpeechEngine,
            playbackGateway, context != null, context,
            // P10: check fresh Android OS truth on EVERY invocation,
            // including delayed STT/ACK continuations, not only on Mic tap.
            // A cached grant or a resumed Activity is never authorization.
            canBeginSession = {
                val app = context?.applicationContext
                VoiceLifecycleSafetyPolicy.canContinueWithFreshOsGrant(
                    uiStarted = uiVoiceStarted,
                    recordAudioGranted = app != null &&
                        ContextCompat.checkSelfPermission(
                            app, Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED,
                )
            },
            artistLibrary = libraryRepository,
            // P11: only typed, coarse and sanitizable values enter Quality.
            // The production sink still drops ALL events (no diagnostic
            // consent/UI or external quality transport is installed).
            // Executed asynchronously by Coordinator; it cannot block Media3.
            tryRecordVoiceOutcome = { event ->
                qualityEventPublisher.tryRecord(event)
                Unit
            },
        )

    /**
     * P09 provider probe only. This must be invoked after a fresh Android
     * RECORD_AUDIO grant; no coordinator.start(), capture, playback command,
     * language-pack download, network call or cloud provider is performed.
     */
    fun onDeviceVoicePracticeCapability(): VoiceProviderCapability =
        if (speechToTextEngine.isAvailable()) VoiceProviderCapability.OnDeviceAvailable
        else VoiceProviderCapability.ProviderUnavailable


    /**
     * P10 production is strictly local-only even for an old DataStore "Granted"
     * flag: no approved remote adapter, legal signoff or disclosure exists.
     */
    fun remoteSpeechDecision(): CloudSpeechGateDecision =
        CloudSpeechConsentPolicy.evaluate(
            remoteAdapterApproved = false,
            legalApproval = false,
            disclosure = null,
            consentGranted =
                preferencesRepository.preferences.value.cloudSpeechConsent == CloudSpeechConsent.Granted,
            drivingMode = false,
        )

    /**
     * Immediately stop diagnostics BEFORE asynchronous DataStore persistence.
     * This method cannot modify Android permissions, STT/cloud consent,
     * user music or Media3. P12 must surface persistence failures in Settings.
     */
    fun revokeDiagnosticsConsent() {
        diagnosticsCollectionAuthorization.requestImmediateRevocation()
        repositoryScope.launch {
            preferencesRepository.update(
                PreferenceUpdate.SetDiagnosticsConsent(DiagnosticsConsent.Revoked)
            )
        }
    }

    /** Revocation is a local DataStore preference, never an Android permission. */
    fun revokeCloudSpeechConsent() {
        repositoryScope.launch {
            preferencesRepository.update(PreferenceUpdate.SetCloudSpeechConsent(CloudSpeechConsent.Revoked))
        }
    }

    val telemetryPrivacyGate: TelemetryPrivacyGate = QualityBindings.privacyGate()
    val telemetrySink: TelemetrySink =
        QualityBindings.sink(diagnosticsCollectionAuthorization)
    val qualityEventPublisher = QualityBindings.publisher(
        telemetryPrivacyGate, telemetrySink, diagnosticsCollectionAuthorization
    )

    /**
     * P05: only the existing Voice owner's accepted start/retry result
     * can issue this callback. The decorator creates no new Voice session,
     * no audio work and no second authority for the recognizer.
     */
    val voiceSessionCoordinator: VoiceSessionCoordinator =
        ObservedVoicePttCoordinator(voiceOwner, ::recordAcceptedVoicePtt)

    /**
     * An accepted interaction is not evidence of user consent. In production
     * every approval flag is FALSE (P04); no event is constructed at all.
     * If a reviewed local collector is installed later, data is delivered
     * off the UI/Voice critical path and reauthorized before sink delivery.
     */
    private fun queueApprovedDiagnostic(event: TelemetryEvent) {
        try {
            applicationScope.launch(Dispatchers.Default) {
                qualityEventPublisher.tryRecord(event)
            }
        } catch (_: Exception) {
            // No Quality failure affects navigation, speech or playback.
        }
    }

    private fun recordAcceptedVoicePtt() {
        if (!diagnosticsCollectionAuthorization.canCollect()) return
        // Read ONLY the already service-authoritative coarse playback state.
        // No queue, MediaRef, track title, position or route identity is read.
        val playback = when (playbackGateway.snapshot.value.playbackState) {
            PlaybackState.Idle -> TelemetryPlaybackState.Idle
            PlaybackState.Buffering -> TelemetryPlaybackState.Buffering
            PlaybackState.Playing -> TelemetryPlaybackState.Playing
            PlaybackState.Paused -> TelemetryPlaybackState.Paused
            PlaybackState.Ended -> TelemetryPlaybackState.Ended
            PlaybackState.Error -> TelemetryPlaybackState.Error
        }
        queueApprovedDiagnostic(TelemetryEvent.VoiceButtonPressed(playback))
    }

    /** Invoked once only after DrivingNavigationViewModel accepts entry. */
    fun recordAcceptedDrivingEntry(entrySource: String) {
        if (!diagnosticsCollectionAuthorization.canCollect()) return
        // Only AudioDeviceInfo.type is read at this explicit boundary, never
        // device name/address, selected BT token, call metadata or location.
        // The v1 event needs a boolean, so unknown hardware truth means DROP.
        val connected = CoarseBluetoothAudioOutput.isConnected(mediaAppContext)
            ?: return
        queueApprovedDiagnostic(
            TelemetryEvent.DrivingModeStarted(entrySource, connected)
        )
    }

    /** Recheck permission/platform truth without blocking Main on every foreground return. */
    private val mediaAppContext = context?.applicationContext
    private val libraryScanUseCase = LibraryScanUseCase(libraryRepository) {
        val app = mediaAppContext
        if (app == null) false
        else {
            val osPermission = if (Build.VERSION.SDK_INT >= 33)
                Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(app, osPermission) ==
                PackageManager.PERMISSION_GRANTED
        }
    }
    /** P10 shared contract, not a second Player. OS permission remains source of truth. */
    val mediaSelection = LibraryMediaSelectionUseCase(
        library = libraryRepository,
        gateway = playbackGateway,
        hasOsMediaAccess = {
            val app = mediaAppContext
            app != null && ContextCompat.checkSelfPermission(
                app,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        },
    )
    private val scanLaunchGate = LibraryScanLaunchGate(repositoryScope)

    /**
     * Explicit one-use permission Continue: never called by onResume or
     * ActivityResult callback. OS permission is checked both here and by
     * LibraryScanUseCase before any MediaStore query.
     */
    fun scanAfterExplicitMediaGrant(context: Context, permissionName: String) {
        val application = context.applicationContext
        scanLaunchGate.launchExplicit {
            if (ContextCompat.checkSelfPermission(application, permissionName) ==
                PackageManager.PERMISSION_GRANTED) {
                libraryScanUseCase.execute(LibraryScanReason.AfterMediaGrant)
            } else {
                libraryRepository.invalidateMediaAccess()
            }
        }
    }

    /** Retry only from a visible recoverable error; rapid taps coalesce. */
    fun retryLibraryAfterError() {
        scanLaunchGate.launchExplicit {
            libraryScanUseCase.execute(LibraryScanReason.RetryRecoverableError)
        }
    }

    /** Never launched by bootstrap, permission callback, or foreground. */
    fun rescanLibraryOnUserRequest() {
        scanLaunchGate.launchExplicit {
            libraryScanUseCase.execute(LibraryScanReason.ExplicitRescan)
        }
    }

    /**
     * On a fresh foreground OS observation, invalidate all cached Ready/Empty
     * and cancel any scan on revoked access. Do not infer revocation from a
     * stale ActivityResult Boolean, and do not auto-scan after an OS regrant.
     */
    fun onForegroundMediaPermissionObserved(granted: Boolean) {
        if (!granted && (scanLaunchGate.isScanInFlight() ||
                libraryRepository.index.value.status !=
                com.latticeward.musiccopilot.data.api.LibraryIndexStatus.Unavailable)) {
            onMediaAccessRevoked()
        }
    }

    /** No user audio or Room rows are deleted; app index is hidden immediately. */
    fun onMediaAccessRevoked() {
        // A revoked music-library permission is a system boundary for the
        // voice-origin screen too. Do not allow pending Voice dispatch to
        // consume a queue that lost its authorized media source.
        if (VoiceLifecycleSafetyPolicy.requiresCancellationAtUiBoundary(
                voiceOwner.state.value
            )
        ) voiceOwner.cancel(VoiceCancelReason.PermissionLost)
        scanLaunchGate.cancelInFlight()
        libraryRepository.invalidateMediaAccess()
    }

    /**
     * P10: onResume/Settings permission observation invalidates an in-flight
     * STT, Confirming prompt or Media3 ACK before any newly visible UI can
     * respond. A new Android grant NEVER starts or retries Voice automatically.
     */
    fun onForegroundMicrophonePermissionObserved(granted: Boolean) {
        if (!granted && VoiceLifecycleSafetyPolicy.mustCancelOnSafetyBoundary(
                voiceOwner.state.value
            )
        ) voiceOwner.cancel(VoiceCancelReason.PermissionLost)
    }

    fun onUiStarted() {
        uiVoiceStarted = true
        selectedBluetoothConnection.onUiStarted()
    }

    fun onForeground() {
        // Activity resume reconnects to the one service-owned Session.
        // BT profile re-observation never drives UI navigation or audio.
        controller.ensureConnected()
        selectedBluetoothConnection.onForegroundPermissionObserved()
    }

    /**
     * P08 lifecycle boundary: UI cannot pin the service by remaining bound
     * after Home, rotation or task dismissal. MediaSessionService itself
     * decides whether real active audio requires ongoing foreground playback.
     */
    fun onUiStopped() {
        uiVoiceStarted = false
        // Application boundary is authoritative even if Activity callbacks
        // raced with route loss, rotation or task dismissal. Cancel FIRST,
        // while this MediaController is still connected so P07 release can
        // reach the service. Never stop or pause the service-owned Player.
        if (VoiceLifecycleSafetyPolicy.requiresCancellationAtUiBoundary(
                voiceOwner.state.value
            )
        ) voiceOwner.cancel(VoiceCancelReason.OriginInvalidated)
        selectedBluetoothConnection.onUiStopped()
        controller.detachUiController()
    }

    override fun close() {
        uiVoiceStarted = false
        selectedBluetoothConnection.close()
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
