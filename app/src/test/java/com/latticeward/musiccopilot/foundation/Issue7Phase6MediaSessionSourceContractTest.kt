package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** P06 source-level security/ownership contract, NOT device Media3 integration. */
class Issue7Phase6MediaSessionSourceContractTest {
    private val root = File("src/main/java/com/latticeward/musiccopilot")
    private fun source(path: String) = File(root, path).readText()

    @Test fun sessionUsesForwardingFacadeOverOneExoPlayer() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertEquals(1, Regex("ExoPlayer\\.Builder\\(").findAll(service).count())
        assertEquals(1, Regex("MediaSession\\.Builder\\(").findAll(service).count())
        assertTrue(service.contains("ServiceSessionTransportPlayer(candidate)"))
        assertTrue(service.contains("MediaSession.Builder(this, systemTransport)"))
    }

    @Test fun systemTransportNeverCallsTheRawPlayerForCommands() {
        val facade = source("playback/media3/ServiceSessionTransportPlayer.kt")
        assertTrue(facade.contains("ForwardingPlayer(delegate)"))
        assertTrue(facade.contains("SystemTransportPolicy.translate(action, id(), positionMs)"))
        assertFalse(facade.contains("delegate.play()"))
        assertFalse(facade.contains("delegate.seekTo("))
    }

    @Test fun callerGetsExactlyExplicitPlayerTransportAllowlist() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val facade = source("playback/media3/ServiceSessionTransportPlayer.kt")
        assertTrue(service.contains(".setAvailablePlayerCommands("))
        assertTrue(service.contains("ServiceSessionTransportPlayer.permittedCommands()"))
        assertTrue(facade.contains("Player.COMMAND_PLAY_PAUSE"))
        assertTrue(facade.contains("Player.COMMAND_SEEK_TO_PREVIOUS"))
        assertTrue(facade.contains("Player.COMMAND_SEEK_TO_NEXT"))
        assertTrue(facade.contains("Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM"))
        assertFalse(facade.contains(".add(Player.COMMAND_CHANGE_MEDIA_ITEMS)"))
        assertFalse(facade.contains(".add(Player.COMMAND_SET_REPEAT_MODE)"))
        assertFalse(facade.contains(".add(Player.COMMAND_SET_SHUFFLE_MODE)"))
    }

    @Test fun externalPlaylistUriAndLegacyPrepareFromUriFailClosed() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("override fun onAddMediaItems("))
        assertTrue(service.contains("override fun onSetMediaItems("))
        assertTrue(service.contains("Futures.immediateFailedFuture("))
    }

    @Test fun privateCustomCommandsCheckCallerUidAndPackage() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("controller.packageName == packageName"))
        assertTrue(service.contains("controller.uid == android.os.Process.myUid()"))
        assertTrue(service.contains("if (!isPrivateCaller(controller))"))
        assertTrue(service.contains("commandBuilder.add(PlaybackWireProtocol.executeCommand())"))
        assertTrue(service.contains("commandBuilder.add(PlaybackWireProtocol.snapshotCommand())"))
    }

    @Test fun inAppAndSystemCommandsShareOneWorker() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertEquals(1, Regex("PlaybackCommandProcessor\\(").findAll(service).count())
        assertTrue(service.contains("processor.submit(intent)"))
        assertTrue(service.contains("processor.submit(command)"))
        val facade = source("playback/media3/ServiceSessionTransportPlayer.kt")
        assertTrue(facade.contains("SystemTransportPolicy.Action.Previous"))
        assertTrue(facade.contains("SystemTransportPolicy.Action.Next"))
    }

    @Test fun systemCannotSeekArbitraryQueueIndex() {
        val facade = source("playback/media3/ServiceSessionTransportPlayer.kt")
        assertTrue(facade.contains("mediaItemIndex == currentMediaItemIndex"))
        assertTrue(facade.contains("SelectQueueIndex"))
        assertFalse(facade.contains("delegate.seekTo(mediaItemIndex"))
    }

    @Test fun controllerReconnectPullsFreshSnapshotNotOptimisticState() {
        val controller = source("playback/controller/PlaybackControllerConnection.kt")
        val relay = source("playback/controller/ControllerSnapshotRelay.kt")
        assertTrue(controller.contains("PlaybackWireProtocol.snapshotCommand()"))
        assertTrue(controller.contains("accept(controller.sessionExtras, true)"))
        assertTrue(controller.contains("accept(response.extras, false)"))
        assertTrue(relay.contains("wholeSnapshot.revision <= mutableSnapshot.value.revision"))
    }
}
