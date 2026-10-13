# Issue #4 — GitHub execution and evidence index

- **Established:** 13 October 2026; status **ACTIVE — Phases 0–4 PASS; Phases 5–14 NOT STARTED**.
- **Tracked issue:** [#4 — Android Foundation & CI](https://github.com/latticeward-labs/music-copilot/issues/4), kept **open / In Progress**.
- **Execution branch:** [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation).
- **Frozen branch ancestor:** [`main@2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4`](https://github.com/latticeward-labs/music-copilot/commit/2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4); real-time GitHub compare should be rechecked before final PR.
- **Uploaded MSM2 source:** original archive SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; all 12 original English PDF members verified in Phase 0.
- **Controlling sources:** [English product/architecture authority register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md), [Accepted ADR-0001..0008](../adr/README.md), [final Architecture](../SOFTWARE_ARCHITECTURE.md), [Binding Phase 10 corrections](../architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md), [Issue #17 handoff](../handoff/README.md).
- **Acceptance matrix:** [`ISSUE_4_PHASE0_TRACEABILITY.json`](ISSUE_4_PHASE0_TRACEABILITY.json). **14/14 AC mapped**, **1 already checked for prior #16 approval**, **13 original implementation ACs still open**.
- **Execution checklist:** [`ISSUE_4_PHASED_EXECUTION_PLAN.md`](ISSUE_4_PHASED_EXECUTION_PLAN.md).
- **Scoped PR template:** [`issue-4-android-foundation.md`](../../.github/PULL_REQUEST_TEMPLATE/issue-4-android-foundation.md) (future final PR only). Existing global template stays untouched.
- **Evidence ethics:** A document, simulated test, extracted PDF or old spike APK is not evidence of a production Gradle build, device run, green Actions workflow, independent human review or merged PR.

## 1. Evidence register (update after every execution phase)

| Phase | Status | Required evidence and exact target | Current recorded evidence | Next action |
|---:|---|---|---|---|
| 0 — source/AC lock | **PASS** | archive/12 PDFs, authorities, 14 AC mapping, baseline GitHub compare | [Phase 0 report](ISSUE_4_BASELINE_AND_TRACEABILITY.md), [JSON trace](ISSUE_4_PHASE0_TRACEABILITY.json), [validation](ISSUE_4_PHASE0_VALIDATION.json) | Keep sources frozen |
| 1 — GitHub execution | **PASS — setup validated** | branch provenance, Issue status, commit plan, scoped PR checklist, spike exclusion, verified file links | This evidence index; [Phase 1 audit](ISSUE_4_PHASE1_GITHUB_EXECUTION.md); [scoped PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-4-android-foundation.md) | [Phase 1 Validation JSON](ISSUE_4_PHASE1_VALIDATION.json): 4/4 requirements, 16/16 file links verified, no modified authority/spike files |
| 2 — Android/Gradle | **PASS — actual GitHub Actions root Debug + unsigned Release build** | root Gradle wrapper, version catalog, `:app`, `minSdk=29`, real clean Gradle projects and APK builds | [Build Matrix](BUILD_MATRIX.md), [Phase 2 Validation](ISSUE_4_PHASE2_VALIDATION.json), [GHA run 38421023043](https://github.com/latticeward-labs/music-copilot/actions/runs/38421023043), [final green run 38421736045](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045) and [APK artifact 11825939906](https://github.com/latticeward-labs/music-copilot/actions/runs/38421736045/artifacts/11825939906) | Keep unimplemented architectural seams delegated to Phases 3–14 |
| 3 — package architecture | **PASS — approved package tree + CI-checked import direction** | 52 logical package paths, core leaves, four API seams, app/di owner; 6 allowed/17 rejected fixtures; real Kotlin build | [Dependency Rules](DEPENDENCY_RULES.md), [Phase 3 Validation](ISSUE_4_PHASE3_VALIDATION.json), [green GHA run 38424771876](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876), [APK artifact 11828810490](https://github.com/latticeward-labs/music-copilot/actions/runs/38424771876/artifacts/11828810490) | Phase 4 real typed contracts and their unit tests |
| 4 — public contracts | **PASS — typed APIs, JVM port fakes/tests and real CI build** | Core + Playback/Voice/Data/Quality contracts, two Phase 10 corrections, SDK-free public packages, 24/24 dependency fixtures | [Phase 4 contract review](ISSUE_4_PHASE4_PUBLIC_CONTRACTS.md), [validation JSON](ISSUE_4_PHASE4_VALIDATION.json), [green GHA run 38427936983](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983), [APK/test artifact 11829238672](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/artifacts/11829238672) | Phase 5 DI/scopes; runtime features remain deferred |
| 5 — DI and scopes | NOT STARTED | `app/di`, application/service/ViewModel lifetime tests | **none** | Implement/test |
| 6 — data seams | NOT STARTED | MediaStore/Room/Proto DataStore separation and fixture tests | **none** | Implement/test |
| 7 — Media3 foundation | NOT STARTED | real service/controller and command serialization/reconnect tests | **none** | Implement/test |
| 8 — Voice seam | NOT STARTED | fake/provider interface and deterministic session/cancellation tests | **none** | Implement/test |
| 9 — privacy/Quality | NOT STARTED | typed gate/NoOp privacy tests, debug/release separation | **none** | Implement/test |
| 10 — Compose shell | NOT STARTED | executable Compose startup shell/token mapping tests | **none** | Implement/test |
| 11 — test/static enforcement | NOT STARTED | real `:app:testDebugUnitTest`, lint, formatter, negative dependency checks | **none** | Run and record |
| 12 — production CI | NOT STARTED | GitHub Actions workflow run URL/HEAD + APK/test reports | **none** | Run and record |
| 13 — clean checkout/devices | NOT STARTED | fresh root build; two *actual* model/API/commit installation+launch records | **none** | Run and record |
| 14 — final acceptance | NOT STARTED | every original AC evidence linked; reviews; PR final HEAD green and merged SHA | **none** | Audit/merge only after pass |

**Phase 1 verification:** [`ISSUE_4_PHASE1_VALIDATION.json`](ISSUE_4_PHASE1_VALIDATION.json) confirms 4/4 GitHub-setup items PASS, source/branch checked, 16/16 audited relative paths exist and 0 approved architecture/spike files changed. This is documentation integrity, not Android CI.\n\n**Phase 2 build proof:** GitHub runner checked out commit `04044a1c2338453a72f822c5ba0ac8fe98d881b6` and successfully executed `./gradlew projects :app:assembleDebug :app:assembleRelease --no-daemon --stacktrace` without private credentials. Root projects reported only `:app`; Debug and unsigned Release builds uploaded to real GHA artifact. No phone installation, architecture-interface implementation, formatter enforcement, full lint/unit/static CI or production signing is claimed by this result. The two earlier SDK/grep CI failures and fixes remain in the Build Matrix.\n\n**Phase 3 enforcement result:** `python3 scripts/check_architecture_dependencies.py --root . --self-test` executed on GitHub runner, **23/23 intended fixtures** (6 allowed, 17 deliberately rejected), **52/52 expected package directories**, 54 production source files, one `:app` and no observed illegal imports/FQNs. The same workflow then built Kotlin Debug and unsigned Release successfully at commit `813818c12af5a2d110b314ddcb534db30db452ae`. Validation covers code/package ownership *direction*, not yet semantic DI/runtime ownership.\n\n### Evidence identity conventions

Use unique IDs and cite **real GitHub URLs**, SHA and environment:

- `I4-P<NN>-SRC-<NN>` — approved GitHub/source input. Include file, blob SHA or commit.
- `I4-P<NN>-CODE-<NN>` — actual created source/build files; cite commit.
- `I4-P<NN>-TEST-<NN>` — test command, exit code, report path, environment and verified run URL.
- `I4-P<NN>-CI-<NN>` — GitHub Actions workflow/run/job, conclusion and tested SHA.
- `I4-P<NN>-DEV-<NN>` — device model/OEM/API/OS, test build SHA, date, action and observed result.
- `I4-P<NN>-REV-<NN>` — exact reviewer identity/type (AI vs independent human), date, issues and decisions.
- `I4-P<NN>-AUD-<NN>` — audit/consistency/link check with performed method and real results.

Never convert a planned slot into PASS on the strength of placeholder text. Keep actual device findings and product/quality acceptance owned by their downstream Issues.

## 2. Commit and review execution contract

One branch persists for Issues #4 Phase 0–14. Small reviewable commits (do not squash history until final **reviewed** PR); phase gates receive at least one linked evidence commit. Suggested semantic scopes comply with [`CONTRIBUTING.md`](../../CONTRIBUTING.md) and do not imply code/features not built.

| Phases | Commit message pattern | Required verification before recording PASS |
|---|---|---|
| 0–1 | `docs(issue-4): ...` | source/branch/acceptance links validated, planning-only |
| 2 | `build(android): bootstrap Kotlin Compose root app` | published toolchain, clean debug build |
| 3 | `test(architecture): enforce package import boundaries` | import rule negative fixture fails as intended |
| 4 | `feat(foundation): add approved public contracts` | typed-port unit tests and Phase 10 correction tests |
| 5 | `feat(foundation): wire application composition root` | DI graph/lifetime test |
| 6 | `feat(data): add local-first foundation adapters` | isolated fixture/ownership tests |
| 7 | `feat(playback): add MediaSessionService controller foundation` | service/controller integration and command ordering |
| 8 | `feat(voice): provide replaceable local provider seam` | fake-provider cancellation/stale-generation assertions |
| 9 | `feat(quality): establish fail-safe privacy gate` | prohibited attributes/NoOp fault isolation |
| 10 | `feat(ui): implement minimal Compose shell from approved tokens` | launch/insets/lifecycle smoke |
| 11 | `test(android): verify foundation contracts` | actual unit/lint/static results |
| 12 | `ci(android): validate root application build and upload artifacts` | real Actions green at recorded SHA |
| 13 | `test(android): record clean checkout and device smoke` | two device evidence records (no fabricated results) |
| 14 | `docs(issue-4): audit acceptance and final delivery` | 14 ACs reconciled, reviewed PR and merge evidence |

**Before each phase:** inspect branch HEAD; compare with `main`; re-read architecture/handoff if `main` changed; commit only changed files within Issue #4 scope; store actual SHA, verifier output and known blockers. **After each phase:** update this index and the phase plan checklist only after passing the declared gate. Never overwrite Accepted source or test evidence to force a green checkbox.

## 3. GitHub PR / review / merge route

`CONTRIBUTING.md` and `README.md` generally name `develop` as the integration branch and `main` as reviewed stable. However, `develop` was observed at `a8944b287498aff557af3d7e17179b599c5c5ae5` (July 26), while current `main` is October 12 at `2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4`. Issue #16's actual [PR #46](https://github.com/latticeward-labs/music-copilot/pull/46) and Issue #17's actual [PR #68](https://github.com/latticeward-labs/music-copilot/pull/68) **merged into `main`**; Phase 0 explicitly selected `main` as Issue #4 branch base.

**Documented project-specific delivery choice:** keep this branch on `main` and propose the final foundation PR **to `main`**, subject to recorded reviewer acceptance of this divergence from generic `develop` guidance before Phase 14 merge. This is an explicit delivery-process exception, not an amendment to ADR, product scope or architecture. If reviewers require `develop`, update/synchronize it safely with current main and re-run all relevant validations first; do not push unreviewed code into a stale integration branch.

- **Do not create the final PR now;** it is a Phase 14 action when code, CI, devices and AC audit exist. PR template is prepared, not submitted.
- Before proposing merge: base/HEAD lineage refreshed, exact commits listed, target reconciled, all required checks real green, independent/AI review distinctions explicitly recorded, Blocker/Major findings resolved.
- No solo self-approval as independent review: observe actual `CONTRIBUTING.md`; if human review is unavailable, record the blocker/owner rather than inventing an approval.
- Squash merge preferred if permitted; final outcome can move to Testing before Done per contribution policy. **Issue close and GitHub Project Done are separate operations and require evidence**; an Issue-body label alone is not Project Status mutation.
- No automatic issue closure keyword until all criteria pass. No phase-1 PR merge or final Issue closure.

## 4. Isolation, security and non-claim register

**Protected source/evidence (do not overwrite/re-purpose):**

- `spikes/android-validation/`: separate independently configured Gradle project named `music-copilot-android-validation`, with namespace and applicationId `com.latticeward.musiccopilot.spike` and its own `:app` inside the spike root. It is **not** the future repository-root production `:app`.
- `.github/workflows/cluster-b-android-spikes.yml`: runs inside `spikes/android-validation`, not evidence for the new root app.
- `.github/workflows/issue-16-architecture-validation.yml`: checks architecture/docs and does not build new root production app.
- `docs/SOFTWARE_ARCHITECTURE.md`, `docs/adr/`, `docs/handoff/`, `docs/design-system/` and English source identity evidence are read-only authorities for this phase.
- Existing `.github/PULL_REQUEST_TEMPLATE.md`, `.editorconfig` and `.gitignore` remain intact.

**Production setup later:** new root `settings.gradle.kts`, `gradle/libs.versions.toml`, `:app` under `app/` with namespace under `com.latticeward.musiccopilot`; root settings must never `includeBuild("spikes/android-validation")` nor point a production module at `spikes/android-validation/app`. Verify this as an automated architecture check later.

**Sensitive-content rule:** no signing material, API keys, user-device media metadata, transcripts/raw voice, unauthorized commercial audio, provider credentials or repository logs containing such payload. `NoOp` telemetry until privacy gate. No unnecessary provider SDK, server, account or streaming.

**Open factual non-claims at Phase 1:** no root Gradle build, new APK, code tests, Android CI, real device, physical OEM/vehicle evidence, project-board field status mutation, independent human design/engineering review, approved cloud speech processor, or production-ready MVP has been established by planning files.


**Phase 4 execution proof — 13 October 2026:** 14 newly committed typed production Kotlin files plus 3 JUnit test suites and updated CI. The finished GitHub Actions run [38427936983](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983) used code SHA `995050eb6ee02bdb7cfcb823a062b6fcc67fd775` and reported `SELFTEST PASS: 24 fixtures`, `ARCHITECTURE PASS: one :app module; 52 package paths; 68 source files`, `:app:testDebugUnitTest`, and `BUILD SUCCESSFUL in 6m 6s`; [artifact 11829238672](https://github.com/latticeward-labs/music-copilot/actions/runs/38427936983/artifacts/11829238672) includes APKs and test reports. [Contract evidence](ISSUE_4_PHASE4_PUBLIC_CONTRACTS.md) and [machine validation](ISSUE_4_PHASE4_VALIDATION.json). An initial contract-only run [38427496212](https://github.com/latticeward-labs/music-copilot/actions/runs/38427496212) failed before the required narrow `core.result -> core.model` checker correction. Intermediate run [38427770367](https://github.com/latticeward-labs/music-copilot/actions/runs/38427770367) was cancelled by the final corrective push. These are not counted as successful validation. All remaining original Issue #4 acceptance criteria stay pending until their own feature/runtime/CI/device gates.
