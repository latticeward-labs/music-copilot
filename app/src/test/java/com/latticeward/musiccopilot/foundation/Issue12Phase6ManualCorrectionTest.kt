package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.voice.ExplicitManualCorrectionOwner
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.VoiceDiagnosticIntent
import com.latticeward.musiccopilot.quality.api.VoiceManualCorrectionCategory
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #12 P06 — SOURCE-AUTHORED ONLY: DO NOT RUN until final integrated QA.
 * An explicit correction request plus an acknowledged same-origin manual
 * action is necessary. Neither ordinary manual touches nor artist choice
 * responses are automatic correction evidence.
 */
class Issue12Phase6ManualCorrectionTest {
    @Test fun normalManualAckWithoutExplicitArmIsNeverCorrection() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 100L })
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PAUSE))
    }

    @Test fun explicitArmPlusSameOriginAckProducesOneClosedCorrection() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 1_000L })
        assertTrue(owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.PLAY))
        val event = owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PAUSE)
        assertEquals(TelemetryEvent.ManualCorrection("PLAY", "manual_pause"), event)
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PAUSE))
        assertTrue(AllowlistedTelemetryPrivacyGate().sanitize(event!!) is AppResult.Success)
    }

    @Test fun wrongOriginCannotClaimTicket() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 100L })
        owner.arm(VoiceOrigin.Driving, VoiceDiagnosticIntent.PAUSE)
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PLAY))
        assertNotNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Driving,
            VoiceManualCorrectionCategory.MANUAL_PLAY))
    }

    @Test fun expiredTicketNeverReportsAnUnrelatedLateAction() {
        var now = 10L
        val owner = ExplicitManualCorrectionOwner({ now }, windowMs = 1_000L)
        owner.arm(VoiceOrigin.Driving, VoiceDiagnosticIntent.NEXT)
        now = 1_011L
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Driving,
            VoiceManualCorrectionCategory.MANUAL_SKIP))
    }

    @Test fun boundaryTimeIsInclusiveButOnlyOnce() {
        var now = 50L
        val owner = ExplicitManualCorrectionOwner({ now }, windowMs = 100L)
        owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.PREVIOUS)
        now = 150L
        assertNotNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_SKIP))
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_SKIP))
    }

    @Test fun cancellationAndBackgroundClearPreventFalseAttribution() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 5L })
        owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.PAUSE)
        owner.clear()
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PLAY))
    }

    @Test fun newExplicitArmReplacesOldIntentRatherThanDuplicating() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 100L })
        owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.PLAY)
        owner.arm(VoiceOrigin.Driving, VoiceDiagnosticIntent.PLAY_ARTIST)
        val event = owner.observeAcknowledgedManualAction(VoiceOrigin.Driving,
            VoiceManualCorrectionCategory.MANUAL_SELECT)
        assertEquals("PLAY_ARTIST", event?.previousIntent)
        assertEquals("manual_select", event?.correctionType)
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PAUSE))
    }

    @Test fun allCorrectionCodesStayInEightEventAllowlist() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { 10L })
        for (category in VoiceManualCorrectionCategory.entries) {
            owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.SEEK_BACK)
            val event = owner.observeAcknowledgedManualAction(VoiceOrigin.Player, category)
            assertNotNull(event)
            assertTrue(AllowlistedTelemetryPrivacyGate().sanitize(event!!)
                is AppResult.Success)
        }
    }

    @Test fun monotonicClockFailureCannotArmOrProduceTelemetry() {
        val owner = ExplicitManualCorrectionOwner(nowElapsedMs = { -1L })
        assertFalse(owner.arm(VoiceOrigin.Player, VoiceDiagnosticIntent.PLAY))
        assertNull(owner.observeAcknowledgedManualAction(VoiceOrigin.Player,
            VoiceManualCorrectionCategory.MANUAL_PAUSE))
    }

    @Test fun noUnapprovedManualSearchOrSeekCategoryIsFabricatedByPlayer() {
        val src = File("src/main/java/com/latticeward/musiccopilot/feature/player/PlayerViewModel.kt")
            .readText()
        assertTrue(src.contains("if (result is AppResult.Success && epoch == requestEpoch)"))
        assertTrue(src.contains("is PlaybackCommand.SelectQueueIndex ->"))
        assertTrue(src.contains("VoiceManualCorrectionCategory.MANUAL_SELECT"))
        assertTrue(src.contains("else -> null"))
        assertFalse(src.contains("is PlaybackCommand.SeekTo ->\n                            VoiceManualCorrectionCategory.MANUAL_SKIP"))
    }

    @Test fun sourceRequiresExplicitGestureAndARealAcknowledgeBeforeReporting() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val panel = File(root + "feature/voice/VoiceSessionPanel.kt").readText()
        val app = File(root + "app/di/ApplicationBindings.kt").readText()
        val nav = File(root + "app/navigation/AppNavHost.kt").readText()
        assertTrue(panel.contains("onCorrectManually"))
        assertTrue(panel.contains("state is VoiceSessionState.Executed"))
        assertTrue(app.contains("acceptedTerminalIntentForCorrection(origin)"))
        assertTrue(app.contains("observeAcknowledgedManualAction(origin, category)"))
        assertTrue(nav.contains("onVerifiedManualCorrection(VoiceOrigin.Player, category)"))
        assertTrue(nav.contains("onVerifiedManualCorrection(VoiceOrigin.Driving, category)"))
        assertTrue(app.contains("correctionOwner.clear()"))
        assertFalse(panel.contains("TelemetryEvent.ManualCorrection"))
    }
}
