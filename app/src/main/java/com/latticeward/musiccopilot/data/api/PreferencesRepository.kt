package com.latticeward.musiccopilot.data.api

import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.flow.StateFlow

enum class CloudSpeechConsent { NotApplicable, NotAsked, Declined, Granted, Revoked }

data class AppPreferences(
    val onboardingComplete: Boolean,
    val selectedBluetoothHint: String?,
    val cloudSpeechConsent: CloudSpeechConsent,
)

sealed interface PreferenceUpdate {
    data class SetOnboardingComplete(val complete: Boolean) : PreferenceUpdate
    data class SetBluetoothHint(val value: String?) : PreferenceUpdate
    data class SetCloudSpeechConsent(val value: CloudSpeechConsent) : PreferenceUpdate
}

/** Permission grants are never persisted as authoritative OS permission truth. */
interface PreferencesRepository {
    val preferences: StateFlow<AppPreferences>
    suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences>
}

