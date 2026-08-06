package com.latticeward.musiccopilot.spike

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.latticeward.musiccopilot.spike.restore.AtomicSnapshotStore
import com.latticeward.musiccopilot.spike.restore.PausedSnapshot
import com.latticeward.musiccopilot.spike.restore.SnapshotDecodeResult
import com.latticeward.musiccopilot.spike.restore.SnapshotCodec
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AtomicSnapshotStoreTest {
    @Test
    fun atomicWriteReturnsOnlyCompleteSnapshot() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val file = File(context.cacheDir, "snapshot-${UUID.randomUUID()}")
        val store = AtomicSnapshotStore(file)
        val snapshot = PausedSnapshot(
            SnapshotCodec.CURRENT_SCHEMA_VERSION,
            7,
            listOf("content://synthetic/1"),
            0,
            500,
            "PLAYING",
            1,
        )

        store.write(snapshot)
        val result = store.read()

        assertTrue(result is SnapshotDecodeResult.Valid)
        assertEquals(snapshot, (result as SnapshotDecodeResult.Valid).snapshot)
        store.clear()
    }
}
