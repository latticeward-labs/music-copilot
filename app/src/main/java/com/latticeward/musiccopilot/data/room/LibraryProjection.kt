package com.latticeward.musiccopilot.data.room

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

/** Rebuildable projection seam; existence is always checked via LocalAudioSource. */
interface LibraryProjection {
    fun observeAll(): Flow<List<LibraryRow>>
    suspend fun all(): List<LibraryRow>
    suspend fun replaceAll(rows: List<LibraryRow>)
}

class RoomLibraryProjection(private val database: MusicCopilotDatabase) : LibraryProjection {
    private val dao get() = database.libraryDao()
    override fun observeAll(): Flow<List<LibraryRow>> = dao.observeAll()
    override suspend fun all(): List<LibraryRow> = dao.all()
    override suspend fun replaceAll(rows: List<LibraryRow>) {
        database.withTransaction {
            dao.clearDerived()
            dao.upsert(rows)
        }
    }
}
