package com.latticeward.musiccopilot.feature.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.createSavedStateHandle
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEffect
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMachine
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.launch

/**
 * P04 destination presentation owner. ViewModel retains a pending save across
 * configuration changes; DataStore remains the final ACKed checkpoint authority
 * on a recreated process. SavedStateHandle retains only non-sensitive UI state.
 *
 * No Android permission request, MediaStore scan or Player/Voice operation.
 */
class FirstUseViewModel(
    private val repository: PreferencesRepository,
    private val replay: Boolean,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var machine by mutableStateOf(
        FirstUseMachine.enter(
            complete = repository.preferences.value.onboardingComplete,
            checkpoint = repository.preferences.value.onboardingCheckpoint,
            replay = replay,
        ),
    )
        private set

    init {
        savedStateHandle["educationReplay"] = replay
        if (machine is FirstUseState.Education) {
            // This mirrors an ACKed DataStore page; never treat it as OS grant truth.
            savedStateHandle["ackedFirstUsePage"] = (machine as FirstUseState.Education).page.name
        }
    }

    fun dispatch(event: FirstUseEvent, onComplete: () -> Unit, onExit: () -> Unit) {
        val step = FirstUseMachine.reduce(machine, event)
        machine = step.state
        step.effects.forEach { effect ->
            when (effect) {
                is FirstUseEffect.PersistCheckpoint, is FirstUseEffect.PersistCompletion -> {
                    viewModelScope.launch {
                        val update = when (effect) {
                            is FirstUseEffect.PersistCheckpoint ->
                                PreferenceUpdate.SetOnboardingCheckpoint(effect.page)
                            is FirstUseEffect.PersistCompletion ->
                                PreferenceUpdate.SetOnboardingComplete(true)
                            else -> error("Unreachable effect")
                        }
                        val operationId = when (effect) {
                            is FirstUseEffect.PersistCheckpoint -> effect.operationId
                            is FirstUseEffect.PersistCompletion -> effect.operationId
                            else -> error("Unreachable effect")
                        }
                        val result = repository.update(update)
                        val next = FirstUseMachine.reduce(
                            machine,
                            if (result is AppResult.Success) FirstUseEvent.Saved(operationId)
                            else FirstUseEvent.SaveFailed(operationId),
                        )
                        machine = next.state
                        if (next.state is FirstUseState.Education) {
                            savedStateHandle["ackedFirstUsePage"] =
                                (next.state as FirstUseState.Education).page.name
                        }
                        if (next.effects.contains(FirstUseEffect.EnterReadiness)) onComplete()
                    }
                }
                FirstUseEffect.EnterReadiness -> onComplete()
                FirstUseEffect.ReturnToOrigin, FirstUseEffect.ExitWithoutCompletion -> onExit()
            }
        }
    }

    companion object {
        fun factory(repository: PreferencesRepository, replay: Boolean): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
                    FirstUseViewModel(repository, replay, extras.createSavedStateHandle()) as T
            }
    }
}
