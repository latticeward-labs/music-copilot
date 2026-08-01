# User Flows and State Inventory - MVP v1.0

- **Status:** In progress - Phase 0 and Cluster A (UF-01 through UF-03) complete
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Workfile:** [Music Copilot - MVP v1.0](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

This document is the repository index for User Flow artifacts. Until Issue #3 is approved, the complete flow set is not implementation-ready.

## Phase 0 foundation - complete

- **Decision:** Approved for User Flow drafting on 01 August 2026
- **Detailed record:** [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- **Figma section:** [00 / Phase 0 Foundation](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=14-2)
- **Boundary:** Phase 0 completed input, scope, state, decision, conflict, terminology, spike, and coverage preparation. It did not approve the final flow set, low-fidelity Wireframes, final Software Architecture, or implementation.
- **ADR dependency:** Issue #2 and the ADR register must align on explicit reviewed outcomes before the complete User Flow set is marked Approved.

## Phase 1 - Cluster A First Readiness - complete for review

**Flows:** UF-01, UF-02, UF-03  
**Decision date:** 01 August 2026  
**Status boundary:** Cluster A is complete and registered. Issue #3 remains open because UF-04 through UF-13 and final cross-role approval are not complete.

### Repository records

- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md)
- [UF-01 - First Launch and Education](user-flows/UF-01_FIRST_LAUNCH_AND_EDUCATION.md)
- [UF-02 - Permission Denial and Settings Recovery](user-flows/UF-02_PERMISSION_RECOVERY.md)
- [UF-03 - Local Scan and Library Readiness](user-flows/UF-03_LIBRARY_READINESS.md)

### Figma records

- [01 / Phase 1 - First Readiness](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-2)
- [UF-01 diagram](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-26)
- [UF-02 diagram](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-27)
- [UF-03 diagram](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-28)
- [Connected Cluster A swimlane](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-29)
- [State Inventory and Traceability](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-30)
- [Review and Exit Record](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF?node-id=23-31)

### Cluster A exit decision

Cluster A covers first launch, two-slide onboarding, Skip/Replay, contextual media access, Denied/Permanently Denied recovery, Android Settings return and recheck, permission revocation, initial/incremental scanning, Scanning/Empty/Ready/error states, six-command education, and just-in-time microphone access.

The next drafting cluster is **Cluster B: UF-04 Browse/Search, UF-05 Manual Playback, UF-06 Background/System Controls**.

## Required flow catalog

| ID | Flow | Priority | Status | Required terminal states |
|---|---|---:|---|---|
| UF-01 | First launch and education | P0 | Complete for Cluster A review | Home Ready, permission recovery, Empty Library, manual fallback |
| UF-02 | Permission denial and Settings recovery | P0 | Complete for Cluster A review; TS-01/TS-06 evidence open | Granted, Denied, Permanently Denied, manual fallback |
| UF-03 | Local scan and library readiness | P0 | Complete for Cluster A review; TS-06 evidence open | Scanning, Empty, Ready, recoverable error/revocation |
| UF-04 | Browse and search local music | P0 | Not started | Result, No Result, Error, playback start |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | Not started | Playing, Paused, end/queue behavior, corrupt item recovery |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | Not started | synchronized notification/lock-screen/Bluetooth state |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | Not started | Playing, Paused, No Track, immediate exit |
| UF-08 | Direct Voice command | P0 | Not started | Listening, Processing, Executed, Failed |
| UF-09 | Ambiguous or missing artist | P0 | Not started | Confirming, confirmed execution, not found |
| UF-10 | Voice timeout and retry | P0 | Not started | one retry, manual fallback, playback preserved |
| UF-11 | Calls, Audio Focus and route changes | P0 | Not started | deterministic pause/duck/recovery; no unintended speaker output |
| UF-12 | Settings, privacy and data clearing | P0 control surface | Not started | Updated, cleared, consent-aware states |
| UF-13 | Liked Songs | P2 | Not started / first complete cut | Empty, Ready; removable from schedule |

## State record template

Every state must record:

- Entry trigger and preconditions
- Visible information and primary action
- Allowed secondary actions
- System work in progress
- Source of truth and persistence expectation
- Success transition
- Failure transition
- Recovery path
- Analytics event and allowed parameters, if any
- Accessibility requirements
- Driving restrictions, if applicable

## Approval checklist

- [ ] Every PRD/MVP P0 requirement maps to at least one completed User Flow.
- [ ] Every flow includes happy, empty, error and recovery behavior where applicable.
- [ ] UI closure, recreation, background playback and route interruptions are covered.
- [ ] Voice certainty, ambiguity, timeout, retry and not-found behavior are covered.
- [x] Cluster A P0 scope and recovery behavior are explicit.
- [x] Cluster A open architecture questions are routed rather than silently decided in Figma.
- [x] Cluster A Figma sections and repository records are linked.
- [ ] P0/P1/P2 scope is explicit across the complete flow set.
- [ ] Product and Android reviewers approve the complete flow set and state inventory.
- [ ] Issue #2 and the ADR register align on explicit reviewed outcomes.

## Artifact links

- [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md) - approved for User Flow drafting
- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md) - complete for review
- Complete UF-04 through UF-13 diagrams and final review date - _to be completed by Issue #3_
