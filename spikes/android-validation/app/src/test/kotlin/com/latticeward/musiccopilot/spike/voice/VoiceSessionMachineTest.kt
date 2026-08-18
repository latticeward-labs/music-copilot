package com.latticeward.musiccopilot.spike.voice

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSessionMachineTest {
    private val next = IntentCandidate(VoiceIntent.NEXT, score = 0.96)

    @Test
    fun explicitStartDucksAndUsesSixSecondBound() {
        val machine = VoiceSessionMachine()
        val effects = machine.start()
        assertEquals(VoiceState.LISTENING, machine.state)
        assertEquals(VoiceSessionMachine.LISTENING_TIMEOUT_MILLIS, 6_000L)
        assertEquals(
            listOf(
                VoiceEffect.SetDucking(true),
                VoiceEffect.StartListening(6_000L, 1),
            ),
            effects,
        )
    }

    @Test
    fun directIntentExecutesWithoutNavigationOrConfirmation() {
        val machine = VoiceSessionMachine()
        machine.start()
        val effects = machine.onMapping(MappingResult.Recognized(next))
        assertEquals(VoiceState.PROCESSING, machine.state)
        assertTrue(effects.contains(VoiceEffect.Execute(next)))
    }

    @Test
    fun firstTimeoutOffersExactlyOneRetry() {
        val machine = VoiceSessionMachine()
        machine.start()
        machine.onRecognitionFailure(RecognitionFailure.TIMEOUT)
        assertEquals(VoiceState.FAILED, machine.state)
        assertTrue(machine.retryAvailable)

        val effects = machine.retry()
        assertEquals(VoiceState.LISTENING, machine.state)
        assertEquals(2, machine.attempt)
        assertEquals(VoiceEffect.StartListening(6_000L, 2), effects.last())

        machine.onRecognitionFailure(RecognitionFailure.TIMEOUT)
        assertFalse(machine.retryAvailable)
        assertEquals(VoiceState.FAILED, machine.state)
    }

    @Test
    fun notFoundDoesNotApproximateOrLoop() {
        val machine = VoiceSessionMachine()
        machine.start()
        val effects = machine.onMapping(
            MappingResult.Unknown(MappingFailureReason.ARTIST_NOT_FOUND),
        )
        assertEquals(VoiceState.FAILED, machine.state)
        assertFalse(machine.retryAvailable)
        assertTrue(effects.contains(VoiceEffect.ShowManualControls))
    }

    @Test
    fun ambiguityAllowsOneBoundedSelection() {
        val machine = VoiceSessionMachine()
        machine.start()
        val candidate = IntentCandidate(
            VoiceIntent.PLAY_ARTIST,
            Artist("artist-1", "Muse"),
            0.84,
        )
        val effects = machine.onMapping(
            MappingResult.Ambiguous(listOf(candidate), MappingFailureReason.LOW_CONFIDENCE),
        )
        assertEquals(VoiceState.CONFIRMING, machine.state)
        assertTrue((effects.last() as VoiceEffect.Speak).sentence.count { it == '?' } <= 1)

        assertEquals(listOf(VoiceEffect.Execute(candidate)), machine.confirm(candidate))
        assertEquals(VoiceState.PROCESSING, machine.state)
    }

    @Test
    fun unclearConfirmationFailsToManualControls() {
        val machine = VoiceSessionMachine()
        machine.start()
        machine.onMapping(MappingResult.Ambiguous(listOf(next), MappingFailureReason.LOW_CONFIDENCE))
        val effects = machine.confirm(null)
        assertEquals(VoiceState.FAILED, machine.state)
        assertTrue(effects.contains(VoiceEffect.ShowManualControls))
    }

    @Test
    fun ttsFailureNeverRollsBackExecutedCommand() {
        val machine = VoiceSessionMachine()
        machine.start()
        machine.onMapping(MappingResult.Recognized(next))
        machine.onCommandAcknowledged("Next track")
        assertEquals(VoiceState.EXECUTED, machine.state)
        assertTrue(machine.onTtsFailure().isEmpty())
        assertEquals(VoiceState.EXECUTED, machine.state)
    }

    @Test
    fun cancelRestoresManualControls() {
        val machine = VoiceSessionMachine()
        machine.start()
        val effects = machine.cancel()
        assertEquals(RecognitionFailure.CANCELLED, machine.terminalFailure)
        assertTrue(effects.contains(VoiceEffect.SetDucking(false)))
        assertTrue(effects.contains(VoiceEffect.ShowManualControls))
    }
}
