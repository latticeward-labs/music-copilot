package com.latticeward.musiccopilot.spike.voice

enum class VoiceIntent {
    PLAY,
    PAUSE,
    NEXT,
    PREVIOUS,
    SEEK_BACK,
    PLAY_ARTIST,
}

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    CONFIRMING,
    EXECUTED,
    FAILED,
}

enum class MappingFailureReason {
    EMPTY,
    LOW_CONFIDENCE,
    MISSING_ARTIST,
    ARTIST_NOT_FOUND,
    UNKNOWN_PHRASE,
    UNSUPPORTED_INTENT,
}

data class Artist(
    val stableId: String,
    val displayName: String,
    val historyRank: Int = 0,
)

data class IntentCandidate(
    val intent: VoiceIntent,
    val artist: Artist? = null,
    val score: Double,
)

sealed interface MappingResult {
    data class Recognized(val candidate: IntentCandidate) : MappingResult

    data class Ambiguous(
        val options: List<IntentCandidate>,
        val reason: MappingFailureReason,
    ) : MappingResult

    data class Unknown(val reason: MappingFailureReason) : MappingResult

    data class Unsupported(val reason: MappingFailureReason) : MappingResult
}

enum class RecognitionFailure {
    TIMEOUT,
    NO_MATCH,
    PROVIDER_UNAVAILABLE,
    LANGUAGE_UNAVAILABLE,
    PERMISSION_DENIED,
    NETWORK,
    AUDIO,
    CANCELLED,
}

sealed interface VoiceEffect {
    data class SetDucking(val enabled: Boolean) : VoiceEffect
    data class StartListening(
        val timeoutMillis: Long,
        val attempt: Int,
        val sessionToken: Long,
    ) : VoiceEffect
    data class Execute(val candidate: IntentCandidate) : VoiceEffect
    data class Speak(val sentence: String) : VoiceEffect
    data object ShowManualControls : VoiceEffect
}
