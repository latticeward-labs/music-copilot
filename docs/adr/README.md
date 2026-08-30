# Architecture Decision Records

- **Version:** MVP v1.0
- **Status:** Current ADR register; synchronized with Issue #16 through Phase 9
- **Updated:** 30 August 2026
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)

## Product/source authority

The Issue #16 architecture is bound to the user-attached English `MSM2(2).rar` package, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

Controlling product members:

- `Music_Copilot_PRD_v1_EN_Clarified.pdf` — SHA-256 `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf`
- `Music_Copilot_MVP_v1_EN.pdf` — SHA-256 `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0`

Repository [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md) are controlled repository representations. The attached English archive and the [Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md) govern provenance and conflict handling.

**Persian/Farsi PRD or MVP attachments are not architecture authority.**

## Current ADR history

ADR-0001 through ADR-0006 completed preliminary review through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2) on 6 August 2026. Issue #16 Phase 4 preserved those accepted files unchanged and added two compatible implementation-level ADRs: ADR-0007 and ADR-0008. Neither supersedes an earlier ADR.

| ADR | Decision | Relationship / review outcome | Status |
|---|---|---|---|
| [0001](0001-native-android-platform.md) | Native Android 10+ | Preliminary baseline; revised during Issue #2 review | Accepted |
| [0002](0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Preliminary baseline; lifecycle details elaborated by ADR-0007 | Accepted |
| [0003](0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore | Preliminary baseline; runtime/persistence details elaborated by ADR-0007/0008 | Accepted |
| [0004](0004-replaceable-voice-provider.md) | Replaceable STT/TTS provider | Preliminary baseline; provider remains replaceable | Accepted |
| [0005](0005-deterministic-intent-mapping.md) | Deterministic six-intent mapping | Preliminary baseline; ambiguity and bounded configuration retained | Accepted |
| [0006](0006-no-backend-account-streaming.md) | No backend, account or streaming product service | Preliminary baseline; support processors remain separately governed | Accepted |
| [0007](0007-same-process-playback-runtime-and-safe-paused-restore.md) | Same-process MediaSessionService runtime, structured concurrency, serialized commands and Safe-Paused restoration | Added by Issue #16 Phase 4; elaborates ADR-0002/0003; does not supersede | Accepted |
| [0008](0008-minimal-modular-monolith-and-dependency-enforcement.md) | One `:app` modular monolith with enforced dependency/state/data ownership and extraction criteria | Added by Issue #16 Phase 4; elaborates ADR-0001/0003/0006; does not supersede | Accepted |

## Current architecture linkage

The ADRs are interpreted together with the current [Software Architecture Baseline](../SOFTWARE_ARCHITECTURE.md), [Architecture Traceability Matrix](../architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md), [Implementation Structure and Public Contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), and [Validation/Test Architecture](../architecture/ISSUE_16_VALIDATION.md).

Phase 9 created **no new ADR** and changed **no Accepted ADR decision**. Its purpose is repository-wide synchronization of the already approved architecture evidence.

## Evidence routed forward

- Android 10/12/14+, Pixel/stock, Samsung/One UI, Xiaomi/MIUI and 3+ head-unit validation
- Production MediaSessionService/controller integration and command-serialization tests
- MediaStore permission/revocation, index invalidation, migration and large-library scan evidence
- Proto DataStore Safe-Paused restoration/migration tests
- STT availability, network-loss and cloud-consent evidence
- Intent corpus, bounded threshold/fuzzy tuning and ambiguity tests
- Physical call, Audio Focus, headset/Bluetooth and route safety validation
- Privacy review for diagnostics, retention duration and any remote speech processor

These are downstream implementation/QA evidence gates; they are not unresolved ADR statuses.

## Architecture gates

1. **Issue #2:** **Done** — ADR-0001..0006 accepted after review.
2. **Issue #16 Phase 4:** **PASS** — consequential-decision audit complete; ADR-0007/0008 added.
3. **Issue #16 Phases 5–8:** **PASS** — views, implementation contracts, runtime policy and validation architecture complete.
4. **Issue #16 Phase 9:** **PASS** — repository/document synchronization complete; no new ADR required.
5. **Issue #16 final gate:** cross-role review plus final acceptance/merge/exit remain before Issue #4 can be unblocked.

## History and supersession rule

Accepted history must not be silently rewritten. If a future material choice changes an Accepted ADR, create a new ADR whose relationship explicitly states **`Supersedes ADR-xxxx`**, preserve the original ADR file, and update this register.

Replacing an implementation tool explicitly marked replaceable does not require supersession when the approved contracts, ownership and dependency direction remain equivalent.

## Lifecycle

- **Proposed:** drafted, not binding
- **Accepted:** reviewed and binding
- **Revised:** changes still required before acceptance
- **Superseded:** replaced while history remains
- **Deprecated:** no longer applicable, with reason

Every ADR includes status, context, drivers, decision, consequences, alternatives, review evidence and validation.
