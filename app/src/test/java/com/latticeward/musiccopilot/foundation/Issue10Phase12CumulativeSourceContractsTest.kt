package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.VoiceCommandReceiptPolicy
import com.latticeward.musiccopilot.voice.intent.RestrictedIntent
import com.latticeward.musiccopilot.voice.session.VoicePlaybackDispatcher
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Issue #10 P12: SOURCE-AUTHORED ONLY. Nothing below has been executed.
 * Neither a JVM fixture nor static string review can prove Media3/OEM phone
 * behavior, an Android compilation, a merged manifest or TalkBack usability.
 */
class Issue10Phase12CumulativeSourceContractsTest {
    private val sid = VoiceSessionId(7)
    private val sourceRoot = File("src/main/java/com/latticeward/musiccopilot")
    private fun source(relative: String) = File(sourceRoot, relative).readText()

    @Test fun voiceIdContainsUnpersistedProcessNonceAndAttempt() {
        val command = VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1)!!
        assertTrue(command.commandId.value.matches(
            Regex("^voice-[0-9a-f]{32}-7-1$")
        ))
        assertTrue(VoiceCommandReceiptPolicy.isVoiceCommand(command))
        assertFalse(command.commandId.value.contains("mp3"))
        assertFalse(command.commandId.value.contains("/storage/"))
    }

    @Test fun oneLogicalAttemptAlwaysUsesSameVoiceCommandReceipt() {
        val a = VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1)!!
        val b = VoicePlaybackDispatcher.commandFor(RestrictedIntent.NEXT, sid, 1)!!
        assertEquals(a.commandId, b.commandId)
        assertTrue(VoiceCommandReceiptPolicy.confirms(a, b.commandId.value))
    }

    @Test fun newSessionAndRetryCannotAccidentallyReuseSameReceipt() {
        val one = VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, sid, 1)!!
        val retry = VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, sid, 2)!!
        val other = VoicePlaybackDispatcher.commandFor(
            RestrictedIntent.PLAY, VoiceSessionId(8), 1
        )!!
        assertNotEquals(one.commandId, retry.commandId)
        assertNotEquals(one.commandId, other.commandId)
        assertNotEquals(retry.commandId, other.commandId)
    }

    @Test fun successReceiptMustMatchTheNewOpaqueIdExactly() {
        val cmd = VoicePlaybackDispatcher.commandFor(RestrictedIntent.PAUSE, sid, 1)!!
        assertTrue(VoiceCommandReceiptPolicy.confirms(cmd, cmd.commandId.value))
        assertFalse(VoiceCommandReceiptPolicy.confirms(cmd, null))
        assertFalse(VoiceCommandReceiptPolicy.confirms(cmd, "voice-7-1"))
        assertFalse(VoiceCommandReceiptPolicy.confirms(cmd, cmd.commandId.value + "x"))
    }

    @Test fun legacyReceiptRecognitionIsCompatibilityOnly() {
        val legacy = PlaybackCommand.Next(CommandId("voice-7-1"))
        assertTrue(VoiceCommandReceiptPolicy.isVoiceCommand(legacy))
        assertTrue(VoiceCommandReceiptPolicy.confirms(legacy, "voice-7-1"))
        val manual = PlaybackCommand.Next(CommandId("manual-7"))
        assertFalse(VoiceCommandReceiptPolicy.isVoiceCommand(manual))
        assertTrue(VoiceCommandReceiptPolicy.confirms(manual, null))
    }

    @Test fun artistAndMalformedAttemptsRemainUnexecutable() {
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY_ARTIST, sid, 1))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, sid, 0))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, sid, 3))
        assertNull(VoicePlaybackDispatcher.commandFor(RestrictedIntent.PLAY, VoiceSessionId(0), 1))
    }

    @Test fun processNonceIsDeclaredOnceAtDispatcherCompanionScope() {
        val code = source("voice/session/VoicePlaybackDispatcher.kt")
        assertTrue(code.contains("private val processNonce: String = UUID.randomUUID()"))
        assertTrue(code.contains("voice-$" + "processNonce-"))
        assertFalse(code.contains("GlobalScope"))
        assertFalse(code.contains("SharedPreferences"))
    }

    @Test fun directPhraseCorpusRetainsOnlyFiveExecutableIntents() {
        val mapper = source("voice/intent/DirectControlPhrasePolicy.kt")
        val dispatcher = source("voice/session/VoicePlaybackDispatcher.kt")
        assertTrue(mapper.contains("GRAMMAR_VERSION = \"issue10-five-controls-v1\""))
        assertTrue(mapper.contains("\"go back 10 seconds\""))
        assertTrue(dispatcher.contains("RestrictedIntent.PLAY_ARTIST -> null"))
        assertFalse(mapper.contains("SpeechRecognizer.createSpeechRecognizer("))
    }

    @Test fun stateMachineUsesBoundedListeningOneRetryAndForegroundTapGate() {
        val c = source("voice/session/DefaultVoiceSessionCoordinator.kt")
        val policy = source("core/application/voice/VoiceLifecycleSafetyPolicy.kt")
        assertTrue(c.contains("withTimeoutOrNull(VoicePolicy.LISTEN_TIMEOUT_MS)"))
        assertTrue(c.contains("canBeginSession()"))
        assertTrue(c.contains("VoiceTransitionPolicy.mayRetry("))
        assertTrue(policy.contains("state == VoiceSessionState.Idle"))
        assertFalse(c.contains("GlobalScope"))
    }

    @Test fun onlyServiceOwnsExoPlayerAndTemporaryDuck() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val dispatcher = source("voice/session/VoicePlaybackDispatcher.kt")
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(service.contains("ExoPlayer.Builder(this)"))
        assertTrue(service.contains("ServiceVoiceDuckLease()"))
        assertFalse(dispatcher.contains("ExoPlayer.Builder"))
        assertFalse(panel.contains("MediaController.Builder"))
    }

    @Test fun bothOriginsUseCommonAccessibleOverlay() {
        assertTrue(source("feature/player/FullPlayerScreen.kt").contains("VoiceSessionPanel("))
        assertTrue(source("feature/driving/DrivingModeScreen.kt").contains("VoiceSessionPanel("))
        val panel = source("feature/voice/VoiceSessionPanel.kt")
        assertTrue(panel.contains("liveRegion = LiveRegionMode.Polite"))
        assertTrue(panel.contains("heading()"))
        assertTrue(panel.contains("val headingText = when"))
        assertTrue(panel.contains("contentDescription = exitA11y"))
    }

    @Test fun voiceRecognitionAndTtsRemainOfflineOnlyAdapters() {
        val stt = source("voice/android/AndroidOnDeviceSpeechToTextEngine.kt")
        val tts = source("voice/android/AndroidOfflineTextToSpeechEngine.kt")
        val bindings = source("app/di/VoiceBindings.kt")
        assertTrue(stt.contains("createOnDeviceSpeechRecognizer(app)"))
        assertTrue(tts.contains("!it.isNetworkConnectionRequired"))
        assertTrue(bindings.contains("AndroidOnDeviceSpeechToTextEngine(context, dispatchers)"))
        assertFalse(bindings.contains("cloudSpeechRecognizer"))
    }

    @Test fun noNewNetworkOrAutomaticMicPermissionIsIntroduced() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("android.permission.RECORD_AUDIO"))
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        val app = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("uiVoiceStarted = false"))
        assertTrue(app.contains("canBeginSession = { uiVoiceStarted }"))
    }

    @Test fun voiceTranscriptCannotBecomeADataClassLogString() {
        val source = source("voice/api/SpeechToTextEngine.kt")
        assertTrue(source.contains("SpeechRecognitionResult(redacted)"))
        val catalog = source("voice/feedback/VoiceFeedbackCatalog.kt")
        assertFalse(catalog.contains("output.transcript"))
        assertFalse(catalog.contains("getCurrentMediaPath"))
    }

    @Test fun allP13DeviceAcceptanceMustRemainUnmeasuredAtSourcePhase() {
        val json = File("../docs/implementation/ISSUE_10_QA_RESULTS.json")
        // Fixture path is intentionally supplied by P13 only; do not interpret
        // an absent local checked-out docs path as a runtime PASS.
        assertFalse(source("voice/session/DefaultVoiceSessionCoordinator.kt").contains(
            "AC_VERIFIED_ON_PHONE"
        ))
    }
}
