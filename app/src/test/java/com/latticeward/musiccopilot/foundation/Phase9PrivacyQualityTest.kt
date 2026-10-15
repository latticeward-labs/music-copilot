package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.di.ApplicationBindings
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.time.Clock
import com.latticeward.musiccopilot.quality.adapters.BestEffortQualityEventPublisher
import com.latticeward.musiccopilot.quality.api.*
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class Phase9PrivacyQualityTest {
    private val gate = AllowlistedTelemetryPrivacyGate()
    private fun accept(event: TelemetryEvent): String {
        val sanitized = gate.sanitize(event)
        assertTrue("Allowed typed event must be sanitized", sanitized is AppResult.Success<*>)
        return (sanitized as AppResult.Success<SanitizedTelemetryEvent>).value.encoded
    }
    private fun reject(event: TelemetryEvent) {
        val result = gate.sanitize(event)
        assertTrue("Unapproved content must be rejected", result is AppResult.Failure)
        assertEquals("QUALITY_DROPPED", (result as AppResult.Failure).error.code)
    }

    @Test fun exactlyEightApprovedEventFamiliesHaveStrictPredictableEncodings() {
        val encodings = listOf(
            accept(TelemetryEvent.DrivingModeStarted("home", false)),
            accept(TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Playing)),
            accept(TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)),
            accept(TelemetryEvent.IntentFailed("PAUSE", "VOICE_TIMEOUT",
                ConfidenceBucket.Low, LatencyBucket.Slow)),
            accept(TelemetryEvent.CommandExecuted("NEXT", LatencyBucket.Expected)),
            accept(TelemetryEvent.ManualCorrection("PAUSE", "manual_play")),
            accept(TelemetryEvent.PlayerError("PLAYBACK_FAILED", RouteType.Bluetooth)),
            accept(TelemetryEvent.SessionCompleted("1_to_5m", 7))
        )
        assertEquals(8, TelemetryEvent::class.java.declaredClasses.size)
        assertEquals(8, encodings.size)
        assertEquals(8, encodings.map { it.split("|")[1] }.toSet().size)
        encodings.forEach { assertTrue(it.startsWith("v1|")) }
        assertEquals("v1|command_executed|intent_type=NEXT|latency_bucket=Expected",
            encodings[4])
    }

    @Test fun deniedStringsNeverEchoRawAudioTranscriptsOrMediaTitles() {
        val sensitive = listOf(
            "synthetic secret key", "my personal track", "album artist",
            "path=/storage/emulated/0/Music/private.mp3",
            "content://media/external/audio/1",
            "audio:1337", "token=not_a_real_secret",
            "{\"metadata\":\"voice transcript\"}", "query=private search"
        )
        sensitive.forEach { payload ->
            reject(TelemetryEvent.IntentDetected(payload, ConfidenceBucket.High))
            reject(TelemetryEvent.IntentFailed("PLAY", payload,
                ConfidenceBucket.Low, LatencyBucket.Fast))
            reject(TelemetryEvent.DrivingModeStarted(payload, true))
            reject(TelemetryEvent.ManualCorrection("PLAY", payload))
            reject(TelemetryEvent.SessionCompleted(payload, 1))
            reject(TelemetryEvent.PlayerError(payload, RouteType.Unknown))
        }
    }

    @Test fun intentAndFailureCodeRestrictionsAreExactAndCaseSensitive() {
        listOf("", "play", "SHUFFLE", "PLAY_ARTIST;DROP", "PLAY|media_uri=...",
            "PLAY_ARTIST\nraw_audio=...").forEach { reject(TelemetryEvent.IntentDetected(it, ConfidenceBucket.High)) }
        listOf("", "error", "VOICE_TIMEOUT\npath=../secret", "NETWORK_TOKEN_REJECTED",
            "http://example.invalid", "STACKTRACE").forEach { code ->
            reject(TelemetryEvent.IntentFailed("PLAY", code, null, null))
        }
        // An unknown intent cannot be smuggled into a nullable field.
        reject(TelemetryEvent.IntentFailed("UNRECOGNIZED", "VOICE_NO_MATCH", null, null))
        assertTrue(accept(TelemetryEvent.IntentFailed(null, "VOICE_NO_MATCH", null, null))
            .contains("|intent_type=none|"))
    }

    @Test fun drivingCorrectionAndDurationFieldsRejectUnknownValues() {
        listOf("car VIN", "bluetooth device mac", "DrivingExit", "home?title=private", "HOME")
            .forEach { reject(TelemetryEvent.DrivingModeStarted(it, false)) }
        listOf("previous secret track", "unknown", "manual_seek=private")
            .forEach { reject(TelemetryEvent.ManualCorrection("PLAY", it)) }
        listOf("precise duration 63.482 seconds", "2m", "last-played=timestamp")
            .forEach { reject(TelemetryEvent.SessionCompleted(it, 5)) }
        reject(TelemetryEvent.ManualCorrection("UNSUPPORTED", "manual_play"))
    }

    @Test fun sessionCountIsBoundedAndNotAnIdentifierCarrier() {
        reject(TelemetryEvent.SessionCompleted("under_1m", -1))
        reject(TelemetryEvent.SessionCompleted("under_1m", 1001))
        assertEquals(0, accept(TelemetryEvent.SessionCompleted("under_1m", 0))
            .takeLast(1).toInt())
        assertTrue(accept(TelemetryEvent.SessionCompleted("over_15m", 1000))
            .endsWith("commands_count=1000"))
    }

    @Test fun sanitizedEventOnlyContainsAllowlistedFieldsNeverFreeFormProperties() {
        val event = TelemetryEvent.PlayerError("MEDIA_ITEM_MISSING", RouteType.Wired)
        val encoded = accept(event)
        assertEquals("v1|player_error|error_code=MEDIA_ITEM_MISSING|route_type=Wired", encoded)
        val ports = File("src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryPorts.kt")
            .readText()
        assertFalse(ports.contains("Map<String"))
        assertFalse(ports.contains("Any?"))
        assertTrue(ports.contains("internal constructor"))
        val model = File("src/main/java/com/latticeward/musiccopilot/quality/api/TelemetryEvent.kt")
            .readText()
        assertFalse(model.contains("Map<"))
        assertFalse(model.contains("metadata:"))
        assertFalse(model.contains("transcript:"))
        assertFalse(model.contains("mediaRef:"))
    }

    @Test fun noOpSinkDoesNotEmitStoreLogOrRequireConsent() {
        val sink = NoOpTelemetrySink()
        repeat(10) {
            assertFalse(sink.tryEmit(
                (gate.sanitize(TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Idle))
                    as AppResult.Success<SanitizedTelemetryEvent>).value
            ))
        }
        val code = File("src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt")
            .readText()
        assertFalse(code.contains("Log."))
        assertFalse(code.contains("System.out"))
        assertFalse(code.contains("Http"))
    }

    @Test fun diagnosticsFailureCannotPropagateToPlaybackOrStartupState() {
        val graph = ApplicationBindings(TestDispatcherProvider(), Clock { 100L })
        try {
            val before = graph.playbackGateway.snapshot.value
            val voiceBefore = graph.voiceSessionCoordinator.state.value
            assertTrue(graph.telemetryPrivacyGate.sanitize(
                TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)
            ) is AppResult.Success<*>)
            assertFalse(graph.qualityEventPublisher.tryRecord(
                TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)))
            assertSame(before, graph.playbackGateway.snapshot.value)
            assertEquals(voiceBefore, graph.voiceSessionCoordinator.state.value)
            assertFalse(graph.qualityEventPublisher.tryRecord(
                TelemetryEvent.IntentDetected("private transcript", ConfidenceBucket.High)))
            assertSame(before, graph.playbackGateway.snapshot.value)
        } finally { graph.close() }
    }

    @Test fun arbitrarySinkExceptionIsContainedWithoutProductMutation() {
        val sink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                throw IllegalStateException("synthetic trace containing personal media")
            }
        }
        val publisher = BestEffortQualityEventPublisher(gate, sink)
        assertFalse(publisher.tryRecord(
            TelemetryEvent.CommandExecuted("PLAY", LatencyBucket.Expected)
        ))
        assertFalse(publisher.tryRecord(
            TelemetryEvent.CommandExecuted("TRACK_TITLE_PRIVATE", LatencyBucket.Expected)
        ))
    }

    @Test fun arbitraryGateExceptionIsContainedAndSinkNeverSeesUntrustedEvent() {
        val badGate = object : TelemetryPrivacyGate {
            override fun sanitize(event: TelemetryEvent): AppResult<SanitizedTelemetryEvent> {
                throw IllegalStateException("synthetic private path should not leak")
            }
        }
        var sends = 0
        val sink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                sends++
                return true
            }
        }
        assertFalse(BestEffortQualityEventPublisher(badGate, sink)
            .tryRecord(TelemetryEvent.PlayerError("PLAYBACK_FAILED", RouteType.Unknown)))
        assertEquals(0, sends)
    }

    @Test fun onlyWhitelistedValuesReachAnOptInInMemoryFakeSink() {
        var emitted: SanitizedTelemetryEvent? = null
        val fakeSink = object : TelemetrySink {
            override fun tryEmit(event: SanitizedTelemetryEvent): Boolean {
                emitted = event
                return true
            }
        }
        val publisher = BestEffortQualityEventPublisher(gate, fakeSink)
        assertFalse(publisher.tryRecord(
            TelemetryEvent.IntentDetected("user favorite song private", ConfidenceBucket.High)))
        assertNull(emitted)
        assertTrue(publisher.tryRecord(
            TelemetryEvent.IntentDetected("PLAY", ConfidenceBucket.High)))
        assertEquals("v1|intent_detected|intent_type=PLAY|confidence_bucket=High", emitted?.encoded)
        // This fake never replaces the production NoOp adapter.
    }

    @Test fun buildAndManifestHardenedForBothReleaseAndDebugWithoutNetworkOrCrashSdk() {
        val gradle = File("build.gradle.kts").readText()
        val manifest = File("src/main/AndroidManifest.xml").readText()
        val root = File("src/main/java/com/latticeward/musiccopilot")
        val qualityBinding = File(root, "app/di/QualityBindings.kt").readText()
        for (field in listOf("QUALITY_EXTERNAL_DIAGNOSTICS_ENABLED", "QUALITY_CRASH_UPLOAD_ENABLED",
            "QUALITY_VERBOSE_LOGGING_ENABLED")) {
            assertTrue(gradle.contains("buildConfigField(\"boolean\", \"$field\", \"false\")"))
        }
        assertTrue(gradle.contains("isDebuggable = false"))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:usesCleartextTraffic=\"false\""))
        assertTrue(manifest.contains("@xml/no_data_extraction"))
        assertTrue(manifest.contains("@xml/no_backup_legacy"))
        assertTrue(qualityBinding.contains("NoOpTelemetrySink()"))
        assertFalse(qualityBinding.contains("Sentry"))
        assertFalse(qualityBinding.contains("Firebase"))
        assertFalse(gradle.contains("firebase-crashlytics"))
        assertFalse(gradle.contains("firebase-analytics"))
    }

    @Test fun backupRulesExcludeCloudAndDeviceTransferOfLocalPersonalData() {
        val legacy = File("src/main/res/xml/no_backup_legacy.xml").readText()
        val modern = File("src/main/res/xml/no_data_extraction.xml").readText()
        assertTrue(legacy.contains("domain=\"root\""))
        assertTrue(modern.contains("<cloud-backup>"))
        assertTrue(modern.contains("<device-transfer>"))
        assertEquals(2, Regex("""<exclude domain="root" path="\."\s*/>""")
            .findAll(modern).count())
    }
}
