# WF-06_UI_RECREATION_SERVICE_RECONNECT — UI recreation and Playback Service reconnect

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-06_UI_RECREATION_SERVICE_RECONNECT.svg](WF-06_UI_RECREATION_SERVICE_RECONNECT.svg)  
**Classification:** **App recovery transition**  
**Priority:** **P0 + P1 paused restore**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-05 / UF-06 |
| Canonical State IDs | `CB-21, CB-22, CB-23, CB-25, CB-32, CB-33` |
| System-only State IDs | `CB-22`, `CB-23`, `CB-25` |
| Golden Scenario | GS-03 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK / SAFE_PAUSED** |

## Ownership boundary

- **Trigger / app state:** UI recreation, return from background, task/OEM event or controller loss.
- **System/coordinator handoff:** UI shows bounded Connecting and requests one complete service snapshot; it creates no replacement player.
- **Playback Service behavior:** Service remains authority for item, queue, state, position and supported actions.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Complete snapshot restores truthful Playing/Paused/Completed. Missing lifecycle may use validated P1 Paused Restore only.
- **Retry policy:** Repeated reconnect failure exposes Retry/Back; invalid restore routes to Library and never auto-plays.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK / SAFE_PAUSED**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No personal media snapshot enters telemetry; only coarse connection failure may be approved.
- **Driving Mode:** Driving reconnect keeps Exit enabled and never fabricates metadata or Playing.
- **TalkBack/reading order:** Connecting status → Retry/Back → refreshed player state and actions.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
