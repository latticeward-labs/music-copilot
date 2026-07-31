# Architecture Decision Records

The product authorities are [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md).

ADR-0001 through ADR-0006 were created for the preliminary technical baseline and are reviewed through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2). Their files existing does not by itself make them binding: each must be explicitly marked **Accepted** or **Revised** after review.

| ADR | Decision | Status |
|---|---|---|
| [0001](0001-native-android-platform.md) | Native Android 10+ | Proposed |
| [0002](0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Proposed |
| [0003](0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore boundaries | Proposed |
| [0004](0004-replaceable-voice-provider.md) | Replaceable Android STT/TTS provider | Proposed |
| [0005](0005-deterministic-intent-mapping.md) | Restricted deterministic six-intent mapping | Proposed |
| [0006](0006-no-backend-account-streaming.md) | No backend, account or streaming in MVP v1.0 | Proposed |

## Lifecycle

- **Proposed:** drafted but not binding
- **Accepted:** reviewed and binding for the current baseline
- **Revised:** the draft must be corrected before acceptance
- **Superseded:** replaced by a later ADR while history remains intact
- **Deprecated:** no longer applicable, with reason recorded

## Two architecture gates

1. **Issue #2 — Preliminary ADR gate:** reviews the six scope-level decisions above.
2. **Issue #16 — Final Software Architecture gate:** validates detailed ownership, lifecycle, dependency, navigation, concurrency and module decisions after User Flows and low-fidelity Wireframes are approved.

Issue #16 may add new ADRs or supersede an accepted ADR when evidence requires a materially different decision. Accepted ADR history must never be silently rewritten.

## ADR quality checklist

Every ADR must include:

- Title and status
- Context and decision drivers
- Decision
- Consequences, including costs and risks
- Alternatives considered and why they were not chosen
- Date and reviewers/owners
- Links to relevant PRD/MVP requirements, Issues, experiments or diagrams
