# MVP v1.0 — Delivery Roadmap

- **Version:** MVP v1.0
- **Status:** Current through Issue #16 Phase 10
- **Updated:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md). Pre-build gates improve sequencing and do not add product scope or extend the locked 10-week implementation timebox automatically.

## Pre-build approval gates

| Order | Issue | Deliverable | Current gate |
|---:|---:|---|---|
| 1 | #1 | PRD v1.0 / MVP v1.0 scope lock | **Done** |
| 2 | #2 | Preliminary technical baseline and ADR-0001…0006 review | **Done** |
| 3 | #3 | User Flows and complete state inventory | **Done / Closed — 23 August 2026** |
| 4 | #15 | Low-fidelity Wireframes | **Done / Closed — 27 August 2026** |
| 5 | #16 | Software Architecture baseline | **In Progress — Phase 10 cross-role review PASS; open Blocker/Major 0/0; final acceptance/merge/closeout pending** |
| 6 | #17 | High-fidelity UI system and clickable prototype | Separate downstream gate |
| 7 | #4 | Android foundation and CI | **Blocked until final #16 exit** |

The current architecture baseline is [SOFTWARE_ARCHITECTURE.md](SOFTWARE_ARCHITECTURE.md), with [Phase 10 review evidence](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md). Phase 10 completion does not by itself release Issue #4.

## Locked 10-week implementation and validation plan

| Week | Deliverable | Gate |
|---:|---|---|
| 1 | Repository foundation, CI, architecture conformance, design-token plumbing and crash logging | Healthy build on two devices |
| 2 | MediaStore, Room, incremental scan, permissions and Library foundation | 5,000 files without blocking error |
| 3 | Media3, queue, background playback, notification and system controls | Playback with screen off |
| 4 | Audio Focus, calls, headset, Bluetooth routes and approved persistence work | Edge-case matrix green |
| 5 | Home, Search, Full Player and empty/error states | Complete manual flow |
| 6 | Driving Mode and optional Bluetooth connection suggestion | Stationary-vehicle test |
| 7 | STT/TTS, Voice state machine and five control intents | At least 90% laboratory success |
| 8 | Play Artist, ambiguity handling, Analytics and Privacy controls | Golden scenario complete |
| 9 | Device/vehicle QA, Accessibility and P0 fixes | Zero open P0 |
| 10 | Alpha, Closed Beta package, tester guide and 50-person launch readiness | Go/No-Go review |

## Scope protection

If week 8 slips, simple Like/Liked Songs (P2) and the Bluetooth entry suggestion (P1) are removed first. Player, Voice, Driving Mode and Stability are not removable.

## Execution policy

- Work starts only from an Issue that satisfies the [Definition of Ready](DELIVERY_WORKFLOW.md#definition-of-ready-for-implementation).
- Structural UX changes return to User Flow and Wireframe review.
- Consequential architecture changes require an ADR or a superseding ADR.
- No v1.1 capability may be introduced through an implementation shortcut.
- Issue #4 remains blocked until Issue #16 passes final acceptance, merge and closeout.
- Phase 10 open Minors remain downstream-owned and do not authorize remote speech, skip runtime accessibility validation or fabricate physical OEM/vehicle evidence.