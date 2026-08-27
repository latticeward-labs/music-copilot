# WF-08_VOICE_FAILED_RETRY — Voice Overlay: Failed / Retry

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-08_VOICE_FAILED_RETRY.svg](WF-08_VOICE_FAILED_RETRY.svg)  
**Package:** `WF-08`  
**Priority:** **P0**  
**Ownership:** App + Voice Coordinator

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-08_VOICE_FAILED_RETRY.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.md)
- **Low-fidelity visual (current `main`):** [WF-08_VOICE_FAILED_RETRY.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-08_VOICE_FAILED_RETRY.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Exactly one explicit Retry; no auto-capture, coercive countdown or third attempt. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** In active Driving Mode, no permission, Settings or consent UI may open; feedback stays brief, one explicit Retry is the maximum, and failure returns to fixed manual controls.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-08 / UF-09 / UF-10 |
| Canonical State IDs | `CD-08, CD-17, CD-18, CD-19` |
| Golden Scenario | GS-04 recovery path |
| Screen owner | UF-08/09/10 · Voice overlay |
| Recovery owner | UF-08 / UF-09 / UF-10 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Retry once after the first settled failure, then return to immediate manual control.
- **Primary action:** Retry once; Manual/Cancel remains available.
- **Visible source of truth:** Typed Voice/session failure; actual playback remains service-owned.
- **Recovery:** Exactly one explicit Retry; no auto-capture, coercive countdown or third attempt.
- **Persistence/recreation:** Retry budget is ephemeral; coarse failure only.

## Navigation

- **Entry:** Timeout/no-match/provider/language/network/audio or execution rejection.
- **Exit:** Retry → second Listening; dismiss/manual → initiating surface; second failure is terminal.
- **Back:** Back returns immediately and exhausts no hidden action.

## Accessibility

- **TalkBack order:** Failed heading → short reason → Retry once → Manual → Cancel/Exit.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Settings recovery is available only outside active Driving Mode.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
