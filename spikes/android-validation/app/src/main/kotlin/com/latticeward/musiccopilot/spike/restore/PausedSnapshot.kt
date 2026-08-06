package com.latticeward.musiccopilot.spike.restore

data class PausedSnapshot(
    val schemaVersion: Int,
    val queueRevision: Long,
    val queueReferences: List<String>,
    val currentIndex: Int,
    val positionMs: Long,
    val capturedState: String,
    val savedAtEpochMs: Long,
)

sealed interface SnapshotDecodeResult {
    data class Valid(val snapshot: PausedSnapshot) : SnapshotDecodeResult
    data class Invalid(val reason: String) : SnapshotDecodeResult
}
