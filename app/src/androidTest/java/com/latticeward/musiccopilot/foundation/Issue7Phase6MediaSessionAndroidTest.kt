package com.latticeward.musiccopilot.foundation

import android.content.ComponentName
import android.os.Bundle
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.playback.command.PlaybackWireProtocol
import com.latticeward.musiccopilot.playback.service.MusicPlaybackService
import java.util.concurrent.TimeUnit
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented service/controller contract, MUST be run on a real/emulated
 * Android 10+ device. The test never requests media storage permission, opens
 * a real audio file, installs a catalog or starts audible playback.
 *
 * Distinct external UID and system notification/BT key tests remain T08–T12.
 */
@RunWith(AndroidJUnit4::class)
class Issue7Phase6MediaSessionAndroidTest {
    private fun withController(block: (MediaController) -> Unit) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val token = SessionToken(
            context, ComponentName(context, MusicPlaybackService::class.java)
        )
        val connection = MediaController.Builder(context, token).buildAsync()
        try {
            val controller = connection.get(15, TimeUnit.SECONDS)
            block(controller)
        } finally {
            MediaController.releaseFuture(connection)
        }
    }

    @Test fun serviceControllerHasNoExternalQueueChangeCommands() = withController { controller ->
        assertFalse(controller.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS))
        assertFalse(controller.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE))
        assertFalse(controller.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE))
        assertTrue(controller.isCommandAvailable(Player.COMMAND_PLAY_PAUSE))
    }

    @Test fun privateSnapshotMatchesServiceStateWithoutOptimisticPlayer() =
        withController { controller ->
            val result = controller.sendCustomCommand(
                PlaybackWireProtocol.snapshotCommand(), Bundle.EMPTY,
            ).get(10, TimeUnit.SECONDS)
            assertEquals(SessionResult.RESULT_SUCCESS, result.resultCode)
            val snapshot = PlaybackWireProtocol.decodeSnapshot(
                result.extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
            )
            assertNotNull(snapshot)
            assertEquals(snapshot?.queue.size, controller.mediaItemCount)
            assertEquals(snapshot?.currentIndex, controller.currentMediaItemIndex.takeIf {
                it >= 0 && it < controller.mediaItemCount
            })
        }
}
