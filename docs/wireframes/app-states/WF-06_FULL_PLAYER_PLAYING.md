# WF-06_FULL_PLAYER_PLAYING — Full Player: Playing

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-06_FULL_PLAYER_PLAYING.svg](WF-06_FULL_PLAYER_PLAYING.svg)  
**Package:** `WF-06`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Phase 6 repository artifact

- **Behavioral record (current `main`):** [WF-06_FULL_PLAYER_PLAYING.md](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md)
- **Low-fidelity visual (current `main`):** [WF-06_FULL_PLAYER_PLAYING.svg](https://github.com/latticeward-labs/music-copilot/blob/main/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.svg)
- **Node-equivalent immutable visual URL:** [SVG at construction commit](https://github.com/latticeward-labs/music-copilot/blob/47f6b91194aa088783e62034e0a773d1fb0dc37f/docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.svg)
- **Construction provenance:** [PR #38](https://github.com/latticeward-labs/music-copilot/pull/38) · [`47f6b91194aa088783e62034e0a773d1fb0dc37f`](https://github.com/latticeward-labs/music-copilot/commit/47f6b91194aa088783e62034e0a773d1fb0dc37f)
- **Visual format:** Editable, text-based repository SVG; no Figma dependency.
- **Review disposition:** Construction PASS / reviewable; named Product, Design, Android and QA approval pending.
- **Empty/Error/Denial/Retry disposition:** Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery. Conditions not owned by this frame are not fabricated; they route to the owning `WF-*` record or system transition.
- **Driving restriction:** This frame is not a Driving Mode setup destination. Active Driving Mode remains on its single fixed-control layer; permission, Settings, consent, typing, deep navigation and long lists are prohibited.

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-05 / UF-06 / UF-11 |
| Canonical State IDs | `CB-15` |
| Golden Scenario | GS-02 / GS-03 / GS-05 |
| Screen owner | UF-05 · Full Player |
| Recovery owner | UF-03 / UF-05 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Control the actual service-owned Playing queue and current item.
- **Primary action:** Pause; Next/Previous/Seek/Queue remain available as supported.
- **Visible source of truth:** MediaSessionService snapshot: item, queue, state, position and supported actions.
- **Recovery:** Command/controller failure refreshes actual snapshot; corrupt item uses bounded recovery.
- **Persistence/recreation:** P0 live service state survives UI recreation.

## Navigation

- **Entry:** MediaSessionService acknowledges playback with a valid item.
- **Exit:** Back keeps playback; Driving Mode requires explicit entry; Queue remains same authority.
- **Back:** Returns to initiating app surface without stopping playback.

## Accessibility

- **TalkBack order:** Now playing → title/artist → position → Previous → Pause → Next → Queue.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

Notification/lock/external controls are system-only synchronized surfaces.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
