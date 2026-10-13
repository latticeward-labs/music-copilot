package com.latticeward.musiccopilot.playback.command

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Exactly one consumer, 32 *waiting* slots. An in-flight command is additional
 * to the bounded pending queue. Accepted submissions always receive an outcome,
 * including on service destruction.
 */
class PlaybackCommandProcessor(
    ownerScope: CoroutineScope,
    private val handler: suspend (PlaybackCommand) -> AppResult<PlaybackSnapshot>,
) : AutoCloseable {
    private data class Pending(
        val command: PlaybackCommand,
        val reply: CompletableDeferred<AppResult<PlaybackSnapshot>>,
    )

    private val queue = Channel<Pending>(32)
    private var inFlight: Pending? = null
    private var closed = false

    private val worker = ownerScope.launch {
        try {
            for (pending in queue) {
                inFlight = pending
                try {
                    pending.reply.complete(handler(pending.command))
                } catch (cancelled: CancellationException) {
                    pending.reply.complete(rejected())
                    throw cancelled
                } catch (_: Exception) {
                    pending.reply.complete(
                        AppResult.Failure(AppError.Playback(
                            AppErrorCodes.PLAYBACK_FAILED,
                            Recoverability.RECOVERABLE, RecoveryAction.Retry
                        ))
                    )
                } finally {
                    inFlight = null
                }
            }
        } finally {
            inFlight?.reply?.complete(rejected())
            while (true) {
                val next = queue.tryReceive().getOrNull() ?: break
                next.reply.complete(rejected())
            }
        }
    }

    fun submit(command: PlaybackCommand): CompletableDeferred<AppResult<PlaybackSnapshot>> {
        val reply = CompletableDeferred<AppResult<PlaybackSnapshot>>()
        if (closed) {
            reply.complete(rejected())
            return reply
        }
        if (!queue.trySend(Pending(command, reply)).isSuccess) {
            reply.complete(AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_BUSY,
                Recoverability.RECOVERABLE, RecoveryAction.Retry
            )))
        }
        return reply
    }

    override fun close() {
        closed = true
        queue.close()
        worker.cancel()
        inFlight?.reply?.complete(rejected())
        while (true) {
            val p = queue.tryReceive().getOrNull() ?: break
            p.reply.complete(rejected())
        }
    }

    private fun rejected(): AppResult.Failure =
        AppResult.Failure(AppError.Playback(
            AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry
        ))
}

