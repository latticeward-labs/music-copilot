package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import kotlinx.coroutines.CancellationException
import java.util.UUID

/**
 * P06 voice-to-existing-Media3-port boundary. Creates no player/controller.
 * Owner Coordinator claims once before calling this; the service's existing
 * serialized worker deduplicates same commandId + payload on receipt.
 * A retry recognition attempt gets a different, deterministic command ID.
 */
internal class VoicePlaybackDispatcher(private val gateway: PlaybackGateway) {

    suspend fun execute(
        intent: RestrictedIntent,
        sessionId: VoiceSessionId,
        attempt: Int,
    ): AppResult<PlaybackSnapshot> {
        val command = commandFor(intent, sessionId, attempt) ?: return rejected()
        if (gateway.connectionState.value != PlaybackConnectionState.Ready) return rejected()
        return try {
            // Only an authoritative acknowledged outcome is considered success.
            // Production controller additionally checks the Media3 ACK's ID.
            gateway.execute(command)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            rejected()
        }
    }

    companion object {
        // Never persist or log this process-local random namespace. The
        // MediaSessionService may outlive Activity/app process recreation:
        // a local sequence beginning at 1 must not collide with a cached
        // command receipt from the previous process.
        private val processNonce: String = UUID.randomUUID().toString().replace("-", "")

        fun commandFor(
            intent: RestrictedIntent,
            sessionId: VoiceSessionId,
            attempt: Int,
        ): PlaybackCommand? {
            if (sessionId.value <= 0L || attempt !in 1..2) return null
            val id = CommandId("voice-$processNonce-${sessionId.value}-$attempt")
            return when (intent) {
                RestrictedIntent.PLAY -> PlaybackCommand.Play(id)
                RestrictedIntent.PAUSE -> PlaybackCommand.Pause(id)
                RestrictedIntent.NEXT -> PlaybackCommand.Next(id)
                RestrictedIntent.PREVIOUS -> PlaybackCommand.Previous(id)
                RestrictedIntent.SEEK_BACK -> PlaybackCommand.SeekBy(
                    id, -PlaybackNavigationPolicy.VOICE_SEEK_BACK_MS
                )
                RestrictedIntent.PLAY_ARTIST -> null
            }
        }

        private fun rejected(): AppResult.Failure = AppResult.Failure(
            AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED,
                Recoverability.RECOVERABLE,
                RecoveryAction.ManualFallback,
            )
        )
    }
}
