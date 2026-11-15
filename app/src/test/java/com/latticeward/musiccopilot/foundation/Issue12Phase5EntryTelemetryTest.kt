package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.app.navigation.Destination
import com.latticeward.musiccopilot.app.navigation.DrivingNavigationViewModel
import com.latticeward.musiccopilot.core.application.voice.ObservedVoicePttCoordinator
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetryPlaybackState
import com.latticeward.musiccopilot.quality.api.SanitizedTelemetryEvent
import com.latticeward.musiccopilot.quality.api.TelemetrySink
import com.latticeward.musiccopilot.quality.noop.NoOpTelemetrySink
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.voice.api.VoiceCancelReason
import com.latticeward.musiccopilot.voice.api.VoiceOrigin
import com.latticeward.musiccopilot.voice.api.VoiceSessionCoordinator
import com.latticeward.musiccopilot.voice.api.VoiceSessionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #12 P05 — source-authored QA only.
 * NEVER compiled, tested or run in this phase (no Gradle/CI/APK).
 */
class Issue12Phase5EntryTelemetryTest {
    private class FakeVoiceOwner : VoiceSessionCoordinator {
        private val mutable = MutableStateFlow<VoiceSessionState>(VoiceSessionState.Idle)
        override val state: StateFlow<VoiceSessionState> = mutable
        var canBegin = true
        private var sequence = 0L
        private fun denied() = AppResult.Failure(
            AppError.Voice(AppErrorCodes.VOICE_UNAVAILABLE,
                Recoverability.USER_ACTION_REQUIRED, RecoveryAction.ManualFallback)
        )

        override fun start(origin: VoiceOrigin): AppResult<VoiceSessionId> {
            if (!canBegin || mutable.value != VoiceSessionState.Idle) return denied()
            val id = VoiceSessionId(++sequence)
            mutable.value = VoiceSessionState.Listening(id, origin, 1)
            return AppResult.Success(id)
        }

        fun failWithRetry() {
            val listening = mutable.value as VoiceSessionState.Listening
            mutable.value = VoiceSessionState.Failed(
                listening.sessionId, listening.origin,
                AppError.Voice(AppErrorCodes.VOICE_NO_MATCH,
                    Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
                canRetry = true
            )
        }

        override fun retry(): AppResult<Unit> {
            val failed = mutable.value as? VoiceSessionState.Failed ?: return denied()
            if (!failed.canRetry || !canBegin) return denied()
            mutable.value = VoiceSessionState.Listening(failed.sessionId, failed.origin, 2)
            return AppResult.Success(Unit)
        }

        override fun confirm(token: VoiceCandidateToken, optionIndex: Int): AppResult<Unit> = denied()
        override fun cancel(reason: VoiceCancelReason) { mutable.value = VoiceSessionState.Idle }
    }

    @Test fun drivingEntryAdmittedOnceForLibraryAndNeverOnDuplicateTap() {
        val nav = DrivingNavigationViewModel()
        assertTrue(nav.enterFrom(Destination.Library, allowedByAppRoute = true))
        assertTrue(nav.state.active)
        assertFalse(nav.enterFrom(Destination.Library, allowedByAppRoute = true))
        assertFalse(nav.enterFrom(Destination.FullPlayer, allowedByAppRoute = true))
        assertEquals(Destination.Library, nav.exit())
        assertFalse(nav.state.active)
        assertTrue(nav.enterFrom(Destination.FullPlayer, allowedByAppRoute = true))
        assertEquals(Destination.FullPlayer, nav.state.origin)
    }

    @Test fun deniedAndUnapprovedDrivingOriginsNeverCount() {
        val nav = DrivingNavigationViewModel()
        assertFalse(nav.enterFrom(Destination.Library, allowedByAppRoute = false))
        assertFalse(nav.enterFrom(Destination.Settings, allowedByAppRoute = true))
        assertFalse(nav.enterFrom(Destination.Search, allowedByAppRoute = true))
        assertFalse(nav.state.active)
    }

    @Test fun systemBoundaryInvalidatesWithoutRestartingDriving() {
        val nav = DrivingNavigationViewModel()
        assertTrue(nav.enterFrom(Destination.Library, true))
        nav.clearForSystemBoundary()
        assertFalse(nav.state.active)
        nav.clearForSystemBoundary()
        assertFalse(nav.state.active)
    }

    @Test fun oneAcceptedMicTapYieldsOneCallbackEvenAfterRepeatStart() {
        val base = FakeVoiceOwner()
        var observations = 0
        val voice = ObservedVoicePttCoordinator(base) { observations++ }
        assertTrue(voice.start(VoiceOrigin.Player) is AppResult.Success<*>)
        assertEquals(1, observations)
        assertTrue(voice.start(VoiceOrigin.Player) is AppResult.Failure)
        assertEquals(1, observations)
        voice.cancel(VoiceCancelReason.UserBack)
        assertTrue(voice.start(VoiceOrigin.Driving) is AppResult.Success<*>)
        assertEquals(2, observations)
    }

    @Test fun failedProviderAdmissionCannotRecordVoiceButtonPress() {
        val base = FakeVoiceOwner().apply { canBegin = false }
        var observations = 0
        val voice = ObservedVoicePttCoordinator(base) { observations++ }
        assertTrue(voice.start(VoiceOrigin.Player) is AppResult.Failure)
        assertTrue(voice.start(VoiceOrigin.Driving) is AppResult.Failure)
        assertEquals(0, observations)
    }

    @Test fun acceptedExplicitRetryCountsOnceButStaleRetryDoesNot() {
        val base = FakeVoiceOwner()
        var observations = 0
        val voice = ObservedVoicePttCoordinator(base) { observations++ }
        assertTrue(voice.start(VoiceOrigin.Driving) is AppResult.Success<*>)
        base.failWithRetry()
        assertTrue(voice.retry() is AppResult.Success<*>)
        assertEquals(2, observations) // Two explicitly admitted PTT attempts.
        assertTrue(voice.retry() is AppResult.Failure)
        assertEquals(2, observations)
    }

    @Test fun qualityExceptionCannotChangeSuccessfulVoiceAdmission() {
        val base = FakeVoiceOwner()
        val voice = ObservedVoicePttCoordinator(base) {
            throw IllegalStateException("synthetic Quality unavailable")
        }
        assertTrue(voice.start(VoiceOrigin.Player) is AppResult.Success<*>)
        assertTrue(voice.state.value is VoiceSessionState.Listening)
    }

    @Test fun bothOriginalEventsUseOnlyApprovedV1Fields() {
        val gate = AllowlistedTelemetryPrivacyGate()
        val driving = gate.sanitize(TelemetryEvent.DrivingModeStarted("home", true))
        val ptt = gate.sanitize(
            TelemetryEvent.VoiceButtonPressed(TelemetryPlaybackState.Paused))
        assertTrue(driving is AppResult.Success<*>)
        assertTrue(ptt is AppResult.Success<*>)
        assertEquals("v1|driving_mode_started|entry_source=home|bt_connected=true",
            (driving as AppResult.Success<SanitizedTelemetryEvent>).value.encoded)
        assertEquals("v1|voice_button_pressed|playback_state=Paused",
            (ptt as AppResult.Success<SanitizedTelemetryEvent>).value.encoded)
        assertFalse(NoOpTelemetrySink().tryEmit(ptt.value))
    }

    @Test fun sourceWiringDoesNotInstrumentRecompositionOrBluetoothSuggestion() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val nav = File(root + "app/navigation/AppNavHost.kt").readText()
        val viewModel = File(root + "app/navigation/DrivingNavigationViewModel.kt").readText()
        val app = File(root + "app/di/ApplicationBindings.kt").readText()
        assertTrue(nav.contains("val enterDrivingOnce: (Destination, Boolean) -> Unit"))
        assertTrue(nav.contains("onDrivingModeEntered(source)"))
        assertTrue(nav.contains("enterDrivingOnce(origin, true)"))
        assertTrue(nav.contains("enterDrivingOnce(\n                        Destination.Library,"))
        assertTrue(nav.contains("enterDrivingOnce(\n                                    Destination.FullPlayer,"))
        assertTrue(viewModel.contains("if (state.active || !next.active) return false"))
        assertTrue(app.contains("ObservedVoicePttCoordinator(voiceOwner"))
        assertTrue(app.contains("CoarseBluetoothAudioOutput.isConnected(mediaAppContext)"))
        assertTrue(app.contains("diagnosticsCollectionAuthorization.canCollect()"))
        assertTrue(app.contains("TelemetryEvent.VoiceButtonPressed(playback)"))
        assertFalse(nav.contains("LaunchedEffect(drivingNavigation.state.active)"))
    }

    @Test fun noDeviceIdentityEntersCoarseBluetoothEvidenceSource() {
        val path = "src/main/java/com/latticeward/musiccopilot/" +
            "app/platform/audio/CoarseBluetoothAudioOutput.kt"
        val source = File(path).readText()
        assertTrue(source.contains("GET_DEVICES_OUTPUTS"))
        assertFalse(source.contains(".address"))
        assertFalse(source.contains(".productName"))
        assertFalse(source.contains("getBondedDevices"))
        assertFalse(source.contains("BLUETOOTH_SCAN"))
    }
}
