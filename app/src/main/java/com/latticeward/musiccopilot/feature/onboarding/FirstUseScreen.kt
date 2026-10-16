package com.latticeward.musiccopilot.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.latticeward.musiccopilot.R
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEffect
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMachine
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.FirstUsePage
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.launch

/** WF-02: only two approved value slides, real persisted checkpoint ACK, no OS prompts. */
@Composable
fun FirstUseScreen(
    repository: PreferencesRepository,
    onComplete: () -> Unit,
    onExit: () -> Unit,
) {
    var machine by remember(repository) {
        mutableStateOf(FirstUseMachine.enter(
            repository.preferences.value.onboardingComplete,
            repository.preferences.value.onboardingCheckpoint,
        ))
    }
    val scope = rememberCoroutineScope()

    fun dispatch(event: FirstUseEvent) {
        val step = FirstUseMachine.reduce(machine, event)
        machine = step.state
        step.effects.forEach { effect ->
            when (effect) {
                is FirstUseEffect.PersistCheckpoint, is FirstUseEffect.PersistCompletion -> {
                    scope.launch {
                        val result = when (effect) {
                            is FirstUseEffect.PersistCheckpoint ->
                                repository.update(PreferenceUpdate.SetOnboardingCheckpoint(effect.page))
                            is FirstUseEffect.PersistCompletion ->
                                repository.update(PreferenceUpdate.SetOnboardingComplete(true))
                            else -> error("Unreachable effect")
                        }
                        val operationId = when (effect) {
                            is FirstUseEffect.PersistCheckpoint -> effect.operationId
                            is FirstUseEffect.PersistCompletion -> effect.operationId
                            else -> error("Unreachable effect")
                        }
                        val next = FirstUseMachine.reduce(
                            machine,
                            if (result is AppResult.Success) FirstUseEvent.Saved(operationId)
                            else FirstUseEvent.SaveFailed(operationId),
                        )
                        machine = next.state
                        if (next.effects.contains(FirstUseEffect.EnterReadiness)) onComplete()
                    }
                }
                FirstUseEffect.EnterReadiness -> onComplete()
                FirstUseEffect.ExitWithoutCompletion, FirstUseEffect.ReturnToOrigin -> onExit()
            }
        }
    }
    BackHandler { dispatch(FirstUseEvent.Back) }

    val saving = machine is FirstUseState.Saving
    val error = machine is FirstUseState.SaveError
    val currentPage = when (val s = machine) {
        is FirstUseState.Education -> s.page
        is FirstUseState.Saving -> s.pageBefore
        is FirstUseState.SaveError -> s.pageBefore
        is FirstUseState.Finished -> FirstUsePage.SafetyAndVoice
    }
    val second = currentPage == FirstUsePage.SafetyAndVoice

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(if (second) R.string.first_use_step_two else R.string.first_use_step_one),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge,
            )
            Text(
                stringResource(if (second) R.string.first_use_second_title else R.string.first_use_first_title),
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(if (second) R.string.first_use_second_body else R.string.first_use_first_body),
                modifier = Modifier.padding(top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge,
            )
            if (error) {
                Text(
                    stringResource(R.string.first_use_save_error),
                    modifier = Modifier.padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(
                onClick = { dispatch(if (error) FirstUseEvent.Retry else FirstUseEvent.Continue) },
                enabled = !saving && machine !is FirstUseState.Finished,
                modifier = Modifier.padding(top = 24.dp).heightIn(min = 48.dp),
            ) {
                Text(stringResource(
                    if (error) R.string.action_retry
                    else if (second) R.string.action_finish else R.string.action_continue,
                ))
            }
            OutlinedButton(
                onClick = { dispatch(if (second) FirstUseEvent.Back else FirstUseEvent.Skip) },
                enabled = !saving && machine !is FirstUseState.Finished,
                modifier = Modifier.padding(top = 12.dp).heightIn(min = 48.dp),
            ) {
                Text(stringResource(if (second) R.string.action_back else R.string.action_skip))
            }
        }
    }
}
