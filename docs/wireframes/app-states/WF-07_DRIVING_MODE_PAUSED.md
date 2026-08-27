# WF-07_DRIVING_MODE_PAUSED — Driving Mode: Paused

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-07_DRIVING_MODE_PAUSED.svg](WF-07_DRIVING_MODE_PAUSED.svg)  
**Package:** `WF-07`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-07_DRIVING_MODE_PAUSED.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md)
- **Low-fidelity visual (current `main`):** [WF-07_DRIVING_MODE_PAUSED.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** Single fixed-control layer with immediate Exit only; no typing, deep navigation, nested menus, long lists, permission prompts, Settings handoffs or consent UI.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-07 / UF-11 |
| Canonical State IDs | `CC-06, CC-16` |
| Golden Scenario | GS-04 / GS-05 |
| Screen owner | UF-07 · Driving Mode |
| Recovery owner | UF-07 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_C_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Resume only through an explicit acknowledged Play action.
- **Primary action:** Play; Exit remains prominent.
- **Visible source of truth:** MediaSessionService Paused snapshot.
- **Recovery:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio.
- **Persistence/recreation:** No auto-play on entry or recreation.

## Navigation

- **Entry:** Complete snapshot reports Paused/Ended.
- **Exit:** Acknowledged Play → Playing; Exit → prior surface.
- **Back:** Back equals immediate Exit; never auto-resumes.

## Accessibility

- **TalkBack order:** Paused → title/artist → Previous → Play → Next → Seek back → Mic → Exit.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Route/focus safety may disable Play with a textual reason.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
