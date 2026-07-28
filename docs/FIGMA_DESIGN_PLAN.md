# Figma Design Plan — Music Copilot MVP v1.1

- **Status:** In Progress
- **Owner:** Latticeward Labs
- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Workfile:** [Music Copilot — MVP v1.1](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Product sources:** Music Copilot PRD v1.1 and MVP v1.1
- **Technical sources:** Issue #2, Technical Decisions and ADR-0001 through ADR-0006

## Design objective

Produce an implementation-ready, accessible Android experience for lawful local music, reliable manual playback, a distraction-minimized Driving Mode and an on-device-first push-to-talk Voice flow. The design must preserve complete manual control whenever Voice is unavailable or network-recognizer consent is absent or revoked.

## Figma structure

The Starter-plan three-page limit is intentional:

1. **00 — Foundations**
   - Product cover and decision summary
   - Light semantic tokens
   - Dark semantic tokens
   - Typography, spacing, radii, elevation and motion rules
   - Accessibility and Compose handoff notes
2. **01 — UX Flows**
   - First run, contextual permissions and scan
   - Library/search to queue and playback
   - Driving Mode entry/exit and Bluetooth suggestion
   - First Voice use, on-device availability and optional consent
   - Voice state machine, confirmation, retry and unavailable paths
   - Error and recovery branches
3. **02 — Screens & Handoff**
   - Sections: Wireframes, Components, Hi-Fi, Prototype and Specs
   - Compact baseline frames at 360×800dp
   - Responsive checks at 412×915dp

## Visual direction

- Material 3 is the implementation baseline, adapted to a distinctive music identity rather than copied as a stock template.
- Library and Player support Light and Dark semantic themes.
- Driving Mode is dark-first with a restrained high-contrast accent.
- Artwork may create subtle ambient color, but text and controls use stable semantic surfaces for predictable contrast.
- Roboto is the Beta/Compose baseline. Any later brand typeface requires explicit approval and Android licensing/performance review.
- In-app copy is English for the first Beta; product documentation remains Persian.

## Required flows

### F1 — First run and local library

Splash → value onboarding → media rationale → permission → scan → ready or empty/error → Library.

### F2 — Manual playback

Library/Search → track selection → queue → Mini Player → Full Player → background/screen-off representation → recovery.

### F3 — Driving Mode

Manual entry or selected-Bluetooth suggestion → responsibility notice → playing/paused/no-track surface → immediate exit.

### F4 — First Voice use

Microphone tap → rationale → microphone permission → on-device capability check.

- Available: continue to Listening.
- Unavailable: explain manual fallback.
- Optional network-capable recognizer: separate consent, clear disclosure and revocation path.
- Consent denied/revoked: manual playback remains complete.

### F5 — Voice command

Idle → Listening → Processing → Confirming when needed → Executed or Failed → Idle.

Direct high-confidence commands execute without opening a new screen. Timeout permits at most one retry. TTS copy is one short sentence.

### F6 — Recovery

Permission denial/permanent denial, corrupt or missing media, empty queue, route loss, Bluetooth disconnect, Voice unavailable, no match and ambiguous artist all have explicit stable states.

## Screen inventory

| Area | Required states |
|---|---|
| Splash | loading, migration error |
| Onboarding | value, optional Voice education, skip |
| Permissions & Privacy | media/microphone rationale, denied, permanently denied, on-device unavailable, optional consent, revocation |
| Home/Library | scanning, empty, ready, error |
| Search | query, results, no result, error |
| Full Player | loading, playing, paused, completed, corrupt/missing file |
| Driving Mode | no track, playing, paused, route warning, immediate exit |
| Voice Overlay | idle, listening, processing, confirming, executed, failed, timeout, unavailable |
| Liked Songs | P2/capacity-gated empty and ready |
| Settings | Bluetooth hint, Voice availability/consent, privacy, data clearing |

## Design system

### Semantic color roles

Primary, on-primary, secondary, surface, surface-container, on-surface, on-surface-variant, outline, error, driving-surface and driving-accent. Light and Dark use the same semantic names.

### Spacing and shape

- Spacing scale: 4, 8, 12, 16, 20, 24, 32, 40 and 48dp.
- Radius scale: 8, 12, 16, 24 and 28dp.
- Minimum touch target: 48×48dp.
- Primary Driving controls: 64–80dp target size.

### Reusable components

- App Bar
- Navigation Bar
- Button and Icon Button
- Search Field
- Track Row
- Album/Artist tile
- Mini Player
- Playback Controls
- Queue Row
- Driving Controls
- Voice State / Voice Action
- Permission/Privacy Card
- Empty, Error and Loading states
- Dialog, Snackbar, Slider and Switch

Music Copilot-specific repeated elements must be local components with variants; Material 3 assets may be reused when their property contract fits Compose implementation.

## Accessibility and driving rules

- WCAG AA contrast for normal text and essential controls.
- Meaning is never communicated by color alone.
- TalkBack labels and logical focus order are specified.
- Text scaling is reviewed without clipping or overlap.
- Driving Mode uses one layer, fixed controls, no typing, no nested menu and no long list.
- Immediate exit remains visible and reachable.
- Copy never claims the app makes driving safe; stationary setup is encouraged.

## Delivery sequence

1. Foundations and semantic tokens
2. Low-fidelity flow map
3. Low-fidelity core frames and all critical states
4. Reusable components and variants
5. Hi-fi Library, Player, Driving and Voice surfaces
6. Stationary golden-scenario prototype
7. Accessibility and responsive review
8. Compose-ready annotations and Issue #3 evidence

## Exit gate

Issue #3 moves to Review only when every required flow/state is represented, components and tokens are reusable, the stationary golden scenario is testable, accessibility checks are recorded and the Figma workfile contains implementation notes linked to PRD/MVP v1.1.
