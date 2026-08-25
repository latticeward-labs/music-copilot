# Low-fidelity Foundations — Issue #15 Phase 3

**Status:** **PASS / COMPLETE for construction foundations**  
**Visual board:** [FOUNDATIONS_BOARD.svg](FOUNDATIONS_BOARD.svg)  
**Machine-readable tokens:** [tokens.json](tokens.json)  
**Validation:** [FOUNDATIONS_VALIDATION.md](FOUNDATIONS_VALIDATION.md)  
**Scope:** Structural hierarchy only; not final UI design

## Android reference frame

- Reference viewport: `360 × 800dp`.
- System status area: 24dp reference only.
- App bar: 56dp.
- Content is responsive and must not assume a fixed physical pixel density.
- Bottom navigation: 64dp when present.
- Android system navigation area is a reference annotation, not an app-owned control.

## Palette

The approved palette is limited to seven grayscale primitives (`000`, `050`, `100`, `300`, `500`, `700`, `900`) and semantic aliases. It communicates hierarchy, not final brand color. Disabled, error, Empty, loading and selected states must also include text, icon/shape or pattern; color alone is prohibited.

## Typography

Only four hierarchy roles are approved: `Title`, `Heading`, `Body`, `Label`. They are implementation references in sp-like units, not final typography approval. Text containers must reflow and grow vertically under font scaling; fixed-height clipping is prohibited.

## Base component contract

| Component family | Reference geometry | TalkBack order | Non-color state rule |
|---|---|---|---|
| Android Device Frame | 360×800 | App content excludes system chrome | System areas are labeled |
| App Bar | 360×56; action 48×48 | Back/action → title → trailing action | Action has visible text or accessible label |
| Bottom Navigation | 360×64; each target ≥48 | Left to right, selected item announced | Selected marker + label + state text |
| Track Row | 360×64; trailing action 48×48 | Track title → artist → action | Unavailable state uses label and icon/shape |
| Button | height 48; Primary/Secondary | Label, then state | Fill difference plus border/label |
| Playback Controls | three 48×48 targets | Previous → Play/Pause → Next | Play/Pause uses distinct label and glyph |
| Permission Rationale | responsive block | Heading → purpose → Primary → Secondary | Denied state includes explicit recovery copy |
| Status Block | Error/Empty/Loading | State heading → explanation → action | State word and symbol accompany tone |
| Voice Overlay | responsive overlay; Cancel 48 | State → recognized text → choices/action → Cancel | Listening/Processing/etc. are written labels |
| Confirmation Choices | each choice ≥48 high | Prompt → choice 1 → choice 2 → Cancel | Choices are numbered/labeled, never color-only |
| System-only Annotation | dashed bounded note | Read after owning app handoff | `[ANDROID / SYSTEM]` prefix is mandatory |

## Accessibility annotations

1. **Touch target:** every interactive target is at least `48dp × 48dp`; larger targets are preferred in Driving Mode.
2. **TalkBack:** reading order follows the intended task sequence, not raw SVG drawing order. The paired `WF-*` Markdown must list any exception explicitly.
3. **Scalable text:** all body and action copy supports user scaling up to 200% without clipping, overlap or loss of action access.
4. **Non-color-only:** selected, disabled, loading, Empty, error, permission and voice states use a state label plus icon/shape/pattern where applicable.
5. **System boundary:** Android-owned dialogs/settings/notification/route surfaces are described as handoffs and never drawn as app destinations.

## Use in Wireframes

Every `WF-*` SVG must reuse these values and name its visible groups using the component-family names above. Deviations require a reason in the paired Markdown and review in the same PR.

