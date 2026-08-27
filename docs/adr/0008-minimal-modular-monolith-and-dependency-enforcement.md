# ADR-0008: Minimal modular monolith and dependency enforcement

- **Status:** Accepted
- **Date:** 2026-08-27
- **Reviewed:** 2026-08-27
- **Source revalidation:** 2026-08-27 against the directly attached English `MSM2(2).rar`
- **Authority:** attached English `Music_Copilot_PRD_v1_EN_Clarified.pdf`, attached English `Music_Copilot_MVP_v1_EN.pdf`, current Accepted [ADR-0001](0001-native-android-platform.md), [ADR-0003](0003-local-first-data-boundaries.md) and [ADR-0006](0006-no-backend-account-streaming.md)
- **Review evidence:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md), [Phase 3 Decision/Spike Register](../architecture/DECISION_AND_SPIKE_REGISTER.md), [Architecture Traceability Matrix](../architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- **Relationship:** Elaborates ADR-0001, ADR-0003 and ADR-0006. It does **not** supersede them.
- **Outcome:** Accepted as the implementation structure contract for Issue #16 Phase 4

## Attached-source identity and boundary

This ADR was revalidated directly against the attached archive bytes supplied for Issue #16 Phase 4:

- `MSM2(2).rar` SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- `Music_Copilot_PRD_v1_EN_Clarified.pdf`: `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf`
- `Music_Copilot_MVP_v1_EN.pdf`: `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0`
- `ADR-0001_Native_Android_Platform_Revised.pdf`: `7569f77eeb9e10e5113d974244ceec0f6133e4cf29fbfa95907c022abfd4fd51`
- `ADR-0003_Local_First_Data_Boundaries_Revised.pdf`: `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4`
- `ADR-0006_No_Backend_Account_or_Streaming.pdf`: `0d500e878742d0bb0e7a7ff90f25cd4ccd59e74b93e28f5414d370c7ea7a0baf`
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`: `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb`
- `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`: `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3`

No Persian/Farsi PRD or MVP is used as authority or evidence in this ADR.

The attached English System Architecture requires the logical UI/Application/Playback/Voice/Data/Quality boundaries but explicitly says exact Gradle-module granularity remains open. The attached English Technical Blueprint proposes a minimal repository shape and says the final architecture may begin with fewer physical modules when package rules enforce the same logical boundaries; it also gives extraction criteria based on independent ownership, build isolation, reusable contracts or measurable dependency enforcement. Therefore the one-`:app` choice below is an Issue #16 finalization of an intentionally open source decision, not a claim that the older PDF had already mandated one Gradle module.

## Context

The attached English PRD defines a native Android 10+ local-first MVP and shows logical module boundaries around UI, Domain, Playback, Voice and Data while keeping the player service-owned, Voice replaceable and backend/account/streaming absent.

The attached English MVP constrains delivery to ten weeks and prioritizes P0 Player/Voice/Driving/Stability, with P1/P2 work removable before those P0 capabilities. The attached English System Architecture states the principle **smallest enforceable structure** and leaves exact physical module granularity to the final gate. The attached Technical Blueprint gives an illustrative repository blueprint rather than an already-approved Gradle graph.

Phase 3 resolved D-12, D-13 and D-17: production starts as one `:app` Gradle module with explicit package boundaries, single-source-of-truth state placement and controlled Room ownership/migration rules. These choices are consequential because they govern repository shape, dependency direction, composition ownership and the conditions under which code may later be extracted into separate modules.

## Decision drivers

- Keep the initial build structure proportional to a ten-week MVP and current team size
- Enforce architecture boundaries even before separate Gradle modules are justified
- Prevent Android/provider/storage implementation details from leaking into feature/UI code
- Preserve one source of truth for playback, Voice, library, settings and quality state
- Keep MediaStore as media authority and Room as a rebuildable/application-owned projection
- Make future module extraction evidence-driven instead of speculative
- Maintain no-backend/no-account/no-streaming boundaries from ADR-0006
- Preserve implementation choices marked replaceable in the attached sources instead of accidentally turning them into permanent ADR constraints

## Decision

### 1. Initial production structure

Start MVP v1.0 production code as a **minimal modular monolith**: one production Gradle application module, `:app`, with enforced package boundaries.

```text
:app
└── com.latticeward.musiccopilot
    ├── app/                 # Application, Activity, NavHost, DI composition root
    ├── core/                # shared models, AppResult/AppError, dispatcher qualifiers
    ├── feature/             # onboarding, library, search, player, driving, settings, liked(P2)
    ├── playback/            # public port/model, Media3 service, controller, command processor, restore
    ├── voice/               # public port/model, Android provider adapter, mapper, session coordinator
    ├── data/                # public repositories, MediaStore, Room, DataStore implementations
    └── quality/             # public quality port, privacy filter, NoOp/approved adapters

spikes/android-validation/   # isolated non-production evidence harness; never an app dependency
```

The package names describe logical architecture boundaries, not seven independent Gradle modules.

### 2. Composition root and replaceable DI

`app/` is the sole production composition root. It may know concrete implementations in order to wire dependency bindings, Android startup, Activity/NavHost and application-scoped services.

The attached English Technical Blueprint lists **Hilt as a replaceable implementation choice**, not a locked product/architecture constraint. Hilt may be used by the initial implementation, but this ADR does **not** make Hilt irreversible. A different DI mechanism may replace it without a superseding ADR when the public contracts, ownership rules and dependency direction remain equivalent.

Concrete implementation construction must not be scattered through feature code.

### 3. Dependency direction

Allowed dependency direction:

- `feature/*` depends on `core` models and explicit ports/use-case contracts.
- `playback`, `voice`, `data` and `quality` implementations depend on their own public contracts plus `core`.
- Application/domain orchestration may coordinate ports; it must not expose concrete Android/provider/storage types upward.
- `app` may depend on all implementation packages for composition only.

Forbidden dependencies:

- feature-to-feature implementation calls that bypass explicit shared contracts;
- UI/ViewModel directly to ExoPlayer, MediaSession, Room DAO, MediaStore cursor, SpeechRecognizer or external analytics SDK;
- Voice provider implementation becoming playback authority;
- quality/analytics code blocking or deciding product behavior;
- data implementations depending on UI/Compose;
- introducing backend/account/streaming clients under a generic `data` or `core` package.

These directions mirror the attached English architecture/technical documents, which explicitly prohibit UI-to-ExoPlayer/MediaSession/Room/Android-speech coupling and provider/telemetry leakage into product authority.

### 4. State ownership

Authoritative/shared state is placed by owning boundary:

- playback state, current media, active queue and position -> service-owned Playback/MediaSession boundary;
- active Voice state and pending confirmation -> `VoiceSessionCoordinator`;
- device media existence -> Android MediaStore;
- derived searchable/indexed library projection -> Data/Room repository;
- settings, Bluetooth preference and consent controls -> DataStore-backed repository;
- destination-specific presentation state -> destination ViewModel;
- navigation/Voice-overlay origin -> root navigation state holder;
- quality/diagnostic session state -> application-scoped Quality boundary.

Duplicated authoritative state is forbidden. Cached presentation projections may exist only when they are clearly derived and replaceable from the owning source.

The attached English System Architecture/Technical Blueprint already establish the high-level owners for Playback, Voice, Library, UI, Settings and Quality. The final screen/shared-state placements are current Issue #16 resolutions consistent with that ownership model.

### 5. Room ownership and migrations

Room may contain only:

- rebuildable library/search projection and normalized search tokens;
- optional app-owned short history if/when approved by privacy/retention policy;
- optional P2 Likes if retained in MVP scope.

MediaStore remains the authority for whether a media item exists on device. The live playback queue does not live in Room.

If the rebuildable library projection becomes corrupt, it may be cleared and rebuilt without deleting or changing user media.

After a release contains user-owned Likes/history, schema changes affecting those records require explicit tested migrations. Destructive migration is allowed only for rebuildable projection data or pre-release development data whose loss is explicitly acceptable.

The attached ADR-0003 source fixes MediaStore as authority and Room as derived/rebuildable while intentionally leaving exact Room schema/DAO/migrations to later architecture/engineering review. This section closes that ownership/migration policy without changing the local-first boundary.

### 6. Module extraction criteria

Do not create additional production Gradle modules merely because a package represents a logical boundary. A package may be extracted only when at least one concrete benefit is demonstrated:

1. independent ownership/release responsibility;
2. a reusable stable public contract needed by more than one module/application target;
3. meaningful build isolation or measurable build-performance benefit;
4. dependency enforcement that package visibility/static rules cannot reliably provide;
5. test isolation that materially improves feedback time or reliability.

Extraction must preserve dependency direction and cannot be used to introduce v1.1 scope.

These criteria directly preserve the attached Technical Blueprint's rule that physical-module extraction needs independent ownership, build isolation, reusable contract or measurable dependency-enforcement value.

### 7. P1/P2 isolation

P1 restoration/Bluetooth hint and P2 Liked Songs remain separable from P0 Playback, Voice, Driving and Stability. Optional feature code must not become a required dependency for P0 startup, playback or recovery paths.

## Consequences

### Positive

- Fast initial builds and simpler project navigation during the ten-week MVP.
- Clear ownership and dependency rules are available before costly module extraction.
- Feature/UI code remains testable against ports instead of Android SDK/storage/provider implementations.
- MediaStore, Room and DataStore responsibilities remain consistent with ADR-0003.
- No-backend/no-account/no-streaming scope remains structurally visible rather than hidden behind generic abstractions.
- Future extraction has explicit evidence-based gates.
- Replaceable implementation choices such as DI tooling remain replaceable rather than accidentally becoming product authority.

### Costs and risks

- A single Gradle module cannot enforce every dependency rule at compile time by itself; package visibility, conventions, code review and architecture tests are required.
- Without discipline, `core` can become a dumping ground; only cross-boundary primitives/contracts belong there.
- Future extraction may require package moves and dependency-binding adjustments.
- Package boundaries can erode if concrete Android types are exposed through public contracts.

## Alternatives considered

- **Many Gradle modules from day one:** rejected as premature for the current timebox/team and because no measured build/ownership need justifies the overhead yet.
- **Flat package structure:** rejected because it obscures ownership and allows concrete implementation dependencies to spread.
- **Clean Architecture layer modules (`presentation/domain/data`) only:** rejected as insufficiently expressive for service-owned Playback and replaceable Voice boundaries, and likely to encourage generic cross-feature data coupling.
- **Dynamic feature modules:** rejected because the MVP has no delivery/on-demand requirement for them.
- **Backend-style service decomposition:** rejected by ADR-0006 and the local-first single-device product boundary.
- **Room as universal app state store:** rejected because live playback/Voice/navigation state has different lifecycle/authority semantics.
- **Binding Hilt permanently in this ADR:** rejected because the attached technical source explicitly classifies DI/Hilt as replaceable.

## Validation

### Source-conformance validation

- Attached English PRD/MVP product scope remains unchanged and no physical-module choice creates new product capability.
- Logical UI/Application/Playback/Voice/Data/Quality boundaries remain visible even though production starts with one Gradle module.
- ADR-0001's replaceable implementation choices remain replaceable; this ADR does not silently convert Hilt or another DI tool into an irreversible architecture constraint.
- ADR-0003's MediaStore-authority/Room-derived boundary and ADR-0006's no-backend/no-account/no-streaming boundary remain intact.
- The one-`:app` choice is recorded as an Issue #16 finalization of an open physical-granularity decision.

### Architecture/contract validation

- Production settings declare one `:app` module for MVP code; spike/evidence projects remain isolated and non-production.
- Package/dependency map documents allowed and forbidden edges.
- Architecture tests/static checks reject direct UI dependencies on ExoPlayer/MediaSession/DAO/SpeechRecognizer/analytics SDK concrete types.
- Dependency bindings/concrete construction remain in `app` or owning implementation packages, not feature screens.
- State-ownership tests/reviews show one authority for Playback, Voice, MediaStore existence, Room projection, settings and quality sessions.
- Room schema tests prove rebuildable projection can be cleared/rebuilt independently and that released user-owned records use explicit migration tests.
- P1/P2 packages can be removed/disabled without breaking P0 composition or startup paths.

### Extraction review trigger

Before adding a new production Gradle module, record the concrete extraction criterion, expected benefit, affected dependency edges and migration cost in the PR. If the extraction changes architecture ownership or scope materially, create a new ADR.

## Supersession rule

This ADR is binding for MVP v1.0. A future move to a materially different module topology or state/data ownership model requires a new ADR that explicitly **Supersedes ADR-0008**. Replacing a source-declared replaceable tool such as Hilt does not require supersession when the approved contracts and dependency direction remain unchanged. ADR-0001, ADR-0003 and ADR-0006 remain Accepted and unchanged.
