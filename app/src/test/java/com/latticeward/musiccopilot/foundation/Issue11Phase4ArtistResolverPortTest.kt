package com.latticeward.musiccopilot.foundation

import com.latticeward.musiccopilot.core.model.MediaRef
import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.ArtistIndexEntry
import com.latticeward.musiccopilot.data.api.ArtistLookupMode
import com.latticeward.musiccopilot.data.api.ArtistLookupPage
import com.latticeward.musiccopilot.data.api.ArtistLookupRequest
import com.latticeward.musiccopilot.data.api.LibraryCandidate
import com.latticeward.musiccopilot.data.api.LibraryGroupIdentity
import com.latticeward.musiccopilot.data.api.LibraryIndexSnapshot
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryItem
import com.latticeward.musiccopilot.data.api.LibraryQuery
import com.latticeward.musiccopilot.data.api.LibraryReconcileResult
import com.latticeward.musiccopilot.data.api.LibraryRepository
import com.latticeward.musiccopilot.data.api.LibraryTextNormalization
import com.latticeward.musiccopilot.voice.intent.ArtistResolver
import com.latticeward.musiccopilot.voice.intent.ArtistResolution
import com.latticeward.musiccopilot.voice.intent.PlayArtistSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/**
 * Issue #11 P04 synthetic paged Data-port fixtures; NOT_RUN / NOT_COMPILED.
 * P13 alone may compile/execute technical QA if owner approves.
 * No device user media, raw STT, Media3 player or real database touched.
 */
class Issue11Phase4ArtistResolverPortTest {
    private fun artist(display: String) = ArtistIndexEntry(
        LibraryGroupIdentity.artistId(display), display,
        LibraryTextNormalization.key(display), 1,
    )
    private fun slot(s: String) = PlayArtistSlot(s, LibraryTextNormalization.key(s))

    private class FakeLibrary(
        val groups: List<ArtistIndexEntry>,
        var totalOverride: Int? = null,
    ) : LibraryRepository {
        override val index = MutableStateFlow(
            LibraryIndexSnapshot(4L, LibraryIndexStatus.Ready, groups.size),
        )
        val requests = mutableListOf<ArtistLookupRequest>()
        var staleFromOffset: Int? = null
        var revokeAfterLastPage = false
        var dropNextOffsetAtFirstPage = false

        override fun observeLibrary(): Flow<List<LibraryItem>> = flowOf(emptyList())
        override suspend fun reconcile(): AppResult<LibraryReconcileResult> = unavailable()
        override suspend fun search(query: LibraryQuery): AppResult<List<LibraryItem>> =
            AppResult.Success(emptyList())
        override suspend fun get(ref: MediaRef): AppResult<LibraryItem> = unavailable()
        override suspend fun resolveVoiceCandidates(
            normalizedTarget: String, limit: Int,
        ): AppResult<List<LibraryCandidate>> = unavailable()

        override suspend fun lookupArtistGroups(
            request: ArtistLookupRequest,
        ): AppResult<ArtistLookupPage> {
            requests.add(request)
            if (request.expectedRevision != null &&
                request.expectedRevision != index.value.revision
            ) return stale()
            if (request.mode == ArtistLookupMode.CandidatePage &&
                staleFromOffset == request.offset
            ) return stale()

            val pool = if (request.mode == ArtistLookupMode.ExactNormalized) {
                groups.filter { it.normalizedKey == request.normalizedTarget }
            } else groups
            val ordered = pool.sortedWith(
                compareBy<ArtistIndexEntry> { it.normalizedKey }
                    .thenBy { it.id.value },
            )
            val total = if (request.mode == ArtistLookupMode.CandidatePage)
                totalOverride ?: ordered.size else ordered.size
            val entries = ordered.drop(request.offset).take(request.limit)
            val next = if (entries.isNotEmpty() &&
                request.offset + entries.size < total
            ) request.offset + entries.size else null
            val returnedNext = if (request.mode == ArtistLookupMode.CandidatePage &&
                request.offset == 0 && dropNextOffsetAtFirstPage
            ) null else next
            if (revokeAfterLastPage && returnedNext == null &&
                request.mode == ArtistLookupMode.CandidatePage
            ) index.value = index.value.copy(status = LibraryIndexStatus.Unavailable)
            return AppResult.Success(
                ArtistLookupPage(entries, total, 4L, returnedNext, request.limit),
            )
        }

        private fun stale(): AppResult.Failure = AppResult.Failure(
            AppError.Library(AppErrorCodes.LIBRARY_BROWSE_STALE,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
        )
        private fun unavailable(): AppResult.Failure = AppResult.Failure(
            AppError.Library(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
                Recoverability.RECOVERABLE, RecoveryAction.ManualFallback),
        )
    }

    private fun failureCode(result: AppResult<*>): String {
        assertTrue(result is AppResult.Failure)
        return (result as AppResult.Failure).error.code
    }

    @Test fun resolverStartsWithExactArtistIndexAndThenPinsCandidateRevision() = runBlocking {
        val library = FakeLibrary(listOf(artist("Adele")))
        val result = ArtistResolver(library).resolve(slot("Adele"))
        assertTrue(result is AppResult.Success<*>)
        assertTrue((result as AppResult.Success<ArtistResolution>).value is ArtistResolution.Clear)
        assertEquals(ArtistLookupMode.ExactNormalized, library.requests[0].mode)
        assertEquals(ArtistLookupMode.CandidatePage, library.requests[1].mode)
        assertEquals(4L, library.requests[1].expectedRevision)
    }

    @Test fun canonicalQuoteVariantsAcrossDifferentIdsAreBothConsidered() = runBlocking {
        val library = FakeLibrary(listOf(artist("L'Arc"), artist("L’Arc")))
        val result = ArtistResolver(library).resolve(slot("L'Arc"))
        assertTrue((result as AppResult.Success<ArtistResolution>).value is ArtistResolution.NeedsConfirmation)
        val selected = result.value as ArtistResolution.NeedsConfirmation
        assertEquals(2, selected.candidates.size)
    }

    @Test fun fuzzyScoreCanFindANameWithoutSubstringEquality() = runBlocking {
        val library = FakeLibrary(listOf(artist("The Weekend"), artist("Mozart")))
        val result = ArtistResolver(library).resolve(slot("The Weeknd"))
        assertTrue((result as AppResult.Success<ArtistResolution>).value is ArtistResolution.Clear)
        assertEquals("The Weekend", (result.value as ArtistResolution.Clear)
            .candidate.displayLabel)
    }

    @Test fun fullyScansTwoBoundedPagesWithoutHydratingSongsOrRoomAll() = runBlocking {
        val labels = (0 until 140).map { "Noise Band $it" } + "The Weekend"
        val library = FakeLibrary(labels.map(::artist))
        val result = ArtistResolver(library).resolve(slot("The Weeknd"))
        assertTrue((result as AppResult.Success<ArtistResolution>).value is ArtistResolution.Clear)
        assertTrue(library.requests.count { it.mode == ArtistLookupMode.CandidatePage } >= 2)
        assertTrue(library.requests.all { it.limit <= ArtistResolver.PAGE_SIZE })
        assertTrue(library.requests.filter { it.mode == ArtistLookupMode.CandidatePage }
            .all { it.expectedRevision == 4L })
    }

    @Test fun emptyVerifiedArtistPageMeansNotFoundOnlyAfterFullRead() = runBlocking {
        val library = FakeLibrary(emptyList())
        val result = ArtistResolver(library).resolve(slot("Adele"))
        assertEquals(ArtistResolution.NotFound(4L), (result as AppResult.Success<ArtistResolution>).value)
    }

    @Test fun invalidIndexGroupMetadataFailsUnavailableNotNotFound() = runBlocking {
        val wrong = ArtistIndexEntry(LibraryGroupIdentity.artistId("Adele"),
            "Adele", "wrong key", 1)
        val library = FakeLibrary(listOf(wrong))
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            failureCode(ArtistResolver(library).resolve(slot("Adele"))))
    }

    @Test fun tooManyArtistGroupsDoNotProducePartialNotFoundOrWrongDirect() = runBlocking {
        val library = FakeLibrary((0 until 100).map { artist("Band $it") },
            totalOverride = ArtistResolver.MAX_SCANNED_ARTISTS + 1)
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            failureCode(ArtistResolver(library).resolve(slot("Adele"))))
    }

    @Test fun aTruncatedPageFailsClosedInsteadOfClaimingNoMatch() = runBlocking {
        val library = FakeLibrary((0 until 120).map { artist("Band $it") })
        library.dropNextOffsetAtFirstPage = true
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            failureCode(ArtistResolver(library).resolve(slot("Adele"))))
    }

    @Test fun staleSecondPagePropagatesTypedFailure() = runBlocking {
        val library = FakeLibrary((0 until 120).map { artist("Band $it") })
        library.staleFromOffset = 100
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(ArtistResolver(library).resolve(slot("Adele"))))
    }

    @Test fun permissionRevocationAtFinalPageIsNotMisreportedAsNotFound() = runBlocking {
        val library = FakeLibrary(listOf(artist("Bach")))
        library.revokeAfterLastPage = true
        assertEquals(AppErrorCodes.LIBRARY_BROWSE_STALE,
            failureCode(ArtistResolver(library).resolve(slot("Adele"))))
    }

    @Test fun twoPlausibleCandidatesReturnOnlyBoundedConfirmationSuggestions() = runBlocking {
        val library = FakeLibrary(listOf(artist("Adelle"), artist("Adelee")))
        val result = ArtistResolver(library).resolve(slot("Adele"))
        val ambiguous = (result as AppResult.Success<ArtistResolution>).value as ArtistResolution.NeedsConfirmation
        assertEquals(2, ambiguous.candidates.size)
        assertEquals(4L, ambiguous.revision)
    }

    @Test fun threeNearEqualMatchesAreSafelyUnresolvedNotPartialConfirmation() = runBlocking {
        val library = FakeLibrary(listOf(
            artist("Adelle"), artist("Adelee"), artist("Adela"),
        ))
        val result = ArtistResolver(library).resolve(slot("Adele"))
        assertTrue((result as AppResult.Success<ArtistResolution>).value is ArtistResolution.TooManyPlausible)
    }

    @Test fun zeroTrustForUnnormalizedOrOversizedSlots() = runBlocking {
        val library = FakeLibrary(listOf(artist("Adele")))
        val slot = PlayArtistSlot("Synthetic", "x".repeat(81))
        assertEquals(AppErrorCodes.VOICE_NO_MATCH,
            failureCode(ArtistResolver(library).resolve(slot)))
        assertTrue(library.requests.isEmpty())
    }

    @Test fun privateResultDoesNotIncludeArtistOrIdInDiagnosticMessage() = runBlocking {
        val library = FakeLibrary(listOf(artist("Synthetic Private Artist")))
        val result = ArtistResolver(library).resolve(slot("Synthetic Private Artist"))
        assertTrue(result is AppResult.Success<*>)
        assertFalse(result.toString().contains("Synthetic Private Artist"))
        val typed = (result as AppResult.Success<ArtistResolution>).value
        assertFalse(typed.toString().contains("Synthetic Private Artist"))
    }
}
