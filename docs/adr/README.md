# Architecture Decision Records

The product authorities are [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md).

ADR-0001 through ADR-0006 completed preliminary review through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2) on 2026-08-06. All six were revised during review to make rationale, consequences, alternatives, validation evidence and deferred decisions explicit, then accepted.

| ADR | Decision | Review outcome | Status |
|---|---|---|---|
| [0001](0001-native-android-platform.md) | Native Android 10+ | Revised for binding vs replaceable choices and Issue #16 boundary | Accepted |
| [0002](0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Revised for lifecycle, P1 restoration and spike boundaries | Accepted |
| [0003](0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore | Revised for authority, cache and privacy boundaries | Accepted |
| [0004](0004-replaceable-voice-provider.md) | Replaceable STT/TTS provider | Revised for network, consent and offline-guarantee boundaries | Accepted |
| [0005](0005-deterministic-intent-mapping.md) | Deterministic six-intent mapping | Revised for ambiguity, safety and bounded configuration | Accepted |
| [0006](0006-no-backend-account-streaming.md) | No backend, account or streaming | Revised to distinguish prohibited product services from reviewed support processors | Accepted |

## Evidence and assumptions routed forward

- Android 10/12/14+, Pixel/stock, Samsung/One UI, Xiaomi/MIUI and three head-unit validation
- Service/controller lifecycle, process-death limits and command serialization spike
- MediaStore permission/revocation, index invalidation, migration and 5,000-file scan evidence
- STT availability, network-loss and cloud-consent evidence
- Intent corpus, bounded threshold/fuzzy tuning and ambiguity tests
- Privacy review for diagnostics and any cloud speech processor

These are validation inputs to User Flows, Wireframes, QA and Issue #16, not unresolved ADR statuses.

## Two architecture gates

1. **Issue #2:** completed 2026-08-06; all six scope-level ADRs accepted after review revisions.
2. **Issue #16:** final ownership, lifecycle, dependency, navigation, concurrency and module decisions after User Flows/Wireframes.

Issue #16 may add or supersede ADRs when evidence requires a materially different decision. Accepted history must not be silently rewritten.

## Lifecycle

- **Proposed:** drafted, not binding
- **Accepted:** reviewed and binding
- **Revised:** changes still required before acceptance
- **Superseded:** replaced while history remains
- **Deprecated:** no longer applicable, with reason

Every ADR includes status, context, drivers, decision, consequences, alternatives, review evidence and validation.
