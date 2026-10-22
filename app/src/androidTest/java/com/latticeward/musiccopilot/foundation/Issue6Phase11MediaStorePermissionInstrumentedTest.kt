package com.latticeward.musiccopilot.foundation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.data.mediastore.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith

/**
 * P11 noninvasive Android permission instrumentation. AUTHORED / NOT_RUN.
 * Never reads or logs titles, never mutates OS permission or deletes media.
 */
@RunWith(AndroidJUnit4::class)
class Issue6Phase11MediaStorePermissionInstrumentedTest {
    private val context get() =
        InstrumentationRegistry.getInstrumentation().targetContext
    private val dispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Main
        override val io = Dispatchers.IO
        override val default = Dispatchers.Default
    }

    @Test fun scannerAccessMatchesRealAndroidAudioPermission() {
        val androidPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else Manifest.permission.READ_EXTERNAL_STORAGE
        val expected = ContextCompat.checkSelfPermission(
            context, androidPermission,
        ) == PackageManager.PERMISSION_GRANTED
        assertEquals(expected, MediaStoreScanner(context, dispatchers).hasAccess())
    }

    @Test fun scannerNeverListsMediaWithoutOsAudioGrant() = runBlocking {
        val scanner = MediaStoreScanner(context, dispatchers)
        // This negative-path native test is skipped on devices with existing
        // grants, NOT silently counted as PASS; use a separately denied test
        // profile in P12 and record OS+SDK evidence.
        assumeFalse("Run on a deliberately permission-denied test profile", scanner.hasAccess())
        try {
            scanner.scan()
            fail("Denied MediaStore scanner must fail closed")
        } catch (_: SecurityException) {
            // Expected before any ContentResolver query.
        }
    }
}
