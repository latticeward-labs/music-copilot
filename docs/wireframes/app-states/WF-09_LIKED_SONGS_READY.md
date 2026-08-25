# WF-09_LIKED_SONGS_READY — Liked Songs: Ready

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-09_LIKED_SONGS_READY.svg](WF-09_LIKED_SONGS_READY.svg)  
**Package:** `WF-09`  
**Priority:** **P2 / Removable**  
**Ownership:** App

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-13 |
| Canonical State IDs | `UL-04, UL-09, UL-10, UL-12` |
| Golden Scenario | — (P2; not required by GS-01..05) |
| Screen owner | UF-13 · Liked Songs |
| Recovery owner | UF-13 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/UF-13_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Play or unlike accessible locally liked tracks.
- **Primary action:** Play list or select a row; unlike preserves media files.
- **Visible source of truth:** Local Like repository plus accessible MediaStore projection; service owns playback.
- **Recovery:** Stale/rejected item refreshes projection; load failure preserves manual playback.
- **Persistence/recreation:** Acknowledged likes persist locally; missing media reference is cleaned, media untouched.

## Navigation

- **Entry:** P2 projection contains accessible liked tracks.
- **Exit:** Playback acknowledgement → Full Player; Back → Library.
- **Back:** Returns without stopping unrelated playback.

## Accessibility

- **TalkBack order:** P2/Removable label → Play list → rows → Like state/action → Back.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Feature remains optional/removable and never blocks P0/P1.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
