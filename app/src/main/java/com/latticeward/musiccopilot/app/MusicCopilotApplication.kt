package com.latticeward.musiccopilot.app

import android.app.Application
import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.app.di.PlaybackBindings
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.playback.api.PlaybackMediaResolutionPort
import com.latticeward.musiccopilot.playback.api.PlaybackResolverHost

/**
 * Real Android-process root. No secondary Activity/Service construction or service locator.
 * Android normally ends a process without calling onTerminate; process teardown releases the
 * jobs automatically. onTerminate is best-effort emulator/host cleanup, not a runtime guarantee.
 */
class MusicCopilotApplication : Application(), PlaybackResolverHost {
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

    override fun onTerminate() {
        if (::bindings.isInitialized) bindings.close()
        super.onTerminate()
    }
}
