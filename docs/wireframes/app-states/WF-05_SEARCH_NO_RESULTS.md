# WF-05_SEARCH_NO_RESULTS — Search: No Result

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-05_SEARCH_NO_RESULTS.svg](WF-05_SEARCH_NO_RESULTS.svg)  
**Package:** `WF-05`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-04 |
| Canonical State IDs | `CB-09, CB-11` |
| Golden Scenario | GS-02 |
| Screen owner | UF-04 · Search |
| Recovery owner | UF-03 / UF-04 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Edit or clear a query with no local matches.
- **Primary action:** Edit query; Clear is secondary.
- **Visible source of truth:** Completed local index query; no remote fallback.
- **Recovery:** Preserve query and Browse access; subsequent error uses Search Error.
- **Persistence/recreation:** Query persists only while Search remains active.

## Navigation

- **Entry:** Local query completes with zero matches.
- **Exit:** Updated query → Results/No Result/Error; Back → Library.
- **Back:** Preserves no mutation; returns to Library.

## Accessibility

- **TalkBack order:** Search heading → query → No results label → Edit/Clear guidance.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No online catalog or network handoff.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
