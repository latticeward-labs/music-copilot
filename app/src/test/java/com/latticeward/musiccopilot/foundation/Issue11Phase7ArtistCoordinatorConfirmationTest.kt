package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.ArtistIndexEntry
import com.latticeward.musiccopilot.data.api.ArtistLookupMode
import com.latticeward.musiccopilot.data.api.ArtistLookupPage
import com.latticeward.musiccopilot.data.api.ArtistLookupRequest
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsPage
import com.latticeward.musiccopilot.data.api.ArtistTrackRefsRequest
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.api.VoiceDuckingPort
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionResult
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.intent.ArtistDecisionPolicy
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

/**
 * ISSUE #11 P07 — AUTHOR_ONLY: no JVM, Android, Gradle, emulator, CI or phone
 * runs before P13. All artist/track names are synthetic. Fake service ACK is
 * NOT evidence of physical Media3 playback or STT provider reliability.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue11Phase7ArtistCoordinatorConfirmationTest {
    private val artistName = "Synthetic Artist"
    private val ref = MediaRef("audio:7")

    private class FakeSTT(
        var speech: SpeechRecognitionResult =
            SpeechRecognitionResult("Play songs by Synthetic Artist", null, "en-US"),
    ) : SpeechToTextEngine {
        var count = 0
        val canceled = mutableListOf<VoiceSessionId>()
        override fun isAvailable(): Boolean = true
        override suspend fun recognize(request: SpeechRecognitionRequest):
            AppResult<SpeechRecognitionResult> {
            count++
            return AppResult.Success(speech)
        }
        override fun cancel(sessionId: VoiceSessionId) { canceled += sessionId }
    }

    private class FakeTTS : TextToSpeechEngine {
        var spoken = 0
        override fun isAvailable(): Boolean = false
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            spoken++
            return AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) = Unit
    }

    private class FakeDuck : VoiceDuckingPort {
        var acquired = 0
        var released = 0
        override suspend fun acquireVoiceDuck(token: String): Boolean {
            acquired++
            return true
        }
        override fun releaseVoiceDuck(token: String) { released++ }
    }

    private class FakeLibrary(
        var artistNames: List<String> = listOf("Synthetic Artist"),
    ) : LibraryRepository {
        override val index = MutableStateFlow(
            LibraryIndexSnapshot(7L, LibraryIndexStatus.Ready, artistNames.size),
        )
        var getCount = 0
        var artistRefsRead = 0
        var denyRef: Boolean = false
        val ref = MediaRef("audio:7")

        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
            AppResult.Failure(AppError.Library(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())

        override suspend fun lookupArtistGroups(
            request: ArtistLookupRequest,
        ): AppResult<ArtistLookupPage> {
            if (request.expectedRevision != null &&
                request.expectedRevision != index.value.revision
            ) return AppResult.Failure(AppError.Library(
                AppErrorCodes.LIBRARY_BROWSE_STALE,
                Recoverability.RECOVERABLE, RecoveryAction.Retry))
            val entries = artistNames.map { name ->
                ArtistIndexEntry(LibraryGroupIdentity.artistId(name), name,
                    LibraryTextNormalization.key(name), 1)
            }.let { list ->
                if (request.mode == ArtistLookupMode.ExactNormalized)
                    list.filter { it.normalizedKey == request.normalizedTarget }
                else list.sortedWith(compareBy<ArtistIndexEntry> { it.normalizedKey }
                    .thenBy { it.id.value })
            }
            val page = entries.drop(request.offset).take(request.limit)
            val next = (request.offset + page.size).takeIf { it < entries.size }
            return AppResult.Success(ArtistLookupPage(page, entries.size,
                index.value.revision, next, request.limit))
        }

        override suspend fun artistTrackRefs(
            request: ArtistTrackRefsRequest,
        ): AppResult<ArtistTrackRefsPage> {
            artistRefsRead++
            if (request.expectedRevision != index.value.revision) {
                return AppResult.Failure(AppError.Library(AppErrorCodes.LIBRARY_BROWSE_STALE,
                    Recoverability.RECOVERABLE, RecoveryAction.Retry))
            }
            val correctGroup = artistNames.any {
                LibraryGroupIdentity.artistId(it) == request.artistGroupId
            }
            val all = if (correctGroup) listOf(ref) else emptyList()
            val page = all.drop(request.offset).take(request.limit)
            return AppResult.Success(ArtistTrackRefsPage(
                page, all.size, index.value.revision, null, request.limit,
            ))
        }

        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> {
            getCount++
            if (denyRef) return AppResult.Failure(AppError.Permission(
                AppErrorCodes.PERMISSION_REVOKED,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings))
            return AppResult.Success(LibraryItem(
                ref, "Synthetic Track", artistNames.firstOrNull(), "Synthetic Album", 1000L,
            ))
        }
    }

    private class FakeGateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(
            PlaybackSnapshot(1L, 2L, PlaybackState.Paused,
                listOf(MediaRef("audio:99")), 0, MediaRef("audio:99"),
                100L, 1000L, true, false, false),
        )
        val calls = mutableListOf<PlaybackCommand>()
        var deferredAck: CompletableDeferred<AppResult<PlaybackSnapshot>>? = null
        var wrongAck = false
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            calls += command
            deferredAck?.let { return withContext(NonCancellable) { it.await() } }
            return if (wrongAck) AppResult.Success(snapshot.value)
            else {
                val items = (command as PlaybackCommand.ReplaceQueue).items
                AppResult.Success(PlaybackSnapshot(
                    2L, 3L, PlaybackState.Buffering,
                    items, 0, items.firstOrNull(),
                    0L, 1000L, true, false, items.size > 1,
                ))
            }
        }
    }

    private class Harness(
        val coordinator: DefaultVoiceSessionCoordinator,
        val stt: FakeSTT,
        val tts: FakeTTS,
        val duck: FakeDuck,
        val library: FakeLibrary,
        val gateway: FakeGateway,
        val job: kotlinx.coroutines.Job,
    ) {
        fun close() { coordinator.close(); job.cancel() }
    }

    private fun create(
        scope: CoroutineScope,
        stt: FakeSTT = FakeSTT(),
        library: FakeLibrary = FakeLibrary(),
        gateway: FakeGateway = FakeGateway(),
        canStart: () -> Boolean = { true },
    ): Harness {
        val tts = FakeTTS()
        val duck = FakeDuck()
        val coordinator = DefaultVoiceSessionCoordinator(
            ownerScope = scope, stt = stt, tts = tts,
            playback = gateway, recognitionEnabled = true,
            ducking = duck, artistLibrary = library,
            artistAdmission = ArtistDecisionPolicy(),
            canBeginSession = canStart,
        )
        return Harness(coordinator, stt, tts, duck, library, gateway,
            scope.coroutineContext[kotlinx.coroutines.Job]!!)
    }

    private fun token(h: Harness): VoiceCandidateToken =
        (h.coordinator.state.value as VoiceSessionState.Confirming).token

    @Test fun moonshineMissingConfidenceEntersOneOptionConfirmingWithoutAutoplay() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val state = h.coordinator.state.value as VoiceSessionState.Confirming
            assertEquals(1, state.options.size)
            assertEquals(0, state.options.single().optionIndex)
            assertEquals(artistName, state.options.single().displayLabel)
            assertTrue(h.gateway.calls.isEmpty())
            assertEquals(1, h.stt.count)
            assertEquals(1, h.duck.acquired)
            assertTrue(h.duck.released >= 1)
        } finally { h.close() }
    }

    @Test fun confirmingRetainsOneActiveGenerationWithoutKeepingMicrophoneListening() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            assertTrue(h.coordinator.activeGenerationJob?.isActive == true)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertEquals(1, h.stt.count)
            assertTrue(h.duck.released >= 1)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun acceptedSelectionSendsExactlyOneArtistReplaceQueueAfterRealAck() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.confirm(token(h), 0) is AppResult.Success)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Processing)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            val cmd = h.gateway.calls.single() as PlaybackCommand.ReplaceQueue
            assertEquals(listOf(ref), cmd.items)
            assertTrue(cmd.playWhenReady)
            assertNull(h.coordinator.activeGenerationJob)
            assertEquals(1, h.stt.count)
        } finally { h.close() }
    }

    @Test fun selectingSameTokenTwiceBeforeSchedulingWorkerDoesNotDoubleDispatch() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Success)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Failure)
            runCurrent()
            assertEquals(1, h.gateway.calls.size)
        } finally { h.close() }
    }

    @Test fun staleOrForgedTokenCannotConsumeValidChoice() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.confirm(VoiceCandidateToken("wrong"), 0)
                is AppResult.Failure)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertTrue(h.gateway.calls.isEmpty())
            assertTrue(h.coordinator.confirm(token(h), 0) is AppResult.Success)
            runCurrent()
            assertEquals(1, h.gateway.calls.size)
        } finally { h.close() }
    }

    @Test fun validTokenWithInvalidOptionConsumesOnlyAllowedResponse() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            assertTrue(h.coordinator.confirm(token, 1) is AppResult.Failure)
            val failure = h.coordinator.state.value as VoiceSessionState.Failed
            assertFalse(failure.canRetry)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Failure)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun neitherIsTerminalAndNeverRestartsSTTOrChangesOldQueue() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            assertTrue(h.coordinator.neither(token(h)) is AppResult.Success)
            val state = h.coordinator.state.value as VoiceSessionState.Failed
            assertFalse(state.canRetry)
            assertTrue(h.coordinator.retry() is AppResult.Failure)
            assertEquals(1, h.stt.count)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun explicitConfirmationCancelReturnsIdleAndKeepsQueueUntouched() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.cancelConfirmation(token(h)) is AppResult.Success)
            assertEquals(VoiceSessionState.Idle, h.coordinator.state.value)
            assertTrue(h.gateway.calls.isEmpty())
            assertNull(h.coordinator.activeGenerationJob)
        } finally { h.close() }
    }

    @Test fun staleNeitherAndCancelCannotDismissCurrentConfirmation() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.neither(VoiceCandidateToken("old")) is AppResult.Failure)
            assertTrue(h.coordinator.cancelConfirmation(VoiceCandidateToken("old"))
                is AppResult.Failure)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
        } finally { h.close() }
    }

    @Test fun confirmationTimeoutExpiresWithoutSecondSpeechTurnOrPlayback() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            advanceTimeBy(10_000L)
            runCurrent()
            val failed = h.coordinator.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_TIMEOUT, failed.error.code)
            assertFalse(failed.canRetry)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Failure)
            assertTrue(h.gateway.calls.isEmpty())
            assertEquals(1, h.stt.count)
        } finally { h.close() }
    }

    @Test fun changedLibraryRevisionAfterPromptRejectsSelectionBeforeMedia3() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            h.library.index.value = h.library.index.value.copy(revision = 8L)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Failure)
            assertFalse((h.coordinator.state.value as VoiceSessionState.Failed).canRetry)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun permissionRevocationBetweenPromptAndChoiceRejectsPlayback() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            h.library.index.value =
                h.library.index.value.copy(status = LibraryIndexStatus.Unavailable)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Failure)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun cancelBeforePendingSelectionWorkerRunsCannotDispatch() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val token = token(h)
            assertTrue(h.coordinator.confirm(token, 0) is AppResult.Success)
            h.coordinator.cancel(VoiceCancelReason.UserBack)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.coordinator.state.value)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun canceledLateNoncancellableServiceAckCannotPublishExecuted() = runTest {
        val gateway = FakeGateway().apply {
            deferredAck = CompletableDeferred()
        }
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            gateway = gateway)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            h.coordinator.confirm(token(h), 0)
            runCurrent()
            assertEquals(1, gateway.calls.size)
            h.coordinator.cancel(VoiceCancelReason.UserBack)
            gateway.deferredAck!!.complete(AppResult.Success(gateway.snapshot.value))
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.coordinator.state.value)
            assertEquals(1, gateway.calls.size)
        } finally { h.close() }
    }

    @Test fun wrongAckNeverPublishesArtistExecuted() = runTest {
        val gateway = FakeGateway().apply { wrongAck = true }
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            gateway = gateway)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            h.coordinator.confirm(token(h), 0)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Failed)
            assertEquals(1, gateway.calls.size)
        } finally { h.close() }
    }

    @Test fun realHighProviderConfidenceCanDirectPlayWithoutConfirmation() = runTest {
        val stt = FakeSTT(SpeechRecognitionResult(
            "Play songs by Synthetic Artist", 0.97, "en-US",
        ))
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = stt)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertEquals(1, h.gateway.calls.size)
            assertEquals(1, stt.count)
        } finally { h.close() }
    }

    @Test fun verifiedVadWithMissingArtistConfidenceStillRequiresExplicitChoice() = runTest {
        val stt = FakeSTT(SpeechRecognitionResult(
            "Play songs by Synthetic Artist", null, "en-US",
            localVadEndpointVerified = true,
        ))
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = stt)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun existingFiveTransportCommandsNeverEnterConfirming() = runTest {
        val stt = FakeSTT(SpeechRecognitionResult(
            "Pause", null, "en-US", localVadEndpointVerified = true,
        ))
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = stt)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertTrue(h.gateway.calls.single() is PlaybackCommand.Pause)
        } finally { h.close() }
    }

    @Test fun unknownArtistAndUnsupportedPhraseNeverCreateConfirmation() = runTest {
        val lib = FakeLibrary(artistNames = listOf("Completely Other"))
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            library = lib)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Failed)
            assertTrue(h.gateway.calls.isEmpty())
        } finally { h.close() }
    }

    @Test fun noSecondMicTapWhileConfirmingAndNoSecondClarification() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.start(VoiceOrigin.Driving) is AppResult.Failure)
            assertTrue(h.coordinator.retry() is AppResult.Failure)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertEquals(1, h.stt.count)
        } finally { h.close() }
    }

    @Test fun canceledOldTokenCannotActInNewExplicitSession() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val old = token(h)
            h.coordinator.cancel(VoiceCancelReason.UserBack)
            h.coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            val next = token(h)
            assertNotEquals(old, next)
            assertTrue(h.coordinator.confirm(old, 0) is AppResult.Failure)
            assertTrue(h.coordinator.confirm(next, 0) is AppResult.Success)
            runCurrent()
            assertEquals(1, h.gateway.calls.size)
        } finally { h.close() }
    }

    @Test fun confirmationStateToStringDoesNotExposeArtistNameOrToken() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val state = h.coordinator.state.value
            assertFalse(state.toString().contains(artistName))
            assertFalse(state.toString().contains(token(h).value))
            assertFalse((state as VoiceSessionState.Confirming).options.single().toString()
                .contains(artistName))
        } finally { h.close() }
    }
}
