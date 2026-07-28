# Architecture Decision Records

This directory contains the architectural decisions that govern Music Copilot MVP v1.

## Status model

- **Proposed:** complete decision awaiting formal approval in [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2).
- **Accepted:** approved and binding for MVP v1.
- **Superseded:** replaced by a newer ADR; the replacement must be linked.
- **Rejected:** considered but not selected.

An ADR is immutable after acceptance except for spelling, links, or explicit status metadata. A changed decision requires a new ADR that supersedes the old one.

## Decision index

| ADR | Decision | Status |
|---|---|---|
| [0001](0001-native-android-platform.md) | Native Android 8.0+ / API 36 release baseline | Proposed |
| [0002](0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Proposed |
| [0003](0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore boundaries | Proposed |
| [0004](0004-replaceable-voice-provider.md) | Replaceable Android STT/TTS provider boundary | Proposed |
| [0005](0005-deterministic-intent-mapping.md) | Restricted deterministic intent mapping | Proposed |
| [0006](0006-no-backend-account-streaming.md) | No backend, account or streaming in MVP v1 | Proposed |

## Source hierarchy

1. Music Copilot PRD v1.0 - status: Approved for MVP
2. Music Copilot MVP v1.0 - status: Ready for Planning
3. Accepted ADRs
4. Derived repository documentation and implementation

If a lower-level source conflicts with a higher-level source, implementation stops until the conflict is resolved and documented.
