package com.latticeward.musiccopilot.core.application.bootstrap

/**
 * P02 pure first-use contract. No Android/UI/Player access or implicit permission prompts.
 * Progress and completion are committed through PreferencesRepository in a later phase.
 */
enum class FirstUsePage { Value, SafetyAndVoice }
enum class FirstUseMode { Initial, EducationReplay }

sealed interface FirstUseState {
    val revision: Long

    data class Education(
        val page: FirstUsePage,
        val mode: FirstUseMode,
        override val revision: Long = 0L,
    ) : FirstUseState

    data class Saving(
        val pageBefore: FirstUsePage,
        val target: FirstUsePage?,
        val operationId: Long,
        override val revision: Long,
    ) : FirstUseState

    data class SaveError(
        val pageBefore: FirstUsePage,
        val target: FirstUsePage?,
        override val revision: Long,
    ) : FirstUseState

    data class Finished(
        val replay: Boolean,
        override val revision: Long,
    ) : FirstUseState
}

sealed interface FirstUseEvent {
    data object Continue : FirstUseEvent
    data object Back : FirstUseEvent
    data object Skip : FirstUseEvent
    data object Retry : FirstUseEvent
    data class Saved(val operationId: Long) : FirstUseEvent
    data class SaveFailed(val operationId: Long) : FirstUseEvent
}

sealed interface FirstUseEffect {
    data class PersistCheckpoint(val page: FirstUsePage, val operationId: Long) : FirstUseEffect
    data class PersistCompletion(val operationId: Long) : FirstUseEffect
    data object EnterReadiness : FirstUseEffect
    data object ReturnToOrigin : FirstUseEffect
    data object ExitWithoutCompletion : FirstUseEffect
}

data class FirstUseStep(val state: FirstUseState, val effects: List<FirstUseEffect> = emptyList())

/**
 * Only ACK from the exact pending DataStore write can advance first-run navigation.
 * Replay is read-only (no Save effect, no scan, permission request or playback command).
 */
object FirstUseMachine {
    fun enter(
        complete: Boolean,
        checkpoint: FirstUsePage,
        replay: Boolean = false,
    ): FirstUseState = when {
        replay -> FirstUseState.Education(FirstUsePage.Value, FirstUseMode.EducationReplay)
        complete -> FirstUseState.Finished(replay = false, revision = 0L)
        else -> FirstUseState.Education(checkpoint, FirstUseMode.Initial)
    }

    fun reduce(current: FirstUseState, event: FirstUseEvent): FirstUseStep {
        if (current is FirstUseState.Saving) {
            return when (event) {
                is FirstUseEvent.Saved -> if (event.operationId == current.operationId) {
                    if (current.target == null) {
                        FirstUseStep(
                            FirstUseState.Finished(false, current.revision),
                            listOf(FirstUseEffect.EnterReadiness),
                        )
                    } else {
                        FirstUseStep(
                            FirstUseState.Education(current.target, FirstUseMode.Initial, current.revision),
                        )
                    }
                } else FirstUseStep(current)
                is FirstUseEvent.SaveFailed -> if (event.operationId == current.operationId) {
                    FirstUseStep(
                        FirstUseState.SaveError(current.pageBefore, current.target, current.revision),
                    )
                } else FirstUseStep(current)
                else -> FirstUseStep(current)
            }
        }
        if (current is FirstUseState.SaveError) {
            return if (event == FirstUseEvent.Retry) {
                persist(current.pageBefore, current.target, current.revision)
            } else FirstUseStep(current)
        }
        if (current !is FirstUseState.Education) return FirstUseStep(current)
        if (current.mode == FirstUseMode.EducationReplay) {
            return when (event) {
                FirstUseEvent.Continue -> if (current.page == FirstUsePage.Value) {
                    FirstUseStep(current.copy(page = FirstUsePage.SafetyAndVoice))
                } else {
                    FirstUseStep(
                        FirstUseState.Finished(true, current.revision),
                        listOf(FirstUseEffect.ReturnToOrigin),
                    )
                }
                FirstUseEvent.Back -> if (current.page == FirstUsePage.SafetyAndVoice) {
                    FirstUseStep(current.copy(page = FirstUsePage.Value))
                } else FirstUseStep(
                    FirstUseState.Finished(true, current.revision),
                    listOf(FirstUseEffect.ReturnToOrigin),
                )
                FirstUseEvent.Skip -> FirstUseStep(
                    FirstUseState.Finished(true, current.revision),
                    listOf(FirstUseEffect.ReturnToOrigin),
                )
                else -> FirstUseStep(current)
            }
        }
        return when (event) {
            FirstUseEvent.Continue -> if (current.page == FirstUsePage.Value) {
                persist(current.page, FirstUsePage.SafetyAndVoice, current.revision)
            } else persist(current.page, null, current.revision)
            FirstUseEvent.Skip -> persist(current.page, null, current.revision)
            FirstUseEvent.Back -> if (current.page == FirstUsePage.SafetyAndVoice) {
                persist(current.page, FirstUsePage.Value, current.revision)
            } else FirstUseStep(
                FirstUseState.Finished(false, current.revision),
                listOf(FirstUseEffect.ExitWithoutCompletion),
            )
            else -> FirstUseStep(current)
        }
    }

    private fun persist(
        before: FirstUsePage,
        target: FirstUsePage?,
        revision: Long,
    ): FirstUseStep {
        val next = revision + 1
        val effect = if (target == null) {
            FirstUseEffect.PersistCompletion(next)
        } else {
            FirstUseEffect.PersistCheckpoint(target, next)
        }
        return FirstUseStep(
            FirstUseState.Saving(before, target, next, next),
            listOf(effect),
        )
    }
}
