# Issue #10 — Push-to-Talk, Voice State Machine & five direct controls

> **TEMPLATE ONLY. DO NOT CREATE A PR in P01.** This file is guidance for the one cumulative P13 owner delivery, not proof of implementation or acceptance. Leave all boxes unchecked until evidence exists.

**Refs #10** (never auto-`Closes #10` until explicitly owner-approved).  
**Proposed HEAD:** `feature/10-ptt-voice-five-control-intents`  
**Proposed BASE:** `main` (project integration exception to CONTRIBUTING.md's preferred `develop`; `develop` was 1066 commits behind `main` at P01, **recheck** and obtain owner approval at P13).  
**Status before P13:** `NOT_READY_FOR_REVIEW / NO_APK / NO_TEST_EXECUTION`.

## Source scope and dependency guards

- [ ] P00–P12 delivered on the same feature branch with accurate phase reports and no unapproved features.
- [ ] One app-scoped Voice session; provider ports remain replaceable, cancel/late callbacks safe; PTT only.
- [ ] Exactly PLAY/PAUSE/NEXT/PREVIOUS/SEEK_BACK executable via service-owned Media3; PLAY_ARTIST remains #11.
- [ ] Full Player and Driving WF-08 overlay; manual fallback, 6s timeout, at most one retry and a11y present.
- [ ] Voice duck lease safely restores with #8 call/focus/route precedence and no unauthorized resume.
- [ ] No new player, background wake listener, streaming/backend/cloud STT without separate consent.
- [ ] Issue #9 M45 parked-vehicle real Voice NEXT retested or labeled BLOCKED with reason.

## Verified one cumulative APK (only P13)

- [ ] Frozen P00–P12 source HEAD SHA: `TBD`.
- [ ] Only `:app:assembleDebug` was run to produce exactly one cumulative APK; build result/attempt count: `TBD`.
- [ ] APK `app-debug.apk` exact SHA-256: `TBD` and size: `TBD`.
- [ ] Actual downloadable GitHub Artifact and Actions run links: `TBD`.
- [ ] No `test`, `lint`, `check`, `connectedAndroidTest`, emulator or phone jobs in build workflow.
- [ ] Technical test sources authored but NOT_RUN are clearly distinguished from actually executed tests.

## Product criteria / actual user evidence

- [ ] AC01 State Machine (see AC register).
- [ ] AC02 Explicit mic tap.
- [ ] AC03 Duck/restore.
- [ ] AC04 <=6 seconds and one Retry.
- [ ] AC05 Replaceable STT/TTS.
- [ ] AC06 Exactly 5 executable intents.
- [ ] AC07 Service dispatch; no extra destination.
- [ ] AC08 UI observer only.
- [ ] AC09 Short TTS sentence.
- [ ] AC10 Safe terminal and manual fallback.
- [ ] AC11 Background/rotation/interruptions no duplicates.
- [ ] AC12 Controlled lab at least **45/50** correct before field QA.
- [ ] AC13 No raw voice stored/logged.
- [ ] AC14 Conditional cloud consent and destination/retention disclosure; mark conditional N/A with reason only if no cloud engine exists.
- [ ] AC15 Mapper/state/timeout/idempotency tests with actual execution evidence if marking PASS.
- [ ] AC16 Approved TalkBack/UI states.

## Evidence references and candid QA ledger

- [P00 readiness audit](../../docs/implementation/ISSUE_10_PHASE0_READINESS_AUDIT.md)
- [P00 AC traceability](../../docs/implementation/ISSUE_10_PHASE0_TRACEABILITY.json)
- [P01 delivery rules](../../docs/implementation/ISSUE_10_PHASE1_EXECUTION_CONTRACT.md)
- [Evidence index](../../docs/implementation/ISSUE_10_EVIDENCE_INDEX.json)
- [Owner M01–M60 + L01–L05 Persian checklist](../../docs/implementation/ISSUE_10_FINAL_PHONE_QA_CHECKLIST_FA.md)
- [QA results, actual runs only](../../docs/implementation/ISSUE_10_QA_RESULTS.json)

**Owner test device / Android:** TBD. **APK SHA:** TBD. **M01–M60 observed cases:** NOT_RUN until user reports. **L01–L05:** NOT_RUN, 50 total attempts required. **T01–T10:** NOT_RUN unless independently executed with logs. **Review:** Not done, reviewer TBD. No fake green badges.

## Privacy, regressions and rollback

- [ ] Check #7 queue, #8 Android media notification Play, media-card open, AirPods Play, call/route focus and #9 Driving/BT suggestion.
- [ ] Verify permission revocation, Android 10/11 truthful manual-only, no hidden cloud capture, no raw transcript/audio/personal paths/MACs in telemetry or QA.
- [ ] Disclose any manifest/SDK/data migration/rollback concerns and unresolved QA/OEM/parked vehicle coverage.
- [ ] Reviewer and owner decisions cited with real evidence; no fabricated independent approval.
- [ ] PR/merge/Issue closure explicitly requested by owner **after** factual P13 reporting and deferral record.

**Merge method suggestion:** squash when approved; no branch cleanup before confirmed merge; never infer `FULL_PASS` from source commit or successful APK build.
