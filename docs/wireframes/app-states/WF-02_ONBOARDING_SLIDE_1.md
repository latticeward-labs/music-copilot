# WF-02_ONBOARDING_SLIDE_1 — Onboarding: Slide 1 — Local music

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-02_ONBOARDING_SLIDE_1.svg](WF-02_ONBOARDING_SLIDE_1.svg)  
**Package:** `WF-02`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-01 |
| Canonical State IDs | `S-02` |
| Golden Scenario | GS-01 |
| Screen owner | UF-01 · Onboarding/Education |
| Recovery owner | UF-01 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Understand that Music Copilot plays local device music without an account.
- **Primary action:** Continue to Slide 2.
- **Visible source of truth:** Static approved product education.
- **Recovery:** Interrupted onboarding resumes the actual pending step.
- **Persistence/recreation:** Completion is stored only after Continue/Skip reaches its committed step.

## Navigation

- **Entry:** First run or explicit replay.
- **Exit:** Slide 2 / Skip Voice education.
- **Back:** Back exits or returns according to the owning first-run route without mutating completion.

## Accessibility

- **TalkBack order:** Heading → explanation → page position → Continue.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No permission is requested on this slide.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
