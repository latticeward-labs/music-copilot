# WF-06_SYSTEM_LOCK_SCREEN_CONTROLS — Lock-screen media controls

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.svg](WF-06_SYSTEM_LOCK_SCREEN_CONTROLS.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-06 |
| Canonical State IDs | `CB-24, CB-27, CB-29, CB-31` |
| System-only State IDs | `CB-24`, `CB-27`, `CB-29` |
| Golden Scenario | GS-03 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** Device locks or screen turns off while a valid MediaSession exists.
- **System/coordinator handoff:** Android renders lock-screen controls; the app does not invent or own this screen.
- **Playback Service behavior:** Playback continues from service truth; commands remain pending until acknowledgement.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Unlock/app return reconnects to the complete current snapshot; surface absence is not playback failure.
- **Retry policy:** Rejected/system-unavailable command refreshes state and restores reachable app controls.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Platform surface follows approved metadata policy; no lock state or media identity telemetry.
- **Driving Mode:** Lock-screen surface is never represented as an in-app Driving destination.
- **TalkBack/reading order:** Android owns focus order and labels; app documents only handoff, command and return.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
