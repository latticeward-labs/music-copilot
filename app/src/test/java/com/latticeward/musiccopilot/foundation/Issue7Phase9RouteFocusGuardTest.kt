package com.latticeward.musiccopilot.foundation

import android.media.AudioDeviceInfo
import android.os.Build
import androidx.media3.common.Player
import com.latticeward.musiccopilot.playback.service.PlaybackInterruptionGate
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #7 P09 specification tests; AUTHORED ONLY, never run in CI or during
 * an intermediate phase. All Android/route/headset cases need P13 device QA.
 */
class Issue7Phase9RouteFocusGuardTest {
    @Test fun startsWithoutRouteLatchOrManualPause() {
        val gate = PlaybackInterruptionGate()
        assertEquals(0L, gate.routeGeneration)
        assertFalse(gate.shouldSuppressAutomaticPlay())
    }

    @Test fun unplugSetsExplicitResumeLatch() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        assertTrue(gate.requiresExplicitResume)
        assertTrue(gate.shouldSuppressAutomaticPlay())
    }

    @Test fun duplicateNoisyAndRemovedEventsShareOneRouteGeneration() {
        val gate = PlaybackInterruptionGate()
        assertTrue(gate.onHeadsetOrBluetoothLost())
        assertEquals(1L, gate.routeGeneration)
        assertFalse(gate.onHeadsetOrBluetoothLost())
        assertEquals(1L, gate.routeGeneration)
        gate.onExplicitUserPlaybackRequest()
        assertTrue(gate.onHeadsetOrBluetoothLost())
        assertEquals(2L, gate.routeGeneration)
    }

    @Test fun pendingMediaStoreRequestIsRejectedAfterLostRoute() {
        val gate = PlaybackInterruptionGate()
        val before = gate.routeGeneration
        gate.onHeadsetOrBluetoothLost()
        assertFalse(gate.requestIsStillCurrent(before))
        assertTrue(gate.requestIsStillCurrent(gate.routeGeneration))
    }

    @Test fun explicitPlayIsOnlyRouteResumeUnlock() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        gate.onExplicitUserPlaybackRequest()
        assertFalse(gate.shouldSuppressAutomaticPlay())
    }

    @Test fun manualPauseWinsOverLaterFocusRegain() {
        val gate = PlaybackInterruptionGate()
        gate.onManualPauseOrStop()
        assertTrue(gate.explicitlyPaused)
        assertTrue(gate.shouldSuppressAutomaticPlay())
    }

    // Issue #8 owner feedback: M34 notification Play and Bluetooth/AirPods
    // Play are new external transport requests, not delayed focus auto-resume.
    // The source of these tests is authored only; no JVM tests run for APK.
    @Test fun pausedNotificationOrHeadsetPlayIsAdmittedOnStableRoute() {
        val gate = PlaybackInterruptionGate()
        gate.onManualPauseOrStop()
        assertTrue(gate.shouldSuppressAutomaticPlay())
        assertTrue(gate.mayAdmitExternalPlayAttempt())
        gate.onExplicitUserPlaybackRequest()
        assertFalse(gate.shouldSuppressAutomaticPlay())
    }

    @Test fun lostBluetoothRouteStillRejectsNotificationAndHeadsetPlay() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        assertFalse(gate.mayAdmitExternalPlayAttempt())
        gate.onBluetoothSinkAvailable(alreadyPlayingOrRequested = false)
        assertFalse(gate.mayAdmitExternalPlayAttempt())
    }

    @Test fun initialBluetoothConnectionWhilePausedRemainsQuarantined() {
        val gate = PlaybackInterruptionGate()
        gate.onBluetoothSinkAvailable(alreadyPlayingOrRequested = false)
        assertFalse(gate.mayAdmitExternalPlayAttempt())
        gate.onExplicitUserPlaybackRequest()
        gate.onManualPauseOrStop()
        assertTrue(gate.mayAdmitExternalPlayAttempt())
    }

    @Test fun routeLossLatchSurvivesManualPause() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        gate.onManualPauseOrStop()
        assertTrue(gate.requiresExplicitResume)
        assertTrue(gate.explicitlyPaused)
    }

    @Test fun explicitPlayClearsManualPauseAndRouteLatchTogether() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        gate.onManualPauseOrStop()
        gate.onExplicitUserPlaybackRequest()
        assertFalse(gate.shouldSuppressAutomaticPlay())
        assertFalse(gate.requiresExplicitResume)
        assertFalse(gate.explicitlyPaused)
    }

    @Test fun explicitResumeDoesNotRollbackRouteGeneration() {
        val gate = PlaybackInterruptionGate()
        gate.onHeadsetOrBluetoothLost()
        gate.onExplicitUserPlaybackRequest()
        assertEquals(1L, gate.routeGeneration)
    }

    @Test fun wiredHeadsetTypesAreProtected() {
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_WIRED_HEADSET))
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_WIRED_HEADPHONES))
    }

    @Test fun classicBluetoothOutputTypesAreProtected() {
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP))
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_BLUETOOTH_SCO))
    }

    @Test fun usbAndHearingAidOutputsAreProtectedAndBleNeedsApi31() {
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_USB_HEADSET))
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_USB_DEVICE))
        assertTrue(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_HEARING_AID))
        // JVM Android stubs are NOT Android 12 devices. P11 intentionally
        // declines to classify API31 BLE constants on SDK29/30.
        val bleSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        assertEquals(bleSupported, PlaybackInterruptionGate.isPersonalOutput(
            AudioDeviceInfo.TYPE_BLE_HEADSET))
        assertEquals(bleSupported, PlaybackInterruptionGate.isPersonalOutput(
            AudioDeviceInfo.TYPE_BLE_SPEAKER))
    }

    @Test fun builtInSpeakerLossNeverTriggersHeadsetGuard() {
        assertFalse(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER))
        assertFalse(PlaybackInterruptionGate.isPersonalOutput(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE))
    }

    @Test fun media3BecomingNoisyReasonIsRecognized() {
        assertTrue(PlaybackInterruptionGate.isNoisyChangeReason(
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_BECOMING_NOISY
        ))
        assertFalse(PlaybackInterruptionGate.isNoisyChangeReason(
            Player.PLAY_WHEN_READY_CHANGE_REASON_USER_REQUEST
        ))
        assertFalse(PlaybackInterruptionGate.isNoisyChangeReason(
            Player.PLAY_WHEN_READY_CHANGE_REASON_AUDIO_FOCUS_LOSS
        ))
    }

    @Test fun routeLossAfterFocusPauseStillRequiresManualResume() {
        val gate = PlaybackInterruptionGate()
        gate.onManualPauseOrStop()
        gate.onHeadsetOrBluetoothLost()
        assertTrue(gate.shouldSuppressAutomaticPlay())
        gate.onExplicitUserPlaybackRequest()
        assertFalse(gate.shouldSuppressAutomaticPlay())
    }
}
