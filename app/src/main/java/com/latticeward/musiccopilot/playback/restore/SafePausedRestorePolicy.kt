package com.latticeward.musiccopilot.playback.restore

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState

/**
 * Pure P1 plan, NOT a player constructor or a restore executor.
 * The caller must requery MediaStore and provide only verified, accessible refs.
 * A live MediaSession always wins. An output is PAUSED and never auto-plays.
 * Full cold-process restore wiring remains owned by Issue #7 / ST-03.
 */
object SafePausedRestorePolicy {
    const val SCHEMA_VERSION = 1
    private const val MAX_ITEMS = 5_000

    data class Plan(
        val items: List<MediaRef>,
        val currentIndex: Int,
        val positionMs: Long,
    ) {
        val playbackState: PlaybackState = PlaybackState.Paused
        val playWhenReady: Boolean = false
    }

    fun prepare(
        snapshot: RestorableQueueSnapshot?,
        liveSessionPresent: Boolean,
        verifiedMediaRefs: Set<MediaRef>,
        verifiedDurationMs: Long?,
    ): AppResult<Plan?> {
        if (liveSessionPresent || snapshot == null) return AppResult.Success(null)
        if (snapshot.schemaVersion > SCHEMA_VERSION) return failure(
            AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA, RecoveryAction.ManualFallback
        )
        if (snapshot.schemaVersion != SCHEMA_VERSION ||
            snapshot.queueRevision < 0L ||
            snapshot.positionMs < 0L ||
            snapshot.capturedAtEpochMs < 0L ||
            snapshot.items.size > MAX_ITEMS ||
            snapshot.items.distinct().size != snapshot.items.size ||
            snapshot.items.any { it.value.isBlank() } ||
            (snapshot.items.isEmpty() &&
                (snapshot.currentIndex != 0 || snapshot.positionMs != 0L)) ||
            (snapshot.items.isNotEmpty() && snapshot.currentIndex !in snapshot.items.indices)
        ) return failure(AppErrorCodes.SNAPSHOT_CORRUPT, RecoveryAction.Rebuild)
        if (snapshot.items.isEmpty()) return AppResult.Success(null)

        val surviving = snapshot.items.filter { it in verifiedMediaRefs }
        if (surviving.isEmpty()) return AppResult.Success(null)
        val previousCurrent = snapshot.items[snapshot.currentIndex]
        val currentSurvives = previousCurrent in surviving
        val index = if (currentSurvives) surviving.indexOf(previousCurrent) else 0
        // Never carry a position from a missing item to a different item.
        val position = if (currentSurvives) snapshot.positionMs else 0L
        val safePosition = if (verifiedDurationMs != null && verifiedDurationMs >= 0L)
            position.coerceAtMost(verifiedDurationMs) else 0L
        return AppResult.Success(Plan(surviving, index, safePosition))
    }

    private fun failure(code: String, recovery: RecoveryAction): AppResult.Failure =
        AppResult.Failure(AppError.Storage(
            code, Recoverability.RECOVERABLE, recovery
        ))
}
