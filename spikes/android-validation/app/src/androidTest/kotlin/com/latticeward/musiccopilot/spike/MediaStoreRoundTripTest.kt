package com.latticeward.musiccopilot.spike

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.latticeward.musiccopilot.spike.library.MediaStoreLibrary
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaStoreRoundTripTest {
    @Test
    fun appOwnedSyntheticAudioIsDiscoveredWithoutMetadataLogging() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val uri = SyntheticMedia.insertSilentWav(context)
        try {
            val scan = MediaStoreLibrary(context).scan()

            assertTrue(scan.mediaItems.any { it.localConfiguration?.uri == uri })
            assertTrue(scan.reconciliationInput.any { it.stableReference == uri.toString() })
        } finally {
            SyntheticMedia.delete(context, uri)
        }
    }
}
