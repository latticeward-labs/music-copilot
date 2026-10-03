# Repository-native High-Fidelity Delivery Contract Addendum — Issue #17

- **Date:** 3 October 2026
- **Tracking:** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Branch:** `issue-17/repository-hifi-delivery`
- **Status:** **ACTIVE — supersedes the 30 August 2026 Figma delivery disposition for Issue #17 only**
- **GitHub repository:** `latticeward-labs/music-copilot`
- **Attached English source package SHA-256:** `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- **Product authorities:** English PRD v1.0 + English MVP v1.0, represented by `docs/PRD.md` and `docs/MVP.md`
- **Architecture authority:** `docs/SOFTWARE_ARCHITECTURE.md` + Accepted ADRs
- **Low-fidelity structural authority:** completed Issue #15 repository-native Wireframes under `docs/wireframes/`

> GitHub is the canonical delivery, behavior, architecture, traceability, review and approval workspace for Issue #17. Repository-stored high-fidelity image artifacts plus their paired Markdown/JSON specifications are the canonical visual-delivery evidence. No Figma account, file, page, node or external workfile is required for acceptance.

## 1. Purpose

This addendum changes the active Issue #17 delivery mechanism because the Figma workspace is unavailable for execution. The scope of Issue #17 does not change: it must still deliver an implementation-ready Design System, high-fidelity screen/state designs, a clickable prototype, accessibility/privacy annotations and engineering handoff.

Only the delivery medium changes.

The historical 30 August 2026 Figma contract is preserved as dated provenance. It is not deleted or rewritten. From 3 October 2026 onward, this addendum controls active Issue #17 delivery.

## 2. Source and authority hierarchy

Issue #17 inherits the source hierarchy already stabilized by the final architecture gate:

1. English PRD v1.0 — product intent, scope, FRs, privacy, quality and exclusions.
2. English MVP v1.0 — P0/P1/P2, delivery acceptance, required screens/states, QA and cut rules.
3. Current Accepted ADRs and final `docs/SOFTWARE_ARCHITECTURE.md` — runtime ownership, lifecycle, data, Voice, playback and privacy boundaries.
4. Approved UF-01..UF-13, GS-01..GS-05 and completed Issue #15 Wireframes — behavior, state, recovery, navigation and structural design inputs.
5. Technical Spikes/review evidence — feasibility and validation evidence only.
6. Proposed/historical PDFs or external-design snapshots — supporting/historical evidence only where current GitHub records have advanced beyond them.

The attached English package used by this work has SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`, matching the package identity already recorded by the Issue #16 Source and Authority Register.

## 3. Canonical Issue #17 workspace

### GitHub owns

- product-scope and priority traceability;
- visual-delivery artifacts for active Issue #17;
- Design System specifications and machine-readable tokens;
- high-fidelity image artifacts and paired behavior/specification records;
- clickable-prototype source and route definitions;
- engineering handoff specifications;
- WF/UF/State/GS traceability;
- architecture/runtime ownership boundaries;
- P0/P1/P2 and removable-branch status;
- accessibility, privacy and Driving Mode annotations;
- review findings, dates, evidence and approval history;
- final Issue #17 acceptance and closeout.

### Image-generation tooling owns nothing authoritative

Image-generation tooling is a production method only. A generated image becomes an Issue #17 artifact only after it is stored in the repository, paired with the required specification/traceability record, reviewed, and accepted.

A generated image by itself is not approval evidence and cannot override PRD/MVP, User Flows, Wireframes, Architecture or ADRs.

## 4. Repository delivery layout

Issue #17 will use these canonical locations:

- `docs/design-system/` — visual foundations, tokens, reusable-component specifications and component-board evidence.
- `docs/high-fidelity/` — approved high-fidelity screen/state images plus paired Markdown/JSON records.
- `docs/prototype/` — clickable prototype source, route definitions, scenario coverage and recovery branches.
- `docs/handoff/` — screen/component/state/action/Compose handoff specifications.
- `docs/design-system/review/` — cross-role review, findings register and validation evidence.

Historical Issue #15 Wireframes remain under `docs/wireframes/` and are not rewritten into high-fidelity artifacts.

## 5. High-fidelity artifact contract

Every reviewable app-visible high-fidelity artifact must have:

- a stable `HF-*` identifier;
- a repository image artifact, normally PNG;
- a paired Markdown specification;
- machine-readable metadata where needed for validation;
- owning `WF-*`, UF, State and GS references;
- P0/P1/P2 status;
- source-of-truth/ownership annotation;
- visible content and actions;
- entry, exit, Back and recovery behavior;
- accessibility notes;
- privacy/analytics allowance notes;
- structural-deviation status.

Recommended naming:

- `HF-##_SCREEN_STATE.png`
- `HF-##_SCREEN_STATE.md`

Generated text shown inside an image is not trusted automatically. The paired Markdown specification controls exact UI copy, labels and implementation-facing values when image-generation rendering introduces textual variation.

## 6. Design System contract

The Design System must define and review:

- color roles and dark-theme behavior;
- typography scale and text-scaling rules;
- spacing, grid and layout rules;
- iconography rules;
- buttons, icon buttons and microphone control;
- player controls, progress and track metadata;
- list/row/search components;
- Empty/error/permission-education components;
- Voice-state feedback: Listening, Processing, Confirming, Executed and Failed;
- Driving Mode components with stable placement and immediate Exit;
- reusable variants for disabled/loading/error/selected states;
- accessibility notes;
- Compose-ready token names and component/state specifications.

Component-board images are visual evidence only; exact tokens/specifications remain in Markdown/JSON.

## 7. Required screen/state coverage

The ten required screen families remain:

1. Splash
2. Onboarding
3. Permissions
4. Home / Library
5. Search
6. Full Player
7. Driving Mode
8. Voice Overlay / Voice State
9. Liked Songs — P2 / removable
10. Settings

The completed Issue #15 state manifest remains controlling for disposition:

- 126 canonical states total;
- 45 App-visible Wireframe states;
- 61 Variant/annotation states;
- 20 Android/System-only transitions.

Issue #17 must produce an explicit high-fidelity disposition for all 126 states. It must not invent app screens for Android/system-only transitions.

## 8. Prototype contract

The clickable prototype remains mandatory but is repository-native rather than Figma-native.

It must cover:

- GS-01 — First Launch to Ready Library;
- GS-02 — Find Music and Start Playback;
- GS-03 — Continue Playback Outside the App;
- GS-04 — Drive and Execute a Direct Voice Command;
- GS-05 — Recover Safely from an Interruption;
- permission denial and Settings recovery;
- Voice ambiguity, not-found, one Retry and Manual Only;
- No Track, Empty Library, stale selection and corrupt-file recovery.

The prototype may be implemented as repository-hosted/static HTML/CSS/JS with high-fidelity image surfaces and interaction hotspots, provided navigation/transition behavior stays source-aligned and system-only handoffs remain clearly annotated rather than modeled as invented app destinations.

## 9. Handoff contract

Handoff must identify for every relevant screen/component/state:

- state source;
- runtime owner;
- actions and command targets;
- transitions and Back behavior;
- system-only handoffs;
- design tokens and dimensions;
- component variants;
- accessibility semantics and expected reading/focus order;
- allowed analytics hooks;
- privacy constraints;
- implementation notes for Jetpack Compose.

UI artifacts must not embed or duplicate business logic. Playback/queue/position authority remains service-owned; Voice remains bounded/deterministic under the final architecture; no new backend/account/streaming scope is introduced.

## 10. Accessibility, Driving and privacy constraints

Binding constraints include:

- minimum 48dp touch targets;
- scalable text and documented 200% text/reflow expectation;
- sufficient contrast;
- TalkBack labels and reading/focus order;
- non-color-only state communication;
- Driving Mode remains one interaction layer;
- immediate Exit from Driving Mode;
- no typing, nested navigation, long result lists or distracting motion in Driving Mode;
- TTS/error copy is at most one short sentence where required;
- no raw audio, transcript or prohibited personal-media metadata in analytics/repository evidence;
- platform dialogs, Android Settings, notification/lock-screen/external controls remain system-owned surfaces.

Runtime Compose semantics and actual 200% reflow remain downstream implementation/QA proof; Issue #17 must provide the design/handoff specification required by `PH10-MIN-001`.

## 11. Change-control and conflict rule

If a high-fidelity artifact conflicts with an approved behavioral, architecture, traceability or acceptance record, the approved GitHub source record controls.

- Structural UX changes return to the Issue #15 Wireframe/User Flow gate.
- Consequential architecture changes return to Issue #16 or require a new/superseding ADR.
- Product-scope changes require PRD/MVP governance.
- No design image may silently introduce v1.1 capabilities.

## 12. Historical supersession

The following remain preserved as historical evidence:

- `issue-17/figma-delivery-contract` branch;
- `FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md`;
- registered Figma workfile/file key/page references;
- prior Issue #17 comments documenting the 30 August Figma contract.

For active Issue #17 acceptance from 3 October 2026 onward, any requirement equivalent to:

- Figma is required;
- a Figma workfile is the canonical visual source;
- reusable components must exist specifically as Figma components/variants;
- GS prototype acceptance requires Figma;
- Figma page/node links are required evidence;
- Figma/GitHub synchronization is an exit criterion

is superseded by this repository-native contract.

Closed Issue #15 history is not changed.

## 13. Active Issue #17 acceptance interpretation

Issue #17 acceptance is now interpreted as follows:

1. High-fidelity visual design must match approved repository-native low-fidelity Wireframes; structural changes return upstream.
2. All ten screen families and approved app-visible critical states must have repository-native high-fidelity coverage.
3. Reusable component definitions/variants must be specified in repository Design System artifacts, not as detached one-off screen inventions.
4. Driving Mode must remain one layer with high contrast, stable placement and at least 48dp targets.
5. No typing, nested navigation, long lists or distracting motion may appear in Driving Mode.
6. TalkBack labels, reading order, text scaling/reflow, contrast and non-color-only state must be documented.
7. TTS/error copy must remain at most one short sentence where required.
8. Loading, Empty, disabled, error and permission-recovery states must be implementation-ready.
9. The repository-native clickable prototype must cover GS-01..GS-05 and required recovery branches.
10. Design tokens and component specifications must be ready for Compose implementation.
11. Handoff must identify state source, actions, transitions and allowed analytics hooks without embedding business logic in UI.
12. Product, Design/UX, Android and QA review must approve the synchronized repository handoff; Accessibility and Privacy review evidence must also be recorded.
13. Approved artifact paths, traceability identifiers, review dates and final disposition must be recorded in GitHub.
14. Repository visual/spec/prototype/handoff artifacts and GitHub behavior/architecture/traceability must be synchronized with no active acceptance contradiction.

## 14. Phase 0 execution evidence

- [x] Attached English source package identity verified by SHA-256.
- [x] Existing Issue #17 Figma contract inspected.
- [x] Completed Issue #15 and Issue #16 gates verified as upstream-cleared.
- [x] New dedicated branch created: `issue-17/repository-hifi-delivery`.
- [x] 30 August Figma contract preserved as historical evidence.
- [x] New repository-native high-fidelity contract created.
- [x] Repository delivery paths and paired image/spec contract defined.
- [x] Repository-native clickable prototype contract defined.
- [x] Figma-specific active acceptance dependencies removed by the superseding contract.
- [x] Design Delivery Plan synchronized to the new delivery model.
- [x] Issue #17 body synchronized to the new delivery model.

## 15. Phase 0 exit gate

**PASS condition:** no active Issue #17 Acceptance Criterion or active delivery-plan requirement may depend on Figma account access, Figma file/page/node identifiers, Figma component objects or a Figma prototype.

Historical Figma references may remain only when explicitly labeled historical/superseded.

**Disposition:** **PASS / COMPLETE at contract level** once this addendum, the updated Design Delivery Plan and the synchronized Issue #17 body are committed and reviewed together.
