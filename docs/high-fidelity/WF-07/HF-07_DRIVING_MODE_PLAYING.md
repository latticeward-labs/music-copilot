# HF-07_DRIVING_MODE_PLAYING — Driving Mode Playing

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md`
- **Image:** [`HF-07_DRIVING_MODE_PLAYING.png`](HF-07_DRIVING_MODE_PLAYING.png)
- **PNG SHA-256:** `e23b63d1280c63e29bb9434c54290b3e892e4ca11eb86a70b9b217c3c8b27899`
- **Priority:** **P0**
- **Ownership:** App + Playback Service
- **User Flow:** UF-07 / UF-11
- **Canonical State IDs:** `CC-05, CC-14, CC-16`
- **Golden Scenario:** GS-04 / GS-05

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-07` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Complete MediaSessionService snapshot and supported actions.

## Approved Primary Action Surface

Pause; four large controls, Mic and Exit remain fixed.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Playing` |
| 3 | `Night Drive` |
| 4 | `Local Artist` |
| 5 | `Prev` |
| 6 | `Pause` |
| 7 | `Next` |
| 8 | `-10s` |
| 9 | `Voice` |
| 10 | `Exit` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-07` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
