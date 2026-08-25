# WF-04_HOME_LIBRARY_RECOVERABLE_ERROR — Home / Library: Recoverable Error

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.svg](WF-04_HOME_LIBRARY_RECOVERABLE_ERROR.svg)  
**Package:** `WF-04`  
**Priority:** **P0**  
**Ownership:** App

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

- **User goal:** Retry a local scan/index failure without losing verified state.
- **Primary action:** Retry scan/reconciliation.
- **Visible source of truth:** Typed repository/index failure plus fresh MediaStore/access check.
- **Recovery:** Retry; no reinstall or destructive reset by default.
- **Persistence/recreation:** Verified prior data is retained; false playability is removed only after authoritative reconciliation.

## Navigation

- **Entry:** Typed recoverable index, storage or reconciliation failure.
- **Exit:** Scanning then Empty/Ready; permission issue routes to WF-03.
- **Back:** Back keeps any verified prior library/playback state intact.

## Accessibility

- **TalkBack order:** Error heading → concise reason → Retry → Back/navigation.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Permission recovery is a separate handoff, not embedded Settings UI.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
