package com.latticeward.musiccopilot.spike.privacy

enum class DataCategory(val clearable: Boolean, val retentionBound: Boolean) {
    SHORT_HISTORY(clearable = true, retentionBound = true),
    LOCAL_DIAGNOSTICS(clearable = true, retentionBound = true),
    PROVIDER_DIAGNOSTICS(clearable = true, retentionBound = true),
    LIKES(clearable = false, retentionBound = false),
    SETTINGS(clearable = false, retentionBound = false),
    CONSENT_RECORDS(clearable = false, retentionBound = true),
    LIBRARY_PROJECTION(clearable = false, retentionBound = false),
    LIVE_PLAYBACK_QUEUE(clearable = false, retentionBound = false),
    MEDIA_FILES(clearable = false, retentionBound = false),
}

enum class DeletionAction {
    CLEAR_LOCAL_HISTORY,
    CLEAR_LOCAL_DIAGNOSTICS,
    REQUEST_PROVIDER_DIAGNOSTIC_DELETION,
}

sealed interface DeletionPlan {
    data class Rejected(val reason: Reason) : DeletionPlan
    data class Approved(val actions: Set<DeletionAction>) : DeletionPlan

    enum class Reason {
        CONFIRMATION_REQUIRED,
        EMPTY_SELECTION,
        PROTECTED_CATEGORY,
    }
}

data class ClearRequest(
    val selected: Set<DataCategory>,
    val confirmed: Boolean,
)

object PrivacyDataPolicy {
    fun plan(request: ClearRequest): DeletionPlan {
        if (!request.confirmed) return DeletionPlan.Rejected(DeletionPlan.Reason.CONFIRMATION_REQUIRED)
        if (request.selected.isEmpty()) return DeletionPlan.Rejected(DeletionPlan.Reason.EMPTY_SELECTION)
        if (request.selected.any { !it.clearable }) {
            return DeletionPlan.Rejected(DeletionPlan.Reason.PROTECTED_CATEGORY)
        }

        val actions = buildSet {
            if (DataCategory.SHORT_HISTORY in request.selected) add(DeletionAction.CLEAR_LOCAL_HISTORY)
            if (DataCategory.LOCAL_DIAGNOSTICS in request.selected) add(DeletionAction.CLEAR_LOCAL_DIAGNOSTICS)
            if (DataCategory.PROVIDER_DIAGNOSTICS in request.selected) {
                add(DeletionAction.REQUEST_PROVIDER_DIAGNOSTIC_DELETION)
            }
        }
        return DeletionPlan.Approved(actions)
    }

    fun collectionEnabled(category: DataCategory, approvedRetentionDays: Int?): Boolean {
        if (!category.retentionBound) return true
        return approvedRetentionDays != null && approvedRetentionDays > 0
    }
}

object TelemetryAllowlist {
    private val allowedEvents = mapOf(
        "player_error" to setOf("error_code", "route_type"),
        "session_completed" to setOf("duration_bucket", "commands_count"),
        "intent_failed" to setOf("intent_type", "confidence_bucket", "reason"),
    )

    private val prohibitedFields = setOf(
        "raw_audio",
        "transcript",
        "title",
        "artist",
        "album",
        "path",
        "content_uri",
        "device_name",
        "device_address",
        "call_identity",
        "location",
        "contacts",
    )

    fun accepts(event: String, fields: Set<String>): Boolean {
        val allowed = allowedEvents[event] ?: return false
        return fields.none { it in prohibitedFields } && fields.all { it in allowed }
    }
}
