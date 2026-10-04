# HF-09_LIKED_SONGS_READY — Liked Songs Ready

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-09_LIKED_SONGS_READY.md`
- **Image:** [`HF-09_LIKED_SONGS_READY.png`](HF-09_LIKED_SONGS_READY.png)
- **PNG SHA-256:** `04ac3fc7ca832e3f63d1254473326e27047a2d2475e80e4be9f0a150b9db314e`
- **Priority:** **P2 / Removable**
- **Ownership:** App
- **User Flow:** UF-13
- **Canonical State IDs:** `UL-04, UL-09, UL-10, UL-12`
- **Golden Scenario:** P2; not required by GS-01..05

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-09` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Local Like repository plus accessible MediaStore projection; service owns playback.

## Approved Primary Action Surface

Play list or select a row; unlike preserves media files.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Liked Songs` |
| 3 | `P2 / removable` |
| 4 | `Play list` |
| 5 | `Night Drive` |
| 6 | `Local Artist` |
| 7 | `Morning Signal` |
| 8 | `Demo Band` |
| 9 | `Library` |
| 10 | `Search` |
| 11 | `Likes` |
| 12 | `Settings` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-09` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
