package com.latticeward.musiccopilot.data.mediastore

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.CancellationSignal
import android.os.OperationCanceledException
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import java.io.FileNotFoundException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Android 10+ read-only probe of one concrete MediaStore audio row.
 * Called on injected IO dispatcher by MediaStorePlaybackResolver, never on Main.
 * Nothing here scans Room, opens a raw file path, or obtains writable access.
 */
class AndroidMediaStorePlaybackProbe(context: Context) : PlaybackMediaProbe {
    private val app = context.applicationContext
    private val resolver: ContentResolver get() = app.contentResolver

    override fun hasAccess(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(app, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    override suspend fun mountedVolumes(): Set<String> {
        currentCoroutineContext().ensureActive()
        requireAccess()
        val volumes = MediaStore.getExternalVolumeNames(app)
        if (volumes.isEmpty() || volumes.any { !MediaStoreRefCodec.isSpecificVolume(it) }) {
            throw IllegalStateException("media volume unavailable")
        }
        currentCoroutineContext().ensureActive()
        requireAccess()
        return volumes
    }

    override suspend fun inspect(volumeName: String, mediaId: Long): MediaProbeOutcome {
        currentCoroutineContext().ensureActive()
        requireAccess()
        if (!MediaStoreRefCodec.isSpecificVolume(volumeName) || mediaId < 0L) {
            return MediaProbeOutcome.Missing
        }
        val uri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.getContentUri(volumeName), mediaId,
        )
        // Additional defense against unexpected/malformed provider authorities.
        if (uri.scheme != ContentResolver.SCHEME_CONTENT ||
            uri.authority != "media" ||
            uri.toString() != "content://media/$volumeName/audio/media/$mediaId"
        ) return MediaProbeOutcome.Unplayable

        // A single signal spans query + read-only descriptor opening. The job
        // cancels the provider work; no cursor or descriptor is retained.
        val signal = CancellationSignal()
        val registration = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause != null) signal.cancel()
        }
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.MediaColumns.VOLUME_NAME,
            )
            val query = try {
                resolver.query(uri, projection, null, null, null, signal)
            } catch (cancelled: OperationCanceledException) {
                currentCoroutineContext().ensureActive()
                return MediaProbeOutcome.ProviderUnavailable
            } ?: return MediaProbeOutcome.ProviderUnavailable

            val rowStatus = query.use { cursor ->
                currentCoroutineContext().ensureActive()
                requireAccess()
                if (!cursor.moveToFirst()) return@use MediaProbeOutcome.Missing
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val musicIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                val mimeIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val volumeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.VOLUME_NAME)
                if (cursor.isNull(idIndex) || cursor.getLong(idIndex) != mediaId ||
                    cursor.isNull(musicIndex) || cursor.getInt(musicIndex) == 0
                ) return@use MediaProbeOutcome.Unplayable
                val mime = cursor.getString(mimeIndex)
                if (!mime.isNullOrBlank() && !mime.startsWith("audio/", ignoreCase = true)) {
                    return@use MediaProbeOutcome.Unplayable
                }
                val observedVolume = cursor.getString(volumeIndex)
                if (!observedVolume.isNullOrBlank() && observedVolume != volumeName) {
                    return@use MediaProbeOutcome.Unplayable
                }
                if (cursor.moveToNext()) return@use MediaProbeOutcome.Unplayable
                MediaProbeOutcome.Readable
            }
            if (rowStatus != MediaProbeOutcome.Readable) return rowStatus
            currentCoroutineContext().ensureActive()
            requireAccess()

            val readable = try {
                resolver.openFileDescriptor(uri, "r", signal)?.use { descriptor ->
                    // Zero bytes are not a playable track; unknown length (-1) is
                    // allowed and decoder errors are handled by P05/P10.
                    descriptor.statSize != 0L
                } ?: false
            } catch (missing: FileNotFoundException) {
                return MediaProbeOutcome.Missing
            } catch (cancelled: OperationCanceledException) {
                currentCoroutineContext().ensureActive()
                return MediaProbeOutcome.ProviderUnavailable
            }

            currentCoroutineContext().ensureActive()
            requireAccess()
            return if (readable) MediaProbeOutcome.Readable else MediaProbeOutcome.Unplayable
        } finally {
            registration?.dispose()
        }
    }

    private fun requireAccess() {
        if (!hasAccess()) throw SecurityException("media access unavailable")
    }
}
