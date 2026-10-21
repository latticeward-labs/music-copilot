# Issue #6 — Phased Execution Plan: MediaStore Library, Browse & Search

**Status:** PLAN RECORDED / IMPLEMENTATION NOT STARTED  
**Source of request:** owner instruction, 22 October 2026.  
**Target:** [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6) · **Branch:** `feature/6-mediastore-library-browse-search` · **Base:** `main`.  
**Special delivery rule:** **ONE consolidated installable APK ONLY IN THE LAST PHASE (P12)**. No per-phase APK handover or per-phase owner phone tests. All P00–P11 implementation is integrated into the SAME codebase before this final artifact is built. Automated tests may be authored alongside their implementations; final full execution is P12. No Actions workflows or expensive device tests are triggered merely by recording this plan.

## 1. Source hierarchy and discovered baseline

1. **Authoritative product:** uploaded `MSM2(1)(1).rar` English PRD (FR-01), MVP (US-02, Library/Search, 5,000 files) and the other official PDFs. Textual archive review performed for this planning task; dated archive ADR status language is superseded by accepted repository ADRs.
2. **Current GitHub architecture:** `docs/SOFTWARE_ARCHITECTURE.md`, `docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md`, `docs/architecture/RUNTIME_POLICIES.md`, accepted ADR-0001..0006 and Issue #16.
3. **User flow:** `docs/user-flows/UF-02_PERMISSION_RECOVERY.md` (if applicable), `UF-03_LIBRARY_READINESS.md`, `UF-04_BROWSE_AND_SEARCH.md`; GS-01, GS-02 and TS-06 evidence, with UF-05 as the *later* playback handoff.
4. **Design:** Issue #17 approved `WF-04` / `HF-04_HOME_LIBRARY_READY` and `WF-05` / `HF-05_SEARCH_RESULTS`, associated empty/scanning/error frames, Night Signal tokens/components and Compose handoff.
5. **Actual main code starting point:** MediaStoreScanner / LocalAudioSource, Room LibraryRow/LibraryDao/RoomLibraryProjection, RoomLibraryRepository, LibraryRepository, LibraryScanUseCase, Issue #5 permission/readiness UI already exist as foundational code. `feature/library` and `feature/search` have only PackageBoundary placeholders; browse/search screens are not delivered. Current Room projection offers `replaceAll`; it is not evidence of a fully incremental 5,000-item implementation.
6. **Dependencies as checked 22 Oct:** Issue #4 and #5 are **closed**; Issue #17 design handoff is accepted. Issue #5 **deferred independent/technical verification** is separately tracked by [Issue #73](https://github.com/latticeward-labs/music-copilot/issues/73). This work must integrate with #5 and not describe #73 as passed. Issue #7 service-owned full playback remains **open**.

**Scope lock:** Native Android 10+ (API 29+), Kotlin, Jetpack Compose, Android MediaStore as existence truth, Room as rebuildable derived index, DataStore for small app-owned settings only. No account/backend/streaming/cloud/catalog/upload/download; no app-owned live playback queue, no P2 Like unless separately capacity-approved; no Voice implementation in this issue.

## 2. Operational phase gates

Each phase has four required fields: **implementation tasks, files/evidence, review checks, PASS gate**. Do not mark a phase PASS merely because a document exists. Keep the issue open until P12 final gate.

### P00 — Source audit, traceability, Definition of Ready
- Read current PRD FR-01, MVP US-02, UF-02/03/04, GS-01/02, TS-06, #4/#5/#16/#17/#73 and source code; inventory actual done vs placeholders/gaps and all 12 Issue #6 acceptance criteria.
- Create evidence/traceability matrix `FR-01 → UF-03/04 → WF-04/05 → HF-04/05 → component/contract → testcase`; note #7 handoff boundary.
- Output: `docs/implementation/ISSUE_6_PHASE0_READINESS_AUDIT.md` plus link register.
- **PASS:** every dependency classified as ready/deferred/blocked and all P0 gaps named without claiming unrun tests.

### P01 — GitHub execution setup and scope freeze
- Work only on `feature/6-mediastore-library-browse-search`; record branch/main SHA, change policy, phase evidence structure, rollback and privacy-safe PR template.
- Establish stable IDs for test cases, files changed and evidence. Set open working status; never merge before final verified gate.
- Output: `docs/implementation/ISSUE_6_PHASE1_EXECUTION_CONTRACT.md`.
- **PASS:** no premature APK/CI run, issue-to-branch links and traceable delivery agreement.

### P02 — Library domain, repository & Room migration contracts
- Reconcile existing `LibraryRepository`, `LibraryItem`, `LibraryQuery`, `LibraryIndexSnapshot`, `MediaRef` and `LibraryScanUseCase` with approved Issue #16 contracts and Issue #11 future artist-matching consumer.
- Define derived `Song / Artist / Album / Folder` browse projections; folder identity/display rules (Android 10+ `RELATIVE_PATH` or safe equivalent), unknown-artist/album labels, ordering, deterministic search query projections.
- Define pagination/limit, index revision, stability, duplicate media references, schema-version migration and non-destructive rebuild. Any public contract extension must be backwards-compatible or explicitly architecture reviewed.
- Output: updated models/interfaces, migration plan, `ISSUE_6_PHASE2_DATA_CONTRACT.md`.
- **PASS:** four browse categories are queryable in principle; migrations preserve approved app-owned data; no queue ownership leaks.

### P03 — Permission-aware Android MediaStore scanner
- Implement robust supported-audio enumeration and per-item requery on IO; Android 10–12 uses the correct granted read-media capability and 13+ uses `READ_MEDIA_AUDIO` (request remains Issue #5-owned).
- Read supported metadata including stable IDs, title, artist, album, duration and folder information without exposing filesystem paths to logs. Use safe content URI handling; close Cursor; skip non-music/unreadable items consistently.
- Explicit denied/revoked mid-scan behavior; platform truth always wins over cached grants. No implicit scan before user-approved access.
- Output: scanner + adapter-level fixtures/tests (author now, execute full suite in P12).
- **PASS:** no unauthorized scan, missing item is safely re-queried, platform errors are typed and sanitised.

### P04 — Normalization and deterministic indexing rules
- Normalize missing/unknown title, artist, album, folder, duration and invalid values for display and search; do not crash on malformed metadata or duplicate display names.
- Use stable keyed references, deterministic locale/case normalization and sorting; preserve only required local metadata. Distinguish two tracks with identical titles.
- Output: mapper/normalizer and unit fixtures.
- **PASS:** malformed and duplicate inputs yield safe, deterministic derived rows with no personal metadata in diagnostics.

### P05 — Incremental, cancellable and transactional reconciliation
- Replace current wholesale `replaceAll` behavior with a measured delta/transactional reconciliation strategy (including added, updated, removed, moved/deleted media).
- Ensure a cancelled or failed scan never leaves a partially committed unusable index; valid existing revision stays available when access allows, while revoked access immediately removes false Ready/playability.
- Serialize overlapping scans and handle permission-loss, process recreation and refresh races; use cancellable structured concurrency (no `GlobalScope`).
- Output: reconciliation logic, atomically published revision/status, test cases for cancel/interrupt/revocation.
- **PASS:** initial + later scans return truthful counts; cancellation and failure leave coherent index; successful rescan removes stale entries.

### P06 — Room queries and local search/browse read performance
- Implement indexed/local queries for Songs, Artists, Albums, Folders and category-member lists, plus title/artist/album search with bounded result count.
- Keep search off Main, avoid repeated full scans per keystroke; review `EXPLAIN QUERY PLAN` and 5,000-row behavior; preserve main-thread smoothness.
- Output: DAO/projection/repository implementation, migrations and deterministic Room tests.
- **PASS:** accurate counts/groupings and stable order; all read paths satisfy architecture scope and 5,000-item performance policy.

### P07 — Scan readiness and lifecycle state integration
- Wire Issue #5 first-use, permission recovery and `LibraryScanUseCase` to `Scanning / Empty / Ready / RecoverableError / Unavailable` with single source of truth.
- Define manual Rescan and Retry, grant-after-denial, revoke-in-Settings and return, background/rotation/process reentry without double scan or stale Ready.
- Output: app composition wiring, ViewModels/state, recovery tests.
- **PASS:** each state reflects verified Android + repository truth; scan is never fabricated or silently repeated.

### P08 — Compose Home / Library with four internal browse views
- Replace `feature/library/PackageBoundary.kt` placeholder with real UI/ViewModel. Implement **Songs, Artists, Albums, Folders**, nested collections, category selection, visible count/empty/retry/rescan, safe Back.
- Match approved `WF-04/HF-04`, Night Signal tokens and component hierarchy; observed MiniPlayer must NEVER synthesize Playing or own MediaSession state.
- Large collections must use lazy lists/stable keys, 48dp touch targets and scalable layouts.
- Output: Compose screens + UI state + screenshot/semantic test definitions using synthetic media only.
- **PASS:** four categories navigate correctly; 5,000 items scroll without blocking; all approved visible states reachable.

### P09 — Compose Search, result handling and query preservation
- Replace `feature/search/PackageBoundary.kt` with Search ViewModel/UI; local Idle → Result/No Result/Error; SearchField, Clear, Retry, Back, accessible result count and row actions.
- Query only local derived index, never a cloud endpoint. Keep query editable after No Result/Error; preserve active query and relevant navigation context across rotation.
- Align `WF-05/HF-05` and UF-04; avoid unapproved implicit fuzzy artist execution (Issue #11).
- Output: Search UI/use case and deterministic fake-index UI tests.
- **PASS:** search returns relevant tracks/groups, no-result/error preserve context, no mutation to verified library on query error.

### P10 — Common selection validation & #7 playback boundary
- For tap on song or a search result: recheck current permission and MediaStore reference, reconcile stale/deleted/moved item, and only submit a valid selection to the approved `PlaybackGateway`/service boundary.
- Preserve browse/search state until the MediaSessionService acknowledges a playback start; never create a UI Player or claim Playing optimistically.
- When #7 playback implementation is not yet ready, show correct *recoverable unavailable/pending* handoff and unit-test an accepted/rejected fake gateway. Actual playback/queue/lock-screen/Bluetooth acceptance belongs to **Issue #7**, not Issue #6.
- Output: selection use case + boundary-level tests + explicit #7 handoff record.
- **PASS:** stale and revoked items cannot fabricate playback; handoff contract exists with controlled failure.

### P11 — Quality hardening & final-test preparation (NO APK handover)
- Author JVM unit, Room integration, scanner/permission instrumentation, Compose semantics/UI and 5,000-row benchmarks; prepare privacy/no-network checks, Lint/Detekt/Spotless where configured, architecture-direction check and no-PII logging scan.
- Review 48dp, TalkBack order, 200% font size, error announcements, empty/scanning/non-color-only semantics. Review cancellation/recreation, offline operation, 5,000 list and no ANR.
- Prepare the exact synthetic QA music dataset, scenario steps, fault injection and `ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md`; do not falsely mark execution PASS.
- Output: authored test suite / review ledger / final checklist. **No per-phase APK or phone tests.**
- **PASS:** all final cases prepared with expected outcomes and build/test instructions; readiness for final integrated gate.

### P12 — FINAL INTEGRATED TEST, ONE APK, OWNER PHONE QA & ISSUE CLOSEOUT
**This is the only APK-delivery / full testing phase. P00–P11 changes are all integrated first.**
1. Freeze single branch HEAD SHA; inspect code/traceability and run final JVM/Room/instrumentation/static/security/privacy suites, where environment supports them. Android API 29/31/33/35 coverage should be executed via available emulator/CI or recorded explicitly as missing — no invented cross-version PASS. Resolve P0/P1 regressions before shipping.
2. Run non-blocking-main-thread and **5,000 audio-record** load/scroll benchmark using synthetic/generated records or a lawful device data set; keep personal track names/paths out of GitHub.
3. **Build exactly ONE consolidated installable APK** from the frozen final integrated source (not one APK per phase). Use the existing approved CI/Gradle entry point once. Record source SHA, workflow run, artifact ID, APK pathname and SHA-256; if a defect requires a correction/rebuild, invalidate the former candidate and declare a *new* candidate rather than claiming the old APK passed.
4. Supply the owner this **one APK** and [complete owner checklist](ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md). Owner installs it on **one actual physical phone**, tests the complete Issue #6 end-to-end sequence and reports each case `PASS / FAIL / BLOCKED / NOT_RUN`; record HONOR X7d Android 15/API35 only if owner confirms that phone was used.
5. Mark only actually executed tests PASS. A test requiring 5,000 real audio items cannot be passed by a phone with 20 tracks; use the separate real/instrumented benchmark evidence. Full actual service playback tests are gated by Issue #7 and must be labelled dependency-blocked if unimplemented.
6. Create `ISSUE_6_FINAL_ACCEPTANCE_AUDIT.md` with each of the original Issue #6 acceptance criteria mapped to code/CI/phone evidence, run IDs and absence/presence of unresolved P0/P1 bugs; independent review status must be truthful.
7. Only if acceptance is actually met (or explicitly amended by the owner with labelled verification debt): open final PR `feature/6... → main`, complete checks/review/merge and close #6. If not, leave it open with blockers.

**P12 PASS = one frozen integrated final APK + completed owner phone checklist + evidence-backed automated/5,000/performance/accessibility/privacy gates + valid architectural boundaries + final audit + verified merge/close.** No fake run IDs, fabricated device evidence or inherited PASS from Issue #5. One physical phone for owner manual QA is the owner-requested Issue #6 plan; this does not reduce separate release-qualification Issue #13 or deferred Issue #73 requirements.

## 3. Phase ordering and deliverables

`P00 → P01 → P02 → P03 → P04 → P05 → P06 → P07 → P08 → P09 → P10 → P11 → P12 (the only final APK + integrated QA)`.

Delivery docs under `docs/implementation/` (phase-specific audit + final report); code in the current `app/src/main` architecture; tests in `app/src/test` and `app/src/androidTest`. Use the existing Issue #16 public ports without a new backend. P2 Likes is intentionally deferred unless owner authorizes and capacity permits; no need to test unfinished P2 Likes to complete #6 core acceptance.

## 4. Issue #6 acceptance-to-phase matrix

| Original acceptance | Implementation phases | Final evidence in P12 |
|---|---|---|
| Consent-before-MediaStore discovery | P03/P07 | Phone M01–M04, permission instrumentation |
| Room songs/artists/albums/folders | P02/P04/P06/P08 | M05–M10, Room tests |
| Incremental cancellable scanning | P05 | M11–M14, cancellation/race tests |
| Scanning/Empty/Ready/recovery | P07/P08 | M03/M04/M11/M15/M16 |
| Search result/no result/error | P06/P09 | M17–M22, search unit/UI |
| Rotation/UI state | P07/P08/P09 | M23–M24, UI lifecycle |
| 5,000-file scan/scroll | P05/P06/P08/P11 | 5k perf instrumentation + M25 if owner data supports it |
| Malformed/corrupt metadata | P03/P04/P05 | M26, mapper/reconcile tests |
| No personal-file titles in analytics | all, P11 | privacy static/runtime check |
| Repository and query tests | P02/P06/P11 | executed JVM/Room suites |
| TalkBack/large text/list/loading | P08/P09/P11 | M27–M29 + UI semantics |
| Optional P2 Like | defer | Not in P0 completion criteria (scope decision recorded) |

## 5. Test/result and evidence policy
- Follow `docs/implementation/ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md`. Owner reports are **OWNER_REPORTED_PHYSICAL**, automated jobs are **CI_VERIFIED** only after real success links. Blank = `NOT_RUN`, not PASS.
- Synthetic filenames only in tests/screenshots. No real screenshots, raw logs, media names, folders, search terms, media references, content URIs or identifying device details published without explicit privacy review.
- Validation states: `PASS`, `FAIL`, `BLOCKED_DEPENDENCY_7`, `NOT_RUN`. Issue #73 technical debt stays tracked separately; do not mark it resolved by this work without executing its own outstanding tests.
