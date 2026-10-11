# Phase 8 · GS-01..GS-05 Prototype Coverage & Source Traceability

**Issue:** #17 · **Phase:** 8 · **Reference:** English MVP/PRD, frozen 126-state register and Stage-2 Golden Scenarios · **Date:** 11 October 2026.

## Five mandatory primary routes

| GS | Source GS document | Primary event/state sequence (UI vs system labeled) | Success | Owner guard |
|---|---|---|---|---|
| GS-01 | [First Launch](../user-flows/golden-scenarios/GS-01_FIRST_LAUNCH_TO_READY_LIBRARY.md) | S-01 Splash → S-02 Onboarding → S-03 Media purpose → **SYS-01 OS handoff** → S-03 granted → S-04 scanning → CB-01 Ready | Verified local Library | Android permission + MediaStore local index; no fabricated OS dialog |
| GS-02 | [Find Music](../user-flows/golden-scenarios/GS-02_FIND_MUSIC_AND_START_PLAYBACK.md) | CB-01 Ready → CB-09 Search Idle → CB-10 Search Result → CB-14 selection/queue pending → CB-15 Playing | MediaSession acknowledges same queue/current item | MediaStore validity and Playback Service authoritative snapshot |
| GS-03 | [Background](../user-flows/golden-scenarios/GS-03_CONTINUE_PLAYBACK_OUTSIDE_APP.md) | CB-15 → **CB-23 OS background** → **CB-26 native controller** → **CB-29 service-command handoff** → CB-21 controller reconnect → CB-15 | One playback owner, refreshed snapshot | Android/MediaSession native controls; no app-owned notification |
| GS-04 | [Driving Voice](../user-flows/golden-scenarios/GS-04_DRIVE_AND_EXECUTE_DIRECT_VOICE.md) | CB-15 → CC-05 explicit Driving → CD-03 Listening → CD-05 Processing → CD-06 pending Next → CD-07 acknowledged success → CC-05 refreshed Driving | One deterministic NEXT, same Driving layer | one STT attempt (+ max one Retry), one confirmation max, one-sentence TTS non-authoritative |
| GS-05 | [Interruption](../user-flows/golden-scenarios/GS-05_RECOVER_SAFELY_FROM_INTERRUPTION.md) | CB-15 → **CE-07 OS route event** → CE-06 app Safe Paused → **CE-09 OS route reconnect** → CE-06 app Safe Paused → CE-06 *explicit* Play → CB-15 acknowledgment | No auto-resume, no speaker leak implied | system route truth + Playback Service four-guard policy |

System handoffs are **not** mock phone views. The prototype phone displays a boundary annotation only and external events live in the review rail.

## Required recovery branches / branch ownership

| GS | Required recovery evidence | State IDs / view/source | Limits |
|---|---|---|---|
| GS-01 | migration error; denied/requestable/permanently denied; Android Settings return; scanning Empty or recoverable error | S-01, S-03, SYS-01, SYS-04, S-04 | No destructive reset; OS recheck; no scan falsely Ready |
| GS-02 | No Result, Search Error, stale selection, rejected queue, corrupt item, controller unavailable | CB-11, CB-12, CB-13, CB-14, CB-19, CB-21 | Retain query and verified prior queue; no online fallback or looping skip |
| GS-03 | notification unavailable/permission, platform settings, lifecycle loss, Paused Restore/invalid restore | CB-30, CB-31, CB-32, CB-33 | OS owns surfaces; P1 restore only Paused, never optimistic Playing |
| GS-04 | Paused/No Track, verified Play last, safe paused, 1/2 candidate ambiguity, unknown/not-found/unsupported, timeout → one Retry → terminal, provider/mic denial, TTS failure after ack | CC-05/06/07; CD-03/04/05/06/07/09/10/13/14/15/17/19/21/22; CE-06 | Exit immediate; unsupported never approximated; 1 confirmation, max 2 options, no third STT attempt |
| GS-05 | interrupted route/focus, four-guard conditional focus resume, disconnection safe pause, reconnection still Paused, invalid-route manual only | CE-03/06/07/09; CB-15 | Route reconnect alone NEVER resumes; explicit Play must be valid and acknowledged |

## Interactive review shell vs actual UI

The interactive phone surface contains only coordinates of **pre-existing app controls** and PNGs under \`docs/high-fidelity/\`. The external event rail is deliberately **not** the application's interface; it makes backend/system outcomes selectable for source review and repeatable QA.

- PNG ownership and 126 canonical IDs: [Phase-5 register](../high-fidelity/HIFI_STATE_COVERAGE_REGISTER.json).
- Driving/Voice specialist correction priority: [Phase-6 register](../high-fidelity/specialist/phase-6/PHASE_6_REGISTER.json).
- Complete accessibility and system-only handoff: [Phase-7 matrix](../high-fidelity/phase-7/ACCESSIBILITY_PRIVACY_SYSTEM_MATRIX.md).
- Runtime authority: [Software Architecture](../SOFTWARE_ARCHITECTURE.md) and accepted ADRs.
- Route graph: [routes.js](routes.js), DOM interaction runner: [app.js](app.js), UI shell: [index.html](index.html).

## PASS interpretation / outstanding QA

Five Golden Scenario primary routes and listed branches must be connected, with all routes using existing State IDs and all images existing and decoding as 1080×2400. System-only nodes must have no image and only system-simulation transitions. Driving Exit/Manual and Voice one-Retry must hold. The source validator enforces these.

**Not proved by the static prototype:** real Media3, OEM background continuity, Android native dialogs/notifications, 3+ vehicles, actual audio-focus behavior, real STT provider/TTS, Compose 200% font scaling/focus/TalkBack and human approvals. These remain outside this Phase-8 artifact gate.
