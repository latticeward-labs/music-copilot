package com.latticeward.musiccopilot.core.application.playback

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.api.PlaybackConnectionState
import com.latticeward.musiccopilot.playback.api.PlaybackGateway
import com.latticeward.musiccopilot.playback.api.PlaybackState
import java.util.UUID
import kotlinx.coroutines.CancellationException

/** An acknowledgement is NOT automatically proof of audio being audible. */
enum class SelectedPlaybackState { Playing, AcceptedNotPlaying }

data class MediaSelectionAcknowledgement(
    val state: SelectedPlaybackState,
    val snapshotRevision: Long,
)

/**
 * P10 shared Library/Search boundary. MediaStore requery/open-file probe is
 * implemented by LibraryRepository.get(), never by Room or the view layer.
 * The app is not a queue/Player owner: the only command goes to the existing
 * service-owned PlaybackGateway and its acknowledgement is authoritative.
 *
 * Issue #7 P05 sends the currently presented song collection in the
 * exact UI order (not one clicked ref); the P04 service independently rechecks
 * each item's real MediaStore existence before accepting a new queue.
 * The returned acknowledgement does not prove audible sound.
 */
class LibraryMediaSelectionUseCase(
    private val library: LibraryRepository,
    private val gateway: PlaybackGateway,
    private val hasOsMediaAccess: () -> Boolean,
    private val nextCommandId: () -> CommandId =
        { CommandId("library-" + UUID.randomUUID().toString()) },
) {
    suspend fun select(
        ref: MediaRef,
        expectedLibraryRevision: Long,
        visibleSongRefs: List<MediaRef> = listOf(ref),
        selectedIndex: Int = 0,
    ): AppResult<MediaSelectionAcknowledgement> {
        // Do not allow a stale UI click to select an unrelated occurrence.
        // This immutable copy also prevents UI pagination/recomposition from
        // changing the list while the asynchronous MediaStore probe is running.
        val proposed = visibleSongRefs.toList()
        if (proposed.isEmpty() || proposed.size > 5_000 ||
            selectedIndex !in proposed.indices || proposed[selectedIndex] != ref ||
            proposed.any { it.value.isBlank() || it.value.length > 128 }
        ) return rejectedSelection()
        if (!hasOsMediaAccess()) return accessDenied()
        if (!validRevision(expectedLibraryRevision)) return staleSelection()

        // Requery platform truth, open a read-only descriptor and reject
        // missing/unreadable/moved items; do not accept a cached Room row.
        val verified = try {
            library.get(ref)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: SecurityException) {
            return accessDenied()
        } catch (_: Exception) {
            return missingSelection()
        }
        if (verified is AppResult.Failure) return verified
        if (verified !is AppResult.Success || verified.value.ref != ref)
            return missingSelection()

        // Permission and accepted scan revision are checked again immediately
        // before a command. The service must independently validate its queue
        // at the Issue #7 boundary.
        if (!hasOsMediaAccess()) return accessDenied()
        if (!validRevision(expectedLibraryRevision)) return staleSelection()
        if (gateway.connectionState.value != PlaybackConnectionState.Ready)
            return unavailableController()

        val acknowledged = try {
            gateway.execute(PlaybackCommand.ReplaceQueue(
                commandId = nextCommandId(),
                items = proposed,
                startIndex = selectedIndex,
                startPositionMs = 0L,
                playWhenReady = true,
            ))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return unavailableController()
        }
        if (acknowledged is AppResult.Failure) return acknowledged
        val confirmed = (acknowledged as AppResult.Success).value
        if (!hasOsMediaAccess()) return accessDenied()
        if (!validRevision(expectedLibraryRevision)) return staleSelection()

        // Fail closed: even an AppResult.Success with a nonmatching or stale
        // snapshot is NOT an acknowledgement for this exact selection.
        if (!VisiblePlaybackQueuePolicy.matchesAcknowledgement(
                requested = proposed,
                requestedIndex = selectedIndex,
                actual = confirmed.queue,
                actualIndex = confirmed.currentIndex,
            ) || confirmed.currentItem != ref ||
            confirmed.playbackState == PlaybackState.Error ||
            confirmed.playbackState == PlaybackState.Idle ||
            confirmed.playbackState == PlaybackState.Ended
        ) return rejectedSelection()
        return AppResult.Success(MediaSelectionAcknowledgement(
            state = if (confirmed.playbackState == PlaybackState.Playing)
                SelectedPlaybackState.Playing else SelectedPlaybackState.AcceptedNotPlaying,
            snapshotRevision = confirmed.revision,
        ))
    }

    private fun validRevision(expected: Long): Boolean {
        val now = library.index.value
        return expected > 0 && now.revision == expected &&
            now.status == LibraryIndexStatus.Ready && now.itemCount > 0
    }

    private fun accessDenied(): AppResult.Failure = AppResult.Failure(
        AppError.Permission(AppErrorCodes.PERMISSION_REVOKED,
            Recoverability.USER_ACTION_REQUIRED, RecoveryAction.OpenSettings))

    private fun staleSelection(): AppResult.Failure = AppResult.Failure(
        AppError.Library(AppErrorCodes.LIBRARY_BROWSE_STALE,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))

    private fun missingSelection(): AppResult.Failure = AppResult.Failure(
        AppError.MediaItem(AppErrorCodes.MEDIA_ITEM_MISSING,
            Recoverability.RECOVERABLE, RecoveryAction.Skip))

    private fun unavailableController(): AppResult.Failure = AppResult.Failure(
        AppError.Controller(AppErrorCodes.CONTROLLER_DISCONNECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))

    private fun rejectedSelection(): AppResult.Failure = AppResult.Failure(
        AppError.Playback(AppErrorCodes.PLAYBACK_REJECTED,
            Recoverability.RECOVERABLE, RecoveryAction.Retry))
}
