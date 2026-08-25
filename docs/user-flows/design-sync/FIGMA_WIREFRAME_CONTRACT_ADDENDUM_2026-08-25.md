# Figma Wireframe Contract Addendum — 25 August 2026

**Product:** Music Copilot MVP v1.0  
**Tracking:** [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Decision date:** 25 August 2026  
**Status:** **ACTIVE FOR ISSUE #15 LOW-FIDELITY WIREFRAMES**  
**Product authorities:** [PRD v1.0](../../PRD.md) and [MVP v1.0](../../MVP.md)  
**Historical input synchronization:** [GitHub-native Design Synchronization — 23 August 2026](GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)

## Decision

The project owner has introduced Figma as the official editable visual workfile for Issue #15. This addendum changes the delivery medium for low-fidelity Wireframes only. It does not change product scope, User Flow behavior, canonical State definitions, Golden Scenarios, accepted ADR boundaries, priority, or the Issue #15 approval gate.

- **GitHub remains the canonical behavioral, traceability, review and approval record.**
- **Figma is the official editable visual workfile for low-fidelity Wireframes.**
- Repository Markdown records and reviewed exports remain required; a Figma node by itself is not an approved Wireframe.
- When Figma and GitHub conflict, the approved GitHub UF/State/GS contract controls until a reviewed repository change synchronizes the behavior.

## Verified Figma workfile

- **File:** [Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)
- **File key:** `CC6I0e23Zwe6qMhRGaqRrT`
- **Verified page:** `0:1`
- **Page name at verification:** `Page 1`
- **Verification date:** 25 August 2026
- **Verification result:** authenticated design-file access PASS; the file and page exist and are available for the Issue #15 wireframe work.

The page name is observational evidence, not a frozen naming requirement. Later Issue #15 workspace setup may rename or add pages while preserving stable repository and Figma node links.

## Two-workspace contract

| Concern | Controlling workspace | Required evidence |
|---|---|---|
| Product scope and priority | GitHub PRD/MVP and approved issue records | Reviewed repository links |
| User Flow, State and Golden behavior | GitHub `docs/user-flows/` | Stable UF/State/GS IDs |
| Editable low-fidelity structure | Figma workfile | Stable page/node URL |
| Per-Wireframe behavior and traceability | GitHub `docs/wireframes/` | `WF-##_SCREEN_STATE.md` |
| Review and approval | GitHub Issue/PR history | Named reviewer, date, finding disposition |
| Visual companion/export | Repository plus Figma source | Reviewed SVG/PNG when produced |

## Synchronization requirements

Every Wireframe submitted for approval under Issue #15 must:

1. use a stable `WF-##_SCREEN_STATE` identifier in Figma and GitHub;
2. link its owning UF, canonical State and GS identifiers;
3. include the Figma page/node URL in its repository Markdown record;
4. identify entry, exit, Back behavior, primary action, visible source of truth, recovery and persistence;
5. distinguish app-visible UI from Android/system-only transitions;
6. retain P0/P1/P2 scope, with UF-13 visibly P2 and non-blocking;
7. record accessibility and Driving Mode constraints;
8. update the GitHub record in the same reviewed change when Figma changes behavior or navigation;
9. avoid treating final color, typography, illustration, motion, high-fidelity UI or implementation as approved.

## Historical integrity

The [23 August 2026 no-Figma synchronization record](GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md) is retained unchanged because it truthfully records the project decision and evidence available at that checkpoint. This addendum supersedes only its active Figma disposition for Issue #15 from 25 August 2026 onward. Historical sign-offs, commit references and measured audits are not rewritten.

## Scope and non-claims

This decision authorizes Figma use for Issue #15 low-fidelity Wireframes. It does not:

- approve any individual `WF-*` artifact;
- approve a final design system, high-fidelity UI or clickable prototype;
- close Issue #15;
- approve Issue #16 architecture or Issue #17 UI-system outputs;
- add backend, account, streaming, Android Auto, iOS or v1.1 scope;
- replace repository review with an external-file-only acceptance gate.

## Phase 0 decision

**PASS — Figma contract activated for Issue #15.** The workfile, file key, verified page ID, authority split, synchronization rules and historical-preservation rule are explicit. Wireframe production and approval remain pending under the unchecked Issue #15 acceptance criteria.
