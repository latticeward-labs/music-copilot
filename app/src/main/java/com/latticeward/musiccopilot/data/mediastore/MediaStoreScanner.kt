package com.latticeward.musiccopilot.data.mediastore

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import kotlinx.coroutines.withContext

/** No file paths, content URIs or user media are emitted into diagnostics. */
data class ScannedAudio(
    val ref: MediaRef,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
)

/** Replaceable Android platform authority seam, for deterministic repository tests. */
interface LocalAudioSource {
    fun hasAccess(): Boolean
    suspend fun scan(): List<ScannedAudio>
    suspend fun requery(ref: MediaRef): ScannedAudio?
}

/** MediaStore ID is an opaque local re-query key, NOT a durable filesystem path. */
object MediaStoreRefCodec {
    fun encode(id: Long): MediaRef {
        require(id >= 0)
        return MediaRef("audio:$id")
    }

    fun decode(ref: MediaRef): Long? {
        val value = ref.value
        if (!value.startsWith("audio:")) return null
        val numeric = value.removePrefix("audio:")
        if (numeric.isEmpty() || numeric.any { !it.isDigit() }) return null
        return numeric.toLongOrNull()?.takeIf { it >= 0 }
    }
}

class MediaStoreScanner(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : LocalAudioSource {
    private val appContext = context.applicationContext
    private val resolver: ContentResolver get() = appContext.contentResolver
    private val collection get() = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    override fun hasAccess(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= 33)
            Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(appContext, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    override suspend fun scan(): List<ScannedAudio> = withContext(dispatchers.io) {
        if (!hasAccess()) throw SecurityException("media capability unavailable")
        query(collection, "${MediaStore.Audio.Media.IS_MUSIC} != 0", null)
    }

    override suspend fun requery(ref: MediaRef): ScannedAudio? = withContext(dispatchers.io) {
        if (!hasAccess()) throw SecurityException("media capability unavailable")
        val id = MediaStoreRefCodec.decode(ref) ?: return@withContext null
        // Query the platform source anew; never treat Room as existence authority.
        query(ContentUris.withAppendedId(collection, id), null, null).firstOrNull()
    }

    private fun query(
        uri: android.net.Uri, where: String?, args: Array<String>?,
    ): List<ScannedAudio> {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )
        val records = mutableListOf<ScannedAudio>()
        val cursor = resolver.query(uri, projection, where, args, null)
            ?: throw IllegalStateException("MediaStore query returned no cursor")
        cursor.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (cursor.moveToNext()) {
                if (cursor.isNull(idColumn)) continue
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)?.trim().orEmpty()
                if (id < 0 || title.isBlank()) continue
                records += ScannedAudio(
                    ref = MediaStoreRefCodec.encode(id),
                    title = title,
                    artist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() },
                    album = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() },
                    durationMs = cursor.getLong(durationColumn).takeIf { it > 0 },
                )
            }
        }
        return records
    }
}
