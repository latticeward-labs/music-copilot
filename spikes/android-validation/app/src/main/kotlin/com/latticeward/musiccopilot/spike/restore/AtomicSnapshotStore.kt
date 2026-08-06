package com.latticeward.musiccopilot.spike.restore

import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AtomicSnapshotStore(file: File) {
    private val atomicFile = AtomicFile(file)

    suspend fun write(snapshot: PausedSnapshot) = withContext(Dispatchers.IO) {
        val bytes = SnapshotCodec.encode(snapshot)
        val output = atomicFile.startWrite()
        try {
            output.write(bytes)
            atomicFile.finishWrite(output)
        } catch (error: Throwable) {
            atomicFile.failWrite(output)
            throw error
        }
    }

    suspend fun read(): SnapshotDecodeResult? = withContext(Dispatchers.IO) {
        if (!atomicFile.baseFile.exists()) return@withContext null
        runCatching { SnapshotCodec.decode(atomicFile.readFully()) }
            .getOrElse { SnapshotDecodeResult.Invalid("read-failed") }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        atomicFile.delete()
    }
}
