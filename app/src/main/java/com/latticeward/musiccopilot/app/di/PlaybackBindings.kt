package com.latticeward.musiccopilot.app.di

import com.latticeward.musiccopilot.playback.controller.PlaybackControllerConnection
import android.content.Context
import kotlinx.coroutines.CoroutineScope

/** Controller connection belongs to the APPLICATION; Player belongs to Service only. */
internal object PlaybackBindings {
    fun controller(scope: CoroutineScope, context: Context?): PlaybackControllerConnection =
        PlaybackControllerConnection(scope, context)
}
