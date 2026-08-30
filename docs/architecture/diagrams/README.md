# Issue #16 Phase 5 — Architecture Diagram Sources

These `.mmd` files are the independent Mermaid sources for the matching inline diagrams in [`docs/SOFTWARE_ARCHITECTURE.md`](../../SOFTWARE_ARCHITECTURE.md).

| View | Source |
|---|---|
| System Context | [`01-system-context.mmd`](01-system-context.mmd) |
| Container/Component Architecture | [`02-container-component-architecture.mmd`](02-container-component-architecture.mmd) |
| Allowed and Forbidden Dependency Direction | [`03-dependency-direction.mmd`](03-dependency-direction.mmd) |
| State Ownership | [`04-state-ownership.mmd`](04-state-ownership.mmd) |
| Playback Ownership and Lifecycle | [`05-playback-ownership-lifecycle.mmd`](05-playback-ownership-lifecycle.mmd) |
| Navigation and Voice Overlay | [`06-navigation-voice-overlay.mmd`](06-navigation-voice-overlay.mmd) |
| Critical Data Flows | [`07-critical-data-flows.mmd`](07-critical-data-flows.mmd) |
| Privacy and Telemetry Boundary | [`08-privacy-telemetry-boundary.mmd`](08-privacy-telemetry-boundary.mmd) |

## Control rule

- The architecture text and Accepted ADRs are authoritative; diagrams visualize them and may not introduce a hidden decision.
- Each diagram change must update both its `.mmd` source and the matching inline Mermaid block in `SOFTWARE_ARCHITECTURE.md` in the same change.
- The Critical Data Flow view covers all eight mandatory Phase 5 routes and the already-controlled P1 cold-restoration route.
- No Persian/Farsi PRD/MVP is an authority for these diagrams; the source package is the attached English `MSM2(2).rar` identified in the Source/Authority Register.
- Phase 5 validation: [`PHASE5_ARCHITECTURE_VIEW_AUDIT.md`](../PHASE5_ARCHITECTURE_VIEW_AUDIT.md).
