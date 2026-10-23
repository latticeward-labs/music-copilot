package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import kotlinx.coroutines.CoroutineScope

/**
 * Narrow service input. It is only a transient, verified content URI, not
 * long-lived permission, queue ownership or permission to log personal files.
 * Display metadata is read locally and must never be logged or sent remotely.
 * Concrete MediaStore access is composed by app.di, never imported by playback.
 */
class VerifiedPlaybackMedia internal constructor(
    val ref: MediaRef,
    val contentUri: String,
    /** Local, bounded MediaStore display data for OS media surfaces only. */
    val displayTitle: String? = null,
    val displayArtist: String? = null,
    val displayAlbum: String? = null,
    /** Verified local duration for safe P1 restore position clamp only. */
    val durationMs: Long? = null,
) {
    override fun toString(): String = "VerifiedPlaybackMedia(redacted)"
}

interface PlaybackMediaResolutionPort {
    suspend fun resolve(ref: MediaRef): AppResult<VerifiedPlaybackMedia>
    fun invalidate()
}

/**
 * Implemented by the process Application; Android creates it before the
 * MediaSessionService, even if the Service starts before any Activity.
 * The implementation is supplied by the app.di composition root.
 */
interface PlaybackResolverHost {
    fun createPlaybackMediaResolver(
        dispatchers: DispatcherProvider,
    ): PlaybackMediaResolutionPort

    /** P1 non-authoritative Proto DataStore. No Player or media lookup here. */
    fun createPlaybackRestorableQueueStore(): RestorableQueueStore

    /** Main application scope persists final checkpoints across service stop. */
    fun playbackCheckpointWriteScope(): CoroutineScope
}
