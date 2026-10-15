package com.latticeward.musiccopilot.foundation

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.data.datastore.DataStorePreferencesRepository
import com.latticeward.musiccopilot.data.datastore.ProtoRestorableQueueStore
import com.latticeward.musiccopilot.data.datastore.RestoreProtoCodec
import com.latticeward.musiccopilot.data.datastore.RestoreSnapshotPolicy
import com.latticeward.musiccopilot.data.datastore.SnapshotValidity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test

private class InMemoryDataStore<T>(initial: T) : DataStore<T> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<T> = state
    override suspend fun updateData(transform: suspend (t: T) -> T): T =
        transform(state.value).also { state.value = it }
}

class LocalDataStoreContractsTest {
    private fun sample() = RestorableQueueSnapshot(
        schemaVersion = RestoreSnapshotPolicy.CURRENT_SCHEMA,
        queueRevision = 2, items = listOf(MediaRef("audio:1"), MediaRef("audio:2")),
        currentIndex = 1, positionMs = 4000, capturedAtEpochMs = 12345
    )

    @Test fun protobufRoundtripPreservesOnlyApprovedOpaqueFields() = runBlocking {
        val output = ByteArrayOutputStream()
        RestoreProtoCodec.writeTo(sample(), output)
        val bytes = output.toByteArray()
        assertTrue(bytes.isNotEmpty())
        val restored = RestoreProtoCodec.readFrom(ByteArrayInputStream(bytes))
        assertEquals(sample(), restored)
        assertFalse(bytes.toString(Charsets.UTF_8).contains("synthetic title"))
    }

    @Test(expected = CorruptionException::class)
    fun malformedWirePayloadFailsClosed() = runBlocking<Unit> {
        RestoreProtoCodec.readFrom(ByteArrayInputStream(byteArrayOf(0xff.toByte())))
    }

    @Test fun corruptAndFutureSchemaCannotBecomePlayableSnapshot() = runBlocking {
        val future = sample().copy(schemaVersion = 99)
        val corrupt = sample().copy(positionMs = -100)
        assertEquals(SnapshotValidity.FutureSchema, RestoreSnapshotPolicy.validate(future))
        assertEquals(SnapshotValidity.Corrupt, RestoreSnapshotPolicy.validate(corrupt))
        val fake = InMemoryDataStore(RestoreSnapshotPolicy.empty())
        val port = ProtoRestorableQueueStore(fake, TestDispatcherProvider())
        assertNull((port.read() as AppResult.Success<*>).value)
        assertTrue(port.write(future) is AppResult.Failure)
        assertEquals(AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA,
            (port.write(future) as AppResult.Failure).error.code)
        assertTrue(port.write(corrupt) is AppResult.Failure)
        assertNull((port.read() as AppResult.Success<*>).value)
        assertTrue(port.write(sample()) is AppResult.Success<*>)
        assertEquals(sample(), (port.read() as AppResult.Success<*>).value)
        assertTrue(port.clear() is AppResult.Success<*>)
        assertNull((port.read() as AppResult.Success<*>).value)
    }

    @Test fun preferencesPersistUpdatesOnlyAsControlState() = runBlocking<Unit> {
        val store = InMemoryDataStore<Preferences>(emptyPreferences())
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val repo = DataStorePreferencesRepository(store, TestDispatcherProvider(), scope)
        assertEquals(CloudSpeechConsent.NotAsked, repo.preferences.value.cloudSpeechConsent)
        assertTrue(repo.update(PreferenceUpdate.SetOnboardingComplete(true)) is AppResult.Success<*>)
        assertTrue(repo.update(PreferenceUpdate.SetCloudSpeechConsent(CloudSpeechConsent.Declined))
            is AppResult.Success<*>)
        assertEquals(CloudSpeechConsent.Declined, repo.preferences.value.cloudSpeechConsent)
        assertTrue(repo.preferences.value.onboardingComplete)
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}
