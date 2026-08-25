# WF-03_PERMISSIONS_DENIED — Permissions: Denied — requestable

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-03_PERMISSIONS_DENIED.svg](WF-03_PERMISSIONS_DENIED.svg)  
**Package:** `WF-03`  
**Priority:** **P0**  
**Ownership:** Hybrid

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
