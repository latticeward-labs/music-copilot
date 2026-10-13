package com.latticeward.musiccopilot.playback.media3

/**
 * Instance-local, single-start ownership. Only MediaSessionService owns this
 * factory; it never enters the application DI graph or UI lifecycle.
 */
class ServicePlaybackRuntimeOwner<T : Any>(
    private val create: () -> T,
    private val dispose: (T) -> Unit,
) : AutoCloseable {
    private var runtime: T? = null
    private var state = State.New

    fun start(): T {
        check(state == State.New) { "Playback runtime can only be created once" }
        val created = create()
        runtime = created
        state = State.Started
        return created
    }

    override fun close() {
        if (state == State.Closed) return
        state = State.Closed
        val last = runtime
        runtime = null
        if (last != null) dispose(last)
    }

    private enum class State { New, Started, Closed }
}
