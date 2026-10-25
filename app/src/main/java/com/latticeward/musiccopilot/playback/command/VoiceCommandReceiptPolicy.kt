package com.latticeward.musiccopilot.playback.command

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot

/**
 * P06: tiny typed wire receipt contract for explicit PTT transport commands.
 * No microphone/STT dependency and no raw voice content in command IDs.
 * The Media3 service's existing PlaybackCommandProcessor owns deduplication.
 */
object VoiceCommandReceiptPolicy {
    private val voiceId = Regex("^voice-[1-9][0-9]*-[12]$")

    fun isVoiceCommand(command: PlaybackCommand): Boolean =
        voiceId.matches(command.commandId.value)

    fun confirms(command: PlaybackCommand, receiptId: String?): Boolean =
        !isVoiceCommand(command) || receiptId == command.commandId.value

    /** End of a no-repeat queue is a Voice failure, not an executed NEXT. */
    fun canAdvance(currentIndex: Int, itemCount: Int): Boolean =
        itemCount > 0 && currentIndex >= 0 && currentIndex < itemCount - 1

    /** Service has the authoritative revision and item; never infer from UI. */
    fun hasCoherentQueue(snapshot: PlaybackSnapshot): Boolean {
        val index = snapshot.currentIndex ?: return false
        return snapshot.queue.isNotEmpty() &&
            index in snapshot.queue.indices &&
            snapshot.currentItem == snapshot.queue[index]
    }
}
