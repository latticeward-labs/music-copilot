# User Flows and State Inventory - MVP v1.0

- **Status:** **COMPLETE / PASS — UF-01 through UF-13, the complete State Inventory, and Stage 2 GS-01 through GS-05 are approved as a source-aligned set**
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Evidence authority:** GitHub repository artifacts only
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)
- **Golden Scenario gate:** **PASS / APPROVED / COMPLETE — 23 August 2026**

This document is the repository index for User Flow artifacts. Every individual flow UF-01 through UF-13 is complete, the phase-wide traceability audit has passed, Issue #3 is closed, and the five Stage 2 Golden Scenario contracts are approved. See the [complete Phase 1 Traceability Matrix](user-flows/PHASE_1_TRACEABILITY_MATRIX.md), [flow-set sign-off](user-flows/PHASE_1_SIGNOFF_2026-08-23.md), and [Stage 2 Golden Scenario index](user-flows/golden-scenarios/README.md).

## Stage 2 - Golden Scenarios - COMPLETE / PASS

**Scenarios:** GS-01 First Launch to Ready Library; GS-02 Find Music and Start Playback; GS-03 Continue Playback Outside the App; GS-04 Drive and Execute a Direct Voice Command; GS-05 Recover Safely from an Interruption  
**Decision date:** 23 August 2026  
**Status:** **PASS / APPROVED / COMPLETE / SOURCE-ALIGNED at scenario-contract level**

The five scenarios compose UF-01 through UF-12 into controlled end-to-end outcomes with explicit US/FR/State/Recovery/Permission/ADR/Technical-Spike mapping. UF-13/FR-09 remains a completed but optional P2 extension and does not block the required set. Stage 2 approval is implementation-independent and does not claim Wireframes, final architecture, production provider/legal enablement, physical three-vehicle execution, Release Candidate or Beta results.

- [Golden Scenario index](user-flows/golden-scenarios/README.md)
- [Stage 2 traceability matrix](user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md)
- [Stage 2 sign-off](user-flows/golden-scenarios/STAGE_2_SIGNOFF_2026-08-23.md)

## Phase 0 foundation - complete

- **Decision:** Approved for User Flow drafting on 01 August 2026
- **Detailed record:** [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- **Boundary:** Phase 0 completed input, scope, state, decision, conflict, terminology, spike, and coverage preparation. It did not approve the final flow set, low-fidelity Wireframes, final Software Architecture, or implementation.
- **ADR governance:** Issue #2 completed on 06 August 2026; ADR-0001 through ADR-0006 are Accepted in the current ADR register.

## Phase 1 - Cluster A First Readiness - complete

**Flows:** UF-01, UF-02, UF-03  
**Decision date:** 01 August 2026  
**Status boundary:** Cluster A is complete. At its 01 August decision, later clusters were still pending; they are now individually complete and the 23 August complete-set approval closes the Issue #3 User Flow gate.

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

FR-01 is complete at User Flow specification level across UF-01 through UF-04. FR-02 is complete across UF-05 and UF-06. FR-03 interruption/route behavior is complete in UF-11. Numeric `T_PREVIOUS` and implementation mechanics remain downstream Issue #16 architecture decisions and are not Cluster B User Flow exit blockers.

## Cluster C - UF-07 - COMPLETE / PASS

**Flow:** UF-07 Driving Mode entry, use, and exit  
**Documentation decision date:** 18 August 2026  
**Formal evidence/sign-off closeout:** 18 August 2026  
**Status:** **COMPLETE / PASS / SOURCE-ALIGNED at User Flow stage**

Cluster C passes source-authority, detailed flow, shared State Inventory, Traceability Matrix, canonical Mermaid parity, connected-flow integration, privacy/safety/accessibility review, Technical Spike applicability, final cross-role sign-off, and formal User Flow exit gates. No Phase 0 Technical Spike is assigned directly as a UF-07 exit blocker; application/device/vehicle execution remains truthfully routed to Issues #9 and #13. Manual entry is P0; the selected-device Bluetooth suggestion is a separable P1 branch and never opens Driving Mode automatically.

### Repository records

- [Cluster C connected review and exit record](user-flows/CLUSTER_C_CONNECTED_REVIEW.md)
- [Cluster C final cross-role sign-off](user-flows/CLUSTER_C_SIGNOFF_2026-08-18.md)
- [Cluster D connected review / exit](user-flows/CLUSTER_D_CONNECTED_REVIEW.md)
- [Cluster D final cross-role sign-off](user-flows/CLUSTER_D_SIGNOFF_2026-08-18.md)
- [Cluster E connected review / exit](user-flows/CLUSTER_E_CONNECTED_REVIEW.md)
- [Cluster E final cross-role sign-off](user-flows/CLUSTER_E_SIGNOFF_2026-08-18.md)
- [UF-11 Interruption and Audio Route](user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md)
- [UF-12 Settings and Privacy](user-flows/UF-12_SETTINGS_PRIVACY.md)
- [Cluster E evidence summary](user-flows/evidence/CLUSTER_E_EVIDENCE_SUMMARY_2026-08-18.md)
- [Cluster C evidence applicability record](user-flows/CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md)
- [Cluster C source authority](user-flows/CLUSTER_C_SOURCE_AUTHORITY.md)
- [Cluster C shared State Inventory](user-flows/CLUSTER_C_STATE_INVENTORY.md)
- [Cluster C Traceability Matrix](user-flows/CLUSTER_C_TRACEABILITY_MATRIX.md)
- [Cluster D connected review / exit](user-flows/CLUSTER_D_CONNECTED_REVIEW.md)
- [Cluster D final cross-role sign-off](user-flows/CLUSTER_D_SIGNOFF_2026-08-18.md)
- [UF-08 Direct Voice](user-flows/UF-08_DIRECT_VOICE.md)
- [UF-09 Ambiguity/Not Found](user-flows/UF-09_AMBIGUITY_NOT_FOUND.md)
- [UF-10 Timeout/Retry](user-flows/UF-10_TIMEOUT_RETRY.md)
- [Cluster D State Inventory](user-flows/CLUSTER_D_STATE_INVENTORY.md)
- [Cluster D Traceability Matrix](user-flows/CLUSTER_D_TRACEABILITY_MATRIX.md)
- [Cluster D evidence summary](user-flows/evidence/CLUSTER_D_EVIDENCE_SUMMARY_2026-08-18.md)
- [TS-03](user-flows/evidence/TS-03_SPEECH_PROVIDER_FEASIBILITY.md)
- [TS-07](user-flows/evidence/TS-07_ARTIST_MATCHING.md)
- [TS-09](user-flows/evidence/TS-09_CONFIDENCE_DEFAULT.md)
- [UF-07 - Driving Mode Entry, Use, and Exit](user-flows/UF-07_DRIVING_MODE.md)
- [UF-07 canonical diagram](user-flows/mermaid/UF-07_DRIVING_MODE.md)
- [UF-07 raw Mermaid source](user-flows/mermaid/UF-07_DRIVING_MODE.mmd)

Cluster C completion is implementation-independent. At its 18 August decision it did not claim later voice/recovery flows; those are now individually complete. It still does not claim Issue #9 implementation, stationary/in-car QA, low-fidelity Wireframes, final Software Architecture, or Issue #3 complete-set approval.

## Cluster D - UF-08 through UF-10 - COMPLETE / PASS

**Flows:** UF-08 Direct Voice; UF-09 Ambiguity/Not Found; UF-10 Timeout/Unavailable/Retry  
**Documentation and executable evidence closeout:** 18 August 2026  
**Status:** **COMPLETE / PASS / SOURCE-ALIGNED at User Flow and executable spike-contract level**

Cluster D passes English-source authority, detailed flow contracts, CD-01 through CD-24 shared State Inventory, Traceability Matrix, canonical Mermaid parity, connected review, privacy/accessibility/safety review, TS-03/TS-07/TS-09 executable reference evidence, and cross-role sign-off. Final workflow run #17 passed build, strict lint, host unit tests, and AOSP API 29/31/34 instrumentation 12/12 on the validated code head.

### Repository records

- [Cluster D connected review and exit](user-flows/CLUSTER_D_CONNECTED_REVIEW.md)
- [Cluster D final cross-role sign-off](user-flows/CLUSTER_D_SIGNOFF_2026-08-18.md)
- [Cluster D source authority](user-flows/CLUSTER_D_SOURCE_AUTHORITY.md)
- [Cluster D shared State Inventory](user-flows/CLUSTER_D_STATE_INVENTORY.md)
- [Cluster D Traceability Matrix](user-flows/CLUSTER_D_TRACEABILITY_MATRIX.md)
- [UF-08 Direct Voice](user-flows/UF-08_DIRECT_VOICE.md)
- [UF-09 Ambiguity/Not Found](user-flows/UF-09_AMBIGUITY_NOT_FOUND.md)
- [UF-10 Timeout/Retry](user-flows/UF-10_TIMEOUT_RETRY.md)
- [Cluster D connected Mermaid](user-flows/mermaid/CLUSTER_D_VOICE_FLOW.md)
- [Cluster D evidence summary](user-flows/evidence/CLUSTER_D_EVIDENCE_SUMMARY_2026-08-18.md)

This completion does not claim production Voice implementation, a selected cloud provider/legal disclosure, physical/OEM/road-noise accuracy, Beta KPIs, or Issue #16 numeric adoption. Unverified speech-provider paths remain Manual Only.

## Cluster E - UF-11 and UF-12 - COMPLETE / PASS

**Flows:** UF-11 Calls, Audio Focus, routes, and corrupt recovery; UF-12 Settings, Bluetooth selection, Privacy, Consent, and Data Clearing  
**Documentation and executable evidence closeout:** 18 August 2026  
**Status:** **COMPLETE / PASS / SOURCE-ALIGNED at User Flow and executable reference-contract level**

Cluster E passes English-source authority, detailed flow contracts, CE-01 through CE-26 shared State Inventory, Traceability Matrix, canonical Mermaid parity, connected review, privacy/accessibility/driving-safety review, TS-04/TS-05/TS-10 executable reference contracts, and cross-role sign-off. Workflow run #21 passed build, strict lint, host unit tests, and AOSP API 29/31/34 instrumentation 12/12 on code head `8c0cddd298932f7499a69b0ee0ea391ea46583e4`.

### Repository records

- [Cluster E connected review and exit](user-flows/CLUSTER_E_CONNECTED_REVIEW.md)
- [Cluster E final cross-role sign-off](user-flows/CLUSTER_E_SIGNOFF_2026-08-18.md)
- [Cluster E source authority](user-flows/CLUSTER_E_SOURCE_AUTHORITY.md)
- [Cluster E shared State Inventory](user-flows/CLUSTER_E_STATE_INVENTORY.md)
- [Cluster E Traceability Matrix](user-flows/CLUSTER_E_TRACEABILITY_MATRIX.md)
- [UF-11 Interruption and Audio Route](user-flows/UF-11_INTERRUPTION_AUDIO_ROUTE.md)
- [UF-12 Settings and Privacy](user-flows/UF-12_SETTINGS_PRIVACY.md)
- [Cluster E connected Mermaid](user-flows/mermaid/CLUSTER_E_RECOVERY_CONTROL_FLOW.md)
- [Cluster E evidence summary](user-flows/evidence/CLUSTER_E_EVIDENCE_SUMMARY_2026-08-18.md)
- [TS-04 Audio Focus policy](user-flows/evidence/TS-04_AUDIO_FOCUS_POLICY.md)
- [TS-05 Route safety](user-flows/evidence/TS-05_ROUTE_SAFETY.md)
- [TS-10 Retention and deletion](user-flows/evidence/TS-10_RETENTION_DELETION.md)

This completion does not claim production implementation, physical Pixel/Samsung/Xiaomi call/route behavior, audible speaker-leak proof, any vehicle/head unit, provider deletion/legal completion, or exact retention durations. Those remain Issues #8/#12/#13/#16.

## UF-13 - optional P2 scope - COMPLETE / PASS

**Flow:** Like and Liked Songs  
**Documentation and executable evidence closeout:** 19 August 2026  
**Status:** **COMPLETE / PASS / SOURCE-ALIGNED at User Flow and executable reference-contract level**

UF-13 passes English-source authority, FR-09/S-09/D-027 traceability, UL-01 through UL-14 State Inventory, canonical Mermaid parity, local Like/MediaStore ownership separation, Empty/Ready and stale-item reconciliation, service-owned playback handoff, privacy/accessibility review, eight host reference tests, and cross-role sign-off. Workflow run #25 passed build, host unit tests, strict lint, artifact creation, and AOSP API 29/31/34 instrumentation jobs on validated code head `9134e90e923343d3551258cbb3c756ae4cc7db2e`.

### Repository records

- [UF-13 connected review and exit](user-flows/UF-13_CONNECTED_REVIEW.md)
- [UF-13 final cross-role sign-off](user-flows/UF-13_SIGNOFF_2026-08-19.md)
- [UF-13 source authority](user-flows/UF-13_SOURCE_AUTHORITY.md)
- [UF-13 detailed flow](user-flows/UF-13_LIKED_SONGS.md)
- [UF-13 State Inventory](user-flows/UF-13_STATE_INVENTORY.md)
- [UF-13 Traceability Matrix](user-flows/UF-13_TRACEABILITY_MATRIX.md)
- [UF-13 canonical Mermaid](user-flows/mermaid/UF-13_LIKED_SONGS.md)
- [UF-13 evidence summary](user-flows/evidence/UF-13_EVIDENCE_SUMMARY_2026-08-19.md)

This completion does not claim production storage/UI/Media3 implementation, final sorting/schema/migration, physical-device evidence, Sprint capacity inclusion, Wireframes, final Software Architecture, or complete-set Issue #3 approval.

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
| UF-08 | Direct Voice command | P0 | **Complete — Cluster D PASS** | Listening, Processing, Executed, Failed |
| UF-09 | Ambiguous or missing artist | P0 | **Complete — Cluster D PASS** | Confirming, confirmed execution, not found |
| UF-10 | Voice timeout and retry | P0 | **Complete — Cluster D PASS** | one retry, manual fallback, playback preserved |
| UF-11 | Calls, Audio Focus and route changes | P0 | **Complete — Cluster E PASS** | deterministic pause/duck/recovery; no unintended speaker output |
| UF-12 | Settings, privacy and data clearing | P0 control surface | **Complete — Cluster E PASS** | Updated, cleared, consent-aware states |
| UF-13 | Liked Songs | P2 | **Complete — UF-13 PASS** | Empty, Ready, playback acknowledged, missing item reconciled; removable from schedule |

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

- [x] Every PRD/MVP P0 requirement maps to at least one completed User Flow across the full UF-01–UF-13 set.
- [x] Every flow includes happy, empty, error and recovery behavior where applicable across the full set.
- [x] Voice certainty, ambiguity, timeout, retry and not-found behavior are covered.
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
- [x] UF-08/UF-09/UF-10 direct, ambiguity/not-found, timeout/unavailable, one-retry, manual fallback, and privacy behavior are complete.
- [x] Cluster D source authority, CD-01 through CD-24 State Inventory, Traceability, Mermaid parity, and connected review PASS.
- [x] TS-03/TS-07/TS-09 reference implementations and tests PASS; build/lint/host and AOSP API 29/31/34 12/12 PASS.
- [x] Cluster D Product/Design/Android-contract/QA-testability/Privacy/Governance sign-off PASS.
- [x] **Formal Cluster D User Flow exit PASS / COMPLETE / SOURCE-ALIGNED.**
- [x] UF-11 call/focus/route/corrupt recovery and UF-12 Settings/Bluetooth/privacy/consent/clearing behavior are complete.
- [x] Cluster E source authority, CE-01 through CE-26 State Inventory, Traceability, Mermaid parity, and connected review PASS.
- [x] TS-04/TS-05/TS-10 reference contracts PASS; build/lint/host and AOSP API 29/31/34 12/12 PASS.
- [x] Cluster E Product/Design/Android-contract/QA-testability/Privacy/Governance sign-off PASS.
- [x] **Formal Cluster E User Flow exit PASS / COMPLETE / SOURCE-ALIGNED.**
- [x] UF-13 Like/Liked Songs Empty/Ready, playback acknowledgement, missing-item reconciliation, local ownership, privacy/accessibility, and P2 removability are complete.
- [x] UF-13 source authority, UL-01 through UL-14 State Inventory, Traceability, Mermaid parity, connected review, and cross-role sign-off PASS.
- [x] UF-13 eight host reference tests plus build/lint and AOSP API 29/31/34 jobs PASS in workflow #25.
- [x] **Formal UF-13 User Flow exit PASS / COMPLETE / SOURCE-ALIGNED.**
- [x] GS-01 through GS-05 are approved with paired Markdown/Mermaid artifacts and complete recovery/authority boundaries.
- [x] Stage 2 maps every P0 User Story and FR plus separable P1 contracts; P2 UF-13 remains explicitly non-blocking.
- [x] P0/P1/P2 scope is explicit across the complete UF-01–UF-13 flow set.
- [x] Product/Design/Android-contract/QA-testability/Privacy/Governance complete-set approval is recorded in `PHASE_1_SIGNOFF_2026-08-23.md`.
- [x] Issue #2 and the ADR register align: ADR-0001 through ADR-0006 are Accepted.

## Artifact links

- [Phase 0 Foundation](user-flows/PHASE_0_FOUNDATION.md)
- [Phase 1 complete Traceability Matrix](user-flows/PHASE_1_TRACEABILITY_MATRIX.md)
- [Phase 1 complete flow-set sign-off](user-flows/PHASE_1_SIGNOFF_2026-08-23.md)
- [Stage 2 Golden Scenario index](user-flows/golden-scenarios/README.md)
- [Stage 2 Traceability Matrix](user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md)
- [Stage 2 sign-off](user-flows/golden-scenarios/STAGE_2_SIGNOFF_2026-08-23.md)
- [Cluster A - First Readiness](user-flows/CLUSTER_A_FIRST_READINESS.md)
- [Cluster B connected review / final exit](user-flows/CLUSTER_B_CONNECTED_REVIEW.md)
- [Cluster B final cross-role sign-off](user-flows/CLUSTER_B_SIGNOFF_2026-08-18.md)
- [Cluster C connected review / exit](user-flows/CLUSTER_C_CONNECTED_REVIEW.md)
- [Cluster C final cross-role sign-off](user-flows/CLUSTER_C_SIGNOFF_2026-08-18.md)
- [Cluster C evidence applicability](user-flows/CLUSTER_C_EVIDENCE_APPLICABILITY_2026-08-18.md)
- [UF-07 Driving Mode](user-flows/UF-07_DRIVING_MODE.md)
- [Cluster C State Inventory](user-flows/CLUSTER_C_STATE_INVENTORY.md)
- [Cluster C Traceability Matrix](user-flows/CLUSTER_C_TRACEABILITY_MATRIX.md)
- [UF-13 connected review / final exit](user-flows/UF-13_CONNECTED_REVIEW.md)
- [UF-13 final cross-role sign-off](user-flows/UF-13_SIGNOFF_2026-08-19.md)
- [UF-13 detailed flow](user-flows/UF-13_LIKED_SONGS.md)
- [UF-13 evidence summary](user-flows/evidence/UF-13_EVIDENCE_SUMMARY_2026-08-19.md)
- [Cluster B evidence index](user-flows/evidence/README.md)
- [TS-01](user-flows/evidence/TS-01_NOTIFICATION_AND_FOREGROUND_MEDIA.md)
- [TS-02](user-flows/evidence/TS-02_LIFECYCLE_BOUNDARIES.md)
- [TS-06](user-flows/evidence/TS-06_MEDIASTORE_RECONCILIATION.md)
- [TS-08](user-flows/evidence/TS-08_PAUSED_RESTORE.md)

Figma is not a User Flow source, dependency, or exit criterion. GitHub is the canonical evidence authority for this stage.
