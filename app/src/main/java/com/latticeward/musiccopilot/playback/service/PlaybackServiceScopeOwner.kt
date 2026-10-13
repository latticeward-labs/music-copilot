package com.latticeward.musiccopilot.playback.service

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Service-OWNED scope template. Instantiate only from the Phase 7 MediaSessionService lifecycle.
 * Never bind or construct this in the application graph/Activity/ViewModel.
 */
class PlaybackServiceScopeOwner(dispatchers: DispatcherProvider) : AutoCloseable {
    private val job = SupervisorJob()
    val scope = CoroutineScope(job + dispatchers.main + CoroutineName("PlaybackService"))
    override fun close() { scope.cancel() }
}

