# WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION — Settings: Clear Data / History Confirmation

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg](WF-10_SETTINGS_CLEAR_DATA_HISTORY_CONFIRMATION.svg)  
**Package:** `WF-10`  
**Priority:** **P0**  
**Ownership:** App + platform/provider boundary

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-12 |
| Canonical State IDs | `CE-23, CE-24` |
| Golden Scenario | — (not on GS-01..05 path) |
| Screen owner | UF-12 · Settings/Privacy |
| Recovery owner | UF-12 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_E_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Confirm only explicitly selected local categories while preserving protected data.
- **Primary action:** Confirm clear; Cancel is equally reachable.
- **Visible source of truth:** Selected local categories and documented provider boundaries.
- **Recovery:** No destructive default; partial/provider failure is reported truthfully later.
- **Persistence/recreation:** No deletion before confirmation; playback and user media are protected.

## Navigation

- **Entry:** At least one non-preselected clear category is selected.
- **Exit:** Confirm → Clearing/Result; Cancel → selection or Settings.
- **Back:** Back equals Cancel; no deletion occurs.

## Accessibility

- **TalkBack order:** Warning heading → selected categories → preserved categories → Confirm clear → Cancel.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Never claims remote deletion unless the provider confirms it.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
