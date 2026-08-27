# WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK — Driving Voice one-Retry manual fallback

**Phase:** 5 — Recovery and System-only Transitions  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Diagram:** [WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.svg](WF-08_DRIVING_VOICE_RETRY_MANUAL_FALLBACK.svg)  
**Classification:** **App recovery transition**  
**Priority:** **P0**

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-07 / UF-08 / UF-10 |
| Canonical State IDs | `CC-14, CD-17, CD-18, CD-19, CD-20, CD-24` |
| System-only State IDs | — (app recovery artifact) |
| Golden Scenario | GS-04 recovery |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| Owning State Inventory | [Source](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| Terminal class | **MANUAL_PLAYBACK / SAFE_PAUSED** |

## Ownership boundary

- **Trigger / app state:** The first explicit Voice attempt settles as timeout/no-match/provider/language/network/audio failure.
- **System/coordinator handoff:** Voice releases capture/duck and shows Retry once plus Manual/Exit; no auto-capture.
- **Playback Service behavior:** Underlying Playback Service state remains unchanged unless a command was acknowledged.
- Android/System surfaces are described as transitions. They are never presented as app destinations.

## Recovery and return

- **Return path:** Retry starts attempt two. Any second failure immediately dismisses to fixed manual controls; a new session needs a new mic tap.
- **Retry policy:** Exactly one explicit Retry; no third attempt, countdown coercion or Settings detour.
- **Mandatory safe terminal:** **MANUAL_PLAYBACK / SAFE_PAUSED**
- The UI refreshes from authoritative platform/service state and never reports optimistic success.

## Privacy, accessibility and Driving Mode

- **Privacy:** Only coarse allowlisted failure may be recorded; no audio/transcript/candidate/media identity.
- **Driving Mode:** Manual Previous/Play-Pause/Next/Seek Back and Exit are immediately reachable after failure.
- **TalkBack/reading order:** Failure reason → Retry once → Manual controls → Exit; focus returns to the initiating control.
- State is written and shaped; color is never the only cue. App actions remain at least 48dp where present.

## Review checklist

- [x] Canonical State IDs and ownership are explicit.
- [x] System-only surface is not drawn as an app screen.
- [x] Service-owned playback truth is preserved.
- [x] Return, retry and safe terminal are explicit.
- [x] Privacy, accessibility and Driving restrictions are explicit.
- [x] Recovery ends in Manual Playback or Safe Paused.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
