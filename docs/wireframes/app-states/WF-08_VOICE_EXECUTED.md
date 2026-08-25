# WF-08_VOICE_EXECUTED — Voice Overlay: Executed

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-08_VOICE_EXECUTED.svg](WF-08_VOICE_EXECUTED.svg)  
**Package:** `WF-08`  
**Priority:** **P0**  
**Ownership:** App + Voice Coordinator

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-08 / UF-09 / UF-10 |
| Canonical State IDs | `CD-07` |
| Golden Scenario | GS-04 recovery path |
| Screen owner | UF-08/09/10 · Voice overlay |
| Recovery owner | UF-08 / UF-09 / UF-10 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Confirm exactly one acknowledged command using refreshed playback truth.
- **Primary action:** Dismiss/return to manual controls.
- **Visible source of truth:** MediaSession refresh; optional TTS is non-authoritative.
- **Recovery:** TTS failure remains Executed; stale/rejected command uses Failed.
- **Persistence/recreation:** Only service playback state persists; no transcript.

## Navigation

- **Entry:** Playback service acknowledges the submitted direct/confirmed command.
- **Exit:** Returns to the initiating surface with actual refreshed state.
- **Back:** Back dismisses; it does not undo acknowledged playback.

## Accessibility

- **TalkBack order:** Executed status → short result → Dismiss/manual controls.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No system surface is created.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
