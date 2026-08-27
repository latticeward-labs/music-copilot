# WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY — Wired/headset disconnect and reconnect

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.svg](WF-06_SYSTEM_WIRED_HEADSET_ROUTE_RECOVERY.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-11 / UF-07 |
| Canonical State IDs | `CC-16, CE-07, CE-08, CE-09` |
| System-only State IDs | `CC-16`, `CE-07`, `CE-08`, `CE-09` |
| Golden Scenario | GS-05 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| Terminal class | **SAFE_PAUSED** |

## Ownership boundary

- **Trigger / app state:** Wired/headset route disconnects or reconnects.
- **System/coordinator handoff:** Android route change invalidates the previous route generation immediately.
- **Playback Service behavior:** Service pauses at disconnect, cancels Voice and prevents speaker leakage.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Reconnect rechecks route but never auto-resumes; explicit Play may resume after acknowledgement.
- **Retry policy:** Manual Play is the only resume attempt; a new loss returns immediately to Safe Paused.
- **Mandatory safe terminal:** **SAFE_PAUSED**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No route/device identity or address is logged.
- **Driving Mode:** Driving Mode remains one layer with Safe Paused, manual Play and Exit.
- **TalkBack/reading order:** Route disconnected → Safe Paused → route available → explicit Play/Exit.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
