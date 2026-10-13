package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.PreferencesRepository
import com.latticeward.musiccopilot.data.api.RestorableQueueStore
import com.latticeward.musiccopilot.data.repository.FoundationLibraryRepository
import com.latticeward.musiccopilot.data.repository.FoundationPreferencesRepository
import com.latticeward.musiccopilot.data.repository.FoundationRestorableQueueStore
import kotlinx.coroutines.CoroutineScope

/** Phase 6 replaces only these bounded repository adapters. */
internal object DataBindings {
    fun library(scope: CoroutineScope): LibraryRepository = FoundationLibraryRepository(scope)
    fun preferences(scope: CoroutineScope): PreferencesRepository =
        FoundationPreferencesRepository(scope)
    fun restorableQueue(scope: CoroutineScope): RestorableQueueStore =
        FoundationRestorableQueueStore(scope)
}

