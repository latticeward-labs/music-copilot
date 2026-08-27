# WF-05_SEARCH_ERROR — Search: Error

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-05_SEARCH_ERROR.svg](WF-05_SEARCH_ERROR.svg)  
**Package:** `WF-05`  
**Priority:** **P0**  
**Ownership:** App

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-05_SEARCH_ERROR.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.md)
- **Low-fidelity visual (current `main`):** [WF-05_SEARCH_ERROR.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-05_SEARCH_ERROR.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-05_SEARCH_ERROR.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Retry/edit/clear; use UF-03 only if index truth is actually stale. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-04 |
| Canonical State IDs | `CB-09, CB-12` |
| Golden Scenario | GS-02 |
| Screen owner | UF-04 · Search |
| Recovery owner | UF-03 / UF-04 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Recover a local query failure without invalidating a verified library.
- **Primary action:** Retry; Edit and Clear remain available.
- **Visible source of truth:** Local search repository failure; verified library remains authoritative.
- **Recovery:** Retry/edit/clear; use UF-03 only if index truth is actually stale.
- **Persistence/recreation:** Active query is retained; coarse error only.

## Navigation

- **Entry:** Typed local query/index failure.
- **Exit:** Retry → Results/No Result; Back → verified Library.
- **Back:** Returns to Library; unrelated playback is unchanged.

## Accessibility

- **TalkBack order:** Error heading → concise reason → Retry → Edit/Clear → Back.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No network fallback or Android surface.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
