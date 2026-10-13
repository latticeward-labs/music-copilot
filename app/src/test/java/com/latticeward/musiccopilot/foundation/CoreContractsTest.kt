package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.CommandId
import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.model.VoiceCandidateToken
import com.latticeward.musiccopilot.core.model.VoiceSessionId
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import org.junit.Assert.*
import org.junit.Test

class CoreContractsTest {
    @Test fun opaqueIdentifiersAreDistinctTypesWithExpectedValues() {
        assertEquals("cmd-1", CommandId("cmd-1").value)
        assertEquals("opaque-ref", MediaRef("opaque-ref").value)
        assertEquals(9L, VoiceSessionId(9L).value)
        assertEquals("one-time", VoiceCandidateToken("one-time").value)
    }

    @Test fun successAndTypedFailureAreExhaustiveBranches() {
        val ok: AppResult<Int> = AppResult.Success(42)
        val failed: AppResult<Int> = AppResult.Failure(
            AppError.Playback(AppErrorCodes.PLAYBACK_BUSY,
                Recoverability.RECOVERABLE, RecoveryAction.Retry)
        )
        assertEquals(42, (ok as AppResult.Success).value)
        val error = (failed as AppResult.Failure).error
        assertTrue(error is AppError.Playback)
        assertEquals("PLAYBACK_BUSY", error.code)
        assertEquals(RecoveryAction.Retry, error.recoveryAction)
    }

    @Test fun mediaErrorCarriesOnlyAnOpaqueReferenceAndSafeCode() {
        val error = AppError.MediaItem(
            AppErrorCodes.MEDIA_ITEM_MISSING,
            Recoverability.USER_ACTION_REQUIRED,
            RecoveryAction.Skip,
            MediaRef("fake-ref")
        )
        assertEquals(MediaRef("fake-ref"), error.mediaRef)
        assertEquals(RecoveryAction.Skip, error.recoveryAction)
    }

    @Test fun qualityErrorsHaveNonBlockingRecoveryDefault() {
        val dropped = AppError.Quality(AppErrorCodes.QUALITY_DROPPED)
        assertEquals(RecoveryAction.None, dropped.recoveryAction)
        assertEquals(Recoverability.RECOVERABLE, dropped.recoverability)
    }

    @Test fun allApprovedRecoveryActionsRemainPresent() {
        assertEquals(7, RecoveryAction.entries.size)
        assertEquals(3, Recoverability.entries.size)
    }
}

