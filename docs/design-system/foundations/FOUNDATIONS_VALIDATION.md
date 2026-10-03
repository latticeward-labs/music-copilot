# Issue #17 Phase 2 — Design Foundations Validation

- **Date:** 3 October 2026
- **Issue:** [#17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Phase:** 2 — Visual Direction and Design Foundations
- **Result:** **PASS / COMPLETE at Design-Foundation level**
- **Source archive SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Phase 1 merge:** `c36ea45824f90a6a9992460f7ac54fcc904111bd`
- **Repository board Git blob:** `9eb01095e5fc5740a38b73a69f69d0c4402a372d` (7,586 bytes)

## Validation matrix

| Check | Expected | Result |
|---|---|---|
| Final high-fidelity screens created in Phase 2 | 0 | **PASS — 0 claimed** |
| Dark visual baseline | Defined | **PASS** |
| Color semantic roles | Defined | **PASS** |
| Typography scale | Defined in sp | **PASS** |
| Spacing/grid | Defined in dp | **PASS** |
| Radius/elevation | Defined | **PASS** |
| Iconography | Defined | **PASS** |
| Artwork treatment | Defined | **PASS** |
| Motion timings/restrictions | Defined | **PASS** |
| Default/Pressed/Focused/Selected/Disabled/Error/Loading rules | Defined | **PASS** |
| Minimum touch target | ≥48dp | **PASS — 48dp** |
| Driving primary controls | Larger than generic minimum | **PASS — ≥64dp** |
| Driving microphone | Larger high-emphasis target | **PASS — ≥72dp** |
| Driving immediate Exit | Required | **PASS** |
| Driving one-layer/no typing/no nested navigation/no long lists | Required | **PASS** |
| Driving fixed manual controls | Previous / Play-Pause / Next / Seek Back | **PASS** |
| Driving visible playback states | Playing / Paused / No Track | **PASS** |
| 200% text/reflow design requirement | Required | **PASS** |
| TalkBack order/labels | Required | **PASS** |
| Non-color-only state | Required | **PASS** |
| Limited motion | Required | **PASS** |
| System-only boundaries preserved | Required | **PASS** |
| Theme setting invented | 0 | **PASS — none introduced** |
| Backend/account/streaming scope introduced | 0 | **PASS — none introduced** |
| Machine-readable token file | Valid JSON structure | **PASS** |
| Visual board | PNG review evidence | **PASS — repository blob `9eb01095e5fc5740a38b73a69f69d0c4402a372d`** |

## Contrast checks

All following ordinary text/semantic pairs exceed the 4.5:1 normal-text target:

| Pair | Ratio |
|---|---:|
| Text Primary / Background | 18.15:1 |
| Text Secondary / Background | 12.52:1 |
| Text Muted / Background | 7.80:1 |
| Primary / Background | 11.91:1 |
| On Primary / Primary | 11.71:1 |
| Error / Background | 7.35:1 |
| Success / Background | 12.02:1 |
| Warning / Background | 13.51:1 |
| Secondary / Background | 8.56:1 |
| Text Primary / Surface | 16.76:1 |
| Text Secondary / Surface | 11.56:1 |
| Text Muted / Surface | 7.20:1 |

These checks validate the frozen semantic pairs only. Later component/screen reviews must recheck actual text/background combinations, opacity, artwork overlays and device rendering.

## Source/decision separation

- Android 10+, Compose, 48dp, scalable text, TalkBack, Driving restrictions, Voice behavior, system-only boundaries and product-scope exclusions are **source-derived constraints**.
- Night Signal naming, dark-first palette, exact color hex values, typography sizes, spacing aliases, radii, elevation and motion durations are **Issue #17 Phase-2 visual design decisions**.
- No visual choice is allowed to override a source-derived behavior/ownership requirement.

## Visual-board integrity

`FOUNDATIONS_BOARD.png` is a visual summary of the same foundation values. It is not the engineering authority. Exact values are controlled by `FOUNDATIONS.md` and `TOKENS.json`.

## Exit decision

**PHASE 2 PASS / COMPLETE at Design-Foundation level.**

Phase 3 Component System may now reuse these tokens. Any later change to a Phase-2 token must update `FOUNDATIONS.md`, `TOKENS.json`, the visual board when visually relevant, and the review evidence in the same reviewed change.
