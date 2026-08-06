# User Flows and State Inventory - MVP v1.0

- **Status:** In progress - Phase 0, Cluster A, UF-04 through UF-06, and Cluster B connected review/shared inventory/shared traceability are complete at documentation level
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Evidence authority:** GitHub repository artifacts only
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

This document is the repository index for User Flow artifacts. Until Issue #3 is approved, the complete flow set is not implementation-ready.

## Phase 0 foundation - complete

- **Decision:** Approved for User Flow drafting on 01 August 2026
- **Detailed record:** [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- **Boundary:** Phase 0 completed input, scope, state, decision, conflict, terminology, spike, and coverage preparation. It did not approve the final flow set, low-fidelity Wireframes, final Software Architecture, or implementation.
- **ADR governance:** Issue #2 completed on 06 August 2026; ADR-0001 through ADR-0006 are Accepted in the current ADR register. Dated Phase 0/Cluster A snapshots do not override that current status.

## Phase 1 - Cluster A First Readiness - complete for review

**Flows:** UF-01, UF-02, UF-03  
**Decision date:** 01 August 2026  
**Status boundary:** Cluster A is complete and registered. Issue #3 remains open because UF-07 through UF-13, Cluster B executed evidence synchronization, and final cross-role approval are not complete.

### Repository records

- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md)
- [UF-01 - First Launch and Education](user-flows/UF-01_FIRST_LAUNCH_AND_EDUCATION.md)
- [UF-02 - Permission Denial and Settings Recovery](user-flows/UF-02_PERMISSION_RECOVERY.md)
- [UF-03 - Local Scan and Library Readiness](user-flows/UF-03_LIBRARY_READINESS.md)



### Cluster A exit decision

Cluster A covers first launch, two-slide onboarding, Skip/Replay, contextual media access, Denied/Permanently Denied recovery, Android Settings return and recheck, permission revocation, initial/incremental scanning, Scanning/Empty/Ready/error states, six-command education, and just-in-time microphone access.

The active drafting cluster is **Cluster B: UF-04 Browse/Search, UF-05 Manual Playback, UF-06 Background/System Controls**.

## Cluster B drafting coverage - UF-04 through UF-06 complete

**Flows:** UF-04 Browse and Search Local Music; UF-05 Manual Playback and Queue Control; UF-06 Background, Screen-off, and System Controls  
**Decision date:** 06 August 2026  
**Status boundary:** UF-04, UF-05, and UF-06 each pass at User Flow specification and traceability level. The canonical connected diagram, integration review, normalized shared State Inventory, and shared Traceability Matrix pass. Formal Cluster B exit remains open for executed Technical Spike evidence and cross-role sign-off.

### Repository records

- [Cluster B connected review record](user-flows/CLUSTER_B_CONNECTED_REVIEW.md)
- [Cluster B shared State Inventory](user-flows/CLUSTER_B_STATE_INVENTORY.md)
- [Cluster B shared Traceability Matrix](user-flows/CLUSTER_B_TRACEABILITY_MATRIX.md)
- [Cluster B canonical connected diagram](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.md)
- [Cluster B raw connected Mermaid source](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.mmd)
- [UF-04 - Browse and Search Local Music](user-flows/UF-04_BROWSE_AND_SEARCH.md)
- [UF-04 canonical Mermaid](user-flows/mermaid/UF-04_BROWSE_AND_SEARCH.md)
- [UF-04 raw Mermaid source](user-flows/mermaid/UF-04_BROWSE_AND_SEARCH.mmd)
- [UF-05 - Manual Playback and Queue Control](user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [UF-05 canonical Mermaid](user-flows/mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [UF-05 raw Mermaid source](user-flows/mermaid/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.mmd)
- [UF-06 - Background, Screen-off, and System Controls](user-flows/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [UF-06 canonical Mermaid](user-flows/mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [UF-06 raw Mermaid source](user-flows/mermaid/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.mmd)

UF-04 covers browse/search and the acknowledged playback-start handoff. UF-05 defines deterministic service-owned queue/manual playback behavior and the user-visible P1 Paused Restore branch. UF-06 now defines P0 background/screen-off continuity; synchronized notification, lock-screen, Bluetooth/headset, vehicle media-key, and returning-UI controllers; truthful reconnect and unavailable-surface recovery; the evidence-safe notification permission/exemption branch; and the separate P1 Paused Restore route. FR-01 is complete at User Flow specification level across UF-01 through UF-04. FR-02 is complete at that level across UF-05 and UF-06. FR-03 interruption/route behavior remains UF-11. TS-01, TS-02, TS-06, TS-08, numeric `T_PREVIOUS`, cross-role sign-off, and final Issue #3 approval remain open. The connected-flow integration review and the shared State Inventory/Traceability documentation gates are no longer open.

## Required flow catalog

| ID | Flow | Priority | Status | Required terminal states |
|---|---|---:|---|---|
| UF-01 | First launch and education | P0 | Complete for Cluster A review | Home Ready, permission recovery, Empty Library, manual fallback |
| UF-02 | Permission denial and Settings recovery | P0 | Complete for Cluster A review; TS-01/TS-06 evidence open | Granted, Denied, Permanently Denied, manual fallback |
| UF-03 | Local scan and library readiness | P0 | Complete for Cluster A review; TS-06 evidence open | Scanning, Empty, Ready, recoverable error/revocation |
| UF-04 | Browse and search local music | P0 | Complete at specification/traceability level; TS-06/reviewer evidence open | Result, No Result, Error, acknowledged playback start |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | Complete at specification/traceability level; TS-02/TS-06/TS-08/reviewer evidence and numeric `T_PREVIOUS` open | Playing, Paused, End of Queue, corrupt/inaccessible recovery, controller reconnect, Paused Restore |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | Complete at specification/traceability level; TS-01/TS-02/TS-08/reviewer evidence open | Synchronized background/screen-off playback, system surfaces, controller reconnect, Paused Restore |
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
- [x] UF-04 completes Songs/Artists/Albums/Folders browse and local Search behavior at specification level, with recovery and UF-05 handoff explicit.
- [x] UF-05 completes the P0 manual Player/Queue behavior and the user-visible P1 Paused Restore branch at specification level, with UF-06/UF-11 handoffs explicit.
- [x] UF-06 completes P0 background/screen-off and synchronized system-control behavior plus the separate P1 Paused Restore branch at specification level; FR-02 documentation coverage is complete.
- [x] Cluster B constituent drafting coverage (UF-04/UF-05/UF-06) is complete.
- [x] Cluster B canonical connected diagram and integration review pass; no UF-04/05/06 handoff gap remains.
- [x] Cluster B shared State Inventory passes with CB-01 through CB-33 and all required state fields.
- [x] Cluster B shared Traceability Matrix passes for requirements, screens, states, authority, recovery, handoffs, and open evidence.
- [ ] Cluster B executed spikes and cross-role sign-off remain open.
- [ ] P0/P1/P2 scope is explicit across the complete flow set.
- [ ] Product and Android reviewers approve the complete flow set and state inventory.
- [x] Issue #2 and the ADR register align: ADR-0001 through ADR-0006 are Accepted.

## Artifact links

- [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md) - approved for User Flow drafting
- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md) - complete for review
- [UF-04 - Browse and Search Local Music](user-flows/UF-04_BROWSE_AND_SEARCH.md) - specification/traceability PASS; downstream evidence open
- [UF-05 - Manual Playback and Queue Control](user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md) - specification/traceability PASS; downstream evidence open
- [UF-06 - Background, Screen-off, and System Controls](user-flows/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md) - specification/traceability PASS; TS-01/TS-02/TS-08/reviewer evidence open
- [Cluster B connected review](user-flows/CLUSTER_B_CONNECTED_REVIEW.md) - connected-flow integration plus shared inventory/traceability review PASS; formal exit gates open
- [Cluster B shared State Inventory](user-flows/CLUSTER_B_STATE_INVENTORY.md) - CB-01 through CB-33 PASS
- [Cluster B shared Traceability Matrix](user-flows/CLUSTER_B_TRACEABILITY_MATRIX.md) - complete repository traceability PASS
- [Cluster B connected diagram](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.md) and [raw source](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.mmd) - canonical pair
- Complete UF-07 through UF-13 diagrams, Cluster B role review, and final review date - _to be completed by Issue #3_


## Cluster B Technical Spike execution pack

- [Evidence index](user-flows/evidence/README.md) — protocol PASS; execution BLOCKED until Android build/device evidence exists
- [TS-01](user-flows/evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md), [TS-02](user-flows/evidence/TS-02_LIFECYCLE_BOUNDARIES.md), [TS-06](user-flows/evidence/TS-06_MEDIASTORE_RECONCILIATION.md), [TS-08](user-flows/evidence/TS-08_PAUSED_RESTORE.md)

Figma is not a User Flow source, dependency, or exit criterion. Only GitHub result bundles count as executed spike evidence.
