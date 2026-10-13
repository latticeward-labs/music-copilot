package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Single app-owned lifecycle seam: active generation has exactly one cancellable child Job.
 * Phase 8 supplies recognition/timeout/retry/candidate execution; this class currently never
 * generates a transcript, command or fabricated success.
 */
class DefaultVoiceSessionCoordinator(
    private val ownerScope: CoroutineScope,
    private val stt: SpeechToTextEngine,
    private val tts: TextToSpeechEngine,
) : VoiceSessionCoordinator, AutoCloseable {
    init { requireNotNull(ownerScope.coroutineContext[Job]) { "Voice needs a parent Job" } }
    private val mutableState = MutableStateFlow<VoiceSessionState>(VoiceSessionState.Idle)
    override val state: StateFlow<VoiceSessionState> = mutableState

    private var sequence = 0L
    private var childJob: Job? = null
    private var currentId: VoiceSessionId? = null

    /** Test seam for cancellation, not a second Voice authority. */
    internal val activeGenerationJob: Job? get() = childJob

    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> {
        cancel(VoiceCancelReason.ReplacedByNewSession)
        if (!stt.isAvailable()) return failure(AppErrorCodes.VOICE_UNAVAILABLE)
        val id = VoiceSessionId(++sequence)
        currentId = id
        childJob = SupervisorJob(ownerScope.coroutineContext[Job])
        mutableState.value = VoiceSessionState.Listening(id, origin, 1)
        // No STT work is started in Phase 5. Explicit push-to-talk/gates belong to Phase 8.
        return AppResult.Success(id)
    }

    override fun confirm(
        token: VoiceCandidateToken, optionIndex: Int
    ): AppResult<Unit> = failure(AppErrorCodes.VOICE_STALE_SESSION)

    override fun retry(): AppResult<Unit> = failure(AppErrorCodes.VOICE_NO_MATCH)

    override fun cancel(reason: VoiceCancelReason) {
        val id = currentId
        if (id != null) {
            stt.cancel(id)
            tts.stop(id)
        }
        childJob?.cancel()
        childJob = null
        currentId = null
        mutableState.value = VoiceSessionState.Idle
    }

    private fun failure(code: String): AppResult.Failure =
        AppResult.Failure(AppError.Voice(
            code, Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback
        ))

    override fun close() {
        cancel(VoiceCancelReason.OriginInvalidated)
    }
}

