# WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS — External media keys and controller handoff

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.svg](WF-06_SYSTEM_EXTERNAL_MEDIA_CONTROLS.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-06 / UF-11 |
| Canonical State IDs | `CB-28, CB-29, CB-31` |
| System-only State IDs | `CB-28`, `CB-29` |
| Golden Scenario | GS-03 / GS-05 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** A supported Bluetooth/headset/vehicle media key issues a MediaSession command.
- **System/coordinator handoff:** Android routes the command; no separate app player or queue is created.
- **Playback Service behavior:** The shared service validates, serializes and acknowledges the command.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** All visible surfaces refresh from the authoritative snapshot; unsupported/unavailable returns to app manual controls.
- **Retry policy:** No optimistic duplicate command; safe retry only after refresh.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** External device name/address and media payload are prohibited from telemetry.
- **Driving Mode:** A media-key command never auto-enters Driving Mode.
- **TalkBack/reading order:** System controller labels are platform-owned; app feedback reflects acknowledged state only.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
