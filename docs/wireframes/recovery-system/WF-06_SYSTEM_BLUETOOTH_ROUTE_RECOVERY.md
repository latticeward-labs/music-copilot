# WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY — Bluetooth route loss and reconnect

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.svg](WF-06_SYSTEM_BLUETOOTH_ROUTE_RECOVERY.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-06 / UF-07 / UF-11 |
| Canonical State IDs | `CB-28, CC-16, CE-07, CE-08, CE-09` |
| System-only State IDs | `CB-28`, `CC-16`, `CE-07`, `CE-08`, `CE-09` |
| Golden Scenario | GS-03 / GS-05 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| Terminal class | **SAFE_PAUSED** |

## Ownership boundary

- **Trigger / app state:** Selected/current Bluetooth output route disconnects or reconnects.
- **System/coordinator handoff:** Android route truth controls output; connection alone never opens Driving Mode.
- **Playback Service behavior:** Service immediately pauses on loss, invalidates the route and prevents speaker fallback leakage.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Reconnect produces Safe Paused. Explicit manual Play revalidates route and waits for service acknowledgement.
- **Retry policy:** No automatic Resume. Failed manual Play stays Safe Paused with Exit/manual controls.
- **Mandatory safe terminal:** **SAFE_PAUSED**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Bluetooth device name, address and stable identity are excluded from telemetry.
- **Driving Mode:** No suggestion/setup during active recovery; fixed Play and Exit remain reachable.
- **TalkBack/reading order:** Bluetooth disconnected → Safe Paused → reconnected → explicit Play/Exit.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
