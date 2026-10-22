# Issue #6 / Phase P05 — Incremental, cancellable and transactional reconciliation

**Audit date:** 2026-10-22 · **Implementation branch:** `feature/6-mediastore-library-browse-search` · **Gate:** `P05_PASS_SCOPED_SOURCE` **only after live GitHub readback** (NOT a real JVM/Room/Android test PASS).  
**Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6) — stays OPEN with original 12 ACs unchecked.  
**Owner archive:** `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, same independently verified 12-PDF package used in P00–P04.

## 1. Authority and precise definition of “incremental”

- Product: `Music_Copilot_PRD_v1_EN_Clarified.pdf` FR-01 and incremental library scan goal; `Music_Copilot_MVP_v1_EN.pdf` US-02 says initial local index and later incremental scans.
- Architecture: `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`, MediaStore → incremental permission-aware reconciliation → Room derived projection. Accepted repository [ADR-0003](../adr/README.md) and [#16 data interfaces](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) supersede historical PDF status.
- Flow: [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), GS-01/02, P02 data contract, P03 source scanner, P04 normalized rows. P01 requires the **single final consolidated APK in P12**, not per-phase builds.

**Honest scope:** P05 implements **true row-level incremental *database updates***, transactional removal/update/addition and source scan cancellation. A full, authorized enumeration of currently available MediaStore music is still performed on every explicit reconcile in order to know which IDs disappeared; P05 does **NOT** claim that Android's MediaStore provider is already queried as generation-keyed “changed files only.” P03 captures `GENERATION_MODIFIED` on API 30+, but generation watermark/OS version logic, persisted fingerprints and query-plan benchmarks are P06/P12 follow-on improvements. Calling the currently implemented source query “changed-files-only scanner” would be false.

## 2. What was implemented

| Path | Actual committed behavior |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryReconcileDelta.kt` | Pure `LibraryDeltaPlanner.calculate(previous,current)` compares complete `LibraryRow` equality by opaque `mediaRef`; builds deterministic `upserts` (added/metadata-changed) and `removedRefs`, counts and no-op predicate; refuses duplicate/blank index keys. Future V2 fields automatically contribute to data-class row equality |
| `app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryDao.kt` | `DELETE ... WHERE mediaRef IN (:refs)` DAO seam; additions/edits reuse existing REPLACE upsert; no media-file mutation |
| `app/src/main/java/com/latticeward/musiccopilot/data/room/LibraryProjection.kt` | Production `RoomLibraryProjection.applyDelta` writes **only changed rows** inside one `database.withTransaction`, in 400-key batches to respect SQLite bind limits; checks coroutine cancellation and OS/epoch guard before/within/after batch processing. No-op scans skip writes. Old `replaceAll` exists for older Issue #5 fake interfaces/recovery compatibility but **is never called by production `reconcile`**. Legacy default `applyDelta` fallback is exclusively for older JVM fake seams |
| `app/src/main/java/com/latticeward/musiccopilot/data/repository/RoomLibraryRepository.kt` | Serializes scans with `Mutex`, derives P04 stable rows, compares with existing Room snapshot and applies delta; accurate `insertedOrUpdated`/ `removedStale` (not “all rows updated”); `skippedInvalid` counts malformed source refs, not duplicate sightings; respects permission epoch; snapshots increment on changed rows/first scan/recovery/regrant but **not** no-op rescans. Retains previously verified rows during subsequent Scanning/RecoverableError with the same revision, hiding them when permission is revoked; `get(ref)` still re-queries MediaStore |
| `app/src/main/java/com/latticeward/musiccopilot/data/mediastore/MediaStoreScanner.kt` | Wraps blocking `ContentResolver.query` with Android `CancellationSignal`, tied to coroutine `Job`, and converts actual cancellation into coroutine cancellation; per-row `ensureActive` and permission rechecks from P03 retained |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase5IncrementalReconciliationTest.kt` | 15 synthetic JVM cases **AUTHORED, NOT_RUN**, incl initial/no-op/edit/move-delete/empty, duplicate keys, unchanged first version, revoked access, cancellation and simulated transaction rollback |

**No Room schema bump**: `@Database(version = 1)`, `LibraryRow` v1, exported `1.json` and non-destructive `DataBindings` remain unchanged; P06 is the only authorized atomic V1→V2 migration phase for folder/album indexes and metadata fingerprints. No changes to #7 Media3 playback, queue, Voice, DataStore consent, physical music files or telemetry.

## 3. Reconciliation algorithm and failure guarantees

1. `LibraryScanUseCase` admits **explicit** grant/retry/rescan per Issue #5; `RoomLibraryRepository` serializes each request with `scanMutex`, checks current Android permission and snapshots `accessEpoch`, revision and previously verified item set.
2. Publishes `Scanning` without invalidating previous trusted rows while the OS still grants access. `MediaStoreScanner.scan()` enumerates all currently mounted concrete volumes on the IO dispatcher and cooperatively cancels provider queries and cursor processing.
3. If source enumeration **fails or cancels**, no delta writer is called and no stale deletions are committed.
4. `LibraryRowMapper.deriveIndexRows()` filters malformed refs, normalizes and collapses duplicate **identical** refs deterministically. Different source refs are always distinct songs.
5. Recheck `accessEpoch` and OS permission; read existing Room V1 index; compute deterministic `LibraryReconcileDelta` using full row equality. Matching rows are **not written**.
6. `RoomLibraryProjection.applyDelta()` executes stale-key deletes and changed-row upserts under **one** `RoomDatabase.withTransaction`. Every batch checks cancellation + the same OS/epoch guard. If exception/cancellation occurs **inside** the transaction, Room rolls back all its writes; never `DELETE FROM library_items`/reinsert all on the production path.
7. On successful commit, recheck OS grant/epoch, update verified display rows and `LibraryIndexSnapshot` under the same access lock; `Ready` requires >0, `Empty` requires completed scan with zero rows. Revision stays unchanged for a successful no-op when previous status is already Ready/Empty, and advances when rows differ or a grant/error recovery invalidated former paging.
8. If permission is revoked during scan/write, return typed `PERMISSION_REVOKED` and synchronously suppress Ready/playability. Derived rows can remain physically cached but cannot be published until a new authorized reconcile.
9. On ordinary provider/Room error, expose `RecoverableError`/sanitized `LIBRARY_RECONCILE_FAILED` without losing the previous revision or prior already verified row cache; the user may retry/rebuild.
10. Cancellation before transaction commit restores the prior verified status/row set (when OS access still exists). Defensive post-commit/pre-publication cancellation detection invalidates the cached row revision and moves to recoverable rebuild rather than lying that stale metadata is current.

**Atomicity boundary:** SQLite protects all delta writes in one Room transaction. A completed database commit and separate in-memory UI snapshot publication cannot be literally one cross-system ACID transaction; the repository uses revision-gated visibility, epoch checks and fail-closed recovery for their race window. Native P12 race/instrumentation testing is mandatory before production PASS. The v1 table contains only derived data; user-owned media remains untouched.

**Consistency caveat:** old JVM fake `LibraryProjection` objects not yet adapted to P05 have a compatibility `applyDelta` method that may call legacy `replaceAll`; this does not run in production, nor is it proof of native transaction safety. Native Room tests must exercise `RoomLibraryProjection` directly.

## 4. Test matrix prepared, not executed

| Synthetic test scenarios (P05 JUnit source) | P12 obligation |
|---|---|
| Delta additions, row edits and deletion; ID order independence | T02/Room T03: verify actual DAO write count and unchanged-row avoidance |
| Initial 2-song scan and verified Empty initial scan | T02: first revision >0; truthful ready/empty |
| Repeat identical scan; no-op revision | T02: zero SQLite row writes/transaction count |
| One metadata edit only | T02: `insertedOrUpdated == 1` |
| Changed identity/deleted or moved ID | T02: `removedStale == 1`, added rows tracked |
| Provider error after Ready | T02: old cache coherent, RecoverableError/Retry |
| Fake writer exception before commit | T02/T03: no partial new index |
| Coroutine cancellation during source enumeration | T02/T05: no write and restore prior valid state |
| Coroutine cancellation while transaction staged | T02/T03: cancellation rolls back and no stale Ready |
| OS revoke before commit | T02/T05: suppress publishing, no partial commit |
| Regrant without metadata changes | T02: revision advances to reject old paging |
| Concurrent explicit rescans | T02: mutex serializes; no duplicated writes |
| Same song title, different source refs | T02: two independent entries remain |

**Deferred technical proof (P12, no individual phase APK):** Gradle compilation and JUnit, real Room V1 transaction and exception injection, API 29/31/33/35 ContentResolver cancellation semantics, permission revoke/regrant/Settings races, SQLite WAL/flow observer ordering, unexpected removable volumes, 5,000-item scan and scroll/CPU/transaction benchmark, P06 real schema/index migration/queries, #7 selection boundary, and owner M01–M34 physical device run.

The project has **10** T01–T10 technical suites and **34** M01–M34 owner phone tests. **All remain NOT_RUN**. No GitHub Actions workflow, emulator, ADB, APK generation, PR or merge is requested in this P05.

## 5. P05 acceptance gate / open risks

- [x] Source archive fingerprint reverified, actual accepted repo contracts examined, no scope drift.
- [x] Real added/updated/deleted derived-row delta and truthful counter implementation.
- [x] Production Room transactional upsert/delete batches; no full table replacement in normal reconcile.
- [x] Coroutines and native provider query cancellation signals; staged write/permission guards and exception recovery.
- [x] Stable revision behavior, no-op avoidance, prior verified rows kept during allowed refresh and hidden after revoke.
- [x] 15 synthetic JVM cases authored, correctly marked **NOT_RUN**.
- [x] Source-only P05 change; Room V1 and P06 migration boundary preserved; no per-phase APK.
- [ ] **Actual** Room/instrumentation/cancellation/JVM proof, 5k performance and integration across Android versions — **NOT_RUN until P12**.
- [ ] Changed-only MediaStore generation-window optimization and Room V2 fingerprints — **NOT IMPLEMENTED; revisit P06 optimization**.

**Disposition after live readback:** `P05_PASS_SCOPED_SOURCE / P06_READY`. **Never translate this into product Issue #6 AC-03 PASS or final P12 QA PASS.** 
