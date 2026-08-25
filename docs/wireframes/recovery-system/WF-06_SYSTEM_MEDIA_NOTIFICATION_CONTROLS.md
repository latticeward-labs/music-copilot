# WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS — Media Notification controls

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.svg](WF-06_SYSTEM_MEDIA_NOTIFICATION_CONTROLS.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-06 |
| Canonical State IDs | `CB-22, CB-26, CB-29, CB-30, CB-31` |
| System-only State IDs | `CB-22`, `CB-26`, `CB-29`, `CB-30` |
| Golden Scenario | GS-03 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** A valid active MediaSession exposes the platform notification surface.
- **System/coordinator handoff:** Play/Pause/Next/Previous commands travel through Android MediaSession; the surface waits for acknowledgement.
- **Playback Service behavior:** Service owns queue/player/state and refreshes all app/system surfaces from one snapshot.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Acknowledged command updates notification and app. Unavailable surface returns control to the app without fabricating stop.
- **Retry policy:** A pending command is serialized; rejection/timeout refreshes truth before any safe retry.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No notification command or media identity is copied into project evidence.
- **Driving Mode:** System controls do not auto-open Driving Mode.
- **TalkBack/reading order:** Track/state → Previous → Play/Pause → Next → open app; pending/failure announced by the system.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
