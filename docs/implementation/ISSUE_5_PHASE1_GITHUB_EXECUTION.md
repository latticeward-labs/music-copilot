# Issue #5 — Phase 1 GitHub Execution and Delivery Contract

**Date:** 2026-10-16 | **Gate:** P01 **PASS (documentation/CI contract only)**; NOT Android feature acceptance.
**Issue:** [#5](https://github.com/music-copilot-organization/music-copilot/issues/5) | **Branch:** `feature/5-onboarding-permissions-recovery` | **Frozen main:** `fbd7bb50238bc6a063ab75feb7f1a00aa969e81d`.
**Phase-0 commit:** `6152bbd2e3f3b62813185f189c9f655d05b418fe` | **Archive:** `MSM2(1)(1).rar`, confirmed SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

## GitHub state verified at P01 entry

- Authenticated repository: `music-copilot-organization/music-copilot`; the prior latticeward-labs repository owner is historical.
- `main` was at `fbd7bb50238bc6a063ab75feb7f1a00aa969e81d` with [successful Android CI #38816374430](https://github.com/music-copilot-organization/music-copilot/actions/runs/38816374430).
- The existing P00 execution branch was one commit **ahead** and zero **behind** current main; maintain that same branch, no new fork, no writes to main.
- `develop@a8944b287498aff557af3d7e17179b599c5c5ae5` was **367 commits behind main**. There were no open PRs and no feature-branch CI runs at audit entry.
- GitHub branch API showed main/develop `protected=false`; this alone is NOT proof that no organizational rulesets apply. GitHub Project V2 exact Status remains NOT VERIFIED.

## Branch and final PR routing policy

**Proposed P14 PR:** `feature/5-onboarding-permissions-recovery` -> `main` after all implementation phases and evidence. `CONTRIBUTING.md` recommends integration into `develop` and explicitly prohibits sole self-approval; however current root Android app, merged #4/#16/#17 and working Android CI are on main. Because develop is stale, creating a PR targeting it would carry unrelated history. This is a **proposed Issue #5 exception requiring explicit independent policy/owner approval before merge**, NOT a granted exception. If no approval, STOP and resolve the integration strategy first; do not silently retarget to stale develop or inherit Issue #4's owner-directed exception.

At P14: one focused PR, scoped title and `Refs #5` (do not claim `Closes #5` until complete); 13/13 current Issue acceptance items must link to actual code, tests, reviewer findings and device/API evidence; successful CI on exact PR head; independent human review per CONTRIBUTING or a separate expressly documented Issue #5 policy change; squash merge preferred and merge/Issue close/Project value independently verified. No self-approval, fabricated review, premature Issue closure or automatic Done.

## Frozen authority and non-feature scope

Controlling product requirements: [English PRD](https://github.com/music-copilot-organization/music-copilot/blob/main/docs/PRD.md) and [English MVP](https://github.com/music-copilot-organization/music-copilot/blob/main/docs/MVP.md). Implementation: [ADR-0001..0008](https://github.com/music-copilot-organization/music-copilot/blob/main/docs/adr/README.md), [final architecture](https://github.com/music-copilot-organization/music-copilot/blob/main/docs/SOFTWARE_ARCHITECTURE.md), Issue #16 contract corrections. Behavior/visual: approved UF-01/02/03, GS-01, WF-02/03/04, Issue #17 Hi-Fi and [Compose handoff](https://github.com/music-copilot-organization/music-copilot/blob/main/docs/handoff/SCREEN_SPECS.md). Proposed archive System Architecture/Code PDFs are historical engineering inputs, not substitutes for approved contracts.

**No Android product code in P01.** Android owns runtime grant/dialog/Settings state; MediaStore owns accessible-media truth; DataStore stores onboarding and consent but never authoritative permission grant; Media3 service owns playback. Cloud STT stays OFF until verified provider-specific purpose/destination/retention and Legal/Privacy consent approval. No account, backend, streaming, telemetry of permission results or private media, unauthorized UI routes or Compose-drawn native dialogs.

## Deliverables and review gates

1. [Source/13-AC traceability](ISSUE_5_PHASE0_TRACEABILITY.json) and [P00 readiness audit](ISSUE_5_PHASE0_SOURCE_TRACEABILITY_DOR.md) retained unchanged.
2. [P01 Evidence Index and synthetic fixture contract](ISSUE_5_EVIDENCE_INDEX.md) covers 17 planned T01..T17 cases, no fake PASS.
3. [Issue-specific final PR template](../../.github/PULL_REQUEST_TEMPLATE/issue-5-onboarding-permissions-recovery.md) and documented `main` exception/review conditions.
4. [Automated P01 validator](../../scripts/validate_issue5_phase1.py), [P01 GitHub workflow](../../.github/workflows/issue-5-phase1-contract.yml), [machine P01 register](ISSUE_5_PHASE1_VALIDATION.json). This workflow tests contracts only, NOT Android build/device runtime.
5. [Updated phased plan](ISSUE_5_PHASED_EXECUTION_PLAN.md), P00 previously PASS; P01 may become PASS only after real green P01 CI and final branch read-back.

## P01 acceptance checklist

- [x] Rechecked archive SHA, issue/open AC 0/13, existing branch ancestry, current main CI, and develop divergence.
- [x] Scoped branch delivery contract, independent review gates, non-inherited #4 exception, and next-phase evidence responsibilities specified.
- [x] PR template, Evidence Index, script and workflow committed to feature branch without app implementation.
- [x] Real initial Phase-1 [GitHub Actions run #38819186656](https://github.com/music-copilot-organization/music-copilot/actions/runs/38819186656) succeeded at `6a9c663cb4999dd0f9a9a8b4e329aa8e4d2007e3` (job #116711292922; checkout and Python contract/negative fixture validation SUCCESS). The post-evidence final head must be checked separately.
- [x] Prior-phase read-back verified the 13-AC/17-test contracts, real workflow result and Issue #5 OPEN (4/4 DoR, 13 AC still unchecked); final evidence commit comparison and CI will be reported in Issue #5 after their separate verification.

**Nonclaims:** no Issue #5 feature implementation, no physical/instrumented Android permission evidence, no external PR reviewer approval, no PR merge, no Project Done. Next: P02 bootstrap/state ownership only. Documentation CI does NOT imply any permissions, Android app or device AC passed.
