package com.latticeward.musiccopilot.core.application.library

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * One process-owned explicit library scan at a time.
 *
 * Distinct from the LibraryScanUseCase Mutex: duplicate rapid UI callbacks
 * must NOT enqueue a second rescan that runs immediately after the first.
 * Retained in the application graph across Activity re-creations. No implicit
 * foreground/startup scan and no permission/Room ownership live here.
 */
class LibraryScanLaunchGate(private val scope: CoroutineScope) {
    private val lock = Any()
    private var inFlight: Job? = null

    /** Returns false when an explicit request was coalesced. */
    fun launchExplicit(block: suspend () -> Unit): Boolean {
        val launched = synchronized(lock) {
            if (inFlight?.isCompleted == false) return false
            // LAZY ensures registration precedes execution even on an
            // Unconfined test dispatcher that runs launch inline.
            scope.launch(start = CoroutineStart.LAZY) { block() }
                .also { inFlight = it }
        }
        launched.invokeOnCompletion {
            synchronized(lock) {
                if (inFlight === launched) inFlight = null
            }
        }
        launched.start()
        return true
    }

    /** Revocation discards the old lease so a later explicit grant can retry. */
    fun cancelInFlight() {
        val cancelled = synchronized(lock) {
            val old = inFlight
            inFlight = null
            old
        }
        cancelled?.cancel()
    }

    fun isScanInFlight(): Boolean = synchronized(lock) {
        inFlight?.isCompleted == false
    }
}
