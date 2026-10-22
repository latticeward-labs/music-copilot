# Issue #7 / P01 — GitHub execution, evidence, branch, rollback and final-delivery contract

**Date:** 2026-10-22  
**Phase gate:** `P01_PASS_CONTRACT_ONLY` **subject to GitHub write/read-back**; *not* Android feature/Issue AC acceptance.  
**Tracking:** [Issue #7](https://github.com/latticeward-labs/music-copilot/issues/7) remains **OPEN**.  
**Prior evidence:** [P00 source/readiness audit](ISSUE_7_PHASE0_READINESS_AUDIT.md) **PASS_AUDIT_ONLY**.  
**Authority:** exact user-provided English `MSM2(1)(1).rar` (12 PDFs), SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`. Independently checked at P01; agrees with P00 and [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md).

## 1. Actual GitHub entry baseline

| Item | Verified at P01 entry |
|---|---|
| Repository | `latticeward-labs/music-copilot`, private; GitHub connector reports admin/push/read authorization |
| Default/live integration branch | `main`, SHA `5679ee5b5f68b09b7715f3c31c339a2201b3c960` |
| **Already-existing execution branch** | `feature/7-media3-playback-queue-session`; **reuse** (not cloned/recreated or reset) |
| Phase-entry branch commit | `5228e4ff66360e3d9fc024c85f6de2be270fdce0` (P00 audit/plan update). Compare with `main`: 7 ahead / 0 behind, only 5 Issue #7 documentation paths, no new production Kotlin edits |
| Existing Issue #7 PR | **None** found using branch-head PR search at P01 entry; do not create PR in this phase |
| `develop` vs `main` | `develop` **831 commits behind `main`**, 0 ahead in GitHub compare; old branch is not this feature's current integrated production source |
| `CONTRIBUTING.md` | Specifies `feature/<issue>-<short-name>`, one Issue/branch/PR, independent review, preferred squash; **normally integrate via `develop`** |
| Branch protection and rulesets | `main` branch endpoint returns `protected=false`; rulesets fetch returns **HTTP 403** (private-plan access restriction): **ruleset enforcement NOT VERIFIED**. Never assume unavailable protection/review checks enforce anything |
| Project V2 | Field values and transitions **NOT VERIFIED / NOT CHANGED** via Issue REST; `Issue.state=OPEN` is independently observed |
| QA baseline | [M01–M36 and T01–T15](ISSUE_7_FINAL_PHONE_QA_CHECKLIST.md), all **NOT_RUN** in [QA results](ISSUE_7_QA_RESULTS.json) |
| Earlier technical debt | #5 and #6 owner-exception closed; follow-up issues [#73](https://github.com/latticeward-labs/music-copilot/issues/73) and [#74](https://github.com/latticeward-labs/music-copilot/issues/74) remain **OPEN** |

The entry SHA is **provenance, not a permanent freeze against future changes to main**. Before final merge, compare with updated main and reconcile/test the actual candidate SHA.

## 2. Binding product and architecture scope

**Product:** [English PRD repository record](../PRD.md), **FR-02** P0 background and MediaSession, **FR-03** P0 calls/focus/audio route, **FR-08** P1 queue/position persistence; [MVP](../MVP.md) P0 basic Player/queue, stable Android system surfaces and US-03.  
**Architecture:** [ADR-0002](../adr/0002-service-owned-media3-playback.md) and [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md) **Accepted**; [ADR-0003](../adr/0003-local-first-data-boundaries.md), [ADR-0006](../adr/0006-no-backend-account-streaming.md), [#16 runtime policy](../architecture/RUNTIME_POLICIES.md) and public ports.  
**Behavior:** [UF-05](../user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md), [UF-06](../user-flows/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md), [UF-11](../user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md), [UF-12](../user-flows/UF-12_SETTINGS_PRIVACY.md), [GS-02](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md), [GS-03](../user-flows/golden-scenarios/GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md) and [GS-05](../user-flows/golden-scenarios/GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md); approved [#17 WF-04/06/07 handoff](../handoff/SCREEN_SPECS.md).

**Authorized changes:** Android 10+ native local-audio playback, one existing same-process `MediaSessionService` and `ExoPlayer`, real MediaStore-verified Content URIs, visible collection queue, play/pause/next/previous/seek, synchronized UI/system surfaces, safe notification/foreground lifecycle, phone/focus/route safety and bounded corrupt-item skip. P1 queue/position cold restoration through existing Proto DataStore remains **Safe-Paused only**, no autoplay.

**Hard constraints:** no new Player/process/streaming/catalog/backend/accounts/cloud sync/DRM bypass, voice STT/TTS or unrelated driving feature implementation; do not write, move or delete user audio; never emit media titles, artists, paths, URIs, user-specific IDs, device IDs, private queue or raw audio/transcripts to CI/analytics/crash telemetry. `Room` is derived index; `MediaStore` owns existence; service owns live queue; `DataStore` holds non-authoritative P1 restore. No extra user permission without a verified platform requirement. Changes to these ownership rules require explicit ADR/#16 review.

## 3. Ordered P00–P13 gates and allowed work

All implementation work lives on the **one existing feature branch**. Every phase creates a scoped record and updates the [evidence index](ISSUE_7_EVIDENCE_INDEX.json). A phase-scoped PASS is not Issue #7 AC PASS.

| Gate | Owner paths/work | Required phase handoff |
|---|---|---|
| P00 | Attachment/PRD/ADR/issue/current Kotlin audit | **PASS_AUDIT_ONLY**, [report](ISSUE_7_PHASE0_READINESS_AUDIT.md) |
| **P01** | This branch/evidence/rollback contract; future PR template; index | **PASS_CONTRACT_ONLY**, GitHub read-back; **no production Kotlin/APK** |
| P02 | `playback/api`, pure queue/navigation policies and tests | Reviewed identity, order, Previous 3s, seek bounds/end/no-track, idempotence, system command parity contract |
| P03 | `data/mediastore` and playback local URI adapter/tests | MediaStore re-query/permissions per ref and per-volume typed failure, no raw path |
| P04 | Service-owned queue engine and deterministic tests | ReplaceQueue, visible ordered collection, selected index, next/previous/seek/end semantics |
| P05 | Extend existing `MusicPlaybackService` and #6 selection/gateway | Actual ExoPlayer-backed local preparation/play/pause and truthful callback snapshots |
| P06 | `MediaSession`, controller, serialized private/external commands | Notification/lock/media keys, same player truth, restricted private command and reconnect contract |
| P07 | Approved `feature/player`/shared Compose navigation | Full Player, Queue, MiniPlayer, Library/Search handoff and A11Y bound to snapshots |
| P08 | Service lifecycle/foreground/system surfaces | Home, task removal, screen-off/recreate continuity and foreground type, no autoplay |
| P09 | Audio focus, calls, wired/Bluetooth route state reducer | Safe pause/duck/guarded resume, **no speaker leakage or reconnect autoplay** |
| P10 | Recovery/error handling from service through UI | Typed private-safe failure, bounded corrupt/missing/permission skip and viable-queue continuation |
| P11 | Existing `RestorableQueueStore`/Proto DataStore + service | P1 schema/version, 5s checkpoint, re-query, clamp, Paused-only cold restore, live-session precedence |
| P12 | Combined code integration, JVM/instrumentation/Compose/privacy/lint QA preparation | Test sources/fixtures/traceability; executed suites only if actually run; **no user APK** |
| **P13** | Freeze integrated P00–P12 candidate and **one** owner APK | `app-debug.apk`, exact commit/SHA-256, M01–M36 + T01–T15 real results, AC audit, guarded PR/review/merge/close |

**P02 mandatory dependency:** resolve P00's one-item-versus-visible-collection selection discrepancy without breaking #6 stale/permission safeguards. The service currently rejects `ReplaceQueue`; the P00/P01 documents explicitly **do not claim** runnable playback.

## 4. One integrated APK — strict owner testing contract

1. **Never build, deliver, or ask the owner to handset-test a per-phase APK** in P00–P12. The **only owner-facing installable debug APK** is built from the final integrated P00–P12 feature commit in **P13**.
2. Source/JVM static/unit/instrumented test *code* may be authored and scoped technical tests executed before P13 when approved; a framework-generated AndroidTest test APK is **internal test equipment**, not an extra owner distribution. Any internal technical test run must report its actual result separately.
3. P13 records `source_commit`, `apk_sha256`, APK name/link, build method/toolchain, optional Actions run URL, signature/installability evidence and on-device OS/model. Exactly **one integrated final delivery artifact**, not per-phase builds. If a candidate fails, identify its hash/FAIL transparently; a corrected candidate has a new SHA, never claim identical old evidence.
4. Owner alone reports observed phone M01–M36 outcomes against the **exact P13 APK hash**. `PASS` only after real observation; `FAIL`, `BLOCKED`, `N/A`, `NOT_RUN` carry reason as needed. No automatic retroactive #6 PASS, audible-playback PASS from ACK, or system controls PASS from the manifest.
5. One phone suffices for the **owner's first MVP testing scope**, not multi-OEM/API/second phone/head-unit certification. Missing vehicle/Bluetooth/OS evidence stays explicitly `NOT_RUN`/deferred and is tracked at final audit.

## 5. Traceability, evidence naming and status discipline

**Canonical phase register:** [`ISSUE_7_EVIDENCE_INDEX.json`](ISSUE_7_EVIDENCE_INDEX.json) records immutable `P00..P13`, `AC01..AC13`, document/code/test paths, evidence references, provenance and future test references. **Authoritative test outcome ledger:** [`ISSUE_7_QA_RESULTS.json`](ISSUE_7_QA_RESULTS.json), containing **M01–M36** and **T01–T15** at `NOT_RUN` until evidence is collected; never substitute a written test for executed proof.

- Namespaced scoped change IDs: `I7-CHG-P02-001` etc. Each changed path + commit SHA + rollback + reason + source requirement.
- Namespaced evidence IDs: `I7-EVD-P02-001` etc. Each links a single outcome with source SHA, test method/fixture, reporter/executor class, timestamp, results and actual run/file URL. Missing actual result = `NOT_RUN`, not PASS.
- Phase gate statuses: `NOT_STARTED`, `IN_PROGRESS`, `PASS_AUDIT_ONLY`, `PASS_CONTRACT_ONLY`, `PASS_SCOPED`, `BLOCKED`, `FAIL`. Product AC status remains `NOT_VERIFIED` until linked executed acceptance evidence; test status follows QA registry.
- Each P02–P12 commit uses `feat(playback):`, `fix(playback):`, `test(playback):`, `docs(issue-7):`; annotate `Refs #7` in PR. No `Closes #7` until P13 final verified/explicit exception.
- At every phase exit update the phase row, touched paths, reviewed ADR/UF/GS/WF references, test evidence and next phase. Do not erase FAIL, previously rejected candidate or #73/#74 debts.
- For P0 safety, independent manual/technical verification is not optional for a strict engineering PASS; any owner-authorized reduced acceptance must preserve actual `NOT_RUN`/deferred statuses and risk.

## 6. GitHub Actions, review, PR policy, and branch protection

**No P01 workflow dispatch, Actions re-run, `assembleDebug`, lint/JVM/emulator execution, APK publication or Pull Request creation.** Preserve owner's Actions quota. Documentation commits carry `[skip ci]` where supported; a commit suffix is **not proof** an arbitrary workflow cannot run. The existing `android-foundation.yml` has explicit push branches (`main` and `feature/4-android-foundation`) **plus `pull_request` targeting `main`**; creating a PR to main can trigger work. The existing `issue-6-p12-integrated-final.yml` is `workflow_dispatch` only and is **not a #7 delivery workflow**. Do not assume a prior #6 artifact/job proves #7. Any actual run must be recorded only after observing its real output; do not intentionally dispatch workflows for P01.

**Future PR:** ONE feature PR, **planned at P13 only**, source `feature/7-media3-playback-queue-session`, **proposed target `main`**. This **deviates from `CONTRIBUTING.md`'s `develop` recommendation** because GitHub compare shows `develop` is 831 commits behind the live main. The exception is **PROPOSED, NOT APPROVED at P01**; record owner/reviewer authorization before final merge. No unrelated forced reconciliation with stale `develop`.

Use the [prepared final-only PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-7-media3-playback-queue-session.md) but **do not open it now**. P13 review must cover Kotlin/service/URI/privacy/security, route/focus safety, notification/Android SDK, Play/Pause/Queue/Restore, #17 visual/UI/A11Y and test coverage. Do not self-approve as only reviewer; any genuine lack of independent review requires an explicit Issue #7-specific authorized exception with a transparent limitation. PR-head checks may cost Actions minutes; no run/merge before final-phase policy and cost decision. `main` reported `protected=false`, and rulesets could not be queried (403), so **no automated enforcement claim**.

**Merge readiness at P13** needs (a) full integrated code, (b) verified P0 safety regressions and AC evidence/explicitly authorized deferral, (c) privacy/rollback, (d) precise candidate SHA and any permitted CI results, (e) final review and `main` deviation approval. Preferred squash only after review; confirm final merge SHA, Issue #7 state and Project V2 state **separately**. Never set Done/close merely because a PR was created or the APK installed.

## 7. Rollback, conflicts, forbidden edits and escalation

- Pre-merge: isolate changes on the branch, use a new **revert commit** for a defective phase, not force-push/reset to `main`; preserve P00/P01 evidence and change history. Revert implementation while maintaining public/media/Room/Proto compatibility; if migration already shipped, prepare forward-compatible correction instead of destroying local data.
- Conflicts: compare with current `main` before P13, review upstream changes, integrate safely, re-test final SHA. If a phase changes an Accepted ADR, service topology, permission/data authority or #17 approved UX, stop that slice, document explicit review and update the contract before continuing.
- System safety regressions (surprise resume, route leak, duplicate player, unsafe permissions) or loss of local personal media are **P0 blockers**; never waive through a documentation-only PASS. Invalid/revoked media must preserve user data and report typed recovery.
- Never commit real personal audio, signing credentials, raw voice, file metadata, personal identifiers, unredacted traces, or user-secret fixtures. Synthetic tests and sanitized errors only.
- Deferred #73/#74, incomplete OEM/vehicle/API breadth and genuine system-permission boundaries stay visible; do not silently close these other issues.

## 8. P01 acceptance checklist

- [x] RAR exists/readable, SHA-256 matches 12-PDF P00 source; product/architecture hierarchy linked.
- [x] Actual private repository, permissions, branch, main/develop compare, Issue state and missing PR inspected.
- [x] One existing branch reused; P00 preserved, no production code or duplicate branch/PR created.
- [x] Strict P00–P13 phase-gate ownership and exactly one P13 owner APK contract defined.
- [x] Stable 14 phase/13 AC/36 manual/15 technical evidence identities and truthful PASS rules established.
- [x] Review/main-target exception, rollback, Action-quota protection, privacy, issue debt and Project V2 uncertainty recorded.
- [x] Prepared final-only PR template and machine-readable evidence ledger; links verified after write.
- [ ] Android implementation tests, Actions/CI, APK and human QA **NOT PART OF P01**.

**Exit rule:** only mark **`P01_PASS_CONTRACT_ONLY`** after GitHub read-back proves the contract, index, template, plan and Issue note exist and agree. It authorizes **P02 deterministic playback contract work**; it does not pass any of Issue #7's original thirteen ACs.
