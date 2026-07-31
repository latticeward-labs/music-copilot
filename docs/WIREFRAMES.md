# Low-fidelity Wireframes — MVP v1.0

- **Status:** Blocked by User Flow approval
- **Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Depends on:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- **Workfile:** [Music Copilot — MVP v1.0](https://www.figma.com/design/MKQWSMNkUFG5H6GNRU7GYF)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Wireframes validate structure, navigation, information hierarchy, controls, feedback and recovery. They do not approve final color, typography, illustration or animation.

## Required screen/state matrix

| Screen | Required states |
|---|---|
| Splash | Loading, short recoverable startup/migration error |
| Onboarding | First Run, Skip Voice education where allowed |
| Permissions | Granted, Denied, Permanently Denied for required access |
| Home / Library | Scanning, Empty, Ready, recoverable error |
| Search | Result, No Result, Error |
| Full Player | Playing, Paused, Corrupt File recovery |
| Driving Mode | Playing, Paused, No Track |
| Voice Overlay | Listening, Processing, Confirming, Executed, Failed |
| Liked Songs | Empty, Ready; P2 marker |
| Settings | Default, Updated, data/history clearing, privacy and Bluetooth |

## Annotation requirements

Every frame must identify:

- User goal and primary action
- Entry and exit paths
- Back behavior
- Source state or service dependency
- Loading, error and recovery behavior
- Persistence expectation after UI closure/recreation
- Accessibility notes
- Driving restrictions
- P0/P1/P2 classification

## Driving Mode rules

- One interaction layer
- Fixed primary controls and immediate exit
- Minimum 48dp touch targets; primary controls preferably larger
- High contrast, scalable text and TalkBack-ready reading order
- No typing, deep navigation, nested menu or long result list
- Short feedback and immediate return to manual control after Voice failure

## Approval checklist

- [ ] Every approved User Flow step maps to a frame or documented system-only transition.
- [ ] No P0 state is missing.
- [ ] Navigation is explicit and consistent.
- [ ] Voice ambiguity and failure states are understandable without a new complex screen.
- [ ] Background and interruption handoffs are annotated.
- [ ] Product, Design and Android feasibility review is complete.
- [ ] Approved Figma page links and review date are recorded below.

## Approved artifact links

_To be completed by Issue #15._
