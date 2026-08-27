# WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG — Android media permission dialog

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.svg](WF-03_SYSTEM_MEDIA_PERMISSION_DIALOG.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-02 / UF-03 |
| Canonical State IDs | `SYS-01, S-03` |
| System-only State IDs | `SYS-01` |
| Golden Scenario | GS-01 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** Local media access is first required after a one-purpose explanation.
- **System/coordinator handoff:** App launches the Android-owned media permission request; the OS result is authoritative.
- **Playback Service behavior:** Existing valid Playback Service state is preserved; no false library readiness is created.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Granted resumes scan/readiness. Denied returns to app recovery with Retry and Not now.
- **Retry policy:** A requestable denial may be retried explicitly; permanent denial uses the separate Settings handoff.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No media name, URI, path or content enters telemetry.
- **Driving Mode:** Never shown as a Driving Mode page.
- **TalkBack/reading order:** Purpose → Continue/Not now → Android dialog → returned result.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
