package com.latticeward.musiccopilot.app

import android.app.Application
import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.app.di.PlaybackBindings
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.playback.api.PlaybackMediaResolutionPort
import com.latticeward.musiccopilot.playback.api.PlaybackResolverHost
import com.latticeward.musiccopilot.playback.api.PlaybackVoiceInterruptionHost
import com.latticeward.musiccopilot.playback.api.PlaybackVoiceInterruptionReason
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import kotlinx.coroutines.CoroutineScope

/**
 * Real Android-process root. No secondary Activity/Service construction or service locator.
 * Android normally ends a process without calling onTerminate; process teardown releases the
 * jobs automatically. onTerminate is best-effort emulator/host cleanup, not a runtime guarantee.
 */
class MusicCopilotApplication : Application(),
    PlaybackResolverHost, PlaybackVoiceInterruptionHost {
    lateinit var bindings: ApplicationBindings
        private set

    override fun onCreate() {
        super.onCreate()
        bindings = ApplicationBindings(context = applicationContext)
    }

    override fun createPlaybackMediaResolver(
        dispatchers: DispatcherProvider,
    ): PlaybackMediaResolutionPort =
        PlaybackBindings.playbackMediaResolver(applicationContext, dispatchers)

    override fun createPlaybackRestorableQueueStore(): RestorableQueueStore =
        bindings.restorableQueueStore

    override fun playbackCheckpointWriteScope(): CoroutineScope =
        bindings.applicationScope

    /**
     * P09: route safety signals from the ONE playback service to the ONE
     * app-scoped VoiceSessionCoordinator. No service-owned STT/TTS or
     * extra coordinator. Callbacks are delivered on service/Main; keep
     * cancellation synchronous so late STT/TTS/confirmation generations
     * are invalidated before a queued media command can resume.
     */
    override fun cancelActiveVoiceForPlaybackInterruption(
        reason: PlaybackVoiceInterruptionReason,
    ) {
        if (!::bindings.isInitialized) return
        val voiceReason = when (reason) {
            PlaybackVoiceInterruptionReason.FOCUS_INVALIDATED ->
                VoiceCancelReason.CallOrFocusInvalidated
            PlaybackVoiceInterruptionReason.ROUTE_INVALIDATED ->
                VoiceCancelReason.RouteInvalidated
        }
        bindings.voiceSessionCoordinator.cancel(voiceReason)
    }

    override fun onTerminate() {
        if (::bindings.isInitialized) bindings.close()
        super.onTerminate()
    }
}
