# Software Architecture Baseline — MVP v1.0

- **Status:** In progress — **Phase 4 ADR gate PASS**, final Issue #16 approval still pending
- **Tracking:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16)
- **Depends on:** [Issue #2](https://github.com/latticeward-labs/music-copilot/issues/2), [Issue #3](https://github.com/latticeward-labs/music-copilot/issues/3) and [Issue #15](https://github.com/latticeward-labs/music-copilot/issues/15)
- **Authority:** [PRD v1.0](PRD.md), [MVP v1.0](MVP.md) and accepted [ADRs](adr/README.md)
- **Phase 4 evidence:** [ADR Decision Audit](architecture/ADR_PHASE4_AUDIT.md), [Decision/Spike Register](architecture/DECISION_AND_SPIKE_REGISTER.md)

This file is the home of the final pre-implementation architecture baseline. Phase 4 has finalized consequential runtime and repository-structure decisions, but the complete Issue #16 baseline still requires the remaining architecture views, final traceability/review and approval gates. Missing details must not be invented inside feature implementation.

## 1. Baseline constraints established by product scope

- Native Android 10+ with Kotlin and Jetpack Compose
- Lawful local audio only; no backend, account, cloud sync, online catalog or streaming
- Media3 playback independent of UI lifecycle and owned by a service
- MediaStore for device media discovery
- Room for indexed/local structured data and DataStore for settings
- Push-to-talk; Android STT/TTS behind a replaceable interface
- Restricted deterministic six-intent mapping
- Raw voice is not stored; prohibited personal media metadata does not enter telemetry
- Android 10/12/14+, Pixel/Samsung/Xiaomi and at least three vehicle/head-unit models in QA

## 2. Phase 4 finalized consequential decisions

The following decisions are now binding for MVP v1.0. They are textual architecture decisions first; later diagrams may illustrate them but may not become their only record.

### Runtime, lifecycle and concurrency — ADR-0007

See [ADR-0007](adr/0007-same-process-playback-runtime-and-safe-paused-restore.md).

- Playback uses a same-application-process `MediaSessionService`; no playback `android:process` is configured.
- UI lifecycle independence is provided by service ownership, not process isolation.
- Player and MediaSession are constructed/released by service lifecycle and are never UI-owned.
- There is no boot/app-start autoplay. Foreground media lifetime starts only from legitimate user/system playback interaction.
- One application-scoped `PlaybackControllerConnection` exposes `Disconnected`, `Connecting`, `Ready` and `RecoverableError` plus a complete authoritative `PlaybackSnapshot` projection.
- P0 guarantees UI recreation/closure and active service continuity within supported Android lifecycle behavior; process-death survival/force-stop is not a P0 guarantee.
- All UI/system/Voice playback commands converge on one service-owned sequential `PlaybackCommandProcessor` with command IDs, authoritative acknowledgement and bounded capacity 32. Parallel Player mutation and silent drops are forbidden.
- `GlobalScope` is forbidden. UI uses `viewModelScope`; Playback uses a service-owned `SupervisorJob`; Voice uses one cancellable child session job; application/repository scopes own restartable work that must outlive a screen.
- Player/MediaSession mutation remains on the application/player looper; IO owns MediaStore/Room/DataStore/blocking adapters; Default owns normalization/matching; Main publishes UI/player state.
- `VoiceSessionCoordinator` owns exactly one active Voice generation and pending confirmation. New sessions and invalidating events cancel prior STT/TTS/timeout/ducking work; stale-token callbacks are ignored.
- FR-08 queue/position restoration is P1 only. A versioned Proto DataStore `RestorableQueueSnapshot` stores stable MediaStore re-query references and state needed for validation, never title/artist/path; cold restore validates and prepares **Paused** only, never autoplay.
- Call/focus/route automatic resume occurs only when prior-playing, focus-returned, unchanged valid route generation and no manual pause all hold. Route disconnect pauses, cancels Voice and invalidates route generation; reconnect remains Paused until explicit Resume.

### Minimal modular monolith and dependency enforcement — ADR-0008

See [ADR-0008](adr/0008-minimal-modular-monolith-and-dependency-enforcement.md).

Production starts with one `:app` Gradle module using enforced logical packages:

```text
:app
└── com.latticeward.musiccopilot
    ├── app/                 # Application, Activity, NavHost, Hilt composition root
    ├── core/                # shared models, AppResult/AppError, dispatcher qualifiers
    ├── feature/             # onboarding, library, search, player, driving, settings, liked(P2)
    ├── playback/            # api, Media3 service, controller, command processor, restore
    ├── voice/               # api, Android provider, mapper, session coordinator
    ├── data/                # api, MediaStore, Room, DataStore
    └── quality/             # api, privacy filter, NoOp/approved adapters

spikes/android-validation/   # isolated non-production evidence harness; not an app dependency
```

Allowed direction: feature/UI code depends on models/ports/use cases; Playback/Voice/Data/Quality concrete implementations stay behind their own contracts; `app` is the composition root and may wire concrete implementations.

Forbidden dependencies include UI/ViewModel direct access to ExoPlayer, MediaSession, Room DAO, MediaStore cursor, SpeechRecognizer or external analytics SDK; feature-to-feature implementation calls; Voice/provider code becoming playback authority; quality adapters blocking product behavior; and any backend/account/streaming client introduced under generic infrastructure packages.

A package becomes a separate production Gradle module only when there is evidence for independent ownership, stable reuse, meaningful build isolation/performance, stronger dependency enforcement or material test-isolation benefit. Module extraction may not introduce v1.1 scope.

## 3. Finalized compatible decisions recorded in the architecture

These Phase 3 decisions are compatible with the existing ADRs and therefore do not require an additional ADR in Phase 4. They remain binding architecture rules and are not allowed to exist only inside diagrams.

### Navigation and Voice overlay ownership

- Single-activity Compose `NavHost`.
- Destinations: Bootstrap, Onboarding, Permission recovery, Home/Library, Search, Full Player, Driving, Settings and optional P2 Liked Songs.
- Voice is an overlay/state owned by the current Player/Driving origin plus `VoiceSessionCoordinator`, not an independent navigation destination.
- Back first dismisses/cancels Voice when active.
- Driving Back/Exit immediately returns to its origin.
- Android Settings/permission screens are external handoffs followed by capability re-check.

### Typed error and recovery model

Use sealed `AppError` categories: Permission, Library, MediaItem, Playback, Controller, Voice, Interruption, Storage, Privacy and Quality.

Each error exposes a stable non-sensitive code, recoverability and one `RecoveryAction`: `Retry`, `OpenSettings`, `Rebuild`, `Skip`, `ManualFallback`, `ManualResume` or `None`. Throwables remain inside infrastructure boundaries; UI observes typed state. Quality failures never block core product behavior.

Raw audio, full transcript, title, filesystem path, full library contents and unnecessary device identifiers are prohibited diagnostic attributes.

### Data/state authority

- Playback/current media/live queue/position -> MediaSessionService Playback boundary
- Voice/pending confirmation -> VoiceSessionCoordinator
- Media existence -> MediaStore
- Derived library/search projection -> Room repository
- Settings/Bluetooth preference/consent -> DataStore repository
- Destination presentation state -> destination ViewModel
- Navigation/Voice-overlay origin -> root navigation state holder
- Quality session -> application-scoped Quality boundary

No duplicated authoritative state is allowed. Presentation caches must be replaceable projections of the owning authority.

### Room ownership and migration policy

Room contains only rebuildable library/search projection plus optional approved short history and optional P2 Likes. MediaStore remains media existence authority; active queue never lives in Room. Rebuildable projection corruption may be cleared/rebuilt without modifying user media. Released user-owned Likes/history require explicit tested migrations; destructive migration is limited to rebuildable projection or explicitly disposable pre-release data.

### Numeric interaction and Voice policy

MVP constants:

- seek-back: **10 seconds**
- PREVIOUS: restart current track when position is **> 3 seconds**, otherwise move to previous item
- Voice listening timeout: **6 seconds**
- retry: **one**
- direct-execution provider confidence `T_DIRECT`: **0.90**
- future validated tuning range: **0.80..0.98**
- artist clear similarity: **0.88**
- plausible similarity: **0.72**
- minimum top-two separation: **0.08**

Missing/invalid confidence never direct-executes. Tuning cannot add intents, widen grammar or bypass confirmation.

### Provider and quality defaults

- Speech remains behind the replaceable ADR-0004 port.
- Production Voice defaults to **Manual Only** unless capability, disclosure and consent gates pass; no generic offline guarantee is claimed.
- Cloud speech is disabled by default.
- Crash/analytics/remote-config/quality adapters are NoOp by default until approved configuration/retention/allowlist exists.
- P0 browsing/manual playback has no network dependency.
- Retention-bound diagnostic/history collection remains disabled until a positive approved duration exists.
- Clear-data actions are explicit and category-scoped; protected media, live queue, settings/consent control and library authority are not falsely represented as cleared.

## 4. Architecture views required before final approval

### 4.1 System context

_To be completed in the diagram/view phase: Android app, Android platform services, local media, Bluetooth/head unit, optional STT processing and privacy-safe quality services._

### 4.2 Component boundaries

Required logical boundaries:

- UI / Presentation
- Application / Domain
- Playback
- Voice
- Data
- Quality / Diagnostics

Each boundary must define responsibilities, public contracts, allowed dependencies and forbidden dependencies consistently with ADR-0007/0008.

### 4.3 State ownership

The textual ownership contract is finalized in Sections 2–3. The remaining view must diagram it without creating a second source of truth.

### 4.4 Lifecycle and background behavior

The textual runtime contract is finalized by ADR-0007. The remaining view must visualize UI recreation, service lifetime, controller reconnection, notification/lock-screen/Bluetooth synchronization, process-death limits and interruption recovery.

### 4.5 Critical data flows

Required diagrams or sequence descriptions:

1. MediaStore scan -> normalization/index -> Library UI
2. Track selection -> queue -> service-owned playback -> UI observation
3. Manual playback command -> command processor -> service -> authoritative state update
4. Voice tap -> STT -> restricted mapper -> certainty/confirmation -> serialized playback command -> acknowledgement -> TTS
5. Call/focus/route interruption -> guarded reducer -> playback/UI/notification synchronization
6. Allowed telemetry event -> privacy filter -> NoOp/approved quality service
7. P1 cold restoration -> Proto snapshot -> MediaStore revalidation -> Paused preparation

### 4.6 Concurrency policy

Finalized by ADR-0007; remaining architecture work must map scopes/dispatchers/cancellation to components and tests without changing the decision silently.

### 4.7 Persistence policy

- MediaStore: authoritative device media existence
- Room: rebuildable library/search projection plus controlled optional app-owned records
- DataStore: settings/consent and P1 non-authoritative restorable queue snapshot
- Live queue/playback: service-owned, never Room/DataStore authority

### 4.8 Navigation and overlay ownership

Finalized in Section 3; remaining work must diagram destinations, entry/exit/back rules and overlay origin.

### 4.9 Error model

Finalized in Section 3; remaining work must map each P0 failure to user recovery, safe logging and tests.

### 4.10 Testing architecture

The final baseline must define unit, integration, UI/service and field-test seams for Intent mapping, queue/seek behavior, repositories, MediaSession/service synchronization, STT adapters, Audio Focus, route handling, controller reconnection, command ordering/overflow, cancellation and Safe-Paused restoration.

### 4.11 Module/package strategy

Finalized by ADR-0008. Later package diagrams and code scaffolding must implement the same dependency direction and extraction criteria.

## 5. ADR control and supersession policy

The authoritative ADR register is [docs/adr/README.md](adr/README.md).

- ADR-0001..0006 remain Accepted and were not rewritten in Phase 4.
- ADR-0007 and ADR-0008 are new Accepted decisions that **elaborate**, not supersede, existing ADRs.
- If a future choice materially changes an Accepted ADR, create a new ADR explicitly stating `Supersedes ADR-xxxx`; preserve the old file and update the register.
- No material decision may be recorded only in a Mermaid/C4/sequence/state diagram.

Phase 4 audit result: [PASS / COMPLETE](architecture/ADR_PHASE4_AUDIT.md).

## 6. Approval checklist

- [ ] Approved User Flows and Wireframes are traceable to architecture paths.
- [ ] Context, component, state-ownership and critical data-flow views exist.
- [x] Consequential runtime/service/concurrency/restoration decisions are explicit in ADR-0007.
- [x] Consequential module/package/dependency/state/data-ownership decisions are explicit in ADR-0008.
- [x] Accepted ADR-0001..0006 history is preserved with no silent supersession.
- [x] Navigation, Voice overlay, typed error, provider-default and numeric-policy decisions are textually recorded and not diagram-only.
- [ ] Dependency direction and public contracts are fully mapped at component/package level.
- [ ] Lifecycle/background/restoration views are complete.
- [ ] Privacy, telemetry and prohibited-data boundaries are fully mapped to flows/components.
- [ ] Test seams and technical-spike results are fully mapped to the final architecture views.
- [ ] Product, Android, Design and QA approve the complete baseline.

## 7. Phase 4 result

**PASS / COMPLETE.** Every Phase 3 decision was reviewed for consequence. ADR-0007 and ADR-0008 formalize the two consequential decision groups, ADR-0001..0006 remain unchanged, the ADR register is updated, and no important architecture choice is left solely inside a diagram.

This is **not** final Issue #16 approval and does not unblock Issue #4 by itself.
