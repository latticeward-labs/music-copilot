package com.latticeward.musiccopilot.app

import android.app.Application
import com.latticeward.musiccopilot.app.di.ApplicationBindings

/**
 * Real Android-process root. No secondary Activity/Service construction or service locator.
 * Android normally ends a process without calling onTerminate; process teardown releases the
 * jobs automatically. onTerminate is best-effort emulator/host cleanup, not a runtime guarantee.
 */
class MusicCopilotApplication : Application() {
    lateinit var bindings: ApplicationBindings
        private set

    override fun onCreate() {
        super.onCreate()
        bindings = ApplicationBindings(context = applicationContext)
    }

    override fun onTerminate() {
        if (::bindings.isInitialized) bindings.close()
        super.onTerminate()
    }
}

