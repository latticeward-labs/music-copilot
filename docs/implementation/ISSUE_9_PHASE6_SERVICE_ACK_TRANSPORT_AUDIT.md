# Issue #9 — P06 Service-ACK Driving transport wiring and static source audit

**Date:** 2026-10-24 · **Gate:** `PASS_SCOPED_COMMAND_WIRING_SOURCE_ONLY_UNTESTED` (not Android compile/runtime/AC PASS). **P07 NEXT.**  
**Repository:** https://github.com/latticeward-labs/music-copilot · **Issue:** https://github.com/latticeward-labs/music-copilot/issues/9  
**Branch:** `feature/9-driving-mode-bluetooth-suggestion` reused from P00; no PR, no merge.  
**Reattached source archive:** `/mnt/data/MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`; verified exact P00/P01 hash. The attached clarified English PRD requires one-glance Driving with four controls and Previous/Play-Pause/Next/Seek Back **10 seconds**, service-owned playback, no backend or streaming. Confirmed against [MVP](../MVP.md), [accepted ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [WF-07](../wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md), [HF-07](../high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.md), [existing P02 policy](ISSUE_9_PHASE2_DRIVING_STATE_AND_COMMAND_POLICY.md), [P05 recovery](ISSUE_9_PHASE5_PAUSED_NO_TRACK_RECOVERY_AUDIT.md).

## 1. Actual committed production wiring and exact ownership

| Path | P06 committed change | Safe boundary |
|---|---|---|
| `app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingCommandAdmissionPolicy.kt` **new** | Pure tap-time admission: compares the complete display source to freshly read `PlaybackGateway.connectionState` + `snapshot`, including connection, monotonic/queue revisions, queue content, active occurrence, Play/Pause state, last service error and supported controls. Returns typed P02 `PlaybackCommand` or null. | Public `playback/api` port only. No internal playback implementation, Android API, Media3 player, queue mutation or command execution |
| `app/src/main/java/com/latticeward/musiccopilot/feature/player/PlayerViewModel.kt` **narrow shared change** | Adds `drivingTransport(DrivingAction)` to already existing **single root-scoped** Player VM, reusing the existing private `submit` and `gateway.execute` ACK pipeline. Updates common submit function's command factory to nullable for fail-closed rejects; adds single active `pending` reservation and monotonically incrementing VM-local request epoch to prevent duplicate accepted taps and old response cleanup/failure from affecting a newer request. Failures are shown only while same request epoch and complete service snapshot identity/revisions still match. Success NEVER modifies local PlaybackState. | **No** changes to service Media3 runtime, its queue/focus/route policies, controller wire protocol, Player UI callbacks, notification, Bluetooth/AVRCP or AirPods #8 fixes. Shared VM change still needs regression testing at P13 |
| `app/src/main/java/com/latticeward/musiccopilot/app/navigation/AppNavHost.kt` **narrow change** | Existing single Driving NavHost destination passes `onTransport = { action -> ... playerModel.drivingTransport(action) }` to the already existing screen. Callback additionally checks *current* Driving route, Bootstrap HomeLibrary, current Granted media permission, no pending permission outcome or higher-priority Settings/Onboarding state. | Explicit touch only. Stale callback after Exit or permission loss cannot launch transport; no implicit BT wake, no second NavHost |
| `app/src/main/java/com/latticeward/musiccopilot/feature/driving/DrivingModeScreen.kt` **unchanged** | Existing four approved ≥64dp buttons now receive the actual callback; their `enabled = control.enabled && onTransport != null` uses P02 source-truth/pending/capability gate. | Mic remains visible but disabled until P07/#10; NoTrack continues to hide invalid controls, Exit remains independent |
| `app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingPresentationPolicy.kt` **unchanged** | Typed Previous/Play/Pause/Next/SeekBy(-10_000L) decision, P02 complete service state, buffering/error/capability guards and Previous/Next edges remain source authority. | No UI-owned queue or service mutation |
| `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase6DrivingCommandAdmissionTest.kt` **new** | **23** pure JUnit `@Test` source methods authored for typed commands, all P02 blocks, queue edges, position/seek, pending tap, reconnect and staleness (including changed queue/error on same revision). | **NOT EXECUTED**; runtime Play/Pause and actual sound not verified |

## 2. Four real typed manual actions: UI → existing service

| P06 user tap | Authoritative typed `PlaybackCommand` | Service-owned effect and invariant |
|---|---|---|
| Previous | `PlaybackCommand.Previous(unique CommandId)` | Existing Media3 service Previous restart/previous-item policy including its 3-second threshold; only when P02 `canSkipPrevious` authorizes; no extra queue |
| Play/Pause | `PlaybackCommand.Pause(id)` when service snapshot = Playing, else `PlaybackCommand.Play(id)` | User must physically tap; Play uses existing `PlaybackPlayOrigin.TRUSTED_PRIVATE_COMMAND` default, *not* an ambiguous system AVRCP Play-on-connect. Service still decides focus, route eligibility and ACK. No optimistic Play |
| Next | `PlaybackCommand.Next(id)` | Service-owned bounded queue skip, only when `canSkipNext` true; one command for a permitted tap |
| Seek back | `PlaybackCommand.SeekBy(id, -10_000L)` | Relative service seek/back 10 seconds with existing service/navigation clamping at zero. P02 verifies current position and canSeek; no UI clock/timer or absolute seek target |

**Full/Mini/Driving parity:** One `PlayerViewModel` and one `PlaybackGateway` instance are reused. All buttons keep the existing Media3 serialization/wire ACK path. `PlaybackControllerConnection.execute` already waits for service-side `SessionResult` with a **10-second** transport timeout (longer only for ReplaceQueue or SelectQueueIndex); it checks active controller generation and on success returns the relay's newest confirmed `PlaybackSnapshot`. It does not accept a stale Android Activity result or unsafe route truth. P06 does not change this or #8 audio safety.

## 3. Pending, double taps, stale screens and late ACKs

1. Root NavHost only wires `onTransport` while actual Driving composable exists; callback rechecks active Driving route and OS media permission at event time.
2. Shared VM's `submit` verifies `!pending`, `ready`, `hasTrack` first. Command factory then calls `DrivingCommandAdmissionPolicy.commandFor` **again** against gateway StateFlows for that exact tap, disallowing stale visible control, queue revision/contents, current MediaRef/index, stale playback mode or route error. Rejected inputs produce `null` → **zero gateway.execute calls**.
3. If accepted, generate a single unique `CommandId`; synchronously set `pending=true` before launching coroutine. P02 projects `CommandPending` and disables all four transports while the service command is outstanding. Subsequent taps fail immediately in common `submit`, even if earlier Compose callback is still queued.
4. `gateway.execute(command)` (existing service connection) is the **sole** command path. Success leads to no optimistic local state change: `PlayerUiState` snapshot collectors render actual service truth only.
5. Typed failure/timeout or exception marks `requestFailed` only if request epoch, service snapshot revision, queue revision, current occurrence and controller readiness still match its recorded request; a newer route/focus/queue observation cannot be overwritten by an old failure.
6. Finally clears `pending` **only for the matching epoch**. The existing single ViewModel already shares a pending flag across Full/Mini/Driving, making rapid duplicate UI submission fail closed.
7. **Residual physical testing risk:** controller may time out after 10 seconds while an OEM/service worker finishes a previously accepted command later. P06 does **not** promise network-grade exactly-once deduplication across a timeout/new distinct user tap; owner M14, M25–M26 and service-ACK scenarios remain required. No automatic retry is implemented. A subsequent deliberate Play/Next still requires fresh valid service state and safety gate. This risk must not be marked PASS from source inspection.

The admission policy intentionally compares *all critical observed fields* and may discard a legitimate very-recent tap while the root VM catches up to a service snapshot. This is safer than inverting stale Play/Pause or skipping the wrong queue occurrence. It does **not** queue a delayed action or replay a touch later.

## 4. Regression and privacy boundary

- No edits to `playback/api` contracts, `playback/media3`, `playback/controller`, `playback/command`, MediaSessionService/Focus/BT/headset code or the Android manifest. Existing system media-card Play and media-card deep-link, AirPods play, and disconnect-safe pause changes from Issue #8 must be retained and physically retested in P13 (M22–M26).
- No new Players, Sessions, MediaStore search, history persistence, direct seek clock, internet, wake word, microphone recording or Bluetooth auto-entry.
- P05 typed failure/pending copy already uses current `PlayerUiState.requestFailed` and `PlaybackSnapshot.lastError` and recovers without fake success.
- P1 Bluetooth suggestion remains independent/cuttable (P09–P11), P07 Mic/Voice unconnected to any fabricated STT.
- Commands use randomly generated opaque `CommandId`, without filenames, transcript, exact position, user/location or hardware identifiers in UI logs/analytics.

## 5. Tests written, not executed

**23** new JUnit pure source methods cover four command kinds; Playing→Pause and Paused→explicit private Play; 10-second relative seek and zero-position service clamp; pending double tap, three non-Ready states, stale screen reconnection; stale snapshot/queue/selected occurrence/playback mode and capability changes; empty NoTrack, Buffering, Error; edge Previous/Next; absent/invalid seek; changed queue content and route error even with an unchanged revision.

Prior P02 (**21**), P03 (**12**) and P05 (**21**) cases remain source-only. **Total Issue #9 authored pure JVM tests through P06: 77; executed: ZERO.** Device truth and runtime integrity are NOT CLAIMED.

The owner checklist [M09–M16](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md) measures actual Driving commands, rapid taps and ACK truth, and [M22–M26](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md) checks existing #8 media card/AirPods/route regressions. [M45–M46](ISSUE_9_FINAL_PHONE_QA_CHECKLIST.md) require actual stationary vehicle/#10 provider and must not be inferred PASS from mock or earbuds.

## 6. Exact phase gate

- [x] Reattached source RAR SHA-256 matches prior locked baseline.
- [x] Actual `PlayerViewModel`, `PlaybackGateway`, `PlaybackCommand`, `PlaybackControllerConnection.execute` and `PlaybackNavigationPolicy` contracts read.
- [x] Four real Driving buttons now have authenticated one-service command callback, correct typed intents and no competing player/queue.
- [x] Latest playback snapshot/connection re-read at tap, active route/permission check, disable/fail closed on stale UI or unsupported control.
- [x] Shared pending and epoch-aware ACK/error update; only service receipt changes displayed Playing/Paused/NoTrack.
- [x] 23 source JUnit cases authored, 0 run.
- [x] No #7/#8 service/media3/focus/headset/BT transport code edits; P1/Voice remain isolated.
- [ ] Kotlin/Gradle/JVM/Android/Compose instrumentation/Lint/Actions: **NOT_RUN** by explicit agreement.
- [ ] Actual command ACK, repeated-click timing, AirPods/system card/BT/focus regression and full vehicle/Voice tests: **NOT_VERIFIED**.
- [ ] Original 14 Issue #9 acceptance criteria, M01–M46 and five technical groups: **NOT_VERIFIED / NOT_RUN**.
- [ ] One cumulative APK: **NOT_BUILT** until P13.

**Disposition: P06 static code/source gate complete; P07 Voice/Mic seam NEXT. No PR, merge or Issue closure.**
