# WF-05_SEARCH_RESULTS — Search: Results

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-05_SEARCH_RESULTS.svg](WF-05_SEARCH_RESULTS.svg)  
**Package:** `WF-05`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-04 |
| Canonical State IDs | `CB-09, CB-10, CB-13` |
| Golden Scenario | GS-02 |
| Screen owner | UF-04 · Search |
| Recovery owner | UF-03 / UF-04 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Select a revalidated local result while preserving the active query.
- **Primary action:** Select a result to request service-owned playback.
- **Visible source of truth:** Local derived-index query; selection revalidated against accessible media.
- **Recovery:** Stale result preserves query and reconciles; rejected playback keeps context.
- **Persistence/recreation:** Query is active-surface state only; no query/result telemetry.

## Navigation

- **Entry:** Local query returns one or more indexed matches.
- **Exit:** Validated selection → Full Player; edit/clear remains in Search.
- **Back:** Back returns to Library with no playback mutation.

## Accessibility

- **TalkBack order:** Search heading → query field → result count → rows → bottom navigation.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Typing and result lists are unavailable in active Driving Mode.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
