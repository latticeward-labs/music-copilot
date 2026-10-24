package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * P12 cross-phase source regressions. AUTHORED ONLY. Do not execute in
 * intermediate phases or GitHub Actions; verify with the P13 integrated
 * technical build and the one owner-facing P13 phone APK.
 *
 * These checks do not prove real Android playback or race ordering.
 */
class Issue7Phase12IntegratedBoundaryContractTest {
    private val main = File("src/main/java/com/latticeward/musiccopilot")
    private fun code(path: String) = File(main, path).readText()
    private fun manifest() = File("src/main/AndroidManifest.xml").readText()

    @Test fun oneServiceOwnsExactlyOneExoPlayerAndSession() {
        val source = code("playback/service/MusicPlaybackService.kt")
        assertEquals(1, Regex("ExoPlayer\\.Builder\\(").findAll(source).count())
        assertEquals(1, Regex("MediaSession\\.Builder\\(").findAll(source).count())
    }

    @Test fun media3LocalWakeModeHasRequiredNormalPermission() {
        val service = code("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("setWakeMode(C.WAKE_MODE_LOCAL)"))
        assertTrue(manifest().contains("android.permission.WAKE_LOCK"))
    }

    @Test fun foregroundServiceAndNoAutoBoot() {
        val xml = manifest()
        assertTrue(xml.contains("android:foregroundServiceType=\"mediaPlayback\""))
        assertTrue(xml.contains("android:stopWithTask=\"false\""))
        assertFalse(xml.contains("android.intent.action.BOOT_COMPLETED"))
        assertFalse(xml.contains("android.permission.INTERNET"))
    }

    @Test fun controllerDefersServiceBindUntilForeground() {
        val controller = code("playback/controller/PlaybackControllerConnection.kt")
        val activity = code("app/MainActivity.kt")
        val graph = code("app/di/ApplicationBindings.kt")
        assertFalse(controller.contains("init { if (appContext != null) ensureConnected() }"))
        assertTrue(graph.contains("fun onForeground()"))
        assertTrue(graph.contains("controller.ensureConnected()"))
        assertTrue(activity.contains("bindings.onForeground()"))
    }

    @Test fun outOfDateControllerCannotPublishAcrossActivityDetach() {
        val controller = code("playback/controller/PlaybackControllerConnection.kt")
        assertTrue(controller.contains("capturedGeneration = generation"))
        assertTrue(controller.contains("generation != capturedGeneration"))
        assertTrue(controller.contains("active !== controller"))
        assertTrue(controller.contains("fun detachUiController()"))
    }

    @Test fun mediaStoreQueuePreparationRespectsNewerCommandReceipts() {
        val service = code("playback/service/MusicPlaybackService.kt")
        // P07 stamps the ORIGINAL receive-time generation, never dispatch.
        assertTrue(service.contains("val commandEpoch = restoreGeneration"))
        assertTrue(service.contains("processor.configureReceiptSafety("))
        assertTrue(service.contains("receipt.commandGeneration == restoreGeneration"))
        assertTrue(service.contains("commandEpoch != restoreGeneration"))
        assertTrue(service.contains("routeAtStart = interruptionGate.routeGeneration"))
        assertTrue(service.contains("recoveryGeneration"))
    }

    @Test fun serviceOwnedFocusNoisyAndRouteLossRemainActive() {
        val service = code("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("setHandleAudioBecomingNoisy(true)"))
        assertTrue(service.contains("interruptionGate.onHeadsetOrBluetoothLost()"))
        // P05/P06 now supply Main-looper named callbacks.
        assertTrue(service.contains("ServiceAudioRouteMonitor("))
        assertTrue(service.contains("onPersonalOutputDisconnected ="))
        assertTrue(service.contains("candidate.setAudioAttributes("))
    }

    @Test fun mediaSessionProhibitsExternalPlaylistInjection() {
        val service = code("playback/service/MusicPlaybackService.kt")
        val transport = code("playback/media3/ServiceSessionTransportPlayer.kt")
        assertTrue(service.contains("setAvailablePlayerCommands("))
        assertTrue(service.contains("override fun onAddMediaItems("))
        assertTrue(service.contains("override fun onSetMediaItems("))
        assertFalse(transport.contains(".add(Player.COMMAND_CHANGE_MEDIA_ITEMS)"))
        assertFalse(transport.contains(".add(Player.COMMAND_SET_REPEAT_MODE)"))
    }

    @Test fun recoveredPlaybackNoticeDoesNotSuppressForegroundOrCheckpoint() {
        val service = code("playback/service/MusicPlaybackService.kt")
        val checkpoint = code("playback/service/ServicePausedCheckpoint.kt")
        assertTrue(service.contains("player.playbackState == Player.STATE_IDLE"))
        assertTrue(service.contains("runtimeError is AppError.MediaItem"))
        assertFalse(checkpoint.contains("snapshot.lastError != null\n        ) return"))
    }

    @Test fun recoveryTriesOnlyFreshlyVerifiedLocalMedia() {
        val service = code("playback/service/MusicPlaybackService.kt")
        val policy = code("playback/service/PlaybackFailureRecoveryPolicy.kt")
        assertTrue(policy.contains("maxForwardCandidates: Int = 3"))
        assertTrue(service.contains("queueEngine.revalidate(MediaRef(media.mediaId))"))
        assertTrue(service.contains("requestIsStillCurrent(routeAtStart)"))
    }

    @Test fun coldRestoreIsPausedNeverAutomaticPlay() {
        val service = code("playback/service/MusicPlaybackService.kt")
        val body = service.substringAfter("private suspend fun restoreColdPaused(")
            .substringBefore("private fun publishSnapshot()")
        assertTrue(body.contains("playWhenReady = false"))
        assertTrue(body.contains("player.pause()"))
        assertFalse(body.contains("player.play()"))
    }

    @Test fun checkpointDoesNotPersistPersonalPathsOrTitles() {
        val checkpoint = code("playback/service/ServicePausedCheckpoint.kt")
        val codec = code("data/datastore/RestoreProtoCodec.kt")
        assertTrue(checkpoint.contains("RestorableQueueSnapshot("))
        assertFalse(checkpoint.contains("displayTitle ="))
        assertFalse(checkpoint.contains("contentUri ="))
        assertFalse(codec.contains("coded.writeString(7"))
    }

    @Test fun libraryAndSearchSelectionUseTheSameServiceGateway() {
        val library = code("feature/library/LibraryBrowseViewModel.kt")
        val search = code("feature/search/LocalSearchViewModel.kt")
        val selection = code("core/application/playback/LibraryMediaSelectionUseCase.kt")
        assertTrue(library.contains("delegate.select(song.ref, revision, visibleRefs, selectedIndex)"))
        assertTrue(search.contains("delegate.select(item.ref, revision, visibleRefs, selectedIndex)"))
        assertTrue(selection.contains("gateway.execute(PlaybackCommand.ReplaceQueue("))
    }

    @Test fun noSecondaryUiOwnedExoplayerOrMediaSession() {
        for (path in listOf(
            "feature/player/PlayerViewModel.kt",
            "feature/player/FullPlayerScreen.kt",
            "feature/player/MiniPlayerBar.kt",
            "app/navigation/AppNavHost.kt",
        )) {
            val text = code(path)
            assertFalse(path, text.contains("ExoPlayer.Builder("))
            assertFalse(path, text.contains("MediaSession.Builder("))
        }
    }

    @Test fun onlyOneFinalOwnerPhoneApkAndNoGitHubActionsGate() {
        val plan = File("../docs/implementation/ISSUE_7_PHASED_EXECUTION_PLAN.md").readText()
        assertTrue(plan.contains("ONE consolidated"))
        assertTrue(plan.contains("P13"))
        assertTrue(plan.contains("M01–M36"))
        assertTrue(plan.contains("T01–T15"))
    }
}
