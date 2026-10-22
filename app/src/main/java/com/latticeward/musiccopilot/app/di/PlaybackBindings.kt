package com.latticeward.musiccopilot.app.di

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.mediastore.AndroidMediaStorePlaybackProbe
import com.latticeward.musiccopilot.data.mediastore.MediaStorePlaybackResolver
import com.latticeward.musiccopilot.playback.api.PlaybackMediaResolutionPort
import com.latticeward.musiccopilot.playback.api.VerifiedPlaybackMedia
import com.latticeward.musiccopilot.playback.controller.PlaybackControllerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/** The only permitted concrete integration boundary. Never owns a Player. */
internal object PlaybackBindings {
    fun controller(scope: CoroutineScope, context: Context?): PlaybackControllerConnection =
        PlaybackControllerConnection(scope, context)

    private data class LocalLabels(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val durationMs: Long? = null,
    )

    /** Returns only display strings, no raw paths, album-art URI or logs. */
    private fun readLocalLabels(context: Context, contentUri: String): LocalLabels {
        return try {
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
            )
            val uri = Uri.parse(contentUri)
            if (uri.scheme != "content" || uri.authority != "media") return LocalLabels()
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use LocalLabels()
                fun text(column: String): String? {
                    val index = cursor.getColumnIndex(column)
                    if (index < 0 || cursor.isNull(index)) return null
                    return cursor.getString(index)?.trim()?.take(200)
                        ?.takeIf { it.isNotEmpty() }
                }
                LocalLabels(
                    text(MediaStore.Audio.Media.TITLE),
                    text(MediaStore.Audio.Media.ARTIST),
                    text(MediaStore.Audio.Media.ALBUM),
                    cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                        .takeIf { it >= 0 && !cursor.isNull(it) }
                        ?.let { cursor.getLong(it).takeIf { value -> value >= 0L } },
                )
            } ?: LocalLabels()
        } catch (_: SecurityException) {
            LocalLabels() // Never log personal provider paths/error messages.
        } catch (_: RuntimeException) {
            LocalLabels()
        }
    }

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
                withContext(dispatchers.io) {
                when (val result = delegate.resolve(ref)) {
                    is AppResult.Success -> {
                        // Still on the resolver's IO context. The verified
                        // URI is ephemeral; labels exist ONLY to populate
                        // Media3's local OS notification/lock-screen metadata.
                        val labels = readLocalLabels(context, result.value.contentUri)
                        AppResult.Success(
                            VerifiedPlaybackMedia(
                                result.value.ref, result.value.contentUri,
                                labels.title, labels.artist, labels.album,
                                labels.durationMs,
                            )
                        )
                    }
                    is AppResult.Failure -> result
                }
                }

            override fun invalidate() = delegate.invalidate()
        }
    }
}
