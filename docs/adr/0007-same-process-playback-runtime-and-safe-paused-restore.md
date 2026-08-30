# ADR-0007: Same-process playback runtime, structured concurrency and Safe-Paused restoration

- **Status:** Accepted
- **Date:** 2026-08-27
- **Reviewed:** 2026-08-27
- **Source revalidation:** 2026-08-27 against the directly attached English `MSM2(2).rar`
- **Authority:** attached English `Music_Copilot_PRD_v1_EN_Clarified.pdf`, attached English `Music_Copilot_MVP_v1_EN.pdf`, current Accepted [ADR-0002](0002-service-owned-media3-playback.md) and [ADR-0003](0003-local-first-data-boundaries.md)
- **Review evidence:** [Issue #16](https://github.com/latticeward-labs/music-copilot/issues/16), [Source/Authority Register](../architecture/SOURCE_AND_AUTHORITY_REGISTER.md), [Phase 3 Decision/Spike Register](../architecture/DECISION_AND_SPIKE_REGISTER.md), TS-01/02/03/04/05/06/08/09
- **Relationship:** Elaborates ADR-0002 and ADR-0003. It does **not** supersede either ADR.
- **Outcome:** Accepted as the implementation contract for Issue #16 Phase 4

## Attached-source identity and boundary

This ADR was revalidated directly against the attached archive bytes supplied for Issue #16 Phase 4:

- `MSM2(2).rar` SHA-256: `0618cf20f1d7bd596ab744c1fa7b19bf257982f538e21670d11f15de0b029eb8`
- `Music_Copilot_PRD_v1_EN_Clarified.pdf`: `486fbd5057e4abf570cd1a4865b6243ee5e31d26a48596414903916d6d81e9cf`
- `Music_Copilot_MVP_v1_EN.pdf`: `98e89a1c9d530f4eb878af94f4f480fc8d4669ce2ed5193ba71f201b032d47b0`
- `ADR-0002_Service_Owned_Media3_Playback.pdf`: `f9ba6adfac14d5c04a33de55cf845abc56dee06ea0301ae7e66f926e83e729c9`
- `ADR-0003_Local_First_Data_Boundaries_Revised.pdf`: `1ceff4cd1f8d4021dd0e1c5ea3aec91c46b0b121d3f87919f913f3f53ce644a4`
- `Music_Copilot_System_Architecture_MVP_v1_EN.pdf`: `b056e3b8d3125e6660c2fd9431f4f883128673ed5ad1a7f3f5184dd179715bfb`
- `Music_Copilot_Code_and_Technical_Documentation_MVP_v1_EN.pdf`: `1d776396a88aebacbdb8b813fe8c6fe82418c951f301762ac594c55a7cd22cf3`

No Persian/Farsi PRD or MVP is used as authority or evidence in this ADR.

The attached English sources establish the binding direction but intentionally leave several final choices open. In particular, the English System Architecture identifies exact coroutine scopes/dispatchers, process-death behavior, queue/position representation and same-process versus separate-process service as Issue #16 decisions; the English Technical Blueprint repeats those as final-gate decisions. Therefore the detailed choices below are not presented as if they came from the older PDF synthesis. They are the current GitHub Phase 3 resolutions, checked for compatibility with the attached English sources and then formalized here.

## Context

The attached English PRD establishes that playback must continue in the background and with the screen off, the system Media Session must remain current, interruption/route behavior must recover safely, and FR-08 queue/position persistence is P1. Its proposed architecture also states that the player is independent of UI lifecycle and managed in a Service.

The attached English MVP requires stable background/lock-screen/Bluetooth playback, shows that UI rotation/closure must leave Player state synchronized with the Service, and keeps incomplete P0 Player/Voice/Driving/Stability work as a Beta blocker.

ADR-0002 established that Media3 playback and MediaSession are service-owned and independent of UI lifecycle, while deliberately deferring exact service process, controller contract, process-death limits, coroutine ownership and command serialization to Issue #16. ADR-0003 established local-first persistence boundaries while leaving exact queue/position restoration mechanics open.

The approved User Flows, Golden Scenarios and Wireframes require P0 playback and interruption recovery to survive UI recreation/closure, synchronize with Android system surfaces, avoid duplicate player ownership and return safely after calls, Audio Focus changes and route loss. FR-08 queue/position restoration remains P1 and must not be promoted into a hidden P0 requirement.

Phase 3 closed the implementation-blocking runtime decisions D-01 through D-08 and D-15 using the accepted repository evidence. Those choices are consequential because changing any of them would alter process topology, service lifetime, state authority, command ordering, cancellation behavior, recovery semantics or persistence contracts.

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

This resolves an explicit open decision in the attached English System Architecture/Technical Blueprint; it does not rewrite ADR-0002.

### 2. Service startup, foreground lifetime and shutdown

- Do not autoplay at boot or ordinary app startup.
- Create the Player and MediaSession in service `onCreate` when the service is legitimately created by app/system media interaction.
- Enter the foreground-media lifecycle only from an accepted user/system Play or Resume path, following the approved service contract.
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

This preserves the attached-source distinction between P0 active playback continuity and P1 FR-08 restoration.

### 5. Playback command serialization

All playback commands from UI, Android system controls and Voice converge on one service-owned `PlaybackCommandProcessor`.

- One consumer mutates the Player on the application/player looper using Main/Main.immediate as appropriate.
- Commands carry an ID and are processed sequentially.
- Parallel Player mutation is forbidden.
- Optimistic success is forbidden.
- Completion is acknowledged by authoritative state/snapshot change or a typed rejection/timeout.
- The command channel is bounded at **32** pending commands.
- Overflow returns a typed `Busy` failure; an accepted command is never silently dropped.

The exact bounded capacity is a Phase 3 repository decision, not a numeric claim from the attached PDFs.

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

The attached English architecture requires structured concurrency, no unmanaged global scope, service-owned long-running playback and no blocking scan/database work on main; it intentionally leaves these exact scope/dispatcher assignments to Issue #16.

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

The attached English sources require FR-08 restoration to remain P1 and explicitly leave the representation/restoration limits open. Proto DataStore, the snapshot schema and checkpoint policy are therefore current Issue #16 decisions, not historical-source facts.

### 9. Interruption, focus and route recovery

Automatic resume is allowed only when **all** guards remain true:

1. playback was Playing before the transient interruption;
2. Audio Focus has returned;
3. the audio-route generation is unchanged and valid;
4. no manual Pause occurred after the interruption began.

Permanent focus loss safe-pauses. Headset/Bluetooth route disconnect immediately pauses playback, cancels Voice and increments route generation. Reconnect remains Paused until explicit Resume. Duplicate or stale interruption/route events must be idempotent.

The attached English MVP requires immediate pause on Bluetooth disconnect and no unintended speaker playback; the exact post-call/focus resume guards were an explicit Issue #16 policy decision and are formalized here from the repository evidence.

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

- **Dedicated playback process:** rejected for MVP because it adds IPC, state replication, dependency-wiring and debugging complexity without an approved product requirement for process isolation.
- **UI-owned Player or queue:** rejected by ADR-0002 and the P0 background/system-control requirements.
- **Unbounded or parallel playback commands:** rejected because order races can create duplicate/stale playback behavior.
- **Unmanaged global coroutine scope for long-running work:** rejected because lifecycle ownership and cancellation become implicit and hard to test.
- **Room as active queue authority:** rejected because live playback authority belongs to the MediaSession service and Room is not required for transient queue mutation.
- **Automatic resume after any reconnect/focus gain:** rejected because it can violate manual pause and route-safety expectations.
- **Restore and autoplay after cold start:** rejected because FR-08 is P1 and a persisted snapshot is not proof of current user intent.
- **Store title/artist/path in restoration payload:** rejected as unnecessary personal media metadata.

## Validation

### Source-conformance validation

- Attached English PRD/MVP scope remains Android/local/Push-to-Talk and does not gain backend, account, streaming or new P0 restoration scope.
- ADR-0002 remains the service-owned playback authority; ADR-0003 remains the local-first data authority.
- P0 active playback continuity remains distinct from P1 queue/position restoration.
- Decisions that the attached System Architecture/Technical Blueprint marked open are explicitly identified as current Issue #16 decisions rather than attributed back to those PDFs.

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
