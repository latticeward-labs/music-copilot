package com.latticeward.musiccopilot.foundation

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Architecture safety gate for the deliberately exported Media3 service.
 * We do not add a manifest bind permission that blocks external head units.
 * The only permitted public behavior is Media3-managed transports; app-private
 * typed commands require both own package and own UID and are checked twice.
 */
class Phase11ExportedMediaSessionSafetyTest {
    private val root = File("src/main")
    private val manifest = File(root, "AndroidManifest.xml").readText()
    private val callbacks = File(root,
        "java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt"
    ).readText()
    private val lint = File("lint.xml").readText()

    @Test fun exactlyOneExportedMedia3ServiceWithApprovedServiceIntent() {
        val services = Regex("<service\\b[^>]*>", RegexOption.DOT_MATCHES_ALL)
            .findAll(manifest).map { it.value }.toList()
        assertEquals(1, services.size)
        assertTrue(services.single().contains("android:exported=\"true\""))
        assertTrue(services.single().contains(
            "android:name=\".playback.service.MusicPlaybackService\""))
        assertTrue(services.single().contains(
            "android:foregroundServiceType=\"mediaPlayback\""))
        assertTrue(manifest.contains("androidx.media3.session.MediaSessionService"))
        assertFalse(services.single().contains("android:permission="))
    }

    @Test fun onlySpecificExportedServiceWarningHasDocumentedLintException() {
        assertTrue(lint.contains("<issue id=\"ExportedService\">"))
        assertTrue(lint.contains("<ignore path=\"src/main/AndroidManifest.xml\" />"))
        assertEquals(1, Regex("<issue\\s+id=").findAll(lint).count())
        assertFalse(lint.contains("severity=\"ignore\""))
        assertFalse(lint.contains("id=\"all\""))
    }

    @Test fun allPrivateCommandsAreCheckedByIdentityNotOnlyDeclaredCapabilities() {
        val check = "controller.packageName == packageName &&"
        assertTrue(callbacks.contains(check))
        assertTrue(callbacks.contains(
            "controller.uid == android.os.Process.myUid()"))
        assertTrue(callbacks.contains("val privateCaller = isPrivateCaller(controller)"))
        assertTrue(callbacks.contains(
            "if (!isPrivateCaller(controller)) {"))
        assertTrue(callbacks.contains("SessionError.ERROR_NOT_SUPPORTED"))
        assertTrue(callbacks.contains("commandBuilder.add(PlaybackWireProtocol.executeCommand())"))
        assertTrue(callbacks.contains("commandBuilder.add(PlaybackWireProtocol.snapshotCommand())"))
    }

    @Test fun noAppPrivateCommandCanBypassServiceCommandWorker() {
        assertTrue(callbacks.contains("processor.submit(command)"))
        assertTrue(callbacks.contains("pendingResults.add(result)"))
        assertFalse(callbacks.contains("GlobalScope"))
    }
}
