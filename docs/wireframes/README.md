# Repository-native Low-fidelity Wireframe Workspace

**Status:** **ISSUE #15 IN PROGRESS — Phase 5 Recovery/System-only construction PASS; cross-role approval pending**  
**Official editable visual workfile:** this directory (`docs/wireframes/`)  
**Gate index:** [WIREFRAMES.md](../WIREFRAMES.md)  
**Workspace topology:** [WORKSPACE_INDEX.md](WORKSPACE_INDEX.md)  
**Coverage manifest:** [WIREFRAME_COVERAGE_MATRIX.md](WIREFRAME_COVERAGE_MATRIX.md)  
**Active contract:** [GitHub-native Wireframe Contract Addendum](GITHUB_NATIVE_WIREFRAME_CONTRACT_ADDENDUM_2026-08-25.md)  
**Foundations:** [specification](foundations/README.md) · [SVG board](foundations/FOUNDATIONS_BOARD.svg) · [tokens](foundations/tokens.json) · [validation](foundations/FOUNDATIONS_VALIDATION.md)  
**Phase 4 App States:** [32-frame register](app-states/README.md) · [machine register](app-states/FRAME_REGISTER.json) · [validation](app-states/PHASE_4_VALIDATION.md)  
**Phase 5 Recovery/System:** [13-transition register](recovery-system/README.md) · [terminal matrix](recovery-system/RECOVERY_TERMINAL_MATRIX.md) · [validation](recovery-system/PHASE_5_VALIDATION.md)

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

Phase 3 PASS authorized construction. Phase 4 now supplies the 32 app-state pairs; neither pass fabricates reviewer approval or closes Issue #15.

## Phase 4 — App State Frames

The [Phase 4 register](app-states/README.md) contains exactly 32 paired Markdown/SVG artifacts across `WF-01` through `WF-10`. Automated checks pass for stable pairing, package totals, XML validity, required behavioral fields, 159 interactive targets at 48dp minimum, TalkBack/scalable-text/non-color state annotations and both `P2 / Removable` Liked Songs frames.

Phase 4 construction is complete. Product, Design, Android and QA approval remains deliberately unchecked; Issue #15 stays open.

## Phase 5 — Recovery and System-only Transitions

The [Phase 5 register](recovery-system/README.md) contains exactly 13 paired Markdown/SVG transition artifacts and maps all 20/20 canonical Android/System-only states. Permission dialogs, Android Settings, notification, lock screen, external controls, lifecycle reconnect, phone/Audio Focus, wired/Bluetooth routing, Cloud Voice consent and Driving Voice one-Retry recovery are explicit.

The [Recovery Terminal Matrix](recovery-system/RECOVERY_TERMINAL_MATRIX.md) permits only reachable `MANUAL_PLAYBACK`, service-owned `SAFE_PAUSED`, or their bounded union. Route reconnect never auto-resumes; cloud consent is prohibited in active Driving Mode; a second Driving Voice failure returns immediately to fixed manual controls. [Phase 5 validation](recovery-system/PHASE_5_VALIDATION.md) is PASS at construction/repository level.

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
