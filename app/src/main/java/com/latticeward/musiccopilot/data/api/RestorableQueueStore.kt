package com.latticeward.musiccopilot.data.api

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppResult

/** P1, non-authoritative: Playback validates refs/schema and cold-restores Paused only. */
data class RestorableQueueSnapshot(
    val schemaVersion: Int,
    val queueRevision: Long,
    val items: List<MediaRef>,
    val currentIndex: Int,
    val positionMs: Long,
    val capturedAtEpochMs: Long,
)

interface RestorableQueueStore {
    suspend fun read(): AppResult<RestorableQueueSnapshot?>
    suspend fun write(snapshot: RestorableQueueSnapshot): AppResult<Unit>
    suspend fun clear(): AppResult<Unit>
}
