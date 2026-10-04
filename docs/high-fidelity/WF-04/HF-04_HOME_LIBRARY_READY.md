# HF-04_HOME_LIBRARY_READY — Home Library Ready

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-04_HOME_LIBRARY_READY.md`
- **Image:** [`HF-04_HOME_LIBRARY_READY.png`](HF-04_HOME_LIBRARY_READY.png)
- **PNG SHA-256:** `da3d7b31a9e20f702d9f22d3ce03f74d78a12ec1edc9d51a353aba8210fc897b`
- **Priority:** **P0**
- **Ownership:** App
- **User Flow:** UF-03 / UF-04
- **Canonical State IDs:** `S-04, CB-01, CB-20`
- **Golden Scenario:** GS-01 / GS-02

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-04` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Derived index supplies library; MediaSession supplies mini-player.

## Approved Primary Action Surface

Open category, Search, select track, or open mini-player.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Library` |
| 3 | `Search` |
| 4 | `Local library ready` |
| 5 | `Artists` |
| 6 | `Albums` |
| 7 | `Tracks` |
| 8 | `Night Drive` |
| 9 | `Local Artist` |
| 10 | `Morning Signal` |
| 11 | `Demo Band` |
| 12 | `Night Drive - Playing` |
| 13 | `Library` |
| 14 | `Search` |
| 15 | `Likes` |
| 16 | `Settings` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-04` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
