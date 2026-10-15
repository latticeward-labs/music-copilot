package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.playback.service.PlaybackServiceScopeOwner
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.junit.Assert.*
import org.junit.Test

class LifecycleScopesTest {
    @Test fun serviceScopeIsFreshServiceOwnedAndNeverAnApplicationSingleton() {
        val first = PlaybackServiceScopeOwner(TestDispatcherProvider())
        val second = PlaybackServiceScopeOwner(TestDispatcherProvider())
        val a = first.scope.coroutineContext[Job]!!
        val b = second.scope.coroutineContext[Job]!!
        try {
            assertNotSame(a, b)
            assertTrue(a.isActive)
            assertTrue(b.isActive)
            first.close()
            assertFalse(a.isActive)
            assertTrue(b.isActive)
        } finally { first.close(); second.close() }
        assertFalse(b.isActive)
    }

    @Test fun voiceGenerationReplacementCancelsOldChildAndProviderOperation() {
        val owner = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val stt = AvailableFakeStt()
        val tts = AvailableFakeTts()
        val voice = DefaultVoiceSessionCoordinator(owner, stt, tts)
        try {
            val first = voice.start(VoiceOrigin.Player) as AppResult.Success<*>
            assertEquals(VoiceSessionId(1), first.value)
            val oldJob = voice.activeGenerationJob!!
            assertTrue(oldJob.isActive)
            val second = voice.start(VoiceOrigin.Driving) as AppResult.Success<*>
            assertEquals(VoiceSessionId(2), second.value)
            assertFalse(oldJob.isActive)
            assertEquals(VoiceSessionId(1), stt.cancelled.single())
            assertEquals(VoiceSessionId(1), tts.stopped.single())
            assertTrue(voice.activeGenerationJob!!.isActive)
            assertEquals(VoiceOrigin.Driving, (voice.state.value as VoiceSessionState.Listening).origin)
            assertTrue(voice.confirm(VoiceCandidateToken("stale"), 0) is AppResult.Failure)
            voice.cancel(VoiceCancelReason.DrivingExit)
            assertFalse(voice.activeGenerationJob?.isActive ?: false)
            assertEquals(VoiceSessionState.Idle, voice.state.value)
            assertEquals(listOf(VoiceSessionId(1), VoiceSessionId(2)), stt.cancelled)
            assertEquals(listOf(VoiceSessionId(1), VoiceSessionId(2)), tts.stopped)
        } finally { voice.close(); owner.coroutineContext[Job]?.cancel() }
    }

    @Test fun whenProviderUnavailableNoVoiceJobAndNoPlaybackSuccess() {
        val owner = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val voice = DefaultVoiceSessionCoordinator(owner, DisabledFakeStt(), AvailableFakeTts())
        try {
            assertTrue(voice.start(VoiceOrigin.Driving) is AppResult.Failure)
            assertNull(voice.activeGenerationJob)
            assertEquals(VoiceSessionState.Idle, voice.state.value)
            assertTrue(voice.retry() is AppResult.Failure)
        } finally { voice.close(); owner.coroutineContext[Job]?.cancel() }
    }
}

private class AvailableFakeStt : SpeechToTextEngine {
    val cancelled = mutableListOf<VoiceSessionId>()
    override fun isAvailable() = true
    override suspend fun recognize(request: SpeechRecognitionRequest):
        AppResult<SpeechRecognitionResult> =
        AppResult.Success(SpeechRecognitionResult("synthetic", 0.95, "en-US"))
    override fun cancel(sessionId: VoiceSessionId) { cancelled.add(sessionId) }
}
private class DisabledFakeStt : SpeechToTextEngine {
    override fun isAvailable() = false
    override suspend fun recognize(request: SpeechRecognitionRequest):
        AppResult<SpeechRecognitionResult> = error("not invoked")
    override fun cancel(sessionId: VoiceSessionId) = Unit
}
private class AvailableFakeTts : TextToSpeechEngine {
    val stopped = mutableListOf<VoiceSessionId>()
    override fun isAvailable() = true
    override suspend fun speak(output: SpeechOutput): AppResult<Unit> = AppResult.Success(Unit)
    override fun stop(sessionId: VoiceSessionId) { stopped.add(sessionId) }
}
