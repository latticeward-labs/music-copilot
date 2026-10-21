# Issue #6 — P06: Room V2, grouped browse and local search implementation

**Date:** 2026-10-22 · **Branch:** `feature/6-mediastore-library-browse-search` · **Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6) (OPEN).  
**Status:** **SOURCE IMPLEMENTATION DELIVERED; P06 NATIVE/SCHEMA-EXPORT VERIFICATION OUTSTANDING.** No Android build, emulator, JVM, Room instrumented test, benchmark or APK has been run for this phase. Do **not** mark Issue #6 AC-02/AC-10 as PASS yet.  
**Source archive:** original uploaded `MSM2(1)(1).rar` — SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (12 PDFs, same as P00–P05). Independent current file check and extracted English authority review completed.

## 1. Scope and architecture authority

User-supplied English PRD FR-01, MVP US-02, System Architecture §MediaStore→normalize/index→Room projection→Library UI and Code/Technical Docs local-first data boundaries were cross-checked against the accepted repo ADR-0003, [#16 public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [runtime policy](../architecture/RUNTIME_POLICIES.md), UF-03/04, GS-01/02 and approved WF-04/05, HF-04/05. [P02 source/data contract](ISSUE_6_PHASE2_DATA_CONTRACT.md), [P05 atomic delta](ISSUE_6_PHASE5_RECONCILIATION_IMPLEMENTATION.md) and [P01 delivery contract](ISSUE_6_PHASE1_EXECUTION_CONTRACT.md) remain binding. No change to #7 service-owned playback, media file ownership, OS permission authority, DataStore consent or remote services.

**P06 deliverables:** production Room V2 entity and registered additive V1→V2 migration, stable Songs/Artists/Albums/Folders grouped SQL category and member pages, bounded local title/artist/album search, normalized persistent keys and synthetic native/JVM test source. Do not confuse source implementation with executed/instrumented PASS.

## 2. Exact committed source changes

| Path | Source implementation and contract |
|---|---|
| `data/room/LibraryRow.kt` | Room `library_items` V2 with **10 additive nullable derived columns**, legacy seven constructor arguments unchanged via trailing defaults. Persist volume, full relative folder, album/artist/folder opaque group IDs, normalized album and folder label key, folder label, local metadata fingerprint and optional MediaStore modified generation. Five compound `@Index` patterns |
| `data/room/LibraryMigrations.kt` | Explicit `Migration(1,2)`: 10 `ALTER TABLE .. ADD COLUMN` statements and five `CREATE INDEX` statements matching exactly the entity's annotations. No table drop/delete/reset, no fabrication of old V1 folder/source IDs |
| `data/room/MusicCopilotDatabase.kt` | Bumped Room annotation from 1 to 2 with `exportSchema=true` |
| `app/di/DataBindings.kt` | Registered `LibraryMigrations.V1_TO_V2` on actual production `Room.databaseBuilder(...)`; still **NO destructive fallback** |
| `data/mapping/LibraryRowMapper.kt` | Real MediaStore scan becomes fully populated derived V2 row, with preserved source-scoped MediaRef, normalized album key, locally hashed group IDs, volume/path label, generation and deterministic SHA-256 fingerprint; duplicate same-source winner further tied on fingerprint |
| `data/room/LibraryDao.kt` | SQL `COUNT` + `LIMIT/OFFSET` for all Songs and group-members; SQLite `GROUP BY`/COUNT(DISTINCT) for Artists, Albums and Folders; deterministic stable order; SQL-bound `instr` across title/artist/album; literal percent/underscore are not wildcard operators; all searches cap rows |
| `data/room/LibraryProjection.kt` | Production `browsePage(request, revision)` performs group/entry count+page inside **one Room read transaction**, returns typed Song/Artist/Album/Folder entries and bounded `nextOffset`. Real `searchLocal` delegates to DAO; pre-P06 JVM fake seam retained (only for old tests, not production). P05 atomic row-delta writes still intact |
| `data/repository/RoomLibraryRepository.kt` | Overrides public `browse`, replaces `search` full `projection.all().filter` with `projection.searchLocal`. Both check current OS access and trusted Library readiness; stale revisions/errors fail closed to typed sanitized recoverable errors, not fake Empty/No Results. Results return on IO and are capped at 100 |
| `core/result/AppError.kt` | Added privacy-safe `LIBRARY_BROWSE_STALE` (no personal media metadata in code or error) |
| `data/room/LibrarySchemaEvolution.kt` | Updated source status to true version 2 **with explicit P12 KAPT-generated 2.json and SQLite migration audit outstanding** |
| `app/src/test/java/.../Issue6Phase6BrowsePortTest.kt` | **7 authored JVM tests**, no execution: denied/readiness/stale/paging/error and album search contract checks |
| `app/src/androidTest/java/.../Issue6Phase6RoomV2InstrumentedTest.kt` | **5 authored native tests**, no execution: real V1 SQLite → V2 Room migration with preserved row; four category/member counts; pagination; literal/bounded three-field search; missing-vs-literal unknown artist grouping |

**File identity:** existing exported `app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/1.json` preserved unchanged. **The new `2.json` is NOT claimed generated or validated.** KAPT/Gradle did not run in this environment (Android SDK/Gradle unavailable; user explicitly reserved consolidated technical testing for P12). The Room compiler must export and check in **its real** `2.json` with genuine compiler identity hash during P12, not a hand-written/guessed hash. P06 final native acceptance remains gated by this deliverable and matching migration validation.

## 3. SQLite behavior / bounded runtime contract

- **Songs:** `ORDER BY normalizedTitle, mediaRef LIMIT :limit OFFSET :offset`; tie break by opaque source ID, not name. Identically titled distinct files are retained.
- **Artists:** grouped by `artistGroupId`, label is deterministic `MIN(COALESCE(artist,'Unknown Artist'))`. Missing metadata and a literal track named `Unknown Artist` have different group IDs even if both display the same fallback. Member query: `WHERE artistGroupId = :id ORDER BY normalizedTitle,mediaRef`.
- **Albums:** group by source-scoped platform `ALBUM_ID` or fallback album+artist identity; album title `MIN(album)`; cross-artist albums use `Various Artists` rather than falsely claiming an arbitrary single artist. Member query is indexed `albumGroupId`.
- **Folders:** group by **full relative folder path** and volume (case/spacing preserved); `folderLabel` is local basename for UI, normalized label for display sorting, full path is never emitted through telemetry. Member query uses `folderGroupId`.
- **Group counts:** SQL `COUNT(DISTINCT <groupId>)`, not loading all rows and grouping in Kotlin. V1 migrated rows with unknown group keys remain temporarily `NULL` until explicit authorized scan; startup readiness is **Unavailable**, not Ready.
- **Pagination:** validated `LibraryPageRequest`: default 50, max 100, nonnegative offset and optional revision; SQL applies bounds before returning rows. Count/page share a Room read transaction. `nextOffset` uses overflow-safe Long comparison. Every result carries accepted revision and fails to surface on permission loss or revision drift.
- **Local Search:** normalization `NFKC / Locale.ROOT` inherited from P04; SQLite `instr(normalizedTitle,...)`, `instr(normalizedArtist,...)`, `instr(normalizedAlbum,...)`; `LIMIT <= 100`, deterministic order. Empty user query is UI Idle, no query. Read not authorized/ready/revision changed yields typed failure; truly absent matches return empty result.
- **Performance honesty:** Compounded indexes support sorted Songs/member pages and keyed lookup/grouping; **substring `instr` is still O(N) scan** for general text. Unlike old code, SQL avoids materializing/filtering **all 5,000 rows in Kotlin on each keystroke**. P09 should debounce input; P12 T04/T07 must `EXPLAIN QUERY PLAN`, measure 5k query and scroll latency, and use FTS/token optimization only if metrics require. No unmeasured 5k performance PASS.
- **Permissions:** OS grant and explicit source reconcile remain authority; even committed cached Room rows never prove existence, audio bytes or playback. Re-query on selection through #7 seam remains required. All data offline.

## 4. Migration/data-integrity and rollback gates

**Upgrade path:** installed Room V1 schema with seven existing fields → `Migration(1,2)` adds nullable derived-only fields + five indexes → Room V2 validation → after OS permission granted, `RoomLibraryRepository.reconcile()` derives and updates V2 fields atomically via existing P05 row-delta → approved Ready/Empty only once MediaStore scan succeeds.

- Existing titles/artist/album/duration are **not rewritten/deleted by migration**. App-owned preference/queue data is in DataStore, untouched.
- Old V1 rows have new fields `NULL` (not fabricated), so `LibraryDeltaPlanner` detects changed V2 rows on first permitted reconcile; the old reference may be removed if MediaStore now maps a source-volume-scoped ID.
- No `fallbackToDestructiveMigration`. No direct media deletion, index telemetry or background/network catalog.
- SQLite migration and KAPT schema validation can **fail**; until an actual run proves clean old-DB upgrade and generated schema, do not deliver APK or merge.
- A binary compiled for V1 cannot silently open released V2 database; require fix-forward/reviewed migration, not uninstall or user data wipe. Historic `1.json` remains as migration reference.

## 5. Actual gate and P12 acceptance debt

**P06 source gate** (live GitHub readback to be captured in `ISSUE_6_PHASE6_VALIDATION.json`):
- [x] Four category/member SQL queries, bounded counts/rows and stable sorting authored.
- [x] Normalized album and folder metadata persisted; local hash group IDs, unknown and cross-volume rules enforced.
- [x] Registered V1→V2 migration code and non-destructive Room 2 source annotation.
- [x] Repository page/search real SQL integration with permission/revision error handling.
- [x] 7 JVM and 5 Android instrumented test methods authored (NOT_RUN).
- [x] No per-phase APK and no Issue #6 product AC checkoff.
- [ ] **Compiler-generated `2.json` and KAPT/Kotlin compilation evidence — NOT_RUN; REQUIRED.**
- [ ] **Real SQLite migration, Room index/SQL parser checks, EXPLAIN, 5k benchmark, API/OEM device evidence — NOT_RUN; REQUIRED.**

**Required P12 execution:** `./gradlew :app:kaptDebugKotlin` (generate genuine `2.json`; commit source schema), compile full app, then native Room V1-to-V2 migration tests and JVM fixtures; inspect `EXPLAIN QUERY PLAN`, measure 5,000 synthetic items, execute T01–T10 with links and screenshots only from synthetic media; finally create **one integrated APK** from P00–P11 and deliver to owner for M01–M34 on one real phone. If any compilation/schema/migration test fails, fix source and generate a new traceable candidate before claiming PASS. No independent UI #7 playback tests inherited automatically, Issue #73 deferred #5 technical debt remains open.

**Disposition:** `P06_SOURCE_IMPLEMENTED; P06_FINAL_VERIFICATION_PENDING_P12`. The project **must not claim P06 fully runtime-PASS**, AC-02/AC-10 PASS, or Issue #6 complete without this executed evidence.
