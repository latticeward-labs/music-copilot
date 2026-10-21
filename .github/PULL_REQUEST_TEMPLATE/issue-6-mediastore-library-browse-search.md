# Issue #6 — Final MediaStore Library / Browse / Search Delivery PR (P12 ONLY)

> **TEMPLATE / NOT A PR.** Do not open, mark Ready or merge before P00–P11 implementation and P12 evidence-backed validation. No per-phase APK or owner phone test.
>
> [Issue #6](https://github.com/latticeward-labs/music-copilot/issues/6) · [P00 Source Audit](../../docs/implementation/ISSUE_6_PHASE0_READINESS_AUDIT.md) · [P01 Execution Contract](../../docs/implementation/ISSUE_6_PHASE1_EXECUTION_CONTRACT.md) · [Evidence Index](../../docs/implementation/ISSUE_6_EVIDENCE_INDEX.json) · [P12 Final Checklist](../../docs/implementation/ISSUE_6_FINAL_PHONE_QA_CHECKLIST.md)

**Suggested title:** `feat(library): implement consent-aware MediaStore browse and local search (#6)`

**Proposed head/base:** `feature/6-mediastore-library-browse-search` → `main` (P12 policy exception to generic `CONTRIBUTING.md` `develop` recommendation: **not yet approved at P01**; main is the actual current integration line; document explicit owner/reviewer approval before merge). Do not silently target `develop` or create an unrelated history merge.

**Issue text:** `Refs #6`. Use `Closes #6` only when the final P12 audited criteria, merge and closure policy authorize it; never presume issue/project status from a PR description.

## Authority and intended outcome

- **Product:** English PRD FR-01; MVP US-02 and local library/search, 5,000 supported-file target.
- **Architecture:** accepted ADRs, #16 public contracts, MediaStore platform existence, Room rebuildable derived index, DataStore app-owned small settings, PlaybackGateway service boundary.
- **UX:** UF-02/03/04, GS-01/02, approved WF-04/05 and HF-04/05 and #17 Compose/A11Y handoff.
- **Scope:** Android API 29+ native Kotlin/Compose, four browse categories, local Search with Result/No Result/Error, permission-aware incrementally reconciled index, source re-query before selection.
- **Out of scope:** backend, account, sync, streaming, cloud catalog, network search, raw media upload, new Voice/Driving work, full #7 playback/queue, unapproved P2 Likes.

## Implementation and Traceability

| Delivery | Commit/change ID | Code path and contract | Result/evidence |
|---|---|---|---|
| P02 Models/schema/migration | TBD | TBD | NOT_VERIFIED |
| P03 MediaStore access | TBD | TBD | NOT_VERIFIED |
| P04 Metadata normalization | TBD | TBD | NOT_VERIFIED |
| P05 Atomic incremental reconciliation | TBD | TBD | NOT_VERIFIED |
| P06 Room category/search queries | TBD | TBD | NOT_VERIFIED |
| P07 Truthful library readiness/recovery | TBD | TBD | NOT_VERIFIED |
| P08 Songs/Artists/Albums/Folders Compose | TBD | TBD | NOT_VERIFIED |
| P09 Search Result/No Result/Error Compose | TBD | TBD | NOT_VERIFIED |
| P10 validated PlaybackGateway selection | TBD | TBD | NOT_VERIFIED |
| P11 quality/test readiness | TBD | TBD | NOT_VERIFIED |

## Binding Issue #6 acceptance proof — 12 unchanged criteria

| ID | Acceptance focus | Actual evidence URL/run/commit | Verified result |
|---|---|---|---|
| AC-01 | Consent precedes MediaStore discovery | TBD | NOT_VERIFIED |
| AC-02 | Room songs/artists/albums/folders | TBD | NOT_VERIFIED |
| AC-03 | Incremental and cancellable scans | TBD | NOT_VERIFIED |
| AC-04 | Scanning/Empty/Ready/recovery truth | TBD | NOT_VERIFIED |
| AC-05 | Search Result/No Result/Error | TBD | NOT_VERIFIED |
| AC-06 | UI recreation state consistent | TBD | NOT_VERIFIED |
| AC-07 | 5,000 files and nonblocking scan/scroll | TBD | NOT_VERIFIED |
| AC-08 | Corrupt/incomplete metadata safe | TBD | NOT_VERIFIED |
| AC-09 | No private titles in telemetry/crash reports | TBD | NOT_VERIFIED |
| AC-10 | Repository/query unit/integration tests | TBD | NOT_VERIFIED |
| AC-11 | TalkBack, text scaling, semantics/announcements | TBD | NOT_VERIFIED |
| AC-12 | P2 Like only if capacity permits | Scope deferred unless separately approved | P2_DEFERRED |

## P12 one-APK test, privacy and review ledger

- [ ] P00–P11 **all changes integrated** into one frozen source commit SHA; no competing release candidates.
- [ ] T01–T10 **actually executed**, with test links, exact source SHA, runner/API and distinct PASS/FAIL/BLOCKED/NOT_RUN status.
- [ ] JVM, actual Room/instrumentation/Compose and architecture, Spotless, Detekt, Android lint/privacy tests as configured: verified at exact candidate SHA.
- [ ] 5,000-file scan/reconcile/scroll/search measured with reproducible method and no avoidable main-thread work.
- [ ] Required Android 10/12/13+/14+ representative API 29/31/33/35 coverage honestly recorded (missing coverage remains missing).
- [ ] **Exactly one consolidated installable APK** delivered for owner phone QA, with actual filename, source SHA, SHA-256, CI artifact identifier or documented reproducible local build.
- [ ] Owner M01–M34 actual results linked in P12 ledger; synthetic fixtures and permission recovery used, one real physical phone per owner constraint, no invented ADB proof.
- [ ] Broken/blocked #7 full actual playback separately identified; do not mark it as #6 successful service playback.
- [ ] Privacy: no user media filename/title/artist/album/folder, private query, URI, filesystem path, opaque track identifier, device identity or raw audio/transcript in issue, PR, CI logs, telemetry or screenshots. Use synthetic/redacted controlled fixtures.
- [ ] Design: WF-04/05 and HF-04/05 UI alignment, 48dp, 200% text, TalkBack semantics and empty/error/retry/back verified.
- [ ] Schema/migration impact: app-owned data preservation; rollback documented; no destructive migration or physical file change.
- [ ] Owner/reviewer explicitly approves proposed `main` integration target deviation from `develop` recommendation; no sole self-approval unless an issue-specific documented policy exception is granted.
- [ ] Independent review findings and CI PR-head checks reconciled; no unresolved P0/P1 blocker (or separately authorized transparent exception).
- [ ] Merge, actual Issue #6 closure and any GitHub Projects V2 Done status **verified separately** after P12; no automatic claims.

**Candidate source SHA:** PENDING  
**Final APK (name/SHA-256/artifact link):** NOT_BUILT  
**M01–M34:** NOT_RUN  
**T01–T10:** NOT_RUN  
**Human reviewer:** NOT_REVIEWED  
**CI PR-head result:** NOT_RUN  
**Final audit:** [ISSUE_6_FINAL_ACCEPTANCE_AUDIT.md](../../docs/implementation/ISSUE_6_FINAL_ACCEPTANCE_AUDIT.md) — NOT_CREATED  
**PR readiness:** NOT_READY; ISSUE OPEN.
