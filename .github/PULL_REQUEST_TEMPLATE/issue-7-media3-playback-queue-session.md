# Issue #7 — Final Media3 Playback / Queue / MediaSession delivery PR (**P13 ONLY**)

> **TEMPLATE, NOT AN OPEN PR.** Prepare the PR in **P13 after P00–P12 integrated code**, not in documentation-only P01. All outcomes, reviewers, CI and APK references below are pending actual evidence.

[Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7) · [P00 Source Audit](../../docs/implementation/ISSUE_7_PHASE0_READINESS_AUDIT.md) · [P01 Execution Contract](../../docs/implementation/ISSUE_7_PHASE1_EXECUTION_CONTRACT.md) · [Phase/AC Evidence Index](../../docs/implementation/ISSUE_7_EVIDENCE_INDEX.json) · [P13 Owner QA](../../docs/implementation/ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md) · [Actual Owner/Technical Results](../../docs/implementation/ISSUE_7_QA_RESULTS.json)

**Proposed PR title:** `feat(playback): deliver service-owned Media3 playback and queue (#7)`  
**Head:** `feature/7-media3-playback-queue-session`  
**Proposed base:** `main` (**exception from `CONTRIBUTING.md`'s `develop` recommendation, pending explicit owner/reviewer approval at P13**). `develop` was 831 commits behind main at P01 entry; re-evaluate before merging.

**PR issue relation:** `Refs #7`; use `Closes #7` only after final acceptance and approved closeout policy. **Do not falsely imply tested/merged/Done**.

## Outcome and strict delivery boundary

Android 10+ local playback via the existing **same-process, service-owned** Media3 ExoPlayer/MediaSession, authoritative UI/System controls, safe background and route behavior, and separable P1 **Safe-Paused** queue/position restoration.

- No duplicate Player, separate process, optimistic UI state, web catalog, streaming, account, sync or unapproved Voice/STT.
- No per-phase **owner APK**. P13 produces **ONE integrated installable owner APK** from the final P00–P12 source SHA. Owner tests the *same identified APK hash* on their phone.
- P0 background/system/focus/route/corrupt safety does not inherit PASS from #4 or #6; P1 restoration is never autoplay.
- Existing #5/#6 deferred evidence stays open in #73/#74. Unknown platform/device/vehicle outcomes remain NOT_RUN/deferred.

## Source/implementation handoff

| Phase | Source SHA / files / evidence | Scoped gate |
|---|---|---|
| P00 | [P00 readiness audit](../../docs/implementation/ISSUE_7_PHASE0_READINESS_AUDIT.md) | PASS_AUDIT_ONLY |
| P01 | [P01 governance contract](../../docs/implementation/ISSUE_7_PHASE1_EXECUTION_CONTRACT.md) | PASS_CONTRACT_ONLY after GitHub read-back |
| P02 | [P02 policy report](../../docs/implementation/ISSUE_7_PHASE2_PLAYBACK_QUEUE_POLICY.md); pure Kotlin queue/navigation contracts, 13 JUnit methods authored, local policy-only 14 checks | PASS_SCOPED — Media3/Gradle/JUnit NOT_RUN |
| P03 | [P03 scoped source/read-back](../../docs/implementation/ISSUE_7_PHASE3_MEDIASTORE_RESOLVER_AUDIT.md): scoped MediaStore codec/URI resolver, read-only Android probe, 12 JUnit methods + 17 smoke checks authored | PASS_SCOPED — Android/JUnit/17-check smoke NOT_RUN |
| P04 | TBD: queue implementation/edge cases | NOT_STARTED |
| P05 | TBD: audible ExoPlayer/selection contract | NOT_STARTED |
| P06 | TBD: MediaSession/system/private controller parity | NOT_STARTED |
| P07 | TBD: WF-04/WF-06/WF-07 native UI/mini-player/queue | NOT_STARTED |
| P08 | TBD: background/foreground notification/lifecycle | NOT_STARTED |
| P09 | TBD: focus/route/headset/BT safe audio | NOT_STARTED |
| P10 | TBD: corrupt/missing/revoked media recovery | NOT_STARTED |
| P11 | TBD: versioned P1 Safe-Paused Proto DataStore restoration | NOT_STARTED |
| P12 | TBD: integrated regression/QA test fixtures/traces | NOT_STARTED |
| P13 | TBD: one final APK, verified manual+technical results, final audit | NOT_STARTED |

## Binding 13 acceptance criteria (keep NOT_VERIFIED until actual P13 evidence)

| AC | Behavior | Actual evidence/run/commit | Status |
|---|---|---|---|
| AC01 | Real Play, Pause, Next, Previous, Seek and Queue | M03–M14 / T01–T03; TBD | NOT_VERIFIED |
| AC02 | One service-owned Player/MediaSession independent of UI | M15–M19 / T04; TBD | NOT_VERIFIED |
| AC03 | Background and screen-off audio | M15–M18 / T04; TBD | NOT_VERIFIED |
| AC04 | Notification, lock screen and Bluetooth actual controls | M20–M27 / T05–T06; TBD | NOT_VERIFIED |
| AC05 | UI observes authoritative state after recreation | M17–M19/M28 / T05; TBD | NOT_VERIFIED |
| AC06 | Valid Library selection creates real correct queue | M02–M06/M12 / T07; TBD | NOT_VERIFIED |
| AC07 | Bounded corrupt-item skip and viable queue continuation | M29–M31 / T08; TBD | NOT_VERIFIED |
| AC08 | Playing/Paused/No Track/Corrupt approved UI states | M01/M04/M29–M31 / T09; TBD | NOT_VERIFIED |
| AC09 | Deterministic queue/seek/previous/end unit tests | T01–T03; TBD | NOT_VERIFIED |
| AC10 | Service/controller integration test execution | T04–T06; TBD | NOT_VERIFIED |
| AC11 | P1 queue/position restore implementation | M32–M35 / T10–T11; TBD | NOT_VERIFIED |
| AC12 | Honest process-death/force-stop limits and restore docs | M35 / T10–T12; TBD | NOT_VERIFIED |
| AC13 | No streaming/catalog dependency | M36 / T13; TBD | NOT_VERIFIED |

## P13 owner APK and testing registry

- [ ] Entire P00–P12 implementation integrated on the one feature branch, compared against updated `main`, exact HEAD frozen.
- [ ] Only **one** consolidated owner `app-debug.apk` built and retained; capture exact file hash/signature/source SHA and installability.
- [ ] Owner M01–M36 real results written with source/APK hash and date, each PASS/FAIL/BLOCKED/NOT_RUN/N/A supported by observation.
- [ ] T01–T15 technical suites have executed, independently evidenced results with runner/platform, reports, exact SHA; authored tests ≠ passed tests.
- [ ] Background/lock/system controls tested; focus/call/route disconnect does not leak audio or auto-resume in unsafe state.
- [ ] Android 10/12/13+/14+ API variations, OEM and optional vehicle contexts explicitly labelled PASS only if run; missing cases deferred.
- [ ] Local/Privacy: validate permission/URI, no private song metadata/paths/queue or user audio in telemetry/CI/issues; no cloud/streaming endpoints.
- [ ] Accessibility: #17 handoff, TalkBack, fontScale 150–200%, semantics/control sizes; verify actual device tests where available.
- [ ] P1 restore is Paused only, live session first, schema validation, missing-item drop, clamped position; no force-stop guarantee.
- [ ] Formal review with Android/runtime, product, design/A11Y, privacy/security, QA; reviewer decisions + exact CI PR-head checks registered.
- [ ] Owner explicitly approves `main` integration deviation; independent reviewer or **Issue #7-specific** documented exception; no pretend approval.
- [ ] Rollback/data/versioning/Android permission consequences reviewed; no dangerous merge against stale base.
- [ ] Merge SHA, Issue #7 state and any Project V2 field state separately re-read and recorded. No automatic Done from mere PR merge.

**P13 source SHA:** PENDING  
**P13 APK SHA-256, artifact URL and build/run:** NOT_BUILT  
**Owner M01–M36:** NOT_RUN  
**Technical T01–T15:** NOT_RUN  
**PR:** NOT_OPENED (P01)  
**Review/CI:** NOT_RUN / NOT_REVIEWED  
**Main target exception:** PROPOSED_NOT_APPROVED  
**Merge/close decision:** NOT_READY.
