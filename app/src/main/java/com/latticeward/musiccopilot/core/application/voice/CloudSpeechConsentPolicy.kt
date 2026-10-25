package com.latticeward.musiccopilot.core.application.voice

/** Do not confuse a local STT provider with approved remote processing. */
data class RemoteSpeechDisclosure(
    val processor: String,
    val destination: String,
    val purpose: String,
    val retention: String,
) {
    /**
     * A stored "Granted" flag does NOT establish a legal disclosure.
     * Reject placeholder/obscured fields and control characters: future
     * provider activation still requires a separate UX/legal review.
     */
    fun complete(): Boolean = listOf(processor, destination, purpose, retention).all {
        val field = it.trim()
        field.isNotEmpty() && field.length <= 256 &&
            field.lowercase(java.util.Locale.ROOT) !in PLACEHOLDERS &&
            field.none { char -> Character.isISOControl(char) }
    }

    private companion object {
        val PLACEHOLDERS = setOf(
            "unknown", "tbd", "n/a", "na", "none", "pending",
            "not applicable", "not disclosed", "undisclosed", "-"
        )
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
