package com.latticeward.musiccopilot.foundation

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/**
 * P11 source architecture regression contracts. They are authored but NOT
 * executed in P11 or GitHub Actions. Actual Android runtime/Proto/Media3
 * tests belong to the single final P13 technical readiness gate.
 */
class Issue7Phase11RestoreSourceContractTest {
    private val root = File("src/main/java/com/latticeward/musiccopilot")
    private fun source(relative: String) = File(root, relative).readText()

    @Test fun applicationOwnsOneExistingRestorableQueueStore() {
        val app = source("app/MusicCopilotApplication.kt")
        val graph = source("app/di/ApplicationBindings.kt")
        assertTrue(app.contains("createPlaybackRestorableQueueStore()"))
        assertTrue(app.contains("playbackCheckpointWriteScope()"))
        assertTrue(graph.contains("val restorableQueueStore: RestorableQueueStore"))
        // Writes must survive normal service teardown, without moving Player
        // ownership out of its existing MediaSessionService lifecycle.
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("appHost.playbackCheckpointWriteScope()"))
    }

    @Test fun serviceNeverImportsConcreteProtoOrRoomImplementation() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertFalse(service.contains("import com.latticeward.musiccopilot.data.datastore."))
        assertFalse(service.contains("import com.latticeward.musiccopilot.data.room."))
        assertTrue(service.contains("createPlaybackRestorableQueueStore()"))
    }

    @Test fun coldRestoreDoesNotCallPlayOrStartForeground() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val cold = service.substringAfter("private suspend fun restoreColdPaused(")
            .substringBefore("    private fun publishSnapshot()")
        assertTrue(cold.contains("playWhenReady = false"))
        assertTrue(cold.contains("player.playWhenReady = false"))
        assertTrue(cold.contains("player.pause()"))
        assertFalse(cold.contains("player.play()"))
        assertFalse(cold.contains("startForeground("))
    }

    @Test fun livePlayerOrNewCommandsOverrideColdRestore() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("player.mediaItemCount == 0"))
        assertTrue(service.contains("expectedGeneration == restoreGeneration"))
        assertTrue(service.contains("++restoreGeneration"))
    }

    @Test fun checkpointIsReadBeforeFirstSaveCanEraseIt() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val scope = service.substringAfter("restoreColdPaused(startingGeneration, startingRouteEpoch)")
            .substringBefore("        // Only the real service Player position")
        assertTrue(scope.contains("pausedCheckpoint.enable()"))
        assertTrue(scope.contains("pausedCheckpoint.onSnapshot(confirmed)"))
    }

    @Test fun noPersonalMetadataOrRawUrisInProtoCheckpoint() {
        val writer = source("playback/service/ServicePausedCheckpoint.kt")
        val proto = source("data/datastore/RestoreProtoCodec.kt")
        assertTrue(writer.contains("RestorableQueueSnapshot("))
        assertTrue(writer.contains("items = snapshot.queue.toList()"))
        assertFalse(writer.contains("displayTitle ="))
        assertFalse(writer.contains("contentUri ="))
        assertFalse(proto.contains("coded.writeString(7"))
    }

    @Test fun fiveSecondActiveCheckpointIsServiceScoped() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("delay(5_000L)"))
        assertTrue(service.contains("if (player.isPlaying && runtimeError == null)"))
        assertTrue(service.contains("pausedCheckpoint.onPlayingCheckpoint("))
    }

    @Test fun pausedSeekAndQueueChangeSaveImmediately() {
        val checkpoint = source("playback/service/ServicePausedCheckpoint.kt")
        assertTrue(checkpoint.contains("pausedPositionChanged"))
        assertTrue(checkpoint.contains("if (changed || newlyPaused || pausedPositionChanged)"))
        assertTrue(checkpoint.contains("mutex.withLock"))
    }

    @Test fun explicitStopClearsCheckpointRatherThanRestoresIt() {
        val service = source("playback/service/MusicPlaybackService.kt")
        val checkpoint = source("playback/service/ServicePausedCheckpoint.kt")
        assertTrue(service.contains("pausedCheckpoint.onStop()"))
        assertTrue(checkpoint.contains("suppressedByStop = true"))
        assertTrue(checkpoint.contains("store.clear()"))
    }

    @Test fun restoredItemsAreAlwaysRevalidatedAndDurationClamped() {
        val service = source("playback/service/MusicPlaybackService.kt")
        assertTrue(service.contains("queueEngine.revalidate(ref)"))
        assertTrue(service.contains("queueEngine.prepare("))
        assertTrue(service.contains("checkedPosition"))
        assertTrue(service.contains("LocalMedia3QueueItems.from(queued)"))
    }

    @Test fun duplicateSongOccurrencesAreNotDeduplicatedOnDisk() {
        val codec = source("data/datastore/RestoreProtoCodec.kt")
        val policy = source("playback/restore/SafePausedRestorePolicy.kt")
        assertFalse(codec.contains("snapshot.items.distinct().size !="))
        assertFalse(policy.contains("snapshot.items.distinct().size !="))
        assertTrue(policy.contains(".count { it in verifiedMediaRefs }"))
    }
}
