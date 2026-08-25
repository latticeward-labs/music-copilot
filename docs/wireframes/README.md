# Repository-native Low-fidelity Wireframe Workspace

**Status:** **ISSUE #15 IN PROGRESS — Phase 2 single-page Figma structure PASS; no visual frames approved yet**  
**Official editable workfile:** [Figma — Music Copilot — MVP v1.0](https://www.figma.com/design/CC6I0e23Zwe6qMhRGaqRrT/Music-Copilot-%E2%80%94-MVP-v1.0?node-id=0-1)  
**Figma file key / verified page:** `CC6I0e23Zwe6qMhRGaqRrT` / `0:1`  
**Gate index:** [WIREFRAMES.md](../WIREFRAMES.md)  
**Coverage manifest:** [WIREFRAME_COVERAGE_MATRIX.md](WIREFRAME_COVERAGE_MATRIX.md)  
**Figma structure evidence:** [FIGMA_STRUCTURE_PHASE_2_2026-08-25.md](FIGMA_STRUCTURE_PHASE_2_2026-08-25.md)  
**Physical Figma layout:** one Page `0:1` named `00_Index`; 14 logical workspace Sections + `P0`, `P1`, `P2`, `System-only`  
**Active contract:** [Figma Wireframe Contract Addendum — 25 August 2026](../user-flows/design-sync/FIGMA_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)

This directory is the canonical GitHub workspace for low-fidelity behavioral records; Figma is the official editable visual workfile. It will contain reviewable Markdown frame specifications and, where useful, repository-stored SVG/PNG companions. The coverage manifest classifies all 126 canonical states before frame construction. A visual asset cannot override its linked UF/State/GS behavior.

## Single-page Figma structure

The Starter-tier Page limit is handled through the user-approved single-page layout recorded in [the Phase 2 evidence](FIGMA_STRUCTURE_PHASE_2_2026-08-25.md). The original logical Page names are top-level Sections inside `00_Index`; no product behavior or coverage mapping changed.

- Logical workspaces: `00_Index` through `13_Review`.
- Priority/ownership lanes: `P0`, `P1`, `P2`, `System-only`.
- Required future node pattern: `WF-##_SCREEN_STATE`.
- Example: `WF-01_SPLASH_LOADING`.

## Planned artifact convention

```text
WF-##_SCREEN_STATE.md
WF-##_SCREEN_STATE.svg    # optional visual companion
```

Each Markdown record must identify:

- screen/state and stable UF/GS/state IDs;
- user goal, primary action, entry/exit/Back behavior;
- source of truth and system-only handoffs;
- loading/Empty/error/permission/interruption/retry recovery;
- persistence after UI recreation;
- P0/P1/P2 status;
- accessibility, Driving Mode and privacy annotations;
- review status/date without fabricated reviewer or runtime claims.

Completing the coverage manifest does not approve any visual Wireframe or complete Issue #15.
