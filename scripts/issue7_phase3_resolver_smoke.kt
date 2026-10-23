import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.mediastore.MediaProbeOutcome
import com.latticeward.musiccopilot.data.mediastore.MediaStorePlaybackResolver
import com.latticeward.musiccopilot.data.mediastore.PlaybackMediaProbe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/** Synthetic only. No Android provider, network or personal media. */
fun main() = runBlocking {
    val dispatch = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }
    class Fake : PlaybackMediaProbe {
        var granted = true
        var volumes = setOf("external_primary", "AAAA-1234")
        var probes = 0
        var action: suspend () -> MediaProbeOutcome = { MediaProbeOutcome.Readable }
        override fun hasAccess() = granted
        override suspend fun mountedVolumes() = volumes
        override suspend fun inspect(volumeName: String, mediaId: Long): MediaProbeOutcome {
            probes++
            return action()
        }
    }
    val fake = Fake()
    val resolver = MediaStorePlaybackResolver(fake, dispatch)
    val primary = resolver.resolve(MediaRef("audio:7")) as AppResult.Success
    check(primary.value.contentUri == "content://media/external_primary/audio/media/7")
    check(primary.value.toString() == "VerifiedLocalMedia(redacted)")
    val removable = resolver.resolve(MediaRef("audio:v:AAAA-1234:7")) as AppResult.Success
    check(removable.value.contentUri == "content://media/AAAA-1234/audio/media/7")
    check(primary.value.contentUri != removable.value.contentUri)
    check(fake.probes == 2)
    fun errorCode(result: AppResult<*>) = (result as AppResult.Failure).error.code
    check(errorCode(resolver.resolve(MediaRef("/private/file.mp3"))) ==
        AppErrorCodes.MEDIA_ITEM_MISSING)
    check(fake.probes == 2)
    fake.volumes = setOf("AAAA-1234")
    check(errorCode(resolver.resolve(MediaRef("audio:7"))) == AppErrorCodes.MEDIA_ITEM_MISSING)
    check(fake.probes == 2)
    fake.volumes = setOf("external_primary", "AAAA-1234")
    fake.action = { MediaProbeOutcome.Missing }
    check(errorCode(resolver.resolve(MediaRef("audio:7"))) == AppErrorCodes.MEDIA_ITEM_MISSING)
    fake.action = { MediaProbeOutcome.Unplayable }
    check(errorCode(resolver.resolve(MediaRef("audio:7"))) == AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
    fake.action = { MediaProbeOutcome.ProviderUnavailable }
    check(errorCode(resolver.resolve(MediaRef("audio:7"))) == AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
    fake.granted = false
    check(errorCode(resolver.resolve(MediaRef("audio:7"))) == AppErrorCodes.PERMISSION_REVOKED)
    fake.granted = true
    val before = fake.probes
    fake.action = { MediaProbeOutcome.Readable }
    check(resolver.resolve(MediaRef("audio:7")) is AppResult.Success)
    check(fake.probes == before + 1)
    val entered = CompletableDeferred<Unit>()
    val release = CompletableDeferred<Unit>()
    fake.action = { entered.complete(Unit); release.await(); MediaProbeOutcome.Readable }
    val pending = async(start = CoroutineStart.UNDISPATCHED) {
        resolver.resolve(MediaRef("audio:7"))
    }
    entered.await()
    resolver.invalidate()
    release.complete(Unit)
    check(errorCode(pending.await()) == AppErrorCodes.MEDIA_ITEM_UNPLAYABLE)
    check(errorCode(resolver.resolve(MediaRef("audio:-4"))) == AppErrorCodes.MEDIA_ITEM_MISSING)
    println("Issue #7 P03 synthetic Kotlin resolver smoke: 17 assertions PASS; no Android/Gradle/APK")
}
