# HF-10_SETTINGS_DEFAULT — Settings Default

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-10_SETTINGS_DEFAULT.md`
- **Image:** [`HF-10_SETTINGS_DEFAULT.png`](HF-10_SETTINGS_DEFAULT.png)
- **PNG SHA-256:** `af44caa742338fcdfc9475d556c78f0ed30e4d62e4a92b6c6ac4d4f3d9e6b1ba`
- **Priority:** **P0**
- **Ownership:** App + platform/provider boundary
- **User Flow:** UF-12
- **Canonical State IDs:** `CE-15, CE-21, CE-22`
- **Golden Scenario:** Not on GS-01..05 path

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-10` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Preferences plus fresh platform/provider truth.

## Approved Primary Action Surface

Open one settings section.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Settings` |
| 3 | `Privacy` |
| 4 | `Local only` |
| 5 | `Voice provider` |
| 6 | `Android speech` |
| 7 | `Permissions` |
| 8 | `Check access` |
| 9 | `Clear history` |
| 10 | `Local command logs` |
| 11 | `Library` |
| 12 | `Search` |
| 13 | `Likes` |
| 14 | `Settings` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-10` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
