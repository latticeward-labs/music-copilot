# WF-09_LIKED_SONGS_EMPTY — Liked Songs: Empty

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-09_LIKED_SONGS_EMPTY.svg](WF-09_LIKED_SONGS_EMPTY.svg)  
**Package:** `WF-09`  
**Priority:** **P2 / Removable**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-13 |
| Canonical State IDs | `UL-02, UL-03` |
| Golden Scenario | — (P2; not required by GS-01..05) |
| Screen owner | UF-13 · Liked Songs |
| Recovery owner | UF-13 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/UF-13_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Understand that the reconciled local liked projection is empty.
- **Primary action:** Back to Library; no invented recommendations.
- **Visible source of truth:** Local user-state repository projected through accessible MediaStore items.
- **Recovery:** Load/reconcile failure exposes Retry/Back; missing media is reconciled without file mutation.
- **Persistence/recreation:** Empty is a valid P2 terminal state.

## Navigation

- **Entry:** P2 entry load completes with zero accessible liked tracks.
- **Exit:** Back; future acknowledged Like can produce Ready.
- **Back:** Returns to initiating surface.

## Accessibility

- **TalkBack order:** P2/Removable label → heading → Empty explanation → Back.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No P0/P1 dependency; feature can be removed without placeholder.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
