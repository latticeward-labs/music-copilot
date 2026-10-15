package com.latticeward.musiccopilot.core.result

import com.latticeward.musiccopilot.core.model.MediaRef

/**
 * Public, sanitized errors. Codes are stable machine-readable identifiers;
 * exception messages, paths, titles and transcripts MUST NOT be put in [code].
 * Infrastructure adapters translate exceptions into these safe categories.
 */
sealed interface AppError {
    val code: String
    val recoverability: Recoverability
    val recoveryAction: RecoveryAction

    data class Permission(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Library(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class MediaItem(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
        val mediaRef: MediaRef? = null,
    ) : AppError

    data class Playback(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Controller(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Voice(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Interruption(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Storage(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Privacy(
        override val code: String,
        override val recoverability: Recoverability,
        override val recoveryAction: RecoveryAction,
    ) : AppError

    data class Quality(
        override val code: String,
        override val recoverability: Recoverability = Recoverability.RECOVERABLE,
        override val recoveryAction: RecoveryAction = RecoveryAction.None,
    ) : AppError
}

/** Safe initial code families from Issue #16 section 3.1; add codes only via owner review. */
object AppErrorCodes {
    const val PERMISSION_DENIED = "PERMISSION_DENIED"
    const val PERMISSION_PERMANENTLY_DENIED = "PERMISSION_PERMANENTLY_DENIED"
    const val PERMISSION_REVOKED = "PERMISSION_REVOKED"
    const val LIBRARY_SCAN_FAILED = "LIBRARY_SCAN_FAILED"
    const val LIBRARY_INDEX_CORRUPT = "LIBRARY_INDEX_CORRUPT"
    const val LIBRARY_RECONCILE_FAILED = "LIBRARY_RECONCILE_FAILED"
    const val MEDIA_ITEM_MISSING = "MEDIA_ITEM_MISSING"
    const val MEDIA_ITEM_CORRUPT = "MEDIA_ITEM_CORRUPT"
    const val MEDIA_ITEM_UNPLAYABLE = "MEDIA_ITEM_UNPLAYABLE"
    const val PLAYBACK_BUSY = "PLAYBACK_BUSY"
    const val PLAYBACK_REJECTED = "PLAYBACK_REJECTED"
    const val PLAYBACK_FAILED = "PLAYBACK_FAILED"
    const val CONTROLLER_DISCONNECTED = "CONTROLLER_DISCONNECTED"
    const val CONTROLLER_CONNECT_FAILED = "CONTROLLER_CONNECT_FAILED"
    const val CONTROLLER_TIMEOUT = "CONTROLLER_TIMEOUT"
    const val VOICE_UNAVAILABLE = "VOICE_UNAVAILABLE"
    const val VOICE_TIMEOUT = "VOICE_TIMEOUT"
    const val VOICE_NO_MATCH = "VOICE_NO_MATCH"
    const val VOICE_SECOND_ATTEMPT_FAILED = "VOICE_SECOND_ATTEMPT_FAILED"
    const val VOICE_STALE_SESSION = "VOICE_STALE_SESSION"
    const val FOCUS_NOT_RECOVERED = "FOCUS_NOT_RECOVERED"
    const val ROUTE_INVALIDATED = "ROUTE_INVALIDATED"
    const val CALL_OR_ROUTE_SAFE_PAUSED = "CALL_OR_ROUTE_SAFE_PAUSED"
    const val STORAGE_READ_FAILED = "STORAGE_READ_FAILED"
    const val STORAGE_WRITE_FAILED = "STORAGE_WRITE_FAILED"
    const val SNAPSHOT_CORRUPT = "SNAPSHOT_CORRUPT"
    const val SNAPSHOT_FUTURE_SCHEMA = "SNAPSHOT_FUTURE_SCHEMA"
    const val CONSENT_REQUIRED = "CONSENT_REQUIRED"
    const val CLEAR_PARTIAL = "CLEAR_PARTIAL"
    const val RETENTION_NOT_APPROVED = "RETENTION_NOT_APPROVED"
    const val QUALITY_DROPPED = "QUALITY_DROPPED"
    const val QUALITY_ADAPTER_UNAVAILABLE = "QUALITY_ADAPTER_UNAVAILABLE"
}
