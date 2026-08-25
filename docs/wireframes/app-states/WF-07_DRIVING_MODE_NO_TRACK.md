# WF-07_DRIVING_MODE_NO_TRACK — Driving Mode: No Track

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-07_DRIVING_MODE_NO_TRACK.svg](WF-07_DRIVING_MODE_NO_TRACK.svg)  
**Package:** `WF-07`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-07 / UF-11 |
| Canonical State IDs | `CC-07, CC-15` |
| Golden Scenario | GS-04 / GS-05 |
| Screen owner | UF-07 · Driving Mode |
| Recovery owner | UF-07 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_C_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Leave or start at most one revalidated last local item without opening a list.
- **Primary action:** Exit; Play last is shown only when a valid local reference exists.
- **Visible source of truth:** Service snapshot plus at most one revalidated MediaStore item.
- **Recovery:** Failure remains No Track and routes to Library after Exit; no list/typing.
- **Persistence/recreation:** Only an already-approved local last-item reference may be considered.

## Navigation

- **Entry:** Complete snapshot has no current playable item.
- **Exit:** Valid Play last → Playing/Paused; Exit → Home/Library.
- **Back:** Back equals Exit.

## Accessibility

- **TalkBack order:** No track heading → explanation → optional Play last → Mic → Exit.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No Android Auto, picker or system list is introduced.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
