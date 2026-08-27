# WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY — Full Player: Corrupt File Recovery

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / COMPLETE**  
**Phase 8 review:** **CROSS-ROLE FEASIBILITY PASS — 27 August 2026**  
**Visual:** [WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg](WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg)  
**Package:** `WF-06`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.md)
- **Low-fidelity visual (current `main`):** [WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg)
- **Node-equivalent immutable visual URL:** [SVG after Phase 8 geometry correction](https://github.com/latticeward-labs/music-copilot/blob/7480843ce01ee7b8054f6d1313d6f58d88de0785/docs/wireframes/app-states/WF-06_FULL_PLAYER_CORRUPT_FILE_RECOVERY.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Phase 8 geometry correction:** [`7480843ce01ee7b8054f6d1313d6f58d88de0785`](https://github.com/latticeward-labs/music-copilot/commit/7480843ce01ee7b8054f6d1313d6f58d88de0785) — approved 360×800dp inner Android frame.
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Phase 8 Product, Design/UX, Android feasibility, Accessibility and Privacy review PASS on 27 August 2026; no open Blocker or Major.
- **Empty/Error/Denial/Retry disposition:** One bounded forward skip; permission/reindex recovery when appropriate. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-05 / UF-06 / UF-11 |
| Canonical State IDs | `CB-19, CE-12, CE-13, CE-14` |
| Golden Scenario | GS-02 / GS-03 / GS-05 |
| Screen owner | UF-05 · Full Player |
| Recovery owner | UF-03 / UF-05 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Recover from an inaccessible item without looping or deleting media.
- **Primary action:** Continue with bounded next-item validation; Library is always reachable.
- **Visible source of truth:** Typed service failure plus MediaStore/index reconciliation.
- **Recovery:** One bounded forward skip; permission/reindex recovery when appropriate.
- **Persistence/recreation:** False playability is removed; user media is untouched.

## Navigation

- **Entry:** Prepare/advance/selection meets corrupt or inaccessible media.
- **Exit:** Valid next → Playing/Paused; none → terminal Paused/Completed or Library.
- **Back:** Returns to Library/manual selection; no file deletion.

## Accessibility

- **TalkBack order:** Unavailable heading → concise explanation → Continue → Library.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No system dialog unless a separately verified permission recovery is needed.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy reviewed in GitHub — [Phase 8 review evidence](../review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
