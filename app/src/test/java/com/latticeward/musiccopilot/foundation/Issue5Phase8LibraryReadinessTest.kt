package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.application.library.LibraryReadinessResolver
import com.latticeward.musiccopilot.core.application.library.LibraryScanOutcome
import com.latticeward.musiccopilot.core.application.library.LibraryScanReason
import com.latticeward.musiccopilot.core.application.library.LibraryScanUseCase
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper
import com.latticeward.musiccopilot.data.mediastore.LocalAudioSource
import com.latticeward.musiccopilot.data.mediastore.MediaStoreRefCodec
import com.latticeward.musiccopilot.data.mediastore.ScannedAudio
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.LibraryProjection
import com.latticeward.musiccopilot.data.room.LibraryRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** P08 exercises real reconciliation logic with fake platform media and fake Room. */
class Issue5Phase8LibraryReadinessTest {
    private val alpha = ScannedAudio(MediaStoreRefCodec.encode(101), "Synthetic Alpha", "A", null, 1000)
    private val beta = ScannedAudio(MediaStoreRefCodec.encode(202), "Synthetic Beta", "B", null, 2000)

    private class FakeMedia : LocalAudioSource {
        var permission = false
        var items: List<ScannedAudio> = emptyList()
        var scanCalls = 0
        var failScan = false
        var suspendScan: (suspend () -> Unit)? = null
        override fun hasAccess() = permission
        override suspend fun scan(): List<ScannedAudio> {
            scanCalls++
            if (!permission) throw SecurityException("synthetic revoke")
            suspendScan?.invoke()
            if (failScan) throw IllegalStateException("synthetic MediaStore failure")
            return items
        }
        override suspend fun requery(ref: MediaRef): ScannedAudio? {
            if (!permission) throw SecurityException("synthetic revoke")
            return items.firstOrNull { it.ref == ref }
        }
    }

    private class FakeRoom : LibraryProjection {
        var rows = emptyList<LibraryRow>()
        var replaceCalls = 0
        var failWrite = false
        private val stream = MutableStateFlow(rows)
        override fun observeAll(): Flow<List<LibraryRow>> = stream
        override suspend fun all(): List<LibraryRow> = rows
        override suspend fun replaceAll(rows: List<LibraryRow>) {
            if (failWrite) throw IllegalStateException("synthetic Room failure")
            this.rows = rows
            replaceCalls++
            stream.value = rows
        }
    }

    private fun repository(media: FakeMedia, room: FakeRoom) =
        RoomLibraryRepository(media, room, TestDispatcherProvider(),
            CoroutineScope(SupervisorJob()))

    @Test fun deniedPermissionNeverInvokesMediaStoreAndNeverPublishesEmpty() = runBlocking {
        val media = FakeMedia()
        val room = FakeRoom()
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertEquals(LibraryScanOutcome.PermissionUnavailable,
            gate.execute(LibraryScanReason.AfterMediaGrant))
        assertEquals(0, media.scanCalls)
        assertEquals(0, room.replaceCalls)
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
    }

    @Test fun explicitGrantPerformsExactlyOneScanAndNoResumeTriggeredScan() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val room = FakeRoom()
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertEquals(0, media.scanCalls)
        assertTrue(gate.execute(LibraryScanReason.AfterMediaGrant) is LibraryScanOutcome.Completed)
        assertEquals(LibraryScanOutcome.AlreadyCurrentOrIneligible,
            gate.execute(LibraryScanReason.AfterMediaGrant))
        assertEquals(1, media.scanCalls)
        assertEquals(1, room.replaceCalls)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(1, repo.index.value.itemCount)
    }

    @Test fun returningUserWithGrantedAccessCanStartExplicitColdStartScan() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val repo = repository(media, FakeRoom())
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertEquals(0, media.scanCalls) // cold start/foreground is never an implicit scan
        assertTrue(gate.execute(LibraryScanReason.ExplicitRescan) is LibraryScanOutcome.Completed)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(1, media.scanCalls)
    }

    @Test fun zeroScanResultsOnlyAfterGrantAreVerifiedEmpty() = runBlocking {
        val media = FakeMedia().apply { permission = true }
        val repo = repository(media, FakeRoom())
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertTrue(gate.execute(LibraryScanReason.AfterMediaGrant) is LibraryScanOutcome.Completed)
        assertEquals(LibraryIndexStatus.Empty, repo.index.value.status)
        assertEquals(1L, repo.index.value.revision)
        assertEquals(0, repo.index.value.itemCount)
    }

    @Test fun scannerFailureIsRecoverableAndManualRetrySucceeds() = runBlocking {
        val media = FakeMedia().apply { permission = true; failScan = true; items = listOf(alpha) }
        val repo = repository(media, FakeRoom())
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertEquals(LibraryScanOutcome.RecoverableFailure,
            gate.execute(LibraryScanReason.AfterMediaGrant))
        assertEquals(LibraryIndexStatus.RecoverableError, repo.index.value.status)
        assertEquals(1, media.scanCalls)
        media.failScan = false
        assertTrue(gate.execute(LibraryScanReason.RetryRecoverableError) is LibraryScanOutcome.Completed)
        assertEquals(2, media.scanCalls)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(LibraryScanOutcome.AlreadyCurrentOrIneligible,
            gate.execute(LibraryScanReason.RetryRecoverableError))
    }

    @Test fun roomWriteFailureNeverPublishesReadyOrErasesKnownRows() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val room = FakeRoom().apply { failWrite = true }
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        assertEquals(LibraryScanOutcome.RecoverableFailure,
            gate.execute(LibraryScanReason.AfterMediaGrant))
        assertEquals(LibraryIndexStatus.RecoverableError, repo.index.value.status)
        assertEquals(0, room.replaceCalls)
        room.failWrite = false
        assertTrue(gate.execute(LibraryScanReason.RetryRecoverableError) is LibraryScanOutcome.Completed)
        assertEquals(1, room.rows.size)
    }

    @Test fun revokeHidesCacheAndDoesNotDestroyAnyPhysicalOrDerivedUserData() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val room = FakeRoom()
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        gate.execute(LibraryScanReason.AfterMediaGrant)
        media.permission = false
        assertEquals(LibraryScanOutcome.PermissionUnavailable,
            gate.execute(LibraryScanReason.ExplicitRescan))
        assertEquals(1, media.scanCalls)
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertTrue(repo.observeLibrary().first().isEmpty())
        assertEquals(1, room.rows.size)
        assertEquals(1, media.items.size)
        assertTrue(repo.get(alpha.ref) is AppResult.Failure)
    }

    @Test fun explicitRescanRemovesMovedOrDeletedRecordsFromDerivedRoomOnly() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha, beta) }
        val room = FakeRoom()
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        gate.execute(LibraryScanReason.AfterMediaGrant)
        media.items = listOf(beta)
        val result = gate.execute(LibraryScanReason.ExplicitRescan)
        assertTrue(result is LibraryScanOutcome.Completed)
        assertEquals(1, (result as LibraryScanOutcome.Completed).result.removedStale)
        assertEquals(listOf(beta.ref.value), room.rows.map { it.mediaRef })
        assertTrue(repo.get(alpha.ref) is AppResult.Failure)
        assertEquals(2, media.scanCalls)
        assertEquals(1, media.items.size)
    }

    @Test fun regrantRequiresExplicitScanBeforeOldCachedRowsCanBeSeenAgain() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val repo = repository(media, FakeRoom())
        val gate = LibraryScanUseCase(repo) { media.permission }
        gate.execute(LibraryScanReason.AfterMediaGrant)
        media.permission = false
        gate.execute(LibraryScanReason.ExplicitRescan)
        media.permission = true
        assertTrue(repo.observeLibrary().first().isEmpty())
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertTrue(gate.execute(LibraryScanReason.AfterMediaGrant) is LibraryScanOutcome.Completed)
        assertEquals(LibraryIndexStatus.Ready, repo.index.value.status)
        assertEquals(2, media.scanCalls)
    }

    @Test fun resolverDoesNotConfuseDeniedWithEmptyOrStaleReady() {
        val fakeReady = LibraryIndexSnapshot(4, LibraryIndexStatus.Ready, 3)
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(false, fakeReady))
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(false,
                LibraryIndexSnapshot(2, LibraryIndexStatus.Empty, 0)))
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(true,
                LibraryIndexSnapshot(0, LibraryIndexStatus.Empty, 0)))
        assertEquals(LibraryIndexStatus.Unavailable,
            LibraryReadinessResolver.verifiedStatus(true,
                LibraryIndexSnapshot(2, LibraryIndexStatus.Ready, 0)))
        assertEquals(LibraryIndexStatus.Ready,
            LibraryReadinessResolver.verifiedStatus(true, fakeReady))
        assertEquals(LibraryIndexStatus.Empty,
            LibraryReadinessResolver.verifiedStatus(true,
                LibraryIndexSnapshot(2, LibraryIndexStatus.Empty, 0)))
        assertEquals(LibraryIndexStatus.Scanning,
            LibraryReadinessResolver.verifiedStatus(true,
                LibraryIndexSnapshot(2, LibraryIndexStatus.Scanning, 0)))
        assertEquals(LibraryIndexStatus.RecoverableError,
            LibraryReadinessResolver.verifiedStatus(true,
                LibraryIndexSnapshot(2, LibraryIndexStatus.RecoverableError, 0)))
    }

    @Test fun revokeDuringInFlightScanNeverPublishesStaleReady() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val entered = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
        media.suspendScan = { entered.complete(Unit); resume.await() }
        val room = FakeRoom()
        val repo = repository(media, room)
        val gate = LibraryScanUseCase(repo) { media.permission }
        val attempt = async { gate.execute(LibraryScanReason.AfterMediaGrant) }
        entered.await()
        assertEquals(LibraryIndexStatus.Scanning, repo.index.value.status)
        media.permission = false
        repo.invalidateMediaAccess()
        resume.complete(Unit)
        assertEquals(LibraryScanOutcome.PermissionUnavailable, attempt.await())
        assertEquals(LibraryIndexStatus.Unavailable, repo.index.value.status)
        assertEquals(0, room.rows.size)
        assertEquals(1, media.scanCalls)
    }

    @Test fun duplicateAfterGrantCallbacksAreSerializedAndDoNotDoubleScan() = runBlocking {
        val media = FakeMedia().apply { permission = true; items = listOf(alpha) }
        val repo = repository(media, FakeRoom())
        val gate = LibraryScanUseCase(repo) { media.permission }
        val first = async { gate.execute(LibraryScanReason.AfterMediaGrant) }
        val duplicate = async { gate.execute(LibraryScanReason.AfterMediaGrant) }
        val result = listOf(first.await(), duplicate.await())
        assertEquals(1, result.count { it is LibraryScanOutcome.Completed })
        assertEquals(1, result.count { it == LibraryScanOutcome.AlreadyCurrentOrIneligible })
        assertEquals(1, media.scanCalls)
    }

    @Test fun noUserScanActionMeansNoMediaMutationOrFileDeletion() {
        val current = java.io.File(System.getProperty("user.dir"))
        val app = if (java.io.File(current, "src/main/java").isDirectory) current
            else java.io.File(current, "app")
        val source = java.io.File(app, "src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt").readText()
        val repository = java.io.File(app, "src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt").readText()
        val adapter = java.io.File(app, "src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt").readText()
        assertTrue(source.contains("throw IllegalStateException(\"MediaStore query returned no cursor\")"))
        assertFalse(source.contains("resolver.delete("))
        assertTrue(repository.contains("projection.replaceAll(rows)"))
        assertFalse(repository.contains("projection.replaceAll(emptyList())"))
        assertFalse(adapter.contains("fun onForeground() {\n        libraryRepository.reconcile()"))
    }
}
