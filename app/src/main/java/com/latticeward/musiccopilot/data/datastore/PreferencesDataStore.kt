package com.latticeward.musiccopilot.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.AppPreferences
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Preference records never contain current Android permission truth. */
object PreferencesStoreFactory {
    fun create(context: Context, scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = scope) {
            File(context.applicationContext.filesDir, "datastore/musico_preferences.preferences_pb")
                .apply { parentFile?.mkdirs() }
        }
}

class DataStorePreferencesRepository(
    private val store: DataStore<Preferences>,
    private val dispatchers: DispatcherProvider,
    scope: CoroutineScope,
) : PreferencesRepository {
    private val onboardingKey = booleanPreferencesKey("onboarding_complete")
    private val bluetoothKey = stringPreferencesKey("bluetooth_hint")
    private val consentKey = stringPreferencesKey("cloud_speech_consent")

    private val mutable = MutableStateFlow(AppPreferences(false, null, CloudSpeechConsent.NotAsked))
    override val preferences: StateFlow<AppPreferences> = mutable

    init {
        scope.launch(dispatchers.io) {
            try {
                store.data.collect { mutable.value = decode(it) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                // Safe defaults remain visible; no fake permission/consent grants.
                mutable.value = AppPreferences(false, null, CloudSpeechConsent.NotAsked)
            }
        }
    }

    private fun decode(p: Preferences): AppPreferences = AppPreferences(
        onboardingComplete = p[onboardingKey] ?: false,
        selectedBluetoothHint = p[bluetoothKey],
        cloudSpeechConsent = p[consentKey]?.let { value ->
            CloudSpeechConsent.entries.firstOrNull { it.name == value }
        } ?: CloudSpeechConsent.NotAsked,
    )

    override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> =
        withContext(dispatchers.io) {
            try {
                val committed = store.edit { p ->
                    when (update) {
                        is PreferenceUpdate.SetOnboardingComplete ->
                            p[onboardingKey] = update.complete
                        is PreferenceUpdate.SetBluetoothHint ->
                            if (update.value == null) p.remove(bluetoothKey)
                            else p[bluetoothKey] = update.value
                        is PreferenceUpdate.SetCloudSpeechConsent ->
                            p[consentKey] = update.value.name
                    }
                }
                val saved = decode(committed)
                mutable.value = saved
                AppResult.Success(saved)
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) {
                AppResult.Failure(
                    AppError.Storage(AppErrorCodes.STORAGE_WRITE_FAILED,
                        Recoverability.RECOVERABLE, RecoveryAction.Retry))
            }
        }
}

