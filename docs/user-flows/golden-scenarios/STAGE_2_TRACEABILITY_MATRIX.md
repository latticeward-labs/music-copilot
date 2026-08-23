# Stage 2 - Golden Scenario Traceability Matrix

**Product:** Music Copilot MVP v1.0  
**Decision date:** 23 August 2026  
**Scope:** GS-01 through GS-05; P0/P1 Golden Scenario definition  
**Input gate:** [Phase 1 complete-set](../PHASE_1_TRACEABILITY_MATRIX.md) - PASS / COMPLETE  
**Decision:** **PASS / COMPLETE / SOURCE-ALIGNED at scenario-contract level**  
**Connected validation:** [Cross-scenario handoff, recovery and authority audit](CROSS_SCENARIO_VALIDATION_2026-08-23.md) — **PASS**

## Authority and interpretation

The official English `Music_Copilot_PRD_v1_EN_Clarified.pdf` and `Music_Copilot_MVP_v1_EN.pdf`, represented by [`docs/PRD.md`](../../PRD.md) and [`docs/MVP.md`](../../MVP.md), are the product authorities. GitHub `main` controls current User Flow, ADR and Technical Spike status. The Persian/Farsi PRD and MVP are translations and are excluded as product authorities.

Every Golden Scenario composes approved User Flows; none replaces a complete flow, creates a screen, approves Wireframes/Software Architecture, or upgrades a reference-policy result into physical/OEM/vehicle/Beta evidence.

## Complete scenario matrix

| Scenario | Outcome | Priority | Primary / recovery flows | US / FR | Canonical state coverage | Permission / consent | Accepted ADRs | Spike/evidence boundary | Result |
|---|---|---:|---|---|---|---|---|---|---|
| [GS-01](GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) | New user reaches a truthful account-free Ready local library | P0 | UF-01 -> UF-02 as needed -> UF-03; UF-04 handoff | US-01/02; FR-01 readiness portion | S-01..04, CA-01/02, SYS-01/04/05, CB-01 | P-MEDIA; optional JIT P-MIC/C-STT | 0001..0006 | TS-06 PASS; TS-03 provider-policy boundary | PASS |
| [GS-02](GS-02_FIND_MUSIC_AND_START_PLAYBACK.md) | User finds accessible local music and starts one acknowledged service-owned queue | P0 | UF-04 -> UF-05; UF-02/03 recovery | US-02; FR-01 complete; FR-02 start | CB-01..21 as applicable; primary CB-01/02-10/13/14/15 | P-MEDIA; explicit P-SETTINGS recovery | 0001/0002/0003/0006 | TS-02/06 PASS; TS-08 restore-only boundary | PASS |
| [GS-03](GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md) | Background/screen-off/system surfaces converge on one MediaSession | P0 + P1 restore | UF-05 -> UF-06; UF-11 handoff | US-03; FR-02; FR-08 | CB-15..33 | contextual P-NOTIF; P-SETTINGS; restored-item P-MEDIA | 0001/0002/0003/0006 | TS-01/02/08 PASS; OEM/vehicle release evidence downstream | PASS |
| [GS-04](GS-04_DRIVE_AND_EXECUTE_DIRECT_VOICE.md) | Explicit Driving Mode + mic tap executes one direct NEXT command | P0 + optional P1 hint | UF-07 -> UF-08; UF-09/10/11/12 recovery | US-04..07; FR-04/05/06/07 | CC-01..18, CD-01..24 | JIT P-MIC; C-STT; optional P-BT; P-SETTINGS outside driving | 0001..0006 | TS-03/07/09 policy/reference PASS; TS-04/05 adjacent | PASS |
| [GS-05](GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md) | Calls/focus/routes settle to conditional Resume or Safe Paused | P0 | UF-11 with UF-05/06; UF-12 control recovery | US-03 stability; FR-03 + FR-02 interaction | CE-01..14; CC-16; CE-18/19 as needed | Platform focus/route truth; no new permission | 0001/0002/0003/0004/0006 | TS-04/05 reference-policy PASS; physical/OEM/vehicle evidence downstream | PASS |

## User Story coverage

| Story | Golden Scenario coverage | Acceptance represented | Result |
|---|---|---|---|
| US-01 | GS-01 | Right-time access explanation, denial recovery and explicit Settings | Covered |
| US-02 | GS-01, GS-02 | Account-free scan/readiness and local browse/search-to-play | Covered |
| US-03 | GS-03, GS-05 | Screen-off/system continuity and interruption/route stability | Covered |
| US-04 | GS-04 | One-layer Driving Mode with essential controls, mic and immediate Exit | Covered |
| US-05 | GS-04 | Certain direct command executes once with short acknowledged response | Covered |
| US-06 | GS-04 recovery | One bounded confirmation; no guessed execution | Covered |
| US-07 | GS-04 recovery | One explicit Retry, then immediate manual fallback | Covered |

No P0 User Story is orphaned.

## Functional Requirement coverage

| Requirement | Golden Scenario coverage | Stage 2 result |
|---|---|---|
| FR-01 local scan and track/album/artist/folder display | GS-01 + GS-02 | Complete |
| FR-02 background/screen-off/MediaSession playback | GS-02 + GS-03; GS-05 interaction | Complete |
| FR-03 calls, disconnect and route changes | GS-05 | Complete |
| FR-04 Driving Mode information/four controls | GS-04 | Complete |
| FR-05 mic, duck, STT and restricted mapping | GS-04 | Complete |
| FR-06 certain execution / ambiguous confirmation | GS-04 primary + recovery | Complete |
| FR-07 manual entry / selected-Bluetooth suggestion | GS-04 | Complete at User Flow level; P1 branch remains removable |
| FR-08 queue/position after UI closes | GS-03 | Complete; P1 Paused Restore remains distinct from P0 continuity |
| FR-09 Like / Liked Songs | Not a required Golden Scenario | P2/removable; UF-13 remains approved and may be added as an optional extension without reopening P0/P1 Stage 2 |

All P0 requirements and both P1 contracts are represented. Excluding P2 FR-09 from the required five does not weaken the MVP critical path or invalidate completed UF-13.

## User Flow coverage

| Flow | Scenario role | Result |
|---|---|---|
| UF-01 | GS-01 primary | Covered |
| UF-02 | GS-01/02 recovery | Covered |
| UF-03 | GS-01 primary; GS-02 recovery | Covered |
| UF-04 | GS-02 primary; GS-01 handoff | Covered |
| UF-05 | GS-02 primary; GS-03/05 entry state | Covered |
| UF-06 | GS-03 primary; GS-05 synchronized surfaces | Covered |
| UF-07 | GS-04 primary | Covered |
| UF-08 | GS-04 primary | Covered |
| UF-09 | GS-04 mandatory ambiguity/not-found recovery | Covered |
| UF-10 | GS-04 mandatory timeout/unavailable/retry recovery | Covered |
| UF-11 | GS-05 primary; GS-03/04 handoff | Covered |
| UF-12 | GS-04/05 explicit outside-driving setup/recheck recovery | Covered |
| UF-13 | Optional P2 extension, not part of the required five | Explicitly non-blocking |

## Recovery completeness

| Seam | Owning scenario / flow | Safe terminal rule |
|---|---|---|
| Startup, permission, Settings and scan | GS-01 / UF-01..03 | Retry, Not now, explicit Settings/recheck, truthful Empty/manual library |
| No result, query/index error, stale selection | GS-02 / UF-02..05 | Preserve query/source/prior queue; reconcile; no network fallback |
| UI/controller/system-surface/lifecycle loss | GS-03 / UF-05/06 | Complete snapshot reconnect; surface absence never fabricates stop; valid restore is Paused only |
| Voice ambiguity/not-found | GS-04 / UF-09 | One confirmation or safe terminal manual state; never guess |
| Voice timeout/provider/permission | GS-04 / UF-10/12 | One Retry; Manual Only; setup only outside active Driving Mode |
| Call/focus/route/corrupt item | GS-05 / UF-11 | Four-guard Resume, Safe Paused, immediate disconnect pause, bounded skip |

Every meaningful primary-path failure has a linked recovery owner and a non-crashing terminal state. None requires account, backend, streaming, Android Auto, Cast, iOS or v1.1 scope.

## Diagram parity and artifact audit

| Scenario | Markdown record | Raw Mermaid | Logical parity |
|---|---|---|---|
| GS-01 | Present | Present | PASS |
| GS-02 | Present | Present | PASS |
| GS-03 | Present | Present | PASS |
| GS-04 | Present | Present | PASS |
| GS-05 | Present | Present | PASS |
| Connected GS-01..05 validation | Present | Present | PASS |

## Stage 2 decision

**PASS / COMPLETE / SOURCE-ALIGNED.** The five required Golden Scenarios are defined, cross-scenario handoffs/recovery/authority have passed the connected validation, and the set is traceable to every P0/P1 User Story and Functional Requirement, connected to explicit State/Recovery/Permission/ADR/Spike contracts, and bounded against unsupported evidence claims. Proceed to low-fidelity Wireframes (Issue #15) and final Software Architecture (Issue #16) as separate gates.

