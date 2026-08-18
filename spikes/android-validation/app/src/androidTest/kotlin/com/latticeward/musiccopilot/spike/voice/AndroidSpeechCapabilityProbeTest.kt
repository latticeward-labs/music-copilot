package com.latticeward.musiccopilot.spike.voice

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidSpeechCapabilityProbeTest {
    @Test
    fun platformProbeNeverInfersCloudDisclosure() {
        val snapshot = AndroidSpeechCapabilityProbe.snapshot(ApplicationProvider.getApplicationContext())
        val mode = AndroidSpeechCapabilityProbe.approveMode(
            snapshot = snapshot,
            disclosure = null,
            cloudConsentGranted = false,
        )
        assertTrue(snapshot.apiLevel >= 29)
        assertEquals(
            if (snapshot.onDeviceRecognitionAvailable) {
                ApprovedSpeechMode.ON_DEVICE
            } else {
                ApprovedSpeechMode.MANUAL_ONLY
            },
            mode,
        )
    }
}
