package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.voice.api.SpeechEndpointEvidence
import com.latticeward.musiccopilot.voice.api.SpeechEndpointQuality
import com.latticeward.musiccopilot.voice.api.SpeechRecognitionRequest
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * P10 source-authored integration checklist, NOT RUN / NOT COMPILED.
 * Instrumented device QA P14 must verify physical end-of-speech and visible
 * response; source assertions do not certify real-world timing accuracy.
 */
class Issue12Phase10MeasurementWiringTest {
    private val root = "src/main/java/com/latticeward/musiccopilot/"
    private fun read(path: String) = File(root + path).readText()

    @Test fun speechRequestCarriesOnlyEphemeralEndpointEvidence() {
        val seen = mutableListOf<SpeechEndpointEvidence>()
        val sample = SpeechEndpointEvidence(
            elapsedRealtimeNanos = 123456L,
            quality = SpeechEndpointQuality.VAD_DETECTED,
        )
        val req = SpeechRecognitionRequest(VoiceSessionId(1), "en-US",
            onEndpointSignal = { seen.add(it) })
        req.onEndpointSignal?.invoke(sample)
        assertEquals(listOf(sample), seen)
        assertEquals(SpeechEndpointQuality.VAD_DETECTED, seen.first().quality)
    }

    @Test fun moonshineTimestampIsAfterRealSileroNonEmptyResult() {
        val s = read("voice/android/MoonshineSileroSpeechToTextEngine.kt")
        val anchor = s.substringAfter("detector.acceptWaveform(")
            .substringBefore("recorder.stop()")
        assertTrue(anchor.contains("if (!detector.empty())"))
        assertTrue(anchor.contains("completed = true"))
        assertTrue(anchor.contains("SpeechEndpointQuality.VAD_DETECTED"))
        assertTrue(anchor.contains("request.onEndpointSignal?.invoke(marker)"))
    }

    @Test fun silero550msSilenceMakesEndpointOnlyAProxyNotTrueSpeechEnd() {
        val s = read("voice/android/MoonshineSileroSpeechToTextEngine.kt")
        assertTrue(s.contains("minSilenceDuration = 0.55f"))
        assertTrue(s.contains("this marker is"))
        assertFalse(s.contains("SpeechEndpointQuality.QA_VERIFIED_SPEECH_END"))
    }

    @Test fun androidOnDeviceRecognizerMarksEndOfSpeechNotASRResultsTimestamp() {
        val s = read("voice/android/AndroidOnDeviceSpeechToTextEngine.kt")
        val callback = s.substringAfter("override fun onEndOfSpeech()")
            .substringBefore("override fun onEvent(")
        assertTrue(callback.contains("SpeechEndpointQuality.ANDROID_RECOGNIZER_DETECTED"))
        assertTrue(callback.contains("live !== session"))
        assertTrue(callback.contains("onEndpointSignal?.invoke("))
    }

    @Test fun lateOrCancelledProviderCannotAttributeMarkerToNewRetry() {
        val coordinator = read("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(coordinator.contains("isCurrentAndAuthorizedLocked(id, generation, number)"))
        assertTrue(coordinator.contains("speechEndpointSignal == null"))
        assertTrue(coordinator.contains("speechEndpointSignal = null"))
        assertTrue(coordinator.contains("terminalFirstDrawClaimed = false"))
    }

    @Test fun voiceResponseMeasurementCannotBeSTTStartToAckBucket() {
        val coordinator = read("voice/session/DefaultVoiceSessionCoordinator.kt")
        val statement = coordinator.substringAfter("fun onTerminalLabelDrawn(")
            .substringBefore("private fun currentLatencyBucketLocked(")
        assertTrue(statement.contains("speechEndpointSignal"))
        assertTrue(statement.contains("elapsedRealtimeNanos()"))
        assertTrue(statement.contains("onTerminalDrawLatencyProxy"))
        assertFalse(statement.contains("attemptStartNanos"))
    }

    @Test fun firstDrawIsTerminalOnlyNotProcessingOrConfirmation() {
        val panel = read("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("Modifier.fillMaxWidth()\n                .drawWithContent"))
        assertTrue(panel.contains("state is VoiceSessionState.Executed"))
        assertTrue(panel.contains("state is VoiceSessionState.Failed"))
        assertTrue(panel.contains("drawContent()"))
        assertTrue(panel.contains("onTerminalLabelDrawn(state)"))
        assertFalse(panel.contains("onTerminalLabelDrawn(VoiceSessionState.Processing"))
    }

    @Test fun currentVoiceGenerationClaimsOnlyOneTerminalDraw() {
        val coordinator = read("voice/session/DefaultVoiceSessionCoordinator.kt")
        val body = coordinator.substringAfter("fun onTerminalLabelDrawn(")
            .substringBefore("private fun currentLatencyBucketLocked(")
        assertTrue(body.contains("terminalFirstDrawClaimed"))
        assertTrue(body.contains("visible != mutableState.value"))
        assertTrue(body.contains("currentId != id"))
        assertTrue(body.contains("terminalFirstDrawClaimed = true"))
    }

    @Test fun playerAndDrivingDrawHaveDistinctOriginAndVisibleRouteCheck() {
        val nav = read("app/navigation/AppNavHost.kt")
        assertTrue(nav.contains("VoicePttAdmissionPolicy.owns(drawn, VoiceOrigin.Player)"))
        assertTrue(nav.contains("VoicePttAdmissionPolicy.owns(drawn, VoiceOrigin.Driving)"))
        assertTrue(nav.contains("onTerminalVoiceLabelDrawn(drawn)"))
        assertTrue(nav.contains("drawn == voiceSessionCoordinator?.state?.value"))
    }

    @Test fun manualAckStartsOnAcceptedUiTapAndEndsOnlyAfterGatewayAck() {
        val player = read("feature/player/PlayerViewModel.kt")
        assertTrue(player.contains("val acceptedAtNanos = System.nanoTime()"))
        val part = player.substringAfter("val result = gateway.execute(command)")
            .substringBefore("// An out-of-order failure/timeout")
        assertTrue(part.contains("result is AppResult.Success && epoch == requestEpoch"))
        assertTrue(part.contains("onVerifiedManualAckLatency?.invoke(elapsed)"))
        assertTrue(part.contains("onVerifiedManualAckLatency?.invoke(null)"))
    }

    @Test fun manualAckIsOriginCheckedAndDoesNotCountAStalePlayerRoute() {
        val nav = read("app/navigation/AppNavHost.kt")
        assertTrue(nav.contains("playerModel.onVerifiedManualAckLatency ="))
        assertTrue(nav.contains("visibleEligible"))
        assertTrue(nav.contains("onManualCommandAckLatency(elapsedMs)"))
        assertTrue(nav.contains("onDispose {"))
    }

    @Test fun appUsesSameAndroidClockAtProductionEndpointAndDraw() {
        val app = read("app/di/VoiceBindings.kt")
        val moonshine = read("voice/android/MoonshineSileroSpeechToTextEngine.kt")
        assertTrue(app.contains("android.os.SystemClock.elapsedRealtimeNanos()"))
        assertTrue(moonshine.contains("android.os.SystemClock.elapsedRealtimeNanos()"))
    }

    @Test fun sourceEventsStillExactlyEightAndNoP10ExtraOutboundFields() {
        val schema = read("quality/privacy/OutboundTelemetrySchemaV1.kt")
        val measurement = read("quality/measurement/QualityMetricProjection.kt")
        assertTrue(schema.contains("session_completed"))
        assertTrue(schema.contains("intent_failed"))
        assertFalse(schema.contains("speech_end_timestamp"))
        assertFalse(schema.contains("participant_uuid"))
        assertFalse(measurement.contains("event = TelemetryEvent.P95Response"))
    }

    @Test fun appDoesNotClaimStrictP95FromUncalibratedEndpointProxy() {
        val bindings = read("app/di/ApplicationBindings.kt")
        assertTrue(bindings.contains("voiceEndpointProxyHistogram"))
        assertTrue(bindings.contains("recordVoiceTerminalLatencyProxy"))
        assertTrue(bindings.contains("officialVoiceP95"))
        assertTrue(bindings.contains("localMeasurementSnapshotIfApproved"))
    }

    @Test fun timeHistogramsAreClearedWithOtherDiagnosticsAndConsentRevoke() {
        val app = read("app/di/ApplicationBindings.kt")
        val section = app.substringAfter("fun clearLocalDiagnostics(): Boolean")
            .substringBefore("fun localDiagnosticsSummaryIfApproved()")
        assertTrue(section.contains("voiceEndpointProxyHistogram.clear()"))
        assertTrue(section.contains("localCommandAckHistogram.clear()"))
        assertTrue(app.contains("diagnosticsCollectionAuthorization.requestImmediateRevocation()"))
        assertTrue(app.contains("if (!diagnosticsCollectionAuthorization.canCollect())"))
    }

    @Test fun noNewAnalyticsSdkOrRemoteTransportOrPersistedLatencyAdded() {
        val app = read("app/di/ApplicationBindings.kt")
        val projection = read("quality/measurement/QualityMetricProjection.kt")
        assertFalse(projection.contains("RoomDatabase"))
        assertFalse(projection.contains("SharedPreferences"))
        assertFalse(projection.contains("FileOutputStream"))
        assertFalse(projection.contains("Firebase"))
        assertFalse(app.contains("uploadLatencyHistogram("))
        assertTrue(projection.contains("MeasurementVerdict.NOT_VERIFIED"))
    }
}
