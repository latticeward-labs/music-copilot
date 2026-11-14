package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
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
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackKey
import com.latticeward.musiccopilot.voice.intent.ArtistDecisionPolicy
import com.latticeward.musiccopilot.voice.session.DefaultVoiceSessionCoordinator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P09 — integration fixtures AUTHORED ONLY. Not compiled or run.
 * The fakes simulate data and ACK envelopes; they are not evidence that
 * Android TTS, Moonshine, audio focus or real Media3 works on the handset.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Issue11Phase9ArtistTtsCoordinatorIntegrationTest {
    private class Stt(
        var text: String = "Play songs by Synthetic Artist",
        var confidence: Double? = null,
        var language: String = "en-US",
    ) : SpeechToTextEngine {
        var requests = 0
        override fun isAvailable() = true
        override suspend fun recognize(request: SpeechRecognitionRequest):
            AppResult<SpeechRecognitionResult> {
            requests++
            return AppResult.Success(SpeechRecognitionResult(text, confidence, language))
        }
        override fun cancel(sessionId: VoiceSessionId) = Unit
    }

    private class Tts(
        var available: Boolean = true,
    ) : TextToSpeechEngine {
        val spoken = mutableListOf<SpeechOutput>()
        val stopped = mutableListOf<VoiceSessionId>()
        var questionBarrier: CompletableDeferred<Unit>? = null
        var failure = false
        override fun isAvailable() = available
        override suspend fun speak(output: SpeechOutput): AppResult<Unit> {
            spoken.add(output)
            if (output.textKey.startsWith("i11_feedback_artist_confirm_")) {
                questionBarrier?.await()
            }
            return if (failure) AppResult.Failure(AppError.Voice(
                AppErrorCodes.VOICE_UNAVAILABLE, Recoverability.RECOVERABLE,
                RecoveryAction.ManualFallback,
            )) else AppResult.Success(Unit)
        }
        override fun stop(sessionId: VoiceSessionId) { stopped.add(sessionId) }
    }

    private class Duck : VoiceDuckingPort {
        var held = 0
        var released = 0
        override suspend fun acquireVoiceDuck(token: String): Boolean {
            held++
            return true
        }
        override fun releaseVoiceDuck(token: String) { released++ }
    }

    private class Library(
        var artists: List<String> = listOf("Synthetic Artist"),
    ) : LibraryRepository {
        override val index = MutableStateFlow(LibraryIndexSnapshot(
            7L, LibraryIndexStatus.Ready, artists.size,
        ))
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

        override suspend fun lookupArtistGroups(request: ArtistLookupRequest):
            AppResult<ArtistLookupPage> {
            val normalized = artists.map { artist ->
                ArtistIndexEntry(
                    LibraryGroupIdentity.artistId(artist), artist,
                    LibraryTextNormalization.key(artist), 1,
                )
            }.let { list ->
                if (request.mode == ArtistLookupMode.ExactNormalized)
                    list.filter { it.normalizedKey == request.normalizedTarget }
                else list.sortedWith(compareBy<ArtistIndexEntry> { it.normalizedKey }
                    .thenBy { it.id.value })
            }
            val page = normalized.drop(request.offset).take(request.limit)
            val next = (request.offset + page.size).takeIf { it < normalized.size }
            return AppResult.Success(ArtistLookupPage(
                page, normalized.size, index.value.revision, next, request.limit,
            ))
        }

        override suspend fun artistTrackRefs(request: ArtistTrackRefsRequest):
            AppResult<ArtistTrackRefsPage> {
            val matches = artists.any {
                LibraryGroupIdentity.artistId(it) == request.artistGroupId
            }
            val refs = if (matches) listOf(ref) else emptyList()
            return AppResult.Success(ArtistTrackRefsPage(
                refs, refs.size, index.value.revision, null, request.limit,
            ))
        }

        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> =
            AppResult.Success(LibraryItem(
                ref, "Synthetic Track", artists.firstOrNull(),
                "Synthetic Album", 10_000L,
            ))
    }

    private class Gateway : PlaybackGateway {
        override val connectionState = MutableStateFlow(PlaybackConnectionState.Ready)
        override val snapshot = MutableStateFlow(PlaybackSnapshot(
            1L, 2L, PlaybackState.Paused, listOf(MediaRef("audio:99")),
            0, MediaRef("audio:99"), 50L, 1000L, true, false, false,
        ))
        val commands = mutableListOf<PlaybackCommand>()
        var ackBarrier: CompletableDeferred<Unit>? = null
        var staleAck = false
        var rejectAck = false
        override suspend fun execute(command: PlaybackCommand):
            AppResult<PlaybackSnapshot> {
            commands.add(command)
            ackBarrier?.await()
            if (rejectAck) return AppResult.Failure(AppError.Playback(
                AppErrorCodes.PLAYBACK_REJECTED, Recoverability.RECOVERABLE,
                RecoveryAction.ManualFallback,
            ))
            if (staleAck) return AppResult.Success(snapshot.value)
            val replace = command as PlaybackCommand.ReplaceQueue
            return AppResult.Success(PlaybackSnapshot(
                2L, 3L, PlaybackState.Buffering, replace.items,
                0, replace.items.firstOrNull(), 0L, 1000L,
                true, false, replace.items.size > 1,
            ))
        }
    }

    private class Harness(
        val stt: Stt,
        val tts: Tts,
        val duck: Duck,
        val library: Library,
        val gateway: Gateway,
        val coordinator: DefaultVoiceSessionCoordinator,
        val job: Job,
    ) {
        fun close() { coordinator.close(); job.cancel() }
    }

    private fun create(
        scope: CoroutineScope,
        stt: Stt = Stt(),
        tts: Tts = Tts(),
        library: Library = Library(),
        gateway: Gateway = Gateway(),
    ): Harness {
        val duck = Duck()
        val coordinator = DefaultVoiceSessionCoordinator(
            ownerScope = scope, stt = stt, tts = tts, playback = gateway,
            recognitionEnabled = true, ducking = duck,
            artistLibrary = library, artistAdmission = ArtistDecisionPolicy(),
        )
        return Harness(stt, tts, duck, library, gateway, coordinator,
            scope.coroutineContext[Job]!!)
    }

    @Test fun missingMoonshineConfidenceSpeaksOneQuestionAndDoesNotEnqueue() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertEquals(listOf("i11_feedback_artist_confirm_one"),
                h.tts.spoken.map { it.textKey })
            assertTrue(h.gateway.commands.isEmpty())
            assertEquals(1, h.stt.requests)
            assertTrue(h.duck.released >= 1)
        } finally { h.close() }
    }

    @Test fun missingLocalTtsDoesNotHideConfirmingOrPauseThePlayer() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            tts = Tts(available = false))
        try {
            h.coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Confirming)
            assertTrue(h.tts.spoken.isEmpty())
            assertTrue(h.gateway.commands.isEmpty())
            val pending = h.coordinator.state.value as VoiceSessionState.Confirming
            assertTrue(h.coordinator.confirm(pending.token, 0) is AppResult.Success)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertTrue(h.tts.spoken.isEmpty())
            assertEquals(1, h.gateway.commands.size)
        } finally { h.close() }
    }

    @Test fun highRealConfidenceSpeaksPlayingOnlyAfterAcknowledgement() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val g = Gateway().apply { ackBarrier = barrier }
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = Stt(confidence = 0.97), gateway = g)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertEquals(1, g.commands.size)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Processing)
            assertTrue(h.tts.spoken.isEmpty())
            barrier.complete(Unit)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertEquals(listOf("i11_feedback_artist_playing"),
                h.tts.spoken.map { it.textKey })
        } finally { h.close() }
    }

    @Test fun confirmedOneStepSpeaksPlayingOnlyAfterSelectedQueueAck() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val g = Gateway().apply { ackBarrier = barrier }
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            gateway = g)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val confirming = h.coordinator.state.value as VoiceSessionState.Confirming
            assertEquals(listOf("i11_feedback_artist_confirm_one"),
                h.tts.spoken.map { it.textKey })
            h.coordinator.confirm(confirming.token, 0)
            runCurrent()
            assertEquals(1, g.commands.size)
            assertTrue(h.coordinator.state.value is VoiceSessionState.Processing)
            assertFalse(h.tts.spoken.any { it.textKey == "i11_feedback_artist_playing" })
            barrier.complete(Unit)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertEquals("i11_feedback_artist_playing", h.tts.spoken.last().textKey)
        } finally { h.close() }
    }

    @Test fun noPlausibleArtistSpeaksNotFoundAndNeverTouchesOldQueue() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = Stt(confidence = 0.97), library = Library(artists = emptyList()))
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val failed = h.coordinator.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_ARTIST_NOT_FOUND, failed.error.code)
            assertFalse(failed.canRetry)
            assertTrue(h.gateway.commands.isEmpty())
            assertEquals("i11_feedback_artist_not_found", h.tts.spoken.single().textKey)
        } finally { h.close() }
    }

    @Test fun staleMedia3AckCannotBeAnnouncedAsPlaying() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = Stt(confidence = 0.97), gateway = Gateway().apply { staleAck = true })
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val failed = h.coordinator.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED, failed.error.code)
            assertTrue(h.tts.spoken.none { it.textKey == "i11_feedback_artist_playing" })
            assertEquals("i11_feedback_artist_playback_failure",
                h.tts.spoken.single().textKey)
        } finally { h.close() }
    }

    @Test fun failedTtsDoesNotUndoAnAcceptedArtistCommand() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = Stt(confidence = 0.97), tts = Tts().apply { failure = true })
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            assertTrue(h.coordinator.state.value is VoiceSessionState.Executed)
            assertEquals(1, h.gateway.commands.size)
        } finally { h.close() }
    }

    @Test fun neitherClosesSingleConfirmationAndStopsItsOptionalQuestion() = runTest {
        val barrier = CompletableDeferred<Unit>()
        val tts = Tts().apply { questionBarrier = barrier }
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            tts = tts)
        try {
            h.coordinator.start(VoiceOrigin.Player)
            runCurrent()
            val confirming = h.coordinator.state.value as VoiceSessionState.Confirming
            assertEquals(1, tts.spoken.size)
            assertTrue(h.coordinator.neither(confirming.token) is AppResult.Success)
            runCurrent()
            val failure = h.coordinator.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_ARTIST_DECLINED, failure.error.code)
            assertFalse(failure.canRetry)
            assertEquals(1, h.stt.requests)
            assertTrue(h.gateway.commands.isEmpty())
            assertTrue(tts.stopped.isNotEmpty())
        } finally { h.close(); barrier.cancel() }
    }

    @Test fun unsupportedArtistVariantFailsSafelyWithItsOwnMessage() = runTest {
        val h = create(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
            stt = Stt("Play songs by Synthetic Artist on Spotify", 0.97))
        try {
            h.coordinator.start(VoiceOrigin.Driving)
            runCurrent()
            val failure = h.coordinator.state.value as VoiceSessionState.Failed
            assertEquals(AppErrorCodes.VOICE_ARTIST_UNSUPPORTED, failure.error.code)
            assertTrue(h.gateway.commands.isEmpty())
            assertEquals("i11_feedback_artist_unsupported", h.tts.spoken.single().textKey)
        } finally { h.close() }
    }
}
