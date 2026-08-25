# Design Delivery Plan — Music Copilot MVP v1.0

**Status:** Active  
**Canonical behavioral, traceability and approval workspace:** GitHub  
**Official low-fidelity visual workfile:** [Figma — Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)  
**Figma file key / verified page:** `CC6I0e23Zwe6qMhRGaqRrT` / `0:1`  
**Active Figma decision:** [Figma Wireframe Contract Addendum — 25 August 2026](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)  
**Historical synchronization:** [GitHub-native Design Sync — 23 August 2026](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)  
**Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Design gates use versioned repository artifacts, stable UF/State/GS identifiers, reviewed links and Git history. Figma is the editable low-fidelity visual workfile for Issue #15; GitHub remains the controlling behavioral and approval record. The visual delivery medium does not change product scope, state behavior or acceptance rigor.

## Authority model

| Concern | Authority |
|---|---|
| Product scope and priority | English PRD/MVP represented in GitHub |
| User Flow, State and Golden behavior | Approved GitHub records |
| Editable low-fidelity structure | Official Figma workfile |
| Per-Wireframe traceability and behavior | `docs/wireframes/WF-##_SCREEN_STATE.md` |
| Review, disposition and approval | GitHub PR and Issue history |
| Final conflict resolution | Approved GitHub behavioral record |

## Gate A — User Flows, State Inventory and Golden Scenarios

- Location: `docs/user-flows/`
- Status: **PASS / COMPLETE**
- Coverage: UF-01..13, 126 canonical states, GS-01..05 and Cross-scenario Validation.
- Historical synchronization disposition: **PASS at the 23 August 2026 no-Figma checkpoint**.
- Current downstream use: consumed by Issue #15 through the [25 August Figma addendum](user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md).

The historical synchronization record remains unchanged. The addendum updates only the active delivery contract for Issue #15.

## Gate B — Low-fidelity Wireframes

- Tracking: [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- Index: [WIREFRAMES.md](WIREFRAMES.md)
- Repository workspace: [`wireframes/`](wireframes/)
- Visual workfile: [Figma — Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)
- File key: `CC6I0e23Zwe6qMhRGaqRrT`
- Verified page: `0:1`
- Verification date: 25 August 2026
- Status: **IN PROGRESS; contract activated; individual Wireframes not yet approved**
- Output: synchronized grayscale structural frames plus repository specifications covering navigation, hierarchy, ownership, feedback, recovery and accessibility.

Each app-visible frame must link UF/State/GS IDs, a stable `WF-*` identifier and its Figma node. System-only surfaces must be documented explicitly without inventing app screens. Repository Markdown controls behavior and approval; reviewed SVG/PNG exports may accompany it.

## Gate C — Architecture feasibility

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Output: final Software Architecture baseline after approved low-fidelity structure.
- Structural corrections return to Gate A/B; consequential decisions use new/superseding ADRs.

## Gate D — UI system and prototype

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Planned locations: `docs/design-system/` and `docs/prototype/`
- Status: separate later gate.
- Output: foundations, reusable component specifications, high-fidelity states and prototype/handoff after separate approval.

Activation of Figma for Issue #15 does not automatically approve a high-fidelity or prototype workflow for Gate D.

## Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay; Liked Songs (P2); Settings. System notification/lock/external controls and platform dialogs remain annotated system-only surfaces rather than invented app destinations.

## Synchronization and review rules

1. Figma and repository artifacts use the same stable `WF-##_SCREEN_STATE` identifier.
2. Each repository record links its Figma node and owning UF/State/GS IDs.
3. Behavioral or navigation changes made in Figma return to the owning GitHub records in the same reviewed change.
4. Figma-only structure is not approved evidence.
5. GitHub-only app-visible structure is not visually complete until the intended Figma node is linked.
6. Historical checkpoint documents are preserved and superseded through dated addenda, not rewritten.
7. Issue #15 remains open until every acceptance criterion and reviewer gate passes.

## Permanent constraints

- GitHub is the canonical behavioral, traceability and approval record.
- Figma is the official editable low-fidelity visual workfile for Issue #15, not a replacement for repository evidence.
- Figma page/node links supplement repository artifacts and are not sufficient by themselves for approval.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- Playback Service/MediaSession remains the live playback authority.
- No raw audio, transcript or personal media metadata enters analytics/evidence.
- No backend, account, streaming, Android Auto, iOS or v1.1 capability is introduced.
