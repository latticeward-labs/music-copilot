# Architecture Decision Records

## Product/source authority

For Issue #16 Phase 4, the product authority was revalidated directly against the attached English `MSM2(2).rar` package, SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`.

The controlling product members are:

- `Music_Copilot_PRD_v1_EN_Clarified.pdf` — SHA-256 `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf`
- `Music_Copilot_MVP_v1_EN.pdf` — SHA-256 `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0`

Repository [PRD v1.0](../PRD.md) and [MVP v1.0](../MVP.md) are controlled repository representations; omitted nuance is governed by the attached English PDFs as recorded in the [Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md).

**Persian/Farsi PRD or MVP attachments are not architecture authority and were not used for the Phase 4 decision revalidation.**

## Current ADR history

ADR-0001 through ADR-0006 completed preliminary review through [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2) on 2026-08-06. All six were revised during review to make rationale, consequences, alternatives, validation evidence and deferred decisions explicit, then accepted in GitHub. The older attached ADR PDFs are decision inputs/provenance snapshots; current status is controlled by the repository.

Issue #16 Phase 4 completed the consequential-decision audit on 2026-08-27 and was then directly source-revalidated against the attached English archive. Accepted ADR-0001..0006 were preserved unchanged. Two compatible implementation-level decisions required new ADRs: ADR-0007 for same-process service runtime/concurrency/Safe-Paused restoration and ADR-0008 for the minimal modular monolith/dependency strategy. Neither supersedes an existing ADR. See the [Phase 4 ADR audit](../architecture/ADR_PHASE4_AUDIT.md).

| ADR | Decision | Review outcome | Status |
|---|---|---|---|
| [0001](0001-native-android-platform.md) | Native Android 10+ | Revised for binding vs replaceable choices and Issue #16 boundary | Accepted |
| [0002](0002-service-owned-media3-playback.md) | Service-owned Media3 playback | Revised for lifecycle, P1 restoration and spike boundaries | Accepted |
| [0003](0003-local-first-data-boundaries.md) | MediaStore, Room and DataStore | Revised for authority, cache and privacy boundaries | Accepted |
| [0004](0004-replaceable-voice-provider.md) | Replaceable STT/TTS provider | Revised for network, consent and offline-guarantee boundaries | Accepted |
| [0005](0005-deterministic-intent-mapping.md) | Deterministic six-intent mapping | Revised for ambiguity, safety and bounded configuration | Accepted |
| [0006](0006-no-backend-account-streaming.md) | No backend, account or streaming | Revised to distinguish prohibited product services from reviewed support processors | Accepted |
| [0007](0007-same-process-playback-runtime-and-safe-paused-restore.md) | Same-process MediaSessionService runtime, structured concurrency, serialized commands and Safe-Paused restoration | Added by Issue #16 Phase 4; revalidated against attached English source; elaborates ADR-0002/0003 | Accepted |
| [0008](0008-minimal-modular-monolith-and-dependency-enforcement.md) | One `:app` minimal modular monolith with enforced dependency/state/data ownership and extraction criteria | Added by Issue #16 Phase 4; revalidated against attached English source; elaborates ADR-0001/0003/0006 | Accepted |

## Phase 4 source-alignment notes

The attached English System Architecture and Technical Blueprint establish the logical boundaries and explicitly leave several final choices to Issue #16, including same-process vs separate-process service, exact coroutine/process-death policy, FR-08 representation, some numeric policies, provider/retention choices and Gradle-module granularity. ADR-0007/0008 and `SOFTWARE_ARCHITECTURE.md` are the current finalizations of those open choices using approved repository evidence.

One direct-source correction is now explicit: the attached Technical Blueprint classifies **Hilt as replaceable**, so ADR-0008 binds the DI composition-root/dependency contract, not Hilt as an irreversible technology choice.

Fail-closed Manual-Only/NoOp foundation defaults also do not satisfy the MVP Closed Beta gate by themselves; P0 Voice and required privacy-safe measurement must be enabled and validated before Beta completion can be claimed.

## Evidence and assumptions routed forward

- Android 10/12/14+, Pixel/stock, Samsung/One UI, Xiaomi/MIUI and three head-unit validation
- Production MediaSessionService/controller integration, process recreation and command-serialization tests
- MediaStore permission/revocation, index invalidation, migration and 5,000-file scan evidence
- Proto DataStore Safe-Paused queue restoration and migration tests
- STT availability, network-loss and cloud-consent evidence
- Intent corpus, bounded threshold/fuzzy tuning and ambiguity tests
- Physical call, Audio Focus, headset/Bluetooth and route safety validation
- Privacy review for diagnostics, retention duration and any cloud speech processor

These are validation inputs to implementation, QA and downstream approval gates; they are not unresolved ADR statuses.

## Architecture gates

1. **Issue #2:** completed 2026-08-06; ADR-0001..0006 scope-level decisions accepted after review revisions.
2. **Issue #16 Phase 4:** completed 2026-08-27; every Phase 3 decision was checked for consequence, ADR-0007/0008 were added, and the result was directly revalidated against the attached English `MSM2(2).rar` package.
3. **Issue #16 final gate:** still requires the remaining architecture views, traceability/review/approval and merge gates before Issue #4 can be unblocked.

## History and supersession rule

Accepted history must not be silently rewritten. If a future material choice changes an Accepted ADR, create a new ADR whose relationship explicitly states **`Supersedes ADR-xxxx`**, preserve the original ADR file, and update this register.

Replacing an implementation tool that the controlling source explicitly marks replaceable does not require supersession when the approved architectural contracts remain equivalent.

## Lifecycle

- **Proposed:** drafted, not binding
- **Accepted:** reviewed and binding
- **Revised:** changes still required before acceptance
- **Superseded:** replaced while history remains
- **Deprecated:** no longer applicable, with reason

Every ADR includes status, context, drivers, decision, consequences, alternatives, review evidence and validation.
