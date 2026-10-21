# Issue #6 — P08 Home / Library four-category Compose implementation

**Date:** 22 October 2026 · **Branch:** `feature/6-mediastore-library-browse-search` · [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6)  
**P08 result:** **SOURCE_IMPLEMENTED / STATIC_REVIEW_PASS_SCOPED**. **Not a native/visual/performance PASS**: Android Gradle, JVM/UI instrumentation, screenshot, accessibility, 5,000-song scrolling and actual APK have NOT_RUN. The final APK remains P12-only.

## 1. Sources actually read

- The supplied `/mnt/data/MSM2(1)(1).rar` has SHA-256 **`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`** (same source as P00–P07), containing the English PRD/MVP, ADRs, architecture, and user-flow authority. The extracted source PDFs include PRD FR-01 and 5,000-song scroll requirement, MVP US-02 and UF-04 handoff.
- Accepted GitHub [WF-04 Ready](../wireframes/app-states/WF-04_HOME_LIBRARY_READY.md), [Scanning](../wireframes/app-states/WF-04_HOME_LIBRARY_SCANNING.md), [Empty](../wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.md), [Recoverable Error](../wireframes/app-states/WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.md), [HF-04 approved Ready](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md), and [Night Signal foundations](../design-system/foundations/FOUNDATIONS.md). The hierarchy remains AppBar + More → four category tabs → local rows → service-only mini-player → four bottom destinations.
- Issue #16 accepted service and feature boundaries, ADR-0003 MediaStore local-first, UF-03/04 GS-01/02, P02 Browse contracts, P06 Room indexed SQL query port, and P07 verified readiness/mutation ownership.
- [Binding execution plan](ISSUE_6_PHASED_EXECUTION_PLAN.md) §P08: real Compose Library + view model, four browse tabs, nested groups, count, Empty/Retry/Rescan, safe Back, stable lazy IDs, 48dp touch areas, 200% scaling, and no invented MediaSession Playing.

## 2. Actual Kotlin and Android resources committed

| Path | Implementation |
|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/feature/library/LibraryBrowseViewModel.kt` | `LibraryBrowseViewModel` and typed `LibraryBrowseUiState`; handles 4 categories, group/member drilldown, Back, 50-item bounded pages, `nextOffset`, revision-pinned and coroutine-generation-gated requests. Cancels previous work on category/group/revision change and permission loss. In-memory only: no user metadata, OS-grant, group ID or selected source ref persisted across process death |
| `app/src/main/java/com/latticeward/musiccopilot/feature/library/LibraryBrowseScreen.kt` | Real WF/HF-04 Night Signal Compose UI with `Library` heading/More, horizontal four category `FilterChip` selector, grouped/song `LazyColumn` with stable keys, bounded auto pagination, category member pages, visible count, Loading/Empty/Recoverable Error/Permission Unavailable panels, explicit Scan/Retry/Rescan, 48dp minimum interactive targets, scalable local text and nested/back handling |
| `app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt` | Replaces the Library `FirstLaunchBoundary` shell route with actual `LibraryBrowseScreen`. Navigation-scoped `ViewModelProvider.Factory` injects the process `LibraryRepository`; Settings goes to existing Settings route. Back from member pages returns to collection list before normal top-level exit |
| `app/src/main/java/com/latticeward/musiccopilot/app/MainActivity.kt` | Observes authoritative `playbackGateway.snapshot` with lifecycle and provides actual app-scoped repository, `LibraryIndexSnapshot`, playback connection and service snapshot to NavHost, retaining P07 verified readiness and permission check |
| `app/src/main/res/values/strings.xml` | Approved English-first Library/Songs/Artists/Albums/Folders/More, group count, loading, no-results, retry, stale index, bottom navigation, local selection and privacy-safe error descriptions |
| `feature/library/PackageBoundary.kt` | Deleted obsolete placeholder after adding actual classes |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue6Phase8LibraryBrowseViewModelTest.kt` | 12 **authored, NOT_RUN** JUnit tests for 4 tabs, page bounds, 120-song pagination, nested Back, stale/out-of-order/cancelled pages, granted→revoked, manual error, no playback or automatic scan |
| `app/src/androidTest/java/com/latticeward/musiccopilot/foundation/Issue6Phase8LibraryComposeInstrumentedTest.kt` | 4 **authored, NOT_RUN** Compose tests for visible tabs, group drilldown/back, non-Playing local selection, denied permission hiding rows |
| `docs/implementation/ISSUE_6_PHASE8_UI_QA_MATRIX.md` | 20 synthetic screenshot/accessibility/manual scenarios for P12 (all **NOT_RUN**) |

## 3. Exact boundaries and user-visible behavior

- **Songs:** category displays stable local MediaRef-backed song rows and artist/album metadata, 50/page with next offset. Distinct same-title tracks have different keys; a tapped song opens an explicitly **Not playing** preview. P10 owns validated selection-to-PlaybackGateway; P08 does not send `PlaybackCommand` or fabricate a successful play.
- **Artists / Albums / Folders:** SQL-sourced group summary count and names; tap drills down into that group's `Song` members with pinned revision; Back returns to the parent grouping. No all-Room-rows loaded in Compose.
- **Status:** P07 verified OS permission + repository revision governs visible Ready/Empty. Scanning has spinner and no duplicate-scan button, Empty is not conflated with denial, recoverable index failure allows manual Retry, verified permission loss hides indexed rows. Transient category read errors have a retry; stale/revision-changing page errors prompt an explicit Rescan.
- **App bar:** Library heading and More (existing Settings action) as approved. Rescan adjacent to visible count. Four bottom destinations are Home/Settings enabled; **Search** remains disabled until P09 and optional **Liked** remains disabled/P2. This is deliberately explicit rather than inventing unimplemented destination behavior.
- **MiniPlayer:** rendered only if `PlaybackConnectionState.Ready` AND a non-null service `currentItem` exist; `Playing/Paused` label comes from service snapshot. The local library never owns MediaSession state, playback queue, or track position. Full Player belongs to #7/P10 handoff.
- **Privacy:** personal music metadata only in local UI. No search/catalog networking, cloud backup, logs, telemetry, physical deletion, or user-owned DataStore media caching. Opaque tokens used solely as row keys.
- **Layout:** real `LazyColumn` (no `Column` for 5,000 media rows), stable keys, 48dp min touch controls, horizontal tab scrolling, readable text reflow. Whether actual 200% scale is clip-free and 5,000 rows scroll without jank is **not proven** by these source checks and remains a mandatory P12 test.

## 4. Evidence/remaining blockers

- P08 scope: authenticated GitHub source readback + per-file SHA and structural invariants recorded separately in `ISSUE_6_PHASE8_VALIDATION.json`.
- **No Gradle Kotlin/Room compilation, Compose preview rendering, native UI semantics test, TalkBack run, 5,000-row measured benchmark or screenshot executed.** 12 JVM and 4 native Compose tests are source only.
- P06 remains **IN_PROGRESS** because genuinely compiler-generated Room V2 `2.json` is missing and native V1→V2 migration/DAO and SQL query tests are unexecuted. P07 remains PASS_SCOPED, not device-verified.
- The agreed **one consolidated installable APK** must be built from all P00–P11 work at P12, then run T01–T10 and owner M01–M34 on a real phone, plus screenshot/semantics matrix P08-S01..20. Do not launch per-phase GitHub Actions/PR or claim a product acceptance checkbox PASS now.
- #7 service-owned actual playback remains open; Search P09 and validated selection P10 not yet implemented. Issue #5 inherited #73 QA debt is separate and never silently passed.

**Disposition:** `P08_PASS_SCOPED_SOURCE / P09_READY` after live GitHub readback, not a full acceptance PASS.
