# Design Delivery Plan — Music Copilot MVP v1.0

- **Version:** MVP v1.0
- **Status:** **Current through Issue #17 Phase 0 contract alignment**
- **Updated:** 30 August 2026
- **Architecture tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Design tracking:** [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- **Canonical visual/component/prototype workspace for Issue #17:** [Figma — Music Copilot Issue 17 Design Workfile](https://www.figma.com/design/2vwiPWEwRg3LdKkveMFRqV)
- **Figma file key:** `2vwiPWEwRg3LdKkveMFRqV`
- **Canonical behavioral, architecture, traceability, review and approval workspace:** GitHub
- **Official low-fidelity visual workfile from completed Issue #15:** [`docs/wireframes/`](wireframes/) — Markdown + SVG + JSON
- **Workspace index:** [GitHub-native Wireframe Workspace Index](wireframes/WORKSPACE_INDEX.md)
- **Issue #17 contract addendum:** [FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md](FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md)
- **Historical Figma/No-Figma references:** [`FIGMA_DESIGN_PLAN.md`](FIGMA_DESIGN_PLAN.md) and dated design-sync/addendum records
- **Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

> Figma is the canonical visual design, component-library and interactive-prototype workspace. GitHub remains authoritative for product behavior, architecture, traceability, review evidence and approval history.

Issue #17 uses a dual-workspace delivery model. Figma owns editable visual design, component-library and interactive-prototype work; GitHub owns behavioral truth, architecture, stable traceability identifiers, review evidence, approval history and the acceptance gate. Neither workspace replaces the other.

## Authority model

| Concern | Authority |
|---|---|
| Product scope and priority | Controlling English PRD/MVP source package represented in GitHub |
| User Flow, State and Golden behavior | Approved GitHub records |
| Editable low-fidelity structure | `docs/wireframes/**/*.svg` plus paired Markdown from completed Issue #15 |
| Per-Wireframe traceability and behavior | `docs/wireframes/WF-##_SCREEN_STATE.md` |
| Architecture ownership/dependency/runtime behavior | final [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md), Accepted ADRs and [Phase 10 reviewed corrections](architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md) |
| Visual foundations, tokens and reusable component library for Issue #17 | **Figma** |
| High-fidelity screens and critical visual states for Issue #17 | **Figma** |
| Interactive prototype for GS-01..GS-05 and required recovery branches | **Figma** |
| Behavioral/component traceability and acceptance evidence | GitHub |
| Review, disposition and approval history | GitHub PR and Issue history |

### Conflict rule

If Figma conflicts with an approved GitHub behavioral, architecture, traceability or acceptance record, **GitHub controls until a reviewed change resolves the conflict**. The resolved behavior is then synchronized into Figma. Structural UX changes route back to the Wireframe/flow gate; consequential architecture changes route to Issue #16 or a new/superseding ADR.

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
- Acceptance: all required paired Markdown/SVG artifacts and system/recovery records are synchronized; open Blocker/Major findings are zero.

Issue #15 is no longer a pending gate. Its repository-native low-fidelity workfile remains the approved structural baseline. Historical design/Figma checkpoint documents remain dated evidence only and do not override the closed/current disposition.

## Gate C — Software Architecture

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Main baseline: [SOFTWARE_ARCHITECTURE.md](SOFTWARE_ARCHITECTURE.md)
- Traceability: [Architecture Traceability Matrix](architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- Validation: [Issue #16 Validation/Test Architecture](architecture/ISSUE_16_VALIDATION.md)
- Cross-role review: [Phase 10 Review](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)
- Final acceptance: [Phase 11 Audit](architecture/PHASE11_FINAL_ACCEPTANCE_AUDIT.md)
- Final delivery: [PR #46](https://github.com/latticeward-labs/music-copilot/pull/46)
- Status: **PASS / COMPLETE / MERGED — Phase 11; open Blocker 0 / Major 0**

The Phase 10 Design/UX and navigation review is **APPROVED — AI-assisted** after correcting the Voice confirmation presentation contract. `VoiceSessionState.Confirming` provides the immutable one/two candidate option labels required by approved `WF-08_VOICE_CONFIRMING` behavior without an invented repository lookup or navigation branch.

Phase 11 reconciled 14/14 Issue #16 Acceptance Criteria with linked evidence and merged the architecture baseline to `main`. Gate C is no longer pending. The final architecture baseline fixes ownership, lifecycle, dependency direction, runtime policy, persistence, navigation, error/recovery, privacy, test seams and package strategy.

## Gate D — UI system, high-fidelity design, prototype and handoff

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Phase 0 contract: [Figma Delivery Contract Addendum](FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md)
- Dedicated branch: `issue-17/figma-delivery-contract`
- Figma workfile: [Music Copilot — Issue 17 Design Workfile](https://www.figma.com/design/2vwiPWEwRg3LdKkveMFRqV)
- File key: `2vwiPWEwRg3LdKkveMFRqV`
- Status: **IN PROGRESS — Phase 0 contract alignment established**
- Repository companion locations: `docs/design-system/` and `docs/prototype/`
- Output: foundations, reusable component specifications, high-fidelity screens/states, clickable prototype, accessibility/privacy annotations and engineering handoff.

### Registered Figma pages

The authenticated Figma Starter plan supports three pages; Issue #17 therefore consolidates all required work into these canonical pages without reducing scope:

| Page | Responsibility |
|---|---|
| `00 — Contract & Foundations` | Delivery contract, visual foundations, accessibility rules and Compose-ready token naming |
| `01 — Components & Hi-Fi` | Reusable components/variants plus all ten required high-fidelity screens and approved states |
| `02 — Prototype & Handoff` | GS-01..GS-05 clickable prototype, recovery branches, annotations and engineering handoff |

Issue #16 architecture approval is not high-fidelity approval. Runtime Compose accessibility/reflow remains a downstream implementation/design Minor recorded as `PH10-MIN-001`.

## Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay/Voice State; Liked Songs (P2); Settings. System notification/lock/external controls, platform dialogs, Settings handoff, lifecycle reconnect and interruption/route recovery are ownership-separated transition records rather than invented app destinations.

## Synchronization and review rules

1. High-fidelity Figma frames retain stable `WF-*`, UF, State and GS traceability from the approved repository records.
2. GitHub companion records link the canonical Figma workfile/page/node evidence needed for review and handoff.
3. Behavioral or navigation changes are reviewed in GitHub first; Figma is synchronized after the controlling change is approved.
4. A Figma artifact alone is not approval evidence; a GitHub behavioral record alone does not satisfy a visual/prototype Acceptance Criterion.
5. Historical checkpoint and No-Figma documents are preserved and superseded prospectively through dated addenda; history is never erased.
6. Current Gate B status remains **Closed / Complete**; Issue #17 does not retroactively make Figma a dependency of closed Issue #15.
7. Current Gate C status remains controlled by final `SOFTWARE_ARCHITECTURE.md`, Phase 10 review, Phase 11 audit and Issue #16 closeout.
8. Final Issue #17 approval requires Figma visual/prototype artifacts and GitHub traceability/review evidence to be synchronized.

## Historical supersession rule

For **Issue #17 only**, earlier active statements equivalent to `Figma: N/A / Not Required`, `no Figma dependency`, `no external-workfile acceptance criterion`, or repository-only high-fidelity/prototype delivery are superseded by [FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md](FIGMA_DELIVERY_CONTRACT_ADDENDUM_2026-08-30.md) from 30 August 2026 onward. The source documents remain in history as dated evidence.

## Permanent constraints

- Figma is canonical for Issue #17 visual design, component library and interactive prototype.
- GitHub is canonical for product behavior, architecture, traceability, review evidence and approval history.
- `docs/wireframes/` remains the official low-fidelity structural workfile from completed Issue #15.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- TTS/error copy remains at most one short sentence where required by approved behavior.
- Playback Service/MediaSession remains the live playback authority.
- UI artifacts do not embed business logic or invent runtime ownership.
- No raw audio, transcript or prohibited personal media metadata enters analytics/evidence.
- No backend/account/streaming product scope, Android Auto/iOS platform expansion or other v1.1 capability is introduced by the design process.