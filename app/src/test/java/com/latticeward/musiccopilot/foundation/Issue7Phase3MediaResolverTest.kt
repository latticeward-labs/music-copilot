package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.mediastore.LocalPlaybackMediaResolver
import com.latticeward.musiccopilot.data.mediastore.MediaProbeOutcome
import com.latticeward.musiccopilot.data.mediastore.MediaStorePlaybackResolver
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.PlaybackMediaProbe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/** No Android framework required: all source access uses synthetic fake probe. */
class Issue7Phase3MediaResolverTest {
    private val dispatchers = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }
    private class FakeProbe : PlaybackMediaProbe {
        var grant = true
        var volumes = setOf("external_primary", "AAAA-1234")
        var probes = 0
        var lastVolume: String? = null
        var lastId: Long? = null
        var inspectBehavior: suspend () -> MediaProbeOutcome = { MediaProbeOutcome.Readable }
        override fun hasAccess() = grant
        override suspend fun mountedVolumes() = volumes
        override suspend fun inspect(volumeName: String, mediaId: Long): MediaProbeOutcome {
            probes++
            lastVolume = volumeName
            lastId = mediaId
            return inspectBehavior()
        }
    }

    private fun verified(result: AppResult<*>): Boolean = result is AppResult.Success<*>
    private fun errorCode(result: AppResult<*>): String =
        (result as AppResult.Failure).error.code

    @Test fun primaryAndRemovableSameIdResolveWithoutCollision() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        val a = resolver.resolve(MediaStoreRefCodec.encode("external_primary", 25L))
            as AppResult.Success
        val b = resolver.resolve(MediaStoreRefCodec.encode("AAAA-1234", 25L))
            as AppResult.Success
        assertEquals("content://media/external_primary/audio/media/25", a.value.contentUri)
        assertEquals("content://media/AAAA-1234/audio/media/25", b.value.contentUri)
        assertNotEquals(a.value.contentUri, b.value.contentUri)
        assertEquals(25L, fake.lastId)
    }

    @Test fun legacyUnscopedRefOnlyResolvesPrimary() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        assertTrue(verified(resolver.resolve(MediaRef("audio:7"))))
        assertEquals("external_primary", fake.lastVolume)
    }

    @Test fun missingPrimaryNeverSilentlyUsesRemovableId() = runBlocking {
        val fake = FakeProbe().apply { volumes = setOf("AAAA-1234") }
        val result = MediaStorePlaybackResolver(fake, dispatchers).resolve(MediaRef("audio:7"))
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING, errorCode(result))
        assertEquals(0, fake.probes)
    }

    @Test fun malformedRefAndRawPathsFailWithoutTouchingProvider() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        for (value in listOf("audio:v:external:3", "content://media/external/audio/media/1",
            "/storage/emulated/0/private.mp3", "audio:v:../evil:3", "audio:-1", "audio:1:2")
        ) assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            errorCode(resolver.resolve(MediaRef(value))))
        assertEquals(0, fake.probes)
    }

    @Test fun permissionDenialBeforeQueryIsTypedAndDoesNotProbe() = runBlocking {
        val fake = FakeProbe().apply { grant = false }
        val result = MediaStorePlaybackResolver(fake, dispatchers).resolve(MediaRef("audio:7"))
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, errorCode(result))
        assertTrue((result as AppResult.Failure).error is AppError.Permission)
        assertEquals(0, fake.probes)
    }

    @Test fun permissionRevokedDuringProbeIsFailClosed() = runBlocking {
        val fake = FakeProbe()
        fake.inspectBehavior = { fake.grant = false; MediaProbeOutcome.Readable }
        val result = MediaStorePlaybackResolver(fake, dispatchers).resolve(MediaRef("audio:7"))
        assertEquals(AppErrorCodes.PERMISSION_REVOKED, errorCode(result))
    }

    @Test fun missingUnplayableProviderUnavailableAreDistinctNonSensitiveOutcomes() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        val cases = listOf(
            MediaProbeOutcome.Missing to AppErrorCodes.MEDIA_ITEM_MISSING,
            MediaProbeOutcome.Unplayable to AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
            MediaProbeOutcome.ProviderUnavailable to AppErrorCodes.MEDIA_ITEM_UNPLAYABLE,
        )
        for ((outcome, expected) in cases) {
            fake.inspectBehavior = { outcome }
            assertEquals(expected, errorCode(resolver.resolve(MediaRef("audio:7"))))
        }
    }

    @Test fun securityAndIoExceptionsAreSanitized() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        fake.inspectBehavior = { throw SecurityException("/private/song.mp3") }
        assertEquals(AppErrorCodes.PERMISSION_REVOKED,
            errorCode(resolver.resolve(MediaRef("audio:7"))))
        fake.inspectBehavior = { throw IOException("/secret/uri") }
        val result = resolver.resolve(MediaRef("audio:7"))
        assertEquals(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE, errorCode(result))
        assertFalse((result as AppResult.Failure).error.code.contains("/"))
    }

    @Test fun noCacheRechecksEachRequestAndRedactsTransportToString() = runBlocking {
        val fake = FakeProbe()
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        val first = resolver.resolve(MediaRef("audio:7")) as AppResult.Success
        assertEquals("VerifiedLocalMedia(redacted)", first.value.toString())
        assertTrue(verified(resolver.resolve(MediaRef("audio:7"))))
        assertEquals(2, fake.probes)
        fake.inspectBehavior = { MediaProbeOutcome.Missing }
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING,
            errorCode(resolver.resolve(MediaRef("audio:7"))))
    }

    @Test fun sourceInvalidationRejectsInFlightReadableResult() = runBlocking {
        val fake = FakeProbe()
        val entered = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
        fake.inspectBehavior = { entered.complete(Unit); resume.await(); MediaProbeOutcome.Readable }
        val resolver: LocalPlaybackMediaResolver = MediaStorePlaybackResolver(fake, dispatchers)
        val pending = async(start = CoroutineStart.UNDISPATCHED) {
            resolver.resolve(MediaRef("audio:7"))
        }
        entered.await()
        resolver.invalidate()
        resume.complete(Unit)
        assertEquals(AppErrorCodes.MEDIA_ITEM_UNPLAYABLE, errorCode(pending.await()))
    }

    @Test fun cancellationPropagatesRatherThanReportingFakeMissingOrEmpty() = runBlocking {
        val fake = FakeProbe()
        val entered = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
        fake.inspectBehavior = { entered.complete(Unit); resume.await(); MediaProbeOutcome.Readable }
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        var cancelled = false
        val pending = launch(start = CoroutineStart.UNDISPATCHED) {
            try { resolver.resolve(MediaRef("audio:7")) }
            catch (expected: CancellationException) { cancelled = true }
        }
        entered.await()
        pending.cancelAndJoin()
        assertTrue(cancelled)
    }

    @Test fun oversizedIdAndUnavailableMountedVolumeCannotProbe() = runBlocking {
        val fake = FakeProbe().apply { volumes = setOf("external_primary") }
        val resolver = MediaStorePlaybackResolver(fake, dispatchers)
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING, errorCode(resolver.resolve(
            MediaRef("audio:v:AAAA-1234:1"))))
        assertEquals(AppErrorCodes.MEDIA_ITEM_MISSING, errorCode(resolver.resolve(
            MediaRef("audio:99999999999999999999999999999"))))
        assertEquals(0, fake.probes)
    }
}
