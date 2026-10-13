# Issue #4 Phase 5 — Dependency Injection and Lifecycle Contract

- **Date:** 13 October 2026
- **Status:** **IMPLEMENTED — REAL CI CONFIRMATION PENDING** (do not mark Phase 5 PASS before the final test run)
- **Issue:** [#4](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Branch:** [`feature/4-android-foundation`](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Implementation tested SHA (target):** [`9cc97090b74d625221a0074670a5fce37b47f148`](https://github.com/latticeward-labs/music-copilot/commit/9cc97090b74d625221a0074670a5fce37b47f148)
- **Authoritative sources:** [Issue #16 Public Contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md)
- **Uploaded MSM2:** SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` matched authoritative archive

## 1. Binding choice

This MVP foundation uses **constructor injection with manual binding functions in the sole `app/di` composition root**. Hilt is a permitted implementation choice but is not installed because the approved architecture binds ownership and lifetimes, not Hilt itself; for the current single physical `:app` project a small typed graph is adequate. A later Hilt replacement may only change wiring, preserving the same public interfaces and scope owners.

[MusicCopilotApplication](../../app/src/main/java/com/latticeward/musiccopilot/app/MusicCopilotApplication.kt) constructs **one** [ApplicationBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) in `onCreate`. There is no static/global DI holder, Activity-created graph, feature-local binding factory or second Voice owner. `MainActivity` remains the Phase 2 neutral Compose bootstrap; it creates none of the bound objects. No dependency on a private credential, backend, analytics SDK or cloud service is needed.

Single-authority components are **not** created by the application graph: it contains no ExoPlayer, MediaSession, MediaSessionService or PlaybackCommandProcessor. Only the future service lifecycle can instantiate these concrete runtime owners (Phase 7).

## 2. Scope/lifetime matrix

| Object / work | Phase 5 owner | Dispatcher or job | Destruction boundary |
|---|---|---|---|
| `ApplicationBindings` | Android `MusicCopilotApplication` process | One application root `SupervisorJob` | Process end; test `close()`; Android `onTerminate` is not a real-device teardown guarantee |
| `PlaybackControllerConnection` implementing `PlaybackGateway` | One application graph | `applicationScope`, Main.immediate | App scope / connection teardown, **not Activity recreation** |
| `LibraryRepository`, `PreferencesRepository`, `RestorableQueueStore` | One application graph | `repositoryScope`, child `SupervisorJob`, injected IO | Repository/application cancellation; restartable after process death |
| `VoiceSessionCoordinator` | One application graph | `voiceScope`, child `SupervisorJob`, Main.immediate | Application; each active Voice generation gets one subordinate cancellable Job |
| STT/TTS engines | Injected behind Voice public ports | Active session cancellation calls STT `cancel` and TTS `stop` | Session replacement, cancel, or owner teardown |
| `TelemetryPrivacyGate` + `TelemetrySink` | One application graph | Synchronous deny-all / NoOp; no async worker | Application |
| `PlaybackServiceScopeOwner` template | **Future MediaSessionService only**, not application | Fresh service `SupervisorJob + dispatchers.main` (production Main.immediate) | `MediaSessionService.onDestroy`, Phase 7 to instantiate |
| UI/destination ViewModels | **Future destination scope** | `viewModelScope`, lifecycle-aware UI collecting public ports | ViewModel cleared; not created in Phase 5 |
| CPU-bound policy tasks | Application and domain | Injected `dispatchers.default` | Caller/owner cancellation |
| Clock | Constructor-injected `core.time.Clock` | Real SystemClock or synthetic test clock | No persistence authority |

Injection seams: [DispatcherProvider](../../app/src/main/java/com/latticeward/musiccopilot/core/dispatchers/DispatcherProvider.kt), [Clock](../../app/src/main/java/com/latticeward/musiccopilot/core/time/Clock.kt), [RuntimeProviders](../../app/src/main/java/com/latticeward/musiccopilot/app/di/RuntimeProviders.kt). No `GlobalScope`.

## 3. Binding owner inventory

- [PlaybackBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/PlaybackBindings.kt) binds one application-scoped [PlaybackControllerConnection](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt). Until actual MediaSession wiring in Phase 7, `Disconnected` and `CONTROLLER_DISCONNECTED` are the truthful outputs; it never fabricates `Playing`.
- [DataBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/DataBindings.kt) binds [foundation fail-closed repositories](../../app/src/main/java/com/latticeward/musiccopilot/data/repository/FoundationRepositories.kt). No MediaStore/Room index is claimed and no fake preference persistence or P1 restore is acknowledged. Phase 6 owns the real adapters.
- [VoiceBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/VoiceBindings.kt) binds one [DefaultVoiceSessionCoordinator](../../app/src/main/java/com/latticeward/musiccopilot/voice/session/DefaultVoiceSessionCoordinator.kt) and two [disabled providers](../../app/src/main/java/com/latticeward/musiccopilot/voice/android/UnavailableVoiceEngines.kt). Session jobs are cancellable, but platform recognition, one-retry policy, real generation callback/timer path and actual six-intent processing remain Phase 8.
- [QualityBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/QualityBindings.kt) binds [deny-all gate](../../app/src/main/java/com/latticeward/musiccopilot/quality/privacy/DenyAllTelemetryPrivacyGate.kt) plus [NoOp sink](../../app/src/main/java/com/latticeward/musiccopilot/quality/noop/NoOpTelemetrySink.kt). The Phase 9 schema/allowlist privacy gate remains a separate required gate; no production sink/provider is enabled.
- [PlaybackServiceScopeOwner](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackServiceScopeOwner.kt) is deliberately **not instantiated by application DI**; it is a template for Phase 7 service creation and destruction, not proof of Media3 playback.

## 4. Tests and evidence gate

| JVM test source | Verifies | Not verified by this test |
|---|---|---|
| [ApplicationCompositionTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/ApplicationCompositionTest.kt) | Stable per-graph port identity, separate graphs do not share owners, child-job cancellation, no optimistic playback, typed unavailable Data/Voice, privacy NoOp | Android Activity instrumentation/device playback |
| [LifecycleScopesTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/LifecycleScopesTest.kt) | Two independently constructed service-owned scope templates, one Voice generation child, replacement cancels prior job and invokes STT/TTS cancellation, unavailable provider doesn't create a fake session | Actual MediaSessionService/Android STT lifecycle |
| [StaticOwnershipTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/StaticOwnershipTest.kt) | Activity does not construct graph, Player, MediaSession or Voice owner; Application sole graph creation; application graph does not construct service scope | OEM and Activity instance recreation instrumentation |

The existing Phase 4 public-port tests remain active. The GitHub workflow [`.github/workflows/issue-4-phase2-bootstrap.yml`](../../.github/workflows/issue-4-phase2-bootstrap.yml) executes Python architecture negative fixtures, Gradle `testDebugUnitTest`, Debug and unsigned Release builds from a clean GitHub Actions checkout.

**Required for Phase 5 PASS:** a real latest-code CI run must show checker and JVM suite green, Debug/Release compilation, correct scope ownership tests; record actual SHA, job, artifact, and conclusions. Do not treat pending/failed/cancelled workflow runs as PASS.

## 5. Future work, non-claims and safety

- **Phase 6:** real MediaStore/Room/DataStore, migration and P1 snapshot store; all current unavailable adapters are intentionally temporary.
- **Phase 7:** same-process MediaSessionService, service-only Player/MediaSession/command processor, actual controller reconnect and Activity recreation tests; do not represent our fake lifecycle unit tests as service proof.
- **Phase 8:** real voice provider eligibility/cancellation, timeout/retry and deterministic mapper, replacing the fail-closed adapters.
- **Phase 9:** typed allowlist and full runtime privacy rejection/fault isolation.
- **Phase 10–13:** real Compose lifecycle collection, ViewModel `viewModelScope`, full formatter/lint/quality CI and two representative device launches.
- **Phase 14:** final audit, independent reviews, PR/merge/close. Issue #4 remains open while remaining ACs are unverified.

No logging or persistence of personal media, audio/transcripts, or identifiers; no cloud STT or network usage. Any change to authority, process topology, state ownership or persistence truth needs the usual ADR/architecture change control.
