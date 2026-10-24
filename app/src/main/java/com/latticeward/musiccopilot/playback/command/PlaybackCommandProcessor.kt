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
 * Receipt-time guard, captured on the same service Main thread that receives
 * system/media session commands and urgent route/focus callbacks. The worker
 * rechecks the immutable stamp *before* any queued handler executes.
 */
data class PlaybackReceiptStamp(
    val routeGeneration: Long,
    val safetyGeneration: Long,
    val commandGeneration: Long,
)

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
        val receipt: PlaybackReceiptStamp,
    )

    // Bounded in-memory idempotency: retries share the same authoritative
    // completion, while a reused ID with a different payload is rejected.
    // No IDs/queue payloads are persisted or sent to telemetry.
    private val recent = LinkedHashMap<String, Pending>()
    private val replayCapacity = 256

    private val queue = Channel<Pending>(32)
    private var inFlight: Pending? = null
    private var closed = false
    private var captureReceipt: (PlaybackCommand) -> PlaybackReceiptStamp = {
        PlaybackReceiptStamp(
            routeGeneration = 0L,
            safetyGeneration = 0L,
            commandGeneration = 0L,
        )
    }
    private var isReceiptCurrent: (PlaybackCommand, PlaybackReceiptStamp) -> Boolean =
        { _, _ -> true }

    /**
     * Called exactly once by the MediaSessionService before it can accept
     * commands, while Main owns all callbacks. Kept optional so existing
     * #7 pure command-worker users retain their source-level contract.
     */
    fun configureReceiptSafety(
        capture: (PlaybackCommand) -> PlaybackReceiptStamp,
        current: (PlaybackCommand, PlaybackReceiptStamp) -> Boolean,
    ) {
        check(!closed)
        captureReceipt = capture
        isReceiptCurrent = current
    }

    private val worker = ownerScope.launch {
        try {
            for (pending in queue) {
                inFlight = pending
                try {
                    // A newer safety event can arrive while an earlier command
                    // waits behind suspended MediaStore IO. Never stamp an
                    // old command again at DISPATCH and let it clear the latch.
                    val outcome = if (
                        isReceiptCurrent(pending.command, pending.receipt)
                    ) handler(pending.command) else rejected()
                    pending.reply.complete(outcome)
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
        // The receipt hook is synchronous and sees the original Main-looper
        // order, even when another command is suspended on IO. It may perform
        // urgent safe Pause for Pause/Stop before queue capacity is checked.
        val stamp = try {
            captureReceipt(command)
        } catch (_: Exception) {
            // A failed admission callback must not enter the worker without
            // a coherent receipt stamp or leave the client without a result.
            reply.complete(rejected())
            return reply
        }
        val pending = Pending(command, reply, stamp)
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
