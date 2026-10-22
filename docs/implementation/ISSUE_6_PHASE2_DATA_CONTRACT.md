# Issue #6 / P02 — Library domain, browse/query and Room migration contracts

**Date:** 2026-10-22 · **Branch:** `feature/6-mediastore-library-browse-search` · **Disposition:** **P02 CONTRACT IMPLEMENTED / READBACK-PENDING**. This phase creates real Kotlin browse model/interface/identity contracts and an authored test fixture. **It does not implement P03 scanning, P05 incremental mutations, P06 Room browse queries/migration, P08/P09 UI, P12 APK, or P12 executed tests.** Do not interpret contract acceptance as runtime Issue #6 acceptance.

**Authority:** original attached English [PRD FR-01](../PRD.md), [MVP US-02](../MVP.md); uploaded `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; [accepted #16 implementation contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) §6.1, [runtime policies](../architecture/RUNTIME_POLICIES.md), [ADR-0003 local first](../adr/README.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md)/[UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md)/[GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md), approved [WF-04](../wireframes/app-states/WF-04_HOME_LIBRARY_READY.md)/[WF-05](../wireframes/app-states/WF-05_SEARCH_RESULTS.md), [HF-04](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md)/[HF-05](../high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md), [P00 audit](ISSUE_6_PHASE0_READINESS_AUDIT.md), [P01 execution policy](ISSUE_6_PHASE1_EXECUTION_CONTRACT.md).

## 1. Verified P02 entry and conflicts

P01 was `PASS_CONTRACT_ONLY`; existing `LibraryRepository` publicly owns `index`, `observeLibrary`, `reconcile`, `search(LibraryQuery)`, `get(MediaRef)`, `resolveVoiceCandidates`. `LibraryItem`, `LibraryIndexSnapshot`, `LibraryReconcileResult`, `MediaRef`, `LibraryScanUseCase` **must remain source-compatible**. `MediaStoreScanner` currently returns ID/title/artist/album/duration, but no volume/folder/album-platform-ID. `LibraryRow` v1 stores title/artist/album/duration and two normalized columns, no folder or grouped query. `RoomLibraryRepository` currently runs `replaceAll` in one transaction and in-memory `projection.all().filter` search. Those implementations are **not** P02 browse-category support, scalable indexed search, or true incremental reconciliation.

**Source-status reconciliation:** dated source PDFs can use `Proposed` ADR status; accepted repository ADRs/current Issue #16 supersede that historical status. #5 owner-accepted/merged with [open deferred QA #73](https://github.com/latticeward-labs/music-copilot/issues/73); #7 still independently owns service playback; #11 depends on eventual indexed artist group but does not authorize fuzzy music execution within #6.

## 2. Approved architecture and unchanged public API

| Type/port | Immutable semantics retained | P02 action |
|---|---|---|
| `MediaRef` | Opaque platform re-query reference, **not a path or live queue**; MediaStore existence is final | **Unchanged**. For multi-volume collisions, P03 may refine codec with compatibility tests, not silently change #16 semantics |
| `LibraryItem` | `ref,title,artist?,album?,durationMs?` | **Unchanged**. Derived groups are additive types, not new service state |
| `LibraryIndexSnapshot` | `revision,status,itemCount,lastError?`, with `Unavailable/Scanning/Ready/Empty/RecoverableError` | **Unchanged**. Revocation must fail closed. `Ready` requires verified >0 and permission, `Empty` requires completed authorized scan |
| `LibraryQuery(text,limit)` | Existing `search()` signature retained, no cloud/network | **Unchanged**. P06 enforces bounded, deterministic search including title/artist/album |
| `LibraryReconcileResult` | scanned/changed/stale/skipped/revision counters | **Unchanged**. P05 will change implementation, not meaning |
| `LibraryScanUseCase` | OS-gated explicit grant/Retry/Rescan + serialization, no UI-owned scan | **Unchanged**, downstream integration in P07 |
| `resolveVoiceCandidates(normalizedTarget,limit=2)` | Existing deterministic #16/#11 seam and bounded 1–2 candidate confirmation | **Unchanged**; no new execution or ambiguity policy |
| `LibraryRepository.browse(request)` | Additional local read port; **no new state owner** | **New, source-compatible default** returns sanitized `LIBRARY_BROWSE_UNAVAILABLE` until P06 overrides with actual Room results. No false `Success(emptyList())` |
| `AppErrorCodes.LIBRARY_BROWSE_UNAVAILABLE` | Existing **Library** error/recovery family (`RECOVERABLE`/`Retry`), no personal metadata | **New safe code**; not a new error owner |

P02 source paths:
- `app/src/main/java/com/latticeward/musiccopilot/data/api/LibraryBrowseContracts.kt` — four categories, closed entry types, bounded page/cursor, stable local group IDs, canonical ordering;
- `data/api/LibraryRepository.kt` — backward-compatible default `browse` method;
- `core/result/AppError.kt` — stable privacy-safe code;
- `data/room/LibrarySchemaEvolution.kt` — explicit **plan-only** schema V2 contract (no installed DB change);
- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase2BrowseContractsTest.kt` — synthetic authored contract tests; execution at P12.

## 3. Four browse projections and member queries

| `LibraryBrowseCategory` | Row exposed in `LibraryBrowseEntry` | Grouping identity | Query/use |
|---|---|---|---|
| **Songs** | `Song(media: LibraryItem)` | Per `MediaRef` (two identically titled songs stay distinct) | Songs list, no groupId, `normalizedTitle ASC, mediaRef ASC` |
| **Artists** | `Artist(id,label,songCount)` | SHA-256 of known/missing normalized artist with explicit type discriminator | Artist list; selecting `groupId` returns its Song members |
| **Albums** | `Album(id,label,artistLabel?,songCount)` | Scoped platform `ALBUM_ID + volume` when valid; otherwise normalized album+artist+volume | Album list; selecting `groupId` returns its Song members |
| **Folders** | `Folder(id,label,songCount)` | SHA-256 of **entire** sanitized media-relative `RELATIVE_PATH` + volume (not last basename) | Folder list; selecting `groupId` returns its Song members |

All categories are contained within **one top-level Home / Library route**, per UF-04 and WF-04. The new `LibraryBrowseRequest(category,page,groupId?)` supports category list or per-group members without inventing a fifth top-level route. In-group members remain `Song` entries, not playback queues.

**Group-key privacy:** SHA-256 local tokens are still internal user-data-derived identifiers and **must not** enter logs, external telemetry, crash metadata, URLs, third-party analytics or public test captures. They may be passed between local repositories/navigation during an active session. Never assume hashing authorizes analytics collection.

**Unknown rules:** actual missing artist/album/folder are displayed as `Unknown Artist`, `Unknown Album`, `Unknown Folder` respectively (future UI localization allowed within existing design). Known metadata with literal text `Unknown Artist` must not be grouped with missing values: missing and present values have different identity tags. Preserve original non-sensitive UI labels locally; `String? = null` remains the absence signal in `LibraryItem`. `P04` owns robust normalization and escaping beyond this contract.

**Folders:** use Android 10+ `MediaStore.Audio.Media.RELATIVE_PATH` where available for app-accessible music; scope by source volume; reject absolute/traversal/invalid paths; show only the last directory name in UI. Distinct `Music/Rock/` and `Podcasts/Rock/` remain separate even if both display `Rock`, distinguished by grouping/navigation context. Do not treat a path as the existence key or log its raw segments. `P03` must obtain folder/volume from MediaStore with capability-aware fallbacks, then `P04` normalize and `P06` persist derived fields.

**Artist/album ambiguity:** normalized artist name is a browse grouping key, not Voice matching/auto-execution. Album grouping prefers platform album ID (scoped to storage volume), reducing accidental splits on compilations; fallback disambiguates album/artist. Actual `ALBUM_ARTIST` availability/platform-specific behavior must be measured by P03/P04, not guessed. The hashed group ID is rebuilt on metadata changes; UI needs stable revision + recovery when membership changes.

## 4. Pagination, deterministic ordering and mutation policy

- `LibraryPageRequest(offset=0, limit=50, expectedRevision=null)`; enforce offset >= 0, limit **1..100**, nonnegative optional revision. P06 must cap underlying SQLite queries, not fetch every item then truncate.
- A `LibraryBrowsePage` has `entries,totalCount,revision,nextOffset?` where totalCount is nonnegative and nextOffset is absent on last page. At **most 100 entries** per returned page; members and category pages share this rule. Future keyset pagination may replace offset internally without breaking the external typed page contract only after compatibility review.
- A paged request carrying an old `expectedRevision` after reconcile must return a **typed stale/recovery result** using the approved Library error family, not mixed rows, stale Ready or an invented empty collection.
- Songs ordering: `normalizedTitle ASC, mediaRef ASC`. Groups: `normalized display label ASC, opaque group ID ASC` (P06 should retain internal normalized sort column; SHA token is only a deterministic tie-breaker). Group members use Song ordering, not implicit queue order.
- Search is local, deterministic and bounded: normalized query across **title, artist and album**; empty query maps to UI Idle (do not fabricate results); blank/no-match distinguishes No Result from Error; storage/permission failure remains typed and does not delete a valid index. P04/P06 decide normalized token matching and actual index plan; `LIKE '%term%'` on a basic B-tree **is not** automatically indexed.
- Revision in `LibraryIndexSnapshot` is repository-accepted generation, **not** an Android permission grant or a durable MediaStore revision. P05 publishes revisions only following committed coherent reconciliation and fresh permission checks; cancellation, overlapping scans and refresh races must not mix page results. P07 preserves screen query/page state where still valid after UI recreation, never stale playability.
- Collision and dedupe: key by opaque `MediaRef` (volume-scoped handling when scanning supports it), not file title, basename, artist name or album display. Exact duplicate source reference is one indexed track; two distinct refs with same metadata remain two Songs.

## 5. Approved V1 → planned V2 Room schema / migration contract

**Current installed reality:** `MusicCopilotDatabase` is **version 1**, with one `library_items` derived table and committed schema `app/schemas/.../1.json`. `Room.databaseBuilder(...).build()` has no destructive fallback. Changing the `@Entity` now without migration+schema would break installed users. Accordingly **P02 deliberately keeps runtime database version, entity and DAO unchanged**. `LibrarySchemaEvolution.STATUS = PLAN_ONLY_P06_IMPLEMENTATION_REQUIRED` prevents pretending a V2 migration ran.

**P06 atomic delivery checklist:** in **one coherent source change**, update Room `@Database(version=2)`, entity fields, DAO/query/index definitions, **register an actual `Migration(1,2)`** in `DataBindings`, export verified `2.json`, and author existing-v1-to-v2 migration tests. No `fallbackToDestructiveMigration`, no unreviewed library-data reset, no shipping a V2 annotation without accompanying migration.

**Proposed V2 columns** (derived `library_items` only): `volumeName`, `relativeFolderPath`, `folderGroupId`, `artistGroupId`, `albumGroupId`, `normalizedAlbum`, `normalizedFolder`, `metadataFingerprint`, `indexedGeneration`. The P06 design must choose safe NULL/default values for existing V1 rows and accurately record the final column types and indexes; **V1 stored rows are preserved** pending permission-approved reconciliation. A V1 → V2 migration cannot reconstruct folder/volume/album-source IDs from columns absent in V1; it must not fabricate them. Mark/migrate old rows as *needs rescan* and suppress any false Ready until a fresh authorized MediaStore source has been reconciled.

**Logical indexes/query obligations:** `(normalizedTitle,mediaRef)`, `(artistGroupId,normalizedTitle,mediaRef)`, `(albumGroupId,normalizedTitle,mediaRef)`, `(folderGroupId,normalizedTitle,mediaRef)`, reconciliation by indexedGeneration/ref. Query plan must be verified with actual Room/SQLite `EXPLAIN QUERY PLAN` and 5,000 synthetic rows at P12. For partial search, measure an FTS strategy versus bounded LIKE; do **not** assert basic indexes make unanchored wildcard matching O(log n).

**Transactional/version policy:** All schema upgrade statements should run inside Room's managed migration transaction, validate v1 schema identity and exported v2 schema, and fail closed with typed recoverable startup error if upgrade fails. Never delete user-owned Like/history tables (none are currently released in V1), user audio, app-owned preferences or queue snapshot on projection failure. Any future app-owned tables need independent additive or preserving migrations; do not implement a generic destructive fallback. After migration re-evaluate Android access; rebuilding derived columns cannot assert platform permission.

**Rollback:** do not reinstall a V1 binary against a released V2 DB and silently delete tables. Fix-forward or controlled backup/migration plan; keep old `1.json` untouched for migration verification. Room V2 schema file is generated with KAPT only when P06 safely implements and builds the actual V2 schema.

## 6. Downstream owners and tests

| Phase | Must consume this P02 contract | Proof owed |
|---|---|---|
| P03 | Query supported `ALBUM_ID`, `VOLUME_NAME`, `RELATIVE_PATH` safely and permission-aware; media refs must re-query | Android 10/12/13+ fixtures and source semantics |
| P04 | Normalize missing values, durable groups, cross-volume/malformed folder input and duplicates | deterministic P12 T01 |
| P05 | Delta and cancellation using per-source fingerprints/index generation; no old Ready after revocation | P12 T02 |
| P06 | Override `browse`, bounded DAO pages, category/member grouping, local title/artist/album search, actual V1→V2 migration | P12 T03/T04/T07 |
| P07 | Invalidate stale index/page revision after scan or Settings return | P12 T05/T06 |
| P08/P09 | Show truthful grouped/empty/loading/error/search states, Lazy lists and accessible text | P12 T06 plus M05–M24 |
| P10/#7 | After choosing Song, re-query source and command through service-owned `PlaybackGateway`; #7 actual Media3 remains independent | P12 selection boundary checks, #7 full playback not falsely passed |
| P11/P12 | Author/execute complete T01–T10 suite, migration tests, 5k, OS matrix, one consolidated APK + M01–M34 physical tests | separately verified actual execution only |

**P02 test file** `Issue6Phase2BrowseContractsTest.kt` exercises artist collisions/missing identity, album platform ID and volume scoping, full relative-path folder identity/display, typed four-category models, bounds, stable sorting, duplicate-title preservation and fail-closed default API with synthetic inputs. **Authored ≠ executed**. P12 must record actual JVM run and exact source SHA; no Actions, emulator, ADB, phone or APK is dispatched in P02.

## 7. P02 gate / release truth

- [x] Same authenticated private repo/feature branch and completed P00/P01 contracts read.
- [x] User-uploaded 12-PDF authoritative source hash and its concrete FR-01 / US-02 / ADR-0003 statements reconciled.
- [x] Existing #16 `LibraryRepository` and #11 candidate signatures retained; no backend or alternate playback/data ownership.
- [x] Typed **four-category** browse and per-group member request/response implemented in code, with bounded revision-aware pagination contract.
- [x] Stable opaque identity rules for artist/album/folder, unknown labels, normalization/tie-breaker and duplicate tracks implemented.
- [x] Source-compatible fail-closed repository read method and sanitized error code added.
- [x] Concrete V1→V2 migration plan and preservation/rollback policy recorded; **V2 not misrepresented as installed**.
- [x] Synthetic test fixtures authored, scheduled for P12; no premature APK or full test claim.
- [ ] P03 platform extraction, P05 delta, P06 actual Room migration/queries, P08/P09 UI, P12 full tests: **deliberately not claimed**.

**Expected disposition after actual GitHub file read-back:** `P02_PASS_CONTRACT_ONLY / P03_READY`. All 12 original Issue #6 AC checkboxes remain unchecked; downstream verification is required before final acceptance. New scope changes or schema owner changes require approved review, not silent contract drift.
