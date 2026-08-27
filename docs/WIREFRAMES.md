# Low-fidelity Wireframes — MVP v1.0

- **Status:** **ISSUE #15 PHASE 8 PASS — cross-role feasibility review complete; ready to close**
- **Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Completed input:** [Phase 8 cross-role review](wireframes/review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md), [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3), [GitHub-native Design Sync](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md), [named Product/UX/Android/QA/Privacy input review](user-flows/design-sync/CROSS_ROLE_EVIDENCE_REVIEW_2026-08-23.md), [126-state Coverage Manifest](wireframes/WIREFRAME_COVERAGE_MATRIX.md), [Phase 3 Foundations](wireframes/foundations/README.md), [Phase 4 32-frame register](wireframes/app-states/README.md), [Phase 5 Recovery/System register](wireframes/recovery-system/README.md), and [Phase 6 complete artifact index](wireframes/README.md)
- **Canonical behavioral and approval workspace:** GitHub — [`docs/wireframes/`](wireframes/)
- **Official editable visual workfile:** [`docs/wireframes/`](wireframes/) — Markdown + SVG + JSON
- **Stable workspace index:** [GitHub-native Workspace Index](wireframes/WORKSPACE_INDEX.md)
- **Active contract:** [GitHub-native Wireframe Contract Addendum — 25 August 2026](wireframes/GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- **Historical Figma reference:** [Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1) (`CC6I0e23Zwe6qMhRGaqRrT`, page `0:1`)
- **Authority:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Wireframes validate structure, navigation, information hierarchy, controls, feedback and recovery. They do not approve final color, typography, illustration or animation.

GitHub controls behavior, low-fidelity visuals, traceability, review and approval. A repository SVG becomes reviewable only when its stable path and owning UF/State/GS identifiers are recorded in the paired `WF-##_SCREEN_STATE.md` artifact.

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

## Phase 3 GitHub-native Foundations

The repository-native foundation set is complete and validated:

- [Foundations specification](wireframes/foundations/README.md)
- [Rendered SVG board](wireframes/foundations/FOUNDATIONS_BOARD.svg)
- [Machine-readable design tokens](wireframes/foundations/tokens.json)
- [Phase 3 validation evidence](wireframes/foundations/FOUNDATIONS_VALIDATION.md)

It defines the Android 360×800 reference frame, seven-step grayscale palette, four hierarchy-only typography roles, 11 component families, 48dp minimum targets, TalkBack order, scalable-text and non-color-only state contracts. This PASS authorizes construction of `WF-*` artifacts; it does not approve any individual screen.
## Phase 4 — 32 App State Frames

- Register: [32 paired Markdown/SVG artifacts](wireframes/app-states/README.md)
- Machine-readable traceability: [FRAME_REGISTER.json](wireframes/app-states/FRAME_REGISTER.json)
- Validation: [PHASE_4_VALIDATION.md](wireframes/app-states/PHASE_4_VALIDATION.md)
- Totals: 32 Markdown + 32 SVG; 32 stable pairs; 0 duplicate; 32/32 valid SVG/XML.
- Accessibility: 159 interactive targets checked at 48dp minimum; 32/32 TalkBack, scalable-text and non-color-only annotations.
- P2 isolation: both Liked Songs frames visibly carry `P2 / Removable`.

**Decision:** Phase 4 construction PASS / COMPLETE. Individual artifacts are reviewable but are not yet cross-role approved.

## Phase 5 — Recovery and System-only Transitions

- Register: [13 paired transition records/diagrams](wireframes/recovery-system/README.md)
- Machine traceability: [SYSTEM_TRANSITION_REGISTER.json](wireframes/recovery-system/SYSTEM_TRANSITION_REGISTER.json)
- Safe-return proof: [RECOVERY_TERMINAL_MATRIX.md](wireframes/recovery-system/RECOVERY_TERMINAL_MATRIX.md) and [overview SVG](wireframes/recovery-system/RECOVERY_TERMINAL_OVERVIEW.svg)
- Validation: [PHASE_5_VALIDATION.md](wireframes/recovery-system/PHASE_5_VALIDATION.md)
- Coverage: 20/20 canonical Android/System-only states; 0 missing; 0 unexpected.
- Terminals: Manual Playback or service-owned Safe Paused only.

**Decision:** Phase 5 construction/repository validation PASS / COMPLETE. System surfaces are ownership-separated transitions, not invented app screens. Cross-frame navigation and Product/Design/Android/QA approval remain pending.

## Phase 6 — Repository Artifacts

- Complete human-readable index: [45 Markdown/SVG pairs](wireframes/README.md)
- Machine-readable index: [ARTIFACT_REGISTER.json](wireframes/ARTIFACT_REGISTER.json)
- Validation: [PHASE_6_REPOSITORY_ARTIFACT_VALIDATION.md](wireframes/PHASE_6_REPOSITORY_ARTIFACT_VALIDATION.md)
- App Frame schema: 32/32 records contain all required behavioral, navigation, authority, persistence, recovery, accessibility, Driving and priority fields.
- Provenance: every artifact records current-main paths, immutable SVG URL, construction PR/commit and truthful review disposition.
- Naming integrity: 0 renamed, 0 duplicated, 45/45 unique IDs.

**Decision:** Phase 6 repository artifact production PASS / COMPLETE. Named cross-role approval remains pending.

## Phase 7 — Traceability and Validation

- [Machine traceability register](wireframes/PHASE_7_TRACEABILITY_REGISTER.json)
- [Navigation and Behavioral Audit](wireframes/PHASE_7_NAVIGATION_BEHAVIOR_AUDIT.md)
- [Link and Active-workfile Audit](wireframes/PHASE_7_LINK_AUDIT.md)
- [Combined validation](wireframes/PHASE_7_TRACEABILITY_VALIDATION.md)
- UF 13/13; GS 5/5; screen families 10/10; State dispositions 126/126.
- P0 disposition 106/106; variant annotations 61/61; System-only states 20/20.
- Entry/Exit/Back 32/32; system return/authority 13/13; connected handoffs 8/8.
- Voice certainty, ambiguity, not-found, timeout and one-Retry recovery PASS.
- Driving one-layer/immediate Exit/no typing/no long list PASS.
- Background playback/UI recreation authority and safe-resume rules PASS.
- Internal broken links 0; invalid validated GitHub paths 0; active Figma dependencies 0.

**Decision:** Phase 7 Traceability and Validation PASS / COMPLETE. Named Product/Design/Android/QA approval remains pending.

## Phase 8 — Review and correction

- [Cross-role review record](wireframes/review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md)
- [Machine-readable findings](wireframes/review/PHASE_8_FINDINGS_REGISTER.json)
- [Phase 8 validation](wireframes/review/PHASE_8_VALIDATION.md)
- [Phase 8 PR #42](https://github.com/latticeward-labs/music-copilot/pull/42) · [evidence commit `475089654254c96b6adda27c19b697e26dfe798d`](https://github.com/latticeward-labs/music-copilot/commit/475089654254c96b6adda27c19b697e26dfe798d)
- Reviewer: **OpenAI Codex**; type: **AI-assisted cross-role evidence reviewer**; date: **27 August 2026**.
- Product, Design/UX, Android feasibility, Accessibility and Privacy: **PASS at Wireframe feasibility level**.
- Blocker: 0 found / 0 open.
- Major: 2 found / 2 resolved / 0 open.
- Minor: 2 recorded as downstream implementation/legal validation / 2 open.
- Major geometry correction: all 32 app-state SVGs changed from an inconsistent 360×820 inner frame to the approved 360×800dp Foundations contract.
- Review screenshots are repository-rendered SVG evidence; stable Markdown/SVG paths and the Phase 8 commit/PR replace Figma Node links.

**Decision:** Phase 8 review PASS / COMPLETE. No Wireframe Blocker or Major remains open.

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
- Stable paired Markdown/SVG repository paths
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

## GitHub-native synchronization gate

- A structural SVG change is not behaviorally approved until the paired Markdown record is updated and reviewed.
- A repository Wireframe record is not visually complete until it links its paired SVG or records an explicit system-only transition.
- If SVG and Markdown conflict, the approved GitHub UF/State/GS behavioral contract controls.
- The historical [23 August 2026 no-Figma synchronization record](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md) remains unchanged; the dated addendum controls Issue #15 from 25 August 2026 onward.

## Approval checklist

- [x] Every approved User Flow step maps to a repository Wireframe, variant annotation or documented system-only transition.
- [x] No P0 state is missing — 106/106 dispositions PASS.
- [x] Navigation, Back, Exit and connected handoffs are explicit and consistent.
- [x] Voice certainty, ambiguity, not-found, timeout and one-Retry failure states are covered without a new complex screen.
- [x] Background, recreation, interruption and route handoffs are annotated and service-authoritative.
- [x] Stable SVG paths match repository `WF-*` identifiers and UF/State/GS ownership.
- [x] Product, Design/UX, Android feasibility, Accessibility and Privacy review is complete — [Phase 8 evidence](wireframes/review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md).
- [x] Repository artifact links, source UF/State/GS IDs, construction provenance, validation date and current review disposition are recorded below.

## Repository and workfile locations

- [Wireframe workspace and artifact convention](wireframes/README.md)
- [Phase 4 — 32 App State Frames](wireframes/app-states/README.md)
- [Phase 4 validation](wireframes/app-states/PHASE_4_VALIDATION.md)
- [Phase 5 — Recovery and System-only Transitions](wireframes/recovery-system/README.md)
- [Phase 5 terminal matrix](wireframes/recovery-system/RECOVERY_TERMINAL_MATRIX.md)
- [Phase 5 validation](wireframes/recovery-system/PHASE_5_VALIDATION.md)
- [Phase 6 machine-readable artifact index](wireframes/ARTIFACT_REGISTER.json)
- [Phase 6 validation](wireframes/PHASE_6_REPOSITORY_ARTIFACT_VALIDATION.md)
- [Phase 7 machine traceability register](wireframes/PHASE_7_TRACEABILITY_REGISTER.json)
- [Phase 7 Navigation and Behavioral Audit](wireframes/PHASE_7_NAVIGATION_BEHAVIOR_AUDIT.md)
- [Phase 7 Link Audit](wireframes/PHASE_7_LINK_AUDIT.md)
- [Phase 7 combined validation](wireframes/PHASE_7_TRACEABILITY_VALIDATION.md)
- [Phase 8 cross-role review](wireframes/review/PHASE_8_CROSS_ROLE_REVIEW_2026-08-27.md)
- [Phase 8 findings register](wireframes/review/PHASE_8_FINDINGS_REGISTER.json)
- [Phase 8 validation](wireframes/review/PHASE_8_VALIDATION.md)
- [126-state Wireframe Coverage Matrix](wireframes/WIREFRAME_COVERAGE_MATRIX.md)
- [Historical Phase 2 single-page Figma structure evidence](wireframes/FIGMA_STRUCTURE_PHASE_2_2026-08-25.md)
- [GitHub-native workspace index](wireframes/WORKSPACE_INDEX.md)
- [Phase 3 Foundations](wireframes/foundations/README.md)
- [Synchronized UF/State/GS design input](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)
- [Active GitHub-native contract addendum](wireframes/GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- [Historical Figma contract addendum](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)
- [Design Delivery Plan](DESIGN_DELIVERY_PLAN.md)
- [Figma Design Plan](FIGMA_DESIGN_PLAN.md)
- [Official Figma workfile](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)

Phase 8 reviewed the complete 45-pair repository-native Wireframe set. All Blocker/Major findings are closed, every artifact has a dated review disposition, and GitHub paths/commits provide the controlling visual evidence. Issue #15 satisfies its Wireframe exit gate; downstream Compose/device/vehicle and Legal checks are not misrepresented as completed.