package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.quality.measurement.ControlledBetaCohortEvidence
import com.latticeward.musiccopilot.quality.measurement.ControlledBetaCohortProjection
import com.latticeward.musiccopilot.quality.measurement.EvidenceCounts
import com.latticeward.musiccopilot.quality.measurement.MeasurementCoverage
import com.latticeward.musiccopilot.quality.measurement.MeasurementEvidenceSource
import com.latticeward.musiccopilot.quality.measurement.MeasurementVerdict
import com.latticeward.musiccopilot.quality.measurement.PilotWindow
import org.junit.Assert.*
import org.junit.Test

/** P10 source-authored SYNTHETIC TEST FIXTURES ONLY — NOT RUN before P14. */
class Issue12Phase10ControlledBetaCohortTest {
    private val qa = MeasurementEvidenceSource.CONTROLLED_BETA_VERIFIED_AGGREGATE
    private val full = MeasurementCoverage(policyApproved = true)

    private fun sample(source: MeasurementEvidenceSource = qa,
                       coverage: MeasurementCoverage = full,
                       enrolled: Long = 50L) = ControlledBetaCohortEvidence(
        window = PilotWindow(1_000_000_000L),
        actualQualifiedEnrolled = enrolled,
        actualDistinctVehicleHeadUnitModels = 3,
        actualDaysObserved = 14,
        week1NorthStarAmongWeek1Active = EvidenceCounts(10, 10, 0),
        week2NorthStarAmongWeek2Active = EvidenceCounts(9, 11, 0),
        returnedInWeek2AmongWeek1Active = EvidenceCounts(6, 14, 0),
        reusedDrivingAmongEnrolled = EvidenceCounts(25, 25, 0),
        source = source, coverage = coverage,
    )

    @Test fun actualPilotT0IsNotInferredAndBothWeeksAreHalfOpen() {
        val t0 = 1_000_000_000L
        val w = PilotWindow(t0)
        assertNull(w.weekForEventAtUtc(t0 - 1))
        assertEquals(1, w.weekForEventAtUtc(t0))
        assertEquals(1, w.weekForEventAtUtc(t0 + PilotWindow.ONE_WEEK_MS - 1))
        assertEquals(2, w.weekForEventAtUtc(t0 + PilotWindow.ONE_WEEK_MS))
        assertEquals(2, w.weekForEventAtUtc(t0 + PilotWindow.TWO_WEEKS_MS - 1))
        assertNull(w.weekForEventAtUtc(t0 + PilotWindow.TWO_WEEKS_MS))
    }

    @Test fun noActualEnrolmentCanBeReplacedByHypotheticalFifty() {
        val report = ControlledBetaCohortProjection.calculate(sample())
        assertTrue(report.betaProtocolEvidenceComplete)
        assertEquals(50L, report.drivingReuse.denominator)
    }

    @Test fun week1NorthStarAndWeek2NorthStarAreSeparate() {
        val report = ControlledBetaCohortProjection.calculate(sample())
        assertEquals(MeasurementVerdict.PASS, report.week1NorthStar.verdict)
        assertEquals(MeasurementVerdict.FAIL, report.week2NorthStar.verdict)
    }

    @Test fun weeklyReturnUsesW1PopulationNotFiftyEnrolled() {
        val report = ControlledBetaCohortProjection.calculate(sample())
        assertEquals(20L, report.weeklyReturn.denominator)
        assertEquals(6L, report.weeklyReturn.numerator)
        assertEquals(MeasurementVerdict.PASS, report.weeklyReturn.verdict)
    }

    @Test fun drivingReuseDenominatorIsEnrolledNotW1Active() {
        val report = ControlledBetaCohortProjection.calculate(sample())
        assertEquals(50L, report.drivingReuse.denominator)
        assertEquals(25L, report.drivingReuse.numerator)
        assertEquals(MeasurementVerdict.PASS, report.drivingReuse.verdict)
    }

    @Test fun unverifiedObservedEventsNeverMakeBetaPass() {
        val report = ControlledBetaCohortProjection.calculate(
            sample(source = MeasurementEvidenceSource.OBSERVED_EVENTS_ONLY))
        assertFalse(report.betaProtocolEvidenceComplete)
        assertEquals(MeasurementVerdict.NOT_VERIFIED, report.drivingReuse.verdict)
    }

    @Test fun consentLossMissingRespondersPreventsBetaProtocolCompletion() {
        val report = ControlledBetaCohortProjection.calculate(
            sample(coverage = full.copy(missingParticipantReports = 2)))
        assertFalse(report.betaProtocolEvidenceComplete)
        assertEquals(MeasurementVerdict.NOT_VERIFIED, report.weeklyReturn.verdict)
    }

    @Test fun fictitiousMissingOrSmallerCohortMustFailStructuralValidation() {
        assertThrows(IllegalArgumentException::class.java) {
            ControlledBetaCohortProjection.calculate(sample(enrolled = 10))
        }
    }

    @Test fun noFakeClockCrossingDayFourteenIntoBetaResults() {
        val w = PilotWindow(0L)
        assertNull(w.weekForEventAtUtc(PilotWindow.TWO_WEEKS_MS))
        assertNull(w.weekForEventAtUtc(-1))
    }
}
