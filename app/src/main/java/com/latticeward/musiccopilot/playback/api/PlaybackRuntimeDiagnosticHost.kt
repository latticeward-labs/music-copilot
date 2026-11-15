package com.latticeward.musiccopilot.playback.api

import com.latticeward.musiccopilot.core.result.AppErrorCodes

/**
 * Issue #12 P07: service -> app composition boundary.
 * Closed vocabulary: the service NEVER passes AppError, MediaRef, a Throwable,
 * PlaybackException, message, URI, AudioDeviceInfo or a user identifier.
 * The host is observational only: cannot change service/player state.
 */
enum class PlayerRuntimeErrorCode {
    PLAYBACK_BUSY, PLAYBACK_REJECTED, PLAYBACK_FAILED,
    CONTROLLER_DISCONNECTED, CONTROLLER_CONNECT_FAILED, CONTROLLER_TIMEOUT,
    MEDIA_ITEM_MISSING, MEDIA_ITEM_CORRUPT, MEDIA_ITEM_UNPLAYABLE,
    FOCUS_NOT_RECOVERED, ROUTE_INVALIDATED, CALL_OR_ROUTE_SAFE_PAUSED;

    companion object {
        fun fromKnownServiceCode(code: String): PlayerRuntimeErrorCode? = when (code) {
            AppErrorCodes.PLAYBACK_BUSY -> PLAYBACK_BUSY
            AppErrorCodes.PLAYBACK_REJECTED -> PLAYBACK_REJECTED
            AppErrorCodes.PLAYBACK_FAILED -> PLAYBACK_FAILED
            AppErrorCodes.CONTROLLER_DISCONNECTED -> CONTROLLER_DISCONNECTED
            AppErrorCodes.CONTROLLER_CONNECT_FAILED -> CONTROLLER_CONNECT_FAILED
            AppErrorCodes.CONTROLLER_TIMEOUT -> CONTROLLER_TIMEOUT
            AppErrorCodes.MEDIA_ITEM_MISSING -> MEDIA_ITEM_MISSING
            AppErrorCodes.MEDIA_ITEM_CORRUPT -> MEDIA_ITEM_CORRUPT
            AppErrorCodes.MEDIA_ITEM_UNPLAYABLE -> MEDIA_ITEM_UNPLAYABLE
            AppErrorCodes.FOCUS_NOT_RECOVERED -> FOCUS_NOT_RECOVERED
            AppErrorCodes.ROUTE_INVALIDATED -> ROUTE_INVALIDATED
            AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED -> CALL_OR_ROUTE_SAFE_PAUSED
            // Includes PERMISSION_REVOKED: currently missing from P03
            // PlayerError allowlist; DROP rather than silently widening it.
            else -> null
        }
    }
}

interface PlaybackRuntimeDiagnosticHost {
    /** Called after an authoritative service snapshot is committed. */
    fun observeServicePlayerError(error: PlayerRuntimeErrorCode)
}
