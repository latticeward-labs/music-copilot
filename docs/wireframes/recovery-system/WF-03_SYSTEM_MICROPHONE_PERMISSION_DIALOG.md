# WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG — Android microphone permission dialog

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.svg](WF-03_SYSTEM_MICROPHONE_PERMISSION_DIALOG.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-02 / UF-08 / UF-10 |
| Canonical State IDs | `SYS-02, S-03, CA-02, CD-21` |
| System-only State IDs | `SYS-02` |
| Golden Scenario | GS-04 recovery |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** The user explicitly taps Mic or chooses Voice practice; never on app or Driving Mode entry.
- **System/coordinator handoff:** App launches the Android-owned microphone dialog after purpose text.
- **Playback Service behavior:** Playback remains service-owned and usable; capture starts only after a fresh Granted result.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Granted continues capability check. Denied returns to manual controls; permanent denial offers Settings only outside Driving Mode.
- **Retry policy:** Requestable denial may retry. No automatic dialog loop and no capture during Retry choice.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Raw audio/transcript is absent before grant and prohibited from evidence/telemetry.
- **Driving Mode:** In Driving Mode, denial returns immediately to fixed manual controls and Exit.
- **TalkBack/reading order:** Mic purpose → Continue/Not now → Android dialog → returned result → manual controls.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
