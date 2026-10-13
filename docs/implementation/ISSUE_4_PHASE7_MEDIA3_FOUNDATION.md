# Issue #4 — Phase 7 Media3 / MediaSessionService foundation and acceptance

- **Date:** 13 October 2026
- **Status:** **PHASE 7 FOUNDATION PASS — verified Android build and JVM scope/command contract suite; no physical-device playback proof**
- **Branch:** [feature/4-android-foundation](https://github.com/latticeward-labs/music-copilot/tree/feature/4-android-foundation)
- **Issue:** [Issue #4](https://github.com/latticeward-labs/music-copilot/issues/4)
- **Tested implementation SHA:** [6c84d6896b016d80913e81097c69ac26165f0e93](https://github.com/latticeward-labs/music-copilot/commit/6c84d6896b016d80913e81097c69ac26165f0e93)
- **Source archive:** MSM2(1)(1).rar, actual local SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (13 RAR entries: 12 source PDFs, 1 directory)
- **Authority:** [Final Issue #16 contracts](../architecture/IMPLEMENTATION_STRUCTURE_AND_PUBLIC_CONTRACTS.md), [runtime policies](../architecture/RUNTIME_POLICIES.md), [ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md).

## 1. Implementation inventory

| Component | Code | Contract |
|---|---|---|
| Android MediaSessionService | [MusicPlaybackService](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/MusicPlaybackService.kt) | Single Android **same-process service** owns the real ExoPlayer and MediaSession; initializes in `onCreate`, releases from service `onDestroy`; no Activity/VM player creation. |
| One-start service factory | [ServicePlaybackRuntimeOwner](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/ServicePlaybackRuntimeOwner.kt) | Exactly one `start` per service instance; idempotent release; construction failure releases candidate player. No global locator or app/DI singleton. |
| Structured service coroutine owner | [PlaybackServiceScopeOwner](../../app/src/main/java/com/latticeward/musiccopilot/playback/service/PlaybackServiceScopeOwner.kt) | Service-only SupervisorJob + Main.immediate dispatcher. Service closes it, independent of UI. |
| Typed serialized commands | [PlaybackCommandProcessor](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackCommandProcessor.kt) | One consumer; **32 queued waiting submissions + one in-flight**, typed `PLAYBACK_BUSY` on queue overflow; accepted commands complete with acknowledged result or typed terminal error. No silent drops on processor close. |
| Service/session protocol | [PlaybackWireProtocol](../../app/src/main/java/com/latticeward/musiccopilot/playback/command/PlaybackWireProtocol.kt) | Typed Media3 custom commands and whole-snapshot encoding; versioned wire key, bounded arguments, stable sanitized error codes. No transcript, path or real media metadata. |
| Authoritative projection | [Media3SnapshotProjector](../../app/src/main/java/com/latticeward/musiccopilot/playback/media3/Media3SnapshotProjector.kt) | Reads only service-owned Player; publishes `PlaybackSnapshot` atomically including revision/queueRevision, position, item, controls and permitted service error categories. |
| Application controller | [PlaybackControllerConnection](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/PlaybackControllerConnection.kt) | App-owned MediaController via service SessionToken; UI never creates/release Player. Sends typed commands using `sendCustomCommand`; 10s adapter timeout is a local implementation timeout, not a product target. |
| Complete reconnect gate | [ControllerSnapshotRelay](../../app/src/main/java/com/latticeward/musiccopilot/playback/controller/ControllerSnapshotRelay.kt) | `Connecting`/error may keep last confirmed snapshot; `Ready` only **after an entire valid service snapshot is replaced**; older revisions from the same session ignored; a fresh reconnection may reset revision safely. |
| Manifest | [AndroidManifest.xml](../../app/src/main/AndroidManifest.xml) | One service with MediaSessionService intent action, `foregroundServiceType=mediaPlayback`, and both Android foreground-service permissions for current target. |
| DI | [PlaybackBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/PlaybackBindings.kt), [ApplicationBindings](../../app/src/main/java/com/latticeward/musiccopilot/app/di/ApplicationBindings.kt) | Exactly one application connection; `onForeground` retries connection; no production Player, MediaSession or command processor in `app/di`. |

## 2. Source-of-truth and lifecycle

The application-scoped controller binds to one service-owned MediaSession through a `SessionToken`. Service-created Player/MediaSession outlive individual Activity creations. `PlaybackSnapshot` is encoded into session extras by the **service**, and the controller atomically replaces a complete snapshot **before** reporting `Ready`. The client does not use a copied Room queue, a DataStore checkpoint or Compose state as playback truth.

The service's typed internal custom-command channel is **restricted to same-package, same-UID application controllers**. External Android MediaController transport remains under MediaSession's single service-owned Player and Android's player looper; the detailed parity policy for external/system commands is an explicit downstream Issue #7 gate, not a Phase 7 full-feature acceptance claim.

`ReplaceQueue` is deliberately **rejected** in the current Phase 7 foundation rather than pretending local files have been validated and prepared. Local queue construction, audio focus, interrupt provenance, Bluetooth route guards, seek semantics and system-control parity require the complete Playback Core (#7/#8), plus physical-device QA (#13). Consequently the current build establishes the real service foundation but **does not establish working on-device music playback as an MVP feature**.

On service destruction: processor rejects/drains pending requests; outstanding MediaSession command futures are completed with typed rejection; MediaSession and Player are released once by the service owner, and service scope is canceled. UI recreation itself never releases or constructs either resource.

## 3. Actual tests and CI

- [Phase7PlaybackFoundationTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/Phase7PlaybackFoundationTest.kt) contains **7 additional JVM tests** for: one-start/one-close factory, 32 pending + one in-flight and overflow, sequential ordering, teardown cancellation, sanitized errors, whole-snapshot/reconnect/stale-revision behavior, service-only error families, and static source independence from Activity/app graph.
- Existing [LifecycleScopesTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/LifecycleScopesTest.kt) verifies fresh service-owned scope jobs and cancellation.
- Existing [StaticOwnershipTest](../../app/src/test/java/com/latticeward/musiccopilot/foundation/StaticOwnershipTest.kt) guards Activity and Application against ExoPlayer/MediaSession/Voice owner creation.
- GitHub source audit: exactly **one** `<service>` declaration, **one** `ExoPlayer.Builder(` and **one** `MediaSession.Builder(` creation in the service class, FGS type and both foreground-service permissions present, capacity **32**. Activity contains no Player constructor.

**Real verified CI:** [Run 38442180745](https://github.com/latticeward-labs/music-copilot/actions/runs/38442180745), job [115451846096](https://github.com/latticeward-labs/music-copilot/actions/runs/38442180745/job/115451846096), code SHA [6c84d6896b016d80913e81097c69ac26165f0e93](https://github.com/latticeward-labs/music-copilot/commit/6c84d6896b016d80913e81097c69ac26165f0e93), `completed/success`. Log: `SELFTEST PASS: 24 fixtures`, `ARCHITECTURE PASS: one :app module; 52 package paths; 99 source files`, `:app:assembleDebug`, `:app:testDebugUnitTest`, `:app:assembleRelease`, `ROOM_SCHEMA_EXPORT PASS`, `BUILD SUCCESSFUL in 7m 34s`.

**Real [Artifact 11837751263](https://github.com/latticeward-labs/music-copilot/actions/runs/38442180745/artifacts/11837751263)** inspected: Debug APK, unsigned Release APK, nine JUnit XML suites, **49 tests / 0 failures / 0 errors / 0 skipped** and byte-consistent [Room v1 schema](../../app/schemas/com.latticeward.musiccopilot.data.room.MusicCopilotDatabase/1.json) with Git blob SHA `c3b1ba7affdaa1a9d6425d3b3dc7ddf10adf300b`.

The earlier [failed run 38441153101](https://github.com/latticeward-labs/music-copilot/actions/runs/38441153101) exposed three obsolete `connection.value` references after the relay refactor; corrected in code, final run passed. Intermediate canceled runs are not counted as successes.

## 4. Remaining gates and truthful non-claims

- No Android emulator or physical smartphone instrumentation was executed for actual MediaSessionService/Activity destruction, lockscreen/background notification, Bluetooth or multiple device models.
- `ReplaceQueue` intentionally unsupported; detailed real-item playback, queue, seek, external control and service interruption/route policy are future Issue #7/#8 deliverables.
- Versioned P1 paused-only queue restoration requires MediaStore validation, missing-item drop and duration clamp; Phase 7 does not consume P1 DataStore as live authority. Do not assert successful P1 cold restore.
- No Cloud STT, backend, streaming, microphone, raw transcripts, media path, user credentials or diagnostics SDK has been enabled.
- Full Issue #4 cross-phase Acceptance Criteria remain **1/14 checked; 13 open**. The Issue must stay `OPEN / In Progress`; no PR merge or final project completion is approved by this bounded Phase 7 PASS.
