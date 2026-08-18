package com.latticeward.musiccopilot.spike.voice

import android.content.Context
import android.os.Build
import android.speech.SpeechRecognizer

data class SpeechCapabilitySnapshot(
    val apiLevel: Int,
    val recognitionServiceAvailable: Boolean,
    val onDeviceRecognitionAvailable: Boolean,
    val englishSupport: SupportState,
) {
    enum class SupportState {
        UNKNOWN_UNTIL_PROVIDER_CALLBACK,
        NOT_AVAILABLE,
    }
}

data class CloudDisclosure(
    val providerOrDestination: String?,
    val purpose: String?,
    val retention: String?,
) {
    val isComplete: Boolean
        get() = !providerOrDestination.isNullOrBlank() &&
            !purpose.isNullOrBlank() &&
            !retention.isNullOrBlank()
}

enum class ApprovedSpeechMode {
    ON_DEVICE,
    NETWORK_ASSISTED_WITH_CONSENT,
    MANUAL_ONLY,
}

object AndroidSpeechCapabilityProbe {
    fun snapshot(context: Context): SpeechCapabilitySnapshot {
        val serviceAvailable = SpeechRecognizer.isRecognitionAvailable(context)
        val onDeviceAvailable = Build.VERSION.SDK_INT >= 31 &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        return SpeechCapabilitySnapshot(
            apiLevel = Build.VERSION.SDK_INT,
            recognitionServiceAvailable = serviceAvailable,
            onDeviceRecognitionAvailable = onDeviceAvailable,
            englishSupport = if (serviceAvailable) {
                SpeechCapabilitySnapshot.SupportState.UNKNOWN_UNTIL_PROVIDER_CALLBACK
            } else {
                SpeechCapabilitySnapshot.SupportState.NOT_AVAILABLE
            },
        )
    }

    /**
     * The generic Android API cannot attest a network recognizer's destination or retention.
     * Those fields must come from a separately reviewed provider disclosure.
     */
    fun approveMode(
        snapshot: SpeechCapabilitySnapshot,
        disclosure: CloudDisclosure?,
        cloudConsentGranted: Boolean,
    ): ApprovedSpeechMode = when {
        snapshot.onDeviceRecognitionAvailable -> ApprovedSpeechMode.ON_DEVICE
        snapshot.recognitionServiceAvailable && disclosure?.isComplete == true && cloudConsentGranted ->
            ApprovedSpeechMode.NETWORK_ASSISTED_WITH_CONSENT
        else -> ApprovedSpeechMode.MANUAL_ONLY
    }
}
