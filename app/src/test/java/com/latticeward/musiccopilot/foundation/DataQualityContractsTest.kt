package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.*
import com.latticeward.musiccopilot.data.api.*
import com.latticeward.musiccopilot.quality.api.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class DataQualityContractsTest {
    @Test fun fakeLibraryImplementsAllPublicReadReconcileAndVoicePorts() = runBlocking {
        val library: LibraryRepository = FakeLibrary()
        assertEquals(LibraryIndexStatus.Empty, library.index.value.status)
        assertTrue(library.reconcile() is AppResult.Success<*>)
        assertTrue(library.search(LibraryQuery("synthetic", 2)) is AppResult.Success<*>)
        assertTrue(library.get(MediaRef("absent")) is AppResult.Failure)
        assertTrue(library.resolveVoiceCandidates("synthetic", 2) is AppResult.Success<*>)
    }

    @Test fun fakePreferencesAreStatefulWithoutPersistingPermissionTruth() = runBlocking {
        val repo: PreferencesRepository = FakePreferences()
        assertEquals(CloudSpeechConsent.NotAsked, repo.preferences.value.cloudSpeechConsent)
        val update = repo.update(PreferenceUpdate.SetCloudSpeechConsent(CloudSpeechConsent.Declined))
        assertTrue(update is AppResult.Success<*>)
        assertEquals(CloudSpeechConsent.Declined, repo.preferences.value.cloudSpeechConsent)
        assertNull(repo.preferences.value.selectedBluetoothHint)
    }

    @Test fun nonAuthoritativeRestoreStoreRoundTripsSyntheticOpaqueRefs() = runBlocking {
        val store: RestorableQueueStore = FakeRestorableStore()
        assertNull((store.read() as AppResult.Success<*>).value)
        val data = RestorableQueueSnapshot(1, 5, listOf(MediaRef("fake:ref")), 0, 0, 12)
        assertTrue(store.write(data) is AppResult.Success<*>)
        assertEquals(data, (store.read() as AppResult.Success<*>).value)
        assertTrue(store.clear() is AppResult.Success<*>)
        assertNull((store.read() as AppResult.Success<*>).value)
    }

    @Test fun qualityUsesClosedEventTypesAndSanitizedSinkPortOnly() {
        val evt: TelemetryEvent = TelemetryEvent.PlayerError("PLAYBACK_FAILED", RouteType.Unknown)
        assertTrue(evt is TelemetryEvent.PlayerError)
        assertEquals(8, TelemetryEvent::class.java.declaredClasses.size)
        assertEquals(1, TelemetrySink::class.java.methods.single { it.name.startsWith("tryEmit") }.parameterTypes.size)
    }

    @Test fun fakeQualityGateFailureDoesNotRequireProductNetworkOrCloudSdk() {
        val gate: TelemetryPrivacyGate = FakeGate()
        val rejection = gate.sanitize(TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High))
        assertTrue(rejection is AppResult.Failure)
        assertEquals(RecoveryAction.None, (rejection as AppResult.Failure).error.recoveryAction)
    }
}

private class FakeLibrary : LibraryRepository {
    private val mutable = MutableStateFlow(LibraryIndexSnapshot(0, LibraryIndexStatus.Empty, 0))
    override val index: StateFlow<LibraryIndexSnapshot> = mutable
    override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
    override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
        AppResult.Success(LibraryReconcileResult(0, 0, 0, 0, 1))
    override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
        AppResult.Success(emptyList())
    override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
        AppResult.Failure(AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_MISSING,
            Recoverability.RECOVERABLE, RecoveryAction.Skip, ref))
    override suspend fun resolveVoiceCandidates(
        normalizedTarget: String, limit: Int
    ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())
}

private class FakePreferences : PreferencesRepository {
    private val mutable = MutableStateFlow(AppPreferences(false, null, CloudSpeechConsent.NotAsked))
    override val preferences: StateFlow<AppPreferences> = mutable
    override val loadState: StateFlow<PreferencesLoadState> = MutableStateFlow(PreferencesLoadState.Ready)
    override suspend fun reload(): AppResult<AppPreferences> = AppResult.Success(mutable.value)
    override suspend fun update(update: PreferenceUpdate): AppResult<AppPreferences> {
        mutable.value = when (update) {
            is PreferenceUpdate.SetOnboardingCheckpoint ->
                mutable.value.copy(onboardingCheckpoint = update.page)
            is PreferenceUpdate.SetOnboardingComplete ->
                mutable.value.copy(onboardingComplete = update.complete)
            is PreferenceUpdate.SetBluetoothHint ->
                mutable.value.copy(selectedBluetoothHint = update.value)
            is PreferenceUpdate.SetCloudSpeechConsent ->
                mutable.value.copy(cloudSpeechConsent = update.value)
        }
        return AppResult.Success(mutable.value)
    }
}

private class FakeRestorableStore : RestorableQueueStore {
    private var value: RestorableQueueSnapshot? = null
    override suspend fun read(): AppResult<RestorableQueueSnapshot?> = AppResult.Success(value)
    override suspend fun write(snapshot: RestorableQueueSnapshot): AppResult<Unit> {
        value = snapshot
        return AppResult.Success(Unit)
    }
    override suspend fun clear(): AppResult<Unit> {
        value = null
        return AppResult.Success(Unit)
    }
}

private class FakeGate : TelemetryPrivacyGate {
    override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> =
        AppResult.Failure(AppError.Quality(AppErrorCodes.QUALITY_DROPPED))
}
