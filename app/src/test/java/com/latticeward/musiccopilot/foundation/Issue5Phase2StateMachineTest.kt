package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapEvidence
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoute
import com.latticeward.musiccopilot.core.application.bootstrap.BootstrapRoutePolicy
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEffect
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseEvent
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMachine
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseMode
import com.latticeward.musiccopilot.core.application.bootstrap.FirstUseState
import com.latticeward.musiccopilot.core.application.permission.NativePermissionObservation
import com.latticeward.musiccopilot.core.application.permission.PermissionCapability
import com.latticeward.musiccopilot.core.application.permission.PermissionEffect
import com.latticeward.musiccopilot.core.application.permission.PermissionIntent
import com.latticeward.musiccopilot.core.application.permission.PermissionMachine
import com.latticeward.musiccopilot.core.application.permission.PermissionPlatformPolicy
import com.latticeward.musiccopilot.core.application.permission.PermissionPurpose
import com.latticeward.musiccopilot.core.application.permission.PermissionStage
import com.latticeward.musiccopilot.core.application.permission.PermissionStatus
import com.latticeward.musiccopilot.data.api.FirstUsePage
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import org.junit.Assert.*
import org.junit.Test

class Issue5Phase2StateMachineTest {
    private fun observed(
        granted: Boolean = false,
        tried: Boolean = true,
        rationale: Boolean = true,
        wasGranted: Boolean = false,
    ) = NativePermissionObservation(granted, tried, rationale, wasGranted)

    @Test fun firstRunOnlyAdvancesAfterExactCheckpointAck() {
        val s = FirstUseMachine.enter(false, FirstUsePage.Value)
        val pending = FirstUseMachine.reduce(s, FirstUseEvent.Continue)
        assertTrue(pending.state is FirstUseState.Saving)
        assertEquals(FirstUseEffect.PersistCheckpoint(FirstUsePage.SafetyAndVoice, 1), pending.effects.single())
        assertEquals(pending.state, FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(22)).state)
        val advanced = FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(1))
        assertEquals(FirstUsePage.SafetyAndVoice, (advanced.state as FirstUseState.Education).page)
    }

    @Test fun completingSecondSlideRequiresPersistenceAckNotOptimisticRouting() {
        val s = FirstUseMachine.enter(false, FirstUsePage.SafetyAndVoice)
        val pending = FirstUseMachine.reduce(s, FirstUseEvent.Continue)
        assertTrue(pending.state is FirstUseState.Saving)
        assertEquals(FirstUseEffect.PersistCompletion(1), pending.effects.single())
        assertTrue(FirstUseMachine.reduce(pending.state, FirstUseEvent.Skip).effects.isEmpty())
        val complete = FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(1))
        assertTrue(complete.state is FirstUseState.Finished)
        assertEquals(listOf(FirstUseEffect.EnterReadiness), complete.effects)
    }

    @Test fun skipIsExplicitAndDoesNotRequestMicrophone() {
        val skip = FirstUseMachine.reduce(FirstUseMachine.enter(false, FirstUsePage.Value), FirstUseEvent.Skip)
        assertEquals(listOf(FirstUseEffect.PersistCompletion(1)), skip.effects)
    }

    @Test fun persistenceFailureKeepsManualRetryAndOldCheckpoint() {
        val saving = FirstUseMachine.reduce(FirstUseMachine.enter(false, FirstUsePage.Value), FirstUseEvent.Continue).state
        val failed = FirstUseMachine.reduce(saving, FirstUseEvent.SaveFailed(1)).state
        assertTrue(failed is FirstUseState.SaveError)
        assertEquals(FirstUsePage.Value, (failed as FirstUseState.SaveError).pageBefore)
        val retry = FirstUseMachine.reduce(failed, FirstUseEvent.Retry)
        assertEquals(FirstUseEffect.PersistCheckpoint(FirstUsePage.SafetyAndVoice, 2), retry.effects.single())
        assertEquals(retry.state, FirstUseMachine.reduce(retry.state, FirstUseEvent.Saved(1)).state)
    }

    @Test fun backFromSlideTwoOnlyChangesPageAfterAcknowledgedCheckpoint() {
        val pending = FirstUseMachine.reduce(
            FirstUseMachine.enter(false, FirstUsePage.SafetyAndVoice), FirstUseEvent.Back
        )
        assertEquals(FirstUseEffect.PersistCheckpoint(FirstUsePage.Value, 1), pending.effects.single())
        assertEquals(FirstUsePage.Value, (
            FirstUseMachine.reduce(pending.state, FirstUseEvent.Saved(1)).state as FirstUseState.Education
        ).page)
    }

    @Test fun educationReplayNeverPersistsOrChangesPermissionsOrPlayback() {
        var state = FirstUseMachine.enter(true, FirstUsePage.SafetyAndVoice, replay = true)
        assertEquals(FirstUseMode.EducationReplay, (state as FirstUseState.Education).mode)
        var next = FirstUseMachine.reduce(state, FirstUseEvent.Continue)
        assertTrue(next.effects.isEmpty())
        state = next.state
        next = FirstUseMachine.reduce(state, FirstUseEvent.Continue)
        assertEquals(listOf(FirstUseEffect.ReturnToOrigin), next.effects)
        assertTrue(next.state is FirstUseState.Finished)
    }

    @Test fun bootstrapDoesNotRacePreferencesOrMigrationOrPermissionTruth() {
        fun sample() = BootstrapEvidence(
            preferencesLoaded = false, migrationsVerified = false, startupFailed = false,
            onboardingComplete = false, mediaPermission = PermissionStatus.Unknown, libraryStatus = null
        )
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(sample()))
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(
            sample().copy(migrationsVerified = true)))
        assertEquals(BootstrapRoute.Onboarding, BootstrapRoutePolicy.project(
            sample().copy(migrationsVerified = true, preferencesLoaded = true)))
        assertEquals(BootstrapRoute.RetryStartup, BootstrapRoutePolicy.project(
            sample().copy(startupFailed = true)))
    }

    @Test fun bootstrapNeverInfersGrantFromReadyIndexOrPlayback() {
        val s = BootstrapEvidence(true, true, false, true, PermissionStatus.Unknown, LibraryIndexStatus.Ready)
        assertEquals(BootstrapRoute.Preparing, BootstrapRoutePolicy.project(s))
        assertEquals(BootstrapRoute.PermissionRecovery, BootstrapRoutePolicy.project(
            s.copy(mediaPermission = PermissionStatus.PermanentlyDenied)))
        assertEquals(BootstrapRoute.HomeLibrary, BootstrapRoutePolicy.project(
            s.copy(mediaPermission = PermissionStatus.DeniedRequestable, userChoseManualFallback = true)))
        assertEquals(BootstrapRoute.HomeLibrary, BootstrapRoutePolicy.project(
            s.copy(mediaPermission = PermissionStatus.Granted)))
    }

    @Test fun platformPermissionNamesFollowAndroidApiLevel() {
        assertEquals("android.permission.READ_EXTERNAL_STORAGE",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, 29))
        assertEquals("android.permission.READ_EXTERNAL_STORAGE",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, 32))
        assertEquals("android.permission.READ_MEDIA_AUDIO",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, 33))
        assertEquals("android.permission.READ_MEDIA_AUDIO",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.MediaAudio, 35))
        assertNull(PermissionPlatformPolicy.manifestPermission(PermissionCapability.Notifications, 32))
        assertEquals("android.permission.POST_NOTIFICATIONS",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.Notifications, 33))
        assertEquals("android.permission.RECORD_AUDIO",
            PermissionPlatformPolicy.manifestPermission(PermissionCapability.Microphone, 29))
    }

    @Test fun platformStateDistinguishesNotAskedRequestablePermanentRevokeAndGrant() {
        val k = PermissionCapability.MediaAudio
        assertEquals(PermissionStatus.NotRequested, PermissionPlatformPolicy.project(
            k, 35, observed(tried = false, rationale = false)))
        assertEquals(PermissionStatus.DeniedRequestable, PermissionPlatformPolicy.project(
            k, 35, observed(rationale = true)))
        assertEquals(PermissionStatus.PermanentlyDenied, PermissionPlatformPolicy.project(
            k, 35, observed(rationale = false)))
        assertEquals(PermissionStatus.Revoked, PermissionPlatformPolicy.project(
            k, 35, observed(wasGranted = true)))
        assertEquals(PermissionStatus.Granted, PermissionPlatformPolicy.project(
            k, 35, observed(granted = true)))
        assertEquals(PermissionStatus.NotApplicable, PermissionPlatformPolicy.project(
            PermissionCapability.Notifications, 29, observed(rationale = false)))
    }

    @Test fun micCanOnlyBeginFromExplicitTapOrPractice() {
        assertTrue(PermissionPlatformPolicy.compatible(
            PermissionCapability.Microphone, PermissionPurpose.ExplicitVoicePractice))
        assertFalse(PermissionPlatformPolicy.compatible(
            PermissionCapability.Microphone, PermissionPurpose.FirstConsentedMediaScan))
        try {
            PermissionMachine.begin(PermissionCapability.Microphone, PermissionPurpose.FirstConsentedMediaScan, 35)
            fail("Unrelated permission request was accepted")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message?.contains("unrelated purpose") == true)
        }
    }

    @Test fun noOsRequestUntilExplanationContinueAndStaleCallbacksRejected() {
        val start = PermissionMachine.begin(
            PermissionCapability.MediaAudio, PermissionPurpose.FirstConsentedMediaScan, 35)
        assertEquals(PermissionStage.Explanation, start.stage)
        val requested = PermissionMachine.reduce(start, PermissionIntent.Continue)
        assertEquals(PermissionStage.WaitingForNativeResult, requested.state.stage)
        assertEquals(PermissionEffect.RequestAndroidPermission(PermissionCapability.MediaAudio, 1),
            requested.effects.single())
        assertEquals(requested.state, PermissionMachine.reduce(
            requested.state, PermissionIntent.NativeResult(0, observed(granted = true))).state)
        val granted = PermissionMachine.reduce(requested.state,
            PermissionIntent.NativeResult(1, observed(granted = true)))
        assertEquals(PermissionStage.Ready, granted.state.stage)
        assertEquals(listOf(PermissionEffect.ContinueBlockedAction), granted.effects)
    }

    @Test fun requestableDenialAndNotNowPreserveManualControls() {
        val request = PermissionMachine.reduce(
            PermissionMachine.begin(PermissionCapability.MediaAudio,
                PermissionPurpose.FirstConsentedMediaScan, 32), PermissionIntent.Continue)
        val denied = PermissionMachine.reduce(request.state,
            PermissionIntent.NativeResult(1, observed(rationale = true)))
        assertEquals(PermissionStatus.DeniedRequestable, denied.state.status)
        assertEquals(PermissionStage.Recovery, denied.state.stage)
        assertEquals(PermissionStage.Explanation,
            PermissionMachine.reduce(denied.state, PermissionIntent.Retry).state.stage)
        assertEquals(PermissionEffect.PreserveManualControls,
            PermissionMachine.reduce(denied.state, PermissionIntent.NotNow).effects.single())
        assertEquals(denied.state, PermissionMachine.reduce(denied.state, PermissionIntent.OpenSettings).state)
    }

    @Test fun settingsRequiresExplicitTapAndFreshReturnObservation() {
        val request = PermissionMachine.reduce(
            PermissionMachine.begin(PermissionCapability.MediaAudio,
                PermissionPurpose.FirstConsentedMediaScan, 35), PermissionIntent.Continue)
        val permanent = PermissionMachine.reduce(request.state,
            PermissionIntent.NativeResult(1, observed(rationale = false))).state
        val external = PermissionMachine.reduce(permanent, PermissionIntent.OpenSettings)
        assertEquals(listOf(PermissionEffect.OpenAndroidSettings), external.effects)
        assertEquals(PermissionStage.WaitingForSettingsReturn, external.state.stage)
        assertEquals(external.state, PermissionMachine.reduce(external.state,
            PermissionIntent.Rechecked(1, observed(granted = true))).state)
        val returnStep = PermissionMachine.reduce(external.state, PermissionIntent.ReturnedFromSettings)
        assertEquals(PermissionStage.WaitingForPlatformRecheck, returnStep.state.stage)
        assertEquals(PermissionEffect.RecheckAndroidPermission(2), returnStep.effects.single())
        assertEquals(returnStep.state, PermissionMachine.reduce(returnStep.state,
            PermissionIntent.Rechecked(1, observed(granted = true))).state)
        assertEquals(PermissionStage.Ready, PermissionMachine.reduce(returnStep.state,
            PermissionIntent.Rechecked(2, observed(granted = true))).state.stage)
    }

    @Test fun allThreeSettingsOutcomesRemainSeparateAndNeverLoop() {
        val initial = PermissionMachine.begin(
            PermissionCapability.Microphone, PermissionPurpose.ExplicitMicrophoneTap, 35)
        val denied = PermissionMachine.reduce(
            PermissionMachine.reduce(initial, PermissionIntent.Continue).state,
            PermissionIntent.NativeResult(1, observed(rationale = false))).state
        val settings = PermissionMachine.reduce(denied, PermissionIntent.OpenSettings).state
        val refreshing = PermissionMachine.reduce(settings, PermissionIntent.ReturnedFromSettings).state
        for ((obs, status) in listOf(
            observed(granted = true) to PermissionStatus.Granted,
            observed(rationale = true) to PermissionStatus.DeniedRequestable,
            observed(rationale = false) to PermissionStatus.PermanentlyDenied
        )) {
            val result = PermissionMachine.reduce(refreshing, PermissionIntent.Rechecked(2, obs))
            assertEquals(status, result.state.status)
            assertFalse(result.effects.contains(PermissionEffect.OpenAndroidSettings))
        }
    }

    @Test fun revocationTriggersReconcileOrVoiceCancellationNotPlaybackMutation() {
        val media = PermissionMachine.begin(
            PermissionCapability.MediaAudio, PermissionPurpose.FirstConsentedMediaScan, 35)
        val lostMedia = PermissionMachine.reduce(media, PermissionIntent.Revoked)
        assertTrue(lostMedia.effects.contains(PermissionEffect.ReconcileRevokedMedia))
        assertTrue(lostMedia.effects.contains(PermissionEffect.PreserveManualControls))
        val mic = PermissionMachine.begin(
            PermissionCapability.Microphone, PermissionPurpose.ExplicitMicrophoneTap, 35)
        assertTrue(PermissionMachine.reduce(mic, PermissionIntent.Revoked).effects
            .contains(PermissionEffect.CancelVoiceGeneration))
        assertEquals(PermissionStage.Explanation,
            PermissionMachine.reduce(lostMedia.state, PermissionIntent.Retry).state.stage)
    }

    @Test fun notificationIsDeferredToApprovedPlaybackPurposeAndOsRequiredVersions() {
        try {
            PermissionMachine.begin(PermissionCapability.Notifications,
                PermissionPurpose.ExplicitMicrophoneTap, 33)
            fail("Unacknowledged notification purpose accepted")
        } catch (expected: IllegalArgumentException) {
            // Must come from a value-acknowledged playback action.
        }
        val start = PermissionMachine.begin(PermissionCapability.Notifications,
            PermissionPurpose.AcknowledgedPlaybackControls, 29)
        val outcome = PermissionMachine.reduce(start, PermissionIntent.Continue)
        assertEquals(PermissionStatus.NotApplicable, outcome.state.status)
        assertEquals(PermissionStage.Ready, outcome.state.stage)
        assertFalse(outcome.effects.any { it is PermissionEffect.RequestAndroidPermission })
    }

    @Test fun cancellationOrDuplicateResultCannotTriggerRepeatedAction() {
        val start = PermissionMachine.begin(
            PermissionCapability.Microphone, PermissionPurpose.ExplicitMicrophoneTap, 35)
        val request = PermissionMachine.reduce(start, PermissionIntent.Continue).state
        val denied = PermissionMachine.reduce(request,
            PermissionIntent.NativeResult(1, observed(rationale = true))).state
        assertTrue(PermissionMachine.reduce(denied,
            PermissionIntent.NativeResult(1, observed(granted = true))).effects.isEmpty())
        val manual = PermissionMachine.reduce(denied, PermissionIntent.NotNow)
        assertEquals(PermissionStage.ManualFallback, manual.state.stage)
        assertTrue(PermissionMachine.reduce(manual.state, PermissionIntent.Continue).effects.isEmpty())
    }
}
