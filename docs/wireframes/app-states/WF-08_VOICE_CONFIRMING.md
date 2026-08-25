# WF-08_VOICE_CONFIRMING — Voice Overlay: Confirming

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-08_VOICE_CONFIRMING.svg](WF-08_VOICE_CONFIRMING.svg)  
**Package:** `WF-08`  
**Priority:** **P0**  
**Ownership:** App + Voice Coordinator

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-08 / UF-09 / UF-10 |
| Canonical State IDs | `CD-09, CD-10` |
| Golden Scenario | GS-04 recovery path |
| Screen owner | UF-08/09/10 · Voice overlay |
| Recovery owner | UF-08 / UF-09 / UF-10 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_D_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Select one immutable presented candidate or reject it.
- **Primary action:** Choose one of at most two large options; Neither/Cancel is reachable.
- **Visible source of truth:** Immutable session candidate set.
- **Recovery:** One bounded question; never keyboard, long list or guessed execution.
- **Persistence/recreation:** Candidates and names are not telemetry/evidence.

## Navigation

- **Entry:** Deterministic resolver returns one low-confidence candidate or two artists.
- **Exit:** Presented selection → single command pending; reject/timeout → safe failure/manual.
- **Back:** Back equals Cancel; no execution.

## Accessibility

- **TalkBack order:** Prompt → candidate 1 → candidate 2 when present → Neither → Cancel.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No new permission or system destination.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
