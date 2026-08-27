# ADR-0008: Minimal modular monolith and dependency enforcement

- **Status:** Accepted
- **Date:** 2026-08-27
- **Reviewed:** 2026-08-27
- **Authority:** [PRD v1.0](../PRD.md), [MVP v1.0](../MVP.md), accepted [ADR-0001](0001-native-android-platform.md), accepted [ADR-0003](0003-local-first-data-boundaries.md), accepted [ADR-0006](0006-no-backend-account-streaming.md)
- **Review evidence:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Phase 3 Decision/Spike Register](../architecture/DECISION_AND_SPIKE_REGISTER.md), [Architecture Traceability Matrix](../architecture/ARCHITECTURE_TRACEABILITY_MATRIX.md)
- **Relationship:** Elaborates ADR-0001, ADR-0003 and ADR-0006. It does **not** supersede them.
- **Outcome:** Accepted as the implementation structure contract for Issue #16 Phase 4

## Context

The MVP is a native Android 10+ application with a ten-week delivery timebox and a deliberately narrow scope. The final architecture must make UI, Domain/Application, Playback, Voice, Data and Quality responsibilities unambiguous without creating speculative enterprise complexity or a large Gradle-module graph before implementation evidence exists.

Phase 3 resolved D-12, D-13 and D-17: production starts as one `:app` Gradle module with explicit package boundaries, single-source-of-truth state placement and controlled Room ownership/migration rules. These choices are consequential because they govern repository shape, dependency direction, composition ownership and the conditions under which code may later be extracted into separate modules.

## Decision drivers

- Keep the initial build structure proportional to a ten-week MVP and current team size
- Enforce architecture boundaries even before separate Gradle modules are justified
- Prevent Android/provider/storage implementation details from leaking into feature/UI code
- Preserve one source of truth for playback, Voice, library, settings and quality state
- Keep MediaStore as media authority and Room as a rebuildable/application-owned projection
- Make future module extraction evidence-driven instead of speculative
- Maintain no-backend/no-account/no-streaming boundaries from ADR-0006

## Decision

### 1. Initial production structure

Start MVP v1.0 production code as a **minimal modular monolith**: one production Gradle application module, `:app`, with enforced package boundaries.

```text
:app
└── com.latticeward.musiccopilot
    ├── app/                 # Application, Activity, NavHost, Hilt composition root
    ├── core/                # shared models, AppResult/AppError, dispatcher qualifiers
    ├── feature/             # onboarding, library, search, player, driving, settings, liked(P2)
    ├── playback/            # public port/model, Media3 service, controller, command processor, restore
    ├── voice/               # public port/model, Android provider adapter, mapper, session coordinator
    ├── data/                # public repositories, MediaStore, Room, DataStore implementations
    └── quality/             # public quality port, privacy filter, NoOp/approved adapters

spikes/android-validation/   # isolated non-production evidence harness; never an app dependency
```

The package names describe logical architecture boundaries, not seven independent Gradle modules.

### 2. Composition root

`app/` is the sole production composition root. It may know concrete implementations in order to wire Hilt bindings, Android startup, Activity/NavHost and application-scoped services.

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

### 5. Room ownership and migrations

Room may contain only:

- rebuildable library/search projection and normalized search tokens;
- optional app-owned short history if/when approved by privacy/retention policy;
- optional P2 Likes if retained in MVP scope.

MediaStore remains the authority for whether a media item exists on device. The live playback queue does not live in Room.

If the rebuildable library projection becomes corrupt, it may be cleared and rebuilt without deleting or changing user media.

After a release contains user-owned Likes/history, schema changes affecting those records require explicit tested migrations. Destructive migration is allowed only for rebuildable projection data or pre-release development data whose loss is explicitly acceptable.

### 6. Module extraction criteria

Do not create additional production Gradle modules merely because a package represents a logical boundary. A package may be extracted only when at least one concrete benefit is demonstrated:

1. independent ownership/release responsibility;
2. a reusable stable public contract needed by more than one module/application target;
3. meaningful build isolation or measurable build-performance benefit;
4. dependency enforcement that package visibility/static rules cannot reliably provide;
5. test isolation that materially improves feedback time or reliability.

Extraction must preserve dependency direction and cannot be used to introduce v1.1 scope.

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

### Costs and risks

- A single Gradle module cannot enforce every dependency rule at compile time by itself; package visibility, conventions, code review and architecture tests are required.
- Without discipline, `core` can become a dumping ground; only cross-boundary primitives/contracts belong there.
- Future extraction may require package moves and Hilt binding adjustments.
- Package boundaries can erode if concrete Android types are exposed through public contracts.

## Alternatives considered

- **Many Gradle modules from day one:** rejected as premature for the current timebox/team and because no measured build/ownership need justifies the overhead yet.
- **Flat package structure:** rejected because it obscures ownership and allows concrete implementation dependencies to spread.
- **Clean Architecture layer modules (`presentation/domain/data`) only:** rejected as insufficiently expressive for service-owned Playback and replaceable Voice boundaries, and likely to encourage generic cross-feature data coupling.
- **Dynamic feature modules:** rejected because the MVP has no delivery/on-demand requirement for them.
- **Backend-style service decomposition:** rejected by ADR-0006 and the local-first single-device product boundary.
- **Room as universal app state store:** rejected because live playback/Voice/navigation state has different lifecycle/authority semantics.

## Validation

### Architecture/contract validation

- Production settings declare one `:app` module for MVP code; spike/evidence projects remain isolated and non-production.
- Package/dependency map documents allowed and forbidden edges.
- Architecture tests/static checks reject direct UI dependencies on ExoPlayer/MediaSession/DAO/SpeechRecognizer/analytics SDK concrete types.
- Hilt bindings/concrete construction remain in `app` or owning implementation packages, not feature screens.
- State-ownership tests/reviews show one authority for Playback, Voice, MediaStore existence, Room projection, settings and quality sessions.
- Room schema tests prove rebuildable projection can be cleared/rebuilt independently and that released user-owned records use explicit migration tests.
- P1/P2 packages can be removed/disabled without breaking P0 composition or startup paths.

### Extraction review trigger

Before adding a new production Gradle module, record the concrete extraction criterion, expected benefit, affected dependency edges and migration cost in the PR. If the extraction changes architecture ownership or scope materially, create a new ADR.

## Supersession rule

This ADR is binding for MVP v1.0. A future move to a materially different module topology or state/data ownership model requires a new ADR that explicitly **Supersedes ADR-0008**. ADR-0001, ADR-0003 and ADR-0006 remain Accepted and unchanged.
