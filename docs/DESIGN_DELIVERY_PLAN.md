# Design Delivery Plan — Music Copilot MVP v1.0

**Status:** Active  
**Canonical behavioral, traceability and approval workspace:** GitHub  
**Official low-fidelity visual workfile:** [`docs/wireframes/`](wireframes/) — Markdown + SVG + JSON  
**Workspace index:** [GitHub-native Wireframe Workspace Index](wireframes/WORKSPACE_INDEX.md)  
**Active decision:** [GitHub-native Wireframe Contract Addendum — 25 August 2026](wireframes/GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)  
**Historical Figma reference:** [`FIGMA_DESIGN_PLAN.md`](FIGMA_DESIGN_PLAN.md)  
**Historical synchronization:** [GitHub-native Design Sync — 23 August 2026](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)  
**Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Design gates use versioned repository artifacts, stable UF/State/GS identifiers, rendered SVG previews, reviewed links and Git history. GitHub is both the editable low-fidelity visual workfile and the controlling behavioral/approval record for Issue #15. The delivery-medium change does not alter product scope, state behavior or acceptance rigor.

## Authority model

| Concern | Authority |
|---|---|
| Product scope and priority | English PRD/MVP represented in GitHub |
| User Flow, State and Golden behavior | Approved GitHub records |
| Editable low-fidelity structure | `docs/wireframes/**/*.svg` plus paired Markdown |
| Per-Wireframe traceability and behavior | `docs/wireframes/WF-##_SCREEN_STATE.md` |
| Review, disposition and approval | GitHub PR and Issue history |
| Final conflict resolution | Approved GitHub behavioral record |

## Gate A — User Flows, State Inventory and Golden Scenarios

- Location: `docs/user-flows/`
- Status: **PASS / COMPLETE**
- Coverage: UF-01..13, 126 canonical states, GS-01..05 and Cross-scenario Validation.
- Historical synchronization disposition: **PASS at the 23 August 2026 no-Figma checkpoint**.
- Current downstream use: consumed by Issue #15 through the [25 August GitHub-native addendum](wireframes/GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md).

The historical synchronization and Figma records remain unchanged. The active addendum updates only the delivery contract for Issue #15.

## Gate B — Low-fidelity Wireframes

- Tracking: [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- Index: [WIREFRAMES.md](WIREFRAMES.md)
- Repository workspace: [`wireframes/`](wireframes/)
- Visual workfile: [`docs/wireframes/`](wireframes/)
- Workspace topology: [WORKSPACE_INDEX.md](wireframes/WORKSPACE_INDEX.md)
- Foundations: [specification](wireframes/foundations/README.md), [SVG board](wireframes/foundations/FOUNDATIONS_BOARD.svg), [tokens](wireframes/foundations/tokens.json)
- App states: [32-frame register](wireframes/app-states/README.md), [machine register](wireframes/app-states/FRAME_REGISTER.json), [validation](wireframes/app-states/PHASE_4_VALIDATION.md)
- Status: **IN PROGRESS; Phase 4 32/32 App State Frames PASS; cross-role approval pending**
- Output: paired grayscale SVG structures and Markdown specifications covering navigation, hierarchy, ownership, feedback, recovery and accessibility.

Each app-visible artifact must link UF/State/GS IDs and use a stable `WF-*` identifier in both Markdown and SVG. System-only surfaces must be documented explicitly without inventing app screens. Markdown controls behavior; SVG controls low-fidelity visual structure; both are reviewed together.

## Gate C — Architecture feasibility

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Output: final Software Architecture baseline after approved low-fidelity structure.
- Structural corrections return to Gate A/B; consequential decisions use new/superseding ADRs.

## Gate D — UI system and prototype

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Planned locations: `docs/design-system/` and `docs/prototype/`
- Status: separate later gate.
- Output: foundations, reusable component specifications, high-fidelity states and prototype/handoff after separate approval.

GitHub-native low-fidelity delivery for Issue #15 does not automatically approve a high-fidelity or prototype workflow for Gate D.

## Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay; Liked Songs (P2); Settings. System notification/lock/external controls and platform dialogs remain annotated system-only surfaces rather than invented app destinations.

## Synchronization and review rules

1. Paired Markdown and SVG artifacts use the same stable `WF-##_SCREEN_STATE` identifier.
2. Each Markdown record links its SVG and owning UF/State/GS IDs.
3. Behavioral or navigation changes update both artifacts in the same reviewed PR.
4. SVG-only structure is not approved evidence.
5. Markdown-only app-visible structure is not visually complete until the paired SVG exists.
6. Historical checkpoint documents are preserved and superseded through dated addenda, not rewritten.
7. Issue #15 remains open until every acceptance criterion and reviewer gate passes.

## Permanent constraints

- GitHub is the canonical behavioral, traceability and approval record.
- `docs/wireframes/` is the official editable low-fidelity visual workfile for Issue #15.
- Stable repository paths and approved commits replace Figma node links for active acceptance.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- Playback Service/MediaSession remains the live playback authority.
- No raw audio, transcript or personal media metadata enters analytics/evidence.
- No backend, account, streaming, Android Auto, iOS or v1.1 capability is introduced.
