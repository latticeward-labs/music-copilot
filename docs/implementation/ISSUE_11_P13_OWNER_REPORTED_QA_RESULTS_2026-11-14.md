# Issue #11 P13 — owner-reported Android phone QA (2026-11-14)

**Source:** user-conducted phone testing and screenshots reported in conversation, not an automated test result. **Issue owner explicitly requested commit, PR merge and issue closure with these known exceptions.** This document is not a statement that all Issue #11 acceptance criteria passed.

- Repository: `latticeward-labs/music-copilot`
- Feature branch: `feature/11-play-artist-one-step-ambiguity`
- Tested APK *assumed to be P13 debug artifact from the same owner testing session*; exact installed on-device checksum **not independently verified**.
- Provenance of generated APK: [build run 43606645653](https://github.com/latticeward-labs/music-copilot/actions/runs/43606645653), build source `a8191014a1a95e4d337314caf196b282749f9f9d`, artifact `14119115504`, SHA-256 `b77004afa8d1b4a61ff483ec90347d0200742bcbc8f49d9b8bd8e96f588e812d`.
- Artist and file names, audio transcripts, paths, screenshots and device identifiers omitted under approved privacy contract.
- Phone status totals **41 PASS (owner-reported), 2 FAIL, 2 REVIEW, 2 BLOCKED, 1 NOT_RUN** (48 cases).
- `PASS` based on owner reporting means observed/applicable behavior only, **not verified ACK counts, privacy audit, instrumentation, or acceptance sign-off**.
- Source test groups `T01–T10`: **NOT_RUN / no technical execution evidence submitted**.
- Six-by-ten voice lab `L01–L06`: **NOT_REPORTED** (no per-intent 10-trial counts), required ≥54/60, zero wrong auto-executions. p95 not measured.
- Existing [P0 Issue #80](https://github.com/latticeward-labs/music-copilot/issues/80) remains unresolved (historic Issue #10 accuracy 24/50 = 48%).
- `REVIEW` means real observed behavior exists but cannot fairly be classified PASS/FAIL against threshold and STT evidence without further analysis. `BLOCKED` and `NOT_RUN` are not PASS.

## Manual M01–M48 outcomes

| ID | Owner-reported status | Redacted observation / evidence limit |
|---|---|---|
| M01 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M02 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M03 | BLOCKED | Moonshine provider confidence=null; high-confidence direct path cannot be verified with this provider. |
| M04 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M05 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M06 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M07 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M08 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M09 | PASS | Case-only artist-name difference reported treated as the same local artist. |
| M10 | REVIEW | Two punctuated/unpunctuated artist groups were distinct in local library; a two-choice confirmation was displayed. Whether the normalization acceptance criterion is satisfied remains unverified. |
| M11 | FAIL | Unpunctuated artist name recognized; apostrophe/backtick variants were not recognized. Hyphenated variant appeared once and later was absent. Cause (ASR/index/resolver) undetermined. |
| M12 | FAIL | Two near-spelling artist groups were both suggested once, but behavior did not repeat reliably. |
| M13 | PASS | Both artist candidates appeared for selection. Owner confirmed visibility; other invariants rely on other owner observations. |
| M14 | NOT_RUN | Explicitly deferred by owner; deterministic repeated-order check has not been run. |
| M15 | REVIEW | Three similarly spelled artist groups present; two screenshots showed differing pairs of two candidate options across Full Player/Driving. Need same STT transcript and candidate score review before final verdict. |
| M16 | PASS | Explicit owner PASS: NotFound case. |
| M17 | PASS | Explicit owner PASS: selecting first candidate worked as expected; service command count not instrumentally verified. |
| M18 | PASS | Explicit owner PASS: selecting second candidate worked as expected; service command count not instrumentally verified. |
| M19 | PASS | Explicit owner PASS: Neither/Cancel. |
| M20 | PASS | Explicit owner PASS: rapid repeated taps; no observable duplicate. |
| M21 | PASS | Explicit owner PASS: Back cancellation. |
| M22 | PASS | Explicit owner PASS: stale UI selection. |
| M23 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M24 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M25 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M26 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M27 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M28 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M29 | BLOCKED | MediaController disconnection condition not safely reproducible from owner UI; previously marked BLOCKED. |
| M30 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M31 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M32 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M33 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M34 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M35 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M36 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M37 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M38 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M39 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M40 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M41 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M42 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M43 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M44 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M45 | PASS | Owner's aggregate PASS report; TTS technical timing not independently measured. |
| M46 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |
| M47 | PASS | Owner's aggregate PASS report; private logs/telemetry not submitted; no independent security verification. |
| M48 | PASS | Owner reported remaining phone checklist cases PASS in aggregate; individual technical evidence not supplied. |

## Acceptance / release caveats

Original Issue #11 acceptance boxes must not all be marked checked: AC02 (normalization), AC04 (real confidence direct path), AC05 (3+ meaningful ambiguity safety), AC11/AC12 (automated test execution), AC13 (60-trial quality gate) lack full proof; remaining AC are only partly owner observed. Implementation can be merged as an **owner-authorized implementation milestone with acknowledged QA exceptions**, not a quality/release PASS. Retain defects and verification tasks in a separate open follow-up Issue. No code corrections, automated tests, or new APK are implied by this QA write-up.
