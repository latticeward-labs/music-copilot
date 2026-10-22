package com.latticeward.musiccopilot.playback.command

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackCommand

/**
 * Pure, side-effect-free contract for Issue #7 P02. This does not touch Media3,
 * accept media access, or replace the service-owned queue. P03/P04 apply it
 * only inside the single Playback Service authority.
 */
object PlaybackQueuePolicy {
    const val MAX_ITEMS = 5_000
    const val MAX_OPAQUE_ID_LENGTH = 128

    enum class Rejection {
        InvalidCommandId,
        EmptyQueue,
        QueueTooLarge,
        InvalidMediaRef,
        InvalidStartIndex,
        NegativePosition,
        InvalidValidationShape,
        SelectedItemUnavailable,
    }

    sealed interface Decision {
        data class Accepted(
            val items: List<MediaRef>,
            val selectedIndex: Int,
            val positionMs: Long,
            val playWhenReady: Boolean,
        ) : Decision

        data class Rejected(val reason: Rejection) : Decision
    }

    /** Structural validation only; every ref still needs a P03 MediaStore requery. */
    fun validate(command: PlaybackCommand.ReplaceQueue): Decision {
        if (command.commandId.value.isBlank() ||
            command.commandId.value.length > MAX_OPAQUE_ID_LENGTH
        ) return Decision.Rejected(Rejection.InvalidCommandId)
        if (command.items.isEmpty()) return Decision.Rejected(Rejection.EmptyQueue)
        if (command.items.size > MAX_ITEMS) return Decision.Rejected(Rejection.QueueTooLarge)
        if (command.items.any {
                it.value.isBlank() || it.value.length > MAX_OPAQUE_ID_LENGTH
            }
        ) return Decision.Rejected(Rejection.InvalidMediaRef)
        if (command.startIndex !in command.items.indices) {
            return Decision.Rejected(Rejection.InvalidStartIndex)
        }
        if (command.startPositionMs < 0L) return Decision.Rejected(Rejection.NegativePosition)
        // Do not deduplicate: an occurrence is identified by its position.
        return Decision.Accepted(
            items = command.items.toList(),
            selectedIndex = command.startIndex,
            positionMs = command.startPositionMs,
            playWhenReady = command.playWhenReady,
        )
    }

    /**
     * Called only AFTER each candidate was checked by the local MediaStore
     * resolver. Missing non-selected entries are omitted without reordering.
     * A missing selected entry fails closed; the live queue is unchanged.
     */
    fun retainPlayable(candidate: Decision.Accepted, playable: List<Boolean>): Decision {
        if (playable.size != candidate.items.size) {
            return Decision.Rejected(Rejection.InvalidValidationShape)
        }
        if (!playable[candidate.selectedIndex]) {
            return Decision.Rejected(Rejection.SelectedItemUnavailable)
        }
        val remaining = candidate.items.filterIndexed { index, _ -> playable[index] }
        val newIndex = (0 until candidate.selectedIndex).count { playable[it] }
        return candidate.copy(items = remaining, selectedIndex = newIndex)
    }

    sealed interface NextDecision {
        data class Advance(val index: Int) : NextDecision
        data object EndOfQueue : NextDecision
        data object NoTrack : NextDecision
    }

    /** Nonwrapping navigation, valid only for the service-verified queue. */
    fun next(currentIndex: Int?, size: Int): NextDecision {
        if (size <= 0 || currentIndex == null || currentIndex !in 0 until size) {
            return NextDecision.NoTrack
        }
        return if (currentIndex + 1 < size) NextDecision.Advance(currentIndex + 1)
        else NextDecision.EndOfQueue
    }
}
