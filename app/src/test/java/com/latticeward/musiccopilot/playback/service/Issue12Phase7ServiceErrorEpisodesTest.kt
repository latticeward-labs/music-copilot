package com.latticeward.musiccopilot.playback.service

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.playback.api.PlayerRuntimeErrorCode
import com.latticeward.musiccopilot.quality.api.RouteType
import com.latticeward.musiccopilot.quality.api.TelemetryEvent
import com.latticeward.musiccopilot.quality.privacy.AllowlistedTelemetryPrivacyGate
import com.latticeward.musiccopilot.core.result.AppResult
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** P07 authored-only. NO compilation, JVM/Android tests or Gradle until P14. */
class Issue12Phase7ServiceErrorEpisodesTest {
    private fun playback(code: String) = AppError.Playback(
        code, Recoverability.RECOVERABLE, RecoveryAction.ManualFallback)
    private fun media(code: String, ref: MediaRef? = null) = AppError.MediaItem(
        code, Recoverability.RECOVERABLE, RecoveryAction.Skip, ref)
    private fun interruption(code: String) = AppError.Interruption(
        code, Recoverability.RECOVERABLE, RecoveryAction.ManualResume)

    @Test fun oneMedia3ErrorEpisodeCannotBeMultipliedByOnEvents() {
        val episodes = ServicePlayerErrorEpisodeGate()
        val error = playback(AppErrorCodes.PLAYBACK_FAILED)
        assertEquals(PlayerRuntimeErrorCode.PLAYBACK_FAILED, episodes.observe(error))
        repeat(100) { assertNull(episodes.observe(error)) }
    }

    @Test fun clearingErrorThenNewErrorIsNewObservedEpisode() {
        val episodes = ServicePlayerErrorEpisodeGate()
        val error = media(AppErrorCodes.MEDIA_ITEM_CORRUPT)
        assertEquals(PlayerRuntimeErrorCode.MEDIA_ITEM_CORRUPT, episodes.observe(error))
        assertNull(episodes.observe(null))
        assertEquals(PlayerRuntimeErrorCode.MEDIA_ITEM_CORRUPT, episodes.observe(error))
    }

    @Test fun changedTypedErrorCodeIsOneNewEpisodeWithoutClear() {
        val episodes = ServicePlayerErrorEpisodeGate()
        assertEquals(PlayerRuntimeErrorCode.MEDIA_ITEM_MISSING,
            episodes.observe(media(AppErrorCodes.MEDIA_ITEM_MISSING)))
        assertEquals(PlayerRuntimeErrorCode.MEDIA_ITEM_UNPLAYABLE,
            episodes.observe(media(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)))
        assertNull(episodes.observe(media(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)))
    }

    @Test fun interruptionIsNotCrashAndDoesNotTakeRawRoute() {
        val episodes = ServicePlayerErrorEpisodeGate()
        assertEquals(PlayerRuntimeErrorCode.ROUTE_INVALIDATED,
            episodes.observe(interruption(AppErrorCodes.ROUTE_INVALIDATED)))
        assertNull(episodes.observe(interruption(AppErrorCodes.ROUTE_INVALIDATED)))
    }

    @Test fun privateMediaRefNeverLeavesClosedCodeBoundary() {
        val episodes = ServicePlayerErrorEpisodeGate()
        val privateRef = MediaRef("audio:UserPrivateTrack")
        val error = media(AppErrorCodes.MEDIA_ITEM_CORRUPT, privateRef)
        val code = episodes.observe(error)
        assertEquals(PlayerRuntimeErrorCode.MEDIA_ITEM_CORRUPT, code)
        val sanitized = AllowlistedTelemetryPrivacyGate().sanitize(
            TelemetryEvent.PlayerError(code!!.name, RouteType.Unknown))
        assertTrue(sanitized is AppResult.Success<*>)
        assertFalse(sanitized.toString().contains("UserPrivateTrack"))
    }

    @Test fun servicePermissionRevokedIsDroppedUntilP03SchemaApproval() {
        val episodes = ServicePlayerErrorEpisodeGate()
        assertNull(episodes.observe(playback(AppErrorCodes.PERMISSION_REVOKED)))
        assertNull(PlayerRuntimeErrorCode.fromKnownServiceCode(AppErrorCodes.PERMISSION_REVOKED))
        assertEquals(PlayerRuntimeErrorCode.PLAYBACK_FAILED,
            episodes.observe(playback(AppErrorCodes.PLAYBACK_FAILED)))
    }

    @Test fun arbitraryProviderErrorIsNotForwarded() {
        val episodes = ServicePlayerErrorEpisodeGate()
        assertNull(episodes.observe(playback("file:///private/music/name.mp3")))
        assertNull(episodes.observe(playback("PLAYBACK_FAILED;username=x")))
        assertNull(PlayerRuntimeErrorCode.fromKnownServiceCode("customer_email"))
    }

    @Test fun nonServiceRuntimeErrorTypesAreNotPlayerError() {
        val episodes = ServicePlayerErrorEpisodeGate()
        assertNull(episodes.observe(AppError.Controller(
            AppErrorCodes.CONTROLLER_TIMEOUT, Recoverability.RECOVERABLE,
            RecoveryAction.ManualFallback)))
        assertNull(episodes.observe(AppError.Voice(
            AppErrorCodes.VOICE_TIMEOUT, Recoverability.RECOVERABLE,
            RecoveryAction.ManualFallback)))
    }

    @Test fun sourceHooksAfterRealServiceSnapshotPublishNotAtPresentationRecompose() {
        val root = "src/main/java/com/latticeward/musiccopilot/"
        val service = File(root+"playback/service/MusicPlaybackService.kt").readText()
        val host = File(root+"app/MusicCopilotApplication.kt").readText()
        val app = File(root+"app/di/ApplicationBindings.kt").readText()
        val body = service.substringAfter("private fun publishSnapshot()")
            .substringBefore("private fun wrap(")
        assertTrue(body.indexOf("session.setSessionExtras(wrap(next))") <
            body.indexOf("playerErrorEpisodeGate.observe(next.lastError)"))
        assertTrue(body.contains("observeServicePlayerError(code)"))
        assertTrue(host.contains("PlaybackRuntimeDiagnosticHost"))
        assertTrue(app.contains("TelemetryEvent.PlayerError(error.name, RouteType.Unknown)"))
        assertTrue(app.contains("diagnosticsCollectionAuthorization.canCollect()"))
    }

    @Test fun onlyOriginalP03PlayerErrorFamilyIsUsed() {
        val event = TelemetryEvent.PlayerError(
            PlayerRuntimeErrorCode.MEDIA_ITEM_CORRUPT.name, RouteType.Unknown)
        assertTrue(AllowlistedTelemetryPrivacyGate().sanitize(event) is AppResult.Success<*>)
        assertTrue(AllowlistedTelemetryPrivacyGate().sanitize(
            TelemetryEvent.PlayerError("VOICE_TIMEOUT", RouteType.Unknown)
        ) is AppResult.Failure)
    }
}
