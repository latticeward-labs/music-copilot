# Issue #4 — Phase 0: Baseline, Source Lock and Acceptance Traceability

> **Final gate:** **PASS — SPECIFICATION / READINESS ONLY** (12 October 2026).  
> **Implementation status:** `Issue #4 OPEN / IN PROGRESS`; **1/14 original Issue #4 Acceptance Criteria checked (Issue #16 approval), 13/14 remain unimplemented and unchecked**. This audit does not assert an APK, Gradle project, green production Android CI, physical device runs, human signoff, or completed product features.

- **Issue:** [#4 — Create the Android 10+ project foundation and CI](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Execution branch:** [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Phase plan:** [`ISSUE_4_PHASED_EXECUTION_PLAN.md`](ISSUE_4_PHASED_EXECUTION_PLAN.md)
- **Machine-readable audit:** [`ISSUE_4_PHASE0_TRACEABILITY.json`](ISSUE_4_PHASE0_TRACEABILITY.json)
- **Snapshot date:** 12 October 2026
- **Observed frozen `main` HEAD:** [`2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4`](https://github.com/latticeward-labs/music-copilot/commit/2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4)
- **Initial branch planning commit:** [`6ba878f7b9f4a04c28c8d0bc5e848b15cb7f3d2d`](https://github.com/latticeward-labs/music-copilot/commit/6ba878f7b9f4a04c28c8d0bc5e848b15cb7f3d2d). GitHub Compare reported `main...feature/4-android-foundation`: 1 commit ahead, 0 behind, only `docs/implementation/ISSUE_4_PHASED_EXECUTION_PLAN.md` added *at the start of this audit*. Later Phase 0 commits deliberately make the branch further ahead.
- **Scope of audit:** exact source-byte/hash verification, precedence, current GitHub approvals, repository state, architecture/Design handoff, acceptance matrix, scope lock and blocker triage.

## 1. Phase 0 checkpoint result

| Checklist item from phased plan | Evidence obtained | Verdict |
|---|---|---|
| P0-01 — reconcile Issue #4 and all 13 pending ACs with Issue #16, ADRs and current #17 handoff | Authenticated [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4), completed [#16](https://github.com/latticeward-labs/music-copilot/issues/16), completed [#17](https://github.com/latticeward-labs/music-copilot/issues/17), controlling architecture and binding corrections; complete AC-01..AC-14 matrix below | **PASS** |
| P0-02 — record `main` tree and distinguish production from pre-existing spike/workflows | GitHub `contents?ref=main` root and `.github/workflows` listings at frozen `main` HEAD, plus `main...feature` GitHub Compare | **PASS** |
| P0-03 — freeze requirements-to-phase/test/evidence crosswalk and feature prohibition | AC-01..AC-14 matrix below + [JSON register](ISSUE_4_PHASE0_TRACEABILITY.json) + scope/exclusion sections | **PASS** |
| P0-04 — classify open implementation selections vs architecture blockers | Accepted [ADR-0001..0008](../adr/README.md), [Issue #16 decision register](../architecture/DECISION_AND_SPIKE_REGISTER.md), [Phase 10 findings register](../architecture/review/ISSUE_16_FINDINGS_REGISTER.json), open-tooling table below | **PASS; 0 architecture blockers** |

**Overall:** **4/4 Phase 0 requirements met**. This is permission to proceed to Phase 1 and then the production foundation, **not** a claim that later phases passed.

## 2. Official MSM2 byte-level identity and authority

### 2.1 Original upload verification

- Examined uploaded **`MSM2(1)(1).rar`**, not filename-inferred copies.
- Calculated SHA-256 of both uploaded archive aliases; they match: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.
- This equals the exact hash recorded in [Issue #16 Source and Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md), [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), and [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17). File-name suffixes `MSM2(2).rar` / `MSM2(1)(1).rar` are aliases here, **not** differing source identities.
- Directly decompressed RAR entries using `libarchive`; verified **12/12 original English PDFs' actual bytes and SHA-256** against the authoritative GitHub Source and Authority Register, including all six ADRs, PRD, MVP, technical blueprint, system architecture, and two User Flow PDFs. All 12 matched.
- Important reproducibility correction: seven previously available temporary files at `/mnt/data/MSM2_extracted/` had mismatching SHA-256 values (the six ADR PDFs and Code/Technical Blueprint PDF). Their sizes differed from the archive. **They were not used as verified source.** Each original member was re-read directly from the archive and the matching hashes were independently calculated. The full correct member hash inventory is frozen in [`ISSUE_4_PHASE0_TRACEABILITY.json`](ISSUE_4_PHASE0_TRACEABILITY.json) and matches the [Source Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md).
- Most important member identities: English PRD `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf`; English MVP `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0`.

**Audit boundary:** raw PDFs stay as attached source material; the repository stores the identity manifest and derived audit, **not** duplicated PDFs, private media or personal data.

### 2.2 Authority precedence and dated-source reconciliation

| Priority | Source and purpose | Implementation consequence |
|---|---|---|
| Tier 1 | `Music_Copilot_PRD_v1_EN_Clarified.pdf` — product problem, features, exclusions, privacy | Never expand to streaming, account, backend, sync, wake word, iOS/Android Auto or unconstrained conversational assistant |
| Tier 1 | `Music_Copilot_MVP_v1_EN.pdf` — P0/P1/P2, ten-week project timebox, acceptance and Beta | Android 10+, six English push-to-talk intents; P0 Player/Voice/Driving/Stability, P1 restoration/hint, removable P2 Likes |
| Accepted architecture | [ADR Register: ADR-0001..0008](../adr/README.md) plus [final architecture](../SOFTWARE_ARCHITECTURE.md) | Runtime, ownership, persistence, DI lifetime, one physical module and change-control contracts are binding |
| Approved derived behavior | [User Flows](../USER_FLOWS.md), [Wireframes](../WIREFRAMES.md), Golden Scenarios, [Issue #17 handoff](../handoff/README.md) | Behavior/navigation/state/visual tokens only; no overriding Tier 1 or Accepted ADR |
| Evidence | technical spikes, validation runs, reviews | Support/test claims only when actually run |
| Historical proposals | System Architecture PDF and Code/Technical Blueprint PDF, dated August 2026; historical ADR-PDF proposed headers | Engineering input only; they explicitly defer approval to Issue #16; any conflicting earlier proposed module graph/code is not binding |
| Workflow status | Current authenticated GitHub Issues/PRs and current repository commit | Overrides stale `READY`/historical review-date prose in older snapshots; never fabricates unperformed tests |

**No source conflict requiring redesign was found.** Some documents predate #17 completion or retain older proposed language; this is *document chronology*, not permission to revert the final accepted architecture. Current #17 design signoff is **AI-assisted artifact-level acceptance** with 0 independent human signatures; owner-approved human review follows MVP completion. Runtime accessibility, phone/OEM/vehicle and remote-STT Legal gates remain outstanding downstream.

## 3. GitHub dependency and release-gate evidence

1. **Architecture gate passed.** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16) is `closed` (30 August 2026). Approved [PR #46](https://github.com/latticeward-labs/music-copilot/pull/46) merged at [`0d3b8890ee37bd19ccbd2a7b0658f5b5d5745f2b`](https://github.com/latticeward-labs/music-copilot/commit/0d3b8890ee37bd19ccbd2a7b0658f5b5d5745f2b). Its acceptance reconciled **14/14 criteria**, with 0 open Blockers and 0 open Majors. The Architecture baseline remains on `main`.
2. **Design delivery gate passed for handoff.** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17) is `closed` (12 October 2026); [PR #68](https://github.com/latticeward-labs/music-copilot/pull/68) merged at `567df7ff2cc4ffd1e202a2214b19df98574dca83`. [Acceptance addendum](../ISSUE_17_AI_ACCEPTANCE_ADDENDUM_2026-10-12.md) documents **14/14 AI-assisted design-level criteria** and owner-authorized **human review after MVP**, not approval of runtime code.
3. **Foundation not yet implemented.** [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4) is `open` and its body says `IN PROGRESS — phase plan created; implementation pending`. **Only first AC checked**; other 13 remain unchecked. The historical issue text saying #17 can run in parallel is already satisfied by its closure; do not silently restore an obsolete Figma dependency.
4. **Three Issue #16 downstream Minor findings remain non-blocking for starting #4**, with their own required gates: [PH10-MIN-001](../architecture/review/ISSUE_16_FINDINGS_REGISTER.json) Compose runtime TalkBack, 48dp and 200% scaling (feature/Accessibility QA); PH10-MIN-002 provider-specific Cloud STT consent/retention/legal (before remote STT activation); PH10-MIN-003 Pixel/Samsung/Xiaomi and 3+ real vehicle/head-unit evidence (QA/Field-Beta).
5. **Issue #17 PH11-MIN-004** is deferred owner-directed *human* multi-role review after MVP; it does not block using approved token specs and does not count as a signed human approval.

## 4. Frozen repository snapshot at Phase 0 start

**GitHub repository:** private `latticeward-labs/music-copilot`. `main` HEAD at snapshot: [`2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4`](https://github.com/latticeward-labs/music-copilot/commit/2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4).

```text
music-copilot/                # main (frozen snapshot)
├── .editorconfig
├── .github/
│   └── workflows/
│       ├── cluster-b-android-spikes.yml       # isolated historical spike CI
│       └── issue-16-architecture-validation.yml # architecture-document CI
├── .gitignore
├── CONTRIBUTING.md
├── README.md
├── SECURITY.md
├── docs/                       # approved product, flows, design, architecture
├── scripts/                    # existing repository validations
└── spikes/
    └── android-validation/      # separate *non-production* Android spike project
```

**Direct root listing showed no production `app/`, `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml`, or root `gradlew`.** This finding is about the root at this frozen snapshot; it does not claim the spike harness lacks its own Gradle app. Therefore Phase 2 must bootstrap the *new root production Android project* without moving/importing the spike harness as a production module.

**Protected historical assets:** existing `spikes/android-validation/`, its workflow, Issue #16 architecture validator, approved documentation, `.gitignore` security rules, and Issue #17 repository-native source assets. Do not overwrite or drop them in the Android bootstrap.

**Branch comparison:** before this Phase 0 audit's own output commits, the feature branch was ahead by exactly the initial phased-plan commit, behind by 0, and contained one added file. `develop` exists but is stale relative to this snapshot; use the already chosen `main`-based execution branch, then submit a reviewed PR to the agreed integration target. No force-push/rebase is required in Phase 0.

## 5. Frozen binding architecture and implementation boundary

| Boundary | Source | Phase #4 implementation rule |
|---|---|---|
| Platform | ADR-0001, PRD/MVP | Native Android API 29+ / Kotlin / Jetpack Compose; do not create iOS or KMP |
| Gradle strategy | ADR-0008, [implementation contracts §§9–12](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) | Exactly **one physical production `:app` module**; logical `app/core/feature/playback/voice/data/quality` packages and checked import rules; measured extraction criterion required for any new module |
| Playback | ADR-0002/0007, [runtime policy](../architecture/RUNTIME_POLICIES.md) | Same-process `MediaSessionService` owns single Player/MediaSession and live queue/position; application-scoped controller, no UI-owned Player; serialized 32-capacity commands with typed Busy, complete-snapshot reconnect |
| Voice | ADR-0004/0005, [public contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) | Replaceable STT/TTS ports, sole `VoiceSessionCoordinator`, exactly six intents, 6000ms listening and one Retry, transient confirmation generation; Voice invokes `PlaybackGateway` only |
| Mandatory Phase 10 corrections | [Reviewed corrections](../architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md) | `VoiceSessionState.Confirming.options` = immutable **1–2** labeled options; `PlaybackSnapshot.lastError: AppError?` limited to Playback, MediaItem, Interruption, no alternate error bus |
| Local data | ADR-0003/0007 | MediaStore = media existence authority; Room = rebuildable projection; DataStore = preferences/consent; Proto DataStore P1 snapshot *non-authoritative*, schema-validated, Safe-Paused only, **never autoplay after cold restore** |
| DI/Concurrency | public contracts and runtime policies | `app/di` owns cross-boundary composition, Hilt replaceable; service owns Player/processor jobs, application owns repositories, ViewModel owns UI, injected clocks/dispatchers, no `GlobalScope` |
| Privacy and Quality | ADR-0006, [analytics/privacy](../ANALYTICS_AND_PRIVACY.md), runtime policy | Typed allowlisted telemetry -> privacy gate -> fail-safe NoOp/approved sink. No raw audio, transcript, title, path, MediaStore ID, content URI, user media queue, exact position or secrets. Remote cloud STT off until legal/consent approval |
| Design interface | [`COMPOSE_TOKEN_MAPPING.md`](../handoff/COMPOSE_TOKEN_MAPPING.md), [`TOKENS.json`](../design-system/foundations/TOKENS.json) | Approved fixed dark Night Signal token names/values can be mapped in a later Compose shell. No arbitrary palette changes or invented feature UI; Voice is overlay state rather than standalone destination |
| Test architecture | [Issue #16 validation](../architecture/ISSUE_16_VALIDATION.md) | Start with leaf unit seams (UT-01..UT-07), Data/Service integration seams (IT-01..IT-07), lifecycle/recovery seams (ST-01..ST-08). Evidence only for tests actually implemented and run |

**Important delegation boundary:** Issue #4 establishes build/ownership/contract/test seams. Full permissions/onboarding #5, search/index #6, usable playback #7, audio focus/route #8, Driving #9, Voice actions #10/#11, production metrics #12 and complete field/OEM/vehicle validation #13 remain their explicitly owned implementation tasks. Basic service and adapter code/test seams in #4 must **not** be misrepresented as completed feature work.

## 6. Exact Issue #4 Acceptance Criteria → phase, test, evidence

**Issue acceptance inventory:** 14 total; AC-01 is already checked in Issue #4 based on completed #16. AC-02..AC-14 remain unchecked. Every row has a future objective test and evidence destination in the accompanying machine-readable register. Planned destinations are not current artifacts.

| ID | Current state in Issue #4 | Phase(s) | Minimum verification | Evidence location when implemented |
|---|---|---|---|---|
| AC-01 Architecture baseline approved | **DONE upstream** | 0 | #16 closed, PR #46 merged, ADR register | [Software Architecture](../SOFTWARE_ARCHITECTURE.md), [#16 final audit](../architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md) |
| AC-02 Kotlin/Compose Android 10+ | OPEN | 2,10,13 | `minSdk 29` build + API-29 app smoke | `app/build.gradle.kts`, device smoke |
| AC-03 version catalog/no speculative frameworks | OPEN | 2,12,14 | dependency and compatible version review + CI | `gradle/libs.versions.toml`, build matrix |
| AC-04 package/module structure | OPEN | 3,14 | settings includes only production `:app`; boundary audit | `settings.gradle.kts`, dependency rules |
| AC-05 dependency rules enforced | OPEN | 3,11,12 | negative/positive forbidden-import/static tests | architecture scripts and unit evidence |
| AC-06 Player UI independence | OPEN | 4,5,7,11 | service owner, UI recreation and controller reconnect test seam | `playback/`, test evidence |
| AC-07 replaceable Voice/Quality services | OPEN | 4,8,9,11 | fake STT/TTS and NoOp/fault isolation contract tests | `voice/api`, `quality/api` |
| AC-08 debug/release separation | OPEN | 2,9,12 | inspect and execute variant tasks | `app/build.gradle.kts`, build matrix |
| AC-09 CI format/lint/tests/debug build | OPEN | 11,12 | real green workflow and failing negative fixture | `.github/workflows/android-foundation.yml` |
| AC-10 privacy-safe non-production crash logging | OPEN | 9,11,12 | NoOp/default guard and prohibited-payload tests | `quality/`, test evidence |
| AC-11 two device launches | OPEN | 13 | two **recorded actual** runs, model/API/date/build commit | `ISSUE_4_DEVICE_SMOKE.md` |
| AC-12 clean checkout/no credentials | OPEN | 2,12,13 | fresh clean checkout `assembleDebug`, no secrets | CI run + smoke evidence |
| AC-13 prohibited secrets/audio/media not committed | OPEN | 2,6,9,12,14 | tracked-file diff, secret/media-fixture audit | `.gitignore`, final audit |
| AC-14 architecture conformance linked to #16/ADRs | OPEN | 0,3,14 | this baseline now establishes references; **code-level** conformance later | this file + final acceptance audit |

**Boundary note:** this Phase 0 document is preliminary evidence toward AC-14, but AC-14 must remain unchecked until actual code dependencies, contracts, permissions and runtime are reviewed. Same for every other OPEN criterion.

## 7. Forbidden additions and work ownership lock

**Forbidden in Issue #4:**

- Accounts/login/auth, product backend, online streaming/catalog, downloads, cloud sync, music licensing/DRM features, Cast, Android Auto/CarPlay, iOS/KMP/Flutter/React Native, remote-control servers, voice wake word or unrestricted conversational AI.
- Second playback Player/MediaSession/queue/error-bus authority; UI/ViewModel-owned playback; extra service process; second Voice session owner; free-form analytics payload; raw-audio/transcript capture in committed test fixtures.
- Premature seven-module Gradle decomposition, invented navigation and high-fidelity feature screens, independent design choices replacing final Issue #17 tokens.
- Declaring completion of #5..#13 or using the historical `spikes/android-validation` APK to satisfy Issue #4 app-launch acceptance.

**Allowed within foundation:** compile-safe public ports, minimal platform implementations/fakes, tests enforcing ownership, first-run neutral Compose shell, app-scoped DI wiring, isolated non-production NoOp diagnostics, version-managed dependency declarations and CI pipeline, documentation needed for downstream work. Changes beyond this scope return to owning Issue or ADR/product review.

## 8. Open decision triage (replaceable tooling vs blocker)

| Decision not yet fixed by Phase 0 | Owner/phase | Type | Risk control |
|---|---|---|---|
| Exact released compatible AGP, Gradle, Kotlin, Compose compiler/runtime, JDK and Android SDK version set | Android foundation / Phase 2 | **Replaceable tool/version selection; not architecture blocker** | Verify compatibility and published artifacts before pinning; lock catalog/wrapper, test clean build; never alter `minSdk 29` contract |
| DI library implementation (Hilt or equivalent) | Android foundation / Phase 5 | **Replaceable implementation selection** | Keep all production cross-boundary binding authority in `app/di`; no hidden service locator; Player always service-owned |
| Formatter, lint/static-analysis and package import enforcement implementation | Android/Quality / Phase 11–12 | **Replaceable enforcement tools** | Fail CI on forbidden dependency/format changes; prefer real checks, avoid fake pass markers |
| Optional crash-reporting configuration | Privacy/Quality / Phase 9–12 | **Replaceable non-production mechanism** | NoOp/default off with privacy gate; do not add production collector or require credentials |
| Future split into physical Gradle modules | New PR+ADR-0008 extraction rule | **Not an open choice for this Issue** | Default single `:app`; reject module increase absent documented measurement/criterion |
| Remote/cloud STT provider | Product + Privacy/Legal, later gate | **Not authorized for activation** | Keep off until separate consent/retention/destination/provider signoff |
| Physical OEM/three-vehicle field results | QA/Field-Beta, Issue #13 | **Required downstream evidence; not an unapproved architecture decision** | Leave untested until real runs; never fabricate |
| Human design approval | Post-MVP per Issue #17 owner addendum | **Deferred approval, not a Phase 0 blocker** | Preserve auditable AI-only acceptance status and final runtime/human review gate |

**Blocker triage:** 0 unresolved P0 product-scope decisions, 0 unresolved accepted-ADR ownership conflicts, 0 unresolved missing design artifacts that prevent starting foundation. The four main tool choices above are intentionally future selections, with binding constraints already fixed. Test/implementation outcomes are **not** predetermined to pass.

## 9. Validation method, guardrails and final Phase 0 sign-off

**Direct verification performed:**

1. Local `sha256sum` of uploaded RAR; direct member extraction and SHA-256 hashing of **all 12** original PDFs, checked against actual Source and Authority Register; incorrect pre-extracted working copies rejected.
2. Authenticated GitHub fetch of Issues #4/#16/#17 including states, timestamps, current acceptance checklist and closeout.
3. Authenticated GitHub fetch of `main` commit, root contents, workflows, architecture/ADR register, contracts, reviewed correction addendum, Runtime/Validation policies, current Design System/Compose handoff and Issue #16 findings.
4. GitHub comparison of current `main` with foundation feature branch *before* adding Phase 0 audit files; no code modification accidentally inherited from the spike harness.
5. Reconciliation of 14 AC rows, 4 Phase 0 checks, upstream gates, Scope prohibitions and owner/deferred gates into the [machine-readable register](ISSUE_4_PHASE0_TRACEABILITY.json). Read-back/validation of committed files must complete before recording PASS as finalized.

**What has NOT been validated:** Gradle dependency compatibility, root Android build, real instrumentation or physical devices, real Player runtime, Voice recognition, operational production CI, human approvals, cloud STT legal contracts, beta KPIs. These are later gated work, not Phase 0 failings.

**Repository-link integrity:** after the initial Phase 0 files were committed, all **19/19 distinct relative Markdown destinations** in this report were individually re-fetched from the correct GitHub `main` or `feature/4-android-foundation` ref with **0 missing files**. Their observed Git blob hashes, the exact check scope and non-claims are recorded in the separate machine-readable `docs/implementation/ISSUE_4_PHASE0_VALIDATION.json`. External GitHub/issues URLs were sourced from authenticated results but were not separately HTTP-probed as part of this relative-path audit.

**Final disposition: `PHASE 0 PASS — SOURCE-ALIGNED / ARCHITECTURE-READY / NO IMPLEMENTATION CLAIM`.** Proceed to Phase 1. Keep Issue #4 `open / In Progress` and leave the thirteen uncompleted Issue #4 Acceptance Criteria unchecked. No PR merge or final issue closure is authorized by Phase 0 alone.
