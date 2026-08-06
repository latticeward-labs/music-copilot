package com.latticeward.musiccopilot.spike

import android.content.ComponentName
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.spike.playback.SpikePlaybackService
import java.util.concurrent.TimeUnit
import java.util.concurrent.FutureTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServiceOwnershipTest {
    @Test
    fun twoControllersObserveOneServiceOwnedQueue() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val uri = SyntheticMedia.insertSilentWav(context)
        val token = SessionToken(context, ComponentName(context, SpikePlaybackService::class.java))
        val firstFuture = MediaController.Builder(context, token).buildAsync()
        val secondFuture = MediaController.Builder(context, token).buildAsync()
        try {
            val first = firstFuture.get(15, TimeUnit.SECONDS)
            val second = secondFuture.get(15, TimeUnit.SECONDS)
            onMain {
                first.setMediaItem(MediaItem.Builder().setMediaId("synthetic-1").setUri(uri).build())
                first.prepare()
            }
            waitUntil(15_000) {
                onMain { first.playbackState != Player.STATE_IDLE && second.mediaItemCount == 1 }
            }

            onMain {
                assertEquals(1, first.mediaItemCount)
                assertEquals(1, second.mediaItemCount)
                assertEquals("synthetic-1", second.currentMediaItem?.mediaId)
                first.pause()
                assertTrue(!first.playWhenReady)
            }
        } finally {
            MediaController.releaseFuture(firstFuture)
            MediaController.releaseFuture(secondFuture)
            SyntheticMedia.delete(context, uri)
        }
    }

    private fun waitUntil(timeoutMs: Long, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (!condition() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100)
        }
        assertTrue("Timed out waiting for controller state", condition())
    }

    private fun <T> onMain(block: () -> T): T {
        val task = FutureTask(block)
        InstrumentationRegistry.getInstrumentation().runOnMainSync(task)
        return task.get(15, TimeUnit.SECONDS)
    }
}
