# MVP v1.1 — 10-Week Roadmap

> Derived from the canonical [PRD v1.1](PRD.md) and [MVP v1.1](MVP.md). If this document conflicts with either source, the canonical source wins.

The timebox is fixed. P0 scope is release blocking. P2 Like and then the P1 Bluetooth hint are the first cuts when delivery slips.

| Week | Deliverable | Exit gate |
|---:|---|---|
| 1 | Figma journeys/foundations, Android seven-module baseline, CI and privacy-safe crash logging | Healthy design baseline and clean build on API 26 plus a current API |
| 2 | Permissions, MediaStore, Room, incremental local-library scan | 5,000-file library without blocking error |
| 3 | Media3 service, queue, persistence, background and notification | Playback continues with screen off |
| 4 | Audio Focus, call/headset/Bluetooth route handling and edge cases | P0 route matrix green |
| 5 | Home/Library, local Search, Full Player and empty/error states | Complete manual flow |
| 6 | Driving Mode and optional selected-Bluetooth suggestion | Tested in a stationary vehicle |
| 7 | Push-to-talk, on-device-first STT/TTS, consent-aware fallback, state machine and five control intents | ≥90% laboratory success on supported Voice sessions |
| 8 | PLAY_ARTIST, fuzzy matching, ambiguity, telemetry and privacy controls | Golden voice scenario complete |
| 9 | Device/vehicle QA, accessibility, regression and all P0 fixes | Zero open P0 |
| 10 | Internal Alpha, signed Beta package, tester guide and rollout | Go/No-Go review for 50-driver Beta |

## Dependency sequence

1. Scope and technical baseline
2. Android foundation and UI design
3. Permissions + local index
4. Playback service + stability
5. Manual surfaces
6. Driving Mode
7. Voice control contract
8. Telemetry and privacy
9. Field QA and accessibility
10. Closed Beta and decision gate

Parallel work is allowed only when dependencies are satisfied. No account, backend, streaming, discovery feed or advanced playlist task may enter the MVP Board.

## Project-board policy

Workflow: `Backlog → Ready → In Progress → Review → Testing → Done`

- **Ready:** acceptance criteria, dependency and owner are clear
- **In Progress:** active implementation with one owner
- **Review:** pull request/design decision awaits review
- **Testing:** merged/approved work is being verified
- **Done:** evidence satisfies Definition of Done
- Maximum active work should protect Player, Voice and Driving P0 delivery
- Closed or superseded out-of-scope work is marked `not planned`, not silently deleted

## Gates

### Prototype gate

10 users can complete the stationary golden scenario.

### Alpha gate

Internal testers verify all P0 scenarios across the Android device matrix.

### Beta entry gate

- Zero open P0
- ≥99% internal serious-crash-free sessions
- Golden scenario on at least 3 vehicles
- Privacy copy approved
- Analytics verified
- Rollback route documented

### Beta decision gate

After 50 qualified drivers use the app naturally for 14 days: GO, ITERATE or STOP using the approved metric thresholds.
