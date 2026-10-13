package com.latticeward.musiccopilot.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.RestorableQueueSnapshot
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/** One app-owned Proto DataStore instance; atomic updateData, no alternate live queue. */
object RestorableQueueStoreFactory {
    fun create(
        context: Context, scope: CoroutineScope,
    ): DataStore<RestorableQueueSnapshot> =
        DataStoreFactory.create(serializer = RestoreProtoCodec, scope = scope) {
            File(context.applicationContext.filesDir, "datastore/restorable_queue.pb")
                .apply { parentFile?.mkdirs() }
        }
}

class ProtoRestorableQueueStore(
    private val store: DataStore<RestorableQueueSnapshot>,
    private val dispatchers: DispatcherProvider,
) : RestorableQueueStore {
    override suspend fun read(): AppResult<RestorableQueueSnapshot?> =
        withContext(dispatchers.io) {
            try {
                val data = store.data.first()
                when (RestoreSnapshotPolicy.validate(data)) {
                    SnapshotValidity.Valid -> AppResult.Success(
                        if (RestoreSnapshotPolicy.isEmpty(data)) null else data
                    )
                    SnapshotValidity.Corrupt -> failure(AppErrorCodes.SNAPSHOT_CORRUPT)
                    SnapshotValidity.FutureSchema -> failure(AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA)
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { failure(AppErrorCodes.SNAPSHOT_CORRUPT) }
        }

    override suspend fun write(snapshot: RestorableQueueSnapshot): AppResult<Unit> =
        withContext(dispatchers.io) {
            when (RestoreSnapshotPolicy.validate(snapshot)) {
                SnapshotValidity.FutureSchema -> failure(AppErrorCodes.SNAPSHOT_FUTURE_SCHEMA)
                SnapshotValidity.Corrupt -> failure(AppErrorCodes.SNAPSHOT_CORRUPT)
                SnapshotValidity.Valid -> {
                    try {
                        store.updateData { snapshot.copy(items = snapshot.items.toList()) }
                        AppResult.Success(Unit)
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (error: Exception) { failure(AppErrorCodes.STORAGE_WRITE_FAILED) }
                }
            }
        }

    override suspend fun clear(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            store.updateData { RestoreSnapshotPolicy.empty() }
            AppResult.Success(Unit)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (failure: Exception) { failure(AppErrorCodes.STORAGE_WRITE_FAILED) }
    }

    private fun failure(code: String) = AppResult.Failure(
        AppError.Storage(code, Recoverability.RECOVERABLE, RecoveryAction.None)
    )
}

