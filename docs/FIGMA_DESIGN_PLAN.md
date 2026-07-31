# Figma Design Plan — Music Copilot MVP v1.0

- **Workfile:** [Music Copilot — MVP v1.0](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Product sources:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)
- **Workflow:** [Delivery Workflow](DELIVERY_WORKFLOW.md)

Figma work is split into separate approval gates. User Flow, Wireframe and visual-design decisions must not be mixed into one uncontrolled design task.

## Phase A — User Flows and state inventory

- **Tracking:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Status:** Ready
- **Output:** Implementation-independent flow diagrams and complete state inventory

Required flow families:

- First use, permissions and recovery
- Local scan, browse and search
- Manual and background playback
- Driving Mode entry/use/exit
- Voice certainty, ambiguity, failure and retry
- Calls, Audio Focus, headset and Bluetooth-route recovery
- Settings, privacy/data clearing and P2 Liked Songs

No visual styling is approved in this phase.

## Phase B — Low-fidelity Wireframes

- **Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Status:** Blocked by #3
- **Output:** Grayscale structural frames, navigation, hierarchy, controls, feedback and recovery

Required screen inventory:

1. Splash — Loading, short recoverable error
2. Onboarding — First Run, Skip where allowed
3. Permissions — Granted, Denied, Permanently Denied
4. Home / Library — Scanning, Empty, Ready
5. Search — Result, No Result, Error
6. Full Player — Playing, Paused, Corrupt File
7. Driving Mode — Playing, Paused, No Track
8. Voice Overlay — Listening, Processing, Confirming, Executed, Failed
9. Liked Songs — Empty, Ready; P2 marker
10. Settings — Default, Updated, Privacy, Bluetooth and data clearing

## Phase C — Architecture feasibility review

- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Status:** Blocked by #2, #3 and #15

Design and Android review the approved flows/Wireframes against lifecycle, background playback, state ownership, service boundaries, permission handling, Voice behavior and audio-route interruptions. Structural corrections return to Phase A or B.

## Phase D — High-fidelity UI system and prototype

- **Tracking:** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Status:** Blocked by #15 and #16
- **Output:** Foundations, reusable components, high-fidelity screens, clickable prototype and handoff

Required design-system coverage:

- Color roles and dark-theme behavior
- Typography and text scaling
- Spacing/grid/layout rules
- Iconography
- Player and Voice controls
- List/search/permission/error components
- Loading, empty, disabled and failure variants
- Accessibility annotations and TalkBack reading order
- Compose-ready tokens and component specifications

## Driving Mode rules across all phases

- One layer
- Fixed controls and immediate exit
- At least 48dp touch targets; primary controls preferably larger
- High contrast, readable/scalable typography and limited motion
- Four primary controls, microphone, current title/artist/artwork and exit
- No typing, nested menu, deep navigation or long result list
- Short TTS/error copy and immediate manual fallback
- No claim that the app makes driving safe; road attention remains the driver’s responsibility

## Required prototype scenarios

1. First use through Library readiness
2. Search and manual playback
3. Driving Mode entry and exit
4. Golden scenario: microphone tap -> “Next song” -> next track -> title update -> one short response -> no extra screen
5. Ambiguous artist confirmation
6. Voice failure and manual fallback
7. Permission denial and Settings recovery
8. Empty Library, No Track and corrupt-file recovery

## Handoff rule

A structural change discovered during high-fidelity design must return to User Flow/Wireframe review. It must not be hidden inside a polished screen or deferred to implementation.
