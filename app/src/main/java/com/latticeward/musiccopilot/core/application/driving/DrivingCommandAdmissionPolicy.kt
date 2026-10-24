package com.latticeward.musiccopilot.core.application.driving

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackSnapshot

/**
 * Issue #9 P06: side-effect-free admission at the *instant* of a manual tap.
 *
 * The displayed complete snapshot can trail the independently collected
 * PlaybackGateway StateFlows. Refuse outdated touch targets: e.g. tapping
 * Play on an old Paused card when the service has since reported Playing must
 * never become Pause (or vice versa). No Media3 Player or second queue.
 */
object DrivingCommandAdmissionPolicy {
    fun commandFor(
        displayedConnection: PlaybackConnectionState,
        displayedSnapshot: PlaybackSnapshot,
        latestConnection: PlaybackConnectionState,
        latestSnapshot: PlaybackSnapshot,
        pending: Boolean,
        action: DrivingAction,
        id: CommandId,
    ): PlaybackCommand? {
        if (pending || displayedConnection != latestConnection ||
            latestConnection != PlaybackConnectionState.Ready
        ) return null
        // A stale queue/current item or service transition changes the
        // meaning of Play/Pause and Previous/Next. Fail closed, not optimistic.
        if (displayedSnapshot.revision != latestSnapshot.revision ||
            displayedSnapshot.queueRevision != latestSnapshot.queueRevision ||
            displayedSnapshot.queue != latestSnapshot.queue ||
            displayedSnapshot.lastError != latestSnapshot.lastError ||
            displayedSnapshot.currentIndex != latestSnapshot.currentIndex ||
            displayedSnapshot.currentItem != latestSnapshot.currentItem ||
            displayedSnapshot.playbackState != latestSnapshot.playbackState ||
            displayedSnapshot.canSkipPrevious != latestSnapshot.canSkipPrevious ||
            displayedSnapshot.canSkipNext != latestSnapshot.canSkipNext ||
            displayedSnapshot.canSeek != latestSnapshot.canSeek
        ) return null

        return DrivingPresentationPolicy.commandFor(
            connection = latestConnection,
            snapshot = latestSnapshot,
            pending = false,
            action = action,
            commandId = id,
        )
    }
}
