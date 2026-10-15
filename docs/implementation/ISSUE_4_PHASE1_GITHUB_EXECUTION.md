# Issue #4 — Phase 1: GitHub Execution Setup & Isolation Audit

**Phase decision: PASS (GitHub planning/setup gate only)** — 13 October 2026.  
**Execution issue:** [#4](https://github.com/latticeward-labs/music-copilot/issues/4) — **OPEN / IN PROGRESS**; only 1/14 original Acceptance Criteria checked (upstream Issue #16 approval), other 13 intentionally unchecked.  
**Branch:** [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation).  
**Companion records:** [Evidence Index](ISSUE_4_EVIDENCE_INDEX.md), [Scoped PR Template](../../.github/PULL_REQUEST_TEMPLATE/issue-4-android-foundation.md), [Phased Execution Plan](ISSUE_4_PHASED_EXECUTION_PLAN.md), [Phase 0 Source/Authority Baseline](ISSUE_4_BASELINE_AND_TRACEABILITY.md), [Phase 1 Validation](ISSUE_4_PHASE1_VALIDATION.json).

## 1. Checklist and required PASS evidence

| Phase 1 task | Verified evidence | Result |
|---|---|---|
| P1-01 — use `feature/4-android-foundation` branched from current `main`; no direct writes to `main` | Authenticated GitHub `main...feature/4-android-foundation` compare at phase entry: **ahead 6, behind 0**, merge-base `2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4` equal to `main` HEAD; changes all limited to 4 previous `docs/implementation/ISSUE_4_*` Phase 0 artifacts. Phase 1 changes are being committed to the same feature branch. | **PASS** |
| P1-02 — Issue #4 status text In Progress; no implementation AC marked complete | Authenticated [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4) body `IN PROGRESS — Phase 0 PASS (source/traceability); implementation pending`; Issue state remains `open`; exactly **1 checked / 13 unchecked** in original Acceptance Criteria block. | **PASS** |
| P1-03 — commit plan, scoped PR checklist, evidence index | [Evidence Index](ISSUE_4_EVIDENCE_INDEX.md) maps Phases 0–14 to future evidence, provides real SHA/run/device/review naming and commit scopes. Scoped Issue #4 [PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-4-android-foundation.md) includes 14 ACs, validation, privacy, reviewer/merge/rollback and explicit non-claims. Existing default template remains intact. | **PASS** |
| P1-04 — confirm no pre-existing production Android module accidentally overridden by spike | GitHub root `main` tree: `.editorconfig`, `.github`, `.gitignore`, `CONTRIBUTING.md`, `README.md`, `SECURITY.md`, `docs`, `scripts`, `spikes`. **No root `app/`, root Gradle wrapper, root `settings.gradle.kts`, root build files.** `spikes/android-validation/settings.gradle.kts` is a separate project `music-copilot-android-validation` with nested `:app`, and `spikes/android-validation/app/build.gradle.kts` uses the independent `.spike` applicationId. Only Phase 0/1 docs/template are changed on branch. | **PASS** |

**Overall:** 4/4 Phase 1 checklist items passed at *repository workflow/setup* scope. The required `main` ancestry and no-unreviewed-product/architecture changes are verified by authenticated GitHub compare; exact final refs/diff and read-back results are serialized into [Phase 1 Validation JSON](ISSUE_4_PHASE1_VALIDATION.json). No production implementation claim is made.

## 2. Rechecked attached source identity and governance

The user reattached `MSM2(1)(1).rar` for Phase 1. The archive bytes were opened directly at the supplied sandbox path and SHA-256 calculated again:

`0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`

This is identical to the [Issue #16 official Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md) and Phase 0 verification (12/12 original English PDFs). No new source variant was adopted and no PDF was committed to GitHub. The English PRD/MVP scope, accepted ADR-0001..0008, [final architecture](../SOFTWARE_ARCHITECTURE.md), and Issue #17 repository-native Compose handoff remain the authorities.

**Immutable architecture for foundation:** native Android `minSdk=29` Kotlin/Compose; one production `:app` Gradle module, logical `app/core/feature/playback/voice/data/quality`; service-owned same-process Playback and controller snapshots; replaceable Voice providers; MediaStore/Room/DataStore authority; typed errors and Quality privacy gate. Phase 1 changes do not add code or modify these approved decisions.

## 3. GitHub initial state and target branch selection

| Object | Authenticated observed state |
|---|---|
| Repository | `latticeward-labs/music-copilot` — private; default `main`; read/push/admin permission available |
| `main` at initial validation | `2c08bb2651ebb82cbb892e1b7648405cdd6d3ab4` (12 October 2026), clean unaffected by Phase 1 |
| Feature branch | `feature/4-android-foundation`, created from `main`, starting comparison ahead 6 / behind 0 |
| `develop` branch | `a8944b287498aff557af3d7e17179b599c5c5ae5` (26 July 2026), stale relative to current `main` |
| Existing historical workflows | `.github/workflows/cluster-b-android-spikes.yml` and `.github/workflows/issue-16-architecture-validation.yml`; neither validates a future new root Android `:app` |
| Existing PR policy | `.github/PULL_REQUEST_TEMPLATE.md` global template; `CONTRIBUTING.md` asks for reviewed change, preferred squash, `develop` integration; no self-approval as only review |
| Confirmed precedent | [#16 PR #46](https://github.com/latticeward-labs/music-copilot/pull/46) and [#17 PR #68](https://github.com/latticeward-labs/music-copilot/pull/68) actually merged to `main` |
| Current Foundation PR | **None**; preparation template only. Final PR belongs to Phase 14, not Phase 1. |
| GitHub Project board status | **Not independently mutated/verified** by this execution. Text status in Issue is not evidence that Project V2 field equals In Progress. |
| Security | Existing `.gitignore` excludes local Gradle/build outputs, `.jks`/`.keystore`, `google-services.json`, `service-account*.json`, `.env`, `local.properties`; do not weaken it. |

**Review/merge target conflict handled explicitly:** Repository generic `CONTRIBUTING.md` recommends the `develop` integration branch. Given that `develop` is months behind and accepted Issues #16/#17 actually merged to `main`, the Issue #4 phase plan is based on `main` and the **proposed final PR base is `main` as a documented project-specific exception**. It is **NOT silently approved**: final reviewer must explicitly accept the exception before Phase 14 merge. If reviewers require `develop`, first synchronize it safely and rerun validations/approval. We intentionally did **not** move `develop`, change branch protection or prematurely create PR.

## 4. Scoped implementation commit policy

Commits are imperative/scoped as required by [`CONTRIBUTING.md`](../../CONTRIBUTING.md). No per-screen or speculative Gradle module explosion; one branch is retained through Issue #4 until final reviewed merge. The exact 0–14 phase commit targets are frozen in [Evidence Index §2](ISSUE_4_EVIDENCE_INDEX.md).

**Build order** follows [Issue #16 Implementation Contract §13](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md) and [Issue #16 Validation §13](../architecture/ISSUE_16_VALIDATION.md): root Gradle first; package/static checks; typed ports; DI; Data seams; service/controller; Voice seam; Quality; Compose shell; automated tests; real root-app CI; clean fresh-build and two distinct device runs; final audit and reviewed merge.

A phase cannot pass merely because a source file exists. Tests and real-world evidence follow their exact remaining owners; `spikes/android-validation` APK/workflow are not accepted as root-app execution evidence. Every phase will update `ISSUE_4_EVIDENCE_INDEX.md` and its own audit/evidence file.

## 5. Protected repository and separation controls

**Must remain intact:** `spikes/android-validation/`, `.github/workflows/cluster-b-android-spikes.yml`, `.github/workflows/issue-16-architecture-validation.yml`, final `docs/SOFTWARE_ARCHITECTURE.md`, all Accepted `docs/adr/*`, `docs/handoff/*`, `docs/design-system/*`, global PR template, `.gitignore`.

**Allowed changes this Phase 1:** this audit, evidence index, validation record, dedicated PR template and Phase 1 checklist/Issue status. No root Gradle files or `app/` tree yet. The future Phase 2 creates a *new root* app rather than using `spikes/android-validation/app` as production code.

**Sensitive material excluded:** product/service credentials, signing files, raw recordings or transcripts, user media filenames/content URIs/IDs, copyrighted media assets, third-party provider config, real device personal logs, prohibited telemetry attributes.

## 6. Exact Phase 1 exit gate

- [x] Branch exists and derives from current `main`; no direct `main` commits by Phase 1.
- [x] GitHub Issue #4 remains `open`, text status `In Progress`, original AC statuses preserved (1 checked upstream, 13 pending).
- [x] Scoped PR template, commit/review route and evidence index committed; original generic PR template preserved.
- [x] Isolated spike Gradle root/applicationId verified and not touched by Phase 1.
- [x] Future final PR and independent human review not fabricated; conflicting `develop` advice explicitly disclosed and reviewer decision reserved for Phase 14.
- [x] No product authority, accepted ADR or source artifact modified.
- [x] Phase 1 outcome scoped strictly to GitHub workflow/readiness, *not Android implementation*.

**PASS** authorizes **Phase 2 — Reproducible Android/Gradle bootstrap**; Issue #4 stays open. Source identity and contract checks are completed; Android SDK/Gradle pinning, wrapper, APK, CI checks and two-device runtime evidence have **not yet occurred**.
