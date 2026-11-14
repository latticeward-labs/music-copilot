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
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P10 -- REGRESSION SOURCE ONLY; NOT COMPILED OR RUN.
 *
 * Synthetic fakes model misbehaving/late STT, TTS and service ACK callbacks.
 * No fake can prove headset, phone call, Audio Focus or real Media3 behavior:
 * physical validation belongs exclusively to owner-run P13 APK.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue11Phase10VoiceLifecycleRaceTest {
    private class Authority(var foregroundAndMicGranted: Boolean = true)

    private class Stt : SpeechToTextEngine {
        var response = SpeechRecognitionResult(
            "Play songs by Synthetic Artist", null, "en-US",
        )
        var slowResult: CompletableDeferred<Unit>? = null
        var recognitionCalls = 0
        val canceled = mutableListOf<VoiceSessionId>()
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest):
            AppResult<SpeechRecognitionResult> {
            recognitionCalls++
            slowResult?.let {
                // Deliberately ignores coroutine cancellation to model a
                // late OEM/third-party callback trying to revive old Voice.
                withContext(NonCancellable) { it.await() }
            }
            return AppResult.Success(response)
        }
        override fun cancel(sessionId: VoiceSessionId) { canceled.add(sessionId) }
    }

    private class Tts : TextToSpeechEngine {
        var available = true
        val outputs = mutableListOf<SpeechOutput>()
        val stopped = mutableListOf<VoiceSessionId>()
        var promptBarrier: CompletableDeferred<Unit>? = null
        override fun isAvailable() = available
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            outputs.add(output)
            if (output.textKey.startsWith("i11_feedback_artist_confirm_"))
                promptBarrier?.await()
            return AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) { stopped.add(sessionId) }
    }

    private class Duck : VoiceDuckingPort {
        val acquired = mutableListOf<String>()
        val released = mutableListOf<String>()
        override suspend fun acquireVoiceDuck(token: String): Boolean {
            acquired.add(token)
            return true
        }
        override fun releaseVoiceDuck(token: String) { released.add(token) }
    }

    private class Library : LibraryRepository {
        override val index = MutableStateFlow(
            LibraryIndexSnapshot(7L, LibraryIndexStatus.Ready, 1),
        )
        private val track = MediaRef("audio:7")
        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> =
            AppResult.Failure(AppError.Library(
                AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback,
            ))
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = AppResult.Success(emptyList())
        override suspend fun lookupArtistGroups(request: ArtistLookupRequest):
            AppResult<ArtistLookupPage> {
            val entry = ArtistIndexEntry(
                LibraryGroupIdentity.artistId("Synthetic Artist"),
                "Synthetic Artist",
                LibraryTextNormalization.key("Synthetic Artist"), 1,
            )
            val candidates = if (request.mode == ArtistLookupMode.CandidatePage ||
                request.normalizedTarget == entry.normalizedKey
            ) listOf(entry) else emptyList()
            val page = candidates.drop(request.offset).take(request.limit)
            val next = (request.offset + page.size).takeIf { it < candidates.size }
            return AppResult.Success(ArtistLookupPage(
                page, candidates.size, index.value.revision, next, request.limit,
            ))
        }
        override suspend fun artistTrackRefs(request: ArtistTrackRefsRequest):
            AppResult<ArtistTrackRefsPage> =
            AppResult.Success(ArtistTrackRefsPage(
                listOf(track), 1, index.value.revision, null, request.limit,
            ))
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(
                ref, "Synthetic Track", "Synthetic Artist", "Synthetic Album",
                1_000L,
            ))
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            1L, 1L, PlaybackState.Paused,
            listOf(MediaRef("audio:99")), 0, MediaRef("audio:99"),
            100L, 1_000L, true, false, false,
        ))
        val accepted = mutableListOf<PlaybackCommand>()
        var lateAck: CompletableDeferred<Unit>? = null
        override suspend fun execute(command: PlaybackCommand): AppResult<PlaybackSnapshot> {
            accepted.add(command)
            // Model platform futures that return after cancellation: a
            // late service ACK cannot be used to resurrect a canceled Voice.
            lateAck?.let { withContext(NonCancellable) { it.await() } }
            if (command !is PlaybackCommand.ReplaceQueue)
                return AppResult.Success(snapshot.value)
            return AppResult.Success(PlaybackSnapshot(
                2L, 3L, PlaybackState.Buffering,
                command.items, 0, command.items.firstOrNull(),
                0L, 1_000L, true, false, command.items.size > 1,
            ))
        }
    }

    private class Harness(
        val voice: DefaultVoiceSessionCoordinator,
        val auth: Authority,
        val stt: Stt,
        val tts: Tts,
        val duck: Duck,
        val gateway: Gateway,
        val job: Job,
    ) {
        fun close() { voice.close(); job.cancel() }
    }

    private fun newHarness(
        scope: CoroutineScope,
        auth: Authority = Authority(),
        stt: Stt = Stt(),
        tts: Tts = Tts(),
        gateway: Gateway = Gateway(),
    ): Harness {
        val duck = Duck()
        val voice = DefaultVoiceSessionCoordinator(
            ownerScope = scope, stt = stt, tts = tts, playback = gateway,
            recognitionEnabled = true, ducking = duck,
            artistLibrary = Library(), artistAdmission = ArtistDecisionPolicy(),
            canBeginSession = { auth.foregroundAndMicGranted },
        )
        return Harness(voice, auth, stt, tts, duck, gateway,
            scope.coroutineContext[Job]!!)
    }

    private fun token(h: Harness): VoiceCandidateToken =
        (h.voice.state.value as VoiceSessionState.Confirming).token

    @Test fun noMicGrantPreventsPushToTalkAndServiceDuck() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            auth = Authority(false),
        )
        try {
            assertTrue(h.voice.start(VoiceOrigin.Player) is AppResult.Failure)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.duck.acquired.isEmpty())
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close() }
    }

    @Test fun permissionsOnlyAllowOneNewExplicitTapAfterForegroundRecovery() = runTest {
        val auth = Authority(false)
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            auth = auth,
        )
        try {
            assertTrue(h.voice.start(VoiceOrigin.Driving) is AppResult.Failure)
            auth.foregroundAndMicGranted = true
            runCurrent()
            assertEquals(0, h.stt.recognitionCalls)
            assertTrue(h.voice.start(VoiceOrigin.Driving) is AppResult.Success)
            runCurrent()
            assertTrue(h.voice.state.value is VoiceSessionState.Confirming)
            assertEquals(1, h.stt.recognitionCalls)
        } finally { h.close() }
    }

    @Test fun osMicRevocationDuringSlowSttInvalidatesGenerationOnLateCallback() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val stt = Stt().apply { slowResult = barrier }
        val auth = Authority()
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            auth = auth, stt = stt,
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.voice.state.value is VoiceSessionState.Listening)
            auth.foregroundAndMicGranted = false
            barrier.complete(Unit)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.gateway.accepted.isEmpty())
            assertTrue(h.duck.released.isNotEmpty())
        } finally { h.close() }
    }

    @Test fun activeConfirmationRejectsSelectionAfterMicGrantWasRevoked() = runTest {
        val auth = Authority()
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            auth = auth,
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            val old = token(h)
            auth.foregroundAndMicGranted = false
            assertTrue(h.voice.confirm(old, 0) is AppResult.Failure)
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close() }
    }

    @Test fun onStopClearsPendingArtistAndRejectsFormerToken() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Driving)
            runCurrent()
            val old = token(h)
            h.voice.cancel(VoiceCancelReason.OriginInvalidated)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.voice.confirm(old, 0) is AppResult.Failure)
            assertTrue(h.gateway.accepted.isEmpty())
            assertTrue(h.tts.stopped.isNotEmpty())
        } finally { h.close() }
    }

    @Test fun losingFocusDuringArtistChoiceInvalidatesWithoutQueueMutation() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            val old = token(h)
            h.voice.cancel(VoiceCancelReason.CallOrFocusInvalidated)
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.voice.confirm(old, 0) is AppResult.Failure)
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close() }
    }

    @Test fun bluetoothRouteLossDuringChoiceInvalidatesTokenAndNoAutoPlay() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Driving)
            runCurrent()
            val old = token(h)
            h.voice.cancel(VoiceCancelReason.RouteInvalidated)
            runCurrent()
            assertTrue(h.voice.confirm(old, 0) is AppResult.Failure)
            assertTrue(h.gateway.accepted.isEmpty())
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertEquals(1, h.stt.recognitionCalls)
        } finally { h.close() }
    }

    @Test fun delayedServiceAckAfterStopCannotPublishExecutedOrPlaying() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val gateway = Gateway().apply { lateAck = barrier }
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            gateway = gateway,
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.voice.confirm(token(h), 0) is AppResult.Success)
            runCurrent()
            assertEquals(1, gateway.accepted.size)
            h.voice.cancel(VoiceCancelReason.OriginInvalidated)
            barrier.complete(Unit)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.tts.outputs.none {
                it.textKey == "i11_feedback_artist_playing"
            })
            // Once a command reaches Media3 it cannot be "unsent"; only
            // publication of late success is prohibited.
            assertEquals(1, gateway.accepted.size)
        } finally { h.close(); barrier.cancel() }
    }

    @Test fun delayedDirectAckAfterMicRevocationCannotAnnounceSuccess() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val gateway = Gateway().apply { lateAck = barrier }
        val auth = Authority()
        val stt = Stt().apply {
            response = SpeechRecognitionResult(
                "Play songs by Synthetic Artist", 0.99, "en-US",
            )
        }
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            auth = auth, stt = stt, gateway = gateway,
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, gateway.accepted.size)
            auth.foregroundAndMicGranted = false
            barrier.complete(Unit)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.tts.outputs.none {
                it.textKey == "i11_feedback_artist_playing"
            })
        } finally { h.close(); barrier.cancel() }
    }

    @Test fun cancelStopsHangingClarificationAudioWithoutSecondStt() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val tts = Tts().apply { promptBarrier = barrier }
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            tts = tts,
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.voice.state.value is VoiceSessionState.Confirming)
            assertEquals(1, tts.outputs.size)
            h.voice.cancel(VoiceCancelReason.UserBack)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertEquals(1, h.stt.recognitionCalls)
            assertTrue(tts.stopped.isNotEmpty())
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close(); barrier.cancel() }
    }

    @Test fun repeatedBackAndInterruptionSignalsRemainIdempotent() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Driving)
            runCurrent()
            h.voice.cancel(VoiceCancelReason.DrivingExit)
            h.voice.cancel(VoiceCancelReason.RouteInvalidated)
            h.voice.cancel(VoiceCancelReason.CallOrFocusInvalidated)
            runCurrent()
            assertEquals(VoiceSessionState.Idle, h.voice.state.value)
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close() }
    }

    @Test fun activityRecreationDoesNotRestorePreviousArtistToken() = runTest {
        val first = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        val second = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            first.voice.start(VoiceOrigin.Player)
            runCurrent()
            val old = token(first)
            first.voice.cancel(VoiceCancelReason.OriginInvalidated)
            first.close()
            assertEquals(VoiceSessionState.Idle, second.voice.state.value)
            assertTrue(second.voice.confirm(old, 0) is AppResult.Failure)
            assertEquals(0, second.stt.recognitionCalls)
            assertTrue(second.gateway.accepted.isEmpty())
        } finally { first.close(); second.close() }
    }

    @Test fun secondTapWhileConfirmingCannotReplaceFirstGeneration() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            val first = token(h)
            assertTrue(h.voice.start(VoiceOrigin.Player) is AppResult.Failure)
            assertEquals(first, token(h))
            assertEquals(1, h.stt.recognitionCalls)
        } finally { h.close() }
    }

    @Test fun confirmedSelectionIsSingleUseEvenIfBothCallbacksArrive() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            val first = token(h)
            assertTrue(h.voice.confirm(first, 0) is AppResult.Success)
            assertTrue(h.voice.confirm(first, 0) is AppResult.Failure)
            runCurrent()
            assertEquals(1, h.gateway.accepted.size)
        } finally { h.close() }
    }

    @Test fun oldDuckLeaseReleaseCannotAuthorizeAnotherMicWithoutExplicitTap() = runTest {
        val h = newHarness(
            CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )
        try {
            h.voice.start(VoiceOrigin.Player)
            runCurrent()
            val oldToken = token(h)
            h.voice.cancel(VoiceCancelReason.UserBack)
            runCurrent()
            assertTrue(h.duck.released.isNotEmpty())
            assertTrue(h.voice.confirm(oldToken, 0) is AppResult.Failure)
            assertEquals(1, h.stt.recognitionCalls)
            assertTrue(h.gateway.accepted.isEmpty())
        } finally { h.close() }
    }
}
