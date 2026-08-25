# Low-fidelity Wireframes — MVP v1.0

- **Status:** **ISSUE #15 IN PROGRESS — Phase 2 single-page Figma structure PASS; individual Wireframes not yet approved**
- **Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Completed input:** [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3), [GitHub-native Design Sync](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md), [named Product/UX/Android/QA/Privacy input review](user-flows/design-sync/CROSS_ROLE_EVIDENCE_REVIEW_2026-08-23.md), and [126-state Wireframe Coverage Manifest](wireframes/WIREFRAME_COVERAGE_MATRIX.md), plus [Phase 2 Figma structure evidence](wireframes/FIGMA_STRUCTURE_PHASE_2_2026-08-25.md)
- **Canonical behavioral and approval workspace:** GitHub — [`docs/wireframes/`](wireframes/)
- **Official editable visual workfile:** [Figma — Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)
- **Figma file key / verified page:** `CC6I0e23Zwe6qMhRGaqRrT` / `0:1`
- **Figma verification date:** 25 August 2026
- **Active contract:** [Figma Wireframe Contract Addendum — 25 August 2026](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Wireframes validate structure, navigation, information hierarchy, controls, feedback and recovery. They do not approve final color, typography, illustration or animation.

GitHub controls behavior, traceability, review and approval. Figma is the official editable visual workfile. A Figma frame becomes reviewable only when its stable node URL and owning UF/State/GS identifiers are recorded in the corresponding repository `WF-##_SCREEN_STATE.md` artifact.

## Phase 1 coverage manifest

The [Wireframe Coverage Matrix](wireframes/WIREFRAME_COVERAGE_MATRIX.md) is the controlling construction manifest for this gate.

- 126/126 canonical State IDs classified; 0 missing, 0 duplicate, 0 unassigned.
- 45 App-visible Wireframe states.
- 61 variants/annotations inside a Wireframe.
- 20 Android/System-only transitions.
- GS-01 through GS-05 are connected to planned `WF-01` through `WF-10` packages.
- P1 and P2 states remain explicitly isolated; UF-13 remains P2 and non-blocking for P0.

This PASS authorizes frame construction only; it does not approve any visual frame.

## Phase 2 Figma structure

The official workfile uses one physical Page, `0:1`, now named `00_Index`. Under the user-approved Starter-tier accommodation, all original logical Page names are represented by 14 non-overlapping top-level Sections; four additional Sections define `P0`, `P1`, `P2`, and `System-only` lanes.

- Test text `Hello`: removed.
- Required naming contract: `WF-##_SCREEN_STATE`.
- Example: `WF-01_SPLASH_LOADING`.
- Structure audit: 18/18 Sections, 0 missing, 0 extra, 0 duplicate, 0 overlap, 0 invalid existing `WF-*` names.
- Evidence: [Figma Structure — Issue #15 Phase 2](wireframes/FIGMA_STRUCTURE_PHASE_2_2026-08-25.md).

This is a physical organization accommodation only; the 126-state Coverage Manifest and GitHub behavioral authority remain unchanged.

## Required screen/state matrix

| Screen | Required states |
|---|---|
| Splash | Loading, short recoverable startup/migration error |
| Onboarding | First Run, two-slide education, Skip Voice education where allowed |
| Permissions | Request/rationale, Granted, Denied, Permanently Denied and Settings recovery |
| Home / Library | Scanning, Empty, Ready, recoverable error |
| Search | Result, No Result, Error |
| Full Player | Playing, Paused, Corrupt File recovery |
| Driving Mode | Playing, Paused, No Track |
| Voice Overlay | Listening, Processing, Confirming, Executed, Failed |
| Liked Songs | Empty, Ready; P2 marker |
| Settings | Default, Updated, data/history clearing, privacy and Bluetooth |

Android permission dialogs, explicit Android Settings handoff, notification/lock-screen/external controls, calls, Audio Focus and wired/Bluetooth route transitions are system-only transition records or annotations. They must not be invented as app destinations.

## Annotation requirements

Every frame must identify:

- Stable `WF-##_SCREEN_STATE` identifier
- Figma page/node URL
- Owning UF, canonical State and GS IDs
- User goal and primary action
- Entry and exit paths
- Back behavior
- Visible source state or service/system dependency
- Loading, Empty, error, denial, interruption and recovery behavior
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

## Figma/GitHub synchronization gate

- A structural Figma change is not behaviorally approved until the repository record is updated and reviewed.
- A repository Wireframe record is not visually complete until it links the intended Figma node or records an explicit system-only transition.
- If Figma and GitHub conflict, the approved GitHub UF/State/GS contract controls.
- The historical [23 August 2026 no-Figma synchronization record](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md) remains unchanged; the dated addendum controls Issue #15 from 25 August 2026 onward.

## Approval checklist

- [ ] Every approved User Flow step maps to a repository Wireframe or documented system-only transition.
- [ ] No P0 state is missing.
- [ ] Navigation is explicit and consistent.
- [ ] Voice ambiguity and failure states are understandable without a new complex screen.
- [ ] Background and interruption handoffs are annotated.
- [ ] Stable Figma node links match repository `WF-*` identifiers and UF/State/GS ownership.
- [ ] Product, Design and Android feasibility review is complete.
- [ ] Approved repository artifact links, source UF/State/GS IDs and review date are recorded below.

## Repository and workfile locations

- [Wireframe workspace and artifact convention](wireframes/README.md)
- [126-state Wireframe Coverage Matrix](wireframes/WIREFRAME_COVERAGE_MATRIX.md)
- [Phase 2 single-page Figma structure evidence](wireframes/FIGMA_STRUCTURE_PHASE_2_2026-08-25.md)
- [Synchronized UF/State/GS design input](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)
- [Active Figma contract addendum](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- [Design Delivery Plan](DESIGN_DELIVERY_PLAN.md)
- [Figma Design Plan](FIGMA_DESIGN_PLAN.md)
- [Official Figma workfile](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)

The synchronized input passed the named cross-role evidence review, but that review does not satisfy the unchecked Wireframe-artifact approvals above. No individual Wireframe is approved yet; Issue #15 must populate, synchronize and review both workspaces.
