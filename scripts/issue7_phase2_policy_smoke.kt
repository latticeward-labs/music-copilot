import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.PlaybackQueuePolicy as Queue
import com.latticeward.musiccopilot.playback.command.PlaybackNavigationPolicy as Nav

/** Offline, non-Android P02 contract smoke; NOT equivalent to Gradle JUnit. */
fun main() {
    val a = MediaRef("audio:1")
    val b = MediaRef("audio:2")
    val c = MediaRef("audio:3")
    val valid = Queue.validate(
        PlaybackCommand.ReplaceQueue(CommandId("p02"), listOf(a, b, a, c), 2, 500L, true)
    ) as Queue.Decision.Accepted

    check(valid.items == listOf(a, b, a, c))
    check(valid.selectedIndex == 2)
    check(valid.playWhenReady)
    val filtered = Queue.retainPlayable(valid, listOf(false, true, true, true))
        as Queue.Decision.Accepted
    check(filtered.items == listOf(b, a, c))
    check(filtered.selectedIndex == 1)
    check(Queue.retainPlayable(valid, listOf(true, true, false, true)) ==
        Queue.Decision.Rejected(Queue.Rejection.SelectedItemUnavailable))
    check(Queue.next(2, 3) == Queue.NextDecision.EndOfQueue)
    check(Queue.next(null, 3) == Queue.NextDecision.NoTrack)
    check(Queue.next(0, 3) == Queue.NextDecision.Advance(1))
    check(Nav.previous(3_000L, true) == Nav.PreviousDecision.RestartCurrent)
    check(Nav.previous(2_999L, true) == Nav.PreviousDecision.MoveToPrevious)
    check(Nav.previous(0L, false) == Nav.PreviousDecision.RestartCurrent)
    check(Nav.seekTo(-10L, 100L, true) == 0L)
    check(Nav.seekBy(Long.MAX_VALUE, Long.MAX_VALUE, 100L, true) == 100L)
    check(Nav.seekBy(4L, Long.MIN_VALUE, 100L, true) == 0L)
    check(Nav.seekBy(10L, 20L, null, true) == null)
    check(Nav.seekBack(3_000L, null, true) == 0L)
    println("P02 pure-policy source smoke: PASS (17 check assertions); not Android/Gradle/JUnit")
}
