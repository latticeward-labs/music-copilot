# WF-08_VOICE_LISTENING — Voice Overlay: Listening

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-08_VOICE_LISTENING.svg](WF-08_VOICE_LISTENING.svg)  
**Package:** `WF-08`  
**Priority:** **P0**  
**Ownership:** App + Voice Coordinator

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-08_VOICE_LISTENING.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.md)
- **Low-fidelity visual (current `main`):** [WF-08_VOICE_LISTENING.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-08_VOICE_LISTENING.svg)
- **Node-equivalent immutable visual URL:** [SVG at construction commit](https://github.com/latticeward-labs/music-copilot/blob/47f6b91194aa088783e62034e0a773d1fb0dc37f/docs/wireframes/app-states/WF-08_VOICE_LISTENING.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Construction PASS / reviewable; named Product, Design, Android and QA approval pending.
- **Empty/Error/Denial/Retry disposition:** Timeout/no-match/provider failure preserves playback and exposes one Retry. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** In active Driving Mode, no permission, Settings or consent UI may open; feedback stays brief, one explicit Retry is the maximum, and failure returns to fixed manual controls.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-08 / UF-09 / UF-10 |
| Canonical State IDs | `CD-03, CC-14` |
| Golden Scenario | GS-04 recovery path |
| Screen owner | UF-08/09/10 · Voice overlay |
| Recovery owner | UF-08 / UF-09 / UF-10 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Provide one explicit six-second listening attempt while manual/Exit remains reachable.
- **Primary action:** Cancel.
- **Visible source of truth:** Voice coordinator transient session; playback remains service-owned.
- **Recovery:** Timeout/no-match/provider failure preserves playback and exposes one Retry.
- **Persistence/recreation:** Raw audio/transcript and attempt state are not persisted.

## Navigation

- **Entry:** Explicit microphone tap after permission/provider capability passes.
- **Exit:** Result → Processing; failure → Failed/Retry; Cancel → initiating surface.
- **Back:** Back equals Cancel and releases capture/duck.

## Accessibility

- **TalkBack order:** Listening status → time limit → Cancel → underlying manual/Exit controls.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Android mic indicator/dialog is system-owned; capture never starts on mode entry.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
