# User Flows and State Inventory - MVP v1.0

- **Status:** In progress — Phase 0 and **Clusters A-C (UF-01 through UF-07) are complete; active stage is Cluster D beginning with UF-08 Direct Command**
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Evidence authority:** GitHub repository artifacts only
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

This document is the repository index for User Flow artifacts. Issue #3 remains open until the complete UF-01 through UF-13 flow set and final issue-level approval are complete.

## Phase 0 foundation - complete

- **Decision:** Approved for User Flow drafting on 01 August 2026
- **Detailed record:** [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- **Boundary:** Phase 0 completed input, scope, state, decision, conflict, terminology, spike, and coverage preparation. It did not approve the final flow set, low-fidelity Wireframes, final Software Architecture, or implementation.
- **ADR governance:** Issue #2 completed on 06 August 2026; ADR-0001 through ADR-0006 are Accepted in the current ADR register.

## Phase 1 - Cluster A First Readiness - complete

**Flows:** UF-01, UF-02, UF-03  
**Decision date:** 01 August 2026  
**Status boundary:** Cluster A is complete. Issue #3 remains open because later User Flow clusters are not yet complete.

### Repository records

- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md)
- [UF-01 - First Launch and Education](user-flows/UF-01_FIRST_LAUNCH_AND_EDUCATION.md)
- [UF-02 - Permission Denial and Settings Recovery](user-flows/UF-02_PERMISSION_RECOVERY.md)
- [UF-03 - Local Scan and Library Readiness](user-flows/UF-03_LIBRARY_READINESS.md)

## Cluster B - UF-04 through UF-06 - COMPLETE / PASS

**Flows:** UF-04 Browse and Search Local Music; UF-05 Manual Playback and Queue Control; UF-06 Background, Screen-off, and System Controls  
**Documentation decision date:** 06 August 2026  
**Formal evidence closeout:** 18 August 2026  
**Status:** **COMPLETE / PASS**

Cluster B passes at User Flow specification, traceability, connected-flow integration, shared State Inventory, shared Traceability, Technical Spike execution/evidence, and cross-role sign-off levels.

### Repository records

- [Cluster B connected review and final exit record](user-flows/CLUSTER_B_CONNECTED_REVIEW.md)
- [Cluster B final cross-role sign-off](user-flows/CLUSTER_B_SIGNOFF_2026-08-18.md)
- [Cluster B shared State Inventory](user-flows/CLUSTER_B_STATE_INVENTORY.md)
- [Cluster B shared Traceability Matrix](user-flows/CLUSTER_B_TRACEABILITY_MATRIX.md)
- [Cluster B canonical connected diagram](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.md)
- [Cluster B raw connected Mermaid source](user-flows/mermaid/CLUSTER_B_CONNECTED_FLOW.mmd)
- [UF-04 - Browse and Search Local Music](user-flows/UF-04_BROWSE_AND_SEARCH.md)
- [UF-05 - Manual Playback and Queue Control](user-flows/UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [UF-06 - Background, Screen-off, and System Controls](user-flows/UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [Cluster B Technical Spike evidence index](user-flows/evidence/README.md)
- [Cluster B retrospective evidence-retention waiver](user-flows/evidence/CLUSTER_B_EVIDENCE_RETENTION_WAIVER_2026-08-18.md)

### Cluster B Technical Spike result

- **TS-01:** PASS — Samsung N01-N10 physical execution + AOSP automated evidence; Pixel/Xiaomi TS-01 physical rows approved N/A/waived.
- **TS-02:** PASS — L01-L12 lifecycle execution and required physical coverage accepted.
- **TS-06:** PASS — M01-M14 MediaStore/reconciliation execution accepted; unavailable historical M12/M13 raw measurements covered by the approved retrospective retention waiver.
- **TS-08:** PASS — R01-R12 real process-loss/Paused-Restore execution accepted.
- **Android review:** PASS — project-owner attestation recorded.
- **QA review:** PASS — project-owner attestation recorded.
- **Product/Design/Privacy/Governance Cluster B sign-off:** PASS.

The immutable result bundles contain manifests, matrices, review sign-offs, execution records, derived state traces, retrospective redacted log evidence, visual-evidence indexes, and artifact Git object IDs. Unavailable historical raw captures are explicitly waived rather than fabricated.

FR-01 is complete at User Flow specification level across UF-01 through UF-04. FR-02 is complete across UF-05 and UF-06. FR-03 interruption/route behavior remains assigned to UF-11. Numeric `T_PREVIOUS` and implementation mechanics remain downstream Issue #16 architecture decisions and are not Cluster B User Flow exit blockers.

## Cluster C - UF-07 - COMPLETE / PASS

**Flow:** UF-07 Driving Mode entry, use, and exit  
**Documentation decision date:** 18 August 2026  
**Formal evidence/sign-off closeout:** 18 August 2026  
**Status:** **COMPLETE / PASS / SOURCE-ALIGNED at User Flow stage**

Cluster C passes source-authority, detailed flow, shared State Inventory, Traceability Matrix, canonical Mermaid parity, connected-flow integration, privacy/safety/accessibility review, Technical Spike applicability, final cross-role sign-off, and formal User Flow exit gates. No Phase 0 Technical Spike is assigned directly as a UF-07 exit blocker; application/device/vehicle execution remains truthfully routed to Issues #9 and #13. Manual entry is P0; the selected-device Bluetooth suggestion is a separable P1 branch and never opens Driving Mode automatically.

### Repository records

- [Cluster C connected review and exit record](user-flows/CLUSTER_C_CONNECTED_REVIEW.md)
- [Cluster C final cross-role sign-off](user-flows/CLUSTER_C_SIGNOFF_2026-08-18.md)
- [Cluster C evidence applicability record](user-flows/CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md)
- [Cluster C source authority](user-flows/CLUSTER_C_SOURCE_AUTHORITY.md)
- [Cluster C shared State Inventory](user-flows/CLUSTER_C_STATE_INVENTORY.md)
- [Cluster C Traceability Matrix](user-flows/CLUSTER_C_TRACEABILITY_MATRIX.md)
- [UF-07 - Driving Mode Entry, Use, and Exit](user-flows/UF-07_DRIVING_MODE.md)
- [UF-07 canonical diagram](user-flows/mermaid/UF-07_DRIVING_MODE.md)
- [UF-07 raw Mermaid source](user-flows/mermaid/UF-07_DRIVING_MODE.mmd)

Cluster C completion is implementation-independent. It does not claim Issue #9 implementation, stationary/in-car QA, low-fidelity Wireframes, final Software Architecture, later voice/recovery flows, or Issue #3 as a whole are complete.

## Active stage — Cluster D

The next active User Flow stage is **Cluster D**, beginning with:

**UF-08 — Direct Voice Command**, followed by UF-09 Ambiguity/Not Found and UF-10 Timeout/Unavailable/Retry.

Cluster C no longer blocks Cluster D drafting/review.

## Required flow catalog

| ID | Flow | Priority | Status | Required terminal states |
|---|---|---:|---|---|
| UF-01 | First launch and education | P0 | Complete | Home Ready, permission recovery, Empty Library, manual fallback |
| UF-02 | Permission denial and Settings recovery | P0 | Complete for completed clusters | Granted, Denied, Permanently Denied, manual fallback |
| UF-03 | Local scan and library readiness | P0 | Complete for completed clusters | Scanning, Empty, Ready, recoverable error/revocation |
| UF-04 | Browse and search local music | P0 | **Complete — Cluster B PASS** | Result, No Result, Error, acknowledged playback start |
| UF-05 | Manual playback and queue control | P0 / P1 restore branch | **Complete — Cluster B PASS** | Playing, Paused, End of Queue, corrupt/inaccessible recovery, controller reconnect, Paused Restore |
| UF-06 | Background, screen-off and system controls | P0 / P1 restore branch | **Complete — Cluster B PASS** | Synchronized background/screen-off playback, system surfaces, controller reconnect, Paused Restore |
| UF-07 | Driving Mode entry, use and exit | P0 / P1 entry hint | **Complete — Cluster C PASS** | Playing, Paused, No Track, immediate exit |
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

- [ ] Every PRD/MVP P0 requirement maps to at least one completed User Flow across the full UF-01–UF-13 set.
- [ ] Every flow includes happy, empty, error and recovery behavior where applicable across the full set.
- [ ] Voice certainty, ambiguity, timeout, retry and not-found behavior are covered.
- [x] Cluster A scope/recovery behavior is complete.
- [x] UF-04 completes Songs/Artists/Albums/Folders browse and local Search behavior.
- [x] UF-05 completes P0 manual Player/Queue behavior and P1 Paused Restore branch.
- [x] UF-06 completes P0 background/system controls and separate P1 Paused Restore branch.
- [x] Cluster B constituent drafting coverage is complete.
- [x] Cluster B canonical connected diagram and integration review pass.
- [x] Cluster B shared State Inventory passes.
- [x] Cluster B shared Traceability Matrix passes.
- [x] **Cluster B Technical Spike execution/evidence and Cluster B cross-role sign-off PASS.**
- [x] **Formal Cluster B exit PASS / COMPLETE.**
- [x] UF-07 manual P0 entry, optional P1 Bluetooth suggestion, Playing/Paused/No Track, one-layer controls, immediate Exit, privacy, accessibility, and downstream handoffs are complete.
- [x] Cluster C source authority, State Inventory, Traceability, Mermaid parity, and connected review PASS.
- [x] Cluster C Technical Spike applicability review records no new UF-07 spike blocker and routes implementation evidence to Issues #9/#13.
- [x] Cluster C Product/Design/Android-contract/QA-testability/Privacy/Governance sign-off PASS.
- [x] **Formal Cluster C User Flow exit PASS / COMPLETE / SOURCE-ALIGNED.**
- [ ] P0/P1/P2 scope is explicit across the complete UF-01–UF-13 flow set.
- [ ] Product and Android reviewers approve the complete Issue #3 flow set and state inventory.
- [x] Issue #2 and the ADR register align: ADR-0001 through ADR-0006 are Accepted.

## Artifact links

- [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md)
- [Cluster B connected review / final exit](user-flows/CLUSTER_B_CONNECTED_REVIEW.md)
- [Cluster B final cross-role sign-off](user-flows/CLUSTER_B_SIGNOFF_2026-08-18.md)
- [Cluster C connected review / exit](user-flows/CLUSTER_C_CONNECTED_REVIEW.md)
- [Cluster C final cross-role sign-off](user-flows/CLUSTER_C_SIGNOFF_2026-08-18.md)
- [Cluster C evidence applicability](user-flows/CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md)
- [UF-07 Driving Mode](user-flows/UF-07_DRIVING_MODE.md)
- [Cluster C State Inventory](user-flows/CLUSTER_C_STATE_INVENTORY.md)
- [Cluster C Traceability Matrix](user-flows/CLUSTER_C_TRACEABILITY_MATRIX.md)
- [Cluster B evidence index](user-flows/evidence/README.md)
- [TS-01](user-flows/evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md)
- [TS-02](user-flows/evidence/TS-02_LIFECYCLE_BOUNDARIES.md)
- [TS-06](user-flows/evidence/TS-06_MEDIASTORE_RECONCILIATION.md)
- [TS-08](user-flows/evidence/TS-08_PAUSED_RESTORE.md)

Figma is not a User Flow source, dependency, or exit criterion. GitHub is the canonical evidence authority for this stage.
