package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.VoiceDuckingPort
import com.latticeward.musiccopilot.playback.api.NoOpVoiceDuckingPort
import java.util.UUID
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
    // Production binds Media3's private duck port. The default is only for
    // legacy no-Android JVM fixtures, NEVER chosen by production VoiceBindings.
    private val ducking: VoiceDuckingPort = NoOpVoiceDuckingPort,
) : VoiceSessionCoordinator, AutoCloseable {
    init { requireNotNull(ownerScope.coroutineContext[Job]) { "Voice needs a parent Job" } }
    private val mutableState = MutableStateFlow<VoiceSessionState>(VoiceSessionState.Idle)
    override val state: StateFlow<VoiceSessionState> = mutableState

    /**
     * UI, provider continuations and Media3 callbacks use the application Main
     * dispatcher. Admission is also lock-protected so a late callback cannot
     * race a synchronous cancel/retry or publish a stale result.
     */
    private val gate = Any()
    private val voiceDispatcher = playback?.let(::VoicePlaybackDispatcher)
    private var sequence = 0L
    private var childJob: Job? = null
    private var currentId: VoiceSessionId? = null
    private var currentOrigin: VoiceOrigin? = null
    private var attempt = 0
    private var dispatchClaimed = false
    private var activeDuckToken: String? = null
    private var closed = false

    internal val activeGenerationJob: Job?
        get() = synchronized(gate) { childJob }

    /**
     * A deliberate new start replaces the previous generation (architecture
     * contract); P03 owns tap-time admission to avoid accidental double taps.
     * Neither permission callbacks nor app bootstrap may call start().
     */
    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> =
        synchronized(gate) {
            if (closed || ownerScope.coroutineContext[Job]?.isActive != true) {
                return@synchronized failure(AppErrorCodes.VOICE_UNAVAILABLE)
            }
            invalidateLocked()
            val ready = try { stt.isAvailable() } catch (_: Exception) { false }
            if (!ready || sequence == Long.MAX_VALUE) {
                return@synchronized failure(AppErrorCodes.VOICE_UNAVAILABLE)
            }
            val id = VoiceSessionId(++sequence)
            currentId = id
            currentOrigin = origin
            attempt = 1
            beginAttemptLocked(id, origin, 1)
            AppResult.Success(id)
        }

    /** Caller holds gate; no second session can become active in this window. */
    private fun beginAttemptLocked(id: VoiceSessionId, origin: VoiceOrigin, number: Int) {
        val generation = SupervisorJob(ownerScope.coroutineContext[Job])
        childJob = generation
        dispatchClaimed = false
        // Unique even if Android kills/restarts only the app controller while
        // MediaSessionService retains music and an old lease.
        val duckToken = "voice-duck-${UUID.randomUUID()}"
        activeDuckToken = duckToken
        mutableState.value = VoiceSessionState.Listening(id, origin, number)
        // Keep the original non-recording fake seam for foundation tests.
        if (!recognitionEnabled) return
        ownerScope.launch(generation) { runAttempt(id, origin, number, generation, duckToken) }
    }

    private suspend fun runAttempt(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        duckToken: String,
    ) {
        try {
            // STT must NEVER open until the service has acknowledged
            // attenuation, not merely a UI/state transition to Listening.
            val acquired = try {
                ducking.acquireVoiceDuck(duckToken)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) { false }
            coroutineContext.ensureActive()
            if (!isCurrent(id, generation, number)) return
            if (!acquired) {
                markFailed(id, origin, number, generation, AppErrorCodes.VOICE_UNAVAILABLE)
                return
            }

            val recognition = try {
                withTimeoutOrNull(VoicePolicy.LISTEN_TIMEOUT_MS) {
                    stt.recognize(
                        SpeechRecognitionRequest(id, "en-US", VoicePolicy.LISTEN_TIMEOUT_MS)
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                failure(AppErrorCodes.VOICE_UNAVAILABLE)
            }

            coroutineContext.ensureActive()
            if (!isCurrent(id, generation, number)) return
            if (recognition == null) {
                markFailed(id, origin, number, generation, AppErrorCodes.VOICE_TIMEOUT)
                return
            }
            when (recognition) {
                is AppResult.Failure -> {
                    val code = when (recognition.error.code) {
                        AppErrorCodes.VOICE_TIMEOUT -> AppErrorCodes.VOICE_TIMEOUT
                        AppErrorCodes.VOICE_NO_MATCH -> AppErrorCodes.VOICE_NO_MATCH
                        else -> AppErrorCodes.VOICE_UNAVAILABLE
                    }
                    markFailed(id, origin, number, generation, code)
                }
                is AppResult.Success -> {
                    if (!publishProcessing(id, origin, number, generation)) return
                    val decision = try {
                        intentMapper.map(recognition.value)
                    } catch (_: Exception) {
                        VoiceIntentDecision.Rejected
                    }
                    when (decision) {
                        is VoiceIntentDecision.Rejected,
                        is VoiceIntentDecision.RequiresConfirmation ->
                            markFailed(
                                id, origin, number, generation, AppErrorCodes.VOICE_NO_MATCH
                            )
                        is VoiceIntentDecision.Direct -> {
                            // P02 limits state admission; P05 will harden grammar.
                            // PLAY_ARTIST remains reserved for Issue #11.
                            val dispatcher = voiceDispatcher
                            if (dispatcher == null || VoicePlaybackDispatcher.commandFor(
                                    decision.intent, id, number
                                ) == null
                            ) {
                                markFailed(
                                    id, origin, number, generation, AppErrorCodes.VOICE_NO_MATCH
                                )
                                return
                            }
                            if (!claimDispatch(id, origin, number, generation)) return
                            val acknowledged = try {
                                dispatcher.execute(decision.intent, id, number)
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                failure(AppErrorCodes.VOICE_UNAVAILABLE)
                            }
                            coroutineContext.ensureActive()
                            if (!isCurrent(id, generation, number)) return
                            when (acknowledged) {
                                is AppResult.Failure ->
                                    markFailed(
                                        id, origin, number, generation, AppErrorCodes.VOICE_UNAVAILABLE
                                    )
                                is AppResult.Success -> {
                                    if (!publishExecuted(id, origin, number, generation)) return
                                    // P08 replaces this existing optional feedback;
                                    // success is based solely on playback ACK.
                                    try {
                                        if (tts.isAvailable()) {
                                            tts.speak(SpeechOutput(id, "voice_done", "Done"))
                                        }
                                    } catch (cancelled: CancellationException) {
                                        throw cancelled
                                    } catch (_: Exception) {
                                        // TTS cannot undo an acknowledged command.
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            // An old attempt's release can never alter the new attempt's
            // different UUID lease in the service.
            runCatching { ducking.releaseVoiceDuck(duckToken) }
            // Do not erase a newer generation if retry/start ran while an old
            // STT, TTS or Playback continuation was cancelling.
            synchronized(gate) {
                if (currentId == id && childJob === generation && attempt == number) {
                    childJob = null
                }
            }
            generation.cancel()
        }
    }

    private fun isCurrent(id: VoiceSessionId, generation: Job, number: Int): Boolean =
        synchronized(gate) { isCurrentLocked(id, generation, number) }

    private fun isCurrentLocked(id: VoiceSessionId, generation: Job, number: Int): Boolean =
        currentId == id && childJob === generation && attempt == number &&
            generation.isActive

    private fun publishProcessing(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, generation: Job,
    ): Boolean = synchronized(gate) {
        if (!isCurrentLocked(id, generation, number) ||
            !VoiceTransitionPolicy.mayProcess(mutableState.value, id, origin, number)
        ) return@synchronized false
        mutableState.value = VoiceSessionState.Processing(id, origin, number)
        true
    }

    private fun claimDispatch(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, generation: Job,
    ): Boolean = synchronized(gate) {
        if (!isCurrentLocked(id, generation, number) ||
            !VoiceTransitionPolicy.mayDispatch(
                mutableState.value, id, origin, number, dispatchClaimed
            )
        ) return@synchronized false
        // Claim BEFORE suspending at Media3. A resumed duplicate callback
        // cannot issue another command from the same attempt.
        dispatchClaimed = true
        true
    }

    private fun publishExecuted(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, generation: Job,
    ): Boolean = synchronized(gate) {
        if (!isCurrentLocked(id, generation, number) || !dispatchClaimed ||
            !VoiceTransitionPolicy.mayComplete(mutableState.value, id, origin, number)
        ) return@synchronized false
        mutableState.value = VoiceSessionState.Executed(id, origin)
        true
    }

    private fun markFailed(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        code: String,
    ) {
        synchronized(gate) {
            if (!isCurrentLocked(id, generation, number)) return
            val active = mutableState.value
            if (!VoiceTransitionPolicy.mayProcess(active, id, origin, number) &&
                !VoiceTransitionPolicy.mayComplete(active, id, origin, number)
            ) return
            // Teardown occurs before Failed becomes available for Retry.
            // Provider exceptions cannot strand the state machine.
            runCatching { stt.cancel(id) }
            activeDuckToken?.let { token ->
                runCatching { ducking.releaseVoiceDuck(token) }
                activeDuckToken = null
            }
            mutableState.value = VoiceSessionState.Failed(
                id,
                origin,
                AppError.Voice(code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
                canRetry = VoiceTransitionPolicy.canOfferRetry(number, code),
            )
        }
    }

    override fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit> =
        // The Confirming shape stays compatible with Issue #11 but is never
        // entered or acted on for Issue #10's five direct transport commands.
        failure(AppErrorCodes.VOICE_STALE_SESSION)

    override fun retry(): AppResult<Unit> = synchronized(gate) {
        val failed = mutableState.value as? VoiceSessionState.Failed
            ?: return@synchronized failure(AppErrorCodes.VOICE_NO_MATCH)
        if (!VoiceTransitionPolicy.mayRetry(
                failed, currentId, currentOrigin, attempt
            )
        ) return@synchronized failure(AppErrorCodes.VOICE_SECOND_ATTEMPT_FAILED)
        if (closed || ownerScope.coroutineContext[Job]?.isActive != true) {
            return@synchronized failure(AppErrorCodes.VOICE_UNAVAILABLE)
        }
        val id = currentId ?: return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
        // The same logical session is retried; invalidate/cancel the old
        // generation and its provider BEFORE starting attempt two.
        childJob?.cancel()
        childJob = null
        activeDuckToken?.let { runCatching { token ->
            ducking.releaseVoiceDuck(token)
        } }
        activeDuckToken = null
        runCatching { stt.cancel(id) }
        runCatching { tts.stop(id) }
        attempt += 1
        beginAttemptLocked(id, failed.origin, attempt)
        AppResult.Success(Unit)
    }

    override fun cancel(reason: VoiceCancelReason) {
        synchronized(gate) { invalidateLocked() }
    }

    /** Synchronously invalidate the current receipt before any provider callback. */
    private fun invalidateLocked() {
        val id = currentId
        currentId = null
        currentOrigin = null
        attempt = 0
        dispatchClaimed = false
        activeDuckToken?.let { token -> runCatching {
            ducking.releaseVoiceDuck(token)
        } }
        activeDuckToken = null
        val job = childJob
        childJob = null
        mutableState.value = VoiceSessionState.Idle
        job?.cancel()
        if (id != null) {
            runCatching { stt.cancel(id) }
            runCatching { tts.stop(id) }
        }
    }

    private fun failure(code: String): AppResult.Failure = AppResult.Failure(
        AppError.Voice(code, Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
    )

    override fun close() {
        synchronized(gate) {
            if (!closed) {
                closed = true
                invalidateLocked()
            }
        }
    }
}
