# GitHub-native Wireframe Contract Addendum — 25 August 2026

**Issue:** [#15 — Create and approve low-fidelity Wireframes](https://github.com/latticeward-labs/music-copilot/issues/15)  
**Decision:** **ACTIVE — GitHub-native visual and behavioral delivery**  
**Effective date:** 25 August 2026  
**Supersedes for active Issue #15 delivery:** the Figma-dependent portions of the 25 August 2026 Figma addendum and plan  
**Preserves:** all historical Figma/no-Figma records as truthful dated checkpoints

## Decision

The project owner directed that the work previously planned in Figma be delivered equivalently inside the GitHub repository. The active low-fidelity workfile is therefore the versioned content under `docs/wireframes/`.

GitHub now owns both:

1. behavioral, traceability, review and approval records; and
2. editable low-fidelity visual artifacts stored as SVG plus machine-readable tokens and Markdown specifications.

The existing Figma file remains a historical reference. It is not deleted or rewritten, but no Figma account, page, node or MCP quota may block Issue #15 acceptance.

## Format translation

| Former Figma concept | Active GitHub-native equivalent |
|---|---|
| Page / Section | Entry in `docs/wireframes/WORKSPACE_INDEX.md` and a repository directory |
| Variable collection | `docs/wireframes/foundations/tokens.json` |
| Component / component set | Named group in `FOUNDATIONS_BOARD.svg` plus a specification in `foundations/README.md` |
| Frame node | Stable `WF-##_SCREEN_STATE.svg` file |
| Node URL | Permanent repository path at an approved commit/PR |
| Annotation | Markdown traceability/recovery/accessibility fields and visible SVG notes |
| Review mode | Pull-request diff, rendered SVG preview and reviewer checklist |
| Version history | Git commits and PR history |

## Authority and precedence

1. English PRD v1.0 and MVP v1.0 represented by `docs/PRD.md` and `docs/MVP.md`.
2. Approved UF-01..UF-13, 126-state inventory, GS-01..GS-05, ADR and Technical Spike records.
3. Issue #15, `docs/WIREFRAMES.md` and the Coverage Matrix.
4. Each `WF-##_SCREEN_STATE.md` behavioral record.
5. Its paired repository SVG.

If Markdown behavior and SVG structure conflict, the approved behavioral record controls until both are corrected in one reviewed PR.

## Naming and pairing

- App-visible artifacts use `WF-##_SCREEN_STATE.md` and `WF-##_SCREEN_STATE.svg`.
- The pair must share the same stable identifier.
- System-owned transitions are annotations or explicitly named system-only records; they are not invented app destinations.
- Every record states UF, State, GS, priority, ownership, entry, exit, Back, source of truth, recovery, persistence and accessibility.

## Visual editing contract

- SVG source must remain human-readable, text-based and editable.
- Low-fidelity assets use only the approved grayscale tokens.
- Final color, branding, illustration, motion and high-fidelity typography remain out of scope.
- Every interactive control is at least 48dp in its reference geometry.
- State must never be communicated by color alone.
- TalkBack order and scalable-text behavior must be recorded in the paired Markdown and, where useful, shown in the SVG.

## Review and approval gate

A Wireframe becomes reviewable only when:

- Markdown and SVG artifacts both exist;
- the Coverage Matrix maps the owning canonical states to the same `WF-*` package;
- SVG validation and repository link checks pass;
- Product, Design, Android and QA findings are recorded in GitHub;
- the reviewing PR is merged.

Issue #15 remains open until all required P0 packages and reviews pass. This addendum changes the delivery medium, not the product scope or acceptance rigor.

