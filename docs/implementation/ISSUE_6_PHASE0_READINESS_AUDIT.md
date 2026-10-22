# Issue #6 / P00 — Source, prerequisites, traceability and readiness audit

**Date:** 2026-10-22  
**Disposition:** **P00_PASS_AUDIT_ONLY — READY_FOR_P01**  
**Scope of PASS:** Source identity, documentary traceability, dependency classification and actual-code gap inventory are completed. **None of the 12 Issue #6 product acceptance criteria is thereby passed.** No Issue #6 code implementation, build, automated test, physical-phone QA, APK, PR, merge or issue closeout is claimed.

- **Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6); execution [P00–P12 plan](ISSUE_6_PHASED_EXECUTION_PLAN.md); [single-APK P12 protocol](ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md).
- **Working branch:** [feature/6-mediastore-library-browse-search](https://github.com/latticeward-labs/music-copilot/tree/feature/6-mediastore-library-browse-search).
- **Inspected main HEAD before P00 changes:** [bb022341bb12a1b12c22a078a70b8bf8217d2176](https://github.com/latticeward-labs/music-copilot/commit/bb022341bb12a1b12c22a078a70b8bf8217d2176).
- **Inspected feature HEAD before P00 changes:** [3693f209cc665997edccf9ba9d0f7a867acad7c9](https://github.com/latticeward-labs/music-copilot/commit/3693f209cc665997edccf9ba9d0f7a867acad7c9); 2 documentation-only commits ahead of main, 0 behind, no Issue #6 implementation changes.
- **Machine-readable companion:** [ISSUE_6_PHASE0_SOURCE_LINK_REGISTER.json](ISSUE_6_PHASE0_SOURCE_LINK_REGISTER.json).

## 1. Source package integrity and authority

The owner-supplied `MSM2(1)(1).rar` was opened and its **12 PDFs extracted and text-inspected**. Its **SHA-256 = `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`**, identical to [Issue #16 Source and Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md) and Issue #17 source records. The archive is **not** copied into the repository; only its content references/hash are recorded, respecting existing source governance.

| Source in archive | What governs for Issue #6 | Verified reference |
|---|---|---|
| Music_Copilot_PRD_v1_EN_Clarified.pdf | Tier 1: **FR-01** user-consented supported audio and track/album/artist/folder display; privacy/performance/accessibility | PDF p.8 (FR-01), p.9 (incremental scan, 5,000 items, 48dp/TalkBack) |
| Music_Copilot_MVP_v1_EN.pdf | Tier 1: **US-02** local, account-free indexed media with later incremental scan; Songs/Artists/Albums/Folders/Search in MVP | PDF p.3–4; p.7 (5,000-file Week 2 delivery) |
| ADR-0001..0006 PDFs (6 files) | Historical engineering/decision input. ADR-0003 local-first, ADR-0002 playback ownership and ADR-0006 no backend particularly relevant | **Current accepted repository ADR register supersedes old `Proposed` PDF labels** |
| Music_Copilot_System_Architecture_MVP_v1_EN.pdf | Engineering synthesis: source/rebuildable index/permission-aware reconciliation | PDF p.13–14; not a replacement for approved Issue #16 |
| Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf | Engineering synthesis: scanner, derived Room projection and library/search contracts | PDF p.34–35; not approved implementation code |
| Music_Copilot_User_Flow_Phase_0_Foundation_v1.0_EN.pdf | Historical flow foundation: S-04/S-05; Songs/Artists/Albums/Folders | PDF p.10–11 and p.16 |
| Music_Copilot_User_Flow_Phase_1_First_Readiness_v1.0_EN(4).pdf | Historical UF-01/02/03 initial readiness; explicitly identifies UF-04 as FR-01 completion | PDF p.2, p.5. Current approved UF-04 on main governs detailed behavior |

**Authority priority:** English PRD → English MVP (delivery priorities) → accepted repository ADRs and [Software Architecture](../SOFTWARE_ARCHITECTURE.md) → approved current UF/GS/WF/HF and engineering handoff → prior spike evidence → engineering proposals. Current GitHub approval/implementation status overrides a dated PDF status assertion. No new product scope was inferred.

## 2. Definition of Ready and dependency ledger

| Dependency | Verified evidence | P00 disposition | Meaning / remaining obligation |
|---|---|---|---|
| **#4 Android foundation** | [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4) is closed; main contains Android, Kotlin/Compose, MediaStore/Room and DI seams | **READY** | Foundation exists; closure alone is not new Issue #6 device/CI proof |
| **#5 onboarding/media permission and recovery** | [Issue #5](https://github.com/latticeward-labs/music-copilot/issues/5) closed under explicit owner-accepted testing/review exception; media permission, grant/revoke and readiness code is on main | **READY_WITH_DEFERRED_QA** | Integration allowed; do not promote its unexecuted latest-head checks to PASS |
| **#73 remaining #5 validation** | [Issue #73](https://github.com/latticeward-labs/music-copilot/issues/73) open | **DEFERRED / RISK (not #6 start blocker)** | JVM/Compose/native/independent review and GS-01 positive Ready evidence not confirmed; original owner defer instruction forbids running those #5 tests without new direction |
| **#16 architecture/data ownership** | [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16) closed, accepted [public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) and [runtime policies](../architecture/RUNTIME_POLICIES.md) on main | **READY** | Changes to ownership/topology/persistence authority still require architecture review |
| **#17 approved Home/Library/Search handoff** | [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17) closed; [WF-04/HF-04](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md), [WF-05/HF-05](../high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md) and state frames are present | **READY_WITH_HUMAN_REVIEW_DEFERRED** | Design artifacts approved at AI-assisted level; real Compose/TalkBack and human review are not inferred |
| **TS-06 MediaStore spike** | [TS-06 record](../user-flows/evidence/TS-06_MEDIASTORE_RECONCILIATION.md): historical spike PASS; retrospective raw-measurement retention waiver disclosed | **REFERENCE_VALID / NON-TRANSFERABLE_QA** | A former spike/fixture is **not** evidence that the current #6 implementation passed 5,000-file or device tests |
| **#7 service-owned playback** | [Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7) open; gateway/API shell exists | **INTEGRATION_BOUNDARY / NOT #6 START BLOCKER** | #6 verifies selection and service handoff; #7 owns actual playback, queue, lockscreen/Bluetooth assertions |
| **#11 PLAY_ARTIST** | [Issue #11](https://github.com/latticeward-labs/music-copilot/issues/11) open, needs #6 artist index | **DOWNSTREAM CONSUMER** | Preserve public candidate contracts; no fuzzy Voice artist execution in #6 |
| **Optional P2 Liked Songs** | [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6) P2 cut rule | **DEFERRED BY SCOPE** | Not included in P0 delivery without explicit capacity decision |

**P00 readiness decision:** Required upstream documents/contracts are available and no unresolved architectural *start blocker* was discovered. Work may continue into P01. Existing deferred QA and future playback dependence must remain visible in P12; the product itself is **not yet ready**.

## 3. Traceability: product → behavior → visual → current code → final tests

| Controlled requirement | User flow / scenario | Approved WF/HF and state | Current code/contract | Delivery owner → P12 evidence |
|---|---|---|---|---|
| **FR-01, US-02: consent before scan** | [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) | WF-04 Scanning/Empty/Ready/Recovery; [HF-04 frames](../high-fidelity/WF-04/frames/) | `MediaRequestGate`, `LibraryScanUseCase`, `MediaStoreScanner`, `LibraryRepository.index` | P03/P07; M01–M04 and native permission tests |
| **FR-01: Songs/Artists/Albums/Folders** | [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) | [WF-04 Ready](../wireframes/app-states/WF-04_HOME_LIBRARY_READY.md), [HF-04 Ready](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md), S-04/CB-01/CB-20 | `LibraryItem`, `LibraryRow`, `LibraryDao`; no category browse projections or UI yet | P02/P04/P06/P08; M05–M10, Room and Compose tests |
| **US-02: initial + later incremental reconciliation** | [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md); [TS-06](../user-flows/evidence/TS-06_MEDIASTORE_RECONCILIATION.md) | WF-04 Scanning/Ready/Recoverable Error, S-04 | `RoomLibraryRepository.reconcile` currently uses transactional `replaceAll` of every row | P03/P05/P07; M11–M16, cancellation/race tests |
| **MVP Search** | [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md) | [WF-05](../wireframes/app-states/WF-05_SEARCH_RESULTS.md), [HF-05](../high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md), CB-09/10/11/12/13 and No Result/Error frames | `LibraryRepository.search` reads/filter-all; feature/search only placeholder | P06/P09; M17–M22, JVM/Room/Compose |
| **Verified selection, no false Playing** | [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [UF-05](../user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md), GS-02 | HF-04/HF-05 media rows, later Player WF-06 | `LibraryRepository.get` re-queries MediaStore; `PlaybackGateway` API exists, full UI handoff absent | P10; M30–M34 as applicable, gateway tests; #7 actual-playback boundary |
| **UI resilience and accessibility** | UF-02/03/04; GS-01/02 | WF-04/05 Loading/Empty/Error/Result; [HF-04](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md), [HF-05](../high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md), CMP-03/04/09/10/11 | Compose Shell / `AppNavHost` present; library/search feature screens absent | P07–P11; M23–M29, 48dp/200%/TalkBack/UI state proof |
| **Privacy/local-only, 5,000-file responsiveness** | UF-03/04, FR-01; TS-06 baseline | WF-04/05 privacy, performance and semantics annotations | injected IO scan; no documented end-to-end #6 5k benchmark; telemetry boundaries in architecture | P05/P06/P08/P11; P12 5,000 benchmark, T01–T10, privacy inspection |

Link register: [machine-readable canonical reference list](ISSUE_6_PHASE0_SOURCE_LINK_REGISTER.json). No personal filenames, search strings or MediaStore IDs are included.

## 4. Actual main-branch implementation inventory (static code inspection only)

| Capability / path | Observed starting point | P00 classification / required phase |
|---|---|---|
| `app/src/main/AndroidManifest.xml` | `READ_EXTERNAL_STORAGE` capped at API 32; `READ_MEDIA_AUDIO` declared; no MediaStore network dependency | **PARTIAL FOUNDATION**; P03 device-behavior proof, P12 OS/version suite |
| `data/mediastore/MediaStoreScanner.kt` | Android version-aware permission gate; IO MediaStore query; `IS_MUSIC != 0`, title/artist/album/duration/id; cursor closed; stable ref codec + requery | **PARTIAL**; folder identity/projection, source validation, malformed/volume/move and cancellation/scale testing P03/P04 |
| `data/api/LibraryRepository.kt` | `Unavailable/Scanning/Ready/Empty/RecoverableError`, revision/count, query/get/candidates public API | **PARTIAL**; define four browse projections, paging/index revision and query semantics P02 |
| `data/room/LibraryRow.kt`, `LibraryDao.kt`, `LibraryProjection.kt` | Single `library_items` table; `observeAll`/`all` sorted songs, `find`, `upsert`; atomic delete-all + upsert | **GAP**: no folder field, category queries, indexed scalable search, delta reconciliation or migration plan P02/P05/P06 |
| `data/mapping/LibraryRowMapper.kt` | trim/lowercase/collapse-whitespace title/artist; no album/folder search key; duplicate title identity distinguished by mediaRef | **PARTIAL**; non-sensitive unknown-value rules, accents/locale, stable group/ordering P04 |
| `data/repository/RoomLibraryRepository.kt` | Mutex serializes scans; revocation epoch and requery guards; derived Room replaceAll; `search()` loads **all** rows and filters title/artist in memory (not album); catches typed failures | **GAP**: no true row-level incremental delta; no bounded DAO search; scanner supplies whole collection, even no-change; failure/cancel availability handling requires P05/P06 review. Do not claim 5k PASS |
| `core/application/library/LibraryScanUseCase.kt` and `BootstrapRoutePolicy.kt` | OS permission gating, explicit post-grant/Retry/Rescan; false Ready protection; route shell | **PARTIAL**: extend readiness, retain query/navigation state and revocation/recovery P07 |
| `feature/library/PackageBoundary.kt`, `feature/search/PackageBoundary.kt`, `AppNavHost.kt` | Feature directories only contain package markers; AppNavHost library is first-use boundary/status screen, Search not implemented as full screen | **NOT IMPLEMENTED**: WF-04 four category browses, WF-05 local query results, lazy lists, UI lifecycle P08/P09 |
| `playback/api/PlaybackGateway.kt` + `PlaybackCommand.kt` | typed service command API exists, `get(ref)` performs existence check | **BOUNDARY EXISTS**; selection validation/wait for acknowledgement belongs P10; full playback belongs open #7 |
| `quality/privacy`, app/DI, Room v1 schema | local/telemetry/privacy seams; DB v1, disposable index; schema export | **PARTIAL**: prohibit raw filenames/queries in logs, migrations that preserve future app-owned records, evidence tests P11/P12 |

**Concrete risks / no hidden completion claims:** (1) current `replaceAll` is atomic but not incremental; (2) Room stores no folder identity; (3) Search uses full-table read/processor filtering and omits album matching; (4) UI screens and result semantics are placeholders; (5) no current versioned Room browse/search migration tests/5k execution shown; (6) retrospective TS-06 and #5 acceptance cannot replace P12 technical evidence; (7) #7 open means no claim of end-to-end playback; (8) Android 10/12/13+/OEM behavior and TalkBack need real validation.

## 5. Twelve Issue #6 acceptance criteria — implementation/evidence ledger

No original checkbox was modified. `BASELINE_PARTIAL` means some prerequisite code exists; `OPEN` is not PASS. Every row must receive actual passing proof at P12 before normal closeout.

| AC | Original requirement, condensed | P00 state | Implement in | Final evidence |
|---|---|---|---|---|
| 01 | User-consented discovery | **BASELINE_PARTIAL** | P03, P07 | M01–M04; OS instrumentation |
| 02 | Room songs/artists/albums/folders index | **OPEN** | P02, P04, P06 | M05–M10; Room |
| 03 | Incremental + cancellable scans | **OPEN** | P05 | M11–M14; races/cancel |
| 04 | Scanning/Empty/Ready/recoverable error | **BASELINE_PARTIAL** | P07, P08 | M03/M04/M11/M15/M16 |
| 05 | Search Result/No Result/Error | **OPEN** | P06, P09 | M17–M22; search/UI |
| 06 | Rotation/UI recreation consistency | **OPEN** | P07–P09 | M23–M24; lifecycle |
| 07 | 5,000-file scan and scroll, no main-thread blocking | **OPEN** | P05/P06/P08/P11 | 5k benchmark; M25 when supported |
| 08 | Safe corrupt/incomplete metadata | **BASELINE_PARTIAL** | P03, P04 | M26; mapper/error tests |
| 09 | No personal-file titles in analytics/crash metadata | **BASELINE_PARTIAL** | P03–P11 | privacy/static/runtime check |
| 10 | Repository/query unit + integration contract tests | **OPEN** | P02/P06/P11 | executed T01–T10 relevant suites |
| 11 | TalkBack, scalable text, list semantics and announcements | **OPEN** | P08/P09/P11 | M27–M29; UI/accessibility |
| 12 | Simple Like P2 only if capacity allows | **SCOPE-DEFERRED (P2)** | not in P0/P1 | explicit cut-rule record, no false PASS |

### Test protocol freeze

- **Manual:** [M01–M34](ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md), to be run by owner **once against a consolidated P00–P11 APK in P12**. P00 creates no APK and conducts no handset QA.
- **Technical:** T01–T10 in the same checklist, all unexecuted for this phase. A test specification is not a passing run.
- Only `PASS/FAIL/BLOCKED/NOT_RUN` with real provenance; `OWNER_REPORTED_PHYSICAL` is not equivalent to `ADB_VERIFIED`. Performance, Android variants and Issue #7 dependency must be reported separately.
- No personal file names, full paths, query text, content URIs, track IDs, library dumps, raw device logs or genuine private media screenshots in GitHub evidence.

## 6. P01 handoff and scoped acceptance

**P00 checks:**
- [x] Uploaded archive accessible; 12 actual PDFs opened; archive SHA-256 checked against authoritative source register.
- [x] Read FR-01/US-02, current approved UF-02/03/04, GS-01/02, WF-04/05, HF-04/05, accepted architecture/ADRs, TS-06 and live #4/#5/#16/#17/#73/#7/#11.
- [x] Cross-referenced real main code and distinguished stubs, partial scaffolding and absent deliverables.
- [x] All **12** original acceptance criteria identified, each linked to phase and later evidence; no premature product PASS.
- [x] Every upstream dependency classified READY/READY_WITH_DEFERRED_QA/REFERENCE/INTEGRATION_BOUNDARY; no unresolved P00 source blocker.
- [x] P12 single integrated APK rule and original issue-open/no-unrun-tests rule preserved.
- [ ] Any Issue #6 feature implementation, final QA, P12 APK or closure: **intentionally not part of P00**.

**Decision: P00_PASS_AUDIT_ONLY / P01_READY.** Implementation must address the documented gaps in P01–P11 and pass truthfully evidenced P12 gates. #73 remains OPEN and #7 remains independently owned. No workflow, build, benchmark or phone test was triggered during this audit.

**Next:** P01 — execution branch, freeze/change policy, evidence and rollback contract.