package com.latticeward.musiccopilot.spike.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyDataPolicyTest {
    @Test
    fun confirmationIsRequired() {
        assertEquals(
            DeletionPlan.Rejected(DeletionPlan.Reason.CONFIRMATION_REQUIRED),
            PrivacyDataPolicy.plan(ClearRequest(setOf(DataCategory.SHORT_HISTORY), confirmed = false)),
        )
    }

    @Test
    fun protectedCategoriesCannotEnterClearPlan() {
        DataCategory.entries.filterNot { it.clearable }.forEach { protected ->
            assertEquals(
                "$protected must remain protected",
                DeletionPlan.Rejected(DeletionPlan.Reason.PROTECTED_CATEGORY),
                PrivacyDataPolicy.plan(ClearRequest(setOf(protected), confirmed = true)),
            )
        }
    }

    @Test
    fun onlySelectedClearableBoundariesArePlanned() {
        assertEquals(
            DeletionPlan.Approved(setOf(DeletionAction.CLEAR_LOCAL_HISTORY)),
            PrivacyDataPolicy.plan(ClearRequest(setOf(DataCategory.SHORT_HISTORY), confirmed = true)),
        )

        assertEquals(
            DeletionPlan.Approved(
                setOf(
                    DeletionAction.CLEAR_LOCAL_DIAGNOSTICS,
                    DeletionAction.REQUEST_PROVIDER_DIAGNOSTIC_DELETION,
                ),
            ),
            PrivacyDataPolicy.plan(
                ClearRequest(
                    setOf(DataCategory.LOCAL_DIAGNOSTICS, DataCategory.PROVIDER_DIAGNOSTICS),
                    confirmed = true,
                ),
            ),
        )
    }

    @Test
    fun retentionBoundCollectionFailsClosedWithoutApprovedPositiveDuration() {
        assertFalse(PrivacyDataPolicy.collectionEnabled(DataCategory.SHORT_HISTORY, null))
        assertFalse(PrivacyDataPolicy.collectionEnabled(DataCategory.SHORT_HISTORY, 0))
        assertFalse(PrivacyDataPolicy.collectionEnabled(DataCategory.LOCAL_DIAGNOSTICS, -1))
        assertTrue(PrivacyDataPolicy.collectionEnabled(DataCategory.SHORT_HISTORY, 1))
        assertTrue(PrivacyDataPolicy.collectionEnabled(DataCategory.SETTINGS, null))
    }

    @Test
    fun coarseTelemetryPassesAndPersonalContentFails() {
        assertTrue(TelemetryAllowlist.accepts("player_error", setOf("error_code", "route_type")))
        assertFalse(TelemetryAllowlist.accepts("player_error", setOf("route_type", "device_name")))
        assertFalse(TelemetryAllowlist.accepts("intent_failed", setOf("reason", "transcript")))
        assertFalse(TelemetryAllowlist.accepts("unknown_event", emptySet()))
    }
}
