package com.latticeward.musiccopilot.quality.measurement

import com.latticeward.musiccopilot.quality.api.LocalDiagnosticsSummary
import kotlin.math.ceil

/**
 * Issue #12 P10. Product PRD/MVP targets are locked; all input evidence
 * comes from a separately reviewed, controlled QA/Beta aggregate workflow.
 * The eight unlinked event families cannot supply intended action, distinct
 * weekly people, session pairing or crash-free truth.
 *
 * No user/installation/session identifiers, artist/title/voice, precise
 * timestamps or individual attempts are representable in this API.
 */
enum class MeasurementEvidenceSource {
    QA_VERIFIED_AGGREGATE,
    CONTROLLED_BETA_VERIFIED_AGGREGATE,
    OBSERVED_EVENTS_ONLY,
    SYNTHETIC_FIXTURE,
    NOT_PROVIDED
}

enum class MeasurementVerdict {
    PASS, FAIL, NOT_VERIFIED, NOT_MEASURABLE
}
enum class MetricDirection { AT_LEAST, STRICTLY_LESS_THAN }

/**
 * QA validator must count every eligible unit as yes/no/unknown. Excluding
 * no-match, unlabelled, consent withdrawal or missing responders from the
 * denominator makes Quality appear better than it is.
 */
data class EvidenceCounts(
    val verifiedTrue: Long,
    val verifiedFalse: Long,
    val unknown: Long,
) {
    init {
        require(verifiedTrue >= 0 && verifiedFalse >= 0 && unknown >= 0)
        require(verifiedTrue <= Long.MAX_VALUE - verifiedFalse)
        require(verifiedTrue + verifiedFalse <= Long.MAX_VALUE - unknown)
    }
    val eligible: Long get() = verifiedTrue + verifiedFalse + unknown
    val known: Long get() = verifiedTrue + verifiedFalse
}

data class MeasurementCoverage(
    val sourceQueueDrops: Long = 0L,
    val eventEvictions: Long = 0L,
    val missingParticipantReports: Long = 0L,
    val unreconciledAttempts: Long = 0L,
    val policyApproved: Boolean = false,
) {
    init {
        require(sourceQueueDrops >= 0L && eventEvictions >= 0L)
        require(missingParticipantReports >= 0L && unreconciledAttempts >= 0L)
    }
    val complete: Boolean get() = sourceQueueDrops == 0L &&
        eventEvictions == 0L && missingParticipantReports == 0L &&
        unreconciledAttempts == 0L && policyApproved
}

data class MetricReport(
    val id: String,
    val numerator: Long,
    val denominator: Long,
    val unknown: Long,
    val target: Double,
    val direction: MetricDirection,
    /** Percentage for ratios; per-100-started sessions for critical errors. */
    val observedValue: Double?,
    val optimisticUpperBound: Double?,
    val verdict: MeasurementVerdict,
    val source: MeasurementEvidenceSource,
    val note: String,
)

object QualityMetricProjection {
    private val verifiedSources = setOf(
        MeasurementEvidenceSource.QA_VERIFIED_AGGREGATE,
        MeasurementEvidenceSource.CONTROLLED_BETA_VERIFIED_AGGREGATE,
    )

    /**
     * RATIO: answer only when all eligible attempted units have independently
     * confirmed yes/no evidence AND missingness/coverage is reconciled.
     * Exactly 15% correction, 3% wrong execution are FAIL; exactly 99% crash
     * free is PASS. Never round a decimal across a threshold.
     */
    fun ratio(
        id: String,
        evidence: EvidenceCounts,
        targetPercent: Double,
        direction: MetricDirection,
        source: MeasurementEvidenceSource,
        coverage: MeasurementCoverage,
        note: String = "",
    ): MetricReport {
        require(targetPercent in 0.0..100.0)
        val d = evidence.eligible
        val actual = if (d == 0L) null
            else evidence.verifiedTrue.toDouble() * 100.0 / d
        val upper = if (d == 0L) null
            else (evidence.verifiedTrue.toDouble() + evidence.unknown) * 100.0 / d
        val verdict = when {
            d == 0L -> MeasurementVerdict.NOT_MEASURABLE
            source !in verifiedSources || evidence.unknown > 0 ||
                !coverage.complete -> MeasurementVerdict.NOT_VERIFIED
            direction == MetricDirection.AT_LEAST && actual!! >= targetPercent ->
                MeasurementVerdict.PASS
            direction == MetricDirection.STRICTLY_LESS_THAN && actual!! < targetPercent ->
                MeasurementVerdict.PASS
            else -> MeasurementVerdict.FAIL
        }
        return MetricReport(id, evidence.verifiedTrue, d, evidence.unknown,
            targetPercent, direction, actual, upper, verdict, source, note)
    }

    /**
     * S02 is incidents PER 100 started sessions, NOT sessions with an error.
     * The number of distinct verified critical incidents may exceed sessions.
     * A caught nonfatal Media3 error is never automatically a fatal crash.
     */
    fun criticalPlayerErrorsPer100(
        verifiedIncidents: Long,
        verifiedStartedSessions: Long,
        unknownStartedSessions: Long,
        source: MeasurementEvidenceSource,
        coverage: MeasurementCoverage,
    ): MetricReport {
        require(verifiedIncidents >= 0 && verifiedStartedSessions >= 0 &&
            unknownStartedSessions >= 0)
        require(verifiedStartedSessions <= Long.MAX_VALUE - unknownStartedSessions)
        val denominator = verifiedStartedSessions + unknownStartedSessions
        val actual = if (denominator == 0L) null
            else verifiedIncidents.toDouble() * 100.0 / denominator
        val complete = unknownStartedSessions == 0L && coverage.complete
        val verdict = when {
            denominator == 0L -> MeasurementVerdict.NOT_MEASURABLE
            !complete || source !in verifiedSources -> MeasurementVerdict.NOT_VERIFIED
            actual!! < 1.0 -> MeasurementVerdict.PASS
            else -> MeasurementVerdict.FAIL
        }
        return MetricReport("S02", verifiedIncidents, denominator,
            unknownStartedSessions, 1.0, MetricDirection.STRICTLY_LESS_THAN,
            actual, null, verdict, source,
            "Verified critical incident count / ALL eligible started Driving sessions")
    }

    /** Targets are from the original PRD/MVP, NOT sampled telemetry estimates. */
    fun allRatios(
        evidence: Map<String, EvidenceCounts>,
        source: MeasurementEvidenceSource,
        coverage: MeasurementCoverage,
    ): List<MetricReport> {
        val defs = listOf(
            Triple("NS01", 50.0, MetricDirection.AT_LEAST),
            Triple("Q01", 85.0, MetricDirection.AT_LEAST),
            Triple("F01", 15.0, MetricDirection.STRICTLY_LESS_THAN),
            Triple("G01", 3.0, MetricDirection.STRICTLY_LESS_THAN),
            Triple("R01", 30.0, MetricDirection.AT_LEAST),
            Triple("R02", 50.0, MetricDirection.AT_LEAST),
            Triple("S01", 99.0, MetricDirection.AT_LEAST),
        )
        return defs.map { (id, target, direction) ->
            ratio(id, evidence[id] ?: EvidenceCounts(0, 0, 0),
                target, direction, source, coverage,
                when (id) {
                    "NS01" -> "Distinct weekly active users; not Driving sessions"
                    "Q01" -> "QA-labelled direct attempts INCLUDING no-match/retries"
                    "F01" -> "Explicit correction attribution, NOT ordinary manual touches"
                    "G01" -> "Independently confirmed wrong intended action, not an ACK"
                    "R01" -> "W1 active participants with verified W2 return"
                    "R02" -> "Enrolled Beta participants with >=2 real Driving entries"
                    "S01" -> "All verified started journeys; incomplete/OS-killed are UNKNOWN"
                    else -> ""
                }
            )
        }
    }

    /**
     * P09 has bounded, short-lived counters but no user-week joins, ground
     * truth or complete crash data. Returning those as KPI ratios is INVALID.
     */
    fun observedLocalOnly(summary: LocalDiagnosticsSummary?): LocalQualityCoverage =
        if (summary == null) LocalQualityCoverage(
            observedEvents = 0, familyCounts = emptyMap(),
            droppedBeforeSanitization = 0, evictedFromRam = 0,
            source = MeasurementEvidenceSource.NOT_PROVIDED,
            verdict = MeasurementVerdict.NOT_MEASURABLE,
        ) else LocalQualityCoverage(
            observedEvents = summary.acceptedSinceClear,
            familyCounts = summary.countsByFamilySinceClear.toMap(),
            droppedBeforeSanitization = summary.sourceQueueDropsSinceClear,
            evictedFromRam = summary.evictedOldestSinceClear,
            source = MeasurementEvidenceSource.OBSERVED_EVENTS_ONLY,
            verdict = MeasurementVerdict.NOT_VERIFIED,
        )
}

data class LocalQualityCoverage(
    val observedEvents: Long,
    val familyCounts: Map<String, Long>,
    val droppedBeforeSanitization: Long,
    val evictedFromRam: Long,
    val source: MeasurementEvidenceSource,
    val verdict: MeasurementVerdict,
)

/**
 * P01/P02: nearest-rank P95 is ceil(0.95 * n), 1-based.
 * For an approved histogram with a bucket boundary strictly BEFORE the
 * threshold, no raw individual durations need to be saved or exported.
 */
data class P95BucketEvidence(
    val belowTarget: Long,
    val atOrAboveTarget: Long,
    val unknown: Long = 0L,
) {
    init {
        require(belowTarget >= 0 && atOrAboveTarget >= 0 && unknown >= 0)
        require(belowTarget <= Long.MAX_VALUE - atOrAboveTarget)
        require(belowTarget + atOrAboveTarget <= Long.MAX_VALUE - unknown)
    }
    val measured: Long get() = belowTarget + atOrAboveTarget
    val total: Long get() = measured + unknown
}

object StrictP95Boundary {
    fun nearestRank(measured: Long): Long {
        if (measured <= 0L) return 0L
        // ceil(95*n / 100) using quotient/remainder avoids overflow.
        return (measured / 100L) * 95L +
            ((measured % 100L) * 95L + 99L) / 100L
    }

    fun atThresholdVerdict(
        evidence: P95BucketEvidence,
        source: MeasurementEvidenceSource,
        complete: Boolean,
    ): MeasurementVerdict {
        if (evidence.measured == 0L) return MeasurementVerdict.NOT_MEASURABLE
        if (source !in setOf(MeasurementEvidenceSource.QA_VERIFIED_AGGREGATE,
                MeasurementEvidenceSource.CONTROLLED_BETA_VERIFIED_AGGREGATE) ||
            evidence.unknown != 0L || !complete
        ) return MeasurementVerdict.NOT_VERIFIED
        val rank = nearestRank(evidence.measured)
        return if (rank <= evidence.belowTarget) MeasurementVerdict.PASS
        else MeasurementVerdict.FAIL
    }

    /** Strict <2500ms voice P95, and <1000ms local ACK P95. */
    fun underVoiceThreshold(ms: Long): Boolean = ms in 0L..2499L
    fun underLocalAckThreshold(ms: Long): Boolean = ms in 0L..999L
}

/** In-memory, bounded coarse Quality stats; no per-attempt timings retained. */
class LocalLatencyHistogram {
    private var under1s = 0L
    private var oneToUnder2s = 0L
    private var twoToUnder2500 = 0L
    private var atOrOver2500 = 0L
    private var notMeasured = 0L

    @Synchronized
    fun addVoiceResponse(elapsedMs: Long?) {
        when {
            elapsedMs == null || elapsedMs < 0L -> notMeasured = increment(notMeasured)
            elapsedMs < 1000L -> under1s = increment(under1s)
            elapsedMs < 2000L -> oneToUnder2s = increment(oneToUnder2s)
            elapsedMs < 2500L -> twoToUnder2500 = increment(twoToUnder2500)
            else -> atOrOver2500 = increment(atOrOver2500)
        }
    }

    @Synchronized
    fun snapshot(): VoiceLatencyBucketSnapshot = VoiceLatencyBucketSnapshot(
        under1s, oneToUnder2s, twoToUnder2500, atOrOver2500, notMeasured
    )

    @Synchronized
    fun clear() {
        under1s = 0L
        oneToUnder2s = 0L
        twoToUnder2500 = 0L
        atOrOver2500 = 0L
        notMeasured = 0L
    }

    private fun increment(old: Long): Long =
        if (old == Long.MAX_VALUE) old else old + 1L
}

data class VoiceLatencyBucketSnapshot(
    val under1000Ms: Long,
    val from1000To1999Ms: Long,
    val from2000To2499Ms: Long,
    val atOrAbove2500Ms: Long,
    val unknown: Long,
) {
    fun strictP95Evidence(): P95BucketEvidence = P95BucketEvidence(
        belowTarget = under1000Ms + from1000To1999Ms + from2000To2499Ms,
        atOrAboveTarget = atOrAbove2500Ms,
        unknown = unknown,
    )
}

/**
 * P02 (<1000ms) local-command input→verified service ACK histogram. Scope:
 * visible Player/Driving manually admitted controls only; other OS controls
 * remain NOT OBSERVED. Failed/stale/no ACK are UNKNOWN, never fast successes.
 */
class LocalManualAckHistogram {
    private var below1000 = 0L
    private var atOrAbove1000 = 0L
    private var unknown = 0L

    @Synchronized
    fun observe(elapsedMs: Long?) {
        when {
            elapsedMs == null || elapsedMs < 0L -> unknown = add(unknown)
            elapsedMs < 1000L -> below1000 = add(below1000)
            else -> atOrAbove1000 = add(atOrAbove1000)
        }
    }

    @Synchronized
    fun evidence(): P95BucketEvidence =
        P95BucketEvidence(below1000, atOrAbove1000, unknown)

    @Synchronized
    fun clear() { below1000 = 0; atOrAbove1000 = 0; unknown = 0 }

    private fun add(value: Long) = if (value == Long.MAX_VALUE) value else value + 1
}

/**
 * P10 app-local, consent-gated observational summary. Neither a user/week
 * cohort nor a validated PRD P95: endpoints are uncalibrated provider
 * signals and screen text draw is not hardware-confirmed human perception.
 * Real QA input goes through StrictP95Boundary separately.
 */
data class LocalMeasurementSnapshot(
    val observed: LocalQualityCoverage,
    val voiceEndpointToDrawProxy: VoiceLatencyBucketSnapshot,
    val manualTapToAck: P95BucketEvidence,
    val officialVoiceP95: MeasurementVerdict = MeasurementVerdict.NOT_VERIFIED,
    val officialManualP95: MeasurementVerdict = MeasurementVerdict.NOT_VERIFIED,
)
