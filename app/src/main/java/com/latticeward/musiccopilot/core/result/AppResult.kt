package com.latticeward.musiccopilot.core.result

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

enum class Recoverability {
    RECOVERABLE, USER_ACTION_REQUIRED, TERMINAL_FOR_ATTEMPT
}

enum class RecoveryAction {
    Retry, OpenSettings, Rebuild, Skip, ManualFallback, ManualResume, None
}

