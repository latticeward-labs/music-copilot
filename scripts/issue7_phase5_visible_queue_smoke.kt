import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.application.playback.VisiblePlaybackQueuePolicy as Policy

/** Source-checked pure Kotlin fixture: NOT a Media3/Android/JUnit test. */
fun main() {
    val a = MediaRef("audio:1")
    val b = MediaRef("audio:2")
    val c = MediaRef("audio:3")
    val d = MediaRef("audio:4")
    var checks = 0
    fun expect(value: Boolean) { check(value); checks++ }

    expect(Policy.matchesAcknowledgement(listOf(a, b, c), 1, listOf(a, b, c), 1))
    expect(Policy.matchesAcknowledgement(listOf(a, b, c), 1, listOf(b, c), 0))
    expect(Policy.matchesAcknowledgement(listOf(a, b, c, d), 2, listOf(b, c), 1))
    expect(!Policy.matchesAcknowledgement(listOf(a, b, c), 1, listOf(c, b, a), 1))
    expect(!Policy.matchesAcknowledgement(listOf(a, b, c), 1, listOf(a, b, d, c), 1))
    expect(!Policy.matchesAcknowledgement(listOf(a, b), 1, listOf(a, b), null))
    expect(!Policy.matchesAcknowledgement(listOf(a, b), 1, listOf(a, b), 0))
    expect(!Policy.matchesAcknowledgement(listOf(a, b), 1, listOf(a, b), 2))
    expect(!Policy.matchesAcknowledgement(emptyList(), 0, listOf(a), 0))
    expect(!Policy.matchesAcknowledgement(listOf(a), -1, listOf(a), 0))
    expect(!Policy.matchesAcknowledgement(listOf(a), 1, listOf(a), 0))
    expect(Policy.matchesAcknowledgement(listOf(a, b, a, c), 2, listOf(b, a, c), 1))
    expect(!Policy.matchesAcknowledgement(listOf(a, b, a, c), 2, listOf(a, b, c), 0))
    expect(Policy.matchesAcknowledgement(listOf(a, a, b), 1, listOf(a, a, b), 1))
    expect(Policy.matchesAcknowledgement(listOf(a, a, b), 1, listOf(a, b), 0))
    println("Issue7 P05: $checks/$checks pure Kotlin queue-confirmation checks PASS (no Android)")
}
