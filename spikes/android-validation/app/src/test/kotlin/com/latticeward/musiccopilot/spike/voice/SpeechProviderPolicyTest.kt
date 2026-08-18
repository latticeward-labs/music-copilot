package com.latticeward.musiccopilot.spike.voice

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechProviderPolicyTest {
    @Test
    fun provenOnDeviceCapabilityDoesNotRequireCloudConsent() {
        val snapshot = SpeechCapabilitySnapshot(
            apiLevel = 34,
            recognitionServiceAvailable = true,
            onDeviceRecognitionAvailable = true,
            englishSupport = SpeechCapabilitySnapshot.SupportState.UNKNOWN_UNTIL_PROVIDER_CALLBACK,
        )
        assertEquals(
            ApprovedSpeechMode.ON_DEVICE,
            AndroidSpeechCapabilityProbe.approveMode(snapshot, disclosure = null, cloudConsentGranted = false),
        )
    }

    @Test
    fun genericNetworkServiceWithoutDisclosureFailsClosed() {
        val snapshot = SpeechCapabilitySnapshot(
            apiLevel = 29,
            recognitionServiceAvailable = true,
            onDeviceRecognitionAvailable = false,
            englishSupport = SpeechCapabilitySnapshot.SupportState.UNKNOWN_UNTIL_PROVIDER_CALLBACK,
        )
        assertEquals(
            ApprovedSpeechMode.MANUAL_ONLY,
            AndroidSpeechCapabilityProbe.approveMode(snapshot, disclosure = null, cloudConsentGranted = true),
        )
    }

    @Test
    fun completeDisclosureStillRequiresSeparateConsent() {
        val snapshot = SpeechCapabilitySnapshot(
            apiLevel = 34,
            recognitionServiceAvailable = true,
            onDeviceRecognitionAvailable = false,
            englishSupport = SpeechCapabilitySnapshot.SupportState.UNKNOWN_UNTIL_PROVIDER_CALLBACK,
        )
        val disclosure = CloudDisclosure("reviewed provider", "speech transcription", "reviewed retention")
        assertEquals(
            ApprovedSpeechMode.MANUAL_ONLY,
            AndroidSpeechCapabilityProbe.approveMode(snapshot, disclosure, cloudConsentGranted = false),
        )
        assertEquals(
            ApprovedSpeechMode.NETWORK_ASSISTED_WITH_CONSENT,
            AndroidSpeechCapabilityProbe.approveMode(snapshot, disclosure, cloudConsentGranted = true),
        )
    }
}
