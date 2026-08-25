# WF-06_FULL_PLAYER_PAUSED — Full Player: Paused

**Phase:** 4 — App State Frames  
**Construction status:** **PASS / REVIEWABLE — approval pending**  
**Visual:** [WF-06_FULL_PLAYER_PAUSED.svg](WF-06_FULL_PLAYER_PAUSED.svg)  
**Package:** `WF-06`  
**Priority:** **P0**  
**Ownership:** App + Playback Service

## Identity and traceability

| Field | Value |
|---|---|
| User Flow | UF-05 / UF-06 / UF-11 |
| Canonical State IDs | `CB-16` |
| Golden Scenario | GS-02 / GS-03 / GS-05 |
| Screen owner | UF-05 · Full Player |
| Recovery owner | UF-03 / UF-05 / UF-11 |
| Coverage Matrix | [126-state manifest](../WIREFRAME_COVERAGE_MATRIX.md) |
| State source | [Owning State Inventory](../../user-flows/CLUSTER_B_STATE_INVENTORY.md) |
| GS source | [Stage 2 traceability](../../user-flows/golden-scenarios/STAGE_2_TRACEABILITY_MATRIX.md) |
| Product authority | [PRD](../../PRD.md) · [MVP](../../MVP.md) |
| Foundation contract | [Low-fidelity Foundations](../foundations/README.md) |

## Behavior contract

- **User goal:** Understand the actual Paused state and resume explicitly.
- **Primary action:** Play.
- **Visible source of truth:** MediaSessionService snapshot.
- **Recovery:** Controller failure refreshes; interruption route rules may require safe manual resume.
- **Persistence/recreation:** Active service retains queue/position; UI is observer only.

## Navigation

- **Entry:** Service reports Paused after command, interruption or entry.
- **Exit:** Acknowledged Play → Playing; Back preserves queue/position.
- **Back:** Returns without auto-playing.

## Accessibility

- **TalkBack order:** Paused state → title/artist → position → Previous → Play → Next → Queue.
- Every interactive target is at least **48dp × 48dp**; Driving Mode controls are larger where practical.
- Text reflows at 200% scaling without clipping the primary or recovery action.
- State uses a written label plus shape/symbol; color is never the only cue.

## App / system boundary

No optimistic resume after route/focus changes.

## Review checklist

- [x] Stable Markdown/SVG identifier pair exists.
- [x] UF, State, GS, priority and ownership are explicit.
- [x] Entry, exit, Back, source of truth, recovery and persistence are explicit.
- [x] 48dp, TalkBack, scalable text and non-color-only rules are annotated.
- [x] No backend, account, streaming, Android Auto, iOS or v1.1 scope is introduced.
- [ ] Product, Design, Android and QA reviewers record approval in GitHub.
