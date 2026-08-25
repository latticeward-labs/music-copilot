# WF-07_DRIVING_MODE_PAUSED — Driving Mode: Paused

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-07_DRIVING_MODE_PAUSED.svg](WF-07_DRIVING_MODE_PAUSED.svg)  
**Package:** `WF-07`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-07 / UF-11 |
| Canonical State IDs | `CC-06, CC-16` |
| Golden Scenario | GS-04 / GS-05 |
| Screen owner | UF-07 · Driving Mode |
| Recovery owner | UF-07 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_C_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Resume only through an explicit acknowledged Play action.
- **Primary action:** Play; Exit remains prominent.
- **Visible source of truth:** MediaSessionService Paused snapshot.
- **Recovery:** Unsafe route remains safely Paused; refresh/reconnect never surprises with audio.
- **Persistence/recreation:** No auto-play on entry or recreation.

## Navigation

- **Entry:** Complete snapshot reports Paused/Ended.
- **Exit:** Acknowledged Play → Playing; Exit → prior surface.
- **Back:** Back equals immediate Exit; never auto-resumes.

## Accessibility

- **TalkBack order:** Paused → title/artist → Previous → Play → Next → Seek back → Mic → Exit.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Route/focus safety may disable Play with a textual reason.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
