# WF-01_SPLASH_LOADING — Splash: Loading

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-01_SPLASH_LOADING.svg](WF-01_SPLASH_LOADING.svg)  
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

- **User goal:** Reach the next truthful local state without fabricated library or playback information.
- **Primary action:** Wait; no repeated action while routing is active.
- **Visible source of truth:** Verified local migration/readiness and platform state.
- **Recovery:** A bounded failure changes to the migration-error frame with Retry.
- **Persistence/recreation:** No UI-owned playback or library state is persisted.

## Navigation

- **Entry:** Cold or warm app start.
- **Exit:** Verified pending onboarding, permission recovery, or Home/Library state.
- **Back:** System Back exits the launch task; no in-app Back control.

## Accessibility

- **TalkBack order:** App name → Loading status.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

System splash/status chrome remains Android-owned.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
