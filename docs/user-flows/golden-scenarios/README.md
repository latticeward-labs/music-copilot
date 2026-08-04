# Stage 2 — Golden Scenarios

This directory is the Stage 2 workspace for Golden Scenarios. The directory uses the durable artifact name `golden-scenarios` instead of a generic `Phase-2` name so the scenarios remain discoverable and reusable during Wireframes, QA and implementation.

- **Input gate:** Stage 1 Cluster A Traceability Matrix — **PASS**
- **Overall User Flow status:** In progress
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Authorities:** [PRD v1.0](../../PRD.md), [MVP v1.0](../../MVP.md), and the [User Flow workspace rules](../README.md)
- **Canonical Stage 1 evidence:** [State Inventory and Traceability](../mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) and [Review Record](../mermaid/01_7_REVIEW_AND_APPROVAL_RECORD.md)

Creating this workspace does not mark Stage 2 complete, approve UF-04 through UF-13, accept the proposed ADRs, close Issue #3, or authorize Wireframes or implementation.

## Purpose

A Golden Scenario describes a primary successful end-to-end user outcome. It must stay connected to its source User Flows and requirements, and it must reference valid recovery branches at every meaningful failure point.

A Golden Scenario is not:

- a replacement for the complete User Flow set;
- a happy-path-only screen list;
- a Wireframe or visual design;
- an implementation specification;
- evidence that an ADR or technical spike has been approved.

## Required scenario record

Every `GS-##` document must include:

| Field | Required content |
|---|---|
| Scenario ID and title | Stable `GS-##` identifier and outcome-oriented name |
| User goal | The value the user is trying to obtain |
| Priority and scope | P0/P1/P2 and explicit MVP v1.0 boundary |
| Entry state | Verified preconditions and source-of-truth state |
| Covered flows | Exact `UF-##` chain |
| Requirement mapping | User Stories, FRs and acceptance criteria |
| Primary success path | Numbered user/system sequence |
| Success state | Observable terminal state and authoritative owner |
| Recovery references | Links to denial, empty, error, interruption and retry branches |
| Data and permissions | MediaStore/index/playback/permission/consent boundaries |
| ADR influence | Relevant ADRs with their actual governance status |
| Open evidence | Technical spikes, unresolved decisions and review blockers |
| Analytics/privacy | Allowed measurement without personal media or raw Voice data |
| Accessibility/driving | Applicable non-visual, touch-target and distraction constraints |
| Review status | Draft, In Review or Approved, with reviewer/date when real |

## Initial scenario backlog

These are planning candidates, not approved scenario definitions.

| Candidate | Intended outcome | Expected flow coverage | Status |
|---|---|---|---|
| GS-01 — First Launch to Ready Library | A new user reaches a truthful Ready or Empty local library without an account | UF-01 → UF-02 as needed → UF-03; UF-04 remains required for full browse behavior | Planned |
| GS-02 — Find Music and Start Playback | A ready user finds local music and begins authoritative playback | UF-04 → UF-05 | Blocked until UF-04/UF-05 are drafted |
| GS-03 — Continue Playback Outside the App | Playback remains synchronized in background, screen-off and system controls | UF-05 → UF-06 | Blocked until UF-05/UF-06 and TS-01 evidence |
| GS-04 — Drive and Execute a Direct Voice Command | A user enters Driving Mode and completes an allowed push-to-talk command | UF-07 → UF-08; UF-09/UF-10 referenced for recovery | Blocked until Voice flows and TS-03 evidence |
| GS-05 — Recover Safely from an Interruption | Playback responds deterministically to calls, Audio Focus or route changes | UF-11 with UF-05/UF-06 state | Blocked until UF-11 is drafted |

## File convention

Use paired files when a scenario includes Mermaid:

```text
GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md
GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.mmd
```

The `.md` file is the reviewable scenario record. The `.mmd` file is the canonical raw diagram source. The Mermaid block embedded in Markdown must remain logically identical to the paired `.mmd` file.

## Stage 2 exit gate

Stage 2 can be marked complete only when:

- the required Golden Scenario set is agreed and documented;
- every scenario maps to explicit User Stories, FRs and `UF-##` records;
- success states and sources of truth are explicit;
- every meaningful failure point references a valid recovery path;
- open ADR/spike/Figma dependencies remain visible;
- Markdown/Mermaid parity is validated;
- scope exclusions remain intact;
- the required reviewers and decision date are recorded.

Until then, all scenario entries remain Draft or Planned.
