# Design Delivery Plan — Music Copilot MVP v1.0

- **Version:** MVP v1.0
- **Status:** **Current through Issue #17 Phase 4 Primary High-Fidelity — PASS / COMPLETE**
- **Updated:** 4 October 2026
- **Architecture tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Design tracking:** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Canonical delivery workspace:** GitHub repository `latticeward-labs/music-copilot`
- **Active Issue #17 contract:** [Repository-native High-Fidelity Delivery Contract Addendum](ISSUE_17_REPOSITORY_HIFI_CONTRACT_ADDENDUM_2026-10-03.md)
- **Official low-fidelity visual workfile:** [`docs/wireframes/`](wireframes/) — Markdown + SVG + JSON
- **Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)
- **Final architecture authority:** [Software Architecture](SOFTWARE_ARCHITECTURE.md) + Accepted ADRs
- **Attached English source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Historical Figma records:** retained as provenance only; not active Issue #17 acceptance dependencies

> GitHub is the canonical delivery, behavior, architecture, traceability, review and approval workspace for Issue #17. Repository-stored high-fidelity images plus paired Markdown/JSON specifications are the canonical visual-delivery evidence. Image-generation tooling is a production method, not an authority.

## Authority model

| Concern | Authority |
|---|---|
| Product scope and priority | Controlling English PRD/MVP source package represented in GitHub |
| User Flow, State and Golden behavior | Approved GitHub records |
| Low-fidelity structure | Completed Issue #15 artifacts under `docs/wireframes/` |
| Architecture ownership/dependency/runtime behavior | Final `docs/SOFTWARE_ARCHITECTURE.md`, Accepted ADRs and reviewed corrections |
| Visual foundations and Design System | Repository specs/tokens + approved visual evidence under `docs/design-system/` |
| High-fidelity screens and critical app-visible states | Paired repository image/spec artifacts under `docs/high-fidelity/` |
| Interactive prototype | Repository-native prototype source/routes under `docs/prototype/` |
| Engineering handoff | Repository specifications under `docs/handoff/` |
| Behavioral/component traceability and acceptance evidence | GitHub |
| Review, findings and approval history | GitHub PR/Issue/review records |

### Conflict rule

If a high-fidelity artifact conflicts with an approved GitHub behavioral, architecture, traceability or acceptance record, the approved source record controls until a reviewed change resolves the conflict.

Structural UX changes route back to the Wireframe/User Flow gate. Consequential architecture changes route to Issue #16 or a new/superseding ADR. Product-scope changes require PRD/MVP governance. Generated images may not silently introduce new behavior or v1.1 scope.

## Gate A — User Flows, State Inventory and Golden Scenarios

- Tracking: [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3)
- Status: **PASS / COMPLETE / CLOSED — 23 August 2026**
- Coverage: UF-01..13, 126 canonical states, GS-01..05 and Cross-scenario Validation.

## Gate B — Low-fidelity Wireframes

- Tracking: [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- Index: [WIREFRAMES.md](WIREFRAMES.md)
- Repository workspace: [`wireframes/`](wireframes/)
- Status: **PASS / COMPLETE / CLOSED — 27 August 2026**
- Phase 8 review/corrections: [PR #42](https://github.com/latticeward-labs/music-copilot/pull/42)
- Phase 9 closeout: [PR #43](https://github.com/latticeward-labs/music-copilot/pull/43)
- Acceptance: complete paired Markdown/SVG artifacts and system/recovery records; open Blocker/Major findings are zero.

Issue #15 remains the approved structural baseline. Issue #17 may style and specify it, but may not silently restructure it.

## Gate C — Software Architecture

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Main baseline: [SOFTWARE_ARCHITECTURE.md](SOFTWARE_ARCHITECTURE.md)
- Traceability: [Architecture Traceability Matrix](architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- Validation: [Issue #16 Validation/Test Architecture](architecture/ISSUE_16_VALIDATION.md)
- Cross-role review: [Phase 10 Review](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)
- Final acceptance: [Phase 11 Audit](architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md)
- Final delivery: [PR #46](https://github.com/latticeward-labs/music-copilot/pull/46)
- Status: **PASS / COMPLETE / MERGED — Phase 11; open Blocker 0 / Major 0**

The final architecture fixes ownership, lifecycle, dependency direction, runtime policy, persistence, navigation, error/recovery, privacy, test seams and package strategy.

`VoiceSessionState.Confirming` exposes the immutable one/two candidate labels required by `WF-08_VOICE_CONFIRMING`. Runtime Compose semantics/TalkBack/focus/reflow remain downstream proof under `PH10-MIN-001`, jointly owned by implementation and Issue #17 handoff.

## Gate D — Design System, high-fidelity design, prototype and handoff

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Contract: [Repository-native High-Fidelity Delivery Contract Addendum](ISSUE_17_REPOSITORY_HIFI_CONTRACT_ADDENDUM_2026-10-03.md)
- Dedicated execution branch: `issue-17/repository-hifi-delivery`
- Status: **IN PROGRESS — Phase 4 Primary High-Fidelity PASS / COMPLETE**
- Design System workspace: `docs/design-system/`
- High-fidelity workspace: `docs/high-fidelity/`
- Prototype workspace: `docs/prototype/`
- Handoff workspace: `docs/handoff/`

### Phase 1 — frozen design input baseline

- Source manifest: [`docs/design-system/ISSUE_17_SOURCE_MANIFEST.md`](design-system/ISSUE_17_SOURCE_MANIFEST.md)
- Human-readable 126-state matrix: [`HIFI_TRACEABILITY_MATRIX.md`](design-system/HIFI_TRACEABILITY_MATRIX.md)
- Machine-readable register: [`HIFI_TRACEABILITY_REGISTER.json`](design-system/HIFI_TRACEABILITY_REGISTER.json)
- Validation: [`ISSUE_17_PHASE_1_VALIDATION.md`](design-system/ISSUE_17_PHASE_1_VALIDATION.md)
- Frozen baseline commit: `b4ac3bb3ccf299bd282e6d1ba3600b7136dd5669`
- Result: **PASS — 126/126 states have WF / UF / State / GS / Priority / Ownership and one initial high-fidelity disposition.**
- Next gate: Phase 2 Design Foundations / Design Tokens. No high-fidelity screen image is approved by Phase 1.

### Phase 2 — visual direction and Design Foundations

- Visual direction: **Night Signal** — dark-first, low-distraction, high-contrast.
- Foundation specification: [`FOUNDATIONS.md`](design-system/foundations/FOUNDATIONS.md)
- Machine-readable tokens: [`TOKENS.json`](design-system/foundations/TOKENS.json)
- Visual review board: [`FOUNDATIONS_BOARD.png`](design-system/foundations/FOUNDATIONS_BOARD.png)
- Validation: [`FOUNDATIONS_VALIDATION.md`](design-system/foundations/FOUNDATIONS_VALIDATION.md)
- Source-locked constraints preserved: Android 10+, Compose, ≥48dp targets, 200% text reflow, TalkBack, sufficient contrast, non-color-only state, limited motion and system-only boundaries.
- Driving foundation preserved: one layer, immediate Exit, no typing/nested navigation/long lists, fixed Previous / Play-Pause / Next / Seek Back + Mic, and visible Playing / Paused / No Track + title / artist / artwork.
- Exact colors, typography, spacing, radii, elevation and motion values are controlled by Markdown/JSON; the PNG is review evidence only.
- Result: **PASS / COMPLETE at Design-Foundation level.**
- Next gate: Phase 3 Component System. No final high-fidelity screen is approved by Phase 2.

### Phase 3 — reusable component system

- Component catalog: [`docs/design-system/components/COMPONENT_CATALOG.md`](design-system/components/COMPONENT_CATALOG.md)
- Machine-readable component tokens: [`COMPONENT_TOKENS.json`](design-system/components/COMPONENT_TOKENS.json)
- App-frame component coverage: [`COMPONENT_COVERAGE_MATRIX.md`](design-system/components/COMPONENT_COVERAGE_MATRIX.md)
- Validation: [`PHASE_3_VALIDATION.md`](design-system/components/PHASE_3_VALIDATION.md)
- Visual evidence:
  - [`COMPONENT_BOARD_CORE.png`](design-system/components/COMPONENT_BOARD_CORE.png)
  - [`COMPONENT_BOARD_PLAYBACK_MEDIA.png`](design-system/components/COMPONENT_BOARD_PLAYBACK_MEDIA.png)
  - [`COMPONENT_BOARD_VOICE_RECOVERY.png`](design-system/components/COMPONENT_BOARD_VOICE_RECOVERY.png)
- Result: **PASS — 23 reusable component families; 32/32 approved app-visible frames mapped; no one-off recurring control required by the approved baseline.**
- All interactive components preserve the minimum 48dp target baseline.
- Driving variants preserve primary controls at ≥64dp, Mic at ≥72dp and immediate Exit at ≥56dp.
- Component states preserve Default, Pressed, Focused, Selected, Disabled, Error and Loading behavior without color-only communication.
- Accessibility rules preserve TalkBack labels/order, visible focus, scalable text and 200% reflow expectations.
- Playback controls remain UI observers/command senders only; the service-owned MediaSession remains the playback/queue/position authority.
- Voice feedback remains bounded to Idle, Listening, Processing, Confirming, Executed and Failed.
- Voice confirmation remains source-locked to 1..2 immutable current candidate labels plus Neither/Cancel recovery actions.
- Permission and Android-owned surfaces remain system-only handoffs rather than recreated app destinations.
- P2 Like behavior remains removable and does not block P0 delivery.
- No final high-fidelity screen is approved by Phase 3.
- Next gate: Phase 4 primary high-fidelity screen-family generation.

### Phase 4 — primary high-fidelity screen families

- Index: [`docs/high-fidelity/README.md`](high-fidelity/README.md)
- Machine-readable primary register: [`PHASE_4_PRIMARY_REGISTER.json`](high-fidelity/PHASE_4_PRIMARY_REGISTER.json)
- Validation: [`PHASE_4_VALIDATION.md`](high-fidelity/PHASE_4_VALIDATION.md)
- Visual review board: [`PHASE_4_PRIMARY_BOARD.png`](high-fidelity/PHASE_4_PRIMARY_BOARD.png)
- Primary coverage: **10/10 required screen families**, one source-locked high-fidelity base state each.
- Paired artifact contract: **10 PNG + 10 Markdown specs** under `docs/high-fidelity/WF-01/` through `WF-10/`.
- Source hierarchy preservation: **0 structural deviations** from the owning `WF-*` baseline.
- Copy control: exact visible UI copy is recorded in every paired Markdown spec and deterministic local render validation is PASS.
- Navigation/scope: **0 new navigation destinations, controls or product capabilities** introduced by Phase 4.
- Android/system boundaries remain external/system-owned.
- Playback and Voice ownership remain unchanged from final architecture.
- Driving primary artifact preserves one layer, immediate Exit, fixed large controls and no typing/deep navigation/long lists.
- Liked Songs remains visibly **P2 / Removable**.
- Result: **PASS / COMPLETE at Primary High-Fidelity level.**
- Next gate: Phase 5 full high-fidelity state/recovery coverage. Phase 4 does not claim that all 126 states have final Hi-Fi coverage.

### Delivery mechanism

High-fidelity visuals are produced as images and committed to GitHub. Each approved app-visible high-fidelity artifact must be paired with a Markdown specification and stable traceability. Machine-readable JSON is used where needed for tokens, route definitions, artifact registers and automated validation.

Image-generation tooling does not own product behavior, text truth, dimensions, state ownership or approval. Exact copy/specification values are controlled by paired repository records.

### Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay/Voice State; Liked Songs (P2); Settings.

System notification/lock/external controls, Android permission dialogs, Settings handoff, lifecycle reconnect and interruption/route recovery remain ownership-separated transitions rather than invented app destinations.

### State-disposition baseline

The completed Issue #15 manifest remains controlling:

- 126 canonical states;
- 45 App-visible states;
- 61 Variant/annotation states;
- 20 Android/System-only transitions.

Issue #17 must provide explicit high-fidelity disposition for all 126 states without turning system-only transitions into app screens.

### Prototype coverage

The repository-native clickable prototype must cover GS-01..GS-05 and the required recovery branches: permission denial/Settings recovery, Voice ambiguity/not-found/one Retry/Manual Only, No Track, Empty Library, stale selection and corrupt-file recovery.

### Handoff coverage

Handoff records must expose source state, runtime owner, actions, transitions, Back behavior, system handoffs, dimensions/tokens, accessibility semantics, allowed analytics hooks and privacy constraints without embedding business logic in UI.

## Synchronization and review rules

1. Every high-fidelity artifact retains stable WF/UF/State/GS traceability.
2. App-visible review artifacts use paired image + Markdown specification; image-only output is not approved evidence.
3. Exact text/copy and implementation-facing values in paired specifications control over image-generation rendering defects.
4. Design System visual boards do not replace machine-readable tokens/specifications.
5. Structural behavior changes are reviewed upstream before visual synchronization.
6. System-only surfaces remain annotated external/system handoffs.
7. Historical Figma records remain preserved but are not active acceptance dependencies.
8. Final Issue #17 approval requires Design System, high-fidelity, prototype, handoff, traceability and review evidence to be synchronized in GitHub.

## Historical supersession

For active Issue #17 delivery from **3 October 2026** onward, the repository-native contract supersedes the 30 August 2026 Figma delivery disposition.

The historical branch `issue-17/figma-delivery-contract`, Figma file key/page references, prior comments and addendum remain provenance only. They are not required for Phase 1+ execution or final acceptance.

Closed Issue #15 history is not rewritten.

## Permanent constraints

- GitHub is the canonical Issue #17 delivery/behavior/traceability/review/approval workspace.
- Repository high-fidelity image + paired spec artifacts are canonical visual-delivery evidence.
- No Figma access/file/page/node is required for active acceptance.
- `docs/wireframes/` remains the official low-fidelity structural baseline.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- TTS/error copy remains at most one short sentence where required.
- Accessibility handoff covers TalkBack order, non-color-only state, scalable text and expected 200% reflow.
- Playback Service/MediaSession remains live playback authority.
- Voice behavior remains bounded/deterministic under the final architecture.
- UI artifacts do not embed business logic or invent runtime ownership.
- No raw audio, transcript or prohibited personal-media metadata enters analytics/repository evidence.
- No backend/account/streaming, Android Auto/iOS, wake-word, conversational AI or other v1.1 capability is introduced by design.
