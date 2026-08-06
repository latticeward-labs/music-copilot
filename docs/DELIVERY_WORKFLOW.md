# Delivery Workflow and Approval Gates - MVP v1.0

> [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md) are authoritative. This file controls sequencing only.

## Current position

- Issue #1 - scope lock: **Done**
- Issue #2 - preliminary ADR gate: **Done (2026-08-06)**
- Issue #3 - User Flows/state inventory: **In Progress**
- Implementation remains blocked until final Software Architecture approval.

## Pre-build sequence

| Gate | Issue | Exit condition | Status |
|---|---:|---|---|
| Product scope | #1 | PRD/MVP locked | Done |
| Preliminary architecture | #2 | Six ADRs accepted; open evidence routed | Done |
| UX discovery | #3 | P0 happy/failure/recovery flows approved | In Progress |
| UX structure | #15 | No invented P0 screen/state/navigation rule | Pending |
| Software Architecture | #16 | Ownership, lifecycle, dependencies approved | Pending |
| Product design | #17 | Handoff/prototype approved | Pending |
| Engineering foundation | #4 | Clean build/CI on two devices | Blocked by #16 |

## Allowed overlap

Issue #3 proceeds using accepted Issue #2 constraints. Issue #4 and #17 may run in parallel only after #16. UI-heavy work requires #4 and relevant design handoff. Service/data work begins from #4 only with approved contracts.

## Definition of Ready for implementation

1. PRD/MVP requirement and priority are explicit.
2. User Flow and states are approved.
3. Wireframes/system-only transitions are approved.
4. State ownership, dependencies and service contracts are approved.
5. Acceptance covers success, failure, recovery, accessibility, privacy and tests.
6. Dependencies are complete; no unresolved P0 decision is hidden.

## Change control

PRD/MVP remain authoritative. Scope additions require formal review. P0 work cannot be traded for P1/P2. Accepted ADRs are not silently rewritten; material changes require a superseding ADR. Structural UX changes return to flow/wireframe review. P2 Like and P1 Bluetooth suggestion are first schedule cuts.

## Evidence expected

- Product: scope, metrics, exclusions
- UX: flows, states, review notes
- Wireframes: screen/state/navigation coverage
- Architecture: context, components, data flows, ownership and ADRs
- Design: components, tokens, accessibility and prototype
- Engineering: build, CI, tests and conformance
- Release: device/vehicle matrix, telemetry, privacy and rollback
