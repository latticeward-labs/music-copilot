package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.voice.api.SpeechOutput
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import com.latticeward.musiccopilot.voice.intent.VoiceIntentMapper
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.coroutineContext

/**
 * Exactly one app-scoped voice owner and one cancellable generation.
 * The default disabled execution seam retains Phase 5 JVM fixture assumptions;
 * production VoiceBindings opts into provider work only on explicit start().
 */
class DefaultVoiceSessionCoordinator(
    private val ownerScope: CoroutineScope,
    private val stt: SpeechToTextEngine,
    private val tts: TextToSpeechEngine,
    private val playback: PlaybackGateway? = null,
    private val intentMapper: VoiceIntentMapper = RestrictedVoiceIntentMapper(),
    private val recognitionEnabled: Boolean = false,
) : VoiceSessionCoordinator, AutoCloseable {
    init { requireNotNull(ownerScope.coroutineContext[Job]) { "Voice needs a parent Job" } }
    private val mutableState = MutableStateFlow<VoiceSessionState>(VoiceSessionState.Idle)
    override val state: StateFlow<VoiceSessionState> = mutableState

    private var sequence = 0L
    private var childJob: Job? = null
    private var currentId: VoiceSessionId? = null
    private var currentOrigin: VoiceOrigin? = null
    private var attempt = 0
    internal val activeGenerationJob: Job? get() = childJob

    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> {
        cancel(VoiceCancelReason.ReplacedByNewSession)
        if (!stt.isAvailable()) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
        val id = VoiceSessionId(++sequence)
        currentId = id
        currentOrigin = origin
        attempt = 1
        newGeneration(id, origin, 1)
        return AppResult.Success(id)
    }

    private fun newGeneration(id: VoiceSessionId, origin: VoiceOrigin, number: Int) {
        val child = SupervisorJob(ownerScope.coroutineContext[Job])
        childJob = child
        mutableState.value = VoiceSessionState.Listening(id, origin, number)
        if (!recognitionEnabled) return
        ownerScope.launch(child) {
            val recognition = try {
                withTimeoutOrNull(VoicePolicy.LISTEN_TIMEOUT_MS) {
                    stt.recognize(SpeechRecognitionRequest(id, "en-US", VoicePolicy.LISTEN_TIMEOUT_MS))
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { failure(AppErrorCodes.VOICE_UNAVAILABLE) }

            coroutineContext.ensureActive()
            if (!isCurrent(id, child, number)) return@launch
            if (recognition == null) {
                stt.cancel(id)
                markFailed(id, origin, number, AppErrorCodes.VOICE_TIMEOUT)
                return@launch
            }
            when (recognition) {
                is AppResult.Failure -> markFailed(
                    id, origin, number,
                    when (recognition.error.code) {
                        AppErrorCodes.VOICE_TIMEOUT -> AppErrorCodes.VOICE_TIMEOUT
                        AppErrorCodes.VOICE_NO_MATCH -> AppErrorCodes.VOICE_NO_MATCH
                        else -> AppErrorCodes.VOICE_UNAVAILABLE
                    }
                )
                is AppResult.Success -> {
                    mutableState.value = VoiceSessionState.Processing(id, origin, number)
                    when (val mapped = intentMapper.map(recognition.value)) {
                        is VoiceIntentDecision.Rejected,
                        is VoiceIntentDecision.RequiresConfirmation ->
                            markFailed(id, origin, number, AppErrorCodes.VOICE_NO_MATCH)
                        is VoiceIntentDecision.Direct -> {
                            val command = mapPlaybackCommand(mapped.intent, id, number)
                            // Artist matching/confirmation is NOT guessed in Phase 8.
                            if (command == null || playback == null) {
                                markFailed(id, origin, number, AppErrorCodes.VOICE_NO_MATCH)
                                return@launch
                            }
                            val acknowledged = try { playback.execute(command) }
                                catch (cancelled: CancellationException) { throw cancelled }
                                catch (_: Exception) { failure(AppErrorCodes.VOICE_UNAVAILABLE) }
                            coroutineContext.ensureActive()
                            if (!isCurrent(id, child, number)) return@launch
                            when (acknowledged) {
                                is AppResult.Failure ->
                                    markFailed(id, origin, number, AppErrorCodes.VOICE_UNAVAILABLE)
                                is AppResult.Success -> {
                                    mutableState.value = VoiceSessionState.Executed(id, origin)
                                    // Feedback is an optional child side effect AFTER playback ack.
                                    if (tts.isAvailable()) {
                                        val phrase = "Done"
                                        try {
                                            tts.speak(SpeechOutput(id, "voice_done", phrase))
                                        } catch (cancelled: CancellationException) { throw cancelled }
                                        catch (_: Exception) { /* no rollback of acknowledged playback */ }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapPlaybackCommand(
        intent: RestrictedIntent, id: VoiceSessionId, number: Int,
    ): PlaybackCommand? {
        val commandId = CommandId("voice-${id.value}-${number}")
        return when (intent) {
            RestrictedIntent.PLAY -> PlaybackCommand.Play(commandId)
            RestrictedIntent.PAUSE -> PlaybackCommand.Pause(commandId)
            RestrictedIntent.NEXT -> PlaybackCommand.Next(commandId)
            RestrictedIntent.PREVIOUS -> PlaybackCommand.Previous(commandId)
            RestrictedIntent.SEEK_BACK -> PlaybackCommand.SeekBy(commandId, -10_000L)
            RestrictedIntent.PLAY_ARTIST -> null // Issue #11 handles bounded artist resolution.
        }
    }

    private fun isCurrent(id: VoiceSessionId, job: Job, number: Int): Boolean =
        currentId == id && childJob === job && attempt == number && job.isActive

    private fun markFailed(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, code: String,
    ) {
        if (currentId != id || attempt != number) return
        mutableState.value = VoiceSessionState.Failed(
            id, origin,
            AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
            canRetry = number < VoicePolicy.MAX_ATTEMPTS
        )
    }

    override fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit> =
        failure(AppErrorCodes.VOICE_STALE_SESSION)

    override fun retry(): AppResult<Unit> {
        val state = state.value as? VoiceSessionState.Failed
            ?: return failure(AppErrorCodes.VOICE_NO_MATCH)
        if (!state.canRetry || attempt >= VoicePolicy.MAX_ATTEMPTS)
            return failure(AppErrorCodes.VOICE_SECOND_ATTEMPT_FAILED)
        val id = currentId ?: return failure(AppErrorCodes.VOICE_STALE_SESSION)
        stt.cancel(id)
        tts.stop(id)
        childJob?.cancel()
        attempt = 2
        newGeneration(id, state.origin, 2)
        return AppResult.Success(Unit)
    }

    override fun cancel(reason: VoiceCancelReason) {
        val id = currentId
        // Invalidate generation synchronously before provider can call back.
        currentId = null
        currentOrigin = null
        attempt = 0
        childJob?.cancel()
        childJob = null
        // Revoke every Listening/Processing/Confirming/Executed presentation
        // immediately. A cancelled generation must never republish a late
        // recognition/confirmation/feedback result as current.
        mutableState.value = VoiceSessionState.Idle
        if (id != null) {
            // Provider teardown is independent: one Android STT provider
            // failure must not prevent stopping potentially audible TTS.
            // No transcripts, audio buffers, routes or voice identifiers
            // are logged as part of interruption cancellation.
            runCatching { stt.cancel(id) }
            runCatching { tts.stop(id) }
        }
    }

    private fun failure(code: String): AppResult.Failure = AppResult.Failure(
        AppError.Voice(code, Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
    )

    override fun close() { cancel(VoiceCancelReason.OriginInvalidated) }
}
