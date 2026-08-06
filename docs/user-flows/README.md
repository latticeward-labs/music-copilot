# User Flow Workspace

This directory contains the durable User Flow artifacts for Music Copilot MVP v1.0.

- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Official product authorities:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Official source filenames:** `Music_Copilot_PRD_v1_EN_Clarified.pdf` and `Music_Copilot_MVP_v1_EN.pdf`
- **Repository-level index:** [User Flows and State Inventory](../USER_FLOWS.md)
- **Current gate:** UF-04/UF-05/UF-06 specifications, connected-flow review, shared State Inventory, and shared Traceability - **PASS**; formal exit evidence remains open
- **Complete User Flow approval:** **Not granted**; UF-07 through UF-13, open Technical Spikes, final reviewer sign-off remain open.

If an artifact conflicts with the official English PRD or MVP, the PRD/MVP v1.0 authorities win. Issue #2 completed on 06 August 2026, and the current [ADR register](../adr/README.md) records ADR-0001 through ADR-0006 as **Accepted**. Older attached PDFs and dated Phase 0/Cluster A snapshots may preserve their earlier Proposed status, but they do not override the current GitHub register.

## Technical Spike evidence

- [Evidence index](evidence/README.md)
- [TS-01](evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md), [TS-02](evidence/TS-02_LIFECYCLE_BOUNDARIES.md), [TS-06](evidence/TS-06_MEDIASTORE_RECONCILIATION.md), [TS-08](evidence/TS-08_PAUSED_RESTORE.md)
- [Result contract](evidence/results/README.md)

Protocol readiness is PASS; execution is BLOCKED until Android build/device evidence exists. Figma is not a source, dependency, or exit criterion.

## Directory map

| Path | Purpose | Current status |
|---|---|---|
| [`mermaid/`](mermaid/) | Canonical diagrams, combined Cluster A logic, Stage 1 traceability, UF-04/UF-05/UF-06 pairs, and the connected Cluster B diagram | Cluster A matrix PASS; UF-04 through UF-06 and connected Cluster B pairs synchronized |
| [`golden-scenarios/`](golden-scenarios/) | Stage 2 workspace for traceable Golden Scenario drafts | Initialized; scenario approval not granted |

Stage names describe workflow gates. User Flows and Golden Scenarios remain durable artifacts for later Wireframe, QA, architecture, and implementation work; they are not archived under generic `Phase-1/` or `Phase-2/` directories.

## Direct artifact register

These are the fourteen files stored directly in `docs/user-flows/`:

| Artifact | Purpose | Controlled status |
|---|---|---|
| [`README.md`](README.md) | Workspace index, authority, catalog, and artifact rules | Current |
| [`PHASE_0_FOUNDATION.md`](PHASE_0_FOUNDATION.md) | Phase 0 terminology, decision, contradiction, and evidence-routing foundation | Complete for drafting; historical gate record |
| [`UF-01_FIRST_LAUNCH_AND_EDUCATION.md`](UF-01_FIRST_LAUNCH_AND_EDUCATION.md) | First launch, resume, replay, education, and readiness handoff | Stage 1 traceability PASS; downstream gates open |
| [`UF-02_PERMISSION_RECOVERY.md`](UF-02_PERMISSION_RECOVERY.md) | Permission denial, three-way Settings recovery, provider boundary, and manual fallback | Stage 1 traceability PASS; TS-01/03/06 open |
| [`UF-03_LIBRARY_READINESS.md`](UF-03_LIBRARY_READINESS.md) | Initial scan, returning-state routing, reconciliation, and truthful readiness | Stage 1 traceability PASS; TS-06 evidence open; UF-04 handoff complete |
| [`UF-04_BROWSE_AND_SEARCH.md`](UF-04_BROWSE_AND_SEARCH.md) | Songs/Artists/Albums/Folders browsing, local Search, recovery, and playback-start handoff | Specification/traceability PASS; TS-06/reviewer gates open |
| [`UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md`](UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) | Queue formation, manual controls, end/corrupt recovery, controller reconnect, and P1 Paused Restore | Specification/traceability PASS; TS-02/TS-06/TS-08/reviewer/Issue #16 gates open |
| [`UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md`](UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md) | Background/screen-off continuity, synchronized system surfaces, controller return, notification branch, and P1 Paused Restore | Specification/traceability PASS; TS-01/TS-02/TS-08/reviewer/Issue #16 gates open |
| [`CLUSTER_B_CONNECTED_REVIEW.md`](CLUSTER_B_CONNECTED_REVIEW.md) | Connected UF-04/05/06 path, seam matrix, invariants, open evidence, and bounded decision | Connected-flow, shared inventory, and shared traceability review PASS; formal exit open |
| [`CLUSTER_B_STATE_INVENTORY.md`](CLUSTER_B_STATE_INVENTORY.md) | Normalized CB-01 through CB-33 state records with all required fields | Shared State Inventory PASS; downstream evidence open |
| [`CLUSTER_B_TRACEABILITY_MATRIX.md`](CLUSTER_B_TRACEABILITY_MATRIX.md) | Requirement, screen, state, authority, recovery, handoff, and evidence mappings | Shared Traceability PASS; downstream evidence open |
| [`CLUSTER_A_FIRST_READINESS.md`](CLUSTER_A_FIRST_READINESS.md) | Connected UF-01/02/03 contract and limited Cluster A decision | Stage 1 matrix PASS; not final approval |
| [`PHASE_1_FIRST_READINESS.md`](PHASE_1_FIRST_READINESS.md) | Controlled Phase 1 gate record synchronized with corrected repository evidence | Stage 1 matrix PASS; open gates retained |
| [`PHASE_1_COMPLETION_CHECKLIST.md`](PHASE_1_COMPLETION_CHECKLIST.md) | Evidence-based pass/open checklist | Cluster A and Cluster B documentation gates PASS; later evidence gates open |

## Required flow catalog

| ID | Flow | Priority | Status |
|---|---|---:|---|
| UF-01 | First launch and education | P0 | Stage 1 traceability PASS; TS-03/reviewer gates open |
| UF-02 | Permission denial and Settings recovery | P0 | Stage 1 traceability PASS; TS-01/TS-03/TS-06 open |
| UF-03 | Local scan and library readiness | P0 | Stage 1 traceability PASS; TS-06 open |
| UF-04 | Browse and search local music | P0 | Complete at specification/traceability level; FR-01 documentation complete; TS-06/reviewer evidence open |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | Complete at specification/traceability level; TS-02/TS-06/TS-08, numeric `T_PREVIOUS`, reviewer evidence open |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | Complete at specification/traceability level; FR-02 documentation complete with UF-05; TS-01/TS-02/TS-08/reviewer evidence open |
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
4. Mark coverage level explicitly. `FR-01` is complete at User Flow specification level across UF-01 through UF-04; `FR-02` is complete at that level across UF-05 and UF-06. TS-01/TS-02/TS-06/TS-08 executed evidence, Wireframes, architecture, and implementation remain open.
5. Preserve these sources of truth: Android permission state for access; MediaStore for accessible media existence/identity; a derived and rebuildable local index for browsing/search; and the playback service/MediaSession for active playback.
6. Preserve the UF-05 queue contract: a new manual selection replaces the active queue with the visible local track collection in visible order only after service acknowledgement; no append, wrap, repeat, shuffle, or playlist editing is invented.
7. Keep P0 active service continuity distinct from P1 Paused Restore; restored playback always requires explicit Resume.
8. Preserve the UF-06 system-surface contract: notification, lock screen, Bluetooth/headset, vehicle media keys, and returning UI are controllers for one MediaSession; permission/exemption behavior is evidence-safe and non-blocking.
9. Keep `Denied - requestable`, `Permanently Denied`, and all three Settings-return outcomes distinct. Keep `Provider unavailable` distinct from permission denial.
10. Preserve `Not now`, manual controls, and non-crashing recovery; never create a coercive Settings or permission loop.
11. Do not add account, backend, sync, catalog, streaming, upload/download, Android Auto, iOS, or v1.1 scope.
12. Use the current GitHub ADR register for ADR status. The canonical connected Cluster B diagram must preserve every UF-04/05/06 seam and open-evidence boundary.
13. Use CB-01 through CB-33 as the canonical shared Cluster B state identities; update the shared Traceability Matrix whenever a constituent state or seam changes.
14. Do not claim Technical Spike evidence, role approval, implementation completion, formal Cluster B exit, or final User Flow approval until the corresponding evidence is recorded.

## Workflow

1. Maintain requirement-to-flow traceability and Markdown/Mermaid parity.
2. Draft traceable Golden Scenarios in [`golden-scenarios/`](golden-scenarios/) without presenting them as approved.
3. Complete UF-07 through UF-13 and all linked recovery behavior; UF-04, UF-05, and UF-06 are complete at specification/traceability level.
4. Produce and record TS-01, TS-02, TS-03, TS-06, and TS-08 evidence.
5. Keep User Flow ADR references synchronized with the Accepted ADR register; route any material later change through Issue #16 and a new/superseding ADR.
6. 7. Obtain real Product, Design, Android, QA, Privacy, and Governance sign-off where required.
8. Close Issue #3 and proceed to downstream gates only when its acceptance criteria are actually satisfied.
