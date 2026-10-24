# Issue #9 — P02 Driving presentation-state machine and command policy

**Date:** 2026-10-24 UTC  
**Gate:** `PASS_SCOPED_CODE_AND_AUTHORED_TEST_SOURCE_ONLY` **(static code and source registration; no execution)**.  
**Prior phase:** P00 source/DoR audited; P01 GitHub contract + traceability completed.  
**Execution branch:** `feature/9-driving-mode-bluetooth-suggestion`; `main` baseline at P02 start still based on `674d0cf19a897dad44b59729fb0afa2c635d802d`.  
**Source:** User-attached `MSM2(1)(1).rar` SHA-256 `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8` (12 English PDFs), [PRD](../PRD.md) FR-04/FR-07, [MVP](../MVP.md) Driving P0 and selected BT Hint P1; [ADR-0002](../adr/0002-service-owned-media3-playback.md), [ADR-0007](../adr/0007-same-process-playback-runtime-and-safe-paused-restore.md), [ADR-0008](../adr/0008-minimal-modular-monolith-and-dependency-enforcement.md), [Runtime Policies](../architecture/RUNTIME_POLICIES.md), [UF-07 / UF-11](../user-flows/CLUSTER_C_STATE_INVENTORY.md), [WF-07 Playing](../wireframes/app-states/WF-07_DRIVING_MODE_PLAYING.md), [WF-07 Paused](../wireframes/app-states/WF-07_DRIVING_MODE_PAUSED.md), [WF-07 No Track](../wireframes/app-states/WF-07_DRIVING_MODE_NO_TRACK.md), [HF-07](../high-fidelity/WF-07/HF-07_DRIVING_MODE_PLAYING.md), [Screen handoff](../handoff/SCREEN_SPECS.md).

## 1. Actually committed P02 source

1. **Pure production state/action projector:** `app/src/main/java/com/latticeward/musiccopilot/core/application/driving/DrivingPresentationPolicy.kt`. Models:
   - `DrivingViewState`: `Playing`, `Paused`, `NoTrack`;
   - `DrivingStatusNotice`: `None`, `Connecting`, `Disconnected`, `ControllerError`, `Buffering`, `PlaybackError`, `CommandPending`, `Interruption`;
   - `DrivingAction`: fixed `Previous`, `PlayPause`, `Next`, `SeekBack`;
   - `DrivingActionLabel`: `Previous`, `Play`, `Pause`, `Next`, `SeekBack10Seconds`;
   - `DrivingDisabledReason`: `NoCurrentTrack`, `ControllerNotReady`, `PlaybackUnavailable`, `CommandPending`, `Unsupported`;
   - `DrivingMicCapability`: `Unavailable` (default), `Available` (explicit future Voice seam only);
   - `DrivingPresentationState`: status, notice, four fixed controls, metadata-access hint, mic enabled and unconditional Exit.
2. **Pure typed command decision:** `DrivingPresentationPolicy.commandFor(connection, snapshot, pending, action, CommandId)` returns an *approved intent object* or `null` (does NOT execute it). It selects only `PlaybackCommand.Previous`, `Play`, `Pause`, `Next`, `SeekBy(-10_000L)`. No `ReplaceQueue`, `Stop`, `SeekTo`, MediaStore, Media3, Android APIs or Activity calls.
3. **Authored, unexecuted test source:** `app/src/test/java/com/latticeward/musiccopilot/foundation/Issue9Phase2DrivingPolicyTest.kt` — **21** JUnit test methods. `@Test` methods are source code only; none has been run.

The policy consumes only **public** `playback/api` `PlaybackSnapshot`, `PlaybackConnectionState`, `PlaybackCommand` and `core` types. An initial reference to internal `playback.command.PlaybackNavigationPolicy` was removed before marking P02 complete because ADR-0008 allows application-to-public-port dependencies, not importing playback implementation. The approved seek interval is identical to the existing `PlaybackNavigationPolicy.VOICE_SEEK_BACK_MS = 10_000L`; P06/P12 must check parity on source review. No source files from #7/#8 Playback/Media3 implementations were modified.

## 2. Truth-preserving projection matrix

Snapshot is valid **only if** its non-null `currentIndex` indexes its **own** `queue` and `queue[currentIndex] == currentItem`. The service projector currently creates precisely that contract. Inconsistent/stale/empty selection is **NoTrack** and all four transport commands are disabled; a disconnected cached Playing snapshot cannot prove ongoing audible audio.

| Gateway connection | Valid current item? | Service `PlaybackState` | Projected Driving state | Notice | Transport |
|---|---|---|---|---|---|
| Ready | yes | Playing | **Playing** | None / typed interruption if present | Capabilities enabled |
| Ready | yes | Paused | **Paused** | None / typed interruption if present | Capabilities enabled, Play requires explicit user action |
| Ready | yes | Idle or Ended | **Paused** | None | Capabilities enabled, no automatic resume |
| Ready | yes | Buffering | **Paused** | Buffering | All disabled until real service snapshot changes |
| Ready | yes | Error | **Paused** | PlaybackError | All disabled, Exit always enabled |
| Ready | no (including malformed queue/item pair) | ANY | **NoTrack** | Applicable connection/service notice | All disabled |
| Connecting | yes | Playing (stale) | **Paused** | Connecting | All disabled |
| Disconnected | yes | Playing (stale) | **Paused** | Disconnected | All disabled |
| RecoverableError | yes | ANY | **Paused** | ControllerError | All disabled |
| Any non-Ready | no | ANY | **NoTrack** | Connection notice | All disabled |
| Ready | yes | ANY, pending=true | Service-derived Playing/Paused | CommandPending (unless higher-priority service error) | All disabled; Exit enabled |

- **Transition authority:** Source state is recalculated only from the latest *complete service snapshot* and controller connection, never a UI flag, saved navigation, metadata lookup, route callback, fake timer or optimistic command response. P06 must collect read-backed state via existing `PlayerViewModel`/`PlaybackGateway` and protect out-of-order ACKs.
- **Status safety:** `PlaybackState.Buffering` is *not* a claim of audio playing. For a valid cached Playing snapshot while controller is not Ready, show Paused + explicit connection notice; do not show false audible Playing. Paused/NoTrack are distinct from an error reason.
- **Error contract:** `snapshot.lastError is AppError.Interruption` produces a non-color-only semantic `Interruption` notice; the policy does **not** guess that Bluetooth/headset/call route has become safe, or override #8 service guards. A later explicit Play request must still be ACKed/rejected by the service.
- **Optional last track:** not inferred from saved UI/queue. P05 may expose a single verified, permission-rechecked MediaStore reference only when actually available; P02 creates no fake Play Last.

## 3. Fixed transport policy: event-to-intent and disabled rules

| Button / label | Enabled only if | Pure returned intent | Source of actual effect |
|---|---|---|---|
| Previous | Ready, valid current item, !pending, not Error/Buffering, `canSkipPrevious` | `PlaybackCommand.Previous(commandId)` | Existing service Previous restart/previous-item policy |
| Play/Pause | Ready, valid current item, !pending, not Error/Buffering | `PlaybackCommand.Pause` if service Playing, otherwise `PlaybackCommand.Play` | Existing service focus/route eligibility + ACK; no implicit autoplay |
| Next | Ready, valid current item, !pending, not Error/Buffering, `canSkipNext` | `PlaybackCommand.Next(commandId)` | Existing service queue/edge policy; no wrap or duplicate player |
| Seek back | Ready, valid current item, !pending, not Error/Buffering, `canSeek`, nonnegative position, valid duration if present | `PlaybackCommand.SeekBy(commandId, -10_000L)` | Existing service clamps to >=0 and supported track position |

P02 does not create a long-press override, queue manager, own Player, MediaSession, service, lifecycle resume, route monitor, phone key handler, Bluetooth receiver, SpeechRecognizer or phone UI. A `null` intent means **no command**. The registered command has a distinct `CommandId` passed by P06; policy itself does not allocate/run work or report success. Command pending prevents optimistic duplicated UI submit **within the projected state**; the future P06 source must still guard rapid callbacks/serialization using the actual pending state and service acknowledgement. **Exit remains enabled regardless of failure/pending/connection** and is implemented in P03.

## 4. Voice, Metadata, Privacy, Accessibility and P1 boundaries

- **Mic:** `DrivingMicCapability` is an explicit caller input; defaults to Unavailable; `project` never starts Voice. P07 checks actual microphone permission, replaceable provider, VoiceSessionCoordinator and #10 runtime; no fake "Listening"/successful STT.
- **Metadata:** the policy exposes only a `showCurrentTrackMetadata` *eligibility boolean*, not track name, artist, path, URI, Bluetooth ID or a stored `MediaRef`. P04/P05 display only current validated service item via existing permission-controlled library lookup; avoid stale artist/title after skips/revocation.
- **Accessibility:** immutable ordered four controls with typed labels and disabled reasons; P04/P08 render 64dp+ transports, 72dp Mic, 56dp Exit, ordered TalkBack, readable 200% reflow, no keyboard/long lists.
- **P1 BT:** no dependencies on `BluetoothAdapter`, DataStore or route broadcasts exist in the P0 projector. P09–P11 can be removed without changing `DrivingPresentationPolicy`. Connection-eligibility is not inferred from an audio route; no automatic Driving entry.
- **No network/account/streaming, no Android Auto, no UI-owned Media3, no raw audio/transcript/file metadata in analytics.** No app manifest, permissions or service source changed in P02.

## 5. Authored JUnit coverage (NOT EXECUTED)

**All 21 cases in one file**, covering: confirmed Playing and Pause label; Paused/Ended/Idle readback; empty NoTrack and unavailable transports; mismatched/out-of-range/missing queue index fail-closed; disconnected cached Playing; connecting/recoverable notice; pending/double-submit prevention; Buffering and Error safe presentation; exact 4-button order; queue boundary availability; canSeek/invalid position; typed Previous/Next/Pause; explicit private Play; seek-back -10s and zero-position clamping delegated to service; mic capability independent of track; transition only after a later service Playing snapshot.

**Honest results:** JUnit 0 executed; Android/instrumented 0 executed; Lint 0; Gradle compile 0; Actions 0; device/vehicle 0; APK 0. A source readback validates files exist, not Kotlin compilation or behavior. Do not label tests PASS or original ACs verified.

## 6. Explicit remaining work and P03 handoff

| Remaining work | Owner phase | Status now |
|---|---|---|
| Single NavHost Driving destination + manual Library/Player entry; recorded origin, Back/Exit, on-recreation truth | P03 | NOT_IMPLEMENTED |
| Real WF-07/HF-07 Driving Playing Compose and typography | P04 | NOT_IMPLEMENTED |
| Real Paused/NoTrack/metadata validated/recovery UI and optional Play Last | P05 | NOT_IMPLEMENTED |
| PlaybackGateway execute/ACK using real command ID, pending and error UI; parity with shared FullPlayer, route safety | P06 | NOT_IMPLEMENTED |
| Truthful Mic Voice origin overlay/handoff (live STT in #10) | P07/#10 | NOT_IMPLEMENTED |
| TalkBack/reflow/instrumentation test source | P08/P12 | NOT_IMPLEMENTED |
| P1 selected paired BT settings/permission, connection eligibility/dedupe, voluntary suggestion | P09–P11 | NOT_IMPLEMENTED |
| Cumulative integration and final **single** build-only APK plus owner M01–M46 | P12–P13 | NOT_DONE |

### P02 static gate/Definition of Done

- [x] Source RAR and repository P00/P01 references matched, same branch reused.
- [x] Actual public PlaybackSnapshot, Connection, Commands and projector semantics reviewed.
- [x] Pure Kotlin three-state projector, reason/disabled state and four fixed ordered controls written.
- [x] Typed side-effect-free command mapping written, including explicit -10s seek.
- [x] Error, pending, reconnect and inconsistent queue fail-closed decisions defined.
- [x] Voice capability remains explicit; Exit always available; P1 disconnected from P0.
- [x] Public-port package boundary corrected before handoff.
- [x] 21 descriptive JUnit methods authored, left **NOT_RUN**.
- [ ] Android/Gradle/real playback/Voice/BT/device validation — **not authorized**.
- [ ] Original 14 Issue #9 product ACs — **NOT_VERIFIED**.

**Disposition:** P02 source authoring and static contract **COMPLETE; P03 NEXT**. No intermediate Build/Actions/APK, PR/merge or Issue closure. Entire application is built once in P13 after P00–P12 source integration.
