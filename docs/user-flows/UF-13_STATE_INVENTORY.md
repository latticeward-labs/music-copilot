# UF-13 State Inventory

**Priority:** P2  
**Authority:** PRD FR-09; MVP Like/Liked Songs; Phase-0 S-09/D-027; ADR-0002/0003/0006  
**Rule:** MediaStore/access is authoritative for playable existence; the local user-state repository is authoritative for acknowledged Like intent.

| ID | State | Entry / visible state | Allowed action | System work / source of truth | Success | Failure / recovery | Persistence / telemetry / constraints |
|---|---|---|---|---|---|---|---|
| UL-01 | P2 not shipped | No Liked Songs entry or Like control | Continue P0/P1 flows | Build/scope capability | Existing flows continue | N/A | No placeholder/event; no P0 dependency |
| UL-02 | Entry loading | Open from Home/Library; bounded progress | Back | Load Like records and accessible media | UL-03 or UL-04 | UL-12 | No personal metadata telemetry |
| UL-03 | Empty | Explanation; no invented recommendations | Back | Reconciled projection is empty | Exit | UL-12 on refresh error | Empty is a required terminal UI state |
| UL-04 | Ready | Accessible liked tracks; Play list; rows | Play list, play row, unlike, back | Repository order projected through accessible media | UL-08/UL-09/UL-06 | UL-12 | P2 label remains visible in review artifacts |
| UL-05 | Like write pending | Last acknowledged Like state remains visible | Cancel/retry only if supported | Idempotent local insert | UL-06 | UL-07 | Persistent local app state; no file mutation |
| UL-06 | Like acknowledged | Control announced selected | Unlike, continue | Local repository owns acknowledged state | Ready/Full Player | N/A | Duplicate records prohibited |
| UL-07 | Mutation failed | Concise failure; prior acknowledged state | Retry, dismiss | Repository reports typed failure | UL-05 or prior state | Prior state retained | No optimistic false success |
| UL-08 | Unlike write pending | Last acknowledged state remains visible | Cancel/retry only if supported | Remove only selected Like record | UL-03, UL-04, or unliked Full Player | UL-07 | Never delete/move media |
| UL-09 | Playback command pending | Selected row/list shows bounded progress | Back/manual controls | Submit accessible queue to service/controller | UL-10 | UL-11 | Playback service owns queue/player |
| UL-10 | Playback acknowledged | Full Player observes Playing | Player controls | Service/controller state | UF-05/UF-06 | UF-11 if interrupted | Required terminal: playback started |
| UL-11 | Playback rejected/stale | No false Playing state | Retry valid item, back | Typed rejection; refresh projection | UL-02, UL-03, or UL-04 | Manual playback remains available | Current unrelated playback preserved |
| UL-12 | Load/reconcile failure | Concise local error | Retry, back | Repository/access boundary | UL-02 | Exit to initiating surface | No crash; no long recovery in Driving Mode |
| UL-13 | Missing item reconciliation | Removed item absent; optional concise update | Continue, back | Compare likes to accessible MediaStore projection; clean stale local reference | UL-03 or UL-04 | UL-12 | Required terminal: missing item reconciled; media untouched |
| UL-14 | UI recreation | Recreated surface shows bounded loading | Back | Reload repositories; observe playback service | UL-03/UL-04/UL-10 | UL-12 | Widget state is not authoritative |

## Inventory checks

- Happy: UL-04 -> UL-09 -> UL-10.
- Empty: UL-02 -> UL-03.
- Like/unlike: UL-05/UL-06 and UL-08 with UL-07 failure.
- Missing media: UL-02 -> UL-13 -> UL-03/UL-04.
- P2 removal: UL-01 has no dependency edge into P0/P1.
- Accessibility: every visible control carries role, state, label, and reachable focus order; 48dp minimum target.
