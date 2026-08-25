# Repository-native Low-fidelity Wireframe Workspace

**Status:** **ISSUE #15 IN PROGRESS — GitHub-native Phase 3 Foundations PASS; individual Wireframes pending**  
**Official editable visual workfile:** this directory (`docs/wireframes/`)  
**Gate index:** [WIREFRAMES.md](../WIREFRAMES.md)  
**Workspace topology:** [WORKSPACE_INDEX.md](WORKSPACE_INDEX.md)  
**Coverage manifest:** [WIREFRAME_COVERAGE_MATRIX.md](WIREFRAME_COVERAGE_MATRIX.md)  
**Active contract:** [GitHub-native Wireframe Contract Addendum](GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)  
**Foundations:** [specification](foundations/README.md) · [SVG board](foundations/FOUNDATIONS_BOARD.svg) · [tokens](foundations/tokens.json) · [validation](foundations/FOUNDATIONS_VALIDATION.md)

This directory is both the canonical behavioral workspace and the active editable low-fidelity visual workfile. Markdown controls behavior and traceability; text-based SVG controls visual structure; JSON controls shared tokens. Pull requests provide rendering, diffs, review and approval history.

The former Figma file, its Phase 2 evidence and dated addendum remain historical records. No Figma page, node, plan or MCP quota is an active acceptance dependency.

## Repository-native topology

The former canvas topology is represented by the [Workspace Index](WORKSPACE_INDEX.md): `00_Index` through `13_Review`, plus `P0`, `P1`, `P2` and `System-only` metadata lanes. No state, UF, GS, priority or ownership mapping changes.

- Required artifact pattern: `WF-##_SCREEN_STATE.md` + `WF-##_SCREEN_STATE.svg`.
- Example: `WF-01_SPLASH_LOADING.md` + `WF-01_SPLASH_LOADING.svg`.
- System-only transitions are explicit annotations/records, never invented app destinations.
- Every paired artifact is reviewed in one PR.

## Phase 3 Foundations

The completed foundation set defines:

- Android reference viewport `360 × 800dp`;
- seven grayscale primitives and semantic aliases;
- four hierarchy-only typography roles;
- 11 base component families;
- minimum 48dp touch targets;
- TalkBack reading-order rules;
- scalable text up to 200% without clipping;
- non-color-only state communication;
- explicit Android/system-only annotation styling.

Phase 3 PASS authorizes individual Wireframe construction; it does not approve a screen or close Issue #15.

## Required artifact record

Each Markdown record must identify:

- stable screen/state and owning UF/State/GS IDs;
- user goal, primary action, entry, exit and Back behavior;
- visible state/service/system source of truth;
- loading, Empty, error, permission, interruption, retry and terminal-safe recovery;
- persistence/recreation behavior;
- P0/P1/P2 and App/Hybrid/System ownership;
- accessibility, Driving Mode and privacy annotations;
- paired SVG path and review disposition/date without fabricated reviewer claims.

## Approval rule

A rendered SVG without its behavioral record is not approved. A behavioral record without its paired SVG is not visually complete. The approved GitHub behavioral record controls any conflict until both are synchronized in a reviewed PR.
