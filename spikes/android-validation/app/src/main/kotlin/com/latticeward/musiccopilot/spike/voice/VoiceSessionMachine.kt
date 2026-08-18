package com.latticeward.musiccopilot.spike.voice

class VoiceSessionMachine {
    var state: VoiceState = VoiceState.IDLE
        private set

    var attempt: Int = 0
        private set

    var retryAvailable: Boolean = false
        private set

    var terminalFailure: RecognitionFailure? = null
        private set

    fun start(): List<VoiceEffect> {
        check(state == VoiceState.IDLE || state == VoiceState.FAILED)
        attempt = 1
        retryAvailable = false
        terminalFailure = null
        state = VoiceState.LISTENING
        return listOf(
            VoiceEffect.SetDucking(true),
            VoiceEffect.StartListening(LISTENING_TIMEOUT_MILLIS, attempt),
        )
    }

    fun onMapping(result: MappingResult): List<VoiceEffect> {
        check(state == VoiceState.LISTENING)
        state = VoiceState.PROCESSING
        return when (result) {
            is MappingResult.Recognized -> listOf(
                VoiceEffect.SetDucking(false),
                VoiceEffect.Execute(result.candidate),
            )
            is MappingResult.Ambiguous -> {
                state = VoiceState.CONFIRMING
                listOf(
                    VoiceEffect.SetDucking(false),
                    VoiceEffect.Speak(confirmationSentence(result.options)),
                )
            }
            is MappingResult.Unknown,
            is MappingResult.Unsupported,
            -> failSafely(RecognitionFailure.NO_MATCH, allowRetry = false)
        }
    }

    fun onRecognitionFailure(failure: RecognitionFailure): List<VoiceEffect> {
        check(state == VoiceState.LISTENING || state == VoiceState.PROCESSING)
        val retryable = failure in RETRYABLE_FAILURES && attempt == 1
        return failSafely(failure, allowRetry = retryable)
    }

    fun retry(): List<VoiceEffect> {
        check(state == VoiceState.FAILED && retryAvailable && attempt == 1)
        attempt = 2
        retryAvailable = false
        terminalFailure = null
        state = VoiceState.LISTENING
        return listOf(
            VoiceEffect.SetDucking(true),
            VoiceEffect.StartListening(LISTENING_TIMEOUT_MILLIS, attempt),
        )
    }

    fun confirm(option: IntentCandidate?): List<VoiceEffect> {
        check(state == VoiceState.CONFIRMING)
        if (option == null) return failSafely(RecognitionFailure.NO_MATCH, allowRetry = false)
        state = VoiceState.PROCESSING
        return listOf(VoiceEffect.Execute(option))
    }

    fun onCommandAcknowledged(spokenResult: String): List<VoiceEffect> {
        check(state == VoiceState.PROCESSING)
        state = VoiceState.EXECUTED
        return listOf(
            VoiceEffect.SetDucking(false),
            VoiceEffect.Speak(oneSentence(spokenResult)),
        )
    }

    fun onCommandRejected(): List<VoiceEffect> =
        failSafely(RecognitionFailure.NO_MATCH, allowRetry = false)

    fun onTtsFailure(): List<VoiceEffect> {
        check(state == VoiceState.EXECUTED)
        return emptyList()
    }

    fun cancel(): List<VoiceEffect> =
        failSafely(RecognitionFailure.CANCELLED, allowRetry = false)

    private fun failSafely(
        failure: RecognitionFailure,
        allowRetry: Boolean,
    ): List<VoiceEffect> {
        state = VoiceState.FAILED
        terminalFailure = failure
        retryAvailable = allowRetry
        return listOf(
            VoiceEffect.SetDucking(false),
            VoiceEffect.ShowManualControls,
        )
    }

    private fun confirmationSentence(options: List<IntentCandidate>): String {
        val prompt = when {
            options.size == 1 -> "Confirm ${options.single().artist?.displayName ?: options.single().intent.name.lowercase()}?"
            else -> "${options[0].artist?.displayName} or ${options[1].artist?.displayName}?"
        }
        return prompt.trim().replace(Regex("[.!?]+.*$"), "?").let {
            if (it.endsWith('?')) it else "$it?"
        }
    }

    private fun oneSentence(value: String): String =
        value.trim().replace(Regex("[.!?]+.*$"), ".").let { if (it.endsWith('.')) it else "$it." }

    companion object {
        const val LISTENING_TIMEOUT_MILLIS = 6_000L

        private val RETRYABLE_FAILURES = setOf(
            RecognitionFailure.TIMEOUT,
            RecognitionFailure.NO_MATCH,
            RecognitionFailure.PROVIDER_UNAVAILABLE,
            RecognitionFailure.LANGUAGE_UNAVAILABLE,
            RecognitionFailure.NETWORK,
            RecognitionFailure.AUDIO,
        )
    }
}
