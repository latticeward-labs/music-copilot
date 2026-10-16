package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.permission.*
import com.latticeward.musiccopilot.core.result.AppResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class Issue5Phase2GatewayAuditTest {
    private class FakeGateway(var observation: NativePermissionObservation) : AndroidPermissionGateway {
        var requests = 0
        var settings = 0
        override suspend fun observe(capability: PermissionCapability) = AppResult.Success(observation)
        override suspend fun request(capability: PermissionCapability):
            AppResult<NativePermissionObservation> {
            requests++
            return AppResult.Success(observation)
        }
        override suspend fun openSettings(): AppResult<Unit> {
            settings++
            return AppResult.Success(Unit)
        }
    }

    @Test fun fakeGatewayDoesNotRequestOnObservationOrColdStart() = runBlocking {
        val fake = FakeGateway(NativePermissionObservation(false, false, false))
        val observed = fake.observe(PermissionCapability.MediaAudio)
        assertTrue(observed is AppResult.Success<*>)
        assertEquals(0, fake.requests)
        assertEquals(0, fake.settings)
    }

    @Test fun blockedScanActionConsumedOnceOnlyAfterMatchingFreshGrant() {
        val begin = PermissionMachine.begin(PermissionCapability.MediaAudio,
            PermissionPurpose.FirstConsentedMediaScan, 33)
        assertEquals(BlockedAction.StartScan, begin.pendingBlockedAction)
        val req = PermissionMachine.reduce(begin, PermissionIntent.Continue)
        val wrong = PermissionMachine.reduce(req.state, PermissionIntent.NativeResult(
            0, NativePermissionObservation(true, true, false)))
        assertEquals(BlockedAction.None, wrong.authorizedAction)
        val success = PermissionMachine.reduce(req.state, PermissionIntent.NativeResult(
            1, NativePermissionObservation(true, true, false)))
        assertEquals(BlockedAction.StartScan, success.authorizedAction)
        assertEquals(BlockedAction.None, success.state.pendingBlockedAction)
        val duplicate = PermissionMachine.reduce(success.state, PermissionIntent.NativeResult(
            1, NativePermissionObservation(true, true, false)))
        assertEquals(BlockedAction.None, duplicate.authorizedAction)
        assertTrue(duplicate.effects.isEmpty())
    }

    @Test fun cancelOrRevocationAlwaysDiscardsPendingScanOrVoice() {
        for (action in listOf(BlockedAction.StartScan, BlockedAction.StartVoicePractice,
            BlockedAction.StartVoiceCapture)) {
            val capability = if (action == BlockedAction.StartScan) PermissionCapability.MediaAudio
                else PermissionCapability.Microphone
            val purpose = when (action) {
                BlockedAction.StartScan -> PermissionPurpose.FirstConsentedMediaScan
                BlockedAction.StartVoicePractice -> PermissionPurpose.ExplicitVoicePractice
                else -> PermissionPurpose.ExplicitMicrophoneTap
            }
            val state = PermissionMachine.begin(capability, purpose, 35)
            assertEquals(BlockedAction.None,
                PermissionMachine.reduce(state, PermissionIntent.NotNow).state.pendingBlockedAction)
            assertEquals(BlockedAction.None,
                PermissionMachine.reduce(state, PermissionIntent.Revoked).state.pendingBlockedAction)
        }
    }

    @Test fun settingsRecheckDenialNeverExecutesBlockedAction() {
        val st = PermissionMachine.begin(PermissionCapability.MediaAudio,
            PermissionPurpose.FirstConsentedMediaScan, 35)
        val req = PermissionMachine.reduce(st, PermissionIntent.Continue)
        val denied = PermissionMachine.reduce(req.state, PermissionIntent.NativeResult(
            1, NativePermissionObservation(false, true, false))).state
        val settings = PermissionMachine.reduce(denied, PermissionIntent.OpenSettings)
        val recheck = PermissionMachine.reduce(settings.state, PermissionIntent.ReturnedFromSettings)
        val result = PermissionMachine.reduce(recheck.state, PermissionIntent.Rechecked(
            2, NativePermissionObservation(false, true, true)))
        assertEquals(BlockedAction.None, result.authorizedAction)
        assertEquals(PermissionStage.Recovery, result.state.stage)
    }

    @Test fun incompatibleCapabilitiesCannotAcquireAnotherBlockedAction() {
        try {
            PermissionMachine.begin(PermissionCapability.MediaAudio,
                PermissionPurpose.FirstConsentedMediaScan, 35,
                BlockedAction.StartVoiceCapture)
            fail("Cross-capability action accepted")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("belongs") == true)
        }
    }
}
