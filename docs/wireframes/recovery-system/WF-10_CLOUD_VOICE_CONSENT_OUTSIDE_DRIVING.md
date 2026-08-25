# WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING — Cloud Voice consent outside Driving Mode

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Diagram:** [WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.svg](WF-10_CLOUD_VOICE_CONSENT_OUTSIDE_DRIVING.svg)  
**Classification:** **App/provider boundary recovery**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-02 / UF-10 / UF-12 |
| Canonical State IDs | `SYS-05, CD-23, CE-21, CE-22` |
| System-only State IDs | `SYS-05` |
| Golden Scenario | GS-04 recovery |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK** |

## Ownership boundary

- **Trigger / app state:** An approved network-assisted Voice adapter is selected and complete disclosure exists.
- **System/coordinator handoff:** App shows purpose, processing destination and retention disclosure outside Driving Mode; this is consent, not Android permission.
- **Playback Service behavior:** Decline or missing disclosure keeps Voice Manual Only and never affects local playback.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Accept/Decline/Revoke persists a versioned local decision and returns to Settings/manual playback.
- **Retry policy:** No coercive countdown, preselection or repeated prompt; future change is user-initiated in Settings.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Raw audio/transcript destination and retention must be truthful; no dark pattern.
- **Driving Mode:** Consent UI is prohibited in active Driving Mode; immediately return to manual controls.
- **TalkBack/reading order:** Cloud processing heading → purpose/destination/retention → Accept → Decline → Back.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
