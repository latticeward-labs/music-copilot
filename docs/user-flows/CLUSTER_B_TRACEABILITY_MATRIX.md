# Cluster B — Shared Traceability Matrix

**Product:** Music Copilot MVP v1.0  
**Scope:** UF-04 Browse/Search → UF-05 Manual Playback/Queue → UF-06 Background/System Controls  
**Traceability date:** 06 August 2026  
**Tracking:** GitHub Issue #3  
**Shared Traceability decision:** **PASS**  
**Formal Cluster B exit:** **OPEN**

## Authority and controlled inputs

The official English PRD/MVP are the product authorities. The current GitHub ADR register is the architecture-status authority; ADR-0001 through ADR-0006 are Accepted. The matrix traces current repository behavior and does not import superseded ADR status from older PDF/RAR snapshots.

Controlled inputs:

- [UF-04 — Browse and Search Local Music](UF-04_BROWSE_AND_SEARCH.md)
- [UF-05 — Manual Playback and Queue Control](UF-05_MANUAL_PLAYBACK_AND_QUEUE_CONTROL.md)
- [UF-06 — Background, Screen-off, and System Controls](UF-06_BACKGROUND_AND_SYSTEM_CONTROLS.md)
- [Cluster B Shared State Inventory](CLUSTER_B_STATE_INVENTORY.md)
- [Cluster B Connected Flow](mermaid/CLUSTER_B_CONNECTED_FLOW.md)
- [Cluster B Connected Review](CLUSTER_B_CONNECTED_REVIEW.md)

## Product requirement to flow/state coverage

| Product requirement / story | Priority | Flow coverage | Canonical states / transitions | Recovery and handoff | Documentation result | Remaining evidence |
|---|---:|---|---|---|---|---|
| US-02 account-free local library precondition and lawful local-media use | P0 | UF-04, with UF-03 upstream | CB-01 through CB-13 | Revoked → UF-02; empty/stale/unknown → UF-03 | PASS | TS-06; Figma; role review |
| PRD/MVP Library browse by Songs, Artists, Albums and Folders; FR-01 completion with UF-01/02/03 | P0 | UF-04 | CB-01 through CB-08; track selection → CB-13 | Query/index recovery; UF-02/03 | PASS at User Flow specification level | TS-06; Issue #15/#16 |
| PRD/MVP local Search with Result, No Result and Error | P0 | UF-04 | CB-09 through CB-13 | Preserve query/library; no remote fallback; UF-02/03 when truth changes | PASS at User Flow specification level | TS-06; ranking/presentation in Issue #15/#16 |
| FR-02 Basic Player and manual controls: Play, Pause, Next, Previous, Seek and Queue | P0 | UF-05 | CB-14 through CB-21 | CB-19 controlled skip; CB-21 reconnect; UF-02/03/04/11 handoffs | PASS at User Flow specification level | TS-02/06/08; numeric T_PREVIOUS; Issue #15/#16 |
| US-03 truthful service-owned playback and synchronized visible state | P0 | UF-05 + UF-06 | CB-14 through CB-31 | Reconnect via CB-21; lifecycle loss only → CB-32/33 | PASS at User Flow specification level | TS-01/02; Figma; role review |
| FR-02 active background/screen-off playback and system controls | P0 | UF-06 | CB-22 through CB-31 | Unsupported/absent surfaces remain truthful; interruptions/routes → UF-11 | PASS at User Flow specification level | TS-01/02; Issue #16 |
| FR-08 queue/current-item/position restoration after active lifecycle loss | P1 | UF-05 + UF-06 | CB-25 → CB-32/33; explicit Resume → CB-14/15 | Invalid snapshot → CB-33 → CB-01/UF-04 | PASS for user-visible contract only | TS-08 representation/migration/error proof |
| FR-03 call, Audio Focus and route recovery boundary | P0, downstream | UF-06 handoff; full behavior belongs to UF-11 | CB-28 route event → UF-11 | UF-11 must define pause/duck/resume and no-speaker-leak policy | Correctly bounded; not complete in Cluster B | UF-11 plus Android/QA evidence |
| Driving Mode entry boundary | P0/P1 hint, downstream | UF-05 handoff; UI behavior belongs to UF-07 | CB-15/16 → UF-07 | Immediate exit/manual return remains UF-07 | Correctly bounded; not complete in Cluster B | UF-07 |
| No account/backend/sync/catalog/streaming/upload/download/Android Auto/Cast/iOS/v1.1 | Locked exclusion | UF-04 + UF-05 + UF-06 | Applies to CB-01 through CB-33 | No alternate out-of-scope recovery | PASS | Governance review remains open |

## Screen and surface coverage

| Screen / surface | Flow | States | Coverage |
|---|---|---|---|
| S-04 Home / Library | UF-04 and UF-05 mini-player observation | CB-01 through CB-08, CB-20 | PASS |
| S-05 Search | UF-04 | CB-09 through CB-13 | PASS |
| S-06 Full Player | UF-05 | CB-14 through CB-19, CB-21 | PASS |
| Queue view | UF-05 | CB-18 | PASS |
| Media Notification / system media controls | UF-06 | CB-26, CB-29/30/31 | PASS at specification level |
| Lock-screen controls | UF-06 | CB-24, CB-27/29/31 | PASS at specification level |
| Bluetooth/headset/compatible vehicle media keys | UF-06 | CB-28/29/31 | PASS at specification level; no Android Auto UI |
| Background / UI absent / returning UI | UF-06 | CB-21 through CB-25 | PASS at specification level |
| P1 Paused Restore / unavailable | UF-05 + UF-06 | CB-32/33 | PASS for user-visible contract |

## Canonical state-to-flow transition crosswalk

| State | Source responsibility | Required entry | Success exits | Failure / recovery exits |
|---|---|---|---|---|
| CB-01 | UF-04 | UF-03 Ready or CB-33 recovery | CB-02/03/05/07/09/20 | UF-02 or UF-03 |
| CB-02 | UF-04 | CB-01 Songs | CB-13 | UF-03 / retry |
| CB-03 | UF-04 | CB-01 Artists | CB-04 | retry |
| CB-04 | UF-04 | CB-03 artist | CB-13 | CB-03 / UF-03 |
| CB-05 | UF-04 | CB-01 Albums | CB-06 | retry |
| CB-06 | UF-04 | CB-05 album | CB-13 | CB-05 / UF-03 |
| CB-07 | UF-04 | CB-01 Folders | CB-08 | UF-03 / retry |
| CB-08 | UF-04 | CB-07 folder | CB-13 | CB-07 / UF-03 |
| CB-09 | UF-04 | CB-01 Search | CB-10/11/12 | edit/clear/return |
| CB-10 | UF-04 | CB-09 matches | CB-13 | CB-09 / UF-03 |
| CB-11 | UF-04 | CB-09 no matches | CB-09/10/12 | Browse remains |
| CB-12 | UF-04 | CB-09 query failure | CB-10/11 | retry/edit/CB-01 |
| CB-13 | UF-04 | Track selection | CB-14 | UF-02/03; source context retained |
| CB-14 | UF-04→UF-05 | Valid selection or queue-item request | CB-15/16 | Prior queue/context; CB-19 where applicable |
| CB-15 | UF-05 | Service reports Playing | CB-16/17/18/21/22 or next item | CB-19/21 |
| CB-16 | UF-05 | Service reports Paused | CB-15/17/18/21/22 | CB-19/21 |
| CB-17 | UF-05 | Natural final completion or Next at end | CB-15/16/18/22 | CB-19/21; Library |
| CB-18 | UF-05 | Queue opened | CB-14/15/16 | CB-19; retain actual queue |
| CB-19 | UF-05 | Invalid/corrupt item | CB-15/16/17 | UF-02/03; terminal CB-17 |
| CB-20 | UF-05 | Existing item outside Full Player | CB-15/16/17 | CB-21 or CB-01 |
| CB-21 | UF-05↔UF-06 | Controller entry/loss/return | CB-15/16/17/20/22 | CB-32/33 only if lifecycle absent |
| CB-22 | UF-05→UF-06 | Verified active state plus system transition | CB-23/24/26/27/28 | CB-21 or CB-32/33 |
| CB-23 | UF-06 | UI absent, service active | Remain, CB-26/27/28/21 | Preserve service; CB-31 |
| CB-24 | UF-06 | Screen off/locked | CB-27/29/21 | CB-31; reconnect |
| CB-25 | UF-06 | Task/OEM lifecycle event | CB-23/24/26 if active | CB-32/33 |
| CB-26 | UF-06 | Notification/system surface available | CB-29 and refreshed surfaces | CB-31/21 |
| CB-27 | UF-06 | Lock-screen surface available | CB-29/21 | CB-31 |
| CB-28 | UF-06 | External media key | CB-29 | UF-11 for route/disconnect; CB-31 |
| CB-29 | UF-06 | Supported external command | CB-15/16/17 and refreshed surfaces | refresh/retry; CB-32/33 only if lifecycle lost |
| CB-30 | UF-06 | Applicable notification visibility/permission evaluation | CB-26 or exempt continuity | Not now/Settings; playback remains |
| CB-31 | UF-06 | Surface absent/stale | CB-21/26/27/28 when available | App controller remains recovery |
| CB-32 | UF-05↔UF-06 | Lifecycle lost; usable snapshot | CB-14/15 after explicit Resume; CB-18 | CB-33 |
| CB-33 | UF-05↔UF-06 | No usable restored item | CB-01 / UF-04 | UF-02/03 on access/index failure |

## Authority and ADR traceability

| Concern | Authoritative source | Influencing ADR | Covered states | Result |
|---|---|---|---|---|
| Permission grant/revocation | Android platform permission state | ADR-0001, ADR-0003 | CB-01/13/30/33 | PASS |
| Accessible media identity/existence | MediaStore | ADR-0003 | CB-01 through CB-13, CB-19/32/33 | PASS |
| Browse/Search projection | Derived, rebuildable local index | ADR-0003 | CB-01 through CB-12 | PASS |
| Live player, queue, current item, state and position | Playback Service/MediaSession | ADR-0002 | CB-14 through CB-31 | PASS |
| P1 local restore snapshot | App-owned local data after validation | ADR-0003 | CB-25/32/33 | PASS for user-visible boundary; TS-08 open |
| Native Android/UI and system surfaces | Android platform + controller snapshots | ADR-0001, ADR-0002 | CB-20 through CB-31 | PASS at specification level |
| No backend/account/streaming | Product boundary | ADR-0006 | CB-01 through CB-33 | PASS |
| Replaceable voice provider and six-intent mapper | Not exercised by Cluster B; later UF-08/09/10 | ADR-0004, ADR-0005 | No Cluster B state | Correctly excluded; no false coverage claim |

## Failure and recovery traceability

| Failure class | Detection state | Required recovery | Destination | Status |
|---|---|---|---|---|
| Media access denied/revoked | CB-01/13/19/33 | Stop affected selection; recover permission | UF-02 | PASS |
| Empty/stale/unknown library or moved/deleted item | CB-01/10/13/19 | Reconcile/rebuild; retain truthful context | UF-03 then CB-01/09 | PASS |
| Search No Result | CB-11 | Preserve query; edit/clear/Browse | CB-09/01 | PASS |
| Search execution error | CB-12 | Retry/edit/clear; preserve verified library | CB-09/10/11/01 | PASS |
| Playback start rejected | CB-14 | Preserve previous queue and Browse/Search context | CB-01/09/13 | PASS |
| Corrupt/inaccessible queue item | CB-19 | Bounded forward skip; terminal Paused/Completed if exhausted | CB-15/16/17 or UF-02/03 | PASS |
| Controller disconnect | CB-21 | Reconnect and refresh complete service snapshot | CB-15/16/17/20/22 | PASS |
| System command rejected/stale | CB-29 | Preserve verified state; refresh/safe retry | CB-15/16/17/21 | PASS |
| System surface unavailable | CB-31 | Use app/another controller; never infer playback failure | CB-21/26/27/28 | PASS |
| Active lifecycle lost | CB-25 | Validate local P1 snapshot; never auto-play | CB-32/33 | PASS for user-visible contract |
| Call/Audio Focus/route event | CB-28 | Governed handoff | UF-11 | Boundary PASS; downstream work open |

## Evidence and decision register

| Gate | Owner | Current state | What remains |
|---|---|---|---|
| TS-01 | Android + QA | OPEN | Notification/foreground-media, permission/exemption and OEM evidence |
| TS-02 | Android + QA | OPEN | UI closure/recreation, task removal, service lifetime, reconnect and process-death evidence |
| TS-06 | Android + QA | OPEN | Permission/MediaStore move-delete/reconciliation/rebuild/scale evidence |
| TS-08 | Android | OPEN | Restore representation, validation, migration/error and Paused Restore proof |
| Issue #15 | Product Design | OPEN | Wireframes, hierarchy, presentation, accessibility interaction details |
| Issue #16 | Product + Android | OPEN | Numeric T_PREVIOUS; query, service/controller, lifecycle and persistence mechanics |
| Figma synchronization | Product Design | OPEN | Copy reviewed repository behavior, inventory and matrix into controlled workfile |
| Cross-role sign-off | Product, Design, Android, QA, Privacy | OPEN | Record actual reviewers, dates and decision |
| Connected-flow integration review | Repository review | PASS | Already recorded in CLUSTER_B_CONNECTED_REVIEW.md |
| Shared State Inventory | Repository review | PASS | CB-01 through CB-33 complete |
| Shared Traceability Matrix | Repository review | PASS | Requirement, screen, state, authority, recovery and evidence mappings complete |

## Decision

**PASS — Cluster B now has a formal shared Traceability Matrix with no unowned UF-04/05/06 state, missing connected-flow seam, or unrecorded recovery boundary.**

This decision closes only the shared Traceability documentation gap. It does not grant Figma parity, executed Technical Spike evidence, cross-role approval, formal Cluster B exit, complete Issue #3 acceptance, Wireframe approval, final Software Architecture approval, or implementation approval.
