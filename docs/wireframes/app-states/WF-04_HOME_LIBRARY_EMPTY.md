# WF-04_HOME_LIBRARY_EMPTY — Home / Library: Empty

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-04_HOME_LIBRARY_EMPTY.svg](WF-04_HOME_LIBRARY_EMPTY.svg)  
**Package:** `WF-04`  
**Priority:** **P0**  
**Ownership:** App

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-04_HOME_LIBRARY_EMPTY.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.md)
- **Low-fidelity visual (current `main`):** [WF-04_HOME_LIBRARY_EMPTY.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-04_HOME_LIBRARY_EMPTY.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Rescan; no online catalog, recommendations or fabricated results. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-03 / UF-04 |
| Canonical State IDs | `S-04` |
| Golden Scenario | GS-01 / GS-02 |
| Screen owner | UF-03/04 · Home/Library |
| Recovery owner | UF-02 / UF-03 / UF-04 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Understand that no accessible local tracks are currently indexed.
- **Primary action:** Rescan; permission recovery appears only when access is actually missing.
- **Visible source of truth:** MediaStore projection reconciled through the local index.
- **Recovery:** Rescan; no online catalog, recommendations or fabricated results.
- **Persistence/recreation:** Empty is a valid terminal state and survives recreation until source truth changes.

## Navigation

- **Entry:** Successful scan/reconciliation produces zero accessible tracks.
- **Exit:** Ready after content appears, or permission recovery when revoked.
- **Back:** Normal app exit/back behavior; state remains truthful on return.

## Accessibility

- **TalkBack order:** Library heading → Empty state → explanation → Rescan.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Android Settings is offered only for verified permission recovery.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
