package com.latticeward.musiccopilot.foundation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.FirstUsePage
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.datastore.DataStorePreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

private class CheckpointMemoryStore(initial: Preferences) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        transform(state.value).also { state.value = it }
}

class Issue5Phase2CheckpointPersistenceTest {
    @Test fun checkpointSurvivesRepositoryRecreationAndOnlyAcknowledgedWriteCompletes() = runBlocking {
        val backing = CheckpointMemoryStore(emptyPreferences())
        val scope1 = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val first = DataStorePreferencesRepository(backing, TestDispatcherProvider(), scope1)
        assertEquals(FirstUsePage.Value, first.preferences.value.onboardingCheckpoint)
        assertFalse(first.preferences.value.onboardingComplete)
        val write = first.update(PreferenceUpdate.SetOnboardingCheckpoint(FirstUsePage.SafetyAndVoice))
        assertTrue(write is AppResult.Success<*>)
        assertEquals(FirstUsePage.SafetyAndVoice, first.preferences.value.onboardingCheckpoint)
        assertFalse(first.preferences.value.onboardingComplete)
        scope1.cancel()

        val scope2 = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val reopened = DataStorePreferencesRepository(backing, TestDispatcherProvider(), scope2)
        assertEquals(FirstUsePage.SafetyAndVoice, reopened.preferences.value.onboardingCheckpoint)
        assertFalse(reopened.preferences.value.onboardingComplete)
        assertTrue(reopened.update(PreferenceUpdate.SetOnboardingComplete(true)) is AppResult.Success<*>)
        assertTrue(reopened.preferences.value.onboardingComplete)
        scope2.cancel()
    }
}
