# Issue #6 / Phase P01 — GitHub Execution, Branch and Delivery Contract

**Date:** 2026-10-22  
**Gate:** `P01_PASS_CONTRACT_ONLY` **after GitHub artifact read-back**; documentation/branch governance only. Not P02 code acceptance and not any Issue #6 product AC PASS.  
**Issue:** [#6](https://github.com/latticeward-labs/music-copilot/issues/6) — OPEN; P00 [readiness audit](ISSUE_6_PHASE0_READINESS_AUDIT.md) = `PASS_AUDIT_ONLY`.  
**Canonical branch:** [`feature/6-mediastore-library-browse-search`](https://github.com/latticeward-labs/music-copilot/tree/feature/6-mediastore-library-browse-search) (reuse; **do not create a duplicate branch**).  
**Source attachment:** owner-supplied English `MSM2(1)(1).rar` (12 PDFs); SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` was independently rechecked at P01, matching Issue #16 and P00.

## 1. Authenticated GitHub baseline frozen at P01 entry

| Control | Actual inspection result |
|---|---|
| Repository | `latticeward-labs/music-copilot` (private), accessible with push/admin permissions |
| Current integration head | `main@bb022341bb12a1b12c22a078a70b8bf8217d2176` |
| **Existing feature head before P01** | `feature/6-mediastore-library-browse-search@86a284daf9ccb99bf9c28dcd7276cadce7ecbf25` |
| Ancestry at P01 entry | Feature ahead **5**, behind **0** relative to main; only four Issue #6 plan/P00/QA documentation paths in diff |
| Current issue | #6 **OPEN**, all **12 original product AC checkboxes unchecked**. Four upstream DoR checkboxes checked separately; do not confuse with original ACs |
| Existing PR for this branch | **None** at P01 entry; no PR created here |
| `develop` head | `a8944b287498aff557af3d7e17179b599c5c5ae5`, **646 commits behind main**, 0 ahead |
| Branch protections | GitHub branch API: main/feature both `protected=false`; **this alone does not establish absence of organization rulesets**. Governance relies on documented human gate unless enforced settings are separately confirmed |
| Running system evidence | **No new P01 Actions run requested**, no build, emulator, ADB, APK or manual test triggered by our calls; these operations are deferred under owner single-P12-APK rule |
| GitHub Projects V2 board status | **NOT_VERIFIED / NOT_CHANGED** (GitHub issue REST state is verified OPEN; Project V2 field is distinct and unavailable in the current action) |

**Branch policy:** Do all Issue #6 work on the already existing feature branch; do not write production code or issue documentation to main before final P12 approval. Freeze base SHA as provenance, **not** as license to disregard future main updates. Before merging in P12 compare ancestry, reconcile conflicts and re-run tests on the exact merged-candidate SHA. No unreviewed force push, branch reset, rebase or change to other issues. Keep P00 artifacts and the accepted prior implementation intact.

## 2. Sources, ownership and scope lock

Follow Tier-1 English [PRD FR-01](../PRD.md) and [MVP US-02](../MVP.md), accepted [ADRs](../adr/README.md), final [#16 architecture](../SOFTWARE_ARCHITECTURE.md) / [public repository contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) and [runtime policies](../architecture/RUNTIME_POLICIES.md). Behavioral/UI authority: [UF-02](../user-flows/UF-02_PERMISSION_RECOVERY.md), [UF-03](../user-flows/UF-03_LIBRARY_READINESS.md), [UF-04](../user-flows/UF-04_BROWSE_AND_SEARCH.md), [GS-01](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md), [GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md), [WF-04](../wireframes/app-states/WF-04_HOME_LIBRARY_READY.md), [WF-05](../wireframes/app-states/WF-05_SEARCH_RESULTS.md) and approved [HF-04](../high-fidelity/WF-04/HF-04_HOME_LIBRARY_READY.md) / [HF-05](../high-fidelity/WF-05/HF-05_SEARCH_RESULTS.md).

**In-scope:** Android 10+ Kotlin/Compose; access-gated local MediaStore scan and re-query, normalized rebuildable Room local index, **true incremental cancellable reconciliation**, Songs/Artists/Albums/Folders browse, offline Search (Idle/Result/No Result/Error), truthful readiness and permission recovery, accessible stable UI/Back/recreation, safe selection and **`PlaybackGateway` boundary-only**.

**Out-of-scope:** full service playback, live queue/MediaSession (owned by open [#7](https://github.com/latticeward-labs/music-copilot/issues/7)); PLAY_ARTIST and Voice (#11), network/backend, user account, cloud sync, streaming, online catalog, remote Search, DRM bypass, writing/deleting user audio, new external analytics. P2 Like/Liked Songs is **deferred by default**. [#73](https://github.com/latticeward-labs/music-copilot/issues/73) remains **open deferred #5 QA**, not inherited by #6 as a success.

**Owner test requirement:** exactly **one final consolidated installable APK** built/delivered at **P12**, **after all P00–P11 implementations**, for one real owner phone testing **M01–M34**. No separate APK or handset QA for each phase. P12 technical suites **T01–T10** and independent check requirements are separate; a single handset does not prove all Android versions or 5,000-item tests. A failed final candidate invalidates that candidate; any corrected APK becomes a newly identified final candidate (not a claimed repeat of the same checksum).

## 3. Engineering write boundaries and phase ownership

| Phase | Allowed principal implementation / records | Controlled outcome |
|---|---|---|
| P00 | `docs/implementation/ISSUE_6_PHASE0_*` | Provenance & DoR; complete |
| **P01** | `docs/implementation/ISSUE_6_PHASE1_*`, `ISSUE_6_EVIDENCE_INDEX.json`, `.github/PULL_REQUEST_TEMPLATE/issue-6-*` | **Documentation only; no Android production changes** |
| P02 | `data/api`, `core/model`, `data/room`, `core/application/library` (+ source/traceability doc) | Stable public models, Songs/Artists/Albums/Folders projections, schema/migration design |
| P03 | `data/mediastore`, adapter fixtures/tests | Correct platform consent, supported audio and content URI safety |
| P04 | `data/mapping`, normalization fixtures/tests | Safe deterministic rows and malformed metadata |
| P05 | `data/repository`, `data/room`, `core/application/library` | Atomic incremental, cancelling, permission-race aware reconcile |
| P06 | `data/room`, `data/api`, repository/query tests | Efficient bounded indexed browse/search queries, migrations |
| P07 | `core/application/library`, `app/di`, application/navigation/readiness UI | Single permission/scan/recovery truth, lifecycle |
| P08 | `feature/library`, approved Compose theme/shared UI; tests | Four nested browse categories, Lazy lists, semantics |
| P09 | `feature/search`, search use case/UI; tests | Local Search Idle/Result/No Result/Error and query retention |
| P10 | `core/application` selection, `app/di`, `playback/api` seam only | OS/source validation and typed #7 handoff; never optimistic Playing |
| P11 | Tests under `app/src/test`, `app/src/androidTest`, quality scripts/docs | Test/fixture readiness; **NO APK HANDOVER** |
| **P12** | Integrated source, actual test results, **single final APK**, final audit/PR | Verified testing, traceability and guarded merge/closeout |

Existing one-module `:app` package architecture stays binding. Changed paths outside phase scope require an explicit issue/documented reason and architecture/design review when relevant; a new public port that changes ownership or persistence needs #16/ADR governance. No unapproved modifications to `main`, other features or prior test evidence.

**Commit naming:** `feat(issue-6): ...`, `fix(issue-6): ...`, `test(issue-6): ...`, `docs(issue-6): ...`; one coherent phase outcome per commit. Include `Refs #6` in final PR and a change/evidence reference in phase report. Never use `Closes #6` before authorized final gate.

## 4. Stable identity, change log, traceability and evidence rules

**Machine-owned starting ledger:** [ISSUE_6_EVIDENCE_INDEX.json](ISSUE_6_EVIDENCE_INDEX.json) (13 `P00..P12` phase IDs, 12 `AC-01..AC-12`, 34 `M01..M34` manual cases, 10 `T01..T10` technical suites). IDs are immutable and namespaced for Issue #6 so historic TS-06's similarly named M01–M14 cannot be mistaken for the new owner tests.

- New change record: `I6-CHG-P02-001`, next per-phase increasing integer; record **phase, commit SHA, changed path list, design/architecture approval when required, rollback and actual test evidence**.
- New evidence record: `I6-EVD-P02-001`, with source SHA, exact test/case ID, sanitized method/fixture, time, executor class, outcome and immutable run URL/path. `M##` and `T##` remain **NOT_RUN** until actually exercised.
- Every phase produces its exact scoped implementation report under `docs/implementation/`, updates the evidence index, links touched code/tests/approved design and passes its own *scoped* gate. Reporting PASS for a **phase** does not imply any issue-wide AC PASS.
- Negative proof is part of the record: denied→no scan, revoked→no false Ready, deleted item→no false Playing, index error→Retry, query error→kept query/verified library, no sensitive telemetry, no unbounded main-thread work.
- Final AC-to-evidence map inherits P00's [source/AC register](ISSUE_6_PHASE0_SOURCE_LINK_REGISTER.json) but only moves an AC to verified in **P12 after executed proof**. Do not manufacture any screenshot, measurement, Actions number, reviewer, device or service acknowledgment.
- Permitted statuses: phase `NOT_STARTED`, `IN_PROGRESS`, `PASS_AUDIT_ONLY`, `PASS_SCOPED`, `PASS_VERIFIED`, `BLOCKED`, `FAIL`; test `PASS`, `FAIL`, `BLOCKED`, `NOT_RUN`; product acceptance `NOT_VERIFIED` / `P2_DEFERRED` until final QA.

## 5. GitHub Actions cost safety, PR routing and review

The 22 existing `.github/workflows/*.yml` files were reviewed for event triggers. None has an unfiltered **push-to-`feature/6-mediastore-library-browse-search`** build rule: default Android Foundation runs on main or feature/4 (or PR to main); Issue #5 push triggers use feature/5 and/or opt-in markers; legacy Issue #16 docs validation triggers on **PR to main**, not merely this branch push. **Do not trigger manual Actions, create a PR, dispatch test workflow, build APK, or run emulator/ADB for P01.** A future PR to main can legitimately trigger CI and must be accounted for at **P12**, not mislabelled an unauthorized Phase 1 test. Any unexpected external runner cannot be interpreted as part of verified P01 without inspecting real runs.

[CONTRIBUTING.md](../../CONTRIBUTING.md) recommends `develop` integration and prohibits sole self-review; historical official merges and current Android runtime are on `main`. At P01, GitHub compare shows **develop 646 commits behind main** and no separate current integration architecture there. Proposed final target is therefore **feature/6 → main at P12**, but this is **a proposed issue-specific exception awaiting owner/reviewer policy approval**, NOT a silently granted waiver. Before P12 merge explicitly reconcile that exception, independent review or separately documented authorization, exact PR-head CI results and unresolved findings. Do not back-merge hundreds of unrelated commits to make the older branch fit. Base and feature branch GitHub APIs report `protected=false`, so **none of these written checks are enforced by default branch protection**; reviewers/owner must uphold them.

**Prepared final-only PR:** [Issue #6 privacy-safe PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-6-mediastore-library-browse-search.md), **NOT an opened PR**. Its 12 acceptance rows and M/T suite requirements start `NOT_VERIFIED/NOT_RUN`. Standard review: Code + UX + Architecture + Accessibility/Privacy + QA. No fabricated independent reviewer approval, no automatic Done on merge; verify GitHub Issue **state**, **merge** and Project V2 **status** separately (Project V2 not verified/modified at P01).

## 6. Rollback, source-of-truth and data protection

1. **Before merge:** branch-only changes can be reverted with a new `git revert <offending_commit>` on `feature/6...`, preserving audit history. If a branch rebase/reset is truly needed, require owner approval, backup known SHA and force-with-lease; never silently rewrite prior phase commits or main.
2. **After merge (P12 only):** use reviewed `git revert <merge_or_squash_commit>` on a new corrective branch/PR, with integration/release evidence. No blind force-reset of protected history; evaluate users' persisted Room schema before reverting released migration code.
3. **Data preservation:** MediaStore and Android grant are authority; Room library index can rebuild, but **never delete/mutate physical user media**, and once app-owned Likes/history data exists it must be migration-safe (no destructive fallback). DataStore consent/settings must be preserved. A rollback may invalidate/rebuild derived index; it must not fabricate an accessible Ready state.
4. **Safety:** avoid raw personal media metadata, titles, artists, albums, folders, query strings, user audio, filesystem paths, MediaStore opaque IDs, content URIs, credentials, contact/device identity, raw logcat, unredacted screenshots or Voice transcripts in GitHub, PRs, assertions, telemetry or crash data. Synthetic named fixtures only; report non-sensitive error codes, counts and SHA/IDs.
5. **On failed gate:** mark phase `BLOCKED` or `FAIL`; do not alter the prior phase's historical record to claim success. Defer release; leave #6 OPEN, ACs unchecked until P12 evidence or a separately documented owner exception.

## 7. P01 acceptance checklist and handoff

- [x] Existing issue #6 and all 12 unchecked acceptance criteria inspected; P00 readiness/report/source register accessible.
- [x] Repository identity, default `main`, existing feature HEAD, ahead/behind and **no open PR** confirmed; branch reused without resetting it.
- [x] Attached source SHA-256 rechecked independently and matched the accepted P00/Issue #16 identity.
- [x] Explicit source/architecture scope freeze, P00–P12 ownership, P2 exclusions, #7/#73 boundaries recorded.
- [x] Stable phase/change/evidence/AC/manual/technical IDs, evidence schema and final single-APK rule documented.
- [x] Issue-specific P12 PR template authored with privacy, architecture, change, test, acceptance and reviewer gates.
- [x] `develop`/main divergence, unprotected branches, final-target policy-exception requirement and audit rollback controls recorded.
- [x] Documentation-only evidence: no P01 Android code, new APK, phone QA, PR or workflow run initiated.
- [x] Files **read back from actual GitHub branch** and issue addendum recorded for this phase before claiming this scoped PASS (see P01 validation record).

**Disposition:** `P01_PASS_CONTRACT_ONLY / P02_READY` after completed live GitHub read-back. **Issue #6 remains OPEN; original 12 ACs unchecked; P12 manual and technical cases remain NOT_RUN.** This phase prepares delivery; it does not substitute for implementation or test evidence.

**Next:** P02 — library/domain contract, four browse projections, schema/migration plan.
