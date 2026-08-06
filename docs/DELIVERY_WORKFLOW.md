# Delivery Workflow and Approval Gates — MVP v1.0

> Product authority: [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md). This document controls sequencing only; it does not add product scope.

## Current position

- Issue #1 — PRD/MVP scope lock: **Done**
- Issue #2 — preliminary technical baseline and ADR review: **Done (2026-08-06)**
- Issue #3 — User Flows and state inventory: **In Progress**
- Implementation has **not** started and must remain blocked until the final Software Architecture gate.

## Pre-build sequence

| Gate | Issue | Required output | Exit condition |
|---|---:|---|---|
| Product scope | #1 | Locked PRD v1.0 / MVP v1.0 | Done |
| Preliminary architecture | #2 | ADR-0001…0006 accepted; open evidence recorded | Done |
| UX discovery | #3 | Approved User Flows and complete state inventory | Every P0 requirement has happy, failure and recovery paths |
| UX structure | #15 | Approved low-fidelity Wireframes | No P0 flow requires an invented screen/state/navigation rule |
| Software Architecture | #16 | Implementation-ready architecture baseline and diagrams | Responsibilities, state ownership, lifecycle and dependencies approved |
| Product design | #17 | High-fidelity UI system and clickable prototype | Design handoff approved; golden scenario testable |
| Engineering foundation | #4 | Buildable Android foundation and CI | Clean checkout and two-device build pass |

## Allowed overlap

- Issue #3 proceeds using the accepted Issue #2 constraints.
- Issue #4 and Issue #17 may run in parallel only after Issue #16 is approved.
- UI-heavy implementation Issues #5, #6, #9 and #10 require both #4 and the relevant #17 handoff.
- Service/data work may begin from #4 when its architecture and contracts are approved, without waiting for decorative UI polish.

## Definition of Ready for implementation

An implementation Issue is Ready only when:

1. Its PRD/MVP requirement and priority are explicit.
2. Required User Flow and states are approved.
3. Required Wireframes or system-only transitions are approved.
4. State ownership, dependencies and service contracts are defined in the Software Architecture baseline.
5. Acceptance criteria include success, failure, recovery, accessibility, privacy and tests where applicable.
6. Dependencies are complete and no unresolved P0 decision is hidden in the Issue.

## Change control

- PRD v1.0 and MVP v1.0 remain authoritative.
- A new capability enters v1.0 only through formal scope review and only when it directly tests a Beta hypothesis or removes a P0 delivery blocker.
- P0 Player, Voice, Driving and Stability work cannot be traded for P1/P2 features.
- Accepted ADRs are not silently rewritten. A materially different decision requires a new ADR that supersedes the previous one.
- Structural UX changes return to User Flow/Wireframe review; they must not be buried inside code implementation.
- P2 Like/Liked Songs and the P1 Bluetooth suggestion are first scope cuts if the schedule slips.

## Evidence expected at each gate

- **Product:** scope, metrics and exclusions
- **UX:** flow diagrams, state inventory and review notes
- **Wireframes:** annotated screen/state coverage and navigation
- **Architecture:** context/component/data-flow/state-ownership views and ADR links
- **Design:** components, tokens, accessibility notes and prototype
- **Engineering:** build, CI, tests and architecture-conformance evidence
- **Release:** device/vehicle matrix, telemetry reliability, privacy approval and rollback
