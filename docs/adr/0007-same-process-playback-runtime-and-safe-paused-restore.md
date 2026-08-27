# ADR-0007: Same-process playback runtime, structured concurrency and Safe-Paused restoration

- **Status:** Accepted
- **Date:** 2026-08-27
- **Reviewed:** 2026-08-27
- **Authority:** [PRD v1.0](../PRD.md), [MVP v1.0](../MVP.md), accepted [ADR-0002](0002-service-owned-media3-playback.md), accepted [ADR-0003](0003-local-first-data-boundaries.md)
- **Review evidence:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Phase 3 Decision/Spike Register](../architecture/DECISION_AND_SPIKE_REGISTER.md), TS-01/02/03/04/05/06/08/09
- **Relationship:** Elaborates ADR-0002 and ADR-0003. It does **not** supersede either ADR.
- **Outcome:** Accepted as the implementation contract for Issue #16 Phase 4

## Context

ADR-0002 established that Media3 playback and MediaSession are service-owned and independent of UI lifecycle, while deliberately deferring the exact service process, lifecycle, controller contract, process-death limits, coroutine ownership and command serialization to Issue #16. ADR-0003 established local-first persistence boundaries while leaving queue/position restoration details for later architecture work.

The approved User Flows, Golden Scenarios and Wireframes require P0 playback and interruption recovery to survive UI recreation/closure, synchronize with Android system surfaces, avoid duplicate player ownership and return safely after calls, Audio Focus changes and route loss. FR-08 queue/position restoration remains P1 and must not be promoted into a hidden P0 requirement.

Phase 3 closed the implementation-blocking runtime decisions D-01 through D-08 and D-15 using the accepted spike evidence. Those choices are consequential because changing any of them would alter process topology, service lifetime, state authority, command ordering, cancellation behavior, recovery semantics or persistence contracts.

## Decision drivers

- Preserve one authoritative Player/MediaSession state independent of UI lifecycle
- Keep the ten-week MVP proportional and avoid unnecessary IPC/process isolation
- Make UI/service reconnection deterministic after recreation
- Serialize all playback mutation from UI, system and Voice entry points
- Ensure long-running work is not accidentally owned by transient UI scopes
- Make interruption/route recovery fail safe rather than auto-resume optimistically
- Keep FR-08 restoration explicitly P1 and Safe-Paused
- Make runtime behavior testable with injected dispatchers, fake controllers and deterministic policies

## Decision

### 1. Process topology

Run the production playback service in the **same Android application process** as the rest of the app. Do not configure `android:process` for the playback service.

UI lifecycle independence is provided by `MediaSessionService` ownership, not by a second process. No custom Binder/IPC protocol or cross-process state replication is introduced for MVP v1.0.

### 2. Service startup, foreground lifetime and shutdown

- Do not autoplay at boot or ordinary app startup.
- Create the Player and MediaSession in service `onCreate` when the service is legitimately created by app/system media interaction.
- Enter the foreground-media lifecycle only from an accepted user/system Play or Resume path, following Media3/platform requirements.
- On task removal, keep the service only while playback is legitimately ongoing; otherwise stop it.
- Explicit Stop, a deliberately cleared queue, or a terminal unrecoverable playback state may stop the service.
- Release Player, MediaSession and service-owned resources in `onDestroy`.

### 3. UI/controller connection

Provide one application-scoped `PlaybackControllerConnection` that exposes these connection states:

- `Disconnected`
- `Connecting`
- `Ready`
- `RecoverableError`

The connection is asynchronous. On reconnect, replace the complete observed `PlaybackSnapshot` atomically before publishing `Ready`. Screens and ViewModels observe snapshots and send commands only; they never construct ExoPlayer, MediaSession or an authoritative queue.

Android/system/external MediaController clients remain supported by the service-owned MediaSession contract.

### 4. Process-death boundary

P0 guarantees UI recreation/closure and active service continuity within supported Android lifecycle behavior. P0 does **not** guarantee survival after process death or force-stop.

On cold start, a live MediaSession always wins over persisted restoration data. Force-stop is treated as a negative control, not a P0 restoration requirement.

### 5. Playback command serialization

All playback commands from UI, Android system controls and Voice converge on one service-owned `PlaybackCommandProcessor`.

- One consumer mutates the Player on the application/player looper using Main/Main.immediate as appropriate.
- Commands carry an ID and are processed sequentially.
- Parallel Player mutation is forbidden.
- Optimistic success is forbidden.
- Completion is acknowledged by authoritative state/snapshot change or a typed rejection/timeout.
- The command channel is bounded at **32** pending commands.
- Overflow returns a typed `Busy` failure; an accepted command is never silently dropped.

### 6. Structured concurrency and dispatchers

- `GlobalScope` is forbidden.
- Dispatchers and long-lived scopes are injected to keep tests deterministic.
- UI destination work uses `viewModelScope` and may be cancelled with the destination.
- Playback uses a service-owned `SupervisorJob` scope; Player/MediaSession access remains on the application/player looper.
- Voice uses one child session job per active Voice generation; starting a replacement session or receiving an invalidating event cancels the previous session.
- Library/repository work uses application/repository-owned scope when it must outlive a screen; it is restartable after process death.
- `Dispatchers.IO` boundary: MediaStore, Room, DataStore and blocking provider/storage adapters.
- `Dispatchers.Default` boundary: normalization, deterministic matching and CPU-bound pure transformations.
- Main boundary: UI state publication and Player/MediaSession mutation.

### 7. Voice overlap and cancellation

`VoiceSessionCoordinator` is the sole owner of an active Voice session and pending confirmation. Exactly one generation token may be active.

Starting a new Voice session cancels STT, TTS, timeout and ducking work belonging to the previous generation. Call/focus/route loss, Driving exit, Back, permission loss and invalidated UI origin cancel the active session. Callbacks carrying stale generation tokens are ignored.

A playback command must be authoritatively acknowledged before success TTS. Failure of TTS after playback success does not roll playback back.

### 8. Safe-Paused P1 queue/position restoration

Persist a versioned `RestorableQueueSnapshot` in **Proto DataStore** as a non-authoritative P1 restoration aid. It is not the live queue source of truth.

The snapshot contains only:

- schema version
- queue revision
- ordered stable MediaStore re-query references
- current index
- non-negative playback position
- capture time/state needed for validation

It must not contain title, artist, filesystem path or other personal media metadata not required for restoration.

Write atomically when the queue/current item/pause state changes and with a debounced checkpoint of **5 seconds** while active. On cold restore:

1. prefer a live MediaSession if present;
2. reject corrupt or unsupported future schema;
3. re-query MediaStore and drop invalid/missing items;
4. clamp position to verified media duration;
5. prepare the restored queue in **Paused** state only;
6. never autoplay merely because a snapshot exists.

### 9. Interruption, focus and route recovery

Automatic resume is allowed only when **all** guards remain true:

1. playback was Playing before the transient interruption;
2. Audio Focus has returned;
3. the audio-route generation is unchanged and valid;
4. no manual Pause occurred after the interruption began.

Permanent focus loss safe-pauses. Headset/Bluetooth route disconnect immediately pauses playback, cancels Voice and increments route generation. Reconnect remains Paused until explicit Resume. Duplicate or stale interruption/route events must be idempotent.

## Consequences

### Positive

- Service-owned playback remains independent of screen lifecycle without cross-process IPC complexity.
- UI, system controls and Voice converge on one command authority and one observable truth.
- Command ordering, cancellation and stale-callback behavior become deterministic and directly testable.
- Route/call recovery cannot silently resume onto an invalid output or override a manual pause.
- P1 restoration is useful after cold start without creating autoplay or privacy regressions.
- The architecture remains proportional to a ten-week MVP.

### Costs and risks

- A same-process service does not survive application process death by isolation; this is an explicit non-goal for P0.
- Service/controller reconnection and bounded command acknowledgement require careful integration tests.
- Proto DataStore schema/version migration becomes a maintained contract once released.
- OEM background, Audio Focus, telephony callback ordering and audible route behavior still require downstream physical validation.
- Bounded command capacity means callers must surface/recover from `Busy` rather than assume infinite buffering.

## Alternatives considered

- **Dedicated playback process:** rejected for MVP because it adds IPC, state replication, DI and debugging complexity without an approved product requirement for process isolation.
- **UI-owned Player or queue:** rejected by ADR-0002 and the P0 background/system-control requirements.
- **Unbounded or parallel playback commands:** rejected because order races can create duplicate/stale playback behavior.
- **`GlobalScope` for long-running work:** rejected because lifecycle ownership and cancellation become implicit and hard to test.
- **Room as active queue authority:** rejected because live playback authority belongs to the MediaSession service and Room is not required for transient queue mutation.
- **Automatic resume after any reconnect/focus gain:** rejected because it can violate manual pause and route-safety expectations.
- **Restore and autoplay after cold start:** rejected because FR-08 is P1 and a persisted snapshot is not proof of current user intent.
- **Store title/artist/path in restoration payload:** rejected as unnecessary personal media metadata.

## Validation

### Architecture/contract validation

- Manifest contains no playback `android:process` override.
- Exactly one production Player/MediaSession instance is created by the service boundary.
- UI/ViewModels depend on controller/port contracts, not ExoPlayer/MediaSession concrete instances.
- Playback command processor proves sequential ordering, acknowledgement and typed overflow behavior.
- Dispatcher/scope injection tests prove no UI scope owns playback, library scan or other required long-running work.
- Voice generation-token tests prove cancellation and stale callback suppression.
- Restoration tests prove corrupt/future-schema rejection, MediaStore revalidation, invalid-item dropping, position clamping and Paused-only restore.
- Interruption reducer tests prove manual-pause-wins, route-generation guarding and idempotence.

### Downstream evidence still required

- Issue #4: production MediaSessionService/controller integration, process recreation, command ordering/overflow, cancellation, Proto DataStore migration and restoration tests.
- Issue #13: physical Android/OEM, phone-call, Bluetooth/headset and vehicle/head-unit behavior, including audible no-speaker-leak validation.
- Provider/legal/field activation remains governed by ADR-0004/0006 and downstream Voice/Privacy/QA gates.

## Supersession rule

This ADR is binding for MVP v1.0. A future move to a dedicated process, materially different service lifetime, different command-concurrency model, or authoritative restoration model requires a new ADR that explicitly **Supersedes ADR-0007**. ADR-0002 and ADR-0003 remain Accepted and unchanged.
