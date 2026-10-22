import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.playback.api.PlaybackCommand
import com.latticeward.musiccopilot.playback.command.SystemTransportPolicy

/** No Android, Media3, personal media, or device emulation. */
fun main() {
    var assertions = 0
    fun expect(value: Boolean) { check(value); assertions++ }
    val key = CommandId("system-test")
    fun command(
        action: SystemTransportPolicy.Action,
        positionMs: Long = 0L,
    ) = SystemTransportPolicy.translate(action, key, positionMs)

    expect(command(SystemTransportPolicy.Action.Play) == PlaybackCommand.Play(key))
    expect(command(SystemTransportPolicy.Action.Pause) == PlaybackCommand.Pause(key))
    expect(command(SystemTransportPolicy.Action.Stop) == PlaybackCommand.Stop(key))
    expect(command(SystemTransportPolicy.Action.Next) == PlaybackCommand.Next(key))
    expect(command(SystemTransportPolicy.Action.Previous) == PlaybackCommand.Previous(key))
    expect(command(SystemTransportPolicy.Action.SeekTo, 10500) == PlaybackCommand.SeekTo(key, 10500))
    expect(command(SystemTransportPolicy.Action.SeekTo, 0) == PlaybackCommand.SeekTo(key, 0))
    expect(command(SystemTransportPolicy.Action.SeekTo, -1) == null)
    expect(command(SystemTransportPolicy.Action.SeekTo, Long.MIN_VALUE) == null)
    expect(command(SystemTransportPolicy.Action.SeekTo, Long.MAX_VALUE) ==
        PlaybackCommand.SeekTo(key, Long.MAX_VALUE))
    expect(command(SystemTransportPolicy.Action.SetQueue) == null)
    expect(command(SystemTransportPolicy.Action.AddMediaItem) == null)
    expect(command(SystemTransportPolicy.Action.SeekOtherQueueItem) == null)
    expect(command(SystemTransportPolicy.Action.Repeat) == null)
    expect(command(SystemTransportPolicy.Action.Shuffle) == null)
    expect(SystemTransportPolicy.translate(
        SystemTransportPolicy.Action.Next, CommandId("other")
    ) == PlaybackCommand.Next(CommandId("other")))

    println("Issue #7 P06: $assertions/$assertions pure system-command checks PASS")
}
