# GitHub-native Design Delivery Plan - Music Copilot MVP v1.0

**Status:** Active  
**External design tool:** **Not required**  
**Figma:** **N/A - no account or workfile**  
**Canonical synchronization:** [GitHub-native Design Sync](user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)  
**Product authorities:** [PRD v1.0](PRD.md) and [MVP v1.0](MVP.md)

Design gates use versioned repository artifacts, stable UF/State/GS identifiers, paired Markdown/Mermaid sources, reviewed links and Git history. The delivery medium changes from an external workfile to GitHub; acceptance rigor and product boundaries do not change.

## Gate A - User Flows, State Inventory and Golden Scenarios

- Location: `docs/user-flows/`
- Status: **PASS / COMPLETE**
- Coverage: UF-01..13, 126 canonical states, GS-01..05 and Cross-scenario Validation.
- Design-sync disposition: **PASS; Figma N/A**.

## Gate B - Low-fidelity Wireframes

- Tracking: [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- Index: [WIREFRAMES.md](WIREFRAMES.md)
- Workspace: [`wireframes/`](wireframes/)
- Status: **READY; not yet approved**
- Output: repository-native grayscale structural frames/specifications with navigation, hierarchy, ownership, feedback, recovery and accessibility annotations.

Each frame must link UF/State/GS IDs and distinguish visible app UI from system-only surfaces. SVG/PNG may illustrate structure, but the reviewable Markdown record controls behavior and traceability.

## Gate C - Architecture feasibility

- Tracking: [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- Output: final Software Architecture baseline after approved low-fidelity structure.
- Structural corrections return to Gate A/B; consequential decisions use new/superseding ADRs.

## Gate D - UI system and prototype

- Tracking: [Issue #17](https://github.com/latticeward-labs/music-copilot/issues/17)
- Planned locations: `docs/design-system/` and `docs/prototype/`
- Status: separate later gate.
- Output: foundations, reusable component specifications, high-fidelity states and a repository-linked prototype/handoff without requiring Figma.

## Required ten-screen coverage

Splash; Onboarding; Permissions; Home/Library; Search; Full Player; Driving Mode; Voice Overlay; Liked Songs (P2); Settings. System notification/lock/external controls and platform dialogs remain annotated system-only surfaces rather than invented app destinations.

## Permanent constraints

- GitHub is the canonical behavioral and approval record.
- Figma page/node links are not acceptance criteria.
- Driving Mode remains one layer with immediate Exit, at least 48dp targets and no typing/deep navigation/long lists.
- Playback Service/MediaSession remains the live playback authority.
- No raw audio, transcript or personal media metadata enters analytics/evidence.
- No backend, account, streaming, Android Auto, iOS or v1.1 capability is introduced.

