package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.quality.api.LocalDiagnosticsSummary
import com.latticeward.musiccopilot.quality.measurement.EvidenceCounts
import com.latticeward.musiccopilot.quality.measurement.LocalLatencyHistogram
import com.latticeward.musiccopilot.quality.measurement.LocalManualAckHistogram
import com.latticeward.musiccopilot.quality.measurement.MeasurementCoverage
import com.latticeward.musiccopilot.quality.measurement.MeasurementEvidenceSource
import com.latticeward.musiccopilot.quality.measurement.MeasurementVerdict
import com.latticeward.musiccopilot.quality.measurement.MetricDirection
import com.latticeward.musiccopilot.quality.measurement.P95BucketEvidence
import com.latticeward.musiccopilot.quality.measurement.QualityMetricProjection
import com.latticeward.musiccopilot.quality.measurement.StrictP95Boundary
import org.junit.Assert.*
import org.junit.Test

/**
 * P10 QA source-authored only. NOT COMPILED, NOT RUN until P14.
 * The inputs below are SYNTHETIC test numbers, NOT Music Copilot results.
 */
class Issue12Phase10MetricProjectionTest {
    private val qa = MeasurementEvidenceSource.QA_VERIFIED_AGGREGATE
    private val full = MeasurementCoverage(policyApproved = true)

    private fun ratio(
        id: String, pos: Long, neg: Long, unknown: Long = 0,
        target: Double, direction: MetricDirection = MetricDirection.AT_LEAST,
        coverage: MeasurementCoverage = full,
        source: MeasurementEvidenceSource = qa,
    ) = QualityMetricProjection.ratio(id, EvidenceCounts(pos, neg, unknown),
        target, direction, source, coverage)

    @Test fun successExactly85PercentPassesAnd84_9Fails() {
        assertEquals(MeasurementVerdict.PASS, ratio("Q01", 85, 15, target = 85.0).verdict)
        assertEquals(MeasurementVerdict.FAIL, ratio("Q01", 849, 151, target = 85.0).verdict)
    }

    @Test fun manualCorrectionExactly15PercentFailsStrictLimit() {
        assertEquals(MeasurementVerdict.FAIL,
            ratio("F01", 15, 85, target = 15.0,
                direction = MetricDirection.STRICTLY_LESS_THAN).verdict)
        assertEquals(MeasurementVerdict.PASS,
            ratio("F01", 14, 86, target = 15.0,
                direction = MetricDirection.STRICTLY_LESS_THAN).verdict)
    }

    @Test fun incorrectExecutionExactly3PercentFailsStrictLimit() {
        assertEquals(MeasurementVerdict.FAIL, ratio("G01", 3, 97,
            target = 3.0, direction = MetricDirection.STRICTLY_LESS_THAN).verdict)
        assertEquals(MeasurementVerdict.PASS, ratio("G01", 2, 98,
            target = 3.0, direction = MetricDirection.STRICTLY_LESS_THAN).verdict)
    }

    @Test fun crashFreeExactly99PercentPassesAndOSUnknownDoesNot() {
        assertEquals(MeasurementVerdict.PASS, ratio("S01", 99, 1,
            target = 99.0).verdict)
        assertEquals(MeasurementVerdict.NOT_VERIFIED, ratio("S01", 99, 0,
            unknown = 1, target = 99.0).verdict)
    }

    @Test fun weeklyAndReuseDenominatorsRemainTwoDifferentPopulations() {
        val report = QualityMetricProjection.allRatios(mapOf(
            "NS01" to EvidenceCounts(5, 5, 0),
            "R01" to EvidenceCounts(3, 7, 0),
            "R02" to EvidenceCounts(25, 25, 0),
        ), qa, full)
        assertEquals(10L, report.first { it.id == "NS01" }.denominator)
        assertEquals(10L, report.first { it.id == "R01" }.denominator)
        assertEquals(50L, report.first { it.id == "R02" }.denominator)
        assertEquals(MeasurementVerdict.PASS, report.first { it.id == "R01" }.verdict)
    }

    @Test fun zeroDenominatorNeverMeansZeroErrorOrPerfectQuality() {
        assertEquals(MeasurementVerdict.NOT_MEASURABLE,
            ratio("Q01", 0, 0, target = 85.0).verdict)
        assertNull(ratio("Q01", 0, 0, target = 85.0).observedValue)
    }

    @Test fun unknownAttemptRemainsInDenominatorAndPreventsPass() {
        val p = ratio("Q01", 85, 10, unknown = 5, target = 85.0)
        assertEquals(100L, p.denominator)
        assertEquals(5L, p.unknown)
        assertEquals(MeasurementVerdict.NOT_VERIFIED, p.verdict)
        assertEquals(90.0, p.optimisticUpperBound!!, 0.0)
    }

    @Test fun sourceQueueOverflowMakesEvenGoodResultNotVerified() {
        val incomplete = full.copy(sourceQueueDrops = 1)
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            ratio("Q01", 99, 1, target = 85.0, coverage = incomplete).verdict)
    }

    @Test fun memoryEvictionOrUnreconciledAttemptPreventsPass() {
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            ratio("S01", 100, 0, target = 99.0,
                coverage = full.copy(eventEvictions = 1)).verdict)
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            ratio("G01", 0, 100, target = 3.0,
                direction = MetricDirection.STRICTLY_LESS_THAN,
                coverage = full.copy(unreconciledAttempts = 1)).verdict)
    }

    @Test fun observedTelemetryWithoutUserIntentionIsNeverQaProof() {
        val observed = MeasurementEvidenceSource.OBSERVED_EVENTS_ONLY
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            ratio("Q01", 100, 0, target = 85.0, source = observed).verdict)
    }

    @Test fun syntheticFixtureCannotBecomeProductPass() {
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            ratio("G01", 0, 100, target = 3.0,
                direction = MetricDirection.STRICTLY_LESS_THAN,
                source = MeasurementEvidenceSource.SYNTHETIC_FIXTURE).verdict)
    }

    @Test fun criticalIncidentsArePer100StartedSessionsNotPercentOfAffectedUsers() {
        val p = QualityMetricProjection.criticalPlayerErrorsPer100(
            1, 100, 0, qa, full)
        assertEquals(MeasurementVerdict.FAIL, p.verdict)
        assertEquals(1.0, p.observedValue!!, 0.0)
        assertEquals(MeasurementVerdict.PASS,
            QualityMetricProjection.criticalPlayerErrorsPer100(
                1, 101, 0, qa, full).verdict)
    }

    @Test fun unknownStartedSessionsNeverSupportCriticalErrorGreen() {
        val x = QualityMetricProjection.criticalPlayerErrorsPer100(
            0, 99, 1, qa, full)
        assertEquals(MeasurementVerdict.NOT_VERIFIED, x.verdict)
        assertEquals(100L, x.denominator)
    }

    @Test fun nearestRankP95WorksAtBoundaryAndSmallN() {
        assertEquals(0L, StrictP95Boundary.nearestRank(0))
        assertEquals(1L, StrictP95Boundary.nearestRank(1))
        assertEquals(19L, StrictP95Boundary.nearestRank(20))
        assertEquals(95L, StrictP95Boundary.nearestRank(100))
    }

    @Test fun voiceExactly2500MillisecondsMustNotPassStrictThreshold() {
        assertTrue(StrictP95Boundary.underVoiceThreshold(2499))
        assertFalse(StrictP95Boundary.underVoiceThreshold(2500))
        assertEquals(MeasurementVerdict.FAIL,
            StrictP95Boundary.atThresholdVerdict(
                P95BucketEvidence(0, 1), qa, true))
    }

    @Test fun localExactly1000MillisecondsMustNotPass() {
        assertTrue(StrictP95Boundary.underLocalAckThreshold(999))
        assertFalse(StrictP95Boundary.underLocalAckThreshold(1000))
    }

    @Test fun percentileCanPassWithOneSlowWhenN20ButFailsWhenN19() {
        assertEquals(MeasurementVerdict.PASS,
            StrictP95Boundary.atThresholdVerdict(
                P95BucketEvidence(19, 1), qa, true))
        assertEquals(MeasurementVerdict.FAIL,
            StrictP95Boundary.atThresholdVerdict(
                P95BucketEvidence(18, 1), qa, true))
    }

    @Test fun p95WithUnknownOrUnapprovedSourceRemainsUnverified() {
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            StrictP95Boundary.atThresholdVerdict(
                P95BucketEvidence(20, 0, unknown = 1), qa, true))
        assertEquals(MeasurementVerdict.NOT_VERIFIED,
            StrictP95Boundary.atThresholdVerdict(
                P95BucketEvidence(20, 0),
                MeasurementEvidenceSource.OBSERVED_EVENTS_ONLY, true))
    }

    @Test fun endpointProxyHistogramKeepsNoIndividualTimestamps() {
        val hist = LocalLatencyHistogram()
        listOf(999L, 1000L, 1999L, 2000L, 2499L, 2500L, null, -1L).forEach {
            hist.addVoiceResponse(it)
        }
        val z = hist.snapshot()
        assertEquals(1L, z.under1000Ms)
        assertEquals(2L, z.from1000To1999Ms)
        assertEquals(2L, z.from2000To2499Ms)
        assertEquals(1L, z.atOrAbove2500Ms)
        assertEquals(2L, z.unknown)
        hist.clear()
        assertEquals(0L, hist.snapshot().under1000Ms)
    }

    @Test fun localAckLatencyHistogramTreatsFailureAsUnknown() {
        val hist = LocalManualAckHistogram()
        hist.observe(999)
        hist.observe(1000)
        hist.observe(null)
        val z = hist.evidence()
        assertEquals(1L, z.belowTarget)
        assertEquals(1L, z.atOrAboveTarget)
        assertEquals(1L, z.unknown)
        hist.clear()
        assertEquals(0L, hist.evidence().total)
    }

    @Test fun localOnlyObservedCountNeverConvertsToUserLevelKpi() {
        val z = QualityMetricProjection.observedLocalOnly(LocalDiagnosticsSummary(
            retainedEvents = 3, acceptedSinceClear = 11,
            evictedOldestSinceClear = 8,
            countsByFamilySinceClear = mapOf("command_executed" to 11),
            sourceQueueDropsSinceClear = 2,
        ))
        assertEquals(MeasurementVerdict.NOT_VERIFIED, z.verdict)
        assertEquals(2L, z.droppedBeforeSanitization)
        assertEquals(8L, z.evictedFromRam)
        assertEquals(11L, z.observedEvents)
    }

    @Test fun unconsentedOrUninitializedLocalStoreIsNotMeasurable() {
        assertEquals(MeasurementVerdict.NOT_MEASURABLE,
            QualityMetricProjection.observedLocalOnly(null).verdict)
    }

    @Test fun evidenceNegativeOrInvalidCounterIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            EvidenceCounts(-1, 0, 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MeasurementCoverage(sourceQueueDrops = -1)
        }
    }
}
