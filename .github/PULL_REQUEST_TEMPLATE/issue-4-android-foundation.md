# Issue #4 — Android Foundation & CI (scoped delivery)

> **Template scope:** use for the FINAL implementation PR from `feature/4-android-foundation` only after Phases 0–14 have been evidence-audited. This is a preparation template, not a completed PR or reviewer approval.
>
> **Issue:** [#4](https://github.com/latticeward-labs/music-copilot/issues/4). **Implementation contract:** [Phase plan](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/docs/implementation/ISSUE_4_PHASED_EXECUTION_PLAN.md), [Phase 0 baseline](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/docs/implementation/ISSUE_4_BASELINE_AND_TRACEABILITY.md), [Evidence index](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/docs/implementation/ISSUE_4_EVIDENCE_INDEX.md), [Issue #16 final Software Architecture](https://github.com/latticeward-labs/music-copilot/blob/main/docs/SOFTWARE_ARCHITECTURE.md).
>
> **Branch/base:** feature/4-android-foundation from approved main baseline. The target PR base is `main` under Issue #4's recorded project-specific final delivery path (same as #16 PR #46 and #17 PR #68). **Exception to CONTRIBUTING.md's generic `develop` integration recommendation must be explicit and accepted in final review.** Do not merge to stale `develop` silently. If an integration target is changed, reconcile ancestry, CI and approvals first.

## Outcome and current gate

Refs #4. Do **not** use `Closes #4` before all 14 Acceptance Criteria are actually verified and final review authorizes closeout.

- One production Android 10+ (minSdk 29) native Kotlin/Compose `:app`, with GitHub Actions build/test gates and no proprietary credentials.
- Zero new backend, account, streaming, cloud sync, wake-word, Android Auto, iOS or unsupported MVP scope.
- **Implementation/QA status:** [ ] Not ready yet  [ ] Final verification complete (attach verified GitHub evidence).

## Required evidence (fill real commit IDs, run URLs and file paths; otherwise leave unchecked)

| Gate | Evidence URL / commit | Status |
|---|---|---|
| Phase 0 Source/Authority/AC baseline | [Phase 0 report](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/docs/implementation/ISSUE_4_BASELINE_AND_TRACEABILITY.md) | Verified specification only |
| Phase 1 branch/PR/evidence setup | [Phase 1 audit](https://github.com/latticeward-labs/music-copilot/blob/feature/4-android-foundation/docs/implementation/ISSUE_4_PHASE1_GITHUB_EXECUTION.md) | Verified after Phase 1 audit |
| Phase 2 Gradle+Android minSdk 29 clean project | _TBD_ | Pending |
| Phase 3 one `:app`, package direction/negative checker | _TBD_ | Pending |
| Phase 4 typed Core/Playback/Voice/Data/Quality ports and Phase 10 corrections | _TBD_ | Pending |
| Phase 5 DI/lifetime and no UI-owned Player | _TBD_ | Pending |
| Phase 6 local-first adapters and Safe-Paused snapshot seam | _TBD_ | Pending |
| Phase 7 service-owned Media3 controller + command serialization | _TBD_ | Pending |
| Phase 8 replaceable Voice, bounded generation/retry tests | _TBD_ | Pending |
| Phase 9 privacy-safe Quality/NoOp and debug/release separation | _TBD_ | Pending |
| Phase 10 minimal Compose shell and approved token handoff | _TBD_ | Pending |
| Phase 11 architecture/unit/static tests and reports | _TBD_ | Pending |
| Phase 12 real root-Android GitHub CI workflow run and APK artifact | _TBD_ | Pending |
| Phase 13 fresh checkout and 2 distinct device install/launch evidence | _TBD_ | Pending |
| Phase 14 AC-by-AC audit and cross-role review | _TBD_ | Pending |

## Definition of Done — original Issue #4 Acceptance Criteria

- [ ] #16 architecture approved (upstream already satisfied; link final gate).
- [ ] Kotlin/Compose Android 10+ actually builds/launches.
- [ ] Dependencies version-managed, no speculative framework.
- [ ] Approved one-production-`:app` package/module architecture.
- [ ] Dependency boundaries enforced with positive and negative tests.
- [ ] Playback service ownership independent of UI, by real contract/test seam.
- [ ] Voice and Quality replaceable ports present.
- [ ] Debug/release builds separated.
- [ ] Root Android CI runs format, static analysis, tests, debug build.
- [ ] Crash/log configuration privacy-safe in nonproduction.
- [ ] App launches on two representative devices, with model/API/build evidence.
- [ ] Fresh clone builds without private secrets.
- [ ] Tracked files contain no signing key, service account, raw voice, personal media metadata or unauthorized audio.
- [ ] Code-level architecture conformance linked to Issue #16 and accepted ADRs.

> Unchecked here is intentional at Phase 1. The original Issue's **one upstream checkbox** stays checked independently. A documentation link alone is not implementation PASS.

## Test/CI evidence

- [ ] Gradle wrapper verified, clean checkout/dependency resolution succeeds.
- [ ] `./gradlew :app:assembleDebug` passes from root.
- [ ] Unit/architecture tests, negative import checks and formatting pass.
- [ ] Android Lint and build-artifact upload pass in **real root Android GitHub Actions** CI (not the isolated spike workflow).
- [ ] Device 1: actual model, OEM, API, date/time, commit, installation + launch result.
- [ ] Device 2: actual model, OEM, API, date/time, commit, installation + launch result.
- [ ] No unrun test / emulator is mislabeled as physical evidence.
- [ ] Failure/recovery and deferred feature tests are accurately marked (Issues #5–#13).

**Build/CI URLs:** _TBD_  
**Debug APK artifact/run link:** _TBD_  
**Exact tested commit SHA:** _TBD_

## Architecture and scope review

- [ ] Source hierarchy, ADR-0001..0008, Issue #16 final contract and Phase 10 corrections respected.
- [ ] Only Playback service constructs Player/MediaSession; one authoritative queue, no separate process.
- [ ] `VoiceSessionState.Confirming.options` immutable 1–2; `PlaybackSnapshot.lastError` typed per Phase 10.
- [ ] MediaStore/Room/DataStore ownership and Safe-Paused P1 separation respected.
- [ ] Only `app/di` owns production composition; Hilt optional/replaceable.
- [ ] No forbidden P1/P2 feature creep or invented feature UI; Issue #17 canonical tokens only.
- [ ] No original source PDFs/approved historical review assets overwritten.
- [ ] Spike harness untouched and excluded from root production settings/CI.
- [ ] Upstream and downstream owners recorded for any unimplemented feature tests.

## Privacy, security, accessibility, rollback

- [ ] No raw voice/transcript, local title/artist/path/content URI or keys in Git, telemetry or CI artifacts.
- [ ] Cloud STT/remote provider remains off until Privacy/Legal gate.
- [ ] Quality sink failure cannot block startup/playback.
- [ ] Accessibility: documented present shell checks, deferred feature screens and 200%/TalkBack tests tagged to downstream gates.
- [ ] Rollback path documented (revert PR without schema/user-media loss).
- [ ] Any migration explicitly documented/tested; `No migration` only if verified.

**Risk/rollback statement:** _TBD_

## Review / merge policy

- [ ] Record reviewer identity, date, role and concrete evidence. **AI-assisted review is clearly labeled**; no invented independent human signature.
- [ ] Independent reviewers requested or independent-approval requirement explicitly addressed per repository `CONTRIBUTING.md`; self-approval alone is insufficient.
- [ ] `main` PR base exception vs stale `develop` is openly reviewed and accepted before merge.
- [ ] Blocking (Blocker/Major) findings resolved with commits/tests; no unreviewed ownership/ADR change.
- [ ] All required checks green at final PR HEAD; no silent bypass of branch protections.
- [ ] Final Issue #4 audit records 14/14 real AC evidence before requesting merge.
- [ ] Squash merge preferred; confirm resulting merge SHA.
- [ ] Close #4/Project Done only after required merge and verification, **not** merely after creating this PR.

**Reviewers and dates:** _TBD_  
**Decision / unresolved findings:** _TBD_  
**Final PR/merge:** _TBD_
