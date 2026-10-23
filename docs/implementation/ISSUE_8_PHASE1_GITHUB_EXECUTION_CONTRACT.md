# Issue #8 — P01 GitHub Execution, Ownership & Delivery Contract

**Status:** P01 DOCUMENT/BRANCH CONTRACT COMPLETE — no code, runtime or QA acceptance.  
**Date:** 2026-10-23 · **Issue:** [#8](https://github.com/latticeward-labs/music-copilot/issues/8) · **Priority:** P0  
**Execution branch:** `feature/8-audio-focus-route-recovery` · **Merge target:** `main` · **Repository:** `latticeward-labs/music-copilot`  
**Baseline main SHA:** `62c6115ac11af4cb2790b306f885f580b82a146b` (merged Issue #7 baseline).  
**P00 audit:** [source/readiness and risks](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md).  
**Traceability index:** [AC ↔ phase ↔ code ↔ manual/technical evidence](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json).  
**P01 scope:** repository/process contract and documentation ONLY. No implementation or verification of Android behavior.

## 1. Repo/branch verification & mutation rules

- GitHub reports repo permissions `admin/push/pull=true`, repo not archived, default branch `main`.
- At P01 entry, compare `main...feature/8-audio-focus-route-recovery` was **ahead 8 / behind 0**, merge-base `62c6115...`. Eight previous branch-only commits changed Issue #8 planning/P00 documents, **not production app code**.
- Do not branch from old pre-#7 code. Work on the existing `feature/8-audio-focus-route-recovery`; no fork, unrelated rebase, overwrite or force-push. Inspect branch tip and `main` before each phase, so if main advances, review/reconcile rather than blindly overwrite.
- Commit each phase's source/docs to this branch with traceable `[skip ci]` commit messages such as `feat(issue-8): P02 ... [skip ci]` or `docs(issue-8): P01 ... [skip ci]`. GitHub may apply workflow rules beyond the commit message; never dispatch Actions, `workflow_dispatch`, test, compile, lint or any build during P00–P12.
- Do **not** open/merge a release-ready PR, merge to `main`, close #8, check original ACs or mark QA PASS merely because a phase document was committed. A later PR/review/merge decision is subject to actual P13 APK/owner evidence and owner authorization. If a PR is created early for visibility, it must be explicitly draft/not mergeable on claims, and cannot trigger tests by our action.
- Existing Issue #8 metadata originally says Backlog/blocked. It is historical planning metadata; current dated annex is P00 COMPLETE, P01 COMPLETE, P02 NEXT. It does **not** certify engineering DoR-01 or DoR-03.

## 2. Authority precedence and cross-issue dependencies

1. **Attached user source:** `MSM2(1)(1).rar`, verified SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, 12 original PDF files extracted and SHA-checked in P01; English PRD FR-03/MVP Week 4/Architecture/ADR-0002 remain the product/design intent.
2. **Binding accepted repo architecture:** [#16](https://github.com/latticeward-labs/music-copilot/issues/16), [ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [Runtime §8](../architecture/RUNTIME_POLICIES.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [TS-04](../user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md). The four transient-resume guards and route/manual precedence are approved runtime rules.
3. **Existing implementation:** [#7 merged PR #76](https://github.com/latticeward-labs/music-copilot/pull/76) supplies the sole service-owned Media3 ExoPlayer/MediaSession, serialized commands, noisy handling, route monitor and P1 cold Safe-Paused restoration. #7 was owner-accepted but lacks independent handset/JVM/technical proof. Reuse code, not its APK or test badges.
4. **Coexistence:** #5 media/Voice permission behavior, #6 MediaStore access and #7 queue/restore must not regress. Their deferred QA debt [#73](https://github.com/latticeward-labs/music-copilot/issues/73) and [#74](https://github.com/latticeward-labs/music-copilot/issues/74) stays separate.
5. **Related/future:** #9 Driving UI receives authoritative interrupted state; #10 Voice integration owns Voice listening/confirmation, with #8 only wiring a cancellation **seam**. Do not declare #9/#10 complete by completing P09.
6. If P02+ discovers a conflict that requires a new Player/service, extra audio-focus requester, process topology, external backend or contract-breaking permission, **STOP that proposed change**, record a decision/ADR review and obtain approval before crossing boundaries.

## 3. Path and state ownership — logical roles, NOT invented GitHub team assignments

| Ownership | Existing authority/path | #8 writable slice and constraint | Phase |
|---|---|---|---|
| Service & Media3 | [MusicPlaybackService](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Existing service alone owns Player/Session, focus mediation, immediate safety pause; never a second Player/focus requester | P03–P08 |
| Pure interruption policy | [PlaybackInterruptionGate](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackInterruptionGate.kt); [reserved interruption package](../../app/src/main/java/com/latticeward/musiccopilot/playback/interruption/PackageBoundary.kt) | Add injectable pure reducer, four guarded transitions, event generations; avoid Android SDK in pure policy | P02, P07 |
| Android routes | [ServiceAudioRouteMonitor](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/ServiceAudioRouteMonitor.kt) | Sink removal/noisy reconciliation, relevant active-route when knowable, conservative fail-closed behavior | P05–P07, P11 |
| Command/event serialization | [PlaybackCommandProcessor](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackCommandProcessor.kt), [SystemTransportPolicy](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/SystemTransportPolicy.kt) | Receipt-time priority, guarded external Play provenance, stale IO invalidation, no duplicate queue owners | P06–P07 |
| MediaSession & UI | [ServiceSessionTransportPlayer](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/ServiceSessionTransportPlayer.kt), [Media3SnapshotProjector](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/Media3SnapshotProjector.kt), [PlaybackControllerConnection](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt), [PlaybackSnapshot](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackSnapshot.kt) | One atomic authoritative snapshot; notification, lock screen, Mini/Full Player & Driving are observers | P08 |
| Voice boundary | [VoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/api/VoiceSessionCoordinator.kt), [DefaultVoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) | Invoke existing `CallOrFocusInvalidated` / `RouteInvalidated` cancellation, generation fencing; no replacement STT or extra Voice owner | P09 |
| Diagnostics/error | [Runtime Policies §12–14](../architecture/RUNTIME_POLICIES.md), [PlaybackSnapshot](../../app/src/main/java/com/latticeward/musiccopilot/playback/api/PlaybackSnapshot.kt) | Stable sanitized `AppError.Interruption` codes, no title/URI/device/telephony/transcript logs or blocking telemetry | P10 |
| Android compatibility | [AndroidManifest.xml](../../app/src/main/AndroidManifest.xml), [app/build.gradle.kts](../../app/build.gradle.kts) | API 29+ / compile & target 36; preserve mediaPlayback foreground and permission minimization; audit focus eligibility API35+ | P11 |
| QA & handoff | [final M/T checklist](ISSUE_8_FINAL_OWNER_PHONE_QA_CHECKLIST.md), [QA ledger](ISSUE_8_QA_RESULTS.json) | P12 author test cases (no execution); P13 one exact-source APK, owner verifies case by case | P12–P13 |
| Requirements/evidence | [P01 traceability JSON](ISSUE_8_PHASE1_TRACEABILITY_INDEX.json), [P00 report](ISSUE_8_PHASE0_SOURCE_AND_READINESS_AUDIT.md) | Record impact per phase, source/compiled APK SHA, risk and rollback; no inferred PASS | P01–P13 |

**Interaction invariant:** system focus/route event → one service-owned transition → same Player/MediaSession → one revisioned PlaybackSnapshot → OS controls/notification/lock screen/Full/Mini/Driving. Voice cancellation goes through existing coordinator. Emergency route pause may bypass suspended IO *only on the service Player/main looper*; it is never a second queue or user-command worker.

## 4. Approved scope, safety and resume contracts

- Audio Focus remains controlled by Media3 using `setAudioAttributes(..., true)`. A pure event adapter/reducer can observe Media3 `Player` signals, but cannot act as a competing `AudioManager.requestAudioFocus` owner. A call is not identifiable from a focus event; no fictional telephony callback or `READ_PHONE_STATE`.
- Four conditional auto-resume guards **all required**: (1) Playing before transient interruption; (2) focus restored; (3) unchanged, valid route epoch; (4) no later manual Pause. A second pre-existing rule requires pause be owned by that interruption. Permanent loss or route disconnect → Safe Paused. Reconnect or cold restore never alone starts playback.
- Route-loss pause and manual Pause/Stop have priority over stale focus gain, delayed service work or controller events. Event processing idempotent, revision/generation tokens monotonic; an unsolicited head-unit Play-on-reconnect without provable user intent must be fenced conservatively.
- Preserve user queue/position via existing service/P1 store, not an Activity cache. No forced streaming, backend/account/cloud, pairing, phone logs, device IDs, MAC, private media URI, title/artist or raw Voice/audio/transcript in diagnostics. Typed coarse errors only. API35+ foreground eligibility must be source-audited and later device-checked.
- OEM callback order and zero phone-speaker leakage **cannot be declared proven by source review**. Stop test on audible leak at P13, record FAIL and block release rather than auto-pass.

## 5. Per-phase commit/review/DoD protocol

For every P02–P12 phase:
1. Read latest `main` and issue branch head, earlier source and current risk/AC mapping.
2. Make **only** phase-owned source and evidence/documentation changes. Preserve service/queue/Voice boundaries and unchanged unrelated files.
3. Record affected files, policy decisions, risk IDs, ACs, implementation limitations, next-step dependencies and one `[skip ci]` commit. No APK/tests/Gradle/Actions; code-path assertions remain `SOURCE_ONLY`.
4. Update [QA ledger](ISSUE_8_QA_RESULTS.json) phase status without touching `NOT_RUN` manual/technical cases or `UNVERIFIED` original ACs absent genuine evidence. A scoped phase review `PASS_SOURCE_ONLY` is not a product PASS.
5. Update Issue #8 with concise evidence links and next phase. Leave Issue OPEN and do not merge until separate final authorization.

**P01-specific DoD:** existing branch/main base confirmed, authority/dependencies and package owners recorded, traceability/evidence schema published, rollback defined, Issue + ledger + plan synchronized; **no code/Build/QA proof**.

## 6. One final artifact and owner-provided evidence

- **P00–P12:** strictly **zero** Gradle compile, build/APK, lint, unit/JVM, integration, instrumented/emulator/phone tests and **zero GitHub Actions invocation**. Test *source* may be written in P12 but cannot execute.
- **P13:** freeze source HEAD and build the cumulative `:app:assembleDebug` once for delivery (retries only to correct a failed build, not a per-phase handoff). Use explicitly triggered BUILD-ONLY Actions with no test/lint steps if needed; otherwise equivalent authorized local build. Record exact `compiled_commit_sha` **not later documentation HEAD**, `apk_sha256`, APK size, application ID/version, run/artifact URLs, build result and timestamp. No earlier Issue #7 APK qualifies.
- Hand off only that final installable APK plus [M01–M45 owner checklist](ISSUE_8_FINAL_OWNER_PHONE_QA_CHECKLIST.md). Owner gives statuses `PASS|FAIL|BLOCKED|NOT_RUN|N/A` and optional safe notes/phone Android version. Store provenance `OWNER_SELF_REPORT` and per-case exact APK/source SHA; never fabricate user screenshots or per-case test logs.
- `T01–T15` and cross-OEM Pixel/Samsung/Xiaomi/three **parked** head-unit/vehicle cases stay `NOT_RUN` or factually `BLOCKED` without separate permission/physical evidence. Automated test-proof AC10 remains UNVERIFIED until separately authorized/actually executed.
- One phase is COMPLETE when its specific source/doc deliverable is stored; Issue #8 acceptance, PR merge, original AC checks and release validation are separate decisions.

### Evidence record schema (all fields are factual or null)

```json
{
  "phase": "P13",
  "compiled_commit_sha": null,
  "apk_sha256": null,
  "apk_bytes": null,
  "build_workflow_run": null,
  "apk_artifact_url": null,
  "android_device_model": null,
  "android_api": null,
  "case_id": "M01",
  "status": "NOT_RUN",
  "provenance": null,
  "notes_sanitized": null,
  "technical_test_log": null,
  "reviewer_approval": null
}
```

## 7. Rollback & safety containment — no force push

| Trigger | Safe action | Required record |
|---|---|---|
| P02–P12 source change creates architecture regression, mistaken owner, or privacy leak | Stop modifying affected subsystem. Revert **specific culprit commit(s)** on feature branch, preserve branch/P00/P01 docs and unrelated later work; document superseding correction. Do not reset `main` or force-push shared history. | Revert SHA, originating phase SHA, impacted risk/AC, re-audit tasks |
| `main` advances or branch diverges | Recompare/review; merge/rebase only after reviewing base deltas and without overwriting #7 live service, Room/Voice state or QA provenance. | base/head/merge-base SHAs before and after |
| Unsafe route/autoplay behavior at P13 on phone | Immediately stop playback/test; mark exact case FAIL, do not accept APK or progress to production/merge. Fix in branch and rebuild only authorized **final candidate**, invalidate old APK hash in ledger. | observed failure, owner provenance, old/new compiled SHA/APK hashes |
| APK build failure | Record failed build/error without declaring source/runtime PASS; correct source and rerun BUILD-ONLY when authorized. Publish only successful cumulative APK candidate. | attempted SHA, build run result, final actual APK details |
| Mistaken QA/AC PASS, technical log absent | Correct to `UNVERIFIED`/`NOT_RUN` and append provenance correction; never silently rewrite history or copy #7 PASS to #8. | correction commit and original claim |
| After later approved PR merge a regression emerges | Revert the **specific Issue #8 merge/commits via a normal PR**; preserve independent #7 runtime and evidence. Reopen #8 if necessary. | revert PR/merge SHA, risk/field report and approval |

**Known safe reference point** before any #8 code: `main` at `62c6115ac11af4cb2790b306f885f580b82a146b`; inspect actual latest `main` before a later rollback. P00/P01 commits are documentation and do not create a runtime binary.

## 8. Truthful phase handoff

- P00 = `PASS_SOURCE_AUDIT_ONLY`; P01 = `PASS_EXECUTION_CONTRACT_ONLY`.
- P02–P13 = `NOT_STARTED` as of this contract; M01–M45 = 45 `NOT_RUN`, T01–T15 = 15 `NOT_RUN`, AC01–AC12 = 12 `UNVERIFIED`.
- DoR-01 = merged/owner-attested but independent stability unverified; DoR-02 = architecture APPROVED; DoR-03 = PARTIAL — P02 injectable focus/route reducer/seam outstanding.
- **Next P02:** implement deterministic event/state machine and injectable focus/route test seams. No test execution until separately approved; no APK before P13.
