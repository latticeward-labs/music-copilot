# HF-03_PERMISSIONS_RATIONALE_REQUEST — Permissions Rationale Request

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-03_PERMISSIONS_RATIONALE_REQUEST.md`
- **Image:** [`HF-03_PERMISSIONS_RATIONALE_REQUEST.png`](HF-03_PERMISSIONS_RATIONALE_REQUEST.png)
- **PNG SHA-256:** `81afc789c4e06e1d87f4b534b86678f79b8f44f3ba4b252aa9dbbf4e543d5ddc`
- **Priority:** **P0**
- **Ownership:** Hybrid
- **User Flow:** UF-02 / UF-10 / UF-12
- **Canonical State IDs:** `S-03, CA-02, SYS-01, SYS-02`
- **Golden Scenario:** GS-01

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-03` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Android permission state is authoritative.

## Approved Primary Action Surface

Continue to Android-owned permission dialog; Not now preserves manual controls.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Permissions` |
| 3 | `Allow local music access` |
| 4 | `Music Copilot needs access to audio files so it can build your local library.` |
| 5 | `Continue` |
| 6 | `Not now` |
| 7 | `[ANDROID / SYSTEM] Permission dialog opens next` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-03` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
