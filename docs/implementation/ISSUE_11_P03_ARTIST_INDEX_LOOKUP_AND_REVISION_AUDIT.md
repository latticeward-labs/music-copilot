# Issue #11 — P03: Real Local Artist-Group Lookup & Revision-Safe Member References

**Date:** 2026-11-14  
**Branch:** `feature/11-play-artist-one-step-ambiguity`  
**Phase:** P03 — **SOURCE IMPLEMENTED / UNWIRED / NOT BUILT / NOT TESTED**  
**Owner source:** attached `MSM2(1)(1).rar`, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; its 12 documents were extracted and relevant sections directly reviewed. Specifically ADR-0003 local-first / MediaStore source-of-truth rules, ADR-0005 artist resolution and stable matching, ADR-0004 Voice boundaries, the Technical Blueprint Voice & Intent / Data sections, plus current GitHub [Voice Contract](../VOICE_CONTRACT.md) and [P00](ISSUE_11_P00_READINESS_AND_GAPS.md), [P01](ISSUE_11_P01_PRODUCT_CONTRACT_AND_STATE_MATRIX.md), [P02](ISSUE_11_P02_PLAY_ARTIST_GRAMMAR_AND_SLOT_AUDIT.md) records.

## 1. Outcome and limits

P03 introduces **real Room/MediaStore-derived artist identity access** for subsequent PLAY_ARTIST matching. It is **not** the fuzzy matcher, confidence policy, VoiceCoordinator confirmation, Media3 command, queue assembly or end-to-end feature. The production Voice flow has **not** been wired to these new Data API methods; until the later phases, five existing controls remain as before and PLAY_ARTIST does not execute.

P03 preserves the existing architecture: Android 10+, offline MediaStore (existence authority), derived Room index, `LibraryRepository` as Data public port, one service-owned Media3 Player, no backend/streaming/secondary library store, no new migration/schema version and no new telemetry. The existing **legacy** title-oriented `resolveVoiceCandidates()` is deliberately left **unchanged** and is **NOT** used as artist identity.

## 2. Source files delivered

| File | Source-level change |
|---|---|
| [`data/api/ArtistLookupContracts.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/api/ArtistLookupContracts.kt) | New typed `ArtistLookupMode`, `ArtistLookupRequest`, `ArtistIndexEntry`, `ArtistLookupPage`, `ArtistTrackRefsRequest`, `ArtistTrackRefsPage`. Private data redacted in diagnostic `toString()`; defensive immutable list snapshots |
| [`data/api/LibraryRepository.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryRepository.kt) | Add `lookupArtistGroups(request)` and `artistTrackRefs(request)` ports, safe `LIBRARY_BROWSE_UNAVAILABLE` defaults for older test/provider fakes |
| [`data/room/LibraryDao.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryDao.kt) | Six additive SQL methods + lightweight `VoiceArtistGroupRow` projection: exact artist key, full-index artist page, member count and bounded member ref page; no song-title candidate query |
| [`data/room/LibraryProjection.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryProjection.kt) | Add fail-closed projection ports and real `RoomLibraryProjection` overrides using `database.withTransaction` around each count/page pair |
| [`data/repository/RoomLibraryRepository.kt`](../../app/src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt) | Add repository implementations checking MediaStore access and approved index state, expected revision, cancellation, post-read access, original revision/status **and access epoch** |
| [`Issue11Phase3ArtistLookupPortTest.kt`](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Issue11Phase3ArtistLookupPortTest.kt) | 14 synthetic JVM scenarios authored, **0 executed** |
| [`Issue11Phase3RoomArtistQueryTest.kt`](../../app/src/androidTest/java/com/latticeward/musiccopilot/Issue11Phase3RoomArtistQueryTest.kt) | 4 native Room/SQLite instrumentation scenarios authored, **0 executed** |

All additions are on `feature/11-play-artist-one-step-ambiguity`. No file in the Media3 service, audio route, STT/Moonshine, UI, existing five-command grammar, Room schema or migrations was changed in P03.

## 3. Exact artist-group and fuzzy candidate data contract

`ArtistLookupRequest(normalizedTarget, mode, offset=0, limit=50, expectedRevision=null)`:

- **Input:** a nonblank, canonical `LibraryTextNormalization.key()` key already produced by P02; length ≤120 (input deliberately private). No transcript or raw artist string is logged.
- **ExactNormalized:** Room predicate `normalizedArtist = :normalizedTarget`, restricted to non-null `artistGroupId`, present original `artist`, nonblank derived `normalizedArtist`. SQL groups by **artistGroupId**, NOT title or album.
- **CandidatePage:** SQL yields **all real artist groups**, ordered `MIN(normalizedArtist)`, then stable `artistGroupId`, with SQLite `LIMIT/OFFSET`. The mode intentionally does **not** filter by substring: P04 can compare fuzzy names that do not contain the literal target. Full index is **not** hydrated into the UI or a Kotlin `projection.all()` list.
- **Output:** `ArtistLookupPage(entries, totalCount, revision, nextOffset, requestedLimit)` with each `ArtistIndexEntry(id: LibraryGroupId, displayLabel, normalizedKey, indexedSongCount)`. Results are capped at **100 entries per read** (default 50); list copied and unmodifiable. Total/count/page read within the **same SQLite transaction**.
- **Paging:** First page may omit expected revision; all later pages require its original revision. A revision mismatch returns typed **`LIBRARY_BROWSE_STALE`**, never a partial, mixed-revision result. No implicit unbounded page traversal is performed by P03.
- **Ordering:** database query is deterministic; fuzzy scoring, near-tie classification and optional approved history priority remain strictly **P04**. Do not equate alphabetical ordering with final artist ranking.

The source identity `LibraryGroupIdentity.artistId(artist)` is based on normalized artist metadata and is stable and distinct from album/song IDs. Hash-like IDs are **still sensitive local identifiers**; no analytics or GitHub reports may publish them.

## 4. Bounded artist-member references (future P06 queue input)

`ArtistTrackRefsRequest(artistGroupId, expectedRevision, offset=0, limit=50)`:

- Requires a typed artist-group ID (not album/folder) and a **pinned** library revision.
- Room selects `mediaRef` **only** from rows with the requested `artistGroupId`, non-null original artist and nonblank normalized artist, ordered by `normalizedTitle, mediaRef`.
- `ArtistTrackRefsPage` contains a copied, unmodifiable list of **up to 100 opaque `MediaRef`** plus `totalCount`, `revision`, `nextOffset`; count and refs share one SQLite read transaction.
- This **does not construct or mutate the live Player queue**, and it does not authorize autoplay. `MediaRef` values can be stale even when an index read was consistent.
- **Mandatory in P06:** re-check index revision/access, call `LibraryRepository.get(ref)` for each chosen ref (live MediaStore re-query), skip/reject unavailable media according to approved policy, re-check before Media3 dispatch, submit a single service-owned `PlaybackCommand.ReplaceQueue` and wait for ACK. No title/URI/media contents travel into Voice UI/telemetry.

## 5. Missing artist metadata policy

- Null/blank/unsupported MediaStore artist metadata is normalized to **null** or an empty key by the already approved `LibraryRowMapper`/`LibraryTextNormalization`; artist-group queries reject `artist IS NULL`, empty `normalizedArtist` or `artistGroupId IS NULL`.
- A **genuinely present literal artist label** `Unknown Artist` is kept distinct from null metadata because `LibraryGroupIdentity.artistId("Unknown Artist") != artistId(null)`. Nothing invents `Unknown Artist` for a null source.
- If an OEM MediaStore provider literally supplies the *string* `Unknown Artist` rather than its usual null/`<unknown>` sentinel, existing normalization cannot infer whether the OEM fabricated that label; this is a **known provider-metadata ambiguity**, not proof of an independently verified artist identity. P06 still requires live MediaStore requery. The 4 native Room tests are source-authored, not executed.
- Old migrated V1 records with null V2 group columns are excluded until an authorized reconcile creates valid derived identities. No destructive migration or new Room schema required.

## 6. Failure, permission and concurrency matrix

| Input / condition | Repository outcome | Access to derived Room? | Playback impact |
|---|---|---|---|
| Permission absent before read | `PERMISSION_REVOKED`; index invalidated | None | None |
| Index `Unavailable`, `Scanning`, `RecoverableError` | `LIBRARY_BROWSE_UNAVAILABLE` | None | None |
| Accepted `Ready/Empty`, revision matches | Snapshot page `AppResult.Success` | Bounded SQL only | None |
| Expected revision stale before query | `LIBRARY_BROWSE_STALE` | None | None |
| Permission revoked during SQL read | `PERMISSION_REVOKED`; index invalidated | No result published | None |
| Revoke/regrant while query in flight (`accessEpoch` changed) | `LIBRARY_BROWSE_STALE` | No old result published | None |
| Reconcile commits a different revision/status during query | `LIBRARY_BROWSE_STALE` | No mixed result published | None |
| Paged query canceled | cancellation propagated; no success published | Cancellable read | None |
| SQL unavailable/error | `LIBRARY_BROWSE_UNAVAILABLE` | No guessed `NotFound` | None |
| Valid index with zero known artist identities | Real empty page (zero count) | Yes | None |
| Artist-group ID not present on current index | Real empty refs page | Yes | None |

`RoomLibraryRepository` checks `source.hasAccess()`, `index` snapshot and `accessEpoch` before/after each new query. It cannot make a result universally immune to a **subsequent** permission revocation or file deletion **after returning**: downstream confirmation/queue handling in P06/P07 must revalidate live data again. No local artist name is placed in a typed error code.

## 7. Quality evidence — 18 scenarios AUTHORED, 0 RUN

- **14 synthetic JVM port cases:** canonical/length bounds, fail-closed before authorized index, no permission, exact artist not song title, missing-vs-literal unknown, stable paged cursor, stale revision pre-query, selected-group refs ordering, revocation in-flight, revoke/regrant epoch race, member read permission loss, reconciliation revision change, private diagnostic redaction, immutable-list copies.
- **4 Android Room SQLite instrumented cases:** exact artist SQL vs title, true bounded/stable paging and missing metadata filtering, artist-only ref selection with title/ref sort, literal Unknown Artist vs null identity.
- **No tests executed.** No Gradle/KAPT/compile, JVM, Android, emulator, device, GitHub Actions, lint, App/APK packaging or CI were run. This is **source-only inspection**, **not** a Room SQL execution result or a PASS.
- All 13 original #11 AC remain `NOT_VERIFIED`/`NOT_RUN`. Specifically AC02 candidate lookup groundwork ≠ completed fuzzy matching/normalization acceptance. P13 planned M01–M48, T01–T10 and six-by-ten L01–L06 remain `NOT_RUN`.

## 8. Remaining P04+ work and explicit open risks

**P04:** versioned exact/fuzzy score + stable final ranking and tie handling (including optional local history priority ONLY if approved data source exists). Must iterate pages with a bounded algorithm and no partial-paging false `NotFound`. **P05:** separate provider-confidence policy; Moonshine provider reports `null`, no fake confidence or unconditional artist auto-Play. **P06:** live MediaStore verification, stable artist queue and service ACK. **P07:** one-step, single-use, stale-safe confirmation. P08–P12: UX/feedback/lifecycle/privacy/wiring. **P13 only:** one cumulative APK and user-run acceptance.

- [#80](https://github.com/latticeward-labs/music-copilot/issues/80) is **OPEN**: previous Issue #10 owner-reported five-command lab 24/50 = 48%, below the 90% gate. P03 does not affect STT accuracy or claim release readiness.
- P03 static source review is not substitute for P13 KAPT schema processing / real SQLite transaction / permission revocation / device evidence. Do not close #11, merge it, or check its acceptance boxes in P03.

**P03 disposition:** **Source implementation complete on feature branch; not built, not tested, not yet wired to Voice. P04 ready to start from these bounded typed Data APIs.**
