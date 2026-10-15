# Issue #4 — Android Foundation & CI: executable phase plan

- **Prepared:** 12 October 2026
- **Current verified closeout (16 October 2026):** **Phase 0–14: PASS for the owner-amended, reduced one-handset foundation acceptance and expedited review process**. [PR #71](https://github.com/music-copilot-organization/music-copilot/pull/71) merged into `main` as `4536cccced3cfbfa0e517bd0d81bd4a5a9060364`, [Issue #4](https://github.com/music-copilot-organization/music-copilot/issues/4) closed completed, final `main` CI [#38815204849](https://github.com/music-copilot-organization/music-copilot/actions/runs/38815204849) success. **Original two real handsets/ADB tests and independent human reviews NOT executed**, documented in [exception addendum](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md); Project V2 status changed automatically but exact `Done` value cannot be verified. Issue #5 untouched.
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

- [x] Implement shared `CommandId`, `MediaRef`, `VoiceSessionId`, typed `AppResult`, `AppError`, `Recoverability`, `RecoveryAction` with safe public error codes.
- [x] Implement `PlaybackCommand`, `PlaybackSnapshot`, `PlaybackGateway` and connection state, applying the Phase 10 correction: `PlaybackSnapshot.lastError: AppError?` with restricted runtime families.
- [x] Implement `SpeechToTextEngine`, `TextToSpeechEngine`, `VoiceSessionCoordinator` and immutable `VoiceConfirmationOption` list (1..2) on `VoiceSessionState.Confirming` per Phase 10 correction.
- [x] Implement `LibraryRepository`, `PreferencesRepository`, `RestorableQueueStore` and typed Quality/Telemetry ports from final Issue #16 signatures.
- [x] Provide fake implementations and contract tests; **do not claim** that full Media3 playback, speech recognition, search or analytics features have been delivered.

**Output (committed):** [typed Core/Playback/Voice/Data/Quality contracts and three JVM test suites](../../app/src/), [Phase 4 contract review](ISSUE_4_PHASE4_PUBLIC_CONTRACTS.md) and [machine-readable validation](ISSUE_4_PHASE4_VALIDATION.json).  
**PASS achieved 13 October 2026:** GitHub Actions [run 38427936983](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983) on SHA `995050eb6ee02bdb7cfcb823a062b6fcc67fd775` completed **success**, executing 24/24 architecture fixtures (7 valid and 17 deliberately forbidden), real Kotlin compile, `:app:testDebugUnitTest`, `:app:assembleDebug` and `:app:assembleRelease` (**BUILD SUCCESSFUL in 6m 6s**) and uploading [artifact 11829238672](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/artifacts/11829238672) with APKs and JUnit reports. Phase 10 corrected options (1–2) and playback error projection (only MediaItem/Playback/Interruption) are covered by JVM tests; no SDK leak or forbidden dependency edge was found. Tests are **public-seam fakes**, not proof of runtime service/Voice/Data adapters, DI lifecycle, on-device behavior, full privacy gate or production analytics. **No original Issue #4 acceptance checkbox is additionally checked from Phase 4 alone.**

## Phase 5 — Composition root, ownership and coroutine lifetimes

- [x] Implement `MusicCopilotApplication`, a minimal `MainActivity`, and `app/di` cross-boundary bindings.
- [x] Select documented, replaceable DI implementation (Hilt is permitted, not mandated). Avoid service locator and duplicate graphs.
- [x] Bind application-scoped controller connection, repositories, Voice coordinator, Quality privacy gate and NoOp sink; Player/MediaSession/command processor remain **service-owned**, never singleton-in-Activity.
- [x] Define injected `DispatcherProvider`, clock, application/repository jobs, ViewModel scopes, service SupervisorJob/Main.immediate; forbid `GlobalScope`.

**Output:** [DI/lifecycle matrix](ISSUE_4_PHASE5_DI_LIFECYCLE.md), [machine validation](ISSUE_4_PHASE5_VALIDATION.json), graph source and JVM scope tests.  
**PASS achieved 13 October 2026:** [GitHub Actions run 38431003572](https://github.com/latticeward-labs/music-copilot/actions/runs/38431003572) for implementation SHA `9cc97090b74d625221a0074670a5fce37b47f148` (job `115413044246`, conclusion **success**) passed 24/24 architecture fixtures (7 allowed/17 rejected), checked one `:app`/52 logical packages/83 Kotlin files, ran `:app:testDebugUnitTest`, `:app:assembleDebug`, `:app:assembleRelease`, and logged **BUILD SUCCESSFUL in 6m 30s**. [Artifact 11831057163](https://github.com/latticeward-labs/music-copilot/actions/runs/38431003572/artifacts/11831057163) includes APKs and JVM reports. App-scoped DI and test seam have passed; production service/Player, physical Activity recreation, real storage/provider behavior and device launches remain later phases. Original Issue #4 ACs remain **1/14 checked**.

## Phase 6 — Local-first persistence seams (foundation only)

- [x] Provide isolated MediaStore authority adapter seam, Room rebuildable index schema/DAO seam and DataStore settings/consent seams.
- [x] Provide versioned **Proto DataStore** `RestorableQueueSnapshot` as a non-authoritative P1 port; playback owns validation and Paused-only cold restore.
- [x] Establish schema/export/migration-test scaffolding and fake repositories. Reconciliation/permission revocation and safe-corruption tests may use fixtures.
- [x] Ensure no personal media sample, real title/path/URI, user database, raw audio/transcript, cloud backend or credentials enter Git.

**Output:** [Local Data ownership/implementation](ISSUE_4_PHASE6_LOCAL_DATA.md), [Migration and schema contract](ISSUE_4_PHASE6_MIGRATIONS.md), [Room v1 exported schema](../../app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/1.json), [machine-validation report](ISSUE_4_PHASE6_VALIDATION.json), source and synthetic tests.  
**PASS for Phase 6 foundation achieved 13 October 2026:** [GHA run 38435760787](https://github.com/latticeward-labs/music-copilot/actions/runs/38435760787) at implementation SHA `f9f60fbcc2446c67ed4e4abf2167d5eacbf21202` (job `115429192025`, **completed/success**) logged `SELFTEST PASS: 24 fixtures`, `ARCHITECTURE PASS: one :app module; 52 package paths; 93 source files`, passed **42 JUnit tests across 8 suites, 0 failures/errors/skips**, built Debug and unsigned Release APK, exported required Room v1 JSON with byte-identical committed blob SHA `c3b1ba7affdaa1a9d6425d3b3dc7ddf10adf300b`, and reported **BUILD SUCCESSFUL in 7m 32s**. [Artifact 11833926331](https://github.com/latticeward-labs/music-copilot/actions/runs/38435760787/artifacts/11833926331). Unit tests and real Android compilation validate the *foundation* only; no real device MediaStore scan, migration from a released schema, or MediaSession P1 restore was executed; those independent later acceptance gates remain open. Original Issue #4 acceptance checklist unchanged.

## Phase 7 — Media3 service lifecycle and controller foundation

- [x] Create one same-process `MediaSessionService` and service-owned factory for exactly one Player/MediaSession per service lifecycle, with version-aware manifest/foreground playback configuration.
- [x] Implement a minimal `PlaybackControllerConnection` / gateway seam; UI only observes authoritative snapshots and sends typed commands.
- [x] Make command processor serialization/capacity **32**/typed `PLAYBACK_BUSY` testable; no optimistic `Playing`.
- [x] Test UI recreation, controller disconnect/reconnect complete snapshot, service owner independence, error projection through authoritative snapshot. Put detailed queue/play/seek/system-control feature completion into #7.
- [x] Define but do not misrepresent unimplemented focus/route/physical field behavior as tested (#8/#13).

**Output:** [Phase 7 implementation and lifetime review](ISSUE_4_PHASE7_MEDIA3_FOUNDATION.md), [machine validation](ISSUE_4_PHASE7_VALIDATION.json), compiled `playback/` production sources and [real CI/APK/JUnit artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/38442180745/artifacts/11837751263).  
**PASS — 13 October 2026, bounded foundation:** [GitHub Actions run 38442180745](https://github.com/latticeward-labs/music-copilot/actions/runs/38442180745) at tested source commit `6c84d6896b016d80913e81097c69ac26165f0e93`, job `115451846096`, **completed/success**. Checked 24/24 architecture fixtures, 99 Kotlin/Java production source files across 52 logical package paths, `testDebugUnitTest` **49/49 tests, 9 suites, 0 failures/errors/skips**, Debug APK, unsigned Release APK, unchanged Room v1 schema export, **BUILD SUCCESSFUL in 7m 34s**. Single-owner service factory, controller snapshot gate and 32-pending serial processor have passing JVM contract tests. Physical Activity recreation, Android OEM/route/focus scenarios, local queue playback, complete P1 paused cold-restore and downstream Issue #7 behavior remain untested/unimplemented and are not claimed as PASS.

## Phase 8 — Replaceable Voice contract seam

- [x] Provide STT/TTS platform adapter interfaces and minimal availability/cancellation adapters or disabled fakes where production platform handling is not yet implemented.
- [x] Make one application-scoped `VoiceSessionCoordinator`, generation tokens, six-intent deterministic mapper seam, six-second listening window, one retry and stale-callback suppression testable.
- [x] Voice dispatches playback only through `PlaybackGateway`; no direct MediaSession/ExoPlayer access or transcript persistence.
- [x] Default remote/cloud STT **disabled** until later provider disclosure/consent/legal/retention approval.
- [x] Leave feature-complete Voice and PLAY_ARTIST implementation to #10/#11.

**Output:** [Phase 8 Voice Provider implementation and limitations](ISSUE_4_PHASE8_VOICE_PROVIDER.md), [Phase 8 machine validation JSON](ISSUE_4_PHASE8_VALIDATION.json), Android adapter/contract source and 9 additional synthetic tests.  
**PASS (13 October 2026, foundation-only scope):** [GitHub Actions run #38454339598](https://github.com/latticeward-labs/music-copilot/actions/runs/38454339598), code SHA `5619393f10bc33b14e14d64b2b641971b8b8fe7b`, job `115493062059`, completed **success**: 24/24 architecture fixtures; 103 source files in 52 package paths; 58/58 JVM tests across 10 suites (9 new Voice tests), zero failures/errors/skips; Debug and unsigned Release APK; `ROOM_SCHEMA_EXPORT PASS`, `BUILD SUCCESSFUL in 7m 22s`. [Artifact #11842495336](https://github.com/latticeward-labs/music-copilot/actions/runs/38454339598/artifacts/11842495336). Android 10/11 production Voice STT intentionally fail-closed (API31 on-device-only support); no on-device recognizer/OEM evaluation, cloud STT, PLAY_ARTIST matching, Voice UI/ducking or field QA is claimed.

## Phase 9 — Privacy-safe Quality and build configuration

- [x] Define allowlisted typed `TelemetryEvent` -> `TelemetryPrivacyGate` -> sanitized `TelemetrySink` flow.
- [x] Default to NoOp/offline sink; configure crash/logging only with non-production, privacy-safe, disabled-by-default or sanitized behavior. No production analytics provider or dashboard is required by #4.
- [x] Reject free-form maps, unknown fields and raw voice, transcript, media titles, artists, paths, IDs/URIs, queue, queries, personal identifiers and secrets.
- [x] Prove Quality sink failure never blocks playback, startup, permission or Voice.
- [x] Preserve later actual measurement/analytics implementation for #12.

**Output:** [Phase 9 Security/Privacy/Quality implementation, constraints and acceptance](ISSUE_4_PHASE9_SECURITY_PRIVACY_QUALITY.md), [machine-readable validation](ISSUE_4_PHASE9_VALIDATION.json), 13 negative/positive JVM fixtures and [CI/real APK/JUnit artifact](https://github.com/latticeward-labs/music-copilot/actions/runs/38457642495/artifacts/11844128692).  
**PASS — foundation implementation and automated test scope (13 October 2026):** [CI run 38457642495](https://github.com/latticeward-labs/music-copilot/actions/runs/38457642495), tested code `a5b52796c80120bbe4be84d6d440717412eb37b7`, job `115504584241` **completed/success**. Static policy self-test (safe baseline + 3 rejected violations), 24/24 architecture fixtures, 105 production sources across 52 logical package paths; **71/71 JVM tests in 11 suites**, no failures/errors/skips; Debug APK and unsigned Release APK; immutable Room schema v1; Gradle `BUILD SUCCESSFUL in 7m 28s`. Enforced non-network/NoOp production quality, strict eight-event allowlist, release-disabled unsafe logging/diagnostics and excluded local backup. **Limitation:** actual Debug APK cold boot on emulator/physical device and OEM backup/logcat behavior were *not exercised*; only no-secret boot-path composition and APK builds are validated in JVM/CI. Full functional/device security acceptance remains a later gate, and telemetry collection/dashboards belong to #12.

## Phase 10 — Minimal Compose startup shell and design token plumbing

- [x] Implement a real launchable single-Activity Compose root, neutral startup state and lifecycle-aware state collection.
- [x] Set up only the navigation shell/typed destination contracts defined by Issue #16; Voice is an overlay, not an independent navigation destination.
- [x] Map token names from Issue #17 `docs/handoff/COMPOSE_TOKEN_MAPPING.md` and `docs/design-system/foundations/`; keep source values/traces documented, avoid invented visual styling.
- [x] Exclude unapproved feature screens, Driving control layouts, complete onboarding/search/player UX; those belong to downstream #5/#6/#7/#9–#11.

**Output:** launchable `MainActivity`, minimal theme/token bridge, design-handoff mapping.  
**Phase 10 PASS (Compose/Design System + API29 emulator) — 13 October 2026:** All **4/4 Phase 10 tasks** have implemented code, exact Issue #17 Night Signal token trace, and real CI. [Code/design CI #38460889744](https://github.com/latticeward-labs/music-copilot/actions/runs/38460889744) passed **80/80 JVM tests**, 24 architecture fixtures, design/privacy negative fixtures and Debug + unsigned Release APK. [Final API29 emulator CI #38475056625](https://github.com/latticeward-labs/music-copilot/actions/runs/38475056625) at `df23268a288813e5a4b5a59dd59ee72b47f61dcd` **completed/success** (job `115563579785`): real debug APK install; `Status: ok`, `LaunchState: COLD`; resumed `MainActivity` and live process after 7 seconds; READ_EXTERNAL_STORAGE/RECORD_AUDIO denied as expected; empty app crash buffer and actual **1080×2220 Compose Bootstrap screenshot** (SHA-256 `4d61cf50694b2a6463e8bc31a4e9a2ea5b021379f349c1f2210aaa2adf40bf88`) archived in [runtime Artifact #11853821575](https://github.com/latticeward-labs/music-copilot/actions/runs/38475056625/artifacts/11853821575). The screenshot was extracted and visually inspected: Night Signal dark theme, title and truthful no-media/permission state rendered, without unapproved feature UI. Earlier failed workflow attempts are retained in [Phase 10 report](ISSUE_4_PHASE10_COMPOSE_SHELL.md); they are not counted as successful. [Machine validation](ISSUE_4_PHASE10_VALIDATION.json) documents the final actual result. **This passes scoped Phase 10, not the entire Issue #4:** Phases 11–14, two representative device runs, full UI/Voice/Playback feature tests, TalkBack/200% and OEM QA remain pending; Issue #4 stays OPEN/In Progress.

## Phase 11 — Automated test and architecture verification

- [x] Unit tests for typed failures, dependency rules, NoOp/privacy rejection, coroutine cancellation, DI graph and public fake contracts.
- [x] Required earliest policy seams: command serialization/Busy, snapshot reconnect, one Voice generation/retry, safe paused restoration, MediaStore/Room separation and typed asynchronous error projection.
- [x] Android lint plus formatting/static-analysis tool with zero undocumented exemptions; test intentionally prohibited imports.
- [x] Report completed vs **deferred to downstream issues** for the full UT-01..UT-07, IT-01..IT-07, ST-01..ST-08 architecture matrix, without claiming unexecuted end-to-end tests.

**Output:** test code, `docs/implementation/ISSUE_4_TEST_EVIDENCE.md`.  
**PASS:** all declared foundation tests actually execute green, negative tests detect violations, deferred work is explicitly owned.

**Actual Phase 11 PASS (14 October 2026):** [independent GitHub Actions run #38548276220](https://github.com/latticeward-labs/music-copilot/actions/runs/38548276220) on implementation SHA [`0a3c42071c65f67e6238960d1667216815074792`](https://github.com/latticeward-labs/music-copilot/commit/0a3c42071c65f67e6238960d1667216815074792) completed **success**, including 26 architecture positive/negative fixtures, privacy and design-token negative self-tests, `:app:testDebugUnitTest` **94/94 across 14 suites (0 failures/errors/skips)**, `:app:lintDebug` **zero reported issues**, `spotlessCheck` and `detekt` **green**. [Archived XML reports, lint, Gradle log](https://github.com/latticeward-labs/music-copilot/actions/runs/38548276220/artifacts/11886011071) were independently inspected. The [Phase 11 evidence document](ISSUE_4_TEST_EVIDENCE.md) records strictly scoped, justified lint exceptions and all UT/IT/ST downstream owners. This is **Phase 11 only**; Phase 12+ not signed off.

## Phase 12 — Real production CI

- [x] Add a root-app GitHub Actions workflow, e.g. `.github/workflows/android-foundation.yml`, running for PRs and main; leave existing spike and architecture workflows intact.
- [x] Perform checkout, documented pinned JDK/Android SDK, Gradle dependency caching and wrapper verification as appropriate.
- [x] Required checks: formatter, Kotlin static analyzer, Android lint, `:app:testDebugUnitTest`, `:app:assembleDebug`, architecture import/static tests and secret/sensitive-fixture guard.
- [x] Archive debug APK, test reports and lint outputs with bounded retention; set least-privilege read-only token permissions and concurrency.
- [x] Provide failure evidence from a deliberate invalid-import/format regression or tested validation fixture (then revert); do not manufacture green CI screenshots.

**Output:** `.github/workflows/android-foundation.yml`, CI evidence links.  
**PASS:** a real GitHub workflow run on Issue #4 branch is green and failure detection is demonstrated.

**Phase 12 PASS — 14 October 2026:** independent [production Android Foundation CI run #38556526636](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636) is **completed/success** on validated code SHA [`c0131d4ef9fd9de39937d354fd1489bf092c8c84`](https://github.com/latticeward-labs/music-copilot/commit/c0131d4ef9fd9de39937d354fd1489bf092c8c84). [Artifact #11890378853](https://github.com/latticeward-labs/music-copilot/actions/runs/38556526636/artifacts/11890378853) was downloaded and independently inspected: signed-off Debug APK, **94/94 JUnit tests in 14 suites (0 failures/errors/skips)**, `lint-results-debug.xml` **0 reported issues**, Detekt XML **0 findings**, genuine invalid-format fixture `Phase12FormatProbe.kt` fails `:spotlessKotlinAndGradleWhitespaceCheck` with an actual whitespace diff and then clean `spotlessCheck` passes. Production checks include a one-module root app, Gradle wrapper validation/cache, pinned JDK 17 + SDK 36 + Build Tools 35.0.0, positive/negative architecture fixtures, privacy and secret/media fixture guards. CI has read-only token, per-commit concurrency isolation, and 14-day artifact retention. See [Phase 12 CI evidence](ISSUE_4_PHASE12_CI_EVIDENCE.md). Configured `push: main` / `pull_request: main` triggers exist, but **there is no claim of a post-merge main-branch run or a PR run**. The two device/vehicle QA gate and Phases 13–14 remain pending.

## Phase 13 — Clean checkout and foundation handset smoke (owner-amended 16 October 2026; original two-device protocol preserved below)

- [x] Use a truly fresh checkout/workspace without local Gradle caches or private credentials, run documented setup, Gradle sync, unit tests and debug build.
- [x] **Owner-revised 16 October 2026 acceptance:** demonstrate the root Issue #4 Bootstrap app visibly launching on **one** real Android 10+ handset, with screenshot/user report of return/relaunch and disclosed limitations. The original two-device ADB verification is **deferred / not executed**, not silently marked tested. See [scope addendum](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md) and [HONOR X7d evidence](ISSUE_4_HONOR_X7D_ANDROID15_MANUAL_SMOKE.md).
- [x] Prefer API 29 plus Android 14+ coverage; if physical devices are not available, report emulator result as emulator only and leave the required two-device acceptance unverified.
- [x] Verify startup, background/foreground, process/UI recreation smoke, build variant, permissions/default-denied safety and sanitized local logs. This is not a claim of complete Phone/BT/vehicle QA.
- [x] Maintain clean evidence with synthetic media only, no personal user assets or identifiers.

**Output:** `docs/implementation/ISSUE_4_DEVICE_SMOKE.md` with real run and APK/CI references.  
**Owner-revised PASS:** one user-reported physical Bootstrap launch with image evidence, two independently tested emulators, clean checkout/CI, no secrets, and explicit acknowledgement that the earlier two-physical-device ADB protocol was **not** executed. See [decision addendum](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md).

**Verified Phase 13 emulator-only smoke — 14 October 2026:** [GitHub Actions run #38559652982](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982) **completed/success with two separately executed clean-build and real install/relaunch jobs** on code SHA [`dbdea7061b375b4660febed1f49e1d5e90c91af8`](https://github.com/latticeward-labs/music-copilot/commit/dbdea7061b375b4660febed1f49e1d5e90c91af8): Android 10/API29 [artifact #11891574050](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982/artifacts/11891574050) and Android 15/API35 [artifact #11892145524](https://github.com/latticeward-labs/music-copilot/actions/runs/38559652982/artifacts/11892145524). Independently downloaded and parsed each artifact: **94/94 tests across 14 suites per job; zero failed/errors/skipped**; real Debug APK and unsigned Release; sanitized JSON confirms installation, cold start, background/foreground, force-stop/relaunch, denied runtime permissions and no app crash; screenshots visually inspected. Both are **Google Android emulators**, not real Pixel/Samsung physical devices. **Phase 13 PARTIAL (4/5 criteria): required physical two-device acceptance is still BLOCKED/UNCHECKED. DO NOT MARK THIS PHASE PASS.** Report and operator commands in [ISSUE_4_DEVICE_SMOKE.md](ISSUE_4_DEVICE_SMOKE.md).

**Historical pre-scope-change, 16 October 2026 handset evidence:** Tester reported HONOR X7d / Android 15 bootstrap screenshot, Home return, recents close/relaunch and no startup permission prompt. See [HONOR X7d evidence](ISSUE_4_HONOR_X7D_ANDROID15_MANUAL_SMOKE.md). This is **1/2 user-reported phone**, **0/2 ADB independently confirmed phones**. The Phase 13 two-phone [ ] checkbox remains open and no Phase 14 checkbox is artificially advanced. Issue #5 intentionally remains untouched.

**Owner-updated Phase 13 disposition (16 October 2026): PASS for revised one-physical-manual-smoke foundation gate (5/5 checkboxes); original two-physical-ADB protocol DEFERRED to Issue #13, not claimed as complete.** [Decision and single-phone evidence](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md).\n\n## Phase 14 — Final architecture, security and delivery audit

- [x] Re-check **every** Issue #4 Acceptance Criterion, mapping each to exact file, commit, CI run or device record; gate any missing item as FAIL/OPEN.
- [x] Run architecture direction, app manifest, dependency/version and forbidden scope audits. Confirm one physical `:app`, one Playback owner, one Voice owner, no cloud/streaming/account.
- [x] Record scoped AI-assisted Product/Android architecture/QA/Accessibility/Privacy review evidence and **owner-requested issue-specific accelerated-review exception** without inventing independent human approvals. Link downstream testing to #5–#13. No open technical Blocker/Major under revised foundation criteria; no independent reviewer approval claimed.
- [x] Open focused [PR #71](https://github.com/music-copilot-organization/music-copilot/pull/71) from `feature/4-android-foundation` to `main`, verify all final-head CI successful, apply the documented owner-directed accelerated-review exception (**no human review claimed**), and squash merge; actual merge SHA [`4536cccced3cfbfa0e517bd0d81bd4a5a9060364`](https://github.com/music-copilot-organization/music-copilot/commit/4536cccced3cfbfa0e517bd0d81bd4a5a9060364).
- [x] Verify Issue #4 closed `completed` after merged PR, post-merge `main` CI green and Project V2 status-change event recorded. **Exact Project board Done value is not independently readable and is NOT claimed verified**; downstream #5/#6/#7/#12 remain independent implementation gates, no edits to Issue #5.

**Output:** `docs/implementation/ISSUE_4_FINAL_ACCEPTANCE_AUDIT.md`, final PR/merge and Issue closeout links.  
**PASS:** all acceptance criteria have real evidence, required reviews and CI are green, PR merged; otherwise leave Issue #4 open with precise blockers.

**Phase 14 audit/integration progress — 14 October 2026: PARTIAL, NOT PASS (2/5)**. [Final AC-by-AC audit](ISSUE_4_FINAL_ACCEPTANCE_AUDIT.md) reviews **all 14** original criteria; 13 foundation-scope criteria have evidence and the **two-physical-device criterion is unsatisfied** ([Phase 13 report](ISSUE_4_DEVICE_SMOKE.md)). Architecture/import, Play­back/Voice/Quality ownership, privacy/manifest, scoped lint exceptions, single-:app project, no backend/account/streaming, Gradle catalog and tracked-sensitive-file fixture gates have been inspected. The legacy Issue #16 check originally failed on [PR #71](https://github.com/latticeward-labs/music-copilot/pull/71) because its historical PR scope validator was applied to unrelated Issue #4 files. The guarded scope correction [commit `75c2c02`](https://github.com/latticeward-labs/music-copilot/commit/75c2c027217be19507173b04efd44b5c40a185c2) was verified by **successful [Issue #16 run #38565590492](https://github.com/latticeward-labs/music-copilot/actions/runs/38565590492)** and [run #38566027179](https://github.com/latticeward-labs/music-copilot/actions/runs/38566027179); this does **not** waive or disable historical Issue #16 protection for architecture-owned changes. PR #71 is **DRAFT, not approved/merged**. No human approval/physical install/merge SHA may be fabricated. Only 2/5 Phase 14 checkboxes are completed; Independent cross-role review, final green CI at merge HEAD, merge and Issue closeout remain blocked.

**16 October 2026 Phase 14 readiness:** [Owner-directed review/scope exception](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md) records the explicit user authorization to use one physical screenshot-backed manual smoke for Issue #4 only and to proceed with merge/close, while **disclosing zero independent GitHub reviewers** and honoring deferred field QA #13. All five final-head checks on pre-amendment commit `4f51baf4` succeeded ([Android CI #38809650612](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809650612), [PR CI #38809643136](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809643136), [Architecture/design #38809650690](https://github.com/music-copilot-organization/music-copilot/actions/runs/38809650690)). Merge/closeout checkboxes remain open until actual GitHub outcomes, not assertions.\n\n
**Actual Phase 14 closeout under owner-approved exceptional scope: PASS 5/5 with explicit limitations.** PR merged, issue closed and real `main` CI green. Do **not** claim two physical phones, ADB testing, human independent reviewers, or verified Project V2 `Done` value; review-policy and hardware scope were intentionally relaxed in the formal owner addendum. See [Closeout evidence](ISSUE_4_FINAL_ACCEPTANCE_AUDIT.md#verified-merge-and-issue-4-closeout--16-october-2026).

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


## Verified merge and Issue #4 closeout — 16 October 2026

- **Completed Issue #4:** [#4](https://github.com/music-copilot-organization/music-copilot/issues/4) = `closed / completed` at **2026-10-15 22:09:57 UTC**.
- **Merged implementation PR:** [#71](https://github.com/music-copilot-organization/music-copilot/pull/71) = `closed / merged` at **2026-10-15 22:09:56 UTC**, squash merge commit [`4536cccced3cfbfa0e517bd0d81bd4a5a9060364`](https://github.com/music-copilot-organization/music-copilot/commit/4536cccced3cfbfa0e517bd0d81bd4a5a9060364); verified `main` ref points to that SHA.
- **Pre-merge final branch HEAD:** `5c44c1fb6cb66a0026d378702f6813fa60f3798a`; Android Foundation CI [#38814216577](https://github.com/music-copilot-organization/music-copilot/actions/runs/38814216577), [#38814212030](https://github.com/music-copilot-organization/music-copilot/actions/runs/38814212030), and Architecture/Design [#38814357653](https://github.com/music-copilot-organization/music-copilot/actions/runs/38814357653) all completed **success**.
- **Actual post-merge `main` Android CI:** [#38815204849](https://github.com/music-copilot-organization/music-copilot/actions/runs/38815204849) on merge SHA `4536cccced3cfbfa0e517bd0d81bd4a5a9060364` completed **success**.
- **Owner-amended foundation contract:** 14/14 effective ACs passed, including one **user-reported** physical HONOR X7d Android 15 Bootstrap smoke under [formal exception](ISSUE_4_OWNER_SCOPE_AND_REVIEW_ADDENDUM_2026-10-16.md). Original two-phone ADB/physical matrix was **not performed**, deferred to later OEM/vehicle QA #13.
- **Review governance disclosure:** zero independent human PR approvals submitted; owner authorized one-Issue accelerated review/merge exception, documented in the addendum. This is **not** fabricated human approval.
- **Project board:** the [Issue #4 GitHub events](https://github.com/music-copilot-organization/music-copilot/issues/4) record an automated `project_v2_item_status_changed` event at **2026-10-15 22:10:00 UTC** immediately after closure. The specific Project V2 status *value* (`Done` vs another state) cannot be read using this available connector and is **not independently verified as Done**; only the observed status-change event is claimed.
- **Issue #5 remains unchanged**. This closeout does not ship feature flows, nor claim OEM/device/vehicle production certification.

**Result:** GitHub **PR merge and issue closeout completed** under the explicitly narrower, owner-approved Issue #4 scope; Project board `Done` exact value and independent human review are not claimed.
