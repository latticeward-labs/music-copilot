# Issue #6 — Phase P09: Local Search SQL, Compose and navigation integration

**Date:** 22 October 2026 · **Branch:** `feature/6-mediastore-library-browse-search` · **Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6) (OPEN).  
**Disposition:** **SOURCE_IMPLEMENTED / 51-OF-51 STATIC SOURCE CHECKS PASS / NATIVE TESTS PENDING P12**. This is **not** an executed, compiled, screenshot, measured-performance or product-acceptance PASS.

## 1. Source authority and no-scope-change guarantees

Actual supplied archive `/mnt/data/MSM2(1)(1).rar` exists and SHA-256 was verified at `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, identical to the 12 English authoritative PDFs from P00–P08. PRD FR-01 / MVP US-02, ADR-0003 local-first OS ownership, accepted Issue #16 architecture, UF-04 / GS-02 and P06 Room/search ports remain the source of truth. Design references reviewed on the actual feature branch:

- `docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md`
- `docs/wireframes/app-states/WF-05_SEARCH_NO_RESULTS.md`
- `docs/wireframes/app-states/WF-05_SEARCH_ERROR.md`
- `docs/high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md` (approved, 360×800dp / 1080×2400px reference image)
- Corresponding `WF-05/frames/HF_FRAME_WF-05_SEARCH_NO_RESULTS.md` and `...SEARCH_ERROR.md`, approved Night Signal foundations/token set.
- `docs/implementation/ISSUE_6_PHASED_EXECUTION_PLAN.md` P09: SearchField → Idle/Results/No Result/Error, Back/Clear/Retry, preserve active query on error/rotation, row actions, local derived index ONLY; no automatic fuzzy-voice artist execution.

No PRD, UX hierarchy, ADR, original Issue #6 acceptance checkboxes, Voice, user files or MediaSession ownership was changed.

## 2. Real production code committed to GitHub

| Path | Real implementation |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/data/api/LibrarySearchContracts.kt` | `LibrarySearchResults`: bounded **1..100** total mixed entries, actual library revision. Reuses closed `LibraryBrowseEntry` Song/Artist/Album types (Folder has generic compatibility). No queue/playability claims |
| `data/api/LibraryRepository.kt` | Backward-compatible `searchResults(query, expectedRevision)` API; pre-P09 or fake providers **fail closed** with typed error rather than incorrectly claiming No Results. Existing `search(query)` voice/public port remains intact |
| `data/room/LibraryDao.kt` | Three bounded **local SQLite** query projections: title-matching tracks, normalized artist group names, normalized album group names. Group result counts and stable display labels come from SQL `GROUP BY` and `COUNT(*)`. Each uses `LIMIT`, deterministic title/group order and literal `instr()` (not `LIKE` wildcard text injection) |
| `data/room/LibraryProjection.kt` | `searchSurface` reads these three bounded projections inside **one Room read transaction**. A balanced cap of 60 from UI means max 30 tracks, 15 artist groups, 15 album groups; exact number may be less. No full 5,000-song Kotlin in-memory hydrate per keystroke |
| `data/repository/RoomLibraryRepository.kt` | Production override with OS-grant pre/post-check, `Ready/Empty` accepted-index gate, optional expected revision, normalization, cancellation propagation, typed `LIBRARY_BROWSE_STALE` or recoverable error; real `Room` query only; no backend, logs or MediaStore rescan |
| `feature/search/LocalSearchViewModel.kt` | Process-local Search surface state `Idle, Searching, Results, NoResults, Error, Stale, Unavailable`. Typing debounced **280ms**, query cap 120 chars, max 60 result entries, generation/cancellation guard, explicit Clear/Retry, error/NoResults preserving query, OS revoke hiding personal rows. Nav-scope ViewModel preserves query across rotation **while Search stays active**; does not write query to SavedStateHandle, DataStore, Room or analytics |
| `feature/search/LocalSearchScreen.kt` | Real approved WF-05 / Night Signal UI: Search+Back bar, local editable SearchField/Clear, verified written result count, lazy typed Artist/Song/Album rows, No local results, Search failed/Retry, idle/permission/loading, 48dp min target intent, four bottom slots Home/Search/Liked/Settings, synthetic-not-playing song preview |
| `app/navigation/AppNavHost.kt` | Activates existing typed `Destination.Search` route with a Navigation-scoped injected ViewModel, Search navigation state and permission fail-closed; Back returns to Library, actual Settings route works; tapping Artist/Album safely hands group token to Library instead of starting playback |
| `feature/library/LibraryBrowseScreen.kt` | Enables P09 Search bottom tab for verified **Ready or Empty** index; consumes an authorized group Open callback from Search and opens real Artist/Album member listing via P08 ViewModel, preserving P08 Back |
| `app/src/main/res/values/strings.xml` | 19 English-first WF-05 labels/messages for query, Clear, count, No Results, Search Error, stale query and local-only group result types; strings resource audit no collisions |
| `feature/search/PackageBoundary.kt` | Removed inert placeholder now that real feature classes exist |
| `app/src/androidTest/.../Issue6Phase8LibraryComposeInstrumentedTest.kt` | Adjusts one P08 regression: Search is **enabled** after P09 rather than disabled |

**Song selection scope:** P09's track row deliberately says **Select** and displays **Not playing**. Approved HF-05 sample shows `Play` for the future completed feature, but falsely sending a command without verified re-query/playback acceptance would contradict #7/ADR-0002. P10 is explicitly responsible for `LibraryRepository.get(ref)` on selection and service-owned `PlaybackGateway` acknowledgement. P09 must NOT manufacture Playing. Artist/Album `Open` is a browse-only action.

## 3. Behavioral truth table

| Authoritative condition | Search display | Safe recovery |
|---|---|---|
| Verified Ready or Empty, no query | Idle / editable | Type local query |
| Verified Ready, nonempty query waiting | Searching (debounced) | Edit/Clear |
| Successful local SQL returns nonempty entries | Results + actual capped row count | Open Artist/Album group; Select track only |
| Successful local SQL returns zero entries | **No local results**, query preserved | Edit/Clear |
| Typed local DB query failure | **Search failed**, query preserved, not No Results | Retry/Edit/Clear |
| Query returns older index revision | Stale/failed state; no rows | Retry or Back to Library/explicit Rescan |
| OS access revoked or scan invalidates trusted snapshot | Unavailable; no cached personal results, Search navigation returns to permission recovery as appropriate | Review access; no auto-regrant/scan |
| Rotation with Nav Search still active | Same ViewModel query, pending debounce bound to lifecycle | Continue without duplicate MediaStore scan |
| Back from Search | Library; no playback mutation | Search text discarded once surface scope exits |
| P2 Liked unavailable | Disabled Liked navigation slot | No invented Likes implementation |

**Important performance nuance:** SQL `instr` substring matching may still inspect O(N) indexed rows; `LIMIT` avoids Kotlin-side unbounded materialization but is **not proof of indexed full-text search or 5,000-row smoothness**. No fake measurements/benchmarks were logged.

## 4. Authored test evidence (NOT_RUN)

- `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase9LocalSearchViewModelTest.kt` — **14 authored JVM** synthetic cases: no premature query; delay/dedup; Clear; NoResults; error/Retry; stale response; revocation; cancel on changed revision; typed group/track; 120-char bound; rotation query retention; Empty index.
- `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase9SearchComposeInstrumentedTest.kt` — **7 authored Compose** interaction/accessibility cases, including Search/Back/Clear, 3 result kinds, No Results, recoverable Error, group Open, selection not playing, revoked permission.
- `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase9SearchSqlInstrumentedTest.kt` — **4 authored native SQLite Room** tests for grouped result aggregation, verified empty, literal wildcard %, max 60.
- `docs/implementation/ISSUE_6_PHASE9_SEARCH_QA_MATRIX.md` — **22 synthetic screenshot/semantic/manual/performance scenarios, ALL NOT_RUN**.
- Authenticated GitHub live source readback: **51 static invariants PASS**, captured in separate `ISSUE_6_PHASE9_VALIDATION.json`.

**Native testing MUST follow complete Kotlin/Room KAPT compilation.** Existing P06 unresolved missing compiler-generated `app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/2.json` and Room V1→V2 native migration still block final Issue #6 acceptance. Compile failures or Room SQL parser disagreements will require actual source correction before final APK. P09 did **not** run Gradle, JVM, Compose, native SQLite tests, TalkBack, 200% scaling, 5,000-row benchmark, screenshot, CI, emulator or phone. This source-level review cannot certify runtime semantics.

## 5. Phase gate, delivery and downstream ownership

- P09 source completed and eligible for downstream **P10** implementation: **PASS_SCOPED_STATIC_SOURCE**, not final fully tested PASS.
- **P10:** verified song selection re-query and actual `PlaybackGateway` command/acknowledgement; **#7** MediaSession/Media3 service playback remains separate.
- **P11:** additional deterministic fixtures, lifecycle and fail-closed hardening; P06 schema closure still due at P12.
- **P12:** one **single consolidated installable APK** from aggregate P00–P11, full T01–T10 JVM/native/lint/CI and one-owner-phone M01–M34 manual checklist; screenshot/semantic/talkback/5k QA, exact Room compiler `2.json` with native migration. Only verified issue product ACs may then be checked. No paid GitHub Actions used on intermediate P09.
- Original Issue #6 stays OPEN with all 12 product AC boxes unchecked (Likes P2 deferred). No PR, merge, main branch change, Android permission, network, cloud, remote speech or service-owned media data mutation from P09. Issue #73 deferred Issue #5 tests remain separate.
