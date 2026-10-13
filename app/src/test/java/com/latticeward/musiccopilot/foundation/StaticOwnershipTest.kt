package com.latticeward.musiccopilot.foundation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/** Explicit source-level guard for the Activity/Application seam; Android lifecycle testing is later. */
class StaticOwnershipTest {
    private fun read(path: String): String {
        val file = File("src/main/java/com/latticeward/musiccopilot/$path")
        require(file.exists()) { "Missing production source: $file" }
        return file.readText()
    }

    @Test fun activityNeverConstructsGraphPlayerVoiceOrService() {
        val source = read("app/MainActivity.kt")
        assertFalse(source.contains("ApplicationBindings("))
        assertFalse(source.contains("ExoPlayer.Builder"))
        assertFalse(source.contains("MediaSession.Builder"))
        assertFalse(source.contains("DefaultVoiceSessionCoordinator("))
        assertFalse(source.contains("PlaybackServiceScopeOwner("))
        assertFalse(source.contains("GlobalScope"))
    }

    @Test fun onlyApplicationEntryPointOwnsTheApplicationGraphCreation() {
        val app = read("app/MusicCopilotApplication.kt")
        val root = read("app/di/ApplicationBindings.kt")
        assertEquals(1, Regex("""bindings\s*=\s*ApplicationBindings\(""").findAll(app).count())
        assertFalse(root.contains("PlaybackServiceScopeOwner("))
        assertFalse(root.contains("ExoPlayer.Builder"))
        assertFalse(root.contains("MediaSession.Builder"))
        assertFalse(root.contains("GlobalScope"))
    }
}

