package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * Source-level P05 invariants; JVM executes without Android runtime.
 * Actual Media3 playback, callback ordering and audibility need P12/P13 tests.
 */
class Issue7Phase5ServicePlaybackContractTest {
    private val root = File("src/main/java/com/latticeward/musiccopilot")
    private fun source(relative: String) = File(root, relative).readText()

    @Test fun oneServiceExoplayerSessionAndNoUiOwnedPlayer() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertEquals(1, Regex("ExoPlayer\\.Builder\\(").findAll(service).count())
        assertEquals(1, Regex("MediaSession\\.Builder\\(").findAll(service).count())
        for (p in listOf(
            "feature/library/LibraryBrowseViewModel.kt",
            "feature/search/LocalSearchViewModel.kt",
            "core/application/playback/LibraryMediaSelectionUseCase.kt"
        )) {
            assertFalse(source(p).contains("ExoPlayer.Builder("))
        }
    }

    @Test fun unpreparedAndEndedQueueMustPrepareOrRestartOnExplicitPlay() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("is PlaybackCommand.Play ->"))
        assertTrue(service.contains("player.playbackState == Player.STATE_IDLE"))
        assertTrue(service.contains("player.playbackState == Player.STATE_ENDED"))
        assertTrue(service.contains("player.prepare()"))
        assertTrue(service.contains("player.seekTo(index, 0L)"))
    }

    @Test fun realAudioConfigurationUsesMusicAttributesNotStreaming() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("setAudioAttributes("))
        assertTrue(service.contains("C.USAGE_MEDIA"))
        assertTrue(service.contains("C.AUDIO_CONTENT_TYPE_MUSIC"))
        val mapper = source("playback/media3/LocalMedia3QueueItems.kt")
        assertTrue(mapper.contains("uri.authority == \"media\""))
        assertTrue(mapper.contains("setUri(uri)"))
        assertFalse(mapper.contains("http://"))
        assertFalse(mapper.contains("https://"))
    }

    @Test fun playbackSnapshotMustProjectPlayerEventsAndQueueIndexChanges() {
        val source = source("playback/media3/Media3SnapshotProjector.kt")
        assertTrue(source.contains("player.isPlaying"))
        assertTrue(source.contains("Player.STATE_BUFFERING"))
        assertTrue(source.contains("player.playerError != null"))
        assertTrue(source.contains("queue != previousQueue || index != previousIndex"))
        assertFalse(source.contains("queueRevision = revision"))
    }

    @Test fun loadingOnIoAndVerifiedQueueBeforePlayerMutation() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.indexOf("queueEngine.prepare(command)") <
            service.indexOf("player.setMediaItems("))
        val queue = source("playback/service/ServiceQueueEngine.kt")
        assertTrue(queue.contains("withContext(dispatchers.io)"))
        assertTrue(service.contains("catch (cancelled: CancellationException)"))
    }

    @Test fun controllerPreservesBufferingInsteadOfOptimisticPlaying() {
        val selection = source("core/application/playback/LibraryMediaSelectionUseCase.kt")
        assertTrue(selection.contains("VisiblePlaybackQueuePolicy.matchesAcknowledgement"))
        assertTrue(selection.contains("confirmed.playbackState == PlaybackState.Playing"))
        assertTrue(selection.contains("SelectedPlaybackState.AcceptedNotPlaying"))
        assertFalse(selection.contains("items = listOf(ref)"))
    }
}
