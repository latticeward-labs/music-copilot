package com.latticeward.musiccopilot.voice.intent

import com.latticeward.musiccopilot.core.result.AppError
import com.latticeward.musiccopilot.core.result.AppErrorCodes
import com.latticeward.musiccopilot.core.result.AppResult
import com.latticeward.musiccopilot.core.result.Recoverability
import com.latticeward.musiccopilot.core.result.RecoveryAction
import com.latticeward.musiccopilot.data.api.ArtistLookupMode
import com.latticeward.musiccopilot.data.api.ArtistLookupRequest
import com.latticeward.musiccopilot.data.api.LibraryGroupId
import com.latticeward.musiccopilot.data.api.LibraryIndexStatus
import com.latticeward.musiccopilot.data.api.LibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Issue #11 P04 artist identity resolver over the P03 local Data port.
 *
 * One ExactNormalized lookup is followed by revision-pinned, SQLite-bounded
 * artist-group pages. All canonical-equivalent artist IDs are considered,
 * including apostrophe variants that the P03 raw SQL normalization keeps
 * in different groups. Artist ranking is independent of SQLite row order.
 *
 * IMPORTANT: source-only until P05 confidence, P06 queue validation and
 * P07 one-use confirmation are wired. No PlaybackGateway is called here.
 */
class ArtistResolver(
    private val library: LibraryRepository,
    private val history: ArtistHistoryPriority = NoArtistHistoryPriority,
) {
    companion object {
        const val RESOLVER_VERSION = "issue11-artist-resolver-v1"
        const val PAGE_SIZE = 100
        const val MAX_SCANNED_ARTISTS = 5_000
    }

    suspend fun resolve(slot: PlayArtistSlot): AppResult<ArtistResolution> {
        val normalizedTarget = ArtistMatchPolicy.normalizedKey(slot.normalizedKey)
            ?: return noMatchFailure()
        if (normalizedTarget.codePointCount(0, normalizedTarget.length) >
            ArtistMatchPolicy.MAX_ARTIST_CODE_POINTS ||
            normalizedTarget.length > ArtistLookupRequest.MAX_TARGET_CHARS
        ) return noMatchFailure()

        val exactRequest = ArtistLookupRequest(
            normalizedTarget = normalizedTarget,
            mode = ArtistLookupMode.ExactNormalized,
            limit = PAGE_SIZE,
        )
        val exact = try {
            library.lookupArtistGroups(exactRequest)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            return unavailableFailure()
        }
        val exactPage = when (exact) {
            is AppResult.Failure -> return exact
            is AppResult.Success -> exact.value
        }
        val revision = exactPage.revision
        if (exactPage.totalCount > PAGE_SIZE ||
            exactPage.entries.size != exactPage.totalCount ||
            exactPage.nextOffset != null
        ) return unavailableFailure()
        if (exactPage.entries.any {
                !ArtistMatchPolicy.isConsistentIndexEntry(it) ||
                    it.normalizedKey != normalizedTarget
            }
        ) return unavailableFailure()
        val expectedRawExactIds = exactPage.entries.map { it.id }.toMutableSet()

        val ranked = ArtistRankAccumulator(normalizedTarget, history)
        val seen = HashSet<LibraryGroupId>()
        var cursor = 0
        var expectedTotal: Int? = null

        while (true) {
            currentCoroutineContext().ensureActive()
            val request = ArtistLookupRequest(
                normalizedTarget = normalizedTarget,
                mode = ArtistLookupMode.CandidatePage,
                offset = cursor,
                limit = PAGE_SIZE,
                expectedRevision = revision,
            )
            val lookup = try {
                library.lookupArtistGroups(request)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                return unavailableFailure()
            }
            val page = when (lookup) {
                is AppResult.Failure -> return lookup
                is AppResult.Success -> lookup.value
            }
            if (page.revision != revision ||
                (expectedTotal != null && expectedTotal != page.totalCount)
            ) return staleFailure()
            if (page.totalCount > MAX_SCANNED_ARTISTS) {
                // A capped partial scan MUST NOT claim NotFound or choose a
                // possibly-wrong winner. Fail to manual controls instead.
                return unavailableFailure()
            }
            if (expectedTotal == null) expectedTotal = page.totalCount
            if (page.totalCount < cursor ||
                page.entries.size > PAGE_SIZE ||
                page.entries.isEmpty() && cursor < page.totalCount
            ) return unavailableFailure()

            for (entry in page.entries) {
                currentCoroutineContext().ensureActive()
                if (!ArtistMatchPolicy.isConsistentIndexEntry(entry) ||
                    !seen.add(entry.id)
                ) return unavailableFailure()
                expectedRawExactIds.remove(entry.id)
                ranked.accept(entry)
            }
            val advanced = cursor + page.entries.size
            if (page.nextOffset == null) {
                if (advanced != page.totalCount || expectedRawExactIds.isNotEmpty()) {
                    return unavailableFailure()
                }
                break
            }
            if (advanced >= page.totalCount || page.nextOffset != advanced) {
                return unavailableFailure()
            }
            cursor = advanced
        }

        // Reconciliations or permission loss after individual page reads must
        // not be mistaken for a complete snapshot, even at the last page.
        // P06 must still requery live MediaStore again before queue mutation.
        val after = library.index.value
        if (after.revision != revision ||
            (after.status != LibraryIndexStatus.Ready &&
                after.status != LibraryIndexStatus.Empty)
        ) return staleFailure()
        return AppResult.Success(ranked.finish(revision))
    }

    private fun noMatchFailure(): AppResult.Failure =
        AppResult.Failure(AppError.Voice(
            code = AppErrorCodes.VOICE_NO_MATCH,
            recoverability = Recoverability.TERMINAL_FOR_ATTEMPT,
            recoveryAction = RecoveryAction.ManualFallback,
        ))

    private fun unavailableFailure(): AppResult.Failure =
        AppResult.Failure(AppError.Library(
            code = AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE,
            recoverability = Recoverability.RECOVERABLE,
            recoveryAction = RecoveryAction.ManualFallback,
        ))

    private fun staleFailure(): AppResult.Failure =
        AppResult.Failure(AppError.Library(
            code = AppErrorCodes.LIBRARY_BROWSE_STALE,
            recoverability = Recoverability.RECOVERABLE,
            recoveryAction = RecoveryAction.ManualFallback,
        ))
}
