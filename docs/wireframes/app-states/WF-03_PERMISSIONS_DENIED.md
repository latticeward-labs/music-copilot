# WF-03_PERMISSIONS_DENIED — Permissions: Denied — requestable

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-03_PERMISSIONS_DENIED.svg](WF-03_PERMISSIONS_DENIED.svg)  
**Package:** `WF-03`  
**Priority:** **P0**  
**Ownership:** Hybrid

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-03_PERMISSIONS_DENIED.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.md)
- **Low-fidelity visual (current `main`):** [WF-03_PERMISSIONS_DENIED.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.svg)
- **Node-equivalent immutable visual URL:** [SVG at construction commit](https://github.com/latticeward-labs/music-copilot/blob/47f6b91194aa088783e62034e0a773d1fb0dc37f/docs/wireframes/app-states/WF-03_PERMISSIONS_DENIED.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Construction PASS / reviewable; named Product, Design, Android and QA approval pending.
- **Empty/Error/Denial/Retry disposition:** Retry or manual fallback; local browse/playback remains available when mic is denied. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-02 / UF-10 / UF-12 |
| Canonical State IDs | `S-03, CA-02` |
| Golden Scenario | GS-01 |
| Screen owner | UF-02 · Permission recovery |
| Recovery owner | UF-02 / UF-10 / UF-12 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Recover or continue manually after a requestable denial.
- **Primary action:** Try again; Not now remains equally reachable.
- **Visible source of truth:** Fresh Android permission result.
- **Recovery:** Retry or manual fallback; local browse/playback remains available when mic is denied.
- **Persistence/recreation:** No false grant; denial is rechecked on future explicit use.

## Navigation

- **Entry:** Android denies while another request remains permitted.
- **Exit:** Try again → rationale/request; Not now → initiating manual surface.
- **Back:** Same as Not now; no automatic Settings launch.

## Accessibility

- **TalkBack order:** Denied heading → consequence → Try again → Not now.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No Android Settings handoff is required for requestable denial.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
