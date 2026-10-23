package com.latticeward.musiccopilot.data.mediastore

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Read-only candidate inspection. Provider access belongs to AndroidMediaStorePlaybackProbe,
 * while this interface permits deterministic JVM fakes without loading android.* APIs.
 */
interface PlaybackMediaProbe {
    fun hasAccess(): Boolean
    suspend fun mountedVolumes(): Set<String>
    suspend fun inspect(volumeName: String, mediaId: Long): MediaProbeOutcome
}

sealed interface MediaProbeOutcome {
    data object Readable : MediaProbeOutcome
    data object Missing : MediaProbeOutcome
    data object Unplayable : MediaProbeOutcome
    data object ProviderUnavailable : MediaProbeOutcome
}

/**
 * This is an internal transport token, NOT a long-lived permission grant.
 * Never print it, put its URI in crash/telemetry, or persist it in the P1 snapshot.
 * Service must handle later Media3 open failures as media may be removed after probing.
 */
class VerifiedLocalMedia internal constructor(
    val ref: MediaRef,
    val contentUri: String,
) {
    override fun toString(): String = "VerifiedLocalMedia(redacted)"
}

interface LocalPlaybackMediaResolver {
    suspend fun resolve(ref: MediaRef): AppResult<VerifiedLocalMedia>
    /** Call when permission, media volumes or library identity is invalidated. */
    fun invalidate()
}

/**
 * Service-consumable, fail-closed MediaStore resolver. No caching: every resolution
 * rechecks the OS and read-only descriptor. Epoch invalidation also rejects
 * in-flight results that completed against an older permission/volume generation.
 */
class MediaStorePlaybackResolver(
    private val platform: PlaybackMediaProbe,
    private val dispatchers: DispatcherProvider,
) : LocalPlaybackMediaResolver {
    private val epoch = AtomicLong(0L)

    override fun invalidate() {
        epoch.incrementAndGet()
    }

    override suspend fun resolve(ref: MediaRef): AppResult<VerifiedLocalMedia> =
        withContext(dispatchers.io) {
            val initialEpoch = epoch.get()
            currentCoroutineContext().ensureActive()
            if (!platform.hasAccess()) return@withContext permissionRevoked()

            val key = MediaStoreRefCodec.decodeScoped(ref)
                ?: return@withContext mediaFailure(AppErrorCodes.MEDIA_ITEM_MISSING)
            val volume = key.volumeName ?: "external_primary"
            if (!MediaStoreRefCodec.isSpecificVolume(volume)) {
                return@withContext mediaFailure(AppErrorCodes.MEDIA_ITEM_MISSING)
            }

            try {
                val volumes = platform.mountedVolumes()
                currentCoroutineContext().ensureActive()
                if (!platform.hasAccess()) return@withContext permissionRevoked()
                if (epoch.get() != initialEpoch) {
                    return@withContext mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
                }
                if (volume !in volumes) {
                    return@withContext mediaFailure(AppErrorCodes.MEDIA_ITEM_MISSING)
                }

                val outcome = platform.inspect(volume, key.id)
                currentCoroutineContext().ensureActive()
                if (!platform.hasAccess()) return@withContext permissionRevoked()
                if (epoch.get() != initialEpoch) {
                    return@withContext mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
                }
                when (outcome) {
                    MediaProbeOutcome.Readable -> AppResult.Success(
                        VerifiedLocalMedia(
                            ref = ref,
                            contentUri = "content://media/$volume/audio/media/${key.id}",
                        ),
                    )
                    MediaProbeOutcome.Missing -> mediaFailure(AppErrorCodes.MEDIA_ITEM_MISSING)
                    MediaProbeOutcome.Unplayable ->
                        mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
                    MediaProbeOutcome.ProviderUnavailable ->
                        mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (denied: SecurityException) {
                permissionRevoked()
            } catch (io: IOException) {
                mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
            } catch (failure: RuntimeException) {
                // Android provider details may contain private URIs: never expose messages.
                if (!platform.hasAccess()) permissionRevoked()
                else mediaFailure(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
            }
        }

    private fun permissionRevoked(): AppResult.Failure = AppResult.Failure(
        AppError.Permission(
            AppErrorCodes.PERMISSION_REVOKED,
            Recoverability.USER_ACTION_REQUIRED,
            RecoveryAction.OpenSettings,
        ),
    )

    private fun mediaFailure(code: String): AppResult.Failure = AppResult.Failure(
        AppError.MediaItem(code, Recoverability.RECOVERABLE, RecoveryAction.Skip),
    )
}
