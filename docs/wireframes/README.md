# Repository-native Low-fidelity Wireframe Workspace

**Status:** **READY FOR ISSUE #15 - no frames approved yet**  
**External workfile:** None  
**Figma:** N/A / Not Required  
**Gate index:** [WIREFRAMES.md](../WIREFRAMES.md)  
**Synchronized input:** [GitHub-native Design Sync](../user-flows/design-sync/GITHUB_NATIVE_DESIGN_SYNC_2026-08-23.md)

This directory is the canonical workspace for low-fidelity structural design artifacts. It will contain reviewable Markdown frame specifications and, where useful, repository-stored SVG/PNG diagrams. A visual asset cannot override its linked UF/State/GS behavior.

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

Initializing this workspace does not approve any Wireframe or complete Issue #15.
