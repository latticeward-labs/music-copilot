package com.latticeward.musiccopilot.app.di

import android.content.Context
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.mediastore.AndroidMediaStorePlaybackProbe
import com.latticeward.musiccopilot.data.mediastore.MediaStorePlaybackResolver
import com.latticeward.musiccopilot.playback.api.PlaybackMediaResolutionPort
import com.latticeward.musiccopilot.playback.api.VerifiedPlaybackMedia
import com.latticeward.musiccopilot.playback.controller.PlaybackControllerConnection
import kotlinx.coroutines.CoroutineScope

/** The only permitted concrete integration boundary. Never owns a Player. */
internal object PlaybackBindings {
    fun controller(scope: CoroutineScope, context: Context?): PlaybackControllerConnection =
        PlaybackControllerConnection(scope, context)

    /** Lazy per-service resolver: no MediaStore query starts during app bootstrap. */
    fun playbackMediaResolver(
        context: Context,
        dispatchers: DispatcherProvider,
    ): PlaybackMediaResolutionPort {
        val delegate = MediaStorePlaybackResolver(
            AndroidMediaStorePlaybackProbe(context.applicationContext),
            dispatchers,
        )
        return object : PlaybackMediaResolutionPort {
            override suspend fun resolve(ref: MediaRef): AppResult<VerifiedPlaybackMedia> =
                when (val result = delegate.resolve(ref)) {
                    is AppResult.Success -> AppResult.Success(
                        VerifiedPlaybackMedia(result.value.ref, result.value.contentUri)
                    )
                    is AppResult.Failure -> result
                }

            override fun invalidate() = delegate.invalidate()
        }
    }
}
