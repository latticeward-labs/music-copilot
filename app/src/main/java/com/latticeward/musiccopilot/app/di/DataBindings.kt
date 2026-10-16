package com.latticeward.musiccopilot.app.di

import android.content.Context
import androidx.room.Room
import com.latticeward.musiccopilot.core.dispatchers.DispatcherProvider
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import com.latticeward.musiccopilot.data.datastore.DataStorePreferencesRepository
import com.latticeward.musiccopilot.data.datastore.PreferencesStoreFactory
import com.latticeward.musiccopilot.data.datastore.ProtoRestorableQueueStore
import com.latticeward.musiccopilot.data.datastore.RestorableQueueStoreFactory
import com.latticeward.musiccopilot.data.mediastore.MediaStoreScanner
import com.latticeward.musiccopilot.data.repository.FoundationLibraryRepository
import com.latticeward.musiccopilot.data.repository.FoundationPreferencesRepository
import com.latticeward.musiccopilot.data.repository.FoundationRestorableQueueStore
import com.latticeward.musiccopilot.data.repository.RoomLibraryRepository
import com.latticeward.musiccopilot.data.room.RoomLibraryProjection
import com.latticeward.musiccopilot.data.room.MusicCopilotDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

/** One production graph for each process. The explicit null seam supports JVM tests only. */
internal object DataBindings {
    class DataComponents(
        val library: LibraryRepository,
        val preferences: PreferencesRepository,
        val restorableQueue: RestorableQueueStore,
        private val database: MusicCopilotDatabase? = null,
    ) : AutoCloseable {
        /** Open SQLite with Room migration validation, without destructive fallback. */
        suspend fun verifyStorage(dispatchers: DispatcherProvider): Boolean =
            withContext(dispatchers.io) {
                if (database == null) return@withContext false
                try {
                    database.openHelper.readableDatabase
                    true
                } catch (failure: Exception) {
                    false
                }
            }

        override fun close() { database?.close() }
    }

    fun create(
        context: Context?,
        scope: CoroutineScope,
        dispatchers: DispatcherProvider,
    ): DataComponents {
        if (context == null) return DataComponents(
            FoundationLibraryRepository(scope),
            FoundationPreferencesRepository(scope),
            FoundationRestorableQueueStore(scope)
        )
        val app = context.applicationContext
        val database = Room.databaseBuilder(
            app, MusicCopilotDatabase::class.java, "musiccopilot_projection.db"
        ).build() // No destructive migration fallback for future user-owned records.
        val library = RoomLibraryRepository(
            source = MediaStoreScanner(app, dispatchers),
            projection = RoomLibraryProjection(database), dispatchers = dispatchers, repositoryScope = scope
        )
        val preferences = DataStorePreferencesRepository(
            PreferencesStoreFactory.create(app, scope), dispatchers, scope
        )
        val restore = ProtoRestorableQueueStore(
            RestorableQueueStoreFactory.create(app, scope), dispatchers
        )
        return DataComponents(library, preferences, restore, database)
    }
}
