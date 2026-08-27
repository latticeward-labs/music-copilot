# WF-10_SETTINGS_DEFAULT — Settings: Default

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-10_SETTINGS_DEFAULT.svg](WF-10_SETTINGS_DEFAULT.svg)  
**Package:** `WF-10`  
**Priority:** **P0**  
**Ownership:** App + platform/provider boundary

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-10_SETTINGS_DEFAULT.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md)
- **Low-fidelity visual (current `main`):** [WF-10_SETTINGS_DEFAULT.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Load/update error retains previous value and exposes Retry where safe. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-12 |
| Canonical State IDs | `CE-15, CE-21, CE-22` |
| Golden Scenario | — (not on GS-01..05 path) |
| Screen owner | UF-12 · Settings/Privacy |
| Recovery owner | UF-12 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Inspect current truthful preferences, permissions and privacy controls outside Driving Mode.
- **Primary action:** Open one settings section.
- **Visible source of truth:** Preferences plus fresh platform/provider truth.
- **Recovery:** Load/update error retains previous value and exposes Retry where safe.
- **Persistence/recreation:** Only approved local settings/consent are stored.

## Navigation

- **Entry:** Settings opened from non-driving navigation.
- **Exit:** Section/update/handoff; Back returns to prior surface.
- **Back:** Returns without changing values or playback.

## Accessibility

- **TalkBack order:** Settings heading → section headings → current values/actions → bottom navigation.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Android Settings handoff is explicit and never launched from active Driving Mode.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
