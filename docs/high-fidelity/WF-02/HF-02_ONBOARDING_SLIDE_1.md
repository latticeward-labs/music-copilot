# HF-02_ONBOARDING_SLIDE_1 — Onboarding Slide 1

- **Issue:** #17
- **Phase:** 4 — High-Fidelity Primary Screen Families
- **Status:** **APPROVED_BASE_HIFI**
- **Wireframe source:** `docs/wireframes/app-states/WF-02_ONBOARDING_SLIDE_1.md`
- **Image:** [`HF-02_ONBOARDING_SLIDE_1.png`](HF-02_ONBOARDING_SLIDE_1.png)
- **PNG SHA-256:** `2628d1fd2acaec2416be7729afde7a45696f140eb6952afcb2641add9130c335`
- **Priority:** **P0**
- **Ownership:** App
- **User Flow:** UF-01
- **Canonical State IDs:** `S-02`
- **Golden Scenario:** GS-01

## Wireframe Hierarchy Lock

This high-fidelity artifact styles the approved `WF-02` wireframe hierarchy only. It does not add navigation, buttons, destinations, runtime authority, storage behavior, telemetry, backend/account/streaming behavior, Android Auto/iOS scope, wake-word behavior or conversational AI.

## Source Of Truth

Static approved product education.

## Approved Primary Action Surface

Continue to Slide 2.

## Exact UI Copy

The PNG was generated from the copy below. Any image output that changes this copy is not approved.

| Order | Visible copy |
|---:|---|
| 1 | `9:41` |
| 2 | `Music Copilot` |
| 3 | `Your music, on this device` |
| 4 | `Play and control local songs without an account or streaming service.` |
| 5 | `1 of 2` |
| 6 | `Continue` |

## Component Foundation Usage

- Visual direction: Night Signal.
- Tokens: `docs/design-system/foundations/TOKENS.json`.
- Component authority: `docs/design-system/components/COMPONENT_TOKENS.json`.
- Minimum target: 48dp; Driving primary controls preserve >=64dp and Mic >=72dp where applicable.
- State communication uses text plus shape/symbol; color is never the only cue.

## Accessibility And Review Notes

- TalkBack order follows the paired `WF-02` behavioral record.
- Text is specified as implementation copy; PNG text is visual evidence only.
- App/system boundaries remain owned by their source records.
- Privacy constraints are preserved: no raw audio, transcript, query/result telemetry or prohibited personal media metadata is introduced.
