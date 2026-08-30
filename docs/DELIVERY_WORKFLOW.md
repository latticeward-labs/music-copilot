# Delivery Workflow and Approval Gates — MVP v1.0

- **Version:** MVP v1.0
- **Status:** Current through Issue #16 Phase 11 final architecture exit
- **Updated:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)

> Product authority: [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md). This document controls sequencing only; it does not add product scope.

## Current position

- Issue #1 — PRD/MVP scope lock: **Done**
- Issue #2 — preliminary technical baseline and ADR review: **Done (6 August 2026)**
- Issue #3 — User Flows and state inventory: **Done / Closed (23 August 2026)**
- Issue #15 — low-fidelity Wireframes: **Done / Closed (27 August 2026)**
- Issue #16 — Software Architecture: **Done — Phase 11 final acceptance PASS; PR #46 merged; open Blocker 0 / open Major 0**
- Issue #4 — Android foundation: **UNBLOCKED / READY by completed Issue #16 architecture exit**

The implementation-facing architecture authority is the final [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md). The [Phase 10 Cross-role Review](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md) records Product, Android, Design/UX, QA, Accessibility and Privacy decisions, and the [Phase 11 Final Acceptance Audit](architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md) records the 14/14 evidence reconciliation and merge gate.

## Pre-build sequence

| Gate | Issue | Required output | Current state / exit condition |
|---|---:|---|---|
| Product scope | #1 | Locked PRD v1.0 / MVP v1.0 | **Done** |
| Preliminary architecture | #2 | ADR-0001…0006 accepted; open evidence recorded | **Done** |
| UX discovery | #3 | Approved User Flows and complete state inventory | **Done / Closed** |
| UX structure | #15 | Approved low-fidelity Wireframes | **Done / Closed** |
| Software Architecture | #16 | Implementation-ready architecture baseline, diagrams, contracts, runtime policy, validation seams and cross-role approval | **Done — Phase 11 PASS / merged** |
| Product design | #17 | High-fidelity UI system and clickable prototype | Separate downstream gate |
| Engineering foundation | #4 | Buildable Android foundation and CI | **UNBLOCKED / READY** |

## Allowed overlap

- Historical overlap rules for Issues #3 and #15 are satisfied because both are complete.
- Issue #4 may now begin from the final Issue #16 baseline and Accepted ADRs.
- Issue #17 remains a separate design gate; any implementation dependency on its handoff must still be respected.
- UI-heavy implementation Issues #5, #6, #9 and #10 require both #4 and the relevant #17 handoff.
- Service/data foundation work may proceed from #4 without waiting for decorative UI polish, provided the approved architecture boundaries remain unchanged.

## Definition of Ready for implementation

An implementation Issue is Ready only when:

1. Its PRD/MVP requirement and priority are explicit.
2. Required User Flow and states are approved.
3. Required Wireframes or system-only transitions are approved.
4. State ownership, dependencies and service contracts are defined in the [Software Architecture baseline](SOFTWARE_ARCHITECTURE.md).
5. Acceptance criteria include success, failure, recovery, accessibility, privacy and tests where applicable.
6. Dependencies are complete and no unresolved P0 decision is hidden in the Issue.
7. Issue #16 final review/merge/exit gate has passed for work blocked by architecture — **satisfied for Issue #4**.

## Change control

- PRD v1.0 and MVP v1.0 remain authoritative product sources under the Issue #16 English source-governance register.
- A new capability enters v1.0 only through formal scope review and only when it directly tests a Beta hypothesis or removes a P0 delivery blocker.
- P0 Player, Voice, Driving and Stability work cannot be traded for P1/P2 features.
- Accepted ADRs are not silently rewritten. A materially different decision requires a new ADR that explicitly supersedes the previous one.
- Structural UX changes return to User Flow/Wireframe review; they must not be buried inside code implementation.
- P2 Like/Liked Songs and the P1 Bluetooth suggestion are first scope cuts if the schedule slips.
- Dated phase documents are historical evidence; current architecture status is controlled by `README.md`, this workflow, `TECHNICAL_DECISIONS.md`, the ADR Register and `SOFTWARE_ARCHITECTURE.md`.
- Phase 10 review corrections are binding where explicitly identified and require no new ADR because they do not change ownership, topology, persistence authority or product scope.
- Phase 11 closes the architecture gate; Issue #4 implementation must not reopen ownership/lifecycle/dependency/recovery choices as local guesses.

## Evidence expected at each gate

- **Product:** scope, metrics and exclusions
- **UX:** flow diagrams, state inventory and review notes
- **Wireframes:** annotated screen/state coverage, navigation and recovery
- **Architecture:** context/component/data-flow/state-ownership views, public contracts, runtime policy, ADR links, test seams, traceability and cross-role review
- **Design:** components, tokens, accessibility notes and prototype
- **Engineering:** build, CI, tests and architecture-conformance evidence
- **Release:** device/vehicle matrix, telemetry reliability, privacy approval and rollback

## Current architecture evidence

- [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md)
- [Phase 11 Final Acceptance Audit](architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md)
- [Architecture Traceability Matrix](architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- [Implementation Structure and Public Contracts](architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md)
- [Runtime Policies](architecture/RUNTIME_POLICIES.md)
- [Validation and Test Architecture](architecture/ISSUE_16_VALIDATION.md)
- [Phase 10 Cross-role Review](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)
- [Phase 10 Findings Register](architecture/review/ISSUE_16_FINDINGS_REGISTER.json)
- [Phase 10 Reviewed Contract Corrections](architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md)
- [ADR Register](adr/README.md)