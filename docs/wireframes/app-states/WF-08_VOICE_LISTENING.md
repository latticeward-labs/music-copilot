# WF-08_VOICE_LISTENING — Voice Overlay: Listening

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-08_VOICE_LISTENING.svg](WF-08_VOICE_LISTENING.svg)  
**Package:** `WF-08`  
**Priority:** **P0**  
**Ownership:** App + Voice Coordinator

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
