# WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS — Phone Call and Audio Focus interruption

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.svg](WF-06_SYSTEM_PHONE_CALL_AUDIO_FOCUS.svg)  
**Classification:** **Android/System-only transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-11 / UF-07 |
| Canonical State IDs | `CC-16, CE-02, CE-03, CE-04, CE-05, CE-06, CE-10, CE-11` |
| System-only State IDs | `CC-16`, `CE-10` |
| Golden Scenario | GS-05 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| Terminal class | **SAFE_PAUSED** |

## Ownership boundary

- **Trigger / app state:** Transient/permanent Audio Focus loss, call, duck request or focus callback error.
- **System/coordinator handoff:** Android supplies focus/call category only; Voice is canceled and no call identity is read.
- **Playback Service behavior:** Service pauses or ducks. Resume occurs only when all four guards pass and service acknowledges it.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Valid transient gain may return to actual Playing; any failed guard or permanent loss remains Safe Paused with manual Resume.
- **Retry policy:** No hidden auto-retry. Manual Resume rechecks route/focus before command.
- **Mandatory safe terminal:** **SAFE_PAUSED**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** No phone number, contact, call identity or personal media identity is stored.
- **Driving Mode:** Driving surface shows concise Interrupted/Safe Paused and fixed manual controls/Exit.
- **TalkBack/reading order:** Interrupted/Paused status → manual Resume if safe → Exit; no unexpected speech or audio.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
