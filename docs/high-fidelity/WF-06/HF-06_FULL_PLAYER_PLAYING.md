# HF-06_FULL_PLAYER_PLAYING — Full Player Playing

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-06_FULL_PLAYER_PLAYING.md`
- **Image:** [`HF-06_FULL_PLAYER_PLAYING.png`](HF-06_FULL_PLAYER_PLAYING.png)
- **PNG SHA-256:** `c7df3dae9dcc89c02151cefa24293110e87b557c8c5bc5ceea8226873cc68080`
- **Priority:** **P0**
- **Ownership:** App + Playback Service
- **User Flow:** UF-05 / UF-06 / UF-11
- **Canonical State IDs:** `CB-15`
- **Golden Scenario:** GS-02 / GS-03 / GS-05

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-06` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

MediaSessionService snapshot: item, queue, state, position and supported actions.

## Approved Primary Action Surface

Pause; Next/Previous/Seek/Queue remain available as supported.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Now playing` |
| 3 | `Night Drive` |
| 4 | `Local Artist` |
| 5 | `1:24` |
| 6 | `3:42` |
| 7 | `Previous` |
| 8 | `Pause` |
| 9 | `Next` |
| 10 | `Driving` |
| 11 | `Mic` |
| 12 | `Up next` |
| 13 | `Morning Signal` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-06` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
