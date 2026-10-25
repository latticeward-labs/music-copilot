package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.playback.api.NoOpVoiceDuckingPort
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.api.VoiceDuckingPort
import com.latticeward.musiccopilot.playback.command.ServiceVoiceDuckLease
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * P07: AUTHOR_ONLY; no Gradle, Kotlin compiler, JUnit or device execution.
 * Pure volume math and synthetic ports are NOT equivalent to OEM focus QA.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue10Phase7VoiceDuckingLeaseTest {
    private fun token(i: Int): String =
        "voice-duck-00000000-0000-4000-8000-" + i.toString().padStart(12, '0')

    @Test fun acquireAttenuatesExactlyOnceAndReleaseRestoresOriginalVolume() {
        val lease = ServiceVoiceDuckLease()
        val first = lease.acquire(token(1), 0.8f)
        assertTrue(first.accepted)
        assertEquals(0.2f, first.volumeToApply ?: -1f, 0.00001f)
        val repeat = lease.acquire(token(1), 0.2f)
        assertTrue(repeat.accepted)
        assertNull(repeat.volumeToApply)
        val released = lease.release(token(1), 0.2f)
        assertTrue(released.accepted)
        assertEquals(0.8f, released.volumeToApply ?: -1f, 0.00001f)
        assertNull(lease.currentToken)
    }

    @Test fun replacingLeaseKeepsPreVoiceBaselineInsteadOfDoubleDucking() {
        val lease = ServiceVoiceDuckLease()
        lease.acquire(token(1), 1f)
        val renewed = lease.acquire(token(2), 0.25f)
        assertTrue(renewed.accepted)
        assertNull(renewed.volumeToApply)
        assertEquals(token(2), lease.currentToken)
        assertFalse(lease.release(token(1), 0.25f).accepted)
        assertEquals(1f, lease.release(token(2), 0.25f).volumeToApply ?: -1f, 0f)
    }

    @Test fun lateReleaseCanNeverUnduckNewerGeneration() {
        val lease = ServiceVoiceDuckLease()
        lease.acquire(token(1), 0.6f)
        lease.acquire(token(2), 0.15f)
        assertFalse(lease.release(token(1), 0.15f).accepted)
        assertEquals(token(2), lease.currentToken)
    }

    @Test fun userVolumeChangeDuringLeaseMustNeverBeOverwrittenOnRelease() {
        val lease = ServiceVoiceDuckLease()
        lease.acquire(token(1), 1f)
        val released = lease.release(token(1), 0.5f)
        assertTrue(released.accepted)
        assertNull(released.volumeToApply)
        assertNull(lease.currentToken)
    }

    @Test fun emergencyRevokeIsIdempotentAndNeverIssuesPlayOrPause() {
        val lease = ServiceVoiceDuckLease()
        lease.acquire(token(1), 0.4f)
        val emergency = lease.revoke(0.1f)
        assertEquals(0.4f, emergency.volumeToApply ?: -1f, 0.00001f)
        assertFalse(lease.revoke(0.4f).accepted)
        assertFalse(lease.release(token(1), 0.4f).accepted)
    }

    @Test fun zeroVolumeStaysZeroAndDoesNotStartAudio() {
        val lease = ServiceVoiceDuckLease()
        val acquire = lease.acquire(token(1), 0f)
        assertTrue(acquire.accepted)
        assertNull(acquire.volumeToApply)
        val release = lease.release(token(1), 0f)
        assertEquals(0f, release.volumeToApply ?: 0f, 0f)
    }

    @Test fun malformedTokenAndInvalidVolumeFailClosed() {
        val lease = ServiceVoiceDuckLease()
        assertFalse(lease.acquire("voice-1-1", 1f).accepted)
        assertFalse(lease.acquire(token(1), Float.NaN).accepted)
        assertFalse(lease.acquire(token(1), -0.5f).accepted)
        assertFalse(lease.acquire(token(1), 1.01f).accepted)
        assertNull(lease.currentToken)
    }

    @Test fun releaseAfterDuplicateOrAbsentLeaseIsHarmless() {
        val lease = ServiceVoiceDuckLease()
        assertFalse(lease.release(token(1), 1f).accepted)
        lease.acquire(token(1), 1f)
        lease.release(token(1), 0.25f)
        assertFalse(lease.release(token(1), 1f).accepted)
    }

    private class Duck(var grant: Boolean = true) : VoiceDuckingPort {
        val events = mutableListOf<String>()
        override suspend fun acquireVoiceDuck(token: String): Boolean {
            events += "acquire"
            return grant
        }
        override fun releaseVoiceDuck(token: String) {
            events += "release"
        }
    }

    private class Stt : SpeechToTextEngine {
        var calls = 0
        var duck: Duck? = null
        var pending: CompletableDeferred<AppResult<SpeechRecognitionResult>>? = null
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> {
            calls++
            if (duck != null) assertEquals("acquire", duck!!.events.first())
            return pending?.await() ?: AppResult.Success(
                SpeechRecognitionResult("pause", 0.99, "en-US")
            )
        }
        override fun cancel(sessionId: VoiceSessionId) = Unit
    }

    private class Tts : TextToSpeechEngine {
        override fun isAvailable() = false
        override suspend fun speak(output: SpeechOutput) = AppResult.Success(Unit)
        override fun stop(sessionId: VoiceSessionId) = Unit
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            0L, 0L, PlaybackState.Paused, listOf(MediaRef("audio:1")),
            0, MediaRef("audio:1"), 1_000L, 10_000L, true, false, false,
        ))
        var calls = 0
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            calls++
            return AppResult.Success(snapshot.value)
        }
    }

    @Test fun coordinatorNeverStartsSttIfDuckingServiceDeniedLease() = runTest {
        val duck = Duck(grant = false)
        val stt = Stt()
        val gateway = Gateway()
        val job = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true, ducking = duck,
        )
        try {
            coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            assertEquals(0, stt.calls)
            assertEquals(0, gateway.calls)
            assertTrue(coordinator.state.value is VoiceSessionState.Failed)
            assertTrue(duck.events.contains("release"))
        } finally { coordinator.close(); job.cancel() }
    }

    @Test fun coordinatorAcquiresBeforeSttAndRestoresAfterSuccessfulAck() = runTest {
        val duck = Duck()
        val stt = Stt().apply { this.duck = duck }
        val gateway = Gateway()
        val job = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true, ducking = duck,
        )
        try {
            coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, stt.calls)
            assertEquals(1, gateway.calls)
            assertTrue(coordinator.state.value is VoiceSessionState.Executed)
            assertEquals(listOf("acquire", "release"), duck.events)
        } finally { coordinator.close(); job.cancel() }
    }

    @Test fun userCancelDuringListeningReleasesLeaseAndNeverDispatches() = runTest {
        val duck = Duck()
        val stt = Stt().apply { pending = CompletableDeferred() }
        val gateway = Gateway()
        val job = SupervisorJob()
        val coordinator = DefaultVoiceSessionCoordinator(
            CoroutineScope(job + StandardTestDispatcher(testScheduler)),
            stt, Tts(), gateway, recognitionEnabled = true, ducking = duck,
        )
        try {
            coordinator.start(VoiceOrigin.Player)
            runCurrent()
            coordinator.cancel(VoiceCancelReason.UserBack)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, coordinator.state.value)
            assertEquals(0, gateway.calls)
            assertTrue(duck.events.contains("release"))
        } finally { coordinator.close(); job.cancel() }
    }

    @Test fun sourceKeepsFocusAndRouteSafetyAboveVoiceLease() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val service = File(root, "playback/service/MusicPlaybackService.kt").readText()
        val controller = File(root, "playback/controller/PlaybackControllerConnection.kt").readText()
        val binding = File(root, "app/di/VoiceBindings.kt").readText()
        val coordinator = File(root, "voice/session/DefaultVoiceSessionCoordinator.kt").readText()
        assertTrue(service.contains("revokeVoiceDuck()"))
        assertTrue(service.contains("FOCUS_INVALIDATED") ||
            service.contains("onVoiceCancellationRequested = { reason ->"))
        assertTrue(service.contains("handlePersonalOutputDisconnected()"))
        assertTrue(service.contains("maxVoiceDuckDurationMs = 22_000L"))
        assertTrue(service.contains("private val voiceDuck = ServiceVoiceDuckLease()"))
        assertTrue(controller.contains("voiceDuckAcquireCommand"))
        assertTrue(controller.contains("voiceDuckReleaseCommand"))
        assertTrue(binding.contains("UnavailableVoiceDuckingPort"))
        assertTrue(coordinator.contains("ducking.acquireVoiceDuck(duckToken)"))
        assertFalse(coordinator.contains("player.volume"))
        assertFalse(coordinator.contains("AudioManager.requestAudioFocus"))
    }

    @Test fun serviceCannotCreateSecondPlayerOrAutomaticallyResumeOnDuckRelease() {
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val service = File(root, "playback/service/MusicPlaybackService.kt").readText()
        val lease = File(root, "playback/command/ServiceVoiceDuckLease.kt").readText()
        assertTrue(service.contains("player.volume = it"))
        assertTrue(service.contains("voiceDuck.currentToken == token"))
        assertTrue(service.contains("expiredOrReleasedVoiceTokens"))
        assertFalse(lease.contains("player.play()"))
        assertFalse(lease.contains("player.pause()"))
        assertFalse(lease.contains("requestAudioFocus"))
    }
}
