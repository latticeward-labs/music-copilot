# HF-08_VOICE_LISTENING — Voice Listening

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-08_VOICE_LISTENING.md`
- **Image:** [`HF-08_VOICE_LISTENING.png`](HF-08_VOICE_LISTENING.png)
- **PNG SHA-256:** `0222e67ade924827ecc518ac7731a8872c8f33bf84603f5b90a08ad99e30f48c`
- **Priority:** **P0**
- **Ownership:** App + Voice Coordinator
- **User Flow:** UF-08 / UF-09 / UF-10
- **Canonical State IDs:** `CD-03, CC-14`
- **Golden Scenario:** GS-04 recovery path

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-08` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Voice coordinator transient session; playback remains service-owned.

## Approved Primary Action Surface

Cancel.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Playing` |
| 3 | `Night Drive` |
| 4 | `Local Artist` |
| 5 | `Listening` |
| 6 | `Say a music command` |
| 7 | `6s max` |
| 8 | `Cancel` |
| 9 | `Playback continues` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-08` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
