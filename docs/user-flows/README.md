# User Flow Workspace

This directory contains the durable User Flow artifacts for Music Copilot MVP v1.0.

- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Product authority:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Repository-level index:** [User Flows and State Inventory](../USER_FLOWS.md)
- **Current gate:** Stage 1 Traceability Matrix for Cluster A — **PASS**
- **Complete User Flow approval:** Not granted; UF-04 through UF-13, open evidence, and final review remain outstanding.

If a User Flow artifact conflicts with the PRD or MVP, the PRD/MVP v1.0 product authorities win. Proposed ADRs may constrain or identify open decisions, but they are not binding until Issue #2 records an explicit Accepted or Revised outcome.

## Directory map

| Path | Purpose | Current status |
|---|---|---|
| [`mermaid/`](mermaid/) | Canonical diagrams and supporting Markdown for Cluster A and Stage 1 traceability/review | Stage 1 matrix PASS |
| [`golden-scenarios/`](golden-scenarios/) | Stage 2 workspace for Golden Scenario definitions and traceable main-success paths | Initialized; scenario drafting pending |
| [`PHASE_0_FOUNDATION.md`](PHASE_0_FOUNDATION.md) | Approved preparation, terminology, decision and evidence-routing foundation | Complete for drafting |
| [`CLUSTER_A_FIRST_READINESS.md`](CLUSTER_A_FIRST_READINESS.md) | First-readiness Cluster A record | Complete for Stage 1 review |
| `UF-01...UF-03` records | First launch, permission recovery and library readiness | Traceability complete; downstream evidence remains open |

Stage names describe the workflow gate. Flow and Golden Scenario artifacts remain durable references in later Wireframe, QA and implementation work; they are not moved into archival `Phase-1/` or `Phase-2/` folders.

## Required flow catalog

| ID | Flow | Priority | Status |
|---|---|---:|---|
| UF-01 | First launch and education | P0 | Stage 1 traceability complete |
| UF-02 | Permission denial and Settings recovery | P0 | Stage 1 traceability complete; TS-01/TS-06 open |
| UF-03 | Local scan and library readiness | P0 | Stage 1 traceability complete; TS-06 open |
| UF-04 | Browse and search local music | P0 | Not started; required to complete FR-01 |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | Not started |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | Not started |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | Not started |
| UF-08 | Direct Voice command | P0 | Not started |
| UF-09 | Ambiguous or missing artist | P0 | Not started |
| UF-10 | Voice timeout and retry | P0 | Not started |
| UF-11 | Calls, Audio Focus and route changes | P0 | Not started |
| UF-12 | Settings, privacy and data clearing | P0 control surface | Not started |
| UF-13 | Liked Songs | P2 | Not started / removable from schedule |

## Artifact rules

1. Use stable identifiers: `UF-##` for User Flows and `GS-##` for Golden Scenarios.
2. Keep every Markdown diagram logically identical to its paired raw `.mmd` source.
3. Record the user goal, priority, User Story/FR mapping, states, ADR influence, failure/recovery, data/permissions and open evidence.
4. Mark partial coverage explicitly. In particular, FR-01 remains partial until UF-04 completes Songs, Artists, Albums and Folders behavior.
5. Preserve Android permission, MediaStore, derived-index and service-owned playback sources of truth.
6. Keep recovery paths and manual controls reachable; do not turn a Golden Scenario into an error-free fiction.
7. Do not add accounts, backend, sync, catalog, streaming, upload/download, Android Auto, iOS or v1.1 scope.
8. Do not claim Figma parity, ADR approval, technical-spike evidence or final User Flow approval until the corresponding review is recorded.

## Workflow

1. Maintain requirement-to-flow traceability.
2. Define Golden Scenarios in [`golden-scenarios/`](golden-scenarios/).
3. Complete the remaining User Flows and recovery branches.
4. Synchronize reviewed diagrams with Figma.
5. Obtain complete Product/Android review and close Issue #3 only when its acceptance criteria are satisfied.
6. Continue to low-fidelity Wireframes only after the applicable User Flow gate authorizes it.
