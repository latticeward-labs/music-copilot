# MVP v1.0 — Delivery Roadmap

> Derived only from [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md). Pre-build gates improve sequencing and do not add product scope or extend the locked 10-week implementation timebox automatically.

## Pre-build approval gates

| Order | Issue | Deliverable | Gate |
|---:|---:|---|---|
| 1 | #1 | PRD v1.0 / MVP v1.0 scope lock | **Done** |
| 2 | #2 | Preliminary technical baseline and ADR-0001…0006 review | Every ADR Accepted or Revised; open assumptions recorded |
| 3 | #3 | User Flows and complete state inventory | Every P0 requirement has success, failure and recovery paths |
| 4 | #15 | Low-fidelity Wireframes | No missing P0 screen, state or navigation rule |
| 5 | #16 | Software Architecture baseline | Ownership, lifecycle, dependencies and critical flows approved |
| 6 | #17 | High-fidelity UI system and clickable prototype | Handoff approved; golden scenario testable |
| 7 | #4 | Android foundation and CI | Clean checkout builds on two devices |

Issue #4 and Issue #17 may overlap after #16. UI-heavy feature implementation still requires the relevant #17 handoff.

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
