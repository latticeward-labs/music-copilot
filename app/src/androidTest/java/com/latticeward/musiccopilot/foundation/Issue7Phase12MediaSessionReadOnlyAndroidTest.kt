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
 * P12 Android-only, READ-ONLY Media3 integration specifications.
 * AUTHORED, NOT_RUN. For P13 technical QA on a controlled test install
 * only, never executed in GitHub Actions under the owner's instruction.
 *
 * No playback commands, media permission prompts, recording, internet,
 * queue writes, personal-music test mutations or notifications are issued.
 */
@RunWith(AndroidJUnit4::class)
class Issue7Phase12MediaSessionReadOnlyAndroidTest {
    private fun withController(block: (MediaController) -> Unit) {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val token = SessionToken(
            ctx, ComponentName(ctx, MusicPlaybackService::class.java)
        )
        val future = MediaController.Builder(ctx, token).buildAsync()
        try {
            val controller = future.get(15, TimeUnit.SECONDS)
            block(controller)
        } finally {
            MediaController.releaseFuture(future)
        }
    }

    @Test fun injectedPlaylistAndRepeatControlsRemainUnavailable() =
        withController { controller ->
            assertFalse(controller.isCommandAvailable(Player.COMMAND_CHANGE_MEDIA_ITEMS))
            assertFalse(controller.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE))
            assertFalse(controller.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE))
            assertTrue(controller.isCommandAvailable(Player.COMMAND_GET_TIMELINE))
        }

    @Test fun refreshedSnapshotIsAuthoritativeAndDoesNotMutateQueue() =
        withController { controller ->
            val beforeIds = (0 until controller.mediaItemCount)
                .map { controller.getMediaItemAt(it).mediaId }
            val result = controller.sendCustomCommand(
                PlaybackWireProtocol.snapshotCommand(), Bundle.EMPTY,
            ).get(15, TimeUnit.SECONDS)
            assertEquals(SessionResult.RESULT_SUCCESS, result.resultCode)
            val snap = PlaybackWireProtocol.decodeSnapshot(
                result.extras.getBundle(PlaybackWireProtocol.KEY_SNAPSHOT)
            )
            assertNotNull(snap)
            assertEquals(beforeIds, snap!!.queue.map { it.value })
            assertEquals(beforeIds,
                (0 until controller.mediaItemCount).map {
                    controller.getMediaItemAt(it).mediaId
                })
        }
}
