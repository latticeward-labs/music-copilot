# WF-03_PERMISSIONS_GRANTED — Permissions: Granted

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-03_PERMISSIONS_GRANTED.svg](WF-03_PERMISSIONS_GRANTED.svg)  
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

- **User goal:** Resume the exact action that was blocked by the capability check.
- **Primary action:** Continue automatically or explicitly to the blocked app action.
- **Visible source of truth:** Fresh Android permission recheck.
- **Recovery:** If capability changes before use, re-enter the appropriate recovery state.
- **Persistence/recreation:** Platform truth is re-read; grant is not copied as permanent app truth.

## Navigation

- **Entry:** Android reports Granted after request or Settings return.
- **Exit:** Media grant → scan/readiness; mic grant → provider/capability check.
- **Back:** Back returns to the initiating surface.

## Accessibility

- **TalkBack order:** Granted status → next action.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Provider availability/cloud consent remains distinct from microphone grant.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
