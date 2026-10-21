package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.VoicePracticeGate
import com.latticeward.musiccopilot.core.application.permission.VoicePracticeStage
import com.latticeward.musiccopilot.core.application.permission.VoiceProviderCapability
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P09: only deliberate Voice education/Mic taps permit native permission entry. */
class Issue5Phase9MicrophonePracticeTest {
    private fun fresh() = VoicePracticeGate()

    @Test fun firstRunSkipOrDrivingEntryNeverRequestsMicOrStartsVoice() {
        val gate = fresh()
        assertEquals(VoicePracticeStage.Idle, gate.stage)
        assertFalse(gate.awaitingAndroidResult)
        assertFalse(gate.confirmRationale())
        gate.manualOnly() // Skip / Back / Not now / non-Voice entry
        assertFalse(gate.awaitingAndroidResult)
        assertEquals(VoicePracticeStage.Idle, gate.stage)
    }

    @Test fun optionalPracticeHasSeparateRationaleBeforeOneAndroidRequest() {
        val gate = fresh()
        assertTrue(gate.practiceTapped())
        assertEquals(VoicePracticeStage.Rationale, gate.stage)
        assertFalse(gate.awaitingAndroidResult)
        assertFalse(gate.practiceTapped()) // duplicate tap is inert
        assertTrue(gate.confirmRationale())
        assertTrue(gate.awaitingAndroidResult)
        assertFalse(gate.confirmRationale()) // exactly one native launcher
        assertFalse(gate.practiceTapped()) // Android sheet is open
        assertEquals(VoicePracticeStage.ReadyForVoiceHandoff,
            gate.androidResult(true, VoiceProviderCapability.OnDeviceAvailable))
        assertFalse(gate.awaitingAndroidResult)
        assertEquals(VoicePracticeStage.ReadyForVoiceHandoff,
            gate.androidResult(false, VoiceProviderCapability.ProviderUnavailable))
    }

    @Test fun separateProviderReadinessFollowsOnlyAFreshAndroidGrant() {
        val gate = fresh()
        gate.practiceTapped()
        gate.confirmRationale()
        assertEquals(VoicePracticeStage.ProviderUnavailable,
            gate.androidResult(true, VoiceProviderCapability.ProviderUnavailable))
        assertFalse(gate.awaitingAndroidResult)
        gate.practiceTapped()
        gate.confirmRationale()
        assertEquals(VoicePracticeStage.LanguageUnsupported,
            gate.androidResult(true, VoiceProviderCapability.LanguageUnsupported))
        gate.practiceTapped()
        gate.confirmRationale()
        assertEquals(VoicePracticeStage.CloudConsentDeclined,
            gate.androidResult(true, VoiceProviderCapability.RemoteConsentRequired))
    }

    @Test fun microphoneDenialIsNotProviderOrCloudConsentFailure() {
        val gate = fresh()
        gate.practiceTapped()
        gate.confirmRationale()
        assertEquals(VoicePracticeStage.PermissionDenied,
            gate.androidResult(false, VoiceProviderCapability.OnDeviceAvailable))
        assertEquals(VoicePracticeStage.PermissionDenied, gate.stage)
        gate.manualOnly()
        assertEquals(VoicePracticeStage.Idle, gate.stage)
        assertFalse(gate.awaitingAndroidResult)
    }

    @Test fun cancelBeforeAndroidCallbackDiscardsStalePermissionAndNeverStartsCapture() {
        val gate = fresh()
        gate.practiceTapped()
        gate.confirmRationale()
        gate.manualOnly()
        assertEquals(VoicePracticeStage.Idle,
            gate.androidResult(true, VoiceProviderCapability.OnDeviceAvailable))
        assertFalse(gate.awaitingAndroidResult)
    }

    @Test fun lifecycleRestoreReusesPendingResultWithoutRequestAgain() {
        val gate = fresh()
        gate.restorePending()
        assertTrue(gate.awaitingAndroidResult)
        assertFalse(gate.confirmRationale())
        assertFalse(gate.practiceTapped())
        assertEquals(VoicePracticeStage.PermissionDenied,
            gate.androidResult(false, VoiceProviderCapability.ProviderUnavailable))
        gate.practiceTapped()
        gate.confirmRationale()
        gate.requestLaunchFailed()
        assertEquals(VoicePracticeStage.PermissionDenied, gate.stage)
        assertFalse(gate.awaitingAndroidResult)
    }

    @Test fun revocationInvalidatesReadyButNeverAutoOpensMicRequest() {
        val gate = fresh()
        gate.microphoneTapped()
        gate.confirmRationale()
        assertEquals(VoicePracticeStage.ReadyForVoiceHandoff,
            gate.androidResult(true, VoiceProviderCapability.OnDeviceAvailable))
        gate.recheck(false)
        assertEquals(VoicePracticeStage.PermissionDenied, gate.stage)
        assertFalse(gate.awaitingAndroidResult)
    }

    @Test fun exactlySixEnglishIntentsAndNoOutOfScopePhrases() {
        val mapper = File("src/main/java/com/latticeward/musiccopilot/voice/intent/RestrictedVoiceIntentMapper.kt").readText()
        val strings = File("src/main/res/values/strings.xml").readText()
        val expected = listOf(
            "PLAY", "PAUSE", "NEXT", "PREVIOUS", "SEEK_BACK", "PLAY_ARTIST",
        )
        val declaration = Regex("enum class RestrictedIntent \\{([^}]+)\\}").find(mapper)?.groupValues?.get(1)
            ?: error("missing six-intent voice mapper")
        assertEquals(expected, declaration.split(",").map { it.trim() })
        val labels = listOf("play", "pause", "next", "previous", "seek_back", "play_artist")
        for (label in labels) assertTrue(strings.contains("name=\"p09_command_$label\""))
        assertTrue(strings.contains("musico back 10 seconds"))
        assertTrue(strings.contains("musico play songs by [artist]"))
        assertFalse(strings.contains("p09_command_shuffle"))
        assertFalse(strings.contains("p09_command_download"))
    }

    @Test fun actualAndroidEntryIsJitNeverDuringOnboardingStartupOrReplay() {
        val activity = File("src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt").readText()
        val ui = File("src/main/java/com/latticeward/musiccopilot/feature/onboarding/FirstUseScreen.kt").readText()
        val binding = File("src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        assertTrue(activity.contains("onPracticeRationaleContinue()"))
        assertTrue(activity.contains("microphoneLauncher.launch(android.Manifest.permission.RECORD_AUDIO)"))
        assertTrue(activity.contains("voicePracticeGate.confirmRationale()"))
        assertTrue(activity.contains("voicePracticeGate.androidResult(freshGrant, provider)"))
        assertTrue(ui.contains("onClick = onPracticeVoice"))
        assertTrue(ui.contains("onClick = onPracticeRationaleContinue"))
        assertTrue(ui.contains("onClick = onPracticeManualOnly"))
        assertFalse(binding.contains("voiceSessionCoordinator.start("))
        assertFalse(ui.contains("SpeechRecognizer"))
        assertFalse(ui.contains("RequestPermission"))
        assertFalse(ui.contains("RECORD_AUDIO"))
    }
}
