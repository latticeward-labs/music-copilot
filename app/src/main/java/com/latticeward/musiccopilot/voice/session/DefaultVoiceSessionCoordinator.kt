package com.latticeward.musiccopilot.voice.session

import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.application.voice.VoiceLifecycleSafetyPolicy
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.VoiceDuckingPort
import com.latticeward.musiccopilot.playback.api.NoOpVoiceDuckingPort
import java.util.UUID
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.voice.api.SpeechToTextEngine
import com.latticeward.musiccopilot.voice.api.TextToSpeechEngine
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import com.latticeward.musiccopilot.voice.api.VoiceConfirmationOption
import com.latticeward.musiccopilot.voice.intent.ArtistDecisionPolicy
import com.latticeward.musiccopilot.voice.intent.ArtistResolver
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationOption
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationResponse
import com.latticeward.musiccopilot.voice.intent.PlayArtistConfirmationSnapshot
import com.latticeward.musiccopilot.voice.intent.PlayArtistDecision
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhrasePolicy
import com.latticeward.musiccopilot.voice.intent.PlayArtistPhraseResult
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.intent.RestrictedVoiceIntentMapper
import com.latticeward.musiccopilot.voice.intent.VoiceIntentDecision
import com.latticeward.musiccopilot.voice.intent.VoiceIntentMapper
import com.latticeward.musiccopilot.voice.policy.VoicePolicy
import com.latticeward.musiccopilot.quality.api.ConfidenceBucket
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticResult
import com.latticeward.musiccopilot.quality.privacy.VoiceIntentDiagnosticPolicy
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackCatalog
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackKey
import com.latticeward.musiccopilot.voice.feedback.VoiceFeedbackPolicy
import com.latticeward.musiccopilot.voice.feedback.EnglishVoiceFeedbackCatalog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val feedbackCatalog: VoiceFeedbackCatalog = EnglishVoiceFeedbackCatalog,
    // Production supplies the current Activity STARTED observation. A queued
    // Mic/Retry callback after onStop cannot reopen the microphone.
    private val canBeginSession: () -> Boolean = { true },
    // P07: supplied only by production composition root. Existing #10
    // synthetic fakes omit this and retain all five transport behaviors.
    private val artistLibrary: LibraryRepository? = null,
    private val artistAdmission: ArtistDecisionPolicy = ArtistDecisionPolicy(),
    // P11: optional and strictly typed. No analytics provider is installed.
    // Observers receive only coarse outcomes, never local media identities.
    private val tryRecordVoiceOutcome: (TelemetryEvent.VoiceIntentOutcome) -> Unit = {},
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
    private val artistResolver = artistLibrary?.let(::ArtistResolver)
    private val artistPlayback = if (artistLibrary != null && playback != null)
        PlayArtistPlaybackUseCase(artistLibrary, playback) else null
    private var sequence = 0L
    private var childJob: Job? = null
    private var currentId: VoiceSessionId? = null
    private var currentOrigin: VoiceOrigin? = null
    private var attempt = 0
    private var dispatchClaimed = false
    private var feedbackClaimed = false
    // Per-attempt private state; only buckets and closed codes can be emitted.
    private var artistIntentObserved = false
    private var diagnosticClaimed = false
    private var diagnosticConfidence: ConfidenceBucket? = null
    private var attemptStartNanos = 0L
    // P09: one optional clarification sentence for an artist choice, kept
    // separate from the subsequent ACK-driven terminal response.
    private var artistPromptClaimed = false
    private var artistPromptJob: Job? = null
    /** The single pending confirmation exists only for the active generation. */
    private data class ArtistPending(
        val snapshot: PlayArtistConfirmationSnapshot,
        val generation: Job,
        val attempt: Int,
    )
    private var pendingArtist: ArtistPending? = null
    private var confirmingDispatchInFlight = false
    private var confirmationTimeoutJob: Job? = null
    private var activeDuckToken: String? = null
    private var closed = false

    internal val activeGenerationJob: Job?
        get() = synchronized(gate) { childJob }

    /**
     * P10: one active generation is exclusive even before Compose collects its
     * new state. A second tap can NEVER silently replace active STT or TTS.
     * The user first explicitly exits/cancels to Idle, then taps Mic again.
     * Neither permission callbacks nor app bootstrap may call start().
     */
    override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> =
        synchronized(gate) {
            val foreground = runCatching { canBeginSession() }.getOrDefault(false)
            if (closed || !foreground || ownerScope.coroutineContext[Job]?.isActive != true ||
                !VoiceLifecycleSafetyPolicy.canAdmitExplicitTap(mutableState.value)
            ) {
                return@synchronized failure(AppErrorCodes.VOICE_UNAVAILABLE)
            }
            // Defensive cleanup for a previous completed/invalidated generation;
            // no fresh command/recorder exists unless this explicit tap passed.
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
        feedbackClaimed = false
        artistIntentObserved = false
        diagnosticClaimed = false
        diagnosticConfidence = null
        attemptStartNanos = System.nanoTime()
        artistPromptClaimed = false
        artistPromptJob?.cancel()
        artistPromptJob = null
        pendingArtist = null
        confirmingDispatchInFlight = false
        confirmationTimeoutJob?.cancel()
        confirmationTimeoutJob = null
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
                failAttempt(id, origin, number, generation, AppErrorCodes.VOICE_UNAVAILABLE)
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
                failAttempt(id, origin, number, generation, AppErrorCodes.VOICE_TIMEOUT)
                return
            }
            when (recognition) {
                is AppResult.Failure -> {
                    val code = when (recognition.error.code) {
                        AppErrorCodes.VOICE_TIMEOUT -> AppErrorCodes.VOICE_TIMEOUT
                        AppErrorCodes.VOICE_NO_MATCH -> AppErrorCodes.VOICE_NO_MATCH
                        else -> AppErrorCodes.VOICE_UNAVAILABLE
                    }
                    failAttempt(id, origin, number, generation, code)
                }
                is AppResult.Success -> {
                    if (!publishProcessing(id, origin, number, generation)) return
                    // P07: only the explicitly approved artist phrase enters
                    // the sixth-intent path. Five exact transport controls
                    // continue through the unmodified #10 intent mapper.
                    val artistPhrase = PlayArtistPhrasePolicy.classify(
                        recognition.value.transcript, recognition.value.detectedLanguageTag,
                    )
                    if (artistPhrase is PlayArtistPhraseResult.Recognized ||
                        artistPhrase == PlayArtistPhraseResult.Unsupported
                    ) {
                        if (!markArtistAttempt(id, generation, number,
                                recognition.value.providerConfidence)) return
                        // The mic has already returned its transcript. Do not
                        // hold a temporary duck lease during a slow P03/P04
                        // library scan or the one-step confirmation wait.
                        // Keep the old #10 transport command duck policy intact.
                        runCatching { ducking.releaseVoiceDuck(duckToken) }
                        synchronized(gate) {
                            if (activeDuckToken == duckToken) activeDuckToken = null
                        }
                        handleArtistRecognition(
                            id, origin, number, generation, artistPhrase,
                            recognition.value.providerConfidence,
                        )
                        return
                    }
                    val decision = try {
                        intentMapper.map(recognition.value)
                    } catch (_: Exception) {
                        VoiceIntentDecision.Rejected
                    }
                    when (decision) {
                        is VoiceIntentDecision.Rejected,
                        is VoiceIntentDecision.RequiresConfirmation ->
                            failAttempt(
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
                                failAttempt(
                                    id, origin, number, generation, AppErrorCodes.VOICE_NO_MATCH
                                )
                                return
                            }
                            if (!claimDispatch(id, origin, number, generation)) return
                            if (!isCurrent(id, generation, number)) return
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
                                    failAttempt(
                                        id, origin, number, generation, AppErrorCodes.VOICE_UNAVAILABLE
                                    )
                                is AppResult.Success -> {
                                    if (!publishExecuted(id, origin, number, generation)) return
                                    // One brief optional sentence ONLY after
                                    // successful service ACK and published Executed.
                                    VoiceFeedbackPolicy.acknowledged(decision.intent)?.let { key ->
                                        deliverFeedback(id, origin, number, generation, key)
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
            // Confirmation must outlive recognition, but microphone/ducking
            // never do. P07 retains ONLY the generation lease for one answer.
            val keepForConfirmation = synchronized(gate) {
                if (activeDuckToken == duckToken) activeDuckToken = null
                val owned = currentId == id && childJob === generation && attempt == number
                val keep = owned && (pendingArtist?.generation === generation ||
                    confirmingDispatchInFlight)
                if (owned && !keep) childJob = null
                keep
            }
            if (!keepForConfirmation) generation.cancel()
        }
    }


    /**
     * P07: P02 grammar -> P03/P04 resolver -> P05 provider gate ->
     * P06 verified Media3 service command. This path is entered only for an
     * approved PLAY_ARTIST phrase; the five transport intents are untouched.
     * Unknown/unsupported/not-found do not mutate the existing player.
     */
    private suspend fun handleArtistRecognition(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        phrase: PlayArtistPhraseResult,
        providerConfidence: Double?,
    ) {
        val recognized = phrase as? PlayArtistPhraseResult.Recognized
        val resolver = artistResolver
        val executor = artistPlayback
        if (recognized == null || resolver == null || executor == null) {
            failAttempt(id, origin, number, generation,
                if (recognized == null) AppErrorCodes.VOICE_ARTIST_UNSUPPORTED
                else AppErrorCodes.VOICE_ARTIST_UNAVAILABLE)
            return
        }
        val resolved = try {
            resolver.resolve(recognized.slot)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            failAttempt(id, origin, number, generation,
                AppErrorCodes.VOICE_ARTIST_UNAVAILABLE)
            return
        }
        coroutineContext.ensureActive()
        if (!isCurrent(id, generation, number)) return

        val outcome = try {
            artistAdmission.decide(phrase, providerConfidence, resolved)
        } catch (_: Exception) {
            failAttempt(id, origin, number, generation,
                AppErrorCodes.VOICE_ARTIST_UNAVAILABLE)
            return
        }
        when (outcome) {
            is AppResult.Failure ->
                failAttempt(id, origin, number, generation,
                    if (outcome.error.code == AppErrorCodes.LIBRARY_BROWSE_STALE)
                        AppErrorCodes.VOICE_ARTIST_STALE
                    else AppErrorCodes.VOICE_ARTIST_UNAVAILABLE)
            is AppResult.Success -> when (val value = outcome.value) {
                is PlayArtistDecision.Direct -> {
                    if (!claimDispatch(id, origin, number, generation)) return
                    if (!isCurrent(id, generation, number)) return
                    val claim = PlayArtistDispatchClaim.fromDirect(value, id, number)
                    if (claim == null) {
                        failAttempt(id, origin, number, generation,
                            AppErrorCodes.VOICE_ARTIST_UNAVAILABLE)
                        return
                    }
                    val sent = try {
                        executor.execute(claim) {
                            synchronized(gate) {
                                isCurrentAndAuthorizedLocked(id, generation, number) &&
                                    dispatchClaimed &&
                                    !confirmingDispatchInFlight &&
                                    VoiceTransitionPolicy.mayComplete(
                                        mutableState.value, id, origin, number,
                                    )
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        failAttempt(id, origin, number, generation,
                            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                        return
                    }
                    coroutineContext.ensureActive()
                    if (!isCurrent(id, generation, number)) return
                    when (sent) {
                        is AppResult.Success -> {
                            if (sent.value == PlayArtistDecision.Executed) {
                                if (publishExecuted(id, origin, number, generation)) {
                                    recordArtistOutcome(id, generation, number,
                                        VoiceDiagnosticResult.EXECUTED)
                                    // P09: never say "Playing" before P06 validates
                                    // a service-owned Media3 acknowledgement.
                                    deliverFeedback(id, origin, number, generation,
                                        VoiceFeedbackKey.ArtistPlaying)
                                }
                            } else {
                                failAttempt(id, origin, number, generation,
                                    AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                            }
                        }
                        is AppResult.Failure ->
                            failAttempt(id, origin, number, generation,
                                AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                    }
                }
                is PlayArtistDecision.Ambiguous -> {
                    if (publishArtistConfirmation(id, origin, number, generation, value)) {
                        // This is a prompt, NOT a successful playback response.
                        // Do not hold up P08 option buttons awaiting optional TTS.
                        queueArtistClarification(id, origin, number, generation)
                    } else {
                        failAttempt(id, origin, number, generation,
                            AppErrorCodes.VOICE_ARTIST_STALE)
                    }
                }
                PlayArtistDecision.Unknown ->
                    failAttempt(id, origin, number, generation, AppErrorCodes.VOICE_NO_MATCH)
                PlayArtistDecision.Unsupported ->
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_UNSUPPORTED)
                PlayArtistDecision.NotFound ->
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_NOT_FOUND)
                PlayArtistDecision.Stale ->
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_STALE)
                PlayArtistDecision.Cancel ->
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_DECLINED)
                PlayArtistDecision.PlaybackFailure ->
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                PlayArtistDecision.Executed ->
                    // A decision result is not itself a Media3 acknowledgement.
                    failAttempt(id, origin, number, generation,
                        AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
            }
        }
    }

    /**
     * P09: the one short spoken clarification is optional and asynchronous.
     * The visible P08 choices must not wait for Android TTS availability.
     * P07 still owns the token/expiry; a new generation cannot speak for it.
     */
    private fun queueArtistClarification(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
    ) {
        artistPromptJob?.cancel()
        artistPromptJob = ownerScope.launch(generation) {
            val key = synchronized(gate) {
                val pending = pendingArtist
                val ui = mutableState.value as? VoiceSessionState.Confirming
                if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
                    pending == null || pending.generation !== generation || ui == null ||
                    ui.sessionId != id || ui.origin != origin ||
                    pending.snapshot.token != ui.token || artistPromptClaimed
                ) null else {
                    artistPromptClaimed = true
                    if (ui.options.size == 1) VoiceFeedbackKey.ArtistConfirmOne
                    else VoiceFeedbackKey.ArtistConfirmTwo
                }
            } ?: return@launch
            val output = VoiceFeedbackPolicy.output(id, key, feedbackCatalog)
                ?: return@launch
            try {
                if (tts.isAvailable()) {
                    withTimeoutOrNull(VoiceFeedbackPolicy.MAX_SPEAK_MS) {
                        val stillConfirming = synchronized(gate) {
                            val pending = pendingArtist
                            val ui = mutableState.value as? VoiceSessionState.Confirming
                            isCurrentAndAuthorizedLocked(id, generation, number) &&
                                pending != null && pending.generation === generation &&
                                ui != null && ui.sessionId == id && ui.origin == origin &&
                                ui.token == pending.snapshot.token
                        }
                        if (stillConfirming) tts.speak(output)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Optional TTS: UI answer and manual Player remain authoritative.
            }
        }
    }

    /**
     * P11: keep the transient STT result private. This claim stores only an
     * enum bucket and a monotonic attempt start timestamp (never persisted).
     * Called only for the sixth supported artist grammar, on this generation.
     */
    private fun markArtistAttempt(
        id: VoiceSessionId, generation: Job, number: Int,
        providerConfidence: Double?,
    ): Boolean = synchronized(gate) {
        if (!isCurrentAndAuthorizedLocked(id, generation, number)) return@synchronized false
        artistIntentObserved = true
        diagnosticConfidence = VoiceIntentDiagnosticPolicy.confidence(providerConfidence)
        true
    }

    /**
     * Capture one closed-schema terminal outcome per attempt while ownership
     * is valid. No candidate, transcript, token, MediaRef or exact millisecond
     * value crosses this boundary; null is "unknown", never guessed.
     * Caller owns gate.
     */
    private fun takeArtistOutcomeLocked(
        id: VoiceSessionId, generation: Job, number: Int,
        code: VoiceDiagnosticResult?,
    ): TelemetryEvent.VoiceIntentOutcome? {
        if (code == null || !artistIntentObserved || diagnosticClaimed ||
            !isCurrentLocked(id, generation, number)
        ) return null
        diagnosticClaimed = true
        val elapsedNanos = System.nanoTime() - attemptStartNanos
        val elapsedMs = elapsedNanos.takeIf { it >= 0L }?.div(1_000_000L)
        return TelemetryEvent.VoiceIntentOutcome(
            intentType = VoiceDiagnosticIntent.PLAY_ARTIST,
            confidenceBucket = diagnosticConfidence,
            latencyBucket = VoiceIntentDiagnosticPolicy.latency(elapsedMs),
            resultCode = code,
        )
    }

    /**
     * Diagnostics may be absent, throw or be canceled: voice and playback
     * never await the sink, and no failure/exception text is propagated or
     * logged. The production sink is NoOp until separate consent approval.
     */
    private fun dispatchArtistOutcome(event: TelemetryEvent.VoiceIntentOutcome?) {
        if (event == null) return
        try {
            ownerScope.launch(Dispatchers.Default) {
                try { tryRecordVoiceOutcome(event) } catch (_: Exception) {
                    // Optional diagnostic transport cannot block product flow.
                }
            }
        } catch (_: Exception) {
            // Could not schedule: silently drop, no retry or disk queue.
        }
    }

    /** Caller owns gate. */
    private fun queueArtistOutcomeLocked(
        id: VoiceSessionId, generation: Job, number: Int,
        code: VoiceDiagnosticResult?,
    ) {
        dispatchArtistOutcome(takeArtistOutcomeLocked(id, generation, number, code))
    }

    private fun recordArtistOutcome(
        id: VoiceSessionId, generation: Job, number: Int,
        code: VoiceDiagnosticResult,
    ) {
        synchronized(gate) {
            // Only the ACK-published Executed state may produce EXECUTED.
            if (code == VoiceDiagnosticResult.EXECUTED &&
                mutableState.value !is VoiceSessionState.Executed
            ) return@synchronized
            queueArtistOutcomeLocked(id, generation, number, code)
        }
    }

    private fun publishArtistConfirmation(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        result: PlayArtistDecision.Ambiguous,
    ): Boolean = synchronized(gate) {
        if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
            pendingArtist != null ||
            !VoiceTransitionPolicy.mayOfferArtistConfirmation(
                mutableState.value, id, origin, number, dispatchClaimed,
            ) ||
            !runCatching { canBeginSession() }.getOrDefault(false)
        ) return@synchronized false
        val index = artistLibrary?.index?.value ?: return@synchronized false
        if (index.status != LibraryIndexStatus.Ready ||
            index.revision != result.expectedLibraryRevision
        ) return@synchronized false

        val token = VoiceCandidateToken(UUID.randomUUID().toString())
        val options = result.candidates.mapIndexed { at, candidate ->
            PlayArtistConfirmationOption(at, candidate)
        }
        val snapshot = PlayArtistConfirmationSnapshot(
            sessionId = id, origin = origin, token = token,
            expectedLibraryRevision = result.expectedLibraryRevision,
            options = options,
        )
        pendingArtist = ArtistPending(snapshot, generation, number)
        mutableState.value = VoiceSessionState.Confirming(
            id, origin, token,
            java.util.Collections.unmodifiableList(
                snapshot.options.map { VoiceConfirmationOption(it.optionIndex, it.candidate.displayLabel) }
            ),
        )
        confirmationTimeoutJob?.cancel()
        confirmationTimeoutJob = ownerScope.launch(generation) {
            delay(CONFIRMATION_TIMEOUT_MS)
            expireArtistConfirmation(id, origin, number, generation, token)
        }
        true
    }

    private fun expireArtistConfirmation(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        token: VoiceCandidateToken,
    ) {
        synchronized(gate) {
            val pending = pendingArtist ?: return@synchronized
            val ui = mutableState.value
            if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
                pending.generation !== generation ||
                pending.snapshot.token != token ||
                ui !is VoiceSessionState.Confirming || ui.token != token
            ) return@synchronized
            finishArtistConfirmationLocked(pending, AppErrorCodes.VOICE_TIMEOUT)
        }
    }

    /** Terminal response without another STT turn or an executable command. */
    private fun finishArtistConfirmationLocked(
        pending: ArtistPending,
        code: String,
    ) {
        pendingArtist = null
        confirmationTimeoutJob?.cancel()
        confirmationTimeoutJob = null
        confirmingDispatchInFlight = false
        // Stop any outstanding short spoken question; cancellation/Neither
        // cannot leave a delayed TTS utterance over manual playback.
        artistPromptJob?.cancel()
        artistPromptJob = null
        runCatching { tts.stop(pending.snapshot.sessionId) }
        mutableState.value = VoiceSessionState.Failed(
            pending.snapshot.sessionId, pending.snapshot.origin,
            AppError.Voice(code, Recoverability.TERMINAL_FOR_ATTEMPT,
                RecoveryAction.ManualFallback),
            canRetry = false,
        )
        if (childJob === pending.generation) childJob = null
        pending.generation.cancel()
    }

    /**
     * P10: every post-suspension continuation checks the same app-foreground
     * and *fresh Android RECORD_AUDIO grant* policy used for explicit start.
     * If the active generation lost authority, invalidate it synchronously
     * before a queued STT, TTS or service ACK can publish a result.
     *
     * Caller owns gate. No new recording is started on foreground recovery.
     */
    private fun isCurrentAndAuthorizedLocked(
        id: VoiceSessionId, generation: Job, number: Int,
    ): Boolean {
        if (!isCurrentLocked(id, generation, number)) return false
        if (!runCatching { canBeginSession() }.getOrDefault(false)) {
            invalidateLocked()
            return false
        }
        return true
    }

    private fun isCurrent(id: VoiceSessionId, generation: Job, number: Int): Boolean =
        synchronized(gate) { isCurrentAndAuthorizedLocked(id, generation, number) }

    private fun isCurrentLocked(id: VoiceSessionId, generation: Job, number: Int): Boolean =
        currentId == id && childJob === generation && attempt == number &&
            generation.isActive

    private fun publishProcessing(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, generation: Job,
    ): Boolean = synchronized(gate) {
        if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
            !VoiceTransitionPolicy.mayProcess(mutableState.value, id, origin, number)
        ) return@synchronized false
        mutableState.value = VoiceSessionState.Processing(id, origin, number)
        true
    }

    private fun claimDispatch(
        id: VoiceSessionId, origin: VoiceOrigin, number: Int, generation: Job,
    ): Boolean = synchronized(gate) {
        if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
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
        if (!isCurrentAndAuthorizedLocked(id, generation, number) || !dispatchClaimed ||
            !VoiceTransitionPolicy.mayComplete(mutableState.value, id, origin, number)
        ) return@synchronized false
        mutableState.value = VoiceSessionState.Executed(id, origin)
        true
    }

    /** A retryable failure is visual-only to avoid talking over the retry. */
    private suspend fun failAttempt(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        code: String,
    ) {
        val failed = synchronized(gate) {
            if (!isCurrentAndAuthorizedLocked(id, generation, number)) return@synchronized null
            val active = mutableState.value
            if (!VoiceTransitionPolicy.mayProcess(active, id, origin, number) &&
                !VoiceTransitionPolicy.mayComplete(active, id, origin, number)
            ) return@synchronized null
            runCatching { stt.cancel(id) }
            activeDuckToken?.let { token ->
                runCatching { ducking.releaseVoiceDuck(token) }
                activeDuckToken = null
            }
            VoiceSessionState.Failed(
                id, origin,
                AppError.Voice(
                    code,
                    if (code in ARTIST_TERMINAL_CODES) Recoverability.TERMINAL_FOR_ATTEMPT
                    else Recoverability.RECOVERABLE,
                    RecoveryAction.ManualFallback,
                ),
                canRetry = code !in ARTIST_TERMINAL_CODES &&
                    VoiceTransitionPolicy.canOfferRetry(number, code),
            ).also { mutableState.value = it }
        } ?: return
        VoiceFeedbackPolicy.terminalFailure(code, failed.canRetry)?.let { key ->
            deliverFeedback(id, origin, number, generation, key)
        }
    }

    /**
     * Only the owning, current terminal attempt may claim its one sentence.
     * ACK/Failed state is already published: TTS has NO playback authority.
     * Provider errors, absence and 2s timeout leave UI/manual playback intact.
     */
    private suspend fun deliverFeedback(
        id: VoiceSessionId,
        origin: VoiceOrigin,
        number: Int,
        generation: Job,
        key: VoiceFeedbackKey,
    ) {
        val output = VoiceFeedbackPolicy.output(id, key, feedbackCatalog) ?: return
        val claimed = synchronized(gate) {
            val st = mutableState.value
            val terminal = when (st) {
                is VoiceSessionState.Executed ->
                    st.sessionId == id && st.origin == origin &&
                        key in SUCCESS_FEEDBACK_KEYS
                is VoiceSessionState.Failed ->
                    st.sessionId == id && st.origin == origin &&
                        !st.canRetry && key in FAILURE_FEEDBACK_KEYS
                else -> false
            }
            if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
                feedbackClaimed || !terminal)
                false
            else {
                feedbackClaimed = true // claim BEFORE suspending
                true
            }
        }
        if (!claimed) return
        try {
            if (tts.isAvailable() && isCurrent(id, generation, number)) {
                withTimeoutOrNull(VoiceFeedbackPolicy.MAX_SPEAK_MS) {
                    // Recheck immediately before optional audio; cancellation
                    // of the generation also stops any active utterance.
                    if (isCurrent(id, generation, number))
                        tts.speak(output) // never affects terminal state
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // A missing/offline TTS voice cannot undo a Media3 ACK.
        }
    }

    private companion object {
        // Explicit one-question window. Timeout is terminal, not a new listen.
        const val CONFIRMATION_TIMEOUT_MS = 10_000L
        val ARTIST_TERMINAL_CODES = setOf(
            AppErrorCodes.VOICE_ARTIST_NOT_FOUND,
            AppErrorCodes.VOICE_ARTIST_UNSUPPORTED,
            AppErrorCodes.VOICE_ARTIST_DECLINED,
            AppErrorCodes.VOICE_ARTIST_STALE,
            AppErrorCodes.VOICE_ARTIST_UNAVAILABLE,
            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED,
        )
        val SUCCESS_FEEDBACK_KEYS = setOf(
            VoiceFeedbackKey.Play, VoiceFeedbackKey.Pause, VoiceFeedbackKey.Next,
            VoiceFeedbackKey.Previous, VoiceFeedbackKey.SeekBack,
            VoiceFeedbackKey.ArtistPlaying,
        )
        val FAILURE_FEEDBACK_KEYS = setOf(
            VoiceFeedbackKey.Timeout, VoiceFeedbackKey.NotRecognized,
            VoiceFeedbackKey.Unavailable, VoiceFeedbackKey.ArtistNotFound,
            VoiceFeedbackKey.ArtistUnsupported, VoiceFeedbackKey.ArtistDeclined,
            VoiceFeedbackKey.ArtistStale, VoiceFeedbackKey.ArtistUnavailable,
            VoiceFeedbackKey.ArtistPlaybackFailure,
        )
    }

    /**
     * The only POSITIVE confirmation answer. This synchronous, lock-protected
     * method consumes the current snapshot/token BEFORE launching any suspend
     * MediaStore/Media3 work. Duplicate taps, wrong tokens, wrong generation,
     * and callbacks after timeout/cancel cannot dispatch the same choice.
     */
    override fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit> =
        synchronized(gate) {
            val pending = pendingArtist
                ?: return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
            val snapshot = pending.snapshot
            val generation = pending.generation
            val id = snapshot.sessionId
            val number = pending.attempt
            if (!isCurrentAndAuthorizedLocked(id, generation, number) ||
                !VoiceTransitionPolicy.mayClaimArtistConfirmation(
                    mutableState.value, id, snapshot.origin, token, dispatchClaimed,
                ) || snapshot.token != token
            ) return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)

            // An invalid option with a valid token is the ONE answer, not an
            // invitation to retry with a second choice or re-enter listening.
            val response = if (optionIndex in snapshot.options.indices)
                PlayArtistConfirmationResponse.Select(token, optionIndex) else null
            val claim = response?.let {
                PlayArtistDispatchClaim.fromSelectedConfirmation(snapshot, it, number)
            }
            if (claim == null) {
                finishArtistConfirmationLocked(pending, AppErrorCodes.VOICE_ARTIST_DECLINED)
                return@synchronized failure(AppErrorCodes.VOICE_ARTIST_DECLINED)
            }

            // Recheck ownership and index revision before accepting selection.
            val index = artistLibrary?.index?.value
            if (index == null || index.status != LibraryIndexStatus.Ready ||
                index.revision != snapshot.expectedLibraryRevision ||
                !runCatching { canBeginSession() }.getOrDefault(false)
            ) {
                finishArtistConfirmationLocked(pending, AppErrorCodes.VOICE_ARTIST_STALE)
                return@synchronized failure(AppErrorCodes.VOICE_ARTIST_STALE)
            }

            // Claim exactly once before coroutine launch or Media3 suspension.
            pendingArtist = null
            confirmationTimeoutJob?.cancel()
            confirmationTimeoutJob = null
            dispatchClaimed = true
            confirmingDispatchInFlight = true
            // An answer supersedes the optional question; never overlap
            // clarification audio with a subsequent service ACK result.
            artistPromptJob?.cancel()
            artistPromptJob = null
            runCatching { tts.stop(id) }
            mutableState.value = VoiceSessionState.Processing(id, snapshot.origin, number)

            val executor = artistPlayback
            ownerScope.launch(generation) {
                try {
                    if (!isCurrent(id, generation, number)) return@launch
                    if (executor == null) {
                        failAttempt(id, snapshot.origin, number, generation,
                            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                        return@launch
                    }
                    val ack = try {
                        executor.execute(claim) {
                            synchronized(gate) {
                                isCurrentAndAuthorizedLocked(id, generation, number) &&
                                    confirmingDispatchInFlight &&
                                    dispatchClaimed &&
                                    VoiceTransitionPolicy.mayComplete(
                                        mutableState.value, id, snapshot.origin, number,
                                    )
                            }
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        failAttempt(id, snapshot.origin, number, generation,
                            AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                        return@launch
                    }
                    coroutineContext.ensureActive()
                    if (!isCurrent(id, generation, number)) return@launch
                    when (ack) {
                        is AppResult.Success ->
                            if (ack.value == PlayArtistDecision.Executed) {
                                if (publishExecuted(id, snapshot.origin, number, generation)) {
                                    recordArtistOutcome(id, generation, number,
                                        VoiceDiagnosticResult.EXECUTED)
                                    deliverFeedback(id, snapshot.origin, number, generation,
                                        VoiceFeedbackKey.ArtistPlaying)
                                }
                            } else {
                                failAttempt(id, snapshot.origin, number, generation,
                                    AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                            }
                        is AppResult.Failure ->
                            failAttempt(id, snapshot.origin, number, generation,
                                AppErrorCodes.VOICE_ARTIST_PLAYBACK_FAILED)
                    }
                } finally {
                    // Do not retain an in-flight generation after terminal ACK.
                    synchronized(gate) {
                        if (currentId == id && childJob === generation && attempt == number) {
                            confirmingDispatchInFlight = false
                            childJob = null
                        }
                    }
                    generation.cancel()
                }
            }
            // An accepted UI selection is NOT itself the service ACK.
            AppResult.Success(Unit)
        }

    /** The one negative "Neither" answer is terminal, never a second STT turn. */
    override fun neither(token: VoiceCandidateToken): AppResult<Unit> =
        synchronized(gate) {
            val pending = pendingArtist
                ?: return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
            val snapshot = pending.snapshot
            if (!isCurrentAndAuthorizedLocked(snapshot.sessionId, pending.generation, pending.attempt) ||
                !VoiceTransitionPolicy.mayClaimArtistConfirmation(
                    mutableState.value, snapshot.sessionId, snapshot.origin, token,
                    dispatchClaimed,
                ) || snapshot.token != token
            ) return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
            finishArtistConfirmationLocked(pending, AppErrorCodes.VOICE_ARTIST_DECLINED)
            AppResult.Success(Unit)
        }

    /** Explicit token-scoped cancel resets to Idle but never changes playback. */
    override fun cancelConfirmation(token: VoiceCandidateToken): AppResult<Unit> =
        synchronized(gate) {
            val pending = pendingArtist
                ?: return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
            val snapshot = pending.snapshot
            if (!isCurrentAndAuthorizedLocked(snapshot.sessionId, pending.generation, pending.attempt) ||
                !VoiceTransitionPolicy.mayClaimArtistConfirmation(
                    mutableState.value, snapshot.sessionId, snapshot.origin, token,
                    dispatchClaimed,
                ) || snapshot.token != token
            ) return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
            invalidateLocked()
            AppResult.Success(Unit)
        }

    override fun retry(): AppResult<Unit> = synchronized(gate) {
        val failed = mutableState.value as? VoiceSessionState.Failed
            ?: return@synchronized failure(AppErrorCodes.VOICE_NO_MATCH)
        if (!VoiceTransitionPolicy.mayRetry(
                failed, currentId, currentOrigin, attempt
            )
        ) return@synchronized failure(AppErrorCodes.VOICE_SECOND_ATTEMPT_FAILED)
        if (closed || !runCatching { canBeginSession() }.getOrDefault(false) ||
            ownerScope.coroutineContext[Job]?.isActive != true
        ) {
            return@synchronized failure(AppErrorCodes.VOICE_UNAVAILABLE)
        }
        val id = currentId ?: return@synchronized failure(AppErrorCodes.VOICE_STALE_SESSION)
        // The same logical session is retried; invalidate/cancel the old
        // generation and its provider BEFORE starting attempt two.
        childJob?.cancel()
        childJob = null
        activeDuckToken?.let { token ->
            runCatching { ducking.releaseVoiceDuck(token) }
        }
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
        feedbackClaimed = false
        artistPromptClaimed = false
        artistPromptJob?.cancel()
        artistPromptJob = null
        pendingArtist = null
        confirmingDispatchInFlight = false
        confirmationTimeoutJob?.cancel()
        confirmationTimeoutJob = null
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
