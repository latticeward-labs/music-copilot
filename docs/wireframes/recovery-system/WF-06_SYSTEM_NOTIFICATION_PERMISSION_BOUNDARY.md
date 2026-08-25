# WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY — Notification permission and visibility boundary

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.svg](WF-06_SYSTEM_NOTIFICATION_PERMISSION_BOUNDARY.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-06 / UF-12 |
| Canonical State IDs | `SYS-03, CB-30` |
| System-only State IDs | `SYS-03`, `CB-30` |
| Golden Scenario | GS-03 recovery |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** Only after acknowledged playback/background-control value exists.
- **System/coordinator handoff:** Android permission/visibility result controls surface availability, not playback truth.
- **Playback Service behavior:** Playback and MediaSession continue when notification access is denied where platform policy permits.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Granted enables the surface; denied returns to app controls with truthful visibility status.
- **Retry policy:** Settings recovery is explicit and outside Driving Mode; no repeated prompt.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Notification content follows approved media-surface privacy; denial is coarse state only.
- **Driving Mode:** No permission or Settings setup surface is opened in active Driving Mode.
- **TalkBack/reading order:** Notification purpose → system handoff → result → Continue with manual playback.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
