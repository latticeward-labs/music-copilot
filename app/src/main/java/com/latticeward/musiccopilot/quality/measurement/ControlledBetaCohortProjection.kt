package com.latticeward.musiccopilot.quality.measurement

/**
 * P10 controlled 14-day pilot: ONE real T0 in UTC, two anchored half-open
 * seven-day windows. It holds NO person/user IDs, contact, car IDs, track
 * names, local media data or per-user activity trail.
 *
 * A real T0 is NOT chosen in source code. The authorized Beta owner must
 * supply its actual UTC start, enrollment and independently reconciled
 * in-person/local aggregate counts. No arbitrary "50 users" fixture.
 */
data class PilotWindow(val t0UtcEpochMs: Long) {
    init {
        require(t0UtcEpochMs >= 0L && t0UtcEpochMs <= Long.MAX_VALUE - TWO_WEEKS_MS)
    }

    fun weekForEventAtUtc(epochMillis: Long): Int? = when {
        epochMillis < t0UtcEpochMs -> null
        epochMillis < t0UtcEpochMs + ONE_WEEK_MS -> 1
        epochMillis < t0UtcEpochMs + TWO_WEEKS_MS -> 2
        else -> null
    }

    companion object {
        const val ONE_WEEK_MS = 7L * 24L * 60L * 60L * 1000L
        const val TWO_WEEKS_MS = 14L * 24L * 60L * 60L * 1000L
    }
}

/**
 * Each EvidenceCounts is a vetted aggregate of distinct eligible people:
 * yes/no/UNKNOWN, never raw event rows or inferred installation IDs.
 *
 * The weekly active denominator must be truthfully reconciled from the
 * approved Beta-owner roster/participant-week local booleans, not telemetry
 * P09 event counts. Unknown W2 contacts are explicitly unknown.
 */
data class ControlledBetaCohortEvidence(
    val window: PilotWindow,
    val actualQualifiedEnrolled: Long,
    val actualDistinctVehicleHeadUnitModels: Long,
    val actualDaysObserved: Long,
    val week1NorthStarAmongWeek1Active: EvidenceCounts,
    val week2NorthStarAmongWeek2Active: EvidenceCounts,
    val returnedInWeek2AmongWeek1Active: EvidenceCounts,
    val reusedDrivingAmongEnrolled: EvidenceCounts,
    val source: MeasurementEvidenceSource,
    val coverage: MeasurementCoverage,
) {
    init {
        require(actualQualifiedEnrolled >= 0L)
        require(actualDistinctVehicleHeadUnitModels >= 0L)
        require(actualDaysObserved in 0L..14L)
        require(reusedDrivingAmongEnrolled.eligible <= actualQualifiedEnrolled)
        require(returnedInWeek2AmongWeek1Active.eligible <= actualQualifiedEnrolled)
        require(week1NorthStarAmongWeek1Active.eligible <= actualQualifiedEnrolled)
        require(week2NorthStarAmongWeek2Active.eligible <= actualQualifiedEnrolled)
    }
}

data class ControlledBetaCohortReport(
    val week1NorthStar: MetricReport,
    val week2NorthStar: MetricReport,
    val weeklyReturn: MetricReport,
    val drivingReuse: MetricReport,
    val betaProtocolEvidenceComplete: Boolean,
    val cohortSource: MeasurementEvidenceSource,
)

object ControlledBetaCohortProjection {
    fun calculate(data: ControlledBetaCohortEvidence): ControlledBetaCohortReport {
        val a = QualityMetricProjection.ratio(
            "NS01_W1", data.week1NorthStarAmongWeek1Active, 50.0,
            MetricDirection.AT_LEAST, data.source, data.coverage,
            "Distinct W1-active participant with >=1 VERIFIED correct Voice Driving session")
        val b = QualityMetricProjection.ratio(
            "NS01_W2", data.week2NorthStarAmongWeek2Active, 50.0,
            MetricDirection.AT_LEAST, data.source, data.coverage,
            "Distinct W2-active participant with >=1 VERIFIED correct Voice Driving session")
        val r = QualityMetricProjection.ratio(
            "R01", data.returnedInWeek2AmongWeek1Active, 30.0,
            MetricDirection.AT_LEAST, data.source, data.coverage,
            "W1-active participants independently verified active again in W2")
        val reuse = QualityMetricProjection.ratio(
            "R02", data.reusedDrivingAmongEnrolled, 50.0,
            MetricDirection.AT_LEAST, data.source, data.coverage,
            "All eligible enrolled participants, not only weekly actives")
        val eligibleSource = data.source in setOf(
            MeasurementEvidenceSource.QA_VERIFIED_AGGREGATE,
            MeasurementEvidenceSource.CONTROLLED_BETA_VERIFIED_AGGREGATE,
        )
        // Enrollment/vehicle/window compliance is a SEPARATE Beta gate;
        // even full evidence is not automatic release GO or Safety approval.
        val protocolReady = eligibleSource && data.coverage.complete &&
            data.actualQualifiedEnrolled >= 50L &&
            data.actualDistinctVehicleHeadUnitModels >= 3L &&
            data.actualDaysObserved == 14L &&
            data.reusedDrivingAmongEnrolled.eligible == data.actualQualifiedEnrolled &&
            data.returnedInWeek2AmongWeek1Active.eligible > 0
        return ControlledBetaCohortReport(a, b, r, reuse, protocolReady, data.source)
    }
}
