# WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE — Onboarding: Slide 2 / Skip Voice

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg](WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg)  
**Package:** `WF-02`  
**Priority:** **P0**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-01 |
| Canonical State IDs | `S-02, CA-01, CA-02` |
| Golden Scenario | GS-01 |
| Screen owner | UF-01 · Onboarding/Education |
| Recovery owner | UF-01 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/mermaid/01_6_STATE_INVENTORY_AND_TRACEABILITY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Understand optional Voice and retain full manual control when it is skipped.
- **Primary action:** Finish; optional Practice Voice is a separate explicit action.
- **Visible source of truth:** Exactly six supported intents; Voice provider/permission remains unrequested until explicit practice.
- **Recovery:** Skip or provider decline returns to manual local control.
- **Persistence/recreation:** Education completion only; permission/provider state is re-read separately.

## Navigation

- **Entry:** Continue from Slide 1 or resume pending education.
- **Exit:** Permission/library readiness path; Skip/Finish requests no microphone.
- **Back:** Back returns to Slide 1 without changing permission or playback.

## Accessibility

- **TalkBack order:** Heading → six-command summary → page position → Practice Voice → Finish without Voice.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Microphone dialog may occur only after explicit Practice Voice.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
