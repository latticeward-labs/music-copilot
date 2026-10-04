# HF-05_SEARCH_RESULTS — Search Results

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-05_SEARCH_RESULTS.md`
- **Image:** [`HF-05_SEARCH_RESULTS.png`](HF-05_SEARCH_RESULTS.png)
- **PNG SHA-256:** `f10f580f7f697c4f964daa83d6eabd0970d94bd953ce13d771ff8703311605a9`
- **Priority:** **P0**
- **Ownership:** App
- **User Flow:** UF-04
- **Canonical State IDs:** `CB-09, CB-10, CB-13`
- **Golden Scenario:** GS-02

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-05` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Local derived-index query; selection revalidated against accessible media.

## Approved Primary Action Surface

Select a result to request service-owned playback.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Search` |
| 3 | `night` |
| 4 | `3 local results` |
| 5 | `Night Drive` |
| 6 | `Local Artist` |
| 7 | `Midnight Local` |
| 8 | `Demo Band` |
| 9 | `Night Walk` |
| 10 | `Folder track` |
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

- TalkBack order follows the paired `WF-05` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
