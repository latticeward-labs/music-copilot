import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.mediastore.LocalPlaybackMediaResolver
import com.latticeward.musiccopilot.data.mediastore.VerifiedLocalMedia
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot
import com.latticeward.musiccopilot.playback.api.PlaybackState
import com.latticeward.musiccopilot.playback.command.PlaybackCommandProcessor
import com.latticeward.musiccopilot.playback.service.ServiceQueueEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/** Pure, synthetic self-check. Never opens user media, Android, Gradle or an APK. */
fun main() = runBlocking {
    var assertions = 0
    fun expect(value: Boolean) { check(value); assertions++ }

    val io = object : DispatcherProvider {
        override val main = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
    }
    val a = MediaRef("audio:1")
    val b = MediaRef("audio:2")
    val c = MediaRef("audio:3")
    class Fake : LocalPlaybackMediaResolver {
        val calls = mutableListOf<MediaRef>()
        var missing = emptySet<MediaRef>()
        var denied = false
        override suspend fun resolve(ref: MediaRef): AppResult<VerifiedLocalMedia> {
            calls += ref
            if (denied) return AppResult.Failure(
                AppError.Permission(
                    AppErrorCodes.PERMISSION_REVOKED,
                    Recoverability.USER_ACTION_REQUIRED,
                    RecoveryAction.OpenSettings,
                )
            )
            if (ref in missing) return AppResult.Failure(
                AppError.MediaItem(
                    AppErrorCodes.MEDIA_ITEM_MISSING,
                    Recoverability.RECOVERABLE,
                    RecoveryAction.Skip,
                )
            )
            return AppResult.Success(
                VerifiedLocalMedia(
                    ref,
                    "content://media/external_primary/audio/media/${ref.value.removePrefix("audio:")}",
                )
            )
        }
        override fun invalidate() {}
    }
    fun replace(items: List<MediaRef>, selected: Int) =
        PlaybackCommand.ReplaceQueue(CommandId("p04-synthetic"), items, selected, 0L, true)

    val fake = Fake()
    val queue = ServiceQueueEngine(fake, io)
    val prepared = queue.prepare(replace(listOf(c, a, b), 1)) as AppResult.Success
    expect(prepared.value.entries.map { it.ref } == listOf(c, a, b))
    expect(prepared.value.selectedIndex == 1)
    expect(fake.calls == listOf(a, c, b, a))
    expect(prepared.value.toString() == "PreparedPlaybackQueue(redacted)")

    fake.calls.clear()
    fake.missing = setOf(c)
    val filtered = queue.prepare(replace(listOf(c, a, b), 1)) as AppResult.Success
    expect(filtered.value.entries.map { it.ref } == listOf(a, b))
    expect(filtered.value.selectedIndex == 0)
    expect(filtered.value.skippedUnavailable == 1)

    fake.calls.clear()
    fake.missing = setOf(a)
    val missing = queue.prepare(replace(listOf(c, a, b), 1)) as AppResult.Failure
    expect(missing.error.code == AppErrorCodes.MEDIA_ITEM_MISSING)
    expect(fake.calls == listOf(a))

    fake.calls.clear()
    fake.missing = emptySet()
    fake.denied = true
    val denied = queue.prepare(replace(listOf(a, b), 0)) as AppResult.Failure
    expect(denied.error.code == AppErrorCodes.PERMISSION_REVOKED)
    fake.denied = false

    val invalid = queue.prepare(replace(emptyList(), 0)) as AppResult.Failure
    expect(invalid.error.code == AppErrorCodes.PLAYBACK_REJECTED)

    val max = queue.prepare(replace(List(5_000) { MediaRef("audio:$it") }, 4_999))
        as AppResult.Success
    expect(max.value.entries.size == 5_000)
    expect(max.value.selectedIndex == 4_999)
    expect((queue.prepare(replace(List(5_001) { b }, 0)) as AppResult.Failure)
        .error.code == AppErrorCodes.PLAYBACK_REJECTED)

    val workerJob = SupervisorJob()
    val release = CompletableDeferred<Unit>()
    var executed = 0
    val worker = PlaybackCommandProcessor(CoroutineScope(workerJob + Dispatchers.Unconfined)) {
        executed++
        if (executed == 1) release.await()
        AppResult.Success(
            PlaybackSnapshot(
                revision = executed.toLong(), queueRevision = 0L,
                playbackState = PlaybackState.Paused,
                queue = listOf(a), currentIndex = 0, currentItem = a,
                positionMs = 0L, durationMs = 100L,
                canSeek = true, canSkipPrevious = false, canSkipNext = false,
            )
        )
    }
    try {
        val action = PlaybackCommand.Pause(CommandId("p04-retry"))
        val first = worker.submit(action)
        expect(first === worker.submit(action.copy()))
        val conflicting = worker.submit(PlaybackCommand.Play(action.commandId))
        expect((conflicting.await() as AppResult.Failure).error.code ==
            AppErrorCodes.PLAYBACK_REJECTED)
        release.complete(Unit)
        expect(first.await() is AppResult.Success)
        expect(executed == 1)
        expect(worker.submit(action).await() is AppResult.Success)
        expect(executed == 1)
    } finally {
        worker.close()
        workerJob.cancel()
    }
    println("Issue #7 P04: $assertions pure Kotlin assertions PASS (no Android/Media3)")
}
