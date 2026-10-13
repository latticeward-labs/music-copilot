# Issue #4 — Phase 11 automated tests and architecture validation

- **Phase:** 11, independent foundation-test acceptance; **DO NOT close Issue #4**.
- **Status:** **VALIDATION IN PROGRESS — pending green Phase 11 GitHub Actions run**. This is not a claimed PASS.
- **Source:** English `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (12 English reference PDFs), accepted [Issue #16 validation architecture](../architecture/ISSUE_16_VALIDATION.md), ADR-0007/0008, approved [execution plan](ISSUE_4_PHASED_EXECUTION_PLAN.md).
- **Scope:** `feature/4-android-foundation` only. Phase 12 CI sign-off and Phases 13–14 are separate work.
- **Test source:** [app/src/test/java/com/latticeward/musiccopilot/foundation](../../app/src/test/java/com/latticeward/musiccopilot/foundation).
- **Independent Phase 11 run:** [GitHub Actions / Phase 11 validation](https://github.com/latticeward-labs/music-copilot/actions/workflows/issue-4-phase11-validation.yml).
- **Concrete run, current revision:** [Actions run 38480623275](https://github.com/latticeward-labs/music-copilot/actions/runs/38480623275). Its result must be read back before this document is marked PASS.

## Acceptance and gates

| Phase 11 acceptance | Implementation/evidence | Latest result |
|---|---|---|
| Typed errors, public fake contracts and DI composition | `CoreContractsTest`, `ApplicationCompositionTest`, `DataQualityContractsTest`, `PlaybackVoiceContractsTest` | JVM execution pending |
| Serialized command, Busy(32), cancellation and reconnect | `Phase7PlaybackFoundationTest`, `LifecycleScopesTest`, `ControllerSnapshotRelay` | JVM execution pending |
| One Voice generation, timeout 6000ms, one Retry | `Phase8VoiceFoundationTest` with virtual test scheduler and fake STT/TTS | JVM execution pending |
| P1 non-authoritative safe Paused policy | `SafePausedRestorePolicy` and **nine** `Phase11PlaybackPoliciesTest` methods; no autoplay; stale refs dropped; schema rejected | JVM execution pending |
| Previous >3000ms and seek-back 10000ms policy | `PlaybackNavigationPolicy`; no direct Player mutation; tested boundaries | JVM execution pending |
| MediaStore authority, Room projection and DataStore boundaries | `LocalFirstRepositoryTest`, `LocalDataStoreContractsTest` | JVM execution pending |
| Privacy allowlist and fault isolation | `Phase9PrivacyQualityTest`, `check_phase9_privacy.py --self-test` | JVM + negative check pending |
| Exact Compose token/ownership guard | `Phase10ComposeShellTest`, `check_phase10_design_tokens.py --self-test` | JVM + negative check pending |
| Dependency graph, FQN/alias/GlobalScope, forbidden import detection | `check_architecture_dependencies.py --root . --self-test`; 26 positive and negative synthetic cases incl. restore DTO and forbidden Room import | negative gate pending |
| Android lint, formatting and static analysis | `:app:lintDebug`, `spotlessCheck`, `detekt` in independent Phase 11 workflow | real Gradle check pending |

**No undocumented suppressions/baselines:** Android Lint uses `abortOnError=true`, `warningsAsErrors=true`; two pre-existing Gradle version/dependency freshness warnings are explicitly documented in `app/build.gradle.kts`, not added for Phase 11. Detekt and Spotless use real Gradle tasks; all failures must be fixed rather than reclassified as PASS.

**Known discovered issue and fix:** The first strict Lint run found `createOnDeviceSpeechRecognizer()` (API31) indirectly guarded only by `isAvailable()`, not statically provable for minSdk29. A direct `Build.VERSION.SDK_INT < Build.VERSION_CODES.S` check was added in the invocation block. This preserves safe failure on Android 10/11 without replacing on-device STT with an online provider. Recheck with real Lint.

## Accepted Issue #16 test matrix: executed seam vs downstream responsibility

A **JVM fake or pure policy test does not imply a physical system/UI/E2E test**.

| ID | Phase 11 boundary coverage | Full architecture acceptance |
|---|---|---|
| UT-01 | Partial: six intents, `musico` prefix, provider confidence / no guess in `Phase8VoiceFoundationTest` | Full ambiguity/candidate corpus and production search: Issue #9 |
| UT-02 | Partial: serialized worker, 32 pending capacity, typed Busy, stop/cancellation; no production queue mutation | Full queue/index/skip/revision behavior: Issue #7 |
| UT-03 | Partial: pure -10 seconds / negative clamp / duration / unsupported policy | Service execution through real Player: Issue #7 |
| UT-04 | Partial: pure 3-second boundary and first item rule | Service integration and external-control parity: Issue #7 |
| UT-05 | Foundation: typed events, privacy allowlist, rejected inputs, NoOp and throwing sink | All downstream integration privacy and retention: Issue #12 / QA |
| UT-06 | Not executed end to end; foundation typed Interruption error and safe fallback only | Focus, route, call reducer and real events: Issue #11 |
| UT-07 | Partial: coordinator generation/timeout/retry/cancellation/ack | Candidate resolution and full UI confirmation: Issues #9/#10 |
| IT-01 | Service lifecycle and serial worker unit seam | Real MediaSession ↔ Player instrumentation: Issue #7 and Phase 13 |
| IT-02 | `ControllerSnapshotRelay` pure atomic reconnect seam | Real MediaController/session disconnect/reconnect: Issue #7 / Phase 13 |
| IT-03 | Derived index/projection and fake reconciliation | In-memory Room integration and full migration tests: Issue #8 |
| IT-04 | Fake MediaStore permission/requery and Room truth test | Instrumented real MediaStore/permission transition: Issue #8 |
| IT-05 | Fake STT contract, cancellation and provider static guard | Android service capability and cancellation instrumentation: Issue #9 |
| IT-06 | In-memory DataStore-facing proto serializer and safe Paused planner | Cold-process restore through actual session and re-query: Issue #7 / ST-03 |
| IT-07 | Privacy gate + NoOp/throwing sink unit isolation | Optional deployment adapter/config integration: Issue #12 |
| ST-01 | Not executed; UI/service snapshot comparison needs installed feature screens | UI/service sync: Issues #5/#7 and Phase 13 |
| ST-02 | Not executed; pure reconnect + app graph recreation proxies only | Real Activity recreation and service continuity: Issues #5/#7 and Phase 13 |
| ST-03 | Not executed; planner is not process-loss or real Media3 execution | Actual cold-process P1 restore: Issue #7 and Phase 13 |
| ST-04 | Not executed | Real corrupt audio and controlled skip: Issue #7 and QA |
| ST-05 | Not executed | Route, call and audio focus: Issue #11 / Phase 13 |
| ST-06 | Not executed | Voice provider availability/interruption: Issues #9/#11 |
| ST-07 | Not executed | Permission recovery/denial on OS: Issue #8 / Phase 13 |
| ST-08 | Not executed | Accessibility / Driving path on UI and real devices: Issues #5/#10 / Phase 13 |

The 22 IDs above are the **architecture validation matrix**, not 22 purportedly green production scenarios. Unimplemented feature behavior is not brought into Issue #4 solely to inflate coverage.

## How to reproduce Phase 11 locally

```bash
python3 scripts/check_architecture_dependencies.py --root . --self-test
python3 scripts/check_phase9_privacy.py --root . --self-test
python3 scripts/check_phase10_design_tokens.py --root . --self-test
./gradlew :app:testDebugUnitTest :app:lintDebug spotlessCheck detekt --no-daemon --stacktrace
```

JDK 17, compile SDK 36 and Android Build Tools 35.0.0 are supplied in the workflow. A green GitHub Actions run, not this file alone, is the evidence for PASS. Unit tests use synthetic media and fake providers; **no audio/credential/user media is uploaded to CI**.
