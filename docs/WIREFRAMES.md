# Low-fidelity Wireframes — MVP v1.0

- **Status:** **READY FOR ISSUE #15 - User Flow/State/Golden synchronization PASS; Wireframes not yet approved**
- **Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Completed input:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3), [GitHub-native Design Sync](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md), and [named Product/UX/Android/QA/Privacy input review](user-flows/design-sync/CROSS_ROLE_EVIDENCE_REVIEW_2026-08-23.md)
- **Canonical workspace:** [`docs/wireframes/`](wireframes/)  
- **Figma:** **N/A / Not Required**
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
- [ ] Approved repository artifact links, source UF/State/GS IDs and review date are recorded below.

## Repository-native artifact locations

- [Wireframe workspace and artifact convention](wireframes/README.md)
- [Synchronized UF/State/GS design input](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)
- [GitHub-native Design Delivery Plan](DESIGN_DELIVERY_PLAN.md)

The synchronized input passed the named cross-role evidence review, but that review does not satisfy the unchecked Wireframe-artifact approvals above. No individual Wireframe is approved yet; Issue #15 must populate and review the workspace.
