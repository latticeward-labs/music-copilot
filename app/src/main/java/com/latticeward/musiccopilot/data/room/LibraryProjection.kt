package com.latticeward.musiccopilot.data.room

import androidx.room.withTransaction
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import com.latticeward.musiccopilot.data.api.LibraryBrowseCategory
import com.latticeward.musiccopilot.data.api.LibraryBrowseEntry
import com.latticeward.musiccopilot.data.api.LibraryBrowsePage
import com.latticeward.musiccopilot.data.api.LibraryBrowseRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.mapping.LibraryRowMapper

/** Derived Room projection seam; MediaStore still verifies actual existence. */
interface LibraryProjection {
    fun observeAll(): Flow<List<LibraryRow>>
    suspend fun all(): List<LibraryRow>

    /**
     * Legacy rebuild seam kept for pre-P05 JVM fakes/older callers.
     * Never used in the P05 production reconcile path.
     */
    suspend fun replaceAll(rows: List<LibraryRow>)

    /** SQL-backed in production. Existing pre-P06 fake projections may omit it. */
    suspend fun browsePage(
        request: LibraryBrowseRequest,
        acceptedRevision: Long,
    ): LibraryBrowsePage = throw UnsupportedOperationException("browse query unavailable")

    /** SQLite bounded in production. Legacy JVM fake uses a safe in-memory seam. */
    suspend fun searchLocal(needle: String, limit: Int): List<LibraryRow> =
        all().filter {
            it.normalizedTitle.contains(needle) ||
                it.normalizedArtist.contains(needle) ||
                (it.normalizedAlbum?.contains(needle) == true)
        }.take(limit)

    /**
     * Apply only changed rows in one logical transaction.
     * preCommitGuard MUST be checked from inside the transaction, not merely
     * before a coroutine switches to the SQLite dispatcher. It throws when an
     * OS permission/epoch invalidates this scan.
     *
     * Backward compatibility for existing Issue #5 fake projections only:
     * production RoomLibraryProjection always overrides with delta SQL.
     */
    suspend fun applyDelta(
        delta: LibraryReconcileDelta,
        preCommitGuard: () -> Unit,
    ) {
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        if (!delta.changed) return
        val rows = all().associateBy { it.mediaRef }.toMutableMap()
        delta.removedRefs.forEach { rows.remove(it) }
        delta.upserts.forEach { rows[it.mediaRef] = it }
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        replaceAll(rows.values.sortedWith(
            compareBy<LibraryRow> { it.normalizedTitle }.thenBy { it.mediaRef }
        ))
    }
}

class RoomLibraryProjection(private val database: MusicCopilotDatabase) : LibraryProjection {
    private val dao get() = database.libraryDao()

    override fun observeAll(): Flow<List<LibraryRow>> = dao.observeAll()
    override suspend fun all(): List<LibraryRow> = dao.all()


    override suspend fun browsePage(
        request: LibraryBrowseRequest,
        acceptedRevision: Long,
    ): LibraryBrowsePage = database.withTransaction {
        val category = request.category
        val id = request.groupId?.value
        val limit = request.page.limit
        val offset = request.page.offset

        // Keep both the total and page in the SAME SQLite read transaction.
        // The repository gates permission/revision before and after this read.
        val count = when (category) {
            LibraryBrowseCategory.Songs -> dao.songCount()
            LibraryBrowseCategory.Artists ->
                if (id != null) dao.artistSongCount(id) else dao.artistCount()
            LibraryBrowseCategory.Albums ->
                if (id != null) dao.albumSongCount(id) else dao.albumCount()
            LibraryBrowseCategory.Folders ->
                if (id != null) dao.folderSongCount(id) else dao.folderCount()
        }
        val entries: List<LibraryBrowseEntry> = if (id != null ||
            category == LibraryBrowseCategory.Songs
        ) {
            val rows = when (category) {
                LibraryBrowseCategory.Songs -> dao.songPage(limit, offset)
                LibraryBrowseCategory.Artists -> dao.artistSongs(id!!, limit, offset)
                LibraryBrowseCategory.Albums -> dao.albumSongs(id!!, limit, offset)
                LibraryBrowseCategory.Folders -> dao.folderSongs(id!!, limit, offset)
            }
            rows.map { LibraryBrowseEntry.Song(LibraryRowMapper.toItem(it)) }
        } else {
            val groups = when (category) {
                LibraryBrowseCategory.Artists -> dao.artists(limit, offset)
                LibraryBrowseCategory.Albums -> dao.albums(limit, offset)
                LibraryBrowseCategory.Folders -> dao.folders(limit, offset)
                LibraryBrowseCategory.Songs -> emptyList()
            }
            groups.map { item ->
                when (category) {
                    LibraryBrowseCategory.Artists -> LibraryBrowseEntry.Artist(
                        LibraryGroupId(item.groupId), item.label, item.songCount,
                    )
                    LibraryBrowseCategory.Albums -> LibraryBrowseEntry.Album(
                        LibraryGroupId(item.groupId), item.label,
                        item.artistLabel, item.songCount,
                    )
                    LibraryBrowseCategory.Folders -> LibraryBrowseEntry.Folder(
                        LibraryGroupId(item.groupId), item.label, item.songCount,
                    )
                    LibraryBrowseCategory.Songs -> error("unexpected songs group")
                }
            }
        }
        val next = if (offset.toLong() + entries.size.toLong() < count.toLong())
            offset + entries.size else null
        LibraryBrowsePage(
            entries = entries, totalCount = count, revision = acceptedRevision,
            nextOffset = next,
        )
    }

    override suspend fun searchLocal(needle: String, limit: Int): List<LibraryRow> =
        dao.searchLocal(needle, limit)

    override suspend fun replaceAll(rows: List<LibraryRow>) {
        // Historical/internal recovery helper, not called by the repository.
        database.withTransaction {
            dao.clearDerived()
            dao.upsert(rows)
        }
    }

    override suspend fun applyDelta(
        delta: LibraryReconcileDelta,
        preCommitGuard: () -> Unit,
    ) {
        // Only a full scan of all currently readable MediaStore volumes can
        // decide stale deletions. A failed scan never invokes this method.
        currentCoroutineContext().ensureActive()
        preCommitGuard()
        if (!delta.changed) return
        database.withTransaction {
            currentCoroutineContext().ensureActive()
            preCommitGuard()
            // SQLite binding limits vary across versions; batches stay below
            // 999 placeholders. ALL chunks use this SINGLE Room transaction.
            for (chunk in delta.removedRefs.chunked(400)) {
                currentCoroutineContext().ensureActive()
                preCommitGuard()
                dao.deleteByRefs(chunk)
            }
            for (chunk in delta.upserts.chunked(400)) {
                currentCoroutineContext().ensureActive()
                preCommitGuard()
                dao.upsert(chunk)
            }
            currentCoroutineContext().ensureActive()
            preCommitGuard()
        }
    }
}
