package com.latticeward.musiccopilot.playback.command

import android.os.Bundle
import androidx.media3.session.SessionCommand
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState

/**
 * Versioned *in-process Media3 session command* transport; not persistence.
 * Only opaque MediaRefs and sanitized enum error codes cross the boundary.
 */
object PlaybackWireProtocol {
    const val ACTION_EXECUTE = "com.latticeward.musiccopilot.playback.EXECUTE_V1"
    const val ACTION_SNAPSHOT = "com.latticeward.musiccopilot.playback.SNAPSHOT_V1"
    const val KEY_SNAPSHOT = "snapshot"
    const val KEY_ERROR = "error_code"
    const val KEY_ERROR_FAMILY = "error_family"
    const val PROTOCOL_VERSION = 1

    fun executeCommand() = SessionCommand(ACTION_EXECUTE, Bundle.EMPTY)
    fun snapshotCommand() = SessionCommand(ACTION_SNAPSHOT, Bundle.EMPTY)

    fun encodeCommand(command: PlaybackCommand): Bundle = Bundle().apply {
        putInt("v", PROTOCOL_VERSION)
        putString("id", command.commandId.value)
        when (command) {
            is PlaybackCommand.ReplaceQueue -> {
                putString("type", "replace")
                putStringArrayList("items", ArrayList(command.items.map { it.value }))
                putInt("index", command.startIndex)
                putLong("start", command.startPositionMs)
                putBoolean("play", command.playWhenReady)
            }
            is PlaybackCommand.SelectQueueIndex -> {
                putString("type", "select_index")
                putInt("index", command.index)
                putLong("qrev", command.expectedQueueRevision)
                putBoolean("play", command.playWhenReady)
            }
            is PlaybackCommand.Play -> putString("type", "play")
            is PlaybackCommand.Pause -> putString("type", "pause")
            is PlaybackCommand.Next -> putString("type", "next")
            is PlaybackCommand.Previous -> putString("type", "previous")
            is PlaybackCommand.SeekBy -> { putString("type", "seek_by"); putLong("delta", command.deltaMs) }
            is PlaybackCommand.SeekTo -> { putString("type", "seek_to"); putLong("position", command.positionMs) }
            is PlaybackCommand.Stop -> putString("type", "stop")
        }
    }

    fun decodeCommand(args: Bundle): PlaybackCommand? {
        if (args.getInt("v", -1) != PROTOCOL_VERSION) return null
        val id = args.getString("id")?.takeIf { it.isNotBlank() && it.length <= 128 }
            ?.let(::CommandId) ?: return null
        return when (args.getString("type")) {
            "select_index" -> {
                val index = args.getInt("index", -1)
                val revision = args.getLong("qrev", -1L)
                if (index < 0 || index >= 5_000 || revision < 0L) return null
                PlaybackCommand.SelectQueueIndex(
                    id, index, revision, args.getBoolean("play", true)
                )
            }
            "play" -> PlaybackCommand.Play(id)
            "pause" -> PlaybackCommand.Pause(id)
            "stop" -> PlaybackCommand.Stop(id)
            "next" -> PlaybackCommand.Next(id)
            "previous" -> PlaybackCommand.Previous(id)
            "seek_by" -> PlaybackCommand.SeekBy(id, args.getLong("delta"))
            "seek_to" -> PlaybackCommand.SeekTo(id, args.getLong("position"))
            "replace" -> {
                val refs = args.getStringArrayList("items") ?: return null
                if (refs.isEmpty() || refs.size > 5_000 || refs.any {
                    it.isBlank() || it.length > 128
                }) return null
                PlaybackCommand.ReplaceQueue(
                    id, refs.map(::MediaRef), args.getInt("index", -1),
                    args.getLong("start", -1), args.getBoolean("play", false)
                )
            }
            else -> null
        }
    }

    fun encodeSnapshot(snapshot: PlaybackSnapshot): Bundle = Bundle().apply {
        putInt("v", PROTOCOL_VERSION)
        putLong("revision", snapshot.revision)
        putLong("queue_revision", snapshot.queueRevision)
        putString("state", snapshot.playbackState.name)
        putStringArrayList("queue", ArrayList(snapshot.queue.map { it.value }))
        putInt("index", snapshot.currentIndex ?: -1)
        putString("current", snapshot.currentItem?.value)
        putLong("position", snapshot.positionMs)
        putLong("duration", snapshot.durationMs ?: -1)
        putBoolean("seek", snapshot.canSeek)
        putBoolean("prev", snapshot.canSkipPrevious)
        putBoolean("next", snapshot.canSkipNext)
        snapshot.lastError?.let {
            putString(KEY_ERROR, it.code)
            putString(KEY_ERROR_FAMILY, when (it) {
                is AppError.MediaItem -> "media"
                is AppError.Interruption -> "interruption"
                else -> "playback"
            })
        }
    }

    fun decodeSnapshot(args: Bundle?): PlaybackSnapshot? {
        if (args == null || args.getInt("v", -1) != PROTOCOL_VERSION) return null
        val state = PlaybackState.entries.firstOrNull {
            it.name == args.getString("state")
        } ?: return null
        val refs = args.getStringArrayList("queue")?.takeIf { it.size <= 5_000 }
            ?.map(::MediaRef) ?: return null
        val index = args.getInt("index", -1).takeIf { it >= 0 }
        val current = args.getString("current")?.let(::MediaRef)
        if (index != null && index !in refs.indices) return null
        if ((index == null) != (current == null)) return null
        if (index != null && refs[index] != current) return null
        val error = args.getString(KEY_ERROR)?.let { code ->
            if (code !in ALLOWED_ERROR_CODES) return null
            when (args.getString(KEY_ERROR_FAMILY)) {
                "media" -> AppError.MediaItem(code, Recoverability.RECOVERABLE, RecoveryAction.Skip)
                "interruption" -> AppError.Interruption(code, Recoverability.RECOVERABLE, RecoveryAction.ManualResume)
                "playback" -> AppError.Playback(code, Recoverability.RECOVERABLE, RecoveryAction.Retry)
                else -> return null
            }
        }
        val revision = args.getLong("revision", -1L)
        val queueRevision = args.getLong("queue_revision", -1L)
        val position = args.getLong("position", -1L)
        if (revision < 0 || queueRevision < 0 || position < 0) return null
        return PlaybackSnapshot(
            revision, queueRevision, state, refs, index, current,
            position, args.getLong("duration", -1L).takeIf { it >= 0 },
            args.getBoolean("seek"), args.getBoolean("prev"),
            args.getBoolean("next"), error
        )
    }

    fun errorExtras(error: AppError): Bundle = Bundle().apply {
        putString(KEY_ERROR, error.code)
        putString(KEY_ERROR_FAMILY, when (error) {
            is AppError.Permission -> "permission"
            is AppError.MediaItem -> "media"
            is AppError.Controller -> "controller"
            is AppError.Interruption -> "interruption"
            else -> "playback"
        })
    }

    fun decodeFailure(args: Bundle): AppResult.Failure {
        val code = args.getString(KEY_ERROR).orEmpty().takeIf { it in ALLOWED_ERROR_CODES }
            ?: AppErrorCodes.PLAYBACK_REJECTED
        val error = when (args.getString(KEY_ERROR_FAMILY)) {
            "permission" -> AppError.Permission(
                code, Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings
            )
            "controller" -> AppError.Controller(code, Recoverability.RECOVERABLE, RecoveryAction.Retry)
            "media" -> AppError.MediaItem(code, Recoverability.RECOVERABLE, RecoveryAction.Skip)
            "interruption" -> AppError.Interruption(code, Recoverability.RECOVERABLE, RecoveryAction.ManualResume)
            else -> AppError.Playback(code, Recoverability.RECOVERABLE, RecoveryAction.Retry)
        }
        return AppResult.Failure(error)
    }

    private val ALLOWED_ERROR_CODES = setOf(
        AppErrorCodes.PLAYBACK_BUSY, AppErrorCodes.PLAYBACK_REJECTED,
        AppErrorCodes.PLAYBACK_FAILED, AppErrorCodes.PERMISSION_REVOKED,
        AppErrorCodes.CONTROLLER_DISCONNECTED,
        AppErrorCodes.CONTROLLER_CONNECT_FAILED, AppErrorCodes.CONTROLLER_TIMEOUT,
        AppErrorCodes.MEDIA_ITEM_MISSING, AppErrorCodes.MEDIA_ITEM_CORRUPT,
        AppErrorCodes.MEDIA_ITEM_UNPLAYABLE, AppErrorCodes.ROUTE_INVALIDATED,
        AppErrorCodes.FOCUS_NOT_RECOVERED, AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED
    )
}
