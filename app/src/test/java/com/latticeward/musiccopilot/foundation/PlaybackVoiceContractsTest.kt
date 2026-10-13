package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.*
import com.latticeward.musiccopilot.playback.api.*
import com.latticeward.musiccopilot.voice.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PlaybackVoiceContractsTest {
    private fun idleSnapshot(error: AppError? = null) = PlaybackSnapshot(
        revision = 0, queueRevision = 0, playbackState = PlaybackState.Idle,
        queue = emptyList(), currentIndex = null, currentItem = null,
        positionMs = 0, durationMs = null,
        canSeek = false, canSkipPrevious = false, canSkipNext = false,
        lastError = error,
    )

    @Test fun everyPlaybackCommandIsTypedAndCarriesCommandId() {
        val id = CommandId("c-01")
        val commands: List<PlaybackCommand> = listOf(
            PlaybackCommand.ReplaceQueue(id, listOf(MediaRef("a")), 0, 0, false),
            PlaybackCommand.Play(id), PlaybackCommand.Pause(id), PlaybackCommand.Next(id),
            PlaybackCommand.Previous(id), PlaybackCommand.SeekBy(id, -10_000),
            PlaybackCommand.SeekTo(id, 0), PlaybackCommand.Stop(id),
        )
        assertEquals(8, commands.size)
        assertTrue(commands.all { it.commandId == id })
        assertEquals(-10_000, (commands[5] as PlaybackCommand.SeekBy).deltaMs)
    }

    @Test fun phase10SnapshotAcceptsAllThreeRuntimeErrorFamilies() {
        val families = listOf(
            AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_CORRUPT,
                Recoverability.RECOVERABLE, RecoveryAction.Skip),
            AppError.Playback(AppErrorCodes.PLAYBACK_FAILED,
                Recoverability.RECOVERABLE, RecoveryAction.ManualResume),
            AppError.Interruption(AppErrorCodes.ROUTE_INVALIDATED,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualResume),
        )
        families.forEach { assertEquals(it, idleSnapshot(it).lastError) }
        assertNull(idleSnapshot().lastError)
    }

    @Test(expected = IllegalArgumentException::class)
    fun phase10SnapshotRejectsControllerErrorAsPlaybackTruth() {
        idleSnapshot(AppError.Controller(AppErrorCodes.CONTROLLER_TIMEOUT,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))
    }

    @Test(expected = IllegalArgumentException::class)
    fun phase10SnapshotRejectsUnrelatedLibraryError() {
        idleSnapshot(AppError.Library(AppErrorCodes.LIBRARY_SCAN_FAILED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))
    }

    @Test fun fakeGatewayAcknowledgesViaStateFlowNotOptimisticScreenState() = runBlocking {
        val fake = FakeGateway(idleSnapshot())
        val before = fake.snapshot.value
        val accepted = fake.execute(PlaybackCommand.Play(CommandId("p-1")))
        assertTrue(accepted is AppResult.Success)
        assertEquals(before.revision + 1, fake.snapshot.value.revision)
        assertEquals(PlaybackState.Paused, fake.snapshot.value.playbackState)
        // A public fake does not claim actual Media3 service, playback or autoplay.
        assertEquals(PlaybackConnectionState.Ready, fake.connectionState.value)
    }

    @Test fun phase10ConfirmingExposesExactlyOneOrTwoOptionsWithStableIndices() {
        val options: List<VoiceConfirmationOption> = listOf(
            VoiceConfirmationOption(0, "synthetic-option-A"),
            VoiceConfirmationOption(1, "synthetic-option-B"),
        )
        val state = VoiceSessionState.Confirming(
            VoiceSessionId(1), VoiceOrigin.Driving, VoiceCandidateToken("token"), options.toList()
        )
        assertEquals(options, state.options)
        assertEquals(listOf(0, 1), state.options.map { it.optionIndex })
        assertEquals("synthetic-option-A", state.options[0].displayLabel)
    }

    @Test(expected = IllegalArgumentException::class)
    fun confirmationCannotHaveZeroOptions() {
        VoiceSessionState.Confirming(VoiceSessionId(1), VoiceOrigin.Player,
            VoiceCandidateToken("t"), emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun confirmationCannotHaveThreeOptions() {
        VoiceSessionState.Confirming(VoiceSessionId(1), VoiceOrigin.Player,
            VoiceCandidateToken("t"), (0..2).map { VoiceConfirmationOption(it, "synthetic") })
    }

    @Test(expected = IllegalArgumentException::class)
    fun confirmationCannotHaveDuplicateOptionIndexes() {
        VoiceSessionState.Confirming(VoiceSessionId(1), VoiceOrigin.Player,
            VoiceCandidateToken("t"), listOf(
                VoiceConfirmationOption(0, "synthetic-1"),
                VoiceConfirmationOption(0, "synthetic-2"),
            ))
    }

    @Test fun fakeVoiceProviderPortsSupportCancellationAndEphemeralResult() = runBlocking {
        val id = VoiceSessionId(101)
        val stt: SpeechToTextEngine = FakeStt()
        assertTrue(stt.isAvailable())
        val result = stt.recognize(SpeechRecognitionRequest(id, "en-US"))
        assertTrue(result is AppResult.Success)
        assertEquals(6_000, SpeechRecognitionRequest(id, "en-US").maxListeningMs)
        stt.cancel(id)
        val cancelled = stt.recognize(SpeechRecognitionRequest(id, "en-US"))
        assertTrue(cancelled is AppResult.Failure)
        val tts: TextToSpeechEngine = FakeTts()
        assertTrue(tts.speak(SpeechOutput(id, "ACK", "Synthetic ack")) is AppResult.Success)
        tts.stop(id)
    }

    @Test fun fakeCoordinatorRejectsStaleTokenAndCancelledConfirmation() {
        val voice = FakeCoordinator()
        val started = voice.start(VoiceOrigin.Driving)
        assertTrue(started is AppResult.Success)
        assertTrue(voice.confirm(VoiceCandidateToken("old"), 0) is AppResult.Failure)
        voice.publishSyntheticConfirmation()
        assertTrue(voice.confirm(VoiceCandidateToken("current"), 0) is AppResult.Success)
        voice.cancel(VoiceCancelReason.DrivingExit)
        assertEquals(VoiceSessionState.Idle, voice.state.value)
        assertTrue(voice.confirm(VoiceCandidateToken("current"), 0) is AppResult.Failure)
    }
}

private class FakeGateway(initial: PlaybackSnapshot) : PlaybackGateway {
    private val _connection = MutableStateFlow(PlaybackConnectionState.Ready)
    private val _snapshot = MutableStateFlow(initial)
    override val connectionState: StateFlow<PlaybackConnectionState> = _connection
    override val snapshot: StateFlow<PlaybackSnapshot> = _snapshot

    override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
        val next = _snapshot.value.copy(
            revision = _snapshot.value.revision + 1, playbackState = PlaybackState.Paused
        )
        _snapshot.value = next
        return AppResult.Success(next)
    }
}

private class FakeStt : SpeechToTextEngine {
    private val cancelled = mutableSetOf<VoiceSessionId>()
    override fun isAvailable() = true
    override suspend fun recognize(request: SpeechRecognitionRequest): AppResult<SpeechRecognitionResult> =
        if (request.sessionId in cancelled) AppResult.Failure(
            AppError.Voice(AppErrorCodes.VOICE_STALE_SESSION,
                Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback))
        else AppResult.Success(SpeechRecognitionResult("synthetic", 0.95, "en-US"))
    override fun cancel(sessionId: VoiceSessionId) { cancelled.add(sessionId) }
}
private class FakeTts : TextToSpeechEngine {
    override fun isAvailable() = true
    override suspend fun speak(output: SpeechOutput): AppResult<Unit> = AppResult.Success(Unit)
    override fun stop(sessionId: VoiceSessionId) = Unit
}

private class FakeCoordinator : VoiceSessionCoordinator {
    private val mutable = MutableStateFlow<VoiceSessionState>(VoiceSessionState.Idle)
    override val state: StateFlow<VoiceSessionState> = mutable
    private var generation = 0L

    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> {
        val id = VoiceSessionId(++generation)
        mutable.value = VoiceSessionState.Listening(id, origin, 1)
        return AppResult.Success(id)
    }
    fun publishSyntheticConfirmation() {
        val listening = mutable.value as VoiceSessionState.Listening
        mutable.value = VoiceSessionState.Confirming(
            listening.sessionId, listening.origin, VoiceCandidateToken("current"),
            listOf(VoiceConfirmationOption(0, "synthetic"))
        )
    }
    override fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit> {
        val current = mutable.value as? VoiceSessionState.Confirming
        if (current != null && token == current.token &&
            current.options.any { it.optionIndex == optionIndex }) {
            mutable.value = VoiceSessionState.Executed(current.sessionId, current.origin)
            return AppResult.Success(Unit)
        }
        return AppResult.Failure(AppError.Voice(AppErrorCodes.VOICE_STALE_SESSION,
            Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback))
    }
    override fun retry(): AppResult<Unit> = AppResult.Failure(
        AppError.Voice(AppErrorCodes.VOICE_SECOND_ATTEMPT_FAILED,
            Recoverability.TERMINAL_FOR_ATTEMPT, RecoveryAction.ManualFallback))
    override fun cancel(reason: VoiceCancelReason) {
        generation++
        mutable.value = VoiceSessionState.Idle
    }
}

