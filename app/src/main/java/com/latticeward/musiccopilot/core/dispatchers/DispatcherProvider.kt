package com.latticeward.musiccopilot.core.dispatchers

import kotlinx.coroutines.CoroutineDispatcher

/** Inject all dispatcher choices; UI/Player callbacks use Main.immediate. */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
