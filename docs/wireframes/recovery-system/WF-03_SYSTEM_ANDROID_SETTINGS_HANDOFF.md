# WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF — Android Settings handoff and recheck

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.svg](WF-03_SYSTEM_ANDROID_SETTINGS_HANDOFF.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-02 / UF-10 / UF-12 |
| Canonical State IDs | `SYS-04, CE-18, CE-19` |
| System-only State IDs | `SYS-04`, `CE-18` |
| Golden Scenario | GS-01 / GS-05 recovery |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** The user explicitly chooses Open Settings for permanent permission/provider recovery.
- **System/coordinator handoff:** The app leaves through an explicit Android Settings intent and assumes no success.
- **Playback Service behavior:** Active playback is preserved; Settings never becomes playback authority.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** On return, the app rechecks Granted, requestable denial, or still permanent/unavailable as distinct outcomes.
- **Retry policy:** No automatic loop back into Settings; Not now and Back return to manual control.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No settings value or device identity is emitted as telemetry.
- **Driving Mode:** Open Settings is never offered or launched from active Driving Mode.
- **TalkBack/reading order:** Leaving-app notice → Open Settings/Not now → system surface → Checking → result/manual controls.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
