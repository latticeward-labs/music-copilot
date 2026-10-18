package com.latticeward.musiccopilot.core.application.voice

/** Do not confuse a local STT provider with approved remote processing. */
data class RemoteSpeechDisclosure(
    val processor: String,
    val destination: String,
    val purpose: String,
    val retention: String,
) {
    fun complete(): Boolean = listOf(processor, destination, purpose, retention).all {
        it.isNotBlank() && it.trim() != "unknown" && it.trim() != "TBD"
    }
}

enum class CloudSpeechGateDecision {
    DisabledNoApprovedAdapter, DisabledNoLegalApproval, DisabledNoDisclosure,
    DisabledNoConsent, DisabledWhileDriving, Permitted,
}

/** Pure fail-closed policy; this MVP registers NO remote adapter/approval. */
object CloudSpeechConsentPolicy {
    fun evaluate(
        remoteAdapterApproved: Boolean,
        legalApproval: Boolean,
        disclosure: RemoteSpeechDisclosure?,
        consentGranted: Boolean,
        drivingMode: Boolean,
    ): CloudSpeechGateDecision = when {
        !remoteAdapterApproved -> CloudSpeechGateDecision.DisabledNoApprovedAdapter
        !legalApproval -> CloudSpeechGateDecision.DisabledNoLegalApproval
        disclosure?.complete() != true -> CloudSpeechGateDecision.DisabledNoDisclosure
        !consentGranted -> CloudSpeechGateDecision.DisabledNoConsent
        drivingMode -> CloudSpeechGateDecision.DisabledWhileDriving
        else -> CloudSpeechGateDecision.Permitted
    }
}
