package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.interruption.InterruptionEnvelope
import com.latticeward.musiccopilot.playback.interruption.InterruptionEvent
import com.latticeward.musiccopilot.playback.interruption.InterruptionNotice
import com.latticeward.musiccopilot.playback.interruption.InterruptionNoticePolicy
import com.latticeward.musiccopilot.playback.interruption.InterruptionReducer
import com.latticeward.musiccopilot.playback.interruption.InterruptionState
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** P12 authored static/JVM source specifications only, NOT EXECUTED. */
class Issue8Phase12NoticeAndOwnershipTest {
    private val sourceRoot = File("src/main/java/com/latticeward/musiccopilot")
    private fun code(path: String) = File(sourceRoot, path).readText()
    private fun once(e: InterruptionEvent) = InterruptionReducer.reduce(
        InterruptionState(), InterruptionEnvelope(1L, 1L, e)
    )

    @Test fun routeDisconnectYieldsOnlyReviewedErrorCodeAndManualRecovery() {
        val transition = once(InterruptionEvent.RouteDisconnected)
        val notice = InterruptionNoticePolicy.fromAccepted(
            InterruptionEvent.RouteDisconnected, transition)
        assertEquals(InterruptionNotice.ROUTE_UNSAFE, notice)
        val error = InterruptionNoticePolicy.error(notice!!)
        assertEquals(AppErrorCodes.ROUTE_INVALIDATED, error?.code)
        assertEquals(Recoverability.USER_ACTION_REQUIRED, error?.recoverability)
        assertEquals(RecoveryAction.ManualResume, error?.recoveryAction)
    }

    @Test fun transientFocusLossUsesNonTelephonySafePausedCode() {
        val event = InterruptionEvent.FocusLostTransient(wasPlayingBeforeLoss = false)
        val notice = InterruptionNoticePolicy.fromAccepted(event, once(event))
        assertEquals(InterruptionNotice.FOCUS_TEMPORARY, notice)
        val error = InterruptionNoticePolicy.error(notice!!)
        assertEquals(AppErrorCodes.CALL_OR_ROUTE_SAFE_PAUSED, error?.code)
        assertEquals(Recoverability.RECOVERABLE, error?.recoverability)
    }

    @Test fun permanentFocusLossAndDeniedReturnUseFocusUnavailable() {
        for (event in listOf(
            InterruptionEvent.FocusLostPermanent,
            InterruptionEvent.FocusAutoResumeRejected
        )) {
            val notice = InterruptionNoticePolicy.fromAccepted(event, once(event))
            assertEquals(InterruptionNotice.FOCUS_UNAVAILABLE, notice)
            val error = InterruptionNoticePolicy.error(notice!!)
            assertEquals(AppErrorCodes.FOCUS_NOT_RECOVERED, error?.code)
            assertEquals(RecoveryAction.ManualResume, error?.recoveryAction)
        }
    }

    @Test fun duplicateRouteSignalsCannotEmitDuplicateVoiceOrNotice() {
        val first = once(InterruptionEvent.RouteDisconnected)
        val duplicate = InterruptionReducer.reduce(first.state,
            InterruptionEnvelope(2L, 2L, InterruptionEvent.RouteDisconnected))
        assertTrue(duplicate.effects.isEmpty())
        assertNull(InterruptionNoticePolicy.fromAccepted(
            InterruptionEvent.RouteDisconnected, duplicate))
    }

    @Test fun clearedNoticeHasNoPersistedErrorPayload() {
        assertNull(InterruptionNoticePolicy.error(InterruptionNotice.CLEARED))
        val code = code("playback/interruption/InterruptionNoticePolicy.kt")
        for (secret in listOf("Log.", "BluetoothDevice.", "TelephonyManager",
            "writeText(", "FileOutputStream(", "INTERNET")) {
            assertFalse(secret, code.contains(secret))
        }
    }

    @Test fun oneMedia3PlayerAudioFocusRequesterAndNotificationOwner() {
        val service = code("playback/service/MusicPlaybackService.kt")
        val transport = code("playback/media3/ServiceSessionTransportPlayer.kt")
        assertEquals(1, Regex("ExoPlayer\\.Builder\\(").findAll(service).count())
        assertEquals(1, Regex("MediaSession\\.Builder\\(").findAll(service).count())
        assertTrue(service.contains("candidate.setAudioAttributes("))
        assertFalse(service.contains("requestAudioFocus("))
        assertFalse(transport.contains("delegate.play()"))
        assertTrue(service.contains("super.onUpdateNotification(session, startInForegroundRequired)"))
    }

    @Test fun p09VoiceEffectsReachExistingAppCoordinatorWithoutNewProvider() {
        val observer = code("playback/service/Media3FocusObservationAdapter.kt")
        val service = code("playback/service/MusicPlaybackService.kt")
        val root = code("app/MusicCopilotApplication.kt")
        val coordinator = code("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(observer.contains("CANCEL_VOICE_FOR_FOCUS"))
        assertTrue(observer.contains("CANCEL_VOICE_FOR_ROUTE"))
        assertTrue(service.contains("onVoiceCancellationRequested ="))
        assertTrue(root.contains("bindings.voiceSessionCoordinator.cancel("))
        assertTrue(coordinator.contains("runCatching { stt.cancel(id) }"))
        assertTrue(coordinator.contains("runCatching { tts.stop(id) }"))
        assertFalse(service.contains("SpeechRecognizer.createSpeechRecognizer("))
    }

    @Test fun p07ReceiptStampAndP10ErrorGuidanceCannotBeOverwrittenByOldCommand() {
        val worker = code("playback/command/PlaybackCommandProcessor.kt")
        val service = code("playback/service/MusicPlaybackService.kt")
        assertTrue(worker.contains("captureReceipt(command)"))
        assertTrue(worker.contains("isReceiptCurrent(pending.command, pending.receipt)"))
        assertTrue(service.contains("val commandEpoch = restoreGeneration"))
        assertTrue(service.contains("if (runtimeError is AppError.Interruption)"))
        assertTrue(service.contains("val verifiedRecoveryAttempt"))
    }

    @Test fun p11BluetoothVersionGuardAndOwnerHandsetMatrixExist() {
        val gate = code("playback/service/PlaybackInterruptionGate.kt")
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val audit = File("../docs/implementation/" +
            "ISSUE_8_PHASE11_ANDROID_OEM_ACCESSIBILITY_COMPATIBILITY.md").readText()
        assertTrue(gate.contains("Build.VERSION.SDK_INT < Build.VERSION_CODES.S"))
        assertTrue(gate.contains("TYPE_BLE_HEADSET"))
        assertTrue(gate.contains("TYPE_BLE_SPEAKER"))
        assertTrue(manifest.contains("foregroundServiceType=\"mediaPlayback\""))
        assertTrue(audit.contains("Honor x7d"))
        assertTrue(audit.contains("Pixel"))
        assertTrue(audit.contains("Samsung"))
        assertTrue(audit.contains("Xiaomi"))
    }
}
