package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.time.Clock
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.CloudSpeechConsent
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.PreferenceUpdate
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.RouteType
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

internal class TestDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Unconfined
    override val io: CoroutineDispatcher = Dispatchers.Unconfined
    override val default: CoroutineDispatcher = Dispatchers.Unconfined
}

class ApplicationCompositionTest {
    private fun graph() = ApplicationBindings(TestDispatcherProvider(), Clock { 123L })

    @Test fun eachApplicationGraphOwnsExactlyOneOfEachPublicService() {
        val app = graph()
        try {
            assertSame(app.playbackGateway, app.playbackGateway)
            assertSame(app.voiceSessionCoordinator, app.voiceSessionCoordinator)
            assertSame(app.libraryRepository, app.libraryRepository)
            assertSame(app.preferencesRepository, app.preferencesRepository)
            assertSame(app.restorableQueueStore, app.restorableQueueStore)
            assertSame(app.telemetryPrivacyGate, app.telemetryPrivacyGate)
            assertSame(app.telemetrySink, app.telemetrySink)
            assertEquals(123L, app.clock.nowEpochMillis())
        } finally { app.close() }
    }

    @Test fun activityRecreationCannotConstructOrReplaceAnyGraphOwnedService() {
        // Two simulated presentation attachments observe exactly the same application graph.
        val app = graph()
        try {
            val screenA = app.playbackGateway to app.voiceSessionCoordinator
            val screenB = app.playbackGateway to app.voiceSessionCoordinator
            assertSame(screenA.first, screenB.first)
            assertSame(screenA.second, screenB.second)
            assertEquals(PlaybackConnectionState.Disconnected, app.playbackGateway.connectionState.value)
            assertEquals(PlaybackState.Idle, app.playbackGateway.snapshot.value.playbackState)
            assertEquals(VoiceSessionState.Idle, app.voiceSessionCoordinator.state.value)
        } finally { app.close() }
        // Physical Activity recreation/service continuity is a Phase 7/13 instrumentation gate.
    }

    @Test fun independentApplicationGraphsDoNotLeakEachOthersScopesOrPorts() {
        val a = graph()
        val b = graph()
        try {
            assertNotSame(a.playbackGateway, b.playbackGateway)
            assertNotSame(a.voiceSessionCoordinator, b.voiceSessionCoordinator)
            assertNotSame(a.repositoryScope.coroutineContext[Job],
                b.repositoryScope.coroutineContext[Job])
            assertNotSame(a.applicationScope.coroutineContext[Job],
                b.applicationScope.coroutineContext[Job])
            assertNotSame(a.voiceScope.coroutineContext[Job],
                b.voiceScope.coroutineContext[Job])
            a.close()
            assertFalse(a.applicationScope.coroutineContext[Job]!!.isActive)
            assertTrue(b.applicationScope.coroutineContext[Job]!!.isActive)
        } finally { a.close(); b.close() }
    }

    @Test fun ownerJobsAreStructuredIndependentSiblingsAndCloseCancelsAll() {
        val a = graph()
        val controllerJob = a.applicationScope.coroutineContext[Job]!!
        val repositoryJob = a.repositoryScope.coroutineContext[Job]!!
        val voiceJob = a.voiceScope.coroutineContext[Job]!!
        assertTrue(controllerJob.isActive)
        assertTrue(repositoryJob.isActive)
        assertTrue(voiceJob.isActive)
        assertNotSame(controllerJob, repositoryJob)
        assertNotSame(repositoryJob, voiceJob)
        a.close()
        assertFalse(controllerJob.isActive)
        assertFalse(repositoryJob.isActive)
        assertFalse(voiceJob.isActive)
    }

    @Test fun disconnectedPlaybackRejectsCommandWithoutOptimisticPlaying() = runBlocking {
        val app = graph()
        try {
            val before = app.playbackGateway.snapshot.value
            val result = app.playbackGateway.execute(PlaybackCommand.Play(CommandId("synthetic")))
            assertTrue(result is AppResult.Failure)
            val error = (result as AppResult.Failure).error
            assertTrue(error is AppError.Controller)
            assertEquals(AppErrorCodes.CONTROLLER_DISCONNECTED, error.code)
            assertSame(before, app.playbackGateway.snapshot.value)
            assertEquals(PlaybackState.Idle, app.playbackGateway.snapshot.value.playbackState)
        } finally { app.close() }
    }

    @Test fun repositoriesNeverPretendPermissionMediaOrConsentWasPersisted() = runBlocking {
        val app = graph()
        try {
            assertEquals(LibraryIndexStatus.Unavailable, app.libraryRepository.index.value.status)
            assertTrue(app.libraryRepository.reconcile() is AppResult.Failure)
            assertEquals(CloudSpeechConsent.NotAsked,
                app.preferencesRepository.preferences.value.cloudSpeechConsent)
            assertTrue(
                app.preferencesRepository.update(
                    PreferenceUpdate.SetCloudSpeechConsent(CloudSpeechConsent.Granted)
                ) is AppResult.Failure
            )
            assertEquals(CloudSpeechConsent.NotAsked,
                app.preferencesRepository.preferences.value.cloudSpeechConsent)
            assertTrue(app.restorableQueueStore.read() is AppResult.Failure)
        } finally { app.close() }
    }

    @Test fun qualityIsNoOpAndVoiceCannotStartUnavailableProvider() {
        val app = graph()
        try {
            val result = app.telemetryPrivacyGate.sanitize(
                TelemetryEvent.PlayerError("PLAYBACK_FAILED", RouteType.Unknown)
            )
            assertTrue(result is AppResult.Failure)
            assertFalse(app.telemetrySink.tryEmit(SanitizedTelemetryEvent("synthetic")))
            val voice = app.voiceSessionCoordinator.start(VoiceOrigin.Player)
            assertTrue(voice is AppResult.Failure)
            assertEquals(VoiceSessionState.Idle, app.voiceSessionCoordinator.state.value)
        } finally { app.close() }
    }
}

