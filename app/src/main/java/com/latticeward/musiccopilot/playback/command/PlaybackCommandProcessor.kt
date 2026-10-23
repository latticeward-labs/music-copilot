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

    // Bounded in-memory idempotency: retries share the same authoritative
    // completion, while a reused ID with a different payload is rejected.
    // No IDs/queue payloads are persisted or sent to telemetry.
    private val recent = LinkedHashMap<String, Pending>()
    private val replayCapacity = 256

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
        if (closed || command.commandId.value.isBlank()) {
            reply.complete(rejected())
            return reply
        }
        recent[command.commandId.value]?.let { prior ->
            if (prior.command == command) return prior.reply
            reply.complete(rejected())
            return reply
        }
        val pending = Pending(command, reply)
        if (!queue.trySend(pending).isSuccess) {
            reply.complete(AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_BUSY,
                Recoverability.RECOVERABLE, RecoveryAction.Retry
            )))
            return reply
        }
        recent[command.commandId.value] = pending
        // Always retain unfinished submissions. With one worker and 32 waiting
        // slots at most 33 are unfinished; evict oldest COMPLETED entries only.
        if (recent.size > replayCapacity) {
            val iterator = recent.entries.iterator()
            while (recent.size > replayCapacity && iterator.hasNext()) {
                if (iterator.next().value.reply.isCompleted) iterator.remove()
            }
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
        recent.clear()
    }

    private fun rejected(): AppResult.Failure =
        AppResult.Failure(AppError.Playback(
            AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry
        ))
}
