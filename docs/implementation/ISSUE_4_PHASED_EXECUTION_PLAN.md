# Issue #4 — Android Foundation & CI: executable phase plan

- **Prepared:** 12 October 2026
- **Status:** **Phases 0–3 PASS (source, GitHub setup, Android bootstrap and package enforcement)** / Phases 4–14 pending; no later phase may be marked PASS without committed evidence
- **Issue:** https://github.com/latticeward-labs/music-copilot/issues/4
- **Delivery branch:** `feature/4-android-foundation`
- **Base:** `main`
- **Authority order:** approved English PRD/MVP; accepted ADR-0001..0008; final `docs/SOFTWARE_ARCHITECTURE.md`; `docs/architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md`; Phase 10 binding corrections; `docs/architecture/RUNTIME_POLICIES.md`; `docs/architecture/ISSUE_16_VALIDATION.md`; Issue #17 approved repository-native design/handoff for *tokens only*.
- **Source archive SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## Boundary and scope

This Issue provides a **buildable, launchable foundation and independently testable architectural seams**, not feature completion. Feature-specific implementation/acceptance belongs to #5 onboarding/permissions, #6 local library/search, #7 full Media3 playback, #8 focus/route, #9 Driving, #10 and #11 Voice, #12 production analytics, #13 comprehensive OEM/vehicle QA. Keep P2 Liked Songs removable. Do not introduce account, backend, online catalog, streaming, cloud sync, always-on wake word, Android Auto, iOS, or other post-MVP capabilities.

Physical production topology is **one `:app` Gradle module**, with logical packages `app/core/feature/playback/voice/data/quality`. Existing `spikes/android-validation` is an isolated evidence harness, not the production application. Changes in owner, process, persistence authority, dependency direction or module strategy need architectural change control; the existence of a previously proposed PDF blueprint is not permission to override final Issue #16 contracts.

**Evidence rule:** each phase needs committed files, execution results, and an audit note linking exact commit/run/device evidence. A planned test or synthetic report does not count as a PASS.

## Phase 0 — Sources and readiness lock

- [x] Reconcile Issue #4 and all 13 remaining Acceptance Criteria with accepted Issue #16/ADR contracts, and latest Issue #17 handoff.
- [x] Record root `main` contents and which existing spike/workflow assets must remain separate.
- [x] Freeze a requirements-to-phase matrix and the prohibition on premature feature/UI work.
- [x] Record any unresolved implementation selection as replaceable tooling, not a new architecture invariant.

**Output:** [`ISSUE_4_BASELINE_AND_TRACEABILITY.md`](ISSUE_4_BASELINE_AND_TRACEABILITY.md), [`ISSUE_4_PHASE0_TRACEABILITY.json`](ISSUE_4_PHASE0_TRACEABILITY.json).  
**PASS achieved 12 October 2026:** 4/4 phase requirements verified, original MSM2 archive SHA + 12/12 original PDF-member hashes matched GitHub source register, 14/14 Issue #4 Acceptance Criteria mapped with 1 verified upstream and 13 pending implementation; 0 architecture blockers. All findings and non-claims are in linked evidence. **This does not check Issue #4 implementation criteria.**

## Phase 1 — GitHub execution setup

- [x] Work on `feature/4-android-foundation` from current `main`; do not commit directly to `main`.
- [x] Update Issue #4 status text to In Progress without checking implementation Acceptance Criteria.
- [x] Adopt a commit plan, scoped PR template/checklist and an evidence index.
- [x] Verify no existing Android production module will be overwritten or confused with the spike harness.

**Output:** Branch [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation), [`ISSUE_4_EVIDENCE_INDEX.md`](ISSUE_4_EVIDENCE_INDEX.md), [`ISSUE_4_PHASE1_GITHUB_EXECUTION.md`](ISSUE_4_PHASE1_GITHUB_EXECUTION.md), [`ISSUE_4_PHASE1_VALIDATION.json`](ISSUE_4_PHASE1_VALIDATION.json) and [scoped PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-4-android-foundation.md).  
**PASS achieved 13 October 2026 (GitHub setup only):** 4/4 checklist requirements satisfied, feature branch is based on current frozen main HEAD (behind 0); Issue is open/In Progress with 1 upstream AC checked and 13 implementation ACs pending; dedicated PR review template and Evidence Index committed; main/spike/approved sources untouched; 16/16 relative links verified. **No production code, PR, CI or APK is claimed.**

## Phase 2 — Reproducible Android Gradle bootstrap

- [x] Create `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml`, checked-in Gradle wrapper, `app/build.gradle.kts` and Android project resources/manifest.
- [x] Choose and lock a compatible published AGP/Gradle/Kotlin/Compose Compiler/JDK/SDK dependency matrix; cite sources and record verification. **minSdk = 29**; record compileSdk and targetSdk explicitly.
- [x] Set up Kotlin, Compose, Coroutines/Flow and lifecycle integration; avoid unneeded libraries.
- [x] Declare only required version-managed Media3, Room, DataStore, test and formatting/static-analysis dependencies. Version pinning must not introduce actual feature implementations.
- [x] Define `debug`/`release` variants; `assembleDebug` must need neither private signing keys nor cloud secrets; retain strict `.gitignore` rules.
- [x] Keep existing `spikes/android-validation` entirely out of production module dependencies.

**Output:** committed root build files, version catalog, verified wrapper (Gradle 8.11.1), `app/src/main/AndroidManifest.xml`, [Build Matrix](BUILD_MATRIX.md), [Phase 2 Validation](ISSUE_4_PHASE2_VALIDATION.json), and real [successful GitHub Actions build](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045) / [APK Artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045/artifacts/11825939906).  
**PASS achieved 13 October 2026:** GHA run `38421736045` at SHA `04044a1c2338453a72f822c5ba0ac8fe98d881b6` executed `./gradlew projects :app:assembleDebug :app:assembleRelease --no-daemon --stacktrace` from a fresh checkout, showed only production `:app`, compiled Kotlin Debug/Release, logged `BUILD SUCCESSFUL`, and uploaded both APK outputs (Release unsigned). Pinned SDK36/minSdk29/JDK17, no secret/signing dependency; separate Spike preserved. Static analysis uses AGP's version-managed Android Lint; current formatting baseline is `.editorconfig`/Kotlin code style, automated formatting gate remains explicitly Phase 11 (no speculative formatter plugin). Gradle configuration cache, actual phone launches, complete CI gates and signed release are **not** claimed; see Build Matrix for two corrected initial workflow failures. No Issue #4 implementation AC checkbox is newly checked by this phase alone.

## Phase 3 — Architecture package skeleton and enforcement conventions

- [x] Under `app/src/main/java/com/latticeward/musiccopilot` establish the Issue #16 logical tree: `app`, `core`, `feature`, `playback`, `voice`, `data`, `quality`.
- [x] Maintain `core/model`, `core/result`, `core/time`, `core/dispatchers` as dependency leaves; put domain/application orchestration in approved core packages.
- [x] Keep `playback/api`, `voice/api`, `data/api`, `quality/api` distinct from concrete implementations; make DI entry point live only in `app/di`.
- [x] Add a documented allowed/forbidden import map and static check under `scripts/` or test sources. It must reject concrete Media3/DAO/Android Speech/quality SDK imports into `feature` and Android UI imports into pure domain.
- [x] Do not add `:core`, `:voice`, `:playback`, etc. as physical modules unless ADR-0008 extraction evidence passes.

**Output:** [52-path package skeleton](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation/app/src/main/java/com/latticeward/musiccopilot), [dependency rules](DEPENDENCY_RULES.md), [CI-enforced Python checker](../../scripts/check_architecture_dependencies.py), [machine validation](ISSUE_4_PHASE3_VALIDATION.json).  
**PASS achieved 13 October 2026:** [GitHub Actions 38424771876](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876) (code SHA `813818c12af5a2d110b314ddcb534db30db452ae`) ran **6 positive + 17 negative deliberate fixtures (23/23)**, source scanner approved **52 paths/54 source files**, Gradle listed only `:app`, Kotlin/Compose Debug and Release compiled, and APK artifacts uploaded; earlier false-positive `R` import fixed and evidence preserved. This is a **package/guard gate only**; Phase 4 public contracts and downstream runtime owners are not yet implemented.

## Phase 4 — Compile the approved public contracts

- [ ] Implement shared `CommandId`, `MediaRef`, `VoiceSessionId`, typed `AppResult`, `AppError`, `Recoverability`, `RecoveryAction` with safe public error codes.
- [ ] Implement `PlaybackCommand`, `PlaybackSnapshot`, `PlaybackGateway` and connection state, applying the Phase 10 correction: `PlaybackSnapshot.lastError: AppError?` with restricted runtime families.
- [ ] Implement `SpeechToTextEngine`, `TextToSpeechEngine`, `VoiceSessionCoordinator` and immutable `VoiceConfirmationOption` list (1..2) on `VoiceSessionState.Confirming` per Phase 10 correction.
- [ ] Implement `LibraryRepository`, `PreferencesRepository`, `RestorableQueueStore` and typed Quality/Telemetry ports from final Issue #16 signatures.
- [ ] Provide fake implementations and contract tests; **do not claim** that full Media3 playback, speech recognition, search or analytics features have been delivered.

**Output:** approved `api` and `core` interfaces/models plus unit tests.  
**PASS:** contracts compile and test; no Android/provider types leak into pure APIs; both Phase 10 fixes present; no duplicate authoritative state.

## Phase 5 — Composition root, ownership and coroutine lifetimes

- [ ] Implement `MusicCopilotApplication`, a minimal `MainActivity`, and `app/di` cross-boundary bindings.
- [ ] Select documented, replaceable DI implementation (Hilt is permitted, not mandated). Avoid service locator and duplicate graphs.
- [ ] Bind application-scoped controller connection, repositories, Voice coordinator, Quality privacy gate and NoOp sink; Player/MediaSession/command processor remain **service-owned**, never singleton-in-Activity.
- [ ] Define injected `DispatcherProvider`, clock, application/repository jobs, ViewModel scopes, service SupervisorJob/Main.immediate; forbid `GlobalScope`.

**Output:** composition wiring, lifetime matrix and scope tests.  
**PASS:** code compiles, scope assertions pass, Activity recreation cannot construct player or duplicate Voice owner.

## Phase 6 — Local-first persistence seams (foundation only)

- [ ] Provide isolated MediaStore authority adapter seam, Room rebuildable index schema/DAO seam and DataStore settings/consent seams.
- [ ] Provide versioned **Proto DataStore** `RestorableQueueSnapshot` as a non-authoritative P1 port; playback owns validation and Paused-only cold restore.
- [ ] Establish schema/export/migration-test scaffolding and fake repositories. Reconciliation/permission revocation and safe-corruption tests may use fixtures.
- [ ] Ensure no personal media sample, real title/path/URI, user database, raw audio/transcript, cloud backend or credentials enter Git.

**Output:** `data/` scaffolds, schema/tests, persistence ownership note.  
**PASS:** media existence never comes from Room truth, live queue never comes from DataStore, corrupt/future snapshot never auto-plays, no main-thread blocking.

## Phase 7 — Media3 service lifecycle and controller foundation

- [ ] Create one same-process `MediaSessionService` and service-owned factory for exactly one Player/MediaSession per service lifecycle, with version-aware manifest/foreground playback configuration.
- [ ] Implement a minimal `PlaybackControllerConnection` / gateway seam; UI only observes authoritative snapshots and sends typed commands.
- [ ] Make command processor serialization/capacity **32**/typed `PLAYBACK_BUSY` testable; no optimistic `Playing`.
- [ ] Test UI recreation, controller disconnect/reconnect complete snapshot, service owner independence, error projection through authoritative snapshot. Put detailed queue/play/seek/system-control feature completion into #7.
- [ ] Define but do not misrepresent unimplemented focus/route/physical field behavior as tested (#8/#13).

**Output:** `playback/` foundation and lifecycle/command contract tests.  
**PASS:** one service/player owner demonstrated at test seam; UI closure/recreation does not create a second authority; reconnect follows final Issue #16 policy.

## Phase 8 — Replaceable Voice contract seam

- [ ] Provide STT/TTS platform adapter interfaces and minimal availability/cancellation adapters or disabled fakes where production platform handling is not yet implemented.
- [ ] Make one application-scoped `VoiceSessionCoordinator`, generation tokens, six-intent deterministic mapper seam, six-second listening window, one retry and stale-callback suppression testable.
- [ ] Voice dispatches playback only through `PlaybackGateway`; no direct MediaSession/ExoPlayer access or transcript persistence.
- [ ] Default remote/cloud STT **disabled** until later provider disclosure/consent/legal/retention approval.
- [ ] Leave feature-complete Voice and PLAY_ARTIST implementation to #10/#11.

**Output:** `voice/` public-port scaffolds and contract tests.  
**PASS:** fake-provider tests prove cancellation/stale-result behavior; no second voice owner and no remote network calls.

## Phase 9 — Privacy-safe Quality and build configuration

- [ ] Define allowlisted typed `TelemetryEvent` -> `TelemetryPrivacyGate` -> sanitized `TelemetrySink` flow.
- [ ] Default to NoOp/offline sink; configure crash/logging only with non-production, privacy-safe, disabled-by-default or sanitized behavior. No production analytics provider or dashboard is required by #4.
- [ ] Reject free-form maps, unknown fields and raw voice, transcript, media titles, artists, paths, IDs/URIs, queue, queries, personal identifiers and secrets.
- [ ] Prove Quality sink failure never blocks playback, startup, permission or Voice.
- [ ] Preserve later actual measurement/analytics implementation for #12.

**Output:** `quality/`, debug/release configuration and privacy negative tests.  
**PASS:** prohibited-field tests fail closed; clean debug boot works without services/credentials; release has no unsafe logging.

## Phase 10 — Minimal Compose startup shell and design token plumbing

- [ ] Implement a real launchable single-Activity Compose root, neutral startup state and lifecycle-aware state collection.
- [ ] Set up only the navigation shell/typed destination contracts defined by Issue #16; Voice is an overlay, not an independent navigation destination.
- [ ] Map token names from Issue #17 `docs/handoff/COMPOSE_TOKEN_MAPPING.md` and `docs/design-system/foundations/`; keep source values/traces documented, avoid invented visual styling.
- [ ] Exclude unapproved feature screens, Driving control layouts, complete onboarding/search/player UX; those belong to downstream #5/#6/#7/#9–#11.

**Output:** launchable `MainActivity`, minimal theme/token bridge, design-handoff mapping.  
**PASS:** startup never crashes on missing library/permission/provider; shell builds at API 29+; token names trace to approved Issue #17.

## Phase 11 — Automated test and architecture verification

- [ ] Unit tests for typed failures, dependency rules, NoOp/privacy rejection, coroutine cancellation, DI graph and public fake contracts.
- [ ] Required earliest policy seams: command serialization/Busy, snapshot reconnect, one Voice generation/retry, safe paused restoration, MediaStore/Room separation and typed asynchronous error projection.
- [ ] Android lint plus formatting/static-analysis tool with zero undocumented exemptions; test intentionally prohibited imports.
- [ ] Report completed vs **deferred to downstream issues** for the full UT-01..UT-07, IT-01..IT-07, ST-01..ST-08 architecture matrix, without claiming unexecuted end-to-end tests.

**Output:** test code, `docs/implementation/ISSUE_4_TEST_EVIDENCE.md`.  
**PASS:** all declared foundation tests actually execute green, negative tests detect violations, deferred work is explicitly owned.

## Phase 12 — Real production CI

- [ ] Add a root-app GitHub Actions workflow, e.g. `.github/workflows/android-foundation.yml`, running for PRs and main; leave existing spike and architecture workflows intact.
- [ ] Perform checkout, documented pinned JDK/Android SDK, Gradle dependency caching and wrapper verification as appropriate.
- [ ] Required checks: formatter, Kotlin static analyzer, Android lint, `:app:testDebugUnitTest`, `:app:assembleDebug`, architecture import/static tests and secret/sensitive-fixture guard.
- [ ] Archive debug APK, test reports and lint outputs with bounded retention; set least-privilege read-only token permissions and concurrency.
- [ ] Provide failure evidence from a deliberate invalid-import/format regression or tested validation fixture (then revert); do not manufacture green CI screenshots.

**Output:** `.github/workflows/android-foundation.yml`, CI evidence links.  
**PASS:** a real GitHub workflow run on Issue #4 branch is green and failure detection is demonstrated.

## Phase 13 — Clean checkout, Android 10+ and two-device smoke

- [ ] Use a truly fresh checkout/workspace without local Gradle caches or private credentials, run documented setup, Gradle sync, unit tests and debug build.
- [ ] Install and launch **the Issue #4 app**, not the separate spike harness, on two distinct representative Android devices; record model, OEM, Android/API version, build commit and timestamp.
- [ ] Prefer API 29 plus Android 14+ coverage; if physical devices are not available, report emulator result as emulator only and leave the required two-device acceptance unverified.
- [ ] Verify startup, background/foreground, process/UI recreation smoke, build variant, permissions/default-denied safety and sanitized local logs. This is not a claim of complete Phone/BT/vehicle QA.
- [ ] Maintain clean evidence with synthetic media only, no personal user assets or identifiers.

**Output:** `docs/implementation/ISSUE_4_DEVICE_SMOKE.md` with real run and APK/CI references.  
**PASS:** two independent recorded launches, clean-checkout build, no secret requirement, no misleading physical QA claim.

## Phase 14 — Final architecture, security and delivery audit

- [ ] Re-check **every** Issue #4 Acceptance Criterion, mapping each to exact file, commit, CI run or device record; gate any missing item as FAIL/OPEN.
- [ ] Run architecture direction, app manifest, dependency/version and forbidden scope audits. Confirm one physical `:app`, one Playback owner, one Voice owner, no cloud/streaming/account.
- [ ] Record Product/Android architecture/QA/Accessibility/Privacy review evidence without inventing human approvals. Link downstream minors to #5–#13; ensure no open Blocker/Major for Issue #4.
- [ ] Create a focused PR from `feature/4-android-foundation` to `main`, review green CI, resolve blocking feedback, then merge.
- [ ] Close Issue #4 as completed and update project board Done **only after** all #4 criteria and merge evidence pass. Update downstream #5/#6/#7/#12 readiness; Issue #17 design gate is already complete.

**Output:** `docs/implementation/ISSUE_4_FINAL_ACCEPTANCE_AUDIT.md`, final PR/merge and Issue closeout links.  
**PASS:** all acceptance criteria have real evidence, required reviews and CI are green, PR merged; otherwise leave Issue #4 open with precise blockers.

## Acceptance-to-phase crosswalk

| Issue #4 acceptance criterion | Gate |
|---|---|
| #16 approved | 0 (already satisfied in original Issue) |
| Kotlin/Compose/Android 10+ | 2, 10, 13 |
| Version-managed dependencies/no speculative SDK | 2, 14 |
| Approved package structure | 3, 14 |
| Enforced dependency direction | 3, 11 |
| Player UI-lifecycle independence | 4, 5, 7, 11 |
| Replaceable Voice and Quality ports | 4, 8, 9 |
| Debug/release separation | 2, 9 |
| CI formatter/static analysis/tests/debug APK | 11, 12 |
| Privacy-safe non-production crash logging | 9, 12 |
| Two representative device launches | 13 |
| Clean checkout without credentials | 2, 12, 13 |
| No secrets/personal audio/media committed | 2, 6, 9, 12, 14 |
| #16/ADRs architecture conformance notes | 0, 3, 14 |

## Release discipline

A foundation test does not substitute for later real-device/vehicle testing, legal cloud-STT approval, complete feature acceptance or independent post-MVP human design sign-off. Keep test fixtures and evidence truthful. No acceptance box may be checked based only on this execution plan.
