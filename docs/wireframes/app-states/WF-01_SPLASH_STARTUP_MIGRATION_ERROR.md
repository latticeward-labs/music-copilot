# WF-01_SPLASH_STARTUP_MIGRATION_ERROR — Splash: Startup / Migration Error

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg](WF-01_SPLASH_STARTUP_MIGRATION_ERROR.svg)  
**Package:** `WF-01`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-01 |
| Canonical State IDs | `S-01` |
| Golden Scenario | GS-01 |
| Screen owner | UF-01 · Splash |
| Recovery owner | UF-01 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Recover startup without clearing valid local data.
- **Primary action:** Retry startup once the user explicitly chooses Retry.
- **Visible source of truth:** Local migration result; no fabricated Empty/Ready state.
- **Recovery:** Retry; repeated failure remains concise and non-destructive.
- **Persistence/recreation:** Verified prior data is retained; no reinstall/clear-data default.

## Navigation

- **Entry:** Typed startup or local migration failure.
- **Exit:** Successful Retry routes from freshly verified state.
- **Back:** System Back exits; Retry never traps focus.

## Accessibility

- **TalkBack order:** Error heading → explanation → Retry.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No Android Settings handoff is implied.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
