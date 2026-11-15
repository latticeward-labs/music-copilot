package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.DiagnosticsConsent
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsCollectionApprovals
import com.latticeward.musiccopilot.core.application.privacy.DiagnosticsConsentPolicy
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.*
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.quality.privacy.ConsentGatedTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.OutboundTelemetrySchemaV1
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #12 P13: cross-phase SOURCE-AUTHORED contracts; NOT EXECUTED.
 *
 * These are proposals for P14/P0 independent technical QA, NOT proof of
 * actual crash, Voice/Media3 execution, UI rendering, network behavior or
 * performance on the owner's device. Follow 50/14/3 and privacy gates.
 */
class Issue12Phase13FinalIntegrationContractsTest {
    private val dir = "src/main/java/com/latticeward/musiccopilot/"
    private fun source(p: String): String = File(dir + p).readText()
    private val gate = AllowlistedTelemetryPrivacyGate()

    @Test fun exportHasExactlyTheEightSourceLockedEventFamilies() {
        assertEquals(linkedSetOf("driving_mode_started", "voice_button_pressed",
            "intent_detected", "intent_failed", "command_executed",
            "manual_correction", "player_error", "session_completed"),
            OutboundTelemetrySchemaV1.fieldKeys.keys)
    }

    @Test fun ninthIssue11VoiceDiagnosticCannotEscapeToFutureSink() {
        var delivered = 0
        val sink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                delivered++; return true
            }
        }
        val publisher = BestEffortQualityEventPublisher(gate, sink)
        assertFalse(publisher.tryRecord(TelemetryEvent.VoiceIntentOutcome(
            VoiceDiagnosticIntent.PLAY_ARTIST, null, null,
            VoiceDiagnosticResult.UNSUPPORTED)))
        assertEquals(0, delivered)
    }

    @Test fun unexpectedPropertiesAndPersonalFieldsAreNotEncodable() {
        for (privateField in listOf("session_id", "track_title",
                "artist", "transcript", "device_id", "bluetooth_address",
                "media_uri", "location", "phone", "raw_audio", "file_path")) {
            assertNull(OutboundTelemetrySchemaV1.encode("voice_button_pressed",
                arrayOf("playback_state" to "Playing",
                        privateField to "private")))
        }
    }

    @Test fun hostileValuesCannotInjectAdditionalSerializedProperties() {
        for (malicious in listOf("PLAY|track=private", "PLAY=private",
                "user@example.test", "file:///private", "PLAY\nprivate",
                "a;b", "a b", "🚫")) {
            assertTrue(gate.sanitize(TelemetryEvent.IntentDetected(
                malicious, ConfidenceBucket.High)) is AppResult.Failure)
        }
    }

    @Test fun invalidPlayerAndFailureCodesAreRejectedWithoutEcho() {
        for (code in listOf("arbitrary", "PLAYBACK_FAILED|secret=1",
                "filename.mp3", "message-from-Throwable")) {
            assertTrue(gate.sanitize(TelemetryEvent.PlayerError(
                code, RouteType.Bluetooth)) is AppResult.Failure)
            assertTrue(gate.sanitize(TelemetryEvent.IntentFailed(
                "PAUSE", code, null, null)) is AppResult.Failure)
        }
    }

    @Test fun oversizeAndUnboundedSessionCountAreRejected() {
        assertTrue(gate.sanitize(TelemetryEvent.SessionCompleted(
            "over_15m", 1001)) is AppResult.Failure)
        assertTrue(gate.sanitize(TelemetryEvent.SessionCompleted(
            "over_15m", -1)) is AppResult.Failure)
        assertNull(OutboundTelemetrySchemaV1.encode("intent_detected",
            arrayOf("intent_type" to "X".repeat(65),
                    "confidence_bucket" to "High")))
    }

    @Test fun noDiagnosticsConsentMeansNoProviderDelivery() {
        var outputs = 0
        val delegate = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                outputs++; return true
            }
        }
        var consent = false
        val sink = ConsentGatedTelemetrySink(delegate) { consent }
        val publisher = BestEffortQualityEventPublisher(
            gate, sink, canCollect = { consent })
        assertFalse(publisher.tryRecord(
            TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Playing)))
        assertEquals(0, outputs)
    }

    @Test fun revocationAfterSanitizationBlocksPendingDelivery() {
        var delivered = 0
        var approved = true
        val sink = ConsentGatedTelemetrySink(
            object : TelemetrySink {
                override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                    delivered++; return true
                }
            }
        ) { approved }
        val event = gate.sanitize(TelemetryEvent.VoiceButtonPressed(
            TelemetryPlaybackState.Playing))
        assertTrue(event is AppResult.Success<*>)
        approved = false
        assertFalse(sink.tryEmit((event as AppResult.Success<SanitizedTelemetryEvent>).value))
        assertEquals(0, delivered)
    }

    @Test fun failedSinkDeliveryDoesNotChangeMusicOrThrow() {
        val sink = ConsentGatedTelemetrySink(
            object : TelemetrySink {
                override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                    throw IllegalStateException("provider unavailable")
                }
            }
        ) { true }
        assertFalse(BestEffortQualityEventPublisher(
            gate, sink, canCollect = { true }
        ).tryRecord(TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused)))
    }

    @Test fun approvedRememberedConsentCannotEnableProductionByItself() {
        assertFalse(DiagnosticsConsentPolicy.canCollect(
            DiagnosticsConsent.Granted, true,
            DiagnosticsCollectionApprovals(), false))
        assertFalse(DiagnosticsConsentPolicy.canCollect(
            DiagnosticsConsent.Granted, false,
            DiagnosticsCollectionApprovals(true, true, true, true), false))
        assertFalse(DiagnosticsConsentPolicy.canCollect(
            DiagnosticsConsent.Granted, true,
            DiagnosticsCollectionApprovals(true, true, true, true), true))
    }

    @Test fun productionStillUsesExplicitNoOpAndNoRemoteProvider() {
        val src = source("app/di/QualityBindings.kt")
        assertTrue(src.contains("LocalDiagnosticsProviderMode.NoOp"))
        assertTrue(src.contains("NoOpTelemetrySink()"))
        assertFalse(src.contains("FirebaseAnalytics"))
        assertFalse(src.contains("Sentry.init"))
        assertFalse(NoOpTelemetrySink().tryEmit(
            (gate.sanitize(TelemetryEvent.VoiceButtonPressed(
                TelemetryPlaybackState.Idle)) as AppResult.Success<SanitizedTelemetryEvent>).value))
    }

    @Test fun queueClearEpochRejectsStaleQueuedEvents() {
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("diagnosticsEpoch.incrementAndGet()"))
        assertTrue(app.contains("pending.epoch == diagnosticsEpoch.get()"))
        assertTrue(app.contains("diagnosticEvents.tryReceive().isSuccess"))
        assertTrue(app.contains("synchronized(diagnosticsDeliveryLock)"))
    }

    @Test fun clearAlsoPurgesCrashAndBothLatencyHistograms() {
        val app = source("app/di/ApplicationBindings.kt")
        val method = app.substringAfter("fun clearLocalDiagnostics(): Boolean")
            .substringBefore("fun localDiagnosticsSummaryIfApproved()")
        for (required in listOf("localDiagnosticsStore.clear()",
                "voiceEndpointProxyHistogram.clear()",
                "localCommandAckHistogram.clear()",
                "crashEvidenceReporter.clear()", "correctionOwner.clear()",
                "drivingSessionOwner.forgetForPrivacy()")) {
            assertTrue(required, method.contains(required))
        }
    }

    @Test fun clearingAndRevokingCannotDeleteLiveMusicOrQueue() {
        val app = source("app/di/ApplicationBindings.kt")
        val region = app.substringAfter("fun clearSelectedAppData(")
            .substringBefore("val telemetryPrivacyGate:")
        for (forbidden in listOf("restorableQueueStore.clear()",
                "libraryDao.clearDerived()", "MediaStore.delete",
                "playbackGateway.execute(", "SetBluetoothHint(")) {
            assertFalse(forbidden, region.contains(forbidden))
        }
    }

    @Test fun noHistoryDaoMeansNoFalseHistoryClearSuccess() {
        val db = source("data/room/MusicCopilotDatabase.kt")
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(db.contains("entities = [LibraryRow::class]"))
        assertTrue(app.contains("ShortHistoryAvailability.NOT_STORED_IN_CURRENT_BUILD"))
        assertTrue(app.contains("LocalClearCategoryResult.UNAVAILABLE"))
    }

    @Test fun synchronousRevocationPrecedesAnyAsyncDataStoreWrite() {
        val app = source("app/di/ApplicationBindings.kt")
        val section = app.substringAfter("fun revokeDiagnosticsConsentWithResult(")
            .substringBefore("fun revokeCloudSpeechConsent()")
        assertTrue(section.indexOf("requestImmediateRevocation()") <
            section.indexOf("repositoryScope.launch"))
        assertTrue(section.indexOf("clearLocalDiagnostics()") <
            section.indexOf("repositoryScope.launch"))
        assertTrue(section.contains("is AppResult.Success"))
    }

    @Test fun separateCloudRevocationCannotToggleDiagnostics() {
        val app = source("app/di/ApplicationBindings.kt")
        val cloud = app.substringAfter("fun revokeCloudSpeechConsentWithResult(")
            .substringBefore("val telemetryPrivacyGate:")
        assertTrue(cloud.contains("SetCloudSpeechConsent"))
        assertFalse(cloud.contains("SetDiagnosticsConsent"))
    }

    @Test fun artistAmbiguityIsNotBypassedByDiagnostics() {
        val app = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        assertTrue(app.contains("pendingArtist"))
        assertTrue(app.contains("claimDispatch"))
        assertTrue(app.contains("tryRecordApprovedVoiceEvent"))
        // This is a structural check, NOT a claim that Issue #82 is fixed.
    }

    @Test fun drivingLifecycleEmitsCompletionFromExplicitExitOnly() {
        val src = source("core/application/driving/DrivingSessionTelemetryOwner.kt")
        assertTrue(src.contains("completeFromExplicitExit()"))
        assertTrue(src.contains("abandonWithoutCompletion()"))
        assertTrue(src.contains("lastOutcome = ExitEvidence.ABANDONED_UNKNOWN"))
    }

    @Test fun playerErrorHasServiceOwnedEpisodeGate() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val gateSrc = source("playback/service/ServicePlayerErrorEpisodeGate.kt")
        assertTrue(service.contains("ServicePlayerErrorEpisodeGate"))
        assertTrue(gateSrc.contains("class ServicePlayerErrorEpisodeGate"))
    }

    @Test fun crashEvidenceCannotCarryThrowableOrStacktrace() {
        val crash = source("quality/api/SafeCrashEvidence.kt")
        assertTrue(crash.contains("enum class SafeCrashCategory"))
        assertFalse(crash.contains("val throwable"))
        assertFalse(crash.contains("stackTrace:"))
        assertFalse(crash.contains("filePath:"))
    }

    @Test fun endpointProxyCannotBeAssertedAsRealSpeechEndP95() {
        val p10 = source("quality/measurement/QualityMetricProjection.kt")
        assertTrue(p10.contains("ENDPOINT_SIGNAL_TO_TERMINAL_DRAW_PROXY"))
        assertTrue(p10.contains("NOT_VERIFIED"))
    }

    @Test fun appMeasurementNeverStoresRawLatencySamples() {
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("voiceEndpointProxyHistogram"))
        assertTrue(app.contains("localCommandAckHistogram"))
        assertTrue(app.contains("clearLocalDiagnostics()"))
        assertFalse(app.contains("FileOutputStream("))
    }

    @Test fun privacyUiClearStartsUncheckedAndNeedsConfirm() {
        val ui = source("feature/settings/privacy/PrivacyDiagnosticsSettingsSection.kt")
        assertTrue(ui.contains("mutableStateOf(false)"))
        assertTrue(ui.contains("showClearConfirmation = true"))
        assertTrue(ui.contains("showRevokeConfirmation = true"))
        assertTrue(ui.contains("AlertDialog("))
    }

    @Test fun voiceMicJustInTimeCopyIsNotAGrantForDiagnostics() {
        val ui = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(ui.contains("R.string.i12_privacy_microphone_jit"))
        val policy = source("core/application/privacy/DiagnosticsConsentPolicy.kt")
        assertTrue(policy.contains("consent == DiagnosticsConsent.Granted"))
    }

    @Test fun qualityCodeCannotDirectlyCommandPlayback() {
        val quality = source("quality/adapters/BestEffortQualityEventPublisher.kt")
        assertFalse(quality.contains("PlaybackCommand("))
        assertFalse(quality.contains("MediaSession("))
        assertTrue(quality.contains("privacyGate.sanitize(event)"))
    }

    @Test fun qualityOverflowDropMustRemainMeasurableUnknown() {
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("diagnosticQueueDrops.getAndUpdate"))
        assertTrue(app.contains("sourceQueueDropsSinceClear"))
        val store = source("quality/adapters/BoundedMemoryDiagnosticsStore.kt")
        assertTrue(store.contains("evictedOldestSinceClear"))
    }

    @Test fun eventNamingNeverAddsRawSessionOrUserId() {
        assertFalse(OutboundTelemetrySchemaV1.fieldKeys.values.flatten().any {
            it in setOf("session_id", "user_id", "device_id", "artist", "transcript")
        })
    }

    @Test fun privateLogsCannotBeIntroducedThroughExtraEventFamily() {
        assertNull(OutboundTelemetrySchemaV1.encode("crash_raw", emptyArray()))
        assertNull(OutboundTelemetrySchemaV1.encode("voice_intent_outcome", emptyArray()))
        assertNull(OutboundTelemetrySchemaV1.encode("location_observed", emptyArray()))
    }
}
