package com.latticeward.musiccopilot.playback.service

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.mediastore.LocalPlaybackMediaResolver
import com.latticeward.musiccopilot.data.mediastore.VerifiedLocalMedia
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * A validated proposal, NOT a live queue. Only MusicPlaybackService commits
 * this immutable proposal into the one service-owned Media3 Player.
 *
 * Never log or persist entries: each includes an internal content URI.
 */
class PreparedPlaybackQueue internal constructor(
    val entries: List<VerifiedLocalMedia>,
    val selectedIndex: Int,
    val startPositionMs: Long,
    val playWhenReady: Boolean,
    val skippedUnavailable: Int,
) {
    init {
        require(entries.isNotEmpty() && selectedIndex in entries.indices)
        require(startPositionMs >= 0L)
    }

    override fun toString(): String = "PreparedPlaybackQueue(redacted)"
}

/**
 * P04 asynchronous queue preparation. No player, session, Room, network,
 * cached grant or mutable live queue exists here. A single call either returns
 * a complete local-media proposal or a typed failure; it never partially
 * commits. Main-thread Media3 mutations belong exclusively to the service.
 */
class ServiceQueueEngine(
    private val resolver: LocalPlaybackMediaResolver,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun prepare(command: PlaybackCommand.ReplaceQueue): AppResult<PreparedPlaybackQueue> =
        withContext(dispatchers.io) {
            currentCoroutineContext().ensureActive()
            val decision = when (val check = PlaybackQueuePolicy.validate(command)) {
                is PlaybackQueuePolicy.Decision.Accepted -> check
                is PlaybackQueuePolicy.Decision.Rejected -> return@withContext rejected()
            }

            // Verify the explicitly selected occurrence first: a missing
            // requested song must not trigger thousands of unrelated probes.
            val selected = when (val result = resolver.resolve(decision.items[decision.selectedIndex])) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> return@withContext AppResult.Failure(result.error)
            }
            val validated = arrayOfNulls<VerifiedLocalMedia>(decision.items.size)
            validated[decision.selectedIndex] = selected

            for ((index, ref) in decision.items.withIndex()) {
                if (index == decision.selectedIndex) continue
                currentCoroutineContext().ensureActive()
                when (val result = resolver.resolve(ref)) {
                    is AppResult.Success -> validated[index] = result.value
                    is AppResult.Failure -> when (result.error) {
                        // A global permission failure invalidates the entire
                        // request, not just the current queue occurrence.
                        is AppError.Permission -> return@withContext AppResult.Failure(result.error)
                        is AppError.MediaItem -> Unit // Only nonselected item is skippable.
                        else -> return@withContext AppResult.Failure(result.error)
                    }
                }
            }
            currentCoroutineContext().ensureActive()

            // Recheck selected media LAST, so it is as fresh as possible
            // after processing the remaining candidate collection.
            val selectedAgain = when (val check = resolver.resolve(decision.items[decision.selectedIndex])) {
                is AppResult.Success -> check.value
                is AppResult.Failure -> return@withContext AppResult.Failure(check.error)
            }
            validated[decision.selectedIndex] = selectedAgain

            val present = validated.map { it != null }
            val filtered = when (val result = PlaybackQueuePolicy.retainPlayable(decision, present)) {
                is PlaybackQueuePolicy.Decision.Accepted -> result
                is PlaybackQueuePolicy.Decision.Rejected -> return@withContext rejected()
            }
            val items = validated.filterNotNull()
            currentCoroutineContext().ensureActive()
            AppResult.Success(
                PreparedPlaybackQueue(
                    entries = items,
                    selectedIndex = filtered.selectedIndex,
                    startPositionMs = filtered.positionMs,
                    playWhenReady = filtered.playWhenReady,
                    skippedUnavailable = decision.items.size - items.size,
                )
            )
        }

    /** Invalidate resolver's pending permission/volume generations. */
    fun invalidate() = resolver.invalidate()

    private fun rejected(): AppResult.Failure = AppResult.Failure(
        AppError.Playback(
            AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE,
            RecoveryAction.Retry,
        )
    )
}
