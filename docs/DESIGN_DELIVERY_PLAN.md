# Design Delivery Plan — Music Copilot MVP v1.0

- **Version:** MVP v1.0
- **Status:** Current through Issue #16 Phase 10
- **Updated:** 30 August 2026
- **Architecture tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Canonical behavioral, traceability and approval workspace:** GitHub
- **Official low-fidelity visual workfile:** [`docs/wireframes/`](wireframes/) — Markdown + SVG + JSON
- **Workspace index:** [GitHub-native Wireframe Workspace Index](wireframes/WORKSPACE_INDEX.md)
- **Historical Figma reference:** [`FIGMA_DESIGN_PLAN.md`](FIGMA_DESIGN_PLAN.md)
- **Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Design gates use versioned repository artifacts, stable UF/State/GS identifiers, rendered SVG previews, reviewed links and Git history. GitHub is the controlling behavioral/approval record for completed Issue #15. The current implementation-facing architecture is controlled by [Software Architecture](SOFTWARE_ARCHITECTURE.md), with Phase 10 cross-role review evidence under [`docs/architecture/review/`](architecture/review/).

## Authority model

| Concern | Authority |
|---|---|
| Product scope and priority | Controlling English PRD/MVP source package represented in GitHub |
| User Flow, State and Golden behavior | Approved GitHub records |
| Editable low-fidelity structure | `docs/wireframes/**/*.svg` plus paired Markdown |
| Per-Wireframe traceability and behavior | `docs/wireframes/WF-##_SCREEN_STATE.md` |
| Architecture ownership/dependency/runtime behavior | [Software Architecture Baseline](SOFTWARE_ARCHITECTURE.md), Accepted ADRs and [Phase 10 reviewed corrections](architecture/review/ISSUE_16_CONTRACT_CORRECTIONS_2026-08-30.md) |
| Review, disposition and approval | GitHub PR and Issue history |

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

Issue #15 is no longer a pending gate. Historical design/Figma checkpoint documents remain dated evidence only and do not override the closed/current disposition.

## Gate C — Software Architecture

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Main baseline: [SOFTWARE_ARCHITECTURE.md](SOFTWARE_ARCHITECTURE.md)
- Traceability: [Architecture Traceability Matrix](architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- Validation: [Issue #16 Validation/Test Architecture](architecture/ISSUE_16_VALIDATION.md)
- Cross-role review: [Phase 10 Review](architecture/review/ISSUE_16_CROSS_ROLE_REVIEW_2026-08-30.md)
- Status: **IN PROGRESS — Phase 10 cross-role review PASS; open Blocker 0 / Major 0; final acceptance/merge/closeout gate pending**

The Phase 10 Design/UX and navigation review is **APPROVED — AI-assisted** after correcting the Voice confirmation presentation contract. `VoiceSessionState.Confirming` now provides the immutable one/two candidate option labels required by the approved `WF-08_VOICE_CONFIRMING` behavior without an invented repository lookup or navigation branch.

The architecture baseline fixes ownership, lifecycle, dependency direction, runtime policy, persistence, navigation, error/recovery, privacy, test seams and package strategy. Structural corrections still route back to approved UX records; consequential incompatible decisions require a new/superseding ADR.

## Gate D — UI system and prototype

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Planned locations: `docs/design-system/` and `docs/prototype/`
- Status: separate downstream gate.
- Output: foundations, reusable component specifications, high-fidelity states and prototype/handoff after separate approval.

Phase 10 architecture approval is not high-fidelity approval. Runtime Compose accessibility/reflow remains a downstream implementation/design Minor recorded as `PH10-MIN-001`.

## Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay; Liked Songs (P2); Settings. System notification/lock/external controls, platform dialogs, Settings handoff, lifecycle reconnect and interruption/route recovery are ownership-separated transition records rather than invented app destinations.

## Synchronization and review rules

1. Paired Markdown and SVG artifacts use the same stable `WF-##_SCREEN_STATE` identifier.
2. Each Markdown record links its SVG and owning UF/State/GS IDs.
3. Behavioral or navigation changes update both artifacts in the same reviewed PR.
4. SVG-only structure is not approved evidence.
5. Historical checkpoint documents are preserved and superseded through dated addenda/closeout records, not rewritten.
6. Current Gate B status is **Closed / Complete**; historical documents may truthfully show earlier in-progress states only when clearly dated.
7. Current Gate C status is controlled by `SOFTWARE_ARCHITECTURE.md`, the Phase 10 review and Issue #16.

## Permanent constraints

- GitHub is the canonical behavioral, traceability and approval record.
- `docs/wireframes/` remains the official low-fidelity visual workfile from completed Issue #15.
- Stable repository paths and approved commits replace external-node dependencies for active acceptance.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- Playback Service/MediaSession remains the live playback authority.
- No raw audio, transcript or prohibited personal media metadata enters analytics/evidence.
- No backend/account/streaming product scope, Android Auto/iOS platform expansion or other v1.1 capability is introduced by the design process.