# WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE — Onboarding: Slide 2 / Skip Voice

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg](WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg)  
**Package:** `WF-02`  
**Priority:** **P0**  
**Ownership:** App

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.md)
- **Low-fidelity visual (current `main`):** [WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_2_SKIP_VOICE.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** Skip or provider decline returns to manual local control. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** In active Driving Mode, no permission, Settings or consent UI may open; feedback stays brief, one explicit Retry is the maximum, and failure returns to fixed manual controls.

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
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
