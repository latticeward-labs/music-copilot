package com.latticeward.musiccopilot.spike.library

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import com.latticeward.musiccopilot.spike.reconcile.ScannedMedia
import com.latticeward.musiccopilot.spike.trace.SpikeTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LibraryScan(
    val mediaItems: List<MediaItem>,
    val reconciliationInput: List<ScannedMedia>,
    val elapsedMs: Long,
)

class MediaStoreLibrary(private val context: Context) {
    suspend fun scan(limit: Int = Int.MAX_VALUE): LibraryScan = withContext(Dispatchers.IO) {
        val startedAt = System.nanoTime()
        val mediaItems = mutableListOf<MediaItem>()
        val reconciliationInput = mutableListOf<ScannedMedia>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DURATION,
        )
        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 0"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media._ID} ASC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (cursor.moveToNext() && mediaItems.size < limit) {
                val id = cursor.getLong(idColumn)
                val duration = cursor.getLong(durationColumn)
                val uri = ContentUris.withAppendedId(collection, id)
                val syntheticToken = "item-${mediaItems.size + 1}"
                mediaItems += MediaItem.Builder()
                    .setMediaId(syntheticToken)
                    .setUri(uri)
                    .build()
                reconciliationInput += ScannedMedia(uri.toString(), duration)
            }
        }

        val elapsedMs = (System.nanoTime() - startedAt) / 1_000_000
        SpikeTrace.event(
            code = "MEDIASTORE_SCAN",
            "count" to mediaItems.size,
            "elapsedMs" to elapsedMs,
        )
        LibraryScan(mediaItems, reconciliationInput, elapsedMs)
    }
}
