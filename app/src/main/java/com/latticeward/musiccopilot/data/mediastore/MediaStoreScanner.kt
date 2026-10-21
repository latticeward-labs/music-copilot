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
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * P03 platform scan DTO. Additive trailing defaults retain every Issue #4/#5
 * fake and the P02/Issue #16 repository signatures without a schema change.
 * P04 normalizes/validates metadata; P05 computes deltas; P06 persists fields.
 *
 * Do not log this object, its strings, identifiers or the raw provider rows.
 */
data class ScannedAudio(
    val ref: MediaRef,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long?,
    val volumeName: String? = null,
    val relativeFolderPath: String? = null,
    val platformAlbumId: Long? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
    val dateModifiedSeconds: Long? = null,
    val generationModified: Long? = null,
)

/** Android OS permissions and MediaStore, never Room/DataStore, own access. */
interface LocalAudioSource {
    fun hasAccess(): Boolean
    suspend fun scan(): List<ScannedAudio>
    suspend fun requery(ref: MediaRef): ScannedAudio?
}

/**
 * Backward-compatible local reference codec.
 *
 * Old v1: audio:<id> => PRIMARY volume only (never an ambiguous all-volume id).
 * New:    audio:v:<specific-volume>:<id>
 *
 * Specific MediaStore volume names are verified; only decimal non-negative IDs
 * are accepted. Never construct filesystem paths from media references.
 */
data class ScopedAudioId(val volumeName: String?, val id: Long)

object MediaStoreRefCodec {
    private val validVolume = Regex("[A-Za-z0-9][A-Za-z0-9_-]{0,127}")

    fun isSpecificVolume(volumeName: String): Boolean =
        validVolume.matches(volumeName) &&
            volumeName != "external" && volumeName != "internal"

    /** For older tests/Room v1 keys; new scanner MUST use scoped encode. */
    fun encode(id: Long): MediaRef {
        require(id >= 0L)
        return MediaRef("audio:$id")
    }

    fun encode(volumeName: String, id: Long): MediaRef {
        require(isSpecificVolume(volumeName))
        require(id >= 0L)
        return MediaRef("audio:v:$volumeName:$id")
    }

    /** Preserves the pre-P03 numeric decode helper for existing consumers. */
    fun decode(ref: MediaRef): Long? = decodeScoped(ref)?.id

    fun decodeScoped(ref: MediaRef): ScopedAudioId? {
        val raw = ref.value
        if (raw.startsWith("audio:v:")) {
            val segments = raw.removePrefix("audio:v:").split(':')
            if (segments.size != 2 || !isSpecificVolume(segments[0])) return null
            val id = parseId(segments[1]) ?: return null
            return ScopedAudioId(segments[0], id)
        }
        if (!raw.startsWith("audio:")) return null
        val id = parseId(raw.removePrefix("audio:")) ?: return null
        return ScopedAudioId(null, id)
    }

    private fun parseId(text: String): Long? =
        text.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
            ?.toLongOrNull()
            ?.takeIf { it >= 0L }
}

/**
 * Pure row metadata policy, no platform Cursor or Android context. Provider
 * exceptions stay errors rather than fabricated Empty. The caller may skip
 * individual non-music/invalid/missing-title rows without deleting user media.
 */
object MediaStoreAudioPolicy {
    fun usableTitle(title: String?): String? = title
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.any(Char::isISOControl) }

    fun optionalMetadata(value: String?): String? = value
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.any(Char::isISOControl) }
        ?.takeUnless { it == MediaStore.UNKNOWN_STRING }

    fun optionalPositive(value: Long?): Long? = value?.takeIf { it > 0L }

    fun safeRelativeFolder(value: String?): String? {
        val raw = value?.trim() ?: return null
        if (raw.isEmpty() || raw.startsWith('/') || raw.startsWith('\\')) return null
        val path = raw.replace('\\', '/')
        val parts = path.trimEnd('/').split('/')
        if (parts.isEmpty() || parts.any { part ->
                part.isBlank() || part == "." || part == ".." ||
                    part.any(Char::isISOControl)
            }) return null
        return parts.joinToString("/") + "/"
    }

    fun acceptableMime(mimeType: String?): Boolean =
        mimeType.isNullOrBlank() || mimeType.startsWith("audio/", ignoreCase = true)
}

/**
 * One permission-gated query per mounted, concrete volume. Android 10+
 * MediaStore.getExternalVolumeNames() prevents cross-volume numeric-ID
 * collisions, and failing one volume fails the WHOLE reconciliation. A missing
 * cursor or unavailable storage is never treated as a verified Empty library.
 *
 * Only called from explicit Issue #5 LibraryScanUseCase actions.
 */
class MediaStoreScanner(
    context: Context,
    private val dispatchers: DispatcherProvider,
) : LocalAudioSource {
    private val appContext = context.applicationContext
    private val resolver: ContentResolver get() = appContext.contentResolver

    override fun hasAccess(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(appContext, permission) ==
            PackageManager.PERMISSION_GRANTED
    }

    override suspend fun scan(): List<ScannedAudio> = withContext(dispatchers.io) {
        requireAccess()
        val volumes = mountedVolumes()
        val records = ArrayList<ScannedAudio>()
        for (volume in volumes) {
            currentCoroutineContext().ensureActive()
            requireAccess()
            val collection = MediaStore.Audio.Media.getContentUri(volume)
            records.addAll(query(collection, volume, MediaStore.Audio.Media.IS_MUSIC + " != 0"))
        }
        requireAccess()
        currentCoroutineContext().ensureActive()
        records
    }

    override suspend fun requery(ref: MediaRef): ScannedAudio? = withContext(dispatchers.io) {
        requireAccess()
        currentCoroutineContext().ensureActive()
        val key = MediaStoreRefCodec.decodeScoped(ref) ?: return@withContext null
        // Legacy unscoped v1 IDs refer ONLY to primary storage, never external
        // aggregate. Without this guard two different volumes could cross-map.
        val volume = key.volumeName ?: MediaStore.VOLUME_EXTERNAL_PRIMARY
        if (volume !in mountedVolumes()) return@withContext null
        val itemUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.getContentUri(volume), key.id,
        )
        val current = query(itemUri, volume, MediaStore.Audio.Media.IS_MUSIC + " != 0")
            .firstOrNull()
        requireAccess()
        currentCoroutineContext().ensureActive()
        if (current == null) return@withContext null
        // Only one selected item is probed. Scanning 5,000 library rows MUST
        // NOT open 5,000 audio descriptors; playback is still service-owned.
        // A provider that can list an entry but cannot open it does not prove
        // the track can be handed off as playable.
        val readable = try {
            resolver.openFileDescriptor(itemUri, "r")?.use { true } ?: false
        } catch (_: IOException) {
            false
        }
        if (!readable) return@withContext null
        requireAccess()
        currentCoroutineContext().ensureActive()
        if (key.volumeName == null) current.copy(ref = ref) else current
    }

    private fun mountedVolumes(): List<String> {
        requireAccess()
        val values = MediaStore.getExternalVolumeNames(appContext)
        // A missing primary/removable volume is NOT proof of zero audio.
        if (values.isEmpty()) throw IllegalStateException("media volume unavailable")
        if (values.any { !MediaStoreRefCodec.isSpecificVolume(it) }) {
            throw IllegalStateException("invalid media volume")
        }
        return values.sorted()
    }

    private fun requireAccess() {
        if (!hasAccess()) throw SecurityException("media access unavailable")
    }

    private suspend fun query(
        uri: android.net.Uri,
        volumeName: String,
        where: String?,
    ): List<ScannedAudio> {
        // Never project MediaColumns.DATA, log a content URI, or write to source.
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.IS_MUSIC)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.MediaColumns.VOLUME_NAME)
            add(MediaStore.MediaColumns.RELATIVE_PATH)
            add(MediaStore.MediaColumns.SIZE)
            add(MediaStore.MediaColumns.DATE_MODIFIED)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                add(MediaStore.MediaColumns.GENERATION_MODIFIED)
            }
        }.toTypedArray()

        val results = ArrayList<ScannedAudio>()
        // Android ContentResolver may block before it yields a Cursor; wire
        // coroutine Job cancellation to the platform CancellationSignal.
        val signal = CancellationSignal()
        val onCompletion = currentCoroutineContext()[Job]?.invokeOnCompletion { cause ->
            if (cause != null) signal.cancel()
        }
        val cursor = try {
            resolver.query(uri, projection, where, null, null, signal)
        } catch (cancelled: OperationCanceledException) {
            // An actual coroutine cancellation must propagate as
            // CancellationException, not be misclassified as a retryable scan.
            currentCoroutineContext().ensureActive()
            throw cancelled
        } finally {
            onCompletion?.dispose()
        } ?: throw IllegalStateException("media provider returned null cursor")
        cursor.use { data ->
            val idCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val musicCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val titleCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val mimeCol = data.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val volumeCol = data.getColumnIndexOrThrow(MediaStore.MediaColumns.VOLUME_NAME)
            val folderCol = data.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
            val sizeCol = data.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val modifiedCol = data.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
            val generationCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                data.getColumnIndexOrThrow(MediaStore.MediaColumns.GENERATION_MODIFIED)
            } else -1

            var processed = 0
            while (data.moveToNext()) {
                currentCoroutineContext().ensureActive()
                if (++processed % 128 == 0) requireAccess()
                if (data.isNull(idCol) || data.isNull(musicCol) ||
                    data.getInt(musicCol) == 0) continue
                val id = data.getLong(idCol)
                if (id < 0L) continue
                // P04: a provider can expose valid music with a missing
                // TITLE. Preserve the song with a safe local display fallback;
                // source identity and IS_MUSIC/MIME remain the eligibility gate.
                val title = LibraryTextNormalization.title(data.getString(titleCol))
                val mime = data.getString(mimeCol)
                if (!MediaStoreAudioPolicy.acceptableMime(mime)) continue
                // The volume name from this concrete query is canonical; a
                // provider reporting a conflicting volume is not trusted.
                val reportedVolume = data.getString(volumeCol)
                if (!reportedVolume.isNullOrBlank() && reportedVolume != volumeName) continue

                results += ScannedAudio(
                    ref = MediaStoreRefCodec.encode(volumeName, id),
                    title = title,
                    artist = MediaStoreAudioPolicy.optionalMetadata(data.getString(artistCol)),
                    album = MediaStoreAudioPolicy.optionalMetadata(data.getString(albumCol)),
                    durationMs = if (data.isNull(durationCol)) null else
                        MediaStoreAudioPolicy.optionalPositive(data.getLong(durationCol)),
                    volumeName = volumeName,
                    relativeFolderPath =
                        MediaStoreAudioPolicy.safeRelativeFolder(data.getString(folderCol)),
                    platformAlbumId = if (data.isNull(albumIdCol)) null else
                        data.getLong(albumIdCol).takeIf { it >= 0L },
                    mimeType = mime,
                    sizeBytes = if (data.isNull(sizeCol)) null else
                        MediaStoreAudioPolicy.optionalPositive(data.getLong(sizeCol)),
                    dateModifiedSeconds = if (data.isNull(modifiedCol)) null else
                        data.getLong(modifiedCol).takeIf { it >= 0L },
                    generationModified = if (generationCol < 0 ||
                        data.isNull(generationCol)) null else
                        data.getLong(generationCol).takeIf { it >= 0L },
                )
            }
        }
        requireAccess()
        currentCoroutineContext().ensureActive()
        return results
    }
}
