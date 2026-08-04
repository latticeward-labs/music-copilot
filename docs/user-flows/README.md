# User Flow Workspace

This directory contains the durable User Flow artifacts for Music Copilot MVP v1.0.

- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Official product authorities:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Official source filenames:** `Music_Copilot_PRD_v1_EN_Clarified.pdf` and `Music_Copilot_MVP_v1_EN.pdf`
- **Repository-level index:** [User Flows and State Inventory](../USER_FLOWS.md)
- **Current gate:** Stage 1 Traceability Matrix for Cluster A — **PASS**
- **Complete User Flow approval:** **Not granted**; FR-01, UF-04 through UF-13, Issue #2, open Technical Spikes, Figma synchronization, and final reviewer sign-off remain open.

If an artifact conflicts with the official English PRD or MVP, the PRD/MVP v1.0 authorities win. ADR-0001 through ADR-0006 currently influence traceability as **Proposed** decisions; none may be treated as Accepted or Revised until Issue #2 and the ADR register record the reviewed outcome.

## Directory map

| Path | Purpose | Current status |
|---|---|---|
| [`mermaid/`](mermaid/) | Canonical diagrams, combined Cluster A logic, Stage 1 traceability, and limited review record | Stage 1 matrix PASS |
| [`golden-scenarios/`](golden-scenarios/) | Stage 2 workspace for traceable Golden Scenario drafts | Initialized; scenario approval not granted |

Stage names describe workflow gates. User Flows and Golden Scenarios remain durable artifacts for later Wireframe, QA, architecture, and implementation work; they are not archived under generic `Phase-1/` or `Phase-2/` directories.

## Direct artifact register

These are the eight files stored directly in `docs/user-flows/`:

| Artifact | Purpose | Controlled status |
|---|---|---|
| [`README.md`](README.md) | Workspace index, authority, catalog, and artifact rules | Current |
| [`PHASE_0_FOUNDATION.md`](PHASE_0_FOUNDATION.md) | Phase 0 terminology, decision, contradiction, and evidence-routing foundation | Complete for drafting; historical gate record |
| [`UF-01_FIRST_LAUNCH_AND_EDUCATION.md`](UF-01_FIRST_LAUNCH_AND_EDUCATION.md) | First launch, resume, replay, education, and readiness handoff | Stage 1 traceability PASS; downstream gates open |
| [`UF-02_PERMISSION_RECOVERY.md`](UF-02_PERMISSION_RECOVERY.md) | Permission denial, three-way Settings recovery, provider boundary, and manual fallback | Stage 1 traceability PASS; TS-01/03/06 open |
| [`UF-03_LIBRARY_READINESS.md`](UF-03_LIBRARY_READINESS.md) | Initial scan, returning-state routing, reconciliation, and truthful readiness | Stage 1 traceability PASS; TS-06 and UF-04 open |
| [`CLUSTER_A_FIRST_READINESS.md`](CLUSTER_A_FIRST_READINESS.md) | Connected UF-01/02/03 contract and limited Cluster A decision | Stage 1 matrix PASS; not final approval |
| [`PHASE_1_FIRST_READINESS.md`](PHASE_1_FIRST_READINESS.md) | Controlled Phase 1 gate record synchronized with corrected repository evidence | Stage 1 matrix PASS; open gates retained |
| [`PHASE_1_COMPLETION_CHECKLIST.md`](PHASE_1_COMPLETION_CHECKLIST.md) | Evidence-based pass/open checklist | PASS only for the matrix gate |

## Required flow catalog

| ID | Flow | Priority | Status |
|---|---|---:|---|
| UF-01 | First launch and education | P0 | Stage 1 traceability PASS; TS-03/Figma/reviewer gates open |
| UF-02 | Permission denial and Settings recovery | P0 | Stage 1 traceability PASS; TS-01/TS-03/TS-06 open |
| UF-03 | Local scan and library readiness | P0 | Stage 1 traceability PASS; TS-06 open |
| UF-04 | Browse and search local music | P0 | Not complete; required to complete FR-01 |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | Not complete |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | Not complete |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | Not complete |
| UF-08 | Direct Voice command | P0 | Not complete |
| UF-09 | Ambiguous or missing artist | P0 | Not complete |
| UF-10 | Voice timeout and retry | P0 | Not complete |
| UF-11 | Calls, Audio Focus and route changes | P0 | Not complete |
| UF-12 | Settings, privacy and data clearing | P0 control surface | Not complete |
| UF-13 | Liked Songs | P2 | Not complete / removable from schedule |

## Artifact rules

1. Use stable identifiers: `UF-##` for User Flows and `GS-##` for Golden Scenarios.
2. Keep each embedded Mermaid block logically identical to its paired raw `.mmd` source.
3. Record all ten Stage 1 fields: Flow ID, user goal, priority, User Story, Functional Requirement, screens/states, influencing ADRs, failure/recovery, data/permissions, and open assumptions/evidence.
4. Mark partial coverage explicitly. `FR-01 = Partial` until UF-04 completes Songs, Artists, Albums, and Folders browsing/search behavior.
5. Preserve these sources of truth: Android permission state for access; MediaStore for accessible media existence/identity; a derived and rebuildable local index for browsing/search; and the playback service/MediaSession for active playback.
6. Keep `Denied - requestable`, `Permanently Denied`, and all three Settings-return outcomes distinct. Keep `Provider unavailable` distinct from permission denial.
7. Preserve `Not now`, manual controls, and non-crashing recovery; never create a coercive Settings or permission loop.
8. Do not add account, backend, sync, catalog, streaming, upload/download, Android Auto, iOS, or v1.1 scope.
9. Do not claim Figma parity, ADR approval, Technical Spike evidence, role approval, complete FR-01, or final User Flow approval until the corresponding evidence is recorded.

## Workflow

1. Maintain requirement-to-flow traceability and Markdown/Mermaid parity.
2. Draft traceable Golden Scenarios in [`golden-scenarios/`](golden-scenarios/) without presenting them as approved.
3. Complete UF-04 through UF-13 and all linked recovery behavior.
4. Produce and record TS-01, TS-03, and TS-06 evidence.
5. Resolve Issue #2 and update the ADR register with explicit Accepted or Revised outcomes.
6. Synchronize reviewed repository behavior into Figma.
7. Obtain real Product, Design, Android, QA, Privacy, and Governance sign-off where required.
8. Close Issue #3 and proceed to downstream gates only when its acceptance criteria are actually satisfied.
