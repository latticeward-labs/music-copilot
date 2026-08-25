# WF-04_HOME_LIBRARY_READY — Home / Library: Ready

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-04_HOME_LIBRARY_READY.svg](WF-04_HOME_LIBRARY_READY.svg)  
**Package:** `WF-04`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-03 / UF-04 |
| Canonical State IDs | `S-04, CB-01, CB-20` |
| Golden Scenario | GS-01 / GS-02 |
| Screen owner | UF-03/04 · Home/Library |
| Recovery owner | UF-02 / UF-03 / UF-04 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Browse accessible local music and reach Search or acknowledged playback.
- **Primary action:** Open a category, Search, or select a track.
- **Visible source of truth:** Derived index supplies library; MediaSession supplies mini-player.
- **Recovery:** Stale item → reconcile; revoked access → permissions; controller pending stays truthful.
- **Persistence/recreation:** Playback survives UI recreation; library index is rebuildable.

## Navigation

- **Entry:** Verified non-empty index or returning route with valid current state.
- **Exit:** Browse/Search/Full Player/Settings; mini-player opens Full Player.
- **Back:** Standard top-level navigation; Back does not stop service-owned playback.

## Accessibility

- **TalkBack order:** Library heading → categories → tracks → mini-player → bottom navigation.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No system surface is represented as app navigation.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
