# User Flow Workspace

This directory contains the durable User Flow artifacts for Music Copilot MVP v1.0.

- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Official product authorities:** [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md)
- **Official source filenames:** `Music_Copilot_PRD_v1_EN_Clarified.pdf` and `Music_Copilot_MVP_v1_EN.pdf`
- **Repository-level index:** [User Flows and State Inventory](../USER_FLOWS.md)
- **Current gate:** **Cluster C UF-07 — PASS / COMPLETE at User Flow specification level**
- **Active stage:** **Cluster D — UF-08/UF-09/UF-10 Voice**
- **Complete User Flow approval:** Not yet granted; UF-08 through UF-13 and final Issue #3 approval remain open.

If an artifact conflicts with the official English PRD or MVP, the PRD/MVP v1.0 authorities win. Issue #2 completed on 06 August 2026, and the current [ADR register](../adr/README.md) records ADR-0001 through ADR-0006 as **Accepted**.

## Cluster B Technical Spike evidence

- [Evidence index](evidence/README.md) — **PASS**
- [TS-01](evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md) — **PASS / approved N/A where recorded**
- [TS-02](evidence/TS-02_LIFECYCLE_BOUNDARIES.md) — **PASS**
- [TS-06](evidence/TS-06_MEDIASTORE_RECONCILIATION.md) — **PASS**
- [TS-08](evidence/TS-08_PAUSED_RESTORE.md) — **PASS**
- [Retrospective evidence-retention waiver](evidence/CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md)
- [Final Cluster B cross-role sign-off](CLUSTER_B_SIGNOFF_2026-08-18.md)
- [Result contract](evidence/results/README.md)

Protocol readiness, harness implementation, automated validation, physical/manual execution, accepted evidence bundles, and Cluster B review/sign-off are complete. Historical raw captures that were not retained are covered by the explicit one-time retrospective waiver and are not fabricated. Figma is not a User Flow source, dependency, or exit criterion.

## Directory map

| Path | Purpose | Current status |
|---|---|---|
| [`mermaid/`](mermaid/) | Canonical diagrams for completed Clusters A-C, including UF-07 Driving Mode | Cluster A PASS; Cluster B PASS; Cluster C UF-07 parity PASS |
| [`golden-scenarios/`](golden-scenarios/) | Stage 2 workspace for traceable Golden Scenario drafts | Initialized; scenario approval not granted |
| [`evidence/`](evidence/) | Technical Spike protocols, accepted result bundles, and evidence governance | **Cluster B evidence PASS** |

## Direct artifact register

| Artifact | Purpose | Controlled status |
|---|---|---|
| [`README.md`](README.md) | Workspace index, authority, catalog, and artifact rules | Current |
| [`PHASE_0_FOUNDATION.md`](PHASE_0_FOUNDATION.md) | Phase 0 terminology, decision, contradiction, and evidence-routing foundation | Complete |
| [`CLUSTER_A_FIRST_READINESS.md`](CLUSTER_A_FIRST_READINESS.md) | Connected UF-01/02/03 contract and limited Cluster A decision | Complete |
| [`UF-01_FIRST_LAUNCH_AND_EDUCATION.md`](UF-01_FIRST_LAUNCH_AND_EDUCATION.md) | First launch, resume, replay, education, readiness handoff | Complete for completed stage |
| [`UF-02_PERMISSION_RECOVERY.md`](UF-02_PERMISSION_RECOVERY.md) | Permission denial, Settings recovery, provider boundary, manual fallback | Complete for completed stage |
| [`UF-03_LIBRARY_READINESS.md`](UF-03_LIBRARY_READINESS.md) | Scan, returning-state routing, reconciliation, truthful readiness | Complete for completed stage |
| [`UF-04_BROWSE_AND_SEARCH.md`](UF-04_BROWSE_AND_SEARCH.md) | Browse/Search, recovery, acknowledged playback-start handoff | **Cluster B PASS** |
| [`UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md`](UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) | Queue formation, controls, recovery, reconnect, P1 Paused Restore | **Cluster B PASS** |
| [`UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md`](UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md) | Background/screen-off continuity, system surfaces, notification branch, P1 restore | **Cluster B PASS** |
| [`CLUSTER_B_CONNECTED_REVIEW.md`](CLUSTER_B_CONNECTED_REVIEW.md) | Connected UF-04/05/06 review and final exit decision | **PASS / COMPLETE** |
| [`CLUSTER_B_SIGNOFF_2026-08-18.md`](CLUSTER_B_SIGNOFF_2026-08-18.md) | Product/Design/Android/QA/Privacy/Governance Cluster B sign-off | **PASS** |
| [`CLUSTER_B_STATE_INVENTORY.md`](CLUSTER_B_STATE_INVENTORY.md) | CB-01 through CB-33 normalized states | PASS |
| [`CLUSTER_B_TRACEABILITY_MATRIX.md`](CLUSTER_B_TRACEABILITY_MATRIX.md) | Requirement/state/authority/recovery/handoff traceability | PASS |
| [`CLUSTER_C_SOURCE_AUTHORITY.md`](CLUSTER_C_SOURCE_AUTHORITY.md) | English MSM2/GitHub authority and Cluster C scope boundary | PASS |
| [`UF-07_DRIVING_MODE.md`](UF-07_DRIVING_MODE.md) | Manual/P1 entry, Playing/Paused/No Track, controls, Exit, and handoffs | **Cluster C PASS** |
| [`CLUSTER_C_STATE_INVENTORY.md`](CLUSTER_C_STATE_INVENTORY.md) | CC-01 through CC-18 normalized Driving Mode states | PASS |
| [`CLUSTER_C_TRACEABILITY_MATRIX.md`](CLUSTER_C_TRACEABILITY_MATRIX.md) | FR-04/FR-07/US-04/ADR/state/safety traceability | PASS |
| [`CLUSTER_C_CONNECTED_REVIEW.md`](CLUSTER_C_CONNECTED_REVIEW.md) | Cluster C connected review and User Flow exit decision | **PASS / COMPLETE** |
| [`PHASE_1_FIRST_READINESS.md`](PHASE_1_FIRST_READINESS.md) | Controlled Phase 1 gate record | Historical stage record |
| [`PHASE_1_COMPLETION_CHECKLIST.md`](PHASE_1_COMPLETION_CHECKLIST.md) | Evidence-based pass/open checklist | **Cluster B formal exit PASS; later flows open** |

## Required flow catalog

| ID | Flow | Priority | Status |
|---|---|---:|---|
| UF-01 | First launch and education | P0 | Complete |
| UF-02 | Permission denial and Settings recovery | P0 | Complete for completed stages |
| UF-03 | Local scan and library readiness | P0 | Complete for completed stages |
| UF-04 | Browse and search local music | P0 | **Complete — Cluster B PASS** |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | **Complete — Cluster B PASS** |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | **Complete — Cluster B PASS** |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | **Complete — Cluster C PASS** |
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
4. Mark coverage level explicitly. `FR-01` is complete at User Flow specification level across UF-01 through UF-04; `FR-02` is complete across UF-05 and UF-06. Cluster B TS-01/02/06/08 evidence is closed.
5. Preserve sources of truth: Android permission state for access; MediaStore for accessible media existence/identity; a derived/rebuildable local index; playback service/MediaSession for active playback.
6. Preserve the UF-05 queue contract: manual selection replaces the active queue with the visible local track collection in visible order only after service acknowledgement; no append/wrap/repeat/shuffle/playlist editing is invented.
7. Keep P0 active service continuity distinct from P1 Paused Restore; restored playback always requires explicit Resume.
8. Preserve the UF-06 system-surface contract: notification, lock screen, Bluetooth/headset, vehicle media keys, and returning UI are controllers for one MediaSession.
9. Keep permission/provider failure states distinct and preserve non-coercive recovery.
10. Do not add account, backend, sync, catalog, streaming, upload/download, Android Auto, iOS, or v1.1 scope.
11. Use the current GitHub ADR register for ADR status.
12. Use CB-01 through CB-33 as the canonical shared Cluster B state identities.
13. Use CC-01 through CC-18 as the canonical Cluster C state identities; preserve manual P0 entry, suggestion-only P1 Bluetooth entry, one-layer controls, immediate Exit, and service-owned playback truth.
14. Do not claim later-flow, implementation, stationary/in-car QA, or final Issue #3 approval until corresponding evidence is recorded.

## Workflow

1. Maintain requirement-to-flow traceability and Markdown/Mermaid parity.
2. Preserve the completed Cluster B evidence/sign-off records as immutable historical closeout artifacts.
3. Preserve the completed Cluster C UF-07 records as the controlled Driving Mode User Flow contract.
4. Proceed to **Cluster D / UF-08, UF-09, and UF-10 Voice flows**, then complete UF-11 through UF-13 in sequence.
5. Produce any later Technical Spike evidence required by those flows, including TS-03 where applicable.
6. Keep ADR references synchronized and route material architecture changes through Issue #16/new ADRs.
7. Obtain final issue-level cross-role approval after the complete UF-01–UF-13 set is finished.
8. Close Issue #3 only when its full acceptance criteria are satisfied.
