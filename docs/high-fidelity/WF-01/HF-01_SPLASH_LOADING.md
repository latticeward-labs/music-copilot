# HF-01_SPLASH_LOADING — Splash Loading

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-01_SPLASH_LOADING.md`
- **Image:** [`HF-01_SPLASH_LOADING.png`](HF-01_SPLASH_LOADING.png)
- **PNG SHA-256:** `b2241a28fae3edb455f554e89537e07dc89ab03107ab9592e08d4ca855d248f3`
- **Priority:** **P0**
- **Ownership:** App
- **User Flow:** UF-01
- **Canonical State IDs:** `S-01`
- **Golden Scenario:** GS-01

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-01` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Verified local migration/readiness and platform state.

## Approved Primary Action Surface

Wait; no repeated action while routing is active.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Music Copilot` |
| 3 | `Preparing your local library...` |
| 4 | `Local files only` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-01` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
